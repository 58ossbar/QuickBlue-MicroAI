<!--
  * 区域表单
  * @Author: zhujw
  * @Date: 2025-12-20
-->
<template>
  <a-modal
      :title="form.id ? '编辑区域' : '添加区域'"
      :width="700"
      :open="visibleFlag"
      @cancel="onClose"
      :maskClosable="false"
      :destroyOnClose="true"
  >
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 8 }">
      <!-- 第一行：编码和名称 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="区域编码" name="regionCode">
            <a-input
                v-model:value="form.regionCode"
                placeholder="请输入区域编码"
                :disabled="!!form.id"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="区域名称" name="regionName">
            <a-input
                v-model:value="form.regionName"
                placeholder="请输入区域名称"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 第二行：简称和上级区域 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="区域简称" name="regionShortName">
            <a-input
                v-model:value="form.regionShortName"
                placeholder="请输入区域简称"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="上级区域" name="parentId">
            <RegionTreeSelect
                ref="regionTreeSelect"
                v-model:value="form.parentId"
                placeholder="请选择上级区域"
                style="width: 100%"
                :exclude-node-id="form.id"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 第三行：层级和排序 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="区域层级" name="level">
            <a-input-number
                style="width: 100%"
                v-model:value="form.level"
                :min="1"
                :max="4"
                placeholder="区域层级"
                disabled
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="排序" name="sortOrder">
            <a-input-number
                style="width: 100%"
                v-model:value="form.sortOrder"
                :min="0"
                placeholder="排序（越大越靠前）"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 第四行：状态和联系电话 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="状态" name="status">
            <a-radio-group v-model:value="form.status">
              <a-radio :value="1">启用</a-radio>
              <a-radio :value="2">禁用</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="联系电话" name="contactPhone">
            <a-input
                v-model:value="form.contactPhone"
                placeholder="联系电话"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 第五行：负责人 -->
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="区域负责人" name="regionManagerName">
            <a-input
                v-model:value="form.regionManagerName"
                placeholder="区域负责人姓名"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 第六行：备注（独占一行） -->
      <a-row :gutter="16">
        <a-col :span="24">
          <a-form-item label="备注" name="remark" :label-col="{ span: 4 }">
            <a-textarea
                v-model:value="form.remark"
                placeholder="请输入备注"
                :rows="3"
                style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>
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
import { reactive, ref, nextTick, watch } from 'vue';
import _ from 'lodash';
import { message } from 'ant-design-vue';
import { Loading } from '/@/components/framework/loading';
import { regionApi } from '/@/api/business/region/region-api';
import { sentry } from '/@/lib/sentry';
import RegionTreeSelect from './RegionTreeSelect.vue';

const emits = defineEmits(['reloadList']);

// ------------------------ 显示与隐藏 ------------------------
const visibleFlag = ref(false);
const regionTreeSelect = ref();

function show(rowData) {
  Object.assign(form, formDefault);
  if (rowData && !_.isEmpty(rowData)) {
    Object.assign(form, rowData);
    // 如果parentId为空，设为null以便清空选择
    if (form.parentId === '' || form.parentId === undefined) {
      form.parentId = null;
    }
  }
  visibleFlag.value = true;
  nextTick(() => {
    formRef.value?.clearValidate();
    // 如果区域选择组件有刷新方法，调用它
    if (regionTreeSelect.value && regionTreeSelect.value.refresh) {
      regionTreeSelect.value.refresh();
    }
  });
}

function onClose() {
  Object.assign(form, formDefault);
  visibleFlag.value = false;
}

// ------------------------ 表单 ------------------------
const formRef = ref();

const formDefault = {
  id: undefined,
  regionCode: '',
  regionName: '',
  regionShortName: '',
  parentId: null,
  level: 1,
  sortOrder: 0,
  status: 1,
  regionManagerId: undefined,
  regionManagerName: '',
  contactPhone: '',
  remark: '',
};

let form = reactive({ ...formDefault });

const rules = {
  regionCode: [
    { required: true, message: '区域编码不能为空' },
    { max: 50, message: '区域编码不能超过50个字符' },
  ],
  regionName: [
    { required: true, message: '区域名称不能为空' },
    { max: 100, message: '区域名称不能超过100个字符' },
  ],
  parentId: [{ required: false }],
  level: [{ required: true, message: '区域层级不能为空' }],
  sortOrder: [{ required: true, message: '排序不能为空' }],
  status: [{ required: true, message: '状态不能为空' }],
};

// 监听parentId变化，计算level
watch(() => form.parentId, (newParentId) => {
  if (!newParentId) {
    form.level = 1;
  } else {
    // 实际项目中应该从API获取父节点的level，这里简化处理
    form.level = 2; // 默认设置为2
  }
});

async function onSubmit() {
  try {
    await formRef.value.validateFields();
    save();
  } catch (err) {
    message.error('参数验证错误，请仔细填写表单数据!');
  }
}

async function save() {
  Loading.show();
  try {
    if (form.id) {
      await regionApi.update(form);
    } else {
      await regionApi.add(form);
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

defineExpose({ show });
</script>

<style scoped lang="less">
/* 如果需要自定义样式可以在这里添加 */
.ant-form-item {
  margin-bottom: 16px;
}

.ant-row {
  margin-bottom: 4px;
}
</style>
