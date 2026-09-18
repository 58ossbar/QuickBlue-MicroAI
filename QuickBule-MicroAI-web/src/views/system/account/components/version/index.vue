<template>
  <div class="version-container">
    <!-- 页面标题 -->
    <div class="header-section">
      <div class="header-title">
        <InfoCircleOutlined class="title-icon" />
        版本说明
      </div>
      <div class="header-actions">
        <a-tag color="blue">v{{ currentVersion }}</a-tag>
      </div>
    </div>

    <!-- 版本信息 -->
    <div class="version-content">
      <a-row :gutter="24">
        <a-col :span="8">
          <a-card class="version-card" :bordered="false" title="当前版本">
            <div class="version-info">
              <div class="version-number">v{{ currentVersion }}</div>
              <div class="version-date">{{ releaseDate }}</div>
              <a-divider />
              <div class="version-meta">
                <p><EnvironmentOutlined /> 系统：QuickBlue</p>
                <p><TeamOutlined /> 开发：布道师团队</p>
                <p><CopyrightOutlined /> 版权：© 2024 Budaos</p>
              </div>
            </div>
          </a-card>
        </a-col>

        <a-col :span="16">
          <a-card class="changelog-card" :bordered="false">
            <template #title>
              <span>
                <HistoryOutlined style="margin-right: 8px" />
                更新日志
              </span>
            </template>
            <template #extra>
              <a-radio-group v-model:value="filterType" button-style="solid" size="small">
                <a-radio-button value="all">全部</a-radio-button>
                <a-radio-button value="feature">新功能</a-radio-button>
                <a-radio-button value="optimize">优化</a-radio-button>
                <a-radio-button value="bugfix">修复</a-radio-button>
              </a-radio-group>
            </template>

            <a-timeline class="version-timeline">
              <a-timeline-item
                v-for="(version, index) in filteredVersions"
                :key="index"
                :color="version.isLatest ? 'green' : 'blue'"
              >
                <div class="timeline-header">
                  <span class="version-tag" :class="{ 'latest-tag': version.isLatest }">
                    {{ version.version }}
                  </span>
                  <span class="release-date">{{ version.date }}</span>
                </div>
                <div class="timeline-content">
                  <a-tag :color="getTypeColor(item.type)" v-for="(item, idx) in version.items" :key="idx" style="margin: 4px">
                    {{ item }}
                  </a-tag>
                </div>
              </a-timeline-item>
            </a-timeline>
          </a-card>
        </a-col>
      </a-row>

      <!-- 功能特性 -->
      <a-row :gutter="24" style="margin-top: 24px">
        <a-col :span="24">
          <a-card class="features-card" :bordered="false" title="系统特性">
            <a-row :gutter="16">
              <a-col :span="6" v-for="(feature, index) in features" :key="index">
                <div class="feature-item">
                  <div class="feature-icon" :style="{ backgroundColor: feature.color }">
                    <component :is="feature.icon" />
                  </div>
                  <div class="feature-content">
                    <div class="feature-title">{{ feature.title }}</div>
                    <div class="feature-desc">{{ feature.desc }}</div>
                  </div>
                </div>
              </a-col>
            </a-row>
          </a-card>
        </a-col>
      </a-row>

      <!-- 技术支持 -->
      <a-row :gutter="24" style="margin-top: 24px">
        <a-col :span="24">
          <a-card class="support-card" :bordered="false">
            <div class="support-content">
              <div class="support-item">
                <QuestionCircleOutlined class="support-icon" />
                <div>
                  <div class="support-title">技术支持</div>
                  <div class="support-desc">如有问题，请联系技术支持团队：support@budaos.com</div>
                </div>
              </div>
              <a-divider type="vertical" style="height: 60px" />
              <div class="support-item">
                <CustomerServiceOutlined class="support-icon" />
                <div>
                  <div class="support-title">客服热线</div>
                  <div class="support-desc">400-888-8888 （周一至周五 9:00-18:00）</div>
                </div>
              </div>
              <a-divider type="vertical" style="height: 60px" />
              <div class="support-item">
                <GlobalOutlined class="support-icon" />
                <div>
                  <div class="support-title">官方网站</div>
                  <div class="support-desc">
                    <a href="https://www.budaos.com" target="_blank">https://www.budaos.com</a>
                  </div>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import {
    InfoCircleOutlined,
    EnvironmentOutlined,
    TeamOutlined,
    CopyrightOutlined,
    HistoryOutlined,
    QuestionCircleOutlined,
    CustomerServiceOutlined,
    GlobalOutlined,
    RocketOutlined,
    SafetyOutlined,
    ThunderboltOutlined,
    DashboardOutlined,
  } from '@ant-design/icons-vue';

  const currentVersion = ref('1.0.0');
  const releaseDate = ref('2024-01-01');
  const filterType = ref('all');

  const versionHistory = ref([
    {
      version: 'v1.0.0',
      date: '2024-01-01',
      isLatest: true,
      items: [
        'feature: 系统正式上线',
        'feature: 用户中心模块',
        'feature: 影院管理模块',
        'feature: 票务管理模块',
        'optimize: 系统性能优化',
        'bugfix: 修复已知问题',
      ],
    },
    {
      version: 'v0.9.0',
      date: '2023-12-15',
      isLatest: false,
      items: [
        'feature: 内测版本发布',
        'feature: 基础功能完成',
        'optimize: 用户体验优化',
      ],
    },
    {
      version: 'v0.8.0',
      date: '2023-11-20',
      isLatest: false,
      items: [
        'feature: 开发阶段',
        'feature: 核心功能开发',
      ],
    },
  ]);

  const filteredVersions = computed(() => {
    if (filterType.value === 'all') {
      return versionHistory.value;
    }
    return versionHistory.value.map((version) => ({
      ...version,
      items: version.items.filter((item) => item.startsWith(filterType.value)),
    })).filter((version) => version.items.length > 0);
  });

  const getTypeColor = (type) => {
    const colorMap = {
      feature: 'blue',
      optimize: 'green',
      bugfix: 'orange',
    };
    return colorMap[type] || 'default';
  };

  const features = ref([
    {
      title: '高性能',
      desc: '采用现代化技术栈，提供极致性能体验',
      icon: ThunderboltOutlined,
      color: '#faad14',
    },
    {
      title: '安全可靠',
      desc: '企业级安全防护，保障数据安全',
      icon: SafetyOutlined,
      color: '#52c41a',
    },
    {
      title: '功能完善',
      desc: '丰富的业务功能模块，满足多样化需求',
      icon: DashboardOutlined,
      color: '#1890ff',
    },
    {
      title: '易于扩展',
      desc: '模块化架构设计，支持灵活扩展',
      icon: RocketOutlined,
      color: '#722ed1',
    },
  ]);
</script>

<style lang="less" scoped>
  .version-container {
    height: 100%;
    display: flex;
    flex-direction: column;

    .header-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding: 16px 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);

      .header-title {
        display: flex;
        align-items: center;
        font-size: 22px;
        font-weight: 600;
        color: #ffffff;

        .title-icon {
          margin-right: 12px;
          font-size: 24px;
        }
      }

      .header-actions {
        display: flex;
        gap: 12px;
      }
    }

    .version-content {
      flex: 1;
      overflow-y: auto;

      .version-card {
        height: 100%;
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

        .version-info {
          text-align: center;

          .version-number {
            font-size: 36px;
            font-weight: 700;
            color: #667eea;
            margin-bottom: 8px;
          }

          .version-date {
            font-size: 14px;
            color: #999;
            margin-bottom: 16px;
          }

          .version-meta {
            text-align: left;
            padding: 0 16px;

            p {
              margin: 12px 0;
              display: flex;
              align-items: center;
              gap: 8px;
              color: #666;
            }
          }
        }
      }

      .changelog-card {
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

        :deep(.ant-card-head) {
          border-bottom: 1px solid #f0f0f0;
          padding: 16px 24px;
        }

        :deep(.ant-card-body) {
          padding: 24px;
        }

        .version-timeline {
          .timeline-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 12px;

            .version-tag {
              font-weight: 600;
              font-size: 16px;
              color: #667eea;

              &.latest-tag {
                color: #52c41a;
              }
            }

            .release-date {
              color: #999;
              font-size: 14px;
            }
          }

          .timeline-content {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
          }
        }
      }

      .features-card {
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

        :deep(.ant-card-body) {
          padding: 24px;
        }

        .feature-item {
          display: flex;
          align-items: flex-start;
          gap: 12px;
          padding: 16px;
          background: #f5f7fa;
          border-radius: 8px;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-4px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          }

          .feature-icon {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            color: #ffffff;
            flex-shrink: 0;
          }

          .feature-content {
            flex: 1;

            .feature-title {
              font-size: 16px;
              font-weight: 600;
              color: #333;
              margin-bottom: 4px;
            }

            .feature-desc {
              font-size: 14px;
              color: #666;
              line-height: 1.5;
            }
          }
        }
      }

      .support-card {
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

        .support-content {
          display: flex;
          justify-content: space-around;
          align-items: center;
        }

        .support-item {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 16px;
          padding: 8px;

          .support-icon {
            font-size: 36px;
            color: #667eea;
          }

          .support-title {
            font-size: 16px;
            font-weight: 600;
            color: #333;
            margin-bottom: 4px;
          }

          .support-desc {
            font-size: 14px;
            color: #666;

            a {
              color: #667eea;

              &:hover {
                text-decoration: underline;
              }
            }
          }
        }
      }
    }
  }
</style>
