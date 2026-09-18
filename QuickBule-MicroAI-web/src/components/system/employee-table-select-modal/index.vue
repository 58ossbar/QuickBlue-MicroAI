<!--
  * 员工 表格 弹窗 选择框
  *
-->
<template>
  <a-modal
      v-model:open="visible"
      :width="900"
      :title="`选择人员（${assignedCount}个已分配）`"
      @cancel="closeModal"
      @ok="onSelectEmployee"
  >
    <!-- 提示信息 -->
    <div style="margin-bottom: 16px;">
      <a-alert
          message="提示"
          description="已分配的核销员默认选中且不可取消，可以继续选择其他人员。带有'已分配到其他影城'标签的员工表示已分配到其他影城，不能重复分配。"
          type="info"
          show-icon
          style="margin-bottom: 16px;"
      />
    </div>

    <a-form class="qb-query-form">
      <a-row class="qb-query-form-row">
        <a-form-item label="关键字" class="qb-query-form-item">
          <a-input style="width: 150px" v-model:value="params.keyword" placeholder="姓名/手机/账号" />
        </a-form-item>
        <a-form-item label="部门" class="qb-query-form-item">
          <DepartmentTreeSelect style="width: 200px" ref="departmentTreeSelect" v-model:value="params.departmentId" />
        </a-form-item>
        <a-form-item label="状态" class="qb-query-form-item">
          <a-select style="width: 120px" v-model:value="params.disabledFlag" placeholder="请选择状态" allowClear>
            <a-select-option :key="1"> 禁用 </a-select-option>
            <a-select-option :key="0"> 启用 </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item class="qb-query-form-item qb-margin-left10">
          <a-button type="primary" @click="onSearch">
            <template #icon>
              <SearchOutlined />
            </template>
            查询
          </a-button>
          <a-button @click="reset" class="qb-margin-left10">
            <template #icon>
              <ReloadOutlined />
            </template>
            重置
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>

    <a-table
        :row-selection="{
        selectedRowKeys: selectedRowKeyList,
        onChange: onSelectChange,
        getCheckboxProps: getCheckboxProps
      }"
        :loading="tableLoading"
        size="small"
        :columns="columns"
        :data-source="tableData"
        :pagination="false"
        bordered
        rowKey="employeeId"
        :scroll="{ y: 300 }"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'actualName'">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span>{{ text || '-' }}</span>
            <a-tag v-if="isAlreadyAssigned(record.employeeId)" color="blue" style="font-size: 12px; height: 20px; line-height: 18px;">
              已分配
            </a-tag>
            <a-tag v-if="isAssignedToOtherCinema(record.employeeId)" color="orange" style="font-size: 12px; height: 20px; line-height: 18px;">
              已分配到其他影城
            </a-tag>
            <!-- 调试信息 -->
            <span style="color: red; font-size: 10px;">
              [{{ isAlreadyAssigned(record.employeeId) ? 'Y' : 'N' }}, {{ isAssignedToOtherCinema(record.employeeId) ? 'Y' : 'N' }}]
            </span>
          </div>
        </template>

        <template v-if="column.dataIndex === 'disabledFlag'">
          <a-tag :color="text ? 'error' : 'processing'" style="margin: 0;">
            {{ text ? '禁用' : '启用' }}
          </a-tag>
        </template>

        <template v-if="column.dataIndex === 'gender'">
          <span>{{ getGenderDesc(text) }}</span>
        </template>

        <template v-if="column.dataIndex === 'departmentName'">
          <span>{{ text || '-' }}</span>
        </template>

        <template v-if="column.dataIndex === 'phone'">
          <span>{{ text || '-' }}</span>
        </template>

        <template v-if="column.dataIndex === 'loginName'">
          <span>{{ text || '-' }}</span>
        </template>
      </template>
    </a-table>

    <div class="qb-query-table-page" style="margin-top: 16px;">
      <a-pagination
          showSizeChanger
          showQuickJumper
          show-less-items
          :pageSizeOptions="PAGE_SIZE_OPTIONS"
          :defaultPageSize="params.pageSize"
          v-model:current="params.pageNum"
          v-model:pageSize="params.pageSize"
          :total="total"
          @change="queryEmployee"
          :show-total="(total) => `共${total}条`"
      />
    </div>
  </a-modal>
</template>

<script setup>
import { message } from 'ant-design-vue';
import { reactive, ref, computed } from 'vue';
import { employeeApi } from '/@/api/system/employee-api';
import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import DepartmentTreeSelect from '/@/components/system/department-tree-select/index.vue';
import { sentry } from '/@/lib/sentry';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import { cinemaApi } from '/@/api/business/cinema/cinema-api';

// ----------------------- 以下是字段定义 emits props ---------------------
const emits = defineEmits(['selectData']);

// 暴露方法给父组件
defineExpose({
  showModal,
  closeModal
});

// ----------------------- modal  显示与隐藏 ---------------------

const visible = ref(false);
const allAssignedEmployeeIds = ref([]); // 所有已分配到影城的员工ID

// 加载所有已分配影城的员工ID
async function loadAllAssignedEmployees() {
  console.log('开始加载所有已分配影城的员工ID...');
  try {
    const res = await cinemaApi.getAllAssignedEmployees();
    console.log('API返回的完整响应:', res);
    console.log('API返回的ok状态:', res.ok);
    console.log('API返回的msg:', res.msg);
    console.log('API返回的数据类型:', typeof res.data);
    console.log('API返回的数据内容:', res.data);
    console.log('API返回的数据是否为数组:', Array.isArray(res.data));

    // 处理可能的数据格式
    let employeeIds = [];
    if (res.ok && res.data) {
      if (Array.isArray(res.data)) {
        employeeIds = res.data;
      } else if (typeof res.data === 'object' && res.data.list) {
        employeeIds = res.data.list;
      } else if (typeof res.data === 'object' && res.data.data) {
        employeeIds = res.data.data;
      }
    }

    console.log('解析前的employeeIds:', employeeIds);

    allAssignedEmployeeIds.value = employeeIds.map(id => {
      const numId = Number(id);
      console.log(`转换ID: ${id} -> ${numId}`);
      return numId;
    });

    console.log('所有已分配影城的员工ID (最终):', allAssignedEmployeeIds.value);
    console.log('allAssignedEmployeeIds.value类型:', typeof allAssignedEmployeeIds.value);
    console.log('allAssignedEmployeeIds.value是否为数组:', Array.isArray(allAssignedEmployeeIds.value));
  } catch (error) {
    console.error('获取已分配员工列表失败:', error);
    console.error('错误详情:', JSON.stringify(error));
    allAssignedEmployeeIds.value = [];
  }
}

async function showModal(selectEmployeeIds) {
  // 确保传入的是数组
  originalRowKeyList.value = Array.isArray(selectEmployeeIds) ? [...selectEmployeeIds] : [];
  // 设置默认选中的ID（包括已分配的）
  selectedRowKeyList.value = [...originalRowKeyList.value];
  visible.value = true;

  console.log('弹窗接收到的已分配员工ID:', originalRowKeyList.value);
  console.log('初始选中的员工ID:', selectedRowKeyList.value);

  // 重置查询参数
  Object.assign(params, defaultParams);

  // 先加载所有已分配影城的员工ID
  await loadAllAssignedEmployees();

  // 确保数据加载完成后再查询员工列表
  await onSearch();

  console.log('表格数据加载完成，allAssignedEmployeeIds:', allAssignedEmployeeIds.value);
  console.log('表格数据:', tableData.value);
}

function closeModal() {
  Object.assign(params, defaultParams);
  originalRowKeyList.value = [];
  selectedRowKeyList.value = [];
  visible.value = false;
}

// ----------------------- 员工查询表单与查询 ---------------------
const tableLoading = ref(false);
const departmentTreeSelect = ref();
const total = ref();

let defaultParams = {
  departmentId: undefined,
  disabledFlag: undefined,
  employeeIdList: undefined,
  keyword: undefined,
  searchCount: undefined,
  pageNum: 1,
  pageSize: PAGE_SIZE,
  sortItemList: undefined,
};
const params = reactive({ ...defaultParams });

function reset() {
  Object.assign(params, defaultParams);
  if (departmentTreeSelect.value) {
    departmentTreeSelect.value.reset();
  }
  queryEmployee();
}

function onSearch() {
  params.pageNum = 1;
  return queryEmployee();
}

async function queryEmployee() {
  tableLoading.value = true;
  try {
    let res = await employeeApi.queryEmployee(params);
    tableData.value = res.data.list || [];
    total.value = res.data.total || 0;

    console.log('查询到的员工数据:', tableData.value);
    console.log('已分配的员工ID:', originalRowKeyList.value);
  } catch (error) {
    console.error('查询员工失败:', error);
    sentry.captureError(error);
    tableData.value = [];
    total.value = 0;
  } finally {
    tableLoading.value = false;
  }
}

// ----------------------- 员工表格选择 ---------------------
const originalRowKeyList = ref([]); // 原始已分配的员工ID
let selectedRowKeyList = ref([]); // 当前选中的员工ID

function onSelectChange(selectedRowKeys) {
  console.log('选择变化，新选的ID:', selectedRowKeys);
  console.log('之前选中的ID:', selectedRowKeyList.value);
  console.log('已分配的ID:', originalRowKeyList.value);
  console.log('所有已分配到影城的员工ID:', allAssignedEmployeeIds.value);

  // 过滤掉已分配到其他影城的员工
  const validSelectedKeys = selectedRowKeys.filter(id => {
    // 如果是当前影城已分配的员工，保留
    if (originalRowKeyList.value.includes(id)) {
      return true;
    }
    // 如果是新选择的员工，但已分配到其他影城，过滤掉
    if (allAssignedEmployeeIds.value.includes(id)) {
      return false;
    }
    return true;
  });

  // 确保已分配的员工不会被取消选中
  const mustKeepIds = originalRowKeyList.value.filter(id =>
      validSelectedKeys.includes(id) || selectedRowKeyList.value.includes(id)
  );

  // 合并：已分配的 + 新选的（已过滤）
  const finalSelected = [...new Set([...mustKeepIds, ...validSelectedKeys])];
  selectedRowKeyList.value = finalSelected;

  console.log('最终选中的员工ID:', selectedRowKeyList.value);
}

function onSelectEmployee() {
  if (selectedRowKeyList.value.length === 0) {
    message.warning('请选择核销人员');
    return;
  }

  console.log('最终提交的员工ID列表:', selectedRowKeyList.value);

  // 验证：检查是否有已分配到其他影城的员工
  const invalidEmployeeIds = selectedRowKeyList.value.filter(id =>
      allAssignedEmployeeIds.value.includes(id) &&
      !originalRowKeyList.value.includes(id)
  );

  if (invalidEmployeeIds.length > 0) {
    // 获取这些员工的姓名
    const invalidEmployees = tableData.value.filter(emp =>
        invalidEmployeeIds.includes(emp.employeeId)
    );

    if (invalidEmployees.length > 0) {
      const names = invalidEmployees.map(e => e.actualName).join('、');
      message.error(`以下员工已分配到其他影城，无法重复分配：${names}`);
      return;
    }
  }

  // 直接传回所有选中的员工ID
  emits('selectData', selectedRowKeyList.value);
  closeModal();
}

function getCheckboxProps(record) {
  const isAssigned = originalRowKeyList.value.includes(record.employeeId);
  // 检查是否已分配到其他影城
  const isAssignedToOtherCinema = allAssignedEmployeeIds.value.includes(record.employeeId) &&
      !isAssigned;
  console.log(`员工 ${record.employeeId} ${record.actualName} 是否已分配:`, isAssigned, '是否已分配到其他影城:', isAssignedToOtherCinema);
  return {
    // 已分配的员工默认选中
    defaultChecked: isAssigned,
    // 已分配的员工或已分配到其他影城的员工禁止选择
    disabled: isAssigned || isAssignedToOtherCinema
  };
}

// 判断员工是否已分配到当前影城
const isAlreadyAssigned = (employeeId) => {
  return originalRowKeyList.value.includes(employeeId);
}

// 判断员工是否已分配到其他影城
const isAssignedToOtherCinema = (employeeId) => {
  return allAssignedEmployeeIds.value.includes(employeeId) &&
      !originalRowKeyList.value.includes(employeeId);
}

// 计算已分配员工数量（用于显示提示）
const assignedCount = computed(() => {
  return originalRowKeyList.value.length;
});

// 获取性别描述
function getGenderDesc(genderValue) {
  if (typeof $enumPlugin !== 'undefined' && $enumPlugin.getDescByValue) {
    return $enumPlugin.getDescByValue('GENDER_ENUM', genderValue);
  }
  // 备用方案
  const genderMap = {
    0: '未知',
    1: '男',
    2: '女'
  };
  return genderMap[genderValue] || '未知';
}

// ----------------------- 员工表格渲染 ---------------------
const tableData = ref([]);

// 表格列定义
const columns = [
  {
    title: '姓名',
    dataIndex: 'actualName',
    width: 120,
    ellipsis: true,
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    width: 120,
    ellipsis: true,
  },
  {
    title: '登录账号',
    dataIndex: 'loginName',
    width: 120,
    ellipsis: true,
  },
  {
    title: '状态',
    dataIndex: 'disabledFlag',
    width: 80,
  },
  {
    title: '部门',
    dataIndex: 'departmentName',
    width: 120,
    ellipsis: true,
  },
  {
    title: '性别',
    dataIndex: 'gender',
    width: 80,
  },
];
</script>
