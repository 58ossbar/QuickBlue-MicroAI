<!--
  * 递归菜单
  *
  * @Author:
  * @Date:      2022-09-06 20:29:12
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <div class="recursion-container" v-show="topMenu.children && topMenu.children.length > 0">
    <!-- 顶部logo区域 -->
    <div class="logo" @click="onGoHome" :style="sideMenuWidth" v-if="!collapsed">
      <img class="logo-img" :src="logoImg" />
      <div class="title qb-logo title-light" v-if="isLight">{{ websiteName }}</div>
      <div class="title qb-logo title-dark" v-if="!isLight">{{ websiteName }}</div>
    </div>
    <div class="min-logo" @click="onGoHome" v-if="collapsed">
      <img class="logo-img" :src="logoImg" />
    </div>
    <!-- 次级菜单展示 -->
    <a-menu
      :selectedKeys="selectedKeys"
      :theme="theme === 'modern' ? 'light' : theme"
      :class="'menu-theme-' + theme"
      :openKeys="openKeys"
      mode="inline"
    >
      <template v-for="item in topMenu.children" :key="item.menuId.toString()">
        <template v-if="item.visibleFlag">
          <template v-if="$lodash.isEmpty(item.children)">
            <a-menu-item :key="item.menuId.toString()" @click="turnToPage(item)">
              <template #icon v-if="item.icon">
                <component :is="$antIcons[item.icon]" />
              </template>
              {{ item.menuName }}
            </a-menu-item>
          </template>
          <template v-else>
            <SubMenu :menu-info="item" :key="item.menuId.toString()" @turnToPage="turnToPage" />
          </template>
        </template>
      </template>
    </a-menu>
  </div>
</template>
<script setup>
  import { ref, computed, watch } from 'vue';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
  import SubMenu from './sub-menu.vue';
  import { router } from '/@/router';
  import { useRoute } from 'vue-router';
  import _ from 'lodash';
  import menuEmitter from './top-expand-menu-mitt';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import { useUserStore } from '/@/store/modules/system/user';
  import logoImg from '/@/assets/images/logo/budaos-logo.png';

  const websiteName = computed(() => useAppConfigStore().websiteName);
  const theme = computed(() => useAppConfigStore().$state.sideMenuTheme);

  const props = defineProps({
    collapsed: {
      type: Boolean,
      default: false,
    },
  });

  //菜单宽度
  const sideMenuWidth = computed(() => useAppConfigStore().$state.sideMenuWidth);

  // 选中的顶级菜单
  let topMenu = ref({});
  menuEmitter.on('selectTopMenu', onSelectTopMenu);

  //动态通知顶部菜单栏侧边栏状态
  watch(
    topMenu,
    (value) => {
      let hasSideMenu = value.children && value.children.length > 0;
      menuEmitter.emit('sideMenuChange', hasSideMenu);
    },
    { immediate: true, deep: true }
  );

  // 监听选中顶级菜单事件
  function onSelectTopMenu(selectedTopMenu) {
    topMenu.value = selectedTopMenu;
    if (selectedTopMenu.children && selectedTopMenu.children.length > 0) {
      openKeys.value = _.map(selectedTopMenu.children, 'menuId').map((e) => e.toString());
    } else {
      openKeys.value = [];
    }
    selectedKeys.value = [];
  }

  //展开的菜单
  let currentRoute = useRoute();
  const selectedKeys = ref([]);
  const openKeys = ref([]);

  function updateSelectKeyAndOpenKey(parentList, currentSelectKey) {
    if (!parentList) {
      return;
    }
    //获取需要展开的menu key集合
    openKeys.value = _.map(parentList, 'name');
    selectedKeys.value = [currentSelectKey];
  }

  watch(
    currentRoute,
    (value) => {
      selectedKeys.value = [value.name];
    },
    {
      immediate: true,
    }
  );
  // 页面跳转
  function turnToPage(route) {
    useUserStore().deleteKeepAliveIncludes(route.menuId.toString());
    router.push({ name: route.menuId.toString() });
  }

  function onGoHome() {
    router.push({ name: HOME_PAGE_NAME });
  }

  defineExpose({ updateSelectKeyAndOpenKey });

  const isLight = computed(() => useAppConfigStore().$state.sideMenuTheme === 'light' || useAppConfigStore().$state.sideMenuTheme === 'modern');
  const color = computed(() => {
    const theme = useAppConfigStore().$state.sideMenuTheme;
    // dark=#001529, light=#ffffff, modern=#edeff4
    if (theme === 'modern') return { background: '#edeff4' };
    return {
      background: theme === 'light' ? '#FFFFFF' : '#001529',
    };
  });
</script>
<style scoped lang="less">
  .recursion-container {
    height: 100%;
    background-color: v-bind('color.background');
  }

  .min-logo {
    height: var(--header-height, 40px);
    line-height: var(--header-height, 40px);
    padding: 0px 15px 0px 15px;
    // background-color: v-bind('color.background');

    width: 80px;
    z-index: 21;
    display: flex;
    justify-content: center;
    align-items: center;
    .logo-img {
      width: 30px;
      height: 30px;
    }
  }
  .top-menu {
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    height: var(--header-height, 40px);
    font-size: 16px;
    color: #515a6e;
    border-bottom: 1px solid #f3f3f3;
    border-right: 1px solid #f3f3f3;
  }
  .logo {
    height: var(--header-height, 40px);
    line-height: var(--header-height, 40px);
    padding: 0px 15px 0px 15px;
    width: 100%;
    z-index: 100;
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;

    .logo-img {
      width: 30px;
      height: 30px;
    }

    .title {
      font-size: 16px;
      font-weight: 600;
      overflow: hidden;
      word-wrap: break-word;
      white-space: nowrap;
      // modern 主题下用深色文字（与 light 一致），dark 主题用白色
      color: v-bind('isLight ? "#001529": "#ffffff"');
    }
  }

  // ====== 现代主题（高端）：卡片悬浮风格 =====
  &.menu-theme-modern,
  :deep(.menu-theme-modern) {
    background: transparent;

    // 一级菜单项：圆角卡片、有外边距、白色悬浮选中
    :deep(.ant-menu-item) {
      color: #1e293b;
      font-weight: 600;
      font-size: 14px;
      letter-spacing: 0.2px;
      margin: 3px 8px;
      width: auto !important;
      border-radius: 10px;
      transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;

      .anticon {
        color: #5b21b6;
        font-size: 16px;
        transition: color 0.2s ease, transform 0.2s ease;
      }

      &:hover {
        color: #4f46e5;
        background-color: rgba(99, 102, 241, 0.08);

        .anticon {
          color: #4f46e5;
          transform: scale(1.10);
        }
      }

      // selected：白色卡片 + 靛蓝边框 + 投影，在灰底上悬浮
      &.ant-menu-item-selected {
        color: #4338ca !important;
        background: #ffffff !important;
        font-weight: 700;
        border-radius: 10px;
        box-shadow: 0 4px 14px rgba(79, 70, 229, 0.14),
                    0 1px 3px rgba(15, 23, 42, 0.06),
                    inset 0 0 0 1.5px rgba(99, 102, 241, 0.28);

        &::after {
          display: none !important;
        }

        &::before {
          background: transparent !important;
        }

        .anticon {
          color: #4f46e5 !important;
          transform: scale(1.05);
        }
      }
    }

    // 子菜单标题（卡片风格）
    :deep(.ant-menu-submenu-title) {
      color: #1e293b;
      font-weight: 600;
      font-size: 14px;
      margin: 3px 8px;
      border-radius: 10px;
      transition: background 0.2s ease, color 0.2s ease;

      .anticon {
        color: #5b21b6;
        font-size: 16px;
        transition: color 0.2s ease;
      }

      .ant-menu-submenu-arrow {
        color: #7c3aed;
        transition: color 0.2s ease;
      }

      &:hover {
        color: #4f46e5;
        background-color: rgba(99, 102, 241, 0.08);

        .anticon {
          color: #4f46e5;
        }

        .ant-menu-submenu-arrow {
          color: #4f46e5;
        }
      }

      &.ant-menu-submenu-open {
        color: #4f46e5;
        background-color: rgba(99, 102, 241, 0.06);
      }
    }

    // 子菜单区：略深嵌套底色
    :deep(.ant-menu-sub) {
      background: rgba(232, 235, 242, 0.55) !important;
      margin: 0 8px;
      border-radius: 8px;
    }

    // 子菜单项：小幅圆角卡片
    :deep(.ant-menu-sub .ant-menu-item) {
      color: #334155;
      font-weight: 500;
      font-size: 13px;
      margin: 2px 8px;
      border-radius: 7px;
      transition: background 0.2s ease, color 0.2s ease;

      &:hover {
        color: #4f46e5;
        background-color: rgba(99, 102, 241, 0.10);
      }

      &.ant-menu-item-selected {
        color: #4f46e5 !important;
        background: #ffffff !important;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(79, 70, 229, 0.12),
                    inset 0 0 0 1.5px rgba(99, 102, 241, 0.20);

        &::before {
          background: transparent !important;
        }
      }
    }
  }
</style>
