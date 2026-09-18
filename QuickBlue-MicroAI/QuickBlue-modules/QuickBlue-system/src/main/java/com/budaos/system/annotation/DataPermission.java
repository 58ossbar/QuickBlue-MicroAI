package com.budaos.system.annotation;


import com.budaos.common.core.enums.DataPermissionInTypeEnum;
import com.budaos.system.strategy.AbstractDataPermissionStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据范围注解
 *
 * 用于标记需要进行数据权限控制的SQL查询方法
 *
 * @author budaos
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataPermission {

    /**
     * 数据权限配置编码
     * 对应 t_data_scope_config 表的 config_code 字段
     * 新增模块时，直接在数据库中插入配置记录，无需修改代码
     *
     * 例如：
     * - "EMPLOYEE" - 员工管理
     * - "NOTICE" - 通知权限
     * - "SALES" - 销售管理（新增时在数据库配置即可）
     */
    String configCode();

    /**
     * WhereIn类型（可选，默认为EMPLOYEE）
     */
    DataPermissionInTypeEnum whereInType() default DataPermissionInTypeEnum.EMPLOYEE;

    /**
     * 自定义策略实现类
     * 仅当whereInType为CUSTOM_STRATEGY时才使用此属性
     */
    Class<? extends AbstractDataPermissionStrategy> joinSqlImplClazz() default AbstractDataPermissionStrategy.class;

    /**
     * 参数名称（多个参数用逗号分隔）
     * 主要用于joinSqlImplClazz实现类根据参数进行不同的范围控制
     * 如果不使用CUSTOM_STRATEGY，可以不配置
     */
    String paramName() default "";

    /**
     * 第几个where条件（从0开始）
     * 用于指定在SQL的哪个where位置插入数据权限条件
     */
    int whereIndex() default 0;

    /**
     * Join SQL语句
     * 当whereInType为CUSTOM_STRATEGY类型时，此属性无效
     */
    String joinSql() default "";

    /**
     * "仅本人"数据权限时使用的字段名
     * 默认为 create_user_id
     * 如果表中没有此字段，需要指定其他字段（如 manager_id）
     */
    String selfScopeColumn() default "create_user_id";

}
