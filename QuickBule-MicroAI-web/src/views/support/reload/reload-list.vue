<!--
  * reload
  *

-->
<template>
  <a-card size="small" :bordered="false" :hoverable="true">
    <a-alert>
      <template v-slot:message>
        <h4>Reload 心跳服务介绍：</h4>
      </template>
      <template v-slot:description>
        <pre>
简介：ConfigRefresh是一个可以在不重启进程的情况下动态重新加载配置或者执行某些预先设置的代码。

原理：
- Java后端会在项目启动的时候开启一个Daemon线程，这个Daemon线程会每隔几秒轮询t_reload_item表的状态。
- 如果【状态标识】与【上次状态标识】比较发生变化，会将参数传入ConfigRefresh实现类，进行自定义操作。
用途：
· 用于刷新内存中的缓存
· 用于执行某些后门代码
· 用于进行Java热加载（前提是类结构不发生变化）
· 其他不能重启服务的应用
</pre
        >
      </template>
    </a-alert>

    <a-row justify="end">
      <TableOperator class="qb-margin-bottom5 qb-margin-top5" v-model="columns" :tableId="TABLE_ID_CONST.SUPPORT.RELOAD" :refresh="ajaxQuery" />
    </a-row>

    <a-table
      ref="tableRef"
      size="small"
      bordered
      class="qb-margin-top10"
      :dataSource="tableData"
      :loading="tableLoading"
      :columns="columns"
      rowKey="tag"
      :pagination="false"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'action'">
          <div class="qb-table-operate">
            <a-button @click="doReload(record.tag)" v-privilege="'support:reload:execute'" type="link">执行</a-button>
            <a-button @click="showResultList(record.tag)" v-privilege="'support:reload:result'" type="link">查看结果</a-button>
          </div>
        </template>
      </template>
    </a-table>

    <DoReloadForm @refresh="ajaxQuery" ref="doReloadForm" />
    <ReloadResultList ref="reloadResultList" />
  </a-card>
</template>
<script setup>
  import { onMounted, ref } from 'vue';
  import DoReloadForm from './do-reload-form-modal.vue';
  import ReloadResultList from './reload-result-list.vue';
  import { reloadApi } from '/@/api/support/reload-api';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  //------------------------ 表格渲染 ---------------------

  const tableRef = ref();
  const columns = ref([
    {
      title: '标签',
      dataIndex: 'tag',
      width: 200,
    },
    {
      title: '运行标识',
      dataIndex: 'identification',
    },
    {
      title: '参数',
      dataIndex: 'args',
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      width: 150,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 150,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 150,
    },
  ]);

  useColumnResize(tableRef, columns);

  const tableLoading = ref(false);
  const tableData = ref([]);

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let res = await reloadApi.queryList();
      tableData.value = res.data;
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  onMounted(ajaxQuery);

  // ------------------------------ 表格操作列： 执行 reload ------------------------------
  const doReloadForm = ref();
  function doReload(tag) {
    doReloadForm.value.showModal(tag);
  }

  // ------------------------------ 表格操作列： 查看执行结果 ------------------------------

  const reloadResultList = ref();
  function showResultList(tag) {
    reloadResultList.value.showModal(tag);
  }
</script>
