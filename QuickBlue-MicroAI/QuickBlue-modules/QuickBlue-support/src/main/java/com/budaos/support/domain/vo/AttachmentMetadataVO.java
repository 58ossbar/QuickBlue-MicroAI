package com.budaos.support.domain.vo;

import lombok.Data;

/**
 * 文件元数据
 */
@Data
public class AttachmentMetadataVO {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小/字节
     */
    private Long fileSize;

    /**
     * 文件格式
     */
    private String fileFormat;
}
