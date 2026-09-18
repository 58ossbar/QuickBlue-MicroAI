<!--
  * 字典 数据 表单 弹窗
-->
<template>
  <a-modal
    :open="visible"
    :title="form.dictDataId ? '编辑字典' : '新增字典'"
    ok-text="确认"
    cancel-text="取消"
    width="560px"
    @ok="onSubmit"
    @cancel="onClose"
  >
    <a-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 17 }"
      class="dict-data-form"
    >
      <a-form-item label="字典分类" name="dictId">
        <a-select
          v-model:value="form.dictId"
          placeholder="请选择字典分类"
          :disabled="!!form.dictDataId"
          :options="typeOptions"
          :field-names="{ label: 'dictName', value: 'dictId' }"
          show-search
          :filter-option="filterTypeOption"
          @change="onTypeChange"
        />
      </a-form-item>

      <a-form-item label="字典类型" name="dictCode">
        <a-input v-model:value="form.dictCode" placeholder="请选择字典分类" disabled />
      </a-form-item>

      <a-form-item label="上级字典" name="parentCode">
        <a-select
          v-model:value="form.parentCode"
          placeholder="请选择上级字典"
          :options="parentOptions"
          :field-names="{ label: 'dataLabel', value: 'dataValue' }"
          allow-clear
          show-search
          :filter-option="filterParentOption"
        />
      </a-form-item>

      <a-form-item label="字典标签" name="dataLabel">
        <a-input v-model:value="form.dataLabel" placeholder="请输入字典标签" />
      </a-form-item>

      <a-form-item label="字典键值" name="dataValue">
        <a-input v-model:value="form.dataValue" placeholder="请输入字典键值" />
      </a-form-item>

      <a-form-item label="排序号" name="sortOrder">
        <a-input-number
          v-model:value="form.sortOrder"
          style="width: 100%"
          :min="0"
          :max="9999"
          placeholder="请输入排序号"
        />
      </a-form-item>

      <a-form-item label="状态" name="disabledFlag">
        <a-switch v-model:checked="enabled" checked-children="启用" un-checked-children="禁用" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { Loading } from '/@/components/framework/loading';
  import { dictApi } from '/@/api/support/dict-api';
  import { sentry } from '/@/lib/sentry';

  // emit
  const emit = defineEmits(['reloadList']);

  const formRef = ref();

  const formDefault = {
    dictId: undefined,
    dictCode: undefined,
    dictDataId: undefined,
    sortOrder: 0,
    dataValue: '',
    dataLabel: '',
    parentCode: undefined,
    disabledFlag: false,
  };
  let form = reactive({ ...formDefault });

  const enabled = computed({
    get: () => !form.disabledFlag,
    set: (val) => {
      form.disabledFlag = !val;
    },
  });

  const rules = {
    dictId: [{ required: true, message: '请选择字典分类' }],
    dataValue: [{ required: true, message: '请输入字典键值' }],
    dataLabel: [{ required: true, message: '请输入字典标签' }],
    sortOrder: [{ required: true, message: '请输入排序号', type: 'number' }],
  };

  const visible = ref(false);
  const typeList = ref([]);
  const parentOptions = ref([]);
  const parentRecord = ref(null);

  const typeOptions = computed(() => typeList.value);

  onMounted(() => {
    loadTypeList();
  });

  async function loadTypeList() {
    try {
      const res = await dictApi.getAllDict();
      // dictId 为 Long，统一转字符串避免前后端类型不一致导致下拉框匹配失败
      typeList.value = (res.data || []).map((e) => ({ ...e, dictId: String(e.dictId) }));
    } catch (error) {
      sentry.captureError(error);
    }
  }

  async function showModal(rowData, defaultDictId, parentData) {
    parentRecord.value = parentData || null;
    Object.assign(form, formDefault);

    if (rowData) {
      Object.assign(form, rowData);
      if (form.dictId) {
        form.dictId = String(form.dictId);
      }
    }

    if (!form.dictId && defaultDictId) {
      form.dictId = String(defaultDictId);
    }

    // 每次打开弹窗都刷新分类列表，确保新分类能显示
    await loadTypeList();

    syncTypeByDictId();
    await loadParentOptions(form.dictId);

    // 新增子级时，默认选中上级字典
    if (!rowData && parentData && parentData.dataValue) {
      form.parentCode = parentData.dataValue;
    }

    visible.value = true;
  }

  function syncTypeByDictId() {
    const type = typeList.value.find((e) => e.dictId === form.dictId);
    form.dictCode = type ? type.dictCode : '';
  }

  async function loadParentOptions(dictId) {
    parentOptions.value = [];
    if (!dictId) return;
    const type = typeList.value.find((e) => e.dictId === dictId);
    if (!type) return;
    try {
      const res = await dictApi.getAllDictData();
      const allData = res.data || [];
      // 过滤出当前分类下的数据作为上级字典候选，编辑时排除自身
      parentOptions.value = allData.filter(
        (e) => e.dictCode === type.dictCode && e.dictDataId !== form.dictDataId
      );
    } catch (error) {
      sentry.captureError(error);
    }
  }

  function onTypeChange() {
    syncTypeByDictId();
    form.parentCode = undefined;
    loadParentOptions(form.dictId);
  }

  function filterTypeOption(input, option) {
    const type = typeList.value.find((e) => e.dictId === option.value);
    if (!type) return false;
    return type.dictName?.includes(input) || type.dictCode?.includes(input);
  }

  function filterParentOption(input, option) {
    return option.dataLabel?.includes(input) || option.dataValue?.includes(input);
  }

  function onClose() {
    Object.assign(form, formDefault);
    parentRecord.value = null;
    parentOptions.value = [];
    visible.value = false;
  }

  function onSubmit() {
    formRef.value
      .validate()
      .then(async () => {
        Loading.show();
        try {
          const payload = { ...form };
          if (payload.parentCode === undefined || payload.parentCode === '') {
            payload.parentCode = null;
          }
          if (payload.dictDataId) {
            await dictApi.updateDictData(payload);
          } else {
            await dictApi.addDictData(payload);
          }
          message.success(`${payload.dictDataId ? '修改' : '新增'}成功`);
          emit('reloadList');
          onClose();
        } catch (error) {
          sentry.captureError(error);
        } finally {
          Loading.hide();
        }
      })
      .catch((error) => {
        console.log('error', error);
        message.error('参数验证错误，请仔细填写表单数据!');
      });
  }

  defineExpose({
    showModal,
  });
</script>
<style scoped lang="less">
  .dict-data-form {
    padding-top: 12px;

    :deep(.ant-input-number-handler-wrap) {
      display: flex;
    }
  }
</style>
