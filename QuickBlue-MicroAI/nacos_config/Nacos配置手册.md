# Nacos 配置导入说明

## 配置文件列表

### 公共配置 (common/)
#### MySQL 数据库配置
- `mysql-common.yaml` - MySQL 数据库通用配置（包含 Druid 连接池配置）

#### PostgreSQL 数据库配置
- `postgresql-common.yaml` - PostgreSQL 数据库通用配置（包含 HikariCP 连接池配置）

#### 其他公共配置
- `redis-common.yaml` - Redis 和 Redisson 配置
- `sa-token-common.yaml` - Sa-Token 权限认证配置
- `common-config.yaml` - 通用配置（Knife4j、文件上传、日志等）
- `level3-protect-common.yaml` - 三级防护配置

### 服务配置 (services/)
#### MySQL 版本
- `QuickBlue-gateway.yaml` - 网关服务配置（路由规则、CORS、Swagger 聚合）
- `QuickBlue-system.yaml` - 系统服务配置（MySQL版本）
- `QuickBlue-business.yaml` - 业务服务配置（MySQL版本）
- `QuickBlue-support.yaml` - 支持服务配置（MySQL版本）
- `QuickBlue-ai.yaml` - AI服务配置（MySQL版本）
- `QuickBlue-admin.yaml` - Admin监控服务配置

#### PostgreSQL 版本
- `QuickBlue-system-pg.yaml` - 系统服务配置（PostgreSQL版本）
- `QuickBlue-business-pg.yaml` - 业务服务配置（PostgreSQL版本）
- `QuickBlue-support-pg.yaml` - 支持服务配置（PostgreSQL版本）
- `QuickBlue-ai-pg.yaml` - AI服务配置（PostgreSQL版本）

## 导入步骤

### 1. 登录 Nacos 控制台
访问地址: `http://81.71.158.147:8848/nacos`
默认账号密码: `nacos/nacos`

### 2. 创建命名空间
- 点击左侧菜单「命名空间」
- 点击「新建命名空间」
- 命名空间ID: `QuickBlue-dev`
- 命名空间名称: `QuickBlue开发环境`
- 描述: `QuickBlue 项目开发环境配置`

### 3. 导入公共配置

切换到 `QuickBlue-dev` 命名空间，根据使用的数据库类型选择导入以下配置：

#### MySQL 数据库配置
##### mysql-common.yaml
- **Data ID**: `mysql-common.yaml`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `common/mysql-common.yaml` 文件内容

#### PostgreSQL 数据库配置
##### postgresql-common.yaml
- **Data ID**: `postgresql-common.yaml`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `common/postgresql-common.yaml` 文件内容

#### 其他公共配置
##### redis-common.yaml
- **Data ID**: `redis-common.yaml`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `common/redis-common.yaml` 文件内容

#### sa-token-common.yaml
- **Data ID**: `sa-token-common.yaml`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `common/sa-token-common.yaml` 文件内容

#### common-config.yaml
- **Data ID**: `common-config.yaml`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `common/common-config.yaml` 文件内容

### 4. 导入服务配置

根据使用的数据库类型选择导入以下服务配置：

#### MySQL 数据库版本配置
##### QuickBlue-gateway.yaml
- **Data ID**: `QuickBlue-gateway`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-gateway.yaml` 文件内容

##### QuickBlue-system.yaml
- **Data ID**: `QuickBlue-system`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-system.yaml` 文件内容

##### QuickBlue-business.yaml
- **Data ID**: `QuickBlue-business`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-business.yaml` 文件内容

##### QuickBlue-support.yaml
- **Data ID**: `QuickBlue-support`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-support.yaml` 文件内容

##### QuickBlue-ai.yaml
- **Data ID**: `QuickBlue-ai`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-ai.yaml` 文件内容

##### QuickBlue-admin.yaml
- **Data ID**: `QuickBlue-admin`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-admin.yaml` 文件内容

#### PostgreSQL 数据库版本配置
##### QuickBlue-gateway.yaml
- **Data ID**: `QuickBlue-gateway`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-gateway.yaml` 文件内容

##### QuickBlue-system-pg.yaml
- **Data ID**: `QuickBlue-system-pg`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-system-pg.yaml` 文件内容

##### QuickBlue-business-pg.yaml
- **Data ID**: `QuickBlue-business-pg`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-business-pg.yaml` 文件内容

##### QuickBlue-support-pg.yaml
- **Data ID**: `QuickBlue-support-pg`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-support-pg.yaml` 文件内容

##### QuickBlue-ai-pg.yaml
- **Data ID**: `QuickBlue-ai-pg`
- **Group**: `QuickBlue_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `services/QuickBlue-ai-pg.yaml` 文件内容

## 配置说明

### 命名空间
- **ID**: `QuickBlue-dev`
- **Group**: `QuickBlue_GROUP`
- **Nacos 地址**: `81.71.158.147:8848`

### 服务端口
- **QuickBlue-gateway**: 8080
- **QuickBlue-system**: 8081
- **QuickBlue-business**: 8082
- **QuickBlue-support**: 8083
- **QuickBlue-ai**: 8084
- **QuickBlue-admin**: 9090

### 数据库配置

#### MySQL 配置
- **主机**: 81.71.158.147
- **端口**: 33241
- **数据库**: quickblue_system, quickblue_business, quickblue_support, quickblue_ai
- **账号**: system_user, business_user, support_user, ai_user

#### PostgreSQL 配置
- **主机**: 81.71.158.147
- **端口**: 33241
- **数据库**: quickblue_system, quickblue_business, quickblue_support, quickblue_ai
- **账号**: system_user, business_user, support_user, ai_user

### Redis 配置
- **主机**: 81.71.158.147
- **端口**: 33242
- **密码**: Nq963369
- **数据库**: 1

## 数据库类型切换

### MySQL 切换到 PostgreSQL

如果需要从MySQL切换到PostgreSQL数据库，需要执行以下步骤：

#### 1. 修改各模块的 bootstrap.yaml

将以下文件中的 `mysql-common.yaml` 修改为 `postgresql-common.yaml`:
- `QuickBlue-system/src/main/resources/bootstrap.yaml`
- `QuickBlue-business/src/main/resources/bootstrap.yaml`
- `QuickBlue-support/src/main/resources/bootstrap.yaml`
- `QuickBlue-ai/src/main/resources/bootstrap.yaml`

示例修改:
```yaml
shared-configs:
  - data-id: postgresql-common.yaml  # 从 mysql-common.yaml 改为 postgresql-common.yaml
    group: QuickBlue_GROUP
    refresh: true
  - data-id: redis-common.yaml
    group: QuickBlue_GROUP
    refresh: true
  - data-id: sa-token-common.yaml
    group: QuickBlue_GROUP
    refresh: true
```

#### 2. 导入PostgreSQL版本的服务配置

参考"导入服务配置"部分，导入以 `-pg.yaml` 结尾的配置文件。

#### 3. 更新Nacos配置

- 删除旧的MySQL服务配置（QuickBlue-system、QuickBlue-business等）
- 导入新的PostgreSQL服务配置（QuickBlue-system-pg、QuickBlue-business-pg等）

#### 4. 验证配置

启动服务，确认能够正常连接PostgreSQL数据库。

### PostgreSQL 切换到 MySQL

操作步骤与上述相反，将 `postgresql-common.yaml` 改回 `mysql-common.yaml`，并导入MySQL版本的服务配置。

## 验证配置

导入完成后，可以通过以下方式验证配置是否正确：

1. 检查 Nacos 控制台配置列表，确认所有配置都已创建
2. 启动各个微服务，查看日志确认配置加载成功
3. 访问各服务的健康检查端点确认服务正常启动
4. 访问 Gateway 的 Swagger 聚合页面: `http://localhost:8080/doc.html`

## 注意事项

1. **敏感信息**: 生产环境建议使用 Nacos 加密功能或环境变量存储密码等敏感信息
2. **动态刷新**: 公共配置的 `refresh` 属性已在 bootstrap.yaml 中设置为 true，支持动态刷新
3. **配置优先级**: 本地配置 > Nacos 配置，确保本地配置不冲突
4. **版本控制**: 建议将 Nacos 配置内容纳入版本管理
5. **环境隔离**: 开发、测试、生产环境使用不同的命名空间隔离

## 配置更新

配置更新后，可以通过以下方式生效：
- 使用 Nacos 控制台的「发布」按钮手动刷新
- 调用 `/actuator/refresh` 端点动态刷新
- 重启对应的服务

## 故障排查

1. **服务无法启动**: 检查 Nacos 连接配置和 Data ID 是否正确
2. **配置未生效**: 确认配置所在的命名空间和 Group 是否正确
3. **连接超时**: 检查 Nacos 服务是否正常运行，网络是否可达
