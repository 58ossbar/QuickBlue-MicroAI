<!--
  * 数据权限配置 列表
  *
-->
<template>
  <div>
    <a-form class="qb-query-form">
      <a-row class="qb-query-form-row">
        <a-form-item label="配置名称" class="qb-query-form-item">
          <a-input style="width: 200px" v-model:value="queryForm.configName" placeholder="请输入配置名称" />
        </a-form-item>
        <a-form-item label="配置编码" class="qb-query-form-item">
          <a-input style="width: 200px" v-model:value="queryForm.configCode" placeholder="请输入配置编码" />
        </a-form-item>
        <a-form-item label="业务模块" class="qb-query-form-item">
          <a-input style="width: 150px" v-model:value="queryForm.businessModule" placeholder="请输入业务模块" />
        </a-form-item>
        <a-form-item label="状态" class="qb-query-form-item">
          <a-select style="width: 100px" v-model:value="queryForm.status" placeholder="请选择" allowClear>
            <a-select-option :value="true">启用</a-select-option>
            <a-select-option :value="false">禁用</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item class="qb-query-form-item qb-margin-left10">
          <a-button-group>
            <a-button type="primary" @click="onSearch" v-privilege="'system:dataScopeConfig:query'">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>
            <a-button @click="resetQuery" v-privilege="'system:dataScopeConfig:query'">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </a-button-group>
          <a-button @click="showForm()" v-privilege="'system:dataScopeConfig:add'" type="primary" class="qb-margin-left20">
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>

    <a-card size="small" :bordered="false" :hoverable="true">
      <a-row justify="end">
        <TableOperator class="qb-margin-bottom5" v-model="columns" :tableId="TABLE_ID_CONST.SYSTEM.DATA_SCOPE_CONFIG" :refresh="ajaxQuery" />
      </a-row>

      <a-table
        ref="tableRef"
        size="small"
        :loading="tableLoading"
        bordered
        :dataSource="tableData"
        :columns="columns"
        rowKey="configId"
        :pagination="false"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      >
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'defaultViewType'">
            <a-tag :color="getViewTypeColor(record.defaultViewType)">
              {{ getViewTypeName(record.defaultViewType) }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="record.status ? 'processing' : 'error'">
              {{ record.status ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <div class="qb-table-operate">
              <a-button @click="showForm(record)" v-privilege="'system:dataScopeConfig:update'" type="link">编辑</a-button>
              <a-button @click="updateStatus(record.configId, record.status)" v-privilege="'system:dataScopeConfig:updateStatus'" type="link">
                {{ record.status ? '禁用' : '启用' }}
              </a-button>
              <a-button @click="deleteOne(record.configId)" v-privilege="'system:dataScopeConfig:delete'" type="link" danger>删除</a-button>
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
    </a-card>

    <DataScopeConfigForm ref="formRef" @reloadList="resetQuery" />

    <!-- 批量删除按钮 -->
    <div class="qb-batch-operate" v-if="selectedRowKeys.length > 0">
      <a-button @click="batchDelete" v-privilege="'system:dataScopeConfig:delete'" type="primary" danger>
        批量删除 ({{ selectedRowKeys.length }})
      </a-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, createVNode } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { dataScopeConfigApi } from '/@/api/system/data-scope-config-api';
import DataScopeConfigForm from './data-scope-config-form.vue';
import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import { sentry } from '/@/lib/sentry';
import { Loading } from '/@/components/framework/loading';
import TableOperator from '/@/components/support/table-operator/index.vue';
import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
import { useColumnResize } from '/@/hooks/useColumnResize';

// 表格列定义
const tableRef = ref();
const columns = ref([
  {
    title: '配置名称',
    dataIndex: 'configName',
    ellipsis: true,
    width: 150,
  },
  {
    title: '配置编码',
    dataIndex: 'configCode',
    ellipsis: true,
    width: 150,
  },
  {
    title: '业务模块',
    dataIndex: 'businessModule',
    ellipsis: true,
    width: 120,
  },
  {
    title: '默认视图类型',
    dataIndex: 'defaultViewType',
    width: 120,
  },
  {
    title: '配置描述',
    dataIndex: 'configDesc',
    ellipsis: true,
    width: 200,
  },
  {
    title: '排序',
    dataIndex: 'sortOrder',
    width: 80,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
  },
  {
    title: '操作',
    dataIndex: 'action',
    fixed: 'right',
    width: 150,
  },
]);

useColumnResize(tableRef, columns);

// 视图类型名称（与 DataScopeViewTypeEnum 保持一致）
function getViewTypeName(viewType) {
  const typeMap = {
    0: '仅本人',
    1: '部门',
    2: '本部门及以下',
    3: '全部数据',
  };
  return typeMap[viewType] || '-';
}

// 视图类型颜色
function getViewTypeColor(viewType) {
  const colorMap = {
    0: 'purple',
    1: 'orange',
    2: 'green',
    3: 'blue',
  };
  return colorMap[viewType] || 'default';
}

// ---------------- 查询数据 -----------------------

const queryFormState = {
  configName: '',
  configCode: '',
  businessModule: '',
  status: undefined,
  pageNum: 1,
  pageSize: 10,
};
const queryForm = reactive({ ...queryFormState });

const tableLoading = ref(false);
const tableData = ref([]);
const total = ref(0);

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
    let responseModel = await dataScopeConfigApi.queryPage(queryForm);
    const list = responseModel.data.list;
    total.value = responseModel.data.total;
    tableData.value = list;
    // 清除选中
    selectedRowKeys.value = [];
  } catch (e) {
    sentry.captureError(e);
  } finally {
    tableLoading.value = false;
  }
}

// ------------------------- 表单操作 弹窗 ------------------------------

const formRef = ref();

function showForm(rowData) {
  formRef.value.show(rowData);
}

// ------------------------- 删除操作 ------------------------------

async function deleteOne(configId) {
  Modal.confirm({
    title: '确定要删除吗？',
    icon: createVNode(ExclamationCircleOutlined),
    content: '删除后数据将无法恢复',
    okText: '删除',
    okType: 'danger',
    async onOk() {
      Loading.show();
      try {
        await dataScopeConfigApi.delete(configId);
        message.success('删除成功');
        ajaxQuery();
      } catch (e) {
        sentry.captureError(e);
      } finally {
        Loading.hide();
      }
    },
    cancelText: '取消',
  });
}

// ------------------------- 批量删除 ------------------------------

const selectedRowKeys = ref([]);

function onSelectChange(keys) {
  selectedRowKeys.value = keys;
}

async function batchDelete() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的数据');
    return;
  }

  Modal.confirm({
    title: '确定要批量删除吗？',
    icon: createVNode(ExclamationCircleOutlined),
    content: `已选择 ${selectedRowKeys.value.length} 条数据，删除后数据将无法恢复`,
    okText: '删除',
    okType: 'danger',
    async onOk() {
      Loading.show();
      try {
        await dataScopeConfigApi.batchDelete(selectedRowKeys.value);
        message.success('删除成功');
        ajaxQuery();
      } catch (e) {
        sentry.captureError(e);
      } finally {
        Loading.hide();
      }
    },
    cancelText: '取消',
  });
}

// ------------------------- 启用/禁用 ------------------------------

async function updateStatus(configId, status) {
  Modal.confirm({
    title: '提醒',
    icon: createVNode(ExclamationCircleOutlined),
    content: `确定要${status ? '禁用' : '启用'}吗？`,
    okText: '确定',
    okType: 'danger',
    async onOk() {
      Loading.show();
      try {
        await dataScopeConfigApi.updateStatus(configId);
        message.success(`${status ? '禁用' : '启用'}成功`);
        ajaxQuery();
      } catch (e) {
        sentry.captureError(e);
      } finally {
        Loading.hide();
      }
    },
    cancelText: '取消',
  });
}

onMounted(ajaxQuery);
</script>

<style scoped lang="less">
.qb-batch-operate {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  background: #fff;
  padding: 10px 20px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
</style>
