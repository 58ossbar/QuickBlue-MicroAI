<!--
  AI模型管理

-->
<template>
  <a-form class="qb-query-form">
    <a-row class="qb-query-form-row">
      <a-form-item label="名称" class="qb-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.name" placeholder="请输入名称" />
      </a-form-item>
      <a-form-item label="供应商" class="qb-query-form-item">
        <a-select style="width: 150px" v-model:value="queryForm.provider" placeholder="请选择供应商" allowClear>
          <a-select-option v-for="item in enumPlugin.getValueDescList('AI_MODEL_PROVIDER_ENUM')" :key="item.value" :value="item.value">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="模型类型" class="qb-query-form-item">
        <a-select style="width: 150px" v-model:value="queryForm.modelType" placeholder="请选择模型类型" allowClear>
          <a-select-option v-for="item in enumPlugin.getValueDescList('AI_MODEL_TYPE_ENUM')" :key="item.value" :value="item.value">
            {{ item.desc }}
          </a-select-option>
        </a-select>
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
    <a-row class="qb-table-btn-block">
      <div class="qb-table-operate-block">
        <a-button @click="addOrUpdateModel" type="primary">
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
        <TableOperator class="qb-margin-bottom5" v-model="columns" :tableId="null" :refresh="ajaxQuery" />
      </div>
    </a-row>

    <a-table
      ref="tableRef"
      size="small"
      :dataSource="tableData"
      :columns="columns"
      :loading="tableLoading"
      rowKey="id"
      :pagination="false"
      bordered
      :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'provider'">
          <a-tag color="blue">{{ getProviderDesc(record.provider) }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'modelType'">
          <a-tag color="green">{{ getModelTypeDesc(record.modelType) }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'activateFlag'">
          <a-switch
            @change="(checked) => handleActivate(checked, record)"
            v-model:checked="record.activeChecked"
            checked-children="已激活"
            un-checked-children="未激活"
          />
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="addOrUpdateModel(record)" type="link">编辑</a-button>
            <a-button @click="handleActivate(true, record)" type="link" v-if="!record.activateFlag">激活</a-button>
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

    <ModelFormModal ref="modelFormModalRef" @reloadList="ajaxQuery" />
  </a-card>
</template>
<script setup>
  import { onMounted, reactive, ref, getCurrentInstance } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { Loading } from '/@/components/framework/loading';
  import { aiModelApi } from '/@/api/ai/model-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import BooleanSelect from '/@/components/framework/boolean-select/index.vue';
  import { SearchOutlined, ReloadOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import ModelFormModal from './model-form-modal.vue';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  // 获取全局实例
  const internalInstance = getCurrentInstance();
  const enumPlugin = internalInstance.appContext.config.globalProperties.$enumPlugin;

  const tableRef = ref();
  const columns = ref([
    {
      title: '名称',
      dataIndex: 'name',
      minWidth: 150,
    },
    {
      title: '供应商',
      dataIndex: 'provider',
      width: 120,
    },
    {
      title: '模型类型',
      dataIndex: 'modelType',
      width: 100,
    },
    {
      title: '模型名称',
      dataIndex: 'modelName',
      minWidth: 150,
      ellipsis: true,
    },
    {
      title: 'API域名',
      dataIndex: 'baseUrl',
      minWidth: 200,
      ellipsis: true,
    },
    {
      title: '状态',
      width: 100,
      dataIndex: 'activateFlag',
    },
    {
      title: '创建时间',
      width: 160,
      dataIndex: 'createTime',
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 120,
    },
  ]);

  useColumnResize(tableRef, columns);

  // ---------------- 查询数据表单和方法 ----------------

  const queryFormState = {
    name: '',
    provider: '',
    modelType: '',
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const tableLoading = ref(false);
  const selectedRowKeyList = ref([]);
  const tableData = ref([]);
  const total = ref(0);
  const modelFormModalRef = ref();

  function onSelectChange(selectedRowKeys) {
    selectedRowKeyList.value = selectedRowKeys;
  }

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    ajaxQuery();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    ajaxQuery();
  }

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseData = await aiModelApi.getPage(queryForm);
      const list = responseData.data.records || [];
      for (let item of list) {
        item.activeChecked = item.activateFlag === 1;
      }
      total.value = responseData.data.total || 0;
      tableData.value = list;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // ----------------------- 激活/停用 ------------------------
  async function handleActivate(activateFlag, model) {
    Loading.show();
    try {
      if (activateFlag) {
        await aiModelApi.activate(model.id);
        message.success('激活成功');
      }
      model.activateFlag = activateFlag ? 1 : 0;
      model.activeChecked = activateFlag;
      await ajaxQuery();
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }

  // ---------------- 批量删除 -----------------

  function confirmBatchDelete() {
    Modal.confirm({
      title: '提示',
      content: '确定要删除选中的模型吗？',
      okText: '删除',
      okType: 'danger',
      onOk() {
        batchDelete();
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  async function batchDelete() {
    try {
      Loading.show();
      await aiModelApi.batchDelete(selectedRowKeyList.value);
      message.success('删除成功');
      await ajaxQuery();
      selectedRowKeyList.value = [];
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }

  // ---------------- 添加/更新 -----------------

  function addOrUpdateModel(rowData) {
    modelFormModalRef.value.showModal(rowData);
  }

  // ---------------- 辅助函数 ----------------

  function getProviderDesc(provider) {
   const desc = enumPlugin.getDescByValue('AI_MODEL_PROVIDER_ENUM', provider);
   return desc || provider;
  }

  function getModelTypeDesc(modelType) {
   const desc = enumPlugin.getDescByValue('AI_MODEL_TYPE_ENUM', modelType);
   return desc || modelType;
  }

  onMounted(ajaxQuery);
</script>
