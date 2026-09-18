package com.budaos.support.service;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.form.ConfigRefreshForm;
import com.budaos.support.domain.vo.ConfigRefreshItemVO;
import com.budaos.support.domain.vo.ConfigRefreshResultVO;

import java.util.List;

/**
 * reload (内存热加载、钩子等)
 */
public interface ConfigRefreshService {

    /**
     * 查询
     */
    ApiResult<List<ConfigRefreshItemVO>> query();

    /**
     * 查询重载结果
     */
    ApiResult<List<ConfigRefreshResultVO>> queryReloadItemResult(String tag);

    /**
     * 通过标签更新标识符
     */
    ApiResult<String> updateByTag(ConfigRefreshForm reloadForm);
}
