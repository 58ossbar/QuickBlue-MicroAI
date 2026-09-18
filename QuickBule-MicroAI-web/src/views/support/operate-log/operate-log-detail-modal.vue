<template>
  <a-modal :open="visible" title="操作记录详情" width="85%" :footer="null" @cancel="close" class="operate-log-detail-modal">
    <!-- 标签页区域 - 放在最上面 -->
    <a-tabs v-model:activeKey="activeTab" class="main-tabs">
      <!-- 基础信息标签页 -->
      <a-tab-pane key="basic" tab="基础信息">
        <div class="tab-content-wrapper">
          <div class="info-section">
            <div class="section-title">
              <InfoCircleOutlined class="title-icon" />
              操作人信息
            </div>
            <a-row :gutter="[24, 16]">
              <a-col :span="8">
                <div class="info-item">
                  <span class="info-label">操作人：</span>
                  <span class="info-value">{{ detail.operateUserName }}</span>
                  <a-tag class="info-tag">ID: {{ detail.operateUserId }}</a-tag>
                </div>
              </a-col>
              <a-col :span="8">
                <div class="info-item">
                  <span class="info-label">操作时间：</span>
                  <span class="info-value">{{ detail.createTime }}</span>
                </div>
              </a-col>
              <a-col :span="8">
                <div class="info-item">
                  <span class="info-label">客户端：</span>
                  <span class="info-value">{{ detail.os }} / {{ detail.browser }}</span>
                  <span v-if="detail.device" class="info-value">/ {{ detail.device }}</span>
                </div>
              </a-col>
              <a-col :span="8">
                <div class="info-item">
                  <span class="info-label">IP地址：</span>
                  <span class="info-value">{{ detail.ip }}</span>
                </div>
              </a-col>
              <a-col :span="8">
                <div class="info-item">
                  <span class="info-label">IP地区：</span>
                  <span class="info-value">{{ formatIpRegion(detail.ipRegion) }}</span>
                  <a-tag v-if="isInternalIp(detail.ip)" class="internal-ip-tag" color="orange">内网IP</a-tag>
                </div>
              </a-col>
              <a-col :span="8">
                <div class="info-item">
                  <span class="info-label">用户类型：</span>
                  <a-tag color="blue">{{ detail.operateUserType === 1 ? '员工' : '用户' }}</a-tag>
                </div>
              </a-col>
            </a-row>
          </div>

          <div class="info-section">
            <div class="section-title">
              <FileTextOutlined class="title-icon" />
              操作详情
            </div>
            <a-row :gutter="[24, 16]">
              <a-col :span="24">
                <div class="info-item-full">
                  <span class="info-label">操作模块：</span>
                  <a-tag color="blue" size="large">{{ detail.module || '未知模块' }}</a-tag>
                </div>
              </a-col>
              <a-col :span="24">
                <div class="info-item-full">
                  <span class="info-label">操作内容：</span>
                  <span class="info-value highlight">{{ detail.content || '无描述' }}</span>
                </div>
              </a-col>
              <a-col :span="24">
                <div class="info-item-full">
                  <span class="info-label">操作状态：</span>
                  <a-tag :color="detail.successFlag ? 'success' : 'error'" size="large">
                    {{ detail.successFlag ? '✓ 操作成功' : '✗ 操作失败' }}
                  </a-tag>
                </div>
              </a-col>
            </a-row>
          </div>
        </div>
      </a-tab-pane>

      <!-- 请求参数标签页 -->
      <a-tab-pane key="param" tab="请求参数">
        <div class="tab-content-wrapper">
          <div class="url-info-section">
            <div class="url-info-item">
              <span class="url-label">请求URL：</span>
              <a-typography-text code>{{ detail.url }}</a-typography-text>
            </div>
            <div class="url-info-item">
              <span class="url-label">调用方法：</span>
              <a-typography-text code>{{ detail.method }}</a-typography-text>
            </div>
          </div>
          <div class="json-wrapper">
            <div class="json-header">
              <span class="json-title">请求参数</span>
              <a-button size="small" @click="copyJson(detail.param)">
                <template #icon><CopyOutlined /></template>
                复制全部
              </a-button>
            </div>
            <div class="json-container">
              <JsonViewer :value="detail.param ? parseJson(detail.param) : {}" :expanded="true" :expandDepth="2" copyable boxed sort theme="light" />
            </div>
          </div>
        </div>
      </a-tab-pane>

      <!-- 返回结果标签页 -->
      <a-tab-pane key="response" tab="返回结果">
        <div class="tab-content-wrapper">
          <div class="json-wrapper">
            <div class="json-header">
              <span class="json-title">返回结果</span>
              <a-button size="small" @click="copyJson(detail.response)">
                <template #icon><CopyOutlined /></template>
                复制全部
              </a-button>
            </div>
            <div class="json-container">
              <JsonViewer :value="detail.response ? parseJson(detail.response) : {}" :expanded="true" :expandDepth="2" copyable boxed sort theme="light" />
            </div>
          </div>
        </div>
      </a-tab-pane>

      <!-- 失败原因标签页 -->
      <a-tab-pane key="failReason" tab="失败原因" v-if="detail.failReason">
        <div class="tab-content-wrapper">
          <a-alert type="error" :message="detail.failReason" show-icon class="fail-alert-full" />
        </div>
      </a-tab-pane>
    </a-tabs>
  </a-modal>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { JsonViewer } from 'vue3-json-viewer';
  import { InfoCircleOutlined, FileTextOutlined, CopyOutlined } from '@ant-design/icons-vue';
  import { message } from 'ant-design-vue';
  import { operateLogApi } from '/@/api/support/operate-log-api';
  import { sentry } from '/@/lib/sentry';
  import { Loading } from '/@/components/framework/loading';
  import uaparser from 'ua-parser-js';

  defineExpose({
    show,
  });

  const visible = ref(false);
  const activeTab = ref('basic'); // 默认显示基础信息标签页

  // 格式化IP地区显示
  function formatIpRegion(ipRegion) {
    if (!ipRegion || ipRegion === '0|0|0|内网IP|内网IP' || ipRegion === '0|0|0|内网IP') {
      return '未知地区';
    }

    // 处理格式：国家|省份|城市|网络运营商
    const parts = ipRegion.split('|');
    if (parts.length < 4) {
      return ipRegion;
    }

    const country = parts[0] || '';
    const province = parts[1] || '';
    const city = parts[2] || '';
    const isp = parts[3] || '';

    // 如果国家是0，说明无法识别
    if (country === '0') {
      return '未知地区';
    }

    // 拼接地区信息
    let result = '';
    if (country && country !== '0') {
      result += country;
    }
    if (province && province !== '0') {
      result += (result ? '·' : '') + province;
    }
    if (city && city !== '0') {
      result += (result ? '·' : '') + city;
    }
    if (isp && isp !== '0' && isp !== '内网IP') {
      result += (result ? ' (' : '') + isp + ')';
    }

    return result || '未知地区';
  }

  // 判断是否为内网IP
  function isInternalIp(ip) {
    if (!ip) return false;

    // 内网IP范围
    const internalIpRanges = [
      /^10\./,
      /^172\.(1[6-9]|2[0-9]|3[0-1])\./,
      /^192\.168\./,
      /^127\./,
      /^0\./,
      /^::1$/,
      /^localhost$/i
    ];

    return internalIpRanges.some(range => range.test(ip));
  }

  function show(operateLogId) {
    visible.value = true;
    clear(detail);
    getDetail(operateLogId);
  }

  function parseJson(str) {
    try {
      return JSON.parse(str);
    } catch (e) {
      return str;
    }
  }

  function copyJson(str) {
    try {
      navigator.clipboard.writeText(str).then(() => {
        message.success('复制成功');
      });
    } catch (e) {
      message.error('复制失败');
    }
  }

  const clear = (info) => {
    const keys = Object.keys(info);
    let obj = {};
    keys.forEach((item) => {
      obj[item] = '';
    });
    Object.assign(info, obj);
  };

  function close() {
    visible.value = false;
  }

  let detail = reactive({
    param: '',
    url: '',
  });

  async function getDetail(operateLogId) {
    try {
      Loading.show();
      let res = await operateLogApi.detail(operateLogId);
      detail = Object.assign(detail, res.data);
      let ua = uaparser(res.data.userAgent);
      detail.browser = ua.browser.name;
      detail.os = ua.os.name;
      detail.device = ua.device.vendor ? ua.device.vendor + ua.device.model : '';
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }
</script>

<style scoped lang="less">
  .operate-log-detail-modal {
    :deep(.ant-modal-body) {
      padding: 0 !important;
    }
  }

  .main-tabs {
    :deep(.ant-tabs-nav) {
      padding: 0 20px;
      margin-bottom: 0;
      background: #fafafa;
      border-bottom: 1px solid #f0f0f0;
    }

    :deep(.ant-tabs-content) {
      padding: 0;
    }

    :deep(.ant-tabs-tab) {
      padding: 16px 20px;
      font-size: 15px;
    }

    :deep(.ant-tabs-tab-active) {
      font-weight: 600;
    }
  }

  .tab-content-wrapper {
    padding: 24px;
    max-height: 70vh;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 8px;
    }

    &::-webkit-scrollbar-track {
      background: #f1f1f1;
    }

    &::-webkit-scrollbar-thumb {
      background: #ccc;
      border-radius: 4px;

      &:hover {
        background: #999;
      }
    }
  }

  .info-section {
    background: #fafafa;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 16px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #333;
    display: flex;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid #e8e8e8;

    .title-icon {
      margin-right: 8px;
      color: #1890ff;
      font-size: 18px;
    }
  }

  .info-item {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    line-height: 1.8;
  }

  .info-item-full {
    display: flex;
    align-items: center;
    gap: 12px;
    line-height: 2;
  }

  .info-label {
    color: #666;
    font-weight: 500;
    font-size: 14px;
    min-width: 80px;
  }

  .info-value {
    color: #333;
    font-size: 14px;
  }

  .info-value.highlight {
    color: #1890ff;
    font-weight: 500;
    font-size: 15px;
  }

  .info-tag {
    font-size: 12px;
  }

  .internal-ip-tag {
    font-size: 11px;
    margin-left: 8px;
  }

  .url-info-section {
    background: #fafafa;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
  }

  .url-info-item {
    margin-bottom: 12px;
    font-size: 14px;

    &:last-child {
      margin-bottom: 0;
    }

    .url-label {
      color: #666;
      font-weight: 500;
      margin-right: 8px;
    }
  }

  .json-wrapper {
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    overflow: hidden;
    background: #fff;
  }

  .json-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #fafafa;
    border-bottom: 1px solid #e8e8e8;

    .json-title {
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }
  }

  .json-container {
    padding: 20px;
    max-height: 60vh;
    overflow-y: auto;
    background: #fff;

    &::-webkit-scrollbar {
      width: 10px;
      height: 10px;
    }

    &::-webkit-scrollbar-track {
      background: #f5f5f5;
    }

    &::-webkit-scrollbar-thumb {
      background: #ccc;
      border-radius: 5px;

      &:hover {
        background: #999;
      }
    }
  }

  .fail-alert-full {
    white-space: pre-wrap;
    word-break: break-all;
    font-size: 13px;
    line-height: 1.8;
  }
</style>
