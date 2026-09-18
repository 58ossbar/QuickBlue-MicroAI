<!--
  * 系统设置 列表
  *

-->
<template>
  <div>
    <a-form class="qb-query-form">
      <a-row class="qb-query-form-row">
        <a-form-item label="参数Key" class="qb-query-form-item">
          <a-input style="width: 300px" v-model:value="queryForm.configKey" placeholder="请输入key" />
        </a-form-item>

        <a-form-item class="qb-query-form-item qb-margin-left10">
          <a-button-group>
            <a-button type="primary" @click="onSearch" v-privilege="'support:config:query'">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>
            <a-button @click="resetQuery" v-privilege="'support:config:query'">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </a-button-group>
          <a-button @click="toEditOrAdd()" v-privilege="'support:config:add'" type="primary" class="qb-margin-left20">
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
        <TableOperator class="qb-margin-bottom5" v-model="columns" :tableId="TABLE_ID_CONST.SUPPORT.CONFIG" :refresh="ajaxQuery" />
      </a-row>

      <a-table ref="tableRef" size="small" :loading="tableLoading" bordered :dataSource="tableData" :columns="columns" rowKey="configId" :pagination="false">
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'action'">
            <div class="qb-table-operate">
              <a-button @click="toEditOrAdd(record)" v-privilege="'support:config:update'" type="link">编辑</a-button>
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
    <ConfigFormModal ref="configFormModal" @reloadList="resetQuery" />
  </div>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { configApi } from '/@/api/support/config-api';
  import ConfigFormModal from './config-form-modal.vue';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  const tableRef = ref();
  const columns = ref([
    {
      title: '参数key',
      dataIndex: 'configKey',
      ellipsis: true,
    },
    {
      title: '参数名称',
      dataIndex: 'configName',
      ellipsis: true,
    },
    {
      title: '参数值',
      dataIndex: 'configValue',
      ellipsis: true,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      ellipsis: true,
      width: 150,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 150,
    },
    {
      title: '修改时间',
      dataIndex: 'updateTime',
      width: 150,
    },

    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 60,
    },
  ]);

  useColumnResize(tableRef, columns);

  // ---------------- 查询数据 -----------------------

  const queryFormState = {
    configKey: '',
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
      let responseModel = await configApi.queryList(queryForm);
      const list = responseModel.data.list;
      total.value = responseModel.data.total;
      tableData.value = list;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // ------------------------- 表单操作 弹窗 ------------------------------

  const configFormModal = ref();
  function toEditOrAdd(rowData) {
    configFormModal.value.showModal(rowData);
  }

  onMounted(ajaxQuery);
</script>
