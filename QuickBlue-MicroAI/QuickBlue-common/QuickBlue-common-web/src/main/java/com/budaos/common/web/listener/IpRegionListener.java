package com.budaos.common.web.listener;

import lombok.extern.slf4j.Slf4j;
import com.budaos.common.core.util.IpRegionUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * 初始化IP地址解析工具类
 * 在应用启动时加载 ip2region.xdb 数据库文件
 *
 */
@Slf4j
public class IpRegionListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final String IP_FILE_NAME = "ip2region.xdb";

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
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
            IpRegionUtil.init(tempFile.getAbsolutePath());
            log.info("IP地址解析工具初始化成功");

        } catch (IOException e) {
            log.error("初始化IP地址解析工具失败", e);
        }
    }
}
