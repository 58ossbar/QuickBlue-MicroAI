package com.budaos.support.service;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.form.HelpCenterCatalogAddForm;
import com.budaos.support.domain.form.HelpCenterCatalogUpdateForm;
import com.budaos.support.domain.vo.HelpCenterCatalogVO;

import java.util.List;

/**
 * 帮助文档目录服务接口
 *
 * @author budaos
 */
public interface HelpCenterCatalogService {

    /**
     * 查询全部目录
     */
    List<HelpCenterCatalogVO> getAll();

    /**
     * 添加目录
     */
    ApiResult<String> add(HelpCenterCatalogAddForm helpDocCatalogAddForm);

    /**
     * 更新目录
     */
    ApiResult<String> update(HelpCenterCatalogUpdateForm updateForm);

    /**
     * 删除目录（如果有子目录、或者有帮助文档，则不能删除）
     */
    ApiResult<String> delete(Long helpDocCatalogId);
}
