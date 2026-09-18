<!--
  * 第一列菜单
  *
  * @Author:
  * @Date:      2022-09-06 20:29:12
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <div class="top-menu-container">
    <!-- 顶部logo区域 -->
    <div class="logo" @click="onGoHome">
      <img class="logo-img" :src="logoImg" />
      <div class="title qb-logo">{{ websiteName }}</div>
    </div>
    <!-- 一级菜单展示 -->
    <a-menu :selectedKeys="selectedKeys" mode="inline" :theme="theme">
      <template v-for="item in menuTree" :key="item.menuId.toString()">
        <template v-if="item.visibleFlag">
          <a-menu-item :key="item.menuId.toString()" @click="onSelectMenu(item)">
            <template #icon>
              <component :is="$antIcons[item.icon]" />
            </template>
            <span class="menu-text">{{ menuNameAdapter(item.menuName) }}</span>
          </a-menu-item>
        </template>
      </template>
    </a-menu>
  </div>
</template>
<script setup>
  import _ from 'lodash';
  import { computed, ref } from 'vue';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
  import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';
  import { router } from '/@/router';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import { useUserStore } from '/@/store/modules/system/user';
  import logoImg from '/@/assets/images/logo/budaos-logo.png';
  import menuEmitter from './side-expand-menu-mitt';

  const websiteName = computed(() => useAppConfigStore().websiteName);
  const theme = computed(() => useAppConfigStore().$state.sideMenuTheme);
  const menuTree = computed(() => useUserStore().getMenuTree || []);

  // 展开菜单的顶级目录名字适配，只展示两个字为好
  function menuNameAdapter(name) {
    return name.substr(0, 2);
  }

  // 选中的顶级菜单
  const selectedKeys = ref([]);

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

  //点击logo回到首页
  function onGoHome() {
    router.push({ name: HOME_PAGE_NAME });
  }

  defineExpose({ updateSelectKey });
</script>
<style scoped lang="less">
  .top-menu-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .logo {
    height: var(--header-height, 40px);
    line-height: var(--header-height, 40px);
    padding: 0 15px;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    flex-shrink: 0;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      background: rgba(0, 0, 0, 0.02);
    }

    .logo-img {
      width: 30px;
      height: 30px;
    }

    .title {
      display: none;
    }
  }

  :deep(.ant-menu) {
    flex: 1;
    border-right: none;
    background: transparent;
    padding: 8px 0;

    .ant-menu-item {
      height: 50px;
      margin: 2px 8px !important;
      border-radius: 6px;
      padding: 0 !important;
      font-size: 12px;
      font-weight: 400;
      transition: background-color 0.15s ease, color 0.15s ease;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: #515a6e;

      .anticon {
        font-size: 16px;
        line-height: 1;
        margin: 0;
        transition: color 0.15s ease;
      }

      .menu-text {
        line-height: 1;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      &:hover {
        color: #1890ff;
        background-color: rgba(24, 144, 255, 0.06);

        .anticon {
          color: #1890ff;
        }
      }

      &.ant-menu-item-selected {
        color: #1890ff;
        font-weight: 500;
        background-color: rgba(24, 144, 255, 0.10);

        &::before {
          content: '';
          position: absolute;
          left: -8px;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 16px;
          background-color: #1890ff;
          border-radius: 0 2px 2px 0;
        }

        &::after {
          display: none;
        }

        .anticon {
          color: #1890ff;
        }
      }
    }

    // 标题容器垂直居中（关键修复：图标 + 文字整体在 li 内居中）
    :deep(.ant-menu-title-content) {
      display: flex !important;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 3px;
      height: 100%;
      line-height: 1;
      text-align: center;
    }

    &.ant-menu-dark {
      background: transparent;

      .ant-menu-item {
        color: rgba(255, 255, 255, 0.65);

        &:hover {
          color: #ffffff;
          background-color: rgba(255, 255, 255, 0.06);

          .anticon {
            color: #ffffff;
          }
        }

        &.ant-menu-item-selected {
          color: #ffffff;
          background-color: rgba(24, 144, 255, 0.20);

          &::before {
            background-color: #1890ff;
          }

          .anticon {
            color: #ffffff;
          }
        }
      }
    }
  }
</style>
