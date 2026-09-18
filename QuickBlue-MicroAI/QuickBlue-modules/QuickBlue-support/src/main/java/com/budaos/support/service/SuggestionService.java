package com.budaos.support.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.form.SuggestionAddForm;
import com.budaos.support.domain.form.SuggestionQueryForm;
import com.budaos.support.domain.vo.SuggestionVO;

/**
 * 意见反馈服务接口
 *
 * @author budaos
 */
public interface SuggestionService {

    /**
     * 分页查询
     */
    ApiResult<PageResponse<SuggestionVO>> query(SuggestionQueryForm queryForm);

    /**
     * 新建
     */
    ApiResult<String> add(SuggestionAddForm addForm, CurrentUser requestUser);
}
