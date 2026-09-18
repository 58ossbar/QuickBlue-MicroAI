package com.budaos.system.service;


import com.budaos.system.dao.DataPermissionConfigDao;
import com.budaos.system.dao.StaffDao;
import com.budaos.system.constant.DataPermissionTypeEnum;
import com.budaos.system.constant.DataPermissionViewTypeEnum;
import com.budaos.common.core.enums.DataPermissionInTypeEnum;
import com.budaos.system.domain.dto.DataPermissionSqlConfig;
import com.budaos.system.domain.entity.DataPermissionConfigEntity;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.service.impl.DataPermissionServiceImpl;
import com.budaos.system.strategy.AbstractDataPermissionStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ClasspathHelper;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据范围SQL配置服务
 *
 * 负责扫描和管理所有带@DataScope注解的方法的SQL配置
 *
 * @author budaos
 */
@Slf4j
@Service
public class DataPermissionSqlConfigService {

    /**
     * 注解joinSql参数
     */
    private static final String EMPLOYEE_PARAM = "#employeeIds";

    private static final String DEPARTMENT_PARAM = "#departmentIds";

    /**
     * 数据权限配置DAO
     */
    @Resource
    private DataPermissionConfigDao dataScopeConfigDao;

    /**
     * 存储方法的SQL配置映射 <类名.方法名, DataPermissionSqlConfig>
     */
    private final ConcurrentHashMap<String, DataPermissionSqlConfig> dataScopeMethodMap = new ConcurrentHashMap<>();

    @Resource
    private DataPermissionViewService dataScopeViewService;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private StaffDao employeeDao;

    @PostConstruct
    private void initDataScopeMethodMap() {
        this.refreshDataScopeMethodMap();
    }

    /**
     * 刷新所有添加数据范围注解的接口方法配置
     * 支持扫描多个 DataPermission 注解（system 模块和 common 模块）
     *
     * @return 方法配置映射
     */
    private Map<String, DataPermissionSqlConfig> refreshDataScopeMethodMap() {
        try {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("com.budaos"))
                    .setScanners(new MethodAnnotationsScanner()));
            
            // 扫描 system 模块中的 DataPermission 注解
            scanDataScopeAnnotation(reflections, com.budaos.system.annotation.DataPermission.class);
            
            // 扫描 common 模块中的 DataPermission 注解
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> commonDataScopeClass = 
                    (Class<? extends Annotation>) Class.forName("com.budaos.common.core.annotation.DataPermission");
                scanDataScopeAnnotation(reflections, commonDataScopeClass);
            } catch (ClassNotFoundException e) {
                log.debug("Common DataPermission annotation not found, skipping");
            }
            
            log.info("DataPermissionSqlConfigService initialized successfully, total methods: {}", dataScopeMethodMap.size());
        } catch (Exception e) {
            log.error("Failed to initialize DataPermissionSqlConfigService", e);
        }
        return dataScopeMethodMap;
    }
    
    /**
     * 扫描指定类型的 DataPermission 注解
     *
     * @param reflections Reflections 实例
     * @param annotationClass 注解类型
     */
    private void scanDataScopeAnnotation(Reflections reflections, Class<? extends Annotation> annotationClass) {
        Set<Method> methods = reflections.getMethodsAnnotatedWith(annotationClass);
        for (Method method : methods) {
            Annotation annotation = method.getAnnotation(annotationClass);
            if (annotation != null) {
                try {
                    DataPermissionSqlConfig configDTO = new DataPermissionSqlConfig();
                    // 使用反射获取注解属性
                    configDTO.setConfigCode((String) annotation.getClass().getMethod("configCode").invoke(annotation));
                    configDTO.setJoinSql((String) annotation.getClass().getMethod("joinSql").invoke(annotation));
                    configDTO.setWhereIndex((Integer) annotation.getClass().getMethod("whereIndex").invoke(annotation));
                    configDTO.setDataScopeWhereInType((DataPermissionInTypeEnum) annotation.getClass().getMethod("whereInType").invoke(annotation));
                    configDTO.setParamName((String) annotation.getClass().getMethod("paramName").invoke(annotation));
                    configDTO.setSelfScopeColumn((String) annotation.getClass().getMethod("selfScopeColumn").invoke(annotation));
                    
                    // joinSqlImplClazz 需要特殊处理
                    @SuppressWarnings("unchecked")
                    Class<? extends AbstractDataPermissionStrategy> implClazz = 
                        (Class<? extends AbstractDataPermissionStrategy>) annotation.getClass().getMethod("joinSqlImplClazz").invoke(annotation);
                    configDTO.setJoinSqlImplClazz(implClazz);
                    
                    dataScopeMethodMap.put(method.getDeclaringClass().getSimpleName() + "." + method.getName(), configDTO);
                } catch (Exception e) {
                    log.warn("Failed to process DataPermission annotation on method: {}", method.getName(), e);
                }
            }
        }
    }

    /**
     * 根据调用的方法获取配置信息
     *
     * @param method 方法名（格式：类名.方法名）
     * @return SQL配置
     */
    public DataPermissionSqlConfig getSqlConfig(String method) {
        return this.dataScopeMethodMap.get(method);
    }

    /**
     * 组装需要拼接的SQL
     *
     * @param paramMap 参数Map
     * @param sqlConfigDTO SQL配置
     * @return 拼接后的SQL条件
     */
    public String getJoinSql(Map<String, Object> paramMap, DataPermissionSqlConfig sqlConfigDTO) {
        Long employeeId = getEmployeeId();
        if (employeeId == null) {
            log.debug("No employeeId found in token, returning empty data scope");
            return "";
        }

        String configCode = sqlConfigDTO.getConfigCode();
        if (org.apache.commons.lang3.StringUtils.isBlank(configCode)) {
            log.warn("Config code is empty, returning empty data scope");
            return "";
        }

        log.debug("Processing data scope for employeeId: {}, configCode: {}", employeeId, configCode);

        // 从数据库获取数据权限配置
        Integer configId = getConfigIdByConfigCode(configCode);
        if (configId == null) {
            log.warn("Failed to determine data scope type for configCode: {}, returning empty data scope", configCode);
            return "";
        }

        DataPermissionViewTypeEnum viewTypeEnum = dataScopeViewService.getEmployeeDataScopeViewType(configId, employeeId);

        log.debug("Data scope config: configCode={}, configId={}, viewType={}", configCode, configId, viewTypeEnum);

        String joinSql = sqlConfigDTO.getJoinSql();

        if (DataPermissionInTypeEnum.CUSTOM_STRATEGY == sqlConfigDTO.getDataScopeWhereInType()) {
            Class<?> strategyClass = sqlConfigDTO.getJoinSqlImplClazz();
            if (strategyClass == null) {
                log.warn("data scope custom strategy class is null");
                return "";
            }
            AbstractDataPermissionStrategy powerStrategy = (AbstractDataPermissionStrategy) applicationContext.getBean(sqlConfigDTO.getJoinSqlImplClazz());
            if (powerStrategy == null) {
                log.warn("data scope custom strategy class：{} ,bean is null", sqlConfigDTO.getJoinSqlImplClazz());
                return "";
            }
            return powerStrategy.getCondition(viewTypeEnum, paramMap, sqlConfigDTO);
        }

        if (DataPermissionInTypeEnum.EMPLOYEE == sqlConfigDTO.getDataScopeWhereInType()) {
            List<Long> canViewEmployeeIds = dataScopeViewService.getCanViewEmployeeId(viewTypeEnum, employeeId);
            log.debug("Employee IDs with permission: {}, viewType={}", canViewEmployeeIds, viewTypeEnum);
            // null 表示可以查看所有数据，不添加过滤条件
            if (canViewEmployeeIds == null) {
                return "";
            }
            // 空列表表示无数据权限，返回永远为false的条件
            if (CollectionUtils.isEmpty(canViewEmployeeIds)) {
                return "1=0";
            }
            String employeeIds = StringUtils.join(canViewEmployeeIds, ",");
            String sql = joinSql.replaceAll(EMPLOYEE_PARAM, employeeIds);
            log.debug("Generated data scope SQL: {}", sql);
            return sql;
        }

        if (DataPermissionInTypeEnum.DEPARTMENT == sqlConfigDTO.getDataScopeWhereInType()) {
            List<Long> canViewDepartmentIds = dataScopeViewService.getCanViewDepartmentId(viewTypeEnum, employeeId);
            log.debug("Department IDs with permission: {}, viewType={}", canViewDepartmentIds, viewTypeEnum);
            // null 表示可以查看所有部门数据，不添加过滤条件
            if (canViewDepartmentIds == null) {
                return "";
            }
            // 空列表表示无数据权限，返回永远为false的条件
            if (CollectionUtils.isEmpty(canViewDepartmentIds)) {
                return "1=0";
            }
            String departmentIds = StringUtils.join(canViewDepartmentIds, ",");
            String sql = joinSql.replaceAll(DEPARTMENT_PARAM, departmentIds);
            log.debug("Generated data scope SQL: {}", sql);
            return sql;
        }

        return "";
    }

    /**
     * 获取当前员工ID
     *
     * @return 员工ID
     */
    private Long getEmployeeId() {
        try {
            // 从Sa-Token获取当前登录用户ID
            String loginId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsString();
            if (loginId != null && loginId.contains(":")) {
                String[] parts = loginId.split(":");
                if (parts.length == 2) {
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get employee id from token", e);
        }
        return null;
    }

    /**
     * 根据配置编码获取配置ID
     *
     * @param configCode 配置编码
     * @return 配置ID（用作数据权限类型）
     */
    private Integer getConfigIdByConfigCode(String configCode) {
        try {
            // 从数据库查询配置
            DataPermissionConfigEntity configEntity = dataScopeConfigDao.selectByConfigCode(configCode);
            if (configEntity == null) {
                log.warn("Data scope config not found for configCode: {}", configCode);
                return null;
            }

            // 返回config_id作为数据权限类型值
            return configEntity.getConfigId().intValue();
        } catch (Exception e) {
            log.error("Failed to get config id by configCode: {}", configCode, e);
            return null;
        }
    }

}
