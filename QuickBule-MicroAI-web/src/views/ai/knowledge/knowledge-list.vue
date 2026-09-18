<!--
  AI知识库管理列表

-->
<template>
  <a-form class="qb-query-form">
    <a-row class="qb-query-form-row">
      <a-form-item label="知识库名称" class="qb-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.name" placeholder="请输入知识库名称" />
      </a-form-item>
      <a-form-item label="类型" class="qb-query-form-item">
        <a-select style="width: 120px" v-model:value="queryForm.type" placeholder="请选择类型" allowClear>
          <a-select-option v-for="item in enumPlugin.getValueDescList('AI_KNOWLEDGE_TYPE_ENUM')" :key="item.value" :value="item.value">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态" class="qb-query-form-item">
        <a-select style="width: 120px" v-model:value="queryForm.status" placeholder="请选择状态" allowClear>
          <a-select-option v-for="item in enumPlugin.getValueDescList('AI_KNOWLEDGE_STATUS_ENUM')" :key="item.value" :value="item.value">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="qb-query-form-item qb-margin-left10">
        <a-button-group>
          <a-button type="primary" @click="onSearch">
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
        <a-button @click="addOrUpdateKnowledge" type="primary">
          <template #icon>
            <PlusOutlined />
          </template>
          新建
        </a-button>

        <a-button @click="confirmBatchDelete" type="primary" danger :disabled="selectedRowKeyList.length === 0">
          <template #icon>
            <DeleteOutlined />
          </template>
          批量删除
        </a-button>
      </div>
      <div class="qb-table-setting-block">
        <TableOperator class="qb-margin-bottom5" v-model="columns" :tableId="null" :refresh="ajaxQuery" />
      </div>
    </a-row>

    <a-table
      ref="tableRef"
      size="small"
      :dataSource="tableData"
      :columns="columns"
      :loading="tableLoading"
      rowKey="id"
      :pagination="false"
      bordered
      :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'type'">
          <a-tag :color="record.type === 'knowledge' ? 'blue' : 'purple'">
            {{ getTypeDesc(record.type) }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 'enable' ? 'green' : 'red'">
            {{ getStatusDesc(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'embedId'">
          <a-tag v-if="record.embedId" color="orange">{{ getModelName(record.embedId) }}</a-tag>
          <span v-else class="text-gray">-</span>
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="addOrUpdateKnowledge(record)" type="link">编辑</a-button>
            <a-button @click="enableOrDisable(record)" type="link" v-if="record.status === 'disable'">启用</a-button>
            <a-button @click="enableOrDisable(record)" type="link" v-if="record.status === 'enable'">禁用</a-button>
          </div>
        </template>
      </template>
    </a-table>

    <div class="qb-query-table-page">
      <a-pagination
        showSizeChanger
        showQuickJumper
        show-less-items
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        :defaultPageSize="queryForm.pageSize"
        v-model:current="queryForm.pageNum"
        v-model:pageSize="queryForm.pageSize"
        :total="total"
        @change="ajaxQuery"
        :show-total="(total) => `共${total}条`"
      />
    </div>

    <KnowledgeFormModal ref="knowledgeFormModalRef" @reloadList="ajaxQuery" />
  </a-card>
</template>

<script setup>
  import { onMounted, reactive, ref, getCurrentInstance } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { Loading } from '/@/components/framework/loading';
  import { aiKnowledgeApi } from '/@/api/ai/knowledge-api';
  import { aiModelApi } from '/@/api/ai/model-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import { SearchOutlined, ReloadOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import KnowledgeFormModal from './knowledge-form-modal.vue';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  // 获取全局实例
  const internalInstance = getCurrentInstance();
  const enumPlugin = internalInstance.appContext.config.globalProperties.$enumPlugin;

  const tableRef = ref();
  const columns = ref([
    {
      title: '知识库名称',
      dataIndex: 'name',
      minWidth: 150,
    },
    {
      title: '类型',
      dataIndex: 'type',
      width: 100,
    },
    {
      title: '向量模型',
      dataIndex: 'embedId',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
    },
    {
      title: '描述',
      dataIndex: 'descr',
      minWidth: 200,
      ellipsis: true,
    },
    {
      title: '创建时间',
      width: 160,
      dataIndex: 'createTime',
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 150,
    },
  ]);

  useColumnResize(tableRef, columns);

  // ---------------- 查询数据表单和方法 ----------------

  const queryFormState = {
    name: '',
    type: '',
    status: '',
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const tableLoading = ref(false);
  const selectedRowKeyList = ref([]);
  const tableData = ref([]);
  const total = ref(0);
  const knowledgeFormModalRef = ref();
  const modelList = ref([]);

  function onSelectChange(selectedRowKeys) {
    selectedRowKeyList.value = selectedRowKeys;
  }

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    ajaxQuery();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    ajaxQuery();
  }

  async function loadModelList() {
    try {
      const response = await aiModelApi.getList();
      modelList.value = response.data || [];
    } catch (e) {
      console.error('加载模型列表失败:', e);
    }
  }

  function getModelName(modelId) {
    const model = modelList.value.find((item) => item.id === modelId);
    return model ? model.name : modelId;
  }

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseData = await aiKnowledgeApi.getPage(queryForm);
      const list = responseData.data.records || [];
      total.value = responseData.data.total || 0;
      tableData.value = list;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // ----------------------- 启用/禁用 ------------------------
  async function enableOrDisable(knowledge) {
    const action = knowledge.status === 'disable' ? '启用' : '禁用';
    Modal.confirm({
      title: '提示',
      content: `确定要${action}知识库"${knowledge.name}"吗？`,
      okText: '确定',
      onOk: async () => {
        Loading.show();
        try {
          if (knowledge.status === 'disable') {
            await aiKnowledgeApi.enable(knowledge.id);
          } else {
            await aiKnowledgeApi.disable(knowledge.id);
          }
          message.success(`${action}成功`);
          await ajaxQuery();
        } catch (e) {
          sentry.captureError(e);
        } finally {
          Loading.hide();
        }
      },
      cancelText: '取消',
    });
  }

  // ---------------- 批量删除 -----------------

  function confirmBatchDelete() {
    Modal.confirm({
      title: '提示',
      content: '确定要删除选中的知识库吗？',
      okText: '删除',
      okType: 'danger',
      onOk() {
        batchDelete();
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  async function batchDelete() {
    try {
      Loading.show();
      await aiKnowledgeApi.batchDelete(selectedRowKeyList.value);
      message.success('删除成功');
      await ajaxQuery();
      selectedRowKeyList.value = [];
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }

  // ---------------- 添加/更新 -----------------

  function addOrUpdateKnowledge(rowData) {
    knowledgeFormModalRef.value.showModal(rowData);
  }

  // ---------------- 辅助函数 ----------------

  function getTypeDesc(type) {
    const desc = enumPlugin.getDescByValue('AI_KNOWLEDGE_TYPE_ENUM', type);
    return desc || type;
  }

  function getStatusDesc(status) {
    const desc = enumPlugin.getDescByValue('AI_KNOWLEDGE_STATUS_ENUM', status);
    return desc || status;
  }

  onMounted(() => {
    loadModelList();
    ajaxQuery();
  });
</script>
