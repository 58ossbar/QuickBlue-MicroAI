package com.budaos.common.web.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * IP地址解析配置
 * 初始化 ip2region 数据库文件，用于IP地址归属地查询
 *
 * @author budaos
 */
@Component
@Slf4j
public class IpRegionConfig {

    private static final String IP_FILE_NAME = "ip2region.xdb";

    /**
     * 初始化 IP地址解析工具
     * 在应用启动时加载 ip2region.xdb 数据库文件
     */
    @PostConstruct
    public void initIp2Region() {
        try {
            // 1. 从 classpath 中加载 ip2region.xdb 文件
            ClassPathResource resource = new ClassPathResource(IP_FILE_NAME);
            if (!resource.exists()) {
                log.warn("ip2region.xdb 文件不存在，IP地址解析功能将不可用");
                return;
            }

            // 2. 复制到临时文件
            File tempFile = File.createTempFile("ip2region", ".xdb");
            tempFile.deleteOnExit();
            FileUtils.copyInputStreamToFile(resource.getInputStream(), tempFile);

            // 3. 初始化 IpRegionUtil
            com.budaos.common.core.util.IpRegionUtil.init(tempFile.getAbsolutePath());
            log.info("IP地址解析工具初始化成功");

        } catch (IOException e) {
            log.error("初始化IP地址解析工具失败", e);
        }
    }

}
