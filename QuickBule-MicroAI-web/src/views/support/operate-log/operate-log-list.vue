<!--
  * 操作记录 列表
  *

-->
<template>
  <!---------- 等保二级必备标注 begin ---------->
  <div style="margin-bottom: 8px;">
    <a-tag color="green" style="margin-right: 8px; font-weight: 600;">二级必备</a-tag>
    <span style="color: rgba(0,0,0,0.45); font-size: 12px;">依据 GB/T 22239-2019《网络安全等级保护基本要求》二级要求，本功能为等保二级核查项</span>
  </div>
  <!---------- 等保二级必备标注 end ---------->
  <a-form class="qb-query-form" v-privilege="'support:operateLog:query'">
    <a-row class="qb-query-form-row">
      <a-form-item label="操作关键字" class="qb-query-form-item">
        <a-input style="width: 150px" v-model:value="queryForm.keywords" placeholder="模块/操作内容" />
      </a-form-item>
      <a-form-item label="请求关键字" class="qb-query-form-item">
        <a-input style="width: 270px" v-model:value="queryForm.requestKeywords" placeholder="请求地址/请求方法/请求参数/返回结果" />
      </a-form-item>
      <a-form-item label="用户名称" class="qb-query-form-item">
        <a-input style="width: 100px" v-model:value="queryForm.userName" placeholder="用户名称" />
      </a-form-item>

      <a-form-item label="请求时间" class="qb-query-form-item">
        <a-range-picker @change="changeCreateDate" v-model:value="createDateRange" :presets="defaultChooseTimeRange" style="width: 240px" />
      </a-form-item>

      <a-form-item label="状态：" class="qb-query-form-item">
        <a-radio-group v-model:value="queryForm.successFlag" @change="onSearch">
          <a-radio-button :value="undefined">全部</a-radio-button>
          <a-radio-button :value="true">成功</a-radio-button>
          <a-radio-button :value="false">失败</a-radio-button>
        </a-radio-group>
      </a-form-item>

      <a-form-item class="qb-query-form-item qb-margin-left10">
        <a-button-group>
          <a-button type="primary" @click="ajaxQuery">
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

  <a-card size="small" :bordered="false" :hoverable="true" >
    <a-row justify="end">
      <TableOperator class="qb-margin-bottom5" v-model="columns" :tableId="TABLE_ID_CONST.SUPPORT.OPERATE_LOG" :refresh="ajaxQuery" />
    </a-row>
    <a-table ref="tableRef" size="small" :loading="tableLoading" :dataSource="tableData" :columns="columns" bordered rowKey="operateLogId" :pagination="false">
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'response'">
          <a-typography-text v-if="text && text.ok">{{ text ? text.msg : '-' }}</a-typography-text>
          <a-typography-text v-else type="warning">{{ text ? text.msg : '-' }}</a-typography-text>
        </template>

        <template v-if="column.dataIndex === 'successFlag'">
          <a-tag :color="text ? 'success' : 'error'">{{ text ? '成功' : '报错' }}</a-tag>
        </template>

        <template v-if="column.dataIndex === 'userAgent'">
          <div>{{ record.os }} / {{ record.browser }}  {{ record.device ? '/' + record.device : record.device }}</div>
        </template>

        <template v-if="column.dataIndex === 'operateUserType'">
          <div>{{ $enumPlugin.getDescByValue('USER_TYPE_ENUM', text) }}</div>
        </template>

        <template v-else-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="showDetail(record.operateLogId)" type="link" v-privilege="'support:operateLog:detail'">详情</a-button>
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

    <OperateLogDetailModal ref="detailModal" />
  </a-card>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import OperateLogDetailModal from './operate-log-detail-modal.vue';
  import { operateLogApi } from '/@/api/support/operate-log-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { defaultTimeRanges } from '/@/lib/default-time-ranges';
  import uaparser from 'ua-parser-js';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  const tableRef = ref();
  const columns = ref([
    {
      title: '用户',
      dataIndex: 'operateUserName',
      width: 70,
    },
    {
      title: '类型',
      dataIndex: 'operateUserType',
      width: 50,
      ellipsis: true,
    },
    {
      title: '操作模块',
      dataIndex: 'module',
      ellipsis: true,
    },
    {
      title: '操作内容',
      dataIndex: 'content',
      ellipsis: true,
    },
    {
      title: '请求路径',
      dataIndex: 'url',
      ellipsis: true,
    },
    {
      title: '返回结果',
      dataIndex: 'response',
      ellipsis: true,
    },
    {
      title: 'IP地址',
      dataIndex: 'ip',
      ellipsis: true,
      width: 120,
    },
    {
      title: 'IP地区',
      dataIndex: 'ipRegion',
      ellipsis: true,
      width: 150,
      customRender: ({ text }) => formatIpRegion(text),
    },
    {
      title: '客户端',
      dataIndex: 'userAgent',
      ellipsis: true,
    },
    {
      title: '操作时间',
      dataIndex: 'createTime',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'successFlag',
      width: 60,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 60,
    },
  ]);

  useColumnResize(tableRef, columns);

  const queryFormState = {
    userName: '',
    requestKeywords: '',
    keywords: '',
    successFlag: undefined,
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
      let responseModel = await operateLogApi.queryList(queryForm);

      const dataList = responseModel.data.dataList || [];
      for (const e of dataList) {
        if(e.response){
          try {
            e.response = JSON.parse(e.response);
          } catch (err) {
            // 兼容历史数据：旧版本后端对超长返回结果做过截断（"..."结尾），无法解析为 JSON，保留原始文本展示
            e.response = { ok: false, msg: '返回结果过长，请在详情页查看完整内容' };
          }
        }

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

  onMounted(ajaxQuery);

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

  // ---------------------- 详情 ----------------------
  const detailModal = ref();
  function showDetail(operateLogId) {
    detailModal.value.show(operateLogId);
  }
</script>
