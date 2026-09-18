<!--
  * Nacos配置历史版本弹窗
  *
-->
<template>
  <a-modal
    v-model:open="visible"
    title="配置历史版本"
    :width="900"
    :footer="null"
  >
    <a-table
      size="small"
      :loading="loading"
      :dataSource="historyList"
      :columns="columns"
      rowKey="id"
      :pagination="pagination"
      @change="onPageChange"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'opType'">
          <a-tag :color="getOpTypeColor(record.opType)">{{ record.opType }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="showDetail(record)">查看详情</a-button>
            <a-popconfirm title="确定要回滚到此版本吗？" @confirm="onRollback(record)">
              <a-button type="link" size="small">回滚</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 详情抽屉 -->
    <a-drawer
      v-model:open="detailVisible"
      title="历史版本详情"
      :width="700"
    >
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="版本ID">{{ detailData.id }}</a-descriptions-item>
        <a-descriptions-item label="操作类型">{{ detailData.opType }}</a-descriptions-item>
        <a-descriptions-item label="操作IP">{{ detailData.srcIp }}</a-descriptions-item>
        <a-descriptions-item label="修改时间">{{ detailData.lastModifiedTime }}</a-descriptions-item>
        <a-descriptions-item label="MD5" :span="2">{{ detailData.md5 }}</a-descriptions-item>
        <a-descriptions-item label="配置内容" :span="2">
          <a-textarea
            :value="detailData.content"
            :rows="15"
            read-only
            class="code-textarea"
          />
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </a-modal>
</template>

<script setup>
  import { ref, reactive } from 'vue';
  import { nacosConfigApi } from '/src/api/support/nacos-config-api';
  import { message } from 'ant-design-vue';
  import { sentry } from '/src/lib/sentry';

  const visible = ref(false);
  const loading = ref(false);

  const currentConfig = ref({});
  const tenantId = ref('');

  const columns = [
    { title: '版本ID', dataIndex: 'id', width: 100 },
    { title: '操作类型', dataIndex: 'opType', width: 100 },
    { title: '操作IP', dataIndex: 'srcIp', width: 120 },
    { title: '修改时间', dataIndex: 'lastModifiedTime', width: 180 },
    { title: '操作', dataIndex: 'action', width: 150 },
  ];

  const historyList = ref([]);

  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条`,
  });

  // 详情
  const detailVisible = ref(false);
  const detailData = ref({});

  async function showModal(config, nsId) {
    visible.value = true;
    currentConfig.value = config;
    tenantId.value = nsId || '';
    pagination.current = 1;
    await loadHistory();
  }

  async function loadHistory() {
    try {
      loading.value = true;
      const res = await nacosConfigApi.getConfigHistory(
        currentConfig.value.dataId,
        currentConfig.value.groupId,
        tenantId.value,
        pagination.current,
        pagination.pageSize
      );
      if (res.data) {
        historyList.value = res.data.list || [];
        pagination.total = res.data.totalNum || 0;
      }
    } catch (e) {
      sentry.captureError(e);
    } finally {
      loading.value = false;
    }
  }

  function onPageChange(page) {
    pagination.current = page.current;
    pagination.pageSize = page.pageSize;
    loadHistory();
  }

  async function showDetail(record) {
    try {
      const res = await nacosConfigApi.getConfigHistoryDetail(
        record.id,
        currentConfig.value.dataId,
        currentConfig.value.groupId,
        tenantId.value
      );
      if (res.data) {
        detailData.value = res.data;
        detailVisible.value = true;
      }
    } catch (e) {
      sentry.captureError(e);
    }
  }

  async function onRollback(record) {
    try {
      const res = await nacosConfigApi.rollbackConfig(
        record.id,
        currentConfig.value.dataId,
        currentConfig.value.groupId,
        tenantId.value
      );
      if (res.ok) {
        message.success('回滚成功');
        loadHistory();
      } else {
        message.error(res.msg || '回滚失败');
      }
    } catch (e) {
      sentry.captureError(e);
    }
  }

  function getOpTypeColor(opType) {
    const colors = {
      I: 'green',
      U: 'blue',
      D: 'red',
    };
    return colors[opType] || 'default';
  }

  defineExpose({ showModal });
</script>

<style scoped>
.code-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  background-color: #1e1e1e;
  color: #d4d4d4;
  border: 1px solid #434343;
}
</style>
