<template>
  <a-form class="qb-query-form">
    <a-row class="qb-query-form-row">
      <a-form-item label="关键字" class="qb-query-form-item">
        <a-input style="width: 300px" v-model:value.trim="queryForm.searchWord" placeholder="标题/内容" />
      </a-form-item>

      <a-form-item label="类型" class="qb-query-form-item">
        <enum-select style="width: 150px" v-model:value="queryForm.messageType" placeholder="消息类型" enum-name="MESSAGE_TYPE_ENUM" />
      </a-form-item>

      <a-form-item label="消息时间" class="qb-query-form-item">
        <a-space direction="vertical" :size="12">
          <a-range-picker v-model:value="searchDate" @change="dateChange" style="width: 220px" />
        </a-space>
      </a-form-item>

      <a-form-item label="已读" class="qb-query-form-item">
        <a-radio-group v-model:value="queryForm.readFlag" @change="quickQuery">
          <a-radio-button :value="null">全部</a-radio-button>
          <a-radio-button :value="false">未读</a-radio-button>
          <a-radio-button :value="true">已读</a-radio-button>
        </a-radio-group>
      </a-form-item>

      <a-form-item class="qb-query-form-item qb-margin-left10">
        <a-button-group>
          <a-button type="primary" @click="quickQuery">
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

  <a-table ref="tableRef" size="small" :dataSource="tableData" :columns="columns" rowKey="messageId" :pagination="false" bordered>
    <template #bodyCell="{ text, record, column }">
      <template v-if="column.dataIndex === 'messageType'">
        <span>{{ $enumPlugin.getDescByValue('MESSAGE_TYPE_ENUM', text) }}</span>
      </template>
      <template v-if="column.dataIndex === 'readFlag'">
        <span v-show="record.readFlag">已读</span>
        <span v-show="!record.readFlag" style="color: red">未读</span>
      </template>
      <template v-if="column.dataIndex === 'title'">
        <span v-show="record.readFlag">
          <a @click="toDetail(record)" style="color: #8c8c8c"
            >【{{ $enumPlugin.getDescByValue('MESSAGE_TYPE_ENUM', record.messageType) }}】{{ text }}</a
          >
        </span>
        <span v-show="!record.readFlag">
          <a @click="toDetail(record)">【{{ $enumPlugin.getDescByValue('MESSAGE_TYPE_ENUM', record.messageType) }}】{{ text }} </a>
        </span>
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

  <MessageDetail ref="messageDetailRef" @refresh="ajaxQuery" />
</template>
<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { messageApi } from '/@/api/support/message-api';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import EnumSelect from '/@/components/framework/enum-select//index.vue';
  import { sentry } from '/@/lib/sentry.js';
  import MessageDetail from './components/message-detail.vue';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  const tableRef = ref();
  const columns = reactive([
    {
      title: '消息',
      dataIndex: 'title',
    },
    {
      title: '已读',
      width: 80,
      dataIndex: 'readFlag',
    },
    {
      title: '时间',
      dataIndex: 'createTime',
      width: 180,
    },
  ]);

  useColumnResize(tableRef, columns);

  const queryFormState = {
    searchWord: '',
    messageType: null,
    dataId: null,
    readFlag: null,
    endDate: null,
    startDate: null,
    pageNum: 1,
    pageSize: PAGE_SIZE,
    searchCount: true,
    receiverType: null,
    receiverId: null,
  };
  const queryForm = reactive({ ...queryFormState });
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  // 日期选择
  let searchDate = ref();

  function dateChange(dates, dateStrings) {
    queryForm.startDate = dateStrings[0];
    queryForm.endDate = dateStrings[1];
  }

  function resetQuery() {
    searchDate.value = [];
    Object.assign(queryForm, queryFormState);
    ajaxQuery();
  }

  function quickQuery() {
    queryForm.pageNum = 1;
    ajaxQuery();
  }

  // 查询
  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseModel = await messageApi.queryMessage(queryForm);
      const list = responseModel.data.list;
      total.value = responseModel.data.total;
      tableData.value = list;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // -------------------- 详情 -----------------------------------

  const messageDetailRef = ref();

  function toDetail(message) {
    messageDetailRef.value.show(message);
  }

  onMounted(ajaxQuery);
</script>
