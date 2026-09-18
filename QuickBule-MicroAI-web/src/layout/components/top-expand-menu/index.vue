<!--
  * 展开菜单
  * 
  * @Author:
  * @Date:      2022-09-06 20:29:12 
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <div class="menu-container">
    <!-- 顶部导航：取自菜单管理中第一级菜单，建议一级菜单都设为目录 -->
    <TopMenu ref="topMenuRef" :collapsed="collapsed" class="top-menu" />
    <!-- 左侧导航：通过在一级菜单下建立页面或目录实现菜单分组 -->
    <RecursionMenu :collapsed="collapsed" ref="recursionMenuRef" class="recursion-menu" />
  </div>
</template>
<script setup>
import { onMounted, ref, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import RecursionMenu from './recursion-menu.vue';
import TopMenu from './top-menu.vue';
import { useUserStore } from '/@/store/modules/system/user';
import { useAppConfigStore } from '/@/store/modules/system/app-config';
import { themeColors } from '/@/theme/color.js';

const props = defineProps({
  placeholder: {
    type: String,
    default: '请选择',
  },
  collapsed: {
    type: Boolean,
    required: false,
    default: false,
  },
});

// 选中的顶级菜单
const topMenuRef = ref();
// 二级菜单引用
const recursionMenuRef = ref();

let currentRoute = useRoute();

// 主题色：跟随 setting 中选择的主题色板变化（用于顶部 head 背景）
const colorIndex = computed(() => useAppConfigStore().colorIndex);
const primaryColor = computed(() => themeColors[colorIndex.value].primaryColor);

// 页面紧凑模式：动态调整顶部 head 高度
const compactFlag = computed(() => useAppConfigStore().compactFlag);
const menuHeaderHeight = computed(() => compactFlag.value ? '32px' : '40px');

// 根据路由更新菜单展开和选中状态
function updateSelectKeyAndOpenKey() {
  // 第一步，根据路由 更新选中 顶级菜单
  let parentList = useUserStore().menuParentIdListMap.get(currentRoute.name) || [];
  if (parentList.length === 0) {
    topMenuRef.value.updateSelectKey(currentRoute.name);
    return;
  }
  topMenuRef.value.updateSelectKey(parentList[0].name);

  //第二步，根据路由 更新 二级菜单的selectKey和openKey
  recursionMenuRef.value.updateSelectKeyAndOpenKey(parentList, currentRoute.name);
}

onMounted(updateSelectKeyAndOpenKey);

//监听路由的变化，进行更新菜单展开项目
watch(currentRoute, () => {
  updateSelectKeyAndOpenKey();
});
</script>
<style scoped lang="less">
.menu-container {
  height: 100%;
  position: relative;
}

/* 顶部分组 head：absolute 定位让蓝色 head 跨越整个 viewport 顶部
 * 关键点：父组件 scoped CSS 才能命中 <TopMenu class="top-menu" /> 传的 class。
 * 17px 留白修复：用 calc(100% - 100vw) 替代硬编码 -100vw
 *  - 100% = 父元素 (a-layout-sider) 宽度 = 208 / 80 (折叠)
 *  - 100vw = viewport 宽度（无论物理/CSS 宽度，calc 都能精确对齐 viewport 右边）
 *  - right: calc(100% - 100vw) 算出元素 right edge = viewport 物理右边
 *  - width: 100vw + right 计算 = 元素 left = 0，宽度 = 100vw
 *  - 最终元素跨度 [0, 100vw] = 整个 viewport 顶部，无 17px 留白
 *  - z-index: 1（低）让 a-layout-header 按 DOM 顺序自然显示在上，不强制覆盖。*/
:deep(.top-menu) {
  position: absolute;
  top: 0;
  right: calc(100% - 100vw);
  width: 100vw;
  height: v-bind(menuHeaderHeight);
  display: flex;
  align-items: center;
  flex-shrink: 0;
  z-index: 1;
  background-color: v-bind('primaryColor');  /* 跟随主题色板，让蓝色 head 横跨整个 viewport 顶部 */
  transition: all 0.2s, background 0s;
}
</style>
