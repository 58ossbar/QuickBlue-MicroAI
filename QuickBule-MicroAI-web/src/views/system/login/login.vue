<template>
  <div class="login-container">
    <div class="login-card">
    <div class="box-item desc">
      <div class="welcome">
        <p class="title">欢迎使用<br><span class="product-name">QuickBlue</span></p>
        <span class="sub-welcome">企业级应用底座 · 开源版</span>
        <div class="slogan">
          <p>一个平台，整合技术，更整合智能。交付更快，运行更稳。</p>
          <p class="slogan-sub">—— 面向微服务架构的 AI 原生应用加速器</p>
        </div>
      </div>
      <img class="welcome-img" :src="loginGif" alt="Welcome illustration" />
    </div>
    <div class="box-item login">
       <div class="login-title">账号登录</div>
      <a-form ref="formRef" class="login-form" :model="loginForm" :rules="rules">
        <a-form-item name="loginName">
          <a-input v-model:value.trim="loginForm.loginName" placeholder="请输入用户名" />
        </a-form-item>

        <a-form-item name="emailCode" v-if="emailCodeShowFlag">
          <a-input-group compact>
            <a-input style="width: calc(100% - 120px)" v-model:value="loginForm.emailCode"
                     autocomplete="on" placeholder="请输入邮箱验证码" />
            <a-button @click="sendSmsCode" class="code-btn" type="primary"
                      :disabled="emailCodeButtonDisabled" :loading="sendingCode">
              {{ emailCodeTips }}
            </a-button>
          </a-input-group>
        </a-form-item>

        <a-form-item name="password">
          <a-input-password
              v-model:value="loginForm.password"
              autocomplete="on"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
          />
          <div class="eye-box" @click="showPassword = !showPassword">
            <EyeOutlined v-if="!showPassword" />
            <EyeInvisibleOutlined v-else />
          </div>
        </a-form-item>

        <a-form-item name="captchaCode">
          <a-input class="captcha-input" v-model:value.trim="loginForm.captchaCode" placeholder="请输入验证码" />
          <img class="captcha-img" :src="captchaBase64Image" @click="getCaptcha" alt="Captcha" />
        </a-form-item>

        <a-form-item>
          <a-checkbox v-model:checked="rememberPwd">记住密码</a-checkbox>
          <a style="float: right; color: #4361ee;" @click="showForgetPassword">忘记密码?</a>
        </a-form-item>

        <a-form-item>
          <button class="btn" :class="{ 'btn-loading': loading }" :disabled="loading" @click="onLogin">
            <template v-if="!loading">登 录</template>
            <span v-else class="loading-text">
              <a-spin size="small" /> 请稍候
            </span>
          </button>
        </a-form-item>
      </a-form>
      <div class="footer-info">
        <div class="copyright">© 布道师学院 版权所有</div>
      </div>
    </div>
    </div>
    <a-modal v-model:open="showForgetModal" title="找回密码" :footer="null" width="400px">
      <ForgetPassword @close="showForgetModal = false" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { sentry } from '/@/lib/sentry';
import { EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { loginApi } from '/@/api/system/login-api';
import { Loading } from '/@/components/framework/loading';
import { LOGIN_DEVICE_ENUM } from '/@/constants/system/login-device-const';
import { useUserStore } from '/@/store/modules/system/user';
import loginGif from '/@/assets/images/login/login.png';
import { buildRoutes } from '/@/router/index';
import { encryptData } from '/@/lib/encrypt';
import { localSave, localRemove } from '/@/utils/local-util.js';
import LocalStorageKeyConst from '/@/constants/local-storage-key-const.js';
import { useDictStore } from '/@/store/modules/system/dict.js';
import { dictApi } from '/@/api/support/dict-api.js';

//--------------------- 登录表单 ---------------------------------

const loginForm = reactive({
  loginName: 'admin',
  password: '',
  captchaCode: '',
  captchaUuid: '',
  loginDevice: LOGIN_DEVICE_ENUM.PC.value,
});
const rules = {
  loginName: [{ required: true, message: '用户名不能为空' }],
  password: [{ required: true, message: '密码不能为空' }],
  captchaCode: [{ required: true, message: '验证码不能为空' }],
};

const showPassword = ref(false);
const router = useRouter();
const formRef = ref();
const rememberPwd = ref(false);
const loading = ref(false);

onMounted(() => {
  document.onkeyup = (e) => {
    if (e.keyCode === 13 && !loading.value) {
      onLogin();
    }
  };
});

onUnmounted(() => {
  document.onkeyup = null;
});

//登录
async function onLogin() {
  // 防止重复提交
  if (loading.value) {
    return;
  }

  // 先校验表单，校验失败直接返回，不触发 loading
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  loading.value = true;
  Loading.show();
  try {
    // 密码加密
    let encryptPasswordForm = Object.assign({}, loginForm, {
      password: encryptData(loginForm.password),
    });
    const res = await loginApi.login(encryptPasswordForm);
    stopRefreshCaptchaInterval();
    localSave(LocalStorageKeyConst.USER_TOKEN, res.data.token ? res.data.token : '');

    // 多租户：保存租户信息到 localStorage
    if (res.data.tenantId) {
      localSave(LocalStorageKeyConst.USER_TENANT_ID, res.data.tenantId);
    } else {
      localRemove(LocalStorageKeyConst.USER_TENANT_ID);
    }
    if (res.data.isPlatformAdmin === true) {
      localSave(LocalStorageKeyConst.USER_PLATFORM_ADMIN, 'true');
    } else {
      localRemove(LocalStorageKeyConst.USER_PLATFORM_ADMIN);
    }

    message.success('登录成功');
    //更新用户信息到pinia
    useUserStore().setUserLoginInfo(res.data);
    // 初始化数据字典
    try {
      const dictRes = await dictApi.getAllDictData();
      useDictStore().initData(dictRes.data);
    } catch (dictError) {
      console.error('数据字典初始化失败:', dictError);
    }
    //构建系统的路由
    buildRoutes();
    // 跳转到首页
    router.push('/home').catch(err => {
      console.error('路由跳转失败:', err);
      // 如果跳转失败，尝试刷新页面
      window.location.href = window.location.origin + '/#/home';
    });
  } catch (e) {
    // axios拦截器已经统一处理了错误消息显示
    // 这里只处理业务逻辑：刷新验证码
    loginForm.captchaCode = '';
    getCaptcha();
    sentry.captureError(e);
    // 失败时恢复按钮，允许重试
    loading.value = false;
  } finally {
    Loading.hide();
  }
}

//--------------------- 验证码 ---------------------------------

const captchaBase64Image = ref('');
async function getCaptcha() {
  try {
    let captchaResult = await loginApi.getCaptcha();
    captchaBase64Image.value = captchaResult.data.captchaBase64Image;
    loginForm.captchaUuid = captchaResult.data.captchaUuid;
    beginRefreshCaptchaInterval(captchaResult.data.expireSeconds);
  } catch (e) {
    console.log(e);
  }
}

let refreshCaptchaInterval = null;
function beginRefreshCaptchaInterval(expireSeconds) {
  if (refreshCaptchaInterval === null) {
    refreshCaptchaInterval = setInterval(getCaptcha, (expireSeconds - 5) * 1000);
  }
}

function stopRefreshCaptchaInterval() {
  if (refreshCaptchaInterval != null) {
    clearInterval(refreshCaptchaInterval);
    refreshCaptchaInterval = null;
  }
}

onMounted(() => {
  getCaptcha();
  getTwoFactorLoginFlag();
});

//--------------------- 邮箱验证码 ---------------------------------

const emailCodeShowFlag = ref(false);
let emailCodeTips = ref('获取邮箱验证码');
let emailCodeButtonDisabled = ref(false);
// 定时器
let countDownTimer = null;
// 开始倒计时
function runCountDown() {
  emailCodeButtonDisabled.value = true;
  let countDown = 60;
  emailCodeTips.value = `${countDown}秒后重新获取`;
  countDownTimer = setInterval(() => {
    if (countDown > 1) {
      countDown--;
      emailCodeTips.value = `${countDown}秒后重新获取`;
    } else {
      clearInterval(countDownTimer);
      emailCodeButtonDisabled.value = false;
      emailCodeTips.value = '获取验证码';
    }
  }, 1000);
}

// 获取双因子登录标识
async function getTwoFactorLoginFlag() {
  try {
    let result = await loginApi.getTwoFactorLoginFlag();
    emailCodeShowFlag.value = result.data;
  } catch (e) {
    sentry.captureError(e);
  }
}

// 发送邮箱验证码
async function sendSmsCode() {
  try {
    Loading.show();
    let result = await loginApi.sendLoginEmailCode(loginForm.loginName);
    message.success('验证码发送成功!请登录邮箱查看验证码~');
    runCountDown();
  } catch (e) {
    sentry.captureError(e);
  } finally {
    Loading.hide();
  }
}
</script>

<style lang="less" scoped>
@import './login.less';

.eye-box {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #6c757d;
  transition: all 0.3s;

  &:hover {
    color: #4361ee;
  }

  .anticon {
    font-size: 16px;
  }
}
</style>
