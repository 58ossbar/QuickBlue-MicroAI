package com.budaos.support.nacos.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.code.SystemErrorCodes;
import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.common.core.util.RequestContextUtil;
import com.budaos.support.nacos.dao.SysConfigAuditDao;
import com.budaos.support.nacos.domain.entity.SysConfigAuditEntity;
import com.budaos.support.nacos.domain.form.ConfigAuditQueryForm;
import com.budaos.support.nacos.domain.form.NacosConfigForm;
import com.budaos.support.nacos.domain.vo.ConfigAuditVO;
import com.budaos.support.nacos.domain.vo.ConfigHistoryVO;
import com.budaos.support.nacos.domain.vo.NacosConfigVO;
import com.budaos.support.nacos.domain.vo.NacosNamespaceVO;
import com.budaos.support.nacos.domain.vo.NacosSyncItemVO;
import com.budaos.support.nacos.domain.vo.NacosSyncResultVO;
import com.budaos.support.nacos.service.NacosConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Nacos配置管理服务实现
 *
 * @author budaos
 * @since 2026-02-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NacosConfigServiceImpl implements NacosConfigService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SysConfigAuditDao sysConfigAuditDao;
    private final ApplicationContext applicationContext;

    @Value("${spring.cloud.nacos.config.server-addr:}")
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.config.username:}")
    private String nacosUsername;

    @Value("${spring.cloud.nacos.config.password:}")
    private String nacosPassword;

    /** 本地 nacos_config 目录（为空时自动探测，找不到则回退 classpath:nacos-config/） */
    @Value("${quickblue.nacos.sync.local-dir:}")
    private String localConfigDir;

    /** 一键同步的目标分组 */
    @Value("${quickblue.nacos.sync.group:${NACOS_GROUP:QuickBlue_GROUP}}")
    private String syncGroup;

    /**
     * Nacos 鉴权开关：
     * auto(默认，先尝试登录，失败则按未开启鉴权访问) / true(必须鉴权) / false(明确不鉴权，不发起登录)
     */
    @Value("${quickblue.nacos.auth.enabled:auto}")
    private String nacosAuthEnabled;

    /** 缓存的 accessToken（Nacos 默认有效期 18 小时） */
    private volatile String accessTokenCache;

    /** 已判定为"未开启鉴权"，后续请求不再带 token */
    private volatile boolean authDisabled;

    /** classpath 内内置配置的存放位置（由构建打包 nacos_config 目录而来） */
    private static final String CLASSPATH_CONFIG_PREFIX = "nacos-config/";

    /**
     * 获取 accessToken
     * <p>
     * Nacos 2.x 默认不开启鉴权，此时不存在 nacos 用户，登录接口返回 500 "User nacos not found"。
     * 因此登录失败时降级为不带 token 访问，保证配置管理功能在两种环境下都可用。
     *
     * @return accessToken，未开启鉴权或登录失败时返回 null
     */
    private String getAccessToken() {
        if (authDisabled) {
            return null;
        }
        if (StringUtils.hasText(accessTokenCache)) {
            return accessTokenCache;
        }
        if ("false".equalsIgnoreCase(nacosAuthEnabled)) {
            authDisabled = true;
            return null;
        }

        try {
            String url = getNacosUrl() + "/nacos/v1/auth/login";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("username", nacosUsername);
            params.add("password", nacosPassword);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if (jsonNode.has("accessToken")) {
                    accessTokenCache = jsonNode.get("accessToken").asText();
                    return accessTokenCache;
                }
            }
        } catch (Exception e) {
            if ("true".equalsIgnoreCase(nacosAuthEnabled)) {
                log.error("获取Nacos accessToken失败，已强制开启鉴权，请检查 spring.cloud.nacos.config.username/password 配置", e);
                return null;
            }
            log.warn("Nacos 登录失败，按「未开启鉴权」继续访问（如实际已开启鉴权，请配置正确的 Nacos 账号）: {}",
                    e.getMessage());
            authDisabled = true;
            return null;
        }

        log.warn("Nacos 登录接口未返回 accessToken，按「未开启鉴权」继续访问");
        authDisabled = true;
        return null;
    }

    /**
     * 拼接 accessToken 参数（以 &amp; 开头），未鉴权时返回空串
     */
    private String tokenParam() {
        String token = getAccessToken();
        return StringUtils.hasText(token) ? "&accessToken=" + token : "";
    }

    /**
     * 拼接 accessToken 参数（以 ? 开头），未鉴权时返回空串
     */
    private String tokenQuery() {
        String token = getAccessToken();
        return StringUtils.hasText(token) ? "?accessToken=" + token : "";
    }

    /**
     * 获取Nacos服务器URL
     */
    private String getNacosUrl() {
        return "http://" + nacosServerAddr;
    }

    /**
     * 获取当前登录用户
     */
    private CurrentUser getCurrentUser() {
        try {
            return RequestContextUtil.getRequestUser();
        } catch (Exception e) {
            log.warn("获取当前登录用户失败", e);
        }
        return null;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
        }
        return "unknown";
    }

    /**
     * 保存审计记录
     */
    private void saveAuditRecord(String dataId, String groupId, String tenantId, String configName,
                                  String oldContent, String newContent, String opType, String remark) {
        CurrentUser user = getCurrentUser();
        SysConfigAuditEntity entity = SysConfigAuditEntity.builder()
                .dataId(dataId)
                .groupId(groupId)
                .tenantId(tenantId)
                .configName(configName)
                .oldContent(oldContent)
                .newContent(newContent)
                .opType(opType)
                .operatorId(user != null ? user.getUserId() : null)
                .operatorName(user != null ? user.getUserName() : "系统")
                .operatorIp(getClientIp())
                .remark(remark)
                .createTime(LocalDateTime.now())
                .build();
        sysConfigAuditDao.insert(entity);
    }

    @Override
    public ApiResult<List<NacosNamespaceVO>> listNamespaces() {
        try {
            String url = getNacosUrl() + "/nacos/v1/console/namespaces" + tokenQuery();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if (jsonNode.has("data")) {
                    List<NacosNamespaceVO> namespaces = objectMapper.readValue(
                            jsonNode.get("data").toString(),
                            new TypeReference<List<NacosNamespaceVO>>() {}
                    );
                    return ApiResult.ok(namespaces);
                }
            }
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "获取命名空间列表失败");
        } catch (Exception e) {
            log.error("获取Nacos命名空间列表失败", e);
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "获取命名空间列表失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<PageResponse<NacosConfigVO>> listConfigs(String tenantId, String groupId, Integer pageNo, Integer pageSize) {
        try {
            String targetGroupId = StringUtils.hasText(groupId) ? groupId : "QuickBlue_GROUP";
            String targetTenantId = StringUtils.hasText(tenantId) ? tenantId : "";
            
            String url = getNacosUrl() + "/nacos/v1/cs/configs?search=blur&dataId=&group=" + targetGroupId +
                    "&tenant=" + targetTenantId + 
                    "&pageNo=" + (pageNo != null ? pageNo : 1) +
                    "&pageSize=" + (pageSize != null ? pageSize : 100) +
                    tokenParam();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                List<NacosConfigVO> configList = new ArrayList<>();
                if (jsonNode.has("pageItems")) {
                    JsonNode pageItems = jsonNode.get("pageItems");
                    for (JsonNode item : pageItems) {
                        NacosConfigVO vo = new NacosConfigVO();
                        vo.setDataId(item.has("dataId") ? item.get("dataId").asText() : "");
                        vo.setGroupId(item.has("group") ? item.get("group").asText() : targetGroupId);
                        vo.setTenantId(targetTenantId);
                        
                        String dataId = vo.getDataId();
                        if (dataId.endsWith(".yaml")) {
                            vo.setType("yaml");
                        } else if (dataId.endsWith(".properties")) {
                            vo.setType("properties");
                        } else {
                            vo.setType("text");
                        }
                        
                        vo.setConfigName(inferConfigName(dataId));
                        configList.add(vo);
                    }
                }
                
                int total = jsonNode.has("totalCount") ? jsonNode.get("totalCount").asInt() : configList.size();
                int currentPageNo = pageNo != null ? pageNo : 1;
                int currentPageSize = pageSize != null ? pageSize : 10;
                
                PageResponse<NacosConfigVO> pageResult = new PageResponse<>();
                pageResult.setList(configList);
                pageResult.setTotal((long) total);
                pageResult.setPageNum((long) currentPageNo);
                pageResult.setPageSize((long) currentPageSize);
                
                return ApiResult.ok(pageResult);
            }
            
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "获取配置列表失败");
        } catch (Exception e) {
            log.error("获取Nacos配置列表失败", e);
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "获取配置列表失败: " + e.getMessage());
        }
    }
    
    private String inferConfigName(String dataId) {
        if (dataId == null || dataId.isEmpty()) {
            return "未知配置";
        }
        
        String name = dataId.replaceAll("\\.(yaml|properties|json|xml|txt)$", "");
        
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("mysql-common", "数据库配置");
        nameMap.put("redis-common", "Redis缓存配置");
        nameMap.put("sa-token-common", "认证Token配置");
        nameMap.put("common-config", "通用配置");
        nameMap.put("level3-protect-common", "三级等保配置");
        nameMap.put("QuickBlue-gateway", "网关服务配置");
        nameMap.put("QuickBlue-system", "系统服务配置");
        nameMap.put("QuickBlue-business", "业务服务配置");
        nameMap.put("QuickBlue-support", "支撑服务配置");
        nameMap.put("application", "应用配置");
        
        return nameMap.getOrDefault(name, name);
    }

    @Override
    public ApiResult<NacosConfigVO> getConfig(String dataId, String groupId, String tenantId) {
        try {
            String url = getNacosUrl() + "/nacos/v1/cs/configs?dataId=" + dataId +
                    "&group=" + groupId +
                    "&tenant=" + (tenantId != null ? tenantId : "") +
                    tokenParam();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                NacosConfigVO vo = new NacosConfigVO();
                vo.setDataId(dataId);
                vo.setGroupId(groupId);
                vo.setTenantId(tenantId);
                vo.setContent(response.getBody());
                vo.setType(dataId.endsWith(".yaml") ? "yaml" : (dataId.endsWith(".properties") ? "properties" : "text"));
                return ApiResult.ok(vo);
            }
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST, "配置不存在");
        } catch (Exception e) {
            log.error("获取Nacos配置失败", e);
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "获取配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> publishConfig(NacosConfigForm form) {
        try {
            String oldContent = null;
            ApiResult<NacosConfigVO> oldConfig = getConfig(form.getDataId(), form.getGroupId(), form.getTenantId());
            if (Boolean.TRUE.equals(oldConfig.getOk()) && oldConfig.getData() != null) {
                oldContent = oldConfig.getData().getContent();
            }

            String url = getNacosUrl() + "/nacos/v1/cs/configs" + tokenQuery();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("dataId", form.getDataId());
            params.add("group", form.getGroupId());
            params.add("content", form.getContent());
            params.add("type", form.getType() != null ? form.getType() : "yaml");
            if (form.getTenantId() != null) {
                params.add("tenant", form.getTenantId());
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && "true".equals(response.getBody())) {
                saveAuditRecord(
                        form.getDataId(),
                        form.getGroupId(),
                        form.getTenantId(),
                        form.getConfigName(),
                        oldContent,
                        form.getContent(),
                        oldContent == null ? "CREATE" : "UPDATE",
                        form.getRemark()
                );
                return ApiResult.ok("配置发布成功");
            }
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "配置发布失败");
        } catch (Exception e) {
            log.error("发布Nacos配置失败", e);
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "配置发布失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> deleteConfig(String dataId, String groupId, String tenantId) {
        try {
            String oldContent = null;
            ApiResult<NacosConfigVO> oldConfig = getConfig(dataId, groupId, tenantId);
            if (Boolean.TRUE.equals(oldConfig.getOk()) && oldConfig.getData() != null) {
                oldContent = oldConfig.getData().getContent();
            }

            String url = getNacosUrl() + "/nacos/v1/cs/configs?dataId=" + dataId +
                    "&group=" + groupId +
                    "&tenant=" + (tenantId != null ? tenantId : "") +
                    tokenParam();

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);

            if (response.getStatusCode() == HttpStatus.OK && "true".equals(response.getBody())) {
                saveAuditRecord(dataId, groupId, tenantId, null, oldContent, null, "DELETE", "删除配置");
                return ApiResult.ok("配置删除成功");
            }
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "配置删除失败");
        } catch (Exception e) {
            log.error("删除Nacos配置失败", e);
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "配置删除失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<PageResponse<ConfigHistoryVO>> getConfigHistory(String dataId, String groupId, String tenantId, Integer pageNo, Integer pageSize) {
        try {
            String url = getNacosUrl() + "/nacos/v1/cs/history?search=accurate" +
                    "&dataId=" + dataId +
                    "&group=" + groupId +
                    "&tenant=" + (tenantId != null ? tenantId : "") +
                    "&pageNo=" + (pageNo != null ? pageNo : 1) +
                    "&pageSize=" + (pageSize != null ? pageSize : 10) +
                    tokenParam();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                List<ConfigHistoryVO> historyList = new ArrayList<>();
                
                if (jsonNode.has("pageItems")) {
                    historyList = objectMapper.readValue(
                            jsonNode.get("pageItems").toString(),
                            new TypeReference<List<ConfigHistoryVO>>() {}
                    );
                }

                PageResponse<ConfigHistoryVO> pageResult = new PageResponse<>();
                pageResult.setList(historyList);
                pageResult.setTotal(jsonNode.has("totalCount") ? jsonNode.get("totalCount").asLong() : historyList.size());
                pageResult.setPageNum(jsonNode.has("pageNumber") ? jsonNode.get("pageNumber").asLong() : 1);
                pageResult.setPageSize(jsonNode.has("pageSize") ? jsonNode.get("pageSize").asLong() : pageSize);
                
                return ApiResult.ok(pageResult);
            }
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "获取配置历史失败");
        } catch (Exception e) {
            log.error("获取Nacos配置历史失败", e);
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "获取配置历史失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<ConfigHistoryVO> getConfigHistoryDetail(String nid, String dataId, String groupId, String tenantId) {
        try {
            String url = getNacosUrl() + "/nacos/v1/cs/history?nid=" + nid +
                    "&dataId=" + dataId +
                    "&group=" + groupId +
                    "&tenant=" + (tenantId != null ? tenantId : "") +
                    tokenParam();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ConfigHistoryVO history = objectMapper.readValue(response.getBody(), ConfigHistoryVO.class);
                return ApiResult.ok(history);
            }
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST, "历史版本不存在");
        } catch (Exception e) {
            log.error("获取Nacos配置历史详情失败", e);
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "获取历史详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> rollbackConfig(String nid, String dataId, String groupId, String tenantId) {
        try {
            ApiResult<ConfigHistoryVO> historyResult = getConfigHistoryDetail(nid, dataId, groupId, tenantId);
            if (!Boolean.TRUE.equals(historyResult.getOk()) || historyResult.getData() == null) {
                return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST, "历史版本不存在");
            }

            ConfigHistoryVO history = historyResult.getData();
            
            NacosConfigForm form = new NacosConfigForm();
            form.setDataId(dataId);
            form.setGroupId(groupId);
            form.setTenantId(tenantId);
            form.setContent(history.getContent());
            form.setType("yaml");
            form.setRemark("回滚到版本: " + nid);

            return publishConfig(form);
        } catch (Exception e) {
            log.error("回滚Nacos配置失败", e);
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "回滚配置失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<PageResponse<ConfigAuditVO>> queryAuditByPage(ConfigAuditQueryForm queryForm) {
        Page<SysConfigAuditEntity> page = PageConvertUtil.convert2PageQuery(queryForm);
        List<SysConfigAuditEntity> entityList = sysConfigAuditDao.queryByPage(page, queryForm);
        List<ConfigAuditVO> voList = BeanCopyUtil.copyList(entityList, ConfigAuditVO.class);

        Map<String, String> opTypeMap = new HashMap<>();
        opTypeMap.put("CREATE", "新增");
        opTypeMap.put("UPDATE", "修改");
        opTypeMap.put("DELETE", "删除");
        voList.forEach(vo -> vo.setOpTypeDesc(opTypeMap.getOrDefault(vo.getOpType(), vo.getOpType())));

        PageResponse<ConfigAuditVO> pageResult = PageConvertUtil.convert2PageResult(page, voList);
        return ApiResult.ok(pageResult);
    }

    @Override
    public ApiResult<ConfigAuditVO> getAuditDetail(Long auditId) {
        SysConfigAuditEntity entity = sysConfigAuditDao.selectById(auditId);
        if (entity == null) {
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST);
        }
        ConfigAuditVO vo = BeanCopyUtil.copyProperties(entity, ConfigAuditVO.class);
        
        Map<String, String> opTypeMap = new HashMap<>();
        opTypeMap.put("CREATE", "新增");
        opTypeMap.put("UPDATE", "修改");
        opTypeMap.put("DELETE", "删除");
        vo.setOpTypeDesc(opTypeMap.getOrDefault(vo.getOpType(), vo.getOpType()));
        
        return ApiResult.ok(vo);
    }

    @Override
    public ApiResult<List<NacosConfigVO>> exportConfigs(List<String> dataIds, String tenantId, String groupId) {
        try {
            ApiResult<PageResponse<NacosConfigVO>> configsResult = listConfigs(tenantId, groupId, 1, 1000);
            if (!Boolean.TRUE.equals(configsResult.getOk()) || configsResult.getData() == null) {
                return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "获取配置列表失败");
            }

            List<NacosConfigVO> exportList = new ArrayList<>();
            List<NacosConfigVO> configList = configsResult.getData().getList();
            
            if (dataIds != null && !dataIds.isEmpty()) {
                configList = configList.stream()
                        .filter(c -> dataIds.contains(c.getDataId()))
                        .toList();
            }

            for (NacosConfigVO config : configList) {
                ApiResult<NacosConfigVO> detailResult = getConfig(config.getDataId(), config.getGroupId(), config.getTenantId());
                if (Boolean.TRUE.equals(detailResult.getOk()) && detailResult.getData() != null) {
                    NacosConfigVO vo = detailResult.getData();
                    vo.setConfigName(config.getConfigName());
                    exportList.add(vo);
                }
            }
            return ApiResult.ok(exportList);
        } catch (Exception e) {
            log.error("导出Nacos配置失败", e);
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "导出配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> importConfigs(List<NacosConfigForm> configs) {
        if (configs == null || configs.isEmpty()) {
            return ApiResult.error(UserErrorCodes.PARAM_ERROR, "配置列表不能为空");
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();

        for (NacosConfigForm form : configs) {
            try {
                ApiResult<String> result = publishConfig(form);
                if (Boolean.TRUE.equals(result.getOk())) {
                    successCount++;
                } else {
                    failCount++;
                    errorMsg.append(form.getDataId()).append(": ").append(result.getMsg()).append("; ");
                }
            } catch (Exception e) {
                failCount++;
                errorMsg.append(form.getDataId()).append(": ").append(e.getMessage()).append("; ");
            }
        }

        if (failCount == 0) {
            return ApiResult.ok(String.format("导入成功，共导入 %d 个配置", successCount));
        } else if (successCount > 0) {
            return ApiResult.ok(String.format("部分导入成功，成功 %d 个，失败 %d 个。失败原因: %s", 
                    successCount, failCount, errorMsg.toString()));
        } else {
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR, "导入失败: " + errorMsg.toString());
        }
    }

    @Override
    public ApiResult<NacosSyncResultVO> syncFromLocal(String tenantId) {
        List<LocalConfigFile> files = new ArrayList<>();
        String source = loadLocalConfigFiles(files);

        if (files.isEmpty()) {
            return ApiResult.error(SystemErrorCodes.BUSINESS_ERROR,
                    "未扫描到本地配置文件。请设置 quickblue.nacos.sync.local-dir 指向 nacos_config 目录，或重新打包以内置 classpath:nacos-config/");
        }

        String groupId = StringUtils.hasText(syncGroup) ? syncGroup : "QuickBlue_GROUP";
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
        List<NacosSyncItemVO> results = new ArrayList<>();

        for (LocalConfigFile file : files) {
            String dataId = file.getFileName();
            try {
                String oldContent = fetchRemoteContent(dataId, groupId, tenantId);
                if (oldContent != null && normalizeContent(oldContent).equals(normalizeContent(file.getContent()))) {
                    skipCount++;
                    results.add(NacosSyncItemVO.builder()
                            .fileName(file.getFileName())
                            .dataId(dataId)
                            .success(true)
                            .message("内容无变化，已跳过")
                            .build());
                    continue;
                }

                NacosConfigForm form = new NacosConfigForm();
                form.setDataId(dataId);
                form.setGroupId(groupId);
                form.setTenantId(tenantId);
                form.setContent(file.getContent());
                form.setType(resolveConfigType(dataId));
                form.setConfigName(inferConfigName(dataId));
                form.setRemark("一键同步（来源: " + source + "）");

                ApiResult<String> publishResult = publishConfig(form);
                boolean published = Boolean.TRUE.equals(publishResult.getOk());
                if (published) {
                    successCount++;
                } else {
                    failCount++;
                }
                results.add(NacosSyncItemVO.builder()
                        .fileName(file.getFileName())
                        .dataId(dataId)
                        .success(published)
                        .message(published
                                ? (oldContent == null ? "新增成功" : "更新成功")
                                : StringUtils.hasText(publishResult.getMsg()) ? publishResult.getMsg() : "发布失败")
                        .build());
            } catch (Exception e) {
                failCount++;
                log.error("同步配置失败: {}", dataId, e);
                results.add(NacosSyncItemVO.builder()
                        .fileName(file.getFileName())
                        .dataId(dataId)
                        .success(false)
                        .message(e.getMessage())
                        .build());
            }
        }

        NacosSyncResultVO vo = new NacosSyncResultVO();
        vo.setTotalFiles(files.size());
        vo.setSuccessCount(successCount);
        vo.setFailCount(failCount);
        vo.setSkipCount(skipCount);
        vo.setTenantId(tenantId == null ? "" : tenantId);
        vo.setGroupId(groupId);
        vo.setSource(source);
        vo.setResults(results);

        log.info("一键同步完成，来源={}，共{}个文件，成功{}，失败{}，跳过{}",
                source, files.size(), successCount, failCount, skipCount);
        return ApiResult.ok(vo);
    }

    /**
     * 加载本地配置文件：优先文件系统目录，找不到则回退 classpath 内置配置
     *
     * @param files 输出参数，收集到的配置文件
     * @return 配置来源描述
     */
    private String loadLocalConfigFiles(List<LocalConfigFile> files) {
        Path dir = resolveLocalConfigDir();
        if (dir != null) {
            try (Stream<Path> stream = Files.walk(dir, 2)) {
                List<Path> paths = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> isConfigFile(p.getFileName().toString()))
                        .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                        .toList();
                for (Path path : paths) {
                    files.add(new LocalConfigFile(path.getFileName().toString(),
                            Files.readString(path, StandardCharsets.UTF_8)));
                }
            } catch (Exception e) {
                log.error("读取本地配置目录失败: {}", dir, e);
                files.clear();
            }
        }
        if (!files.isEmpty()) {
            return dir != null ? dir.toString() : "";
        }
        files.addAll(loadConfigFilesFromClasspath());
        return "classpath:" + CLASSPATH_CONFIG_PREFIX;
    }

    /**
     * 探测本地 nacos_config 目录：优先取配置的路径，否则按常见相对位置依次探测
     *
     * @return 命中的目录，未找到返回 null
     */
    private Path resolveLocalConfigDir() {
        List<String> candidates = new ArrayList<>();
        if (StringUtils.hasText(localConfigDir)) {
            candidates.add(localConfigDir);
        }
        candidates.addAll(List.of(
                "nacos_config",
                "../nacos_config",
                "../../nacos_config",
                "../../../nacos_config",
                "../QuickBlue-MicroAI/nacos_config",
                "../../QuickBlue-MicroAI/nacos_config"
        ));

        for (String candidate : candidates) {
            try {
                Path path = Paths.get(candidate).toAbsolutePath().normalize();
                if (Files.isDirectory(path) && containsConfigFile(path)) {
                    return path;
                }
            } catch (Exception e) {
                log.debug("候选配置目录不可用: {}", candidate);
            }
        }
        return null;
    }

    private boolean containsConfigFile(Path dir) {
        try (Stream<Path> stream = Files.walk(dir, 2)) {
            return stream.anyMatch(p -> Files.isRegularFile(p) && isConfigFile(p.getFileName().toString()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 读取打包时内置到 classpath 的配置（nacos_config 目录）
     */
    private List<LocalConfigFile> loadConfigFilesFromClasspath() {
        List<LocalConfigFile> files = new ArrayList<>();
        try {
            ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(applicationContext);
            Resource[] resources = resolver.getResources("classpath*:" + CLASSPATH_CONFIG_PREFIX + "**/*");
            for (Resource resource : resources) {
                String name = resource.getFilename();
                if (name == null || !isConfigFile(name)) {
                    continue;
                }
                String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                files.add(new LocalConfigFile(name, content));
            }
            files.sort(Comparator.comparing(LocalConfigFile::getFileName));
        } catch (Exception e) {
            log.error("读取 classpath 内置配置失败", e);
        }
        return files;
    }

    /**
     * 查询 Nacos 上已有的配置内容，不存在时返回 null（不抛异常、不打错误日志）
     */
    private String fetchRemoteContent(String dataId, String groupId, String tenantId) {
        try {
            String url = getNacosUrl() + "/nacos/v1/cs/configs?dataId=" + dataId +
                    "&group=" + groupId +
                    "&tenant=" + (tenantId != null ? tenantId : "") +
                    tokenParam();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return null;
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.debug("查询Nacos配置未命中，按新增处理: group={}, dataId={}", groupId, dataId);
            return null;
        }
    }

    private String normalizeContent(String content) {
        return content == null ? "" : content.replace("\r\n", "\n").trim();
    }

    private String resolveConfigType(String dataId) {
        if (dataId.endsWith(".yaml") || dataId.endsWith(".yml")) {
            return "yaml";
        }
        if (dataId.endsWith(".properties")) {
            return "properties";
        }
        return "text";
    }

    private boolean isConfigFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".yaml") || lower.endsWith(".yml") || lower.endsWith(".properties");
    }

    /**
     * 本地配置文件（文件名即 Nacos 的 dataId）
     */
    @Getter
    @AllArgsConstructor
    private static class LocalConfigFile {
        private final String fileName;
        private final String content;
    }
}
