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
import com.budaos.support.nacos.service.NacosConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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

    @Value("${spring.cloud.nacos.config.server-addr:}")
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.config.username:}")
    private String nacosUsername;

    @Value("${spring.cloud.nacos.config.password:}")
    private String nacosPassword;

    /**
     * 获取accessToken
     */
    private String getAccessToken() {
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
                return jsonNode.has("accessToken") ? jsonNode.get("accessToken").asText() : null;
            }
        } catch (Exception e) {
            log.error("获取Nacos accessToken失败", e);
        }
        return null;
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
            String url = getNacosUrl() + "/nacos/v1/console/namespaces?accessToken=" + getAccessToken();
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
                    "&accessToken=" + getAccessToken();
            
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
                    "&accessToken=" + getAccessToken();

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

            String url = getNacosUrl() + "/nacos/v1/cs/configs?accessToken=" + getAccessToken();
            
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
                    "&accessToken=" + getAccessToken();

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
                    "&accessToken=" + getAccessToken();

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
                    "&accessToken=" + getAccessToken();

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
}
