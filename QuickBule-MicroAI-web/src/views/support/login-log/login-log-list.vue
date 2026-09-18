<!--
  * 登录、登出 日志
  *

-->
<template>
  <!---------- 等保二级必备标注 begin ---------->
  <div style="margin-bottom: 8px;">
    <a-tag color="green" style="margin-right: 8px; font-weight: 600;">二级必备</a-tag>
    <span style="color: rgba(0,0,0,0.45); font-size: 12px;">依据 GB/T 22239-2019《网络安全等级保护基本要求》二级要求，本功能为等保二级核查项</span>
  </div>
  <!---------- 等保二级必备标注 end ---------->
  <a-form class="qb-query-form" v-privilege="'support:loginLog:query'" ref="queryFormRef">
    <a-row class="qb-query-form-row">
      <a-form-item label="用户名称" class="qb-query-form-item">
        <a-input style="width: 300px" v-model:value="queryForm.userName" placeholder="用户名称" />
      </a-form-item>

      <a-form-item label="用户IP" class="qb-query-form-item">
        <a-input style="width: 120px" v-model:value="queryForm.ip" placeholder="IP" />
      </a-form-item>

      <a-form-item label="时间" class="qb-query-form-item">
        <a-range-picker @change="changeCreateDate" v-model:value="createDateRange" :presets="defaultChooseTimeRange" style="width: 240px" />
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
    <a-row justify="end" ref="tableOperatorRef">
      <TableOperator class="qb-margin-bottom5" v-model="columns" :tableId="TABLE_ID_CONST.SUPPORT.LOGIN_LOG" :refresh="ajaxQuery" />
    </a-row>
    <a-table
      ref="tableRef"
      size="small"
      :dataSource="tableData"
      :columns="columns"
      bordered
      rowKey="loginLogId"
      :pagination="false"
      :loading="tableLoading"
      :scroll="{ y: scrollY }"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'loginResult'">
          <template v-if="text === LOGIN_RESULT_ENUM.LOGIN_SUCCESS.value">
            <a-tag color="success">登录成功</a-tag>
          </template>
          <template v-if="text === LOGIN_RESULT_ENUM.LOGIN_FAIL.value">
            <a-tag color="error">登录失败</a-tag>
          </template>
          <template v-if="text === LOGIN_RESULT_ENUM.LOGIN_OUT.value">
            <a-tag color="processing">退出登录</a-tag>
          </template>
        </template>

        <template v-if="column.dataIndex === 'userAgent'">
          <div>{{ record.browser }} / {{ record.os }} / {{ record.device }}</div>
        </template>

        <template v-if="column.dataIndex === 'userType'">
          <span>{{ $enumPlugin.getDescByValue('USER_TYPE_ENUM', text) }}</span>
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
</template>
<script setup>
  import { onMounted, onUnmounted, reactive, ref } from 'vue';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { defaultTimeRanges } from '/@/lib/default-time-ranges';
  import uaparser from 'ua-parser-js';
  import { LOGIN_RESULT_ENUM } from '/@/constants/support/login-log-const';
  import { loginLogApi } from '/@/api/support/login-log-api';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { calcTableHeight } from '/@/lib/table-auto-height';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  const tableRef = ref();
  const columns = ref([
    {
      title: '用户名',
      dataIndex: 'userName',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'userType',
      width: 50,
      ellipsis: true,
    },
    {
      title: 'IP',
      dataIndex: 'loginIp',
      ellipsis: true,
    },
    {
      title: 'IP地区',
      dataIndex: 'loginIpRegion',
      ellipsis: true,
      customRender: ({ text }) => formatIpRegion(text),
    },
    {
      title: '设备信息',
      dataIndex: 'userAgent',
      ellipsis: true,
    },
    {
      title: '结果',
      dataIndex: 'loginResult',
      ellipsis: true,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      ellipsis: true,
    },
    {
      title: '时间',
      dataIndex: 'createTime',
      width: 150,
    },
  ]);

  useColumnResize(tableRef, columns);

  const queryFormState = {
    userName: '',
    ip: '',
    startDate: undefined,
    endDate: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const createDateRange = ref([]);
  const defaultChooseTimeRange = defaultTimeRanges;
  // 时间变动
  function changeCreateDate(dates, dateStrings) {
    queryForm.startDate = dateStrings[0];
    queryForm.endDate = dateStrings[1];
  }

  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    createDateRange.value = [];
    ajaxQuery();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    ajaxQuery();
  }

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseModel = await loginLogApi.queryList(queryForm);

      const dataList = responseModel.data.dataList || [];
      for (const e of dataList) {
        if (!e.userAgent) {
          continue;
        }
        let ua = uaparser(e.userAgent);
        e.browser = ua.browser.name;
        e.os = ua.os.name;
        e.device = ua.device.vendor ? ua.device.vendor + ua.device.model : '';
      }

      total.value = responseModel.data.total;
      tableData.value = dataList;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // ----------------- 表格自适应高度 --------------------
  const scrollY = ref(100);
  const tableOperatorRef = ref();
  const queryFormRef = ref();

  function autoCalcTableHeight() {
    calcTableHeight(scrollY, [tableOperatorRef, queryFormRef], 10);
  }

  window.addEventListener('resize', autoCalcTableHeight);

  onMounted(() => {
    ajaxQuery();
    autoCalcTableHeight();
  });

  onUnmounted(() => {
    window.removeEventListener('resize', autoCalcTableHeight);
  });

  // 格式化IP地区显示
  function formatIpRegion(ipRegion) {
    if (!ipRegion || ipRegion === '0|0|0|内网IP|内网IP' || ipRegion === '0|0|0|内网IP') {
      return '未知地区';
    }

    // 处理格式：国家|省份|城市|网络运营商
    const parts = ipRegion.split('|');
    if (parts.length < 4) {
      return ipRegion;
    }

    const country = parts[0] || '';
    const province = parts[1] || '';
    const city = parts[2] || '';
    const isp = parts[3] || '';

    // 如果国家是0，说明无法识别
    if (country === '0') {
      return '未知地区';
    }

    // 拼接地区信息
    let result = '';
    if (country && country !== '0') {
      result += country;
    }
    if (province && province !== '0') {
      result += (result ? '·' : '') + province;
    }
    if (city && city !== '0') {
      result += (result ? '·' : '') + city;
    }
    if (isp && isp !== '0' && isp !== '内网IP') {
      result += (result ? ' (' : '') + isp + ')';
    }

    return result || '未知地区';
  }
</script>
