package com.budaos.support.service.impl;

import com.budaos.support.dao.ConfigRefreshItemDao;
import com.budaos.support.dao.ConfigRefreshResultDao;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.ConfigRefreshItemEntity;
import com.budaos.support.domain.form.ConfigRefreshForm;
import com.budaos.support.domain.vo.ConfigRefreshItemVO;
import com.budaos.support.domain.vo.ConfigRefreshResultVO;
import com.budaos.support.service.ConfigRefreshService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * reload (内存热加载、钩子等)
 */
@Service
public class ConfigRefreshServiceImpl implements ConfigRefreshService {

    @Resource
    private ConfigRefreshItemDao reloadItemDao;

    @Resource
    private ConfigRefreshResultDao reloadResultDao;

    /**
     * 查询
     */
    @Override
    public ApiResult<List<ConfigRefreshItemVO>> query() {
        List<ConfigRefreshItemVO> list = reloadItemDao.query();
        return ApiResult.ok(list);
    }

    /**
     * 查询重载结果
     */
    @Override
    public ApiResult<List<ConfigRefreshResultVO>> queryReloadItemResult(String tag) {
        List<ConfigRefreshResultVO> reloadResultList = reloadResultDao.query(tag);
        return ApiResult.ok(reloadResultList);
    }

    /**
     * 通过标签更新标识符
     */
    @Override
    public ApiResult<String> updateByTag(ConfigRefreshForm reloadForm) {
        ConfigRefreshItemEntity reloadItemEntity = reloadItemDao.selectById(reloadForm.getTag());
        if (null == reloadItemEntity) {
            return ApiResult.paramError("数据不存在");
        }
        reloadItemEntity.setIdentification(reloadForm.getIdentification());
        reloadItemEntity.setUpdateTime(LocalDateTime.now());
        reloadItemEntity.setArgs(reloadForm.getArgs());
        reloadItemDao.updateById(reloadItemEntity);
        return ApiResult.ok();
    }
}
