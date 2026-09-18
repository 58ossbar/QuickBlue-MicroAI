<template>
  <div class="admin-monitor-page">
    <a-page-header
      title="微服务监控中心"
      sub-title="融合 Gateway 入口 · 实时服务健康监控"
      @back="() => $router.back()"
    >
      <template #extra>
        <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
          <a-radio-button value="native">
            <DashboardOutlined /> 监控大屏
          </a-radio-button>
          <a-radio-button value="sba">
            <LinkOutlined /> Spring Boot Admin
          </a-radio-button>
        </a-radio-group>
      </template>
    </a-page-header>

    <!-- 原生监控大屏 -->
    <MonitorDashboard v-if="viewMode === 'native'" />

    <!-- Spring Boot Admin 外部 iframe -->
    <div v-else class="sba-container">
      <a-alert
        type="info"
        show-icon
        closable
        message="Spring Boot Admin 独立 Dashboard"
        description="SBA 运行在端口 9090，提供更丰富的管理功能（日志级别、环境变量、线程 Dump 等），适合运维人员使用。日常监控推荐使用「监控大屏」视图。"
        style="margin-bottom: 16px;"
      />
      <iframe
        :src="sbaUrl"
        class="sba-iframe"
        frameborder="0"
        title="Spring Boot Admin"
      ></iframe>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { DashboardOutlined, LinkOutlined } from '@ant-design/icons-vue';
import MonitorDashboard from './monitor-dashboard.vue';

const viewMode = ref('native');
const sbaUrl = ref('http://localhost:9090');
</script>

<style scoped lang="less">
.admin-monitor-page {
  :deep(.ant-page-header) {
    padding: 12px 0 16px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 16px;
  }

  .sba-container {
    .sba-iframe {
      width: 100%;
      height: calc(100vh - 180px);
      min-height: 600px;
      border-radius: 8px;
      border: 1px solid #f0f0f0;
    }
  }
}
</style>
