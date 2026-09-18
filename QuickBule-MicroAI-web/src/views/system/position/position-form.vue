<!--
  * 岗位表单弹窗
  *

-->
<template>
  <a-modal
    :title="form.positionId ? '编辑岗位' : '添加岗位'"
    width="680px"
    :open="visibleFlag"
    @cancel="onClose"
    :maskClosable="false"
    :destroyOnClose="true"
    :bodyStyle="{ padding: '24px 32px', maxHeight: '65vh', overflowY: 'auto' }"
    forceRender
  >
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="岗位名称" name="positionName">
        <a-input style="width: 100%" v-model:value.trim="form.positionName" placeholder="请输入岗位名称" />
      </a-form-item>
      <a-form-item label="岗位编码" name="positionCode">
        <a-input style="width: 100%" v-model:value.trim="form.positionCode" placeholder="留空自动生成" />
        <div v-if="form.parentId && autoPositionCode" style="color: #1677ff; font-size: 12px; margin-top: 4px;">
          建议编码：{{ autoPositionCode }}
        </div>
      </a-form-item>
      <a-form-item label="岗位类别" name="category">
        <DictSelect dictCode="POST_TYPE" v-model:value="form.category" placeholder="请选择岗位类别" width="100%" />
      </a-form-item>
      <a-form-item label="父级岗位" name="parentId">
        <a-select
          style="width: 100%"
          v-model:value="form.parentId"
          placeholder="请选择父级岗位（可选）"
          allowClear
          showSearch
          :filterOption="filterParentOption"
          @change="onParentChange"
        >
          <a-select-option v-for="item in parentPositionList" :key="item.positionId" :value="item.positionId">
            {{ item.positionName }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="职级等级" name="gradeLevel">
        <a-input-number style="width: 100%" v-model:value="form.gradeLevel" :min="1" placeholder="选择父级后自动推算" :precision="0" />
      </a-form-item>
      <a-form-item label="排序" name="sort">
        <a-input-number style="width: 100%" v-model:value="form.sort" :min="0" placeholder="选择父级后自动推算" :precision="0" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-switch v-model:checked="statusChecked" checked-children="启用" un-checked-children="停用" />
        <span style="margin-left: 12px; color: #666; font-size: 13px;">{{ statusChecked ? '当前为启用状态' : '当前为停用状态' }}</span>
      </a-form-item>
      <a-form-item label="岗位职责描述" name="description">
        <a-textarea v-model:value="form.description" placeholder="请输入岗位职责描述（可选）" :rows="5" :maxlength="2000" showCount />
      </a-form-item>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="onClose">取消</a-button>
        <a-button type="primary" @click="onSubmit">保存</a-button>
      </a-space>
    </template>
  </a-modal>
</template>
<script setup>
  import { reactive, ref, nextTick, computed } from 'vue';
  import _ from 'lodash';
  import { message } from 'ant-design-vue';
  import { Loading } from '/@/components/framework/loading';
  import { positionApi } from '/@/api/system/position-api';
  import { sentry } from '/@/lib/sentry';
  import DictSelect from '/@/components/support/dict-select/index.vue';

  // ------------------------ 事件 ------------------------

  const emits = defineEmits(['reloadList']);

  // ------------------------ 显示与隐藏 ------------------------
  // 是否显示
  const visibleFlag = ref(false);

  // 上级岗位列表（用于下拉选择）
  const parentPositionList = ref([]);

  async function loadParentList() {
    try {
      let resp = await positionApi.queryList();
      parentPositionList.value = resp.data || [];
    } catch (e) {
      sentry.captureError(e);
    }
  }

  function filterParentOption(input, option) {
    return option.children && option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  }

  function show(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
      // parentId 可能是 0 或空字符串，做下处理
      if (!form.parentId) {
        form.parentId = undefined;
      }
    }
    visibleFlag.value = true;
    loadParentList().then(() => {
      // 父级列表加载完成后，若已有parentId则触发自动推算
      if (form.parentId) {
        onParentChange(form.parentId);
      }
    });
    nextTick(() => {
      formRef.value.clearValidate();

      // 解决弹窗错误信息显示
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

  function onClose() {
    Object.assign(form, formDefault);
    visibleFlag.value = false;
  }

  // ------------------------ 表单 ------------------------

  // 组件ref
  const formRef = ref();

  const formDefault = {
    positionId: undefined,
    positionName: undefined,
    positionCode: undefined,
    category: undefined,
    gradeLevel: undefined,
    parentId: undefined,
    sort: 0,
    status: 1,
    remark: undefined,
    description: undefined,
  };

  let form = reactive({ ...formDefault });

  // status switch 双向绑定
  const statusChecked = computed({
    get: () => form.status === 1,
    set: (val) => { form.status = val ? 1 : 0; },
  });

  // 建议编码：父编码 + 序号
  const autoPositionCode = computed(() => {
    if (!form.parentId) return '';
    const parent = parentPositionList.value.find(p => p.positionId === form.parentId);
    if (!parent || !parent.positionCode) return '';
    // 同级已有岗位数 + 1
    const siblings = parentPositionList.value.filter(p => p.parentId === form.parentId);
    const seq = siblings.length + 1;
    return parent.positionCode + '-' + String(seq).padStart(2, '0');
  });

  /** 选择父级后自动推算职级等级和排序 */
  function onParentChange(parentId) {
    if (!parentId) {
      // 仅当本次操作是新增时重置（编辑时不重置已有值）
      if (!form.positionId) {
        form.gradeLevel = undefined;
        form.sort = 0;
      }
      return;
    }
    const parent = parentPositionList.value.find(p => p.positionId === parentId);
    if (!parent) return;
    // 职级等级 = 父级 + 1
    if (parent.gradeLevel != null) {
      form.gradeLevel = parent.gradeLevel + 1;
    }
    // 排序 = 同父级最大排序 + 1
    const siblings = parentPositionList.value.filter(p => p.parentId === parentId);
    const maxSort = siblings.length > 0 ? Math.max(...siblings.map(s => s.sort || 0)) : -1;
    form.sort = maxSort + 1;
  }

  const rules = {
    positionName: [
      { required: true, message: '请输入岗位名称' },
      { max: 50, message: '岗位名称不能大于50个字符', trigger: 'blur' },
    ],
    positionCode: [
      { max: 100, message: '岗位编码不能大于100个字符', trigger: 'blur' },
    ],
    gradeLevel: [
      { type: 'number', message: '职级等级必须为数字' },
    ],
    description: [
      { max: 2000, message: '岗位职责描述不能大于2000个字符', trigger: 'blur' },
    ],
  };

  // 点击确定，验证表单
  async function onSubmit() {
    try {
      await formRef.value.validateFields();
      save();
    } catch (err) {
      message.error('参数验证错误，请仔细填写表单数据!');
    }
  }

  // 新建、编辑API
  async function save() {
    Loading.show();
    try {
      if (form.positionId) {
        await positionApi.update(form);
      } else {
        await positionApi.add(form);
      }
      message.success('操作成功');
      emits('reloadList');
      onClose();
    } catch (err) {
      sentry.captureError(err);
    } finally {
      Loading.hide();
    }
  }

  defineExpose({
    show,
  });
</script>
