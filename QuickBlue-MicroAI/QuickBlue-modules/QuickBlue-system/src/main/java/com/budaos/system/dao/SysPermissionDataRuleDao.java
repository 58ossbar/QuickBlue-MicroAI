package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.system.domain.entity.SysPermissionDataRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据权限规则表DAO
 *
 * @author QuickBlue
 */
@Mapper
public interface SysPermissionDataRuleDao extends BaseMapper<SysPermissionDataRule> {

    /**
     * 查询数据权限规则列表
     */
    List<SysPermissionDataRule> queryRuleList(@Param("permissionId") Long permissionId);
}
