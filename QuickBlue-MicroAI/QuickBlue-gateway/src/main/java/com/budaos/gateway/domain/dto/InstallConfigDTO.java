package com.budaos.gateway.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 安装配置DTO
 *
 * @author budaos
 */
@Data
@Schema(description = "安装配置")
public class InstallConfigDTO {

    @Schema(description = "安装类型", example = "simple")
    private String installType;

    @Schema(description = "数据库配置")
    private DatabaseConfig database;

    @Schema(description = "Redis配置")
    private RedisConfiguration redis;

    @Schema(description = "Nacos配置")
    private NacosConfig nacos;

    @Schema(description = "服务配置")
    private ServiceConfig service;

    @Schema(description = "存储配置")
    private StorageConfig storage;

    @Schema(description = "管理员配置")
    private AdminConfig admin;

    @Data
    @Schema(description = "数据库配置")
    public static class DatabaseConfig {
        @Schema(description = "数据库类型")
        private String dbType;

        @Schema(description = "主机地址")
        private String host;

        @Schema(description = "端口")
        private Integer port;

        @Schema(description = "管理员账号")
        private String adminUser;

        @Schema(description = "管理员密码")
        private String adminPassword;

        @Schema(description = "数据库列表")
        private List<DatabaseInfo> databases;
    }

    @Data
    @Schema(description = "数据库信息")
    public static class DatabaseInfo {
        @Schema(description = "数据库名称")
        private String name;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "密码")
        private String password;
    }

    @Data
    @Schema(description = "Redis配置")
    public static class RedisConfiguration {
        @Schema(description = "主机地址")
        private String host;

        @Schema(description = "端口")
        private Integer port;

        @Schema(description = "密码")
        private String password;

        @Schema(description = "数据库编号")
        private Integer database;

        @Schema(description = "超时时间")
        private Integer timeout;
    }

    @Data
    @Schema(description = "Nacos配置")
    public static class NacosConfig {
        @Schema(description = "服务器地址")
        private String serverAddr;

        @Schema(description = "命名空间")
        private String namespace;

        @Schema(description = "配置分组")
        private String group;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "密码")
        private String password;
    }

    @Data
    @Schema(description = "服务配置")
    public static class ServiceConfig {
        @Schema(description = "网关URL")
        private String gatewayUrl;

        @Schema(description = "网关端口")
        private Integer gatewayPort;

        @Schema(description = "支撑服务端口")
        private Integer supportPort;

        @Schema(description = "系统服务端口")
        private Integer systemPort;

        @Schema(description = "业务服务端口")
        private Integer businessPort;

        @Schema(description = "AI服务端口")
        private Integer aiPort;

        @Schema(description = "管理后台端口")
        private Integer adminPort;
    }

    @Data
    @Schema(description = "存储配置")
    public static class StorageConfig {
        @Schema(description = "存储模式")
        private String mode;

        @Schema(description = "本地存储配置")
        private LocalStorageConfig local;

        @Schema(description = "云存储配置")
        private CloudStorageInfo cloud;
    }

    @Data
    @Schema(description = "本地存储配置")
    public static class LocalStorageConfig {
        @Schema(description = "上传路径")
        private String uploadPath;

        @Schema(description = "访问前缀")
        private String urlPrefix;

        @Schema(description = "最大文件大小(MB)")
        private Integer maxFileSize;
    }

    @Data
    @Schema(description = "云存储配置")
    public static class CloudStorageInfo {
        @Schema(description = "云服务商")
        private String provider;

        @Schema(description = "区域")
        private String region;

        @Schema(description = "存储桶名称")
        private String bucketName;

        @Schema(description = "访问密钥ID")
        private String accessKeyId;

        @Schema(description = "访问密钥Secret")
        private String accessKeySecret;

        @Schema(description = "端点")
        private String endpoint;
    }

    @Data
    @Schema(description = "管理员配置")
    public static class AdminConfig {
        @Schema(description = "用户名")
        private String username;

        @Schema(description = "密码")
        private String password;

        @Schema(description = "真实姓名")
        private String realName;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "部门")
        private String department;

        @Schema(description = "职位")
        private String position;
    }
}
