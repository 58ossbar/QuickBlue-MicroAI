package com.budaos.common.core.constant;

/**
 * 缓存key常量
 *
 * @author budaos
 */
public class CacheKeyConstants {

    /**
     * 字典缓存
     */
    public static class Dict {

        /**
         * 字典数据缓存
         */
        public static final String DICT_DATA = "dictData";

    }

    /**
     * 部门相关缓存
     */
    public static class Department {

        /**
         * 部门列表
         */
        public static final String DEPARTMENT_LIST_CACHE = "department_list_cache";

        /**
         * 部门树
         */
        public static final String DEPARTMENT_TREE_CACHE = "department_tree_cache";

        /**
         * 某个部门以及下级的id列表
         */
        public static final String DEPARTMENT_SELF_CHILDREN_CACHE = "department_self_children_cache";

        /**
         * 部门路径 缓存
         */
        public static final String DEPARTMENT_PATH_CACHE = "department_path_cache";

    }

    /**
     * 区域相关缓存
     */
    public static class Region {

        public static final String REGION_LIST_CACHE = "region_list_cache";

        public static final String REGION_TREE_CACHE = "region_tree_cache";

        public static final String REGION_SELF_CHILDREN_CACHE = "region_self_children_cache";
    }

    /**
     * 登录相关
     */
    public static class Login {

        /**
         * 请求用户信息
         */
        public static final String REQUEST_EMPLOYEE = "login_request_employee";

        /**
         * 请求用户信息权限
         */
        public static final String USER_PERMISSION = "login_user_permission";
    }

}
