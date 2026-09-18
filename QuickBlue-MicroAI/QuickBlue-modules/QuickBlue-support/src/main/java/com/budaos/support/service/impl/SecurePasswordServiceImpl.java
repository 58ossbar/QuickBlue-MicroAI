package com.budaos.support.service.impl;

import com.budaos.support.dao.PasswordRecordDao;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.domain.entity.PasswordRecordEntity;
import com.budaos.support.service.SafeGuardConfigService;
import com.budaos.support.service.SecurePasswordService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 三级等保 密码 相关
 */
@Service
public class SecurePasswordServiceImpl implements SecurePasswordService {

    private static final int PASSWORD_LENGTH = 8;

    @Resource
    private PasswordRecordDao passwordLogDao;

    @Resource
    private SafeGuardConfigService level3ProtectConfigService;

    static Argon2PasswordEncoder ARGON2_PASSWORD_ENCODER = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    /**
     * 校验密码复杂度
     */
    @Override
    public ApiResult<String> validatePasswordComplexity(String password) {

        if (StringUtils.isEmpty(password)) {
            return ApiResult.userErrorParam(PASSWORD_FORMAT_MSG);
        }

        // 密码长度必须大于等于8位
        if (password.length() < PASSWORD_LENGTH) {
            return ApiResult.userErrorParam(PASSWORD_FORMAT_MSG);
        }

        // 无需校验 密码复杂度
        if (!level3ProtectConfigService.isPasswordComplexityEnabled()) {
            return ApiResult.ok();
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            return ApiResult.userErrorParam(PASSWORD_FORMAT_MSG);
        }

        return ApiResult.ok();
    }

    /**
     * 校验密码重复次数
     */
    @Override
    public ApiResult<String> validatePasswordRepeatTimes(CurrentUser requestUser, String newPassword) {

        // 密码重复次数小于1  无需校验
        if (level3ProtectConfigService.getRegularChangePasswordNotAllowRepeatTimes() < 1) {
            return ApiResult.ok();
        }

        // 检查最近几次是否有重复密码
        List<String> oldPasswords = passwordLogDao.selectOldPassword(requestUser.getUserType(), requestUser.getUserId(), level3ProtectConfigService.getRegularChangePasswordNotAllowRepeatTimes());
        boolean isDuplicate = oldPasswords.stream().anyMatch(oldPassword -> ARGON2_PASSWORD_ENCODER.matches(newPassword, oldPassword));
        if (isDuplicate) {
            return ApiResult.userErrorParam(String.format("与前%d个历史密码重复，请换个密码!", level3ProtectConfigService.getRegularChangePasswordNotAllowRepeatTimes()));
        }

        return ApiResult.ok();
    }

    /**
     * 随机生成密码
     */
    @Override
    public String randomPassword() {
        // 未开启密码复杂度，则由8为数字构成
        if (!level3ProtectConfigService.isPasswordComplexityEnabled()) {
            return RandomStringUtils.randomNumeric(PASSWORD_LENGTH);
        }

        // 3位大写字母，2位数字，2位小写字母 + 1位特殊符号
        return RandomStringUtils.randomAlphabetic(3).toUpperCase()
                + RandomStringUtils.randomNumeric(2)
                + RandomStringUtils.randomAlphabetic(2).toLowerCase()
                + (ThreadLocalRandom.current().nextBoolean() ? "#" : "@");
    }

    /**
     * 保存修改密码
     */
    @Override
    public void saveUserChangePasswordLog(CurrentUser requestUser, String newPassword, String oldPassword) {

        PasswordRecordEntity passwordLogEntity = new PasswordRecordEntity();
        passwordLogEntity.setNewPassword(newPassword);
        passwordLogEntity.setOldPassword(oldPassword);
        passwordLogEntity.setUserId(requestUser.getUserId());
        passwordLogEntity.setUserType(requestUser.getUserType());
        passwordLogDao.insert(passwordLogEntity);
    }

    /**
     * 检查是否需要修改密码
     */
    @Override
    public boolean checkNeedChangePassword(Integer userType, Long userId) {

        if (level3ProtectConfigService.getRegularChangePasswordDays() < 1) {
            return false;
        }

        PasswordRecordEntity passwordLogEntity = passwordLogDao.selectLastByUserTypeAndUserId(userType, userId);
        if (passwordLogEntity == null) {
            return false;
        }

        LocalDateTime nextUpdateTime = passwordLogEntity.getCreateTime().plusDays(level3ProtectConfigService.getRegularChangePasswordDays());
        return nextUpdateTime.isBefore(LocalDateTime.now());
    }
}
