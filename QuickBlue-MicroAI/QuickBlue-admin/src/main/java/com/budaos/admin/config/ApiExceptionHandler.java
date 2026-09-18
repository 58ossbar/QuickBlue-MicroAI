package com.budaos.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

/**
 * 全局异常处理
 * 主要处理客户端断开连接等可忽略的异常
 */
@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    /**
     * 处理客户端断开连接异常 (不记录错误日志)
     * 这通常发生在用户关闭浏览器、刷新页面或取消请求时
     */
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException e) {
        String message = e.getMessage();
        // 客户端断开连接的常见情况，只记录 debug 级别
        if (message != null && (
            message.contains("Broken pipe") ||
            message.contains("Connection reset") ||
            message.contains("远程主机强迫关闭") ||
            message.contains("软件中止了一个已建立的连接") ||
            message.contains("An established connection was aborted")
        )) {
            log.debug("客户端断开连接: {}", message);
        } else {
            log.warn("IO异常: {}", message, e);
        }
    }
}
