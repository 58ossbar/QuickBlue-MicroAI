<!--
  * 我的通知详情弹窗
-->
<template>
  <a-modal
    v-model:open="visible"
    title="通知详情"
    width="800"
    :footer="null"
    @cancel="onClose"
  >
    <a-spin :spinning="loading">
      <div v-if="noticeDetail" class="notice-detail-modal-body">
        <div class="content-header">
          <div class="content-header-title">{{ noticeDetail.title }}</div>
          <a-descriptions bordered :column="2" size="small">
            <a-descriptions-item label="作者">{{ noticeDetail.author || '无' }}</a-descriptions-item>
            <a-descriptions-item label="来源">{{ noticeDetail.source || '无' }}</a-descriptions-item>
            <a-descriptions-item label="发布时间">{{ noticeDetail.publishTime }}</a-descriptions-item>
            <a-descriptions-item label="阅读量">{{ noticeDetail.pageViewCount || 0 }}</a-descriptions-item>
          </a-descriptions>
          <div class="content-header-actions">
            <a-button type="link" @click="print">
              <PrinterOutlined /> 打印本页
            </a-button>
          </div>
        </div>

        <a-divider />

        <div class="content-html" v-html="noticeDetail.contentHtml"></div>

        <a-divider />

        <div>
          <strong>附件：</strong>
          <file-preview v-if="!$lodash.isEmpty(noticeDetail.attachment)" :fileList="noticeDetail.attachment" />
          <span v-else>无</span>
        </div>

        <div class="notice-view-records">
          <a-card title="查看记录" size="small">
            <NoticeViewRecordList ref="noticeViewRecordList" :noticeId="currentNoticeId" />
          </a-card>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup>
import { ref } from 'vue';
import { noticeApi } from '/src/api/business/notice/notice-api';
import { Loading } from '/src/components/framework/loading';
import FilePreview from '/src/components/support/file-preview/index.vue';
import { sentry } from '/src/lib/sentry';
import NoticeViewRecordList from './notice-view-record-list.vue';
import { PrinterOutlined } from '@ant-design/icons-vue';

const visible = ref(false);
const loading = ref(false);
const currentNoticeId = ref(null);
const noticeDetail = ref({});
const noticeViewRecordList = ref();

async function showDetail(noticeId) {
  currentNoticeId.value = noticeId;
  visible.value = true;
  loading.value = true;
  try {
    Loading.show();
    const result = await noticeApi.view(noticeId);
    noticeDetail.value = result.data;

    // 查看通知后会自动标记为已读
    await noticeViewRecordList.value?.onSearch?.();
  } catch (err) {
    sentry.captureError(err);
  } finally {
    Loading.hide();
    loading.value = false;
  }
}

function onClose() {
  visible.value = false;
  noticeDetail.value = {};
}

// 打印
function print() {
  const bdhtml = window.document.body.innerHTML;
  const sprnstr = '<!--startprint-->';
  const eprnstr = '<!--endprint-->';
  let prnhtml = bdhtml.substring(bdhtml.indexOf(sprnstr));
  prnhtml = prnhtml.substring(0, prnhtml.indexOf(eprnstr));
  const newWin = window.open('');
  newWin.document.body.innerHTML = prnhtml;
  newWin.document.close();
  newWin.focus();
  newWin.print();
  newWin.close();
}

defineExpose({
  showDetail,
});
</script>

<style scoped>
.notice-detail-modal-body {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 8px;
}

.notice-detail-modal-body::-webkit-scrollbar {
  width: 6px;
}

.notice-detail-modal-body::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.notice-detail-modal-body::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.content-header {
  .content-header-title {
    margin: 16px 0;
    font-size: 18px;
    font-weight: bold;
    text-align: center;
  }
  .content-header-actions {
    margin: 8px 0;
    text-align: right;
  }
}

.content-html {
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
  min-height: 200px;
  line-height: 1.8;

  img {
    max-width: 100%;
    height: auto;
    display: inline-block;
    object-fit: contain;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    border: 3px solid #fff;
    border-radius: 4px;
    margin: 10px auto;
  }

  img:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  }

  /* 图片居左 */
  img.text-left {
    float: left !important;
    display: inline-block !important;
    margin: 10px 20px 10px 0 !important;
  }

  /* 图片居右 */
  img.text-right {
    float: right !important;
    display: inline-block !important;
    margin: 10px 0 10px 20px !important;
  }

  /* 图片居中 */
  img.text-center {
    display: block !important;
    margin: 10px auto !important;
  }

  ul,
  ol {
    margin: 12px 0;
    padding-left: 24px;
    line-height: 1.8;
    color: #333;
    list-style-position: outside;
  }

  ul {
    list-style-type: disc;
  }

  ol {
    list-style-type: decimal;
  }

  li {
    margin-bottom: 4px;
    font-size: 14px;
    line-height: 1.8;
    color: #333;
    display: list-item;
    list-style-position: outside;
  }

  :deep(.w-e-text-container) ul,
  :deep(.w-e-text-container) ol {
    margin: 12px 0;
    padding-left: 24px;
    line-height: 1.8;
    color: #333;
    list-style-position: outside;
  }

  :deep(.w-e-text-container) li {
    margin-bottom: 4px;
    font-size: 14px;
    line-height: 1.8;
    color: #333;
    display: list-item;
    list-style-position: outside;
  }
}

.notice-view-records {
  margin-top: 16px;
}
</style>
