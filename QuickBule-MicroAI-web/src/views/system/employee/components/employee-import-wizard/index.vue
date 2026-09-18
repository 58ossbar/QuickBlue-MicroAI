<!--
  员工导入向导
  - 第一步：下载模板（可跳过）
  - 第二步：选择导入文件
  - 第三步：导入进度
  - 第四步：导入结果确认
-->
<template>
  <a-modal
    :open="visible"
    :title="currentStep === 4 ? '导入完成' : '员工导入'"
    :width="600"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <a-steps :current="currentStep - 1" class="import-steps">
      <a-step title="下载模板" />
      <a-step title="选择文件" />
      <a-step title="导入中" />
      <a-step title="完成" />
    </a-steps>

    <!-- 第一步：下载模板 -->
    <div v-if="currentStep === 1" class="step-content">
      <a-alert
        message="导入说明"
        description="1. 请按照模板格式填写员工数据；2. 带*号的字段为必填项；3. 部门名称为必填，需填写系统中已存在的部门；4. 岗位名称为选填，如填写需与系统中名称一致。"
        type="info"
        show-icon
        style="margin-bottom: 20px"
      />
      <div class="template-actions">
        <a-button type="primary" size="large" @click="downloadTemplate">
          <template #icon><DownloadOutlined /></template>
          下载导入模板
        </a-button>
        <a-divider type="vertical" />
        <a-button size="large" @click="skipTemplate">
          已有模板，跳过
        </a-button>
      </div>
    </div>

    <!-- 第二步：选择文件 -->
    <div v-if="currentStep === 2" class="step-content">
      <a-upload-dragger
        v-model:fileList="fileList"
        name="file"
        accept=".xlsx,.xls"
        :beforeUpload="beforeUpload"
        :remove="handleRemove"
        :maxCount="1"
        @change="handleFileChange"
      >
        <p class="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p class="ant-upload-text">点击或拖拽文件到此处上传</p>
        <p class="ant-upload-hint">支持 .xlsx 或 .xls 格式的 Excel 文件</p>
      </a-upload-dragger>
      <div class="step-actions">
        <a-button @click="goToStep(1)">上一步</a-button>
        <a-button type="primary" :disabled="!selectedFile" @click="startImport">开始导入</a-button>
      </div>
    </div>

    <!-- 第三步：导入进度 -->
    <div v-if="currentStep === 3" class="step-content">
      <div class="import-progress">
        <a-spin :spinning="importing" tip="正在导入数据...">
          <a-progress :percent="progress" status="active" />
          <p class="progress-text">{{ progressText }}</p>
        </a-spin>
      </div>
    </div>

    <!-- 第四步：导入结果 -->
    <div v-if="currentStep === 4" class="step-content">
      <a-result
        :status="importResult.success ? 'success' : 'warning'"
        :title="importResult.success ? '导入成功' : '导入完成'"
      >
        <template #subTitle>
          <div class="result-summary">
            <p>共处理：{{ importResult.totalCount }} 条数据</p>
            <p v-if="importResult.successCount > 0" style="color: #52c41a">
              成功导入：{{ importResult.successCount }} 条
            </p>
            <p v-if="importResult.failedCount > 0" style="color: #ff4d4f">
              导入失败：{{ importResult.failedCount }} 条
            </p>
          </div>
        </template>
        <template #extra v-if="importResult.errors && importResult.errors.length > 0 && importResult.failedCount > 0">
          <div class="error-details">
            <a-alert
              :message="importResult.success ? '成功详情' : '失败详情'"
              :type="importResult.success ? 'success' : 'error'"
              :description="importResult.errors"
              show-icon
              style="text-align: left; max-height: 300px; overflow-y: auto"
            />
          </div>
        </template>
        <template #extra>
          <a-button type="primary" @click="handleFinish">完成</a-button>
          <a-button v-if="importResult.failedCount > 0" @click="goToStep(2)">重新导入</a-button>
        </template>
      </a-result>
    </div>
  </a-modal>
</template>

<script setup>
  import { DownloadOutlined, InboxOutlined } from '@ant-design/icons-vue';
  import { message } from 'ant-design-vue';
  import { ref } from 'vue';
  import { employeeApi } from '/@/api/system/employee-api';
  import { Loading } from '/@/components/framework/loading';

  const emit = defineEmits(['refresh']);
  const visible = ref(false);
  const currentStep = ref(1);
  const fileList = ref([]);
  const selectedFile = ref(null);
  const importing = ref(false);
  const progress = ref(0);
  const progressText = ref('');
  const importResult = ref({
    success: true,
    totalCount: 0,
    successCount: 0,
    failedCount: 0,
    errors: ''
  });

  function showModal() {
    visible.value = true;
    currentStep.value = 1;
    fileList.value = [];
    selectedFile.value = null;
    progress.value = 0;
    progressText.value = '';
  }

  function handleCancel() {
    if (importing.value) {
      message.warning('导入进行中，请稍候...');
      return;
    }
    visible.value = false;
  }

  function goToStep(step) {
    currentStep.value = step;
  }

  // 下载模板
  async function downloadTemplate() {
    Loading.show();
    try {
      const response = await employeeApi.downloadTemplate();
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = '员工导入模板.xlsx';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      message.success('模板下载成功，请按照模板填写数据');
      goToStep(2);
    } catch (error) {
      message.error('模板下载失败');
    } finally {
      Loading.hide();
    }
  }

  // 跳过下载模板
  function skipTemplate() {
    goToStep(2);
  }

  // 文件上传前
  function beforeUpload(file) {
    const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                    file.type === 'application/vnd.ms-excel' ||
                    file.name.endsWith('.xlsx') || file.name.endsWith('.xls');
    if (!isExcel) {
      message.error('只能上传 Excel 文件！');
      return false;
    }
    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      message.error('文件大小不能超过 10MB！');
      return false;
    }
    return false; // 阻止自动上传
  }

  // 文件变化
  function handleFileChange({ fileList: newFileList }) {
    fileList.value = newFileList;
    if (newFileList.length > 0 && newFileList[0].status !== 'removed') {
      selectedFile.value = newFileList[0].originFileObj;
    } else {
      selectedFile.value = null;
    }
  }

  // 移除文件
  function handleRemove() {
    selectedFile.value = null;
  }

  // 开始导入
  async function startImport() {
    if (!selectedFile.value) {
      message.warning('请先选择要导入的文件');
      return;
    }
    goToStep(3);
    importing.value = true;
    progress.value = 10;
    progressText.value = '正在解析文件...';

    try {
      progress.value = 30;
      progressText.value = '正在校验数据...';

      const result = await employeeApi.importEmployee(selectedFile.value);

      progress.value = 80;
      progressText.value = '正在保存数据...';

      // 模拟进度
      const progressInterval = setInterval(() => {
        if (progress.value < 90) {
          progress.value += 5;
        }
      }, 200);

      await new Promise(resolve => setTimeout(resolve, 500));
      clearInterval(progressInterval);

      progress.value = 100;
      progressText.value = '导入完成！';

      await new Promise(resolve => setTimeout(resolve, 500));

      // 解析结果
      let resultText = result.data || '';
      let successCount = 0;
      let failedCount = 0;
      let errors = '';

      if (resultText) {
        const successMatch = resultText.match(/成功\s*(\d+)/);
        const failedMatch = resultText.match(/失败\s*(\d+)/);
        if (successMatch) successCount = parseInt(successMatch[1]);
        if (failedMatch) failedCount = parseInt(failedMatch[1]);
        errors = resultText;
      }

      importResult.value = {
        success: failedCount === 0,
        totalCount: successCount + failedCount,
        successCount: successCount,
        failedCount: failedCount,
        errors: errors
      };

      setTimeout(() => {
        importing.value = false;
        goToStep(4);
        emit('refresh');
      }, 300);

    } catch (error) {
      progress.value = 100;
      progressText.value = '导入失败';

      importResult.value = {
        success: false,
        totalCount: 0,
        successCount: 0,
        failedCount: 1,
        errors: error.msg || error.data || '导入失败，请检查文件格式和数据是否正确'
      };

      setTimeout(() => {
        importing.value = false;
        goToStep(4);
      }, 500);
    }
  }

  // 完成
  function handleFinish() {
    visible.value = false;
    emit('refresh');
  }

  defineExpose({
    showModal
  });
</script>

<style scoped lang="less">
  .import-steps {
    margin-top: -12px;
    margin-bottom: 30px;
  }

  .step-content {
    min-height: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  .template-actions,
  .step-actions {
    display: flex;
    gap: 10px;
    margin-top: 20px;
  }

  .step-actions {
    justify-content: center;
  }

  .import-progress {
    width: 80%;
    text-align: center;

    .progress-text {
      margin-top: 20px;
      color: #666;
      font-size: 14px;
    }
  }

  .result-summary {
    margin: 20px 0;

    p {
      margin: 8px 0;
      font-size: 16px;
    }
  }

  .error-details {
    width: 100%;
    margin: 20px 0;
    text-align: left;

    :deep(.ant-alert-description) {
      white-space: pre-line;
      line-height: 1.6;
    }
  }

  :deep(.ant-steps-item-process > .ant-steps-item-container > .ant-steps-item-icon) {
    background: #1890ff;
    border-color: #1890ff;
  }
</style>
