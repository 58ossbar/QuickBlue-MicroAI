<!--
  * 单号 生成记录
  *
-->
<template>
  <a-modal :open="visible" :title="'生成记录' + (businessName ? ' - ' + businessName : '')" width="70%" :footer="null" @cancel="onClose">
    <a-table ref="tableRef" size="small" :dataSource="tableData" :columns="columns" bordered rowKey="recordDate" :loading="tableLoading" :pagination="false">
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'lastNumber' || column.dataIndex === 'originalSequence'">
          <span v-if="text != null" class="code-text">{{ text }}</span>
          <span v-else class="table-empty">-</span>
        </template>

        <template v-else-if="column.dataIndex === 'lastTime'">
          <span v-if="text">{{ text }}</span>
          <span v-else class="table-empty">-</span>
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
  </a-modal>
</template>
<script setup>
  import { reactive, ref } from 'vue';
  import { serialNumberApi } from '/@/api/support/serial-number-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { sentry } from '/@/lib/sentry';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  defineExpose({
    showModal,
  });

  // ----------------------- 表单 隐藏 与 显示 ------------------------
  // 是否展示
  const visible = ref(false);
  const businessName = ref('');

  function showModal(data) {
    businessName.value = data.businessName || '';
    queryForm.serialNumberId = data.serialNumberId;
    queryForm.pageNum = 1;
    queryForm.pageSize = 10;
    ajaxQuery();
    visible.value = true;
  }

  function onClose() {
    visible.value = false;
  }

  // ----------------------- 表格 ------------------------
  const tableRef = ref();
  const columns = reactive([
    {
      title: '记录日期',
      dataIndex: 'recordDate',
      width: 120,
    },
    {
      title: '生成数量',
      dataIndex: 'count',
      width: 100,
    },
    {
      title: '最后更新值',
      dataIndex: 'lastNumber',
      width: 170,
    },
    {
      title: '原始序列号',
      dataIndex: 'originalSequence',
      width: 170,
    },
    {
      title: '上次生成时间',
      dataIndex: 'lastTime',
      width: 180,
    },
  ]);

  useColumnResize(tableRef, columns);

  const queryForm = reactive({
    serialNumberId: -1,
    pageNum: 1,
    pageSize: 10,
  });

  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseModel = await serialNumberApi.queryRecord(queryForm);
      const list = responseModel.data.list;
      total.value = responseModel.data.total;
      tableData.value = list;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }
</script>
<style lang="less" scoped>
  .code-text {
    font-family: 'JetBrains Mono', Consolas, Monaco, 'Courier New', monospace;
    font-size: 12px;
  }

  .table-empty {
    color: rgba(0, 0, 0, 0.25);
  }
</style>
