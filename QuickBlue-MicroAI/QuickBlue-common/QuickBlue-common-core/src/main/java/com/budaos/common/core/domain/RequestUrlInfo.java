package com.budaos.common.core.domain;

import lombok.Data;

/**
 * 请求URL信息
 *
 */
@Data
public class RequestUrlInfo {

    /**
     * URL路径
     */
    private String url;

    /**
     * 方法名称（类名.方法名）
     */
    private String name;

    /**
     * 方法注释
     */
    private String comment;
}
