<!--
  * 缓存管理
  *
-->
<template>
  <!--- 顶部说明 begin --->
  <a-alert class="qb-margin-bottom10" type="info" show-icon :closable="false">
    <template #message>
      <span style="font-weight: 600;">缓存管理</span>
      <span style="color: rgba(0,0,0,0.45); margin-left: 8px; font-size: 13px;">查看系统缓存运行情况，支持按缓存维度查看 Key、删除单个 Key 与一键清空</span>
    </template>
  </a-alert>
  <!--- 顶部说明 end --->

  <!--- 统计 begin --->
  <a-row :gutter="12" class="qb-margin-bottom10">
    <a-col :xs="12" :md="6" v-for="item in statisticList" :key="item.title" class="qb-margin-bottom10">
      <div class="cache-stat-card">
        <div class="cache-stat-icon" :style="{ backgroundColor: item.bg, color: item.color }">
          <component :is="item.icon" />
        </div>
        <div class="cache-stat-body">
          <div class="cache-stat-value">{{ item.value }}</div>
          <div class="cache-stat-title">{{ item.title }}</div>
        </div>
      </div>
    </a-col>
  </a-row>
  <!--- 统计 end --->

  <!--- 查询 begin --->
  <a-form class="qb-query-form" ref="queryFormRef">
    <a-row class="qb-query-form-row">
      <a-form-item label="缓存名称" class="qb-query-form-item">
        <a-input style="width: 220px" v-model:value="filterForm.cacheName" placeholder="缓存名称" allow-clear @pressEnter="onSearch" />
      </a-form-item>

      <a-form-item label="状态" class="qb-query-form-item">
        <a-radio-group v-model:value="filterForm.status" size="small">
          <a-radio-button value="all">全部</a-radio-button>
          <a-radio-button value="has">有数据</a-radio-button>
          <a-radio-button value="empty">空缓存</a-radio-button>
        </a-radio-group>
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
    <a-table
      ref="tableRef"
      size="small"
      :dataSource="pageData"
      :columns="columns"
      bordered
      rowKey="cacheName"
      :loading="tableLoading"
      :pagination="false"
      :scroll="{ x: 720 }"
    >
      <template #emptyText>
        <div class="cache-empty">
          <InboxOutlined class="cache-empty-icon" />
          <div class="cache-empty-title">暂未发现缓存数据</div>
          <div class="cache-empty-desc">若为 Caffeine 进程内缓存，需业务访问后才会注册缓存名称；Redis 缓存则需存在 QuickBlue 前缀的 Key</div>
        </div>
      </template>

      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'cacheName'">
          <span class="code-text">{{ text }}</span>
        </template>

        <template v-else-if="column.dataIndex === 'keyCount'">
          <span v-if="text > 0" class="cache-key-count">{{ text }}</span>
          <span v-else class="table-empty">0</span>
        </template>

        <template v-else-if="column.dataIndex === 'status'">
          <a-tag v-if="record.keyCount > 0" color="green">有数据</a-tag>
          <a-tag v-else>空缓存</a-tag>
        </template>

        <template v-else-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="showKeys(record)" v-privilege="'support:cache:keys'" type="link">
              <template #icon>
                <KeyOutlined />
              </template>
              查看 Key
            </a-button>
            <a-button @click="removeCache(record)" v-privilege="'support:cache:delete'" type="link">
              <template #icon>
                <ClearOutlined />
              </template>
              清空
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

  <!--- 缓存 Key 弹窗 begin --->
  <a-modal :open="keysVisible" :title="`缓存 Key 列表 - ${currentCache.cacheName || ''}`" width="960px" :footer="null" @cancel="keysVisible = false">
    <div class="cache-keys-toolbar">
      <a-input style="width: 260px" v-model:value="keySearch" placeholder="搜索 Key" allow-clear @pressEnter="keyPageNum = 1">
        <template #prefix>
          <SearchOutlined />
        </template>
      </a-input>
      <div style="flex: 1"></div>
      <a-button @click="loadKeys">
        <template #icon>
          <ReloadOutlined />
        </template>
        刷新
      </a-button>
      <a-button danger v-privilege="'support:cache:delete'" :disabled="keyList.length === 0" @click="clearAllKeys">
        <template #icon>
          <ClearOutlined />
        </template>
        清空全部
      </a-button>
    </div>

    <a-table
      size="small"
      :dataSource="keyPageData"
      :columns="keyColumns"
      bordered
      rowKey="__key"
      :loading="keyLoading"
      :pagination="false"
      :scroll="{ x: 860, y: 380 }"
    >
      <template #emptyText>
        <a-empty :description="keySearch ? '未找到匹配的 Key' : '当前缓存暂无 Key'" />
      </template>

      <template #bodyCell="{ text, record, column, index }">
        <template v-if="column.dataIndex === 'index'">
          <span class="table-empty">{{ (keyPageNum - 1) * keyPageSize + index + 1 }}</span>
        </template>

        <template v-else-if="column.dataIndex === 'key'">
          <span class="code-text" :title="text">{{ text }}</span>
        </template>

        <template v-else-if="column.dataIndex === 'value'">
          <a-tooltip :title="text">
            <span class="code-text" style="max-width: 240px; display: inline-block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ text }}</span>
          </a-tooltip>
        </template>

        <template v-else-if="column.dataIndex === 'action'">
          <a-button type="link" size="small" @click="copyKey(record.__key)">
            <template #icon>
              <CopyOutlined />
            </template>
            复制
          </a-button>
          <a-button type="link" size="small" danger v-privilege="'support:cache:delete'" @click="removeOneKey(record.__key)">
            <template #icon>
              <DeleteOutlined />
            </template>
            删除
          </a-button>
        </template>
      </template>
    </a-table>

    <div class="qb-query-table-page">
      <a-pagination
        showSizeChanger
        showQuickJumper
        show-less-items
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        :defaultPageSize="keyPageSize"
        v-model:current="keyPageNum"
        v-model:pageSize="keyPageSize"
        :total="keyTotal"
        :show-total="(total) => `共${total}条`"
      />
    </div>
  </a-modal>
  <!--- 缓存 Key 弹窗 end --->
</template>
<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import {
    SearchOutlined,
    ReloadOutlined,
    DatabaseOutlined,
    CheckCircleOutlined,
    InboxOutlined,
    KeyOutlined,
    ClearOutlined,
    CopyOutlined,
    DeleteOutlined,
  } from '@ant-design/icons-vue';
  import { message, Modal } from 'ant-design-vue';
  import { cacheApi } from '/@/api/support/cache-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { sentry } from '/@/lib/sentry';

  // ----------------------- 统计 ------------------------
  // 注：后端 JacksonConfig 全局把 Long 序列化为字符串，前端需统一转为数字后再计算
  const statisticList = computed(() => {
    const data = tableData.value.map((item) => ({
      ...item,
      keyCount: Number(item.keyCount || 0),
    }));
    const totalCount = data.length;
    const hasDataCount = data.filter((item) => item.keyCount > 0).length;
    const emptyCount = data.filter((item) => item.keyCount === 0).length;
    const keyTotalCount = data.reduce((sum, item) => sum + item.keyCount, 0);
    return [
      { title: '缓存总数', value: totalCount, icon: DatabaseOutlined, color: '#1677ff', bg: 'rgba(22,119,255,0.10)' },
      { title: '有数据', value: hasDataCount, icon: CheckCircleOutlined, color: '#52c41a', bg: 'rgba(82,196,26,0.10)' },
      { title: '空缓存', value: emptyCount, icon: InboxOutlined, color: '#faad14', bg: 'rgba(250,173,20,0.12)' },
      { title: 'Key 总数', value: keyTotalCount, icon: KeyOutlined, color: '#13c2c2', bg: 'rgba(19,194,194,0.10)' },
    ];
  });

  // ----------------------- 查询过滤 ------------------------
  const filterForm = reactive({
    cacheName: '',
    status: 'all',
  });

  function filteredRows() {
    const keyword = filterForm.cacheName.trim().toLowerCase();
    return tableData.value.filter((item) => {
      if (keyword && !(item.cacheName || '').toLowerCase().includes(keyword)) return false;
      if (filterForm.status === 'has' && !(item.keyCount > 0)) return false;
      if (filterForm.status === 'empty' && item.keyCount > 0) return false;
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
    filterForm.cacheName = '';
    filterForm.status = 'all';
    onSearch();
  }

  // ----------------------- 表格渲染 ------------------------
  const tableRef = ref();
  const columns = ref([
    {
      title: '缓存名称',
      dataIndex: 'cacheName',
      width: 300,
      ellipsis: true,
    },
    {
      title: 'Key 数量',
      dataIndex: 'keyCount',
      width: 120,
      align: 'center',
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 110,
      align: 'center',
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 150,
    },
  ]);

  const tableLoading = ref(false);
  const tableData = ref([]);
  const pageData = ref([]);
  const total = ref(0);

  const queryForm = reactive({
    pageNum: 1,
    pageSize: 10,
  });

  // 降级模式标识：旧后端无 /cache/list 接口时，回退到 names + 逐个统计 key 数量
  const degradedMode = ref(false);

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let list = [];
      try {
        const res = await cacheApi.getAll();
        list = res.data || [];
      } catch (e) {
        // 旧后端无 /list 接口，降级兼容
        degradedMode.value = true;
        const namesRes = await cacheApi.getAllCacheNames();
        const names = (namesRes.data || []).filter((n) => n && n !== '');
        list = [];
        for (const name of names) {
          let count = 0;
          try {
            const keysRes = await cacheApi.getKeys(name);
            count = (keysRes.data || []).length;
          } catch (e2) {
            // 单个缓存统计失败不阻断整体加载
          }
          list.push({ cacheName: name, keyCount: count });
        }
      }
      tableData.value = list;
      filterAndPage();
    } catch (e) {
      sentry.captureError(e);
      message.error('缓存数据加载失败，请确认服务端缓存服务正常');
      tableData.value = [];
      filterAndPage();
    } finally {
      tableLoading.value = false;
    }
  }

  onMounted(ajaxQuery);

  // ------------------------------ 查看 Key ------------------------------
  const keysVisible = ref(false);
  const currentCache = ref({});
  const keyList = ref([]);
  const keySearch = ref('');
  const keyPageNum = ref(1);
  const keyPageSize = ref(10);
  const keyLoading = ref(false);

  const keyColumns = ref([
    {
      title: '序号',
      dataIndex: 'index',
      width: 60,
      align: 'center',
    },
    {
      title: 'Key',
      dataIndex: 'key',
      width: 260,
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'dataType',
      width: 80,
      align: 'center',
    },
    {
      title: 'TTL(秒)',
      dataIndex: 'ttl',
      width: 90,
      align: 'center',
    },
    {
      title: '内容摘要',
      dataIndex: 'value',
      width: 260,
      ellipsis: true,
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 130,
      fixed: 'right',
    },
  ]);

  const filteredKeys = computed(() => {
    const keyword = keySearch.value.trim().toLowerCase();
    if (!keyword) {
      return keyList.value;
    }
    return keyList.value.filter((k) => String(k.key).toLowerCase().includes(keyword));
  });

  const keyTotal = computed(() => filteredKeys.value.length);

  const keyPageData = computed(() => {
    const start = (keyPageNum.value - 1) * keyPageSize.value;
    return filteredKeys.value.slice(start, start + keyPageSize.value).map((item, index) => ({
      ...item,
      __key: item.key,
      index: (keyPageNum.value - 1) * keyPageSize.value + index + 1,
    }));
  });

  async function showKeys(record) {
    currentCache.value = record;
    keySearch.value = '';
    keyPageNum.value = 1;
    keysVisible.value = true;
    await loadKeys();
  }

  async function loadKeys() {
    keyLoading.value = true;
    try {
      const res = await cacheApi.getKeyDetails(currentCache.value.cacheName);
      keyList.value = res.data || [];
    } catch (e) {
      message.error('获取缓存 Key 失败，请确认服务端已升级缓存管理接口');
      keyList.value = [];
    } finally {
      keyLoading.value = false;
    }
  }

  function copyKey(key) {
    navigator.clipboard
      .writeText(key)
      .then(() => message.success('已复制'))
      .catch(() => message.error('复制失败'));
  }

  async function removeOneKey(key) {
    try {
      await cacheApi.removeKey(currentCache.value.cacheName, key);
      message.success('Key 已删除');
      keyList.value = keyList.value.filter((k) => k.key !== key);
      // 同步主列表 keyCount
      const row = tableData.value.find((r) => r.cacheName === currentCache.value.cacheName);
      if (row && row.keyCount > 0) {
        row.keyCount -= 1;
      }
      filterAndPage();
    } catch (e) {
      message.error('删除失败，请确认服务端已升级（需支持单 Key 删除接口）');
    }
  }

  // ------------------------------ 清空缓存 ------------------------------
  async function doRemoveCache(cacheName) {
    try {
      await cacheApi.remove(cacheName);
      message.success('缓存已清空');
      if (keysVisible.value && currentCache.value.cacheName === cacheName) {
        keyList.value = [];
        keyPageNum.value = 1;
      }
      await ajaxQuery();
    } catch (e) {
      message.error('清空失败，请确认服务端已升级缓存管理接口');
    }
  }

  function removeCache(record) {
    Modal.confirm({
      title: '清空缓存',
      content: `确定清空缓存「${record.cacheName}」下的所有 Key？`,
      okText: '清空',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: () => doRemoveCache(record.cacheName),
    });
  }

  function clearAllKeys() {
    Modal.confirm({
      title: '清空缓存',
      content: `确定清空缓存「${currentCache.value.cacheName}」下的所有 Key？`,
      okText: '清空',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: () => doRemoveCache(currentCache.value.cacheName),
    });
  }
</script>
<style lang="less" scoped>
  .cache-stat-card {
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

  .cache-stat-icon {
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

  .cache-stat-body {
    min-width: 0;
  }

  .cache-stat-value {
    font-size: 22px;
    font-weight: 600;
    line-height: 1.2;
    color: rgba(0, 0, 0, 0.88);
  }

  .cache-stat-title {
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

  .cache-key-count {
    font-size: 14px;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.88);
  }

  .cache-empty {
    padding: 24px 0;
    text-align: center;

    .cache-empty-icon {
      font-size: 36px;
      color: rgba(0, 0, 0, 0.18);
    }

    .cache-empty-title {
      margin-top: 8px;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
    }

    .cache-empty-desc {
      margin-top: 4px;
      font-size: 12px;
      color: rgba(0, 0, 0, 0.35);
    }
  }

  .cache-keys-toolbar {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
  }
</style>
