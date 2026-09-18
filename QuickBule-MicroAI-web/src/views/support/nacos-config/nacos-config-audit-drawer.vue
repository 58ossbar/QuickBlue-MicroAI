<!--
  * Nacos配置审计抽屉
  *
-->
<template>
  <a-drawer
    v-model:open="visible"
    title="配置变更审计"
    :width="900"
  >
    <a-form layout="inline" class="qb-margin-bottom10">
      <a-form-item label="配置ID">
        <a-input v-model:value="queryForm.dataId" placeholder="配置ID" allowClear style="width: 200px" />
      </a-form-item>
      <a-form-item label="操作类型">
        <a-select v-model:value="queryForm.opType" placeholder="全部" allowClear style="width: 120px">
          <a-select-option value="CREATE">新增</a-select-option>
          <a-select-option value="UPDATE">修改</a-select-option>
          <a-select-option value="DELETE">删除</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="操作人">
        <a-input v-model:value="queryForm.operatorName" placeholder="操作人" allowClear style="width: 120px" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="onSearch">
          <template #icon><SearchOutlined /></template>
          查询
        </a-button>
        <a-button @click="resetQuery" class="qb-margin-left10">重置</a-button>
      </a-form-item>
    </a-form>

    <a-table
      size="small"
      :loading="loading"
      :dataSource="tableData"
      :columns="columns"
      rowKey="auditId"
      :pagination="pagination"
      @change="onPageChange"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'opType'">
          <a-tag :color="getOpTypeColor(record.opType)">{{ record.opTypeDesc }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <a-button type="link" size="small" @click="showDetail(record)">详情</a-button>
        </template>
      </template>
    </a-table>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="变更详情"
      :width="900"
      :footer="null"
    >
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="配置ID">{{ detailData.dataId }}</a-descriptions-item>
        <a-descriptions-item label="配置分组">{{ detailData.groupId }}</a-descriptions-item>
        <a-descriptions-item label="操作类型">
          <a-tag :color="getOpTypeColor(detailData.opType)">{{ detailData.opTypeDesc }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="操作人">{{ detailData.operatorName }}</a-descriptions-item>
        <a-descriptions-item label="操作IP">{{ detailData.operatorIp }}</a-descriptions-item>
        <a-descriptions-item label="操作时间">{{ detailData.createTime }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detailData.remark }}</a-descriptions-item>
      </a-descriptions>

      <a-divider>变更对比</a-divider>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-card size="small" title="修改前">
            <a-textarea
              :value="detailData.oldContent || '(无)'"
              :rows="12"
              read-only
              class="code-textarea"
            />
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card size="small" title="修改后">
            <a-textarea
              :value="detailData.newContent || '(无)'"
              :rows="12"
              read-only
              class="code-textarea"
            />
          </a-card>
        </a-col>
      </a-row>
    </a-modal>
  </a-drawer>
</template>

<script setup>
  import { ref, reactive } from 'vue';
  import { nacosConfigApi } from '/src/api/support/nacos-config-api';
  import { sentry } from '/src/lib/sentry';

  const visible = ref(false);
  const loading = ref(false);
  const tenantId = ref('');

  const queryFormState = {
    dataId: '',
    groupId: '',
    tenantId: '',
    opType: '',
    operatorName: '',
    startTime: null,
    endTime: null,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });

  const columns = [
    { title: '配置ID', dataIndex: 'dataId', width: 200, ellipsis: true },
    { title: '配置名称', dataIndex: 'configName', width: 120 },
    { title: '操作类型', dataIndex: 'opType', width: 80 },
    { title: '操作人', dataIndex: 'operatorName', width: 100 },
    { title: '操作IP', dataIndex: 'operatorIp', width: 120 },
    { title: '操作时间', dataIndex: 'createTime', width: 160 },
    { title: '操作', dataIndex: 'action', width: 80 },
  ];

  const tableData = ref([]);

  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条`,
  });

  const detailVisible = ref(false);
  const detailData = ref({});

  function show(nsId) {
    visible.value = true;
    tenantId.value = nsId || '';
    resetQuery();
  }

  async function loadData() {
    try {
      loading.value = true;
      const params = {
        ...queryForm,
        tenantId: tenantId.value,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const res = await nacosConfigApi.queryAuditByPage(params);
      if (res.data) {
        tableData.value = res.data.list || [];
        pagination.total = res.data.totalNum || 0;
      }
    } catch (e) {
      sentry.captureError(e);
    } finally {
      loading.value = false;
    }
  }

  function onSearch() {
    pagination.current = 1;
    loadData();
  }

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    pagination.current = 1;
    loadData();
  }

  function onPageChange(page) {
    pagination.current = page.current;
    pagination.pageSize = page.pageSize;
    loadData();
  }

  async function showDetail(record) {
    try {
      const res = await nacosConfigApi.getAuditDetail(record.auditId);
      if (res.data) {
        detailData.value = res.data;
        detailVisible.value = true;
      }
    } catch (e) {
      sentry.captureError(e);
    }
  }

  function getOpTypeColor(opType) {
    const colors = {
      CREATE: 'green',
      UPDATE: 'blue',
      DELETE: 'red',
    };
    return colors[opType] || 'default';
  }

  defineExpose({ show });
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
