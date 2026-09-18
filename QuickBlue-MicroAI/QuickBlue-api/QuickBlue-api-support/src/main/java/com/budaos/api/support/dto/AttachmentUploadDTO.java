package com.budaos.api.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传响应DTO
 *
 * @author budaos
 */
@Data
@Schema(description = "文件上传响应")
public class AttachmentUploadDTO {

    @Schema(description = "文件id")
    private Long fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "文件Key")
    private String fileKey;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;
}
