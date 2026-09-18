<!--
  *  定期强制修改密码
  *
  * @Author:
  * @Date:      2024-08-06 20:40:16
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <a-modal :open="visible" width="620px" :footer="null" :bodyStyle="{ height: '420px' }" title="" :closable="false" :maskClosable="true">
    <a-alert style="width: 550px" message="根据《网络安全法》和《数据安全法》要求，需要定期修改密码保障数据安全！" type="warning" show-icon />
    <Password @on-success="refresh" />
  </a-modal>
</template>
<script setup>
  import { computed } from 'vue';
  import Password from '/@/views/system/account/components/password/index.vue';
  import { useUserStore } from '/@/store/modules/system/user.js';
  import { loginApi } from '/@/api/system/login-api.js';
  import { sentry } from '/@/lib/sentry.js';
  import { Loading } from '/@/components/framework/loading/index.js';

  /**
   * 修改密码弹窗
   */
  const visible = computed(() => {
    return useUserStore().$state.needUpdatePwdFlag;
  });

  /**
   * 刷新
   */
  async function refresh() {
    try {
      Loading.show();
      //获取登录用户信息
      const res = await loginApi.getLoginInfo();
      //更新用户信息到pinia
      useUserStore().setUserLoginInfo(res.data);
    } catch (e) {
      sentry.captureError(e);
    } finally {
      Loading.hide();
    }
  }
</script>
