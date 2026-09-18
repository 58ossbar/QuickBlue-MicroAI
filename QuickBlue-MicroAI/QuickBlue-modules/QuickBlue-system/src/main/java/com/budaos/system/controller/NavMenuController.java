package com.budaos.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.budaos.common.core.domain.RequestUrlInfo;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.NavMenuAddForm;
import com.budaos.system.domain.form.NavMenuUpdateForm;
import com.budaos.system.domain.vo.NavMenuTreeVO;
import com.budaos.system.domain.vo.NavMenuVO;
import com.budaos.system.service.NavMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author budaos
 */
@Tag(name = "菜单管理", description = "菜单管理相关接口")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class NavMenuController {

    private final NavMenuService menuService;

    /**
     * 添加菜单
     */
    @Operation(summary = "添加菜单")
    @PostMapping("/add")
    @SaCheckPermission("system:menu:add")
    public ApiResult<String> addMenu(@RequestBody @Valid NavMenuAddForm menuAddForm) {
        menuAddForm.setCreateUserId(getCurrentUserId());
        return menuService.addMenu(menuAddForm);
    }

    /**
     * 更新菜单
     */
    @Operation(summary = "更新菜单")
    @PostMapping("/update")
    @SaCheckPermission("system:menu:update")
    public ApiResult<String> updateMenu(@RequestBody @Valid NavMenuUpdateForm menuUpdateForm) {
        menuUpdateForm.setUpdateUserId(getCurrentUserId());
        return menuService.updateMenu(menuUpdateForm);
    }

    /**
     * 批量删除菜单
     */
    @Operation(summary = "批量删除菜单")
    @GetMapping("/batchDelete")
    @SaCheckPermission("system:menu:batchDelete")
    public ApiResult<String> batchDeleteMenu(@RequestParam("menuIdList") List<Long> menuIdList) {
        return menuService.batchDeleteMenu(menuIdList, getCurrentUserId());
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // 登录ID格式为 "userType:userId"，需要解析
        String loginId = StpUtil.getLoginIdAsString();
        if (loginId != null && loginId.contains(":")) {
            String[] parts = loginId.split(":");
            return Long.parseLong(parts[1]);
        }
        return null;
    }

    /**
     * 查询菜单列表
     */
    @Operation(summary = "查询菜单列表")
    @GetMapping("/query")
    public ApiResult<List<NavMenuVO>> queryMenuList() {
        return ApiResult.ok(menuService.queryMenuList(null));
    }

    /**
     * 查询菜单详情
     */
    @Operation(summary = "查询菜单详情")
    @GetMapping("/detail/{menuId}")
    public ApiResult<NavMenuVO> getMenuDetail(@PathVariable Long menuId) {
        return menuService.getMenuDetail(menuId);
    }

    /**
     * 查询菜单树
     */
    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    public ApiResult<List<NavMenuTreeVO>> queryMenuTree(@RequestParam(value = "onlyMenu", defaultValue = "false") Boolean onlyMenu) {
        return menuService.queryMenuTree(onlyMenu);
    }

    /**
     * 获取所有请求路径
     */
    @Operation(summary = "获取所有请求路径")
    @GetMapping("/auth/url")
    public ApiResult<List<RequestUrlInfo>> getAuthUrl() {
        return menuService.getAuthUrl();
    }
}

