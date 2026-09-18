package com.budaos.support.domain;

import com.budaos.common.core.domain.CurrentUser;
import lombok.Data;

/**
 * 请求用户信息实现类
 *
 * @author budaos
 */
@Data
public class CurrentUserImpl implements CurrentUser {

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 请求IP
     */
    private String ip;

    /**
     * 请求user-agent
     */
    private String userAgent;
}
