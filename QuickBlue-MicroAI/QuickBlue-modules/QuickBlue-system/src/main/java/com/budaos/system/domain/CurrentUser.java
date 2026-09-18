package com.budaos.system.domain;

import cn.dev33.satoken.stp.StpUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户信息
 *
 * @author budaos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工UID
     */
    private String employeeUid;

    /**
     * 登录账号
     */
    private String loginName;

    /**
     * 员工姓名
     */
    private String actualName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 岗位ID
     */
    private Long positionId;

    /**
     * 获取当前登录用户
     */
    public static CurrentUser getCurrentUser() {
        Object userObj = StpUtil.getSession().get("user");
        if (userObj != null) {
            return (CurrentUser) userObj;
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        CurrentUser user = getCurrentUser();
        return user != null ? user.getEmployeeId() : null;
    }

    /**
     * 获取当前登录账号
     */
    public static String getCurrentLoginName() {
        CurrentUser user = getCurrentUser();
        return user != null ? user.getLoginName() : null;
    }
}
