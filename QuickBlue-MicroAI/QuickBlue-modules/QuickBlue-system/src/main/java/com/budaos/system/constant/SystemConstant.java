package com.budaos.system.constant;

/**
 * 系统常量
 *
 * @author budaos
 */
import com.budaos.common.core.constant.StringConstants;

public interface SystemConstant {

    /**
     * 万能密码的 sa token loginId 前缀
     */
    String SUPER_PASSWORD_LOGIN_ID_PREFIX = "S";

    /**
     * 登录ID分隔符（Sa-Token 1.46 起 loginId 禁止包含冒号，统一复用 common-core 的定义）
     */
    String LOGIN_ID_SEPARATOR = StringConstants.LOGIN_ID_SEPARATOR;

}
