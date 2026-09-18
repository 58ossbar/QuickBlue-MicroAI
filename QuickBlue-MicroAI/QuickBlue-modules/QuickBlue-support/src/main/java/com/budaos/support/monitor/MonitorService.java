package com.budaos.support.monitor;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 服务监控服务
 *
 * <p>通过 Nacos 服务发现获取所有微服务实例，调用各实例的 Actuator 端点
 * （health / metrics）聚合运行状态与 JVM 指标。</p>
 *
 * @author budaos
 */
@Slf4j
@Service
public class MonitorService {

    /** 健康状态 */
    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String STATUS_UNKNOWN = "UNKNOWN";

    /** 指标统计类型 */
    private static final String STATISTIC_VALUE = "VALUE";
    private static final String STATISTIC_COUNT = "COUNT";

    /** Actuator 指标名称 */
    private static final String METRIC_MEMORY_USED = "jvm.memory.used";
    private static final String METRIC_MEMORY_MAX = "jvm.memory.max";
    private static final String METRIC_THREADS_LIVE = "jvm.threads.live";
    private static final String METRIC_THREADS_PEAK = "jvm.threads.peak";
    private static final String METRIC_PROCESS_UPTIME = "process.uptime";
    private static final String METRIC_HTTP_REQUESTS = "http.server.requests";

    /** Actuator 端点路径 */
    private static final String ACTUATOR_HEALTH = "/actuator/health";
    private static final String ACTUATOR_METRICS = "/actuator/metrics/";

    /** Actuator 探测线程池大小 */
    private static final int PROBE_THREAD_POOL_SIZE = 8;

    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private RestTemplate restTemplate;

    /** Actuator 端点探测线程池 */
    private final ExecutorService probeExecutor = Executors.newFixedThreadPool(PROBE_THREAD_POOL_SIZE, r -> {
        Thread t = new Thread(r, "monitor-probe");
        t.setDaemon(true);
        return t;
    });

    /** QPS 采样缓存：serviceName -> 上次采样 */
    private final ConcurrentHashMap<String, RequestSample> qpsCache = new ConcurrentHashMap<>();

    /**
     * 服务监控概览：聚合所有注册服务的健康状态与 JVM 指标
     */
    public Map<String, Object> getOverview() {
        List<Map<String, Object>> services = new ArrayList<>();
        int upCount = 0;
        int downCount = 0;
        long totalMemoryUsed = 0;
        long totalMemoryMax = 0;
        long totalRequests = 0;

        List<String> serviceNames = discoveryClient.getServices().stream().sorted().collect(Collectors.toList());
        for (String serviceName : serviceNames) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (instances == null || instances.isEmpty()) {
                continue;
            }

            ServiceStat stat = aggregateServiceStatus(serviceName, instances);
            services.add(stat.toMap());

            if (STATUS_UP.equals(stat.status)) {
                upCount++;
            } else {
                downCount++;
            }
            totalMemoryUsed += stat.memoryUsedBytes;
            totalMemoryMax += stat.memoryMaxBytes;
            totalRequests += stat.totalRequests;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCount", services.size());
        result.put("upCount", upCount);
        result.put("downCount", downCount);
        result.put("totalMemoryUsed", totalMemoryUsed);
        result.put("totalMemoryMax", totalMemoryMax);
        result.put("totalRequests", totalRequests);
        result.put("timestamp", System.currentTimeMillis());
        result.put("services", services);
        return result;
    }

    /**
     * 指定服务详细指标（用于详情抽屉）
     */
    public Map<String, Object> getMetrics(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances == null || instances.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("error", "服务未注册或已下线: " + serviceName);
            return result;
        }
        ServiceInstance inst = instances.get(0);
        String baseUrl = instanceBaseUrl(inst);

        // 并行探测各指标，避免串行阻塞
        CompletableFuture<Double> memUsedF = asyncMetric(baseUrl, METRIC_MEMORY_USED);
        CompletableFuture<Double> memMaxF = asyncMetric(baseUrl, METRIC_MEMORY_MAX);
        CompletableFuture<Double> threadLiveF = asyncMetric(baseUrl, METRIC_THREADS_LIVE);
        CompletableFuture<Double> threadPeakF = asyncMetric(baseUrl, METRIC_THREADS_PEAK);
        CompletableFuture<Double> uptimeF = asyncMetric(baseUrl, METRIC_PROCESS_UPTIME);
        CompletableFuture<Double> requestF = asyncMetric(baseUrl, METRIC_HTTP_REQUESTS, STATISTIC_COUNT);

        double totalRequests = requestF.join();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("memory", asValueMap(memUsedF.join(), memMaxF.join()));
        result.put("threads", asValueMap(threadLiveF.join(), threadPeakF.join()));
        result.put("uptime", valueItem(uptimeF.join()));
        result.put("totalRequests", (long) totalRequests);
        result.put("qps", computeQps(serviceName, (long) totalRequests));
        return result;
    }

    /**
     * 聚合单个服务的所有实例探测结果
     */
    private ServiceStat aggregateServiceStatus(String serviceName, List<ServiceInstance> instances) {
        List<InstanceHealth> healthList = probeInstances(instances);
        ServiceInstance first = instances.get(0);

        ServiceStat stat = new ServiceStat();
        stat.serviceName = serviceName;
        stat.host = first.getHost();
        stat.port = first.getPort();

        boolean allUp = true;
        for (InstanceHealth health : healthList) {
            allUp = allUp && STATUS_UP.equalsIgnoreCase(health.status);
            stat.memoryUsedBytes += health.memoryUsed;
            stat.memoryMaxBytes += health.memoryMax;
            stat.totalRequests += health.totalRequests;
            if (health.error != null) {
                stat.error = health.error;
            }
            if (!health.components.isEmpty()) {
                stat.details = health.components;
            }
        }
        stat.status = allUp ? STATUS_UP : STATUS_DOWN;
        return stat;
    }

    /**
     * 并行探测服务全部实例
     */
    private List<InstanceHealth> probeInstances(List<ServiceInstance> instances) {
        return instances.stream()
                .map(inst -> CompletableFuture.supplyAsync(() -> probeInstance(inst), probeExecutor))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * 探测单实例：健康状态 + 内存 + 请求量
     */
    private InstanceHealth probeInstance(ServiceInstance inst) {
        InstanceHealth health = new InstanceHealth();
        String baseUrl = instanceBaseUrl(inst);
        health.components = fetchHealthComponents(baseUrl, health);
        health.memoryUsed = (long) queryMetric(baseUrl, METRIC_MEMORY_USED);
        health.memoryMax = (long) queryMetric(baseUrl, METRIC_MEMORY_MAX);
        health.totalRequests = (long) queryMetric(baseUrl, METRIC_HTTP_REQUESTS, STATISTIC_COUNT);
        return health;
    }

    /**
     * 拉取健康状态详情，同时回填实例整体状态
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchHealthComponents(String baseUrl, InstanceHealth health) {
        try {
            Map<String, Object> body = getForMap(baseUrl + ACTUATOR_HEALTH);
            if (body != null) {
                health.status = String.valueOf(body.getOrDefault("status", STATUS_UNKNOWN));
                Object components = body.get("components");
                if (components instanceof Map) {
                    return (Map<String, Object>) components;
                }
                Object details = body.get("details");
                if (details instanceof Map) {
                    // Spring Boot 2.x 兼容
                    return (Map<String, Object>) details;
                }
            }
        } catch (Exception e) {
            health.status = STATUS_DOWN;
            health.error = e.getMessage();
            log.debug("健康检查失败: {} - {}", baseUrl, e.getMessage());
        }
        return Collections.emptyMap();
    }

    private String instanceBaseUrl(ServiceInstance inst) {
        return "http://" + inst.getHost() + ":" + inst.getPort();
    }

    /** 查询指标端点，取 VALUE 统计值 */
    private double queryMetric(String baseUrl, String metricName) {
        return queryMetric(baseUrl, metricName, STATISTIC_VALUE);
    }

    /** 查询指标端点，取指定统计值 */
    private double queryMetric(String baseUrl, String metricName, String statistic) {
        try {
            Map<String, Object> body = getForMap(baseUrl + ACTUATOR_METRICS + metricName);
            if (body == null) {
                return 0;
            }
            Object measurements = body.get("measurements");
            if (!(measurements instanceof List)) {
                return 0;
            }
            for (Object item : (List<?>) measurements) {
                if (item instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) item;
                    if (statistic.equals(String.valueOf(m.get("statistic")))) {
                        Object value = m.get("value");
                        return value instanceof Number ? ((Number) value).doubleValue() : 0;
                    }
                }
            }
        } catch (Exception e) {
            // 指标不存在或调用失败时返回 0
            log.debug("查询指标失败: {} - {}", metricName, e.getMessage());
        }
        return 0;
    }

    private CompletableFuture<Double> asyncMetric(String baseUrl, String metricName) {
        return CompletableFuture.supplyAsync(() -> queryMetric(baseUrl, metricName), probeExecutor);
    }

    private CompletableFuture<Double> asyncMetric(String baseUrl, String metricName, String statistic) {
        return CompletableFuture.supplyAsync(() -> queryMetric(baseUrl, metricName, statistic), probeExecutor);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getForMap(String url) {
        return restTemplate.getForObject(url, Map.class);
    }

    /** 将两个指标值构造成 {key: [{value: v}]} 形式的双指标结构 */
    private Map<String, Object> asValueMap(double first, double second) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("used", Collections.singletonList(valueItem(first)));
        result.put("max", Collections.singletonList(valueItem(second)));
        return result;
    }

    private Map<String, Object> valueItem(double value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("value", value);
        return item;
    }

    /**
     * 基于累计请求量采样计算 QPS
     */
    private double computeQps(String serviceName, long currentCount) {
        long now = System.currentTimeMillis();
        RequestSample previous = qpsCache.put(serviceName, new RequestSample(currentCount, now));
        if (previous == null || previous.count <= 0) {
            return 0;
        }
        long elapsed = now - previous.timeMillis;
        if (elapsed <= 0) {
            return 0;
        }
        long delta = currentCount - previous.count;
        if (delta < 0) {
            return 0;
        }
        return delta * 1000.0 / elapsed;
    }

    @PreDestroy
    public void shutdown() {
        probeExecutor.shutdownNow();
    }

    /** 单实例健康探测结果 */
    private static class InstanceHealth {
        String status = STATUS_UNKNOWN;
        long memoryUsed;
        long memoryMax;
        long totalRequests;
        Map<String, Object> components = Collections.emptyMap();
        String error;
    }

    /** 单个服务聚合后的状态 */
    private static class ServiceStat {
        String serviceName;
        String status;
        String host;
        int port;
        long totalRequests;
        long memoryUsedBytes;
        long memoryMaxBytes;
        String error;
        Map<String, Object> details = Collections.emptyMap();

        Map<String, Object> toMap() {
            Map<String, Object> svc = new LinkedHashMap<>();
            svc.put("serviceName", serviceName);
            svc.put("status", status);
            svc.put("instanceHost", host);
            svc.put("instancePort", port);
            svc.put("totalRequests", totalRequests);
            svc.put("memoryUsedBytes", memoryUsedBytes);
            svc.put("memoryMaxBytes", memoryMaxBytes);
            svc.put("error", error);
            svc.put("details", details);
            return svc;
        }
    }

    /** QPS 采样点 */
    private static class RequestSample {
        final long count;
        final long timeMillis;

        RequestSample(long count, long timeMillis) {
            this.count = count;
            this.timeMillis = timeMillis;
        }
    }
}
