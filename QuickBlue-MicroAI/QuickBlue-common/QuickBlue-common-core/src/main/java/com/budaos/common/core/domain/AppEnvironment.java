package com.budaos.common.core.domain;

import lombok.Data;

/**
 * 系统环境信息
 *
 */
@Data
public class AppEnvironment {

    /**
     * 是否为生产环境
     */
    private Boolean prod;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 当前环境
     */
    private String currentEnvironment;

    public AppEnvironment() {
    }

    public AppEnvironment(Boolean prod, String projectName, String currentEnvironment) {
        this.prod = prod;
        this.projectName = projectName;
        this.currentEnvironment = currentEnvironment;
    }

    /**
     * 是否为生产环境
     */
    public boolean isProd() {
        return "prod".equalsIgnoreCase(currentEnvironment);
    }

    /**
     * 是否为开发环境
     */
    public boolean isDev() {
        return "dev".equalsIgnoreCase(currentEnvironment);
    }

    /**
     * 是否为测试环境
     */
    public boolean isTest() {
        return "test".equalsIgnoreCase(currentEnvironment);
    }
}
