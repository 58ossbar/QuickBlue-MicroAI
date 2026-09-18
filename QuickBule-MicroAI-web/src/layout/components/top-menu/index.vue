<!--
  * 顶部菜单
  *
  * @Author:    budaos
  * @Date:      2022-09-06 20:29:12
-->
<template>
  <!--总共3部分：1、logo区域，包含 logo和名称; 2、菜单区域  ;3、用户操作区域-->
  <div class="header-main" :class="{ 'modern-header-main': isModernTheme }">
    <!-- 1、logo区域 -->
    <div class="logo" :class="{ 'modern-logo': isModernTheme }" @click="onGoHome">
      <div class="logo-icon-box" :class="{ 'modern-logo-icon-box': isModernTheme }">
        <img class="logo-img" :src="logoImg" />
      </div>
      <div class="logo-divider" v-if="isModernTheme"></div>
      <div class="title qb-logo title-light" v-if="isLightTheme">{{ websiteName }}</div>
      <div class="title qb-logo title-dark" v-if="!isLightTheme">{{ websiteName }}</div>
    </div>
    <!-- 2、菜单区域 -->
    <RecursionMenu ref="menuRef" />

    <!-- 3、用户操作区域 -->
    <div class="user-space" :class="{ 'modern-user-space': isModernTheme }">
      <div class="setting">
        <!---消息通知--->
        <HeaderMessage ref="headerMessage" />
        <!---设置--->
        <a-button type="text" @click="showSetting" class="operate-icon" :class="{ 'modern-operate-icon': isModernTheme }">
          <template #icon><setting-outlined /></template>
        </a-button>
      </div>
      <!---头像信息--->
      <div class="user-space-item">
        <HeaderAvatar />
      </div>
      <HeaderSetting ref="headerSetting" />
    </div>
  </div>
</template>

<script setup>
  import { computed, ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import RecursionMenu from './recursion-menu.vue';
  import logoImg from '/@/assets/images/logo/budaos-logo.png';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import HeaderAvatar from '../header-user-space/header-avatar.vue';
  import HeaderSetting from '../header-user-space/header-setting.vue';
  import HeaderMessage from '../header-user-space/header-message.vue';

  // 设置
  const headerSetting = ref();
  function showSetting() {
    headerSetting.value.show();
  }

  //消息通知
  const headerMessage = ref();
  function showMessage() {
    headerMessage.value.showMessage();
  }

  const websiteName = computed(() => useAppConfigStore().websiteName);
  const sideMenuTheme = computed(() => useAppConfigStore().sideMenuTheme);

  const props = defineProps({
    collapsed: {
      type: Boolean,
      required: false,
      default: false,
    },
  });

  const menuRef = ref();

  watch(
    () => props.collapsed,
    (newValue, oldValue) => {
      // 如果是展开菜单的话，重新获取更新菜单的展开项: openkeys和selectKeys
      if (!newValue) {
        menuRef.value.updateSelectKeys();
      }
    }
  );

  // 浅色系主题：light + modern（modern 也是浅色底，文字需深色）
  const isLightTheme = computed(() => {
    const t = useAppConfigStore().$state.sideMenuTheme;
    return t === 'light' || t === 'modern';
  });
  const isModernTheme = computed(() => useAppConfigStore().$state.sideMenuTheme === 'modern');
  const color = computed(() => {
    // modern 主题底色是 #edeff4，其余浅色是 #fff
    const t = useAppConfigStore().$state.sideMenuTheme;
    const background = t === 'modern' ? '#edeff4' : isLightTheme.value ? '#FFFFFF' : '#001529';
    return {
      color: isLightTheme.value ? '#001529' : '#FFFFFF',
      background,
    };
  });

  const router = useRouter();
  function onGoHome() {
    router.push({ name: HOME_PAGE_NAME });
  }
</script>

<style lang="less" scoped>
  .header-main {
    display: flex;
    flex-direction: row;
    justify-content: flex-start;
    width: 100%;
    padding-left: 16px;
    height: var(--top-menu-height, 48px);
    z-index: 21;
    background: v-bind('color.background');
    border-bottom: 1px solid rgb(238, 238, 238);

    /* 高端主题：去除默认边框，整体更精致 */
    &.modern-header-main {
      border-bottom-color: transparent;
      padding-left: 12px;

      /* 菜单区域加一点左边距，与 logo 区域拉开 */
      :deep(.ant-menu) {
        margin-left: 6px;
      }
    }

    .logo {
      min-width: 192px;
      display: flex;
      flex-direction: row;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      transition: opacity 0.2s ease;

      &:hover {
        opacity: 0.88;
      }

      .logo-img {
        display: inline-block;
        height: 30px;
        vertical-align: middle;
      }
      .title {
        font-size: 16px;
        font-weight: 600;
        margin-left: 8px;
        user-select: none;
        white-space: nowrap;
      }
      .title-light {
        color: #001529;
      }
      .title-dark {
        color: #ffffff;
      }

      /* 高端主题 logo：精致容器 + 分隔条 + 品牌色 */
      &.modern-logo {
        min-width: 210px;
        padding: 0 4px;

        &:hover {
          opacity: 1;
          .logo-icon-box {
            transform: scale(1.06);
            box-shadow:
              0 2px 8px rgba(99, 102, 241, 0.18),
              0 0 0 1px rgba(99, 102, 241, 0.12);
          }
          .title-light {
            color: #3730a3;
          }
        }

        .logo-icon-box {
          width: 34px;
          height: 34px;
          border-radius: 9px;
          background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          transition: transform 0.25s ease, box-shadow 0.25s ease;
          box-shadow:
            0 1px 3px rgba(99, 102, 241, 0.10),
            0 0 0 1px rgba(99, 102, 241, 0.08);

          .logo-img {
            height: 22px;
          }
        }

        .logo-divider {
          width: 1px;
          height: 20px;
          background: linear-gradient(
            180deg,
            rgba(99, 102, 241, 0.06) 0%,
            rgba(99, 102, 241, 0.20) 50%,
            rgba(99, 102, 241, 0.06) 100%
          );
          margin: 0 10px;
          flex-shrink: 0;
        }

        .title {
          margin-left: 0;
        }

        .title-light {
          color: #1e293b;
          letter-spacing: 0.4px;
          font-weight: 700;
          font-size: 15px;
          background: linear-gradient(135deg, #4338ca 0%, #6366f1 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          transition: background 0.25s ease;
        }
      }
    }

    .user-space {
      min-width: 208px;
      margin-left: auto;
      padding-right: 10px;
      color: v-bind('color.color');
      display: flex;
      flex-direction: row;
      vertical-align: middle;
      align-items: center;
      justify-content: flex-end;

      .setting {
        height: var(--header-height, 40px);
        line-height: var(--header-height, 40px);
        vertical-align: middle;
        display: flex;
        align-items: center;

        :deep(.ant-badge) {
          color: v-bind('color.color');
        }
      }
      .operate-icon {
        margin-left: 20px;
        color: v-bind('color.color');
      }
      /* 高端主题：操作图标 hover 渐变为靛蓝 */
      .modern-operate-icon {
        color: #475569;
        &:hover {
          color: #4f46e5 !important;
          background: rgba(99, 102, 241, 0.08) !important;
        }
      }

      .user-space-item {
        margin-left: 10px;
      }

      /* 高端主题：用户区域更精致的间距 */
      &.modern-user-space {
        .setting :deep(.ant-btn) {
          color: #475569;
          &:hover {
            color: #4f46e5 !important;
          }
        }
        :deep(.header-trigger .name) {
          color: #334155;
          font-weight: 600;
        }
        :deep(.avatar-image) {
          box-shadow: 0 1px 3px rgba(99, 102, 241, 0.18),
                      0 0 0 2px rgba(99, 102, 241, 0.08);
        }
      }
    }
  }

  :deep(.ant-menu-horizontal) {
    border-bottom: 0;
  }
</style>
