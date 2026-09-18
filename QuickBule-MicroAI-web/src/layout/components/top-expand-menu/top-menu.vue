<!--
  * 第一列菜单
  *
  * 包含 2 部分：
  *   1、logo 区域（左侧，宽度 = sidebar 宽度，208 / 80 (折叠)）
  *   2、横向菜单区域（占满 logo 右侧空间）
  * 配合父组件 index.vue 的 :deep(.top-menu) absolute + width: 100vw，蓝色 head 跨越整个 viewport 顶部
  *
  * @Author:
  * @Date:      2022-09-06 20:29:12
  * @Copyright
-->
<template>
  <div class="top-menu-container">
    <!-- 1、左侧 logo 区域（展开状态） -->
    <div class="logo" :style="sideMenuStyle" @click="onGoHome" v-if="!collapsed">
      <img class="logo-img" :src="logoImg" />
      <div class="title qb-logo title-light" v-if="isLight">{{ websiteName }}</div>
      <div class="title qb-logo title-dark" v-if="!isLight">{{ websiteName }}</div>
    </div>
    <!-- 折叠状态：仅显示小 logo 图标 -->
    <div class="min-logo" v-if="collapsed" @click="onGoHome">
      <img class="min-logo-img" :src="logoImg" />
    </div>

    <!-- 2、横向菜单区域（一级菜单展示） -->
    <a-menu class="top-menu-nav" :selectedKeys="selectedKeys" mode="horizontal" :theme="theme">
      <template v-for="item in menuTree" :key="item.menuId.toString()">
        <template v-if="item.visibleFlag">
          <a-menu-item :key="item.menuId.toString()" @click="onSelectMenu(item)">
            <template #icon>
              <component :is="$antIcons[item.icon]" />
            </template>
            {{ item.menuName }}
          </a-menu-item>
        </template>
      </template>
    </a-menu>
  </div>
</template>

<script setup>
  import _ from 'lodash';
  import { computed, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';
  import { router } from '/@/router';
  import logoImg from '/@/assets/images/logo/budaos-logo.png';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import { useUserStore } from '/@/store/modules/system/user';
  import { themeColors } from '/@/theme/color.js';
  import menuEmitter from './top-expand-menu-mitt';

  // 网站名 / 主题
  const websiteName = computed(() => useAppConfigStore().websiteName);
  const theme = computed(() => useAppConfigStore().$state.sideMenuTheme);
  const isLight = computed(() => theme.value === 'light');
  const sideMenuWidth = computed(() => useAppConfigStore().sideMenuWidth);
  // 当前 sidebar 宽度（用于 logo 区域占位：展开 208 / 折叠 80）
  const sideMenuStyle = computed(() => 'width:' + sideMenuWidth.value + 'px');

  const menuTree = computed(() => useUserStore().getMenuTree || []);
  // 主题色（跟随 setting 中选择的主题色板）
  const colorIndex = computed(() => useAppConfigStore().colorIndex);
  const primaryColor = computed(() => themeColors[colorIndex.value].primaryColor);
  const activeColor = computed(() => themeColors[colorIndex.value].activeColor);
  const hoverColor = computed(() => themeColors[colorIndex.value].hoverColor);

  // 页面紧凑模式：动态调整 menu header 高度（紧凑 32px，正常 40px）
  const compactFlag = computed(() => useAppConfigStore().compactFlag);
  const menuHeaderHeight = computed(() => compactFlag.value ? '32px' : '40px');

  const props = defineProps({
    collapsed: {
      type: Boolean,
      default: false,
    },
  });

  // 选中的顶级菜单
  const selectedKeys = ref([]);

  // 点击 logo 跳转首页
  const route = useRoute();
  function onGoHome() {
    if (route.name !== HOME_PAGE_NAME) {
      router.push({ name: HOME_PAGE_NAME });
    }
  }

  // 展开菜单的顶级目录名字适配，只展示两个字为好
  function menuNameAdapter(name) {
    return name.substr(0, 2);
  }

  // 选中菜单，页面跳转
  function onSelectMenu(menuItem) {
    selectedKeys.value = [menuItem.menuId.toString()];
    if (menuItem.menuType === MENU_TYPE_ENUM.MENU.value && (_.isEmpty(menuItem.children) || menuItem.children.every((e) => !e.visibleFlag))) {
      useUserStore().deleteKeepAliveIncludes(menuItem.menuId.toString());
      router.push({ name: menuItem.menuId.toString() });
    }
    menuEmitter.emit('selectTopMenu', menuItem);
  }

  // 更新选中的菜单
  function updateSelectKey(key) {
    selectedKeys.value = [key];
    let selectMenu = _.find(menuTree.value, { menuId: Number(key) });
    if (selectMenu) {
      menuEmitter.emit('selectTopMenu', selectMenu);
    }
  }

  defineExpose({ updateSelectKey });
</script>

<style scoped lang="less">
  /* 关键：a-menu 自身背景透明，让父组件 :deep(.top-menu) 的 primaryColor 透出来——这样蓝色 head 才横跨整个 viewport 顶部
   * (而不是只 208px sidebar 宽度范围) */
  .ant-menu-dark,
  .ant-menu-light {
    background-color: transparent;
    color: #ffffff;
  }

  // 选中态：使用主题色板中的 activeColor（深色变体，替换原硬编码 #0958d9）
  :deep(.ant-menu-item-selected) {
    background-color: v-bind('activeColor') !important;
    color: #ffffff !important;
  }

  // hover 态：使用主题色板中的 hoverColor（亮色变体）
  :deep(.ant-menu-item:hover) {
    background-color: v-bind('hoverColor') !important;
    color: #ffffff !important;
  }

  /* 让 a-menu 横向菜单项正确居中在蓝色 head 内 */
  :deep(.ant-menu-horizontal) {
    border-bottom: 0;
    line-height: v-bind(menuHeaderHeight);
  }
  :deep(.ant-menu-horizontal > .ant-menu-item) {
    height: v-bind(menuHeaderHeight);
    line-height: v-bind(menuHeaderHeight);
    top: 0;
    margin-top: 0;
  }

  /* 容器：flex 横向排列 logo / min-logo + a-menu */
  .top-menu-container {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: row;
    align-items: center;
  }

  /* 横向菜单占满 logo 剩余空间 */
  .top-menu-nav {
    flex: 1;
    min-width: 0;
    background-color: transparent !important;
  }

  /* 展开状态：logo + 系统名称（占 sidebar 宽度） */
  .logo {
    height: v-bind(menuHeaderHeight);
    line-height: v-bind(menuHeaderHeight);
    padding: 0px 15px 0px 15px;
    flex-shrink: 0;
    z-index: 100;
    display: flex;
    flex-direction: row;
    align-items: center;
    cursor: pointer;
    box-sizing: border-box;

    .logo-img {
      width: 30px;
      height: 30px;
      display: inline-block;
      vertical-align: middle;
    }

    .title {
      font-size: 16px;
      font-weight: 600;
      margin-left: 8px;
      overflow: hidden;
      word-wrap: break-word;
      white-space: nowrap;
    }
    .title-light {
      color: #001529;
    }
    .title-dark {
      color: #ffffff;
    }
  }

  /* 折叠状态：仅显示小 logo 图标（80px） */
  .min-logo {
    height: v-bind(menuHeaderHeight);
    line-height: v-bind(menuHeaderHeight);
    width: 80px;
    flex-shrink: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    .min-logo-img {
      width: 30px;
      height: 30px;
    }
  }
</style>
