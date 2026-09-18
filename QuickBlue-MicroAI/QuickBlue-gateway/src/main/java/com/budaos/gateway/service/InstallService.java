package com.budaos.gateway.service;

import com.budaos.gateway.domain.dto.*;
import com.budaos.gateway.domain.vo.EnvironmentCheckVO;
import com.budaos.gateway.domain.vo.InstallProgressVO;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * 安装服务接口
 *
 * @author budaos
 */
public interface InstallService {

    /**
     * 检查环境
     */
    EnvironmentCheckVO checkEnvironment();

    /**
     * 测试数据库连接
     */
    void testDbConnection(DbConnectionDTO dto);

    /**
     * 测试Redis连接
     */
    void testRedisConnection(RedisConnectionDTO dto);

    /**
     * 测试Nacos连接
     */
    Map<String, String> testNacosConnection(NacosConnectionDTO dto);

    /**
     * 测试云存储连接
     */
    void testCloudStorage(CloudStorageDTO dto);

    /**
     * 检查端口占用
     */
    boolean checkPort(Integer port);

    /**
     * 开始安装
     */
    InstallProgressVO startInstall(InstallConfigDTO dto);

    /**
     * 获取安装进度
     */
    InstallProgressVO getInstallProgress();

    /**
     * 下载安装配置
     */
    Mono<Void> downloadConfig(ServerWebExchange exchange);

    /**
     * 检查系统是否已安装
     */
    boolean isInstalled();

    /**
     * 获取系统信息
     */
    Map<String, Object> getSystemInfo();
}
