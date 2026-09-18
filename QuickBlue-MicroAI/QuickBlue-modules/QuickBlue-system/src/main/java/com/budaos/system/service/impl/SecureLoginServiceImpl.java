package com.budaos.system.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.PageUtil;
import com.budaos.common.security.config.SafeGuardConfigUtil;
import com.budaos.system.dao.LoginAttemptDao;
import com.budaos.system.domain.entity.LoginAttemptEntity;
import com.budaos.system.domain.form.LoginAttemptQueryForm;
import com.budaos.system.domain.vo.LoginAttemptVO;
import com.budaos.system.service.SecureLoginService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录安全服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecureLoginServiceImpl implements SecureLoginService {

    private static final String LOGIN_LOCK_MSG = "您已连续登录失败%s次，账号锁定%s分钟，解锁时间为：%s，请您耐心等待！";
    private static final String LOGIN_FAIL_MSG = "登录名或密码错误！连续登录失败%s次，账号将锁定%s分钟！您还可以再尝试%s次！";

    private final LoginAttemptDao loginFailDao;

    // 从Common模块的三级等保配置中获取
    @Override
    public Integer getLoginFailMaxTimes() {
        return SafeGuardConfigUtil.getLoginFailMaxTimes();
    }

    // 从Common模块的三级等保配置中获取
    @Override
    public Integer getLoginFailLockSeconds() {
        return SafeGuardConfigUtil.getLoginFailLockSeconds();
    }

    @Override
    public ApiResult<LoginAttemptEntity> checkLogin(Long userId, Integer userType) {
        // 若登录最大失败次数小于1，无需校验
        if (getLoginFailMaxTimes() < 1) {
            return ApiResult.ok();
        }

        LoginAttemptEntity loginFailEntity = loginFailDao.selectByUserIdAndUserType(userId, userType);
        if (loginFailEntity == null) {
            return ApiResult.ok();
        }

        // 校验登录失败次数
        if (loginFailEntity.getLoginFailCount() < getLoginFailMaxTimes()) {
            return ApiResult.ok(loginFailEntity);
        }

        // 校验是否锁定
        if (loginFailEntity.getLoginLockBeginTime() == null) {
            return ApiResult.ok(loginFailEntity);
        }

        // 校验锁定时长
        if (loginFailEntity.getLoginLockBeginTime().plusSeconds(getLoginFailLockSeconds()).isBefore(LocalDateTime.now())) {
            return ApiResult.ok(loginFailEntity);
        }

        LocalDateTime unlockTime = loginFailEntity.getLoginLockBeginTime().plusSeconds(getLoginFailLockSeconds());
        return ApiResult.error(UserErrorCodes.LOGIN_FAIL_LOCK, String.format(LOGIN_LOCK_MSG,
                loginFailEntity.getLoginFailCount(),
                getLoginFailLockSeconds() / 60,
                LocalDateTimeUtil.formatNormal(unlockTime)));
    }

    @Override
    public LoginAttemptEntity getLoginFailEntity(Long userId, Integer userType) {
        return loginFailDao.selectByUserIdAndUserType(userId, userType);
    }

    @Override
    public String recordLoginFail(Long userId, Integer userType, String loginName, LoginAttemptEntity loginFailEntity) {
        // 若登录最大失败次数小于1，无需记录
        if (getLoginFailMaxTimes() < 1) {
            return null;
        }

        // 登录失败
        int loginFailCount = loginFailEntity == null ? 1 : loginFailEntity.getLoginFailCount() + 1;
        boolean lockFlag = loginFailCount >= getLoginFailMaxTimes();
        LocalDateTime lockBeginTime = lockFlag ? LocalDateTime.now() : null;

        if (loginFailEntity == null) {
            loginFailEntity = LoginAttemptEntity.builder()
                    .userId(userId)
                    .userType(userType)
                    .loginName(loginName)
                    .loginFailCount(loginFailCount)
                    .lockFlag(lockFlag)
                    .loginLockBeginTime(lockBeginTime)
                    .build();
            loginFailDao.insert(loginFailEntity);
        } else {
            loginFailEntity.setLoginLockBeginTime(lockBeginTime);
            loginFailEntity.setLoginFailCount(loginFailCount);
            loginFailEntity.setLockFlag(lockFlag);
            loginFailEntity.setLoginName(loginName);
            loginFailDao.updateById(loginFailEntity);
        }

        // 提示信息
        if (lockFlag) {
            LocalDateTime unlockTime = loginFailEntity.getLoginLockBeginTime().plusSeconds(getLoginFailLockSeconds());
            return String.format(LOGIN_LOCK_MSG,
                    loginFailEntity.getLoginFailCount(),
                    getLoginFailLockSeconds() / 60,
                    LocalDateTimeUtil.formatNormal(unlockTime));
        } else {
            return String.format(LOGIN_FAIL_MSG,
                    getLoginFailMaxTimes(),
                    getLoginFailLockSeconds() / 60,
                    getLoginFailMaxTimes() - loginFailEntity.getLoginFailCount());
        }
    }

    @Override
    public void removeLoginFail(Long userId, Integer userType) {
        // 若登录最大失败次数小于1，无需校验
        if (getLoginFailMaxTimes() < 1) {
            return;
        }
        loginFailDao.deleteByUserIdAndUserType(userId, userType);
    }

    @Override
    public PageResponse<LoginAttemptVO> queryPage(LoginAttemptQueryForm queryForm) {
        Page<LoginAttemptVO> page = PageUtil.convert2PageQuery(queryForm);
        List<LoginAttemptVO> list = loginFailDao.queryPage(page, queryForm);
        return PageUtil.convert2PageResult(page, list);
    }

    @Override
    public ApiResult<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ApiResult.ok();
        }
        loginFailDao.deleteBatchIds(idList);
        return ApiResult.ok();
    }
}
