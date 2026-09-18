package com.budaos.support.nacos.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateAddForm;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateQueryForm;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateUpdateForm;
import com.budaos.support.nacos.domain.vo.NacosConfigTemplateVO;
import com.budaos.support.nacos.service.NacosConfigTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Nacos配置模板管理控制器
 *
 * @author budaos
 * @since 2026-02-24
 */
@Tag(name = "配置模板管理", description = "Nacos配置模板管理相关接口")
@RestController
@RequestMapping("/nacos-config-template")
@RequiredArgsConstructor
public class NacosConfigTemplateController {

    private final NacosConfigTemplateService templateService;

    /**
     * 分页查询配置模板
     */
    @Operation(summary = "分页查询配置模板")
    @PostMapping("/query")
    @SaCheckPermission("system:nacos-config-template:query")
    public ApiResult<PageResponse<NacosConfigTemplateVO>> queryTemplatePage(@RequestBody @Valid NacosConfigTemplateQueryForm queryForm) {
        return templateService.queryTemplatePage(queryForm);
    }

    /**
     * 查询所有模板列表(不分页)
     */
    @Operation(summary = "查询所有模板列表")
    @GetMapping("/list")
    @SaCheckPermission("system:nacos-config-template:query")
    public ApiResult<List<NacosConfigTemplateVO>> listAllTemplates() {
        return templateService.listAllTemplates();
    }

    /**
     * 根据ID查询模板详情
     */
    @Operation(summary = "根据ID查询模板详情")
    @GetMapping("/detail/{templateId}")
    @SaCheckPermission("system:nacos-config-template:query")
    public ApiResult<NacosConfigTemplateVO> getTemplateById(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        return templateService.getTemplateById(templateId);
    }

    /**
     * 根据模板编码查询模板详情
     */
    @Operation(summary = "根据模板编码查询模板详情")
    @GetMapping("/detail/code/{templateCode}")
    @SaCheckPermission("system:nacos-config-template:query")
    public ApiResult<NacosConfigTemplateVO> getTemplateByCode(
            @Parameter(description = "模板编码") @PathVariable String templateCode) {
        return templateService.getTemplateByCode(templateCode);
    }

    /**
     * 添加配置模板
     */
    @Operation(summary = "添加配置模板")
    @PostMapping("/add")
    @SaCheckPermission("system:nacos-config-template:add")
    public ApiResult<String> addTemplate(@RequestBody @Valid NacosConfigTemplateAddForm addForm) {
        return templateService.addTemplate(addForm);
    }

    /**
     * 更新配置模板
     */
    @Operation(summary = "更新配置模板")
    @PostMapping("/update")
    @SaCheckPermission("system:nacos-config-template:edit")
    public ApiResult<String> updateTemplate(@RequestBody @Valid NacosConfigTemplateUpdateForm updateForm) {
        return templateService.updateTemplate(updateForm);
    }

    /**
     * 删除配置模板
     */
    @Operation(summary = "删除配置模板")
    @DeleteMapping("/{templateId}")
    @SaCheckPermission("system:nacos-config-template:delete")
    public ApiResult<String> deleteTemplate(
            @Parameter(description = "模板ID") @PathVariable Long templateId) {
        return templateService.deleteTemplate(templateId);
    }
}
