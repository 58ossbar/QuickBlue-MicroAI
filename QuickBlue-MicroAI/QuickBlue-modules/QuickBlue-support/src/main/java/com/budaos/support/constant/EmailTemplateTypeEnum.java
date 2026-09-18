package com.budaos.support.constant;

import com.budaos.common.core.domain.BaseEnumeration;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邮件模板类型
 *
 * @author budaos
 */
@Getter
@AllArgsConstructor
public enum EmailTemplateTypeEnum implements BaseEnumeration {

    STRING("string", "字符串替代器"),

    FREEMARKER("freemarker", "freemarker模板引擎");

    private final String value;

    private final String desc;
}
