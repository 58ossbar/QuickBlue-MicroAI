<!--
  AI应用表单

-->
<template>
  <a-modal
    :title="form.id ? '编辑应用' : '新建应用'"
    :open="visible"
    @cancel="handleCancel"
    :maskClosable="false"
    :destroyOnClose="true"
    width="900px"
    forceRender
  >
    <a-form ref="formRef" :model="form" :rules="formRules" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 基础配置 -->
        <a-tab-pane key="basic" tab="基础配置">
          <a-form-item label="应用名称" name="name">
            <a-input v-model:value="form.name" placeholder="请输入应用名称" />
          </a-form-item>

          <a-form-item label="应用类型" name="type">
            <a-select v-model:value="form.type" placeholder="请选择应用类型">
              <a-select-option v-for="item in enumPlugin.getValueDescList('AI_APP_TYPE_ENUM')" :key="item.value" :value="item.value">
                {{ item.desc }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="应用图标" name="icon">
            <a-input v-model:value="form.icon" placeholder="请输入图标URL" />
          </a-form-item>

          <a-form-item label="应用描述" name="descr">
            <a-textarea v-model:value="form.descr" placeholder="请输入应用描述" :rows="3" />
          </a-form-item>

          <a-form-item label="开场白" name="prologue">
            <a-textarea v-model:value="form.prologue" placeholder="请输入开场白" :rows="2" />
          </a-form-item>

          <a-form-item label="预设问题" name="presetQuestion">
            <a-textarea v-model:value="form.presetQuestion" placeholder="请输入预设问题，每行一个" :rows="3" />
          </a-form-item>
        </a-tab-pane>

        <!-- 模型配置 -->
        <a-tab-pane key="model" tab="模型配置">
          <a-form-item label="选择模型" name="modelId">
            <a-select v-model:value="form.modelId" placeholder="请选择模型" show-search :filter-option="filterOption">
              <a-select-option v-for="item in modelList" :key="item.id" :value="item.id">
                {{ item.name }} ({{ item.provider }})
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="历史消息数" name="msgNum">
            <a-input-number
              v-model:value="form.msgNum"
              :min="0"
              :max="50"
              :step="1"
              style="width: 200px"
              placeholder="保留的历史消息上下文数量"
            />
          </a-form-item>
        </a-tab-pane>

        <!-- 知识库配置 -->
        <a-tab-pane key="knowledge" tab="知识库配置">
          <a-form-item label="关联知识库" name="knowledgeIds">
            <a-select
              v-model:value="selectedKnowledgeIds"
              mode="multiple"
              placeholder="请选择知识库"
              show-search
              :filter-option="filterOption"
            >
              <a-select-option v-for="item in knowledgeList" :key="item.id" :value="item.id">
                {{ item.name }} ({{ item.type }})
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="记忆开关" name="izOpenMemory">
            <a-switch
              v-model:checked="form.izOpenMemory"
              checked-children="开启"
              un-checked-children="关闭"
              @change="handleMemoryChange"
            />
          </a-form-item>

          <a-form-item label="记忆库" name="memoryId" v-if="form.izOpenMemory">
            <a-select v-model:value="form.memoryId" placeholder="请选择记忆库">
              <a-select-option v-for="item in memoryKnowledgeList" :key="item.id" :value="item.id">
                {{ item.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-tab-pane>

        <!-- 高级配置 -->
        <a-tab-pane key="advanced" tab="高级配置">
          <a-form-item label="提示词" name="prompt">
            <a-textarea v-model:value="form.prompt" placeholder="请输入系统提示词" :rows="6" />
          </a-form-item>

          <a-form-item label="插件配置" name="plugins">
            <a-textarea v-model:value="form.plugins" placeholder='请输入插件配置JSON，如：[{"pluginId":"xxx","pluginName":"xxx","category":"mcp"}]' :rows="4" />
          </a-form-item>

          <a-form-item label="变量配置" name="variables">
            <a-textarea v-model:value="form.variables" placeholder='请输入变量配置JSON，如：{"name":"用户名","value":"张三"}' :rows="4" />
          </a-form-item>

          <a-form-item label="记忆提示词" name="memoryPrompt">
            <a-textarea v-model:value="form.memoryPrompt" placeholder="请输入记忆和变量提示词" :rows="4" />
          </a-form-item>
        </a-tab-pane>
      </a-tabs>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleSubmit">保存</a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup>
  import { reactive, ref, getCurrentInstance, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import { Loading } from '/@/components/framework/loading';
  import { aiAppApi } from '/@/api/ai/app-api';
  import { aiModelApi } from '/@/api/ai/model-api';
  import { aiKnowledgeApi } from '/@/api/ai/knowledge-api';
  import { sentry } from '/@/lib/sentry';

  // 获取全局实例
  const internalInstance = getCurrentInstance();
  const enumPlugin = internalInstance.appContext.config.globalProperties.$enumPlugin;

  const emit = defineEmits(['reloadList']);

  const visible = ref(false);
  const formRef = ref();
  const isUpdate = ref(false);
  const activeTab = ref('basic');
  const modelList = ref([]);
  const knowledgeList = ref([]);
  const memoryKnowledgeList = ref([]);
  const selectedKnowledgeIds = ref([]);

  // 表单数据
  const formState = {
    id: undefined,
    name: '',
    type: 'chat',
    icon: '',
    descr: '',
    prologue: '',
    presetQuestion: '',
    modelId: undefined,
    msgNum: 10,
    knowledgeIds: '',
    izOpenMemory: false,
    memoryId: undefined,
    prompt: '',
    plugins: '',
    variables: '',
    memoryPrompt: '',
  };

  const form = reactive({ ...formState });

  // 表单验证规则
  const formRules = {
    name: [
      { required: true, message: '请输入应用名称' },
      { max: 100, message: '应用名称不能超过100个字符' },
    ],
    type: [{ required: true, message: '请选择应用类型' }],
    descr: [{ max: 500, message: '应用描述不能超过500个字符' }],
    modelId: [{ required: true, message: '请选择模型' }],
  };

  // 加载模型列表
  async function loadModelList() {
    try {
      const response = await aiModelApi.getList();
      modelList.value = response.data || [];
    } catch (e) {
      console.error('加载模型列表失败:', e);
    }
  }

  // 加载知识库列表
  async function loadKnowledgeList() {
    try {
      const response = await aiKnowledgeApi.getList();
      const allKnowledge = response.data || [];
      knowledgeList.value = allKnowledge;
      memoryKnowledgeList.value = allKnowledge.filter((item) => item.type === 'memory');
    } catch (e) {
      console.error('加载知识库列表失败:', e);
    }
  }

  // 显示弹窗
  async function showModal(rowData) {
    resetForm();
    isUpdate.value = !!rowData;
    visible.value = true;

    // 加载下拉数据
    await loadModelList();
    await loadKnowledgeList();

    nextTick(() => {
      formRef.value?.clearValidate();
    });

    if (rowData && rowData.id) {
      try {
        const response = await aiAppApi.getById(rowData.id);
        const data = response.data;
        // 填充表单
        Object.keys(form).forEach((key) => {
          if (data[key] !== undefined && data[key] !== null) {
            form[key] = data[key];
          }
        });
        // 处理知识库ID列表
        if (data.knowledgeIds) {
          selectedKnowledgeIds.value = data.knowledgeIds.split(',');
        } else {
          selectedKnowledgeIds.value = [];
        }
        // 处理记忆开关
        form.izOpenMemory = data.izOpenMemory === 1;
      } catch (e) {
        sentry.captureError(e);
        message.error('获取应用详情失败');
      }
    }
  }

  // 重置表单
  function resetForm() {
    Object.assign(form, formState);
    selectedKnowledgeIds.value = [];
    activeTab.value = 'basic';
    if (formRef.value) {
      formRef.value.clearValidate();
    }
  }

  // 处理记忆开关变更
  function handleMemoryChange(checked) {
    if (!checked) {
      form.memoryId = undefined;
    }
  }

  // 过滤选项
  function filterOption(input, option) {
    return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  }

  // 提交表单
  async function handleSubmit() {
    try {
      await formRef.value.validateFields();

      // 验证插件配置JSON格式
      if (form.plugins) {
        try {
          JSON.parse(form.plugins);
        } catch (e) {
          message.error('插件配置必须是有效的 JSON 格式');
          return;
        }
      }

      // 验证变量配置JSON格式
      if (form.variables) {
        try {
          JSON.parse(form.variables);
        } catch (e) {
          message.error('变量配置必须是有效的 JSON 格式');
          return;
        }
      }

      const submitData = {
        id: form.id,
        name: form.name,
        type: form.type,
        icon: form.icon,
        descr: form.descr,
        prologue: form.prologue,
        presetQuestion: form.presetQuestion,
        modelId: form.modelId,
        msgNum: form.msgNum,
        knowledgeIds: selectedKnowledgeIds.value.join(','),
        izOpenMemory: form.izOpenMemory ? 1 : 0,
        memoryId: form.memoryId,
        prompt: form.prompt,
        plugins: form.plugins,
        variables: form.variables,
        memoryPrompt: form.memoryPrompt,
        status: form.status || 'enable',
      };

      Loading.show();
      if (isUpdate.value) {
        await aiAppApi.update(submitData);
        message.success('更新成功');
      } else {
        await aiAppApi.create(submitData);
        message.success('创建成功');
      }
      handleCancel();
      emit('reloadList');
    } catch (e) {
      message.error('参数验证错误，请仔细填写表单数据!');
    } finally {
      Loading.hide();
    }
  }

  // 取消
  function handleCancel() {
    resetForm();
    visible.value = false;
  }

  defineExpose({
    showModal,
  });
</script>
