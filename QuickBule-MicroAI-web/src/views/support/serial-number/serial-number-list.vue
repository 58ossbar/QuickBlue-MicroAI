<!--
  * 单号管理
  *
-->
<template>
  <!--- 顶部说明 begin --->
  <a-alert class="qb-margin-bottom10" type="info" show-icon :closable="false">
    <template #message>
      <span style="font-weight: 600;">单号生成器</span>
      <span style="color: rgba(0,0,0,0.45); margin-left: 8px; font-size: 13px;">根据日期与规则动态生成订单号、合同号、采购单号等业务单号，支持随机生成、生成记录查询与动态配置</span>
      <a-button type="link" size="small" style="padding: 0 4px; margin-left: 8px;" @click="usageVisible = true">
        <QuestionCircleOutlined />
        使用说明
      </a-button>
    </template>
  </a-alert>
  <!--- 顶部说明 end --->

  <!--- 统计 begin --->
  <a-row :gutter="12" class="qb-margin-bottom10">
    <a-col :xs="12" :md="6" v-for="item in statisticList" :key="item.title" class="qb-margin-bottom10">
      <div class="serial-stat-card">
        <div class="serial-stat-icon" :style="{ backgroundColor: item.bg, color: item.color }">
          <component :is="item.icon" />
        </div>
        <div class="serial-stat-body">
          <div class="serial-stat-value">{{ item.value }}</div>
          <div class="serial-stat-title">{{ item.title }}</div>
        </div>
      </div>
    </a-col>
  </a-row>
  <!--- 统计 end --->

  <!--- 查询 begin --->
  <a-form class="qb-query-form" ref="queryFormRef">
    <a-row class="qb-query-form-row">
      <a-form-item label="业务名称" class="qb-query-form-item">
        <a-input style="width: 180px" v-model:value="filterForm.businessName" placeholder="业务名称" allow-clear @pressEnter="onSearch" />
      </a-form-item>

      <a-form-item label="格式" class="qb-query-form-item">
        <a-input style="width: 210px" v-model:value="filterForm.format" placeholder="如 DD[yyyy][mm][dd]NNNN" allow-clear @pressEnter="onSearch" />
      </a-form-item>

      <a-form-item label="循环周期" class="qb-query-form-item">
        <a-select style="width: 120px" v-model:value="filterForm.ruleType" placeholder="全部" allow-clear>
          <a-select-option v-for="(item, key) in SERIAL_NUMBER_RULE_TYPE_ENUM" :key="key" :value="item.value">{{ item.desc }}</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="安全模式" class="qb-query-form-item">
        <a-select style="width: 130px" v-model:value="filterForm.secureMode" placeholder="全部" allow-clear>
          <a-select-option v-for="(item, key) in SERIAL_NUMBER_SECURE_MODE_ENUM" :key="key" :value="item.value">{{ item.desc }}</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="备注" class="qb-query-form-item">
        <a-input style="width: 150px" v-model:value="filterForm.remark" placeholder="备注" allow-clear @pressEnter="onSearch" />
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
  <!--- 查询 end --->

  <a-card size="small" :bordered="false" :hoverable="true">
    <a-row justify="end" ref="tableOperatorRef">
      <TableOperator class="qb-margin-bottom5" v-model="columns" :tableId="TABLE_ID_CONST.SUPPORT.SERIAL_NUMBER" :refresh="ajaxQuery" />
    </a-row>
    <a-table
      ref="tableRef"
      size="small"
      :dataSource="pageData"
      :columns="columns"
      bordered
      rowKey="serialNumberId"
      :loading="tableLoading"
      :pagination="false"
      :scroll="{ x: 1250 }"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'ruleType'">
          <a-tag :color="ruleTypeTag(record.ruleType).color">{{ ruleTypeTag(record.ruleType).desc }}</a-tag>
        </template>

        <template v-else-if="column.dataIndex === 'secureMode'">
          <a-tag :color="secureModeTag(record.secureMode).color">{{ secureModeTag(record.secureMode).desc }}</a-tag>
        </template>

        <template v-else-if="column.dataIndex === 'format'">
          <span class="code-text">{{ text }}</span>
        </template>

        <template v-else-if="column.dataIndex === 'initNumber' || column.dataIndex === 'stepRandomRange'">
          <span v-if="text != null">{{ text }}</span>
          <span v-else class="table-empty">-</span>
        </template>

        <template v-else-if="column.dataIndex === 'lastNumber'">
          <span v-if="text != null" class="code-text">{{ text }}</span>
          <span v-else class="table-empty">-</span>
        </template>

        <template v-else-if="column.dataIndex === 'lastTime'">
          <span v-if="text">{{ text }}</span>
          <span v-else class="table-empty">-</span>
        </template>

        <template v-else-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="generate(record)" v-privilege="'support:serialNumber:generate'" type="link">
              <template #icon>
                <ThunderboltOutlined />
              </template>
              生成
            </a-button>
            <a-button @click="showRecord(record)" v-privilege="'support:serialNumber:record'" type="link">
              <template #icon>
                <HistoryOutlined />
              </template>
              查看记录
            </a-button>
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
        @change="filterAndPage"
        :show-total="(total) => `共${total}条`"
      />
    </div>
  </a-card>

  <!--- 使用说明 begin --->
  <a-modal :open="usageVisible" title="单号生成器使用说明" width="680px" :footer="null" @cancel="usageVisible = false">
    <a-descriptions :column="1" bordered size="small">
      <a-descriptions-item label="功能简介">
        根据日期、规则生成一系列业务单号，如订单号、合同号、采购单号等；支持动态配置、随机生成与生成记录查询。
      </a-descriptions-item>
      <a-descriptions-item label="生成原理">
        内部提供三种实现方式：内存锁实现（默认）、Redis 锁实现、PostgreSQL for update 实现。
      </a-descriptions-item>
      <a-descriptions-item label="切换实现">
        将对应实现类上的 <a-tag>@Service</a-tag> 注解迁移至目标类即可：SerialNumberInternService（内存锁）、SerialNumberRedisService（Redis 锁）、SerialNumberServiceImpl（PostgreSQL 锁）。
      </a-descriptions-item>
      <a-descriptions-item label="安全模式">
        普通模式（连续数字）、随机模式（随机跳跃）、时间戳模式（时间戳+序列）、加密模式（可逆加密），随机/时间戳/加密模式会产生原始序列号，可在生成记录中查看。
      </a-descriptions-item>
      <a-descriptions-item label="格式占位符">
        <a-tag>[yyyy]</a-tag> 年 <a-tag>[mm]</a-tag> 月 <a-tag>[dd]</a-tag> 日 <a-tag>N</a-tag> 数字位，例如 <a-tag color="processing">DD[yyyy][mm][dd]NNNN</a-tag>
      </a-descriptions-item>
    </a-descriptions>
  </a-modal>
  <!--- 使用说明 end --->

  <!--- 生成表单 --->
  <SerialNumberGenerateFormModal ref="generateForm" @refresh="ajaxQuery" />
  <!--- 生成记录 --->
  <SerialNumberRecordList ref="recordList" />
</template>
<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import {
    SearchOutlined,
    ReloadOutlined,
    QuestionCircleOutlined,
    NumberOutlined,
    FieldTimeOutlined,
    CalendarOutlined,
    StopOutlined,
    ThunderboltOutlined,
    HistoryOutlined,
  } from '@ant-design/icons-vue';
  import SerialNumberGenerateFormModal from './serial-number-generate-form-modal.vue';
  import SerialNumberRecordList from './serial-number-record-list.vue';
  import { serialNumberApi } from '/@/api/support/serial-number-api';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { SERIAL_NUMBER_RULE_TYPE_ENUM, SERIAL_NUMBER_SECURE_MODE_ENUM } from '/@/constants/support/serial-number-const';
  import { sentry } from '/@/lib/sentry';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  // ----------------------- 使用说明 ------------------------
  const usageVisible = ref(false);

  // ----------------------- 统计 ------------------------
  const statisticList = computed(() => {
    const totalCount = tableData.value.length;
    const todayStr = new Date().toISOString().slice(0, 10);
    const todayCount = tableData.value.filter((item) => item.lastTime && String(item.lastTime).startsWith(todayStr)).length;
    const dayRuleCount = tableData.value.filter((item) => item.ruleType === '[dd]').length;
    const neverCount = tableData.value.filter((item) => item.lastNumber == null).length;
    return [
      { title: '单号定义', value: totalCount, icon: NumberOutlined, color: '#1677ff', bg: 'rgba(22,119,255,0.10)' },
      { title: '今日已生成', value: todayCount, icon: FieldTimeOutlined, color: '#52c41a', bg: 'rgba(82,196,26,0.10)' },
      { title: '日周期规则', value: dayRuleCount, icon: CalendarOutlined, color: '#13c2c2', bg: 'rgba(19,194,194,0.10)' },
      { title: '从未生成', value: neverCount, icon: StopOutlined, color: '#faad14', bg: 'rgba(250,173,20,0.12)' },
    ];
  });

  // ----------------------- 查询过滤 ------------------------
  const filterForm = reactive({
    businessName: '',
    format: '',
    ruleType: undefined,
    secureMode: undefined,
    remark: '',
  });

  function filteredRows() {
    const business = filterForm.businessName.trim().toLowerCase();
    const format = filterForm.format.trim().toLowerCase();
    const remark = filterForm.remark.trim().toLowerCase();
    return tableData.value.filter((item) => {
      if (business && !(item.businessName || '').toLowerCase().includes(business)) return false;
      if (format && !(item.format || '').toLowerCase().includes(format)) return false;
      if (remark && !(item.remark || '').toLowerCase().includes(remark)) return false;
      if (filterForm.ruleType !== undefined && filterForm.ruleType !== '' && item.ruleType !== filterForm.ruleType) return false;
      if (filterForm.secureMode !== undefined && filterForm.secureMode !== '' && Number(item.secureMode) !== Number(filterForm.secureMode)) return false;
      return true;
    });
  }

  function filterAndPage() {
    const rows = filteredRows();
    total.value = rows.length;
    const start = (queryForm.pageNum - 1) * queryForm.pageSize;
    pageData.value = rows.slice(start, start + queryForm.pageSize);
  }

  function onSearch() {
    queryForm.pageNum = 1;
    filterAndPage();
  }

  function resetQuery() {
    filterForm.businessName = '';
    filterForm.format = '';
    filterForm.ruleType = undefined;
    filterForm.secureMode = undefined;
    filterForm.remark = '';
    onSearch();
  }

  // ----------------------- 表格渲染 ------------------------
  const tableRef = ref();
  const columns = ref([
    {
      title: '业务名称',
      dataIndex: 'businessName',
      width: 150,
      ellipsis: true,
    },
    {
      title: '格式',
      dataIndex: 'format',
      width: 220,
      ellipsis: true,
    },
    {
      title: '循环周期',
      dataIndex: 'ruleType',
      width: 100,
    },
    {
      title: '安全模式',
      dataIndex: 'secureMode',
      width: 110,
    },
    {
      title: '初始值',
      dataIndex: 'initNumber',
      width: 100,
    },
    {
      title: '随机增量',
      dataIndex: 'stepRandomRange',
      width: 100,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      width: 140,
      ellipsis: true,
    },
    {
      title: '上次产生单号',
      dataIndex: 'lastNumber',
      width: 170,
    },
    {
      title: '上次产生时间',
      dataIndex: 'lastTime',
      width: 180,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 150,
    },
  ]);

  useColumnResize(tableRef, columns);

  // 循环周期 展示映射
  function ruleTypeTag(val) {
    const map = {
      '': { desc: '无周期', color: 'default' },
      '[yyyy]': { desc: '年周期', color: 'blue' },
      '[mm]': { desc: '月周期', color: 'cyan' },
      '[dd]': { desc: '日周期', color: 'green' },
    };
    return map[val] || { desc: val || '无周期', color: 'default' };
  }

  // 安全模式 展示映射
  function secureModeTag(val) {
    const map = {
      0: { desc: '普通模式', color: 'default' },
      1: { desc: '随机模式', color: 'purple' },
      2: { desc: '时间戳模式', color: 'blue' },
      3: { desc: '加密模式', color: 'gold' },
    };
    return map[val] || { desc: '未知', color: 'default' };
  }

  const tableLoading = ref(false);
  const tableData = ref([]);
  const pageData = ref([]);
  const total = ref(0);

  const queryForm = reactive({
    pageNum: 1,
    pageSize: 10,
  });

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let res = await serialNumberApi.getAll();
      tableData.value = res.data;
      filterAndPage();
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  onMounted(ajaxQuery);

  // ------------------------------ 表格操作列： 生成 ------------------------------
  const generateForm = ref();
  function generate(record) {
    generateForm.value.showModal(record);
  }

  // ------------------------------ 表格操作列： 查看记录 ------------------------------
  const recordList = ref();
  function showRecord(record) {
    recordList.value.showModal(record);
  }
</script>
<style lang="less" scoped>
  .serial-stat-card {
    display: flex;
    align-items: center;
    height: 100%;
    padding: 14px 16px;
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    transition: all 0.3s;
    cursor: default;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      transform: translateY(-2px);
    }
  }

  .serial-stat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    width: 44px;
    height: 44px;
    margin-right: 12px;
    border-radius: 10px;
    font-size: 20px;
  }

  .serial-stat-body {
    min-width: 0;
  }

  .serial-stat-value {
    font-size: 22px;
    font-weight: 600;
    line-height: 1.2;
    color: rgba(0, 0, 0, 0.88);
  }

  .serial-stat-title {
    font-size: 12px;
    color: rgba(0, 0, 0, 0.45);
  }

  .code-text {
    font-family: 'JetBrains Mono', Consolas, Monaco, 'Courier New', monospace;
    font-size: 12px;
  }

  .table-empty {
    color: rgba(0, 0, 0, 0.25);
  }
</style>
