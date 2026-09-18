package com.budaos.system.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.constant.RedisKeyConstants;
import com.budaos.common.core.util.StringUtil;
import com.budaos.system.domain.form.VerifyCodeForm;
import com.budaos.system.domain.vo.VerifyCodeVO;
import com.budaos.system.service.VerifyCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyCodeServiceImpl implements VerifyCodeService {

    /**
     * 过期时间：65秒
     */
    private static final long EXPIRE_SECOND = 65L;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.profiles.active}")
    private String environment;

    @Override
    public VerifyCodeVO generateCaptcha() {
        // 生成四位验证码
        String captchaText = RandomUtil.randomNumbers(4);

        // 定义图形验证码的长、宽、验证码位数、干扰线数量
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(125, 43, 4, 80);

        // 设置背景颜色
        lineCaptcha.setBackground(new Color(230, 244, 255));

        // 生成图片
        Image image = lineCaptcha.createImage(captchaText);

        // 转为base64
        String base64Code = ImgUtil.toBase64(image, "jpg");

        // UUID唯一标识
        String uuid = IdUtil.fastSimpleUUID();

        VerifyCodeVO captchaVO = new VerifyCodeVO();
        captchaVO.setCaptchaUuid(uuid);
        captchaVO.setCaptchaBase64Image("data:image/png;base64," + base64Code);
        captchaVO.setExpireSeconds(EXPIRE_SECOND);

        // 非生产环境返回验证码文字
        if (!"prod".equals(environment)) {
            captchaVO.setCaptchaText(captchaText);
        }

        // 存储到Redis
        String redisCaptchaKey = RedisKeyConstants.CAPTCHA_KEY + uuid;
        redisTemplate.opsForValue().set(redisCaptchaKey, captchaText, EXPIRE_SECOND, TimeUnit.SECONDS);

        return captchaVO;
    }

    @Override
    public ApiResult<String> checkCaptcha(VerifyCodeForm captchaForm) {
        if (StringUtil.isEmpty(captchaForm.getCaptchaUuid()) || StringUtil.isEmpty(captchaForm.getCaptchaCode())) {
            return ApiResult.userErrorParam("请输入正确验证码");
        }

        // 校验Redis里的验证码
        String redisCaptchaKey = RedisKeyConstants.CAPTCHA_KEY + captchaForm.getCaptchaUuid();
        String redisCaptchaCode = (String) redisTemplate.opsForValue().get(redisCaptchaKey);

        if (StringUtil.isEmpty(redisCaptchaCode)) {
            return ApiResult.userErrorParam("验证码已过期，请刷新重试");
        }

        if (!Objects.equals(redisCaptchaCode, captchaForm.getCaptchaCode())) {
            return ApiResult.userErrorParam("验证码错误，请输入正确的验证码");
        }

        // 删除已使用的验证码
        redisTemplate.delete(redisCaptchaKey);

        return ApiResult.ok();
    }
}
