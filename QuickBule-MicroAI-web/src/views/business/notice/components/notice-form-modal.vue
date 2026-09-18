<!--
  * 通知  表单
  *
-->
<template>
  <a-modal
    v-model:open="visible"
    :title="formData.noticeId ? '编辑' : '新建'"
    :width="800"
    :footer="null"
    @cancel="onClose"
    :destroyOnClose="true"
    wrapClassName="notice-modal-wrapper"
  >
    <a-spin :spinning="loading">
      <div class="modal-content">
        <div class="modal-body">
          <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="公告标题" name="title">
          <a-input v-model:value="formData.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-form-item label="分类" name="noticeTypeId">
          <a-select v-model:value="formData.noticeTypeId" style="width: 100%" :showSearch="true" :allowClear="true">
            <a-select-option v-for="item in noticeTypeList" :key="item.noticeTypeId" :value="item.noticeTypeId">
              {{ item.noticeTypeName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="文号">
          <a-input v-model:value="formData.documentNumber" placeholder="文号，如：长影集团发〔2026〕字第36号" />
        </a-form-item>
        <a-form-item label="作者" name="author">
          <a-input v-model:value="formData.author" placeholder="请输入作者" />
        </a-form-item>
        <a-form-item label="来源" name="source">
          <a-input v-model:value="formData.source" placeholder="请输入来源" />
        </a-form-item>
        <a-form-item label="可见范围" name="allVisibleFlag">
          <a-select v-model:value="formData.allVisibleFlag" placeholder="请选择可见范围">
            <a-select-option :value="1">全部可见</a-select-option>
            <a-select-option :value="0">部分可见</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-show="!formData.allVisibleFlag" label="可见员工/部门">
          <a-button type="primary" @click="showNoticeVisibleModal">选择</a-button>
          <div class="visible-list">
            <div class="visible-item" v-for="(item, index) in formData.visibleRangeList" :key="item.dataId">
              <a-tag>
                <span>{{ item.dataName }}</span>
                <close-outlined @click="removeVisibleItem(index)" />
              </a-tag>
            </div>
          </div>
        </a-form-item>
        <a-form-item label="定时发布">
          <a-switch
            v-model:checked="formData.scheduledPublishFlag"
            checked-children="开"
            un-checked-children="关"
            @change="changesSheduledPublishFlag"
          />
        </a-form-item>
        <a-form-item v-show="formData.scheduledPublishFlag" label="发布时间">
          <a-date-picker
            v-model:value="releaseTime"
            :format="timeFormat"
            showTime
            :allowClear="false"
            placeholder="请选择发布时间"
            style="width: 200px"
            @change="changeTime"
          />
        </a-form-item>
        <a-form-item label="公告内容" name="contentHtml">
          <Wangeditor ref="contentRef" :modelValue="formData.contentHtml" :height="300" />
        </a-form-item>
        <a-form-item label="附件">
          <Upload
            :defaultFileList="defaultFileList"
            :maxUploadSize="10"
            :folderType="FILE_FOLDER_TYPE_ENUM.NOTICE.value"
            buttonText="上传附件"
            listType="text"
            extraMsg="最多上传10个附件，支持格式：图片(jpg/jpeg/png/gif/bmp)、PDF、Word(doc/docx)、RAR、ZIP"
            accept=".jpg,.jpeg,.png,.gif,.bmp,.pdf,.doc,.docx,.rar,.zip"
            @change="changeAttachment"
          />
        </a-form-item>
          </a-form>
        </div>

        <div class="modal-footer">
        <a-space>
          <a-button @click="onClose">取消</a-button>
          <a-button type="primary" @click="onSubmit">保存</a-button>
          </a-space>
        </div>
      </div>
    </a-spin>

    <!-- 选择可见范围弹窗 -->
    <NoticeFormVisibleModal ref="noticeFormVisibleModal" @selectedFinish="finishCanSelectedVisibleRange" />
  </a-modal>
</template>

<script setup>
  import { reactive, ref, onMounted, nextTick } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import _ from 'lodash';
  import dayjs from 'dayjs';
  import { Loading } from '/src/components/framework/loading';
  import { FILE_FOLDER_TYPE_ENUM } from '/src/constants/support/file-const';
  import { noticeApi } from '/src/api/business/notice/notice-api';
  import Wangeditor from '/src/components/framework/wangeditor/index.vue';
  import Upload from '/src/components/support/file-upload/index.vue';
  import NoticeFormVisibleModal from './notice-form-visible-modal.vue';
  import { sentry } from '/src/lib/sentry';

  const emits = defineEmits(['reloadList']);

  // ------------------ 显示，关闭 ------------------
  const visible = ref(false);
  const loading = ref(false);

  function showModal(noticeId) {
    Object.assign(formData, defaultFormData);
    releaseTime.value = null;
    defaultFileList.value = [];
    queryNoticeTypeList();
    if (noticeId) {
      getNoticeUpdate(noticeId);
    }

    visible.value = true;
    nextTick(() => {
      formRef.value?.clearValidate();
    });
  }

  function onClose() {
    visible.value = false;
    Object.assign(formData, defaultFormData);
  }

  // ------------------ 表单 ------------------

  const formRef = ref();
  const contentRef = ref();
  const noticeFormVisibleModal = ref();

  const defaultFormData = {
    noticeId: undefined,
    noticeTypeId: undefined,
    title: undefined, // 标题
    categoryId: undefined, // 分类
    source: undefined, // 来源
    documentNumber: undefined, // 文号
    author: undefined, // 作者
    allVisibleFlag: 1, // 是否全部可见
    visibleRangeList: [], // 可见范围
    scheduledPublishFlag: false, // 是否定时发布
    publishTime: undefined, // 发布时间
    attachment: [], // 附件
    contentHtml: '', // html内容
    contentText: '', // 纯文本内容
  };

  const formData = reactive({ ...defaultFormData });

  const formRules = {
    title: [{ required: true, message: '请输入' }],
    noticeTypeId: [{ required: true, message: '请选择分类' }],
    allVisibleFlag: [{ required: true, message: '请选择' }],
    source: [{ required: true, message: '请输入来源' }],
    author: [{ required: true, message: '请输入作者' }],
    contentHtml: [{ required: true, message: '请输入内容' }],
  };

  async function getNoticeUpdate(noticeId) {
    try {
      loading.value = true;
      const result = await noticeApi.getUpdateNoticeInfo(noticeId);
      const attachment = result.data.attachment;
      if (!_.isEmpty(attachment)) {
        defaultFileList.value = attachment;
      } else {
        defaultFileList.value = [];
      }
      Object.assign(formData, result.data);
      formData.allVisibleFlag = formData.allVisibleFlag ? 1 : 0;

      releaseTime.value = dayjs(result.data.publishTime);
    } catch (err) {
      sentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  async function onSubmit() {
    try {
      formData.contentHtml = contentRef.value.getHtml();
      formData.contentText = contentRef.value.getText();
      await formRef.value.validateFields();
      save();
    } catch (err) {
      message.error('参数验证错误，请仔细填写表单数据!');
    }
  }

  async function save() {
    try {
      Loading.show();
      if (formData.allVisibleFlag) {
        formData.visibleRangeList = [];
      }
      if (!formData.publishTime) {
        formData.publishTime = dayjs().format(timeFormat);
      }
      if (formData.noticeId) {
        await noticeApi.updateNotice(formData);
      } else {
        await noticeApi.addNotice(formData);
      }
      message.success('保存成功');
      emits('reloadList');
      onClose();
    } catch (err) {
      sentry.captureError(err);
    } finally {
      Loading.hide();
    }
  }

  // ------------------ 通知分类 ------------------

  const noticeTypeList = ref([]);
  async function queryNoticeTypeList() {
    try {
      const result = await noticeApi.getAllNoticeTypeList();
      noticeTypeList.value = result.data;
      if (noticeTypeList.value.length > 0 && !formData.noticeId) {
        formData.noticeTypeId = noticeTypeList.value[0].noticeTypeId;
      }
    } catch (err) {
      sentry.captureError(err);
    }
  }

  // ----------------------- 可见员工/部门 ----------------------------
  function showNoticeVisibleModal() {
    const visibleRangeList = formData.visibleRangeList || [];
    noticeFormVisibleModal.value.showModal(visibleRangeList);
  }

  function finishCanSelectedVisibleRange(selectedList) {
    formData.visibleRangeList = selectedList;
  }

  function removeVisibleItem(index) {
    Modal.confirm({
      title: '提示',
      content: '确定移除吗？',
      onOk() {
        formData.visibleRangeList.splice(index, 1);
      },
    });
  }

  // ----------------------- 发布时间 ----------------------------
  const timeFormat = 'YYYY-MM-DD HH:mm:ss';
  const releaseTime = ref(null);
  function changeTime(date, dateString) {
    formData.publishTime = dateString;
  }
  function changesSheduledPublishFlag(checked) {
    releaseTime.value = checked ? dayjs() : null;
    formData.publishTime = checked ? dayjs().format(timeFormat) : null;
  }

  // ----------------------- 上传附件 ----------------------------
  const defaultFileList = ref([]);
  function changeAttachment(fileList) {
    defaultFileList.value = fileList;
    formData.attachment = _.isEmpty(fileList) ? [] : fileList;
  }

  defineExpose({
    showModal,
  });
</script>

<style lang="less" scoped>
.modal-content {
  display: flex;
  flex-direction: column;
  height: 65vh;
  max-height: calc(100vh - 230px);
  min-height: 300px;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
  padding-bottom: 16px;
  min-height: 0;
}

.modal-body::-webkit-scrollbar {
  width: 6px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.modal-body::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.modal-footer {
  flex-shrink: 0;
  margin-top: 0;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}

.visible-list {
  display: flex;
  flex-wrap: wrap;
  .visible-item {
    padding-top: 8px;
  }
}
</style>

<style lang="less">
/* 确保弹窗内容区域 flex 布局正常工作 */
.notice-modal-wrapper .ant-modal-body {
  overflow: hidden !important;
}

/* 编辑器全屏样式修复 */
.notice-modal-wrapper .w-e-full-screen-container {
  z-index: 99999 !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  position: fixed !important;
}

/* 确保编辑器工具栏和面板在全屏模式下正常显示 */
.notice-modal-wrapper .w-e-toolbar {
  position: relative;
  z-index: 1000 !important;
}

.notice-modal-wrapper .w-e-panel-container {
  z-index: 10050 !important;
}

.notice-modal-wrapper .w-e-select-list {
  z-index: 10050 !important;
}

.notice-modal-wrapper .w-e-bar-tooltip {
  z-index: 10050 !important;
}

.notice-modal-wrapper .w-e-emotion-panel {
  z-index: 10050 !important;
}
</style>
