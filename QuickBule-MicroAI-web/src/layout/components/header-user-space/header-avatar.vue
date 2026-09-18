<!--
  * 头像
  *
  * @Author:
  * @Date:      2022-09-06 20:02:01
  * @Wechat:
  * @Email:
  * @Copyright
-->

<template>
  <a-dropdown class="header-trigger" :get-popup-container="getPopupContainer">
    <div class="wrapper">
      <img
        class="avatar-image"
        :src="avatar || defaultAvatarImg"
        @error="onAvatarError"
      />
      <span class="name">{{ actualName }}</span>
    </div>
    <template #overlay>
      <a-menu :class="['avatar-menu']">
        <a-menu-item @click="toAccount()">
          <span>个人中心</span>
        </a-menu-item>
        <a-menu-item @click="toAccount(ACCOUNT_MENU.PASSWORD.menuId)">
          <span>修改密码</span>
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item @click="goHelpDoc">
          <span>帮助文档</span>
        </a-menu-item>
        <a-menu-item @click="onLogout">
          <span>退出登录</span>
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
  <HeaderResetPassword ref="resetPasswordRef" />
</template>
<script setup>
  import { computed, ref, onMounted } from 'vue';
  import defaultAvatarImg from '/@/assets/images/logo/avatar.jpg';
  import { loginApi } from '/@/api/system/login-api';
  import { useUserStore } from '/@/store/modules/system/user';
  import { sentry } from '/@/lib/sentry';
  import HeaderResetPassword from './header-reset-password-modal/index.vue';
  import { useRouter } from 'vue-router';
  import { ACCOUNT_MENU } from '/@/views/system/account/account-menu.js';

  // 头像背景颜色
  const AVATAR_BACKGROUND_COLOR_ARRAY = ['#87d068', '#00B853', '#f56a00', '#1890ff'];

  //监听退出登录方法
  async function onLogout() {
    try {
      await loginApi.logout();
    } catch (e) {
      sentry.captureError(e);
    } finally {
      useUserStore().logout();
      location.reload();
    }
  }

  // ------------------------ 个人中心 ------------------------
  const router = useRouter();
  function toAccount(menuId) {
    router.push({
      path: '/account',
      query: { menuId },
    });
  }

  // ------------------------ 帮助文档 ------------------------
  function goHelpDoc() {
    // 新标签页打开帮助文档
    const routeData = router.resolve({ path: '/help-doc' });
    window.open(routeData.href, '_blank');
  }

  function getPopupContainer() {
    return document.body;
  }

  // ------------------------ 修改密码 ------------------------
  const resetPasswordRef = ref();

  function showUpdatePwdModal() {
    resetPasswordRef.value.showModal();
  }

  // ------------------------ 以下是 头像和姓名 相关 ------------------------

  const avatarName = ref('');
  const avatar = computed(() => useUserStore().avatar);
  const actualName = computed(() => useUserStore().actualName);

  // 头像图片加载失败时，自动 fallback 到默认头像（防止后端返回 fileKey / URL 失效时显示破图）
  function onAvatarError(e) {
    if (e?.target && e.target.src !== defaultAvatarImg) {
      e.target.src = defaultAvatarImg;
    }
  }

  // 更新头像信息
  function updateAvatar() {
    if (useUserStore().actualName) {
      avatarName.value = useUserStore().actualName.substr(0, 1);
      const avatar = document.getElementById('budaosAvatar');
      if (avatar) {
        avatar.style.backgroundColor = AVATAR_BACKGROUND_COLOR_ARRAY[hashcode(avatarName.value) % 4];
      }
    }
  }

  /**
   * 通过计算固定字符串的hash，来选择颜色，这也每次登录的颜色是相同的
   */
  function hashcode(str) {
    let hash = 1,
      i,
      chr;
    if (str.length === 0) return hash;
    for (i = 0; i < str.length; i++) {
      chr = str.charCodeAt(i);
      hash = (hash << 5) - hash + chr;
      hash |= 0; // Convert to 32bit integer
    }
    return hash;
  }

  onMounted(updateAvatar);
</script>
<style lang="less" scoped>
  .wrapper {
    cursor: pointer;
    display: flex;
    align-items: center;

    .avatar-image {
      width: 20px;
      height: 20px;
      object-fit: cover;
      border-radius: 50%;
    }
  }

  .header-trigger {
    height: var(--header-height, 40px);
    line-height: var(--header-height, 40px);

    .avatar {
      vertical-align: middle;
    }

    .name {
      margin-left: 5px;
      font-weight: 500;
    }
  }
</style>
