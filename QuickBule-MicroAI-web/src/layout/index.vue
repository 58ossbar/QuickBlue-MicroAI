<!--
  *  layout 多种模式
  *
  * @Author:
  * @Date:      2022-09-06 20:40:16
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <!--垂直：左侧窄菜单-->
  <SideLayout v-if="layout === LAYOUT_ENUM.SIDE.value" />
  <!--双列菜单：左侧宽菜单-->
  <SideExpandLayout v-if="layout === LAYOUT_ENUM.SIDE_EXPAND.value" />
  <!--水平：顶部菜单-->
  <TopLayout v-if="layout === LAYOUT_ENUM.TOP.value" />
  <!--侧边导航：极窄图标菜单-->
  <SideIconLayout v-if="layout === LAYOUT_ENUM.SIDE_ICON.value" />
  <!--混合垂直：顶部菜单 + 左侧窄菜单-->
  <TopSideLayout v-if="layout === LAYOUT_ENUM.TOP_SIDE.value" />
  <!--混合双列：顶部菜单 + 左侧宽菜单-->
  <TopExpandLayout v-if="layout === LAYOUT_ENUM.TOP_EXPAND.value" />
  <!--定期修改密码-->
  <RegularChangePasswordModal />
</template>
<script setup>
  import { computed } from 'vue';
  import { LAYOUT_ENUM } from '/@/constants/layout-const';
  import SideLayout from './side-layout.vue';
  import SideExpandLayout from './side-expand-layout.vue';
  import TopLayout from './top-layout.vue';
  import SideIconLayout from './side-icon-layout.vue';
  import TopSideLayout from './top-side-layout.vue';
  import TopExpandLayout from './top-expand-layout.vue';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import RegularChangePasswordModal from './components/change-password/regular-change-password-modal.vue';

  const layout = computed(() => useAppConfigStore().$state.layout);
</script>
