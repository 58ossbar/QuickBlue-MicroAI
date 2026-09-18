<!--
  * 系统更新日志 查看
  *

-->
<template>
  <a-modal title="更新日志" width="800px" :open="visibleFlag" @cancel="onClose" :wrapClassName="change-log-view-modal-wrapper">
    <div class="change-log-content" v-html="content"></div>
    <div v-if="link" class="change-log-link">
      链接：<a :href="link" target="_blank">{{ link }}</a>
    </div>

    <template #footer>
      <a-space>
        <a-button type="primary" @click="onClose">关闭</a-button>
      </a-space>
    </template>
  </a-modal>
</template>
<script setup>
  import { ref } from 'vue';

  const visibleFlag = ref(false);
  const content = ref('');
  const link = ref('');

  function show(changeLog) {
    content.value = changeLog.content;
    link.value = changeLog.link;
    visibleFlag.value = true;
  }

  function onClose() {
    visibleFlag.value = false;
  }

  defineExpose({
    show,
  });
</script>

<style lang="less" scoped>
  .change-log-content {
    max-height: 60vh;
    overflow-y: auto;
    padding: 16px;
    background-color: #fafafa;
    border-radius: 4px;
    line-height: 1.8;

    // HTML 内容样式
    :deep(img) {
      max-width: 100%;
      height: auto;
    }

    :deep(p) {
      margin-bottom: 12px;
    }

    :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
      margin-top: 16px;
      margin-bottom: 12px;
      font-weight: 600;
    }

    :deep(ul), :deep(ol) {
      margin-bottom: 12px;
      padding-left: 20px;
    }

    :deep(li) {
      margin-bottom: 6px;
    }

    :deep(blockquote) {
      margin: 12px 0;
      padding: 8px 16px;
      border-left: 4px solid #1890ff;
      background-color: #f0f7ff;
    }

    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 12px;

      th, td {
        border: 1px solid #e8e8e8;
        padding: 8px 12px;
        text-align: left;
      }

      th {
        background-color: #fafafa;
        font-weight: 600;
      }
    }

    :deep(code) {
      padding: 2px 6px;
      background-color: #f5f5f5;
      border-radius: 3px;
      font-family: 'Courier New', monospace;
      font-size: 14px;
    }

    :deep(pre) {
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
      overflow-x: auto;

      code {
        padding: 0;
        background-color: transparent;
      }
    }
  }

  .change-log-link {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #e8e8e8;
  }
</style>

<style lang="less">
  .change-log-view-modal-wrapper .ant-modal-body {
    padding-top: 16px;
    padding-bottom: 16px;
  }

  .change-log-view-modal-wrapper .ant-modal-footer {
    position: relative;
    z-index: 10;
  }
</style>
