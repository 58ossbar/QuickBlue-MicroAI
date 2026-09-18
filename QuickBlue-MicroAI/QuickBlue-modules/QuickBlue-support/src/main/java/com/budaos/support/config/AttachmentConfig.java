package com.budaos.support.config;

import com.budaos.support.service.FileStorageService;
import com.budaos.support.service.impl.CloudFileStorageService;
import com.budaos.support.service.impl.LocalFileStorageServiceImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * 文件上传 配置
 */
@Data
@Configuration
public class AttachmentConfig implements WebMvcConfigurer {

    private static final String HTTPS = "https://";

    private static final String HTTP = "http://";

    private static final String MODE_CLOUD = "cloud";

    private static final String MODE_LOCAL = "local";

    @Value("${file.storage.cloud.region:oss-cn-hangzhou}")
    private String region;

    @Value("${file.storage.cloud.endpoint:oss-cn-hangzhou.aliyuncs.com}")
    private String endpoint;

    @Value("${file.storage.cloud.bucket-name:QuickBlue-files}")
    private String bucketName;

    @Value("${file.storage.cloud.access-key:}")
    private String accessKey;

    @Value("${file.storage.cloud.secret-key:}")
    private String secretKey;

    @Value("${file.storage.cloud.private-url-expire-seconds:3600}")
    private Long privateUrlExpireSeconds;

    @Value("${file.storage.cloud.url-prefix:https://QuickBlue-files.oss-cn-hangzhou.aliyuncs.com/}")
    private String urlPrefix;

    @Value("${file.storage.local.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.storage.mode:local}")
    private String mode;

    /**
     * 初始化 云oss client 配置
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "file.storage", name = {"mode"}, havingValue = MODE_CLOUD)
    public S3Client initAmazonS3() {
        return S3Client.builder()
                .region(Region.AWS_GLOBAL)
                .endpointOverride(URI.create((urlPrefix.startsWith(HTTPS) ? HTTPS : HTTP) + endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .build();
    }

    @Bean("cloudFileStorageService")
    @ConditionalOnProperty(prefix = "file.storage", name = {"mode"}, havingValue = MODE_CLOUD)
    public FileStorageService initCloudFileService() {
        return new CloudFileStorageService();
    }

    @Bean("localFileStorageService")
    @Primary
    @ConditionalOnProperty(prefix = "file.storage", name = {"mode"}, havingValue = MODE_LOCAL)
    public FileStorageService initLocalFileService() {
        return new LocalFileStorageServiceImpl();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (MODE_LOCAL.equals(mode)) {
            String path = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";

            // 添加 public 目录的直接访问（兼容性）
            registry.addResourceHandler("/public/**")
                    .addResourceLocations("file:" + path + "public/")
                    .setCachePeriod(3600); // 缓存1小时
        }
    }
}
