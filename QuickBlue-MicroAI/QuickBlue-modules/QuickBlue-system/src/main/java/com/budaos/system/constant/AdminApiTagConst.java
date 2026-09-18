package com.budaos.system.constant;

import com.budaos.common.swagger.constant.OpenApiTagConst;

/**
 * Admin模块swagger标签常量
 *
 */
public class AdminApiTagConst extends OpenApiTagConst {

    public static class Business {
        public static final String MANAGER_CATEGORY = "分类管理";
        public static final String OA_NOTICE = "通知公告";
        public static final String QuickBlue_CINEMA = "票务-影城管理";

        public static final String QuickBlue_QuickBlue_TYPE="票务-票种管理";
        public static final String QuickBlue_QuickBlue_BATCH="票务-批次管理";
        public static final String QuickBlue_QuickBlue = "票务-票券管理";
        public static final String QuickBlue_STATISTICS = "票务-统计报表";

        public static final String QuickBlue_WRITE_OFF = "票务-核销";

        public static final String QuickBlue_QuickBlue_TYPE_STATISTICS ="票务-票种统计" ;
    }


    public static class System {

        public static final String SYSTEM_LOGIN = "系统-员工登录";

        public static final String SYSTEM_EMPLOYEE = "系统-员工管理";

        public static final String SYSTEM_DEPARTMENT = "系统-部门管理";

        public static final String SYSTEM_MENU = "系统-菜单";

        public static final String SYSTEM_DATA_SCOPE = "系统-系统-数据范围";

        public static final String SYSTEM_ROLE = "系统-角色";

        public static final String SYSTEM_ROLE_DATA_SCOPE = "系统-角色-数据范围";

        public static final String SYSTEM_ROLE_EMPLOYEE = "系统-角色-员工";

        public static final String SYSTEM_ROLE_MENU = "系统-角色-菜单";

        public static final String SYSTEM_POSITION = "系统-职务管理";

        public static final String SYSTEM_MESSAGE = "系统-消息";

    }

}
