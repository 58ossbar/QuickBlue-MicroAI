<!--
  * iframe 组件
  *

-->
<template>
  <iframe :id="name" :src="url" frameborder="0" height="800" scrolling="yes" width="100%"></iframe>
</template>
<script setup>
  let props = defineProps({
    name: String,
    url: String,
  });
</script>
