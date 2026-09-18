package com.budaos.system.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.constant.LoginRecordResultEnum;
import com.budaos.system.domain.entity.LoginRecordEntity;
import com.budaos.system.domain.form.LoginRecordQueryForm;
import com.budaos.system.domain.vo.LoginRecordVO;

/**
 * 登录日志服务接口
 *
 * @author budaos
 */
public interface LoginRecordService {

    /**
     * 分页查询登录日志
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ApiResult<PageResponse<LoginRecordVO>> queryByPage(LoginRecordQueryForm queryForm);

    /**
     * 记录登录日志
     *
     * @param loginLogEntity 登录日志实体
     */
    void log(LoginRecordEntity loginLogEntity);

    /**
     * 查询上一个登录记录
     *
     * @param userId             用户ID
     * @param userTypeEnum        用户类型枚举
     * @param loginLogResultEnum  登录结果枚举
     * @return 登录日志VO
     */
    LoginRecordVO queryLastByUserId(Long userId, Integer userType, LoginRecordResultEnum loginLogResultEnum);

    /**
     * 分页查询当前登录人信息
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ApiResult<PageResponse<LoginRecordVO>> queryByPageLogin(LoginRecordQueryForm queryForm);
}
