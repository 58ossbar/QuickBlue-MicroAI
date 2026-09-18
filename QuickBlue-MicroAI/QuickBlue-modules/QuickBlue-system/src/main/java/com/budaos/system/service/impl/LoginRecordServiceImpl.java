package com.budaos.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.system.common.enums.AccountTypeEnum;
import com.budaos.system.dao.LoginRecordDao;
import com.budaos.system.constant.LoginRecordResultEnum;
import com.budaos.system.domain.entity.LoginRecordEntity;
import com.budaos.system.domain.form.LoginRecordQueryForm;
import com.budaos.system.domain.vo.LoginRecordVO;
import com.budaos.system.service.LoginRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 登录日志服务实现
 *
 * @author budaos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginRecordServiceImpl implements LoginRecordService {

    private final LoginRecordDao loginLogDao;

    @Override
    public ApiResult<PageResponse<LoginRecordVO>> queryByPage(LoginRecordQueryForm queryForm) {
        Page<LoginRecordVO> page = PageConvertUtil.convert2PageQuery(queryForm);
        Page<LoginRecordVO> resultPage = loginLogDao.queryByPage(page, queryForm);
        PageResponse<LoginRecordVO> pageResult = PageConvertUtil.convert2PageResult(resultPage);
        return ApiResult.ok(pageResult);
    }

    @Override
    public void log(LoginRecordEntity loginLogEntity) {
        try {
            loginLogDao.insert(loginLogEntity);
        } catch (Throwable e) {
            log.error("记录登录日志失败", e);
        }
    }

    @Override
    public LoginRecordVO queryLastByUserId(Long userId, Integer userType, LoginRecordResultEnum loginLogResultEnum) {
        return loginLogDao.queryLastByUserId(userId, userType, loginLogResultEnum.getValue());
    }

    /**
     * 分页查询当前登录人信息
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    public ApiResult<PageResponse<LoginRecordVO>> queryByPageLogin(LoginRecordQueryForm queryForm) {
        Object user = StpUtil.getSession().get("requestUser");
        if (user instanceof com.budaos.system.domain.SessionEmployee) {
            com.budaos.system.domain.SessionEmployee requestEmployee = (com.budaos.system.domain.SessionEmployee) user;
            queryForm.setUserId(requestEmployee.getUserId());
            queryForm.setUserType(AccountTypeEnum.ADMIN_EMPLOYEE.getValue());
        }
        return queryByPage(queryForm);
    }
}
