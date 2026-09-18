package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传响应
 */
@Data
public class AttachmentUploadVO {

    @Schema(description = "文件id")
    private Long fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "fileUrl")
    private String fileUrl;

    @Schema(description = "fileKey")
    private String fileKey;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;
}
