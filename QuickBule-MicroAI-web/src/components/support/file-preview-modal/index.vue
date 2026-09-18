<!--
  * 文件预览 弹窗
  *

  *
-->
<template>
  <div class="container">
    <a-image
      class="img-prev"
      :style="{ display: 'none' }"
      :preview="{
        visible,
        onVisibleChange: setVisible,
      }"
      :src="previewUrl"
    />
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import { fileApi } from '/@/api/support/file-api';
  import { sentry } from '/@/lib/sentry';
  import { Loading } from '/@/components/framework/loading';

  const imgFileType = ['jpg', 'jpeg', 'png', 'gif'];
  const previewUrl = ref();

  function showPreview(fileItem) {
    if (!fileItem.fileUrl) {
      (async () => {
        Loading.show();
        try {
          let res = await fileApi.getUrl(fileItem.fileKey);
          fileItem.fileUrl = res.data;
          showFile(fileItem);
        } catch (e) {
          sentry.captureError(e);
        } finally {
          Loading.hide();
        }
      })();
    } else {
      showFile(fileItem);
    }
  }

  const visible = ref(false);
  const setVisible = (value) => {
    visible.value = value;
  };

  function showFile(fileItem) {
    if (isImg(fileItem.fileType)) {
      previewUrl.value = fileItem.fileUrl;
      setVisible(true);
      return;
    }
    fileApi.downLoadFile(fileItem.fileKey);
  }

  // 判断图片类型
  function isImg(fileType) {
    return imgFileType.includes(fileType);
  }

  defineExpose({
    showPreview,
  });
</script>

<style lang="less" scoped>
  .container {
    display: flex;
    justify-content: center;
    align-items: center;
  }
</style>
