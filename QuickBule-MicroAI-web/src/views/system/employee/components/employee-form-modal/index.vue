<!--
  *  员工 表单 弹窗
  *

-->
<template>
  <a-modal
    :title="form.employeeId ? '编辑员工' : '添加员工'"
    :width="900"
    :open="visible"
    @cancel="onClose"
    destroyOnClose
    :footer="null"
    wrap-class-name="employee-modal-wrapper"
  >
    <div class="modal-content">
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="姓名" name="actualName">
          <a-input v-model:value.trim="form.actualName" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value.trim="form.phone" placeholder="请输入手机号" :disabled="!!form.employeeId" />
          <p v-if="form.employeeId" class="hint">脱敏展示，不可修改</p>
        </a-form-item>
        <a-form-item label="部门" name="departmentId">
          <DepartmentTreeSelect ref="departmentTreeSelect" width="100%" :init="false" v-model:value="form.departmentId" />
        </a-form-item>
        <a-form-item label="登录名" name="loginName">
          <a-input v-model:value.trim="form.loginName" placeholder="请输入登录名" />
          <p class="hint">初始密码默认为：随机</p>
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value.trim="form.email" placeholder="请输入邮箱" :disabled="!!form.employeeId" />
          <p v-if="form.employeeId" class="hint">脱敏展示，不可修改</p>
        </a-form-item>
        <a-form-item label="性别" name="gender">
          <enum-select style="width: 100%" v-model:value="form.gender" placeholder="请选择性别" enum-name="GENDER_ENUM" />
        </a-form-item>
        <a-form-item label="状态" name="disabledFlag">
          <a-select v-model:value="form.disabledFlag" placeholder="请选择状态">
            <a-select-option :value="0">启用</a-select-option>
            <a-select-option :value="1">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="岗位" name="positionId">
          <PositionSelect v-model:value="form.positionId" placeholder="请选择岗位" />
        </a-form-item>
        <a-form-item label="角色" name="roleIdList">
          <a-select mode="multiple" v-model:value="form.roleIdList" optionFilterProp="title" placeholder="请选择角色">
            <a-select-option v-for="item in roleList" :key="item.roleId" :title="item.roleName">{{ item.roleName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="超级管理员" name="administratorFlag">
          <a-checkbox v-model:checked="form.administratorFlag">设为超级管理员</a-checkbox>
          <p class="hint">超级管理员拥有所有权限，请谨慎设置</p>
        </a-form-item>
      </a-form>
    </div>
    <div class="footer">
      <a-button style="margin-right: 8px" @click="onClose">取消</a-button>
      <a-button type="primary" style="margin-right: 8px" @click="onSubmit(false)">保存</a-button>
      <a-button v-if="!form.employeeId" type="primary" @click="onSubmit(true)">保存并继续添加</a-button>
    </div>
  </a-modal>
</template>
<script setup>
  import { message } from 'ant-design-vue';
  import _ from 'lodash';
  import { computed, nextTick, reactive, ref } from 'vue';
  import { employeeApi } from '/@/api/system/employee-api';
  import { roleApi } from '/@/api/system/role-api';
  import DepartmentTreeSelect from '/@/components/system/department-tree-select/index.vue';
  import EnumSelect from '/@/components/framework/enum-select/index.vue';
  import PositionSelect from '/@/components/system/position-select/index.vue';
  import { GENDER_ENUM } from '/@/constants/common-const';
  import { regular } from '/@/constants/regular-const';
  import { Loading } from '/@/components/framework/loading';
  import { sentry } from '/@/lib/sentry';
  // ----------------------- 以下是字段定义 emits props ---------------------
  const departmentTreeSelect = ref();
  // emit
  const emit = defineEmits(['refresh', 'show-account']);

  // ----------------------- 显示/隐藏 ---------------------

  const visible = ref(false); // 是否展示弹窗
  // 隐藏
  function onClose() {
    reset();
    visible.value = false;
  }
  // 显示
  async function showDrawer(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    visible.value = true;
    nextTick(() => {
      queryAllRole();
    });
  }

  // 为了兼容，暴露 showDrawer 方法
  function showModal(rowData) {
    showDrawer(rowData);
  }

  // ----------------------- 表单显示 ---------------------

  const roleList = ref([]); //角色列表
  async function queryAllRole() {
    let res = await roleApi.queryAll();
    roleList.value = res.data;
  }

  const formRef = ref(); // 组件ref
  const formDefault = {
    employeeId: undefined,
    actualName: undefined,
    departmentId: undefined,
    disabledFlag: 0,
    leaveFlag: 0,
    gender: GENDER_ENUM.MAN.value,
    loginName: undefined,
    phone: undefined,
    roleIdList: undefined,
    positionId: undefined,
    email: undefined,
    administratorFlag: false,
  };

  let form = reactive(_.cloneDeep(formDefault));
  function reset() {
    Object.assign(form, formDefault);
    formRef.value.resetFields();
  }

  // ----------------------- 表单提交 ---------------------
  // 是否为编辑模式
  const isEdit = computed(() => !!form.employeeId);

  // 表单规则（编辑模式下手机号/邮箱为脱敏只读展示，不参与校验，避免脱敏值触发格式错误）
  const rules = computed(() => ({
    actualName: [
      { required: true, message: '姓名不能为空' },
      { max: 30, message: '姓名不能大于30个字符', trigger: 'blur' },
    ],
    phone: isEdit.value
      ? []
      : [
          { required: true, message: '手机号不能为空' },
          { pattern: regular.phone, message: '请输入正确的手机号码', trigger: 'blur' },
        ],
    loginName: [
      { required: true, message: '登录账号不能为空' },
      { max: 30, message: '登录账号不能大于30个字符', trigger: 'blur' },
    ],
    gender: [{ required: true, message: '性别不能为空' }],
    departmentId: [{ required: true, message: '部门不能为空' }],
    disabledFlag: [{ required: true, message: '状态不能为空' }],
    leaveFlag: [{ required: true, message: '在职状态不能为空' }],
    email: isEdit.value
      ? []
      : [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  }));

  // 校验表单
  function validateForm(formRef) {
    return new Promise((resolve) => {
      formRef
        .validate()
        .then(() => {
          resolve(true);
        })
        .catch(() => {
          resolve(false);
        });
    });
  }

  // 提交数据
  async function onSubmit(keepAdding) {
    // 编辑模式下手机号/邮箱为脱敏只读展示，提交时剔除，避免脱敏值回写覆盖真实数据
    if (form.employeeId) {
      delete form.phone;
      delete form.email;
    }
    let validateFormRes = await validateForm(formRef.value);
    if (!validateFormRes) {
      message.error('参数验证错误，请仔细填写表单数据!');
      return;
    }
    Loading.show();
    if (form.employeeId) {
      await updateEmployee(keepAdding);
    } else {
      await addEmployee(keepAdding);
    }
  }

  async function addEmployee(keepAdding) {
    try {
      let { data } = await employeeApi.addEmployee(form);
      message.success('添加成功');
      emit('show-account', form.loginName, data);
      if (keepAdding) {
        reset();
      } else {
        onClose();
      }
      emit('refresh');
    } catch (error) {
      sentry.captureError(error);
    } finally {
      Loading.hide();
    }
  }
  async function updateEmployee(keepAdding) {
    try {
      let result = await employeeApi.updateEmployee(form);
      message.success('更新成功');
      if (keepAdding) {
        reset();
      } else {
        onClose();
      }
      emit('refresh');
    } catch (error) {
      sentry.captureError(error);
    } finally {
      Loading.hide();
    }
  }

  // ----------------------- 以下是暴露的方法内容 ----------------------------
  defineExpose({
    showDrawer,
    showModal,
  });
</script>
<style scoped lang="less">
  .modal-content {
    max-height: 55vh;
    overflow-y: auto;
    padding-right: 8px;
  }
  :deep(.ant-form-item) {
    margin-bottom: 16px;
  }
  .footer {
    margin-top: 16px;
    text-align: right;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    padding-bottom: 4px;
  }
  .hint {
    margin-top: 4px;
    margin-bottom: 0;
    color: #8c8c8c;
    font-size: 12px;
    line-height: 1.5;
  }
</style>
