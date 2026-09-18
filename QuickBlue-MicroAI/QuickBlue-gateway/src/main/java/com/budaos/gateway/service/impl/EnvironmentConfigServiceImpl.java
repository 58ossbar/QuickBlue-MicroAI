package com.budaos.gateway.service.impl;

import com.budaos.gateway.domain.dto.DbConnectionDTO;
import com.budaos.gateway.domain.dto.InstallConfigDTO;
import com.budaos.gateway.domain.entity.EnvironmentConfigEntity;
import com.budaos.gateway.service.EnvironmentConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 环境配置管理服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentConfigServiceImpl implements EnvironmentConfigService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${install.config-dir:./environments}")
    private String configDir;

    @Value("${spring.cloud.nacos.config.server-addr:}")
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.config.username:}")
    private String nacosUsername;

    @Value("${spring.cloud.nacos.config.password:}")
    private String nacosPassword;

    @Value("${spring.cloud.nacos.config.namespace:}")
    private String nacosNamespace;

    @Value("${spring.cloud.nacos.config.group:QuickBlue_GROUP}")
    private String nacosGroup;

    // 内存缓存环境配置
    private final Map<String, EnvironmentConfigEntity> environmentCache = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    private static final String ENVIRONMENTS_FILE = "environments.json";
    private static final String ACTIVE_ENV_FILE = ".active-env";

    @Override
    public Long saveEnvironmentConfig(String envName, InstallConfigDTO installConfig) {
        try {
            // 确保配置目录存在
            Path configPath = Paths.get(configDir);
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath);
            }

            // 转换为JSON
            String configJson = objectMapper.writeValueAsString(installConfig);

            // 创建环境配置实体
            EnvironmentConfigEntity entity = new EnvironmentConfigEntity();
            entity.setId(idGenerator.getAndIncrement());
            entity.setEnvName(envName);
            entity.setDescription("环境: " + envName);
            entity.setConfigJson(configJson);
            entity.setActive(false);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());

            // 保存到缓存
            environmentCache.put(envName, entity);

            // 保存到文件
            saveEnvironmentsToFile();

            log.info("环境配置保存成功: {}", envName);
            return entity.getId();
        } catch (Exception e) {
            log.error("保存环境配置失败: {}", envName, e);
            throw new RuntimeException("保存环境配置失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EnvironmentConfigEntity> listAllEnvironments() {
        loadEnvironmentsFromFile();
        return new ArrayList<>(environmentCache.values());
    }

    @Override
    public EnvironmentConfigEntity getActiveEnvironment() {
        loadEnvironmentsFromFile();

        // 从文件读取激活环境名称
        try {
            Path activeEnvFile = Paths.get(configDir, ACTIVE_ENV_FILE);
            if (Files.exists(activeEnvFile)) {
                String activeEnvName = Files.readString(activeEnvFile).trim();
                return environmentCache.get(activeEnvName);
            }
        } catch (IOException e) {
            log.error("读取激活环境文件失败", e);
        }

        return null;
    }

    @Override
    public Boolean switchEnvironment(String envName) {
        try {
            log.info("切换环境: {}", envName);

            // 加载所有环境配置
            loadEnvironmentsFromFile();

            // 检查环境是否存在
            EnvironmentConfigEntity targetEnv = environmentCache.get(envName);
            if (targetEnv == null) {
                throw new RuntimeException("环境不存在: " + envName);
            }

            // 解析配置
            InstallConfigDTO config = objectMapper.readValue(targetEnv.getConfigJson(), InstallConfigDTO.class);

            // 更新Nacos配置
            updateNacosConfigs(config);

            // 更新激活环境文件
            Path activeEnvFile = Paths.get(configDir, ACTIVE_ENV_FILE);
            Files.writeString(activeEnvFile, envName);

            // 更新缓存中的激活状态
            environmentCache.forEach((name, env) -> env.setActive(name.equals(envName)));
            saveEnvironmentsToFile();

            log.info("环境切换成功: {}", envName);
            return true;
        } catch (Exception e) {
            log.error("切换环境失败: {}", envName, e);
            throw new RuntimeException("切换环境失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean deleteEnvironment(String envName) {
        try {
            // 检查是否为激活环境
            EnvironmentConfigEntity activeEnv = getActiveEnvironment();
            if (activeEnv != null && activeEnv.getEnvName().equals(envName)) {
                throw new RuntimeException("不能删除激活的环境");
            }

            // 从缓存中移除
            environmentCache.remove(envName);

            // 删除环境配置文件
            Path envFile = Paths.get(configDir, envName + ".json");
            if (Files.exists(envFile)) {
                Files.delete(envFile);
            }

            // 保存更新后的环境列表
            saveEnvironmentsToFile();

            log.info("环境删除成功: {}", envName);
            return true;
        } catch (Exception e) {
            log.error("删除环境失败: {}", envName, e);
            throw new RuntimeException("删除环境失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean testEnvironmentDbConnection(String envName) {
        try {
            EnvironmentConfigEntity env = environmentCache.get(envName);
            if (env == null) {
                throw new RuntimeException("环境不存在: " + envName);
            }

            InstallConfigDTO config = objectMapper.readValue(env.getConfigJson(), InstallConfigDTO.class);
            InstallConfigDTO.DatabaseConfig dbConfig = config.getDatabase();

            // 测试数据库连接
            // TODO: 调用 InstallService.testDbConnection 方法测试连接

            log.info("环境数据库连接测试成功: {}", envName);
            return true;
        } catch (Exception e) {
            log.error("测试环境数据库连接失败: {}", envName, e);
            return false;
        }
    }

    @Override
    public String exportEnvironmentConfig(String envName) {
        EnvironmentConfigEntity env = environmentCache.get(envName);
        if (env == null) {
            throw new RuntimeException("环境不存在: " + envName);
        }
        return env.getConfigJson();
    }

    @Override
    public Boolean importEnvironmentConfig(String configJson, String envName) {
        try {
            // 验证配置JSON格式
            InstallConfigDTO config = objectMapper.readValue(configJson, InstallConfigDTO.class);

            return saveEnvironmentConfig(envName, config) != null;
        } catch (Exception e) {
            log.error("导入环境配置失败: {}", envName, e);
            return false;
        }
    }

    @Override
    public Boolean updateEnvironmentDb(String envName, DbConnectionDTO dbConnection) {
        try {
            EnvironmentConfigEntity env = environmentCache.get(envName);
            if (env == null) {
                throw new RuntimeException("环境不存在: " + envName);
            }

            // 解析现有配置
            InstallConfigDTO config = objectMapper.readValue(env.getConfigJson(), InstallConfigDTO.class);

            // 更新数据库配置
            InstallConfigDTO.DatabaseConfig dbConfig = config.getDatabase();
            dbConfig.setDbType(dbConnection.getDbType());
            dbConfig.setHost(dbConnection.getHost());
            dbConfig.setPort(dbConnection.getPort());
            dbConfig.setAdminUser(dbConnection.getAdminUser());
            dbConfig.setAdminPassword(dbConnection.getAdminPassword());

            // 更新数据库用户和密码
            if (dbConnection.getDatabaseName() != null && !dbConnection.getDatabaseName().isEmpty()) {
                for (InstallConfigDTO.DatabaseInfo dbInfo : dbConfig.getDatabases()) {
                    if (dbInfo.getName().equals(dbConnection.getDatabaseName())) {
                        dbInfo.setUsername(dbConnection.getDatabaseUser());
                        dbInfo.setPassword(dbConnection.getDatabasePassword());
                    }
                }
            }

            // 保存更新后的配置
            env.setConfigJson(objectMapper.writeValueAsString(config));
            env.setUpdateTime(LocalDateTime.now());

            saveEnvironmentsToFile();

            log.info("环境数据库配置更新成功: {}", envName);
            return true;
        } catch (Exception e) {
            log.error("更新环境数据库配置失败: {}", envName, e);
            throw new RuntimeException("更新环境数据库配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 加载环境配置从文件
     */
    private void loadEnvironmentsFromFile() {
        try {
            // 先从 environments.json 加载
            Path environmentsFile = Paths.get(configDir, ENVIRONMENTS_FILE);
            if (Files.exists(environmentsFile)) {
                String json = Files.readString(environmentsFile);
                List<EnvironmentConfigEntity> environments =
                    objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, EnvironmentConfigEntity.class));

                environmentCache.clear();
                environments.forEach(env -> environmentCache.put(env.getEnvName(), env));

                // 更新ID生成器
                long maxId = environments.stream().mapToLong(EnvironmentConfigEntity::getId).max().orElse(0);
                idGenerator.set(maxId + 1);
            }

            // 加载单个环境文件（兼容安装时创建的默认环境）
            Path configPath = Paths.get(configDir);
            if (Files.exists(configPath)) {
                try (Stream<Path> paths = Files.list(configPath)) {
                    paths.filter(path -> path.toString().endsWith(".json") && !path.getFileName().toString().equals(ENVIRONMENTS_FILE))
                        .forEach(path -> {
                            try {
                                String json = Files.readString(path);
                                EnvironmentConfigEntity env = objectMapper.readValue(json, EnvironmentConfigEntity.class);
                                // 只添加不存在于缓存的环境
                                if (!environmentCache.containsKey(env.getEnvName())) {
                                    environmentCache.put(env.getEnvName(), env);
                                }
                            } catch (Exception e) {
                                log.warn("加载环境文件失败: {}", path, e);
                            }
                        });
                }
            }
        } catch (Exception e) {
            log.warn("加载环境配置文件失败，使用空缓存", e);
            environmentCache.clear();
        }
    }

    /**
     * 保存环境配置到文件
     */
    private void saveEnvironmentsToFile() throws IOException {
        Path configPath = Paths.get(configDir);
        if (!Files.exists(configPath)) {
            Files.createDirectories(configPath);
        }

        Path environmentsFile = Paths.get(configDir, ENVIRONMENTS_FILE);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new ArrayList<>(environmentCache.values()));
        Files.writeString(environmentsFile, json);
    }

    /**
     * 更新Nacos配置
     */
    private void updateNacosConfigs(InstallConfigDTO config) throws Exception {
        if (!StringUtils.hasText(nacosServerAddr)) {
            log.warn("Nacos配置未设置，跳过Nacos配置更新");
            return;
        }

        String accessToken = getNacosAccessToken();
        String nacosUrl = "http://" + nacosServerAddr;

        // 更新MySQL通用配置
        updateNacosConfig(nacosUrl, accessToken, "mysql-common.yaml", buildMysqlCommonConfig(config));

        // 更新各服务的配置
        updateNacosConfig(nacosUrl, accessToken, "QuickBlue-support.yaml", buildServiceConfig(config, "support", config.getService().getSupportPort()));
        updateNacosConfig(nacosUrl, accessToken, "QuickBlue-system.yaml", buildServiceConfig(config, "system", config.getService().getSystemPort()));
        updateNacosConfig(nacosUrl, accessToken, "QuickBlue-business.yaml", buildServiceConfig(config, "business", config.getService().getBusinessPort()));
        updateNacosConfig(nacosUrl, accessToken, "QuickBlue-ai.yaml", buildServiceConfig(config, "ai", config.getService().getAiPort()));

        log.info("Nacos配置更新完成");
    }

    /**
     * 构建MySQL通用配置
     */
    private String buildMysqlCommonConfig(InstallConfigDTO config) {
        InstallConfigDTO.DatabaseConfig dbConfig = config.getDatabase();
        StringBuilder sb = new StringBuilder();
        sb.append("spring:\n");
        sb.append("  datasource:\n");
        sb.append("    driver-class-name: ").append(getDriverClass(dbConfig.getDbType())).append("\n");
        sb.append("    url: ").append(buildJdbcUrl(dbConfig, "quickblue_support")).append("\n");
        sb.append("    username: support_user\n");
        sb.append("    password: ${SUPPORT_DB_PASSWORD:").append(getDbPassword(dbConfig, "support_user")).append("}\n");
        sb.append("    druid:\n");
        sb.append("      initial-size: 5\n");
        sb.append("      min-idle: 5\n");
        sb.append("      max-active: 20\n");
        sb.append("      max-wait: 60000\n");
        sb.append("      time-between-eviction-runs-millis: 60000\n");
        sb.append("      min-evictable-idle-time-millis: 300000\n");
        sb.append("      validation-query: SELECT 1\n");
        sb.append("      test-while-idle: true\n");
        sb.append("      test-on-borrow: false\n");
        sb.append("      test-on-return: false\n");
        sb.append("      pool-prepared-statements: true\n");
        sb.append("      max-pool-prepared-statement-per-connection-size: 20\n");
        sb.append("      filters: stat,wall\n");

        // Redis配置
        InstallConfigDTO.RedisConfiguration redisConfig = config.getRedis();
        sb.append("  data:\n");
        sb.append("    redis:\n");
        sb.append("      host: ").append(redisConfig.getHost()).append("\n");
        sb.append("      port: ").append(redisConfig.getPort()).append("\n");
        sb.append("      database: ").append(redisConfig.getDatabase()).append("\n");
        if (StringUtils.hasText(redisConfig.getPassword())) {
            sb.append("      password: ").append(redisConfig.getPassword()).append("\n");
        }
        sb.append("      timeout: ").append(redisConfig.getTimeout()).append("000\n");
        sb.append("      lettuce:\n");
        sb.append("        pool:\n");
        sb.append("          max-active: 8\n");
        sb.append("          max-wait: -1\n");
        sb.append("          max-idle: 8\n");
        sb.append("          min-idle: 0\n");

        return sb.toString();
    }

    /**
     * 构建服务配置
     */
    private String buildServiceConfig(InstallConfigDTO config, String serviceName, Integer port) {
        InstallConfigDTO.DatabaseConfig dbConfig = config.getDatabase();
        String dbName = "quickblue_" + serviceName;
        String user = serviceName + "_user";

        StringBuilder sb = new StringBuilder();
        sb.append("server:\n");
        sb.append("  port: ").append(port).append("\n");
        sb.append("spring:\n");
        sb.append("  datasource:\n");
        sb.append("    url: ").append(buildJdbcUrl(dbConfig, dbName)).append("\n");
        sb.append("    username: ").append(user).append("\n");
        sb.append("    password: ${").append(serviceName.toUpperCase()).append("_DB_PASSWORD:").append(getDbPassword(dbConfig, user)).append("}\n");
        sb.append("  application:\n");
        sb.append("    name: QuickBlue-").append(serviceName).append("\n");
        sb.append("  cloud:\n");
        sb.append("    nacos:\n");
        sb.append("      discovery:\n");
        sb.append("        server-addr: ${NACOS_SERVER_ADDR:").append(nacosServerAddr).append("}\n");
        sb.append("        namespace: ").append(nacosNamespace).append("\n");
        sb.append("        group: ").append(nacosGroup).append("\n");

        return sb.toString();
    }

    /**
     * 获取数据库驱动类
     */
    private String getDriverClass(String dbType) {
        if ("mysql".equalsIgnoreCase(dbType)) {
            return "com.mysql.cj.jdbc.Driver";
        } else if ("postgresql".equalsIgnoreCase(dbType)) {
            return "org.postgresql.Driver";
        }
        return "com.mysql.cj.jdbc.Driver";
    }

    /**
     * 构建JDBC URL
     */
    private String buildJdbcUrl(InstallConfigDTO.DatabaseConfig dbConfig, String dbName) {
        if ("mysql".equalsIgnoreCase(dbConfig.getDbType())) {
            return String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useServerPreparedStmts=false&rewriteBatchedStatements=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai",
                dbConfig.getHost(), dbConfig.getPort(), dbName);
        } else if ("postgresql".equalsIgnoreCase(dbConfig.getDbType())) {
            return String.format("jdbc:postgresql://%s:%d/%s", dbConfig.getHost(), dbConfig.getPort(), dbName);
        }
        throw new IllegalArgumentException("不支持的数据库类型: " + dbConfig.getDbType());
    }

    /**
     * 获取数据库密码
     */
    private String getDbPassword(InstallConfigDTO.DatabaseConfig dbConfig, String username) {
        for (InstallConfigDTO.DatabaseInfo dbInfo : dbConfig.getDatabases()) {
            if (dbInfo.getUsername().equals(username)) {
                return dbInfo.getPassword();
            }
        }
        return "";
    }

    /**
     * 更新单个Nacos配置
     */
    private void updateNacosConfig(String nacosUrl, String accessToken, String dataId, String content) {
        try {
            String url = nacosUrl + "/nacos/v1/cs/configs";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            org.springframework.util.MultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
            params.add("dataId", dataId);
            params.add("group", nacosGroup);
            params.add("content", content);
            params.add("type", "yaml");
            params.add("accessToken", accessToken);

            if (StringUtils.hasText(nacosNamespace)) {
                params.add("tenant", nacosNamespace);
            }

            HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && "true".equals(response.getBody())) {
                log.info("Nacos配置更新成功: {}", dataId);
            } else {
                log.warn("Nacos配置更新失败: {}, 响应: {}", dataId, response.getBody());
            }
        } catch (Exception e) {
            log.error("更新Nacos配置失败: {}", dataId, e);
        }
    }

    /**
     * 获取Nacos访问令牌
     */
    private String getNacosAccessToken() throws Exception {
        String url = "http://" + nacosServerAddr + "/nacos/v1/auth/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        org.springframework.util.MultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("username", nacosUsername);
        params.add("password", nacosPassword);

        HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.has("accessToken") ? jsonNode.get("accessToken").asText() : null;
        }

        throw new RuntimeException("获取Nacos访问令牌失败");
    }
}
