package com.budaos.system.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.vo.DataPermissionAndViewTypeVO;
import com.budaos.system.service.DataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据权限控制器
 *
 * @author budaos
 */
@Tag(name = "数据权限管理", description = "数据权限管理相关接口")
@RestController
@RequiredArgsConstructor
public class DataPermissionController {

    private final DataPermissionService dataScopeService;

    /**
     * 获取当前系统所配置的所有数据范围
     */
    @Operation(summary = "获取当前系统所配置的所有数据范围")
    @GetMapping("/dataScope/list")
    public ApiResult<List<DataPermissionAndViewTypeVO>> dataScopeList() {
        return dataScopeService.dataScopeList();
    }
}
