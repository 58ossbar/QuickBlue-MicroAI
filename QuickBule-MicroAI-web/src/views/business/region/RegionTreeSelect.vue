<!--
  * 区域树选择组件（支持智能搜索）
  * @Author: zhujw
  * @Date: 2025-12-20
-->
<template>
  <a-tree-select
      v-model:value="innerValue"
      :tree-data="treeData"
      placeholder="请选择区域"
      :style="{ width }"
      :multiple="multiple"
      :treeCheckable="multiple"
      :show-search="true"
      :tree-default-expand-all="false"
      :dropdown-match-select-width="false"
      :filterTreeNode="filterTreeNode"
      :tree-node-filter-prop="'title'"
      :fieldNames="{ children: 'children', label: 'displayName', value: 'id' }"
      :allowClear="true"
      :disabled="disabled"
      :tree-expand-on-click-node="true"
      :tree-line="true"
      @change="handleChange"
      @search="handleSearch"
      @dropdownVisibleChange="handleDropdownVisibleChange"
  />
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue';
import { regionApi } from '/@/api/business/region/region-api';
import { sentry } from '/@/lib/sentry';

const props = defineProps({
  modelValue: [String, Number, Array],
  width: {
    type: String,
    default: '100%',
  },
  multiple: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  // 是否排除当前节点及其子节点（用于编辑时不能选择自己作为父节点）
  excludeNodeId: {
    type: String,
    default: '',
  },
  // 是否只显示叶子节点
  leafOnly: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:modelValue', 'change']);

const innerValue = ref(props.modelValue);
const treeData = ref([]);
const originalTreeData = ref([]); // 保存原始数据
const searchKeyword = ref(''); // 搜索关键词

// 计算显示名称（包含编码和名称）
const computeDisplayName = (node) => {
  if (node.regionCode && node.regionCode !== node.regionName) {
    return `${node.regionCode} - ${node.regionName}`;
  }
  return node.regionName;
};

// 转换树数据，添加显示名称
const transformTreeData = (nodes) => {
  if (!nodes || !Array.isArray(nodes)) return [];

  return nodes.map(node => {
    // 处理排除的节点
    if (props.excludeNodeId && node.id === props.excludeNodeId) {
      return null;
    }

    // 如果只显示叶子节点，检查是否有子节点
    if (props.leafOnly && node.children && node.children.length > 0) {
      return null;
    }

    const displayName = computeDisplayName(node);

    const transformedNode = {
      ...node,
      displayName: displayName,
      title: displayName,
      value: node.id,
      key: node.id,
      disabled: props.disabled || (props.excludeNodeId === node.id)
    };

    // 递归转换子节点
    if (node.children && node.children.length > 0) {
      const children = transformTreeData(node.children);
      if (children.length > 0) {
        transformedNode.children = children;
      } else {
        delete transformedNode.children;
      }
    }

    return transformedNode;
  }).filter(node => node !== null);
};

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  innerValue.value = newVal;
});

// 监听内部值变化
watch(innerValue, (newVal) => {
  emit('update:modelValue', newVal);
  emit('change', newVal);
});

// 监听排除节点变化
watch(() => props.excludeNodeId, () => {
  if (originalTreeData.value.length > 0) {
    treeData.value = transformTreeData(originalTreeData.value);
  }
});

// 获取区域树数据
async function loadRegionTree() {
  try {
    const res = await regionApi.treeList();
    originalTreeData.value = res.data || [];
    treeData.value = transformTreeData(originalTreeData.value);
  } catch (error) {
    console.error('加载区域树失败:', error);
    sentry.captureError(error);
  }
}

// 自定义筛选函数
const filterTreeNode = (inputValue, treeNode) => {
  if (!inputValue) return true;

  const keyword = inputValue.toLowerCase();
  const node = treeNode;

  // 搜索区域编码
  if (node.regionCode && node.regionCode.toLowerCase().includes(keyword)) {
    return true;
  }

  // 搜索区域名称
  if (node.regionName && node.regionName.toLowerCase().includes(keyword)) {
    return true;
  }

  // 搜索区域简称
  if (node.regionShortName && node.regionShortName.toLowerCase().includes(keyword)) {
    return true;
  }

  // 搜索显示名称
  if (node.displayName && node.displayName.toLowerCase().includes(keyword)) {
    return true;
  }

  return false;
};

// 处理搜索
function handleSearch(value) {
  searchKeyword.value = value.toLowerCase();
}

// 处理下拉框显示/隐藏
function handleDropdownVisibleChange(open) {
  if (open) {
    // 下拉框打开时，如果没有数据则加载
    if (treeData.value.length === 0) {
      loadRegionTree();
    }
  }
}

// 处理选择变化
function handleChange(value, node, extra) {
  console.log('选择变化:', { value, node, extra });
  emit('change', value);
}

onMounted(() => {
  loadRegionTree();
});

// 暴露刷新方法
function refresh() {
  loadRegionTree();
}

defineExpose({ refresh });
</script>

<style scoped>
/* 可以添加一些自定义样式 */
:deep(.ant-select-tree) {
  max-height: 400px;
  overflow-y: auto;
}

:deep(.ant-select-tree-treenode) {
  padding: 4px 0;
}

:deep(.ant-select-tree-node-content-wrapper) {
  min-height: 24px;
  line-height: 24px;
}

:deep(.ant-select-tree-title) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
