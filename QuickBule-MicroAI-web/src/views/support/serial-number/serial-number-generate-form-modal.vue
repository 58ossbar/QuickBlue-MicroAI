<!--
  * 生成单号
  *
-->
<template>
  <a-modal :open="visible" title="生成单号" ok-text="生成" cancel-text="关闭" :confirm-loading="generating" width="560px" @ok="onSubmit" @cancel="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="业务名称">
        <a-input v-model:value="form.businessName" :disabled="true" />
      </a-form-item>

      <a-form-item label="格式">
        <div class="code-text">{{ form.format }}</div>
      </a-form-item>

      <a-form-item label="循环周期">
        <a-tag :color="ruleTypeInfo.color">{{ ruleTypeInfo.desc }}</a-tag>
      </a-form-item>

      <a-form-item label="安全模式">
        <a-tag :color="secureModeInfo.color">{{ secureModeInfo.desc }}</a-tag>
      </a-form-item>

      <a-form-item label="上次产生单号">
        <span v-if="form.lastNumber != null" class="code-text">{{ form.lastNumber }}</span>
        <span v-else class="table-empty">-</span>
      </a-form-item>

      <a-form-item label="生成数量" name="count">
        <a-input-number v-model:value="form.count" :min="1" :max="1000" :precision="0" style="width: 100%" placeholder="1 - 1000" />
      </a-form-item>

      <a-form-item label="生成结果">
        <div v-if="generateResults.length" class="result-wrap">
          <a-tag v-for="(item, index) in generateResults" :key="index" color="processing" class="result-tag">{{ item }}</a-tag>
        </div>
        <div v-else class="result-empty">生成后展示结果，支持一次生成多条</div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup>
  import { message } from 'ant-design-vue';
  import { reactive, ref } from 'vue';
  import { serialNumberApi } from '/@/api/support/serial-number-api';
  import { Loading } from '/@/components/framework/loading';
  import { sentry } from '/@/lib/sentry';

  // emit
  const emit = defineEmits(['refresh']);
  defineExpose({
    showModal,
  });

  // ----------------------- 表单 隐藏 与 显示 ------------------------
  // 是否展示
  const visible = ref(false);

  // 循环周期 / 安全模式 展示信息
  const ruleTypeInfo = ref({ desc: '无周期', color: 'default' });
  const secureModeInfo = ref({ desc: '普通模式', color: 'default' });

  function ruleTypeTag(val) {
    const map = {
      '': { desc: '无周期', color: 'default' },
      '[yyyy]': { desc: '年周期', color: 'blue' },
      '[mm]': { desc: '月周期', color: 'cyan' },
      '[dd]': { desc: '日周期', color: 'green' },
    };
    return map[val] || { desc: val || '无周期', color: 'default' };
  }

  function secureModeTag(val) {
    const map = {
      0: { desc: '普通模式', color: 'default' },
      1: { desc: '随机模式', color: 'purple' },
      2: { desc: '时间戳模式', color: 'blue' },
      3: { desc: '加密模式', color: 'gold' },
    };
    return map[val] || { desc: '未知', color: 'default' };
  }

  function showModal(data) {
    form.serialNumberId = data.serialNumberId;
    form.businessName = data.businessName;
    form.format = data.format;
    form.ruleType = data.ruleType;
    form.lastNumber = data.lastNumber == null ? null : data.lastNumber;
    form.count = 1;
    ruleTypeInfo.value = ruleTypeTag(data.ruleType);
    secureModeInfo.value = secureModeTag(data.secureMode);
    generateResults.value = [];
    visible.value = true;
  }

  function onClose() {
    visible.value = false;
    emit('refresh');
  }

  // ----------------------- 表单 ------------------------
  const rules = {
    count: [
      { required: true, message: '请输入生成数量' },
      { type: 'number', min: 1, max: 1000, message: '数量范围 1 - 1000', trigger: 'blur' },
    ],
  };

  // 生成结果
  const generateResults = ref([]);

  //  组件
  const formRef = ref();

  const form = reactive({
    serialNumberId: -1,
    businessName: '',
    format: '',
    ruleType: '',
    lastNumber: null,
    count: 1,
  });

  // 生成中
  const generating = ref(false);

  function onSubmit() {
    formRef.value
      .validate()
      .then(async () => {
        generating.value = true;
        Loading.show();
        try {
          let res = await serialNumberApi.generate(form);
          message.success('生成成功');
          generateResults.value = res.data || [];
        } catch (error) {
          sentry.captureError(error);
        } finally {
          Loading.hide();
          generating.value = false;
        }
      })
      .catch((error) => {
        console.log('error', error);
        message.error('参数验证错误，请仔细填写表单数据!');
      });
  }
</script>
<style lang="less" scoped>
  .code-text {
    font-family: 'JetBrains Mono', Consolas, Monaco, 'Courier New', monospace;
    font-size: 12px;
  }

  .table-empty {
    color: rgba(0, 0, 0, 0.25);
  }

  .result-wrap {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    max-height: 220px;
    padding: 4px 0;
    overflow: auto;
  }

  .result-tag {
    margin: 0;
    font-family: 'JetBrains Mono', Consolas, Monaco, 'Courier New', monospace;
  }

  .result-empty {
    padding: 12px 0;
    color: rgba(0, 0, 0, 0.25);
  }
</style>
