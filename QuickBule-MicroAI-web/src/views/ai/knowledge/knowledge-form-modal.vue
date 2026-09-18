<!--
  AI知识库表单

-->
<template>
  <a-modal
    :title="form.id ? '编辑知识库' : '新建知识库'"
    :open="visible"
    @cancel="handleCancel"
    :maskClosable="false"
    :destroyOnClose="true"
    width="600px"
    forceRender
  >
    <a-form ref="formRef" :model="form" :rules="formRules" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="知识库名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入知识库名称" />
      </a-form-item>

      <a-form-item label="类型" name="type">
        <a-select v-model:value="form.type" placeholder="请选择类型">
          <a-select-option v-for="item in enumPlugin.getValueDescList('AI_KNOWLEDGE_TYPE_ENUM')" :key="item.value" :value="item.value">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="form.status">
          <a-radio value="enable">启用</a-radio>
          <a-radio value="disable">禁用</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="向量模型" name="embedId">
        <a-select
          v-model:value="form.embedId"
          placeholder="请选择向量模型"
          show-search
          :filter-option="filterOption"
          :not-found-content="
            embeddingModelList.length === 0
              ? '暂无向量模型，请先到「AI模型管理」创建一个模型类型为「向量」的模型'
              : undefined
          "
        >
          <a-select-option v-for="item in embeddingModelList" :key="item.id" :value="item.id">
            {{ item.name }} ({{ item.provider }})
          </a-select-option>
        </a-select>
        <div class="qb-form-tip">用于文档向量化的嵌入模型</div>
      </a-form-item>

      <a-form-item label="描述" name="descr">
        <a-textarea v-model:value="form.descr" placeholder="请输入知识库描述" :rows="4" />
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
  import { aiKnowledgeApi } from '/@/api/ai/knowledge-api';
  import { aiModelApi } from '/@/api/ai/model-api';
  import { sentry } from '/@/lib/sentry';

  // 获取全局实例
  const internalInstance = getCurrentInstance();
  const enumPlugin = internalInstance.appContext.config.globalProperties.$enumPlugin;

  const emit = defineEmits(['reloadList']);

  const visible = ref(false);
  const formRef = ref();
  const isUpdate = ref(false);
  const embeddingModelList = ref([]);

  // 表单数据
  const formState = {
    id: undefined,
    name: '',
    type: 'knowledge',
    status: 'enable',
    embedId: undefined,
    descr: '',
  };

  const form = reactive({ ...formState });

  // 表单验证规则
  const formRules = {
    name: [
      { required: true, message: '请输入知识库名称' },
      { max: 100, message: '知识库名称不能超过100个字符' },
    ],
    type: [{ required: true, message: '请选择类型' }],
    status: [{ required: true, message: '请选择状态' }],
    embedId: [{ required: true, message: '请选择向量模型' }],
    descr: [{ max: 500, message: '知识库描述不能超过500个字符' }],
  };

  // 加载向量模型列表
  async function loadEmbeddingModelList() {
    try {
      const response = await aiModelApi.getList();
      const allModels = response.data || [];
      // 只显示向量模型
      embeddingModelList.value = allModels.filter((item) => item.modelType === 'EMBED' || item.modelType === 'embedding');
    } catch (e) {
      console.error('加载向量模型列表失败:', e);
    }
  }

  // 显示弹窗
  async function showModal(rowData) {
    resetForm();
    isUpdate.value = !!rowData;
    visible.value = true;

    // 加载向量模型列表
    await loadEmbeddingModelList();

    nextTick(() => {
      formRef.value?.clearValidate();
    });

    if (rowData && rowData.id) {
      try {
        const response = await aiKnowledgeApi.getById(rowData.id);
        const data = response.data;
        // 填充表单
        Object.keys(form).forEach((key) => {
          if (data[key] !== undefined && data[key] !== null) {
            form[key] = data[key];
          }
        });
      } catch (e) {
        sentry.captureError(e);
        message.error('获取知识库详情失败');
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

  // 过滤选项
  function filterOption(input, option) {
    return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  }

  // 提交表单
  async function handleSubmit() {
    try {
      await formRef.value.validateFields();

      const submitData = {
        id: form.id,
        name: form.name,
        type: form.type,
        status: form.status,
        embedId: form.embedId,
        descr: form.descr,
      };

      Loading.show();
      if (isUpdate.value) {
        await aiKnowledgeApi.update(submitData);
        message.success('更新成功');
      } else {
        await aiKnowledgeApi.create(submitData);
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

<style scoped>
.text-gray {
  color: #999;
}
.qb-form-tip {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>
