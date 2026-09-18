package com.budaos.support.domain.vo;

import lombok.Data;

/**
 * 文件下载
 */
@Data
public class AttachmentDownloadVO {

    /**
     * 文件字节数据
     */
    private byte[] data;

    /**
     * 文件元数据
     */
    private AttachmentMetadataVO metadata;

}
