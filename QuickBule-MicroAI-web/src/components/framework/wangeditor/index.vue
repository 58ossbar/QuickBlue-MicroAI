<!--
  * 编辑器
  * 解决弹窗高度警告信息显示：固定 w-e-text-container 高度为 300px
  *
  *
-->
<template>
  <div style="border: 1px solid #ccc">
    <Toolbar
      style="border-bottom: 1px solid #ccc"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
    />
    <Editor
      style="overflow-y: hidden"
      :style="{ height: `${height}px` }"
      v-model="editorHtml"
      :defaultConfig="editorConfig"
      @onCreated="handleCreated"
      @onChange="handleChange"
    />
  </div>
</template>
<script setup>
  import { shallowRef, onBeforeUnmount, watch, ref } from 'vue';
  import { FILE_FOLDER_TYPE_ENUM } from '/@/constants/support/file-const';
  import { fileApi } from '/@/api/support/file-api';
  import '@wangeditor-next/editor/dist/css/style.css';
  import { Editor, Toolbar } from '@wangeditor-next/editor-for-vue';
  import { sentry } from '/@/lib/sentry';

  // 工具栏配置
  const toolbarConfig = {
    // 排除不需要的菜单
    excludeKeys: [
      'group-video'  // 排除视频分组
    ]
  };

  // 菜单配置
  const editorConfig = { MENU_CONF: {} };

  //上传
  let customUpload = {
    async customUpload(file, insertFn) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        let res = await fileApi.uploadFile(formData, FILE_FOLDER_TYPE_ENUM.COMMON.value);
        let data = res.data;
        insertFn(data.fileUrl, '', {
          width: 'auto',
          height: 'auto'
        });
      } catch (error) {
        sentry.captureError(error);
      }
    },
  };
  editorConfig.MENU_CONF['uploadImage'] = customUpload;
  editorConfig.MENU_CONF['uploadVideo'] = customUpload;

  // 图片尺寸配置 - 推荐高清比例为原图的2-3倍
  editorConfig.MENU_CONF['editImage'] = {
    // 配置图片编辑的默认尺寸
    defaultSize: '80%', // 默认显示大小为80%
  };

  // 图片对齐配置
  editorConfig.MENU_CONF['justify'] = {
    alignJustify: true,
  };

  // 插入图片配置
  editorConfig.MENU_CONF['insertImage'] = {
    parseImageSrc: (src) => {
      return src;
    },
  };

  // ----------------------- 以下是公用变量 emits props ----------------
  const editorHtml = ref();
  let props = defineProps({
    modelValue: String,
    height: {
      type: Number,
      default: 500,
    },
  });
  watch(
    () => props.modelValue,
    (nVal) => {
      editorHtml.value = nVal;
    },
    {
      immediate: true,
      deep: true,
    }
  );

  // 获取编辑器实例html
  const emit = defineEmits(['update:modelValue']);
  const editorRef = shallowRef();
  const handleCreated = (editor) => {
    editorRef.value = editor;
  };
  const handleChange = (editor) => {
    emit('update:modelValue', editorHtml.value);
  };

  function getHtml() {
    const htmlContent = editorRef.value.getHtml();
    return htmlContent === '<p><br></p>' ? '' : htmlContent;
  }
  function getText() {
    return editorRef.value.getText();
  }

  // 组件销毁时，也及时销毁编辑器
  onBeforeUnmount(() => {
    const editor = editorRef.value;
    if (editor == null) return;
    editor.destroy();
  });

  defineExpose({
    editorRef,
    getHtml,
    getText,
  });
</script>

<style scoped>
</style>
<style>
  ::v-deep .w-e-text-container {
    height: 300px !important;
  }
  .w-e-text-container .w-e-scroll {
    height: 300px !important;
    -webkit-overflow-scrolling: touch;
  }

  /* 编辑器全屏模式样式 */
  .w-e-full-screen-container {
    z-index: 99999 !important;
    top: 0 !important;
    left: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
    width: 100vw !important;
    height: 100vh !important;
    position: fixed !important;
  }

  .w-e-full-screen-container .w-e-toolbar {
    height: auto !important;
  }

  .w-e-full-screen-container .w-e-text-container {
    height: calc(100vh - 50px) !important;
    max-height: calc(100vh - 50px) !important;
    overflow-y: auto !important;
  }

  .w-e-full-screen-container .w-e-text-container .w-e-scroll {
    height: auto !important;
    max-height: calc(100vh - 50px) !important;
    overflow-y: auto !important;
    overflow-x: hidden !important;
    -webkit-overflow-scrolling: touch;
  }
</style>
<style>
  ::v-deep .w-e-text-container {
    height: 300px !important;
  }
  .w-e-text-container .w-e-scroll {
    height: 300px !important;
    -webkit-overflow-scrolling: touch;
  }

  /* 编辑器内图片样式优化 */
  .w-e-text-container img {
    max-width: 100%;
    height: auto;
    display: inline-block;
    object-fit: contain;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    border: 3px solid #fff;
    border-radius: 4px;
    margin: 10px auto;
  }

  /* 编辑器内文字清晰度优化 */
  .w-e-text-container {
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    font-smooth: always;
    text-rendering: optimizeLegibility;
    letter-spacing: 0.3px;
  }

  .w-e-text-container p,
  .w-e-text-container span,
  .w-e-text-container div {
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
    letter-spacing: 0.3px;
  }

  .w-e-text-container img:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  }

  /* 图片居左 */
  .w-e-text-container img.text-left {
    float: left !important;
    display: inline-block !important;
    margin: 10px 20px 10px 0 !important;
  }

  /* 图片居右 */
  .w-e-text-container img.text-right {
    float: right !important;
    display: inline-block !important;
    margin: 10px 0 10px 20px !important;
  }

  /* 图片居中 */
  .w-e-text-container img.text-center {
    display: block !important;
    margin: 10px auto !important;
  }
</style>
