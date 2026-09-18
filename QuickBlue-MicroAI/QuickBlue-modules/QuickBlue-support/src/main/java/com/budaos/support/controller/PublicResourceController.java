package com.budaos.support.controller;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 公共资源访问控制器
 * 解决头像等静态资源访问问题
 */
@Slf4j
@RestController
@RequestMapping("/public")
@Tag(name = "公共资源", description = "公共静态资源访问")
public class PublicResourceController {

    @Value("${file.storage.local.upload-path:./uploads}")
    private String uploadPath;

    @Resource
    private AttachmentService fileService;

    /**
     * 访问公共目录下的文件
     * 解决 public/common/ 等路径的访问问题
     */
    @GetMapping("/**")
    @Operation(summary = "访问公共文件", description = "访问public目录下的静态文件")
    public ResponseEntity<byte[]> getPublicFile(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String contextPath = request.getContextPath();

            // 移除contextPath和前缀
            String relativePath = requestURI;
            if (contextPath != null && !contextPath.isEmpty()) {
                relativePath = relativePath.substring(contextPath.length());
            }

            // 移除 /public 前缀
            if (relativePath.startsWith("/public/")) {
                relativePath = relativePath.substring("/public/".length());
            }

            if (relativePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 构建文件完整路径
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve("public").resolve(relativePath).normalize();

            // 安全检查：确保文件在public目录内
            if (!filePath.startsWith(uploadDir.resolve("public"))) {
                log.warn("非法访问尝试，路径: {}, 请求URI: {}", filePath, requestURI);
                return ResponseEntity.badRequest().build();
            }

            // 检查文件是否存在
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                log.debug("公共文件不存在，路径: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 检查文件是否可读
            if (!Files.isReadable(filePath)) {
                log.warn("公共文件不可读，路径: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 读取文件内容
            byte[] fileContent = Files.readAllBytes(filePath);

            // 确定内容类型
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            // 对于图片文件，添加缓存头
            if (contentType.startsWith("image/")) {
                headers.setCacheControl("public, max-age=86400"); // 缓存1天
                headers.setExpires(System.currentTimeMillis() + 86400000);
                headers.setETag(String.valueOf(Files.getLastModifiedTime(filePath).toMillis()));
            } else {
                headers.setCacheControl("public, max-age=3600"); // 缓存1小时
            }

            return new ResponseEntity<>(fileContent, headers, org.springframework.http.HttpStatus.OK);

        } catch (IOException e) {
            log.error("获取公共文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * 检查公共文件是否存在
     */
    @GetMapping("/check/**")
    @Operation(summary = "检查公共文件是否存在", description = "检查指定路径的公共文件是否存在")
    public ApiResult<Boolean> checkPublicFileExists(HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            String contextPath = request.getContextPath();

            // 提取相对路径
            String relativePath = requestURI;
            if (contextPath != null && !contextPath.isEmpty()) {
                relativePath = relativePath.substring(contextPath.length());
            }

            if (relativePath.startsWith("/public/check/")) {
                relativePath = relativePath.substring("/public/check/".length());
            }

            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path filePath = uploadDir.resolve("public").resolve(relativePath).normalize();

            boolean exists = Files.exists(filePath) && !Files.isDirectory(filePath);
            return ApiResult.ok(exists);

        } catch (Exception e) {
            log.error("检查公共文件存在性失败", e);
            return ApiResult.userErrorParam("检查失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查公共资源访问健康状态")
    public ApiResult<String> healthCheck() {
        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path publicDir = uploadDir.resolve("public");

            if (Files.exists(publicDir) && Files.isWritable(publicDir)) {
                return ApiResult.ok("公共资源访问正常");
            } else {
                return ApiResult.userErrorParam("公共目录不可访问: " + publicDir);
            }
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return ApiResult.userErrorParam("健康检查失败: " + e.getMessage());
        }
    }
}
