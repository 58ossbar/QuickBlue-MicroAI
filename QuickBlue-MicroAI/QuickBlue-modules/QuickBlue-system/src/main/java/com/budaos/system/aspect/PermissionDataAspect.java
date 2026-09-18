package com.budaos.system.aspect;

import cn.hutool.core.util.StrUtil;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.security.context.PermissionDataContext;
import com.budaos.system.annotation.PermissionData;
import com.budaos.system.domain.entity.SysPermissionDataRule;
import com.budaos.system.service.SysPermissionDataRuleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据权限切面
 * 用于在方法执行前查询数据权限规则，并构建权限SQL
 *
 * @author QuickBlue
 */
@Slf4j
@Aspect
@Component
public class PermissionDataAspect {

    @Resource
    private SysPermissionDataRuleService permissionDataRuleService;

    // TODO: 从登录上下文获取当前用户
    // CurrentUser currentUser = SecurityUtil.getCurrentUser();
    // 这里暂时使用 ThreadLocal 或其他方式获取

    @Around("@annotation(com.budaos.system.annotation.PermissionData)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取当前用户
        CurrentUser currentUser = getCurrentUser();
        if (currentUser == null) {
            log.warn("数据权限切面：当前用户未登录，跳过数据权限控制");
            return point.proceed();
        }

        // 获取注解
        MethodSignature signature = (MethodSignature) point.getSignature();
        PermissionData permissionData = signature.getMethod().getAnnotation(PermissionData.class);
        if (permissionData == null) {
            return point.proceed();
        }

        // 获取方法名
        String methodName = signature.getMethod().getName();

        // 获取权限ID（从页面组件中获取）
        Long permissionId = getPermissionId(permissionData.pageComponent());
        if (permissionId == null) {
            log.warn("数据权限切面：未找到权限ID，跳过数据权限控制");
            return point.proceed();
        }

        // 查询数据权限规则
        List<SysPermissionDataRule> ruleList = permissionDataRuleService.queryRuleList(permissionId);
        if (ruleList == null || ruleList.isEmpty()) {
            log.debug("数据权限切面：权限规则为空，跳过数据权限控制");
            return point.proceed();
        }

        // 构建权限SQL
        String permissionSql = buildPermissionSql(ruleList, currentUser);
        if (StrUtil.isBlank(permissionSql)) {
            log.debug("数据权限切面：权限SQL为空，跳过数据权限控制");
            return point.proceed();
        }

        // 设置权限SQL到ThreadLocal
        PermissionDataContext.setPermissionSql(permissionSql);

        log.debug("数据权限切面：设置权限SQL: {}", permissionSql);

        try {
            // 执行方法
            return point.proceed();
        } finally {
            // 清除ThreadLocal
            PermissionDataContext.clear();
        }
    }

    /**
     * 获取当前用户
     * TODO: 从登录上下文获取，暂时返回null
     */
    private CurrentUser getCurrentUser() {
        // TODO: 实现从登录上下文获取当前用户
        // 示例：return SecurityUtil.getCurrentUser();
        // 或从 ThreadLocal 获取
        return null;
    }

    /**
     * 构建权限SQL
     */
    private String buildPermissionSql(List<SysPermissionDataRule> ruleList, CurrentUser currentUser) {
        StringBuilder sql = new StringBuilder();

        // 构建SQL变量
        Map<String, Object> sqlVariables = new HashMap<>();
        // 注意：RequestUser接口中没有这些字段，需要根据实际实现调整
        // sqlVariables.put("sys_user_code", currentUser.getLoginName());
        // sqlVariables.put("sys_user_id", currentUser.getEmployeeId());
        // sqlVariables.put("sys_depart_id", currentUser.getDepartmentId());
        // sqlVariables.put("sys_depart_ids", currentUser.getDepartmentIds());
        // sqlVariables.put("sys_user_name", currentUser.getName());
        
        // 暂时使用RequestUser接口中可用的字段
        sqlVariables.put("sys_user_id", currentUser.getUserId());
        sqlVariables.put("sys_user_name", currentUser.getUserName());

        // 遍历权限规则
        for (int i = 0; i < ruleList.size(); i++) {
            SysPermissionDataRule rule = ruleList.get(i);

            if (i > 0) {
                sql.append(" OR ");
            }

            sql.append(rule.getRuleColumn()).append(" ");
            sql.append(rule.getRuleConditions()).append(" ");

            // 替换SQL变量
            String ruleValue = replaceVariables(rule.getRuleValue(), sqlVariables);
            sql.append(ruleValue);
        }

        return sql.toString();
    }

    /**
     * 替换SQL变量
     */
    private String replaceVariables(String ruleValue, Map<String, Object> variables) {
        String result = ruleValue;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String value = String.valueOf(entry.getValue());
            // 处理IN语句的情况
            if (value.startsWith("[") && value.endsWith("]")) {
                // 数组形式，直接替换
                result = result.replace("#{" + entry.getKey() + "}", value);
            } else {
                // 字符串形式，添加引号
                result = result.replace("#{" + entry.getKey() + "}", "'" + value + "'");
            }
        }
        return result;
    }

    /**
     * 获取权限ID（从页面组件中获取）
     * 实际项目中应该从菜单权限表中查询
     * 这里简化处理，返回null表示不启用数据权限
     */
    private Long getPermissionId(String pageComponent) {
        // TODO: 实际项目中应该根据pageComponent从菜单权限表中查询permissionId
        // 这里简化处理，如果pageComponent不为空，假设权限ID为1
        if (StrUtil.isNotBlank(pageComponent)) {
            return 1L;
        }
        return null;
    }
}
