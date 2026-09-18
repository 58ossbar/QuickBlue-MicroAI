package com.budaos.gateway.service;

import com.budaos.gateway.domain.dto.DbConnectionDTO;
import com.budaos.gateway.domain.entity.EnvironmentConfigEntity;
import com.budaos.gateway.domain.dto.InstallConfigDTO;

import java.util.List;

/**
 * 环境配置管理服务
 *
 * @author budaos
 */
public interface EnvironmentConfigService {

    /**
     * 保存安装配置为环境
     *
     * @param envName 环境名称
     * @param installConfig 安装配置
     * @return 环境ID
     */
    Long saveEnvironmentConfig(String envName, InstallConfigDTO installConfig);

    /**
     * 获取所有环境配置
     *
     * @return 环境列表
     */
    List<EnvironmentConfigEntity> listAllEnvironments();

    /**
     * 获取当前激活的环境
     *
     * @return 环境配置
     */
    EnvironmentConfigEntity getActiveEnvironment();

    /**
     * 切换环境
     *
     * @param envName 环境名称
     * @return 是否成功
     */
    Boolean switchEnvironment(String envName);

    /**
     * 删除环境配置
     *
     * @param envName 环境名称
     * @return 是否成功
     */
    Boolean deleteEnvironment(String envName);

    /**
     * 测试环境数据库连接
     *
     * @param envName 环境名称
     * @return 是否成功
     */
    Boolean testEnvironmentDbConnection(String envName);

    /**
     * 导出环境配置
     *
     * @param envName 环境名称
     * @return 配置JSON
     */
    String exportEnvironmentConfig(String envName);

    /**
     * 导入环境配置
     *
     * @param configJson 配置JSON
     * @param envName 环境名称
     * @return 是否成功
     */
    Boolean importEnvironmentConfig(String configJson, String envName);

    /**
     * 更新环境的数据库连接信息
     *
     * @param envName 环境名称
     * @param dbConnection 数据库连接信息
     * @return 是否成功
     */
    Boolean updateEnvironmentDb(String envName, DbConnectionDTO dbConnection);
}
