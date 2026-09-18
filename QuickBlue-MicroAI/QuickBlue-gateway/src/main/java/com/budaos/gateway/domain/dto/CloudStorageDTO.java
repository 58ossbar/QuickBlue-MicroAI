package com.budaos.gateway.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 云存储DTO
 *
 * @author budaos
 */
@Data
@Schema(description = "云存储信息")
public class CloudStorageDTO {

    @Schema(description = "云服务商", example = "aliyun")
    private String provider;

    @Schema(description = "区域", example = "oss-cn-hangzhou")
    private String region;

    @Schema(description = "存储桶名称", example = "quickblue-files")
    private String bucketName;

    @Schema(description = "访问密钥ID")
    private String accessKeyId;

    @Schema(description = "访问密钥Secret")
    private String accessKeySecret;

    @Schema(description = "端点", example = "oss-cn-hangzhou.aliyuncs.com")
    private String endpoint;
}
