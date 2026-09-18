<!--
  * 通知  详情 （员工列表）
  *

-->
<template>
  <a-form class="qb-query-form">
    <a-row class="qb-query-form-row">
      <a-form-item label="关键字" class="qb-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.keywords" placeholder="标题、作者、来源、文号" />
      </a-form-item>

      <a-form-item label="发布时间" class="qb-query-form-item">
        <a-range-picker v-model:value="publishDate" @change="publishDateChange" style="width: 220px" />
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
    <a-row class="qb-query-form-row" />
  </a-form>

  <a-card size="small" :bordered="false">
    <a-tabs @change="changeNotViewFlag" size="small">
      <a-tab-pane :key="0" tab="全部" />
      <a-tab-pane :key="1" tab="未读" />
    </a-tabs>

    <a-table ref="tableRef" rowKey="noticeId" :columns="tableColumns" :dataSource="tableData" :pagination="false" :loading="tableLoading" bordered size="small">
      <template #bodyCell="{ column, record, text }">
        <template v-if="column.dataIndex === 'title'">
          <span v-show="record.viewFlag">
            <a @click="viewDetail(record.noticeId)" style="color: #8c8c8c">【{{ record.noticeTypeName }}】{{ text }}（已读）</a>
          </span>
          <span v-show="!record.viewFlag">
            <a @click="viewDetail(record.noticeId)"
              >【{{ record.noticeTypeName }}】{{ text }}
              <span style="color: red">（未读）</span>
            </a>
          </span>
        </template>
        <template v-if="column.dataIndex === 'pageViewCount'"> {{ record.userViewCount }}人 / {{ record.pageViewCount }}次 </template>
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

  <NoticeEmployeeDetailDrawer ref="noticeDetailDrawer" @refreshList="queryNoticeList" />
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/src/constants/common-const';
  import { noticeApi } from '/src/api/business/notice/notice-api';
  import { sentry } from '/src/lib/sentry';
  import NoticeEmployeeDetailDrawer from './components/notice-employee-detail-drawer.vue';
  import { useColumnResize } from '/src/hooks/useColumnResize';

  const tableRef = ref();
  const tableColumns = reactive([
    {
      title: `标题`,
      dataIndex: 'title',
      width: 250,
    },
    {
      title: '访问量',
      dataIndex: 'pageViewCount',
      width: 90,
    },
    {
      title: `来源`,
      dataIndex: 'source',
      width: 150,
      ellipsis: true,
    },
    {
      title: `作者`,
      dataIndex: 'author',
      width: 80,
      ellipsis: true,
    },
    {
      title: '发布时间',
      dataIndex: 'publishTime',
      width: 120,
    },
  ]);

  useColumnResize(tableRef, tableColumns);

  // ------------------ 查询相关 ------------------

  const queryFormState = {
    noticeTypeId: undefined, //分类
    keywords: '', //标题、作者、来源
    publishTimeBegin: null, //发布-开始时间
    publishTimeEnd: null, //发布-截止时间
    notViewFlag: false, //未读
    pageNum: 1,
    pageSize: PAGE_SIZE,
  };
  const queryForm = reactive({ ...queryFormState });

  const tableData = ref([]);
  const total = ref(0);
  const tableLoading = ref(false);

  onMounted(() => {
    queryNoticeList();
  });

  function changeNotViewFlag(value) {
    queryForm.notViewFlag = value === 0 ? null : true;
    onSearch();
  }

  // 查询列表
  async function queryNoticeList() {
    try {
      tableLoading.value = true;
      const result = await noticeApi.queryEmployeeNotice(queryForm);
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
    queryNoticeList();
  }

  // 发布日期选择
  const publishDate = ref([]);
  function publishDateChange(dates, dateStrings) {
    queryForm.publishTimeBegin = dateStrings[0];
    queryForm.publishTimeEnd = dateStrings[1];
  }

  // ------------------ 详情 ------------------

  // 查看详情
  const noticeDetailDrawer = ref();
  function viewDetail(noticeId) {
    noticeDetailDrawer.value.showDetail(noticeId);
  }
</script>

<style lang="less" scoped></style>
