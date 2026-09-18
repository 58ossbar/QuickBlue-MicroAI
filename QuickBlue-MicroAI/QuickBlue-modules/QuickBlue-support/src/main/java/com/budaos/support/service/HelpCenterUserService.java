package com.budaos.support.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.form.HelpCenterViewRecordQueryForm;
import com.budaos.support.domain.vo.HelpCenterDetailVO;
import com.budaos.support.domain.vo.HelpCenterCompleteVO;
import com.budaos.support.domain.vo.HelpCenterVO;
import com.budaos.support.domain.vo.HelpCenterViewRecordVO;

import java.util.List;

/**
 * 用户查看帮助文档服务接口
 *
 * @author budaos
 */
public interface HelpCenterUserService {

    /**
     * 查询全部帮助文档
     */
    ApiResult<List<HelpCenterVO>> queryAllHelpDocList();

    /**
     * 查询我的待查看的帮助文档清单
     */
    ApiResult<HelpCenterDetailVO> view(CurrentUser requestUser, Long helpDocId);

    /**
     * 分页查询查看记录
     */
    PageResponse<HelpCenterViewRecordVO> queryViewRecord(HelpCenterViewRecordQueryForm helpDocViewRecordQueryForm);

    /**
     * 获取完整的帮助文档数据（包含所有目录和文档内容，用于导出PDF）
     */
    HelpCenterCompleteVO getCompleteHelpDoc();
}
