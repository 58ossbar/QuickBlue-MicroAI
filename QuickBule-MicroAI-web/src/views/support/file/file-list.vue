<!--
  * 文件
  *

-->
<template>
  <!---------- 查询表单form begin ----------->
  <a-form class="qb-query-form" v-privilege="'support:file:query'">
    <a-row class="qb-query-form-row">
      <a-form-item label="文件夹类型" class="qb-query-form-item">
        <EnumSelect width="150px" v-model:value="queryForm.folderType" enumName="FILE_FOLDER_TYPE_ENUM" placeholder="文件夹类型" />
      </a-form-item>
      <a-form-item label="文件名" class="qb-query-form-item">
        <a-input style="width: 150px" v-model:value="queryForm.fileName" placeholder="文件名" />
      </a-form-item>
      <a-form-item label="文件Key" class="qb-query-form-item">
        <a-input style="width: 150px" v-model:value="queryForm.fileKey" placeholder="文件Key" />
      </a-form-item>
      <a-form-item label="文件类型" class="qb-query-form-item">
        <a-input style="width: 150px" v-model:value="queryForm.fileType" placeholder="文件类型" />
      </a-form-item>
      <a-form-item label="创建人" class="qb-query-form-item">
        <a-input style="width: 150px" v-model:value="queryForm.creatorName" placeholder="创建人" />
      </a-form-item>
      <a-form-item label="创建时间" class="qb-query-form-item">
        <a-range-picker v-model:value="queryForm.createTime" :presets="defaultTimeRanges" style="width: 220px" @change="onChangeCreateTime" />
      </a-form-item>
      <a-form-item class="qb-query-form-item">
        <a-button-group>
          <a-button type="primary" @click="queryData">
            <template #icon>
              <SearchOutlined />
            </template>
            查询
          </a-button>
          <a-button @click="resetQuery">
            <template #icon>
              <ReloadOutlined />
            </template>
            重置
          </a-button>
        </a-button-group>
      </a-form-item>
    </a-row>
  </a-form>
  <!---------- 查询表单form end ----------->

  <a-card size="small" :bordered="false" :hoverable="true">
    <!---------- 表格操作行 begin ----------->
    <a-row class="qb-table-btn-block">
      <div class="qb-table-operate-block">
        <a-button type="primary" @click="showUploadModal">
          <template #icon>
            <cloud-upload-outlined />
          </template>
          上传文件
        </a-button>
      </div>
      <div class="qb-table-setting-block">
        <TableOperator v-model="columns" :tableId="null" :refresh="queryData" />
      </div>
    </a-row>
    <!---------- 表格操作行 end ----------->

    <!---------- 表格 begin ----------->
    <a-table
      ref="tableRef"
      size="small"
      :scroll="{ x: 1300 }"
      :dataSource="tableData"
      :columns="columns"
      rowKey="fileId"
      bordered
      :loading="tableLoading"
      :pagination="false"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'folderType'">
          <span>{{ $enumPlugin.getDescByValue('FILE_FOLDER_TYPE_ENUM', text) }}</span>
        </template>
        <template v-if="column.dataIndex === 'creatorUserType'">
          <span>{{ $enumPlugin.getDescByValue('USER_TYPE_ENUM', text) }}</span>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="view(record)" type="link">查看</a-button>
            <a-button @click="download(record)" type="link">下载</a-button>
          </div>
        </template>
      </template>
    </a-table>
    <!---------- 表格 end ----------->

    <div class="qb-query-table-page">
      <a-pagination
        showSizeChanger
        showQuickJumper
        show-less-items
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        :defaultPageSize="queryForm.pageSize"
        v-model:current="queryForm.pageNum"
        v-model:pageSize="queryForm.pageSize"
        :total="total"
        @change="queryData"
        :show-total="(total) => `共${total}条`"
      />
    </div>

    <FilePreviewModal ref="filePreviewModalRef" />

    <a-modal v-model:open="uploadModalFlag" title="上传文件" @onCancel="hideUploadModal" @ok="hideUploadModal">
      <FileUpload
        list-type="text"
        :maxUploadSize="5"
        buttonText="点击上传文件"
        :defaultFileList="[]"
        :multiple="true"
        :folderType="FILE_FOLDER_TYPE_ENUM.COMMON.value"
      />
    </a-modal>
  </a-card>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { fileApi } from '/@/api/support/file-api';
  import EnumSelect from '/@/components/framework/enum-select/index.vue';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { defaultTimeRanges } from '/@/lib/default-time-ranges';
  import { sentry } from '/@/lib/sentry';
  import FilePreviewModal from '/@/components/support/file-preview-modal/index.vue';
  import FileUpload from '/@/components/support/file-upload/index.vue';
  import { FILE_FOLDER_TYPE_ENUM } from '/@/constants/support/file-const';
  import { useColumnResize } from '/@/hooks/useColumnResize';
  // ---------------------------- 表格列 ----------------------------

  const tableRef = ref();
  const columns = ref([
    {
      title: '文件夹',
      dataIndex: 'folderType',
      ellipsis: true,
      width: 100,
    },
    {
      title: '文件名称',
      dataIndex: 'fileName',
      width: 200,
    },
    {
      title: '文件大小',
      dataIndex: 'fileSize',
      ellipsis: true,
      width: 100,
    },
    {
      title: '文件类型',
      dataIndex: 'fileType',
      ellipsis: true,
      width: 80,
    },
    {
      title: '上传人',
      dataIndex: 'creatorName',
      ellipsis: true,
      width: 100,
    },
    {
      title: '人类型',
      dataIndex: 'creatorUserType',
      ellipsis: true,
      width: 100,
    },
    {
      title: '上传时间',
      dataIndex: 'createTime',
      ellipsis: true,
      width: 150,
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 120,
      fixed: 'right',
    },
  ]);

  useColumnResize(tableRef, columns);

  // ---------------------------- 查询数据表单和方法 ----------------------------

  const queryFormState = {
    folderType: undefined, //文件夹类型
    fileName: undefined, //文件名词
    fileKey: undefined, //文件Key
    fileType: undefined, //文件类型
    creatorName: undefined, //创建人
    createTime: [], //创建时间
    createTimeBegin: undefined, //创建时间 开始
    createTimeEnd: undefined, //创建时间 结束
    pageNum: 1,
    pageSize: 10,
  };
  // 查询表单form
  const queryForm = reactive({ ...queryFormState });
  // 表格加载loading
  const tableLoading = ref(false);
  // 表格数据
  const tableData = ref([]);
  // 总数
  const total = ref(0);

  // 重置查询条件
  function resetQuery() {
    let pageSize = queryForm.pageSize;
    Object.assign(queryForm, queryFormState);
    queryForm.pageSize = pageSize;
    queryData();
  }

  // 查询数据

  function onSearch() {
    queryForm.pageNum = 1;
    queryData();
  }

  async function queryData() {
    tableLoading.value = true;
    try {
      let queryResult = await fileApi.queryPage(queryForm);
      console.log('查询结果:', queryResult);

      // 检查 queryResult 是否为空
      if (!queryResult) {
        console.error('查询结果为空');
        tableData.value = [];
        total.value = 0;
        return;
      }

      // 检查后端是否返回错误（code 为 '0' 或 0 都表示成功）
      if (queryResult.code !== undefined && queryResult.code !== '0' && queryResult.code !== 0) {
        console.error('后端返回错误 code:', queryResult.code, 'msg:', queryResult.msg);
        tableData.value = [];
        total.value = 0;
        return;
      }

      // 检查 data 字段是否存在
      if (!queryResult.data) {
        console.error('data字段不存在，queryResult:', queryResult);
        tableData.value = [];
        total.value = 0;
        return;
      }

      console.log('data:', queryResult.data);
      console.log('data.dataList:', queryResult.data.dataList);

      // 检查 dataList 是否为数组
      if (!queryResult.data.dataList || !Array.isArray(queryResult.data.dataList)) {
        console.error('dataList不存在或不是数组:', queryResult.data.dataList);
        tableData.value = [];
        total.value = 0;
        return;
      }

      // 处理文件列表
      for (const file of queryResult.data.dataList) {
        file.fileSize = getFileSize(file.fileSize);
      }
      tableData.value = queryResult.data.dataList;
      total.value = queryResult.data.total || 0;
      console.log('加载了', tableData.value.length, '条数据');
    } catch (e) {
      console.error('查询失败:', e);

      // 打印更详细的错误信息
      if (e.response) {
        console.error('响应状态:', e.response.status);
        console.error('响应数据:', e.response.data);
        console.error('响应头:', e.response.headers);
      } else if (e.request) {
        console.error('请求已发送但没有收到响应:', e.request);
      } else {
        console.error('错误消息:', e.message);
        console.error('错误堆栈:', e.stack);
      }

      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  function onChangeCreateTime(dates, dateStrings) {
    queryForm.createTimeBegin = dateStrings[0];
    queryForm.createTimeEnd = dateStrings[1];
  }

  function getFileSize(size) {
    //把字节转换成正常文件大小
    if (!size) return '';
    var num = 1024.0; //byte
    if (size < num) return size + 'B';
    if (size < Math.pow(num, 2)) return (size / num).toFixed(2) + 'KB'; //kb
    if (size < Math.pow(num, 3)) return (size / Math.pow(num, 2)).toFixed(2) + 'MB'; //M
    if (size < Math.pow(num, 4)) return (size / Math.pow(num, 3)).toFixed(2) + 'G'; //G
    return (size / Math.pow(num, 4)).toFixed(2) + 'T'; //T
  }

  // 查看文件
  const filePreviewModalRef = ref();
  function view(file) {
    filePreviewModalRef.value.showPreview(file);
  }

  // 下载文件
  async function download(file) {
    try {
      await fileApi.downLoadFile(file.fileKey);
    } catch (e) {
      sentry.captureError(e);
    }
  }

  onMounted(queryData);

  // ------------- 上传文件 --------------------
  const uploadModalFlag = ref(false);

  function showUploadModal() {
    uploadModalFlag.value = true;
  }

  function hideUploadModal() {
    uploadModalFlag.value = false;
    queryData();
  }
</script>
