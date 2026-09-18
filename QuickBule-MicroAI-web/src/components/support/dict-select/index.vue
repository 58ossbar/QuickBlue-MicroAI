<!---
  * 字段 下拉选择框
  *

  *
-->
<template>
  <div>
    <a-select
      v-model:value="selectValue"
      :style="`width: ${width}`"
      :placeholder="props.placeholder"
      :allowClear="true"
      :size="size"
      :mode="mode"
      @change="onChange"
      :disabled="disabled"
    >
      <a-select-option v-for="item in dictDataList" :key="item.dataValue" :value="item.dataValue" :disabled="disabledOption.includes(item.valueCode)">
        {{ item.dataLabel }}
      </a-select-option>
    </a-select>
  </div>
</template>

<script setup>
  import { computed, ref, watch } from 'vue';
  import { useDictStore } from '/@/store/modules/system/dict.js';

  const props = defineProps({
    dictCode: String,
    value: [Array, String, Number],
    mode: {
      type: String,
      default: 'combobox',
    },
    width: {
      type: String,
      default: '100%',
    },
    placeholder: {
      type: String,
      default: '请选择',
    },
    size: {
      type: String,
      default: 'default',
    },
    // 禁用整个下拉选择框
    disabled: {
      type: Boolean,
      default: false,
    },
    // 需要禁用的选项字典值编码
    disabledOption: {
      type: Array,
      default: () => [],
    },
    // 需要隐藏的选项字典值编码
    hiddenOption: {
      type: Array,
      default: () => [],
    },
  });

  // -------------------------- 查询 字典数据 --------------------------

  const dictDataList = computed(() => {
    const dictData = useDictStore().getDictData(props.dictCode);
    if (!dictData || dictData.length === 0) {
      return [];
    }

    return dictData.filter((item) => {
      // 处理隐藏选项
      const isHidden = props.hiddenOption.some(hidden => {
        // 类型安全比较：都转为字符串比较
        return String(hidden) === String(item.dataValue);
      });

      return !isHidden && !item.disabledFlag;
    });
  });

  // -------------------------- 选中 相关、事件 --------------------------

  const selectValue = ref(props.value);

  watch(
      () => props.value,
      (newValue) => {
        if (Array.isArray(newValue)) {
          selectValue.value = newValue.filter((item) => {
            const itemStr = String(item);
            return !props.disabledOption.some(disabled => String(disabled) === itemStr) &&
                !props.hiddenOption.some(hidden => String(hidden) === itemStr);
          });
        } else {
          // 处理单个值的情况
          const dictData = useDictStore().getDictData(props.dictCode);

          // 如果字典数据为空，直接设置原值
          if (!dictData || dictData.length === 0) {
            selectValue.value = newValue;
            return;
          }

          // 查找匹配的字典项（宽松匹配：数字5和字符串'5'应该匹配）
          const matchedItem = dictData.find(item => {
            // 使用 == 进行宽松比较
            return item.dataValue == newValue;
          });

          if (matchedItem) {
            // 找到匹配项，使用字典中的值（确保类型一致）
            selectValue.value = matchedItem.dataValue;
          } else {
            // 没找到匹配项，检查是否被禁用或隐藏
            const newValueStr = String(newValue);
            const isHidden = props.hiddenOption.some(hidden => String(hidden) === newValueStr);
            const isDisabled = props.disabledOption.some(disabled => String(disabled) === newValueStr);
            selectValue.value = isHidden || isDisabled ? undefined : newValue;
          }
        }
      },
      { immediate: true }
  );
  const emit = defineEmits(['update:value', 'change']);

  function onChange(value) {
    emit('update:value', value);
    emit('change', value);
  }
</script>
