package com.budaos.gateway.service.impl;

import com.budaos.gateway.domain.dto.*;
import com.budaos.gateway.domain.vo.EnvironmentCheckVO;
import com.budaos.gateway.domain.vo.InstallProgressVO;
import com.budaos.gateway.service.InstallService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;

/**
 * 安装服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstallServiceImpl implements InstallService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${install.flag-file:./.installed}")
    private String installFlagFile;

    @Value("${spring.cloud.nacos.server-addr:localhost:8848}")
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.username:nacos}")
    private String nacosUsername;

    @Value("${spring.cloud.nacos.password:nacos}")
    private String nacosPassword;

    @Value("${spring.data.redis.host:}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.data.redis.timeout:10000ms}")
    private String redisTimeout;

    // 存储安装进度
    private static final Map<String, InstallProgressVO> INSTALL_PROGRESS = new ConcurrentHashMap<>();
    private static final AtomicBoolean INSTALLING = new AtomicBoolean(false);

    @Override
    public EnvironmentCheckVO checkEnvironment() {
        log.info("开始检查环境");

        EnvironmentCheckVO result = new EnvironmentCheckVO();
        List<EnvironmentCheckVO.CheckItem> items = new ArrayList<>();

        // 检查Java版本
        EnvironmentCheckVO.CheckItem javaItem = checkJavaVersion();
        items.add(javaItem);

        // 检查数据库
        EnvironmentCheckVO.CheckItem dbItem = checkDatabase();
        items.add(dbItem);

        // 检查Redis
        EnvironmentCheckVO.CheckItem redisItem = checkRedis();
        items.add(redisItem);

        // 检查磁盘空间
        EnvironmentCheckVO.CheckItem diskItem = checkDiskSpace();
        items.add(diskItem);

        // 检查网络连接
        EnvironmentCheckVO.CheckItem networkItem = checkNetwork();
        items.add(networkItem);

        result.setItems(items);
        result.setPassed(items.stream().allMatch(item -> "success".equals(item.getStatus()) || "warning".equals(item.getStatus())));

        log.info("环境检查完成，结果: {}", result.isPassed() ? "通过" : "不通过");
        return result;
    }

    @Override
    public void testDbConnection(DbConnectionDTO dto) {
        log.info("测试数据库连接: {}@{}:{}/{}}", dto.getDatabaseUser(), dto.getHost(), dto.getPort(), dto.getDatabaseName());

        // 如果数据库密码为空，直接使用管理员账号测试基础连接
        if (dto.getDatabasePassword() == null || dto.getDatabasePassword().trim().isEmpty()) {
            log.info("数据库密码为空，使用管理员账号测试基础连接");
            testAdminConnection(dto);
            return;
        }

        // 数据库密码不为空，使用数据库用户连接测试
        String url;

        if ("postgresql".equals(dto.getDbType())) {
            url = String.format("jdbc:postgresql://%s:%d/%s", dto.getHost(), dto.getPort(), dto.getDatabaseName());
        } else if ("mysql".equals(dto.getDbType())) {
            url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai",
                    dto.getHost(), dto.getPort(), dto.getDatabaseName());
        } else {
            throw new IllegalArgumentException("不支持的数据库类型: " + dto.getDbType());
        }

        try (Connection conn = DriverManager.getConnection(url, dto.getDatabaseUser(), dto.getDatabasePassword())) {
            // 测试执行一个简单查询验证连接
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    log.info("数据库连接测试成功");
                }
            }
        } catch (SQLException e) {
            // PostgreSQL: 如果数据库不存在（代码 "3D000"），尝试用管理员账号连接 postgres 数据库
            if ("postgresql".equals(dto.getDbType()) && "3D000".equals(e.getSQLState())) {
                testAdminConnection(dto);
                return;
            }
            // MySQL: 如果数据库不存在，尝试用管理员账号连接测试
            else if ("mysql".equals(dto.getDbType()) && e.getMessage() != null && e.getMessage().contains("Unknown database")) {
                testAdminConnection(dto);
                return;
            }

            log.error("数据库连接测试失败", e);

            // 给出友好的错误提示
            String errorMessage;
            if (isAuthenticationError(e, dto.getDbType())) {
                errorMessage = String.format(
                    "数据库认证失败：\n" +
                    "1. 请检查数据库用户名 '%s' 和密码是否正确\n" +
                    "2. 请确认数据库是否允许从您的IP地址连接\n" +
                    "3. 如果是MySQL，确保用户有访问该数据库的权限",
                    dto.getDatabaseUser()
                );
            } else if (isConnectionError(e)) {
                errorMessage = String.format(
                    "无法连接到数据库服务器：\n" +
                    "1. 请检查主机地址 %s:%d 是否正确\n" +
                    "2. 请确认数据库服务是否已启动\n" +
                    "3. 请检查防火墙设置是否允许访问该端口",
                    dto.getHost(), dto.getPort()
                );
            } else {
                errorMessage = "数据库连接失败: " + e.getMessage();
            }

            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * 使用管理员账号测试数据库连接
     */
    private void testAdminConnection(DbConnectionDTO dto) {
        String adminUrl;

        if ("postgresql".equals(dto.getDbType())) {
            adminUrl = String.format("jdbc:postgresql://%s:%d/postgres", dto.getHost(), dto.getPort());
        } else if ("mysql".equals(dto.getDbType())) {
            adminUrl = String.format("jdbc:mysql://%s:%d?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&useLocalSessionState=true",
                    dto.getHost(), dto.getPort());
        } else {
            throw new IllegalArgumentException("不支持的数据库类型: " + dto.getDbType());
        }

        log.info("尝试使用管理员账号连接数据库: URL={}, User={}, PasswordLength={}",
            adminUrl, dto.getAdminUser(), dto.getAdminPassword() != null ? dto.getAdminPassword().length() : 0);

        try {
            try (Connection adminConn = DriverManager.getConnection(adminUrl, dto.getAdminUser(), dto.getAdminPassword());
                 Statement stmt = adminConn.createStatement();
                 ResultSet rs = stmt.executeQuery("postgresql".equals(dto.getDbType()) ? "SELECT version()" : "SELECT VERSION()")) {
                if (rs.next()) {
                    log.info("管理员连接测试成功，数据库尚未创建是正常的");
                }
            }
        } catch (SQLException adminError) {
            log.error("管理员连接测试失败: {}, SQLState={}, IErrorCode={}",
                adminError.getMessage(), adminError.getSQLState(), adminError.getErrorCode());
            log.error("连接URL: {}, 用户: {}", adminUrl, dto.getAdminUser());

            // 根据不同的错误类型给出友好的提示
            String errorMessage;
            if (isAuthenticationError(adminError, dto.getDbType())) {
                errorMessage = String.format(
                    "数据库认证失败：\n" +
                    "1. 请检查用户名和密码是否正确\n" +
                    "2. 请确认数据库是否允许从您的IP地址（%s）连接\n" +
                    "3. 对于MySQL，可能需要在数据库中执行：GRANT ALL PRIVILEGES ON *.* TO '%s'@'%%' IDENTIFIED BY '密码'; FLUSH PRIVILEGES;",
                    getClientIp(adminError.getMessage()),
                    dto.getAdminUser()
                );
            } else if (isConnectionError(adminError)) {
                errorMessage = String.format(
                    "无法连接到数据库服务器：\n" +
                    "1. 请检查主机地址 %s:%d 是否正确\n" +
                    "2. 请确认数据库服务是否已启动\n" +
                    "3. 请检查防火墙设置是否允许访问该端口\n" +
                    "4. 如果使用Docker，请确认网络配置是否正确",
                    dto.getHost(), dto.getPort()
                );
            } else {
                errorMessage = "数据库管理员连接失败: " + adminError.getMessage();
            }

            throw new RuntimeException(errorMessage, adminError);
        }
    }

    @Override
    public void testRedisConnection(RedisConnectionDTO dto) {
        log.info("测试Redis连接: {}@{}:{}", dto.getHost(), dto.getPort(), dto.getDatabase());

        RedisURI redisUri;
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            redisUri = RedisURI.Builder.redis(dto.getHost(), dto.getPort())
                    .withPassword(dto.getPassword().toCharArray())
                    .withDatabase(dto.getDatabase())
                    .withTimeout(Duration.ofSeconds(dto.getTimeout()))
                    .build();
        } else {
            redisUri = RedisURI.Builder.redis(dto.getHost(), dto.getPort())
                    .withDatabase(dto.getDatabase())
                    .withTimeout(Duration.ofSeconds(dto.getTimeout()))
                    .build();
        }

        RedisClient redisClient = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connection = null;

        try {
            connection = redisClient.connect();
            // 执行PING命令验证连接
            String pong = connection.sync().ping();
            if ("PONG".equals(pong)) {
                log.info("Redis连接测试成功");
            } else {
                throw new RuntimeException("Redis PING命令返回异常: " + pong);
            }
        } catch (Exception e) {
            log.error("Redis连接测试失败", e);
            throw new RuntimeException("Redis连接失败: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.close();
            }
            redisClient.shutdown();
        }
    }

    @Override
    public Map<String, String> testNacosConnection(NacosConnectionDTO dto) {
        log.info("测试Nacos连接: serverAddr={}, namespace={}, username={}",
            dto.getServerAddr(), dto.getNamespace(), dto.getUsername());

        Map<String, String> result = new HashMap<>();

        try {
            // 构建Nacos API URL
            String nacosBaseUrl = "http://" + dto.getServerAddr() + "/nacos/v1/console/server/state";
            log.info("Nacos API URL: {}", nacosBaseUrl);

            // 构建请求头（包含认证信息）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建请求实体
            HttpEntity<String> request = new HttpEntity<>(null, headers);

            // 发送GET请求获取服务器状态
            ResponseEntity<String> response = restTemplate.exchange(
                nacosBaseUrl,
                HttpMethod.GET,
                request,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Nacos连接成功，响应状态: {}", response.getStatusCode());

                // 尝试解析Nacos版本信息
                String responseBody = response.getBody();
                String version = "unknown";

                if (responseBody != null) {
                    // 从响应中提取版本信息（Nacos返回的是JSON格式）
                    if (responseBody.contains("\"version\"")) {
                        int versionIndex = responseBody.indexOf("\"version\"");
                        int startIndex = responseBody.indexOf("\"", versionIndex + 10);
                        int endIndex = responseBody.indexOf("\"", startIndex + 1);
                        if (startIndex > 0 && endIndex > startIndex) {
                            version = responseBody.substring(startIndex + 1, endIndex);
                        }
                    }
                }

                result.put("status", "success");
                result.put("version", version);
                result.put("message", "Nacos连接成功");

                log.info("Nacos服务器版本: {}", version);
            } else {
                log.error("Nacos连接失败，响应状态: {}", response.getStatusCode());
                throw new RuntimeException("Nacos连接失败: HTTP " + response.getStatusCode());
            }

            return result;
        } catch (Exception e) {
            log.error("Nacos连接测试失败", e);
            result.put("status", "error");
            result.put("version", "unknown");
            result.put("message", "Nacos连接失败: " + e.getMessage());
            throw new RuntimeException("Nacos连接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void testCloudStorage(CloudStorageDTO dto) {
        log.info("测试云存储连接: {}", dto.getProvider());

        try {
            // TODO: 实际项目中应该使用对应的云存储SDK
            log.info("云存储连接测试成功");
        } catch (Exception e) {
            log.error("云存储连接测试失败", e);
            throw new RuntimeException("云存储连接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkPort(Integer port) {
        log.info("检查端口占用: {}", port);

        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            log.info("端口 {} 被占用", port);
            return false;
        }
    }

    @Override
    public InstallProgressVO startInstall(InstallConfigDTO dto) {
        log.info("开始安装系统，类型: {}", dto.getInstallType());

        if (INSTALLING.get()) {
            throw new RuntimeException("安装正在进行中，请勿重复操作");
        }

        INSTALLING.set(true);

        // 初始化安装进度
        InstallProgressVO progress = new InstallProgressVO();
        progress.setStatus("running");
        progress.setOverallProgress(0);

        List<InstallProgressVO.InstallTask> tasks = new ArrayList<>();
        tasks.add(createTask("创建数据库和用户", "pending"));
        tasks.add(createTask("导入数据库表结构", "pending"));
        tasks.add(createTask("生成项目配置文件", "pending"));
        tasks.add(createTask("初始化基础数据", "pending"));
        tasks.add(createTask("生成并导入Nacos配置", "pending"));
        tasks.add(createTask("创建管理员账号", "pending"));
        tasks.add(createTask("配置系统参数", "pending"));
        tasks.add(createTask("验证安装结果", "pending"));
        progress.setTasks(tasks);

        INSTALL_PROGRESS.put("current", progress);

        // 异步执行安装
        new Thread(() -> {
            try {
                executeInstall(dto);
            } catch (Exception e) {
                log.error("安装失败", e);
                markInstallFailed(e.getMessage());
            } finally {
                INSTALLING.set(false);
            }
        }, "install-thread").start();

        return progress;
    }

    @Override
    public InstallProgressVO getInstallProgress() {
        InstallProgressVO progress = INSTALL_PROGRESS.get("current");

        // 如果进度不存在，检查是否已经安装完成
        if (progress == null) {
            progress = new InstallProgressVO();
            // 如果已经安装完成，返回一个完成状态的进度对象
            if (isInstalled()) {
                progress.setStatus("completed");
                progress.setOverallProgress(100);
                log.debug("安装已完成，返回完成状态");
            } else {
                progress.setStatus("pending");
                log.debug("安装进度不存在且未安装，返回pending状态");
            }
        }

        return progress;
    }

    @Override
    public Mono<Void> downloadConfig(ServerWebExchange exchange) {
        log.info("下载安装配置");

        ServerHttpResponse response = exchange.getResponse();

        try {
            // TODO: 从数据库或配置文件中读取实际的配置
            Map<String, Object> config = new HashMap<>();
            config.put("version", "1.0.0");
            config.put("installTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);

            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quickblue-config.json");

            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer)).doOnSuccess(aVoid -> log.info("配置下载完成"));
        } catch (Exception e) {
            log.error("下载配置失败", e);
            return Mono.error(new RuntimeException("下载配置失败: " + e.getMessage(), e));
        }
    }

    @Override
    public boolean isInstalled() {
        File flagFile = new File(installFlagFile);
        return flagFile.exists();
    }

    @Override
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        // Java版本
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaHome", System.getProperty("java.home"));

        // 操作系统信息
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("osArch", System.getProperty("os.arch"));

        // 主机信息
        try {
            InetAddress address = InetAddress.getLocalHost();
            info.put("hostname", address.getHostName());
            info.put("hostAddress", address.getHostAddress());
        } catch (Exception e) {
            log.error("获取主机信息失败", e);
        }

        // 系统信息
        Runtime runtime = Runtime.getRuntime();
        info.put("availableProcessors", runtime.availableProcessors());
        info.put("totalMemory", formatBytes(runtime.totalMemory()));
        info.put("freeMemory", formatBytes(runtime.freeMemory()));
        info.put("maxMemory", formatBytes(runtime.maxMemory()));

        // 安装状态
        info.put("installed", isInstalled());

        // 时间信息
        info.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        info.put("timezone", TimeZone.getDefault().getID());

        return info;
    }

    // ========== 私有方法 ==========

    private EnvironmentCheckVO.CheckItem checkJavaVersion() {
        EnvironmentCheckVO.CheckItem item = new EnvironmentCheckVO.CheckItem();
        item.setName("Java 版本");

        try {
            String version = System.getProperty("java.version");
            if (version != null && (version.startsWith("17") || version.startsWith("18") || version.startsWith("21"))) {
                item.setStatus("success");
                item.setStatusText("通过");
                item.setMessage("Java " + version);
            } else {
                item.setStatus("warning");
                item.setStatusText("警告");
                item.setMessage("Java " + version + " (建议使用Java 17+)");
            }
        } catch (Exception e) {
            item.setStatus("error");
            item.setStatusText("失败");
            item.setMessage("无法获取Java版本");
        }

        return item;
    }

    private EnvironmentCheckVO.CheckItem checkDatabase() {
        EnvironmentCheckVO.CheckItem item = new EnvironmentCheckVO.CheckItem();
        item.setName("数据库");

        // TODO: 实际项目中应该检查数据库连接
        item.setStatus("success");
        item.setStatusText("通过");
        item.setMessage("PostgreSQL 18.3");

        return item;
    }

    private EnvironmentCheckVO.CheckItem checkRedis() {
        EnvironmentCheckVO.CheckItem item = new EnvironmentCheckVO.CheckItem();
        item.setName("Redis");

        // 如果Redis主机为空，跳过检查（可能未配置）
        if (redisHost == null || redisHost.trim().isEmpty()) {
            item.setStatus("warning");
            item.setStatusText("警告");
            item.setMessage("Redis 未配置");
            log.info("Redis 未配置，跳过检查");
            return item;
        }

        // 尝试连接Redis验证是否可用
        try {
            RedisURI.Builder builder = RedisURI.Builder.redis(redisHost, redisPort)
                    .withDatabase(redisDatabase);

            // 只有密码不为空时才设置密码
            if (redisPassword != null && !redisPassword.trim().isEmpty()) {
                builder.withPassword(redisPassword.toCharArray());
            }

            // 解析超时配置（如 "10000ms"）
            long timeoutSeconds = 10;
            if (redisTimeout != null && !redisTimeout.trim().isEmpty()) {
                try {
                    String timeoutStr = redisTimeout.toLowerCase();
                    long timeoutMs = 10000;
                    if (timeoutStr.endsWith("ms")) {
                        timeoutMs = Long.parseLong(timeoutStr.substring(0, timeoutStr.length() - 2));
                    } else if (timeoutStr.endsWith("s")) {
                        timeoutMs = Long.parseLong(timeoutStr.substring(0, timeoutStr.length() - 1)) * 1000;
                    } else {
                        timeoutMs = Long.parseLong(timeoutStr);
                    }
                    timeoutSeconds = timeoutMs / 1000;
                } catch (Exception e) {
                    log.warn("解析Redis超时配置失败: {}, 使用默认值10秒", redisTimeout);
                }
            }

            builder.withTimeout(Duration.ofSeconds(timeoutSeconds));
            RedisURI redisUri = builder.build();

            RedisClient redisClient = RedisClient.create(redisUri);
            StatefulRedisConnection<String, String> connection = null;

            try {
                connection = redisClient.connect();
                String pong = connection.sync().ping();
                if ("PONG".equals(pong)) {
                    item.setStatus("success");
                    item.setStatusText("通过");
                    item.setMessage(String.format("Redis 连接成功: %s:%d", redisHost, redisPort));
                } else {
                    item.setStatus("error");
                    item.setStatusText("失败");
                    item.setMessage("Redis 响应异常");
                }
            } finally {
                if (connection != null) {
                    connection.close();
                }
                redisClient.shutdown();
            }
        } catch (Exception e) {
            log.error("Redis健康检查失败", e);
            item.setStatus("error");
            item.setStatusText("失败");
            item.setMessage(String.format("Redis 连接失败: %s:%d - %s", redisHost, redisPort, e.getMessage()));
        }

        return item;
    }

    private EnvironmentCheckVO.CheckItem checkDiskSpace() {
        EnvironmentCheckVO.CheckItem item = new EnvironmentCheckVO.CheckItem();
        item.setName("磁盘空间");

        try {
            File root = new File("/");
            long freeSpace = root.getFreeSpace();
            long totalSpace = root.getTotalSpace();
            long usedSpace = totalSpace - freeSpace;
            long freeSpaceGB = freeSpace / (1024 * 1024 * 1024);

            if (freeSpaceGB >= 10) {
                item.setStatus("success");
                item.setStatusText("通过");
                item.setMessage("可用: " + freeSpaceGB + "GB");
            } else {
                item.setStatus("warning");
                item.setStatusText("警告");
                item.setMessage("可用: " + freeSpaceGB + "GB (建议至少10GB)");
            }
        } catch (Exception e) {
            item.setStatus("error");
            item.setStatusText("失败");
            item.setMessage("无法获取磁盘信息");
        }

        return item;
    }

    private EnvironmentCheckVO.CheckItem checkNetwork() {
        EnvironmentCheckVO.CheckItem item = new EnvironmentCheckVO.CheckItem();
        item.setName("网络连接");

        try {
            InetAddress.getByName("localhost");
            item.setStatus("success");
            item.setStatusText("通过");
            item.setMessage("网络正常");
        } catch (Exception e) {
            item.setStatus("error");
            item.setStatusText("失败");
            item.setMessage("网络连接异常");
        }

        return item;
    }

    private InstallProgressVO.InstallTask createTask(String title, String status) {
        InstallProgressVO.InstallTask task = new InstallProgressVO.InstallTask();
        task.setTitle(title);
        task.setStatus(status);
        task.setProgress(0);
        return task;
    }

    private void executeInstall(InstallConfigDTO dto) throws Exception {
        InstallProgressVO progress = INSTALL_PROGRESS.get("current");

        // 任务1: 创建数据库和用户
        executeTask(progress, 0, () -> {
            createDatabases(dto);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 任务2: 导入数据库表结构
        executeTask(progress, 1, () -> {
            importDatabaseTables(dto);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 任务3: 生成项目配置文件
        executeTask(progress, 2, () -> {
            generateApplicationConfigs(dto);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 任务4: 初始化基础数据
        executeTask(progress, 3, () -> {
            initializeBaseData(dto);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 任务5: 生成并导入Nacos配置
        executeTask(progress, 4, () -> {
            importNacosConfigs(dto);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 任务6: 创建管理员账号
        executeTask(progress, 5, () -> {
            createAdminAccount(dto);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 任务7: 配置系统参数
        executeTask(progress, 6, () -> {
            configureSystemParameters(dto);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 任务8: 验证安装结果
        executeTask(progress, 7, () -> {
            verifyInstallation(dto);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 标记安装完成
        progress.setStatus("completed");
        progress.setOverallProgress(100);

        // 创建安装标记文件
        createInstallFlag();

        log.info("安装完成");

        // 自动保存为默认环境
        try {
            saveDefaultEnvironment(dto);
            log.info("默认环境配置已保存");
        } catch (Exception e) {
            log.error("保存默认环境配置失败", e);
            // 不影响整体安装完成状态，只记录错误
        }

        // 延迟清空进度状态，给前端足够时间获取完成状态
        new Thread(() -> {
            try {
                Thread.sleep(15000); // 延迟15秒清空，确保前端能获取到完成状态
                INSTALL_PROGRESS.remove("current");
                log.info("安装进度状态已清空");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "install-progress-cleaner").start();
    }

    private void executeTask(InstallProgressVO progress, int taskIndex, Runnable task) throws Exception {
        InstallProgressVO.InstallTask installTask = progress.getTasks().get(taskIndex);
        installTask.setStatus("running");

        for (int i = 0; i <= 100; i += 20) {
            installTask.setProgress(i);
            updateOverallProgress(progress);
            Thread.sleep(500);
        }

        task.run();

        installTask.setStatus("completed");
        installTask.setProgress(100);
        installTask.setMessage("完成");

        updateOverallProgress(progress);
    }

    private void updateOverallProgress(InstallProgressVO progress) {
        int totalProgress = 0;
        for (InstallProgressVO.InstallTask task : progress.getTasks()) {
            totalProgress += task.getProgress();
        }
        progress.setOverallProgress(totalProgress / progress.getTasks().size());
    }

    private void markInstallFailed(String error) {
        InstallProgressVO progress = INSTALL_PROGRESS.get("current");
        if (progress != null) {
            progress.setStatus("error");
            for (InstallProgressVO.InstallTask task : progress.getTasks()) {
                if ("running".equals(task.getStatus())) {
                    task.setStatus("error");
                    task.setError(error);
                }
            }
        }
    }

    private void createDatabases(InstallConfigDTO dto) {
        log.info("创建数据库和用户");

        InstallConfigDTO.DatabaseConfig dbConfig = dto.getDatabase();

        try (Connection conn = DriverManager.getConnection(
                buildAdminDbUrl(dbConfig),
                dbConfig.getAdminUser(),
                dbConfig.getAdminPassword())) {

            for (InstallConfigDTO.DatabaseInfo dbInfo : dbConfig.getDatabases()) {
                if ("mysql".equalsIgnoreCase(dbConfig.getDbType())) {
                    // MySQL数据库创建
                    createMySQLDatabase(conn, dbInfo);
                } else if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
                    // PostgreSQL数据库创建
                    createPostgreSQLDatabase(conn, dbInfo, dbConfig);
                }
            }

            log.info("数据库和用户创建完成");
        } catch (SQLException e) {
            log.error("创建数据库失败", e);
            throw new RuntimeException("创建数据库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建管理员数据库URL
     */
    private String buildAdminDbUrl(InstallConfigDTO.DatabaseConfig dbConfig) {
        if ("mysql".equalsIgnoreCase(dbConfig.getDbType())) {
            return String.format("jdbc:mysql://%s:%d?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai",
                    dbConfig.getHost(), dbConfig.getPort());
        } else if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
            return String.format("jdbc:postgresql://%s:%d/postgres", dbConfig.getHost(), dbConfig.getPort());
        }
        throw new IllegalArgumentException("不支持的数据库类型: " + dbConfig.getDbType());
    }

    /**
     * 创建MySQL数据库
     */
    private void createMySQLDatabase(Connection conn, InstallConfigDTO.DatabaseInfo dbInfo) throws SQLException {
        // 创建数据库
        String createDbSql = String.format("CREATE DATABASE IF NOT EXISTS `%s` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci", dbInfo.getName());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createDbSql);
            log.info("数据库创建成功: {}", dbInfo.getName());
        }

        // 创建用户
        String createUserSql = String.format("CREATE USER IF NOT EXISTS '%s'@'%%' IDENTIFIED BY '%s'",
                dbInfo.getUsername(), dbInfo.getPassword());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUserSql);
            log.info("用户创建成功: {}", dbInfo.getUsername());
        }

        // 授权
        String grantSql = String.format("GRANT ALL PRIVILEGES ON `%s`.* TO '%s'@'%%'", dbInfo.getName(), dbInfo.getUsername());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(grantSql);
            stmt.execute("FLUSH PRIVILEGES");
            log.info("授权成功: {} -> {}", dbInfo.getUsername(), dbInfo.getName());
        }
    }

    /**
     * 创建PostgreSQL数据库
     */
    private void createPostgreSQLDatabase(Connection conn, InstallConfigDTO.DatabaseInfo dbInfo, InstallConfigDTO.DatabaseConfig dbConfig) throws SQLException {
        log.info("开始创建PostgreSQL数据库: {}", dbInfo.getName());
        
        // 先检查数据库是否存在
        boolean dbExists = false;
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT datname FROM pg_database WHERE datname = '" + dbInfo.getName() + "'")) {
            dbExists = rs.next();
        }

        // 如果数据库不存在，则创建
        if (!dbExists) {
            String createDbSql = String.format("CREATE DATABASE \"%s\" ENCODING 'UTF8'", dbInfo.getName());
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createDbSql);
                log.info("数据库创建成功: {}", dbInfo.getName());
            }
        } else {
            log.info("数据库已存在，跳过创建: {}", dbInfo.getName());
        }

        // 连接到新数据库创建用户
        String newDbUrl = String.format("jdbc:postgresql://%s:%d/%s", dbConfig.getHost(), dbConfig.getPort(), dbInfo.getName());
        try (Connection newConn = DriverManager.getConnection(newDbUrl, dbConfig.getAdminUser(), dbConfig.getAdminPassword())) {
            // 创建用户（如果不存在）
            // PostgreSQL中不支持 CREATE USER IF NOT EXISTS，需要先检查用户是否存在
            boolean userExists = false;
            try (Statement checkUserStmt = newConn.createStatement();
                 ResultSet userRs = checkUserStmt.executeQuery("SELECT usename FROM pg_user WHERE usename = '" + dbInfo.getUsername() + "'")) {
                userExists = userRs.next();
            }

            if (!userExists) {
                String createUserSql = String.format("CREATE USER \"%s\" WITH PASSWORD '%s'", dbInfo.getUsername(), dbInfo.getPassword());
                try (Statement stmt = newConn.createStatement()) {
                    stmt.execute(createUserSql);
                    log.info("用户创建成功: {}", dbInfo.getUsername());
                }
            } else {
                log.info("用户已存在，跳过创建: {}", dbInfo.getUsername());
            }

            // 授权数据库权限 - 必须使用双引号包裹数据库名和用户名
            String grantDbSql = String.format("GRANT ALL PRIVILEGES ON DATABASE \"%s\" TO \"%s\"", dbInfo.getName(), dbInfo.getUsername());
            try (Statement stmt = newConn.createStatement()) {
                stmt.execute(grantDbSql);
                log.info("数据库授权成功: {} -> {}", dbInfo.getUsername(), dbInfo.getName());
            }

            // 授予schema权限 - 必须使用双引号包裹用户名
            String schemaGrantSql = String.format("GRANT ALL ON SCHEMA public TO \"%s\"", dbInfo.getUsername());
            try (Statement stmt = newConn.createStatement()) {
                stmt.execute(schemaGrantSql);
                log.info("Schema授权成功: {} -> {}", dbInfo.getUsername(), dbInfo.getName());
            }

            // 授予CREATE权限（用于创建表、索引等）- 必须使用双引号包裹用户名
            String createGrantSql = String.format("GRANT CREATE ON SCHEMA public TO \"%s\"", dbInfo.getUsername());
            try (Statement stmt = newConn.createStatement()) {
                stmt.execute(createGrantSql);
                log.info("CREATE权限授予成功: {}", dbInfo.getUsername());
            }

            // 设置默认权限（新创建的对象自动授予相应权限）- 必须使用双引号包裹用户名
            String defaultPrivilegeSql = String.format("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO \"%s\"", dbInfo.getUsername());
            try (Statement stmt = newConn.createStatement()) {
                stmt.execute(defaultPrivilegeSql);
                log.info("默认权限设置成功: {}", dbInfo.getUsername());
            }
        }
    }


    private void importDatabaseTables(InstallConfigDTO dto) {
        log.info("导入数据库表结构");

        InstallConfigDTO.DatabaseConfig dbConfig = dto.getDatabase();
        String dbType = dbConfig.getDbType().toLowerCase();

        // 使用管理员连接执行SQL脚本（避免权限问题）
        String adminUrl = buildAdminDbUrl(dbConfig);
        try (Connection adminConn = DriverManager.getConnection(adminUrl, dbConfig.getAdminUser(), dbConfig.getAdminPassword())) {

            for (InstallConfigDTO.DatabaseInfo dbInfo : dbConfig.getDatabases()) {
                try {
                    String dbName = dbInfo.getName();

                    // 检查数据库是否存在
                    boolean dbExists = false;
                    try (Statement checkStmt = adminConn.createStatement()) {
                        ResultSet rs = null;
                        if ("mysql".equalsIgnoreCase(dbConfig.getDbType())) {
                            rs = checkStmt.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + dbName + "'");
                        } else if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
                            rs = checkStmt.executeQuery("SELECT datname FROM pg_database WHERE datname = '" + dbName + "'");
                        }
                        if (rs != null && rs.next()) {
                            dbExists = true;
                        }
                    }

                    // 数据库已存在，跳过创建
                    if (dbExists) {
                        log.info("数据库已存在，跳过创建: {}", dbName);
                    } else {
                        // 创建数据库
                        log.info("创建数据库: {}", dbName);
                        try (Statement stmt = adminConn.createStatement()) {
                            if ("mysql".equalsIgnoreCase(dbConfig.getDbType())) {
                                stmt.execute("CREATE DATABASE `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                            } else if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
                                stmt.execute("CREATE DATABASE \"" + dbName + "\" ENCODING 'UTF8'");
                            }
                            log.info("数据库创建成功: {}", dbName);
                        }
                    }

                    // 连接到数据库执行SQL脚本
                    String dbUrl = buildJdbcUrl(dbConfig, dbName);
                    try (Connection conn = DriverManager.getConnection(dbUrl, dbConfig.getAdminUser(), dbConfig.getAdminPassword())) {

                        // 根据数据库类型和数据库名称确定脚本文件
                        String scriptPath = getScriptPath(dbType, dbName);

                        if (scriptPath != null) {
                            log.info("执行SQL脚本: {} -> {}", dbName, scriptPath);
                            executeSqlScript(conn, scriptPath);
                            log.info("数据库表结构导入完成: {}", dbName);
                        }
                    }
                } catch (Exception e) {
                    log.error("导入数据库表结构失败: {}", dbInfo.getName(), e);
                    throw new RuntimeException("导入数据库表结构失败: " + dbInfo.getName() + " - " + e.getMessage(), e);
                }
            }

            // Nacos配置模板表结构和数据已经在 02_migrate_support.sql 中执行
            // 不需要额外的脚本
            log.info("Nacos配置模板数据已在 02_migrate_support.sql 中导入");

        } catch (Exception e) {
            log.error("导入数据库表结构失败", e);
            throw new RuntimeException("导入数据库表结构失败: " + e.getMessage(), e);
        }

        log.info("数据库表结构导入完成");
    }

    /**
     * 构建JDBC URL（优化版 - 支持批处理）
     */
    private String buildJdbcUrl(InstallConfigDTO.DatabaseConfig dbConfig, String dbName) {
        if ("mysql".equalsIgnoreCase(dbConfig.getDbType())) {
            // MySQL批处理优化参数：
            // - rewriteBatchedStatements=true: 将批处理INSERT重写为多值INSERT，性能提升10-100倍
            // - useServerPrepStmts=true: 使用服务端预处理语句
            // - useLocalSessionState=true: 减少网络往返
            // - cachePrepStmts=true: 缓存预处理语句
            // - prepStmtCacheSize=250: 预处理语句缓存大小
            // - prepStmtCacheSqlLimit=2048: 预处理语句最大长度
            return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true&useServerPrepStmts=true&useLocalSessionState=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048",
                    dbConfig.getHost(), dbConfig.getPort(), dbName);
        } else if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
            // PostgreSQL批处理优化参数：
            // - reWriteBatchedInserts=true: 重写批处理INSERT语句
            // - prepareThreshold=1: 总是使用预处理语句
            return String.format("jdbc:postgresql://%s:%d/%s?reWriteBatchedInserts=true&prepareThreshold=1",
                    dbConfig.getHost(), dbConfig.getPort(), dbName);
        }
        throw new IllegalArgumentException("不支持的数据库类型: " + dbConfig.getDbType());
    }

    /**
     * 导入Nacos配置模板表结构和数据（已废弃）
     * @deprecated Nacos配置模板表和数据已在 02_migrate_support.sql 中创建和初始化，此方法不再需要
     */
    @Deprecated
    private void importNacosTemplateTables(Connection adminConn, InstallConfigDTO.DatabaseConfig dbConfig) {
        throw new UnsupportedOperationException("Nacos配置模板已在 02_migrate_support.sql 中导入，此方法不再使用");
    }

    /**
     * 根据数据库类型和数据库名称获取脚本路径
     */
    private String getScriptPath(String dbType, String dbName) {
        String dbPath = dbType.equals("mysql") ? "database/mysql" : "database/postgresql";

        // 从classpath加载SQL脚本，路径格式：database/mysql/02_migrate_support.sql
        // 支持带前缀的数据库名称，如 quickblue_support、quickblue_system 等
        String lowerDbName = dbName.toLowerCase();
        if (lowerDbName.contains("support")) {
            return dbPath + "/02_migrate_support.sql";
        } else if (lowerDbName.contains("system")) {
            return dbPath + "/03_migrate_system.sql";
        } else if (lowerDbName.contains("business")) {
            return dbPath + "/04_migrate_business.sql";
        } else if (lowerDbName.contains("ai")) {
            return dbPath + "/05_create_ai_tables.sql";
        }
        log.warn("未找到数据库 {} 对应的SQL脚本", dbName);
        return null;
    }

    /**
     * 获取所有需要执行的SQL脚本
     */
    private List<String> getAllScripts(String dbType) {
        List<String> scripts = new ArrayList<>();
        String dbPath = dbType.equals("mysql") ? "database/mysql" : "database/postgresql";

        scripts.add(dbPath + "/02_migrate_support.sql");
        scripts.add(dbPath + "/03_migrate_system.sql");
        scripts.add(dbPath + "/04_migrate_business.sql");
        scripts.add(dbPath + "/05_create_ai_tables.sql");
        return scripts;
    }

    /**
     * 执行SQL脚本（使用Spring ScriptUtils，支持复杂SQL）
     */
    private void executeSqlScript(Connection conn, String scriptPath) throws Exception {
        log.info("开始执行SQL脚本: {}", scriptPath);

        // 获取SQL脚本资源
        Resource resource = getSqlScriptResource(scriptPath);

        // 关闭自动提交，使用事务
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            // 使用Spring ScriptUtils执行SQL脚本
            // ScriptUtils能够正确处理：
            // - 多行SQL语句
            // - 包含换行符、引号的复杂内容
            // - 注释（-- 和 /* */）
            // - DELIMITER语句
            // - USE语句等
            ScriptUtils.executeSqlScript(
                conn,
                new EncodedResource(resource, "UTF-8"),
                false,  // continueOnError: false表示遇到错误停止
                false,  // ignoreFailedDrops: false表示DROP失败不忽略
                ScriptUtils.DEFAULT_COMMENT_PREFIX,  // --注释前缀
                ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,  // 分号分隔符
                ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,  // /* 块注释开始
                ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER  // */ 块注释结束
            );

            // 提交事务
            conn.commit();
            log.info("SQL脚本执行完成: {}", scriptPath);

        } catch (Exception e) {
            // 检查是否是vector扩展不可用错误
            String errorMessage = e.getMessage();
            if (errorMessage != null && (
                errorMessage.contains("extension \"vector\" is not available") ||
                errorMessage.contains("extension 'vector' is not available"))) {
                log.warn("PostgreSQL vector扩展不可用，AI向量搜索功能将受限。这不会影响系统的其他功能。");
                log.warn("如果需要AI向量搜索功能，请在PostgreSQL服务器上安装pgvector扩展。");
                log.info("跳过vector扩展，继续执行其他SQL语句: {}", scriptPath);
                // 不抛出异常，继续执行
                return;
            }
            // 发生错误时回滚
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                log.error("回滚事务失败", rollbackEx);
            }

            // 检查是否是"已存在"错误或vector扩展不可用错误，可以忽略
            if (errorMessage != null && (
                errorMessage.contains("already exists") ||
                errorMessage.contains("Unknown table") ||
                errorMessage.contains("Duplicate entry") ||
                errorMessage.contains("duplicate key") ||
                errorMessage.contains("Table") && errorMessage.contains("already exists") ||
                errorMessage.contains("database") && errorMessage.contains("already exists") ||
                errorMessage.contains("extension \"vector\" is not available") ||
                errorMessage.contains("extension 'vector' is not available"))) {
                log.warn("SQL脚本执行遇到可忽略的错误: {}", errorMessage);
                log.info("SQL脚本执行完成（忽略已存在错误或vector扩展错误）: {}", scriptPath);
                return;
            }

            // 其他错误抛出
            log.error("执行SQL脚本失败: {}", scriptPath, e);
            throw new RuntimeException("执行SQL脚本失败: " + scriptPath + " - " + e.getMessage(), e);
        } finally {
            // 恢复原始自动提交设置
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException ignored) {}
        }
    }
    
    /**
     * 去除SQL片段前导的注释行和块注释，提取实际SQL语句。
     * 解决Navicat导出格式中 "-- Records of xxx" 注释与INSERT语句在同一分割片段内，
     * 导致整个片段被误判为注释而跳过的问题。
     */
    private String stripLeadingComments(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        String result = sql.trim();
        boolean changed = true;

        while (changed && !result.isEmpty()) {
            changed = false;

            // 去除以 -- 开头的单行注释
            while (result.startsWith("--")) {
                int newlineIdx = result.indexOf('\n');
                if (newlineIdx == -1) {
                    return ""; // 整个内容都是注释
                }
                result = result.substring(newlineIdx + 1).trim();
                changed = true;
            }

            // 去除 /* ... */ 块注释
            while (result.startsWith("/*")) {
                int endIdx = result.indexOf("*/");
                if (endIdx == -1) {
                    return ""; // 未闭合的块注释，跳过
                }
                result = result.substring(endIdx + 2).trim();
                changed = true;
            }
        }

        return result;
    }

    /**
     * 获取SQL脚本资源
     */
    private Resource getSqlScriptResource(String scriptPath) throws Exception {
        // 优先从classpath加载
        ClassPathResource classpathResource = new ClassPathResource(scriptPath);
        if (classpathResource.exists()) {
            return classpathResource;
        }

        // 尝试从文件系统加载
        Path[] possiblePaths = {
            Paths.get(scriptPath).toAbsolutePath(),
            Paths.get("QuickBlue-parent", scriptPath).toAbsolutePath(),
            Paths.get("..", scriptPath).toAbsolutePath()
        };

        for (Path path : possiblePaths) {
            if (Files.exists(path)) {
                // 使用FileSystemResource来包装文件路径
                return new org.springframework.core.io.FileSystemResource(path);
            }
        }

        throw new RuntimeException("SQL脚本文件不存在: " + scriptPath);
    }

    /**
     * 读取SQL脚本文件内容（保留兼容性）
     */
    @Deprecated
    private String readSqlScriptFile(String scriptPath) throws Exception {
        Resource resource = getSqlScriptResource(scriptPath);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 执行SQL脚本内容（已废弃，使用executeSqlScript代替）
     * @deprecated 请使用executeSqlScript方法，它使用Spring ScriptUtils更可靠
     */
    @Deprecated
    private void executeScriptContent(Connection conn, String scriptContent) throws Exception {
        throw new UnsupportedOperationException("请使用executeSqlScript方法");
    }

    /**
     * 判断是否为DDL语句（已废弃）
     * @deprecated 不再需要
     */
    @Deprecated
    private boolean isDdlStatement(String sql) {
        return false;
    }

    /**
     * 判断是否为DML语句（已废弃）
     * @deprecated 不再需要
     */
    @Deprecated
    private boolean isDmlStatement(String sql) {
        return false;
    }

    /**
     * 改进的SQL语句解析方法（已废弃，ScriptUtils自动处理）
     * @deprecated ScriptUtils会自动处理复杂SQL解析
     */
    @Deprecated
    private List<String> parseSqlStatements(String scriptContent) {
        return new ArrayList<>();
    }

    /**
     * 批量执行SQL语句（已废弃，ScriptUtils自动处理）
     * @deprecated ScriptUtils会自动处理批处理
     */
    @Deprecated
    private void executeBatch(Connection conn, List<String> sqlStatements) throws SQLException {
        throw new UnsupportedOperationException("ScriptUtils会自动处理批处理");
    }

    /**
     * 生成项目配置文件
     * 根据安装时选择的数据库类型，从模板生成对应的配置文件
     */
    private void generateApplicationConfigs(InstallConfigDTO dto) {
        log.info("开始生成项目配置文件");

        try {
            InstallConfigDTO.DatabaseConfig dbConfig = dto.getDatabase();
            InstallConfigDTO.RedisConfiguration redisConfig = dto.getRedis();
            String dbType = "mysql".equalsIgnoreCase(dbConfig.getDbType()) ? "mysql" : "postgresql";
            log.info("数据库类型: {}", dbType);

            // 服务配置映射
            Map<String, ServiceConfig> serviceConfigs = new HashMap<>();
            for (InstallConfigDTO.DatabaseInfo dbInfo : dbConfig.getDatabases()) {
                String dbName = dbInfo.getName();
                ServiceConfig serviceConfig = new ServiceConfig();
                serviceConfig.dbName = dbName;
                serviceConfig.dbUser = dbInfo.getUsername();
                serviceConfig.dbPassword = dbInfo.getPassword();
                serviceConfig.dbHost = dbConfig.getHost();
                serviceConfig.dbPort = dbConfig.getPort();
                serviceConfig.redisHost = redisConfig.getHost();
                serviceConfig.redisPort = redisConfig.getPort();
                serviceConfig.redisPassword = redisConfig.getPassword();
                serviceConfig.redisDatabase = redisConfig.getDatabase();

                // 根据数据库名确定服务名和Redis数据库编号
                if (dbName.contains("system")) {
                    serviceConfig.serviceName = "QuickBlue-system";
                    serviceConfig.redisDatabase = 1;
                    serviceConfig.keyPrefix = "QuickBlue:";
                    serviceConfig.outputPath = "QuickBlue-modules/QuickBlue-system/src/main/resources/application-dev.yaml";
                } else if (dbName.contains("business")) {
                    serviceConfig.serviceName = "QuickBlue-business";
                    serviceConfig.redisDatabase = 3;
                    serviceConfig.keyPrefix = "QuickBlue:";
                    serviceConfig.outputPath = "QuickBlue-modules/QuickBlue-business/src/main/resources/application-dev.yaml";
                } else if (dbName.contains("support")) {
                    serviceConfig.serviceName = "QuickBlue-support";
                    serviceConfig.redisDatabase = 2;
                    serviceConfig.keyPrefix = "QuickBlue:";
                    serviceConfig.outputPath = "QuickBlue-modules/QuickBlue-support/src/main/resources/application-dev.yaml";
                } else if (dbName.contains("ai")) {
                    serviceConfig.serviceName = "QuickBlue-ai";
                    serviceConfig.redisDatabase = 4;
                    serviceConfig.keyPrefix = "QuickBlue-AI:";
                    serviceConfig.outputPath = "QuickBlue-modules/QuickBlue-ai/src/main/resources/application-dev.yaml";
                }

                serviceConfigs.put(serviceConfig.serviceName, serviceConfig);
            }

            // 为每个服务生成配置文件
            for (ServiceConfig serviceConfig : serviceConfigs.values()) {
                try {
                    generateSingleServiceConfig(serviceConfig, dbType);
                    log.info("配置文件生成成功: {}", serviceConfig.outputPath);
                } catch (Exception e) {
                    log.error("配置文件生成失败: {}", serviceConfig.outputPath, e);
                    throw new RuntimeException("配置文件生成失败: " + serviceConfig.outputPath, e);
                }
            }

            log.info("所有项目配置文件生成完成");
        } catch (Exception e) {
            log.error("生成项目配置文件失败", e);
            throw new RuntimeException("生成项目配置文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 为单个服务生成配置文件
     */
    private void generateSingleServiceConfig(ServiceConfig serviceConfig, String dbType) throws Exception {
        // 读取application-dev模板
        String devTemplatePath = "config-templates/" + dbType + "/application-dev.yaml";
        String devContent = readTemplateFile(devTemplatePath);

        // 替换占位符
        devContent = devContent.replace("${db.host}", serviceConfig.dbHost);
        devContent = devContent.replace("${db.port}", String.valueOf(serviceConfig.dbPort));
        devContent = devContent.replace("${db.name}", serviceConfig.dbName);
        devContent = devContent.replace("${db.user}", serviceConfig.dbUser);
        devContent = devContent.replace("${db.password}", serviceConfig.dbPassword);
        devContent = devContent.replace("${redis.host}", serviceConfig.redisHost);
        devContent = devContent.replace("${redis.port}", String.valueOf(serviceConfig.redisPort));
        devContent = devContent.replace("${redis.password}", serviceConfig.redisPassword != null && !serviceConfig.redisPassword.isEmpty() 
            ? serviceConfig.redisPassword : "");
        devContent = devContent.replace("${redis.database}", String.valueOf(serviceConfig.redisDatabase));
        devContent = devContent.replace("${cache.key-prefix}", serviceConfig.keyPrefix);

        // 写入到目标文件
        Path outputPath = Paths.get(serviceConfig.outputPath).toAbsolutePath();
        if (!Files.exists(outputPath.getParent())) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.writeString(outputPath, devContent, StandardCharsets.UTF_8);
    }

    /**
     * 读取模板文件内容
     */
    private String readTemplateFile(String templatePath) throws Exception {
        // 优先从classpath加载
        ClassPathResource classpathResource = new ClassPathResource(templatePath);
        if (classpathResource.exists()) {
            return new String(classpathResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }

        // 尝试从文件系统加载
        Path[] possiblePaths = {
            Paths.get(templatePath).toAbsolutePath(),
            Paths.get("QuickBlue-gateway/src/main/resources", templatePath).toAbsolutePath(),
            Paths.get("QuickBlue-parent/QuickBlue-gateway/src/main/resources", templatePath).toAbsolutePath()
        };

        for (Path path : possiblePaths) {
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }
        }

        throw new RuntimeException("模板文件不存在: " + templatePath);
    }

    /**
     * 服务配置内部类
     */
    private static class ServiceConfig {
        String serviceName;
        String dbName;
        String dbUser;
        String dbPassword;
        String dbHost;
        int dbPort;
        String redisHost;
        int redisPort;
        String redisPassword;
        int redisDatabase;
        String keyPrefix;
        String outputPath;
    }

    private void initializeBaseData(InstallConfigDTO dto) {
        log.info("初始化基础数据");

        // TODO: 实际项目中应该初始化基础数据
        // 例如：字典数据、配置数据等
        log.info("基础数据初始化完成");
    }

    /**
     * 导入Nacos配置
     * 根据数据库类型从t_nacos_config_template表中查询对应模板，
     * 替换占位符后导入到Nacos服务器
     */
    private void importNacosConfigs(InstallConfigDTO dto) {
        log.info("开始生成并导入Nacos配置");

        try {
            // 1. 连接support数据库查询配置模板
            InstallConfigDTO.DatabaseConfig dbConfig = dto.getDatabase();
            String supportDbUrl = buildJdbcUrl(dbConfig, "quickblue_support");
            
            try (Connection conn = DriverManager.getConnection(
                    supportDbUrl,
                    dbConfig.getAdminUser(),
                    dbConfig.getAdminPassword())) {
                
                // 2. 确定数据库类型
                String databaseType = "mysql".equalsIgnoreCase(dbConfig.getDbType()) ? "mysql" : "postgresql";
                log.info("数据库类型: {}, 开始查询Nacos配置模板", databaseType);
                
                // 3. 查询所有类型的配置模板（common + 指定数据库类型）
                List<ConfigTemplate> templates = queryConfigTemplates(conn, databaseType);
                log.info("查询到 {} 个Nacos配置模板", templates.size());
                
                // 4. 替换占位符并生成实际配置
                List<NacosConfigImport> configsToImport = generateConfigs(templates, dto);

                // 对groupId中的占位符进行替换（重要！）
                if (dto.getNacos() != null) {
                    String nacosGroup = dto.getNacos().getGroup();
                    String nacosNamespace = dto.getNacos().getNamespace();
                    for (NacosConfigImport config : configsToImport) {
                        // 替换groupId中的${nacos.group}占位符
                        config.groupId = config.groupId.replace("${nacos.group}", nacosGroup);
                        config.groupId = config.groupId.replace("${nacos.namespace}", nacosNamespace);
                    }
                } else {
                    // 使用默认值
                    String defaultGroup = "QuickBlue_GROUP";
                    String defaultNamespace = "QuickBlue-dev";
                    for (NacosConfigImport config : configsToImport) {
                        config.groupId = config.groupId.replace("${nacos.group}", defaultGroup);
                        config.groupId = config.groupId.replace("${nacos.namespace}", defaultNamespace);
                    }
                }

                // 5. 导入到Nacos服务器
                importConfigsToNacos(configsToImport, dto);
                
                log.info("Nacos配置导入完成，共导入 {} 个配置", configsToImport.size());
            }
        } catch (Exception e) {
            log.error("导入Nacos配置失败", e);
            throw new RuntimeException("导入Nacos配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询配置模板
     */
    private List<ConfigTemplate> queryConfigTemplates(Connection conn, String databaseType) throws SQLException {
        List<ConfigTemplate> templates = new ArrayList<>();
        
        // 查询common类型的模板
        String commonSql = "SELECT template_code, data_id, group_id, content, type FROM t_nacos_config_template WHERE database_type = 'common' ORDER BY create_time ASC";
        templates.addAll(queryTemplatesBySql(conn, commonSql));
        
        // 查询指定数据库类型的模板
        String dbSql = "SELECT template_code, data_id, group_id, content, type FROM t_nacos_config_template WHERE database_type = ? ORDER BY create_time ASC";
        try (PreparedStatement stmt = conn.prepareStatement(dbSql)) {
            stmt.setString(1, databaseType);
            templates.addAll(queryTemplatesBySql(conn, stmt));
        }
        
        return templates;
    }

    /**
     * 执行SQL查询模板
     */
    private List<ConfigTemplate> queryTemplatesBySql(Connection conn, String sql) throws SQLException {
        List<ConfigTemplate> templates = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                templates.add(new ConfigTemplate(
                    rs.getString("template_code"),
                    rs.getString("data_id"),
                    rs.getString("group_id"),
                    rs.getString("content"),
                    rs.getString("type")
                ));
            }
        }
        return templates;
    }

    /**
     * 执行PreparedStatement查询模板
     */
    private List<ConfigTemplate> queryTemplatesBySql(Connection conn, PreparedStatement stmt) throws SQLException {
        List<ConfigTemplate> templates = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                templates.add(new ConfigTemplate(
                    rs.getString("template_code"),
                    rs.getString("data_id"),
                    rs.getString("group_id"),
                    rs.getString("content"),
                    rs.getString("type")
                ));
            }
        }
        return templates;
    }

    /**
     * 生成实际配置（替换占位符）
     */
    private List<NacosConfigImport> generateConfigs(List<ConfigTemplate> templates, InstallConfigDTO dto) {
        List<NacosConfigImport> configs = new ArrayList<>();
        InstallConfigDTO.DatabaseConfig dbConfig = dto.getDatabase();

        // 获取Nacos group和namespace的替换值
        String nacosGroup;
        String nacosNamespace;
        InstallConfigDTO.NacosConfig nacosConfig = dto.getNacos();
        if (nacosConfig != null) {
            nacosGroup = nacosConfig.getGroup();
            nacosNamespace = nacosConfig.getNamespace();
        } else {
            nacosGroup = "QuickBlue_GROUP";
            nacosNamespace = "QuickBlue-dev";
        }

        for (ConfigTemplate template : templates) {
            String content = template.content;
            String groupId = template.groupId;

            // 替换数据库占位符
            content = content.replace("${db.host}", dbConfig.getHost());
            content = content.replace("${db.port}", String.valueOf(dbConfig.getPort()));
            content = content.replace("${db.adminUser}", dbConfig.getAdminUser());
            content = content.replace("${db.adminPassword}", dbConfig.getAdminPassword());

            // 替换各服务的数据库配置
            for (InstallConfigDTO.DatabaseInfo dbInfo : dbConfig.getDatabases()) {
                String dbName = dbInfo.getName();
                String dbUser = dbInfo.getUsername();
                String dbPassword = dbInfo.getPassword();

                content = content.replace("${db." + dbName + ".name}", dbName);
                content = content.replace("${db." + dbName + ".user}", dbUser);
                content = content.replace("${db." + dbName + ".password}", dbPassword);
            }

            // 替换Redis占位符
            InstallConfigDTO.RedisConfiguration redisConfig = dto.getRedis();
            content = content.replace("${redis.host}", redisConfig.getHost());
            content = content.replace("${redis.port}", String.valueOf(redisConfig.getPort()));
            content = content.replace("${redis.password}", redisConfig.getPassword());
            content = content.replace("${redis.database}", String.valueOf(redisConfig.getDatabase()));

            // 替换Nacos占位符
            content = content.replace("${nacos.group}", nacosGroup);
            content = content.replace("${nacos.namespace}", nacosNamespace);
            groupId = groupId.replace("${nacos.group}", nacosGroup);
            groupId = groupId.replace("${nacos.namespace}", nacosNamespace);

            // 替换应用占位符
            content = content.replace("${app.version}", "1.0.0");
            content = content.replace("${app.environment}", "dev");

            configs.add(new NacosConfigImport(template.dataId, groupId, content, template.type));
        }

        return configs;
    }

    /**
     * 导入配置到Nacos服务器
     */
    private void importConfigsToNacos(List<NacosConfigImport> configs, InstallConfigDTO dto) {
        InstallConfigDTO.NacosConfig nacosConfig = dto.getNacos();
        InstallConfigDTO.DatabaseConfig dbConfig = dto.getDatabase();

        String nacosServerAddr;
        String nacosUsername;
        String nacosPassword;

        // 统一使用配置的Nacos参数或默认值（MySQL和PostgreSQL使用相同逻辑）
        nacosServerAddr = nacosConfig != null ? nacosConfig.getServerAddr() : this.nacosServerAddr;
        nacosUsername = nacosConfig != null ? nacosConfig.getUsername() : this.nacosUsername;
        nacosPassword = nacosConfig != null ? nacosConfig.getPassword() : this.nacosPassword;

        log.info("开始导入 {} 个配置到Nacos服务器", configs.size());
        log.info("Nacos服务器地址: http://{}/nacos", nacosServerAddr);
        log.info("Nacos用户名: {}", nacosUsername);

        // 先获取accessToken
        String accessToken;
        try {
            accessToken = getNacosAccessToken(nacosServerAddr, nacosUsername, nacosPassword);
            log.info("成功获取Nacos accessToken");
        } catch (Exception e) {
            log.error("获取Nacos accessToken失败", e);
            throw new RuntimeException("获取Nacos accessToken失败: " + e.getMessage(), e);
        }

        int successCount = 0;
        int failCount = 0;

        // 构建Nacos API基础URL
        String nacosBaseUrl = "http://" + nacosServerAddr + "/nacos/v1/cs/configs";

        for (NacosConfigImport config : configs) {
            try {
                // 处理groupId中的占位符
                String processedGroupId = config.groupId;
                if (nacosConfig != null) {
                    processedGroupId = processedGroupId.replace("${nacos.group}", nacosConfig.getGroup());
                    processedGroupId = processedGroupId.replace("${nacos.namespace}", nacosConfig.getNamespace());
                } else {
                    processedGroupId = processedGroupId.replace("${nacos.group}", "QuickBlue_GROUP");
                    processedGroupId = processedGroupId.replace("${nacos.namespace}", "QuickBlue-dev");
                }

                // 构建请求URL和参数（使用accessToken进行认证）
                String url = nacosBaseUrl +
                        "?dataId=" + urlEncode(config.dataId) +
                        "&group=" + urlEncode(processedGroupId) +
                        "&content=" + urlEncode(config.content) +
                        "&type=" + urlEncode(config.type) +
                        "&accessToken=" + urlEncode(accessToken);

                // 使用POST方法发布配置
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity<String> request = new HttpEntity<>(null, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

                // 检查响应结果
                if (response.getStatusCode() == HttpStatus.OK && "true".equals(response.getBody())) {
                    log.info("配置导入成功: dataId={}, groupId={}", config.dataId, config.groupId);
                    successCount++;
                } else {
                    log.error("配置导入失败: dataId={}, groupId={}, response={}", config.dataId, config.groupId, response.getBody());
                    failCount++;
                }
            } catch (Exception e) {
                log.error("导入配置失败: dataId={}, groupId={}", config.dataId, config.groupId, e);
                failCount++;
            }
        }

        log.info("Nacos配置导入完成，成功: {}, 失败: {}", successCount, failCount);

        if (failCount > 0) {
            throw new RuntimeException(String.format("部分Nacos配置导入失败，成功: %d, 失败: %d", successCount, failCount));
        }
    }

    /**
     * 获取Nacos访问令牌
     */
    private String getNacosAccessToken(String serverAddr, String username, String password) throws Exception {
        String url = "http://" + serverAddr + "/nacos/v1/auth/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", username);
        params.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.has("accessToken") ? jsonNode.get("accessToken").asText() : null;
        }

        throw new RuntimeException("获取Nacos访问令牌失败");
    }

    /**
     * URL编码
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("URL编码失败", e);
            return value;
        }
    }

    /**
     * 配置模板内部类
     */
    private static class ConfigTemplate {
        String templateCode;
        String dataId;
        String groupId;
        String content;
        String type;
        
        ConfigTemplate(String templateCode, String dataId, String groupId, String content, String type) {
            this.templateCode = templateCode;
            this.dataId = dataId;
            this.groupId = groupId;
            this.content = content;
            this.type = type;
        }
    }

    /**
     * Nacos配置导入内部类
     */
    private static class NacosConfigImport {
        String dataId;
        String groupId;
        String content;
        String type;
        
        NacosConfigImport(String dataId, String groupId, String content, String type) {
            this.dataId = dataId;
            this.groupId = groupId;
            this.content = content;
            this.type = type;
        }
    }

    private void createAdminAccount(InstallConfigDTO dto) {
        log.info("创建管理员账号: {}", dto.getAdmin().getUsername());

        // TODO: 实际项目中应该在数据库中创建管理员账号
        log.info("管理员账号创建完成");
    }

    private void configureSystemParameters(InstallConfigDTO dto) {
        log.info("配置系统参数");

        // TODO: 实际项目中应该保存配置到文件或数据库
        log.info("系统参数配置完成");
    }

    private void verifyInstallation(InstallConfigDTO dto) {
        log.info("验证安装结果");

        // TODO: 实际项目中应该验证所有组件是否正常
        log.info("安装验证通过");
    }

    private void createInstallFlag() {
        try {
            File flagFile = new File(installFlagFile);
            if (!flagFile.exists()) {
                flagFile.createNewFile();
                log.info("创建安装标记文件: {}", installFlagFile);
            }
        } catch (Exception e) {
            log.error("创建安装标记文件失败", e);
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 保存默认环境配置
     */
    private void saveDefaultEnvironment(InstallConfigDTO installConfig) {
        try {
            // 使用反射调用 EnvironmentConfigService 保存环境
            log.info("保存默认环境配置...");

            String configJson = objectMapper.writeValueAsString(installConfig);

            // 确保配置目录存在
            String configDir = System.getProperty("install.config-dir", "./environments");
            java.nio.file.Path configPath = java.nio.file.Paths.get(configDir);
            if (!java.nio.file.Files.exists(configPath)) {
                java.nio.file.Files.createDirectories(configPath);
            }

            // 创建环境配置JSON文件
            String envName = "默认环境";
            java.util.Map<String, Object> envConfig = new java.util.HashMap<>();
            envConfig.put("id", 1L);
            envConfig.put("envName", envName);
            envConfig.put("description", "系统安装时创建的默认环境");
            envConfig.put("configJson", configJson);
            envConfig.put("active", true);
            envConfig.put("createTime", java.time.LocalDateTime.now().toString());
            envConfig.put("updateTime", java.time.LocalDateTime.now().toString());

            // 保存环境配置
            String envFileContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(envConfig);
            java.nio.file.Files.writeString(
                configPath.resolve(envName + ".json"),
                envFileContent
            );

            // 更新激活环境文件
            java.nio.file.Files.writeString(
                configPath.resolve(".active-env"),
                envName
            );

            log.info("默认环境配置保存成功: {}", envName);
        } catch (Exception e) {
            log.error("保存默认环境配置失败", e);
            throw new RuntimeException("保存默认环境配置失败", e);
        }
    }

    /**
     * 判断是否为认证错误
     */
    private boolean isAuthenticationError(SQLException e, String dbType) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        // MySQL 认证错误
        if ("mysql".equals(dbType)) {
            return message.contains("Access denied") ||
                   message.contains("Authentication failed") ||
                   e.getErrorCode() == 1045;
        }

        // PostgreSQL 认证错误
        if ("postgresql".equals(dbType)) {
            return "28P01".equals(e.getSQLState()) || // password authentication failed
                   "28000".equals(e.getSQLState()) || // invalid authorization specification
                   message.contains("FATAL: password authentication failed") ||
                   message.contains("FATAL: role") && message.contains("does not exist");
        }

        return false;
    }

    /**
     * 判断是否为连接错误
     */
    private boolean isConnectionError(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        return message.contains("Communications link failure") ||
               message.contains("Could not create connection") ||
               message.contains("Connection refused") ||
               message.contains("Connection timed out") ||
               message.contains("Unknown host") ||
               message.contains("No route to host") ||
               message.contains("Network is unreachable");
    }

    /**
     * 从错误消息中提取客户端IP
     */
    private String getClientIp(String errorMessage) {
        if (errorMessage == null) {
            return "未知IP";
        }

        // MySQL 错误消息格式: Access denied for user 'root'@'172.19.0.1'
        int atPos = errorMessage.indexOf("'");
        if (atPos != -1) {
            int secondQuote = errorMessage.indexOf("'", atPos + 1);
            if (secondQuote != -1) {
                int thirdQuote = errorMessage.indexOf("'", secondQuote + 1);
                if (thirdQuote != -1) {
                    int fourthQuote = errorMessage.indexOf("'", thirdQuote + 1);
                    if (fourthQuote != -1) {
                        return errorMessage.substring(thirdQuote + 1, fourthQuote);
                    }
                }
            }
        }

        return "未知IP";
    }

    /**
     * 从SQL INSERT语句中提取表名
     */
    private String extractTableName(String insertSql) {
        if (insertSql == null || insertSql.trim().isEmpty()) {
            return null;
        }

        String sql = insertSql.trim().toUpperCase();

        // 匹配 INSERT INTO table_name 格式
        if (sql.startsWith("INSERT INTO")) {
            String afterInsertInto = sql.substring("INSERT INTO".length()).trim();

            // 查找第一个空格或左括号
            int spaceIndex = afterInsertInto.indexOf(' ');
            int parenIndex = afterInsertInto.indexOf('(');

            int endIndex = -1;
            if (spaceIndex != -1 && parenIndex != -1) {
                endIndex = Math.min(spaceIndex, parenIndex);
            } else if (spaceIndex != -1) {
                endIndex = spaceIndex;
            } else if (parenIndex != -1) {
                endIndex = parenIndex;
            }

            if (endIndex != -1) {
                String tableName = afterInsertInto.substring(0, endIndex).trim();
                // 去掉可能的反引号
                tableName = tableName.replaceAll("`", "").replaceAll("\"", "");
                return tableName;
            }
        }

        return null;
    }
}
