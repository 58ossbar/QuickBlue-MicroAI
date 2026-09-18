package com.budaos.system.service;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.constant.DataPermissionViewTypeEnum;
import com.budaos.system.domain.dto.DataPermissionDTO;
import com.budaos.system.domain.vo.DataPermissionAndViewTypeVO;
import com.budaos.system.domain.vo.DataPermissionViewTypeVO;

import java.util.Comparator;
import java.util.List;

/**
 * 数据权限服务接口
 *
 * @author budaos
 */
public interface DataPermissionService {

    /**
     * 获取所有可以进行数据范围配置的信息
     *
     * @return 数据范围和视图类型列表
     */
    ApiResult<List<DataPermissionAndViewTypeVO>> dataScopeList();

    /**
     * 获取当前系统存在的数据可见范围
     *
     * @return 数据可见范围列表
     */
    default List<DataPermissionViewTypeVO> getViewType() {
        return List.of(
            DataPermissionViewTypeVO.builder()
                .viewType(DataPermissionViewTypeEnum.ME.getValue())
                .viewTypeLevel(DataPermissionViewTypeEnum.ME.getLevel())
                .viewTypeName(DataPermissionViewTypeEnum.ME.getDesc())
                .build(),
            DataPermissionViewTypeVO.builder()
                .viewType(DataPermissionViewTypeEnum.DEPARTMENT.getValue())
                .viewTypeLevel(DataPermissionViewTypeEnum.DEPARTMENT.getLevel())
                .viewTypeName(DataPermissionViewTypeEnum.DEPARTMENT.getDesc())
                .build(),
            DataPermissionViewTypeVO.builder()
                .viewType(DataPermissionViewTypeEnum.DEPARTMENT_AND_SUB.getValue())
                .viewTypeLevel(DataPermissionViewTypeEnum.DEPARTMENT_AND_SUB.getLevel())
                .viewTypeName(DataPermissionViewTypeEnum.DEPARTMENT_AND_SUB.getDesc())
                .build(),
            DataPermissionViewTypeVO.builder()
                .viewType(DataPermissionViewTypeEnum.ALL.getValue())
                .viewTypeLevel(DataPermissionViewTypeEnum.ALL.getLevel())
                .viewTypeName(DataPermissionViewTypeEnum.ALL.getDesc())
                .build()
        ).stream().sorted(Comparator.comparing(DataPermissionViewTypeVO::getViewTypeLevel)).toList();
    }

    /**
     * 获取数据范围类型
     *
     * @return 数据范围类型列表（已废弃，请使用DataScopeConfigService）
     * @deprecated 请使用 DataPermissionConfigService.dataScopeConfigList() 替代
     */
    @Deprecated
    default List<DataPermissionDTO> getDataScopeType() {
        return List.of();
    }
}
