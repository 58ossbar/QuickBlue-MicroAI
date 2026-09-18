<!--
  AI模型表单

-->
<template>
  <a-modal
    :title="form.id ? '编辑模型' : '新建模型'"
    :open="visible"
    @cancel="handleCancel"
    :maskClosable="false"
    :destroyOnClose="true"
    width="700px"
    forceRender
  >
    <a-form ref="formRef" :model="form" :rules="formRules" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="模型名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入模型名称" />
      </a-form-item>

      <a-form-item label="供应商" name="provider">
        <a-select v-model:value="form.provider" placeholder="请选择供应商" @change="handleProviderChange">
          <a-select-option v-for="item in enumPlugin.getValueDescList('AI_MODEL_PROVIDER_ENUM')" :key="item.value" :value="item.value">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="模型类型" name="modelType">
        <a-select v-model:value="form.modelType" placeholder="请选择模型类型">
          <a-select-option v-for="item in enumPlugin.getValueDescList('AI_MODEL_TYPE_ENUM')" :key="item.value" :value="item.value">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="模型标识" name="modelName">
        <a-input v-model:value="form.modelName" placeholder="请输入模型标识，如：gpt-3.5-turbo" />
      </a-form-item>

      <a-form-item label="API域名" name="baseUrl">
        <a-input v-model:value="form.baseUrl" placeholder="请输入 API Base URL" />
      </a-form-item>

      <a-form-item label="API Key" name="credential">
        <a-input-password v-model:value="form.credential" placeholder="请输入 API Key" />
      </a-form-item>

      <a-form-item label="Temperature" name="temperature">
        <a-input-number
          v-model:value="form.temperature"
          :min="0"
          :max="2"
          :step="0.1"
          style="width: 100%"
          placeholder="控制随机性，0-2"
        />
      </a-form-item>

      <a-form-item label="Max Tokens" name="maxTokens">
        <a-input-number
          v-model:value="form.maxTokens"
          :min="1"
          :step="100"
          style="width: 100%"
          placeholder="单次响应最大 token 数"
        />
      </a-form-item>

      <a-form-item label="其他参数" name="otherParams">
        <a-textarea
          v-model:value="form.otherParams"
          placeholder='请输入 JSON 格式的其他参数，如：{"top_p": 0.9}'
          :rows="3"
        />
      </a-form-item>
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
  import { aiModelApi } from '/@/api/ai/model-api';
  import { sentry } from '/@/lib/sentry';

  // 获取全局实例
  const internalInstance = getCurrentInstance();
  const enumPlugin = internalInstance.appContext.config.globalProperties.$enumPlugin;

  const emit = defineEmits(['reloadList']);

  const visible = ref(false);
  const formRef = ref();
  const isUpdate = ref(false);

  // 供应商默认配置
  const providerConfig = {
    openai: {
      baseUrl: 'https://api.openai.com/v1',
      modelName: 'gpt-3.5-turbo',
    },
    ollama: {
      baseUrl: 'http://localhost:11434',
      modelName: 'llama2',
    },
    zhipu: {
      baseUrl: 'https://open.bigmodel.cn/api/paas/v4',
      modelName: 'glm-3-turbo',
    },
    qianfan: {
      baseUrl: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1',
      modelName: 'ERNIE-Bot',
    },
    dashscope: {
      baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
      modelName: 'qwen-turbo',
    },
    anthropic: {
      baseUrl: 'https://api.anthropic.com',
      modelName: 'claude-3-opus-20240229',
    },
  };

  // 表单数据
  const formState = {
    id: undefined,
    name: '',
    provider: '',
    modelType: 'chat',
    modelName: '',
    baseUrl: '',
    credential: '',
    temperature: 0.7,
    maxTokens: 2048,
    otherParams: '',
  };

  const form = reactive({ ...formState });

  // 表单验证规则
  const formRules = {
    name: [{ required: true, message: '请输入模型名称' }],
    provider: [{ required: true, message: '请选择供应商' }],
    modelType: [{ required: true, message: '请选择模型类型' }],
    modelName: [{ required: true, message: '请输入模型标识' }],
    baseUrl: [{ required: true, message: '请输入 API域名' }],
    credential: [{ required: true, message: '请输入 API Key' }],
  };

  // 显示弹窗
  async function showModal(rowData) {
    resetForm();
    isUpdate.value = !!rowData;
    visible.value = true;

    nextTick(() => {
      formRef.value?.clearValidate();
    });

    if (rowData && rowData.id) {
      try {
        const response = await aiModelApi.getById(rowData.id);
        const data = response.data;
        // 填充表单
        Object.keys(form).forEach((key) => {
          if (data[key] !== undefined && data[key] !== null) {
            form[key] = data[key];
          }
        });
        // 解析模型参数
        if (data.modelParams) {
          try {
            const params = JSON.parse(data.modelParams);
            form.temperature = params.temperature || 0.7;
            form.maxTokens = params.maxTokens || 2048;
            delete params.temperature;
            delete params.maxTokens;
            form.otherParams = Object.keys(params).length > 0 ? JSON.stringify(params, null, 2) : '';
          } catch (e) {
            console.error('解析模型参数失败:', e);
          }
        }
      } catch (e) {
        sentry.captureError(e);
        message.error('获取模型详情失败');
      }
    }
  }

  // 重置表单
  function resetForm() {
    Object.assign(form, formState);
    if (formRef.value) {
      formRef.value.clearValidate();
    }
  }

  // 处理供应商变更
  function handleProviderChange(provider) {
    const config = providerConfig[provider];
    if (config) {
      if (!form.baseUrl || form.baseUrl === formState.baseUrl) {
        form.baseUrl = config.baseUrl;
      }
      if (!form.modelName || form.modelName === formState.modelName) {
        form.modelName = config.modelName;
      }
    }
  }

  // 提交表单
  async function handleSubmit() {
    try {
      await formRef.value.validateFields();

      // 构建模型参数
      let otherParamsObj = {};
      if (form.otherParams) {
        try {
          otherParamsObj = JSON.parse(form.otherParams);
        } catch (e) {
          message.error('其他参数必须是有效的 JSON 格式');
          return;
        }
      }

      const modelParams = {
        temperature: form.temperature,
        maxTokens: form.maxTokens,
        ...otherParamsObj,
      };

      const submitData = {
        id: form.id,
        name: form.name,
        provider: form.provider,
        modelType: form.modelType,
        modelName: form.modelName,
        baseUrl: form.baseUrl,
        credential: form.credential,
        modelParams: JSON.stringify(modelParams),
        activateFlag: form.activateFlag || 0,
      };

      Loading.show();
      if (isUpdate.value) {
        await aiModelApi.update(submitData);
        message.success('更新成功');
      } else {
        await aiModelApi.create(submitData);
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
