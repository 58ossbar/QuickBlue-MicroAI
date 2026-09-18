/*
 * 服务监控
 *
 */
import { getRequest } from '/@/lib/axios';

// 获取服务监控概览（所有服务健康状态 + JVM 指标汇总）
export const getServicesOverview = () => {
    return getRequest('/support/monitor/overview');
};

// 获取指定服务的详细指标（内存、线程、运行时长、QPS）
export const getServiceMetrics = (serviceName) => {
    return getRequest('/support/monitor/metrics', { serviceName });
};
