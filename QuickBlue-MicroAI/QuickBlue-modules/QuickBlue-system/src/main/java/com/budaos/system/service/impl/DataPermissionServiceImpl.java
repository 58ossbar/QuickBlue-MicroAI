package com.budaos.system.service.impl;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.vo.DataPermissionAndViewTypeVO;
import com.budaos.system.domain.vo.DataPermissionConfigVO;
import com.budaos.system.domain.vo.DataPermissionViewTypeVO;
import com.budaos.system.service.DataPermissionConfigService;
import com.budaos.system.service.DataPermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
public class DataPermissionServiceImpl implements DataPermissionService {

    @Resource
    private DataPermissionConfigService dataScopeConfigService;

    @Override
    public ApiResult<List<DataPermissionAndViewTypeVO>> dataScopeList() {
        // 从数据库读取配置
        ApiResult<List<DataPermissionConfigVO>> configResult = dataScopeConfigService.dataScopeConfigList();

        if (!configResult.getOk() || configResult.getData() == null || configResult.getData().isEmpty()) {
            log.warn("Failed to get data scope config list or list is empty, returning empty list");
            return ApiResult.ok(new ArrayList<>());
        }

        List<DataPermissionConfigVO> configList = configResult.getData();
        List<DataPermissionViewTypeVO> typeList = dataScopeConfigService.getViewTypeList();

        // 转换为DataScopeAndViewTypeVO
        List<DataPermissionAndViewTypeVO> dataScopeAndTypeList = new ArrayList<>();

        for (DataPermissionConfigVO config : configList) {
            DataPermissionAndViewTypeVO vo = new DataPermissionAndViewTypeVO();
            // 使用config_id作为dataScopeType，确保唯一性
            // 这样可以避免修改代码，完全依赖数据库配置
            vo.setDataScopeType(config.getConfigId().intValue());
            vo.setDataScopeTypeName(config.getConfigName());
            vo.setDataScopeTypeDesc(config.getConfigDesc());
            vo.setDataScopeTypeSort(config.getSortOrder());
            vo.setDefaultViewType(config.getDefaultViewType());
            vo.setViewTypeList(typeList);
            dataScopeAndTypeList.add(vo);
        }

        return ApiResult.ok(dataScopeAndTypeList);
    }
}
