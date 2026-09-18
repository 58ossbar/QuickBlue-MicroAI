package com.budaos.support.nacos.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateAddForm;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateQueryForm;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateUpdateForm;
import com.budaos.support.nacos.domain.vo.NacosConfigTemplateVO;

import java.util.List;

/**
 * Nacos配置模板服务接口
 *
 * @author budaos
 * @since 2026-02-24
 */
public interface NacosConfigTemplateService {

    /**
     * 分页查询配置模板
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    ApiResult<PageResponse<NacosConfigTemplateVO>> queryTemplatePage(NacosConfigTemplateQueryForm queryForm);

    /**
     * 查询所有模板列表(不分页)
     *
     * @return 模板列表
     */
    ApiResult<List<NacosConfigTemplateVO>> listAllTemplates();

    /**
     * 根据ID查询模板详情
     *
     * @param templateId 模板ID
     * @return 模板详情
     */
    ApiResult<NacosConfigTemplateVO> getTemplateById(Long templateId);

    /**
     * 根据模板编码查询模板详情
     *
     * @param templateCode 模板编码
     * @return 模板详情
     */
    ApiResult<NacosConfigTemplateVO> getTemplateByCode(String templateCode);

    /**
     * 添加配置模板
     *
     * @param addForm 添加表单
     * @return 操作结果
     */
    ApiResult<String> addTemplate(NacosConfigTemplateAddForm addForm);

    /**
     * 更新配置模板
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ApiResult<String> updateTemplate(NacosConfigTemplateUpdateForm updateForm);

    /**
     * 删除配置模板
     *
     * @param templateId 模板ID
     * @return 操作结果
     */
    ApiResult<String> deleteTemplate(Long templateId);

    /**
     * 根据数据库类型查询模板列表
     *
     * @param databaseType 数据库类型 (common/mysql/postgresql)
     * @return 模板列表
     */
    ApiResult<List<NacosConfigTemplateVO>> listTemplatesByDatabaseType(String databaseType);

    /**
     * 根据数据库类型和模板编码查询模板详情
     *
     * @param databaseType 数据库类型
     * @param templateCode 模板编码
     * @return 模板详情
     */
    ApiResult<NacosConfigTemplateVO> getTemplateByDatabaseTypeAndCode(String databaseType, String templateCode);
}
