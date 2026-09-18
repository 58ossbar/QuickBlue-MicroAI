<!--
  * 配置对比组件 (Diff视图)
  *
-->
<template>
  <a-modal
    v-model:open="visible"
    title="配置对比"
    :width="1100"
    :footer="null"
  >
    <div class="diff-header">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-select
            v-model:value="leftVersion"
            placeholder="选择版本"
            style="width: 100%"
            @change="onVersionChange('left')"
          >
            <a-select-option v-for="item in versionList" :key="item.id" :value="item.id">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :span="12">
          <a-select
            v-model:value="rightVersion"
            placeholder="选择版本"
            style="width: 100%"
            @change="onVersionChange('right')"
          >
            <a-select-option v-for="item in versionList" :key="item.id" :value="item.id">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-col>
      </a-row>
    </div>

    <div class="diff-container">
      <div class="diff-pane">
        <div class="diff-title">{{ leftTitle }}</div>
        <div class="diff-content">
          <div v-html="leftDiffHtml" class="diff-html"></div>
        </div>
      </div>
      <div class="diff-pane">
        <div class="diff-title">{{ rightTitle }}</div>
        <div class="diff-content">
          <div v-html="rightDiffHtml" class="diff-html"></div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import { nacosConfigApi } from '/src/api/support/nacos-config-api';
  import { sentry } from '/src/lib/sentry';

  const visible = ref(false);
  const leftContent = ref('');
  const rightContent = ref('');
  const leftVersion = ref('');
  const rightVersion = ref('');
  const leftTitle = ref('版本1');
  const rightTitle = ref('版本2');
  const versionList = ref([]);

  const currentConfig = ref({});
  const tenantId = ref('');
  const currentContent = ref(''); // 当前配置内容

  // 简单的行级diff算法
  function computeDiff(oldText, newText) {
    const oldLines = (oldText || '').split('\n');
    const newLines = (newText || '').split('\n');
    
    const oldHtml = [];
    const newHtml = [];
    
    // 使用简单的LCS算法
    const lcs = computeLCS(oldLines, newLines);
    
    let oldIdx = 0;
    let newIdx = 0;
    let lcsIdx = 0;
    
    while (oldIdx < oldLines.length || newIdx < newLines.length) {
      if (lcsIdx < lcs.length && oldIdx < oldLines.length && oldLines[oldIdx] === lcs[lcsIdx]) {
        // 匹配行
        oldHtml.push(`<div class="line unchanged">${escapeHtml(oldLines[oldIdx]) || '&nbsp;'}</div>`);
        oldIdx++;
        lcsIdx++;
      } else if (lcsIdx < lcs.length && newIdx < newLines.length && newLines[newIdx] === lcs[lcsIdx]) {
        // 新增行
        newHtml.push(`<div class="line added">${escapeHtml(newLines[newIdx]) || '&nbsp;'}</div>`);
        newIdx++;
      } else if (oldIdx < oldLines.length && newIdx < newLines.length && 
                 oldLines[oldIdx] !== newLines[newIdx]) {
        // 修改行
        oldHtml.push(`<div class="line removed">${escapeHtml(oldLines[oldIdx]) || '&nbsp;'}</div>`);
        newHtml.push(`<div class="line added">${escapeHtml(newLines[newIdx]) || '&nbsp;'}</div>`);
        oldIdx++;
        newIdx++;
      } else if (oldIdx < oldLines.length) {
        // 删除行
        oldHtml.push(`<div class="line removed">${escapeHtml(oldLines[oldIdx]) || '&nbsp;'}</div>`);
        oldIdx++;
      } else if (newIdx < newLines.length) {
        // 新增行
        newHtml.push(`<div class="line added">${escapeHtml(newLines[newIdx]) || '&nbsp;'}</div>`);
        newIdx++;
      }
    }
    
    return { oldHtml: oldHtml.join(''), newHtml: newHtml.join('') };
  }

  // 计算最长公共子序列
  function computeLCS(arr1, arr2) {
    const m = arr1.length;
    const n = arr2.length;
    const dp = Array(m + 1).fill(null).map(() => Array(n + 1).fill(0));
    
    for (let i = 1; i <= m; i++) {
      for (let j = 1; j <= n; j++) {
        if (arr1[i - 1] === arr2[j - 1]) {
          dp[i][j] = dp[i - 1][j - 1] + 1;
        } else {
          dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
        }
      }
    }
    
    // 回溯找出LCS
    const lcs = [];
    let i = m, j = n;
    while (i > 0 && j > 0) {
      if (arr1[i - 1] === arr2[j - 1]) {
        lcs.unshift(arr1[i - 1]);
        i--;
        j--;
      } else if (dp[i - 1][j] > dp[i][j - 1]) {
        i--;
      } else {
        j--;
      }
    }
    
    return lcs;
  }

  function escapeHtml(text) {
    if (!text) return '';
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  const leftDiffHtml = computed(() => {
    const diff = computeDiff(leftContent.value, rightContent.value);
    return diff.oldHtml;
  });

  const rightDiffHtml = computed(() => {
    const diff = computeDiff(leftContent.value, rightContent.value);
    return diff.newHtml;
  });

  async function showModal(config, nsId) {
    currentConfig.value = config;
    tenantId.value = nsId || '';
    visible.value = true;
    
    // 获取当前配置内容
    try {
      const res = await nacosConfigApi.getConfig(config.dataId, config.groupId, nsId);
      if (res.data) {
        currentContent.value = res.data.content || '';
      }
    } catch (e) {
      sentry.captureError(e);
    }

    // 获取历史版本列表
    await loadVersions();
  }

  async function loadVersions() {
    try {
      const res = await nacosConfigApi.getConfigHistory(
        currentConfig.value.dataId,
        currentConfig.value.groupId,
        tenantId.value,
        1,
        20
      );
      if (res.data) {
        // 添加当前版本
        versionList.value = [
          { id: 'current', label: '当前版本', content: currentContent.value }
        ];
        
        // 添加历史版本
        (res.data.list || []).forEach(item => {
          versionList.value.push({
            id: item.id,
            label: `版本 ${item.id} (${item.lastModifiedTime || ''})`,
            content: item.content,
            time: item.lastModifiedTime
          });
        });

        // 默认选择前两个版本
        if (versionList.value.length >= 2) {
          leftVersion.value = versionList.value[0].id;
          leftContent.value = versionList.value[0].content;
          leftTitle.value = versionList.value[0].label;
          
          rightVersion.value = versionList.value[1].id;
          rightContent.value = versionList.value[1].content || '';
          rightTitle.value = versionList.value[1].label;
        }
      }
    } catch (e) {
      sentry.captureError(e);
    }
  }

  async function onVersionChange(side) {
    const versionId = side === 'left' ? leftVersion.value : rightVersion.value;
    const version = versionList.value.find(v => v.id === versionId);
    
    if (version) {
      if (version.id === 'current') {
        // 当前版本
        const res = await nacosConfigApi.getConfig(
          currentConfig.value.dataId,
          currentConfig.value.groupId,
          tenantId.value
        );
        if (res.data) {
          if (side === 'left') {
            leftContent.value = res.data.content || '';
            leftTitle.value = '当前版本';
          } else {
            rightContent.value = res.data.content || '';
            rightTitle.value = '当前版本';
          }
        }
      } else {
        // 历史版本
        const res = await nacosConfigApi.getConfigHistoryDetail(
          versionId,
          currentConfig.value.dataId,
          currentConfig.value.groupId,
          tenantId.value
        );
        if (res.data) {
          if (side === 'left') {
            leftContent.value = res.data.content || '';
            leftTitle.value = version.label;
          } else {
            rightContent.value = res.data.content || '';
            rightTitle.value = version.label;
          }
        }
      }
    }
  }

  defineExpose({ showModal });
</script>

<style scoped>
.diff-header {
  margin-bottom: 12px;
}

.diff-container {
  display: flex;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
}

.diff-pane {
  flex: 1;
  border-right: 1px solid #d9d9d9;
}

.diff-pane:last-child {
  border-right: none;
}

.diff-title {
  padding: 8px 12px;
  background: #fafafa;
  border-bottom: 1px solid #d9d9d9;
  font-weight: 500;
  font-size: 13px;
}

.diff-content {
  height: 400px;
  overflow: auto;
  background: #1e1e1e;
}

.diff-html {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.diff-html :deep(.line) {
  padding: 0 12px;
  white-space: pre;
  color: #ffffff;
  min-height: 20px;
}

.diff-html :deep(.line.unchanged) {
  background: #1e1e1e;
  color: #ffffff;
}

.diff-html :deep(.line.added) {
  background: #264f36;
  color: #85dc89;
}

.diff-html :deep(.line.removed) {
  background: #5a1e1e;
  color: #f14c4c;
}
</style>
