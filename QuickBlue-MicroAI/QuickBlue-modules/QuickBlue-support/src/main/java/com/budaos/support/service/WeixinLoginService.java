package com.budaos.support.service;

import com.budaos.support.domain.form.LoginAppForm;
import com.budaos.support.domain.vo.AuthLoginWeixinVO;

/**
 * 微信登录服务
 *
 * @author budaos
 */
public interface WeixinLoginService {

    /**
     * 员工登录
     *
     * @param loginAppForm 登录表单
     * @param ip           IP地址
     * @param userAgent    User-Agent
     * @return 登录结果VO
     */
    AuthLoginWeixinVO login(LoginAppForm loginAppForm, String ip, String userAgent);
}
