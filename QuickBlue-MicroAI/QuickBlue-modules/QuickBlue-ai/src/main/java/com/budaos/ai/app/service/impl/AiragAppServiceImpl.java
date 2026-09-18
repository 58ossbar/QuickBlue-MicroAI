package com.budaos.ai.app.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.ai.app.entity.AiragApp;
import com.budaos.ai.app.mapper.AiragAppMapper;
import com.budaos.ai.app.service.IAiragAppService;
import com.budaos.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * AI应用 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiragAppServiceImpl extends ServiceImpl<AiragAppMapper, AiragApp> implements IAiragAppService {

    private final AiragAppMapper appMapper;

    @Override
    public AiragApp getById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BizException("应用ID不能为空");
        }
        AiragApp app = super.getById(id);
        if (app == null) {
            throw new BizException("应用不存在");
        }
        return app;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveApp(AiragApp app) {
        log.info("创建AI应用, 应用名称: {}", app.getName());

        // 基础校验
        validateApp(app);

        // 检查名称唯一性
        checkNameUnique(null, app.getName());

        // 设置默认状态
        if (!StringUtils.hasText(app.getStatus())) {
            app.setStatus("disable");
        }

        // 设置默认历史消息数
        if (app.getMsgNum() == null) {
            app.setMsgNum(10);
        } else if (app.getMsgNum() < 0 || app.getMsgNum() > 50) {
            throw new BizException("历史消息数必须在0-50之间");
        }

        // 设置默认记忆开关
        if (app.getIzOpenMemory() == null) {
            app.setIzOpenMemory(0);
        }

        // 设置租户ID
        setTenantInfo(app);

        boolean result = save(app);
        log.info("AI应用创建成功, 应用ID: {}, 应用名称: {}", app.getId(), app.getName());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateApp(AiragApp app) {
        log.info("更新AI应用, 应用ID: {}, 应用名称: {}", app.getId(), app.getName());

        if (!StringUtils.hasText(app.getId())) {
            throw new BizException("应用ID不能为空");
        }

        // 检查应用是否存在
        AiragApp existApp = getById(app.getId());

        // 基础校验
        validateApp(app);

        // 检查名称唯一性(排除自己)
        checkNameUnique(app.getId(), app.getName());

        // 验证历史消息数
        if (app.getMsgNum() != null && (app.getMsgNum() < 0 || app.getMsgNum() > 50)) {
            throw new BizException("历史消息数必须在0-50之间");
        }

        // 更新时间
        app.setUpdateTime(new Date());

        boolean result = updateById(app);
        log.info("AI应用更新成功, 应用ID: {}", app.getId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteApp(String id) {
        log.info("删除AI应用, 应用ID: {}", id);

        if (!StringUtils.hasText(id)) {
            throw new BizException("应用ID不能为空");
        }

        AiragApp app = getById(id);

        // 检查应用状态
        if ("release".equals(app.getStatus())) {
            throw new BizException("已发布的应用不能删除,请先禁用");
        }

        boolean result = removeById(id);
        log.info("AI应用删除成功, 应用ID: {}", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(List<String> ids) {
        log.info("批量删除AI应用, 应用ID列表: {}", ids);

        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择要删除的应用");
        }

        // 检查是否有已发布的应用
        List<AiragApp> apps = listByIds(ids);
        for (AiragApp app : apps) {
            if ("release".equals(app.getStatus())) {
                throw new BizException("已发布的应用[" + app.getName() + "]不能删除,请先禁用");
            }
        }

        boolean result = removeByIds(ids);
        log.info("批量删除AI应用成功, 删除数量: {}", ids.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishApp(String id) {
        log.info("发布AI应用, 应用ID: {}", id);

        AiragApp app = getById(id);

        if ("release".equals(app.getStatus())) {
            throw new BizException("应用已是发布状态");
        }

        if ("disable".equals(app.getStatus())) {
            throw new BizException("请先启用应用后再发布");
        }

        app.setStatus("release");
        app.setUpdateTime(new Date());
        boolean result = updateById(app);

        log.info("AI应用发布成功, 应用ID: {}", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableApp(String id) {
        log.info("禁用AI应用, 应用ID: {}", id);

        AiragApp app = getById(id);

        app.setStatus("disable");
        app.setUpdateTime(new Date());
        boolean result = updateById(app);

        log.info("AI应用禁用成功, 应用ID: {}", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableApp(String id) {
        log.info("启用AI应用, 应用ID: {}", id);

        AiragApp app = getById(id);

        app.setStatus("enable");
        app.setUpdateTime(new Date());
        boolean result = updateById(app);

        log.info("AI应用启用成功, 应用ID: {}", id);
        return result;
    }

    @Override
    public boolean existsByModelId(String modelId) {
        return count(new LambdaQueryWrapper<AiragApp>()
                .eq(AiragApp::getModelId, modelId)) > 0;
    }

    @Override
    public boolean existsByKnowledgeId(String knowledgeId) {
        List<AiragApp> apps = list();
        for (AiragApp app : apps) {
            String knowledgeIds = app.getKnowledgeIds();
            if (StringUtils.hasText(knowledgeIds) && knowledgeIds.contains(knowledgeId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验应用数据
     */
    private void validateApp(AiragApp app) {
        if (app == null) {
            throw new BizException("应用数据不能为空");
        }

        if (!StringUtils.hasText(app.getName())) {
            throw new BizException("应用名称不能为空");
        }

        if (app.getName().length() > 100) {
            throw new BizException("应用名称不能超过100个字符");
        }

        if (StringUtils.hasText(app.getDescr()) && app.getDescr().length() > 500) {
            throw new BizException("应用描述不能超过500个字符");
        }

        // 验证状态值
        if (StringUtils.hasText(app.getStatus())) {
            String status = app.getStatus();
            if (!status.equals("enable") && !status.equals("disable") && !status.equals("release")) {
                throw new BizException("应用状态不合法,只能是enable/disable/release");
            }
        }

        // 验证类型值
        if (StringUtils.hasText(app.getType())) {
            String type = app.getType();
            if (!type.equals("chat") && !type.equals("agent") && !type.equals("workflow")) {
                throw new BizException("应用类型不合法,只能是chat/agent/workflow");
            }
        }

        // 验证记忆开关
        if (app.getIzOpenMemory() != null && app.getIzOpenMemory() != 0 && app.getIzOpenMemory() != 1) {
            throw new BizException("记忆开关只能为0或1");
        }
    }

    /**
     * 检查名称唯一性
     */
    private void checkNameUnique(String id, String name) {
        LambdaQueryWrapper<AiragApp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiragApp::getName, name);

        if (StringUtils.hasText(id)) {
            wrapper.ne(AiragApp::getId, id);
        }

        // 添加租户隔离
        wrapper.eq(AiragApp::getTenantId, getTenantId());

        Long count = count(wrapper);
        if (count > 0) {
            throw new BizException("应用名称[" + name + "]已存在");
        }
    }

    /**
     * 设置租户信息
     */
    private void setTenantInfo(AiragApp app) {
        try {
            // 从Sa-Token获取登录用户信息
            String userId = StpUtil.getLoginIdAsString();
            app.setCreateBy(userId);
            app.setUpdateBy(userId);

            // 设置租户ID(假设从token中获取,根据实际情况调整)
            String tenantId = StpUtil.getSession().getString("tenantId");
            if (StringUtils.hasText(tenantId)) {
                app.setTenantId(tenantId);
            } else {
                // 如果没有租户ID,使用用户ID作为租户ID
                app.setTenantId(userId);
            }

            app.setCreateTime(new Date());
            app.setUpdateTime(new Date());
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
