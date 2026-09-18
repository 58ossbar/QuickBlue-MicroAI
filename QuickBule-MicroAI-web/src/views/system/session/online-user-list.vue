<!--
  * 在线用户管理
  *
-->
<template>
  <!---------- 等保二级必备标注 begin ---------->
  <div style="margin-bottom: 8px;">
    <a-tag color="green" style="margin-right: 8px; font-weight: 600;">二级必备</a-tag>
    <span style="color: rgba(0,0,0,0.45); font-size: 12px;">依据 GB/T 22239-2019《网络安全等级保护基本要求》二级要求，本功能为等保二级核查项</span>
  </div>
  <!---------- 等保二级必备标注 end ---------->
  <a-form class="qb-query-form" v-privilege="'security:onlineUser:query'" ref="queryFormRef">
    <a-row class="qb-query-form-row">
      <a-form-item label="关键词" class="qb-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.keywords" placeholder="用户名/登录账号" />
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
    <a-row justify="space-between" ref="tableOperatorRef">
      <a-space>
        <a-button type="primary" danger v-privilege="'security:onlineUser:forceLogout'" @click="batchForceLogout" :disabled="selectedRowKeys.length === 0">
          <template #icon>
            <LogoutOutlined />
          </template>
          批量强制下线
        </a-button>
      </a-space>
      <a-space>
        <a-tag color="blue">在线人数：{{ onlineCount }}</a-tag>
        <a-button @click="ajaxQuery">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </a-space>
    </a-row>

    <a-table
      ref="tableRef"
      size="small"
      :dataSource="tableData"
      :columns="columns"
      bordered
      :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      rowKey="loginId"
      :pagination="false"
      :loading="tableLoading"
      :scroll="{ y: scrollY }"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'userType'">
          <span>{{ $enumPlugin.getDescByValue('USER_TYPE_ENUM', text) }}</span>
        </template>

        <template v-if="column.dataIndex === 'onlineDuration'">
          <span>{{ record.onlineDurationDesc || '-' }}</span>
        </template>

        <template v-if="column.dataIndex === 'action'">
          <a-button type="link" danger size="small" v-privilege="'security:onlineUser:forceLogout'" @click="forceLogout(record)">
            强制下线
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
  import { message, Modal } from 'ant-design-vue';
  import { PAGE_SIZE_OPTIONS } from '/src/constants/common-const';
  import { sessionApi } from '/src/api/system/session-api';
  import { sentry } from '/src/lib/sentry';
  import { calcTableHeight } from '/src/lib/table-auto-height';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  const tableRef = ref();
  const columns = ref([
    {
      title: '用户名',
      dataIndex: 'userName',
      ellipsis: true,
    },
    {
      title: '登录账号',
      dataIndex: 'loginName',
      ellipsis: true,
    },
    {
      title: '类型',
      dataIndex: 'userType',
      width: 80,
      ellipsis: true,
    },
    {
      title: '部门',
      dataIndex: 'departmentName',
      ellipsis: true,
    },
    {
      title: '在线时长',
      dataIndex: 'onlineDuration',
      width: 120,
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 100,
      fixed: 'right',
    },
  ]);

  useColumnResize(tableRef, columns);

  const queryFormState = {
    keywords: '',
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });

  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);
  const onlineCount = ref(0);
  const selectedRowKeys = ref([]);

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    selectedRowKeys.value = [];
    ajaxQuery();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    ajaxQuery();
  }

  function onSelectChange(keys) {
    selectedRowKeys.value = keys;
  }

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseModel = await sessionApi.queryOnlineUserPage(queryForm);
      tableData.value = responseModel.data.list || [];
      total.value = responseModel.data.total;

      // 获取在线人数
      let countResp = await sessionApi.getOnlineUserCount();
      onlineCount.value = countResp.data;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // 强制下线
  function forceLogout(record) {
    Modal.confirm({
      title: '确认操作',
      content: `确定要强制下线用户【${record.userName || record.loginName}】吗？`,
      okText: '确定',
      okType: 'danger',
      cancelText: '取消',
      async onOk() {
        try {
          let result = await sessionApi.forceLogout(record.userId, record.userType);
          if (result.ok) {
            message.success('强制下线成功');
            ajaxQuery();
          } else {
            message.error(result.msg || '操作失败');
          }
        } catch (e) {
          sentry.captureError(e);
          message.error('操作失败');
        }
      },
    });
  }

  // 批量强制下线
  function batchForceLogout() {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请选择要强制下线的用户');
      return;
    }

    // 获取选中用户的信息
    const selectedUsers = tableData.value.filter(item => selectedRowKeys.value.includes(item.loginId));
    const userIds = selectedUsers.map(item => item.userId);
    const userType = selectedUsers[0]?.userType;

    Modal.confirm({
      title: '确认操作',
      content: `确定要批量强制下线 ${selectedRowKeys.value.length} 个用户吗？`,
      okText: '确定',
      okType: 'danger',
      cancelText: '取消',
      async onOk() {
        try {
          let result = await sessionApi.batchForceLogout(userIds, userType);
          if (result.ok) {
            message.success(result.data || '批量强制下线成功');
            selectedRowKeys.value = [];
            ajaxQuery();
          } else {
            message.error(result.msg || '操作失败');
          }
        } catch (e) {
          sentry.captureError(e);
          message.error('操作失败');
        }
      },
    });
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
</script>
