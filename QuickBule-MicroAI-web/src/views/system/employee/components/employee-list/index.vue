<!--
  *  员工 列表
  *

-->
<template>
  <a-card class="employee-container" :bordered="false">
    <div class="header">
      <a-typography-title :level="5">部门人员</a-typography-title>
    </div>
    <a-form class="query-bar" layout="inline" :label-col="{ style: { width: '70px' } }">
      <a-form-item label="状态">
        <a-radio-group v-model:value="params.disabledFlag" size="small" @change="queryEmployeeByKeyword(false)">
          <a-radio-button :value="undefined">全部</a-radio-button>
          <a-radio-button :value="false">启用</a-radio-button>
          <a-radio-button :value="true">禁用</a-radio-button>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="范围">
        <a-tooltip :title="params.includeSubDepartment ? '显示本级及子部门员工' : '仅显示本级部门员工'">
          <a-switch
            v-model:checked="params.includeSubDepartment"
            @change="queryEmployee"
          >
            <template #checkedChildren>含子级</template>
            <template #unCheckedChildren>仅本级</template>
          </a-switch>
        </a-tooltip>
      </a-form-item>
      <a-form-item label="关键字">
        <a-input
          v-model:value.trim="params.keyword"
          placeholder="姓名/手机号/登录账号"
          class="search-input"
          allow-clear
          @pressEnter="queryEmployeeByKeyword(true)"
        >
          <template #prefix><SearchOutlined /></template>
        </a-input>
      </a-form-item>
      <a-form-item>
        <a-space :size="8">
          <a-button type="primary" @click="queryEmployeeByKeyword(true)">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
          <a-button @click="reset">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
    <div class="btn-group">
      <a-space :size="8">
        <a-button type="primary" @click="showDrawer" v-privilege="'system:employee:add'">
          <template #icon><PlusOutlined /></template>
          添加成员
        </a-button>
        <a-button @click="updateEmployeeDepartment" v-privilege="'system:employee:department:update'">
          <template #icon><SwapOutlined /></template>
          调整部门
        </a-button>
        <a-button danger @click="batchDelete" v-privilege="'system:employee:delete'">
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
        <a-button @click="exportEmployee" v-privilege="'system:employee:export'">
          <template #icon><ExportOutlined /></template>
          导出
        </a-button>
        <a-button type="primary" ghost @click="showImportWizard" v-privilege="'system:employee:import'">
          <template #icon><ImportOutlined /></template>
          导入员工
        </a-button>
      </a-space>
      <span class="qb-table-column-operate">
        <Export :columns="columns" :dataSource="tableData" fileName="员工列表" title="员工列表" />
        <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.SYSTEM.EMPLOYEE" :refresh="queryEmployee" />
      </span>
    </div>

    <a-table
      ref="tableRef"
      :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      size="small"
      :columns="columns"
      :data-source="tableData"
      :pagination="false"
      :loading="tableLoading"
      :scroll="{ x: 'max-content' }"
      row-key="employeeId"
      bordered
      @change="handleTableChange"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'administratorFlag'">
          <a-tag color="error" v-if="text">超管</a-tag>
        </template>
        <template v-if="column.dataIndex === 'disabledFlag'">
          <a-tag :color="text ? 'error' : 'processing'">{{ text ? '禁用' : '启用' }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'gender'">
          <span>{{ $enumPlugin.getDescByValue('GENDER_ENUM', text) }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'operate'">
          <div class="qb-table-operate">
            <a-button v-privilege="'system:employee:update'" type="link" size="small" @click="showDrawer(record)">编辑</a-button>
            <a-button
              v-privilege="'system:employee:password:reset'"
              type="link"
              size="small"
              @click="resetPassword(record.employeeId, record.loginName)"
              >重置密码</a-button
            >
            <a-button v-privilege="'system:employee:disabled'" type="link" @click="updateDisabled(record.employeeId, record.disabledFlag)">{{
              record.disabledFlag ? '启用' : '禁用'
            }}</a-button>
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
        :defaultPageSize="params.pageSize"
        v-model:current="params.pageNum"
        v-model:pageSize="params.pageSize"
        :total="total"
        @change="queryEmployee"
        :show-total="showTableTotal"
      />
    </div>
    <EmployeeFormModal ref="employeeFormModal" @refresh="queryEmployee" @show-account="showAccount" />
    <EmployeeDepartmentFormModal ref="employeeDepartmentFormModal" @refresh="queryEmployee" />
    <EmployeePasswordDialog ref="employeePasswordDialog" />
    <EmployeeImportWizard ref="employeeImportWizard" @refresh="queryEmployee" />
  </a-card>
</template>
<script setup>
  import { ExclamationCircleOutlined, ExportOutlined, ImportOutlined, ReloadOutlined, SearchOutlined, PlusOutlined, SwapOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import { message, Modal } from 'ant-design-vue';
  import _ from 'lodash';
  import { computed, createVNode, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
  import { employeeApi } from '/@/api/system/employee-api';
  import { PAGE_SIZE } from '/@/constants/common-const';
  import { Loading } from '/@/components/framework/loading';
  import EmployeeFormModal from '../employee-form-modal/index.vue';
  import EmployeeDepartmentFormModal from '../employee-department-form-modal/index.vue';
  import EmployeePasswordDialog from '../employee-password-dialog/index.vue';
  import EmployeeImportWizard from '../employee-import-wizard/index.vue';
  import { PAGE_SIZE_OPTIONS, showTableTotal } from '/@/constants/common-const';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import Export from '/@/components/support/export/index.vue';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import departmentEmitter from '../../department-mitt';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  // ----------------------- 以下是字段定义 emits props ---------------------

  const props = defineProps({
    departmentId: [Number, String],
    breadcrumb: Array,
  });

  //-------------回显账号密码信息----------
  let employeePasswordDialog = ref();
  function showAccount(accountName, passWord) {
    employeePasswordDialog.value.showModal(accountName, passWord);
  }

  // ----------------------- 表格/列表/ 搜索 ---------------------
  //字段
  const columns = ref([
    {
      title: '姓名',
      dataIndex: 'actualName',
      key: 'actualName',
      width: 85,
      sorter: true,
    },
    {
      title: '性别',
      dataIndex: 'gender',
      key: 'gender',
      width: 70,
      enumName: 'GENDER_ENUM',
    },
    {
      title: '登录账号',
      dataIndex: 'loginName',
      key: 'loginName',
      width: 100,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      width: 85,
      sorter: true,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 100,
      ellipsis: true,
    },
    {
      title: '超管',
      dataIndex: 'administratorFlag',
      key: 'administratorFlag',
      width: 60,
    },
    {
      title: '状态',
      dataIndex: 'disabledFlag',
      key: 'disabledFlag',
      width: 60,
      sorter: true,
    },
    {
      title: '岗位',
      dataIndex: 'positionName',
      key: 'positionName',
      width: 100,
      ellipsis: true,
    },
    {
      title: '角色',
      dataIndex: 'roleNameList',
      key: 'roleNameList',
      width: 100,
    },
    {
      title: '部门',
      dataIndex: 'departmentName',
      key: 'departmentName',
      ellipsis: true,
      width: 200,
    },
    {
      title: '操作',
      dataIndex: 'operate',
      key: 'operate',
      width: 140,
    },
  ]);
  const tableData = ref();
  const tableRef = ref();

  // 启用列宽拖拽调整
  useColumnResize(tableRef, columns);

  let defaultParams = {
    departmentId: undefined,
    includeSubDepartment: true,  // 默认显示本级及子部门员工
    disabledFlag: false,
    keyword: undefined,
    searchCount: undefined,
    pageNum: 1,
    pageSize: PAGE_SIZE,
    sortItemList: undefined,
  };
  const params = reactive({ ...defaultParams });
  const total = ref(0);

  // 搜索重置
  function reset() {
    Object.assign(params, defaultParams);
    queryEmployee();
  }

  const tableLoading = ref(false);
  let queryVersion = 0; // 用于处理并发请求

  // 查询
  async function queryEmployee() {
    tableLoading.value = true;
    const currentVersion = ++queryVersion;
    try {
      // 确保 departmentId 有值
      if (params.departmentId == null) {
        params.departmentId = props.departmentId;
      }
      console.log('queryEmployee params:', JSON.stringify(params));
      console.log('queryEmployee departmentId:', params.departmentId, 'props.departmentId:', props.departmentId);
      let res = await employeeApi.queryEmployee(params);
      // 只处理最新的请求结果
      if (currentVersion !== queryVersion) {
        console.log('queryEmployee: 请求已被新请求替代，跳过');
        return;
      }
      console.log('queryEmployee result: total=', res.data.total);
      for (const item of res.data.list) {
        item.roleNameList = _.join(item.roleNameList, ',');
      }
      tableData.value = res.data.list;
      total.value = res.data.total;
      // 清除选中
      selectedRowKeys.value = [];
      selectedRows.value = [];
    } catch (error) {
      sentry.captureError(error);
    } finally {
      if (currentVersion === queryVersion) {
        tableLoading.value = false;
      }
    }
  }

  // 根据关键字 查询
  async function queryEmployeeByKeyword(allDepartment) {
    tableLoading.value = true;
    const currentVersion = ++queryVersion;
    try {
      params.pageNum = 1;
      params.departmentId = allDepartment ? undefined : props.departmentId;
      let res = await employeeApi.queryEmployee(params);
      // 只处理最新的请求结果
      if (currentVersion !== queryVersion) {
        return;
      }
      for (const item of res.data.list) {
        item.roleNameList = _.join(item.roleNameList, ',');
      }
      tableData.value = res.data.list;
      total.value = res.data.total;
      // 清除选中
      selectedRowKeys.value = [];
      selectedRows.value = [];
    } catch (error) {
      sentry.captureError(error);
    } finally {
      if (currentVersion === queryVersion) {
        tableLoading.value = false;
      }
    }
  }

  // 表格排序、筛选变化处理
  function handleTableChange(pagination, filters, sorter) {
    // 仅处理排序变化（sorter 有值且是排序动作）
    if (!sorter || !sorter.columnKey && !sorter.field) {
      return;
    }
    params.pageNum = 1;
    if (sorter.order) {
      // 后端 SortItem: { isAsc: Boolean, column: String }
      const column = sorter.field || sorter.columnKey;
      if (column) {
        params.sortItemList = [{ isAsc: sorter.order === 'ascend', column }];
      }
    } else {
      // 取消排序
      params.sortItemList = undefined;
    }
    queryEmployee();
  }

  // 部门变化处理函数
  const handleDepartmentChange = (departmentId) => {
    console.log('departmentChange event received, departmentId:', departmentId);
    // 重置开关到默认状态（含子级）
    params.includeSubDepartment = true;
    params.pageNum = 1;
    params.departmentId = departmentId;
    queryEmployee();
  };

  // 立即注册事件监听器（在 setup 中，早于 onMounted）
  departmentEmitter.on('departmentChange', handleDepartmentChange);

  // 标记是否已初始化
  let initialized = false;

  onMounted(() => {
    // 如果还没有初始化，且有 departmentId，则主动查询
    if (!initialized && props.departmentId) {
      initialized = true;
      params.departmentId = props.departmentId;
      queryEmployee();
    }
  });

  // 监听 props.departmentId 变化
  watch(
    () => props.departmentId,
    (newVal) => {
      if (newVal && !initialized) {
        initialized = true;
        params.departmentId = newVal;
        queryEmployee();
      }
    },
    { immediate: true }
  );

  onUnmounted(() => {
    // 只移除自己的事件监听器
    departmentEmitter.off('departmentChange', handleDepartmentChange);
  });

  // ----------------------- 多选操作 ---------------------

  let selectedRowKeys = ref([]);
  let selectedRows = ref([]);
  // 是否有选中：用于 批量操作按钮的禁用
  const hasSelected = computed(() => selectedRowKeys.value.length > 0);

  function onSelectChange(keyArray, selectRows) {
    selectedRowKeys.value = keyArray;
    selectedRows.value = selectRows;
  }

  // 批量删除员工
  function batchDelete() {
    if (!hasSelected.value) {
      message.warning('请选择要删除的员工');
      return;
    }
    const actualNameArray = selectedRows.value.map((e) => e.actualName);
    const employeeIdArray = selectedRows.value.map((e) => e.employeeId);
    Modal.confirm({
      title: '确定要删除如下员工吗?',
      icon: createVNode(ExclamationCircleOutlined),
      content: _.join(actualNameArray, ','),
      okText: '删除',
      okType: 'danger',
      async onOk() {
        Loading.show();
        try {
          await employeeApi.batchDeleteEmployee(employeeIdArray);
          message.success('删除成功');
          queryEmployee();
          selectedRowKeys.value = [];
          selectedRows.value = [];
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

  // 批量更新员工部门
  const employeeDepartmentFormModal = ref();

  function updateEmployeeDepartment() {
    if (!hasSelected.value) {
      message.warning('请选择要调整部门的员工');
      return;
    }
    const employeeIdArray = selectedRows.value.map((e) => e.employeeId);
    employeeDepartmentFormModal.value.showModal(employeeIdArray);
  }

  // ----------------------- 添加、修改、禁用、重置密码 ------------------------------------

  const employeeFormModal = ref(); //组件
  const employeeImportWizard = ref(); //导入向导组件

  // 展示编辑弹窗
  function showDrawer(rowData) {
    let params = {};
    if (rowData) {
      params = _.cloneDeep(rowData);
      params.disabledFlag = params.disabledFlag ? 1 : 0;
    } else if (props.departmentId) {
      params.departmentId = props.departmentId;
    }
    employeeFormModal.value.showDrawer(params);
  }

  // 重置密码
  function resetPassword(id, name) {
    Modal.confirm({
      title: '提醒',
      icon: createVNode(ExclamationCircleOutlined),
      content: '确定要重置密码吗?',
      okText: '确定',
      okType: 'danger',
      async onOk() {
        Loading.show();
        try {
          let { data: passWord } = await employeeApi.resetPassword(id);
          message.success('重置成功');
          employeePasswordDialog.value.showModal(name, passWord);
          queryEmployee();
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

  // 禁用 / 启用
  function updateDisabled(id, disabledFlag) {
    Modal.confirm({
      title: '提醒',
      icon: createVNode(ExclamationCircleOutlined),
      content: `确定要${disabledFlag ? '启用' : '禁用'}吗?`,
      okText: '确定',
      okType: 'danger',
      async onOk() {
        Loading.show();
        try {
          await employeeApi.updateDisabled(id);
          message.success(`${disabledFlag ? '启用' : '禁用'}成功`);
          queryEmployee();
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

  // 导出员工数据
  async function exportEmployee() {
    Loading.show();
    try {
      const response = await employeeApi.exportEmployee(params);

      // 创建下载链接
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      // 从响应头获取文件名，如果没有则使用默认文件名
      const fileName = `员工数据_${new Date().getTime()}.xlsx`;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      message.success('导出成功');
    } catch (error) {
      // axios拦截器已经统一处理了错误消息显示
      sentry.captureError(error);
    } finally {
      Loading.hide();
    }
  }

  // 显示导入向导
  function showImportWizard() {
    employeeImportWizard.value.showModal();
  }
</script>
<style scoped lang="less">
  .employee-container {
    height: 100%;
    border-radius: 6px;
  }

  .header {
    display: flex;
    align-items: center;
    margin-bottom: 0;

    h5 {
      margin-bottom: 0;
    }
  }

  .query-bar {
    margin: 12px 0 8px;
    padding: 12px 14px;
    background: #fafafa;
    border-radius: 4px;
    border: 1px solid #f0f0f0;

    :deep(.ant-form-item) {
      margin-bottom: 0;
    }

    .search-input {
      width: 320px;
    }
  }

  .btn-group {
    margin: 8px 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
  }
</style>
