package com.budaos.system.service;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.form.VerifyCodeForm;
import com.budaos.system.domain.vo.VerifyCodeVO;

/**
 * 图形验证码服务接口
 *
 * @author budaos
 */
public interface VerifyCodeService {

    /**
     * 生成图形验证码
     *
     * @return 验证码VO
     */
    VerifyCodeVO generateCaptcha();

    /**
     * 校验图形验证码
     *
     * @param captchaForm 验证码表单
     * @return 校验结果
     */
    ApiResult<String> checkCaptcha(VerifyCodeForm captchaForm);
}
