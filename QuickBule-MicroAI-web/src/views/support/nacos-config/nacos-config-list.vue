<!--
  * Nacos配置管理 主页面
  *
-->
<template>
  <div>
    <a-card size="small" :bordered="false" class="qb-margin-bottom10">
      <a-space wrap>
        <a-select
          v-model:value="selectedNamespace"
          placeholder="选择命名空间"
          style="width: 200px"
          @change="onNamespaceChange"
        >
          <a-select-option v-for="ns in namespaces" :key="ns.namespace" :value="ns.namespace">
            {{ ns.namespaceShowName }}
          </a-select-option>
        </a-select>
        <a-input v-model:value="searchGroupId" placeholder="配置分组" style="width: 150px" allowClear />
        <a-button type="primary" @click="onSearch">
          <template #icon><SearchOutlined /></template>
          查询
        </a-button>
        <a-divider type="vertical" />
        <a-button @click="showEditModal()" v-privilege="'system:nacos-config:edit'" type="primary">
          <template #icon><PlusOutlined /></template>
          新建
        </a-button>
        <a-button @click="showAuditDrawer" v-privilege="'system:nacos-config:audit'">
          <template #icon><AuditOutlined /></template>
          审计
        </a-button>
        <a-divider type="vertical" />
        <a-dropdown>
          <a-button :loading="exportLoading" :disabled="selectedRowKeys.length === 0">
            <template #icon><ExportOutlined /></template>
            导出 ({{ selectedRowKeys.length }})
            <DownOutlined />
          </a-button>
          <template #overlay>
            <a-menu @click="handleExport">
              <a-menu-item key="yaml">导出为YAML</a-menu-item>
              <a-menu-item key="json">导出为JSON</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button @click="showImportModal">
          <template #icon><ImportOutlined /></template>
          导入
        </a-button>
        <a-button @click="showTemplateDrawer">
          <template #icon><FileTextOutlined /></template>
          模板库
        </a-button>
        <a-divider type="vertical" />
        <a-popconfirm
          title="将本地 nacos_config 目录下的所有配置文件同步到 Nacos 服务器，同名配置将被覆盖。确定同步？"
          ok-text="确定"
          cancel-text="取消"
          @confirm="onSyncFromLocal"
        >
          <a-button :loading="syncLoading" type="primary" ghost v-privilege="'system:nacos-config:edit'">
            <template #icon><CloudSyncOutlined /></template>
            一键同步
          </a-button>
        </a-popconfirm>
      </a-space>
    </a-card>

    <a-card size="small" :bordered="false" :hoverable="true">
      <a-table
        ref="tableRef"
        size="small"
        :loading="tableLoading"
        bordered
        :dataSource="tableData"
        :columns="columns"
        rowKey="dataId"
        :pagination="pagination"
        :row-selection="rowSelection"
        @change="onPageChange"
      >
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'dataId'">
            <a @click="showEditModal(record)" style="color: #1890ff">{{ record.dataId }}</a>
          </template>
          <template v-if="column.dataIndex === 'type'">
            <a-tag :color="record.type === 'yaml' ? 'green' : 'orange'" size="small">{{ record.type }}</a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a-button @click="showEditModal(record)" v-privilege="'system:nacos-config:edit'" type="link" size="small">编辑</a-button>
              <a-button @click="showHistoryModal(record)" v-privilege="'system:nacos-config:query'" type="link" size="small">历史</a-button>
              <a-button @click="showDiffModal(record)" v-privilege="'system:nacos-config:query'" type="link" size="small">对比</a-button>
              <a-popconfirm title="确定要删除此配置吗？" @confirm="onDelete(record)">
                <a-button v-privilege="'system:nacos-config:delete'" type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 配置编辑弹窗 -->
    <NacosConfigEditModal ref="editModal" @reload="loadConfigs" />

    <!-- 配置历史弹窗 -->
    <NacosConfigHistoryModal ref="historyModal" />

    <!-- 配置审计抽屉 -->
    <NacosConfigAuditDrawer ref="auditDrawer" />

    <!-- 配置对比弹窗 -->
    <NacosConfigDiffModal ref="diffModal" />

    <!-- 配置导入弹窗 -->
    <NacosConfigImportModal ref="importModal" @reload="loadConfigs" />

    <!-- 配置模板抽屉 -->
    <a-drawer
      v-model:open="templateDrawerVisible"
      title="配置模板库"
      :width="800"
      :bodyStyle="{ padding: '12px' }"
    >
      <NacosConfigTemplateList ref="templateList" @reload="loadConfigs" @useTemplate="handleUseTemplate" />
    </a-drawer>
  </div>
</template>

<script setup>
  import { onMounted, reactive, ref, computed } from 'vue';
  import { nacosConfigApi } from '/src/api/support/nacos-config-api';
  import { Loading } from '/src/components/framework/loading';
  import { sentry } from '/src/lib/sentry';
  import { message, Modal } from 'ant-design-vue';
  import NacosConfigEditModal from './nacos-config-edit-modal.vue';
  import NacosConfigHistoryModal from './nacos-config-history-modal.vue';
  import NacosConfigAuditDrawer from './nacos-config-audit-drawer.vue';
  import NacosConfigDiffModal from './nacos-config-diff-modal.vue';
  import NacosConfigImportModal from './nacos-config-import-modal.vue';
  import NacosConfigTemplateList from './nacos-config-template-list.vue';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  // 模板抽屉
  const templateDrawerVisible = ref(false);
  const templateList = ref();

  function showTemplateDrawer() {
    templateDrawerVisible.value = true;
  }

  // 使用模板创建配置
  async function handleUseTemplate(template) {
    templateDrawerVisible.value = false;
    // 打开配置编辑弹窗并预填充模板内容
    editModal.value.showModal(template, selectedNamespace.value);
  }

  const tableRef = ref();
  const columns = ref([
    { title: '配置ID', dataIndex: 'dataId', width: 200, ellipsis: true },
    { title: '配置名称', dataIndex: 'configName', width: 120 },
    { title: '配置分组', dataIndex: 'groupId', width: 120 },
    { title: '类型', dataIndex: 'type', width: 60 },
    { title: '操作', dataIndex: 'action', fixed: 'right', width: 170 },
  ]);

  useColumnResize(tableRef, columns);

  // 命名空间列表
  const namespaces = ref([]);
  const selectedNamespace = ref('');
  const searchGroupId = ref('');

  // 表格数据
  const tableLoading = ref(false);
  const tableData = ref([]);

  // 分页
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条`,
  });

  // 导出loading
  const exportLoading = ref(false);

  // 同步loading
  const syncLoading = ref(false);

  // 选择相关
  const selectedRowKeys = ref([]);
  const rowSelection = computed(() => ({
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys) => {
      selectedRowKeys.value = keys;
    },
    columnWidth: 40,
  }));

  // 加载命名空间
  async function loadNamespaces() {
    try {
      const res = await nacosConfigApi.listNamespaces();
      if (res.data) {
        namespaces.value = res.data;
        // 默认选择第一个非public命名空间
        const devNs = res.data.find((n) => n.namespace && n.namespace !== 'public');
        if (devNs) {
          selectedNamespace.value = devNs.namespace;
        }
      }
    } catch (e) {
      sentry.captureError(e);
    }
  }

  // 加载配置列表
  async function loadConfigs() {
    try {
      tableLoading.value = true;
      const res = await nacosConfigApi.listConfigs(
        selectedNamespace.value,
        searchGroupId.value,
        pagination.current,
        pagination.pageSize
      );
      if (res.data) {
        tableData.value = res.data.list || [];
        pagination.total = res.data.totalNum || 0;
      }
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // 搜索
  function onSearch() {
    pagination.current = 1;
    selectedRowKeys.value = [];
    loadConfigs();
  }

  // 分页变化
  function onPageChange(page) {
    pagination.current = page.current;
    pagination.pageSize = page.pageSize;
    loadConfigs();
  }

  // 命名空间变更
  function onNamespaceChange() {
    pagination.current = 1;
    selectedRowKeys.value = [];
    loadConfigs();
  }

  // 编辑弹窗
  const editModal = ref();
  async function showEditModal(record) {
    // 如果是编辑，先获取配置内容
    if (record && record.dataId) {
      try {
        Loading.show();
        const res = await nacosConfigApi.getConfig(record.dataId, record.groupId, selectedNamespace.value);
        if (res.data) {
          editModal.value.showModal({
            ...res.data,
            configName: record.configName,
          }, selectedNamespace.value);
        }
      } catch (e) {
        sentry.captureError(e);
      } finally {
        Loading.hide();
      }
    } else {
      editModal.value.showModal(null, selectedNamespace.value);
    }
  }

  // 历史弹窗
  const historyModal = ref();
  function showHistoryModal(record) {
    historyModal.value.showModal(record, selectedNamespace.value);
  }

  // 对比弹窗
  const diffModal = ref();
  function showDiffModal(record) {
    diffModal.value.showModal(record, selectedNamespace.value);
  }

  // 删除配置
  async function onDelete(record) {
    try {
      const res = await nacosConfigApi.deleteConfig(record.dataId, record.groupId, selectedNamespace.value);
      if (res.ok) {
        message.success('删除成功');
        loadConfigs();
      } else {
        message.error(res.msg || '删除失败');
      }
    } catch (e) {
      sentry.captureError(e);
    }
  }

  // 审计抽屉
  const auditDrawer = ref();
  function showAuditDrawer() {
    auditDrawer.value.show(selectedNamespace.value);
  }

  // 导入弹窗
  const importModal = ref();
  function showImportModal() {
    importModal.value.showModal(selectedNamespace.value);
  }

  // 导出配置
  async function handleExport({ key }) {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请先选择要导出的配置');
      return;
    }

    // YAML格式只支持单个导出
    if (key === 'yaml' && selectedRowKeys.value.length > 1) {
      message.warning('YAML格式只支持导出单个配置，请选择一个配置或使用JSON格式批量导出');
      return;
    }

    try {
      exportLoading.value = true;
      const res = await nacosConfigApi.exportConfigs(
        selectedRowKeys.value,
        selectedNamespace.value,
        searchGroupId.value
      );
      if (res.data) {
        if (key === 'yaml') {
          // 导出为YAML格式
          const config = res.data[0];
          const blob = new Blob([config.content || ''], { type: 'text/yaml' });
          const url = URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = config.dataId;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(url);
          message.success(`成功导出配置: ${config.dataId}`);
        } else {
          // 导出为JSON格式
          const jsonStr = JSON.stringify(res.data, null, 2);
          const blob = new Blob([jsonStr], { type: 'application/json' });
          const url = URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `nacos-config-${selectedNamespace.value || 'export'}-${new Date().toISOString().slice(0, 10)}.json`;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(url);
          message.success(`成功导出 ${res.data.length} 个配置`);
        }
      } else {
        message.error(res.msg || '导出失败');
      }
    } catch (e) {
      sentry.captureError(e);
      message.error('导出失败');
    } finally {
      exportLoading.value = false;
    }
  }

  // 一键同步：从本地目录推送配置到 Nacos
  async function onSyncFromLocal() {
    try {
      syncLoading.value = true;
      const res = await nacosConfigApi.syncFromLocal(selectedNamespace.value);
      if (res.data) {
        const { totalFiles, successCount, failCount, skipCount, results } = res.data;

        // 构建详细结果信息
        let detailMsg = `共扫描 ${totalFiles} 个文件，发布成功 ${successCount} 个`;
        if (skipCount > 0) detailMsg += `，跳过 ${skipCount} 个（无变化）`;
        if (failCount > 0) {
          detailMsg += `，失败 ${failCount} 个`;
          const fails = results.filter(r => !r.success).map(r => `${r.fileName}: ${r.message}`);
          if (fails.length > 0) detailMsg += '\n失败详情:\n' + fails.join('\n');
        }

        if (failCount > 0) {
          Modal.warning({ title: '部分同步失败', content: detailMsg, width: 520 });
        } else {
          message.success(detailMsg);
        }
        loadConfigs();
      } else {
        message.error(res.msg || '同步失败');
      }
    } catch (e) {
      sentry.captureError(e);
      message.error('一键同步失败，请检查 Nacos 连接');
    } finally {
      syncLoading.value = false;
    }
  }

  onMounted(async () => {
    await loadNamespaces();
    loadConfigs();
  });
</script>
