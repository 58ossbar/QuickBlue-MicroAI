package com.budaos.system.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.entity.LoginAttemptEntity;
import com.budaos.system.domain.form.LoginAttemptQueryForm;
import com.budaos.system.domain.vo.LoginAttemptVO;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 登录安全服务接口
 *
 * @author budaos
 */
public interface SecureLoginService {

    /**
     * 检查是否可以登录
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 登录失败实体
     */
    ApiResult<LoginAttemptEntity> checkLogin(Long userId, Integer userType);

    /**
     * 获取登录失败实体
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 登录失败实体
     */
    LoginAttemptEntity getLoginFailEntity(Long userId, Integer userType);

    /**
     * 登录失败后记录
     *
     * @param userId           用户ID
     * @param userType         用户类型
     * @param loginName        登录名
     * @param loginFailEntity  登录失败实体
     * @return 提示信息
     */
    String recordLoginFail(Long userId, Integer userType, String loginName, LoginAttemptEntity loginFailEntity);

    /**
     * 清除登录失败
     *
     * @param userId   用户ID
     * @param userType 用户类型
     */
    void removeLoginFail(Long userId, Integer userType);

    /**
     * 分页查询
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResponse<LoginAttemptVO> queryPage(LoginAttemptQueryForm queryForm);

    /**
     * 批量删除
     *
     * @param idList ID列表
     * @return 操作结果
     */
    ApiResult<String> batchDelete(List<Long> idList);

    /**
     * 获取登录失败最大次数
     *
     * @return 次数
     */
    Integer getLoginFailMaxTimes();

    /**
     * 获取登录失败锁定时长（秒）
     *
     * @return 秒数
     */
    Integer getLoginFailLockSeconds();
}
