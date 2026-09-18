package com.budaos.support.util;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 表字典安全校验工具类
 * 防止SQL注入攻击
 *
 * @author QuickBlue
 */
@Slf4j
@Component
public class DictTableSecurityUtil {

    /**
     * 表名白名单（从数据库加载）
     */
    private List<DictTableWhiteList> whiteList;

    /**
     * SQL注入关键字黑名单
     */
    private static final String[] SQL_INJECTION_KEYWORDS = {
        "'", "\"", ";", "--", "/*", "*/", "xp_", "exec", "execute",
        "insert", "update", "delete", "drop", "alter", "create", "truncate",
        "union", "join", "having", "group", "order", "limit", "offset"
    };

    /**
     * 表名和字段名正则表达式（只允许字母、数字、下划线）
     */
    private static final Pattern TABLE_FIELD_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    /**
     * 检查表名是否在白名单中
     *
     * @param tableName  表名
     * @param keyField   键字段
     * @param labelField 标签字段
     * @return true-通过校验，false-未通过校验
     */
    public boolean isInWhiteList(String tableName, String keyField, String labelField) {
        if (whiteList == null || whiteList.isEmpty()) {
            log.warn("表字典白名单为空，请先配置白名单数据");
            return false;
        }

        return whiteList.stream()
                .anyMatch(item ->
                        item.getTableName().equalsIgnoreCase(tableName) &&
                        item.getKeyField().equalsIgnoreCase(keyField) &&
                        item.getLabelField().equalsIgnoreCase(labelField) &&
                        item.getStatus() == 1
                );
    }

    /**
     * 检查表名或字段名是否合法
     *
     * @param name 表名或字段名
     * @return true-合法，false-不合法
     */
    public boolean isValidTableOrFieldName(String name) {
        if (StrUtil.isBlank(name)) {
            return false;
        }

        // 检查是否只包含字母、数字、下划线
        if (!TABLE_FIELD_PATTERN.matcher(name).matches()) {
            log.warn("表名或字段名格式不合法: {}", name);
            return false;
        }

        // 检查长度限制
        if (name.length() > 64) {
            log.warn("表名或字段名长度超过限制: {}", name);
            return false;
        }

        return true;
    }

    /**
     * 检查查询条件是否包含SQL注入
     *
     * @param condition 查询条件
     * @return true-安全，false-可能存在SQL注入
     */
    public boolean isSafeCondition(String condition) {
        if (StrUtil.isBlank(condition)) {
            return true;
        }

        String lowerCondition = condition.toLowerCase();

        // 检查是否包含SQL注入关键字
        for (String keyword : SQL_INJECTION_KEYWORDS) {
            if (lowerCondition.contains(keyword)) {
                log.warn("查询条件包含可能的SQL注入关键字: keyword={}, condition={}", keyword, condition);
                return false;
            }
        }

        // 检查是否包含注释符号
        if (lowerCondition.contains("--") || lowerCondition.contains("/*") || lowerCondition.contains("*/")) {
            log.warn("查询条件包含注释符号: {}", condition);
            return false;
        }

        return true;
    }

    /**
     * 完整的安全校验
     *
     * @param tableName  表名
     * @param keyField   键字段
     * @param labelField 标签字段
     * @param condition  查询条件
     * @return 错误信息，如果为null表示校验通过
     */
    public String validate(String tableName, String keyField, String labelField, String condition) {
        // 1. 检查表名和字段名格式
        if (!isValidTableOrFieldName(tableName)) {
            return "表名格式不合法: " + tableName;
        }
        if (!isValidTableOrFieldName(keyField)) {
            return "键字段格式不合法: " + keyField;
        }
        if (!isValidTableOrFieldName(labelField)) {
            return "标签字段格式不合法: " + labelField;
        }

        // 2. 检查查询条件安全性
        if (!isSafeCondition(condition)) {
            return "查询条件可能存在SQL注入风险";
        }

        // 3. 检查是否在白名单中
        if (!isInWhiteList(tableName, keyField, labelField)) {
            return "表字典未在白名单中: table=" + tableName + ", key=" + keyField + ", label=" + labelField;
        }

        return null; // 校验通过
    }

    /**
     * 设置白名单列表
     *
     * @param whiteList 白名单列表
     */
    public void setWhiteList(List<DictTableWhiteList> whiteList) {
        this.whiteList = whiteList;
        log.info("表字典白名单已更新，共 {} 条记录", whiteList.size());
    }

    /**
     * 表字典白名单实体
     */
    @Data
    public static class DictTableWhiteList {
        private Long id;
        private String tableName;
        private String keyField;
        private String labelField;
        private String remark;
        private Integer status;
    }
}
