<!--
  * 岗位管理列表页
  *

-->
<template>
  <!---------- 查询表单form begin ----------->
  <a-form class="qb-query-form">
    <a-row class="qb-query-form-row">
      <a-form-item label="关键字查询" class="qb-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.keywords" placeholder="岗位名称/编码" />
      </a-form-item>
      <a-form-item label="岗位类别" class="qb-query-form-item">
        <DictSelect dictCode="POST_TYPE" v-model:value="queryForm.category" placeholder="请选择" width="150px" />
      </a-form-item>
      <a-form-item label="状态" class="qb-query-form-item">
        <a-select style="width: 100px" v-model:value="queryForm.status" placeholder="请选择" allowClear>
          <a-select-option :value="1">启用</a-select-option>
          <a-select-option :value="0">停用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item class="qb-query-form-item">
        <a-button type="primary" @click="queryData">
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
      </a-form-item>
    </a-row>
  </a-form>
  <!---------- 查询表单form end ----------->

  <a-card size="small" :bordered="false" :hoverable="true">
    <!---------- 表格操作行 begin ----------->
    <a-row class="qb-table-btn-block">
      <div class="qb-table-operate-block">
        <a-button @click="showForm" type="primary">
          <template #icon>
            <PlusOutlined />
          </template>
          新建
        </a-button>
        <a-button @click="confirmBatchDelete" type="primary" danger :disabled="selectedRowKeyList.length === 0">
          <template #icon>
            <DeleteOutlined />
          </template>
          批量删除
        </a-button>
      </div>
      <div class="qb-table-setting-block">
        <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.SYSTEM.POSITION" :refresh="queryData" />
      </div>
    </a-row>
    <!---------- 表格操作行 end ----------->

    <!---------- 表格 begin ----------->
    <a-table
      ref="tableRef"
      size="small"
      :dataSource="tableData"
      :columns="columns"
      rowKey="positionId"
      bordered
      :loading="tableLoading"
      :pagination="false"
      :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }"
      v-model:expandedRowKeys="expandedRowKeys"
      :childrenColumnName="'children'"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'categoryName'">
          <a-tag v-if="record.category" :color="getCategoryColor(record.category)">{{ getCategoryLabel(record.category) }}</a-tag>
          <span v-else>-</span>
        </template>
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="text === 1 ? 'green' : 'red'">{{ text === 1 ? '启用' : '停用' }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          <span>{{ formatDateTime(text) }}</span>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="showChildForm(record)" type="link">添加子岗位</a-button>
            <a-button @click="showForm(record)" type="link">编辑</a-button>
            <a-button @click="onDelete(record)" danger type="link">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>
    <!---------- 表格 end ----------->

    <div style="padding: 12px 0; color: #999; font-size: 13px;">
      共 {{ total }} 条记录
    </div>

    <PositionForm ref="formRef" @reloadList="queryData" />
  </a-card>
</template>
<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { Loading } from '/@/components/framework/loading';
  import { positionApi } from '/@/api/system/position-api';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import PositionForm from './position-form.vue';
  import DictSelect from '/@/components/support/dict-select/index.vue';
  import { useDictStore } from '/@/store/modules/system/dict.js';
  import _ from 'lodash';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { useColumnResize } from '/@/hooks/useColumnResize';
  // ---------------------------- 表格列 ----------------------------

  const tableRef = ref();
  const columns = ref([
    {
      title: '岗位名称',
      dataIndex: 'positionName',
      ellipsis: true,
      width: 180,
    },
    {
      title: '岗位编码',
      dataIndex: 'positionCode',
      ellipsis: true,
      width: 130,
    },
    {
      title: '岗位类别',
      dataIndex: 'categoryName',
      ellipsis: true,
      width: 100,
    },
    {
      title: '职级等级',
      dataIndex: 'gradeLevel',
      ellipsis: true,
      width: 90,
    },
    {
      title: '排序',
      dataIndex: 'sort',
      ellipsis: true,
      width: 70,
    },
    {
      title: '状态',
      dataIndex: 'status',
      ellipsis: true,
      width: 70,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      ellipsis: true,
      width: 160,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 100,
    },
  ]);

  useColumnResize(tableRef, columns);

  // 类别颜色映射
  function getCategoryColor(category) {
    const colorMap = { '1': 'blue', '2': 'green', '3': 'orange', '4': 'purple' };
    return colorMap[category] || 'default';
  }

  const dictStore = useDictStore();

  // 岗位类别中文翻译（从字典 POST_TYPE 取值）
  function getCategoryLabel(category) {
    if (!category) return '';
    return dictStore.getDataLabels('POST_TYPE', category);
  }

  /** 时间格式化：统一为 YYYY-MM-DD HH:mm:ss */
  function formatDateTime(val) {
    if (!val) return '-';
    // 处理 JSON 序列化的时间格式：2026-07-06T15:30:00 / 2026-07-06T15:30:00.123
    return val.replace('T', ' ').replace(/\.\d+/, '');
  }

  // ---------------------------- 查询数据表单和方法 ----------------------------

  const queryFormState = {
    keywords: undefined,
    category: undefined,
    status: undefined,
  };
  // 查询表单form
  const queryForm = reactive({ ...queryFormState });
  // 原始全量数据
  const rawData = ref([]);
  // 表格加载loading
  const tableLoading = ref(false);
  // 表格数据（树形）
  const tableData = ref([]);
  // 展开的树节点 key（默认全展开）
  const expandedRowKeys = ref([]);
  // 总数
  const total = ref(0);

  /**
   * 将扁平列表组装为树形结构
   */
  function buildTree(list) {
    if (!list || list.length === 0) return [];
    const map = {};
    const roots = [];
    // 创建映射
    for (const item of list) {
      map[item.positionId] = { ...item, children: [] };
    }
    // 组装树
    for (const item of list) {
      const node = map[item.positionId];
      // 排除自引用，防止生成循环结构导致递归死循环
      if (item.parentId && item.parentId !== item.positionId && map[item.parentId]) {
        map[item.parentId].children.push(node);
      } else {
        roots.push(node);
      }
    }
    return roots;
  }

  /**
   * 客户端过滤树（关键字、类别、状态）
   */
  function filterTree(nodes, keywords, category, status, depth = 0) {
    // 深度保护，防止异常数据（如循环引用）导致无限递归
    if (depth > 20) return [];
    const result = [];
    for (const node of nodes) {
      // 先递归过滤子节点，保留匹配的子树
      const filteredChildren = filterTree(node.children || [], keywords, category, status, depth + 1);
      // 判断当前节点是否匹配条件
      let match = true;
      if (keywords) {
        const kw = keywords.toLowerCase();
        match = match && ((node.positionName && node.positionName.toLowerCase().includes(kw)) ||
          (node.positionCode && node.positionCode.toLowerCase().includes(kw)));
      }
      if (category !== undefined && category !== null && category !== '') {
        match = match && node.category === category;
      }
      if (status !== undefined && status !== null) {
        match = match && node.status === status;
      }
      // 当前节点匹配 或 子节点有匹配项 → 保留该节点及其匹配子节点
      if (match || filteredChildren.length > 0) {
        result.push({
          ...node,
          children: filteredChildren,
        });
      }
    }
    return result;
  }

  // 重置查询条件
  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    applyFilter();
  }

  /** 加载全量数据 */
  async function loadAllData() {
    tableLoading.value = true;
    try {
      let queryResult = await positionApi.queryTree();
      rawData.value = queryResult.data || [];
      total.value = rawData.value.length;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  /** 收集所有含子节点的 key（默认全部展开） */
  function collectExpandKeys(nodes, keys) {
    for (const node of nodes) {
      if (node.children && node.children.length > 0) {
        keys.push(node.positionId);
        collectExpandKeys(node.children, keys);
      }
    }
  }

  /** 应用筛选条件到本地数据 */
  function applyFilter() {
    const tree = buildTree(rawData.value);
    const filtered = filterTree(tree, queryForm.keywords, queryForm.category, queryForm.status);
    tableData.value = filtered;
    // 数据更新后默认展开全部节点
    const keys = [];
    collectExpandKeys(filtered, keys);
    expandedRowKeys.value = keys;
  }

  // 查询数据（先加载再筛选）
  async function queryData() {
    await loadAllData();
    applyFilter();
  }

  onMounted(queryData);

  // ---------------------------- 添加/修改 ----------------------------
  const formRef = ref();

  function showForm(data) {
    formRef.value.show(data);
  }

  /** 快捷添加子岗位，自动预填父级 */
  function showChildForm(data) {
    formRef.value.show({ parentId: data.positionId, status: 1 });
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
      await positionApi.delete(data.positionId);
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
    if (_.isEmpty(selectedRowKeyList.value)) {
      message.success('请选择要删除的数据');
      return;
    }
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
      await positionApi.batchDelete(selectedRowKeyList.value);
      message.success('删除成功');
      queryData();
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }
</script>
