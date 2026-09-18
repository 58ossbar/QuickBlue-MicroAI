<!--
  * 通知公告详情弹窗
-->
<template>
  <a-modal
    v-model:open="visible"
    title="通知公告详情"
    width="800"
    :footer="null"
    @cancel="onClose"
  >
    <a-spin :spinning="loading">
      <div v-if="noticeData" class="notice-detail-modal-body">
        <a-descriptions bordered :column="2" size="small">
          <a-descriptions-item label="分类">{{ noticeData.noticeTypeName }}</a-descriptions-item>
          <a-descriptions-item label="文号">{{ noticeData.documentNumber || '无' }}</a-descriptions-item>
          <a-descriptions-item label="作者">{{ noticeData.author || '无' }}</a-descriptions-item>
          <a-descriptions-item label="来源">{{ noticeData.source || '无' }}</a-descriptions-item>
          <a-descriptions-item label="发布状态">
            <a-tag :color="noticeData.publishFlag ? 'success' : 'warning'" size="small">
              {{ noticeData.publishFlag ? '已发布' : '待发布' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="浏览量">{{ noticeData.pageViewCount }}次 / {{ noticeData.userViewCount }}人</a-descriptions-item>
          <a-descriptions-item label="创建人">{{ noticeData.createUserName }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ noticeData.createTime }}</a-descriptions-item>
          <a-descriptions-item label="发布时间">{{ noticeData.publishTime || '未发布' }}</a-descriptions-item>
          <a-descriptions-item label="可见范围" :span="2">
            <template v-if="noticeData.allVisibleFlag">全部可见</template>
            <div v-else class="visible-list">
              <span class="visible-item" v-for="item in noticeData.visibleRangeList" :key="item.dataId">
                {{ item.dataName }}
              </span>
            </div>
          </a-descriptions-item>
        </a-descriptions>

        <div class="notice-content">
          <h4>公告内容</h4>
          <div v-html="noticeData.contentHtml" class="content-html"></div>
        </div>

        <div v-if="!$lodash.isEmpty(noticeData.attachment)" class="notice-attachments">
          <h4>附件</h4>
          <a-list :data-source="noticeData.attachment" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a :href="item.fileUrl" target="_blank" download>
                  {{ item.fileName }}
                </a>
              </a-list-item>
            </template>
          </a-list>
        </div>

        <div class="notice-view-records">
          <h4>查看记录</h4>
          <NoticeViewRecordList :noticeId="currentNoticeId" />
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup>
import { ref } from 'vue';
import { noticeApi } from '/src/api/business/notice/notice-api';
import { sentry } from '/src/lib/sentry';
import NoticeViewRecordList from './notice-view-record-list.vue';

const visible = ref(false);
const loading = ref(false);
const currentNoticeId = ref(null);
const noticeData = ref(null);

async function showDetail(noticeId) {
  currentNoticeId.value = noticeId;
  visible.value = true;
  loading.value = true;
  try {
    const result = await noticeApi.getUpdateNoticeInfo(noticeId);
    noticeData.value = result.data;
  } catch (err) {
    sentry.captureError(err);
  } finally {
    loading.value = false;
  }
}

function onClose() {
  visible.value = false;
  noticeData.value = null;
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

.notice-content {
  margin-top: 24px;
}

.notice-content h4 {
  margin-bottom: 12px;
  font-weight: 500;
}

.content-html {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
  min-height: 100px;
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
}

.notice-attachments {
  margin-top: 24px;
}

.notice-attachments h4 {
  margin-bottom: 12px;
  font-weight: 500;
}

.visible-list {
  display: flex;
  flex-wrap: wrap;
  .visible-item {
    margin-right: 10px;
    color: #666;
  }
}

.notice-view-records {
  margin-top: 24px;
}

.notice-view-records h4 {
  margin-bottom: 12px;
  font-weight: 500;
}
</style>
