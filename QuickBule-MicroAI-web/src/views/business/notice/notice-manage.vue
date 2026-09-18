<!--
  * 通知公告统一管理页面
  * 整合了通知公告管理和我的通知功能
-->
<template>
  <a-card size="small" :bordered="false">
    <a-tabs v-model:activeKey="activeTab" @change="handleTabChange" size="small">
      <!-- 通知公告管理 -->
      <a-tab-pane key="manage" tab="通知公告管理">
        <NoticeManagement ref="manageRef" v-if="activeTab === 'manage'" />
      </a-tab-pane>

      <!-- 我的通知 -->
      <a-tab-pane key="my" tab="我的通知">
        <MyNotice ref="myNoticeRef" v-if="activeTab === 'my'" />
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script setup>
import { ref } from 'vue';
import NoticeManagement from './notice-list.vue';
import MyNotice from './notice-employee-list.vue';

const activeTab = ref('manage');
const manageRef = ref(null);
const myNoticeRef = ref(null);

function handleTabChange(key) {
  console.log('切换到标签:', key);
}
</script>
