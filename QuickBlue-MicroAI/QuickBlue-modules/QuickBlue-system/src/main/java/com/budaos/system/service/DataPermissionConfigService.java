package com.budaos.system.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.DataPermissionConfigAddForm;
import com.budaos.system.domain.form.DataPermissionConfigQueryForm;
import com.budaos.system.domain.form.DataPermissionConfigUpdateForm;
import com.budaos.system.domain.vo.DataPermissionConfigVO;
import com.budaos.system.domain.vo.DataPermissionViewTypeVO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限配置服务接口
 *
 * @author budaos
 */
public interface DataPermissionConfigService {

    /**
     * 获取所有数据权限配置（用于前端展示）
     *
     * @return 数据权限配置列表
     */
    ApiResult<List<DataPermissionConfigVO>> dataScopeConfigList();

    /**
     * 分页查询
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ApiResult<PageResponse<DataPermissionConfigVO>> queryPage(DataPermissionConfigQueryForm queryForm);

    /**
     * 添加
     *
     * @param addForm 添加表单
     * @return 响应
     */
    ApiResult<String> add(DataPermissionConfigAddForm addForm);

    /**
     * 更新
     *
     * @param updateForm 更新表单
     * @return 响应
     */
    ApiResult<String> update(DataPermissionConfigUpdateForm updateForm);

    /**
     * 删除
     *
     * @param configId 配置ID
     * @return 响应
     */
    ApiResult<String> delete(Long configId);

    /**
     * 批量删除
     *
     * @param idList ID列表
     * @return 响应
     */
    ApiResult<String> batchDelete(List<Long> idList);

    /**
     * 更新状态
     *
     * @param configId 配置ID
     * @return 响应
     */
    ApiResult<String> updateStatus(Long configId);

    /**
     * 获取所有数据可见范围类型（与 DataPermissionViewTypeEnum 保持一致）
     *
     * @return 数据可见范围类型列表
     */
    default List<DataPermissionViewTypeVO> getViewTypeList() {
        return List.of(
            DataPermissionViewTypeVO.builder()
                .viewType(0)
                .viewTypeLevel(0)
                .viewTypeName("仅本人")
                .build(),
            DataPermissionViewTypeVO.builder()
                .viewType(1)
                .viewTypeLevel(1)
                .viewTypeName("部门")
                .build(),
            DataPermissionViewTypeVO.builder()
                .viewType(2)
                .viewTypeLevel(2)
                .viewTypeName("本部门及以下")
                .build(),
            DataPermissionViewTypeVO.builder()
                .viewType(3)
                .viewTypeLevel(3)
                .viewTypeName("全部数据")
                .build()
        ).stream().sorted(Comparator.comparing(DataPermissionViewTypeVO::getViewTypeLevel)).collect(Collectors.toList());
    }
}
