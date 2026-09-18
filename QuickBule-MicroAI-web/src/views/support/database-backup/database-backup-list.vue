<!--
  * 数据库备份列表
-->
<template>
  <div>
    <!---------- 等保二级必备标注 begin ---------->
    <div style="margin-bottom: 8px;">
      <a-tag color="green" style="margin-right: 8px; font-weight: 600;">二级必备</a-tag>
      <span style="color: rgba(0,0,0,0.45); font-size: 12px;">依据 GB/T 22239-2019《网络安全等级保护基本要求》二级要求，本功能为等保二级核查项</span>
    </div>
    <!---------- 等保二级必备标注 end ---------->
    <a-form class="qb-query-form">
      <a-row class="qb-query-form-row">
        <a-form-item label="文件名" class="qb-query-form-item">
          <a-input v-model:value="queryForm.fileName" placeholder="请输入文件名" style="width: 200px" />
        </a-form-item>

        <a-form-item label="备份类型" class="qb-query-form-item">
          <EnumSelect width="120px" v-model:value="queryForm.backupType" enumName="BACKUP_TYPE_ENUM" placeholder="全部" />
        </a-form-item>

        <a-form-item label="备份状态" class="qb-query-form-item">
          <EnumSelect width="120px" v-model:value="queryForm.backupStatus" enumName="BACKUP_STATUS_ENUM" placeholder="全部" />
        </a-form-item>

        <a-form-item label="操作人" class="qb-query-form-item">
          <a-input v-model:value="queryForm.operator" placeholder="请输入操作人" style="width: 150px" />
        </a-form-item>

        <a-form-item class="qb-query-form-item qb-margin-left10">
          <a-button-group>
            <a-button type="primary" @click="query">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>

            <a-button @click="resetQuery">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </a-button-group>
        </a-form-item>
      </a-row>
    </a-form>

    <a-card size="small" :bordered="false" :hoverable="true">
      <a-row class="qb-table-btn-block">
        <div class="qb-table-operate-block">
          <a-button type="primary" @click="showConfig">
            <template #icon>
              <CloudUploadOutlined />
            </template>
            备份配置
          </a-button>

          <a-button type="primary" @click="executeBackup">
            <template #icon>
              <CloudUploadOutlined />
            </template>
            手动备份
          </a-button>

          <a-button type="primary" @click="cleanExpired">
            <template #icon>
              <DeleteOutlined />
            </template>
            清理过期备份
          </a-button>
        </div>
        <div class="qb-table-setting-block">
          <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.SUPPORT.DATABASE_BACKUP" :refresh="query" />
        </div>
      </a-row>

      <a-table
        ref="tableRef"
        size="small"
        :scroll="{ x: 1200 }"
        :dataSource="tableData"
        :columns="columns"
        :loading="tableLoading"
        :pagination="pagination"
        rowKey="backupId"
        @change="changeTable"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'backupType'">
            <a-tag :color="record.backupType === 1 ? 'blue' : 'green'">
              {{ record.backupTypeName }}
            </a-tag>
          </template>

          <template v-if="column.dataIndex === 'backupStatus'">
            <a-tag :color="record.backupStatus === 1 ? 'success' : record.backupStatus === 2 ? 'error' : 'processing'">
              {{ record.backupStatusName }}
            </a-tag>
          </template>

          <template v-if="column.dataIndex === 'fileSizeDisplay'">
            <span>{{ record.fileSizeDisplay || '-' }}</span>
          </template>

          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a-button
                v-if="record.backupStatus === 1"
                type="link"
                size="small"
                @click="downloadBackup(record)"
              >
                <template #icon>
                  <DownloadOutlined />
                </template>
                下载
              </a-button>

              <a-button type="link" size="small" danger @click="confirmDelete(record)">
                <template #icon>
                  <DeleteOutlined />
                </template>
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 备份配置弹窗 -->
    <a-modal v-model:open="configModal.visible" title="数据库备份配置" width="800px" @ok="saveConfig" :confirmLoading="configModal.loading">
      <div style="max-height: 60vh; overflow-y: auto; padding-right: 8px;">
        <a-alert
          type="warning"
          message="提示"
          description="配置数据库信息后才能进行手动备份和自动备份。"
          show-icon
          style="margin-bottom: 16px; font-size: 13px;"
        />

        <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
          <a-form-item label="数据库主机">
            <a-input v-model:value="configFormData.dbHost" placeholder="例如: localhost" />
          </a-form-item>

          <a-form-item label="数据库端口">
            <a-input-number v-model:value="configFormData.dbPort" :min="1" :max="65535" style="width: 100%" />
          </a-form-item>

          <a-form-item label="数据库名称">
            <a-input v-model:value="configFormData.dbName" placeholder="请输入数据库名称" />
          </a-form-item>

          <a-form-item label="数据库用户名">
            <a-input v-model:value="configFormData.dbUsername" placeholder="请输入数据库用户名" />
          </a-form-item>

          <a-form-item label="数据库密码">
            <a-input-password v-model:value="configFormData.dbPassword" placeholder="请输入数据库密码" />
          </a-form-item>

        <a-form-item label="备份路径" :help="'请确保该路径有写入权限'">
          <a-input v-model:value="configFormData.backupPath" placeholder="例如: /data/backup" />
        </a-form-item>

        <a-form-item label="pg_dump路径" :help="'可选，如果不填则自动查找。Windows示例: C:\\Program Files\\PostgreSQL\\16\\bin\\pg_dump.exe'">
          <a-input v-model:value="configFormData.pgDumpPath" placeholder="例如: C:\\Program Files\\PostgreSQL\\16\\bin\\pg_dump.exe" />
        </a-form-item>

        <a-form-item label="启用自动备份">
          <a-switch v-model:checked="configFormData.autoBackupEnabled" />
        </a-form-item>

          <template v-if="configFormData.autoBackupEnabled">
            <a-form-item label="备份时间表达式" :help="'Cron表达式，例如每天凌晨2点执行: 0 0 2 * * ?'">
              <a-input v-model:value="configFormData.autoBackupCron" placeholder="例如: 0 0 2 * * ?" />
            </a-form-item>

            <a-form-item label="保留天数" :help="'超过保留天数的备份将被自动清理'">
              <a-input-number v-model:value="configFormData.retentionDays" :min="1" :max="365" style="width: 100%" />
            </a-form-item>
          </template>

          <a-form-item label="备注">
            <a-textarea v-model:value="configFormData.remark" :rows="2" placeholder="请输入备注" />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { SearchOutlined, ReloadOutlined, CloudUploadOutlined, DeleteOutlined, DownloadOutlined } from '@ant-design/icons-vue';
import { databaseBackupApi } from '/@/api/support/database-backup-api.js';
import EnumSelect from '/@/components/framework/enum-select/index.vue';
import {TABLE_ID_CONST} from "/@/constants/support/table-id-const.js";
import TableOperator from '/@/components/support/table-operator/index.vue';
import { useColumnResize } from '/@/hooks/useColumnResize';

// 轮询相关
let pollingTimer = null;
const POLLING_INTERVAL = 3000; // 3秒轮询一次

const tableRef = ref();
const columns = ref([
  { title: '文件名', dataIndex: 'fileName', width: 250, ellipsis: true },
  { title: '备份类型', dataIndex: 'backupType', width: 100 },
  { title: '备份状态', dataIndex: 'backupStatus', width: 100 },
  { title: '文件大小', dataIndex: 'fileSizeDisplay', width: 100 },
  { title: '备注', dataIndex: 'remark', width: 200, ellipsis: true },
  { title: '操作人', dataIndex: 'operator', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', dataIndex: 'action', width: 150, fixed: 'right' },
]);

useColumnResize(tableRef, columns);

const queryForm = reactive({
  fileName: '',
  backupType: undefined,
  backupStatus: undefined,
  operator: '',
});

const tableData = ref([]);
const tableLoading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`,
});

const configModal = reactive({
  visible: false,
  loading: false,
});

const configFormData = reactive({
  configId: undefined,
  dbHost: 'localhost',
  dbPort: 5432,
  dbName: '',
  dbUsername: '',
  dbPassword: '',
  backupPath: '/data/backup',
  pgDumpPath: '',
  autoBackupEnabled: false,
  autoBackupCron: '0 0 2 * * ?',
  retentionDays: 30,
  remark: '',
});

// 查询
const query = async () => {
  try {
    tableLoading.value = true;
    const params = {
      ...queryForm,
      pageSize: pagination.pageSize,
      pageNum: pagination.current,
    };
    const result = await databaseBackupApi.queryPage(params);
    if (result.data) {
      tableData.value = result.data.dataList;
      pagination.total = result.data.total;
    }
    // 检查是否有正在进行的备份任务
    checkRunningBackup();
  } catch (e) {
    message.error('查询失败');
  } finally {
    tableLoading.value = false;
  }
};

// 检查是否有正在进行的备份
const checkRunningBackup = () => {
  const hasRunningBackup = tableData.value.some(item => item.backupStatus === 0); // 0表示备份中
  if (hasRunningBackup) {
    startPolling();
  } else {
    stopPolling();
  }
};

// 开始轮询
const startPolling = () => {
  if (pollingTimer) return;
  pollingTimer = setInterval(() => {
    querySilently();
  }, POLLING_INTERVAL);
};

// 停止轮询
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer);
    pollingTimer = null;
  }
};

// 静默查询（不显示loading）
const querySilently = async () => {
  try {
    const params = {
      ...queryForm,
      pageSize: pagination.pageSize,
      pageNum: pagination.current,
    };
    const result = await databaseBackupApi.queryPage(params);
    if (result.data) {
      tableData.value = result.data.dataList;
      pagination.total = result.data.total;
    }
    checkRunningBackup();
  } catch (e) {
    // 静默失败，不显示错误信息
  }
};

// 重置查询
const resetQuery = () => {
  Object.assign(queryForm, {
    fileName: '',
    backupType: undefined,
    backupStatus: undefined,
    operator: '',
  });
  pagination.current = 1;
  query();
};

// 表格变化
const changeTable = (pagination) => {
  pagination.current = pagination.current;
  pagination.pageSize = pagination.pageSize;
  query();
};

// 执行备份
const executeBackup = () => {
  Modal.confirm({
    title: '确认执行备份',
    content: '确认执行数据库备份？',
    okText: '执行备份',
    cancelText: '取消',
    onOk: async () => {
      try {
        await databaseBackupApi.manualBackup({});
        message.success('备份任务已提交，正在后台执行');
        query();
      } catch (e) {
        message.error('备份提交失败');
      }
    },
  });
};

// 下载备份
const downloadBackup = (record) => {
  try {
    databaseBackupApi.downloadBackup(record.backupId);
  } catch (e) {
    message.error('下载失败');
  }
};

// 确认删除
const confirmDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除备份 "${record.fileName}" 吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await databaseBackupApi.delete(record.backupId);
        message.success('删除成功');
        query();
      } catch (e) {
        message.error('删除失败');
      }
    },
  });
};

// 清理过期备份
const cleanExpired = () => {
  Modal.confirm({
    title: '确认清理',
    content: '确定要清理所有过期备份吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const result = await databaseBackupApi.cleanExpired();
        message.success(result.data || '清理完成');
        query();
      } catch (e) {
        message.error('清理失败');
      }
    },
  });
};

// 显示配置弹窗
const showConfig = async () => {
  try {
    configModal.loading = true;
    configModal.visible = true;
    const result = await databaseBackupApi.getConfig();
    if (result.data) {
      Object.assign(configFormData, result.data);
    }
  } catch (e) {
    message.error('加载配置失败');
  } finally {
    configModal.loading = false;
  }
};

// 保存配置
const saveConfig = async () => {
  try {
    configModal.loading = true;
    await databaseBackupApi.updateConfig(configFormData);
    message.success('配置保存成功');
    configModal.visible = false;
  } catch (e) {
    message.error('配置保存失败');
  } finally {
    configModal.loading = false;
  }
};

onMounted(() => {
  query();
});

// 组件卸载时清理定时器
onBeforeUnmount(() => {
  stopPolling();
});
</script>

<style scoped lang="less"></style>
