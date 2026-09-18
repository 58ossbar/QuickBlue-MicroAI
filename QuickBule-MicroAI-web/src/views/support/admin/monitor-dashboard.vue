<template>
  <div class="monitor-dashboard">
    <!-- 概览统计卡片 -->
    <a-row :gutter="16" class="overview-row">
      <a-col :span="4">
        <a-card class="stat-card stat-total" :loading="loading">
          <div class="stat-body">
            <div class="stat-icon">
              <ClusterOutlined />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalCount }}</div>
              <div class="stat-label">服务总数</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card stat-up" :loading="loading">
          <div class="stat-body">
            <div class="stat-icon">
              <CheckCircleOutlined />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.upCount }}</div>
              <div class="stat-label">正常运行</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card stat-down" :loading="loading">
          <div class="stat-body">
            <div class="stat-icon">
              <CloseCircleOutlined />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.downCount }}</div>
              <div class="stat-label">异常服务</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card stat-qps" :loading="loading">
          <div class="stat-body">
            <div class="stat-icon">
              <RiseOutlined />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatCount(totalRequests) }}</div>
              <div class="stat-label">总请求量</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card stat-memory" :loading="loading">
          <div class="stat-body">
            <div class="stat-icon">
              <CloudServerOutlined />
            </div>
              <div class="stat-info">
              <div class="stat-value">{{ overview.totalMemoryUsed ? formatBytes(overview.totalMemoryUsed) : '--' }}</div>
              <div class="stat-label">JVM 总内存</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="stat-card stat-refresh" :loading="loading">
          <div class="stat-body">
            <div class="stat-icon">
              <ReloadOutlined :spin="refreshing" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ refreshCountdown }}s</div>
              <div class="stat-label">
                自动刷新
                <a-switch v-model:checked="autoRefresh" size="small" style="margin-left: 8px" />
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 颜色图例 -->
    <a-alert type="info" show-icon class="legend-alert">
      <template #message>
        <span class="legend-title">颜色说明：</span>
        <span class="legend-item"><span class="legend-dot" style="background:#1677ff"></span>蓝色：服务总数</span>
        <a-divider type="vertical" />
        <span class="legend-item"><span class="legend-dot" style="background:#52c41a"></span>绿色：正常运行</span>
        <a-divider type="vertical" />
        <span class="legend-item"><span class="legend-dot" style="background:#ff4d4f"></span>红色：异常告警</span>
        <a-divider type="vertical" />
        <span class="legend-item"><span class="legend-dot" style="background:#722ed1"></span>紫色：总 QPS</span>
        <a-divider type="vertical" />
        <span class="legend-item"><span class="legend-dot" style="background:#faad14"></span>橙色：自动刷新</span>
      </template>
    </a-alert>

    <!-- 微服务健康状态网格 -->
    <a-card title="微服务运行状态" class="service-card" :loading="loading">
      <template #extra>
        <a-button type="primary" size="small" @click="doRefresh" :loading="refreshing">
          <ReloadOutlined /> 立即刷新
        </a-button>
      </template>
      <a-row :gutter="[16, 16]">
        <a-col :span="8" v-for="svc in services" :key="svc.serviceName">
          <div class="service-item" :class="'service-' + (svc.status === 'UP' ? 'up' : 'down')">
            <div class="service-header">
              <div class="service-status-dot" :class="svc.status === 'UP' ? 'dot-up' : 'dot-down'"></div>
              <div class="service-name">{{ svc.serviceName }}</div>
              <a-tag :color="svc.status === 'UP' ? 'success' : 'error'" class="service-tag">
                {{ svc.status === 'UP' ? '正常' : '异常' }}
              </a-tag>
            </div>
            <div class="service-addr">
              <EnvironmentOutlined /> {{ svc.instanceHost }}:{{ svc.instancePort }}
            </div>
            <div class="service-qps">
              <RiseOutlined /> 请求量: {{ formatCount(svc.totalRequests) }}
            </div>
            <div class="service-memory" v-if="svc.memoryUsedBytes">
              <CloudServerOutlined /> 内存: {{ formatBytes(svc.memoryUsedBytes) }}<template v-if="svc.memoryMaxBytes"> / {{ formatBytes(svc.memoryMaxBytes) }}</template>
            </div>
            <div class="service-error" v-if="svc.error">
              <a-tooltip :title="svc.error">
                <ExclamationCircleOutlined style="color: #ff4d4f" />
                <span class="error-text">{{ svc.error }}</span>
              </a-tooltip>
            </div>
            <!-- 健康检查组件详情 -->
            <div class="service-components" v-if="svc.details && Object.keys(svc.details).length > 0">
              <a-divider style="margin: 8px 0; font-size: 12px;">组件健康</a-divider>
              <div class="component-grid">
                <div v-for="(detail, compName) in svc.details" :key="compName" class="comp-item">
                  <span class="comp-dot" :class="detail.status === 'UP' ? 'dot-up' : 'dot-down'"></span>
                  <span class="comp-name">{{ getCompDisplayName(compName) }}</span>
                </div>
              </div>
            </div>
            <div class="service-actions">
              <a-button type="link" size="small" @click="showServiceDetail(svc)">
                查看详情
              </a-button>
            </div>
          </div>
        </a-col>
      </a-row>
    </a-card>

    <!-- 服务详情抽屉 -->
    <a-drawer
      :title="'微服务详情 - ' + selectedService?.serviceName"
      :open="drawerVisible"
      :width="640"
      @close="drawerVisible = false"
      destroyOnClose
    >
      <template v-if="selectedService">
        <a-descriptions bordered :column="1" size="small" class="detail-desc">
          <a-descriptions-item label="服务名称">{{ selectedService.serviceName }}</a-descriptions-item>
          <a-descriptions-item label="运行状态">
            <a-tag :color="selectedService.status === 'UP' ? 'success' : 'error'">
              {{ selectedService.status === 'UP' ? '正常' : '异常' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="实例地址">
            {{ selectedService.instanceHost }}:{{ selectedService.instancePort }}
          </a-descriptions-item>
        </a-descriptions>

        <a-divider>JVM 运行时指标</a-divider>
        <a-spin :spinning="metricsLoading">
          <div v-if="metricsData" class="metrics-section">
            <!-- 内存 -->
            <a-card size="small" title="内存使用" class="metrics-card">
              <a-progress
                v-if="memoryUsagePercent > 0"
                :percent="memoryUsagePercent"
                :stroke-color="memoryUsagePercent > 80 ? '#ff4d4f' : '#52c41a'"
              />
              <div v-if="memoryUsagePercent > 0" class="memory-detail">
                {{ formatMemory(metricsData.memory) }}
              </div>
              <div v-else class="no-data">暂无数据</div>
            </a-card>
            <!-- QPS -->
            <a-card size="small" title="请求统计" class="metrics-card">
              <div class="metric-row" v-if="selectedQps !== null">
                <span>平均 QPS</span>
                <span class="metric-value">{{ formatQps(selectedQps) }}</span>
              </div>
              <div class="metric-row" v-if="selectedTotalRequests !== null">
                <span>累计请求</span>
                <span class="metric-value">{{ formatCount(selectedTotalRequests) }}</span>
              </div>
              <div v-else class="no-data">暂无数据</div>
            </a-card>
            <!-- 线程 -->
            <a-card size="small" title="线程" class="metrics-card">
              <div class="metric-row" v-if="threadsLive !== null">
                <span>活跃线程</span>
                <span class="metric-value">{{ threadsLive }}</span>
              </div>
              <div class="metric-row" v-if="threadsPeak !== null">
                <span>峰值线程</span>
                <span class="metric-value">{{ threadsPeak }}</span>
              </div>
              <div v-else class="no-data">暂无数据</div>
            </a-card>
            <!-- 运行时长 -->
            <a-card size="small" title="运行时长" class="metrics-card">
              <div class="metric-row" v-if="uptimeSeconds > 0">
                <span>进程运行时间</span>
                <span class="metric-value">{{ formatUptime(uptimeSeconds) }}</span>
              </div>
              <div v-else class="no-data">暂无数据</div>
            </a-card>
          </div>
          <a-empty v-else-if="!metricsLoading" description="无法获取 JVM 指标" />
        </a-spin>
      </template>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue';
import { message } from 'ant-design-vue';
import {
  ClusterOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ReloadOutlined,
  RiseOutlined,
  EnvironmentOutlined,
  ExclamationCircleOutlined,
  CloudServerOutlined,
} from '@ant-design/icons-vue';
import { getServicesOverview, getServiceMetrics } from '/@/api/support/monitor-api';

// ===== 概览数据 =====
const loading = ref(false);
const refreshing = ref(false);
const overview = reactive({
  totalCount: 0,
  upCount: 0,
  downCount: 0,
  totalMemoryUsed: null,
  totalMemoryMax: null,
  timestamp: 0,
});
const services = ref([]);

// ===== 自动刷新 =====
const autoRefresh = ref(true);
const REFRESH_INTERVAL = 15; // 秒
const refreshCountdown = ref(REFRESH_INTERVAL);
let refreshTimer = null;
let countdownTimer = null;

// ===== 服务详情抽屉 =====
const drawerVisible = ref(false);
const selectedService = ref(null);
const metricsLoading = ref(false);
const metricsData = ref(null);

// ===== 组件名称中英文映射 =====
const COMPONENT_NAME_MAP = {
  diskSpace: '磁盘空间',
  db: '数据库',
  redis: 'Redis',
  rabbit: 'RabbitMQ',
  mongo: 'MongoDB',
  elasticsearch: 'ES搜索',
  mail: '邮件服务',
  ping: '网络连通',
  livenessState: '存活探针',
  readinessState: '就绪探针',
  discoveryComposite: '服务发现',
  diskSpaceHealth: '磁盘监控',
  hystrix: '断路器',
  consul: 'Consul',
  configServer: '配置中心',
  refreshScope: '配置刷新',
};

const getCompDisplayName = (name) => COMPONENT_NAME_MAP[name] || name;

// ===== 计算属性 =====
const memoryUsagePercent = computed(() => {
  if (!metricsData.value?.memory) return 0;
  const usedArr = metricsData.value.memory.used;
  const maxArr = metricsData.value.memory.max;
  if (!usedArr?.length || !maxArr?.length) return 0;
  const usedVal = usedArr[0]?.value;
  const maxVal = maxArr[0]?.value;
  if (!maxVal) return 0;
  return Math.round((usedVal / maxVal) * 100);
});

const threadsLive = computed(() => {
  if (!metricsData.value?.threads?.live?.length) return null;
  return metricsData.value.threads.live[0]?.value ?? null;
});

const threadsPeak = computed(() => {
  if (!metricsData.value?.threads?.peak?.length) return null;
  return metricsData.value.threads.peak[0]?.value ?? null;
});

const uptimeSeconds = computed(() => {
  if (!metricsData.value?.uptime?.length) return 0;
  return metricsData.value.uptime[0]?.value ?? 0;
});

const totalRequests = computed(() => {
  if (!services.value?.length) return 0;
  return services.value.reduce((sum, svc) => sum + (svc.totalRequests ?? 0), 0);
});

const selectedQps = computed(() => {
  if (!metricsData.value) return null;
  return metricsData.value.qps ?? null;
});

const selectedTotalRequests = computed(() => {
  if (!metricsData.value) return null;
  return metricsData.value.totalRequests ?? null;
});

// ===== 数据加载 =====
async function fetchOverview() {
  loading.value = true;
  try {
    const res = await getServicesOverview();
    if (res?.ok && res?.data) {
      overview.totalCount = res.data.totalCount ?? 0;
      overview.upCount = res.data.upCount ?? 0;
      overview.downCount = res.data.downCount ?? 0;
      overview.totalMemoryUsed = res.data.totalMemoryUsed ?? null;
      overview.totalMemoryMax = res.data.totalMemoryMax ?? null;
      overview.timestamp = res.data.timestamp ?? Date.now();
      services.value = res.data.services ?? [];
    }
  } catch (e) {
    console.error('获取监控概览失败:', e);
    message.error('获取服务监控数据失败，请检查各服务是否正常运行');
    // 保留上一次数据，不清空
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
}

async function doRefresh() {
  refreshing.value = true;
  resetCountdown();
  await fetchOverview();
}

// ===== 服务详情 =====
async function showServiceDetail(svc) {
  selectedService.value = svc;
  drawerVisible.value = true;
  metricsData.value = null;
  metricsLoading.value = true;

  try {
    const res = await getServiceMetrics(svc.serviceName);
    if (res?.ok && res?.data) {
      metricsData.value = res.data;
    }
  } catch (e) {
    console.error('获取 JVM 指标失败:', e);
  } finally {
    metricsLoading.value = false;
  }
}

// ===== 格式化工具 =====
function formatMemory(memory) {
  if (!memory?.used?.length) return '-- / --';
  const used = memory.used[0]?.value ?? 0;
  const max = memory.max[0]?.value ?? 0;
  return `${formatBytes(used)} / ${formatBytes(max)}`;
}

function formatBytes(bytes) {
  if (!bytes || bytes <= 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return (bytes / Math.pow(1024, i)).toFixed(i > 0 ? 1 : 0) + ' ' + units[i];
}

function formatQps(value) {
  if (value === null || value === undefined || Number.isNaN(value)) return '0';
  if (value < 1000) return value.toFixed(1);
  if (value < 1000000) return (value / 1000).toFixed(1) + 'K';
  return (value / 1000000).toFixed(1) + 'M';
}

function formatCount(value) {
  if (value === null || value === undefined) return '0';
  if (typeof value !== 'number') return String(value);
  if (value < 1000) return String(value);
  if (value < 1000000) return (value / 1000).toFixed(1) + 'K';
  return (value / 1000000).toFixed(1) + 'M';
}

function formatUptime(seconds) {
  if (!seconds || seconds <= 0) return '--';
  const d = Math.floor(seconds / 86400);
  const h = Math.floor((seconds % 86400) / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  const parts = [];
  if (d > 0) parts.push(`${d}天`);
  if (h > 0) parts.push(`${h}时`);
  if (m > 0) parts.push(`${m}分`);
  parts.push(`${s}秒`);
  return parts.join(' ');
}

// ===== 定时刷新 =====
function startAutoRefresh() {
  stopAutoRefresh();
  if (!autoRefresh.value) return;

  refreshCountdown.value = REFRESH_INTERVAL;
  countdownTimer = setInterval(() => {
    refreshCountdown.value--;
    if (refreshCountdown.value <= 0) {
      resetCountdown();
      fetchOverview();
    }
  }, 1000);
}

function stopAutoRefresh() {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
  if (refreshTimer) {
    clearTimeout(refreshTimer);
    refreshTimer = null;
  }
}

function resetCountdown() {
  refreshCountdown.value = REFRESH_INTERVAL;
}

watch(autoRefresh, (val) => {
  if (val) startAutoRefresh();
  else stopAutoRefresh();
});

// ===== 生命周期 =====
onMounted(() => {
  fetchOverview();
  startAutoRefresh();
});

onUnmounted(() => {
  stopAutoRefresh();
});
</script>

<style scoped lang="less">
.monitor-dashboard {
  padding: 0;

  .overview-row {
    margin-bottom: 16px;
  }

  .legend-alert {
    margin-bottom: 16px;
    border-radius: 8px;

    .legend-title {
      font-weight: 600;
      margin-right: 4px;
    }

    .legend-item {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
    }

    .legend-dot {
      display: inline-block;
      width: 10px;
      height: 10px;
      border-radius: 50%;
      flex-shrink: 0;
    }
  }

  .stat-card {
    border-radius: 8px;

    &.stat-total {
      border-left: 4px solid @primary-color;
    }
    &.stat-up {
      border-left: 4px solid #52c41a;
    }
    &.stat-down {
      border-left: 4px solid #ff4d4f;
    }
    &.stat-qps {
      border-left: 4px solid #722ed1;
    }
    &.stat-memory {
      border-left: 4px solid #13c2c2;
    }
    &.stat-refresh {
      border-left: 4px solid #faad14;
    }

    :deep(.ant-card-body) {
      padding: 20px 24px;
    }

    .stat-body {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        font-size: 36px;
        flex-shrink: 0;
      }

      .stat-total & .stat-icon { color: @primary-color; }
      .stat-up & .stat-icon { color: #52c41a; }
      .stat-down & .stat-icon { color: #ff4d4f; }
      .stat-qps & .stat-icon { color: #722ed1; }
      .stat-memory & .stat-icon { color: #13c2c2; }
      .stat-refresh & .stat-icon { color: #faad14; }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: 700;
          line-height: 1.2;
          color: #262626;
        }
        .stat-label {
          font-size: 13px;
          color: #8c8c8c;
          display: flex;
          align-items: center;
        }
      }
    }
  }

  .service-card {
    border-radius: 8px;

    :deep(.ant-card-head) {
      border-bottom: 1px solid #f0f0f0;
    }
  }

  .service-item {
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    padding: 16px;
    transition: all 0.3s;
    height: 100%;
    background: #fff;

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    }

    &.service-up {
      border-left: 3px solid #52c41a;
    }

    &.service-down {
      border-left: 3px solid #ff4d4f;
      background: #fff7f7;
    }

    .service-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .service-name {
        font-weight: 600;
        font-size: 14px;
        flex: 1;
        color: #262626;
      }
    }

    .service-status-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      flex-shrink: 0;

      &.dot-up {
        background: #52c41a;
        box-shadow: 0 0 6px rgba(82, 196, 26, 0.4);
      }
      &.dot-down {
        background: #ff4d4f;
        box-shadow: 0 0 6px rgba(255, 77, 79, 0.4);
      }
    }

    .service-addr {
      color: #8c8c8c;
      font-size: 12px;
      margin-bottom: 4px;
    }

    .service-qps {
      color: #722ed1;
      font-size: 12px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .service-memory {
      color: #13c2c2;
      font-size: 12px;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .service-error {
      margin-top: 4px;
      .error-text {
        margin-left: 4px;
        color: #ff4d4f;
        font-size: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        display: inline-block;
        max-width: 200px;
        vertical-align: middle;
      }
    }

    .service-components {
      .component-grid {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .comp-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          padding: 2px 8px;
          background: #fafafa;
          border-radius: 4px;

          .comp-dot {
            width: 6px;
            height: 6px;
            border-radius: 50%;

            &.dot-up { background: #52c41a; }
            &.dot-down { background: #ff4d4f; }
          }

          .comp-name {
            color: #595959;
          }
        }
      }
    }

    .service-actions {
      margin-top: 8px;
      text-align: right;
      border-top: 1px solid #f5f5f5;
      padding-top: 8px;
    }
  }

  // 详情抽屉
  .detail-desc {
    margin-bottom: 16px;
  }

  .metrics-section {
    .metrics-card {
      margin-bottom: 12px;
      border-radius: 6px;

      :deep(.ant-card-head) {
        min-height: 32px;
        font-size: 13px;
        padding: 0 12px;
      }
      :deep(.ant-card-body) {
        padding: 12px;
      }
    }

    .metric-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 4px 0;
      font-size: 13px;

      .metric-value {
        font-weight: 600;
        color: #262626;
      }
    }

    .memory-detail {
      text-align: center;
      margin-top: 8px;
      font-size: 13px;
      color: #595959;
    }

    .no-data {
      color: #bfbfbf;
      text-align: center;
      padding: 8px;
      font-size: 13px;
    }
  }
}
</style>
