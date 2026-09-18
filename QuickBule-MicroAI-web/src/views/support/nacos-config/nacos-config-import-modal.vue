<!--
  * 配置导入弹窗
  *
-->
<template>
  <a-modal
    v-model:open="visible"
    title="导入配置"
    :width="700"
    :confirmLoading="confirmLoading"
    @ok="onImport"
    @cancel="onClose"
  >
    <a-alert
      message="导入说明"
      description="支持导入 JSON 或 YAML 格式的配置文件。JSON格式可包含多个配置，YAML格式为单个配置内容"
      type="info"
      show-icon
      class="qb-margin-bottom10"
    />

    <a-form layout="inline" class="qb-margin-bottom10">
      <a-form-item label="导入格式">
        <a-radio-group v-model:value="importType">
          <a-radio value="json">JSON (批量)</a-radio>
          <a-radio value="yaml">YAML (单个)</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>

    <a-form v-if="importType === 'yaml'" layout="vertical" class="qb-margin-bottom10">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="配置ID">
            <a-input v-model:value="yamlForm.dataId" placeholder="如: postgresql-common.yaml" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="配置分组">
            <a-input v-model:value="yamlForm.groupId" placeholder="如: QuickBlue_GROUP" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="配置名称">
            <a-input v-model:value="yamlForm.configName" placeholder="配置名称" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="配置类型">
            <a-select v-model:value="yamlForm.type">
              <a-select-option value="yaml">YAML</a-select-option>
              <a-select-option value="properties">Properties</a-select-option>
              <a-select-option value="text">Text</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>

    <a-upload-dragger
      :file-list="fileList"
      :before-upload="beforeUpload"
      :remove="onRemove"
      :accept="importType === 'json' ? '.json' : '.yaml,.yml'"
    >
      <p class="ant-upload-drag-icon">
        <inbox-outlined />
      </p>
      <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
      <p class="ant-upload-hint">
        {{ importType === 'json' ? '支持 .json 格式的配置文件' : '支持 .yaml, .yml 格式的配置文件' }}
      </p>
    </a-upload-dragger>

    <div v-if="previewData.length > 0 && importType === 'json'" class="qb-margin-top10">
      <a-divider orientation="left">预览配置 ({{ previewData.length }} 项)</a-divider>
      <a-table
        size="small"
        :dataSource="previewData"
        :columns="previewColumns"
        :pagination="false"
        rowKey="dataId"
        :scroll="{ y: 200 }"
      >
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'type'">
            <a-tag :color="record.type === 'yaml' ? 'green' : 'orange'" size="small">{{ record.type }}</a-tag>
          </template>
        </template>
      </a-table>
    </div>

    <div v-if="yamlContent && importType === 'yaml'" class="qb-margin-top10">
      <a-divider orientation="left">配置内容预览</a-divider>
      <a-textarea
        :value="yamlContent"
        :rows="10"
        read-only
        class="code-textarea"
      />
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, watch } from 'vue';
  import { nacosConfigApi } from '/src/api/support/nacos-config-api';
  import { message } from 'ant-design-vue';
  import { sentry } from '/src/lib/sentry';
  import { InboxOutlined } from '@ant-design/icons-vue';

  const emit = defineEmits(['reload']);

  const visible = ref(false);
  const confirmLoading = ref(false);
  const tenantId = ref('');
  const fileList = ref([]);
  const previewData = ref([]);
  const importType = ref('yaml');
  const yamlContent = ref('');

  const yamlForm = reactive({
    dataId: '',
    groupId: 'QuickBlue_GROUP',
    configName: '',
    type: 'yaml',
    content: '',
    tenantId: '',
  });

  const previewColumns = [
    { title: '配置ID', dataIndex: 'dataId', width: 200, ellipsis: true },
    { title: '配置名称', dataIndex: 'configName', width: 120 },
    { title: '类型', dataIndex: 'type', width: 80 },
  ];

  function showModal(nsId) {
    visible.value = true;
    tenantId.value = nsId || '';
    fileList.value = [];
    previewData.value = [];
    yamlContent.value = '';
    importType.value = 'yaml';
    Object.assign(yamlForm, {
      dataId: '',
      groupId: 'QuickBlue_GROUP',
      configName: '',
      type: 'yaml',
      content: '',
      tenantId: nsId || '',
    });
  }

  function beforeUpload(file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const content = e.target.result;
        
        if (importType.value === 'json') {
          const configs = JSON.parse(content);
          
          if (!Array.isArray(configs)) {
            message.error('配置文件格式错误，应为数组格式');
            return;
          }

          previewData.value = configs.map(c => ({
            ...c,
            tenantId: tenantId.value,
            groupId: c.groupId || 'QuickBlue_GROUP',
            type: c.type || 'yaml',
          }));
          
          fileList.value = [file];
          message.success(`已解析 ${configs.length} 个配置`);
        } else {
          // YAML格式
          yamlContent.value = content;
          yamlForm.content = content;
          yamlForm.tenantId = tenantId.value;
          
          // 根据文件名自动填充dataId
          if (file.name && !yamlForm.dataId) {
            yamlForm.dataId = file.name;
          }
          
          fileList.value = [file];
          message.success('已解析配置文件');
        }
      } catch (e) {
        message.error('解析文件失败: ' + e.message);
      }
    };
    reader.readAsText(file);
    return false;
  }

  function onRemove() {
    fileList.value = [];
    previewData.value = [];
    yamlContent.value = '';
  }

  async function onImport() {
    if (importType.value === 'json') {
      if (previewData.value.length === 0) {
        message.warning('请先选择要导入的配置文件');
        return;
      }

      try {
        confirmLoading.value = true;
        const res = await nacosConfigApi.importConfigs(previewData.value);
        if (res.ok) {
          message.success(res.data || '导入成功');
          visible.value = false;
          emit('reload');
        } else {
          message.error(res.msg || '导入失败');
        }
      } catch (e) {
        sentry.captureError(e);
        message.error('导入失败');
      } finally {
        confirmLoading.value = false;
      }
    } else {
      // YAML格式导入
      if (!yamlForm.dataId) {
        message.warning('请输入配置ID');
        return;
      }
      if (!yamlForm.content) {
        message.warning('请先选择配置文件');
        return;
      }

      try {
        confirmLoading.value = true;
        const res = await nacosConfigApi.publishConfig(yamlForm);
        if (res.ok) {
          message.success('导入成功');
          visible.value = false;
          emit('reload');
        } else {
          message.error(res.msg || '导入失败');
        }
      } catch (e) {
        sentry.captureError(e);
        message.error('导入失败');
      } finally {
        confirmLoading.value = false;
      }
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
</style>
