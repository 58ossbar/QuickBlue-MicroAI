<!--
  * 侧边图标导航布局（极窄菜单，仅显示图标）
  *
  * @Author:    budaos
  * @Date:      2024-08-27
-->
<template>
  <a-layout class="layout-container">
    <!--左侧菜单-->
    <a-layout-sider
      :trigger="null"
      collapsible
      breakpoint="lg"
      collapsedWidth="48"
      v-model:collapsed="collapsed"
      :theme="sideMenuTheme"
      :class="{ 'modern-sider': sideMenuTheme === 'modern' }"
      width="48"
    >
      <SideIconMenu />
    </a-layout-sider>

    <!--右侧页面-->
    <a-layout>
      <!-- 顶部header-->
      <a-layout-header class="layout-header" :class="{ 'modern-header': sideMenuTheme === 'modern' }">
        <HeaderUserSpace />
      </a-layout-header>
      <!-- 内容区域-->
      <a-layout-content class="layout-content" :class="{ 'modern-content': sideMenuTheme === 'modern' }">
        <div class="container">
          <router-view v-slot="{ Component }">
            <keep-alive :include="keepAliveIncludes">
              <component :is="Component" :key="route.name" v-if="route.meta && route.meta.keepAlive" />
            </keep-alive>
            <component :is="Component" :key="route.name" v-if="!(route.meta && route.meta.keepAlive)" />
          </router-view>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
  import { computed } from 'vue';
  import { useRoute } from 'vue-router';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import { useUserStore } from '/@/store/modules/system/user';
  import SideIconMenu from './components/side-icon-menu/index.vue';
  import HeaderUserSpace from './components/header-user-space/index.vue';

  const route = useRoute();
  const collapsed = computed(() => useAppConfigStore().$state.sideMenuCollapsed);
  const sideMenuTheme = computed(() => useAppConfigStore().$state.sideMenuTheme);
  const keepAliveIncludes = computed(() => useUserStore().keepAliveIncludes);
</script>

<style lang="less" scoped>
  .layout-container {
    display: flex;
    width: 100vw;
    min-height: 100vh;
  }

  .layout-header {
    background: #fff;
    padding: 0;
    height: var(--header-height, 48px);
    line-height: var(--header-height, 48px);
    z-index: 21;
    border-bottom: 1px solid #f0f0f0;
  }

  .modern-header {
    border-bottom-color: transparent;
    background: #edeff4;
  }

  .layout-content {
    overflow: hidden;
    background: #f0f2f5;
  }

  .modern-content {
    background: #edeff4;
  }

  .container {
    overflow-y: auto;
    overflow-x: hidden;
    padding: 16px;
    min-height: calc(100vh - var(--header-height, 48px));
  }

  :deep(.ant-layout-sider) {
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
    z-index: 20;
  }

  :deep(.modern-sider) {
    background: #edeff4 !important;
    box-shadow: 2px 0 8px rgba(15, 23, 42, 0.04);
  }
</style>
