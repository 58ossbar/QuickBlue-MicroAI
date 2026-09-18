package com.budaos.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.dao.SysPermissionDataRuleDao;
import com.budaos.system.domain.entity.SysPermissionDataRule;
import com.budaos.system.service.SysPermissionDataRuleService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限规则服务实现
 *
 * @author QuickBlue
 */
@Service
public class SysPermissionDataRuleServiceImpl implements SysPermissionDataRuleService {

    @Resource
    private SysPermissionDataRuleDao permissionDataRuleDao;

    @Override
    public List<SysPermissionDataRule> queryRuleList(Long permissionId) {
        LambdaQueryWrapper<SysPermissionDataRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermissionDataRule::getPermissionId, permissionId);
        wrapper.eq(SysPermissionDataRule::getStatus, 1); // 只查询启用的规则
        wrapper.orderByAsc(SysPermissionDataRule::getSortOrder);
        return permissionDataRuleDao.selectList(wrapper);
    }

    @Override
    public PageResponse<SysPermissionDataRule> queryPage(Long permissionId, Integer pageNum, Integer pageSize) {
        Page<SysPermissionDataRule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysPermissionDataRule> wrapper = new LambdaQueryWrapper<>();
        if (permissionId != null) {
            wrapper.eq(SysPermissionDataRule::getPermissionId, permissionId);
        }
        wrapper.orderByAsc(SysPermissionDataRule::getSortOrder);
        Page<SysPermissionDataRule> result = permissionDataRuleDao.selectPage(page, wrapper);

        PageResponse<SysPermissionDataRule> pageResult = new PageResponse<>();
        pageResult.setPageNum((long) pageNum);
        pageResult.setPageSize((long) pageSize);
        pageResult.setTotal(result.getTotal());
        pageResult.setList(result.getRecords());
        return pageResult;
    }

    @Override
    public SysPermissionDataRule selectById(Long ruleId) {
        return permissionDataRuleDao.selectById(ruleId);
    }

    @Override
    public ApiResult<String> add(SysPermissionDataRule rule) {
        // 参数校验
        if (StrUtil.isBlank(rule.getRuleName())) {
            return ApiResult.userErrorParam("规则名称不能为空");
        }
        if (StrUtil.isBlank(rule.getRuleColumn())) {
            return ApiResult.userErrorParam("规则字段不能为空");
        }
        if (StrUtil.isBlank(rule.getRuleConditions())) {
            return ApiResult.userErrorParam("规则条件不能为空");
        }
        if (StrUtil.isBlank(rule.getRuleValue())) {
            return ApiResult.userErrorParam("规则值不能为空");
        }

        permissionDataRuleDao.insert(rule);
        return ApiResult.ok();
    }

    @Override
    public ApiResult<String> update(SysPermissionDataRule rule) {
        if (rule.getRuleId() == null) {
            return ApiResult.userErrorParam("规则ID不能为空");
        }

        // 参数校验
        if (StrUtil.isBlank(rule.getRuleName())) {
            return ApiResult.userErrorParam("规则名称不能为空");
        }
        if (StrUtil.isBlank(rule.getRuleColumn())) {
            return ApiResult.userErrorParam("规则字段不能为空");
        }
        if (StrUtil.isBlank(rule.getRuleConditions())) {
            return ApiResult.userErrorParam("规则条件不能为空");
        }
        if (StrUtil.isBlank(rule.getRuleValue())) {
            return ApiResult.userErrorParam("规则值不能为空");
        }

        permissionDataRuleDao.updateById(rule);
        return ApiResult.ok();
    }

    @Override
    public ApiResult<String> delete(Long ruleId) {
        if (ruleId == null) {
            return ApiResult.userErrorParam("规则ID不能为空");
        }
        permissionDataRuleDao.deleteById(ruleId);
        return ApiResult.ok();
    }

    @Override
    public ApiResult<String> batchDelete(List<Long> ruleIds) {
        if (CollectionUtils.isEmpty(ruleIds)) {
            return ApiResult.userErrorParam("规则ID列表不能为空");
        }
        permissionDataRuleDao.deleteBatchIds(ruleIds);
        return ApiResult.ok();
    }
}
