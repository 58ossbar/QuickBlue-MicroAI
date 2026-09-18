package com.budaos.ai.llm.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.ai.app.service.IAiragAppService;
import com.budaos.ai.llm.entity.AiragKnowledge;
import com.budaos.ai.llm.mapper.AiragKnowledgeMapper;
import com.budaos.ai.llm.service.IAiragKnowledgeService;
import com.budaos.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * AI知识库 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiragKnowledgeServiceImpl extends ServiceImpl<AiragKnowledgeMapper, AiragKnowledge> implements IAiragKnowledgeService {

    private final IAiragAppService appService;

    @Override
    public List<AiragKnowledge> listEnabled() {
        return list(new LambdaQueryWrapper<AiragKnowledge>()
                .eq(AiragKnowledge::getStatus, "enable")
                .orderByDesc(AiragKnowledge::getCreateTime));
    }

    @Override
    public List<AiragKnowledge> listByType(String type) {
        return list(new LambdaQueryWrapper<AiragKnowledge>()
                .eq(AiragKnowledge::getType, type)
                .eq(AiragKnowledge::getStatus, "enable")
                .orderByDesc(AiragKnowledge::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(AiragKnowledge entity) {
        log.info("创建知识库, 知识库名称: {}", entity.getName());

        // 基础校验
        validateKnowledge(entity);

        // 检查名称唯一性
        checkNameUnique(null, entity.getName());

        // 设置默认状态
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("enable");
        }

        // 设置默认类型
        if (!StringUtils.hasText(entity.getType())) {
            entity.setType("knowledge");
        }

        // 设置租户信息
        setTenantInfo(entity);

        boolean result = super.save(entity);
        log.info("知识库创建成功, 知识库ID: {}, 知识库名称: {}", entity.getId(), entity.getName());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AiragKnowledge entity) {
        log.info("更新知识库, 知识库ID: {}, 知识库名称: {}", entity.getId(), entity.getName());

        if (!StringUtils.hasText(entity.getId())) {
            throw new BizException("知识库ID不能为空");
        }

        // 检查知识库是否存在
        AiragKnowledge existKnowledge = getById(entity.getId());

        // 基础校验
        validateKnowledge(entity);

        // 检查名称唯一性(排除自己)
        checkNameUnique(entity.getId(), entity.getName());

        // 更新时间
        entity.setUpdateTime(new Date());

        boolean result = super.updateById(entity);
        log.info("知识库更新成功, 知识库ID: {}", entity.getId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        String idStr = id.toString();
        log.info("删除知识库, 知识库ID: {}", idStr);

        if (!StringUtils.hasText(idStr)) {
            throw new BizException("知识库ID不能为空");
        }

        AiragKnowledge knowledge = getById(id);

        // 检查是否被应用引用
        if (appService.existsByKnowledgeId(idStr)) {
            if (knowledge != null) {
                throw new BizException("知识库[" + knowledge.getName() + "]正在被应用使用,无法删除");
            } else {
                throw new BizException("知识库正在被应用使用,无法删除");
            }
        }

        boolean result = super.removeById(id);
        log.info("知识库删除成功, 知识库ID: {}", idStr);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(List<?> list) {
        log.info("批量删除知识库, 删除数量: {}", list.size());

        if (list == null || list.isEmpty()) {
            throw new BizException("请选择要删除的知识库");
        }

        // 检查是否有被应用引用的知识库
        for (Object id : list) {
            if (appService.existsByKnowledgeId(id.toString())) {
                AiragKnowledge knowledge = getById(id.toString());
                throw new BizException("知识库[" + knowledge.getName() + "]正在被应用使用,无法删除");
            }
        }

        boolean result = super.removeByIds(list);
        log.info("批量删除知识库成功, 删除数量: {}", list.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableKnowledge(String id) {
        log.info("启用知识库, 知识库ID: {}", id);

        AiragKnowledge knowledge = getById(id);
        if (knowledge == null) {
            throw new BizException("知识库不存在, ID: " + id);
        }

        knowledge.setStatus("enable");
        knowledge.setUpdateTime(new Date());
        boolean result = super.updateById(knowledge);

        log.info("知识库启用成功, 知识库ID: {}", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableKnowledge(String id) {
        log.info("禁用知识库, 知识库ID: {}", id);

        AiragKnowledge knowledge = getById(id);
        if (knowledge == null) {
            throw new BizException("知识库不存在, ID: " + id);
        }

        knowledge.setStatus("disable");
        knowledge.setUpdateTime(new Date());
        boolean result = super.updateById(knowledge);

        log.info("知识库禁用成功, 知识库ID: {}", id);
        return result;
    }

    @Override
    public boolean existsReferencedByApp(String knowledgeId) {
        return appService.existsByKnowledgeId(knowledgeId);
    }

    /**
     * 校验知识库数据
     */
    private void validateKnowledge(AiragKnowledge knowledge) {
        if (knowledge == null) {
            throw new BizException("知识库数据不能为空");
        }

        if (!StringUtils.hasText(knowledge.getName())) {
            throw new BizException("知识库名称不能为空");
        }

        if (knowledge.getName().length() > 100) {
            throw new BizException("知识库名称不能超过100个字符");
        }

        if (StringUtils.hasText(knowledge.getDescr()) && knowledge.getDescr().length() > 500) {
            throw new BizException("知识库描述不能超过500个字符");
        }

        // 验证状态值
        if (StringUtils.hasText(knowledge.getStatus())) {
            String status = knowledge.getStatus();
            if (!status.equals("enable") && !status.equals("disable")) {
                throw new BizException("知识库状态不合法,只能是enable/disable");
            }
        }

        // 验证类型值
        if (StringUtils.hasText(knowledge.getType())) {
            String type = knowledge.getType();
            if (!type.equals("knowledge") && !type.equals("memory")) {
                throw new BizException("知识库类型不合法,只能是knowledge/memory");
            }
        }

        // 验证向量模型ID
        if (!StringUtils.hasText(knowledge.getEmbedId())) {
            throw new BizException("向量模型不能为空");
        }
    }

    /**
     * 检查名称唯一性
     */
    private void checkNameUnique(String id, String name) {
        LambdaQueryWrapper<AiragKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiragKnowledge::getName, name);

        if (StringUtils.hasText(id)) {
            wrapper.ne(AiragKnowledge::getId, id);
        }

        // 添加租户隔离
        wrapper.eq(AiragKnowledge::getTenantId, getTenantId());

        Long count = count(wrapper);
        if (count > 0) {
            throw new BizException("知识库名称[" + name + "]已存在");
        }
    }

    /**
     * 设置租户信息
     */
    private void setTenantInfo(AiragKnowledge knowledge) {
        try {
            // 从Sa-Token获取登录用户信息
            String userId = StpUtil.getLoginIdAsString();
            knowledge.setCreateBy(userId);
            knowledge.setUpdateBy(userId);

            // 设置租户ID
            String tenantId = StpUtil.getSession().getString("tenantId");
            if (StringUtils.hasText(tenantId)) {
                knowledge.setTenantId(tenantId);
            } else {
                knowledge.setTenantId(userId);
            }

            knowledge.setCreateTime(new Date());
            knowledge.setUpdateTime(new Date());
        } catch (Exception e) {
            log.warn("设置租户信息失败,可能是未登录状态", e);
            throw new BizException("获取用户信息失败,请重新登录");
        }
    }

    /**
     * 获取租户ID
     */
    private String getTenantId() {
        try {
            String tenantId = StpUtil.getSession().getString("tenantId");
            if (StringUtils.hasText(tenantId)) {
                return tenantId;
            }
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            log.warn("获取租户ID失败", e);
            throw new BizException("获取用户信息失败,请重新登录");
        }
    }
}
