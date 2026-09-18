<!--
  * 底部：版权、公司等信息
  *
  * @Author:
  * @Date:      2022-09-06 20:29:12
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <div class="version">
    <a target="_blank" class="qb-copyright" href="https://www.hbfilm.com.cn"> ©2025-{{ currentYear }} 湖北长江电影集团有限责任公司 票务分公司 </a>
  </div>
</template>
<script setup>
  import dayjs from 'dayjs';

  const currentYear = dayjs().year();
</script>

<style lang="less" scoped>
  .version {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.45);
    a {
      color: rgba(0, 0, 0, 0.45);
    }
    a:hover {
      color: @primary-color;
    }
  }
</style>
