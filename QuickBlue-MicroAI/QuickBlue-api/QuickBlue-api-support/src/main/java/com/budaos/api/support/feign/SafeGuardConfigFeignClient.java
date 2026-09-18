package com.budaos.api.support.feign;

import com.budaos.api.support.dto.SafeGuardConfigDTO;
import com.budaos.common.core.domain.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 三级等保配置服务 Feign Client
 *
 * @author budaos
 */
@FeignClient(contextId = "level3ProtectConfigFeignClient", value = "QuickBlue-support", path = "/protect")
public interface SafeGuardConfigFeignClient {

    /**
     * 获取三级等保配置
     */
    @GetMapping("/level3protect/getConfigObject")
    ApiResult<SafeGuardConfigDTO> getConfig();

    /**
     * 更新三级等保配置
     */
    @PostMapping("/level3protect/updateConfig")
    ApiResult<String> updateConfig(@RequestBody SafeGuardConfigDTO configForm);

    /**
     * 是否开启密码复杂度校验
     */
    @GetMapping("/level3protect/isPasswordComplexityEnabled")
    ApiResult<Boolean> isPasswordComplexityEnabled();

    /**
     * 获取定期修改密码不允许重复的次数
     */
    @GetMapping("/level3protect/getRegularChangePasswordNotAllowRepeatTimes")
    ApiResult<Integer> getRegularChangePasswordNotAllowRepeatTimes();

    /**
     * 获取定期修改密码天数
     */
    @GetMapping("/level3protect/getRegularChangePasswordDays")
    ApiResult<Integer> getRegularChangePasswordDays();

    /**
     * 获取连续登录失败最大次数
     */
    @GetMapping("/level3protect/getLoginFailMaxTimes")
    ApiResult<Integer> getLoginFailMaxTimes();

    /**
     * 获取连续登录失败锁定时间（秒）
     */
    @GetMapping("/level3protect/getLoginFailLockSeconds")
    ApiResult<Integer> getLoginFailLockSeconds();
}
