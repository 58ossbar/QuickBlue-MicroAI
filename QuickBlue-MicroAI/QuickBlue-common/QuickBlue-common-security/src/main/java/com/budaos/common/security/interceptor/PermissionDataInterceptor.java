package com.budaos.common.security.interceptor;

import cn.hutool.core.util.StrUtil;
import com.budaos.common.security.context.PermissionDataContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * 数据权限拦截器
 * 自动在SQL中注入数据权限条件
 *
 * @author QuickBlue
 */
@Slf4j
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class PermissionDataInterceptor implements Interceptor {

    private static final String[] IGNORE_STATEMENT_IDS = {
            "com.budaos.system.dao.SysPermissionDataRuleDao",
            "com.budaos.support.dao.DictionaryDao",
            "com.budaos.support.dao.DictionaryDataDao"
    };

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 1. 获取参数
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        // 2. 检查是否需要拦截
        if (!shouldIntercept(ms.getId())) {
            return invocation.proceed();
        }

        // 3. 获取权限SQL
        String permissionSql = PermissionDataContext.getPermissionSql();
        if (StrUtil.isBlank(permissionSql)) {
            return invocation.proceed();
        }

        // 4. 获取原始SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql().toUpperCase();

        // 5. 注入权限条件（只处理SELECT语句）
        if (!originalSql.trim().startsWith("SELECT")) {
            return invocation.proceed();
        }

        // 6. 构建新的SQL
        String newSql = injectPermissionCondition(boundSql.getSql(), permissionSql);

        if (StrUtil.isNotBlank(newSql) && !newSql.equals(boundSql.getSql())) {
            log.debug("数据权限SQL注入: 权限条件={}", permissionSql);
            log.debug("数据权限SQL注入: 新SQL={}", newSql);

            // 7. 创建新的BoundSql并设置到MappedStatement
            BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), newSql, boundSql.getParameterMappings(), parameter);
            // 复制额外的参数
            for (String key : boundSql.getAdditionalParameters().keySet()) {
                newBoundSql.setAdditionalParameter(key, boundSql.getAdditionalParameters().get(key));
            }

            // 8. 创建新的MappedStatement
            MappedStatement newMs = copyMappedStatement(ms, new BoundSqlSourceImpl(newBoundSql));

            // 9. 替换参数中的MappedStatement
            args[0] = newMs;
        }

        return invocation.proceed();
    }

    /**
     * 判断是否需要拦截
     */
    private boolean shouldIntercept(String statementId) {
        // 排除指定的Mapper
        for (String ignoreId : IGNORE_STATEMENT_IDS) {
            if (statementId.startsWith(ignoreId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 注入权限条件到SQL
     * 简化版：使用字符串操作，只处理简单场景
     */
    private String injectPermissionCondition(String sql, String permissionSql) {
        String upperSql = sql.toUpperCase();

        // 查找WHERE子句
        int whereIndex = upperSql.indexOf(" WHERE ");
        int groupByIndex = upperSql.indexOf(" GROUP BY ");
        int orderByIndex = upperSql.indexOf(" ORDER BY ");
        int havingIndex = upperSql.indexOf(" HAVING ");
        int limitIndex = upperSql.indexOf(" LIMIT ");

        // 确定插入位置
        int insertPos = sql.length();
        if (whereIndex > 0) {
            insertPos = Math.min(insertPos, whereIndex + 7);
        }
        if (groupByIndex > 0 && groupByIndex > whereIndex) {
            insertPos = Math.min(insertPos, groupByIndex);
        }
        if (orderByIndex > 0 && orderByIndex > whereIndex) {
            insertPos = Math.min(insertPos, orderByIndex);
        }
        if (havingIndex > 0 && havingIndex > whereIndex) {
            insertPos = Math.min(insertPos, havingIndex);
        }
        if (limitIndex > 0 && limitIndex > whereIndex) {
            insertPos = Math.min(insertPos, limitIndex);
        }

        // 构建条件
        String condition;
        if (whereIndex > 0 && insertPos == whereIndex + 7) {
            condition = " AND (" + permissionSql + ") ";
        } else {
            condition = " WHERE (" + permissionSql + ") ";
        }

        return sql.substring(0, insertPos) + condition + sql.substring(insertPos);
    }

    /**
     * 复制MappedStatement
     */
    private MappedStatement copyMappedStatement(MappedStatement ms, BoundSqlSourceImpl newBoundSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
                ms.getConfiguration(),
                ms.getId(),
                newBoundSqlSource,
                ms.getSqlCommandType()
        );

        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件读取配置
    }

    /**
     * 自定义SqlSource实现
     */
    private static class BoundSqlSourceImpl implements SqlSource {
        private final BoundSql boundSql;

        public BoundSqlSourceImpl(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
