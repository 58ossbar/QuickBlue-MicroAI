<!--
  AI应用管理列表

-->
<template>
  <a-form class="qb-query-form">
    <a-row class="qb-query-form-row">
      <a-form-item label="应用名称" class="qb-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.name" placeholder="请输入应用名称" />
      </a-form-item>
      <a-form-item label="状态" class="qb-query-form-item">
        <a-select style="width: 120px" v-model:value="queryForm.status" placeholder="请选择状态" allowClear>
          <a-select-option v-for="item in enumPlugin.getValueDescList('AI_APP_STATUS_ENUM')" :key="item.value" :value="item.value">
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
        <a-button @click="addOrUpdateApp" type="primary">
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
        <template v-if="column.dataIndex === 'icon'">
          <a-avatar v-if="record.icon" :src="record.icon" :size="32" />
          <a-avatar v-else icon="user" :size="32" />
        </template>
        <template v-else-if="column.dataIndex === 'type'">
          <a-tag :color="record.type === 'chat' ? 'blue' : 'green'">
            {{ getTypeDesc(record.type) }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusDesc(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="addOrUpdateApp(record)" type="link">编辑</a-button>
            <a-button @click="publishApp(record)" type="link" v-if="record.status === 'enable'">发布</a-button>
            <a-button @click="disableApp(record)" type="link" v-if="record.status === 'release'">禁用</a-button>
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

    <AppFormModal ref="appFormModalRef" @reloadList="ajaxQuery" />
  </a-card>
</template>

<script setup>
  import { onMounted, reactive, ref, getCurrentInstance } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { Loading } from '/@/components/framework/loading';
  import { aiAppApi } from '/@/api/ai/app-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import { SearchOutlined, ReloadOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import AppFormModal from './app-form-modal.vue';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  // 获取全局实例
  const internalInstance = getCurrentInstance();
  const enumPlugin = internalInstance.appContext.config.globalProperties.$enumPlugin;

  const tableRef = ref();
  const columns = ref([
    {
      title: '图标',
      width: 60,
      dataIndex: 'icon',
    },
    {
      title: '应用名称',
      dataIndex: 'name',
      minWidth: 150,
    },
    {
      title: '类型',
      dataIndex: 'type',
      width: 100,
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
    status: '',
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const tableLoading = ref(false);
  const selectedRowKeyList = ref([]);
  const tableData = ref([]);
  const total = ref(0);
  const appFormModalRef = ref();

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

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseData = await aiAppApi.getPage(queryForm);
      const list = responseData.data.records || [];
      total.value = responseData.data.total || 0;
      tableData.value = list;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // ----------------------- 发布/禁用 ------------------------
  async function publishApp(app) {
    Modal.confirm({
      title: '提示',
      content: `确定要发布应用"${app.name}"吗？`,
      okText: '确定',
      onOk: async () => {
        Loading.show();
        try {
          await aiAppApi.publish(app.id);
          message.success('发布成功');
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

  async function disableApp(app) {
    Modal.confirm({
      title: '提示',
      content: `确定要禁用应用"${app.name}"吗？`,
      okText: '确定',
      okType: 'danger',
      onOk: async () => {
        Loading.show();
        try {
          await aiAppApi.disable(app.id);
          message.success('禁用成功');
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
      content: '确定要删除选中的应用吗？',
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
      await aiAppApi.batchDelete(selectedRowKeyList.value);
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

  function addOrUpdateApp(rowData) {
    appFormModalRef.value.showModal(rowData);
  }

  // ---------------- 辅助函数 ----------------

  function getTypeDesc(type) {
    const desc = enumPlugin.getDescByValue('AI_APP_TYPE_ENUM', type);
    return desc || type;
  }

  function getStatusDesc(status) {
    const desc = enumPlugin.getDescByValue('AI_APP_STATUS_ENUM', status);
    return desc || status;
  }

  function getStatusColor(status) {
    const colorMap = {
      enable: 'green',
      disable: 'red',
      release: 'blue',
    };
    return colorMap[status] || 'default';
  }

  onMounted(ajaxQuery);
</script>
