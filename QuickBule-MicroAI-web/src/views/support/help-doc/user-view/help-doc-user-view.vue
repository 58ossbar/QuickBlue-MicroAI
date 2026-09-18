<!--
  * 帮助文档详情
  *

-->
<template>
  <a-card size="small" :bordered="false">
    <div v-if="helpDocDetail" id="helpDocContent" class="pdf-container">
      <div class="content-header">
        <!--startprint-->
        <div class="content-header-title">
          {{ helpDocDetail.title }}
        </div>
        <div class="content-header-info">
          <span>阅读量：{{ helpDocDetail.pageViewCount }}</span>
          <span v-show="helpDocDetail.author">作者：{{ helpDocDetail.author }}</span>
          <span>发布于：{{ helpDocDetail.createTime }}</span>
          <span>修改于：{{ helpDocDetail.updateTime }}</span>
          <span @click="print">【打印本页】</span>
          <span @click="exportPDF">【导出PDF】</span>
        </div>
      </div>
      <div class="content-html" v-html="helpDocDetail.contentHtml"></div>
      <!--endprint-->
    </div>
    <a-divider v-if="helpDocDetail.attachment && helpDocDetail.attachment.length > 0" />
    <div v-if="helpDocDetail.attachment && helpDocDetail.attachment.length > 0">附件：<FilePreview :fileList="helpDocDetail.attachment" /></div>
  </a-card>

  <a-card title="阅读记录" size="small" class="qb-margin-top10" :bordered="false">
    <HelpDocViewRecordList ref="helpDocViewRecordListRef" :helpDocId="route.query.helpDocId" />
  </a-card>

  <!-- 预览附件 -->
  <FilePreview ref="filePreviewRef" />
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import HelpDocViewRecordList from './components/help-doc-view-record-list.vue';
import { helpDocApi } from '/@/api/support/help-doc-api';
import { Loading } from '/@/components/framework/loading';
import FilePreview from '/@/components/support/file-preview/index.vue';
import { sentry } from '/@/lib/sentry';
import html2pdf from 'html2pdf.js';
import { message } from 'ant-design-vue';

const route = useRoute();

const activeKey = ref(1);

const helpDocDetail = ref({});

onMounted(() => {
  if (route.query.helpDocId) {
    queryHelpDocDetail();
  }
});

const helpDocViewRecordListRef = ref();

// 查询详情
async function queryHelpDocDetail() {
  try {
    Loading.show();
    const result = await helpDocApi.view(route.query.helpDocId);
    helpDocDetail.value = result.data;

    helpDocViewRecordListRef.value.onSearch();
  } catch (err) {
    sentry.captureError(err);
  } finally {
    Loading.hide();
  }
}

// 预览附件
const filePreviewRef = ref();
function onPrevFile(fileItem) {
  filePreviewRef.value.showPreview(fileItem);
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

// 导出PDF
async function exportPDF() {
  try {
    Loading.show();

    const element = document.getElementById('helpDocContent');
    if (!element) {
      message.error('未找到文档内容');
      return;
    }

    // 构建导出文件名
    const fileName = `${helpDocDetail.value.title || '帮助文档'}.pdf`;

    // PDF导出配置 - 优化为与HTML显示一致，并改进分页
    const opt = {
      margin: [15, 15, 15, 15],
      filename: fileName,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: {
        scale: 2,
        useCORS: true,
        allowTaint: true,
        logging: false,
        letterRendering: true,
        backgroundColor: '#ffffff',
        // 确保CSS样式完全保留
        onclone: (clonedDoc) => {
          // 在克隆的文档中保留所有样式
          const clonedElement = clonedDoc.getElementById('helpDocContent');
          if (clonedElement) {
            // 确保图片样式完全保留
            const images = clonedElement.querySelectorAll('img');
            images.forEach(img => {
              img.style.maxWidth = '100%';
              img.style.height = 'auto';
              img.style.display = 'inline-block';
              img.style.objectFit = 'contain';
              img.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
              img.style.border = '3px solid #fff';
              img.style.borderRadius = '4px';
              img.style.margin = '10px auto';
              img.style.pageBreakInside = 'avoid';
            });

            // 确保段落不被切断
            const paragraphs = clonedElement.querySelectorAll('p');
            paragraphs.forEach(p => {
              p.style.pageBreakInside = 'avoid';
              p.style.orphans = '2';
              p.style.widows = '2';
            });

            // 确保标题不被切断
            const headings = clonedElement.querySelectorAll('h1, h2, h3, h4, h5, h6');
            headings.forEach(h => {
              h.style.pageBreakAfter = 'avoid';
              h.style.pageBreakInside = 'avoid';
            });

            // 确保表格不被切断
            const tables = clonedElement.querySelectorAll('table');
            tables.forEach(t => {
              t.style.pageBreakInside = 'avoid';
            });

            // 确保列表不被切断
            const lists = clonedElement.querySelectorAll('ul, ol');
            lists.forEach(l => {
              l.style.pageBreakInside = 'avoid';
            });
          }
        }
      },
      jsPDF: {
        unit: 'mm',
        format: 'a4',
        orientation: 'portrait'
      },
      // 改进分页控制
      pagebreak: {
        mode: ['avoid-all', 'css', 'legacy'],
        before: '.page-break-before',
        after: '.page-break-after',
        avoid: ['img', 'table', 'ul', 'ol', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'p', '.content-html']
      }
    };

    await html2pdf().set(opt).from(element).save();
    message.success('PDF导出成功');
  } catch (error) {
    console.error('PDF导出失败:', error);
    message.error('PDF导出失败: ' + error.message);
    sentry.captureError(error);
  } finally {
    Loading.hide();
  }
}
</script>

<style lang="less" scoped>
.pdf-container {
  background: #fff;
  padding: 20px;
}

/* 打印和PDF导出优化 */
@media print {
  .pdf-container {
    width: 100%;
    max-width: none;
    padding: 20px;
    box-sizing: border-box;
  }

  :deep(.content-html) {
    margin-top: 30px;
    padding: 0 8px;
    line-height: 28px;
    font-size: 16px;

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
      page-break-inside: avoid;
      page-break-after: auto;
    }

    img.text-left {
      float: left !important;
      display: inline-block !important;
      margin: 10px 20px 10px 0 !important;
    }

    img.text-right {
      float: right !important;
      display: inline-block !important;
      margin: 10px 0 10px 20px !important;
    }

    img.text-center {
      display: block !important;
      margin: 10px auto !important;
    }

    /* 段落不被切断 */
    p {
      page-break-inside: avoid;
      orphans: 2;
      widows: 2;
    }

    /* 标题不被切断 */
    h1, h2, h3, h4, h5, h6 {
      page-break-after: avoid;
      page-break-inside: avoid;
    }

    /* 表格不被切断 */
    table {
      page-break-inside: avoid;
    }

    /* 列表不被切断 */
    ul, ol {
      page-break-inside: avoid;
    }

    /* 避免在元素内分页 */
    li, td, th {
      page-break-inside: avoid;
    }
  }
}

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
    font-size: 20px;
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
/*样式深入*/
:deep(.content-html) {
  margin-top: 30px;
  padding: 0 8px;
  line-height: 28px;
  font-size: 16px;
  border: #1e1e1e;
  overflow-x: hidden;

  /* 图片通用样式 */
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

  body {
    margin: 0 auto;
    color: #ccd1d8;
    line-height: 1.5;
    padding: 16px;
    background-color: #333842;
    font-size: 16px;
    /* 字体清晰度优化 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    font-smooth: always;
    text-rendering: optimizeLegibility;
  }

  h1, h2, h3, h4, h5, h6 {
    color: #0D366F;
    font-weight: bold;
    margin-top: 20px;
    margin-bottom: 10px;
    padding: 0;
    /* 标题字体优化 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
    letter-spacing: 0.5px;
    /* 标题字体优化 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
    letter-spacing: 0.5px;
  }

  p {
    padding: 0;
    margin-bottom: 16px;
    /* 段落字体优化 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
    letter-spacing: 0.3px;
    /* 段落字体优化 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
    letter-spacing: 0.3px;
  }

  h1 {
    font-size: 26px;
  }

  h2 {
    font-size: 24px;
  }

  h3 {
    font-size: 22px;
  }

  h4 {
    font-size: 20px;
  }

  h5 {
    font-size: 19px;
  }

  h6 {
    font-size: 18px;
  }

  a {
    color: #61afef;
    margin: 0;
    padding: 0;
    vertical-align: baseline;
    text-decoration: none;
    word-break: break-word;
  }

  a:hover {
    text-decoration: underline;
  }

  a:visited {
    color: #ba68c8;
  }

  ul, ol {
    padding: 0;
    padding-left: 24px;
  }

  li {
    line-height: 24px;
  }

  li ul, li ol {
    margin-left: 16px;
  }

  p, ul, ol {
    font-size: 16px;
    line-height: 24px;
  }

  mark {
    color: #000000;
    background-color: #c4c400;
  }

  pre {
    display: block;
    overflow-y: hidden;
    overflow-x: auto;
    -moz-tab-size: 4;
    tab-size: 4;
  }

  code {
    color: #98c379;
    word-break: break-word;
    /* 代码字体优化 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    /* 代码字体优化 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  }

  pre code {
    display: block;
    padding-left: 0.5em;
    padding-right: 0.5em;
    color: #98c379;
    background-color: #2d323b;
    line-height: 1.5;
    white-space: pre;
    -moz-tab-size: 4;
    tab-size: 4;
    /* 预格式化代码字体优化 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    letter-spacing: 0;
    /* 预格式化代码字体优化 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-rendering: optimizeLegibility;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    letter-spacing: 0;
  }

  aside {
    display: block;
    float: right;
    width: 390px;
  }

  blockquote {
    color: #abb2bf;
    border-left: .5em solid #abb2bf;
    padding: 0 1em;
    margin-left: 0;
  }

  blockquote p {
    color: #abb2bf;
  }

  hr {
    display: block;
    text-align: left;
    margin: 1em 0;
    border: none;
    height: 2px;
    background-color: #4c5562;
  }

  table {
    padding: 0;
    margin: 1rem 0.5rem;
    border-collapse: collapse;
  }

  table tr {
    border-top: 1px solid #4c5562;
    margin: 0;
    padding: 0;
  }

  table tr:hover {
    background-color: #DBE5F2;
  }

  table tr th {
    font-weight: bold;
    background-color: #90BFFF;
    border: 1px solid #4c5562;
    margin: 0;
    padding: 6px 13px;
  }

  table tr td {
    border: 1px solid #4c5562;
    margin: 0;
    padding: 6px 13px;
  }

  table tr th :first-child, table tr td :first-child {
    margin-top: 0;
  }

  table tr th :last-child, table tr td :last-child {
    margin-bottom: 0;
  }
}
</style>
