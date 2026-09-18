<!--
  * 角色 树形结构
  *

  *
-->
<template>
  <div>
    <div class="tree-header">
      <p>设置角色对应的功能操作、后台管理权限</p>
      <a-space>
        <a-button size="small" @click="expandAll">全部展开</a-button>
        <a-button size="small" @click="collapseAll">全部收起</a-button>
        <a-button v-if="selectRoleId" type="primary" @click="saveChange" v-privilege="'system:role:menu:update'"> 保存 </a-button>
      </a-space>
    </div>
    <!-- 功能权限勾选部分 -->
    <RoleTreeCheckbox :tree="tree" />
  </div>
</template>
<script setup>

import { inject, ref, watch, provide, reactive } from 'vue';
  import { message } from 'ant-design-vue';
  import _ from 'lodash';
  import RoleTreeCheckbox from './role-tree-checkbox.vue';
  import { roleMenuApi } from '/@/api/system/role-menu-api';
  import { useRoleStore } from '/@/store/modules/system/role';
  import { Loading } from '/@/components/framework/loading';
  import { sentry } from '/@/lib/sentry';

  let roleStore = useRoleStore();
  let tree = ref();
  let selectRoleId = inject('selectRoleId');

  // ---------- 折叠/展开状态管理 ----------
  const collapsedMap = reactive({});

  function toggleCollapse(menuId) {
    collapsedMap[menuId] = !collapsedMap[menuId];
  }

  function isCollapsed(menuId) {
    return !!collapsedMap[menuId];
  }

  // 收集所有有子节点的 menuId
  function collectCollapsibleIds(treeList) {
    const ids = [];
    function walk(list) {
      if (!list) return;
      list.forEach((node) => {
        if (node.children && node.children.length > 0) {
          ids.push(node.menuId);
          walk(node.children);
        }
      });
    }
    walk(treeList);
    return ids;
  }

  function expandAll() {
    collectCollapsibleIds(tree.value).forEach((id) => {
      delete collapsedMap[id];
    });
  }

  function collapseAll() {
    collectCollapsibleIds(tree.value).forEach((id) => {
      collapsedMap[id] = true;
    });
  }

  provide('toggleCollapse', toggleCollapse);
  provide('isCollapsed', isCollapsed);

  watch(selectRoleId, () => getRoleSelectedMenu(), {
    immediate: true,
  });

  async function getRoleSelectedMenu() {
    if (!selectRoleId.value) {
      return;
    }
    let res = await roleMenuApi.getRoleSelectedMenu(selectRoleId.value);
    let data = res.data;
    if (_.isEmpty(roleStore.treeMap)) {
      roleStore.initTreeMap(data.menuTreeList || []);
    }
    roleStore.initCheckedData(data.selectedMenuId || []);
    tree.value = data.menuTreeList;
  }
  async function saveChange() {
    let checkedData = roleStore.checkedData;
    if (_.isEmpty(checkedData)) {
      message.error('还未选择任何权限');
      return;
    }
    let params = {
      roleId: selectRoleId.value,
      menuIdList: checkedData,
    };
    Loading.show();
    try {
      await roleMenuApi.updateRoleMenu(params);
      message.success('保存成功');
    } catch (error) {
      sentry.captureError(error);
    } finally {
      Loading.hide();
    }
  }
</script>
<style scoped lang="less">
  @import 'index.less';
</style>
