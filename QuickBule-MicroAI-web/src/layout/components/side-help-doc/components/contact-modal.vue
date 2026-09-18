<!--
  *  客服人员弹窗
  *
  *  @Author:    budaos
-->
<template>
  <a-modal :open="visible" width="480px" title="联系客服人员" :closable="false" :maskClosable="true">
    <div class="contact-info">
      <div class="contact-item">
        <span class="label">客服电话：</span>
        <span class="value">18637925892</span>
      </div>
      <div class="contact-item">
        <span class="label">微信号：</span>
        <span class="value">budaos</span>
      </div>
      <div class="contact-item">
        <span class="label">工作时间：</span>
        <span class="value">周一至周五 9:00 - 18:00</span>
      </div>
    </div>
    <template #footer>
      <a-button type="primary" @click="hide">知道了</a-button>
    </template>
  </a-modal>
</template>
<script setup>
  import { ref } from 'vue';

  defineExpose({
    show,
  });

  const visible = ref(false);
  function show() {
    visible.value = true;
  }
  function hide() {
    visible.value = false;
  }
</script>
<style lang="less" scoped>
  .contact-info {
    padding: 8px 24px;
    .contact-item {
      display: flex;
      align-items: center;
      padding: 8px 0;
      font-size: 14px;
      .label {
        color: rgba(0, 0, 0, 0.45);
        width: 80px;
      }
      .value {
        font-weight: 600;
      }
    }
  }
</style>
