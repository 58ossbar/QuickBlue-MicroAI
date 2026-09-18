<!--
  * 顶部菜单-递归菜单
  * 
  * @Author:    budaos 
  * @Date:      2022-09-06 20:29:12 
-->
<template>
  <a-menu
    v-model:openKeys="openKeys"
    v-model:selectedKeys="selectedKeys"
    class="qb-menu"
    :class="'menu-theme-' + theme"
    mode="horizontal"
    :theme="theme === 'modern' ? 'light' : theme"
  >
    <template v-for="item in menuTree" :key="item.menuId.toString()">
      <template v-if="item.visibleFlag && !item.disabledFlag">
        <template v-if="$lodash.isEmpty(item.children)">
          <a-menu-item :key="item.menuId.toString()" @click="turnToPage(item)">
            <template #icon>
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
</template>
<script setup>
  import _ from 'lodash';
  import { computed, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import SubMenu from './sub-menu.vue';
  import { router } from '/@/router/index';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import { useUserStore } from '/@/store/modules/system/user';

  const theme = computed(() => useAppConfigStore().$state.sideMenuTheme);

  // 页面紧凑模式：动态调整顶部菜单项高度
  const compactFlag = computed(() => useAppConfigStore().compactFlag);
  const topMenuItemHeight = computed(() => compactFlag.value ? '40px' : '48px');
  const topMenuDropdownItemHeight = computed(() => compactFlag.value ? '30px' : '36px');

  const menuTree = computed(() => useUserStore().getMenuTree || []);

  //展开的菜单
  let currentRoute = useRoute();
  const selectedKeys = ref([]);
  const openKeys = ref([]);

  // 页面跳转
  function turnToPage(menu) {
    useUserStore().deleteKeepAliveIncludes(menu.menuId.toString());
    router.push({ path: menu.path });
  }

  /**
   * router 的 name 与后端存储的 menu 的 id 一致
   * 所以此处可以直接监听路由，根据路由更新菜单的选中和展开
   */
  function updateSelectKeys() {
    // 更新选中
    selectedKeys.value = [currentRoute.name];
  }

  watch(
    currentRoute,
    () => {
      updateSelectKeys();
    },
    {
      immediate: true,
    }
  );

  defineExpose({
    updateSelectKeys,
  });
</script>

<style lang="less" scoped>
  .qb-menu {
    position: relative;
    border-bottom: none;

    // ====== 一级菜单项 ======
    :deep(.ant-menu-item) {
      border-radius: 8px;
      margin: 0 4px !important;
      padding: 0 18px !important;
      font-size: 14px;
      font-weight: 500;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      height: v-bind(topMenuItemHeight);
      line-height: v-bind(topMenuItemHeight);

      &:hover {
        color: #1890ff !important;
        background: rgba(24, 144, 255, 0.06) !important;
      }

      &::after {
        display: none !important;
      }

      &.ant-menu-item-selected {
        color: #1890ff !important;
        font-weight: 600;
        background: rgba(24, 144, 255, 0.06) !important;

        &::before {
          content: '';
          position: absolute;
          bottom: 0;
          left: 20%;
          right: 20%;
          height: 3px;
          background: #1890ff;
          border-radius: 3px 3px 0 0;
        }
      }
    }

    // ====== 子菜单标题 ======
    :deep(.ant-menu-submenu) {
      padding: 0 4px !important;

      .ant-menu-submenu-title {
        border-radius: 8px;
        margin: 0;
        padding: 0 18px !important;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        height: v-bind(topMenuItemHeight);
        line-height: v-bind(topMenuItemHeight);

        &:hover {
          color: #1890ff !important;
          background: rgba(24, 144, 255, 0.06) !important;
        }

        &::after {
          display: none !important;
        }
      }

      &.ant-menu-submenu-open > .ant-menu-submenu-title {
        color: #1890ff !important;
        background: rgba(24, 144, 255, 0.06) !important;

        &::before {
          content: '';
          position: absolute;
          bottom: 0;
          left: 20%;
          right: 20%;
          height: 3px;
          background: #1890ff;
          border-radius: 3px 3px 0 0;
        }
      }
    }

    // ====== 下拉菜单项 ======
    :deep(.ant-menu-submenu-popup) {
      .ant-menu-item {
        border-radius: 6px;
        margin: 2px 8px !important;
        height: v-bind(topMenuDropdownItemHeight);
        line-height: v-bind(topMenuDropdownItemHeight);
        padding: 0 12px !important;

        &::before {
          display: none !important;
        }

        &:hover {
          color: #1890ff !important;
          background: rgba(24, 144, 255, 0.05) !important;
        }

        &.ant-menu-item-selected {
          color: #1890ff !important;
          font-weight: 500;
          background-color: rgba(24, 144, 255, 0.10) !important;
        }
      }
    }

    // ====== 深色主题 ======
    &.ant-menu-dark {
      :deep(.ant-menu-item) {
        color: rgba(255, 255, 255, 0.75);

        &:hover {
          color: #fff !important;
          background: rgba(255, 255, 255, 0.08) !important;
        }

        &.ant-menu-item-selected {
          color: #fff !important;
          background: rgba(255, 255, 255, 0.1) !important;

          &::before {
            background: #1890ff;
          }
        }
      }

      :deep(.ant-menu-submenu-title) {
        color: rgba(255, 255, 255, 0.75);

        &:hover {
          color: #fff !important;
          background: rgba(255, 255, 255, 0.08) !important;
        }
      }

      :deep(.ant-menu-submenu.ant-menu-submenu-open > .ant-menu-submenu-title) {
        color: #fff !important;
        background: rgba(255, 255, 255, 0.1) !important;

        &::before {
          background: #1890ff;
        }
      }
    }

    // ====== 现代主题（高端）：卡片悬浮 + 靛蓝主色 =====
    // 顶部横向菜单：白色卡片选中态 + 靛蓝边框阴影 + 底部渐变指示条
    &.menu-theme-modern {
      :deep(.ant-menu-item) {
        color: #1e293b;
        font-weight: 600;
        letter-spacing: 0.2px;
        border-radius: 9px;
        margin: 4px 3px;
        padding: 0 16px;
        transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;

        .anticon {
          color: #5b21b6;
          font-weight: 600;
          font-size: 16px;
          transition: color 0.2s ease, transform 0.2s ease;
        }

        &:hover {
          color: #4f46e5 !important;
          background: rgba(99, 102, 241, 0.08) !important;
          border-radius: 9px;

          .anticon {
            color: #4f46e5 !important;
            transform: scale(1.10);
          }
        }

        // selected：白色卡片 + 靛蓝边框 + 投影悬浮
        &.ant-menu-item-selected {
          color: #4338ca !important;
          background: #ffffff !important;
          font-weight: 700;
          border-radius: 9px;
          box-shadow: 0 4px 14px rgba(79, 70, 229, 0.14),
                      0 1px 3px rgba(15, 23, 42, 0.06),
                      inset 0 0 0 1.5px rgba(99, 102, 241, 0.28);

          .anticon {
            color: #4f46e5 !important;
          }

          &::before {
            background: linear-gradient(90deg, #6366f1 0%, #4f46e5 100%) !important;
            height: 2.5px !important;
            box-shadow: 0 0 8px rgba(99, 102, 241, 0.40);
          }
        }
      }

      // 子菜单标题（横向，卡片风格）
      :deep(.ant-menu-submenu-title) {
        color: #1e293b;
        font-weight: 600;
        border-radius: 9px;
        margin: 4px 3px;
        padding: 0 16px;
        transition: background 0.2s ease, color 0.2s ease;

        .anticon {
          color: #5b21b6;
          font-size: 16px;
          transition: color 0.2s ease;
        }

        &:hover {
          color: #4f46e5 !important;
          background: rgba(99, 102, 241, 0.08) !important;
          border-radius: 9px;

          .anticon {
            color: #4f46e5 !important;
          }
        }
      }

      // 子菜单展开（open）：白色卡片
      :deep(.ant-menu-submenu.ant-menu-submenu-open > .ant-menu-submenu-title) {
        color: #4338ca !important;
        background: #ffffff !important;
        font-weight: 700;
        border-radius: 9px;
        box-shadow: 0 4px 14px rgba(79, 70, 229, 0.14),
                    0 1px 3px rgba(15, 23, 42, 0.06),
                    inset 0 0 0 1.5px rgba(99, 102, 241, 0.28);

        .anticon {
          color: #4f46e5 !important;
        }

        &::before {
          background: linear-gradient(90deg, #6366f1 0%, #4f46e5 100%) !important;
          box-shadow: 0 0 8px rgba(99, 102, 241, 0.40);
        }
      }

      // 下拉弹出层圆角卡片
      :deep(.ant-menu-submenu-popup) {
        .ant-menu {
          background: #ffffff !important;
          box-shadow: 0 8px 30px rgba(15, 23, 42, 0.12),
                      0 2px 4px rgba(15, 23, 42, 0.06);
          border-radius: 12px;
          padding: 6px;
        }
      }
    }
  }
</style>
