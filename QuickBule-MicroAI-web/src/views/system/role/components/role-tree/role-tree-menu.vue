<!--
  * 角色 菜单
  *

  *
-->
<template>
  <li v-for="module in props.tree" :key="module.menuId">
    <div class="menu" :style="{ marginLeft: `${props.index * 4}%` }">
      <!-- 展开/收起箭头 -->
      <span
        v-if="hasChildren(module)"
        class="collapse-arrow"
        :class="{ collapsed: isCollapsed(module.menuId) }"
        @click.stop="handleToggle(module.menuId)"
      >
        <span class="arrow-icon"></span>
      </span>
      <span v-else class="collapse-placeholder"></span>

      <a-checkbox @change="selectCheckbox(module)" class="checked-box-label" :value="module.menuId">{{ module.menuName }} </a-checkbox>

      <!-- 功能点(操作按钮) - 收起时隐藏 -->
      <div v-if="!isCollapsed(module.menuId) && hasPoints(module)">
        <RoleTreePoint :tree="module.children" @selectCheckbox="selectCheckbox" />
      </div>
    </div>

    <!-- 子菜单 - 收起时隐藏 -->
    <template v-if="!isCollapsed(module.menuId) && hasSubMenus(module)">
      <RoleTreeMenu :tree="module.children" :index="props.index + 1" />
    </template>
  </li>
</template>
<script setup>
  import { inject } from 'vue';
  import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';
  import { useRoleStore } from '/@/store/modules/system/role';
  import RoleTreePoint from './role-tree-point.vue';
  import RoleTreeMenu from './role-tree-menu.vue';

  const props = defineProps({
    tree: {
      type: Array,
      default: () => [],
    },
    index: {
      type: Number,
      default: 0,
    },
  });
  defineEmits(['update:value']);

  const toggleCollapse = inject('toggleCollapse');
  const isCollapsed = inject('isCollapsed');

  function hasChildren(module) {
    return module.children && module.children.length > 0;
  }

  function hasPoints(module) {
    return module.children && module.children.some((e) => e.menuType === MENU_TYPE_ENUM.POINTS.value);
  }

  function hasSubMenus(module) {
    return module.children && !module.children.some((e) => e.menuType === MENU_TYPE_ENUM.POINTS.value);
  }

  function handleToggle(menuId) {
    toggleCollapse(menuId);
  }

  let roleStore = useRoleStore();
  function selectCheckbox(module) {
    if (!module.menuId) {
      return;
    }
    // 是否勾选
    let checkedData = roleStore.checkedData;
    let findIndex = checkedData.indexOf(module.menuId);
    // 选中
    if (findIndex === -1) {
      // 选中本级以及子级
      roleStore.addCheckedDataAndChildren(module);
      // 选中上级
      roleStore.selectUpperLevel(module);
      // 是否有关联菜单 有则选中
      if (module.contextMenuId) {
        roleStore.addCheckedData(module.contextMenuId);
      }
    } else {
      // 取消选中本级以及子级
      roleStore.deleteCheckedDataAndChildren(module);
    }
  }
</script>
