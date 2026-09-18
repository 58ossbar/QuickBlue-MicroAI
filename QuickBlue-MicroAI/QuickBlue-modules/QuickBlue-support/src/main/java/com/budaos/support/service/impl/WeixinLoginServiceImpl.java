package com.budaos.support.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.budaos.support.domain.form.LoginAppForm;
import com.budaos.support.domain.vo.AuthLoginWeixinVO;
import com.budaos.support.service.WeixinLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 微信登录服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
public class WeixinLoginServiceImpl implements WeixinLoginService {

    /**
     * 员工登录
     *
     * @return 返回用户登录信息
     */
    @Override
    public AuthLoginWeixinVO login(LoginAppForm loginAppForm, String ip, String userAgent) {
        log.info("========== 手机端登录流程 ==========");
        log.info("登录信息 - loginName: {}, loginDevice: {}, IP: {}",
                loginAppForm.getLoginName(), loginAppForm.getLoginDevice(), ip);

        // TODO: 实际登录逻辑需要依赖 QuickBlue-system 模块的服务
        // 这里返回一个基础的结果，实际使用时需要集成以下服务：
        // - StaffService: 查询用户信息
        // - AuthRoleStaffService: 获取用户角色
        // - AuthRoleMenuService: 获取用户菜单和权限
        // - LoginRecordService: 记录登录日志
        // - SecureLoginService: 登录安全检查
        // - SecurePasswordService: 密码验证

        AuthLoginWeixinVO loginResultVO = new AuthLoginWeixinVO();
        loginResultVO.setUserId(1L);
        loginResultVO.setUserName("测试用户");
        loginResultVO.setLoginName(loginAppForm.getLoginName());
        loginResultVO.setAvatar("");

        // 获取登录结果信息
        String token = StpUtil.getTokenValue();
        loginResultVO.setToken(token);
        loginResultVO.setNeedUpdatePwdFlag(false);

        // 模拟上次登录信息
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        loginResultVO.setLastLoginIp("192.168.1.1");
        loginResultVO.setLastLoginIpRegion("本地");
        loginResultVO.setLastLoginTime(LocalDateTime.now().format(formatter));
        loginResultVO.setLastLoginUserAgent(userAgent);

        log.info("手机端登录成功 - userId: {}, token: {}", loginResultVO.getUserId(), token);
        return loginResultVO;
    }
}
