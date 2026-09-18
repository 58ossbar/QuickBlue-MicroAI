package com.budaos.common.web.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.exception.BizException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>将各类异常统一转换为 {@link ApiResult} 返回，并控制日志级别：</p>
 * <ul>
 *   <li>客户端可预期的异常（业务异常、参数校验、未登录、无权限等）使用 warn 级别</li>
 *   <li>非预期的系统异常使用 error 级别并记录完整堆栈</li>
 * </ul>
 *
 * @author budaos
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public ApiResult<Void> handleBusinessException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ApiResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public ApiResult<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录异常: {}", e.getMessage());
        return ApiResult.error(UserErrorCodes.LOGIN_STATE_INVALID);
    }

    /**
     * 权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ApiResult<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足异常: {}", e.getMessage());
        return ApiResult.error(UserErrorCodes.NO_PERMISSION);
    }

    /**
     * 角色不足异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ApiResult<Void> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足异常: {}", e.getMessage());
        return ApiResult.error(UserErrorCodes.NO_PERMISSION);
    }

    /**
     * 参数校验异常（@Valid 请求体）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return paramError(message);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ApiResult<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return paramError(message);
    }

    /**
     * 参数校验异常（@Validated 方法参数）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return paramError(message);
    }

    /**
     * 请求资源不存在（404）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResult<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("请求资源不存在: {}", e.getMessage());
        return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST, "接口不存在");
    }

    /**
     * 其他未预期异常
     *
     * <p>兼容业务代码直接抛出带提示信息的 RuntimeException 的写法，
     * 将消息透传给前端用于提示，同时记录完整堆栈便于排查。</p>
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResult.error(e.getMessage(), null);
    }

    /**
     * 参数校验失败统一返回
     */
    private ApiResult<Void> paramError(String message) {
        log.warn("参数校验异常: {}", message);
        return ApiResult.paramError(message);
    }
}
