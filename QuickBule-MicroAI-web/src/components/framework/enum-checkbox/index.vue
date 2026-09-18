<!--
  * 枚举 多选框
  *

  *
-->
<template>
  <a-checkbox-group :style="`width: ${width}`" v-model:value="selectValue" @change="handleChange" :disabled="disabled">
    <a-checkbox v-for="item in valueDescList" :key="item.value" :value="item.value" :disabled="disabledOption.includes(item.value)">
      {{ item.desc }}
    </a-checkbox>
  </a-checkbox-group>
</template>

<script setup>
  import { ref, watch, getCurrentInstance, onMounted } from 'vue';

  const props = defineProps({
    enumName: String,
    value: Array,
    width: {
      type: String,
      default: '200px',
    },
    // 禁用整个多选框
    disabled: {
      type: Boolean,
      default: false,
    },
    // 需要禁用的选项枚举值
    disabledOption: {
      type: Array,
      default: () => [],
    },
    // 需要隐藏的选项枚举值
    hiddenOption: {
      type: Array,
      default: () => [],
    },
  });

  // ------------ 枚举数据 加载和构建 ------------

  const valueDescList = ref([]);

  onMounted(() => {
    const internalInstance = getCurrentInstance(); // 有效  全局
    const enumPlugin = internalInstance.appContext.config.globalProperties.$enumPlugin;
    valueDescList.value = enumPlugin.getValueDescList(props.enumName).filter((item) => !props.hiddenOption.includes(item.value));
  });

  // ------------ 数据选中 事件及其相关 ------------

  const selectValue = ref(props.value);

  watch(
    () => props.value,
    (newValue) => {
      // 如果传入的值是被禁用或被隐藏的选项，则移除这些选项
      selectValue.value = newValue.filter((item) => !props.hiddenOption.includes(item) && !props.disabledOption.includes(item));
    },
    { immediate: true }
  );

  const emit = defineEmits(['update:value', 'change']);

  function handleChange(value) {
    emit('update:value', value);
    emit('change', value);
  }
</script>
