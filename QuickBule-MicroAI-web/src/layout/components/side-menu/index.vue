<!--
  * 侧边菜单
  *
  * @Author:    budaos
  * @Date:      2022-09-06 20:29:12
-->
<template>
  <div class="side-menu-wrapper" :class="{ 'wrapper-modern': sideMenuTheme === 'modern' }">
    <!-- 1、顶部logo区域 -->
    <template v-if="showLogo">
      <div class="logo" :style="sideMenuWidth" v-if="!collapsed" @click="onGoHome">
        <div class="logo-inner">
          <div class="logo-icon">
            <img class="logo-img" :src="currentLogo" :style="{ filter: logoFilter }" />
          </div>
          <div class="logo-body">
            <span class="logo-name" :class="sideMenuTheme === 'dark' ? 'text-white' : 'text-dark'">{{ websiteName }}</span>
            <span class="logo-sub" :class="sideMenuTheme === 'dark' ? 'text-white-secondary' : 'text-gray'">Cloud Native Platform</span>
          </div>
        </div>
      </div>
      <div class="min-logo" v-if="collapsed" @click="onGoHome">
        <img class="min-logo-img" :src="currentLogo" :style="{ filter: logoFilter }" />
      </div>
    </template>

    <!-- 2、下方菜单区域 -->
    <div class="menu" :class="{ 'menu-dark': sideMenuTheme === 'dark', 'menu-light': sideMenuTheme === 'light', 'menu-modern': sideMenuTheme === 'modern' }">
      <RecursionMenu :collapsed="collapsed" ref="menuRef" />
    </div>

    <!-- 3、底部帮助文档 -->
    <div class="menu-footer" :class="{ 'footer-dark': sideMenuTheme === 'dark', 'footer-light': sideMenuTheme === 'light', 'footer-modern': sideMenuTheme === 'modern' }" @click="goHelpDoc">
      <QuestionCircleOutlined class="footer-icon" />
      <span v-if="!collapsed" class="footer-text">帮助文档</span>
    </div>
  </div>
</template>

<script setup>
  import { computed, nextTick, ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import RecursionMenu from './recursion-menu.vue';
  import logoImg from '/@/assets/images/logo/budaos-logo.png';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import { QuestionCircleOutlined } from '@ant-design/icons-vue';


  const websiteName = computed(() => useAppConfigStore().websiteName);
  const sideMenuWidth = computed(() => 'width:' + useAppConfigStore().sideMenuWidth + 'px');
  const sideMenuTheme = computed(() => useAppConfigStore().sideMenuTheme);

  const currentLogo = computed(() => logoImg);

  const logoFilter = computed(() => {
    if (sideMenuTheme.value === 'dark') {
      return 'drop-shadow(0 0 4px rgba(255,255,255,0.35))';
    }
    return 'none';
  });

  const props = defineProps({
    collapsed: {
      type: Boolean,
      default: false,
    },
    showLogo: {
      type: Boolean,
      default: true,
    },
  });

  const menuRef = ref();

  watch(
    () => props.collapsed,
    (newValue) => {
      if (!newValue) {
        nextTick(() => menuRef.value.updateOpenKeysAndSelectKeys());
      }
    }
  );

  const router = useRouter();
  function onGoHome() {
    router.push({ name: HOME_PAGE_NAME });
  }

  function goHelpDoc() {
    // 新标签页打开帮助文档
    const routeData = router.resolve({ path: '/help-doc' });
    window.open(routeData.href, '_blank');
  }
</script>

<style lang="less" scoped>
.side-menu-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: relative;
  z-index: 20;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.04);

  /* 现代主题（高端）：卡片悬浮风格，深灰底衬托白色卡片 */
  &.wrapper-modern {
    background: #edeff4;
  }
}

/* ====== Logo 区域（展开） ====== */
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  cursor: pointer;
  flex-shrink: 0;
  position: relative;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    .logo-icon {
      transform: scale(1.05);
    }
  }

  .logo-inner {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    overflow: hidden;
  }

  .logo-icon {
    width: 36px;
    height: 36px;
    border-radius: 9px;
    background: linear-gradient(135deg, #f0f5ff, #e6f7ff);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.08);
  }

  .logo-img {
    width: 24px;
    height: auto;
    max-height: 24px;
    object-fit: contain;
  }

  .logo-body {
    display: flex;
    flex-direction: column;
    overflow: hidden;
    flex: 1;
    min-width: 0;
  }

  .logo-name {
    font-size: 14px;
    font-weight: 700;
    letter-spacing: 0.3px;
    line-height: 1.3;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .logo-sub {
    font-size: 9px;
    line-height: 1.4;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    letter-spacing: 0.3px;
    text-transform: uppercase;
    margin-top: 1px;
  }

  .text-dark { color: #1a1a2e; }
  .text-white { color: #ffffff; }
  .text-white-secondary { color: rgba(255, 255, 255, 0.5); }
  .text-gray { color: #8c8c8c; }
}

/* ====== 底部帮助文档 ====== */
.menu-footer {
  height: 48px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  cursor: pointer;
  flex-shrink: 0;
  font-size: 14px;
  transition: all 0.25s;
  border-top: 1px solid transparent;
  gap: 10px;

  .footer-icon {
    font-size: 16px;
  }

  .footer-text {
    white-space: nowrap;
    overflow: hidden;
  }

  /* 浅色主题 */
  &.footer-light {
    color: #595959;
    border-top-color: #f0f0f0;
    background: #ffffff;

    &:hover {
      color: #1890ff;
      background: #e6f7ff;
    }
  }

  /* 深色主题 */
  &.footer-dark {
    color: rgba(255, 255, 255, 0.65);
    border-top-color: rgba(255, 255, 255, 0.1);
    background: #001529;

    &:hover {
      color: #ffffff;
      background: rgba(255, 255, 255, 0.05);
    }
  }

  /* 现代主题 */
  &.footer-modern {
    color: #595959;
    border-top-color: rgba(15, 23, 42, 0.06);
    background: #edeff4;

    &:hover {
      color: #4361ee;
      background: rgba(67, 97, 238, 0.06);
    }
  }
}

/* ====== 小 Logo（折叠状态） ====== */
.min-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba(0, 0, 0, 0.03);
  }

  .min-logo-img {
    width: 30px;
    height: auto;
    max-height: 30px;
    object-fit: contain;
  }
}

/* ====== 菜单区域 ====== */
.menu {
  flex: 1;
  overflow: hidden;
  padding: 0;

  /* 浅色主题 */
  &.menu-light {
    background: #ffffff;
  }

  /* 深色主题 */
  &.menu-dark {
    background: linear-gradient(180deg, #001529 0%, #000c17 100%);
  }

  /* 现代主题（高端）：灰底上悬浮白色卡片，与浅色纯白底形成明显差异 */
  &.menu-modern {
    background: #edeff4;
    border-right: 1px solid rgba(15, 23, 42, 0.06);
  }
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}
</style>
