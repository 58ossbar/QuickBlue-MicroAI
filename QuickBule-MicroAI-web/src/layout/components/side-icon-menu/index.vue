<!--
  * 侧边图标菜单（极窄，仅显示图标）
  *
  * @Author:    budaos
  * @Date:      2024-08-27
-->
<template>
  <div class="side-icon-menu-wrapper" :class="{ 'wrapper-modern': sideMenuTheme === 'modern' }">
    <!-- 顶部logo -->
    <div class="logo" @click="onGoHome">
      <img class="logo-img" :src="currentLogo" :style="{ filter: logoFilter }" />
    </div>

    <!-- 图标菜单 -->
    <div class="icon-menu" :class="{ 'menu-dark': sideMenuTheme === 'dark', 'menu-light': sideMenuTheme === 'light', 'menu-modern': sideMenuTheme === 'modern' }">
      <template v-for="item in filteredMenuTree" :key="item.menuId">
        <!-- 有子菜单：点击弹出子菜单面板 -->
        <a-popover
          v-if="hasChildren(item)"
          :open="currentOpen === item.menuId"
          @open-change="(open) => onOpenChange(item.menuId, open)"
          placement="rightTop"
          trigger="click"
          overlay-class-name="side-icon-popover"
          :overlay-style="{ padding: 0 }"
        >
          <template #content>
            <div class="popover-panel" :class="'panel-' + sideMenuTheme">
              <div class="panel-title">
                <span class="panel-icon">
                  <component :is="$antIcons[item.icon]" v-if="item.icon && $antIcons[item.icon]" />
                </span>
                {{ item.menuName }}
              </div>
              <SideIconSubMenu :menu="item" @navigate="onNavigate" />
            </div>
          </template>
          <div
            class="icon-item"
            :class="{
              active: isActive(item),
              'active-dark': sideMenuTheme === 'dark' && isActive(item),
              'active-light': sideMenuTheme === 'light' && isActive(item),
              'active-modern': sideMenuTheme === 'modern' && isActive(item),
            }"
          >
            <component :is="$antIcons[item.icon]" v-if="item.icon && $antIcons[item.icon]" class="menu-icon" />
            <AppstoreOutlined v-else class="menu-icon" />
          </div>
        </a-popover>

        <!-- 无子菜单：直接跳转 -->
        <a-tooltip v-else placement="right" :title="item.menuName">
          <div
            class="icon-item"
            :class="{
              active: isActive(item),
              'active-dark': sideMenuTheme === 'dark' && isActive(item),
              'active-light': sideMenuTheme === 'light' && isActive(item),
              'active-modern': sideMenuTheme === 'modern' && isActive(item),
            }"
            @click="onClickMenu(item)"
          >
            <component :is="$antIcons[item.icon]" v-if="item.icon && $antIcons[item.icon]" class="menu-icon" />
            <AppstoreOutlined v-else class="menu-icon" />
          </div>
        </a-tooltip>
      </template>
    </div>

    <!-- 底部设置 -->
    <div class="icon-footer" :class="{ 'footer-dark': sideMenuTheme === 'dark', 'footer-light': sideMenuTheme === 'light', 'footer-modern': sideMenuTheme === 'modern' }">
      <a-tooltip placement="right" title="设置">
        <div class="icon-item" @click="showSetting">
          <SettingOutlined class="menu-icon" />
        </div>
      </a-tooltip>
      <a-tooltip placement="right" title="帮助文档">
        <div class="icon-item" @click="goHelpDoc">
          <QuestionCircleOutlined class="menu-icon" />
        </div>
      </a-tooltip>
    </div>
  </div>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useUserStore } from '/@/store/modules/system/user';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import { AppstoreOutlined, SettingOutlined, QuestionCircleOutlined } from '@ant-design/icons-vue';
  import logoImg from '/@/assets/images/logo/budaos-logo.png';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
  import SideIconSubMenu from './side-icon-sub-menu.vue';

  const router = useRouter();
  const route = useRoute();
  const userStore = useUserStore();
  const appConfigStore = useAppConfigStore();

  const menuTree = computed(() => userStore.menuTree || []);
  // 过滤分组标签（menuType===0）及隐藏/禁用的一级菜单
  const filteredMenuTree = computed(() =>
    menuTree.value.filter((m) => m.menuType !== 0 && m.visibleFlag && !m.disabledFlag)
  );
  const sideMenuTheme = computed(() => appConfigStore.sideMenuTheme);
  const currentLogo = computed(() => logoImg);

  // 当前打开的弹出面板（一级菜单 menuId）
  const currentOpen = ref(null);

  const logoFilter = computed(() => {
    if (sideMenuTheme.value === 'dark') {
      return 'drop-shadow(0 0 4px rgba(255,255,255,0.35))';
    }
    return 'none';
  });

  // 过滤后是否存在可用的子菜单
  function hasChildren(menu) {
    return (menu.children || []).some((c) => c.menuType !== 0 && c.visibleFlag && !c.disabledFlag);
  }

  // 递归判断当前路由是否在该菜单子树下
  function isActive(menu) {
    if (!menu) return false;
    const path = menu.path || menu.menuUrl;
    if (path && (route.path === path || route.path.startsWith(path + '/'))) return true;
    return (menu.children || []).some((c) => isActive(c));
  }

  function onOpenChange(menuId, open) {
    currentOpen.value = open ? menuId : null;
  }

  function onClickMenu(menu) {
    router.push(menu.path || menu.menuUrl);
  }

  // 从弹出面板中选择菜单后跳转并关闭面板
  function onNavigate(menu) {
    userStore.deleteKeepAliveIncludes(menu.menuId.toString());
    router.push(menu.path || menu.menuUrl);
    currentOpen.value = null;
  }

  function onGoHome() {
    router.push({ name: HOME_PAGE_NAME });
  }

  function showSetting() {
    // 通过事件触发设置面板
    appConfigStore.$patch({ showSetting: true });
  }

  function goHelpDoc() {
    const routeData = router.resolve({ path: '/help-doc' });
    window.open(routeData.href, '_blank');
  }
</script>

<style lang="less" scoped>
.side-icon-menu-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100vh;
  width: 48px;
  position: relative;
  z-index: 20;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.04);

  &.wrapper-modern {
    background: #edeff4;
  }
}

.logo {
  height: 48px;
  width: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.25s;

  &:hover {
    background: rgba(0, 0, 0, 0.03);
  }

  .logo-img {
    width: 24px;
    height: auto;
    max-height: 24px;
    object-fit: contain;
  }
}

.icon-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  width: 100%;
  padding: 8px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  &.menu-light {
    background: #ffffff;
  }

  &.menu-dark {
    background: linear-gradient(180deg, #001529 0%, #000c17 100%);
  }

  &.menu-modern {
    background: #edeff4;
  }
}

.icon-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: #595959;

  &:hover {
    background: rgba(24, 144, 255, 0.1);
    color: #1890ff;
  }

  &.active-light {
    background: #e6f7ff;
    color: #1890ff;
  }

  &.active-dark {
    background: rgba(24, 144, 255, 0.2);
    color: #40a9ff;
  }

  &.active-modern {
    background: rgba(67, 97, 238, 0.1);
    color: #4361ee;
  }

  .menu-icon {
    font-size: 18px;
  }
}

/* ---------- 弹出子菜单面板 ---------- */
.popover-panel {
  min-width: 210px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 4px;

  .panel-title {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 10px 10px;
    font-size: 13px;
    font-weight: 600;
    color: #262626;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 6px;

    .panel-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 22px;
      height: 22px;
      border-radius: 6px;
      background: rgba(24, 144, 255, 0.1);
      color: #1890ff;
      font-size: 13px;
      flex-shrink: 0;
    }
  }

  &.panel-dark {
    .panel-title {
      color: rgba(255, 255, 255, 0.85);
      border-bottom-color: #303030;

      .panel-icon {
        background: rgba(24, 144, 255, 0.2);
      }
    }
  }

  &.panel-modern {
    .panel-title {
      .panel-icon {
        background: rgba(67, 97, 238, 0.1);
        color: #4361ee;
      }
    }
  }
}

.icon-footer {
  width: 100%;
  padding: 8px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  border-top: 1px solid transparent;

  &.footer-light {
    border-top-color: #f0f0f0;
    background: #ffffff;
  }

  &.footer-dark {
    border-top-color: rgba(255, 255, 255, 0.1);
    background: #001529;
  }

  &.footer-modern {
    border-top-color: rgba(15, 23, 42, 0.06);
    background: #edeff4;
  }
}
</style>

<!-- popover 壳挂载于 body，需非 scoped 全局样式 -->
<style lang="less">
  .side-icon-popover {
    .ant-popover-inner {
      padding: 6px;
      border-radius: 10px;
      box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
    }

    .ant-popover-inner-content {
      padding: 0;
    }
  }

  :global([data-theme='dark']) {
    .side-icon-popover {
      .ant-popover-inner {
        background: #1f1f1f;
      }

      .ant-popover-arrow::before,
      .ant-popover-arrow::after {
        background: #1f1f1f;
      }
    }
  }
</style>
