<!--
  * Nacos配置编辑弹窗
  *
-->
<template>
  <a-modal
    v-model:open="visible"
    :title="isEdit ? '编辑配置' : '新建配置'"
    :width="800"
    :confirmLoading="confirmLoading"
    @ok="onSubmit"
    @cancel="onClose"
  >
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="配置ID" name="dataId" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
            <a-input v-model:value="form.dataId" placeholder="如: postgresql-common.yaml" :disabled="isEdit" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="配置名称" name="configName" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
            <a-input v-model:value="form.configName" placeholder="配置名称" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="配置分组" name="groupId" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
            <a-input v-model:value="form.groupId" placeholder="如: QuickBlue_GROUP" :disabled="isEdit" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="配置类型" name="type" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
            <a-select v-model:value="form.type" placeholder="选择配置类型">
              <a-select-option value="yaml">YAML</a-select-option>
              <a-select-option value="properties">Properties</a-select-option>
              <a-select-option value="text">Text</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item label="配置内容" name="content">
        <div style="margin-bottom: 8px;">
          <a-button size="small" @click="showTemplateSelect">
            <template #icon><FileTextOutlined /></template>
            从模板选择
          </a-button>
        </div>
        <a-textarea
          v-model:value="form.content"
          :rows="12"
          placeholder="请输入配置内容"
          class="code-textarea"
        />
      </a-form-item>
      <a-form-item label="备注" name="remark">
        <a-input v-model:value="form.remark" placeholder="变更说明" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- 模板选择弹窗 -->
  <a-modal
    v-model:open="templateSelectVisible"
    title="选择配置模板"
    :width="800"
    :footer="null"
  >
    <a-list
      :grid="{ gutter: 16, column: 2 }"
      :data-source="templateList"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-card :hoverable="true" @click="selectTemplate(item)" size="small">
            <template #title>
              <div>
                <div>{{ item.templateName }}</div>
                <a-tag :color="item.type === 'yaml' ? 'green' : 'orange'" size="small" style="margin-top: 4px">
                  {{ item.type?.toUpperCase() }}
                </a-tag>
              </div>
            </template>
            <template #description>
              <div style="height: 60px; overflow: hidden; text-overflow: ellipsis;">
                {{ item.description || '暂无描述' }}
              </div>
            </template>
            <div style="margin-top: 8px; color: #666; font-size: 12px;">
              <div>编码: {{ item.templateCode }}</div>
              <div>DataId: {{ item.dataId }}</div>
            </div>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </a-modal>
</template>

<script setup>
  import { ref, reactive } from 'vue';
  import { nacosConfigApi, nacosConfigTemplateApi } from '/src/api/support/nacos-config-api';
  import { message } from 'ant-design-vue';
  import { sentry } from '/src/lib/sentry';

  // 模板选择相关
  const templateList = ref([]);
  const templateSelectVisible = ref(false);

  async function showTemplateSelect() {
    try {
      const res = await nacosConfigTemplateApi.listAllTemplates();
      if (res.data) {
        templateList.value = res.data;
        templateSelectVisible.value = true;
      }
    } catch (e) {
      sentry.captureError(e);
      message.error('加载模板列表失败');
    }
  }

  async function selectTemplate(template) {
    try {
      const res = await nacosConfigTemplateApi.getTemplateById(template.templateId);
      if (res.data) {
        form.content = res.data.content;
        form.type = res.data.type || 'yaml';
        if (!form.dataId) {
          form.dataId = res.data.dataId;
        }
        if (!form.groupId || form.groupId === 'QuickBlue_GROUP') {
          form.groupId = res.data.groupId;
        }
        templateSelectVisible.value = false;
        message.success('模板已应用');
      }
    } catch (e) {
      sentry.captureError(e);
      message.error('加载模板失败');
    }
  }

  const emit = defineEmits(['reload']);

  const visible = ref(false);
  const confirmLoading = ref(false);
  const isEdit = ref(false);
  const tenantId = ref('');

  const formRef = ref();
  const form = reactive({
    dataId: '',
    groupId: 'QuickBlue_GROUP',
    tenantId: '',
    content: '',
    type: 'yaml',
    configName: '',
    remark: '',
  });

  const rules = {
    dataId: [{ required: true, message: '请输入配置ID' }],
    groupId: [{ required: true, message: '请输入配置分组' }],
    content: [{ required: true, message: '请输入配置内容' }],
  };

  function showModal(record, nsId) {
    visible.value = true;
    tenantId.value = nsId || '';
    
    // 重置表单
    Object.assign(form, {
      dataId: '',
      groupId: 'QuickBlue_GROUP',
      tenantId: nsId || '',
      content: '',
      type: 'yaml',
      configName: '',
      remark: '',
    });

    if (record) {
      isEdit.value = true;
      Object.assign(form, {
        dataId: record.dataId,
        groupId: record.groupId,
        tenantId: record.tenantId || nsId || '',
        content: record.content,
        type: record.type || 'yaml',
        configName: record.configName || '',
        remark: '',
      });
    } else {
      isEdit.value = false;
      form.tenantId = nsId || '';
    }
  }

  async function onSubmit() {
    try {
      await formRef.value.validate();
      confirmLoading.value = true;

      const res = await nacosConfigApi.publishConfig({
        ...form,
        tenantId: tenantId.value,
      });

      if (res.ok) {
        message.success(isEdit.value ? '配置更新成功' : '配置创建成功');
        visible.value = false;
        emit('reload');
      } else {
        message.error(res.msg || '操作失败');
      }
    } catch (e) {
      sentry.captureError(e);
    } finally {
      confirmLoading.value = false;
    }
  }

  function onClose() {
    visible.value = false;
  }

  defineExpose({ showModal });
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
