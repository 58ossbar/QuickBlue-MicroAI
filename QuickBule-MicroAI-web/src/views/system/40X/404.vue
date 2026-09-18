<!--
  * 404 不存在 页面
  *

-->
<template>
  <a-result status="404" title="对不起，您访问的内容不存在！">
    <template #extra>
      <a-button type="primary" @click="goHome">返回首页</a-button>
    </template>
  </a-result>
</template>
<script setup>
  import { useRouter } from 'vue-router';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';

  const router = useRouter();
  function goHome() {
    router.push({ name: HOME_PAGE_NAME });
  }
</script>
