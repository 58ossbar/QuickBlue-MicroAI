<script setup>
import _ from 'lodash';
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { theme as antTheme } from 'ant-design-vue';
import SubMenu from './sub-menu.vue';
import { router } from '/@/router/index';
import { useAppConfigStore } from '/@/store/modules/system/app-config';
import { useUserStore } from '/@/store/modules/system/user';

const { useToken } = antTheme;
const { token } = useToken();

const primaryColor = computed(() => token.value.colorPrimary);
const textColor = computed(() => token.value.colorText);
const textColorSecondary = computed(() => token.value.colorTextSecondary);

const menuTheme = computed(() => useAppConfigStore().$state.sideMenuTheme);
const menuSingleExpandFlag = computed(() => useAppConfigStore().$state.menuSingleExpandFlag);
const menuCollapsedShow = computed(() => useAppConfigStore().$state.menuCollapsedShow);

// 页面紧凑模式：动态调整菜单项高度
const compactFlag = computed(() => useAppConfigStore().compactFlag);
const menuItemHeight = computed(() => compactFlag.value ? '30px' : '38px');
const menuSubItemHeight = computed(() => compactFlag.value ? '26px' : '34px');
const collapsedItemHeight = computed(() => compactFlag.value ? '28px' : '36px');

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false,
  },
});

const menuTree = computed(() => useUserStore().getMenuTree || []);
const rootSubmenuKeys = computed(() =>
  menuTree.value.map((item) => String(item.menuId))
);

// 分组后的菜单数据：将 menuType===0 的分组标签与后续菜单项组合
const groupedMenuTree = computed(() => {
  const groups = [];
  let currentGroup = null;

  for (const item of menuTree.value) {
    if (!item.visibleFlag || item.disabledFlag) continue;

    if (item.menuType === 0) {
      // 分组标签
      currentGroup = { label: item, items: [] };
      groups.push(currentGroup);
    } else {
      // 菜单项
      if (!currentGroup) {
        currentGroup = { label: null, items: [] };
        groups.push(currentGroup);
      }
      currentGroup.items.push(item);
    }
  }

  return groups;
});

let currentRoute = useRoute();
const selectedKeys = ref([]);
const openKeys = ref([]);

function turnToPage(menu) {
  useUserStore().deleteKeepAliveIncludes(menu.menuId.toString());
  router.push({ path: menu.path });
}

function updateOpenKeysAndSelectKeys() {
  selectedKeys.value = [String(currentRoute.name)];

  let menuParentIdListMap = useUserStore().getMenuParentIdListMap;
  let parentList = menuParentIdListMap.get(currentRoute.name) || [];

  if (props.collapsed) return;

  let needOpenKeys = _.map(parentList, 'name');
  if (menuSingleExpandFlag.value) {
    if (needOpenKeys.length > 0) {
      openKeys.value = [...needOpenKeys];
    }
  } else {
    openKeys.value = _.union(openKeys.value, needOpenKeys);
  }
}

watch(currentRoute, () => updateOpenKeysAndSelectKeys(), { immediate: true });

function onOpenChange(openKeysParams) {
  // 统一转为字符串，与 a-menu 各节点 :key（menuId.toString()）保持一致，
  // 避免 ant-design-vue 内部 openKeys.includes(key) 因数字/字符串类型不一致而失配
  const normalizedKeys = (openKeysParams || []).map((k) => String(k));

  if (!menuSingleExpandFlag.value) {
    openKeys.value = normalizedKeys;
    return;
  }

  const newMainMenus = normalizedKeys.filter((k) => rootSubmenuKeys.value.includes(k));
  const oldMainMenus = openKeys.value.filter((k) => rootSubmenuKeys.value.includes(k));

  const newlyOpened = newMainMenus.find((k) => !oldMainMenus.includes(k));
  if (newlyOpened) {
    // 单展开模式：点击其他顶级菜单时，只保留新展开的菜单
    openKeys.value = [newlyOpened];
    return;
  }

  // 其余情况（含收缩当前顶级菜单、子菜单展开/收缩）直接跟随用户操作
  openKeys.value = normalizedKeys;
}

defineExpose({ updateOpenKeysAndSelectKeys });
</script>

<template>
  <div class="menu-root-container">
    <!-- ====== 分组标签（放在 a-menu 外部，避免破坏 ant-design 内部 DOM 管理） ====== -->
    <template v-for="group in groupedMenuTree" :key="group.label ? 'group-' + group.label.menuId : 'group-ungrouped'">
      <div
        v-if="group.label && !collapsed"
        class="menu-group-label"
        :class="menuTheme === 'dark' ? 'label-dark' : 'label-light'"
      >
        <span class="label-line"></span>
        <span class="label-text">{{ group.label.menuName }}</span>
        <span class="label-line"></span>
      </div>
    </template>

    <!-- ====== 菜单主体 ====== -->
    <a-menu
      :open-keys="openKeys"
      v-model:selectedKeys="selectedKeys"
      class="qb-menu"
      :class="[
        'menu-theme-' + menuTheme,
        collapsed ? 'collapsed-show-' + menuCollapsedShow : '',
      ]"
      mode="inline"
      :inline-collapsed="collapsed"
      :theme="menuTheme === 'modern' ? 'light' : menuTheme"
      @openChange="onOpenChange"
    >
      <template v-for="item in menuTree" :key="item.menuId.toString()">
        <template v-if="item.visibleFlag && !item.disabledFlag && item.menuType !== 0">
          <template v-if="$lodash.isEmpty(item.children)">
            <a-menu-item :key="item.menuId.toString()" @click="turnToPage(item)">
              <template #icon>
                <span class="menu-icon-box">
                  <component :is="$antIcons[item.icon]" />
                </span>
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

<style lang="less" scoped>
.menu-root-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 8px;

  scrollbar-width: thin;
  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 2px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
}

/* ====== 分组标签 ====== */
.menu-group-label {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px 6px 20px;
  user-select: none;
  flex-shrink: 0;
  position: relative;

  &::before {
    content: '';
    width: 3px;
    height: 3px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .label-line {
    flex: 1;
    height: 1px;
    background-color: transparent;
  }

  .label-text {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 1.5px;
    text-transform: uppercase;
    white-space: nowrap;
    padding: 2px 0;
  }

  &.label-light {
    &::before { background-color: #8c8c8c; }
    .label-line { background-color: #e8e8e8; }
    .label-text { color: #8c8c8c; }
  }

  &.label-dark {
    &::before { background-color: rgba(255, 255, 255, 0.4); }
    .label-line { background-color: rgba(255, 255, 255, 0.12); }
    .label-text { color: rgba(255, 255, 255, 0.4); }
  }

  &:first-child {
    padding-top: 10px;
  }
}

/* ====== a-menu ====== */
.qb-menu {
  flex: 1;
  overflow-y: visible;
  overflow-x: hidden;
  border-right: none;
  background: transparent;

  --menu-primary: v-bind('primaryColor');
  --menu-text: v-bind('textColor');
  --menu-text-secondary: v-bind('textColorSecondary');

  // ====== 图标容器 ======
  .menu-icon-box {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    font-size: 15px;
    transition: color 0.15s ease;
  }

  // ====== 一级菜单项 ======
  :deep(.ant-menu-item) {
    height: v-bind(menuItemHeight);
    line-height: v-bind(menuItemHeight);
    margin: 1px 8px !important;
    padding: 0 12px !important;
    border-radius: 6px;
    transition: background-color 0.15s ease, color 0.15s ease;
    position: relative;
    color: var(--menu-text);
    background: transparent;
    font-size: 13.5px;
    font-weight: 400;

    &:hover {
      color: var(--menu-primary);
      background-color: rgba(24, 144, 255, 0.06);

      .menu-icon-box {
        color: var(--menu-primary);
      }
    }

    &.ant-menu-item-selected {
      color: var(--menu-primary);
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
        background-color: var(--menu-primary);
        border-radius: 0 2px 2px 0;
      }

      &::after {
        display: none !important;
      }

      .menu-icon-box {
        color: var(--menu-primary);
      }
    }

    .anticon {
      font-size: 15px;
      margin-right: 10px;
      color: var(--menu-text-secondary);
      transition: color 0.15s ease;
    }
  }

  // ====== 子菜单标题 ======
  :deep(.ant-menu-submenu-title) {
    height: v-bind(menuItemHeight);
    line-height: v-bind(menuItemHeight);
    margin: 1px 8px !important;
    padding: 0 12px !important;
    border-radius: 6px;
    transition: background-color 0.15s ease, color 0.15s ease;
    color: var(--menu-text);
    background: transparent;
    font-size: 13.5px;
    font-weight: 400;
    position: relative;

    &:hover {
      color: var(--menu-primary);
      background-color: rgba(24, 144, 255, 0.06);

      .ant-menu-submenu-arrow {
        color: var(--menu-primary);
      }
    }

    &.ant-menu-submenu-open {
      color: var(--menu-primary);
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
        background-color: var(--menu-primary);
        border-radius: 0 2px 2px 0;
      }

      .ant-menu-submenu-arrow {
        color: var(--menu-primary);
        transform: translateY(-50%) rotate(180deg);
      }
    }

    .anticon {
      font-size: 15px;
      margin-right: 10px;
      color: var(--menu-text-secondary);
      transition: color 0.15s ease;
    }
  }

  // ====== 子菜单展开箭头 ======
  :deep(.ant-menu-submenu-arrow) {
    transition: color 0.15s ease, transform 0.2s ease !important;
    color: var(--menu-text-secondary);
  }

  // ====== 子菜单下的菜单项 ======
  :deep(.ant-menu-sub) {
    background: transparent !important;
  }

  :deep(.ant-menu-sub .ant-menu-item) {
    height: v-bind(menuSubItemHeight);
    line-height: v-bind(menuSubItemHeight);
    padding-left: 44px !important;
    margin: 1px 12px !important;
    border-radius: 5px;
    font-size: 13px;
    font-weight: 400;
    background: transparent;
    transition: background-color 0.15s ease, color 0.15s ease;
    position: relative;

    &::after {
      display: none !important;
    }

    &:hover {
      color: var(--menu-primary);
      background-color: rgba(24, 144, 255, 0.06);
    }

    &.ant-menu-item-selected {
      color: var(--menu-primary);
      font-weight: 500;
      background-color: rgba(24, 144, 255, 0.10);

      &::before {
        content: '';
        position: absolute;
        left: 28px;
        top: 50%;
        transform: translateY(-50%);
        width: 2px;
        height: 14px;
        background-color: var(--menu-primary);
        border-radius: 1px;
      }
    }
  }

  // 三级菜单
  :deep(.ant-menu-sub .ant-menu-sub .ant-menu-item) {
    padding-left: 56px !important;
    margin: 1px 16px !important;
    font-size: 12.5px;
  }

  // 四级菜单
  :deep(.ant-menu-sub .ant-menu-sub .ant-menu-sub .ant-menu-item) {
    padding-left: 68px !important;
    margin: 1px 20px !important;
  }

  // ====== 深色主题 ======
  &.ant-menu-dark {
    background: transparent;

    :deep(.ant-menu-item) {
      color: rgba(255, 255, 255, 0.7);

      &:hover {
        color: #fff;
        background-color: rgba(255, 255, 255, 0.06);

        .anticon {
          color: #fff;
        }
      }

      &.ant-menu-item-selected {
        color: #fff;
        background-color: rgba(24, 144, 255, 0.20);

        &::before {
          background-color: #1890ff;
        }
      }

      .anticon {
        color: rgba(255, 255, 255, 0.5);
      }
    }

    :deep(.ant-menu-submenu-title) {
      color: rgba(255, 255, 255, 0.7);

      &:hover {
        color: #fff;
        background-color: rgba(255, 255, 255, 0.06);

        .anticon {
          color: #fff;
        }
      }

      &.ant-menu-submenu-open {
        color: #fff;
        background-color: rgba(24, 144, 255, 0.20);

        &::before {
          background-color: #1890ff;
        }
      }

      .anticon {
        color: rgba(255, 255, 255, 0.5);
      }
    }

    :deep(.ant-menu-sub .ant-menu-item) {
      color: rgba(255, 255, 255, 0.6);

      &:hover {
        color: #fff;
        background-color: rgba(255, 255, 255, 0.05);
      }

      &.ant-menu-item-selected {
        color: #fff;
        background-color: rgba(24, 144, 255, 0.18);

        &::before {
          background-color: #1890ff;
        }
      }
    }

    :deep(.ant-menu-submenu-arrow) {
      color: rgba(255, 255, 255, 0.5);
    }
  }

  // ====== 浅色主题 ======
  &.ant-menu-light {
    :deep(.ant-menu-item.ant-menu-item-selected) {
      color: var(--menu-primary);
      background-color: rgba(24, 144, 255, 0.10);

      &::before {
        background-color: var(--menu-primary);
      }
    }

    :deep(.ant-menu-submenu-open > .ant-menu-submenu-title) {
      color: var(--menu-primary);
      font-weight: 500;
      background-color: rgba(24, 144, 255, 0.10);

      &::before {
        background-color: var(--menu-primary);
      }
    }

    :deep(.ant-menu-sub .ant-menu-item.ant-menu-item-selected) {
      &::before {
        background-color: var(--menu-primary);
      }
    }
  }

  // ====== 现代主题（高端）：卡片悬浮风格 =====
  // 与浅色主题的核心区别：
  //   浅色 — 纯白底、扁平化、蓝色 primary
  //   高端 — 灰底上悬浮白色圆角卡片、靛蓝 indigo 主色、有阴影层级
  // 视觉特征：圆角卡片 + 间距留白 + 白色选中卡 + 靛蓝边框 + 精致阴影
  &.menu-theme-modern {
    // 一级菜单项：深色字、加粗、圆角卡片、有外边距
    :deep(.ant-menu-item) {
      color: #1e293b;
      font-weight: 600;
      font-size: 14px;
      letter-spacing: 0.2px;
      margin: 3px 8px;
      width: auto !important;
      border-radius: 10px;
      transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;

      .menu-icon-box,
      .anticon {
        color: #5b21b6;
        font-size: 16px;
        transition: color 0.2s ease, transform 0.2s ease;
      }

      &:hover {
        color: #4f46e5;
        background-color: rgba(99, 102, 241, 0.08);

        .menu-icon-box,
        .anticon {
          color: #4f46e5;
          transform: scale(1.10);
        }
      }

      // selected：白色圆角卡片 + 靛蓝边框 + 精致投影，在灰底上悬浮
      &.ant-menu-item-selected {
        color: #4338ca !important;
        background: #ffffff !important;
        font-weight: 700;
        border-radius: 10px;
        box-shadow: 0 4px 14px rgba(79, 70, 229, 0.14),
                    0 1px 3px rgba(15, 23, 42, 0.06),
                    inset 0 0 0 1.5px rgba(99, 102, 241, 0.28);

        // 去除 antd 默认左侧条，改用卡片投影区分
        &::before {
          background: transparent !important;
        }

        .menu-icon-box,
        .anticon {
          color: #4f46e5 !important;
          transform: scale(1.05);
        }
      }
    }

    // 子菜单标题（卡片风格，与一级一致）
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

    // 子菜单区：略深底色 + 左侧缩进，与一级菜单形成层次嵌套
    :deep(.ant-menu-sub) {
      background: rgba(232, 235, 242, 0.55) !important;
      margin: 0 8px;
      border-radius: 8px;
    }

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

  // ====== 折叠状态 ======
  &.ant-menu-inline-collapsed {
    :deep(.ant-menu-item) {
      padding: 0 !important;
      text-align: center;
      margin: 2px 12px !important;
      height: v-bind(collapsedItemHeight);
      line-height: v-bind(collapsedItemHeight);

      .menu-icon-box {
        margin: 0;
      }
    }

    :deep(.ant-menu-submenu-title) {
      padding: 0 !important;
      text-align: center;
      margin: 2px 12px !important;
      height: v-bind(collapsedItemHeight);
      line-height: v-bind(collapsedItemHeight);
    }

    :deep(.ant-menu-submenu-arrow) {
      display: none;
    }
  }

  // ====== 折叠后显示：仅文字 ======
  &.collapsed-show-text.ant-menu-inline-collapsed {
    :deep(.ant-menu-item),
    :deep(.ant-menu-submenu-title) {
      padding: 0 6px !important;
      display: flex;
      align-items: center;
      justify-content: center;

      .menu-icon-box,
      .anticon {
        display: none !important;
      }

      .ant-menu-title-content {
        opacity: 1 !important;
        visibility: visible !important;
        max-width: none !important;
        margin-left: 0 !important;
        display: inline-block !important;
        font-size: 13px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  // ====== 折叠后显示：图标+文字（左右结构，大小与二级菜单一致） ======
  &.collapsed-show-both.ant-menu-inline-collapsed {
    :deep(.ant-menu-item),
    :deep(.ant-menu-submenu-title) {
      padding: 0 6px !important;
      height: v-bind(collapsedItemHeight);
      line-height: v-bind(collapsedItemHeight);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      // 整体向左微移，确保在菜单宽度视觉中心
      transform: translateX(-4px);

      .menu-icon-box,
      .anticon {
        margin: 0 !important;
        font-size: 14px;
      }

      .ant-menu-title-content {
        opacity: 1 !important;
        visibility: visible !important;
        max-width: none !important;
        margin-left: 0 !important;
        display: inline-block !important;
        font-size: 13px;
        line-height: 1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}
</style>
