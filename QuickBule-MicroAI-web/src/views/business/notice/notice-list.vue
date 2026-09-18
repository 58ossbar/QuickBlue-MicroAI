<!--
  * 通知  管理列表
  *

-->

<template>
  <a-form class="qb-query-form" v-privilege="'oa:notice:query'">
    <a-row class="qb-query-form-row">
      <a-form-item label="分类" class="qb-query-form-item">
        <a-select v-model:value="queryForm.noticeTypeId" style="width: 100px" :showSearch="true" :allowClear="true" placeholder="分类">
          <a-select-option v-for="item in noticeTypeList" :key="item.noticeTypeId" :value="item.noticeTypeId">
            {{ item.noticeTypeName }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="关键字" class="qb-query-form-item">
        <a-input style="width: 300px" v-model:value="queryForm.keywords" placeholder="标题、作者、来源" />
      </a-form-item>

      <a-form-item label="文号" class="qb-query-form-item">
        <a-input style="width: 150px" v-model:value="queryForm.documentNumber" placeholder="文号" />
      </a-form-item>

      <a-form-item label="创建人" class="qb-query-form-item">
        <a-input style="width: 100px" v-model:value="queryForm.createUserName" placeholder="创建人" />
      </a-form-item>

      <a-form-item label="是否删除" class="qb-query-form-item">
        <BooleanSelect v-model:value="queryForm.deletedFlag" style="width: 70px" />
      </a-form-item>

      <a-form-item label="发布时间" class="qb-query-form-item">
        <a-range-picker v-model:value="publishDate" :presets="defaultTimeRanges" @change="publishDateChange" style="width: 220px" />
      </a-form-item>

      <a-form-item label="创建时间" class="qb-query-form-item">
        <a-range-picker v-model:value="createDate" :presets="defaultTimeRanges" @change="createDateChange" style="width: 220px" />
      </a-form-item>

      <a-form-item class="qb-query-form-item qb-margin-left10">
        <a-button-group>
          <a-button type="primary" @click="onSearch">
            <template #icon>
              <SearchOutlined />
            </template>
            查询
          </a-button>
          <a-button @click="onReload">
            <template #icon>
              <ReloadOutlined />
            </template>
            重置
          </a-button>
        </a-button-group>
      </a-form-item>
    </a-row>
  </a-form>

  <a-card size="small" :bordered="false">
    <a-row class="qb-table-btn-block">
      <div class="qb-table-operate-block">
        <a-button type="primary" @click="addOrUpdate()" v-privilege="'oa:notice:add'">
          <template #icon>
            <PlusOutlined />
          </template>
          新建
        </a-button>
      </div>
      <div class="qb-table-setting-block">
        <TableOperator v-model="tableColumns" :tableId="TABLE_ID_CONST.BUSINESS.OA.NOTICE" :refresh="queryNoticeList" />
      </div>
    </a-row>

    <a-table
      ref="tableRef"
      rowKey="noticeId"
      :columns="tableColumns"
      :dataSource="tableData"
      :scroll="{ x: 1210 }"
      :pagination="false"
      :loading="tableLoading"
      size="small"
      bordered
    >
      <template #bodyCell="{ column, record, text }">
        <template v-if="column.dataIndex === 'title'">
          <a @click="viewDetail(record.noticeId)">{{ text }}</a>
        </template>
        <template v-if="column.dataIndex === 'documentNumber'">
          {{ text ? text : '无' }}
        </template>
        <template v-else-if="column.dataIndex === 'allVisibleFlag'"> {{ text ? '全部可见' : '部分可见' }} </template>
        <template v-else-if="column.dataIndex === 'publishFlag'">
          {{ text ? '已发布' : '待发布' }}
        </template>
        <template v-else-if="column.dataIndex === 'deletedFlag'">
          <a-tag v-show="text" color="error">已删除</a-tag>
          <a-tag v-show="!text" color="success">未删除</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <div class="qb-table-operate" v-if="!record.deletedFlag">
            <a-button type="link" @click="addOrUpdate(record.noticeId)" v-privilege="'oa:notice:update'">编辑</a-button>
            <a-button type="link" @click="onDelete(record.noticeId)" v-privilege="'oa:notice:delete'" danger>删除</a-button>
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
        @change="queryNoticeList"
        :show-total="(total) => `共${total}条`"
      />
    </div>
  </a-card>

  <NoticeFormModal ref="noticeFormDrawer" @reloadList="queryNoticeList" />
  <NoticeDetailDrawer ref="noticeDetailDrawer" />
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/src/constants/common-const';
  import BooleanSelect from '/src/components/framework/boolean-select/index.vue';
  import { noticeApi } from '/src/api/business/notice/notice-api';
  import NoticeFormModal from './components/notice-form-modal.vue';
  import { defaultTimeRanges } from '/src/lib/default-time-ranges';
  import { sentry } from '/src/lib/sentry';
  import TableOperator from '/src/components/support/table-operator/index.vue';
  import { useColumnResize } from '/src/hooks/useColumnResize';
  import { TABLE_ID_CONST } from '/src/constants/support/table-id-const';
  import NoticeDetailDrawer from './components/notice-detail-drawer.vue';

  const queryFormState = {
    noticeTypeId: undefined, //分类
    keywords: '', //标题、作者、来源
    documentNumber: '', //文号
    createUserName: undefined, //创建人
    deletedFlag: undefined, //删除标识
    createTimeBegin: null, //创建-开始时间
    createTimeEnd: null, //创建-截止时间
    publishTimeBegin: null, //发布-开始时间
    publishTimeEnd: null, //发布-截止时间
    pageNum: 1,
    pageSize: PAGE_SIZE,
  };
  const queryForm = reactive({ ...queryFormState });

  const tableRef = ref();
  const tableColumns = ref([
    {
      title: `标题`,
      dataIndex: 'title',
      width: 300,
      ellipsis: true,
    },
    {
      title: `文号`,
      dataIndex: 'documentNumber',
      width: 100,
      ellipsis: true,
    },
    {
      title: '分类',
      dataIndex: 'noticeTypeName',
      width: 60,
      ellipsis: true,
    },
    {
      title: `作者`,
      dataIndex: 'author',
      width: 80,
      ellipsis: true,
    },
    {
      title: `来源`,
      dataIndex: 'source',
      width: 90,
      ellipsis: true,
    },

    {
      title: '可见范围',
      dataIndex: 'allVisibleFlag',
      width: 90,
      ellipsis: true,
    },
    {
      title: '发布状态',
      dataIndex: 'publishFlag',
      width: 80,
    },
    {
      title: '删除',
      dataIndex: 'deletedFlag',
      width: 80,
    },
    {
      title: '发布时间',
      dataIndex: 'publishTime',
      width: 150,
    },
    {
      title: '页面浏览量',
      dataIndex: 'pageViewCount',
      width: 90,
    },
    {
      title: '用户浏览量',
      dataIndex: 'userViewCount',
      width: 90,
    },
    {
      title: '创建人',
      dataIndex: 'createUserName',
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
      width: 90,
    },
  ]);

  useColumnResize(tableRef, tableColumns);

  // ------------------ 通知分类 ------------------

  // 查询分类列表
  const noticeTypeList = ref([]);
  async function queryNoticeTypeList() {
    try {
      const result = await noticeApi.getAllNoticeTypeList();
      noticeTypeList.value = result.data;
    } catch (err) {
      sentry.captureError(err);
    }
  }

  // ------------------ 查询相关 ------------------

  const tableData = ref([]);
  const total = ref(0);
  const tableLoading = ref(false);

  onMounted(() => {
    queryNoticeTypeList();
    queryNoticeList();
  });

  // 查询列表
  async function queryNoticeList() {
    try {
      tableLoading.value = true;
      const result = await noticeApi.queryNotice(queryForm);
      tableData.value = result.data.list;
      total.value = result.data.total;
    } catch (err) {
      sentry.captureError(err);
    } finally {
      tableLoading.value = false;
    }
  }

  // 点击查询
  function onSearch() {
    queryForm.pageNum = 1;
    queryNoticeList();
  }

  // 点击重置
  function onReload() {
    Object.assign(queryForm, queryFormState);
    publishDate.value = [];
    createDate.value = [];
    queryNoticeList();
  }

  // 发布日期选择
  const publishDate = ref([]);
  function publishDateChange(dates, dateStrings) {
    queryForm.publishTimeBegin = dateStrings[0];
    queryForm.publishTimeEnd = dateStrings[1];
  }
  // 创建日期选择
  const createDate = ref([]);
  function createDateChange(dates, dateStrings) {
    queryForm.createTimeBegin = dateStrings[0];
    queryForm.createTimeEnd = dateStrings[1];
  }

  // ------------------ 新建、编辑 ------------------

  // 新建、编辑
  const noticeFormDrawer = ref();
  function addOrUpdate(noticeId) {
    noticeFormDrawer.value.showModal(noticeId);
  }

  // ------------------ 删除 ------------------

  // 删除
  function onDelete(noticeId) {
    Modal.confirm({
      title: '提示',
      content: '确认删除此数据吗?',
      onOk() {
        deleteNotice(noticeId);
      },
    });
  }

  // 删除API
  async function deleteNotice(noticeId) {
    try {
      tableLoading.value = true;
      await noticeApi.deleteNotice(noticeId);
      message.success('删除成功');
      queryNoticeList();
    } catch (err) {
      sentry.captureError(err);
    } finally {
      tableLoading.value = false;
    }
  }

  // ------------------ 详情 ------------------

  // 查看详情
  const noticeDetailDrawer = ref();
  function viewDetail(noticeId) {
    noticeDetailDrawer.value.showDetail(noticeId);
  }
</script>

<style lang="less" scoped></style>
