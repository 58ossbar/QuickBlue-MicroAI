package com.budaos.common.core.util;

import cn.dev33.satoken.stp.StpUtil;
import com.budaos.common.core.domain.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求工具类
 *
 * @author budaos
 */
public final class RequestContextUtil {

    /** Session 中完整用户信息的 Key */
    private static final String SESSION_USER_KEY = "requestUser";

    /** Session 中用户名的 Key */
    private static final String SESSION_USER_NAME_KEY = "userName";

    /** loginId 分隔符：类型:用户ID */
    private static final String LOGIN_ID_SEPARATOR = ":";

    private RequestContextUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 获取当前请求对象
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getRequest();
    }

    /**
     * 获取当前用户ID
     * 需要从 CurrentUser 中获取
     *
     * @return 用户ID
     */
    public static Long getRequestUserId() {
        try {
            LoginIdInfo info = parseLoginId(StpUtil.getLoginIdAsString());
            return info == null ? null : info.userId;
        } catch (Exception e) {
            // 未登录时忽略
            return null;
        }
    }

    /**
     * 获取当前用户信息
     *
     * @return CurrentUser
     */
    public static CurrentUser getRequestUser() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }

            // 优先从 Sa-Token Session 中获取完整的用户信息
            Object sessionUser = StpUtil.getSession().get(SESSION_USER_KEY);
            if (sessionUser instanceof CurrentUser) {
                return (CurrentUser) sessionUser;
            }

            // Session 中没有完整用户信息时，基于 loginId 构建基础的 CurrentUser
            LoginIdInfo info = parseLoginId(StpUtil.getLoginIdAsString());
            if (info == null || info.userId == null) {
                return null;
            }

            final String userName = (String) StpUtil.getSession().get(SESSION_USER_NAME_KEY);
            return new CurrentUser() {
                @Override
                public Long getUserId() {
                    return info.userId;
                }

                @Override
                public String getUserName() {
                    return userName;
                }

                @Override
                public Integer getUserType() {
                    return info.userType;
                }

                @Override
                public String getIp() {
                    return getClientIp();
                }

                @Override
                public String getUserAgent() {
                    HttpServletRequest request = getRequest();
                    if (request == null) {
                        return null;
                    }
                    return request.getHeader("User-Agent");
                }
            };
        } catch (Exception e) {
            // 未登录或 Session 异常时返回 null，由调用方处理
            return null;
        }
    }

    /**
     * 解析 loginId：格式为 类型:用户ID
     *
     * @param loginId 登录ID
     * @return 解析结果，无法解析时返回 null
     */
    private static LoginIdInfo parseLoginId(String loginId) {
        if (loginId == null) {
            return null;
        }
        try {
            String[] parts = loginId.split(LOGIN_ID_SEPARATOR);
            if (parts.length < 2) {
                return null;
            }
            LoginIdInfo info = new LoginIdInfo();
            info.userId = Long.parseLong(parts[1]);
            info.userType = Integer.parseInt(parts[0]);
            return info;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取当前请求URL
     *
     * @return URL
     */
    public static String getRequestUrl() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getRequestURI();
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getRemoteAddr();
    }

    /**
     * loginId 解析结果
     */
    private static class LoginIdInfo {
        Long userId;
        Integer userType;
    }
}
