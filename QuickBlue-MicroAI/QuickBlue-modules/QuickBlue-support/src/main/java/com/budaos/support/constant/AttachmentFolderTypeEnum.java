package com.budaos.support.constant;

import com.budaos.common.core.domain.BaseEnumeration;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AttachmentFolderTypeEnum implements BaseEnumeration {
    /**
     * 通用
     */
    COMMON(1, "public/common/", "通用"),

    /**
     * 公告
     */
    NOTICE(2, "public/notice/", "公告"),

    /**
     * 帮助中心
     */
    HELP_DOC(3, "public/help-doc/", "帮助中心"),

    /**
     * 意见反馈
     */
    FEEDBACK(4, "public/feedback/", "意见反馈"),

    /**
     * 票券二维码
     */
    QuickBlue_QRCODE(5, "public/QuickBlue/qrcode/", "票券二维码"),

    /**
     * 票券批量二维码
     */
    QuickBlue_BATCH_QRCODE(6, "public/QuickBlue/batch-qrcode/", "票券批量二维码"),

    /**
     * 公共文件夹前缀
     */
    FOLDER_PUBLIC(null, "public/", "公共文件夹"),

    /**
     * 私有文件夹前缀
     */
    FOLDER_PRIVATE(null, "private/", "私有文件夹"),

    ;

    private final Integer value;

    private final String folder;

    private final String desc;
}
