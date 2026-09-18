package com.budaos.support.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.dao.SuggestionDao;
import com.budaos.support.domain.entity.SuggestionEntity;
import com.budaos.support.domain.form.SuggestionAddForm;
import com.budaos.support.domain.form.SuggestionQueryForm;
import com.budaos.support.domain.vo.SuggestionVO;
import com.budaos.support.service.SuggestionService;
import com.budaos.common.core.util.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 意见反馈服务实现
 *
 * @author budaos
 */
@Service
public class SuggestionServiceImpl implements SuggestionService {

    @Resource
    private SuggestionDao feedbackDao;

    /**
     * 分页查询
     */
    @Override
    public ApiResult<PageResponse<SuggestionVO>> query(SuggestionQueryForm queryForm) {
        Page page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        List<SuggestionVO> list = feedbackDao.queryPage(page, queryForm);
        PageResponse<SuggestionVO> pageResultDTO = new PageResponse<>();
        pageResultDTO.setPageNum(page.getCurrent());
        pageResultDTO.setPageSize(page.getSize());
        pageResultDTO.setTotal(page.getTotal());
        pageResultDTO.setList(list);
        return ApiResult.ok(pageResultDTO);
    }

    /**
     * 新建
     */
    @Override
    public ApiResult<String> add(SuggestionAddForm addForm, CurrentUser requestUser) {
        SuggestionEntity feedback = BeanCopyUtil.copyProperties(addForm, SuggestionEntity.class);
        feedback.setUserType(requestUser.getUserType());
        feedback.setUserId(requestUser.getUserId());
        feedback.setUserName(requestUser.getUserName());
        feedbackDao.insert(feedback);
        return ApiResult.ok();
    }
}
