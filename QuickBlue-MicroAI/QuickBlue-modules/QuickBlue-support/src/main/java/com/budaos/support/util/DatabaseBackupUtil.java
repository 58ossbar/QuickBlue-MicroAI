package com.budaos.support.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MySQL数据库备份工具类
 */
@Slf4j
public class DatabaseBackupUtil {

    /**
     * 数据库备份状态枚举
     */
    public enum BackupStatus {
        BACKUPING(0, "备份中"),
        SUCCESS(1, "备份成功"),
        FAILED(2, "备份失败");

        private final Integer value;
        private final String desc;

        BackupStatus(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 备份类型枚举
     */
    public enum BackupType {
        AUTO(1, "自动备份"),
        MANUAL(2, "手动备份");

        private final Integer value;
        private final String desc;

        BackupType(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 备份结果类
     */
    public static class BackupResult {
        private Boolean success;
        private String fileName;
        private String filePath;
        private Long fileSize;
        private String errorMessage;

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 查找mysqldump命令路径
     */
    public static String findMysqldumpPath(String customPath) {
        // 如果用户指定了路径，使用指定的路径
        if (StrUtil.isNotBlank(customPath)) {
            return customPath;
        }

        String osName = System.getProperty("os.name").toLowerCase();
        String mysqldumpName = osName.contains("windows") ? "mysqldump.exe" : "mysqldump";

        // 尝试从系统PATH查找
        String[] possiblePaths = new String[]{
                mysqldumpName,
                "/usr/bin/" + mysqldumpName,
                "/usr/local/bin/" + mysqldumpName,
                "/usr/local/mysql/bin/" + mysqldumpName,
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\" + mysqldumpName,
                "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\" + mysqldumpName,
                "C:\\Program Files\\MySQL\\MySQL Server 5.6\\bin\\" + mysqldumpName,
                "C:\\xampp\\mysql\\bin\\" + mysqldumpName,
                "D:\\xampp\\mysql\\bin\\" + mysqldumpName,
        };

        for (String path : possiblePaths) {
            if (new File(path).exists()) {
                log.info("找到mysqldump: {}", path);
                return path;
            }
        }

        // 没有找到，返回默认命令名称
        log.warn("未找到mysqldump，使用命令: {}", mysqldumpName);
        return mysqldumpName;
    }

    /**
     * 备份数据库
     *
     * @param dbHost 数据库主机
     * @param dbPort 数据库端口
     * @param dbName 数据库名
     * @param dbUsername 数据库用户名
     * @param dbPassword 数据库密码
     * @param backupPath 备份路径
     * @param mysqldumpPath mysqldump路径
     * @return 备份结果
     */
    public static BackupResult backupDatabase(String dbHost, Integer dbPort, String dbName,
                                                String dbUsername, String dbPassword,
                                                String backupPath, String mysqldumpPath) {
        BackupResult result = new BackupResult();
        result.setSuccess(false);

        try {
            // 查找mysqldump路径
            String actualMysqldumpPath = findMysqldumpPath(mysqldumpPath);

            // 创建备份目录
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // 生成备份文件名
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = dbName + "_" + sdf.format(new Date()) + ".sql";
            String filePath = backupPath + File.separator + fileName;

            // 构建mysqldump命令
            List<String> command = new ArrayList<>();
            command.add(actualMysqldumpPath);
            command.add("-h" + dbHost);
            command.add("-P" + dbPort);
            command.add("-u" + dbUsername);
            command.add("-p" + dbPassword);
            command.add("--default-character-set=utf8mb4");
            command.add("--single-transaction");
            command.add("--routines");
            command.add("--triggers");
            command.add("--events");
            command.add(dbName);

            // 执行备份命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // 写入备份文件
                FileUtil.writeStringToFile(filePath, output.toString(), "UTF-8");

                // 获取文件大小
                File backupFile = new File(filePath);
                long fileSize = backupFile.length();

                result.setSuccess(true);
                result.setFileName(fileName);
                result.setFilePath(filePath);
                result.setFileSize(fileSize);

                log.info("数据库备份成功: {}, 大小: {} bytes", filePath, fileSize);
            } else {
                result.setErrorMessage("备份失败，mysqldump退出码: " + exitCode);
                log.error("数据库备份失败，退出码: {}, 输出: {}", exitCode, output);
            }

        } catch (Exception e) {
            result.setErrorMessage("备份异常: " + e.getMessage());
            log.error("数据库备份异常", e);
        }

        return result;
    }

    /**
     * 文件操作工具类
     */
    private static class FileUtil {
        public static void writeStringToFile(String filePath, String content, String encoding) throws Exception {
            java.nio.file.Files.write(new File(filePath).toPath(), content.getBytes(encoding));
        }
    }
}
