<!--
  * 角色 数据范围 - 优化版本
  * 表格化布局,适合业务模块较多的场景
-->
<template>
  <div class="data-scope-wrapper">
    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-area">
        <a-input
          v-model:value="searchKeyword"
          placeholder="搜索业务模块..."
          allow-clear
          style="width: 240px"
          size="middle"
        >
          <template #prefix>
            <SearchOutlined />
          </template>
        </a-input>
        <a-select
          v-model:value="filterStatus"
          placeholder="配置状态"
          allow-clear
          style="width: 100px; margin-left: 12px"
          size="middle"
        >
          <a-select-option :value="1">已配置</a-select-option>
          <a-select-option :value="0">未配置</a-select-option>
        </a-select>
      </div>
      <div class="btn-group">
        <a-button @click="applyDefaultScope" ghost size="small">
          <template #icon><ApiOutlined /></template>
          应用默认
        </a-button>
        <a-button @click="resetAll" ghost danger size="small">
          <template #icon><ReloadOutlined /></template>
          重置
        </a-button>
        <a-button type="primary" @click="updateDataScope" v-privilege="'system:role:dataScope:update'" :loading="saving" size="small">
          <template #icon><SaveOutlined /></template>
          保存配置
        </a-button>
      </div>
    </div>

    <!-- 数据范围配置表格 -->
    <div class="data-container">
      <a-table
        :dataSource="filteredDataScopeList"
        :columns="columns"
        :pagination="false"
        :scroll="{ y: 450 }"
        rowKey="dataScopeType"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'moduleName'">
            <div class="module-cell">
              <div class="module-icon">
                <DatabaseOutlined />
              </div>
              <div class="module-info">
                <div class="module-name">{{ record.dataScopeTypeName }}</div>
                <div class="module-desc">{{ record.dataScopeTypeDesc }}</div>
              </div>
            </div>
          </template>

          <template v-if="column.key === 'viewType'">
            <a-radio-group
              v-model:value="getSelectedItem(record.dataScopeType).viewType"
              @change="onScopeChange(record.dataScopeType)"
              button-style="solid"
              size="small"
            >
              <a-radio-button
                v-for="scope in record.viewTypeList"
                :key="`${record.dataScopeType}-${scope.viewType}`"
                :value="scope.viewType"
                class="scope-radio"
              >
                {{ scope.viewTypeName }}
              </a-radio-button>
            </a-radio-group>
          </template>

          <template v-if="column.key === 'action'">
            <a-button
              type="link"
              size="small"
              @click="clearScope(record.dataScopeType)"
              v-if="hasConfig(record.dataScopeType)"
            >
              清除
            </a-button>
            <a-button
              type="link"
              size="small"
              @click="applyDefault(record.dataScopeType, record)"
              v-else
            >
              应用默认
            </a-button>
          </template>
        </template>
      </a-table>

      <!-- 底部统计信息 -->
      <div class="footer-statistics">
        <span class="stat-item">
          <span class="stat-label">业务模块:</span>
          <span class="stat-value">{{ dataScopeList.length }}</span>
        </span>
        <span class="stat-item">
          <span class="stat-label">已配置:</span>
          <span class="stat-value configured">{{ configuredCount }}</span>
        </span>
        <span class="stat-item">
          <span class="stat-label">未配置:</span>
          <span class="stat-value unconfigured">{{ unconfiguredCount }}</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { message } from 'ant-design-vue';
import { Modal } from 'ant-design-vue';
import _ from 'lodash';
import { inject, onMounted, ref, watch, computed } from 'vue';
import {
  ApiOutlined,
  SaveOutlined,
  ReloadOutlined,
  DatabaseOutlined,
  SearchOutlined
} from '@ant-design/icons-vue';
import { roleApi } from '/@/api/system/role-api';
import { sentry } from '/@/lib/sentry';

const props = defineProps({
  value: [Number, String],
});

defineEmits(['update:value']);

// ----------------------- 数据 ---------------------------------

let selectRoleId = inject('selectRoleId');
let dataScopeList = ref([]);
let selectedDataScopeList = ref([]);
let saving = ref(false);
let searchKeyword = ref('');
let filterStatus = ref(undefined);

// 表格列定义
const columns = [
  {
    title: '业务模块',
    key: 'moduleName',
    width: '35%',
    fixed: 'left',
  },
  {
    title: '数据范围',
    key: 'viewType',
    width: '55%',
  },
  {
    title: '操作',
    key: 'action',
    width: '10%',
    align: 'center',
  },
];

// 过滤后的数据列表
const filteredDataScopeList = computed(() => {
  let result = dataScopeList.value;

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(item =>
      item.dataScopeTypeName.toLowerCase().includes(keyword) ||
      item.dataScopeTypeDesc.toLowerCase().includes(keyword)
    );
  }

  // 状态过滤
  if (filterStatus.value !== undefined) {
    result = result.filter((item, index) => {
      const isConfigured = !_.isUndefined(selectedDataScopeList.value[index]?.viewType);
      return filterStatus.value === 1 ? isConfigured : !isConfigured;
    });
  }

  return result;
});

// 已配置数量
const configuredCount = computed(() => {
  return selectedDataScopeList.value.filter(item => !_.isUndefined(item.viewType)).length;
});

// 未配置数量
const unconfiguredCount = computed(() => {
  return selectedDataScopeList.value.filter(item => _.isUndefined(item.viewType)).length;
});

watch(
  () => selectRoleId.value,
  () => getRoleDataScope()
);

onMounted(getDataScope);

// ----------------------- 方法 ---------------------------------

// 根据 dataScopeType 获取选中的数据项
function getSelectedItem(dataScopeType) {
  return selectedDataScopeList.value.find(item => item.dataScopeType == dataScopeType) || { viewType: undefined, dataScopeType };
}

// 根据 dataScopeType 获取原始数据项
function getDataScopeItem(dataScopeType) {
  return dataScopeList.value.find(item => item.dataScopeType == dataScopeType);
}

// 判断是否已配置
function hasConfig(dataScopeType) {
  const item = getSelectedItem(dataScopeType);
  return !_.isUndefined(item?.viewType);
}

// 数据范围变化时
function onScopeChange(dataScopeType) {
  // 可以在这里添加即时保存的提示
}

// 清除单个配置
function clearScope(dataScopeType) {
  const item = getSelectedItem(dataScopeType);
  if (item) {
    item.viewType = undefined;
    message.success('已清除配置');
  }
}

// 应用单个默认配置
function applyDefault(dataScopeType, record) {
  const item = getSelectedItem(dataScopeType);
  // 使用配置的默认视图类型
  const defaultViewType = record?.defaultViewType;
  if (item && defaultViewType !== undefined && defaultViewType !== null) {
    item.viewType = defaultViewType;
    message.success('已应用默认配置');
  } else if (item && record?.viewTypeList?.length > 0) {
    // 如果没有配置默认值，则使用第一个选项
    item.viewType = record.viewTypeList[0].viewType;
    message.success('已应用默认配置');
  } else {
    message.warning('未找到默认配置');
  }
}

// 应用默认权限
function applyDefaultScope() {
  Modal.confirm({
    title: '应用默认权限',
    content: '确定要为所有业务模块应用默认的数据权限吗？这将覆盖当前配置。',
    okText: '确定',
    cancelText: '取消',
    onOk: () => {
      // 使用配置的默认视图类型
      selectedDataScopeList.value.forEach((item) => {
        const dataScopeItem = getDataScopeItem(item.dataScopeType);
        // 优先使用配置的默认视图类型
        const defaultViewType = dataScopeItem?.defaultViewType;
        if (defaultViewType !== undefined && defaultViewType !== null) {
          item.viewType = defaultViewType;
        } else if (dataScopeItem?.viewTypeList?.length > 0) {
          // 如果没有配置默认值，则使用第一个选项
          item.viewType = dataScopeItem.viewTypeList[0].viewType;
        }
      });
      message.success('已应用默认权限');
    },
  });
}

// 重置所有
function resetAll() {
  Modal.confirm({
    title: '重置所有配置',
    content: '确定要清空所有数据权限配置吗？配置后将使用系统默认权限。',
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: () => {
      selectedDataScopeList.value.forEach(item => {
        item.viewType = undefined;
      });
      message.success('已重置所有配置');
    },
  });
}

// 获取系统支持的所有种类的数据范围
async function getDataScope() {
  try {
    let result = await roleApi.getDataScopeList();
    dataScopeList.value = result.data || [];

    selectedDataScopeList.value = [];
    dataScopeList.value.forEach((item) => {
      selectedDataScopeList.value.push({
        viewType: undefined,
        dataScopeType: item.dataScopeType,
      });
    });
    getRoleDataScope();
  } catch (e) {
    sentry.captureError(e);
    message.error('加载数据范围失败');
  }
}

// 获取数据范围根据角色id，并赋予选中状态
async function getRoleDataScope() {
  if (!selectRoleId.value) return;
  try {
    let result = await roleApi.getDataScopeByRoleId(selectRoleId.value);
    let data = result.data || [];
    selectedDataScopeList.value = [];

    dataScopeList.value.forEach((item) => {
      let find = data.find((e) => e.dataScopeType == item.dataScopeType);
      selectedDataScopeList.value.push({
        viewType: find ? find.viewType : undefined,
        dataScopeType: item.dataScopeType,
      });
    });
  } catch (e) {
    sentry.captureError(e);
  }
}

// 更新数据范围
async function updateDataScope() {
  if (!selectRoleId.value) {
    message.warning('请先选择角色');
    return;
  }

  try {
    saving.value = true;
    let data = {
      roleId: selectRoleId.value,
      dataScopeItemList: selectedDataScopeList.value.filter((e) => !_.isUndefined(e.viewType)),
    };

    await roleApi.updateDataScope(data);
    message.success('保存成功');
  } catch (e) {
    sentry.captureError(e);
    message.error('保存失败');
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped lang="less">
.data-scope-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 12px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #fafafa;
  border-radius: 6px;
  flex-wrap: wrap;
  gap: 8px;

  .search-area {
    display: flex;
    align-items: center;
    flex: 1;
  }

  .btn-group {
    display: flex;
    gap: 6px;
  }
}

.data-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  :deep(.ant-table-wrapper) {
    flex: 1;
    overflow: hidden;
  }

  :deep(.ant-table) {
    font-size: 13px;
  }
}

.module-cell {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 4px 0;

  .module-icon {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 6px;
    color: #fff;
    font-size: 16px;
    flex-shrink: 0;
  }

  .module-info {
    flex: 1;
    min-width: 0;

    .module-name {
      font-size: 13px;
      font-weight: 600;
      color: #262626;
      margin-bottom: 2px;
    }

    .module-desc {
      font-size: 12px;
      color: #8c8c8c;
      line-height: 1.3;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }
}

.scope-radio {
  :deep(.ant-radio-button-wrapper) {
    height: 28px;
    line-height: 26px;
    font-size: 12px;
    padding: 0 12px;
  }
}

.footer-statistics {
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 6px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 12px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;

    .stat-label {
      color: #8c8c8c;
    }

    .stat-value {
      font-size: 13px;
      font-weight: 600;
      color: #262626;

      &.configured {
        color: #3f8600;
      }

      &.unconfigured {
        color: #cf1322;
      }
    }
  }
}
</style>
