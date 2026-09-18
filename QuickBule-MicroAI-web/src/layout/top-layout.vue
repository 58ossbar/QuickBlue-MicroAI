<template>
  <a-layout class="admin-layout">
    <!-- 顶部菜单  -->
    <a-layout-header
      class="top-menu"
      :class="{ 'modern-header': isModernTheme }"
      :theme="theme"
      :id="LAYOUT_ELEMENT_IDS.menu"
      v-if="!fullScreenFlag"
    >
      <TopMenu />
    </a-layout-header>

    <!--中间内容-->
    <a-layout-content :id="LAYOUT_ELEMENT_IDS.content" class="admin-layout-content">
      <!---标签页-->
      <div class="page-tag-div" v-show="(pageTagFlag && !fullScreenFlag) || breadCrumbFlag" :id="LAYOUT_ELEMENT_IDS.header">
        <MenuLocationBreadcrumb v-if="pageTagLocation !== 'top'" />
        <PageTag />
      </div>

      <!--不keepAlive的iframe使用单个iframe组件-->
      <IframeIndex v-if="iframeNotKeepAlivePageFlag" :key="route.name" :name="route.name" :url="route.meta.frameUrl" />

      <!--keepAlive的iframe 每个页面一个iframe组件-->
      <IframeIndex
        v-for="item in keepAliveIframePages"
        v-show="route.name === item.name"
        :key="item.name"
        :name="item.name"
        :url="item.meta.frameUrl"
      />

      <!--非iframe使用router-view-->
      <div v-show="!iframeNotKeepAlivePageFlag && keepAliveIframePages.every((e) => route.name !== e.name)" :style="{height: contentBoxHeight+'px'}" class="admin-content">
        <router-view v-slot="{ Component }">
          <keep-alive :include="keepAliveIncludes">
            <component :is="Component" :key="route.name" />
          </keep-alive>
        </router-view>
      </div>
    </a-layout-content>

    <!-- footer 版权公司信息 -->
    <a-layout-footer class="layout-footer" v-show="footerFlag">
      <footer />
    </a-layout-footer>
    <!--- 回到顶部 -->
    <a-back-top :target="backTopTarget" :visibilityHeight="80" />
  </a-layout>
</template>

<script setup>
  import { computed, onMounted, ref, watch } from 'vue';
  import { useAppConfigStore } from '../store/modules/system/app-config';
  import PageTag from './components/page-tag/index.vue';
  import TopMenu from './components/top-menu/index.vue';
  import Footer from './components/footer/index.vue';
  import { keepAlive } from './components/keep-alive';
  import IframeIndex from '/@/components/framework/iframe/iframe-index.vue';
  import watermark from '../lib/watermark';
  import { useUserStore } from '/@/store/modules/system/user';
  import { useRouter } from 'vue-router';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
  import { LAYOUT_ELEMENT_IDS } from '/@/layout/layout-const.js';
  import MenuLocationBreadcrumb from './components/menu-location-breadcrumb/index.vue';

  const windowHeight = ref(window.innerHeight);
  //主题颜色
  const theme = computed(() => useAppConfigStore().$state.sideMenuTheme);
  // 浅色系主题：light + modern
  const isLightTheme = computed(() => {
    const t = useAppConfigStore().$state.sideMenuTheme;
    return t === 'light' || t === 'modern';
  });
  const isModernTheme = computed(() => useAppConfigStore().$state.sideMenuTheme === 'modern');
  const color = computed(() => {
    const t = useAppConfigStore().$state.sideMenuTheme;
    const background = t === 'modern' ? '#edeff4' : isLightTheme.value ? '#FFFFFF' : '#001529';
    return {
      color: isLightTheme.value ? '#001529' : '#FFFFFF',
      background,
    };
  });

  //是否全屏
  const fullScreenFlag = computed(() => useAppConfigStore().$state.fullScreenFlag);
  //是否显示标签页
  const pageTagFlag = computed(() => useAppConfigStore().$state.pageTagFlag);
  // 是否显示页脚
  const footerFlag = computed(() => useAppConfigStore().$state.footerFlag);
  // 是否显示水印
  const watermarkFlag = computed(() => useAppConfigStore().$state.watermarkFlag);
  // 水印文字
  const watermarkText = computed(() => useAppConfigStore().$state.watermarkText);
  // 标签页位置
  const pageTagLocation = computed(() => useAppConfigStore().$state.pageTagLocation);
  // 面包屑
  const breadCrumbFlag = computed(() => useAppConfigStore().$state.breadCrumbFlag);
  // 页面宽度
  const pageWidth = computed(() => useAppConfigStore().$state.pageWidth);

  let contentBoxHeight=ref()
  // 多余高度
  const dueHeight = computed(() => {
    if (fullScreenFlag.value) {
      return '0';
    }

    let due = '45px';
    if (useAppConfigStore().$state.pageTagFlag || useAppConfigStore().$state.breadCrumbFlag) {
      due = '85px';
    }
    if (
      useAppConfigStore().$state.pageTagFlag &&
      useAppConfigStore().$state.pageTagLocation === 'center' &&
      useAppConfigStore().$state.breadCrumbFlag
    ) {
      due = '125px';
    }
    return due;
  });

  watch(() => dueHeight.value, () => {
    let dom=document.querySelector('.admin-layout-content')
    contentBoxHeight.value=dom.offsetHeight - 20 - dueHeight.value.split('px')[0]
  });
  onMounted(() => {
    let dom=document.querySelector('.admin-layout-content')
    contentBoxHeight.value=dom.offsetHeight - 20 - dueHeight.value.split('px')[0]
  });
  //页面初始化的时候加载水印
  onMounted(() => {
    if (watermarkFlag.value) {
      watermark.set(LAYOUT_ELEMENT_IDS.content, {
        text: watermarkText.value,
        showDate: false
      });
    } else {
      watermark.clear();
    }
  });

  watch(
    () => watermarkFlag.value,
    (newValue) => {
      if (newValue) {
        watermark.set(LAYOUT_ELEMENT_IDS.content, {
          text: watermarkText.value,
          showDate: false
        });
      } else {
        watermark.clear();
      }
    }
  );

  watch(
    () => watermarkText.value,
    (newValue) => {
      if (watermarkFlag.value) {
        watermark.set(LAYOUT_ELEMENT_IDS.content, {
          text: newValue,
          showDate: false
        });
      }
    }
  );

  //回到顶部
  const backTopTarget = () => {
    return document.getElementById(LAYOUT_ELEMENT_IDS.main);
  };

  const router = useRouter();
  function goHome() {
    router.push({ name: HOME_PAGE_NAME });
  }

  window.addEventListener('resize', function () {
    windowHeight.value = window.innerHeight;
  });

  // ----------------------- keep-alive相关 -----------------------
  let { route, keepAliveIncludes, iframeNotKeepAlivePageFlag, keepAliveIframePages } = keepAlive();
</script>

<style lang="less" scoped>
  .admin-layout {
    min-height: 100%;

    .top-menu {
      padding: 0px;
      height: var(--top-menu-height, 48px);
      line-height: var(--top-menu-height, 48px);
      width: 100%;
      z-index: 3;
      right: 0;
      position: fixed;
      background-color: v-bind('color.background');
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      /* 高端主题：玻璃拟态 header */
      &.modern-header {
        background: rgba(237, 239, 244, 0.68);
        backdrop-filter: saturate(180%) blur(24px);
        -webkit-backdrop-filter: saturate(180%) blur(24px);
        box-shadow:
          0 1px 2px rgba(15, 23, 42, 0.04),
          0 4px 16px rgba(15, 23, 42, 0.06),
          inset 0 -1px 0 rgba(99, 102, 241, 0.10);

        /* 底部靛蓝渐变修饰条 */
        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          height: 2px;
          background: linear-gradient(
            90deg,
            rgba(99, 102, 241, 0.08) 0%,
            rgba(99, 102, 241, 0.24) 50%,
            rgba(99, 102, 241, 0.08) 100%
          );
          pointer-events: none;
        }
      }
    }

    .admin-layout-content {
      background-color: inherit;
      min-height: auto;
      position: relative;
      overflow-x: hidden;
      padding: 10px 0;
      width: v-bind(pageWidth);
      margin-top: v-bind(dueHeight);
      margin-left: auto;
      margin-right: auto;

      .page-tag-div {
        position: fixed;
        top: var(--top-menu-height, 48px);
        width: v-bind(pageWidth);
        height: 40px;
        line-height: 40px;
        z-index: 3;
      }
    }
  }

  .layout-footer {
    position: relative;
    padding: 7px 0px;
    display: flex;
    justify-content: center;
  }
</style>
