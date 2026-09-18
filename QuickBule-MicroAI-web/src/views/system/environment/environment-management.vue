<template>
  <div class="environment-management">
    <a-card>
      <template #title>
        <div class="card-title">
          <DatabaseOutlined />
          <span>环境管理</span>
        </div>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="showCreateModal">
            <PlusOutlined /> 新建环境
          </a-button>
          <a-button @click="loadEnvironments">
            <ReloadOutlined /> 刷新
          </a-button>
        </a-space>
      </template>

      <!-- 环境列表 -->
      <a-spin :spinning="loading">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="env in environments" :key="env.envName">
            <a-card
              :class="['environment-card', { 'active': env.active }]"
              :hoverable="true"
              @click="selectEnvironment(env)"
            >
              <div class="env-header">
                <div class="env-icon">
                  <EnvironmentOutlined />
                </div>
                <div class="env-info">
                  <div class="env-name">{{ env.envName }}</div>
                  <div class="env-description">{{ env.description }}</div>
                </div>
                <a-tag v-if="env.active" color="green">当前激活</a-tag>
              </div>

              <div class="env-config">
                <div class="config-item">
                  <span class="label">数据库:</span>
                  <span class="value">{{ getDbTypeText(env) }}</span>
                </div>
                <div class="config-item">
                  <span class="label">创建时间:</span>
                  <span class="value">{{ formatDate(env.createTime) }}</span>
                </div>
              </div>

              <div class="env-actions">
                <a-space size="small">
                  <a-button
                    v-if="!env.active"
                    type="primary"
                    size="small"
                    @click.stop="handleSwitch(env)"
                    :loading="switching && switchingEnv === env.envName"
                  >
                    切换
                  </a-button>
                  <a-button
                    size="small"
                    @click.stop="showEditDbModal(env)"
                  >
                    配置数据库
                  </a-button>
                  <a-button
                    size="small"
                    @click.stop="handleTestDb(env)"
                    :loading="testing && testingEnv === env.envName"
                  >
                    测试连接
                  </a-button>
                  <a-dropdown>
                    <a-button size="small">
                      更多 <DownOutlined />
                    </a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item @click="handleExport(env)">
                          <ExportOutlined /> 导出配置
                        </a-menu-item>
                        <a-menu-item @click="showImportModal(env)">
                          <ImportOutlined /> 导入配置
                        </a-menu-item>
                        <a-menu-item
                          v-if="!env.active"
                          @click="handleDelete(env)"
                          danger
                        >
                          <DeleteOutlined /> 删除
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </a-space>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-empty v-if="environments.length === 0" description="暂无环境配置" />
      </a-spin>
    </a-card>

    <!-- 数据库配置弹窗 -->
    <a-modal
      v-model:open="dbConfigModalVisible"
      title="配置数据库连接"
      :width="600"
      @ok="handleUpdateDb"
      :confirmLoading="updating"
    >
      <a-form
        ref="dbFormRef"
        :model="dbConfigForm"
        :rules="dbRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="数据库类型">
          <a-radio-group v-model:value="dbConfigForm.dbType" disabled>
            <a-radio value="postgresql">PostgreSQL</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="主机地址" name="host">
          <a-input v-model:value="dbConfigForm.host" placeholder="localhost" />
        </a-form-item>

        <a-form-item label="端口" name="port">
          <a-input-number v-model:value="dbConfigForm.port" :min="1" :max="65535" style="width: 100%;" />
        </a-form-item>

        <a-form-item label="管理员账号" name="adminUser">
          <a-input v-model:value="dbConfigForm.adminUser" />
        </a-form-item>

        <a-form-item label="管理员密码" name="adminPassword">
          <a-input-password v-model:value="dbConfigForm.adminPassword" placeholder="请输入管理员密码" />
        </a-form-item>

        <a-form-item label="选择数据库">
          <a-select v-model:value="dbConfigForm.databaseName" placeholder="请选择要配置的数据库">
            <a-select-option value="quickblue_support">支撑服务数据库</a-select-option>
            <a-select-option value="quickblue_system">系统服务数据库</a-select-option>
            <a-select-option value="quickblue_business">业务服务数据库</a-select-option>
            <a-select-option value="quickblue_ai">AI服务数据库</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item v-if="dbConfigForm.databaseName" label="数据库用户" name="databaseUser">
          <a-input v-model:value="dbConfigForm.databaseUser" />
        </a-form-item>

        <a-form-item v-if="dbConfigForm.databaseName" label="数据库密码" name="databasePassword">
          <a-input-password v-model:value="dbConfigForm.databasePassword" placeholder="请输入数据库密码" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 新建环境弹窗 -->
    <a-modal
      v-model:open="createModalVisible"
      title="新建环境"
      :width="700"
      @ok="handleCreateEnvironment"
      :confirmLoading="creating"
    >
      <a-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-divider orientation="left">基本信息</a-divider>
        <a-form-item label="环境名称" name="envName">
          <a-input v-model:value="createForm.envName" placeholder="如：开发环境、测试环境" />
        </a-form-item>

        <a-form-item label="环境描述" name="description">
          <a-textarea
            v-model:value="createForm.description"
            :rows="2"
            placeholder="请输入环境描述"
          />
        </a-form-item>

        <a-divider orientation="left">数据库配置</a-divider>
        <a-form-item label="数据库类型" name="dbType">
          <a-radio-group v-model:value="createForm.dbType">
            <a-radio value="postgresql">PostgreSQL</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="主机地址" name="host">
          <a-input v-model:value="createForm.host" placeholder="localhost" />
        </a-form-item>

        <a-form-item label="端口" name="port">
          <a-input-number
            v-model:value="createForm.port"
            :min="1"
            :max="65535"
            style="width: 100%;"
            placeholder="默认端口：PostgreSQL 5432"
          />
        </a-form-item>

        <a-form-item label="管理员账号" name="adminUser">
          <a-input v-model:value="createForm.adminUser" placeholder="postgres" />
        </a-form-item>

        <a-form-item label="管理员密码" name="adminPassword">
          <a-input-password v-model:value="createForm.adminPassword" placeholder="请输入管理员密码" />
        </a-form-item>

        <a-form-item label="选择数据库" name="databaseName">
          <a-select v-model:value="createForm.databaseName" placeholder="请选择要配置的数据库">
            <a-select-option value="quickblue_support">支撑服务数据库</a-select-option>
            <a-select-option value="quickblue_system">系统服务数据库</a-select-option>
            <a-select-option value="quickblue_business">业务服务数据库</a-select-option>
            <a-select-option value="quickblue_ai">AI服务数据库</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item v-if="createForm.databaseName" label="数据库用户" name="databaseUser">
          <a-input v-model:value="createForm.databaseUser" placeholder="请输入数据库用户名" />
        </a-form-item>

        <a-form-item v-if="createForm.databaseName" label="数据库密码" name="databasePassword">
          <a-input-password v-model:value="createForm.databasePassword" placeholder="请输入数据库密码" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 导入配置弹窗 -->
    <a-modal
      v-model:open="importModalVisible"
      title="导入环境配置"
      :width="600"
      @ok="handleImport"
      :confirmLoading="importing"
    >
      <a-form
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="环境名称">
          <a-input v-model:value="importForm.envName" placeholder="请输入环境名称" />
        </a-form-item>

        <a-form-item label="配置JSON">
          <a-textarea
            v-model:value="importForm.configJson"
            :rows="10"
            placeholder="请粘贴配置JSON"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 导出配置弹窗 -->
    <a-modal
      v-model:open="exportModalVisible"
      title="导出环境配置"
      :width="600"
      :footer="null"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="配置JSON">
          <a-textarea
            :value="exportConfigJson"
            :rows="15"
            readonly
            copyable
          />
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-button type="primary" @click="copyExportConfig">
            <CopyOutlined /> 复制配置
          </a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  DatabaseOutlined,
  EnvironmentOutlined,
  PlusOutlined,
  ReloadOutlined,
  DeleteOutlined,
  ExportOutlined,
  ImportOutlined,
  DownOutlined,
  CopyOutlined
} from '@ant-design/icons-vue';
import environmentApi from '/@/api/environment/environment-api';
import { DB_TYPE_ENUM } from '/@/constants/environment-const';
import dayjs from 'dayjs';

const loading = ref(false);
const environments = ref([]);
const switching = ref(false);
const switchingEnv = ref('');
const testing = ref(false);
const testingEnv = ref('');
const updating = ref(false);
const importing = ref(false);
const creating = ref(false);
const currentEnv = ref(null);

// 新建环境弹窗
const createModalVisible = ref(false);
const createFormRef = ref();
const createForm = reactive({
  envName: '',
  description: '',
  dbType: 'postgresql',
  host: 'localhost',
  port: 5432,
  adminUser: '',
  adminPassword: '',
  databaseName: '',
  databaseUser: '',
  databasePassword: ''
});

const createRules = {
  envName: [
    { required: true, message: '请输入环境名称' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '环境名称只能包含字母、数字、下划线和中文' }
  ],
  description: [{ required: true, message: '请输入环境描述' }],
  dbType: [{ required: true, message: '请选择数据库类型' }],
  host: [{ required: true, message: '请输入主机地址' }],
  port: [{ required: true, message: '请输入端口' }],
  adminUser: [{ required: true, message: '请输入管理员账号' }],
  adminPassword: [{ required: true, message: '请输入管理员密码' }],
  databaseName: [{ required: true, message: '请选择数据库' }]
};

// 数据库配置弹窗
const dbConfigModalVisible = ref(false);
const dbFormRef = ref();
const dbConfigForm = reactive({
  envName: '',
  dbType: 'postgresql',
  host: '',
  port: 5432,
  adminUser: '',
  adminPassword: '',
  databaseName: '',
  databaseUser: '',
  databasePassword: ''
});

const dbRules = {
  host: [{ required: true, message: '请输入主机地址' }],
  port: [{ required: true, message: '请输入端口' }],
  adminUser: [{ required: true, message: '请输入管理员账号' }],
  adminPassword: [{ required: true, message: '请输入管理员密码' }]
};

// 导入配置弹窗
const importModalVisible = ref(false);
const importForm = reactive({
  envName: '',
  configJson: ''
});

// 导出配置弹窗
const exportModalVisible = ref(false);
const exportConfigJson = ref('');

// 加载环境列表
async function loadEnvironments() {
  loading.value = true;
  try {
    const response = await environmentApi.listEnvironments();
    environments.value = response.data || [];
  } catch (error) {
    message.error('加载环境列表失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
}

// 选择环境
function selectEnvironment(env) {
  currentEnv.value = env;
}

// 切换环境
async function handleSwitch(env) {
  Modal.confirm({
    title: '确认切换环境',
    content: `确定要切换到环境【${env.envName}】吗？此操作会更新Nacos配置，可能需要重启服务。`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      switching.value = true;
      switchingEnv.value = env.envName;

      try {
        await environmentApi.switchEnvironment({ envName: env.envName });
        message.success(`已切换到环境【${env.envName}】`);
        await loadEnvironments();
      } catch (error) {
        message.error('切换环境失败: ' + (error.message || '未知错误'));
      } finally {
        switching.value = false;
        switchingEnv.value = '';
      }
    }
  });
}

// 测试数据库连接
async function handleTestDb(env) {
  testing.value = true;
  testingEnv.value = env.envName;

  try {
    const response = await environmentApi.testEnvironmentDb(env.envName);
    if (response.data) {
      message.success('数据库连接测试成功');
    } else {
      message.error('数据库连接测试失败');
    }
  } catch (error) {
    message.error('测试数据库连接失败: ' + (error.message || '未知错误'));
  } finally {
    testing.value = false;
    testingEnv.value = '';
  }
}

// 显示编辑数据库配置弹窗
function showEditDbModal(env) {
  currentEnv.value = env;

  // 解析配置
  try {
    const config = JSON.parse(env.configJson);
    const dbConfig = config.database;

    dbConfigForm.envName = env.envName;
    dbConfigForm.dbType = dbConfig.dbType;
    dbConfigForm.host = dbConfig.host;
    dbConfigForm.port = dbConfig.port;
    dbConfigForm.adminUser = dbConfig.adminUser;
    dbConfigForm.adminPassword = dbConfig.adminPassword;
    dbConfigForm.databaseName = '';
    dbConfigForm.databaseUser = '';
    dbConfigForm.databasePassword = '';

    dbConfigModalVisible.value = true;
  } catch (error) {
    message.error('解析配置失败');
  }
}

// 更新数据库配置
async function handleUpdateDb() {
  try {
    await dbFormRef.value.validate();
  } catch (error) {
    return;
  }

  updating.value = true;

  try {
    await environmentApi.updateEnvironmentDb(currentEnv.value.envName, dbConfigForm);
    message.success('数据库配置更新成功');
    dbConfigModalVisible.value = false;
  } catch (error) {
    message.error('更新数据库配置失败: ' + (error.message || '未知错误'));
  } finally {
    updating.value = false;
  }
}

// 删除环境
function handleDelete(env) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除环境【${env.envName}】吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        await environmentApi.deleteEnvironment(env.envName);
        message.success('删除成功');
        await loadEnvironments();
      } catch (error) {
        message.error('删除失败: ' + (error.message || '未知错误'));
      }
    }
  });
}

// 显示导入弹窗
function showImportModal(env) {
  importForm.envName = env ? env.envName : '';
  importForm.configJson = '';
  importModalVisible.value = true;
}

// 导入配置
async function handleImport() {
  if (!importForm.envName) {
    message.error('请输入环境名称');
    return;
  }

  if (!importForm.configJson) {
    message.error('请输入配置JSON');
    return;
  }

  // 验证JSON格式
  try {
    JSON.parse(importForm.configJson);
  } catch (error) {
    message.error('配置JSON格式错误: ' + error.message);
    return;
  }

  importing.value = true;

  try {
    await environmentApi.importEnvironment(importForm);
    message.success('导入成功');
    importModalVisible.value = false;
    await loadEnvironments();
  } catch (error) {
    message.error('导入失败: ' + (error.message || '未知错误'));
  } finally {
    importing.value = false;
  }
}

// 导出配置
async function handleExport(env) {
  try {
    const response = await environmentApi.exportEnvironment(env.envName);
    exportConfigJson.value = response.data;
    exportModalVisible.value = true;
  } catch (error) {
    message.error('导出失败: ' + (error.message || '未知错误'));
  }
}

// 复制导出配置
function copyExportConfig() {
  navigator.clipboard.writeText(exportConfigJson.value).then(() => {
    message.success('已复制到剪贴板');
  }).catch(() => {
    message.error('复制失败');
  });
}

// 显示创建弹窗
function showCreateModal() {
  createForm.envName = '';
  createForm.description = '';
  createForm.dbType = 'postgresql';
  createForm.host = 'localhost';
  createForm.port = 5432;
  createForm.adminUser = '';
  createForm.adminPassword = '';
  createForm.databaseName = '';
  createForm.databaseUser = '';
  createForm.databasePassword = '';
  createModalVisible.value = true;
}

// 创建环境
async function handleCreateEnvironment() {
  try {
    await createFormRef.value.validate();
  } catch (error) {
    return;
  }

  creating.value = true;

  try {
    // 构建配置JSON
    const configJson = JSON.stringify({
      installType: 'simple',
      database: {
        dbType: createForm.dbType,
        host: createForm.host,
        port: createForm.port,
        adminUser: createForm.adminUser,
        adminPassword: createForm.adminPassword,
        databases: [
          {
            name: createForm.databaseName,
            username: createForm.databaseUser,
            password: createForm.databasePassword
          }
        ]
      }
    });

    const data = {
      envName: createForm.envName,
      configJson: configJson
    };

    await environmentApi.importEnvironment(data);
    message.success('环境创建成功');
    createModalVisible.value = false;
    await loadEnvironments();
  } catch (error) {
    message.error('创建环境失败: ' + (error.message || '未知错误'));
  } finally {
    creating.value = false;
  }
}

// 格式化日期
function formatDate(dateStr) {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss');
}

// 获取数据库类型文本
function getDbTypeText(env) {
  try {
    const config = JSON.parse(env.configJson);
    const dbType = config.database?.dbType;
    if (dbType === 'postgresql') {
      return 'PostgreSQL';
    }
    return '未知';
  } catch (error) {
    return '未知';
  }
}

// 生命周期
onMounted(() => {
  loadEnvironments();
});
</script>

<style lang="less" scoped>
.environment-management {
  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 500;
  }

  .environment-card {
    cursor: pointer;
    transition: all 0.3s;
    border: 2px solid transparent;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }

    &.active {
      border-color: #52c41a;
      background-color: #f6ffed;
    }
  }

  .env-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;

    .env-icon {
      font-size: 32px;
      color: #1890ff;
    }

    .env-info {
      flex: 1;

      .env-name {
        font-size: 16px;
        font-weight: 500;
        color: #262626;
        margin-bottom: 4px;
      }

      .env-description {
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }

  .env-config {
    margin-bottom: 16px;

    .config-item {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      margin-bottom: 8px;

      .label {
        color: #8c8c8c;
      }

      .value {
        color: #262626;
        font-weight: 500;
      }
    }
  }

  .env-actions {
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>
