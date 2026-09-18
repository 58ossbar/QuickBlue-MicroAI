<!--
  * 三级等保配置（现代化：总览横幅 + 分组卡片，功能与接口保持不变）
  *
-->
<template>
  <div class="protect-page">
    <!---------- 总览横幅 begin ---------->
    <a-card :bordered="false" class="banner-card">
      <div class="banner">
        <div class="banner-icon">
          <SafetyCertificateOutlined />
        </div>
        <div class="banner-text">
          <div class="banner-title">三级等保配置 <a-tag color="orange" style="font-weight: 600;">三级必备</a-tag></div>
          <div class="banner-desc">
            依据《信息安全技术 网络安全等级保护基本要求》，对登录安全、密码策略、文件安全进行集中管控。以下配置保存后即时生效。
          </div>
        </div>
        <div class="banner-status">
          <a-tag color="success" class="status-tag">
            <template #icon><CheckCircleOutlined /></template>
            安全防护已开启
          </a-tag>
        </div>
      </div>
    </a-card>
    <!---------- 总览横幅 end ---------->

    <a-form
      :model="form"
      :rules="rules"
      ref="formRef"
      layout="vertical"
      autocomplete="off"
    >
      <!---------- 登录安全 begin ---------->
      <a-card :bordered="false" class="group-card">
        <template #title>
          <div class="group-title">
            <LockOutlined class="group-icon" />
            <span>登录安全</span>
          </div>
        </template>
        <a-row :gutter="24">
          <a-col :xs="24" :md="12">
            <a-form-item
              label="配置双因子登录模式"
              extra="在用户登录时，需要同时提供用户名和密码以及其他形式的身份验证信息，例如短信验证码等"
            >
              <a-switch v-model:checked="form.twoFactorLoginEnabled" checked-children="开启" un-checked-children="关闭" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item
              name="loginFailMaxTimes"
              label="最大连续登录失败次数"
              extra="连续登录失败超过该次数则锁定账号；默认 5 次；0 表示不锁定"
            >
              <a-input-number :min="0" :max="10" v-model:value="form.loginFailMaxTimes" placeholder="最大连续登录失败次数" addon-after="次" class="full-width" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item
              name="loginFailLockMinutes"
              label="连续登录失败锁定分钟"
              extra="连续登录失败后锁定的时长；默认 30 分钟；0 表示不锁定"
            >
              <a-input-number :min="0" v-model:value="form.loginFailLockMinutes" placeholder="连续登录失败锁定分钟" addon-after="分钟" class="full-width" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item
              name="loginActiveTimeoutMinutes"
              label="登录后无操作自动退出分钟"
              extra="如登录后 1 小时无操作自动退出当前登录状态；默认 30 分钟"
            >
              <a-input-number :min="-1" v-model:value="form.loginActiveTimeoutMinutes" placeholder="登录后无操作自动退出分钟" addon-after="分钟" class="full-width" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>
      <!---------- 登录安全 end ---------->

      <!---------- 密码策略 begin ---------->
      <a-card :bordered="false" class="group-card">
        <template #title>
          <div class="group-title">
            <KeyOutlined class="group-icon" />
            <span>密码策略</span>
          </div>
        </template>
        <a-row :gutter="24">
          <a-col :xs="24" :md="12">
            <a-form-item
              label="开启密码复杂度"
              extra="密码长度为 8-20 位且必须包含字母、数字、特殊符号（如：@#$%^&*()_+-=）等三种字符"
            >
              <a-switch v-model:checked="form.passwordComplexityEnabled" checked-children="开启" un-checked-children="关闭" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item
              name="regularChangePasswordMonths"
              label="定期修改密码时间间隔"
              extra="定期修改密码时间间隔，默认 3 个月"
            >
              <a-input-number :min="-1" :max="6" v-model:value="form.regularChangePasswordMonths" placeholder="定期修改密码时间间隔" addon-after="月" class="full-width" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item
              name="regularChangePasswordNotAllowRepeatTimes"
              label="定期修改密码不允许重复次数"
              extra="定期修改密码不允许重复次数，默认 3 次以内密码不能相同"
            >
              <a-input-number
                :min="-1"
                :max="6"
                v-model:value="form.regularChangePasswordNotAllowRepeatTimes"
                placeholder="相同密码不允许重复次数"
                addon-after="次"
                class="full-width"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>
      <!---------- 密码策略 end ---------->

      <!---------- 文件安全 begin ---------->
      <a-card :bordered="false" class="group-card">
        <template #title>
          <div class="group-title">
            <FileProtectOutlined class="group-icon" />
            <span>文件安全</span>
          </div>
        </template>
        <a-row :gutter="24">
          <a-col :xs="24" :md="12">
            <a-form-item
              label="文件安全检测"
              extra="对文件类型、恶意文件进行检测（具体请看后端 SecurityFileService.checkFile 方法）"
            >
              <a-switch v-model:checked="form.fileDetectFlag" checked-children="开启" un-checked-children="关闭" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item
              name="maxUploadFileSizeMb"
              label="上传文件大小限制"
              extra="上传文件大小限制，默认 50 mb（0 表示不限制）"
            >
              <a-input-number :min="0" v-model:value="form.maxUploadFileSizeMb" placeholder="上传文件大小限制" addon-after="mb(兆)" class="full-width" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>
      <!---------- 文件安全 end ---------->

      <!---------- 操作区 begin ---------->
      <a-card :bordered="false" class="group-card action-card">
        <a-space :size="16">
          <a-button type="primary" @click.prevent="onSubmit">
            <template #icon><SaveOutlined /></template>
            保存配置
          </a-button>
          <a-button @click="reset">
            <template #icon><UndoOutlined /></template>
            恢复三级等保默认配置
          </a-button>
          <a-button danger @click="clear">
            <template #icon><DeleteOutlined /></template>
            清除所有配置
          </a-button>
        </a-space>
      </a-card>
      <!---------- 操作区 end ---------->
    </a-form>
  </div>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import {
    SafetyCertificateOutlined,
    CheckCircleOutlined,
    LockOutlined,
    KeyOutlined,
    FileProtectOutlined,
    SaveOutlined,
    UndoOutlined,
    DeleteOutlined,
  } from '@ant-design/icons-vue';
  import { level3ProtectApi } from '/@/api/support/level3-protect-api.js';
  import { Loading } from '/@/components/framework/loading/index.js';
  import { sentry } from '/@/lib/sentry.js';
  import { message, Modal } from 'ant-design-vue';

  // 三级等保的默认值
  const protectDefaultValues = {
    // 连续登录失败次数则锁定
    loginFailMaxTimes: 5,
    // 连续登录失败锁定分钟
    loginFailLockMinutes: 30,
    // 最低活跃时间分钟
    loginActiveTimeoutMinutes: 30,
    // 密码复杂度
    passwordComplexityEnabled: true,
    // 定期修改密码时间间隔 月份
    regularChangePasswordMonths: 3,
    // 定期修改密码不允许重复次数，默认：3次以内密码不能相同
    regularChangePasswordNotAllowRepeatTimes: 3,
    // 开启双因子登录
    twoFactorLoginEnabled: true,
    // 文件检测，默认：不开启
    fileDetectFlag: true,
    // 文件大小限制，单位 mb ，(默认：50 mb)
    maxUploadFileSizeMb: 50,
  };

  // 三级等保的不保护的默认值
  const noProtectDefaultValues = {
    // 连续登录失败次数则锁定
    loginFailMaxTimes: 0,
    // 连续登录失败锁定分钟
    loginFailLockMinutes: 0,
    // 最低活跃时间分钟
    loginActiveTimeoutMinutes: 0,
    // 密码复杂度
    passwordComplexityEnabled: false,
    // 定期修改密码时间间隔 月份
    regularChangePasswordMonths: 0,
    // 定期修改密码不允许重复次数，
    regularChangePasswordNotAllowRepeatTimes: 0,
    // 开启双因子登录
    twoFactorLoginEnabled: false,
    // 文件大小限制，单位 mb ，
    maxUploadFileSizeMb: 0,
  };

  // 三级等保配置表单
  const form = reactive({
    ...protectDefaultValues,
  });

  const rules = {
    loginFailMaxTimes: [{ required: true, message: '请输入 最大连续登录失败次数' }],
    loginFailLockMinutes: [{ required: true, message: '请输入 连续登录失败锁定分钟' }],
    loginActiveTimeoutMinutes: [{ required: true, message: '请输入 最低活跃时间分钟' }],
    regularChangePasswordMonths: [{ required: true, message: '请输入 定期修改密码时间间隔' }],
    regularChangePasswordNotAllowRepeatTimes: [{ required: true, message: '请输入 定期修改密码时间间隔' }],
    maxUploadFileSizeMb: [{ required: true, message: '请输入 上传文件大小限制' }],
  };

  //获取配置
  async function getConfig() {
    Loading.show();
    try {
      let res = await level3ProtectApi.getConfig();
      if (!res.data) {
        message.warn('当前未配置三级等保');
        return;
      }
      let json = JSON.parse(res.data);
      form.loginFailMaxTimes = json.loginFailMaxTimes;
      form.loginFailLockMinutes = json.loginFailLockMinutes;
      form.loginActiveTimeoutMinutes = json.loginActiveTimeoutMinutes;
      form.passwordComplexityEnabled = json.passwordComplexityEnabled;
      form.regularChangePasswordMonths = json.regularChangePasswordMonths;
      form.regularChangePasswordNotAllowRepeatTimes = json.regularChangePasswordNotAllowRepeatTimes;
      form.twoFactorLoginEnabled = json.twoFactorLoginEnabled;
      form.maxUploadFileSizeMb = json.maxUploadFileSizeMb;
      form.fileDetectFlag = json.fileDetectFlag;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }

  onMounted(getConfig);

  const formRef = ref();
  // 提交修改
  function onSubmit() {
    formRef.value
      .validate()
      .then(save)
      .catch((error) => {
        message.error('参数验证错误，请仔细填写表单数据!');
      });
  }

  // 提交修改配置
  async function save() {
    Loading.show();
    try {
      let res = await level3ProtectApi.updateConfig(form);
      message.success(res.msg);
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }

  // 重置
  function reset() {
    Object.assign(form, protectDefaultValues);
    save();
  }

  // 清除所有配置
  function clear() {
    Modal.confirm({
      title: '提示',
      content: '确定要清除三级等保配置吗?这样系统不安全哦',
      okText: '清除三级等保配置',
      okType: 'danger',
      onOk() {
        Object.assign(form, noProtectDefaultValues);
        save();
      },
      cancelText: '取消',
      onCancel() {},
    });
  }
</script>

<style lang="less" scoped>
  .protect-page {
    .full-width {
      width: 100%;
    }

    .banner-card {
      margin-bottom: 16px;

      .banner {
        display: flex;
        align-items: center;
        gap: 20px;

        .banner-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 64px;
          height: 64px;
          border-radius: 16px;
          font-size: 34px;
          color: #1677ff;
          background: linear-gradient(135deg, rgba(22, 119, 255, 0.12), rgba(22, 119, 255, 0.04));
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

        .banner-status {
          flex-shrink: 0;

          .status-tag {
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 13px;
          }
        }
      }
    }

    .group-card {
      margin-bottom: 16px;

      :deep(.ant-card-head) {
        border-bottom: 1px solid rgba(0, 0, 0, 0.06);
      }

      .group-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        font-weight: 600;

        .group-icon {
          color: #1677ff;
        }
      }
    }

    .action-card {
      :deep(.ant-card-body) {
        padding: 16px 24px;
      }
    }

    :deep(.ant-form-item-label > label) {
      font-weight: 500;
    }

    :deep(.ant-form-item-extra) {
      font-size: 12px;
    }
  }

  /* 暗色模式适配 */
  :global([data-theme='dark']) {
    .banner-text {
      .banner-title {
        color: rgba(255, 255, 255, 0.88) !important;
      }

      .banner-desc {
        color: rgba(255, 255, 255, 0.55) !important;
      }
    }

    .banner-icon {
      background: linear-gradient(135deg, rgba(22, 119, 255, 0.25), rgba(22, 119, 255, 0.1)) !important;
    }
  }
</style>
