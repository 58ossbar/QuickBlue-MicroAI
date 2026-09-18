package com.budaos.support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.support.constant.SystemConfigKeyEnum;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.form.SystemConfigAddForm;
import com.budaos.support.domain.form.SystemConfigQueryForm;
import com.budaos.support.domain.form.SystemConfigUpdateForm;
import com.budaos.support.domain.vo.SystemConfigVO;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {

    /**
     * 分页查询系统配置
     */
    ApiResult<PageResponse<SystemConfigVO>> queryConfigPage(SystemConfigQueryForm queryForm);

    /**
     * 查询配置缓存
     */
    SystemConfigVO getConfig(SystemConfigKeyEnum configKey);

    /**
     * 查询配置缓存
     */
    SystemConfigVO getConfig(String configKey);

    /**
     * 查询配置缓存参数
     */
    String getConfigValue(SystemConfigKeyEnum configKey);

    /**
     * 根据参数key查询 并转换为对象
     */
    <T> T getConfigValue2Obj(SystemConfigKeyEnum configKey, Class<T> clazz);

    /**
     * 添加系统配置
     */
    ApiResult<String> add(SystemConfigAddForm configAddForm);

    /**
     * 更新系统配置
     */
    ApiResult<String> updateConfig(SystemConfigUpdateForm updateDTO);

    /**
     * 更新系统配置
     */
    ApiResult<String> updateValueByKey(SystemConfigKeyEnum key, String value);
}
