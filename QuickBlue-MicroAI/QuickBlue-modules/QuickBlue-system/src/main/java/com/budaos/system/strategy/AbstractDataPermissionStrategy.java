package com.budaos.system.strategy;

import com.budaos.system.constant.DataPermissionViewTypeEnum;
import com.budaos.system.domain.dto.DataPermissionSqlConfig;

import java.util.Map;

/**
 * 数据范围策略抽象类
 *
 * 使用DataScopeWhereInTypeEnum.CUSTOM_STRATEGY类型时，
 * 需要继承此类实现自定义的数据权限SQL拼接逻辑
 *
 * @author budaos
 */
public abstract class AbstractDataPermissionStrategy {

    /**
     * 获取Join SQL字符串
     *
     * @param viewTypeEnum 数据可见范围类型
     * @param paramMap 参数Map
     * @param sqlConfigDTO SQL配置
     * @return Join SQL字符串
     */
    public abstract String getCondition(DataPermissionViewTypeEnum viewTypeEnum, Map<String, Object> paramMap, DataPermissionSqlConfig sqlConfigDTO);

}
