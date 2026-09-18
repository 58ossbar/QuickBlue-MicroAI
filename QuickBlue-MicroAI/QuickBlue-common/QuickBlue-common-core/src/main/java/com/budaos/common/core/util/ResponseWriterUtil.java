package com.budaos.common.core.util;

import com.budaos.common.core.domain.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 响应工具类
 *
 * @author budaos
 */
public class ResponseWriterUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 设置下载文件响应头
     *
     * @param response HttpServletResponse
     * @param fileName 文件名
     * @param fileSize 文件大小
     */
    public static void setDownloadFileHeader(HttpServletResponse response, String fileName, Long fileSize) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        if (fileSize != null) {
            response.setContentLengthLong(fileSize);
        }
    }

    /**
     * 写入响应
     *
     * @param response HttpServletResponse
     * @param responseDTO 响应对象
     */
    public static void write(HttpServletResponse response, ApiResult<?> responseDTO) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(OBJECT_MAPPER.writeValueAsString(responseDTO));
        } catch (IOException e) {
            throw new RuntimeException("写入响应失败", e);
        }
    }
}
