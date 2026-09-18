package com.budaos.system.domain.dto;

import com.budaos.common.core.enums.DataPermissionInTypeEnum;
import com.budaos.system.strategy.AbstractDataPermissionStrategy;
import lombok.Data;

/**
 * 数据范围SQL配置
 *
 * 用于存储DataScope注解的配置信息
 *
 * @author budaos
 */
@Data
public class DataPermissionSqlConfig {

    /**
     * 数据权限配置编码
     * 对应 t_data_scope_config 表的 config_code 字段
     */
    private String configCode;

    /**
     * Join SQL语句
     */
    private String joinSql;

    /**
     * Where条件索引
     */
    private Integer whereIndex;

    /**
     * WhereIn类型
     */
    private DataPermissionInTypeEnum dataScopeWhereInType;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * Join SQL实现类
     */
    private Class<? extends AbstractDataPermissionStrategy> joinSqlImplClazz;

    /**
     * "仅本人"数据权限时使用的字段名
     */
    private String selfScopeColumn;

}
