<!--
  * 部门表单 弹窗
  *
-->
<template>
  <a-modal v-model:open="visible" :title="formState.departmentId ? '编辑部门' : '添加部门'" @ok="handleOk" destroyOnClose
    width="680px" :bodyStyle="{ padding: '24px 32px', maxHeight: '60vh', overflowY: 'auto' }">
    <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="上级部门" name="parentId" v-if="formState.parentId">
        <DepartmentTreeSelect ref="departmentTreeSelect" v-model:value="formState.parentId" :defaultValueFlag="false"
          width="100%" />
      </a-form-item>
      <a-form-item label="部门名称" name="departmentName">
        <a-input v-model:value.trim="formState.departmentName" placeholder="请输入部门名称" />
      </a-form-item>
      <a-form-item label="部门编码" name="departmentCode">
        <a-input v-model:value.trim="formState.departmentCode" placeholder="请输入部门编码（可选）" />
      </a-form-item>
      <a-form-item label="部门负责人" name="managerId">
        <EmployeeSelect ref="employeeSelect" placeholder="请选择部门负责人" width="100%" v-model:value="formState.managerId"
          :leaveFlag="false" />
      </a-form-item>
      <a-form-item label="部门排序" name="sort">
        <a-input-number style="width: 100%" v-model:value="formState.sort" :min="0" placeholder="值越大越靠前"
          :precision="0" />
      </a-form-item>
      <a-form-item label="部门状态" name="status">
        <a-switch v-model:checked="statusChecked" checked-children="启用" un-checked-children="禁用" />
        <span style="margin-left: 12px; color: #666; font-size: 13px;">{{ statusChecked ? '当前为启用状态' : '当前为禁用状态' }}</span>
      </a-form-item>
      <a-form-item label="层级" name="level" v-if="formState.departmentId">
        <a-tag color="blue">第 {{ formState.level || '-' }} 层</a-tag>
      </a-form-item>
      <a-form-item label="部门描述" name="remark">
        <a-textarea v-model:value="formState.remark" placeholder="请输入部门描述（可选）" :rows="4" :maxlength="200" showCount />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup>
  import message from 'ant-design-vue/lib/message';
  import { nextTick, reactive, ref, computed } from 'vue';
  import { departmentApi } from '/@/api/system/department-api';
  import DepartmentTreeSelect from '/@/components/system/department-tree-select/index.vue';
  import EmployeeSelect from '/@/components/system/employee-select/index.vue';
  import { sentry } from '/@/lib/sentry';
  import { Loading } from '/@/components/framework/loading';

// ----------------------- 对外暴漏 ---------------------

defineExpose({
  showModal,
});

// ----------------------- modal 的显示与隐藏 ---------------------
const emits = defineEmits(['refresh']);

  const visible = ref(false);
  function showModal(data) {
    visible.value = true;
    updateFormData(data);
    nextTick(() => {
      const domArr = document.getElementsByClassName('ant-modal');
      if (domArr && domArr.length > 0) {
        Array.from(domArr).forEach((item) => {
          if (item.childNodes && item.childNodes.length > 0) {
            Array.from(item.childNodes).forEach((child) => {
              if (child.setAttribute) {
                child.setAttribute('aria-hidden', 'false');
              }
            });
          }
        });
      }
    });
  }
  function closeModal() {
    visible.value = false;
    resetFormData();
  }

  // ----------------------- form 表单操作 ---------------------
  const formRef = ref();
  const departmentTreeSelect = ref();
  const defaultDepartmentForm = {
    departmentId: undefined,
    departmentName: undefined,
    departmentCode: undefined,
    managerId: undefined,
    parentId: undefined,
    sort: 0,
    status: 1,
    remark: undefined,
  };
  const employeeSelect = ref();

let formState = reactive({
  ...defaultDepartmentForm,
});

// status switch 双向绑定
const statusChecked = computed({
  get: () => formState.status === 1,
  set: (val) => { formState.status = val ? 1 : 0; },
});

// 表单校验规则
const rules = {
  parentId: [{ required: true, message: '上级部门不能为空' }],
  departmentName: [
    { required: true, message: '部门名称不能为空' },
    { max: 50, message: '部门名称不能大于50个字符', trigger: 'blur' },
  ],
  departmentCode: [
    { max: 50, message: '部门编码不能大于50个字符', trigger: 'blur' },
  ],
  remark: [
    { max: 200, message: '部门描述不能大于200个字符', trigger: 'blur' },
  ],
};
// 更新表单数据
function updateFormData(data) {
  Object.assign(formState, defaultDepartmentForm);
  if (data) {
    Object.assign(formState, data);
  }
  visible.value = true;
}
// 重置表单数据
function resetFormData() {
  Object.assign(formState, defaultDepartmentForm);
}

  async function handleOk() {
    try {
      await formRef.value.validate();
      if (formState.departmentId) {
        updateDepartment();
      } else {
        addDepartment();
      }
    } catch (error) {
      message.error('参数验证错误，请仔细填写表单数据!');
    }
  }

  // ----------------------- form 表单  ajax 操作 ---------------------
  async function addDepartment() {
    Loading.show();
    try {
      await departmentApi.addDepartment(formState);
      emits('refresh');
      closeModal();
    } catch (error) {
      sentry.captureError(error);
    } finally {
      Loading.hide();
    }
  }

  async function updateDepartment() {
    Loading.show();
    try {
      if (formState.parentId == formState.departmentId) {
        message.warning('上级部门不能为自己');
        return;
      }
      await departmentApi.updateDepartment(formState);
      emits('refresh');
      closeModal();
    } catch (error) {
      sentry.captureError(error);
    } finally {
      Loading.hide();
    }
  }
</script>
