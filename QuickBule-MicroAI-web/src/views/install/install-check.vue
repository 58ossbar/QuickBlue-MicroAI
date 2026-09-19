<template>
  <div class="install-check-container">
    <div class="install-check-wrapper">
      <div class="logo">
        <span class="logo-icon">⚡</span>
        <span class="logo-text">QuickBlue</span>
        <span class="version-badge">v4.0.0 · MySQL 版</span>
      </div>

      <div class="check-content">
        <a-spin :spinning="checking" size="large">
          <div v-if="checking" class="checking-message">
            <h3>正在检测系统状态...</h3>
            <p>请稍候，我们正在检查系统是否已安装</p>
          </div>

          <div v-else-if="installed" class="installed-message">
            <a-result
              status="success"
              title="系统已安装"
              sub-title="QuickBlue v4.0.0（MySQL 版）已经安装完成，您可以直接登录使用"
            >
              <template #icon>
                <span class="success-icon">✅</span>
              </template>
              <template #extra>
                <a-button type="primary" size="large" @click="goToLogin">
                  前往登录 <RightOutlined />
                </a-button>
              </template>
            </a-result>
          </div>

          <div v-else class="not-installed-message">
            <a-result
              status="info"
              title="欢迎使用 QuickBlue"
              sub-title="系统尚未安装，点击下方按钮开始 v4.0.0（MySQL 版）安装向导"
            >
              <template #icon>
                <span class="install-icon">🚀</span>
              </template>
              <template #extra>
                <a-button type="primary" size="large" @click="goToInstall">
                  开始安装 <RocketOutlined />
                </a-button>
              </template>
            </a-result>
          </div>
        </a-spin>
      </div>

      <div class="footer-info">
        <p>© 湖北长江电影集团有限责任公司票务分公司 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { RightOutlined, RocketOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import installApi from '/@/api/install/install-api';

const router = useRouter();
const checking = ref(true);
const installed = ref(false);

onMounted(async () => {
  await checkInstalled();
});

async function checkInstalled() {
  try {
    // 调用网关 /install/check-installed 获取真实安装状态
    const response = await installApi.checkInstalled();
    installed.value = !!(response && response.data);

    // 如果已安装，自动跳转到登录页面
    if (installed.value) {
      message.success('系统已安装，正在跳转到登录页面...');
      setTimeout(() => {
        router.push('/login');
      }, 2000);
    }
  } catch (error) {
    console.error('检查安装状态失败:', error);
    // 检查失败时（例如网关未启动），默认视为未安装，交由用户进入安装向导
    installed.value = false;
  } finally {
    checking.value = false;
  }
}

function goToInstall() {
  router.push('/install');
}

function goToLogin() {
  router.push('/login');
}
</script>

<style lang="less" scoped>
.install-check-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.install-check-wrapper {
  width: 100%;
  max-width: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 48px 32px;
  text-align: center;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 48px;
}

.logo-icon {
  font-size: 48px;
  margin-right: 12px;
}

.logo-text {
  font-size: 32px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.version-badge {
  align-self: center;
  margin-left: 10px;
  font-size: 12px;
  color: #764ba2;
  background: rgba(118, 75, 162, 0.1);
  padding: 2px 10px;
  border-radius: 10px;
  white-space: nowrap;
}

.check-content {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checking-message {
  h3 {
    font-size: 20px;
    color: #262626;
    margin-bottom: 8px;
  }

  p {
    color: #8c8c8c;
  }
}

.success-icon,
.install-icon {
  font-size: 64px;
}

.installed-message,
.not-installed-message {
  width: 100%;
}

.footer-info {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;

  p {
    color: #8c8c8c;
    font-size: 12px;
    margin: 0;
  }
}
</style>
