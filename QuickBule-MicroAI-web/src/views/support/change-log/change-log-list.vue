<!--
  * 系统更新日志
  *

-->
<template>
  <!---------- 查询表单form begin ----------->
  <a-form class="qb-query-form" v-privilege="'support:changeLog:query'">
    <a-row class="qb-query-form-row">
      <a-form-item label="更新类型" class="qb-query-form-item">
        <EnumSelect width="200px" v-model:value="queryForm.type" enumName="CHANGE_LOG_TYPE_ENUM" placeholder="更新类型" />
      </a-form-item>
      <a-form-item label="关键字" class="qb-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.keyword" placeholder="关键字" />
      </a-form-item>
      <a-form-item label="发布日期" class="qb-query-form-item">
        <a-range-picker v-model:value="queryForm.publicDate" :presets="defaultTimeRanges" style="width: 240px" @change="onChangePublicDate" />
      </a-form-item>
      <a-form-item label="创建时间" class="qb-query-form-item">
        <a-date-picker valueFormat="YYYY-MM-DD" v-model:value="queryForm.createTime" style="width: 150px" />
      </a-form-item>
      <a-form-item class="qb-query-form-item">
        <a-button-group>
          <a-button type="primary" @click="onSearch">
            <template #icon>
              <SearchOutlined />
            </template>
            查询
          </a-button>
          <a-button @click="resetQuery" class="qb-margin-left10">
            <template #icon>
              <ReloadOutlined />
            </template>
            重置
          </a-button>
        </a-button-group>
      </a-form-item>
    </a-row>
  </a-form>
  <!---------- 查询表单form end ----------->

  <a-card size="small" :bordered="false" :hoverable="true">
    <!---------- 表格操作行 begin ----------->
    <a-row class="qb-table-btn-block">
      <div class="qb-table-operate-block">
        <a-button @click="showForm" type="primary" v-privilege="'support:changeLog:add'">
          <template #icon>
            <PlusOutlined />
          </template>
          新建
        </a-button>
        <a-button @click="confirmBatchDelete" danger :disabled="selectedRowKeyList.length === 0" v-privilege="'support:changeLog:batchDelete'">
          <template #icon>
            <DeleteOutlined />
          </template>
          批量删除
        </a-button>
      </div>
      <div class="qb-table-setting-block">
        <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.SUPPORT.CHANGE_LOG" :refresh="queryData" />
      </div>
    </a-row>
    <!---------- 表格操作行 end ----------->

    <!---------- 表格 begin ----------->
    <a-table
      ref="tableRef"
      size="small"
      :dataSource="tableData"
      :columns="columns"
      rowKey="changeLogId"
      bordered
      :pagination="false"
      :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'updateVersion'">
          <a-button @click="showModal(record)" type="link">{{ text }}</a-button>
        </template>
        <template v-if="column.dataIndex === 'type'">
          <a-tag color="success">
            <template #icon>
              <check-circle-outlined />
            </template>
            {{ $enumPlugin.getDescByValue('CHANGE_LOG_TYPE_ENUM', text) }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="showForm(record)" type="link" v-privilege="'support:changeLog:update'">编辑</a-button>
            <a-button @click="onDelete(record)" danger type="link" v-privilege="'support:changeLog:delete'">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>
    <!---------- 表格 end ----------->

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
        @change="queryData"
        :show-total="(total) => `共${total}条`"
      />
    </div>

    <ChangeLogForm ref="formRef" @reloadList="queryData" />

    <ChangeLogModal ref="modalRef" />
  </a-card>
</template>
<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { Loading } from '/@/components/framework/loading';
  import { changeLogApi } from '/@/api/support/change-log-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import EnumSelect from '/@/components/framework/enum-select/index.vue';
  import { defaultTimeRanges } from '/@/lib/default-time-ranges';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import ChangeLogModal from './change-log-modal.vue';
  import ChangeLogForm from './change-log-form.vue';
  import { useColumnResize } from '/@/hooks/useColumnResize';
  // ---------------------------- 表格列 ----------------------------

  const tableRef = ref();
  const columns = ref([
    {
      title: '版本',
      dataIndex: 'updateVersion',
      ellipsis: true,
    },
    {
      title: '更新类型',
      dataIndex: 'type',
      ellipsis: true,
    },
    {
      title: '发布人',
      dataIndex: 'publishAuthor',
      ellipsis: true,
    },
    {
      title: '发布日期',
      dataIndex: 'publicDate',
      ellipsis: true,
    },
    {
      title: '更新内容',
      dataIndex: 'content',
      ellipsis: true,
    },
    {
      title: '跳转链接',
      dataIndex: 'link',
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      ellipsis: true,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      ellipsis: true,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 90,
    },
  ]);

  useColumnResize(tableRef, columns);

  // ---------------------------- 查询数据表单和方法 ----------------------------

  const queryFormState = {
    type: undefined, //更新类型:[1:特大版本功能更新;2:功能更新;3:bug修复]
    keyword: undefined, //关键字
    publicDate: [], //发布日期
    publicDateBegin: undefined, //发布日期 开始
    publicDateEnd: undefined, //发布日期 结束
    createTime: undefined, //创建时间
    link: undefined, //跳转链接
    pageNum: 1,
    pageSize: 10,
  };
  // 查询表单form
  const queryForm = reactive({ ...queryFormState });
  // 表格数据
  const tableData = ref([]);
  // 总数
  const total = ref(0);

  // 重置查询条件
  function resetQuery() {
    let pageSize = queryForm.pageSize;
    Object.assign(queryForm, queryFormState);
    queryForm.pageSize = pageSize;
    queryData();
  }

  // 搜索
  function onSearch() {
    queryForm.pageNum = 1;
    queryData();
  }

  // 查询数据
  async function queryData() {
    try {
      let queryResult = await changeLogApi.queryPage(queryForm);
      tableData.value = queryResult.data.dataList;
      total.value = queryResult.data.total;
    } catch (e) {
      sentry.captureError(e);
    }
  }

  function onChangePublicDate(dates, dateStrings) {
    queryForm.publicDateBegin = dateStrings[0];
    queryForm.publicDateEnd = dateStrings[1];
  }

  onMounted(queryData);

  // ---------------------------- 查看 ----------------------------
  const modalRef = ref();

  function showModal(data) {
    modalRef.value.show(data);
  }

  // ---------------------------- 添加/修改 ----------------------------
  const formRef = ref();

  function showForm(data) {
    formRef.value.show(data);
  }

  // ---------------------------- 单个删除 ----------------------------
  //确认删除
  function onDelete(data) {
    Modal.confirm({
      title: '提示',
      content: '确定要删除选吗?',
      okText: '删除',
      okType: 'danger',
      onOk() {
        requestDelete(data);
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  //请求删除
  async function requestDelete(data) {
    Loading.show();
    try {
      await changeLogApi.delete(data.changeLogId);
      message.success('删除成功');
      queryData();
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }

  // ---------------------------- 批量删除 ----------------------------

  // 选择表格行
  const selectedRowKeyList = ref([]);

  function onSelectChange(selectedRowKeys) {
    selectedRowKeyList.value = selectedRowKeys;
  }

  // 批量删除
  function confirmBatchDelete() {
    Modal.confirm({
      title: '提示',
      content: '确定要批量删除这些数据吗?',
      okText: '删除',
      okType: 'danger',
      onOk() {
        requestBatchDelete();
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  //请求批量删除
  async function requestBatchDelete() {
    try {
      Loading.show();
      await changeLogApi.batchDelete(selectedRowKeyList.value);
      message.success('删除成功');
      queryData();
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }
</script>
