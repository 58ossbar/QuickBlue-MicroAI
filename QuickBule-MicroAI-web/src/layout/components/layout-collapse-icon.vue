<!--
  * 布局折叠图标（替换 Ant Design 默认的 MenuFold/MenuUnfoldOutlined 细线条 icon）
  *
  * 设计：自定义三横线汉堡菜单 SVG，线条粗 stroke-width=2.2 + 圆头 stroke-linecap=round，
  *       视觉上更醒目，hover 时变蓝，与 antd 默认细线条对比度高
  *
  * 状态：
  *   - 展开 (collapsed=false)：三条等长横线（标准汉堡菜单）
  *   - 折叠 (collapsed=true)： 中间一条变短（暗示菜单已收起）
  *
  * 用法：
  *   <LayoutCollapseIcon :collapsed="collapsed" @click="() => (collapsed = !collapsed)" />
  *
  * @Author: CodeBuddy
-->
<template>
  <span class="layout-collapse-icon" :class="{ 'is-collapsed': collapsed }" @click="$emit('click')">
    <!-- 展开状态：三条等长横线（汉堡菜单） -->
    <svg
      v-if="!collapsed"
      viewBox="0 0 24 24"
      width="20"
      height="20"
      fill="none"
      stroke="currentColor"
      stroke-width="2.2"
      stroke-linecap="round"
    >
      <line x1="4" y1="6" x2="20" y2="6" />
      <line x1="4" y1="12" x2="20" y2="12" />
      <line x1="4" y1="18" x2="20" y2="18" />
    </svg>
    <!-- 折叠状态：中间一条变短（暗示菜单已收起） -->
    <svg
      v-else
      viewBox="0 0 24 24"
      width="20"
      height="20"
      fill="none"
      stroke="currentColor"
      stroke-width="2.2"
      stroke-linecap="round"
    >
      <line x1="4" y1="6" x2="20" y2="6" />
      <line x1="4" y1="12" x2="14" y2="12" />
      <line x1="4" y1="18" x2="20" y2="18" />
    </svg>
  </span>
</template>

<script setup>
  defineProps({
    collapsed: {
      type: Boolean,
      default: false,
    },
  });
  defineEmits(['click']);
</script>

<style scoped lang="less">
  .layout-collapse-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #595959;
    padding: 6px 8px;
    border-radius: 6px;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    line-height: 0;

    &:hover {
      color: #1890ff;
      background: rgba(24, 144, 255, 0.06);
    }

    svg {
      display: block;
    }
  }
</style>
