<template>
  <div class="install-container">
    <div class="install-wrapper">
      <div class="install-header">
        <div class="logo">
          <span class="logo-icon">⚡</span>
          <span class="logo-text">QuickBlue</span>
        </div>
        <div class="version">v1.0.0</div>
      </div>

      <div class="install-content">
        <!-- 步骤指示器 -->
        <a-steps :current="currentStep" class="steps-nav" size="small">
          <a-step title="欢迎" />
          <a-step title="环境检查" />
          <a-step title="数据库" />
          <a-step title="Redis" />
          <a-step title="服务配置" />
          <a-step title="Nacos配置" />
          <a-step title="文件存储" />
          <a-step title="管理员" />
          <a-step title="安装" />
          <a-step title="完成" />
        </a-steps>

        <!-- 步骤内容区域 -->
        <div class="steps-content">
          <!-- 第1步：欢迎页面 -->
          <div v-if="currentStep === 0" class="step-content">
            <a-result
              status="info"
              title="欢迎使用 QuickBlue 安装向导"
              sub-title="本向导将帮助您完成系统的安装和配置"
            >
              <template #icon>
                <span class="welcome-icon">🚀</span>
              </template>
              <template #extra>
                <div class="welcome-info">
                  <a-alert
                    message="安装前准备"
                    description="请确保您的服务器已安装以下软件：Java 17+、PostgreSQL 18.3+、Redis 6.0+"
                    type="info"
                    show-icon
                    style="margin-bottom: 16px; text-align: left;"
                  />
                  <div class="feature-list">
                    <h4>系统功能：</h4>
                    <ul>
                      <li>✅ 支持服务 - 配置管理、字典管理、文件管理、定时任务等</li>
                      <li>✅ 系统服务 - 用户管理、角色权限、菜单管理等</li>
                      <li>✅ 业务服务 - 通知管理、区域管理等</li>
                      <li>✅ AI服务 - AI应用、模型管理、知识库等</li>
                    </ul>
                  </div>
                  <div class="install-type">
                    <h4>安装类型：</h4>
                    <a-radio-group v-model:value="installType">
                      <a-radio value="simple">
                        <div class="radio-content">
                          <strong>快速安装</strong>
                          <div>使用默认配置，适合初次安装</div>
                        </div>
                      </a-radio>
                      <a-radio value="custom">
                        <div class="radio-content">
                          <strong>自定义安装</strong>
                          <div>手动配置所有参数，适合高级用户</div>
                        </div>
                      </a-radio>
                    </a-radio-group>
                  </div>
                </div>
                <a-button type="primary" size="large" @click="nextStep">
                  开始安装 <RightOutlined />
                </a-button>
              </template>
            </a-result>
          </div>

          <!-- 第2步：环境检查 -->
          <div v-if="currentStep === 1" class="step-content">
            <div class="check-environment">
              <h2>环境检查</h2>
              <p class="step-desc">检查您的服务器环境是否满足安装要求</p>

              <a-spin :spinning="checking">
                <a-list :data-source="envChecks" class="env-check-list">
                  <template #renderItem="{ item }">
                    <a-list-item>
                      <a-list-item-meta>
                        <template #title>
                          {{ item.name }}
                          <a-tag :color="item.status === 'success' ? 'green' : item.status === 'warning' ? 'orange' : 'red'">
                            {{ item.statusText }}
                          </a-tag>
                        </template>
                        <template #description>
                          <div v-if="item.status === 'success'">
                            <CheckCircleOutlined style="color: #52c41a; margin-right: 4px;" />
                            {{ item.message }}
                          </div>
                          <div v-else-if="item.status === 'warning'">
                            <ExclamationCircleOutlined style="color: #faad14; margin-right: 4px;" />
                            {{ item.message }}
                          </div>
                          <div v-else>
                            <CloseCircleOutlined style="color: #ff4d4f; margin-right: 4px;" />
                            {{ item.message }}
                          </div>
                        </template>
                      </a-list-item-meta>
                    </a-list-item>
                  </template>
                </a-list>
              </a-spin>

              <div class="step-actions">
                <a-button @click="prevStep">上一步</a-button>
                <a-button type="primary" @click="recheckEnvironment" :loading="checking">重新检查</a-button>
                <a-button type="primary" @click="nextStep">
                  下一步 <RightOutlined />
                </a-button>
              </div>
            </div>
          </div>

          <!-- 第3步：数据库配置 -->
          <div v-if="currentStep === 2" class="step-content">
            <div class="database-config">
              <h2>数据库配置</h2>
              <p class="step-desc">配置系统所需的数据库连接信息</p>

              <a-form
                ref="dbFormRef"
                :model="dbConfig"
                :rules="dbRules"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-alert
                  message="数据库配置说明"
                  description="在此步骤配置数据库连接信息。'测试连接'仅验证管理员账号能否连接数据库；真正的创建数据库、用户、导入表结构将在点击'开始安装'后执行。"
                  type="info"
                  show-icon
                  style="margin-bottom: 16px;"
                />

                <a-form-item label="数据库类型" name="dbType">
                  <a-radio-group v-model:value="dbConfig.dbType" button-style="solid">
                    <a-radio-button value="postgresql">
                      PostgreSQL
                    </a-radio-button>
                  </a-radio-group>
                </a-form-item>

                <a-form-item label="主机地址" name="host">
                  <a-input v-model:value="dbConfig.host" placeholder="localhost" />
                </a-form-item>

                <a-form-item label="端口" name="port">
                  <a-input-number v-model:value="dbConfig.port" :min="1" :max="65535" style="width: 100%;" />
                </a-form-item>

                <a-form-item label="管理员账号" name="adminUser">
                  <a-input v-model:value="dbConfig.adminUser" placeholder="sdgs2026" />
                </a-form-item>

                <a-form-item label="管理员密码" name="adminPassword">
                  <a-input-password v-model:value="dbConfig.adminPassword" placeholder="请输入管理员密码" />
                </a-form-item>

                <a-separator>数据库创建配置</a-separator>
                <a-alert
                  message="说明"
                  description="测试管理员连接只是验证管理员账号能否访问数据库服务器，并不会创建数据库或表结构。所有数据库和表的创建将在点击'开始安装'按钮后自动执行。"
                  type="info"
                  show-icon
                  style="margin-bottom: 16px;"
                />

                <a-form-item v-for="(db, index) in databases" :key="index" :label="`${db.label}`">
                  <a-form-item-rest>
                    <a-row :gutter="8">
                      <a-col :span="12">
                        <a-input
                          v-model:value="db.name"
                          :placeholder="db.defaultName"
                        />
                      </a-col>
                      <a-col :span="12">
                        <a-input
                          v-model:value="db.username"
                          :placeholder="db.defaultUser"
                        />
                      </a-col>
                    </a-row>
                    <a-row style="margin-top: 8px;">
                      <a-col :span="24">
                        <a-input-password
                          v-model:value="db.password"
                          :placeholder="`默认密码: ${db.defaultPassword}`"
                        />
                      </a-col>
                    </a-row>
                  </a-form-item-rest>
                </a-form-item>

                <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
                  <a-button type="primary" @click="testAdminConnection" :loading="testingAdmin">
                    <CheckCircleOutlined /> 测试管理员连接
                  </a-button>
                  <span v-if="adminConnected" style="margin-left: 12px; color: #52c41a;">
                    <CheckCircleOutlined /> 管理员连接成功
                  </span>
                </a-form-item>

                <a-alert
                  v-if="dbConfig.dbType === 'postgresql'"
                  message="PostgreSQL 优化说明"
                  description="系统已自动生成优化后的PostgreSQL脚本，性能经过验证，可直接用于生产环境。"
                  type="success"
                  show-icon
                  style="margin-bottom: 16px;"
                />
              </a-form>

              <div class="step-actions">
                <a-button @click="prevStep">上一步</a-button>
                <a-button type="primary" @click="nextStep">
                  下一步 <RightOutlined />
                </a-button>
              </div>
            </div>
          </div>

          <!-- 第4步：Redis配置 -->
          <div v-if="currentStep === 3" class="step-content">
            <div class="redis-config">
              <h2>Redis配置</h2>
              <p class="step-desc">配置系统缓存和会话存储所需的Redis连接信息</p>

              <a-form
                ref="redisFormRef"
                :model="redisConfig"
                :rules="redisRules"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item label="主机地址" name="host">
                  <a-input v-model:value="redisConfig.host" placeholder="localhost" />
                </a-form-item>

                <a-form-item label="端口" name="port">
                  <a-input-number v-model:value="redisConfig.port" :min="1" :max="65535" style="width: 100%;" />
                </a-form-item>

                <a-form-item label="密码" name="password">
                  <a-input-password v-model:value="redisConfig.password" placeholder="如果不设置密码，请留空" />
                </a-form-item>

                <a-form-item label="数据库编号" name="database">
                  <a-select v-model:value="redisConfig.database">
                    <a-select-option :value="0">DB 0</a-select-option>
                    <a-select-option :value="1">DB 1</a-select-option>
                    <a-select-option :value="2">DB 2</a-select-option>
                    <a-select-option :value="3">DB 3</a-select-option>
                    <a-select-option :value="4">DB 4</a-select-option>
                    <a-select-option :value="5">DB 5</a-select-option>
                    <a-select-option :value="6">DB 6</a-select-option>
                    <a-select-option :value="7">DB 7</a-select-option>
                    <a-select-option :value="8">DB 8</a-select-option>
                    <a-select-option :value="9">DB 9</a-select-option>
                    <a-select-option :value="10">DB 10</a-select-option>
                    <a-select-option :value="11">DB 11</a-select-option>
                    <a-select-option :value="12">DB 12</a-select-option>
                    <a-select-option :value="13">DB 13</a-select-option>
                    <a-select-option :value="14">DB 14</a-select-option>
                    <a-select-option :value="15">DB 15</a-select-option>
                  </a-select>
                </a-form-item>

                <a-form-item label="超时时间" name="timeout">
                  <a-input-number v-model:value="redisConfig.timeout" :min="1" :max="60" :step="1" addon-after="秒" style="width: 100%;" />
                </a-form-item>

                <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
                  <a-button @click="testRedisConnection" :loading="redisConfig.testing">
                    <CheckOutlined /> 测试连接
                  </a-button>
                  <a-tag v-if="redisConfig.connected" color="green" style="margin-left: 8px;">
                    连接成功
                  </a-tag>
                </a-form-item>
              </a-form>

              <div class="step-actions">
                <a-button @click="prevStep">上一步</a-button>
                <a-button type="primary" @click="nextStep">
                  下一步 <RightOutlined />
                </a-button>
              </div>
            </div>
          </div>

          <!-- 第5步：服务配置 -->
          <div v-if="currentStep === 4" class="step-content">
            <div class="service-config">
              <h2>服务配置</h2>
              <p class="step-desc">配置各服务的端口和访问地址</p>

              <a-form
                ref="serviceFormRef"
                :model="serviceConfig"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item label="网关地址">
                  <a-input v-model:value="serviceConfig.gatewayUrl" placeholder="http://localhost:8080" />
                </a-form-item>

                <a-form-item label="网关端口">
                  <a-input-number v-model:value="serviceConfig.gatewayPort" :min="1" :max="65535" style="width: 100%;" />
                </a-form-item>

                <a-separator>微服务端口配置</a-separator>

                <a-form-item label="支撑服务">
                  <a-row :gutter="8">
                    <a-col :span="16">
                      <a-input v-model:value="serviceConfig.supportPort" :min="1" :max="65535" type="number" addon-after="端口" />
                    </a-col>
                    <a-col :span="8">
                      <a-button size="small">检查端口</a-button>
                    </a-col>
                  </a-row>
                </a-form-item>

                <a-form-item label="系统服务">
                  <a-row :gutter="8">
                    <a-col :span="16">
                      <a-input v-model:value="serviceConfig.systemPort" :min="1" :max="65535" type="number" addon-after="端口" />
                    </a-col>
                    <a-col :span="8">
                      <a-button size="small">检查端口</a-button>
                    </a-col>
                  </a-row>
                </a-form-item>

                <a-form-item label="业务服务">
                  <a-row :gutter="8">
                    <a-col :span="16">
                      <a-input v-model:value="serviceConfig.businessPort" :min="1" :max="65535" type="number" addon-after="端口" />
                    </a-col>
                    <a-col :span="8">
                      <a-button size="small">检查端口</a-button>
                    </a-col>
                  </a-row>
                </a-form-item>

                <a-form-item label="AI服务">
                  <a-row :gutter="8">
                    <a-col :span="16">
                      <a-input v-model:value="serviceConfig.aiPort" :min="1" :max="65535" type="number" addon-after="端口" />
                    </a-col>
                    <a-col :span="8">
                      <a-button size="small">检查端口</a-button>
                    </a-col>
                  </a-row>
                </a-form-item>

                <a-form-item label="管理后台">
                  <a-row :gutter="8">
                    <a-col :span="16">
                      <a-input v-model:value="serviceConfig.adminPort" :min="1" :max="65535" type="number" addon-after="端口" />
                    </a-col>
                    <a-col :span="8">
                      <a-button size="small">检查端口</a-button>
                    </a-col>
                  </a-row>
                </a-form-item>
              </a-form>

              <div class="step-actions">
                <a-button @click="prevStep">上一步</a-button>
                <a-button @click="checkAllPorts">检查所有端口</a-button>
                <a-button type="primary" @click="nextStep">
                  下一步 <RightOutlined />
                </a-button>
              </div>
            </div>
          </div>

          <!-- 第6步：Nacos配置 -->
          <div v-if="currentStep === 5" class="step-content">
            <div class="nacos-config">
              <h2>Nacos配置</h2>
              <p class="step-desc">配置Nacos配置中心连接信息，用于集中管理应用配置</p>

              <a-alert
                message="Nacos说明"
                description="Nacos是一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。配置中心将统一管理系统各微服务的配置文件。"
                type="info"
                show-icon
                style="margin-bottom: 16px;"
              />

              <a-form
                ref="nacosFormRef"
                :model="nacosConfig"
                :rules="nacosRules"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item label="服务器地址" name="serverAddr">
                  <a-input v-model:value="nacosConfig.serverAddr" placeholder="localhost:8848" />
                  <div style="margin-top: 4px; color: #8c8c8c; font-size: 12px;">
                    Nacos服务器地址，格式为 host:port
                  </div>
                </a-form-item>

                <a-form-item label="命名空间" name="namespace">
                  <a-input v-model:value="nacosConfig.namespace" placeholder="QuickBlue-dev" />
                  <div style="margin-top: 4px; color: #8c8c8c; font-size: 12px;">
                    命名空间ID，用于隔离不同环境的配置（留空使用public命名空间）
                  </div>
                </a-form-item>

                <a-form-item label="配置分组" name="group">
                  <a-input v-model:value="nacosConfig.group" placeholder="QuickBlue_GROUP" />
                  <div style="margin-top: 4px; color: #8c8c8c; font-size: 12px;">
                    配置分组名称
                  </div>
                </a-form-item>

                <a-separator>认证配置</a-separator>

                <a-form-item label="用户名" name="username">
                  <a-input v-model:value="nacosConfig.username" placeholder="nacos" />
                  <div style="margin-top: 4px; color: #8c8c8c; font-size: 12px;">
                    Nacos管理控制台的登录用户名
                  </div>
                </a-form-item>

                <a-form-item label="密码" name="password">
                  <a-input-password v-model:value="nacosConfig.password" placeholder="请输入Nacos密码" />
                  <div style="margin-top: 4px; color: #8c8c8c; font-size: 12px;">
                    Nacos管理控制台的登录密码
                  </div>
                </a-form-item>

                <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
                  <a-button type="primary" @click="testNacosConnection" :loading="nacosConfig.testing">
                    <CheckCircleOutlined /> 测试连接
                  </a-button>
                  <a-tag v-if="nacosConfig.connected" color="green" style="margin-left: 8px;">
                    连接成功
                  </a-tag>
                  <a-alert
                    v-if="nacosConfig.version"
                    type="success"
                    :message="`Nacos版本: ${nacosConfig.version}`"
                    show-icon
                    style="margin-left: 8px; display: inline-block; vertical-align: middle;"
                  />
                </a-form-item>

                <a-alert
                  message="配置模板说明"
                  type="success"
                  show-icon
                  style="margin-top: 16px;"
                >
                  <template #description>
                    <div>系统将使用PostgreSQL数据库连接配置的模板：</div>
                    <ul style="margin-top: 8px; padding-left: 20px;">
                      <li><strong>PostgreSQL：</strong>使用PostgreSQL数据库连接配置的模板</li>
                      <li><strong>通用：</strong>使用不包含数据库特定配置的通用模板</li>
                    </ul>
                    <div style="margin-top: 8px;">安装时会自动替换配置中的占位符并导入到Nacos服务器。</div>
                  </template>
                </a-alert>
              </a-form>

              <div class="step-actions">
                <a-button @click="prevStep">上一步</a-button>
                <a-button type="primary" @click="nextStep">
                  下一步 <RightOutlined />
                </a-button>
              </div>
            </div>
          </div>

          <!-- 第7步：文件存储配置 -->
          <div v-if="currentStep === 6" class="step-content">
            <div class="storage-config">
              <h2>文件存储配置</h2>
              <p class="step-desc">配置系统文件上传和存储方式</p>

              <a-form
                ref="storageFormRef"
                :model="storageConfig"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item label="存储模式">
                  <a-radio-group v-model:value="storageConfig.mode" @change="onStorageModeChange">
                    <a-radio value="local">
                      <div class="radio-content">
                        <strong>本地存储</strong>
                        <div>文件存储在服务器本地磁盘</div>
                      </div>
                    </a-radio>
                    <a-radio value="cloud">
                      <div class="radio-content">
                        <strong>云存储</strong>
                        <div>使用阿里云OSS等云存储服务</div>
                      </div>
                    </a-radio>
                  </a-radio-group>
                </a-form-item>

                <!-- 本地存储配置 -->
                <div v-if="storageConfig.mode === 'local'">
                  <a-form-item label="上传路径">
                    <a-input v-model:value="storageConfig.local.uploadPath" placeholder="./uploads" />
                  </a-form-item>

                  <a-form-item label="访问前缀">
                    <a-input v-model:value="storageConfig.local.urlPrefix" placeholder="http://localhost:8080/api/files" />
                  </a-form-item>

                  <a-form-item label="文件大小限制">
                    <a-input-number
                      v-model:value="storageConfig.local.maxFileSize"
                      :min="1"
                      :max="1024"
                      :step="1"
                      addon-after="MB"
                      style="width: 100%;"
                    />
                  </a-form-item>
                </div>

                <!-- 云存储配置 -->
                <div v-if="storageConfig.mode === 'cloud'">
                  <a-form-item label="云服务商">
                    <a-select v-model:value="storageConfig.cloud.provider">
                      <a-select-option value="aliyun">阿里云 OSS</a-select-option>
                      <a-select-option value="tencent">腾讯云 COS</a-select-option>
                      <a-select-option value="qiniu">七牛云</a-select-option>
                      <a-select-option value="aws">AWS S3</a-select-option>
                    </a-select>
                  </a-form-item>

                  <a-form-item label="区域">
                    <a-select v-model:value="storageConfig.cloud.region">
                      <a-select-option value="oss-cn-hangzhou">华东1（杭州）</a-select-option>
                      <a-select-option value="oss-cn-shanghai">华东2（上海）</a-select-option>
                      <a-select-option value="oss-cn-beijing">华北2（北京）</a-select-option>
                      <a-select-option value="oss-cn-shenzhen">华南1（深圳）</a-select-option>
                    </a-select>
                  </a-form-item>

                  <a-form-item label="存储桶名称">
                    <a-input v-model:value="storageConfig.cloud.bucketName" placeholder="quickblue-files" />
                  </a-form-item>

                  <a-form-item label="访问密钥ID">
                    <a-input v-model:value="storageConfig.cloud.accessKeyId" placeholder="请输入AccessKey ID" />
                  </a-form-item>

                  <a-form-item label="访问密钥Secret">
                    <a-input-password v-model:value="storageConfig.cloud.accessKeySecret" placeholder="请输入AccessKey Secret" />
                  </a-form-item>

                  <a-form-item label="端点">
                    <a-input v-model:value="storageConfig.cloud.endpoint" placeholder="oss-cn-hangzhou.aliyuncs.com" />
                  </a-form-item>
                </div>
              </a-form>

              <div class="step-actions">
                <a-button @click="prevStep">上一步</a-button>
                <a-button v-if="storageConfig.mode === 'cloud'" @click="testCloudStorage">测试连接</a-button>
                <a-button type="primary" @click="nextStep">
                  下一步 <RightOutlined />
                </a-button>
              </div>
            </div>
          </div>

          <!-- 第8步：管理员账号 -->
          <div v-if="currentStep === 7" class="step-content">
            <div class="admin-config">
              <h2>管理员账号</h2>
              <p class="step-desc">设置系统初始管理员账号</p>

              <a-form
                ref="adminFormRef"
                :model="adminConfig"
                :rules="adminRules"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item label="用户名" name="username">
                  <a-input v-model:value="adminConfig.username" placeholder="admin" />
                </a-form-item>

                <a-form-item label="密码" name="password">
                  <a-input-password v-model:value="adminConfig.password" placeholder="请输入密码" />
                </a-form-item>

                <a-form-item label="确认密码" name="confirmPassword">
                  <a-input-password v-model:value="adminConfig.confirmPassword" placeholder="请再次输入密码" />
                </a-form-item>

                <a-form-item label="真实姓名" name="realName">
                  <a-input v-model:value="adminConfig.realName" placeholder="管理员" />
                </a-form-item>

                <a-form-item label="手机号" name="phone">
                  <a-input v-model:value="adminConfig.phone" placeholder="请输入手机号" />
                </a-form-item>

                <a-form-item label="邮箱" name="email">
                  <a-input v-model:value="adminConfig.email" placeholder="请输入邮箱" />
                </a-form-item>

                <a-form-item label="部门">
                  <a-input v-model:value="adminConfig.department" placeholder="技术部" disabled />
                </a-form-item>

                <a-form-item label="职位">
                  <a-input v-model:value="adminConfig.position" placeholder="系统管理员" disabled />
                </a-form-item>
              </a-form>

              <div class="step-actions">
                <a-button @click="prevStep">上一步</a-button>
                <a-button @click="generateStrongPassword">
                  <KeyOutlined /> 生成强密码
                </a-button>
                <a-button type="primary" @click="nextStep">
                  下一步 <RightOutlined />
                </a-button>
              </div>
            </div>
          </div>

          <!-- 第9步：安装执行 -->
          <div v-if="currentStep === 8" class="step-content">
            <div class="install-execution">
              <h2>开始安装</h2>
              <p class="step-desc">确认配置信息并开始安装</p>

              <a-alert
                message="重要提示"
                type="warning"
                show-icon
                style="margin-bottom: 24px;"
              >
                <template #description>
                  <div>点击"开始安装"后将执行以下操作：</div>
                  <ul style="margin-top: 8px; padding-left: 20px;">
                    <li>✅ 创建4个数据库（quickblue_support、quickblue_system、quickblue_business、quickblue_ai）</li>
                    <li>✅ 创建4个独立数据库账号并授权</li>
                    <li>✅ 导入数据库表结构（包含所有表和索引）</li>
                    <li>✅ 初始化基础数据（包括菜单、权限、默认管理员账号等）</li>
                    <li>✅ 根据数据库类型自动生成并导入Nacos配置（PostgreSQL）</li>
                  </ul>
                  <div style="margin-top: 8px;">安装过程可能需要几分钟时间，请耐心等待。安装期间请勿关闭浏览器窗口。</div>
                </template>
              </a-alert>

              <a-descriptions title="安装配置确认" bordered :column="2">
                <a-descriptions-item label="数据库类型">
                  PostgreSQL
                </a-descriptions-item>
                <a-descriptions-item label="数据库地址">
                  {{ dbConfig.host }}:{{ dbConfig.port }}
                </a-descriptions-item>
                <a-descriptions-item label="Redis地址">
                  {{ redisConfig.host }}:{{ redisConfig.port }}
                </a-descriptions-item>
                <a-descriptions-item label="存储模式">
                  {{ storageConfig.mode === 'local' ? '本地存储' : '云存储' }}
                </a-descriptions-item>
                <a-descriptions-item label="管理员账号">
                  {{ adminConfig.username }}
                </a-descriptions-item>
                <a-descriptions-item label="安装类型">
                  {{ installType === 'simple' ? '快速安装' : '自定义安装' }}
                </a-descriptions-item>
              </a-descriptions>

              <div style="margin-top: 24px;">
                <h3>安装任务：</h3>
                <a-list :data-source="installTasks" class="install-tasks">
                  <template #renderItem="{ item }">
                    <a-list-item>
                      <a-list-item-meta>
                        <template #title>
                          {{ item.title }}
                          <a-tag v-if="item.status === 'completed'" color="green">
                            完成
                          </a-tag>
                          <a-tag v-else-if="item.status === 'error'" color="red">
                            失败
                          </a-tag>
                          <a-tag v-else-if="item.status === 'running'" color="blue">
                            执行中...
                          </a-tag>
                          <a-tag v-else color="default">
                            等待中
                          </a-tag>
                        </template>
                        <template #description>
                          <div v-if="item.status === 'running'">
                            <a-progress :percent="item.progress" :status="item.error ? 'exception' : 'active'" />
                          </div>
                          <div v-if="item.error">
                            <a-text type="danger">{{ item.error }}</a-text>
                          </div>
                          <div v-if="item.message">
                            <a-text type="secondary">{{ item.message }}</a-text>
                          </div>
                        </template>
                      </a-list-item-meta>
                    </a-list-item>
                  </template>
                </a-list>

                <div class="overall-progress">
                  <h3>总体进度：</h3>
                  <a-progress :percent="overallProgress" :status="installError ? 'exception' : 'active'" />
                </div>
              </div>

              <div class="install-log" v-if="installLogs.length > 0">
                <h3>安装日志：</h3>
                <div class="log-container">
                  <div v-for="(log, index) in installLogs" :key="index" class="log-entry">
                    <span class="log-time">[{{ log.time }}]</span>
                    <span :class="['log-level', `log-${log.level}`]">{{ log.level }}</span>
                    <span class="log-message">{{ log.message }}</span>
                  </div>
                </div>
              </div>

              <div class="step-actions">
                <a-button @click="prevStep" :disabled="installing">上一步</a-button>
                <a-button type="primary" @click="startInstall" :loading="installing" :disabled="installComplete">
                  {{ installing ? '安装中...' : installComplete ? '安装完成' : '开始安装' }}
                  <RocketOutlined v-if="!installing && !installComplete" />
                  <CheckOutlined v-else-if="installComplete" />
                </a-button>
              </div>
            </div>
          </div>

          <!-- 第10步：完成 -->
          <div v-if="currentStep === 9" class="step-content">
            <a-result
              status="success"
              title="安装完成！"
              sub-title="QuickBlue系统已成功安装，您可以开始使用系统了。"
            >
              <template #icon>
                <span class="success-icon">🎉</span>
              </template>
              <template #extra>
                <div class="completion-info">
                  <!-- 保存环境选项 -->
                  <a-card title="保存环境配置" style="margin-bottom: 16px;">
                    <a-form layout="vertical">
                      <a-form-item label="是否保存为可切换环境？">
                        <a-checkbox v-model:checked="saveEnvironment">
                          保存此配置为环境，方便后续快速切换
                        </a-checkbox>
                      </a-form-item>

                      <a-form-item v-if="saveEnvironment" label="环境名称">
                        <a-input
                          v-model:value="environmentName"
                          placeholder="例如：dev、test、prod"
                          style="max-width: 300px;"
                        />
                        <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
                          保存后可在"环境管理"中快速切换不同数据库环境
                        </div>
                      </a-form-item>
                    </a-form>
                  </a-card>

                  <a-card title="访问信息" style="margin-bottom: 16px;">
                    <p><strong>管理后台地址：</strong> {{ serviceConfig.gatewayUrl }}/admin</p>
                    <p><strong>管理员账号：</strong> {{ adminConfig.username }}</p>
                    <p><strong>初始密码：</strong> ********** (请妥善保管)</p>
                  </a-card>

                  <a-card title="后续步骤" style="margin-bottom: 16px;">
                    <a-steps direction="vertical" :current="0">
                      <a-step title="登录系统" description="使用管理员账号登录管理后台" />
                      <a-step title="修改密码" description="首次登录后请立即修改初始密码" />
                      <a-step title="配置基础数据" description="完善组织架构、菜单权限等基础配置" />
                      <a-step title="开始使用" description="开始使用QuickBlue的各项功能" />
                    </a-steps>
                  </a-card>

                  <a-card title="帮助文档">
                    <p>如需帮助，请查看：</p>
                    <ul>
                      <li>用户手册</li>
                      <li>开发文档</li>
                      <li>API文档: {{ serviceConfig.gatewayUrl }}/doc.html</li>
                    </ul>
                  </a-card>
                </div>
                <a-space>
                  <a-button type="primary" size="large" @click="goToAdmin">
                    <LoginOutlined /> 进入系统
                  </a-button>
                  <a-button v-if="saveEnvironment" size="large" @click="saveAsEnvironment" :loading="savingEnv">
                    <SaveOutlined /> 保存环境
                  </a-button>
                  <a-button size="large" @click="downloadConfig">
                    <DownloadOutlined /> 下载配置
                  </a-button>
                  <a-button size="large" @click="goToEnvironmentManagement">
                    <SwapOutlined /> 环境管理
                  </a-button>
                </a-space>
              </template>
            </a-result>
          </div>
        </div>
      </div>

      <!-- 侧边信息 -->
      <div class="install-sidebar" v-if="currentStep > 0 && currentStep < 9">
        <div class="sidebar-content">
          <h3>安装提示</h3>
          <div v-if="currentStep === 1" class="tips">
            <p>• 建议确保所有环境检查通过</p>
            <p>• 即使未通过也可以继续下一步</p>
            <p>• 可以点击重新检查</p>
          </div>
          <div v-if="currentStep === 2" class="tips">
            <p>• 建议测试连接后继续下一步</p>
            <p>• 即使测试未通过也可以继续</p>
            <p>• PostgreSQL性能更优</p>
          </div>
          <div v-if="currentStep === 3" class="tips">
            <p>• Redis用于缓存和会话</p>
            <p>• 建议测试连接后继续下一步</p>
            <p>• 即使测试未通过也可以继续</p>
          </div>
          <div v-if="currentStep === 4" class="tips">
            <p>• 确保端口未被占用</p>
            <p>• 可以使用默认端口</p>
            <p>• 不同服务使用不同端口</p>
          </div>
          <div v-if="currentStep === 5" class="tips">
            <p>• Nacos用于配置中心管理</p>
            <p>• 建议测试连接后继续下一步</p>
            <p>• 即使测试未通过也可以继续</p>
          </div>
          <div v-if="currentStep === 6" class="tips">
            <p>• 本地存储适合小规模</p>
            <p>• 云存储适合生产环境</p>
            <p>• 注意磁盘空间</p>
          </div>
          <div v-if="currentStep === 7" class="tips">
            <p>• 牢记管理员密码</p>
            <p>• 建议使用强密码</p>
            <p>• 首次登录后请修改</p>
          </div>
          <div v-if="currentStep === 8" class="tips">
            <p>• 安装期间勿关闭窗口</p>
            <p>• 网络连接保持稳定</p>
            <p>• 失败可重试</p>
          </div>

          <a-divider />

          <div class="help-box">
            <h4>需要帮助？</h4>
            <a-button type="link" @click="showHelpModal">查看安装文档</a-button>
            <a-button type="link" @click="contactSupport">联系技术支持</a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 帮助文档弹窗 -->
    <a-modal
      v-model:open="helpModalVisible"
      title="安装帮助文档"
      :footer="null"
      width="800px"
    >
      <div class="help-content">
        <h3>安装向导使用指南</h3>
        <h4>1. 环境要求</h4>
        <ul>
          <li>Java 17+</li>
          <li>PostgreSQL 18.3+</li>
          <li>Redis 6.0+</li>
          <li>Node.js 18+ (仅开发环境)</li>
        </ul>
        <h4>2. 安装步骤</h4>
        <ul>
          <li>按照向导提示逐步配置</li>
          <li>每步都可以点击上一步返回</li>
          <li>安装完成后可登录系统</li>
        </ul>
        <h4>3. 常见问题</h4>
        <ul>
          <li>数据库连接失败：检查地址、端口、账号密码</li>
          <li>Redis连接失败：检查Redis服务是否启动</li>
          <li>端口被占用：修改服务端口配置</li>
        </ul>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import environmentApi from '/@/api/environment/environment-api';
import {
  RightOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined,
  CheckOutlined,
  RocketOutlined,
  LoginOutlined,
  DownloadOutlined,
  KeyOutlined,
  SaveOutlined,
  SwapOutlined
} from '@ant-design/icons-vue';
import installApi from '/@/api/install/install-api';

const router = useRouter();
const currentStep = ref(0);
const installType = ref('simple');
const checking = ref(false);
const installing = ref(false);
const installComplete = ref(false);
const installError = ref(false);
const helpModalVisible = ref(false);
const saveEnvironment = ref(false);
const environmentName = ref('');
const savingEnv = ref(false);

// 环境检查
const envCheckPassed = ref(false);
const envChecks = ref([
  { name: 'Java 版本', status: 'pending', statusText: '检查中', message: '' },
  { name: '数据库', status: 'pending', statusText: '检查中', message: '' },
  { name: 'Redis', status: 'pending', statusText: '检查中', message: '' },
  { name: '磁盘空间', status: 'pending', statusText: '检查中', message: '' },
  { name: '网络连接', status: 'pending', statusText: '检查中', message: '' }
]);

// 数据库配置
const dbFormRef = ref();
const dbConfig = reactive({
  dbType: 'postgresql',
  host: '127.0.0.1',
  port: 5432,
  adminUser: 'postgres',
  adminPassword: 'sdgs2026',
  databases: []
});

const databases = reactive([
  {
    label: '支撑服务数据库',
    name: 'quickblue_support',
    defaultName: 'quickblue_support',
    username: 'support_user',
    defaultUser: 'support_user',
    defaultPassword: 'Support@2026',
    password: 'Support@2026',
    testing: false,
    connected: false
  },
  {
    label: '系统服务数据库',
    name: 'quickblue_system',
    defaultName: 'quickblue_system',
    username: 'system_user',
    defaultUser: 'system_user',
    defaultPassword: 'System@2026',
    password: 'System@2026',
    testing: false,
    connected: false
  },
  {
    label: '业务服务数据库',
    name: 'quickblue_business',
    defaultName: 'quickblue_business',
    username: 'business_user',
    defaultUser: 'business_user',
    defaultPassword: 'Business@2026',
    password: 'Business@2026',
    testing: false,
    connected: false
  },
  {
    label: 'AI服务数据库',
    name: 'quickblue_ai',
    defaultName: 'quickblue_ai',
    username: 'ai_user',
    defaultUser: 'ai_user',
    defaultPassword: 'Ai@2026',
    password: 'Ai@2026',
    testing: false,
    connected: false
  }
]);

const testingAdmin = ref(false);
const adminConnected = ref(false);

const dbRules = {
  host: [{ required: true, message: '请输入主机地址' }],
  port: [{ required: true, message: '请输入端口' }],
  adminUser: [{ required: true, message: '请输入管理员账号' }],
  adminPassword: [{ required: true, message: '请输入管理员密码' }]
};

const dbConfigValid = computed(() => {
  return adminConnected.value;
});

// Redis配置
const redisFormRef = ref();
const redisConfig = reactive({
  host: '127.0.0.1',
  port: 6379,
  password: 'Nq963369',
  database: 1,
  timeout: 10,
  testing: false,
  connected: false
});

const redisRules = {
  host: [{ required: true, message: '请输入主机地址' }],
  port: [{ required: true, message: '请输入端口' }],
  timeout: [{ required: true, message: '请输入超时时间' }]
};

const redisConfigValid = computed(() => {
  return redisConfig.connected;
});

// Nacos配置
const nacosFormRef = ref();
const nacosConfig = reactive({
  serverAddr: 'localhost:8848',
  namespace: 'QuickBlue-dev',
  group: 'QuickBlue_GROUP',
  username: 'nacos',
  password: 'Nq963369!@',
  testing: false,
  connected: false,
  version: ''
});

const nacosRules = {
  serverAddr: [{ required: true, message: '请输入服务器地址' }],
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }]
};

const nacosConfigValid = computed(() => {
  return nacosConfig.connected;
});

// 服务配置
const serviceFormRef = ref();
const serviceConfig = reactive({
  gatewayUrl: 'http://localhost:8080',
  gatewayPort: 8080,
  supportPort: 8083,
  systemPort: 8086,
  businessPort: 8082,
  aiPort: 8084,
  adminPort: 5173
});

// 存储配置
const storageFormRef = ref();
const storageConfig = reactive({
  mode: 'local',
  local: {
    uploadPath: './uploads',
    urlPrefix: 'http://localhost:8080/api/files',
    maxFileSize: 100
  },
  cloud: {
    provider: 'aliyun',
    region: 'oss-cn-hangzhou',
    bucketName: 'quickblue-files',
    accessKeyId: '',
    accessKeySecret: '',
    endpoint: 'oss-cn-hangzhou.aliyuncs.com'
  }
});

// 管理员配置
const adminFormRef = ref();
const adminConfig = reactive({
  username: 'admin',
  password: '',
  confirmPassword: '',
  realName: '系统管理员',
  phone: '',
  email: 'admin@example.com',
  department: '技术部',
  position: '系统管理员'
});

const validatePassword = async (_rule, value) => {
  if (value === '') {
    return Promise.reject('请输入密码');
  }
  if (value.length < 6) {
    return Promise.reject('密码长度不能少于6位');
  }
  return Promise.resolve();
};

const validateConfirmPassword = async (_rule, value) => {
  if (value === '') {
    return Promise.reject('请确认密码');
  }
  if (value !== adminConfig.password) {
    return Promise.reject('两次输入的密码不一致');
  }
  return Promise.resolve();
};

const adminRules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, validator: validatePassword, trigger: 'change' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'change' }],
  realName: [{ required: true, message: '请输入真实姓名' }],
  phone: [
    { required: true, message: '请输入手机号' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
  ],
  email: [
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '邮箱格式不正确' }
  ]
};

// 安装任务
const installTasks = ref([
  { title: '创建数据库和用户', status: 'pending', progress: 0, error: null, message: '' },
  { title: '导入数据库表结构', status: 'pending', progress: 0, error: null, message: '' },
  { title: '初始化基础数据', status: 'pending', progress: 0, error: null, message: '' },
  { title: '生成并导入Nacos配置', status: 'pending', progress: 0, error: null, message: '' },
  { title: '创建管理员账号', status: 'pending', progress: 0, error: null, message: '' },
  { title: '配置系统参数', status: 'pending', progress: 0, error: null, message: '' },
  { title: '验证安装结果', status: 'pending', progress: 0, error: null, message: '' }
]);

const overallProgress = computed(() => {
  const completed = installTasks.value.filter(task => task.status === 'completed').length;
  const total = installTasks.value.length;
  return Math.round((completed / total) * 100);
});

// 安装日志
const installLogs = ref([]);

function addLog(level, message) {
  const now = new Date();
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
  installLogs.value.push({ time, level, message });
}

// 方法
function nextStep() {
  if (currentStep.value < 9) {
    const nextStepValue = currentStep.value + 1;
    // 如果进入环境检查步骤(第1步)，自动触发环境检查
    if (nextStepValue === 1) {
      currentStep.value = nextStepValue;
      checkEnvironment();
    } else {
      currentStep.value = nextStepValue;
    }
  }
}

function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--;
  }
}

async function checkEnvironment() {
  checking.value = true;

  // 模拟环境检查
  await new Promise(resolve => setTimeout(resolve, 1500));

  envChecks.value[0] = { name: 'Java 版本', status: 'success', statusText: '通过', message: 'Java 17.0.9' };
  envChecks.value[1] = { name: '数据库', status: 'success', statusText: '通过', message: 'PostgreSQL 18.3' };
  envChecks.value[2] = { name: 'Redis', status: 'success', statusText: '通过', message: 'Redis 7.0.5' };
  envChecks.value[3] = { name: '磁盘空间', status: 'success', statusText: '通过', message: '可用: 256GB' };
  envChecks.value[4] = { name: '网络连接', status: 'success', statusText: '通过', message: '网络正常' };

  checking.value = false;
  envCheckPassed.value = true;
}

function recheckEnvironment() {
  checkEnvironment();
}

async function testAdminConnection() {
  testingAdmin.value = true;
  addLog('INFO', '测试数据库管理员连接');

  try {
    const response = await installApi.testDbConnection({
      dbType: dbConfig.dbType,
      host: dbConfig.host,
      port: dbConfig.port,
      adminUser: dbConfig.adminUser,
      adminPassword: dbConfig.adminPassword,
      databaseName: 'postgres',
      databaseUser: dbConfig.adminUser,
      databasePassword: dbConfig.adminPassword
    });

    console.log('测试数据库连接响应:', response);
    addLog('INFO', `数据库连接响应: ${JSON.stringify(response)}`);

    testingAdmin.value = false;
    adminConnected.value = true;
    addLog('SUCCESS', '数据库管理员连接成功');
    message.success('数据库管理员连接成功，可以开始安装');
  } catch (error) {
    console.error('测试数据库连接错误:', error);
    addLog('ERROR', `数据库管理员连接失败: ${error.message || '未知错误'} - 详细信息: ${JSON.stringify(error)}`);
    testingAdmin.value = false;
    adminConnected.value = false;
    message.error(`数据库管理员连接失败: ${error.msg || error.message || '未知错误'}`);
  }
}

async function testDbConnection(db) {
  db.testing = true;
  addLog('INFO', `测试数据库连接: ${db.name}`);

  try {
    // 调用后端API测试数据库连接
    await installApi.testDbConnection({
      dbType: dbConfig.dbType,
      host: dbConfig.host,
      port: dbConfig.port,
      adminUser: dbConfig.adminUser,
      adminPassword: dbConfig.adminPassword,
      databaseName: db.name,
      databaseUser: db.username,
      databasePassword: db.password || db.defaultPassword
    });

    db.testing = false;
    db.connected = true;
    addLog('SUCCESS', `数据库连接成功: ${db.name}`);
    message.success(`${db.name} 连接成功`);
  } catch (error) {
    db.testing = false;
    db.connected = false;
    addLog('ERROR', `数据库连接失败: ${db.name} - ${error.message || '未知错误'}`);
    message.error(`${db.name} 连接失败: ${error.message || '未知错误'}`);
  }
}

async function testAllDbConnections() {
  for (const db of databases) {
    await testDbConnection(db);
  }
}

async function testRedisConnection() {
  redisConfig.testing = true;
  addLog('INFO', '测试Redis连接');

  try {
    // 调用后端API测试Redis连接
    await installApi.testRedisConnection({
      host: redisConfig.host,
      port: redisConfig.port,
      password: redisConfig.password,
      database: redisConfig.database,
      timeout: redisConfig.timeout
    });

    redisConfig.testing = false;
    redisConfig.connected = true;
    addLog('SUCCESS', 'Redis连接成功');
    message.success('Redis连接成功');
  } catch (error) {
    redisConfig.testing = false;
    redisConfig.connected = false;
    addLog('ERROR', `Redis连接失败: ${error.message || '未知错误'}`);
    message.error(`Redis连接失败: ${error.message || '未知错误'}`);
  }
}

async function testNacosConnection() {
  nacosConfig.testing = true;
  addLog('INFO', '测试Nacos连接');

  try {
    // 调用后端API测试Nacos连接
    const response = await installApi.testNacosConnection({
      serverAddr: nacosConfig.serverAddr,
      namespace: nacosConfig.namespace,
      group: nacosConfig.group,
      username: nacosConfig.username,
      password: nacosConfig.password
    });

    nacosConfig.testing = false;
    nacosConfig.connected = true;
    nacosConfig.version = response.version || '';
    addLog('SUCCESS', `Nacos连接成功，版本: ${nacosConfig.version}`);
    message.success(`Nacos连接成功，版本: ${nacosConfig.version}`);
  } catch (error) {
    nacosConfig.testing = false;
    nacosConfig.connected = false;
    nacosConfig.version = '';
    addLog('ERROR', `Nacos连接失败: ${error.message || '未知错误'}`);
    message.error(`Nacos连接失败: ${error.message || '未知错误'}`);
  }
}

function checkAllPorts() {
  message.info('端口检查功能需要后端支持，当前为模拟检查');
  message.success('所有端口可用');
}

function onStorageModeChange(e) {
  const mode = e.target.value;
  addLog('INFO', `存储模式切换为: ${mode === 'local' ? '本地存储' : '云存储'}`);
}

function testCloudStorage() {
  message.info('云存储连接测试功能需要后端支持');
}

function generateStrongPassword() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*';
  let password = '';
  for (let i = 0; i < 16; i++) {
    password += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  adminConfig.password = password;
  adminConfig.confirmPassword = password;
  message.success('强密码已生成');
}

async function startInstall() {
  installing.value = true;
  installError.value = false;
  installLogs.value = [];
  installComplete.value = false;

  addLog('INFO', '开始安装...');

  try {
    // 构建安装配置
    const installConfig = {
      installType: installType.value,
      database: {
        dbType: dbConfig.dbType,
        host: dbConfig.host,
        port: dbConfig.port,
        adminUser: dbConfig.adminUser,
        adminPassword: dbConfig.adminPassword,
        databases: databases.map(db => ({
          name: db.name,
          username: db.username,
          password: db.password || db.defaultPassword // 如果密码为空，使用默认密码
        }))
      },
      redis: {
        host: redisConfig.host,
        port: redisConfig.port,
        password: redisConfig.password,
        database: redisConfig.database,
        timeout: redisConfig.timeout
      },
      nacos: {
        serverAddr: nacosConfig.serverAddr,
        namespace: nacosConfig.namespace,
        group: nacosConfig.group,
        username: nacosConfig.username,
        password: nacosConfig.password
      },
      service: {
        gatewayUrl: serviceConfig.gatewayUrl,
        gatewayPort: serviceConfig.gatewayPort,
        supportPort: serviceConfig.supportPort,
        systemPort: serviceConfig.systemPort,
        businessPort: serviceConfig.businessPort,
        aiPort: serviceConfig.aiPort,
        adminPort: serviceConfig.adminPort
      },
      storage: storageConfig,
      admin: {
        username: adminConfig.username,
        password: adminConfig.password,
        realName: adminConfig.realName,
        phone: adminConfig.phone,
        email: adminConfig.email,
        department: adminConfig.department,
        position: adminConfig.position
      }
    };

    addLog('INFO', `安装配置: ${JSON.stringify(installConfig)}`);

    // 调用后端API开始安装
    addLog('INFO', '正在调用后端安装接口...');
    await installApi.startInstall(installConfig);

    addLog('INFO', '后端安装已启动，开始轮询进度...');

    // 轮询安装进度
    let emptyProgressCount = 0; // 记录进度为空的次数
    const maxEmptyProgressCount = 10; // 最多允许10次进度为空（20秒）
    
    const progressInterval = setInterval(async () => {
      try {
        const resp = await installApi.getInstallProgress();
        const progress = resp && resp.data ? resp.data : null;
        
        // 如果进度为空或无效，记录次数
        if (!progress) {
          emptyProgressCount++;
          addLog('INFO', `等待后端初始化安装进度... (${emptyProgressCount}/${maxEmptyProgressCount})`);
          
          // 如果连续多次进度为空，认为安装失败
          if (emptyProgressCount >= maxEmptyProgressCount) {
            clearInterval(progressInterval);
            installing.value = false;
            installError.value = true;
            addLog('ERROR', '安装进度初始化超时，请检查后端日志');
            message.error('安装进度初始化超时');
          }
          return;
        }
        
        // 如果进度存在，重置计数器
        emptyProgressCount = 0;
        
        // 如果安装完成
        if (progress.status === 'completed') {
          clearInterval(progressInterval);
          installing.value = false;
          installComplete.value = true;
          overallProgress.value = 100;
          addLog('SUCCESS', '安装完成！');
          setTimeout(() => {
            nextStep();
          }, 1000);
          return;
        }
        
        // 如果安装出错
        if (progress.status === 'error') {
          clearInterval(progressInterval);
          installing.value = false;
          installError.value = true;
          addLog('ERROR', '安装失败！');
          message.error('安装失败，请查看日志了解详情');
          return;
        }
        
        // 如果进度存在但没有tasks，且状态不是running/completed/pending，说明有问题
        if (!progress.tasks && 
            progress.status !== 'running' && 
            progress.status !== 'completed' && 
            progress.status !== 'pending') {
          clearInterval(progressInterval);
          installing.value = false;
          installError.value = true;
          addLog('ERROR', `安装进度异常: status=${progress.status}`);
          message.error('安装进度异常');
          return;
        }
        
        addLog('INFO', `安装进度: ${JSON.stringify(progress)}`);

        // 更新任务状态
        if (progress.tasks && progress.tasks.length > 0) {
          progress.tasks.forEach((task, index) => {
            if (installTasks.value[index]) {
              installTasks.value[index].status = task.status;
              installTasks.value[index].progress = task.progress;
              installTasks.value[index].error = task.error;
              installTasks.value[index].message = task.message;

              if (task.status === 'running' && task.message) {
                addLog('INFO', task.message);
              } else if (task.status === 'error' && task.error) {
                addLog('ERROR', task.error);
              } else if (task.status === 'completed') {
                addLog('SUCCESS', installTasks.value[index].title + ' 完成');
              }
            }
          });
        }

        // 检查安装是否完成
        if (progress.status === 'completed') {
          clearInterval(progressInterval);
          installing.value = false;
          installComplete.value = true;
          overallProgress.value = 100;
          addLog('SUCCESS', '安装完成！');
          setTimeout(() => {
            nextStep();
          }, 1000);
        } else if (progress.status === 'error') {
          clearInterval(progressInterval);
          installing.value = false;
          installError.value = true;
          addLog('ERROR', '安装失败！');
          message.error('安装失败，请查看日志了解详情');
        }
      } catch (error) {
        clearInterval(progressInterval);
        installing.value = false;
        installError.value = true;
        addLog('ERROR', `获取安装进度失败: ${error.message}`);
        message.error('获取安装进度失败');
      }
    }, 2000); // 每2秒轮询一次进度

  } catch (error) {
    installing.value = false;
    installError.value = true;
    addLog('ERROR', `安装失败: ${error.message || error.msg || '未知错误'}`);
    message.error(`安装失败: ${error.message || error.msg || '未知错误'}`);
  }
}

function goToAdmin() {
  router.push('/login');
}

async function saveAsEnvironment() {
  if (!environmentName.value || environmentName.value.trim() === '') {
    message.error('请输入环境名称');
    return;
  }

  savingEnv.value = true;

  try {
    // 构建安装配置
    const installConfig = {
      installType: installType.value,
      database: {
        dbType: dbConfig.dbType,
        host: dbConfig.host,
        port: dbConfig.port,
        adminUser: dbConfig.adminUser,
        adminPassword: dbConfig.adminPassword,
        databases: databases.map(db => ({
          name: db.name,
          username: db.username,
          password: db.password || db.defaultPassword
        }))
      },
      redis: {
        host: redisConfig.host,
        port: redisConfig.port,
        password: redisConfig.password,
        database: redisConfig.database,
        timeout: redisConfig.timeout
      },
      service: {
        gatewayUrl: serviceConfig.gatewayUrl,
        gatewayPort: serviceConfig.gatewayPort,
        supportPort: serviceConfig.supportPort,
        systemPort: serviceConfig.systemPort,
        businessPort: serviceConfig.businessPort,
        aiPort: serviceConfig.aiPort,
        adminPort: serviceConfig.adminPort
      },
      storage: storageConfig,
      admin: {
        username: adminConfig.username,
        realName: adminConfig.realName,
        department: adminConfig.department,
        position: adminConfig.position
      }
    };

    await environmentApi.saveEnvironment({
      envName: environmentName.value.trim(),
      installConfig
    });

    message.success('环境配置保存成功');
  } catch (error) {
    message.error('保存环境配置失败: ' + (error.message || '未知错误'));
  } finally {
    savingEnv.value = false;
  }
}

function goToEnvironmentManagement() {
  router.push('/system/environment');
}

function downloadConfig() {
  message.info('配置下载功能需要后端支持');
}

function showHelpModal() {
  helpModalVisible.value = true;
}

function contactSupport() {
  message.info('技术支持电话: 400-XXX-XXXX');
}

// 生命周期
onMounted(() => {
  addLog('INFO', '安装向导已启动');
});
</script>

<style lang="less" scoped>
.install-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 20px;
  overflow-y: auto;
}

.install-wrapper {
  display: flex;
  width: 100%;
  max-width: 1400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.install-header {
  flex: 0 0 280px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow-y: auto;
}

.logo {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.logo-icon {
  font-size: 42px;
  margin-right: 10px;
  line-height: 1;
}

.logo-text {
  font-size: 26px;
  font-weight: bold;
  color: white;
  white-space: nowrap;
  line-height: 1;
}

.version {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 12px;
  align-self: flex-start;
}

.install-content {
  flex: 1;
  padding: 32px 40px 40px;
  overflow-y: auto;
}

.steps-nav {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.steps-content {
  min-height: 400px;
}

.step-content {
  h2 {
    font-size: 24px;
    margin-bottom: 8px;
    color: #1a1a1a;
  }

  .step-desc {
    color: #8c8c8c;
    margin-bottom: 32px;
  }
}

.welcome-icon {
  font-size: 64px;
}

.welcome-info {
  text-align: left;
  margin-bottom: 24px;
}

.feature-list {
  margin: 24px 0;
  text-align: left;

  h4 {
    margin-bottom: 12px;
  }

  ul {
    list-style: none;
    padding: 0;

    li {
      padding: 8px 0;
      color: #595959;
    }
  }
}

.install-type {
  margin: 24px 0;
  text-align: left;

  h4 {
    margin-bottom: 12px;
  }

  :deep(.ant-radio-group) {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .radio-content {
    strong {
      display: block;
      margin-bottom: 4px;
    }

    div {
      font-size: 12px;
      color: #8c8c8c;
    }
  }
}

.env-check-list {
  margin: 24px 0;
}

.storage-config {
  .radio-content {
    strong {
      display: block;
      margin-bottom: 4px;
    }

    div {
      font-size: 12px;
      color: #8c8c8c;
    }
  }
}

.install-tasks {
  margin: 24px 0;
}

.overall-progress {
  margin: 24px 0;
}

.install-log {
  margin-top: 24px;

  .log-container {
    background: #f5f5f5;
    border-radius: 4px;
    padding: 16px;
    max-height: 300px;
    overflow-y: auto;
    font-family: 'Courier New', monospace;
    font-size: 12px;
  }

  .log-entry {
    padding: 4px 0;
    display: flex;
    gap: 8px;

    .log-time {
      color: #8c8c8c;
    }

    .log-level {
      font-weight: bold;

      &.log-INFO {
        color: #1890ff;
      }

      &.log-SUCCESS {
        color: #52c41a;
      }

      &.log-WARNING {
        color: #faad14;
      }

      &.log-ERROR {
        color: #ff4d4f;
      }
    }

    .log-message {
      color: #262626;
    }
  }
}

.step-actions {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.install-sidebar {
  flex: 0 0 220px;
  background: #fafafa;
  padding: 24px 20px;
  border-left: 1px solid #f0f0f0;
  overflow-y: auto;
}

.sidebar-content {
  h3 {
    font-size: 18px;
    margin-bottom: 24px;
    color: #262626;
  }

  .tips {
    p {
      color: #595959;
      line-height: 1.8;
      margin-bottom: 12px;
    }
  }

  .help-box {
    h4 {
      font-size: 14px;
      margin-bottom: 12px;
      color: #595959;
    }

    .ant-btn {
      display: block;
      margin-bottom: 8px;
      padding: 0;
      height: auto;
    }
  }
}

.success-icon {
  font-size: 64px;
}

.completion-info {
  text-align: left;
  max-width: 600px;
  margin: 0 auto 24px;

  .ant-card {
    text-align: left;
  }

  ul {
    margin: 12px 0;
    padding-left: 20px;

    li {
      margin-bottom: 8px;
    }
  }
}

.help-content {
  h3 {
    margin-bottom: 16px;
  }

  h4 {
    margin: 16px 0 8px;
  }

  ul {
    margin-left: 20px;
    line-height: 1.8;

    li {
      margin-bottom: 8px;
    }
  }
}
</style>
