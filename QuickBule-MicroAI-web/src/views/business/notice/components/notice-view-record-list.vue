<!--
  * 通知  查看记录
  *

-->
<template>
  <div>
    <a-form class="qb-query-form">
      <a-row class="qb-query-form-row">
        <a-form-item label="部门" class="qb-query-form-item" style="width: 280px; margin-right: 20px">
          <DepartmentTreeSelect v-model:value="queryForm.departmentId" width="100%" />
        </a-form-item>
        <a-form-item label="关键字" class="qb-query-form-item" style="width: 280px">
          <a-input v-model:value="queryForm.keywords" placeholder="姓名/IP/设备" />
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
    <a-table rowKey="employeeId" :columns="tableColumns" :dataSource="tableData" :pagination="false" :loading="tableLoading" size="small" bordered>
      <template #bodyCell="{ column, record, text }">
        <template v-if="column.dataIndex === 'employeeName'"> {{ text }}({{ record.departmentName }}) </template>
        <template v-if="column.dataIndex === 'firstIp'"> {{ text }} ({{ record.firstDevice }}) </template>
        <template v-if="column.dataIndex === 'lastIp'"> {{ text }} ({{ record.lastDevice }}) </template>
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
        @change="queryViewRecord"
        :show-total="(total) => `共${total}条`"
      />
    </div>
  </div>
</template>
<script setup>
  import { reactive, ref } from 'vue';
  import { noticeApi } from '/src/api/business/notice/notice-api';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/src/constants/common-const';
  import DepartmentTreeSelect from '/src/components/system/department-tree-select/index.vue';
  import uaparser from 'ua-parser-js';

  const props = defineProps({
    noticeId: {
      type: [Number, String],
    },
  });

  defineExpose({
    onSearch,
  });

  const tableColumns = [
    {
      title: '姓名',
      dataIndex: 'employeeName',
    },
    {
      title: '查看次数',
      dataIndex: 'pageViewCount',
    },
    {
      title: '首次查看设备',
      dataIndex: 'firstIp',
    },
    {
      title: '首次查看时间',
      dataIndex: 'createTime',
    },
    {
      title: '最后一次查看设备',
      dataIndex: 'lastIp',
    },
    {
      title: '最后一次查看时间',
      dataIndex: 'updateTime',
      with: 80,
    },
  ];

  const tableData = ref([]);
  const total = ref(0);
  const tableLoading = ref(false);

  const defaultQueryForm = {
    noticeId: props.noticeId,
    departmentId: null,
    keywords: '',
    pageNum: 1,
    pageSize: PAGE_SIZE,
  };

  const queryForm = reactive({ ...defaultQueryForm });

  function buildDeviceInfo(userAgent) {
    if (!userAgent) {
      return '';
    }

    let ua = uaparser(userAgent);
    let browser = ua.browser.name;
    let os = ua.os.name;
    return browser + '/' + os + '/' + (ua.device.vendor ? ua.device.vendor + ua.device.model : '');
  }

  async function queryViewRecord() {
    try {
      tableLoading.value = true;
      const result = await noticeApi.queryViewRecord(queryForm);

      for (const e of result.data.list) {
        e.firstDevice = buildDeviceInfo(e.firstUserAgent);
        e.lastDevice = buildDeviceInfo(e.lastUserAgent);
      }

      tableData.value = result.data.list;
      total.value = result.data.total;
    } catch (err) {
      console.log(err);
    } finally {
      tableLoading.value = false;
    }
  }
  // 点击查询
  function onSearch() {
    queryForm.pageNum = 1;
    queryViewRecord();
  }

  // 点击重置
  function resetQuery() {
    Object.assign(queryForm, defaultQueryForm);
    queryViewRecord();
  }
</script>
