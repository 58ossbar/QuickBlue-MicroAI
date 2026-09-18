<!-- App.vue -->
<template>
  <a-config-provider
      :locale="antdLocale"
      :theme="{
      algorithm: themeAlgorithm,
      token: {
        colorPrimary: themeColors[colorIndex].primaryColor,
        colorLink: themeColors[colorIndex].primaryColor,
        colorLinkActive: themeColors[colorIndex].activeColor,
        colorLinkHover: themeColors[colorIndex].hoverColor,
        colorIcon: themeColors[colorIndex].primaryColor,
        borderRadius: borderRadius,
        fontSize: fontSize,
      },
      components: {
        Button: {
          colorLink: themeColors[colorIndex].primaryColor,
          colorLinkActive: themeColors[colorIndex].activeColor,
          colorLinkHover: themeColors[colorIndex].hoverColor,
        },
        Icon: {
          colorIcon: themeColors[colorIndex].primaryColor,
        },
      },
    }"
      :transformCellText="transformCellText"
  >
    <!-- 优化全局加载提示 -->
    <a-spin
        :spinning="spinning"
        :tip="loadingText"
        size="large"
        :delay="200"
        class="qb-spin"
    >
      <RouterView />
    </a-spin>
  </a-config-provider>
</template>

<script setup>
import dayjs from 'dayjs';
import { computed, h, ref, watch } from 'vue';
import { messages } from '/@/i18n';
import { useAppConfigStore } from '/@/store/modules/system/app-config';
import { useSpinStore } from '/@/store/modules/system/spin';
import { Popover, theme } from 'ant-design-vue';
import { themeColors } from '/@/theme/color.js';
import CopyIcon from '/@/components/framework/copy-icon/index.vue';

const antdLocale = computed(() => messages[useAppConfigStore().language].antdLocale);
const dayjsLocale = computed(() => messages[useAppConfigStore().language].dayjsLocale);
dayjs.locale(dayjsLocale);

// 全局加载状态
const spinStore = useSpinStore();
const spinning = computed(() => spinStore.loading);
const loadingText = ref('加载中...');

// 动态提示语
const tips = [
  '加载中，请稍候...',
  '正在准备数据...',
  '优化界面中...',
  '检查系统状态...'
];

let tipTimer = null;

// 监听加载状态变化
watch(spinning, (isLoading) => {
  if (isLoading) {
    let index = 0;
    loadingText.value = tips[0];

    tipTimer = setInterval(() => {
      index = (index + 1) % tips.length;
      loadingText.value = tips[index];
    }, 3000);
  } else {
    if (tipTimer) {
      clearInterval(tipTimer);
      tipTimer = null;
    }
  }
});

// 其他原有代码保持不变...
const colorIndex = computed(() => useAppConfigStore().colorIndex);
const themeAlgorithm = computed(() => {
  let themeArray = [];
  themeArray.push(useAppConfigStore().darkModeFlag ? theme.darkAlgorithm : theme.defaultAlgorithm);
  if (useAppConfigStore().compactFlag) {
    themeArray.push(theme.compactAlgorithm);
  }
  return themeArray;
});
const compactFlag = computed(() => useAppConfigStore().compactFlag);
const darkModeFlag = computed(() => useAppConfigStore().darkModeFlag);
const borderRadius = computed(() => useAppConfigStore().borderRadius);
const fontSize = computed(() => useAppConfigStore().fontSize);
const menuCollapsedShow = computed(() => useAppConfigStore().menuCollapsedShow);

// 页面紧凑模式 & 菜单高度 CSS 变量（所有组件的 header/menu 高度通过此变量动态调整）
watch([compactFlag, fontSize], () => {
  document.documentElement.style.setProperty('--header-height', compactFlag.value ? '32px' : '40px');
  document.documentElement.style.setProperty('--top-menu-height', compactFlag.value ? '40px' : '48px');
}, { immediate: true });

// 暗色模式：为 html 设置 data-theme 属性（供全局 less 选择器适配），并切换页面底色
watch(darkModeFlag, (isDark) => {
  document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
  document.body.style.backgroundColor = isDark ? '#0f0f0f' : '#f8f8f8';
  document.body.style.colorScheme = isDark ? 'dark' : 'light';
}, { immediate: true });

// 监听字体大小变化并应用到全局
watch(fontSize, (newFontSize) => {
  // 设置根元素字体大小
  document.documentElement.style.fontSize = newFontSize + 'px';
  // 设置全局CSS变量
  document.documentElement.style.setProperty('--global-font-size', newFontSize + 'px');

  // 更新全局字体大小样式
  let styleElement = document.getElementById('dynamic-font-size-style');
  if (!styleElement) {
    styleElement = document.createElement('style');
    styleElement.id = 'dynamic-font-size-style';
    document.head.appendChild(styleElement);
  }

  const baseFontSize = newFontSize;
  styleElement.innerHTML = `
    html, body {
      font-size: ${baseFontSize}px !important;
    }

    :deep(div),
    :deep(span),
    :deep(p),
    :deep(h1),
    :deep(h2),
    :deep(h3),
    :deep(h4),
    :deep(h5),
    :deep(h6),
    :deep(a),
    :deep(button),
    :deep(input),
    :deep(select),
    :deep(textarea),
    :deep(label),
    :deep(.ant-form-item-label),
    :deep(.ant-btn),
    :deep(.ant-input),
    :deep(.ant-select),
    :deep(.ant-table),
    :deep(.ant-table-cell),
    :deep(.ant-menu-item),
    :deep(.ant-menu-title-content),
    :deep(.ant-card),
    :deep(.ant-modal),
    :deep(.ant-tag),
    :deep(.ant-typography) {
      font-size: ${baseFontSize}px !important;
    }
  `;
}, { immediate: true });

function transformCellText({ text, column, record, index }) {
  if (column && column.textEllipsisFlag === true) {
    return h(
        Popover,
        { placement: 'bottom' },
        {
          default: () =>
              h(
                  'div',
                  {
                    style: { whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
                    id: `${column.dataIndex}${index}`,
                  },
                  text
              ),
          content: () =>
              h('div', { style: { display: 'flex' } }, [
                h('div', text),
                h(CopyIcon, { value: document.getElementById(`${column.dataIndex}${index}`).innerText }),
              ]),
        }
    );
  } else {
    return text;
  }
}

const { useToken } = theme;
const { token } = useToken();
</script>

<style lang="less">
@color-bg-container: v-bind('token.colorBgContainer');

/* 优化加载样式 */
.qb-spin {
  min-height: 100vh;

  :deep(.ant-spin-text) {
    margin-top: 12px;
    font-size: 14px;
    color: #666;
    font-weight: 500;
  }

  :deep(.ant-spin-dot) {
    font-size: 32px;
  }
}

/* 原有样式保持不变 */
:deep(.ant-table-column-sorters) {
  align-items: flex-start !important;
}

.qb-query-form {
  background-color: @color-bg-container;
  padding: 16px 20px;
  margin-bottom: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.25s, border-color 0.25s, background-color 0.25s;

  &:hover {
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
  }
}

.qb-detail-header {
  background-color: @color-bg-container;
  padding: 10px;
}

/* 卡片现代化：统一圆角、轻阴影与细分隔线（所有列表/详情卡片生效） */
.ant-card {
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.25s, border-color 0.25s;

  &:hover {
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
  }
}
</style>
