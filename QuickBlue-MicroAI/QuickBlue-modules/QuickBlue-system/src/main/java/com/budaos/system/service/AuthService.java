package com.budaos.system.service;

import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.SessionEmployee;
import com.budaos.system.domain.form.AuthLoginForm;
import com.budaos.system.domain.form.VerifyCodeForm;
import com.budaos.system.domain.form.OnlineUserQueryForm;
import com.budaos.system.domain.vo.AuthLoginVO;
import com.budaos.system.domain.vo.VerifyCodeVO;
import com.budaos.system.domain.vo.OnlineUserVO;

import java.util.List;

/**
 * 登录服务接口
 *
 * @author budaos
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param loginForm 登录表单
     * @param ip 客户端IP
     * @param userAgent User-Agent
     * @return 登录结果
     */
    ApiResult<AuthLoginVO> login(AuthLoginForm loginForm, String ip, String userAgent);

    /**
     * 获取登录信息
     *
     * @return 登录结果
     */
    ApiResult<AuthLoginVO> getLoginInfo();

    /**
     * 退出登录
     *
     * @param requestUser 当前用户
     * @return 操作结果
     */
    ApiResult<String> logout(CurrentUser requestUser);

    /**
     * 获取验证码
     *
     * @return 验证码信息
     */
    ApiResult<VerifyCodeVO> getCaptcha();

    /**
     * 验证验证码
     *
     * @param captchaId 验证码ID
     * @param captcha 验证码
     * @return 是否验证成功
     */
    boolean verifyCaptcha(String captchaId, String captcha);

    /**
     * 根据 token 获取员工信息
     *
     * @param loginId 登录ID
     * @return 员工信息
     */
    SessionEmployee getLoginEmployee(String loginId);

    /**
     * 根据登录ID获取员工ID
     *
     * @param loginId 登录ID
     * @return 员工ID
     */
    Long getEmployeeIdByLoginId(String loginId);

    /**
     * 清除员工登录缓存
     *
     * @param employeeId 员工ID
     */
    void clearLoginEmployeeCache(Long employeeId);

    // ==================== 在线用户管理 ====================

    /**
     * 分页查询在线用户
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ApiResult<PageResponse<OnlineUserVO>> queryOnlineUserPage(OnlineUserQueryForm queryForm);

    /**
     * 获取所有在线用户列表
     *
     * @return 在线用户列表
     */
    List<OnlineUserVO> queryAllOnlineUsers();

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    long getOnlineUserCount();

    /**
     * 强制下线
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 操作结果
     */
    ApiResult<String> forceLogout(Long userId, Integer userType);

    /**
     * 批量强制下线
     *
     * @param userIds  用户ID列表
     * @param userType 用户类型
     * @return 操作结果
     */
    ApiResult<String> batchForceLogout(List<Long> userIds, Integer userType);
}
