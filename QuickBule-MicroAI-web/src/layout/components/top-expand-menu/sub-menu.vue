<!--
  * 第二列菜单区域
  * 
  * @Author:
  * @Date:      2022-09-06 20:29:12 
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <a-sub-menu :key="props.menuInfo.menuId.toString()">
    <template #icon>
      <component :is="$antIcons[props.menuInfo.icon]" />
    </template>
    <template #title>{{ props.menuInfo.menuName }}</template>
    <template v-for="item in props.menuInfo.children" :key="item.menuId.toString()">
      <template v-if="item.visibleFlag">
        <template v-if="!item.children">
          <a-menu-item :key="item.menuId.toString()" @click="turnToPage(item)">
            <template #icon>
              <component :is="$antIcons[item.icon]" />
            </template>
            {{ item.menuName }}
          </a-menu-item>
        </template>
        <template v-else>
          <SubMenu :menu-info="item" :key="item.menuId.toString()" @turnToPage="turnToPage" />
        </template>
      </template>
    </template>
  </a-sub-menu>
</template>
<script setup>
  let props = defineProps({
    menuInfo: Object,
  });
  const emits = defineEmits(['turnToPage']);
  const turnToPage = (route) => {
    emits('turnToPage', route);
  };
</script>
<style scoped lang="less">
  :deep(.ant-menu-item-selected) {
    border-right: 3px !important;
  }
</style>
