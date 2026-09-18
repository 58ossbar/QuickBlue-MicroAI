package com.budaos.common.web.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * OpenFeign 请求拦截器
 * 自动传递 Token、TraceId、用户信息等请求头
 *
 * @author budaos
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TRACE_ID_HEADER = "Trace-Id";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String TENANT_ID_HEADER = "Tenant-Id";

    private static final String INNER_PATH = "/inner/";

    @Override
    public void apply(RequestTemplate template) {
        try {
            String url = template.url();

            // 内部接口（/inner/）不需要传递请求头，直接返回
            if (url.startsWith(INNER_PATH)) {
                return;
            }

            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String authorization = request.getHeader(AUTHORIZATION_HEADER);
            if (authorization != null && !authorization.trim().isEmpty()) {
                template.header(AUTHORIZATION_HEADER, authorization);
            }

            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (traceId != null && !traceId.trim().isEmpty()) {
                template.header(TRACE_ID_HEADER, traceId);
            } else {
                traceId = generateTraceId();
                template.header(TRACE_ID_HEADER, traceId);
            }

            String userId = request.getHeader(USER_ID_HEADER);
            if (userId != null && !userId.trim().isEmpty()) {
                template.header(USER_ID_HEADER, userId);
            }

            String tenantId = request.getHeader(TENANT_ID_HEADER);
            if (tenantId != null && !tenantId.trim().isEmpty()) {
                template.header(TENANT_ID_HEADER, tenantId);
            }

        } catch (Exception e) {
        }
    }

    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
