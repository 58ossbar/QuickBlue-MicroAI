<!--
  * 通知  详情
  *

-->
<template>
  <a-card style="margin-bottom: 15px" size="small">
    <a-descriptions :title="noticeDetail.title" :column="4" size="small">
      <template #extra>
        <a-button type="primary" size="small" @click="onEdit">编辑</a-button>
      </template>
      <a-descriptions-item label="分类">{{ noticeDetail.noticeTypeName }}</a-descriptions-item>
      <a-descriptions-item label="文号">{{ noticeDetail.documentNumber ? noticeDetail.documentNumber : '无' }}</a-descriptions-item>
      <a-descriptions-item label="来源">{{ noticeDetail.source }}</a-descriptions-item>
      <a-descriptions-item label="作者">{{ noticeDetail.author }}</a-descriptions-item>
      <a-descriptions-item label="页面浏览量">{{ noticeDetail.pageViewCount }}</a-descriptions-item>
      <a-descriptions-item label="用户浏览量">{{ noticeDetail.userViewCount }}</a-descriptions-item>
      <a-descriptions-item label="创建时间">{{ noticeDetail.createTime }}</a-descriptions-item>
      <a-descriptions-item label="发布时间">{{ noticeDetail.publishTime }}</a-descriptions-item>
      <a-descriptions-item label="发布状态">{{ noticeDetail.publishFlag ? '已发布' : '待发布' }}</a-descriptions-item>
      <a-descriptions-item label="删除状态">{{ noticeDetail.deletedFlag ? '已删除' : '未删除' }}</a-descriptions-item>
      <a-descriptions-item v-if="!$lodash.isEmpty(noticeDetail.attachment)" label="附件">
        <div class="file-list">
          <a class="file-item" v-for="item in noticeDetail.attachment" :key="item.fileId" @click="onPrevFile(item)">{{ item.fileName }}</a>
        </div>
      </a-descriptions-item>
      <a-descriptions-item label="可见范围">
        <template v-if="noticeDetail.allVisibleFlag">全部可见</template>
        <div class="visible-list">
          <div class="visible-item" v-for="item in noticeDetail.visibleRangeList" :key="item.dataId">
            {{ item.dataName }}
          </div>
        </div>
      </a-descriptions-item>
    </a-descriptions>
  </a-card>

  <a-card size="small">
    <a-tabs v-model:activeKey="activeKey" size="small">
      <a-tab-pane :key="1" tab="内容">
        <div class="content-html" v-html="noticeDetail.contentHtml"></div>
      </a-tab-pane>
      <a-tab-pane :key="2" tab="查看记录" force-render>
        <NoticeViewRecordList ref="noticeViewRecordList" :noticeId="route.query.noticeId" />
      </a-tab-pane>
      <a-tab-pane :key="3" tab="操作记录" />
    </a-tabs>
  </a-card>

  <!-- 编辑 -->
  <NoticeFormModal ref="noticeFormDrawerRef" @reloadList="queryNoticeDetail" />

  <!-- 预览附件 -->
  <FilePreviewModal ref="filePreviewRef" />
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import NoticeFormModal from './components/notice-form-modal.vue';
  import NoticeViewRecordList from './components/notice-view-record-list.vue';
  import { noticeApi } from '/src/api/business/notice/notice-api';
  import { Loading } from '/src/components/framework/loading';
  import FilePreviewModal from '/src/components/support/file-preview-modal/index.vue';
  import { sentry } from '/src/lib/sentry';

  const route = useRoute();

  const props = defineProps({
    newsType: {
      type: Number,
    },
  });

  const activeKey = ref(1);

  const noticeDetail = ref({});
  const noticeViewRecordList = ref();

  onMounted(() => {
    if (route.query.noticeId) {
      queryNoticeDetail();
      noticeViewRecordList.value.onSearch();
    }
  });

  // 查询详情
  async function queryNoticeDetail() {
    try {
      Loading.show();
      const result = await noticeApi.getUpdateNoticeInfo(route.query.noticeId);
      noticeDetail.value = result.data;
    } catch (err) {
      sentry.captureError(err);
    } finally {
      Loading.hide();
    }
  }

  // 点击编辑
  const noticeFormDrawerRef = ref();
  function onEdit() {
    noticeFormDrawerRef.value.showModal(noticeDetail.value.noticeId);
  }

  // 预览附件
  const filePreviewRef = ref();
  function onPrevFile(fileItem) {
    filePreviewRef.value.showPreview(fileItem);
  }
</script>

<style lang="less" scoped>
  :deep(.ant-descriptions-item-content) {
    flex: 1;
    overflow: hidden;
  }
  .file-list {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    .file-item {
      display: block;
      margin-right: 10px;
    }
  }
  .visible-list {
    display: flex;
    flex-wrap: wrap;
    .visible-item {
      margin-right: 10px;
      color: #666;
    }
  }
  .content-html {
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
</style>
