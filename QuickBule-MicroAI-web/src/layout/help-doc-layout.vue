<template>
  <!--
      中间内容，一共三部分：
      1、顶部
      2、中间内容区域
      3、底部（一般是公司版权信息）
     -->
  <a-layout class="help-doc-layout" id="budaosMain">
    <!-- 顶部头部信息 -->
    <a-layout-header class="layout-header">
      <a-row class="layout-header-title">
        <img class="logo-img" :src="logoImg" />
        <div class="title">{{ websiteName }}</div>
        <div class="title">帮助文档</div>
        <div class="title-actions">
          <a-button type="primary" @click="exportAllToPDF" size="small" :loading="exportingPDF">导出帮助手册(PDF)</a-button>
        </div>
        <a-col class="avatar">
          <div v-if="isLogin">
            <HeaderAvatar />
          </div>
          <a-button v-else type="primary" @click="goToLogin" size="small">登录</a-button>
        </a-col>
      </a-row>
    </a-layout-header>
    <a-layout :style="`height: ${windowHeight}px`">
      <!-- 侧边目录 side-menu -->
      <a-layout-sider class="side-menu" :style="`height: ${windowHeight}px`" :collapsed="false" theme="light" :width="300">
        <div class="help-doc-tree">
          <!-- 目录内容 -->
          <div>
            <a-directory-tree
              v-model:expandedKeys="expandedKeys"
              v-model:selectedKeys="selectedKeys"
              :tree-data="helpDocTreeData"
              @select="selectHelpDoc"
              :defaultExpandAll="false"
            />
          </div>
        </div>
      </a-layout-sider>

      <!--中间内容-->
      <a-layout-content id="budaosLayoutContent" class="help-doc-layout-content">
        <router-view v-slot="{ Component }">
          <div :key="route.fullPath">
            <component :is="Component" />
          </div>
        </router-view>
        <!-- footer 版权公司信息 -->
        <a-layout-footer class="layout-footer">
          <Footer />
        </a-layout-footer>
      </a-layout-content>
    </a-layout>

    <a-back-top :target="backTopTarget" :visibilityHeight="80" />
  </a-layout>
</template>

<script setup>
  import _ from 'lodash';
  import { computed, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { sentry } from '../lib/sentry';
  import { useAppConfigStore } from '../store/modules/system/app-config';
  import Footer from './components/footer/index.vue';
  import { helpDocApi } from '/@/api/support/help-doc-api';
  import { helpDocCatalogApi } from '/@/api/support/help-doc-catalog-api';
  import logoImg from '/@/assets/images/logo/budaos-logo-white.png';
  import { Loading } from '/@/components/framework/loading';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
  import { PAGE_PATH_LOGIN } from '/@/constants/common-const';
  import watermark from '../lib/watermark';
  import { useUserStore } from '/@/store/modules/system/user';
  import HeaderAvatar from './components/header-user-space/header-avatar.vue';
  import { LAYOUT_ELEMENT_IDS } from '/@/layout/layout-const';
  import { localRead } from '/@/utils/local-util';
  import LocalStorageKeyConst from '/@/constants/local-storage-key-const.js';
  import html2pdf from 'html2pdf.js';
  import { message } from 'ant-design-vue';

  const websiteName = computed(() => useAppConfigStore().websiteName);
  const windowHeight = window.innerHeight;

  // 导出PDF状态
  const exportingPDF = ref(false);

  // 判断是否已登录
  const isLogin = computed(() => {
    const token = localRead(LocalStorageKeyConst.USER_TOKEN);
    return !!token;
  });

  onMounted(() => {
    watermark.set(LAYOUT_ELEMENT_IDS.content, {
      text: 'QuickBlue 快速开发平台',
      showDate: false
    });
  });
  const backTopTarget = () => {
    return document.getElementById(LAYOUT_ELEMENT_IDS.main);
  };
  const router = useRouter();
  const route = useRoute();
  function goHome() {
    router.push({ name: HOME_PAGE_NAME });
  }

  function goToLogin() {
    router.push({ path: PAGE_PATH_LOGIN });
  }

  // 导出整个帮助文档为PDF
  // 导出整个帮助文档为PDF
  async function exportAllToPDF() {
    try {
      exportingPDF.value = true;
      Loading.show();

      // 获取完整数据
      const result = await helpDocApi.getComplete();
      const completeData = result.data;

      console.log('=== 导出调试信息 ===');
      console.log('获取到的完整数据:', JSON.stringify(completeData, null, 2));
      console.log('目录树数量:', completeData?.catalogTree?.length || 0);
      console.log('当前页面 URL:', window.location.href);
      console.log('当前页面 Origin:', window.location.origin);

      // 详细检查数据结构
      if (completeData.catalogTree && completeData.catalogTree.length > 0) {
        console.log('第一个目录结构:', JSON.stringify(completeData.catalogTree[0], null, 2));
        if (completeData.catalogTree[0].docList && completeData.catalogTree[0].docList.length > 0) {
          console.log('第一个文档内容长度:', completeData.catalogTree[0].docList[0].contentHtml?.length || 0);
          console.log('第一个文档内容预览:', completeData.catalogTree[0].docList[0].contentHtml?.substring(0, 500) || '无HTML内容');
        }
      }

      if (!completeData || !completeData.catalogTree || completeData.catalogTree.length === 0) {
        message.warning('暂无帮助文档可导出');
        exportingPDF.value = false;
        Loading.hide();
        return;
      }

      // 生成HTML内容
      let htmlContent = '';

      // 封面设计 - 使用简单布局
      htmlContent += `
        <div style="
          width: 210mm;
          height: 297mm;
          background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
          padding: 100px 50px 80px 50px;
          margin: 0;
          text-align: center;
          box-sizing: border-box;
          page-break-after: always;
          page-break-inside: avoid;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
        ">
          <!-- 顶部装饰线 -->
          <div style="
            width: 100px;
            height: 4px;
            background: #ff4081;
            margin: 0 auto;
          "></div>

          <!-- 空白间距 -->
          <div style="height: 80px;"></div>

          <!-- 主标题区域 -->
          <div>
            <h1 style="
              font-size: 36px;
              color: #ffffff;
              font-weight: 700;
              margin: 0 0 40px 0;
              letter-spacing: 6px;
              line-height: 1.3;
            ">
              QuickBlue 快速开发平台
            </h1>

            <p style="
              font-size: 32px;
              color: #ffffff;
              font-weight: 600;
              margin: 0;
              letter-spacing: 8px;
            ">
              操作手册
            </p>
          </div>

          <!-- 空白间距，推到底部 -->
          <div style="height: 150px;"></div>

          <!-- 底部信息区域 -->
          <div>
            <div style="
              width: 100px;
              height: 3px;
              background: rgba(255, 255, 255, 0.4);
              margin: 0 auto 30px auto;
            "></div>

            <p style="
              font-size: 13px;
              color: rgba(255, 255, 255, 0.9);
              margin: 8px 0;
              font-weight: 400;
            ">
              生成时间：${new Date().toLocaleString('zh-CN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
              })}
            </p>

            <p style="
              font-size: 13px;
              color: rgba(255, 255, 255, 0.85);
              margin: 10px 0;
              font-weight: 400;
              letter-spacing: 0.5px;
            ">
              QuickBlue 企业级应用底座 · 开源版
            </p>

            <p style="
              font-size: 11px;
              color: rgba(255, 255, 255, 0.7);
              margin: 6px 0 0 0;
              font-weight: 300;
              letter-spacing: 1px;
            ">
              QUICKBLUE ENTERPRISE APPLICATION FOUNDATION
            </p>
          </div>
        </div>
      `;

      // 目录页
      const tocHtml = renderTableOfContents(completeData.catalogTree, 1, '', { count: 0 });
      if (tocHtml.trim()) {
        htmlContent += `
          <div id="toc" style="
            width: 210mm;
            min-height: 297mm;
            background: #ffffff;
            padding: 60px 50px 40px 50px;
            margin: 0;
            box-sizing: border-box;
            page-break-after: always;
            page-break-inside: avoid;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
          ">
            <h1 style="
              font-size: 32px;
              color: #1a237e;
              font-weight: 700;
              text-align: center;
              margin: 0 0 50px 0;
              padding-bottom: 20px;
              border-bottom: 3px solid #1a237e;
            ">
              目录
            </h1>
            ${tocHtml}
          </div>
        `;
      }

      // 递归渲染目录树内容
      const catalogHtml = renderCatalogTree(completeData.catalogTree, 1, true);
      console.log('生成的目录HTML长度:', catalogHtml.length);
      console.log('生成的目录HTML预览:', catalogHtml.substring(0, 500));
      if (catalogHtml.length === 0) {
        console.error('生成的目录HTML为空！');
      }

      htmlContent += `
        <div style="
          width: 210mm;
          background: #ffffff;
          padding: 50px 30px 40px 30px;
          box-sizing: border-box;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
          page-break-before: always;
        ">
          ${catalogHtml}
        </div>
      `;

      console.log('生成的HTML总长度:', htmlContent.length);
      console.log('HTML内容预览（前1000字符）:', htmlContent.substring(0, 1000));

      console.log('等待完成，开始生成PDF');

      // 保存原始页面标题
      const originalTitle = document.title;
      // 设置PDF文件名
      document.title = 'QuickBlue 快速开发平台操作手册';

      // 创建打印容器
      const printContainer = document.createElement('div');
      printContainer.id = 'print-container';
      printContainer.innerHTML = htmlContent;
      printContainer.style.position = 'fixed';
      printContainer.style.top = '0';
      printContainer.style.left = '0';
      printContainer.style.width = '100%';
      printContainer.style.zIndex = '999999';
      printContainer.style.backgroundColor = 'white';
      document.body.appendChild(printContainer);
      console.log('打印容器已添加到DOM');

      // 添加打印样式
      const style = document.createElement('style');
      style.id = 'print-style';
      style.textContent = `
        @media print {
          /* 隐藏页面其他内容 */
          body > *:not(#print-container) {
            display: none !important;
          }
          #print-container {
            position: static !important;
            width: 100% !important;
            z-index: auto !important;
          }
          /* 页面设置 - 保留页边距 */
          @page {
            size: A4;
            margin: 15mm 20mm;
          }
          body {
            margin: 0;
            padding: 0;
          }
        }
      `;
      document.head.appendChild(style);

      // 等待图片加载完成
      const images = printContainer.querySelectorAll('img');
      console.log(`找到 ${images.length} 张图片`);
      const imagePromises = Array.from(images).map((img, index) => {
        return new Promise((resolve) => {
          if (img.complete) {
            resolve();
          } else {
            img.onload = () => {
              console.log(`图片 ${index + 1}/${images.length} 加载成功`);
              resolve();
            };
            img.onerror = () => {
              console.warn(`图片 ${index + 1}/${images.length} 加载失败`);
              resolve(); // 即使失败也继续
            };
          }
        });
      });

      await Promise.all(imagePromises);
      console.log('所有图片加载完成');

      // 为每个带page-break-after的元素添加页码
      const pageElements = printContainer.querySelectorAll('[page-break-after="always"]');
      pageElements.forEach((el, index) => {
        // 跳过封面页
        if (index === 0) return;

        // 在元素底部添加页码
        const pageNumber = document.createElement('div');
        pageNumber.className = 'page-number';
        pageNumber.textContent = `- ${index} -`;
        el.appendChild(pageNumber);
      });

      // 添加页码样式
      const pageNumberStyle = document.createElement('style');
      pageNumberStyle.className = 'page-number-style';
      pageNumberStyle.textContent = `
        .page-number {
          text-align: center;
          font-size: 12px;
          color: #666;
          margin-top: 30px;
          padding-top: 10px;
          border-top: none;
        }
      `;
      document.head.appendChild(pageNumberStyle);

      // 等待片刻确保DOM完全渲染
      await new Promise(resolve => setTimeout(resolve, 500));

      // 调用浏览器打印对话框
      window.print();

      message.success('请在打印对话框中选择"另存为PDF"，并在"更多设置"中取消勾选"页眉和页脚"选项');

      // 延迟清理
      setTimeout(() => {
        const existingStyle = document.getElementById('print-style');
        if (existingStyle) {
          existingStyle.remove();
        }
        const pageNumberStyle = document.querySelector('.page-number-style');
        if (pageNumberStyle) {
          pageNumberStyle.remove();
        }
        // 恢复原始页面标题
        document.title = originalTitle;
        message.info('打印完成后，页面将自动恢复');
      }, 2000);
    } catch (error) {
      console.error('=== 导出失败 ===');
      console.error('错误详情:', error);
      console.error('错误堆栈:', error.stack);
      message.error('导出失败: ' + error.message);
      sentry.captureError(error);
      // 恢复原始页面标题
      document.title = originalTitle;
    } finally {
      exportingPDF.value = false;
      Loading.hide();
      // 清理打印容器和样式
      setTimeout(() => {
        const printContainer = document.getElementById('print-container');
        if (printContainer) {
          printContainer.remove();
          console.log('打印容器已移除');
        }
        const printStyle = document.getElementById('print-style');
        if (printStyle) {
          printStyle.remove();
        }
        const pageNumberStyle = document.querySelector('.page-number-style');
        if (pageNumberStyle) {
          pageNumberStyle.remove();
        }
        // 恢复原始页面标题
        document.title = originalTitle;
      }, 5000); // 5秒后清理，给用户足够时间打印
    }
  }

  // 为HTML中的图片添加crossorigin属性，支持CORS跨域访问
  function addCrossOriginToImages(html) {
    if (!html) return html;
    // 为所有 <img> 标签添加 crossorigin="anonymous" 属性
    return html.replace(/<img([^>]*)>/gi, (match, attrs) => {
      // 如果已经有crossorigin属性则不重复添加
      if (attrs.includes('crossorigin')) {
        return match;
      }
      return `<img${attrs} crossorigin="anonymous">`;
    });
  }

  // 递归渲染目录树为HTML
  function renderCatalogTree(catalogTree, level, isFirst = false) {
    if (!catalogTree || catalogTree.length === 0) {
      console.log(`renderCatalogTree: 目录树为空或null, level=${level}`);
      return '';
    }

    console.log(`renderCatalogTree: 开始处理，目录数=${catalogTree.length}, level=${level}, isFirst=${isFirst}`);

    let html = '';
    let docCount = 0;

    for (const catalog of catalogTree) {
      const headingLevel = Math.min(level + 1, 6);
      const hasDocuments = catalog.docList && catalog.docList.length > 0;
      const hasChildren = catalog.children && catalog.children.length > 0;

      // 只在有文档或子目录时才渲染该章节
      if (!hasDocuments && !hasChildren) {
        console.log(`--- 跳过目录 ${catalog.name}，没有文档和子目录`);
        continue;
      }

      console.log(`=== 处理目录: ${catalog.name}, 层级: ${level}, 文档数: ${catalog.docList?.length || 0} ===`);

      // 先收集该目录下的内容
      let catalogContent = '';

      // 该目录下的文档
      if (hasDocuments) {
        for (const doc of catalog.docList) {
          docCount++;
          const docId = `doc-${docCount}`;
          const hasContent = !!(doc.contentHtml || doc.contentText);
          console.log(`--- 处理文档 #${docCount}: ${doc.title}, 有内容: ${hasContent}`);
          console.log(`--- 文档 #${docCount} contentHtml长度: ${doc.contentHtml?.length || 0}, contentText长度: ${doc.contentText?.length || 0}`);

          // 为HTML中的图片添加crossorigin属性
          const contentHtmlWithCORS = addCrossOriginToImages(doc.contentHtml);

          catalogContent += `
            <div id="${docId}" style="margin: 20px 0; page-break-inside: avoid;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                <div style="flex: 1;"></div>
                <a href="#toc" style="display: inline-block; color: #1976d2; font-size: 13px; font-weight: 500; text-decoration: none; padding: 5px 12px; background: #e3f2fd; border-radius: 4px;">返回目录 →</a>
              </div>
              <h${Math.min(headingLevel + 1, 6)} style="color: #0d47a1; font-size: ${20 - level}px; font-weight: 600; margin: 0 0 12px 0;">
                ${doc.title}
              </h${Math.min(headingLevel + 1, 6)}>
              <div style="line-height: 1.8; color: #333; font-size: 14px;">
                ${contentHtmlWithCORS || doc.contentText || '<p style="color: #999; font-style: italic;">暂无内容</p>'}
              </div>
            </div>
          `;
        }
      } else {
        console.log(`--- 目录 ${catalog.name} 下没有文档`);
      }

      // 递归渲染子目录
      if (hasChildren) {
        const childrenContent = renderCatalogTree(catalog.children, level + 1, false);
        catalogContent += childrenContent;
      }

      // 只添加内容，不显示模块标题
      if (catalogContent.trim()) {
        html += catalogContent;
      } else {
        console.log(`--- 目录 ${catalog.name} 下没有实际内容，跳过`);
      }
    }

    console.log(`renderCatalogTree: 完成，共生成 ${docCount} 个文档，HTML长度=${html.length}`);
    return html;
  }

  // 生成目录页内容 - 收集所有文档标题
  function renderTableOfContents(catalogTree, level, parentIndex = '', docIndex = { count: 0 }) {
    if (!catalogTree || catalogTree.length === 0) return '';

    let html = '';
    let index = 0;

    for (const catalog of catalogTree) {
      index++;
      const currentIndex = parentIndex ? `${parentIndex}.${index}` : `${index}`;
      const hasDocuments = catalog.docList && catalog.docList.length > 0;
      const hasChildren = catalog.children && catalog.children.length > 0;

      if (!hasDocuments && !hasChildren) continue;

      const indent = (level - 1) * 30;

      if (hasDocuments) {
        catalog.docList.forEach((doc, docIdx) => {
          docIndex.count++;
          const docId = `doc-${docIndex.count}`;
          html += `
            <div style="margin-bottom: 14px; display: flex; align-items: baseline; padding: 8px 0; border-bottom: 1px dashed #e0e0e0;">
              <span style="display: inline-block; width: ${indent}px;"></span>
              <span style="display: inline-block; width: 45px; text-align: left; color: #1a237e; font-weight: 700; font-size: 14px;">${docIndex.count}</span>
              <a href="#${docId}" style="flex: 1; font-weight: 500; color: #1976d2; font-size: 15px; padding-left: 10px; text-decoration: none; display: block; transition: color 0.2s;">${doc.title}</a>
            </div>
          `;
        });
      }

      if (hasChildren) {
        html += renderTableOfContents(catalog.children, level + 1, currentIndex, docIndex);
      }
    }

    return html;
  }

  // ----------------------- 选中 节点 -----------------------------
  const expandedKeys = ref([]);
  const selectedKeys = ref([]);

  function selectHelpDoc(selectedKeys) {
    let key = selectedKeys[0];
    if (key.indexOf(TYPE_CATALOG_PREFIX) > -1) {
      return;
    }

    let helpDocId = key.substr(TYPE_HELP_DOC_PREFIX.length);
    router.push({ path: '/help-doc/detail', query: { helpDocId } });
  }

  // 更新展开节点
  function updateExpandedKeys(helpDocId, helpDocList, catalogList) {
    // 不再重置 expandedKeys，而是添加到现有的展开节点中
    expandedKeys.value.push(TYPE_HELP_DOC_PREFIX + helpDocId);
    selectedKeys.value = [TYPE_HELP_DOC_PREFIX + helpDocId];

    let helpDoc = helpDocList.filter((e) => e.helpDocId === helpDocId);
    let catalogId = null;
    if (helpDoc.length > 0) {
      catalogId = helpDoc[0].helpDocCatalogId;
    }

    if (catalogId) {
      expandedKeys.value.push(TYPE_CATALOG_PREFIX + catalogId);
    }

    let parentId = catalogId;
    while (parentId !== 0) {
      let catalog = catalogList.filter((e) => e.helpDocCatalogId === parentId);
      if (catalog.length > 0) {
        parentId = catalog[0].parentId;
        expandedKeys.value.push(TYPE_CATALOG_PREFIX + catalog[0].helpDocCatalogId);
      } else {
        parentId = 0;
      }
    }
  }

  // ----------------------- 帮助文档 目录 树 -----------------------------
  onMounted(queryHelpDocTree);

  const helpDocTreeData = ref([]);

  const TYPE_CATALOG_PREFIX = 'catalog_';
  const TYPE_HELP_DOC_PREFIX = 'help_doc_';
  //目录默认id为0
  const HELP_DOC_CATALOG_PARENT_ID = 0;
  //查询帮助文档树形结构
  async function queryHelpDocTree() {
    Loading.show();
    try {
      let { data: catalogList } = await helpDocCatalogApi.getAll();
      let { data: helpDocList } = await helpDocApi.getAllHelpDocList();

      //设置前缀
      for (const item of catalogList) {
        item.key = TYPE_CATALOG_PREFIX + item.helpDocCatalogId;
        item.title = item.name;
      }
      //转为map，供递归使用
      let helpDocMap = new Map();
      for (const item of helpDocList) {
        item.key = TYPE_HELP_DOC_PREFIX + item.helpDocId;
        let list = helpDocMap.get(item.helpDocCatalogId);
        if (!list) {
          list = [];
          helpDocMap.set(item.helpDocCatalogId, list);
        }
        list.push(item);
      }

      helpDocTreeData.value = buildHelpDocCatalogTree(catalogList, 0, helpDocMap);

      // 默认展开第一级目录
      const firstLevelCatalogs = catalogList.filter((e) => e.parentId == 0);
      firstLevelCatalogs.forEach((catalog) => {
        expandedKeys.value.push(TYPE_CATALOG_PREFIX + catalog.helpDocCatalogId);
      });

      console.log('=== 第一级目录已展开 ===');
      console.log('展开的目录:', firstLevelCatalogs.map(c => c.name));
      console.log('expandedKeys.value:', expandedKeys.value);

      if (!route.query.helpDocId && firstHelpDocId) {
        selectHelpDoc([TYPE_HELP_DOC_PREFIX + firstHelpDocId]);
        return;
      }

      //更新展开节点
      updateExpandedKeys(route.query.helpDocId, helpDocList, catalogList);
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }

  // 记录第一个树
  let firstHelpDocId = null;
  // 构建目录树
  function buildHelpDocCatalogTree(data, parentId, helpDocMap) {
    let children = data.filter((e) => e.parentId == parentId) || [];
    //排序
    children = _.sortBy(children, (e) => e.sort);

    let helpDocList = helpDocMap.get(parentId);
    if (helpDocList) {
      //排序
      helpDocList = _.sortBy(helpDocList, (e) => e.sort);
      if (!firstHelpDocId) {
        firstHelpDocId = helpDocList[0].helpDocId;
      }
      children.push(...helpDocList);
    }

    for (const e of children) {
      if (e.key.indexOf(TYPE_HELP_DOC_PREFIX) > -1) {
        continue;
      }
      e.isLeaf = false;
      e.children = buildHelpDocCatalogTree(data, e.helpDocCatalogId, helpDocMap);
    }

    return children;
  }
</script>

<style lang="less" scoped>
  :deep(.ant-layout-header) {
    height: auto;
  }
  :deep(.layout-header) {
    height: auto;
  }
  :deep(.ant-tree-treenode) {
    margin: 2px 0;
  }

  .help-doc-layout {
    overflow-y: hidden;
    height: 100vh;
    overflow-x: hidden;
  }

  .layout-header {
    background: @primary-color;
    padding: 0;
    z-index: 999;
    color: white;
    height: var(--header-height, 40px);
    line-height: var(--header-height, 40px);
    display: flex;
    justify-content: flex-start;

    .layout-header-title {
      height: var(--header-height, 40px);
      line-height: var(--header-height, 40px);
      padding: 0px 15px 0px 15px;
      z-index: 9999;
      display: flex;
      cursor: pointer;
      justify-content: flex-start;
      margin-bottom: 10px;

      .logo-img {
        width: 40px;
        height: var(--header-height, 40px);
      }
      .title {
        font-size: 18px;
        font-weight: 600;
        margin-left: 10px;
        text-align: center;
        color: '#001529';
      }
      .title-actions {
        margin-left: 20px;
      }
      .avatar {
        position: fixed;
        top: 0;
        right: 18px;
      }
    }
  }

  .layout-container {
    height: calc(100vh - @header-height);
    overflow-x: hidden;
    overflow-y: auto;
  }

  .side-menu {
    height: 100vh;
    overflow: scroll;
    .help-doc-tree {
      color: #001529;
      margin-top: 10px;
      font-size: 16px;
    }
  }

  .help-doc-layout-content {
    min-height: auto;
    position: relative;
    overflow-y: scroll;
    overflow-x: hidden;
    margin-left: 5px;
    margin-top: 8px;
    height: calc(100% - 40px);
  }

  .layout-footer {
    padding: 0 !important;
    position: fixed;
    bottom: 0;
    right: calc(50% - 300px);
    display: flex;
    height: 30px;
    justify-content: center;
  }
</style>
../lib/watermark
