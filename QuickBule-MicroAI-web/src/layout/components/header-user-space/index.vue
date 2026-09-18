<!--
  * 头部一整行
  *
  * @Author:
  * @Date:      2022-09-06 20:18:20
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <a-space :size="10">
    <div class="setting">
      <!---消息通知--->
      <HeaderMessage ref="headerMessage" />
      <!---国际化--->
      <!-- <a-button type="text" @click="showSetting" class="operate-icon">
        <template #icon><switcher-outlined /></template>
        i18n
      </a-button> -->
      <!---设置--->
      <a-button type="text" @click="showSetting" class="operate-icon">
        <template #icon><setting-outlined /></template>
      </a-button>
    </div>
    <!---头像信息--->
    <div class="user-space-item">
      <HeaderAvatar />
    </div>

    <HeaderSetting ref="headerSetting" />
  </a-space>
</template>

<script setup>
  import HeaderAvatar from './header-avatar.vue';
  import HeaderSetting from './header-setting.vue';
  import HeaderMessage from './header-message.vue';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import { computed, ref } from 'vue';
  import { theme } from 'ant-design-vue';

  // 设置
  const headerSetting = ref();
  function showSetting() {
    headerSetting.value.show();
  }

  const { useToken } = theme;
  const { token } = useToken();
</script>

<style lang="less" scoped>
  .user-space-item {
    height: 100%;
    color: inherit;
    padding: 0 12px;
    cursor: pointer;
    align-self: center;

    a {
      color: inherit;

      i {
        font-size: 16px;
      }
    }
  }

  .user-space-item:hover {
    color: v-bind('token.colorPrimary');
    background-color: @hover-bg-color !important;
  }

  .setting {
    height: var(--header-height, 40px);
    line-height: var(--header-height, 40px);
    vertical-align: middle;
    display: flex;
    align-items: center;
  }
  .operate-icon {
    margin-left: 20px;
  }
</style>
