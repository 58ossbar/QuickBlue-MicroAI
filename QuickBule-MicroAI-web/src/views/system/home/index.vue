<template>
  <div class="home-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1 class="banner-title">欢迎使用 QuickBlue</h1>
        <p class="banner-subtitle"><span class="cloud-native-tag">企业级应用底座 · 开源版</span></p>
        <div class="banner-slogan">
          <p class="slogan-main">一个平台，整合技术，更整合智能。交付更快，运行更稳。</p>
          <p class="slogan-sub">—— 面向微服务架构的 AI 原生应用加速器</p>
        </div>
      </div>
      <div class="banner-icon">
        <CloudServerOutlined class="cloud-server-icon" />
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <a-row :gutter="[16, 16]" style="margin-top: 24px;">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card primary" hoverable>
          <div class="metric-content">
            <div class="metric-icon">
              <AppstoreOutlined />
            </div>
            <div class="metric-info">
              <div class="metric-title">微服务数量</div>
              <div class="metric-value">{{ metrics.serviceCount }}</div>
              <div class="metric-trend up">↑ 2 新增</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card success" hoverable>
          <div class="metric-content">
            <div class="metric-icon">
              <ApiOutlined />
            </div>
            <div class="metric-info">
              <div class="metric-title">API 接口总数</div>
              <div class="metric-value">{{ metrics.apiCount }}</div>
              <div class="metric-trend up">↑ 12 新增</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card warning" hoverable>
          <div class="metric-content">
            <div class="metric-icon">
              <DatabaseOutlined />
            </div>
            <div class="metric-info">
              <div class="metric-title">数据表总数</div>
              <div class="metric-value">{{ metrics.tableCount }}</div>
              <div class="metric-trend">稳定运行</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="metric-card info" hoverable>
          <div class="metric-content">
            <div class="metric-icon">
              <SafetyOutlined />
            </div>
            <div class="metric-info">
              <div class="metric-title">系统健康度</div>
              <div class="metric-value">{{ metrics.health }}</div>
              <div class="metric-trend status-online">运行正常</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 微服务架构图 -->
    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :span="24">
        <a-card title="微服务架构" class="chart-card" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="goMonitorDashboard">
                <template #icon><DashboardOutlined /></template>
                监控大屏
              </a-button>
              <a-button type="primary" @click="openMicroServicePortal">
                <template #icon><LinkOutlined /></template>
                微服务文档入口
              </a-button>
            </a-space>
          </template>
          <MicroserviceArchitecture />
        </a-card>
      </a-col>
    </a-row>
       <!-- 数据库支持与最新动态 -->
    <a-row :gutter="[16, 16]" style="margin-top: 16px;" class="align-cards-row">
      <a-col :xs="24" :lg="12" class="align-card-col">
        <a-card title="数据库支持" class="chart-card" :bordered="false">
          <div class="database-grid">
            <div class="database-item" v-for="database in databases" :key="database.name">
              <div class="database-icon">
                <component :is="database.icon" />
              </div>
              <div class="database-info">
                <div class="database-header">
                  <div class="database-name">{{ database.name }}</div>
                  <a-tag :color="database.status === 'online' ? 'success' : 'error'" size="small">
                    {{ database.statusText }}
                  </a-tag>
                </div>
                <div class="database-details">
                  <div class="database-detail-item">
                    <span class="detail-label">版本</span>
                    <span class="detail-value">{{ database.version }}</span>
                  </div>
                  <div class="database-detail-item">
                    <span class="detail-label">连接数</span>
                    <span class="detail-value">{{ database.connections }}</span>
                  </div>
                </div>
                <div class="database-features">
                  <a-tag v-for="feature in database.features" :key="feature" color="default" size="small">
                    {{ feature }}
                  </a-tag>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12" class="align-card-col">
        <a-card title="最新动态" class="chart-card" :bordered="false">
          <a-timeline class="activity-timeline">
            <a-timeline-item v-for="activity in activities" :key="activity.id" :color="activity.color">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-desc">{{ activity.desc }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { homeApi } from '/@/api/system/home-api';
import { message } from 'ant-design-vue';
import MicroserviceArchitecture from '/@/components/microservice-architecture.vue';
import {
  CloudServerOutlined,
  AppstoreOutlined,
  ApiOutlined,
  DatabaseOutlined,
  SafetyOutlined,
  UserOutlined,
  SettingOutlined,
  CodeOutlined,
  BarChartOutlined,
  FileTextOutlined,
  RobotOutlined,
  CloudOutlined,
  RocketOutlined,
  TeamOutlined,
  LockOutlined,
  ReloadOutlined,
  BellOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  ClockCircleOutlined,
  LinkOutlined,
  DashboardOutlined
} from '@ant-design/icons-vue';

const router = useRouter();

// 核心指标
const metrics = ref({
  serviceCount: '5',
  apiCount: '156',
  tableCount: '48',
  health: '98%'
});

// 微服务列表
const services = ref([
  {
    name: 'System 服务',
    icon: LockOutlined,
    status: 'online',
    statusText: '运行中',
    requests: '12,456',
    responseTime: '23ms'
  },
  {
    name: 'Business 服务',
    icon: AppstoreOutlined,
    status: 'online',
    statusText: '运行中',
    requests: '8,234',
    responseTime: '31ms'
  },
  {
    name: 'Support 服务',
    icon: TeamOutlined,
    status: 'online',
    statusText: '运行中',
    requests: '5,678',
    responseTime: '28ms'
  },
  {
    name: 'AI 服务',
    icon: RobotOutlined,
    status: 'online',
    statusText: '运行中',
    requests: '3,421',
    responseTime: '156ms'
  }
]);

// 快捷操作
const quickActions = ref([
  { title: '用户管理', desc: '管理系统用户', path: '/system/employee/employee-list', icon: UserOutlined },
  { title: '角色权限', desc: '配置角色权限', path: '/system/role/role-list', icon: SafetyOutlined },
  { title: '菜单管理', desc: '系统菜单配置', path: '/system/menu/menu-list', icon: SettingOutlined },
  { title: 'AI 应用', desc: 'AI 应用管理', path: '/ai/app/app-list', icon: RobotOutlined },
  { title: '知识库', desc: '知识库管理', path: '/ai/knowledge/knowledge-list', icon: DatabaseOutlined }
]);

// 最新动态
const activities = ref([
  { id: 1, title: '系统升级完成', desc: '升级至 Spring Cloud Alibaba 2025', time: '10 分钟前', color: 'blue' },
  { id: 2, title: '新功能上线', desc: 'AI 智能体模块已上线', time: '30 分钟前', color: 'blue' },
  { id: 3, title: '性能优化', desc: 'Gateway 网关性能提升 40%', time: '1 小时前', color: 'green' },
  { id: 4, title: '安全更新', desc: '修复 3 个安全漏洞', time: '2 小时前', color: 'orange' }
]);

// 技术栈
const techStack = ref([
  { name: 'Spring Boot 3.5.10', color: 'green' },
  { name: 'Spring Cloud Alibaba 2025', color: 'blue' },
  { name: 'Vue 3.4.27', color: 'cyan' },
  { name: 'Ant Design Vue 4.2.5', color: 'red' },
  { name: 'Nacos 2.4.3', color: 'purple' },
  { name: 'Redisson 3.50.0', color: 'orange' },
  { name: 'MyBatis-Plus 3.5.7', color: 'geekblue' },
  { name: 'Sa-Token 1.44.0', color: 'magenta' },
  { name: 'PostgreSQL 18', color: 'cyan' },
  { name: 'Java 21', color: 'volcano' },
  { name: 'Vite 5.2.12', color: 'gold' }
]);

// 数据库支持
const databases = ref([
  {
    name: 'MySQL',
    icon: DatabaseOutlined,
    status: 'online',
    statusText: '运行中',
    version: '8.0.33',
    connections: '15/100',
    features: ['事务支持', '主从复制', '分库分表', '全文索引']
  },
  {
    name: 'PostgreSQL',
    icon: DatabaseOutlined,
    status: 'online',
    statusText: '运行中',
    version: '16.0',
    connections: '8/100',
    features: ['JSON支持', '全文搜索', 'GIS支持', '窗口函数']
  }
]);

const handleQuickAction = (item) => {
  router.push(item.path);
};

// 跳转到服务监控大屏
const goMonitorDashboard = () => {
  router.push('/support/admin-monitor');
};

// 打开微服务入口
const openMicroServicePortal = () => {
  window.open('http://localhost:8080', '_blank');
};

// 获取微服务统计数据
const fetchMicroServiceStatistics = async () => {
  // 暂时禁用接口调用，使用静态数据
  // 后端接口未实现时，避免404错误
  // try {
  //   const response = await homeApi.microServiceStatistics();
  //   if (response && response.data) {
  //     metrics.value.serviceCount = response.data.serviceCount || metrics.value.serviceCount;
  //     metrics.value.apiCount = response.data.apiCount || metrics.value.apiCount;
  //     metrics.value.tableCount = response.data.tableCount || metrics.value.tableCount;
  //     metrics.value.health = response.data.health || metrics.value.health;
  //   }
  // } catch (error) {
  //   console.log('获取微服务统计数据失败，使用默认数据:', error);
  //   // 使用默认数据，不提示错误
  // }
};

// 页面加载时获取数据
onMounted(() => {
  // fetchMicroServiceStatistics();
});
</script>

<style scoped>
.home-container {
  padding: 16px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.welcome-banner {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #262626;
  border: 1px solid #f0f0f0;

  .banner-content {
    flex: 1;
  }

  .banner-title {
    font-size: 28px;
    font-weight: 600;
    margin: 0 0 10px 0;
  }

  .banner-subtitle {
    font-size: 16px;
    opacity: 0.9;
    margin: 0;

    .cloud-native-tag {
      display: inline-block;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #667eea 100%);
      background-size: 200% 100%;
      color: white;
      padding: 4px 16px;
      border-radius: 6px;
      font-weight: 500;
      animation: gradientFlow 3s ease infinite;
    }
  }

  .banner-slogan {
    margin-top: 14px;

    .slogan-main {
      font-size: 15px;
      color: #595959;
      margin: 0;
    }

    .slogan-sub {
      font-size: 13px;
      color: #8c8c8c;
      margin: 6px 0 0 0;
    }
  }

  .banner-icon {
    font-size: 64px;
    opacity: 1;
  }

  .cloud-server-icon {
    color: #764ba2;
    animation: pulse 2s ease-in-out infinite;
  }
}

@keyframes gradientFlow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
}

.metric-card {
  height: 100%;

  .metric-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .metric-icon {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #1890ff;
    background: #e6f7ff;
  }

  &.primary .metric-icon {
    background: #e6f7ff;
    color: #1890ff;
  }

  &.success .metric-icon {
    background: #f6ffed;
    color: #52c41a;
  }

  &.warning .metric-icon {
    background: #fffbe6;
    color: #faad14;
  }

  &.info .metric-icon {
    background: #f9f0ff;
    color: #722ed1;
  }

  .metric-info {
    flex: 1;
  }

  .metric-title {
    font-size: 14px;
    color: #8c8c8c;
    margin-bottom: 4px;
  }

  .metric-value {
    font-size: 24px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 4px;
  }

  .metric-trend {
    font-size: 12px;
    color: #8c8c8c;

    &.up {
      color: #52c41a;
    }

    &.status-online {
      color: #52c41a;
    }
  }
}

.chart-card {
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.ant-card-body) {
    flex: 1;
  }

  .ant-card-head {
    border-bottom: 1px solid #f0f0f0;
  }
}

.align-cards-row {
  display: flex;
  align-items: stretch;
}

.align-card-col {
  display: flex;
  flex-direction: column;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
}

.service-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;

  &:hover {
    background: #f0f2f5;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border-color: #d9d9d9;
  }

  .service-icon {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #1890ff;
    background: #e6f7ff;
    margin-right: 16px;
  }

  .service-info {
    flex: 1;
  }

  .service-name {
    font-size: 16px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 6px;
  }

  .service-status {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #8c8c8c;

    .status-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      margin-right: 6px;

      &.online {
        background: #52c41a;
      }

      &.offline {
        background: #ff4d4f;
      }
    }

    &.online {
      color: #52c41a;
    }
  }

  .service-metrics {
    display: flex;
    gap: 24px;
  }

  .service-metric {
    text-align: right;

    .metric-label {
      font-size: 12px;
      color: #8c8c8c;
      display: block;
      margin-bottom: 4px;
    }

    .metric-value {
      font-size: 14px;
      font-weight: 600;
      color: #262626;
    }
  }
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.quick-action-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;

  &:hover {
    background: #f0f2f5;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-color: #d9d9d9;
  }

  .quick-action-icon {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #1890ff;
    background: #e6f7ff;
    margin-right: 16px;
  }

  .quick-action-info {
    flex: 1;
  }

  .quick-action-title {
    font-size: 16px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 6px;
  }

  .quick-action-desc {
    font-size: 13px;
    color: #8c8c8c;
  }
}

.activity-title {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #bfbfbf;
}

.activity-timeline {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tech-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tech-tag {
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 4px;
  margin: 4px;
}

.database-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.database-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    background: #f0f2f5;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.database-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #1890ff;
  background: #e6f7ff;
  flex-shrink: 0;
}

.database-info {
  flex: 1;
}

.database-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.database-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.database-details {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.database-detail-item {
  .detail-label {
    font-size: 12px;
    color: #8c8c8c;
    display: block;
    margin-bottom: 4px;
  }

  .detail-value {
    font-size: 14px;
    font-weight: 500;
    color: #262626;
  }
}

.database-features {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>
