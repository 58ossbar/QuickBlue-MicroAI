package com.budaos.support.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.support.constant.SystemConfigKeyEnum;
import com.budaos.support.dao.SystemConfigDao;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.SystemConfigEntity;
import com.budaos.support.domain.form.SystemConfigAddForm;
import com.budaos.support.domain.form.SystemConfigQueryForm;
import com.budaos.support.domain.form.SystemConfigUpdateForm;
import com.budaos.support.domain.vo.SystemConfigVO;
import com.budaos.support.manager.ConfigManager;
import com.budaos.support.service.SystemConfigService;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 系统配置业务类
 */
@Slf4j
@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    /**
     * 一个简单的系统配置缓存
     */
    private final ConfigManager CONFIG_CACHE = new ConfigManager();

    @Resource
    private SystemConfigDao configDao;

    /**
     * 初始化系统设置缓存
     */
    @PostConstruct
    private void loadConfigCache() {
        CONFIG_CACHE.clear();
        List<SystemConfigEntity> entityList = configDao.selectList(null);
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        entityList.forEach(entity -> CONFIG_CACHE.put(entity.getConfigKey().toLowerCase(), entity));
        log.info("################# 系统配置缓存初始化完毕:{} ###################", CONFIG_CACHE.size());
    }

    /**
     * 刷新系统设置缓存
     */
    private void refreshConfigCache(Long configId) {
        // 重新查询 加入缓存
        SystemConfigEntity configEntity = configDao.selectById(configId);
        if (null == configEntity) {
            return;
        }
        CONFIG_CACHE.put(configEntity.getConfigKey().toLowerCase(), configEntity);
    }

    /**
     * 分页查询系统配置
     */
    @Override
    public ApiResult<PageResponse<SystemConfigVO>> queryConfigPage(SystemConfigQueryForm queryForm) {
        Page<SystemConfigEntity> page = PageConvertUtil.convert2PageQuery(queryForm);
        List<SystemConfigEntity> entityList = configDao.queryByPage(page, queryForm);
        List<SystemConfigVO> voList = BeanCopyUtil.copyList(entityList, SystemConfigVO.class);
        PageResponse<SystemConfigVO> pageResult = new PageResponse<>();
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setList(voList);
        return ApiResult.ok(pageResult);
    }

    /**
     * 查询配置缓存
     */
    @Override
    public SystemConfigVO getConfig(SystemConfigKeyEnum configKey) {
        return this.getConfig(configKey.getValue());
    }

    /**
     * 查询配置缓存
     */
    @Override
    public SystemConfigVO getConfig(String configKey) {
        if (StrUtil.isBlank(configKey)) {
            return null;
        }
        SystemConfigEntity entity = CONFIG_CACHE.get(configKey.toLowerCase());
        return BeanCopyUtil.copyProperties(entity, SystemConfigVO.class);
    }

    /**
     * 查询配置缓存参数
     */
    @Override
    public String getConfigValue(SystemConfigKeyEnum configKey) {
        SystemConfigVO config = this.getConfig(configKey);
        return config == null ? null : config.getConfigValue();
    }

    /**
     * 根据参数key查询 并转换为对象
     */
    @Override
    public <T> T getConfigValue2Obj(SystemConfigKeyEnum configKey, Class<T> clazz) {
        String configValue = this.getConfigValue(configKey);
        return JSON.parseObject(configValue, clazz);
    }

    /**
     * 添加系统配置
     */
    @Override
    public ApiResult<String> add(SystemConfigAddForm configAddForm) {
        SystemConfigEntity entity = configDao.selectByKey(configAddForm.getConfigKey());
        if (null != entity) {
            return ApiResult.userErrorParam("配置已存在");
        }
        entity = BeanCopyUtil.copyProperties(configAddForm, SystemConfigEntity.class);
        configDao.insert(entity);

        // 刷新缓存
        this.refreshConfigCache(entity.getConfigId());
        return ApiResult.ok();
    }

    /**
     * 更新系统配置
     */
    @Override
    public ApiResult<String> updateConfig(SystemConfigUpdateForm updateDTO) {
        Long configId = updateDTO.getConfigId();
        SystemConfigEntity entity = configDao.selectById(configId);
        if (null == entity) {
            return ApiResult.userErrorParam("数据不存在");
        }
        SystemConfigEntity alreadyEntity = configDao.selectByKey(updateDTO.getConfigKey());
        if (null != alreadyEntity && !Objects.equals(configId, alreadyEntity.getConfigId())) {
            return ApiResult.userErrorParam("config key 已存在");
        }

        // 更新数据
        entity = BeanCopyUtil.copyProperties(updateDTO, SystemConfigEntity.class);
        configDao.updateById(entity);

        // 刷新缓存
        this.refreshConfigCache(configId);
        return ApiResult.ok();
    }

    /**
     * 更新系统配置
     */
    @Override
    public ApiResult<String> updateValueByKey(SystemConfigKeyEnum key, String value) {
        SystemConfigVO config = this.getConfig(key);
        if (null == config) {
            return ApiResult.userErrorParam("数据不存在");
        }

        // 更新数据
        Long configId = config.getConfigId();
        SystemConfigEntity entity = new SystemConfigEntity();
        entity.setConfigId(configId);
        entity.setConfigValue(value);
        configDao.updateById(entity);

        // 刷新缓存
        this.refreshConfigCache(configId);
        return ApiResult.ok();
    }
}
