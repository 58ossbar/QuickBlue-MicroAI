package com.budaos.common.security.context;

/**
 * 数据权限上下文
 * 用于在MyBatis拦截器中获取当前方法的数据权限SQL
 *
 * @author QuickBlue
 */
public class PermissionDataContext {

    private static final ThreadLocal<String> PERMISSION_SQL = new ThreadLocal<>();

    /**
     * 设置权限SQL
     */
    public static void setPermissionSql(String permissionSql) {
        PERMISSION_SQL.set(permissionSql);
    }

    /**
     * 获取权限SQL
     */
    public static String getPermissionSql() {
        return PERMISSION_SQL.get();
    }

    /**
     * 清除
     */
    public static void clear() {
        PERMISSION_SQL.remove();
    }
}
