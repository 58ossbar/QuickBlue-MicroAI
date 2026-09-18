<!--
  * 通知  详情 （员工）
  *

-->
<template>
  <a-card size="small">
    <div>
      <div class="content-header">
        <!--startprint-->
        <div class="content-header-title">
          {{ noticeDetail.title }}
        </div>
        <div class="content-header-info">
          <span v-show="noticeDetail.author">作者：{{ noticeDetail.author }}</span>
          <span v-show="noticeDetail.source">来源：{{ noticeDetail.source }}</span>
          <span>发布时间：{{ noticeDetail.publishTime }}</span>
          <span>阅读量：{{ noticeDetail.pageViewCount }}</span>
          <span @click="print">【打印本页】</span>
        </div>
      </div>
      <div class="content-html" v-html="noticeDetail.contentHtml"></div>
      <!--endprint-->
    </div>
    <a-divider />
    <div>
      附件：
      <file-preview v-if="!$lodash.isEmpty(noticeDetail.attachment)" :fileList="noticeDetail.attachment" />
      <span v-else>无</span>
    </div>
  </a-card>

  <a-card title="记录" size="small" class="qb-margin-top10">
    <NoticeViewRecordList ref="noticeViewRecordList" :noticeId="route.query.noticeId" />
  </a-card>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import NoticeViewRecordList from './components/notice-view-record-list.vue';
  import { noticeApi } from '/src/api/business/notice/notice-api';
  import { Loading } from '/src/components/framework/loading';
  import FilePreview from '/src/components/support/file-preview/index.vue';
  import { sentry } from '/src/lib/sentry';

  const route = useRoute();

  const noticeDetail = ref({});

  onMounted(() => {
    if (route.query.noticeId) {
      queryNoticeDetail();
    }
  });

  const noticeViewRecordList = ref();

  // 查询详情
  async function queryNoticeDetail() {
    try {
      Loading.show();
      const result = await noticeApi.view(route.query.noticeId);
      noticeDetail.value = result.data;

      noticeViewRecordList.value.onSearch();
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

  // 打印
  function print() {
    let bdhtml = window.document.body.innerHTML;
    let sprnstr = '<!--startprint-->'; //必须在页面添加<!--startprint-->和<!--endprint-->而且需要打印的内容必须在它们之间
    let eprnstr = '<!--endprint-->';
    let prnhtml = bdhtml.substr(bdhtml.indexOf(sprnstr));
    prnhtml = prnhtml.substring(0, prnhtml.indexOf(eprnstr));
    let newWin = window.open(''); //新打开一个空窗口
    newWin.document.body.innerHTML = prnhtml;
    newWin.document.close(); //在IE浏览器中使用必须添加这一句
    newWin.focus(); //在IE浏览器中使用必须添加这一句
    newWin.print(); //打印
    newWin.close(); //关闭窗口
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
  .content-header {
    .content-header-title {
      margin: 10px 0px;
      font-size: 18px;
      font-weight: bold;
      text-align: center;
    }
    .content-header-info {
      margin: 10px 0px;
      font-size: 14px;
      color: #888;
      text-align: center;
      span {
        margin: 0 10px;
        cursor: pointer;
      }
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

    /* 有序列表和无序列表样式 */
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

      ul {
        list-style-type: circle;

        ul {
          list-style-type: square;
        }
      }
    }

    ol {
      list-style-type: decimal;

      ol {
        list-style-type: lower-alpha;

        ol {
          list-style-type: lower-roman;
        }
      }
    }

    li {
      margin-bottom: 4px;
      font-size: 14px;
      line-height: 1.8;
      color: #333;
      display: list-item;
      list-style-position: outside;
    }

    /* wangeditor 特定样式支持 */
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
</style>
