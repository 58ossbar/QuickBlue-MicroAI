<!--
  *  布尔 树形选择组件
  *
-->
<template>
  <a-select
    v-model:value="selectValue"
    :style="`width: ${width}px`"
    :placeholder="placeholder"
    :showSearch="true"
    :allowClear="true"
    :size="size"
    @change="handleChange"
  >
    <a-select-option v-for="item in $enumPlugin.getValueDescList('FLAG_NUMBER_ENUM')" :key="item.value" :value="item.value">
      {{ item.desc }}
    </a-select-option>
  </a-select>
</template>

<script setup>
  import _ from 'lodash';
  import { ref, watch } from 'vue';

  const props = defineProps({
    value: Boolean,
    width: {
      type: Number,
      default: 100,
    },
    placeholder: {
      type: String,
      default: '请选择',
    },
    size: {
      type: String,
      default: 'default',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['update:value', 'change']);

  function convertBoolean2number(value) {
    let result = null;
    if (_.isNaN(value) || _.isNull(value) || _.isUndefined(value)) {
      result = null;
    } else {
      result = value ? 1 : 0;
    }
    return result;
  }
  // 箭头value变化
  const selectValue = ref(convertBoolean2number(props.value));
  watch(
    () => props.value,
    (newValue) => {
      selectValue.value = convertBoolean2number(newValue);
    }
  );

  const handleChange = (value) => {
    console.log('boolean enum select', value);
    let booleanResult = null;
    if (!_.isUndefined(value)) {
      booleanResult = value === 1;
    }
    emit('update:value', booleanResult);
    emit('change', booleanResult);
  };
</script>
