<!--
  * 区域管理列表（树形表格，保留查询条件）
  * @Author: zhujw
  * @Date: 2025-12-20
-->
<template>
  <div class="qb-region-management">
    <!---------- 查询表单 ----------->
    <a-form class="qb-query-form">
      <a-row class="qb-query-form-row">
        <a-form-item label="区域名称" class="qb-query-form-item">
          <a-input
              style="width: 200px"
              v-model:value="queryForm.regionName"
              placeholder="区域名称"
              @pressEnter="onSearch"
          />
        </a-form-item>
        <a-form-item label="状态" class="qb-query-form-item">
          <a-select
              style="width: 100px"
              v-model:value="queryForm.status"
              placeholder="状态"
              :options="statusOptions"
              allowClear
          />
        </a-form-item>
        <a-form-item label="区域负责人" class="qb-query-form-item">
          <a-input
              style="width: 200px"
              v-model:value="queryForm.regionManagerName"
              placeholder="区域负责人姓名"
              @pressEnter="onSearch"
          />
        </a-form-item>
        <a-form-item label="联系电话" class="qb-query-form-item">
          <a-input
              style="width: 200px"
              v-model:value="queryForm.contactPhone"
              placeholder="联系电话"
              @pressEnter="onSearch"
          />
        </a-form-item>
        <a-form-item class="qb-query-form-item">
          <a-button-group>
            <a-button v-privilege="'region:query'" type="primary" @click="onSearch">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>
            <a-button v-privilege="'region:query'" @click="resetQuery">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </a-button-group>
          <a-button
              v-privilege="'region:add'"
              type="primary"
              @click="addRegion"
              class="qb-margin-left20"
          >
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>

    <a-card size="small" :bordered="true">
      <a-table
          ref="tableRef"
          size="small"
          bordered
          :loading="tableLoading"
          rowKey="id"
          :columns="columns"
          :data-source="regionTreeData"
          :defaultExpandAllRows="false"
          :expandedRowKeys="expandedRowKeys"
          :rowClassName="getRowClassName"
          :pagination="false"
          @expand="handleExpand"
      >
        <template #bodyCell="{ text, record, column }">
          <template v-if="column.dataIndex === 'regionName'">
            <span :class="{ 'qb-text-highlight': record._matched }">
              {{ record.regionName }}
            </span>
            <span v-if="record.regionShortName" class="qb-text-grey">
              ({{ record.regionShortName }})
            </span>
          </template>

          <template v-if="column.dataIndex === 'level'">
            <a-tag :color="getLevelColor(record.level)">
              {{ getLevelText(record.level) }}
            </a-tag>
          </template>

          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>

          <template v-if="column.dataIndex === 'action'">
            <div class="qb-table-operate">
              <a-button @click="addRegion(record)" v-privilege="'region:add'" type="link">
                添加下级
              </a-button>
              <a-button @click="updateRegion(record)" v-privilege="'region:update'" type="link">
                编辑
              </a-button>
              <a-button
                  danger
                  v-if="record.id !== topRegionId"
                  v-privilege="'region:delete'"
                  @click="deleteRegion(record.id)"
                  type="link"
              >
                删除
              </a-button>
            </div>
          </template>
        </template>
      </a-table>

      <!---------- 添加编辑区域弹窗 ----------->
      <RegionForm ref="regionFormRef" @reloadList="loadInitialData" />
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, createVNode, nextTick } from 'vue';
import { regionApi } from '/@/api/business/region/region-api';
import { Modal, message } from 'ant-design-vue';
import { ExclamationCircleOutlined, SearchOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue';
import _ from 'lodash';
import { Loading } from '/@/components/framework/loading';
import RegionForm from './region-form.vue';
import { sentry } from '/@/lib/sentry';
import { useColumnResize } from '/@/hooks/useColumnResize';

// ----------------------- 查询条件 -----------------------
const queryFormState = {
  regionName: '',
  status: undefined,
  regionManagerName: '',
  contactPhone: '',
};

const queryForm = reactive({ ...queryFormState });
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 2 },
];

// ----------------------- 区域树的展示 -----------------------
const tableLoading = ref(false);
const topRegionId = ref();
const regionTreeData = ref([]);
const allTreeData = ref([]); // 保存原始数据用于重置
const expandedRowKeys = ref([]); // 控制展开的行
const matchedNodeIds = ref(new Set()); // 匹配的节点ID集合
const userExpandedKeys = ref(new Set()); // 用户手动展开的节点ID

const columns = ref([
  {
    title: '区域名称',
    dataIndex: 'regionName',
    key: 'regionName',
  },
  {
    title: '区域编码',
    dataIndex: 'regionCode',
    key: 'regionCode',
    width: 150,
  },
  {
    title: '区域层级',
    dataIndex: 'level',
    key: 'level',
    width: 100,
  },
  {
    title: '负责人',
    dataIndex: 'regionManagerName',
    key: 'regionManagerName',
    width: 120,
  },
  {
    title: '联系电话',
    dataIndex: 'contactPhone',
    key: 'contactPhone',
    width: 120,
  },
  {
    title: '排序',
    dataIndex: 'sortOrder',
    key: 'sortOrder',
    width: 80,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150,
  },
  {
    title: '操作',
    dataIndex: 'action',
    fixed: 'right',
    width: 200,
  },
]);

const tableRef = ref();
useColumnResize(tableRef, columns);

onMounted(() => {
  loadInitialData();
});

// 加载初始数据（无查询条件）
async function loadInitialData() {
  try {
    tableLoading.value = true;
    // 初始加载使用 treeList 接口（无参数）
    let res = await regionApi.treeList();

    allTreeData.value = _.cloneDeep(res.data || []);
    regionTreeData.value = _.cloneDeep(allTreeData.value);

    // 重置匹配状态
    matchedNodeIds.value.clear();
    clearMatchedFlag(regionTreeData.value);

    // 重置用户展开记录
    userExpandedKeys.value.clear();

    // 设置默认展开第一个根节点
    if (regionTreeData.value.length > 0) {
      topRegionId.value = regionTreeData.value[0].id;
      expandedRowKeys.value = [topRegionId.value];
      userExpandedKeys.value.add(topRegionId.value);
    }
  } catch (e) {
    console.error('加载数据失败:', e);
    sentry.captureError(e);
  } finally {
    tableLoading.value = false;
  }
}

// 清除所有节点的匹配标记
function clearMatchedFlag(treeData) {
  treeData.forEach(node => {
    node._matched = false;
    if (node.children && node.children.length > 0) {
      clearMatchedFlag(node.children);
    }
  });
}

// 获取层级颜色
function getLevelColor(level) {
  const colors = {
    1: 'blue',
    2: 'green',
    3: 'orange',
    4: 'purple',
  };
  return colors[level] || 'default';
}

// 获取层级文本
function getLevelText(level) {
  const texts = {
    1: '大区',
    2: '省/市',
    3: '城市',
    4: '区县',
  };
  return texts[level] || level;
}

// 重置查询
function resetQuery() {
  Object.assign(queryForm, queryFormState);
  // 直接显示全部数据
  regionTreeData.value = _.cloneDeep(allTreeData.value);
  matchedNodeIds.value.clear();
  clearMatchedFlag(regionTreeData.value);

  // 重置展开状态 - 使用用户之前手动展开的记录，否则使用默认
  if (userExpandedKeys.value.size > 0) {
    expandedRowKeys.value = Array.from(userExpandedKeys.value);
  } else if (regionTreeData.value.length > 0) {
    topRegionId.value = regionTreeData.value[0].id;
    expandedRowKeys.value = [topRegionId.value];
    userExpandedKeys.value.add(topRegionId.value);
  }
}

// 搜索
async function onSearch() {
  try {
    tableLoading.value = true;

    // 重置匹配状态
    matchedNodeIds.value.clear();

    // 构建查询参数
    const queryParams = {
      regionName: queryForm.regionName || '',
      status: queryForm.status,
      regionManagerName: queryForm.regionManagerName || '',
      contactPhone: queryForm.contactPhone || ''
    };

    // 如果没有查询条件，显示全部数据
    if (!queryForm.regionName && queryForm.status === undefined &&
        !queryForm.regionManagerName && !queryForm.contactPhone) {
      regionTreeData.value = _.cloneDeep(allTreeData.value);
      clearMatchedFlag(regionTreeData.value);

      // 重置展开状态 - 使用用户手动展开的记录
      if (userExpandedKeys.value.size > 0) {
        expandedRowKeys.value = Array.from(userExpandedKeys.value);
      } else if (regionTreeData.value.length > 0) {
        topRegionId.value = regionTreeData.value[0].id;
        expandedRowKeys.value = [topRegionId.value];
        userExpandedKeys.value.add(topRegionId.value);
      }
      return;
    }

    // 调用后端查询接口
    let res = await regionApi.queryTree(queryParams);

    if (!res.data || res.data.length === 0) {
      // 没有查询结果
      regionTreeData.value = [];
      expandedRowKeys.value = [];
      matchedNodeIds.value.clear();
      message.info('未找到匹配的区域');
      return;
    }

    // 保存查询结果
    const queryResult = _.cloneDeep(res.data);

    // 标记匹配的节点
    const matchIds = new Set();
    markMatchedNodes(queryResult, matchIds);
    matchedNodeIds.value = matchIds;

    // 更新数据
    regionTreeData.value = queryResult;

    // 收集所有需要展开的节点ID（匹配节点及其所有父节点）
    const expandIds = new Set();
    collectAllParentIdsForMatches(queryResult, matchIds, expandIds);

    // 合并用户手动展开的节点和自动展开的节点
    const finalExpandIds = new Set([
      ...Array.from(userExpandedKeys.value),
      ...Array.from(expandIds)
    ]);

    // 设置展开的节点 - 使用新的展开逻辑
    expandedRowKeys.value = Array.from(finalExpandIds);

    // 等待DOM更新后，滚动到第一个匹配节点
    nextTick(() => {
      if (matchIds.size > 0) {
        const firstMatchedId = Array.from(matchIds)[0];
        highlightAndScrollToRow(firstMatchedId);
      }
    });

  } catch (e) {
    console.error('查询失败:', e);
    sentry.captureError(e);
    message.error('查询失败：' + (e.message || '未知错误'));
  } finally {
    tableLoading.value = false;
  }
}

// 标记匹配的节点（递归标记）
function markMatchedNodes(treeData, matchIds) {
  treeData.forEach(node => {
    // 检查当前节点是否匹配
    const isMatch = checkNodeMatch(node, queryForm);
    node._matched = isMatch;

    if (isMatch) {
      matchIds.add(node.id);
    }

    // 递归处理子节点
    if (node.children && node.children.length > 0) {
      markMatchedNodes(node.children, matchIds);
    }
  });
}

// 收集匹配节点及其所有父节点ID（递归收集完整的父链）
function collectAllParentIdsForMatches(treeData, matchIds, expandIds) {
  // 收集所有匹配节点及其完整父链
  matchIds.forEach(matchId => {
    // 查找匹配节点的所有父节点（包括顶级父节点）
    const parentChain = findParentChain(matchId, treeData);
    parentChain.forEach(id => expandIds.add(id));
  });

  // 同时展开所有有子节点的匹配节点（确保能看到子节点）
  function expandMatchedNodesWithChildren(data) {
    data.forEach(node => {
      if (matchIds.has(node.id) && node.children && node.children.length > 0) {
        expandIds.add(node.id);
        expandMatchedNodesWithChildren(node.children);
      }
      if (node.children && node.children.length > 0) {
        expandMatchedNodesWithChildren(node.children);
      }
    });
  }
  expandMatchedNodesWithChildren(treeData);
}

// 查找节点的完整父节点链
function findParentChain(nodeId, treeData, chain = []) {
  // 先查找节点本身
  const node = findNodeById(nodeId, treeData);
  if (!node) return chain;

  // 如果节点有parentId，继续查找父节点
  if (node.parentId) {
    const parentNode = findNodeById(node.parentId, treeData);
    if (parentNode) {
      chain.push(parentNode.id);
      return findParentChain(parentNode.id, treeData, chain);
    }
  } else if (treeData.some(item => item.id === nodeId)) {
    // 如果是根节点，添加到链中
    chain.push(nodeId);
  }

  return chain;
}

// 根据ID查找节点
function findNodeById(nodeId, treeData) {
  for (const node of treeData) {
    if (node.id === nodeId) {
      return node;
    }
    if (node.children && node.children.length > 0) {
      const found = findNodeById(nodeId, node.children);
      if (found) return found;
    }
  }
  return null;
}

// 检查节点是否匹配查询条件
function checkNodeMatch(node, query) {
  // 区域名称筛选（支持区域名称和简称）
  const nameMatch = !query.regionName ||
      (node.regionName && node.regionName.toLowerCase().includes(query.regionName.toLowerCase())) ||
      (node.regionShortName && node.regionShortName.toLowerCase().includes(query.regionName.toLowerCase()));

  // 状态筛选
  const statusMatch = query.status === undefined ||
      (query.status === '' || node.status === parseInt(query.status));

  // 负责人筛选
  const managerMatch = !query.regionManagerName ||
      (node.regionManagerName && node.regionManagerName.toLowerCase().includes(query.regionManagerName.toLowerCase()));

  // 联系电话筛选
  const phoneMatch = !query.contactPhone ||
      (node.contactPhone && node.contactPhone.includes(query.contactPhone));

  return nameMatch && statusMatch && managerMatch && phoneMatch;
}

// 获取行类名，用于高亮匹配的行
function getRowClassName(record) {
  if (record._matched) {
    return 'qb-row-highlight';
  }
  return '';
}

// 处理展开/收缩事件
function handleExpand(expanded, record) {
  if (expanded) {
    // 添加展开的节点ID到用户手动展开记录
    userExpandedKeys.value.add(record.id);
    expandedRowKeys.value = [...expandedRowKeys.value, record.id];
  } else {
    // 从用户手动展开记录中移除
    userExpandedKeys.value.delete(record.id);
    expandedRowKeys.value = expandedRowKeys.value.filter(id => id !== record.id);
  }
}

// 高亮并滚动到指定行（辅助函数）
function highlightAndScrollToRow(nodeId) {
  const rowElement = document.querySelector(`tr[data-row-key="${nodeId}"]`);
  if (rowElement) {
    // 添加临时高亮效果
    rowElement.classList.add('qb-row-highlight-temp');

    // 滚动到该行
    rowElement.scrollIntoView({ behavior: 'smooth', block: 'center' });

    // 3秒后移除临时高亮
    setTimeout(() => {
      rowElement.classList.remove('qb-row-highlight-temp');
    }, 3000);
  }
}

// ----------------------- 表单操作 -----------------------
const regionFormRef = ref();

// 添加区域
function addRegion(e) {
  let data = {
    id: undefined,
    regionCode: '',
    regionName: '',
    regionShortName: '',
    parentId: e ? e.id : null,
    level: e ? (e.level + 1) : 1,
    sortOrder: 0,
    status: 1,
    regionManagerName: '',
    contactPhone: '',
    remark: '',
  };
  regionFormRef.value.show(data);
}

// 编辑区域
function updateRegion(e) {
  regionFormRef.value.show(e);
}

// 删除区域
function deleteRegion(id) {
  Modal.confirm({
    title: '提醒',
    icon: createVNode(ExclamationCircleOutlined),
    content: '确定要删除该区域吗?',
    okText: '删除',
    okType: 'danger',
    async onOk() {
      Loading.show();
      try {
        await regionApi.delete(id);
        message.success('删除成功');
        await loadInitialData();
      } catch (error) {
        sentry.captureError(error);
      } finally {
        Loading.hide();
      }
    },
    cancelText: '取消',
    onCancel() {},
  });
}
</script>

<style scoped lang="less">
.qb-region-management {
  .qb-text-grey {
    color: #999;
    margin-left: 4px;
  }

  .qb-text-highlight {
    color: #1890ff;
    font-weight: bold;
  }

  .qb-table-operate {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .ant-btn-link {
      padding: 0 4px;
    }
  }

  :deep(.ant-table-tbody) {
    .qb-row-highlight {
      background-color: #e6f7ff !important;

      &:hover {
        background-color: #d1e8ff !important;
      }

      .ant-table-cell {
        background-color: transparent;
      }
    }

    .qb-row-highlight-temp {
      animation: highlightBlink 1s ease-in-out 3;

      @keyframes highlightBlink {
        0%, 100% {
          background-color: #e6f7ff;
        }
        50% {
          background-color: #fffb8f;
        }
      }
    }
  }
}
</style>
