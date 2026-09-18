package com.budaos.system.plugin;

import cn.hutool.core.util.StrUtil;
import com.budaos.system.domain.dto.DataPermissionSqlConfig;
import com.budaos.system.service.DataPermissionSqlConfigService;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * MyBatis数据权限拦截器
 *
 * 自动拦截带@DataScope注解的SQL查询，拼接数据权限条件
 *
 * @author budaos
 */
@Slf4j
@Intercepts({
        @Signature(
                type = org.apache.ibatis.executor.Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        )
})
public class MyBatisDataPermissionPlugin extends DataPermissionPlugin {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 手动设置 ApplicationContext（用于非 Spring 管理的实例）
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            Object parameter = invocation.getArgs()[1];

            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            String originalSql = boundSql.getSql().trim();
            String id = mappedStatement.getId();
            List<String> methodStrList = StrUtil.split(id, ".");
            String path = methodStrList.get(methodStrList.size() - 2) + "." + methodStrList.get(methodStrList.size() - 1);

            log.debug("MyBatisDataPermissionPlugin intercepted: id={}, path={}, originalSql={}", id, path, originalSql);

            DataPermissionSqlConfigService dataScopeSqlConfigService = this.dataScopeSqlConfigService();
            if (dataScopeSqlConfigService == null) {
                log.warn("DataPermissionSqlConfigService is null, skipping data scope");
                return invocation.proceed();
            }

            DataPermissionSqlConfig sqlConfigDTO = dataScopeSqlConfigService.getSqlConfig(path);
            if (sqlConfigDTO != null) {
                log.info("Found DataPermission config for path: {}, configCode: {}, joinSql: {}", path, sqlConfigDTO.getConfigCode(), sqlConfigDTO.getJoinSql());
                Map<String, Object> paramMap = this.getParamList(sqlConfigDTO.getParamName(), parameter);
                String joinSql = this.dataScopeSqlConfigService().getJoinSql(paramMap, sqlConfigDTO);
                log.info("Generated data scope SQL: {}", joinSql);
                if (StringUtils.isEmpty(joinSql)) {
                    log.warn("Empty joinSql generated, skipping data scope filter");
                    return invocation.proceed();
                }
                BoundSql newBoundSql = copyFromBoundSql(mappedStatement, boundSql, this.joinSql(originalSql, paramMap, sqlConfigDTO));
                String newSql = newBoundSql.getSql();
                log.info("Modified SQL: {}", newSql);
                ParameterMap map = mappedStatement.getParameterMap();
                MappedStatement newMs = copyFromMappedStatement(mappedStatement, new BoundSqlSqlSource(newBoundSql), map);
                invocation.getArgs()[0] = newMs;
            } else {
                log.warn("No DataPermission config found for path: {}", path);
            }

            Object obj = invocation.proceed();
            return obj;
        } catch (Exception e) {
            log.error("MyBatisDataPermissionPlugin intercept error", e);
            throw e;
        }
    }

    /**
     * 获取参数列表
     *
     * @param paramName 参数名称（逗号分隔）
     * @param parameter 参数对象
     * @return 参数Map
     */
    private Map<String, Object> getParamList(String paramName, Object parameter) {
        Map<String, Object> paramMap = Maps.newHashMap();
        if (StringUtils.isEmpty(paramName)) {
            return paramMap;
        }
        if (parameter == null) {
            return paramMap;
        }
        if (parameter instanceof Map) {
            String[] paramNameArray = paramName.split(",");
            Map<?, ?> parameterMap = (Map<?, ?>) parameter;
            for (String param : paramNameArray) {
                if (parameterMap.containsKey(param)) {
                    paramMap.put(param, parameterMap.get(param));
                }
            }
        }
        return paramMap;
    }

    /**
     * 拼接SQL
     *
     * @param sql 原始SQL
     * @param paramMap 参数Map
     * @param sqlConfigDTO SQL配置
     * @return 拼接后的SQL
     */
    private String joinSql(String sql, Map<String, Object> paramMap, DataPermissionSqlConfig sqlConfigDTO) {
        if (null == sqlConfigDTO) {
            return sql;
        }

        String appendSql = this.dataScopeSqlConfigService().getJoinSql(paramMap, sqlConfigDTO);
        if (StringUtils.isEmpty(appendSql)) {
            return sql;
        }

        Integer appendSqlWhereIndex = sqlConfigDTO.getWhereIndex();
        String where = "where";
        String order = "order by";
        String group = "group by";

        int whereIndex = StringUtils.ordinalIndexOf(sql.toLowerCase(), where, appendSqlWhereIndex + 1);
        int orderIndex = sql.toLowerCase().indexOf(order);
        int groupIndex = sql.toLowerCase().indexOf(group);

        if (whereIndex > -1) {
            String subSql = sql.substring(0, whereIndex + where.length() + 1);
            subSql = subSql + " " + appendSql + " AND " + sql.substring(whereIndex + where.length() + 1);
            return subSql;
        }

        if (groupIndex > -1) {
            String subSql = sql.substring(0, groupIndex);
            subSql = subSql + " where " + appendSql + " " + sql.substring(groupIndex);
            return subSql;
        }

        if (orderIndex > -1) {
            String subSql = sql.substring(0, orderIndex);
            subSql = subSql + " where " + appendSql + " " + sql.substring(orderIndex);
            return subSql;
        }

        sql += " where " + appendSql;
        return sql;
    }

    /**
     * 获取DataScopeSqlConfigService
     *
     * @return DataPermissionSqlConfigService
     */
    public DataPermissionSqlConfigService dataScopeSqlConfigService() {
        try {
            return (DataPermissionSqlConfigService) applicationContext.getBean("dataScopeSqlConfigService");
        } catch (Exception e) {
            // 如果Spring容器还未完全刷新，返回null
            return null;
        }
    }

    /**
     * BoundSql的SqlSource实现
     */
    public class BoundSqlSqlSource implements org.apache.ibatis.mapping.SqlSource {

        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    /**
     * 复制MappedStatement对象
     */
    private MappedStatement copyFromMappedStatement(MappedStatement ms, org.apache.ibatis.mapping.SqlSource newSqlSource, ParameterMap parameterMap) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
                ms.getConfiguration(),
                ms.getId(),
                newSqlSource,
                ms.getSqlCommandType()
        );
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(parameterMap);
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    /**
     * 复制BoundSql对象
     */
    private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql, String sql) {
        BoundSql newBoundSql = new BoundSql(
                ms.getConfiguration(),
                sql,
                boundSql.getParameterMappings(),
                boundSql.getParameterObject()
        );
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBoundSql;
    }

    @Override
    public Object plugin(Object arg0) {
        return Plugin.wrap(arg0, this);
    }

    @Override
    public void setProperties(Properties arg0) {

    }

}
