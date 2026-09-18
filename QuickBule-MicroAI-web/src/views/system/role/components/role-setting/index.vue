<!--
  * 角色 设置
  *

  *
-->
<template>
  <a-card class="role-container">
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane key="1" tab="角色-功能权限">
        <RoleTree />
      </a-tab-pane>
      <a-tab-pane key="2" tab="角色-数据范围">
        <RoleDataScope />
      </a-tab-pane>
      <a-tab-pane key="3" tab="角色-员工列表">
        <RoleEmployeeList />
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>
<script setup>
  import { ref } from 'vue';
  import RoleDataScope from '../role-data-scope/index.vue';
  import RoleEmployeeList from '../role-employee-list/index.vue';
  import RoleTree from '../role-tree/index.vue';

  defineProps({
    value: [Number, String],
  });

  defineEmits(['update:value']);

  let activeKey = ref();
</script>
