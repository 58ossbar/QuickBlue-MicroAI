<!--
  * 侧边导航（图标布局）弹出面板递归子菜单
  *
  * 递归渲染多级菜单：有子菜单的项点击展开/收起下级，无子菜单的项点击跳转
  * 组件通过 SFC 文件名隐式自引用（SideIconSubMenu）
-->

<template>
  <div class="sub-menu-tree">
    <template v-for="child in visibleChildren" :key="child.menuId">
      <!-- 有子菜单：可展开 -->
      <div
        v-if="hasChildren(child)"
        class="sub-item has-children"
        :class="{ expanded: expandedKeys.has(child.menuId) }"
        @click="toggle(child.menuId)"
      >
        <span class="sub-icon">
          <component :is="$antIcons[child.icon]" v-if="child.icon && $antIcons[child.icon]" class="sub-icon-comp" />
        </span>
        <span class="sub-label">{{ child.menuName }}</span>
        <DownOutlined class="sub-arrow" />
      </div>
      <div v-if="hasChildren(child) && expandedKeys.has(child.menuId)" class="sub-children">
        <SideIconSubMenu :menu="child" @navigate="onNavigateChild" />
      </div>

      <!-- 无子菜单：点击跳转 -->
      <div
        v-else
        class="sub-item"
        :class="{ active: isActive(child) }"
        @click="onNavigate(child)"
      >
        <span class="sub-icon">
          <component :is="$antIcons[child.icon]" v-if="child.icon && $antIcons[child.icon]" class="sub-icon-comp" />
        </span>
        <span class="sub-label">{{ child.menuName }}</span>
      </div>
    </template>
  </div>
</template>
<script setup>
  import { computed, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { DownOutlined } from '@ant-design/icons-vue';

  const props = defineProps({
    menu: {
      type: Object,
      default: () => ({}),
    },
  });

  const emit = defineEmits(['navigate']);

  const route = useRoute();

  // 当前层级已展开的子菜单 menuId 集合
  const expandedKeys = ref(new Set());

  // 过滤分组标签（menuType===0）及隐藏/禁用的菜单
  const visibleChildren = computed(() =>
    (props.menu.children || []).filter((c) => c.menuType !== 0 && c.visibleFlag && !c.disabledFlag)
  );

  function hasChildren(menu) {
    return (menu.children || []).some((c) => c.menuType !== 0 && c.visibleFlag && !c.disabledFlag);
  }

  function toggle(menuId) {
    const next = new Set(expandedKeys.value);
    if (next.has(menuId)) {
      next.delete(menuId);
    } else {
      next.add(menuId);
    }
    expandedKeys.value = next;
  }

  function isActive(menu) {
    if (!menu) return false;
    const path = menu.path || menu.menuUrl;
    if (path && (route.path === path || route.path.startsWith(path + '/'))) return true;
    return (menu.children || []).some((c) => isActive(c));
  }

  function onNavigate(menu) {
    emit('navigate', menu);
  }

  // 子层递归组件触发时向上转发
  function onNavigateChild(menu) {
    emit('navigate', menu);
  }
</script>
<style lang="less" scoped>
  .sub-menu-tree {
    padding: 2px 0;

    .sub-item {
      display: flex;
      align-items: center;
      gap: 8px;
      height: 34px;
      padding: 0 10px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 13px;
      color: #595959;
      transition: all 0.2s;
      user-select: none;

      &:hover {
        background: rgba(24, 144, 255, 0.08);
        color: #1890ff;
      }

      &.active {
        background: rgba(24, 144, 255, 0.12);
        color: #1890ff;
        font-weight: 500;
      }

      &.has-children {
        &.expanded {
          color: #262626;
          background: #f5f5f5;
        }
      }

      .sub-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 18px;
        height: 18px;
        flex-shrink: 0;

        .sub-icon-comp {
          font-size: 14px;
        }
      }

      .sub-label {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .sub-arrow {
        font-size: 10px;
        color: #bfbfbf;
        transition: transform 0.2s;
        flex-shrink: 0;
      }

      &.expanded {
        .sub-arrow {
          transform: rotate(180deg);
        }
      }
    }

    .sub-children {
      padding-left: 14px;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 8px;
        top: 0;
        bottom: 0;
        width: 1px;
        background: #f0f0f0;
      }
    }
  }

  /* 暗黑模式适配 */
  :global([data-theme='dark']) {
    .sub-item {
      color: rgba(255, 255, 255, 0.65);

      &:hover {
        background: rgba(24, 144, 255, 0.2);
        color: #40a9ff;
      }

      &.active {
        background: rgba(24, 144, 255, 0.25);
        color: #40a9ff;
      }

      &.has-children.expanded {
        color: rgba(255, 255, 255, 0.85);
        background: #262626;
      }

      .sub-arrow {
        color: rgba(255, 255, 255, 0.35);
      }
    }

    .sub-children {
      &::before {
        background: #303030;
      }
    }
  }
</style>
