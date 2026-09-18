<!--
  * 数据权限配置 表单
  *
-->
<template>
  <a-modal
    v-model:open="visible"
    :title="form.configId ? '编辑数据权限配置' : '新增数据权限配置'"
    :width="600"
    :confirmLoading="loading"
    @ok="onSubmit"
    @cancel="onClose"
  >
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="配置编码" name="configCode">
        <a-input v-model:value="form.configCode" placeholder="请输入配置编码" :maxlength="100" :disabled="!!form.configId" />
      </a-form-item>

      <a-form-item label="配置名称" name="configName">
        <a-input v-model:value="form.configName" placeholder="请输入配置名称" :maxlength="100" />
      </a-form-item>

      <a-form-item label="业务模块" name="businessModule">
        <a-input v-model:value="form.businessModule" placeholder="请输入业务模块名称" :maxlength="100" />
      </a-form-item>

      <a-form-item label="默认视图类型" name="defaultViewType">
        <a-select v-model:value="form.defaultViewType" placeholder="请选择默认视图类型">
          <a-select-option :value="0">仅本人</a-select-option>
          <a-select-option :value="1">部门</a-select-option>
          <a-select-option :value="2">本部门及以下</a-select-option>
          <a-select-option :value="3">全部数据</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="配置描述" name="configDesc">
        <a-textarea v-model:value="form.configDesc" placeholder="请输入配置描述" :rows="3" :maxlength="500" />
      </a-form-item>

      <a-form-item label="排序" name="sortOrder">
        <a-input-number v-model:value="form.sortOrder" :min="0" :max="9999" style="width: 100%" placeholder="请输入排序" />
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-switch v-model:checked="form.status" checked-children="启用" un-checked-children="禁用" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { message } from 'ant-design-vue';
import { dataScopeConfigApi } from '/@/api/system/data-scope-config-api';
import { Loading } from '/@/components/framework/loading';
import { sentry } from '/@/lib/sentry';

const emits = defineEmits(['reloadList']);

const visible = ref(false);
const loading = ref(false);
const formRef = ref();

// 表单默认值
const formDefault = {
  configId: null,
  configCode: '',
  configName: '',
  configDesc: '',
  businessModule: '',
  defaultViewType: 0,
  sortOrder: 0,
  status: true,
};

const form = reactive({ ...formDefault });

// 表单校验规则
const rules = {
  configCode: [{ required: true, message: '请输入配置编码', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
};

// 显示弹窗
async function show(rowData) {
  Object.assign(form, formDefault);
  if (rowData) {
    Object.assign(form, rowData);
  }
  visible.value = true;
}

// 提交表单
async function onSubmit() {
  try {
    await formRef.value.validate();
    loading.value = true;

    if (form.configId) {
      await dataScopeConfigApi.update(form);
      message.success('修改成功');
    } else {
      await dataScopeConfigApi.add(form);
      message.success('添加成功');
    }

    emits('reloadList');
    onClose();
  } catch (e) {
    if (e.errorFields) {
      // 表单校验错误
      return;
    }
    sentry.captureError(e);
  } finally {
    loading.value = false;
  }
}

// 关闭弹窗
function onClose() {
  visible.value = false;
  formRef.value?.resetFields();
}

defineExpose({ show });
</script>
