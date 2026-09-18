<!--
  * 系统更新日志
  *
-->
<template>
  <a-modal
    :title="form.changeLogId ? '编辑' : '添加'"
    width="800px"
    :closable="true"
    :open="visibleFlag"
    @close="onClose"
    :onCancel="onClose"
    :maskClosable="false"
    :destroyOnClose="true"
    :wrapClassName="change-log-modal-wrapper"
  >
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="版本" name="updateVersion">
            <a-input v-model:value="form.updateVersion" placeholder="版本" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="更新类型" name="type">
            <EnumSelect width="100%" v-model:value="form.type" enumName="CHANGE_LOG_TYPE_ENUM" placeholder="更新类型" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="发布人" name="publishAuthor">
            <a-input v-model:value="form.publishAuthor" placeholder="发布人" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="发布日期" name="publicDate">
            <a-date-picker valueFormat="YYYY-MM-DD" v-model:value="form.publicDate" style="width: 100%" placeholder="发布日期" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item label="跳转链接" name="link">
        <a-input v-model:value="form.link" placeholder="跳转链接" />
      </a-form-item>
      <a-form-item label="更新内容" name="content">
        <Wangeditor ref="contentRef" :modelValue="form.content" :height="230" />
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
  import { reactive, ref, nextTick } from 'vue';
  import _ from 'lodash';
  import { message } from 'ant-design-vue';
  import { Loading } from '/@/components/framework/loading';
  import { changeLogApi } from '/@/api/support/change-log-api';
  import { sentry } from '/@/lib/sentry';
  import EnumSelect from '/@/components/framework/enum-select/index.vue';
  import Wangeditor from '/@/components/framework/wangeditor/index.vue';

  // ------------------------ 事件 ------------------------

  const emits = defineEmits(['reloadList']);

  // ------------------------ 显示与隐藏 ------------------------
  // 是否显示
  const visibleFlag = ref(false);

  function show(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    visibleFlag.value = true;
    nextTick(() => {
      formRef.value.clearValidate();
    });
  }

  function onClose() {
    Object.assign(form, formDefault);
    visibleFlag.value = false;
  }

  // ------------------------ 表单 ------------------------

  // 组件ref
  const formRef = ref();
  const contentRef = ref();

  const formDefault = {
    changeLogId: undefined,
    updateVersion: undefined, //版本
    type: undefined, //更新类型:[1:特大版本功能更新;2:功能更新;3:bug修复]
    publishAuthor: undefined, //发布人
    publicDate: undefined, //发布日期
    content: '', //更新内容
    link: undefined, //跳转链接
  };

  let form = reactive({ ...formDefault });

  const rules = {
    updateVersion: [{ required: true, message: '版本 必填' }],
    type: [{ required: true, message: '更新类型:[1:特大版本功能更新;2:功能更新;3:bug修复] 必填' }],
    publishAuthor: [{ required: true, message: '发布人 必填' }],
    publicDate: [{ required: true, message: '发布日期 必填' }],
    content: [{ required: true, message: '更新内容 必填' }],
  };

  // 点击确定，验证表单
  async function onSubmit() {
    try {
      form.content = contentRef.value.getHtml();
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
      if (form.changeLogId) {
        await changeLogApi.update(form);
      } else {
        await changeLogApi.add(form);
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

<style lang="less">
  .change-log-modal-wrapper {
    .ant-modal-body {
      max-height: 65vh;
      overflow-y: auto;
      padding-right: 0;
      padding-bottom: 8px;
    }

    /* 确保编辑器所有元素可交互 */
    .w-e-toolbar,
    .w-e-toolbar *,
    .w-e-text-container,
    .w-e-text-container * {
      pointer-events: auto !important;
    }

    /* 优化编辑器容器样式 */
    .ant-form-item {
      margin-bottom: 16px;
    }

    .ant-row {
      margin-bottom: 0 !important;
    }
  }

  /* 编辑器工具栏和下拉菜单的 z-index 设置 */
  .change-log-modal-wrapper .w-e-toolbar {
    position: relative;
    z-index: 1001 !important;
  }

  .change-log-modal-wrapper .w-e-bar-item {
    pointer-events: auto !important;
  }

  .change-log-modal-wrapper .w-e-bar-item button {
    pointer-events: auto !important;
    cursor: pointer !important;
  }

  .change-log-modal-wrapper .w-e-panel-container {
    z-index: 10050 !important;
    pointer-events: auto !important;
  }

  .change-log-modal-wrapper .w-e-select-list {
    z-index: 10050 !important;
    pointer-events: auto !important;
  }

  .change-log-modal-wrapper .w-e-select-list .w-e-select-option {
    pointer-events: auto !important;
    cursor: pointer !important;
  }

  .change-log-modal-wrapper .w-e-full-screen-container {
    z-index: 99999 !important;
  }

  .change-log-modal-wrapper .w-e-bar-tooltip {
    z-index: 10050 !important;
  }

  .change-log-modal-wrapper .w-e-emotion-panel {
    z-index: 10050 !important;
  }

  .change-log-modal-wrapper .w-e-text-container {
    pointer-events: auto !important;
  }

  /* 确保模态框的 footer 区域不会被覆盖 */
  .change-log-modal-wrapper .ant-modal-footer {
    position: relative;
    z-index: 10000 !important;
    background-color: #fff;
  }
</style>
