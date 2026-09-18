<!-- /src/components/support/dict-tag/index.vue -->
<template>
  <span>{{ label }}</span>
</template>

<script setup>
import { computed } from 'vue';
import { useDictStore } from '/@/store/modules/system/dict.js';

const props = defineProps({
  dictCode: {
    type: String,
    required: true
  },
  dataValue: {
    type: [String, Number],
    default: undefined
  }
});

const dictStore = useDictStore();

const label = computed(() => {
  return dictStore.getDataLabels(props.dictCode, props.dataValue);
});
</script>
