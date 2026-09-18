<!--
  * 数据脱敏（现代化：说明横幅 + 演示表格，接口保持不变）
  *
-->
<template>
  <div class="masking-page">
    <!---------- 说明横幅 begin ---------->
    <a-card :bordered="false" class="banner-card">
      <div class="banner">
        <div class="banner-icon">
          <SafetyCertificateOutlined />
        </div>
        <div class="banner-text">
          <div class="banner-title">数据脱敏 Data Masking <a-tag color="green" style="font-weight: 600;">二级必备</a-tag></div>
          <div class="banner-desc">
            《信息安全技术 网络安全等级保护基本要求》明确规定，二级以上保护等级需对敏感数据进行脱敏处理。数据脱敏是对身份证号、手机号、卡号、邮箱等敏感信息按规则变形，实现敏感隐私数据的可靠保护。
          </div>
        </div>
      </div>
      <div class="usage">
        <div class="usage-title">使用方式</div>
        <div class="usage-item">
          <span class="tag">注解</span>
          <code>@DataMasking(DataMaskingEnum.PHONE)</code>
          <span class="desc">标注在 VO 字段上，接口返回时自动脱敏（手机号 / 身份证 / 银行卡 / 邮箱 / 姓名 / 地址）</span>
        </div>
        <div class="usage-item">
          <span class="tag">工具类</span>
          <code>DataMaskingService.maskEmail(value)</code>
          <span class="desc">Service 层手动调用，灵活控制脱敏场景</span>
        </div>
      </div>
    </a-card>
    <!---------- 说明横幅 end ---------->

    <a-card :bordered="false" class="table-card">
      <a-form class="qb-query-form">
        <a-row class="qb-query-form-row">
          <a-form-item class="qb-query-form-item qb-margin-left10">
            <a-button-group>
              <a-button type="primary" @click="onSearch">
                <template #icon>
                  <SearchOutlined />
                </template>
                查询
              </a-button>
            </a-button-group>
          </a-form-item>
        </a-row>
      </a-form>

      <a-table
        ref="tableRef"
        size="small"
        bordered
        :scroll="{ x: 1100 }"
        :loading="tableLoading"
        class="qb-margin-top10"
        :dataSource="tableData"
        :columns="columns"
        :pagination="false"
      />
    </a-card>
  </div>
</template>

<script setup>
  import { onMounted, ref } from 'vue';
  import { SearchOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue';
  import { sentry } from '/@/lib/sentry';
  import { dataMaskingApi } from '/@/api/support/data-masking-api.js';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  //------------------------ 表格渲染 ---------------------

  const tableRef = ref();
  const columns = ref([
    {
      title: '默认',
      dataIndex: 'other',
      width: 100,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 100,
    },
    {
      title: '身份证',
      dataIndex: 'idCard',
      width: 150,
    },
    {
      title: '密码',
      dataIndex: 'password',
      width: 100,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      width: 120,
    },
    {
      title: '车牌号',
      dataIndex: 'carLicense',
      width: 120,
    },
    {
      title: '银行卡',
      dataIndex: 'bankCard',
      width: 170,
    },
    {
      title: '地址',
      dataIndex: 'address',
      width: 210,
    },
  ]);

  useColumnResize(tableRef, columns);

  const tableLoading = ref(false);
  const tableData = ref([]);

  function onSearch() {
    ajaxQuery();
  }

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseModel = await dataMaskingApi.query();
      tableData.value = responseModel.data;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  onMounted(ajaxQuery);
</script>

<style lang="less" scoped>
  .masking-page {
    .banner-card {
      margin-bottom: 16px;

      .banner {
        display: flex;
        align-items: center;
        gap: 20px;
        margin-bottom: 16px;

        .banner-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 64px;
          height: 64px;
          border-radius: 16px;
          font-size: 34px;
          color: #52c41a;
          background: linear-gradient(135deg, rgba(82, 196, 26, 0.14), rgba(82, 196, 26, 0.04));
          flex-shrink: 0;
        }

        .banner-text {
          flex: 1;
          min-width: 0;

          .banner-title {
            font-size: 18px;
            font-weight: 600;
            color: rgba(0, 0, 0, 0.88);
          }

          .banner-desc {
            margin-top: 6px;
            font-size: 13px;
            color: rgba(0, 0, 0, 0.55);
            line-height: 1.6;
          }
        }
      }

      .usage {
        padding: 14px 16px;
        border-radius: 8px;
        background: rgba(82, 196, 26, 0.05);
        border: 1px solid rgba(82, 196, 26, 0.12);

        .usage-title {
          font-size: 13px;
          font-weight: 600;
          margin-bottom: 10px;
          color: rgba(0, 0, 0, 0.65);
        }

        .usage-item {
          display: flex;
          align-items: center;
          gap: 10px;
          flex-wrap: wrap;
          margin-bottom: 6px;
          font-size: 13px;

          &:last-child {
            margin-bottom: 0;
          }

          .tag {
            flex-shrink: 0;
            padding: 1px 8px;
            border-radius: 4px;
            font-size: 12px;
            color: #52c41a;
            background: rgba(82, 196, 26, 0.1);
            border: 1px solid rgba(82, 196, 26, 0.25);
          }

          code {
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            background: rgba(0, 0, 0, 0.05);
            border: 1px solid rgba(0, 0, 0, 0.08);
          }

          .desc {
            color: rgba(0, 0, 0, 0.45);
          }
        }
      }
    }

    .table-card {
      :deep(.ant-card-body) {
        padding-bottom: 8px;
      }
    }
  }

  /* 暗色模式适配 */
  :global([data-theme='dark']) {
    .banner-title {
      color: rgba(255, 255, 255, 0.88) !important;
    }

    .banner-desc,
    .usage-title,
    .desc {
      color: rgba(255, 255, 255, 0.55) !important;
    }

    .usage {
      background: rgba(82, 196, 26, 0.08) !important;
      border-color: rgba(82, 196, 26, 0.2) !important;
    }

    .usage-item code {
      background: rgba(255, 255, 255, 0.08) !important;
      border-color: rgba(255, 255, 255, 0.12) !important;
    }
  }
</style>
