<template>
  <a-drawer
    :width="320"
    :open="helpDocExpandFlag"
    :closable="false"
    :maskClosable="true"
    :bodyStyle="{ padding: '0' }"
    @close="closeHelpDoc"
    class="help-doc-drawer"
  >
    <div class="help-doc-panel">
      <!-- 头部 -->
      <div class="help-doc-header">
        <div class="help-doc-title">
          <QuestionCircleOutlined class="help-icon" />
          <span>帮助中心</span>
        </div>
        <a-button type="text" size="small" class="close-btn" @click="closeHelpDoc">
          <template #icon><CloseOutlined /></template>
        </a-button>
      </div>

      <!-- 搜索 -->
      <div class="help-doc-search">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索帮助文档..."
          @search="onSearch"
          allowClear
        />
      </div>

      <!-- 快捷入口 -->
      <div class="help-doc-section">
        <div class="section-title">快捷入口</div>
        <div class="quick-links">
          <a-button type="text" block class="quick-link" @click="goToDoc()">
            <template #icon><ReadOutlined /></template>
            系统帮助文档
          </a-button>
          <a-button type="text" block class="quick-link" @click="goToDoc('guide')">
            <template #icon><RocketOutlined /></template>
            新手指南
          </a-button>
          <a-button type="text" block class="quick-link" @click="goToDoc('faq')">
            <template #icon><MessageOutlined /></template>
            常见问题
          </a-button>
          <a-button type="text" block class="quick-link" @click="goToDoc('api')">
            <template #icon><ApiOutlined /></template>
            API 文档
          </a-button>
        </div>
      </div>

      <!-- 热门问题 -->
      <div class="help-doc-section">
        <div class="section-title">热门问题</div>
        <div class="hot-questions">
          <a-button
            v-for="item in hotQuestions"
            :key="item.id"
            type="link"
            class="question-link"
            @click="goToDoc(item.id)"
          >
            {{ item.title }}
          </a-button>
        </div>
      </div>

      <!-- 底部 -->
      <div class="help-doc-footer">
        <a-divider style="margin: 12px 0" />
        <div class="footer-actions">
          <a-button type="text" size="small" @click="openFeedback">
            <template #icon><EditOutlined /></template>
            意见反馈
          </a-button>
          <a-button type="text" size="small" @click="openContact">
            <template #icon><CustomerServiceOutlined /></template>
            联系客服
          </a-button>
        </div>
      </div>
    </div>

    <!-- 弹窗 -->
    <ContactModal ref="contactModalRef" />
    <FeedbackModal ref="feedbackModalRef" />
  </a-drawer>
</template>

<script setup>
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import {
    QuestionCircleOutlined,
    CloseOutlined,
    ReadOutlined,
    RocketOutlined,
    MessageOutlined,
    ApiOutlined,
    EditOutlined,
    CustomerServiceOutlined,
  } from '@ant-design/icons-vue';
  import ContactModal from './components/contact-modal.vue';
  import FeedbackModal from './components/feedback-modal.vue';

  const router = useRouter();

  const props = defineProps({
    helpDocExpandFlag: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['closeHelpDoc']);

  function closeHelpDoc() {
    emit('closeHelpDoc');
  }

  // 搜索
  const searchKeyword = ref('');
  function onSearch(keyword) {
    if (!keyword) return;
    router.push({ path: '/help-doc', query: { keyword } });
    closeHelpDoc();
  }

  // 跳转文档
  function goToDoc(id) {
    router.push({ path: '/help-doc', query: id ? { id } : undefined });
    closeHelpDoc();
  }

  // 热门问题
  const hotQuestions = ref([
    { id: '1', title: '如何快速创建项目？' },
    { id: '2', title: '如何配置数据源？' },
    { id: '3', title: '如何进行权限管理？' },
    { id: '4', title: '如何部署到生产环境？' },
  ]);

  // 意见反馈
  const feedbackModalRef = ref();
  function openFeedback() {
    closeHelpDoc();
    feedbackModalRef.value.show();
  }

  // 联系客服
  const contactModalRef = ref();
  function openContact() {
    closeHelpDoc();
    contactModalRef.value.show();
  }
</script>

<style scoped lang="less">
  .help-doc-panel {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .help-doc-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;

    .help-doc-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #262626;

      .help-icon {
        font-size: 18px;
        color: #4361ee;
      }
    }

    .close-btn {
      color: #8c8c8c;

      &:hover {
        color: #262626;
      }
    }
  }

  .help-doc-search {
    padding: 16px 20px 8px;
  }

  .help-doc-section {
    padding: 8px 20px;

    .section-title {
      font-size: 13px;
      font-weight: 600;
      color: #8c8c8c;
      margin-bottom: 8px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
  }

  .quick-links {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .quick-link {
      justify-content: flex-start;
      text-align: left;
      padding: 8px 12px;
      border-radius: 6px;
      font-size: 14px;
      color: #595959;
      transition: all 0.2s;

      &:hover {
        background: #f5f7ff;
        color: #4361ee;
      }
    }
  }

  .hot-questions {
    display: flex;
    flex-direction: column;

    .question-link {
      justify-content: flex-start;
      text-align: left;
      padding: 6px 0;
      font-size: 13px;
      color: #595959;

      &:hover {
        color: #4361ee;
      }
    }
  }

  .help-doc-footer {
    margin-top: auto;
    padding: 0 20px 16px;

    .footer-actions {
      display: flex;
      justify-content: space-between;

      :deep(.ant-btn) {
        color: #8c8c8c;
        font-size: 13px;

        &:hover {
          color: #4361ee;
        }
      }
    }
  }
</style>
