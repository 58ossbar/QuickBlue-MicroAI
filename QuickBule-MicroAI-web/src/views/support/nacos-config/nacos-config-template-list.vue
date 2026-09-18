<!--
  * Nacos配置模板管理
  *
-->
<template>
  <div>
    <a-form class="qb-query-form">
      <a-row class="qb-query-form-row">
        <a-form-item label="模板名称" class="qb-query-form-item">
          <a-input v-model:value="queryForm.templateName" placeholder="请输入模板名称" style="width: 200px" />
        </a-form-item>
        <a-form-item label="模板编码" class="qb-query-form-item">
          <a-input v-model:value="queryForm.templateCode" placeholder="请输入模板编码" style="width: 200px" />
        </a-form-item>
        <a-form-item label="配置类型" class="qb-query-form-item">
          <a-select v-model:value="queryForm.type" placeholder="请选择类型" style="width: 150px" allowClear>
            <a-select-option value="yaml">YAML</a-select-option>
            <a-select-option value="properties">Properties</a-select-option>
            <a-select-option value="text">Text</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item class="qb-query-form-item qb-margin-left10">
          <a-button-group>
            <a-button type="primary" @click="onSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="resetQuery">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-button-group>
          <a-button type="primary" @click="showEditDrawer()" class="qb-margin-left20">
            <template #icon><PlusOutlined /></template>
            新建模板
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>

    <a-card size="small" :bordered="false" :hoverable="true">
      <a-table
        ref="tableRef"
        size="small"
        :loading="tableLoading"
        bordered
        :dataSource="tableData"
        :columns="columns"
        rowKey="templateId"
        :pagination="pagination"
        @change="onPageChange"
      >
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'type'">
            <a-tag :color="record.type === 'yaml' ? 'green' : record.type === 'properties' ? 'orange' : 'blue'" size="small">
              {{ record.type?.toUpperCase() }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a-button @click="showEditDrawer(record)" type="link" size="small">编辑</a-button>
              <a-button @click="handleUseTemplate(record)" type="link" size="small" style="color: #52c41a">使用</a-button>
              <a-popconfirm title="确定要删除此模板吗？" @confirm="onDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 模板编辑抽屉 -->
    <a-drawer
      v-model:open="editDrawerVisible"
      :title="currentTemplate ? '编辑模板' : '新建模板'"
      :width="700"
      @close="handleDrawerClose"
    >
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="模板名称" name="templateName">
          <a-input v-model:value="form.templateName" placeholder="请输入模板名称" />
        </a-form-item>

        <a-form-item label="模板编码" name="templateCode">
          <a-input v-model:value="form.templateCode" placeholder="请输入模板编码(唯一标识)" :disabled="!!form.templateId" />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="配置ID" name="dataId" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
              <a-input v-model:value="form.dataId" placeholder="如: application.yml" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="配置分组" name="groupId" :label-col="{ span: 10 }" :wrapper-col="{ span: 14 }">
              <a-input v-model:value="form.groupId" placeholder="如: DEFAULT_GROUP" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="配置类型" name="type">
          <a-radio-group v-model:value="form.type">
            <a-radio value="yaml">YAML</a-radio>
            <a-radio value="properties">Properties</a-radio>
            <a-radio value="text">Text</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="配置内容" name="content">
          <a-textarea
            v-model:value="form.content"
            :rows="15"
            placeholder="请输入配置内容"
            class="code-textarea"
          />
        </a-form-item>

        <a-form-item label="模板描述" name="description">
          <a-textarea v-model:value="form.description" placeholder="请输入模板描述" :rows="3" />
        </a-form-item>
      </a-form>

      <template #footer>
        <a-button @click="editDrawerVisible = false">取消</a-button>
        <a-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</a-button>
      </template>
    </a-drawer>
  </div>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { nacosConfigTemplateApi } from '/src/api/support/nacos-config-api';
  import { sentry } from '/src/lib/sentry';
  import { message } from 'ant-design-vue';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  const emit = defineEmits(['reload', 'useTemplate']);

  const tableRef = ref();
  const columns = ref([
    { title: '模板名称', dataIndex: 'templateName', width: 180, ellipsis: true },
    { title: '模板编码', dataIndex: 'templateCode', width: 150, ellipsis: true },
    { title: '配置ID', dataIndex: 'dataId', width: 150, ellipsis: true },
    { title: '配置分组', dataIndex: 'groupId', width: 120 },
    { title: '类型', dataIndex: 'type', width: 80 },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '创建人', dataIndex: 'createUserName', width: 100 },
    { title: '创建时间', dataIndex: 'createTime', width: 150 },
    { title: '操作', dataIndex: 'action', fixed: 'right', width: 180 },
  ]);

  useColumnResize(tableRef, columns);

  // 查询表单
  const queryFormState = {
    templateName: '',
    templateCode: '',
    type: '',
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });

  // 表格数据
  const tableLoading = ref(false);
  const tableData = ref([]);

  // 分页
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条`,
  });

  // 加载模板列表
  async function loadTemplates() {
    try {
      tableLoading.value = true;
      const res = await nacosConfigTemplateApi.queryTemplatePage(queryForm);
      if (res.data) {
        tableData.value = res.data.list || [];
        pagination.total = res.data.total || 0;
      }
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // 搜索
  function onSearch() {
    queryForm.pageNum = 1;
    pagination.current = 1;
    loadTemplates();
  }

  // 重置查询
  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    onSearch();
  }

  // 分页变化
  function onPageChange(page) {
    queryForm.pageNum = page.current;
    queryForm.pageSize = page.pageSize;
    pagination.current = page.current;
    pagination.pageSize = page.pageSize;
    loadTemplates();
  }

  // 编辑抽屉相关
  const editDrawerVisible = ref(false);
  const currentTemplate = ref(null);
  const submitLoading = ref(false);
  const formRef = ref();

  // 表单数据
  const form = reactive({
    templateId: null,
    templateName: '',
    templateCode: '',
    dataId: '',
    groupId: 'DEFAULT_GROUP',
    content: '',
    type: 'yaml',
    description: '',
  });

  // 表单校验规则
  const rules = {
    templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
    templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
    dataId: [{ required: true, message: '请输入配置ID', trigger: 'blur' }],
    groupId: [{ required: true, message: '请输入配置分组', trigger: 'blur' }],
    content: [{ required: true, message: '请输入配置内容', trigger: 'blur' }],
  };

  // 显示编辑抽屉
  async function showEditDrawer(record) {
    currentTemplate.value = record;
    
    // 重置表单
    Object.assign(form, {
      templateId: null,
      templateName: '',
      templateCode: '',
      dataId: '',
      groupId: 'DEFAULT_GROUP',
      content: '',
      type: 'yaml',
      description: '',
    });
    formRef.value?.resetFields();

    // 如果是编辑，加载模板详情
    if (record && record.templateId) {
      try {
        const res = await nacosConfigTemplateApi.getTemplateById(record.templateId);
        if (res.data) {
          Object.assign(form, res.data);
        }
      } catch (e) {
        message.error('加载模板详情失败');
      }
    }

    editDrawerVisible.value = true;
  }

  // 提交表单
  async function handleSubmit() {
    try {
      await formRef.value.validate();
      
      submitLoading.value = true;
      
      const apiFunc = form.templateId
        ? nacosConfigTemplateApi.updateTemplate
        : nacosConfigTemplateApi.addTemplate;
      
      const res = await apiFunc(form);
      
      if (res.ok) {
        message.success(form.templateId ? '更新成功' : '创建成功');
        editDrawerVisible.value = false;
        loadTemplates();
      } else {
        message.error(res.msg || '操作失败');
      }
    } catch (e) {
      // 校验失败
    } finally {
      submitLoading.value = false;
    }
  }

  // 关闭抽屉
  function handleDrawerClose() {
    formRef.value?.resetFields();
  }

  // 使用模板
  async function handleUseTemplate(record) {
    try {
      const res = await nacosConfigTemplateApi.getTemplateById(record.templateId);
      if (res.data) {
        // 通知父组件使用模板
        emit('useTemplate', {
          dataId: res.data.dataId,
          groupId: res.data.groupId,
          content: res.data.content,
          type: res.data.type,
          configName: res.data.templateName,
        });
      }
    } catch (e) {
      sentry.captureError(e);
    }
  }

  // 删除模板
  async function onDelete(record) {
    try {
      const res = await nacosConfigTemplateApi.deleteTemplate(record.templateId);
      if (res.ok) {
        message.success('删除成功');
        loadTemplates();
      } else {
        message.error(res.msg || '删除失败');
      }
    } catch (e) {
      sentry.captureError(e);
    }
  }

  onMounted(() => {
    loadTemplates();
  });
</script>

<style scoped>
.code-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  background-color: #1e1e1e;
  color: #d4d4d4;
  border: 1px solid #434343;
}
.code-textarea::placeholder {
  color: #6a6a6a;
}
</style>
