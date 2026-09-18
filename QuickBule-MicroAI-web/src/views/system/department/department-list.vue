<template>
  <div class="department-manager">
    <!-- 左侧部门树 -->
    <div class="dept-left-panel">
      <div class="left-panel-header">
        <span class="panel-title">
          <ApartmentOutlined />
          组织架构
        </span>
        <a-tooltip title="新建一级部门">
          <a-button v-privilege="'system:department:add'" type="primary" size="small" @click="addTopDepartment">
            <PlusOutlined />
          </a-button>
        </a-tooltip>
      </div>
      <div class="tree-search-box">
        <a-input v-model:value="keywords" placeholder="搜索部门" allow-clear size="small" @change="onSearch">
          <template #prefix><SearchOutlined /></template>
        </a-input>
      </div>
      <div class="tree-wrapper">
        <a-spin :spinning="treeLoading" v-if="departmentTreeData.length === 0 && !treeLoading">
          <div class="tree-empty">暂无部门数据</div>
        </a-spin>
        <a-tree
          v-else
          v-model:selectedKeys="selectedKeys"
          v-model:expandedKeys="expandedKeys"
          :treeData="departmentTreeData"
          :fieldNames="{ title: 'departmentName', key: 'departmentId' }"
          :showLine="{ showLeafIcon: false }"
          blockNode
          @select="onTreeSelect"
        >
          <template #title="nodeData">
            <div
              class="tree-node-title"
              :class="{ active: selectedKeys[0] === nodeData.departmentId }"
              @contextmenu.prevent="onContextMenu($event, nodeData)"
            >
              <FolderOutlined class="node-icon" />
              <span class="node-label">{{ nodeData.departmentName }}</span>
            </div>
          </template>
        </a-tree>
      </div>
    </div>

    <!-- 右侧详情 -->
    <div class="dept-right-panel">
      <template v-if="currentDepartment">
        <!-- 操作栏 -->
        <div class="detail-toolbar">
          <div class="toolbar-title">
            {{ currentDepartment.departmentName }}
          </div>
          <a-space>
            <a-button v-privilege="'system:department:add'" type="primary" size="small" @click="addSubDepartment">
              <PlusOutlined /> 添加下级
            </a-button>
            <a-button v-privilege="'system:department:update'" size="small" @click="editDepartment">
              <EditOutlined /> 编辑
            </a-button>
            <a-button
              v-privilege="'system:department:delete'"
              danger
              size="small"
              @click="deleteDepartment(currentDepartment.departmentId)"
              v-if="currentDepartment.departmentId != topDepartmentId"
            >
              <DeleteOutlined /> 删除
            </a-button>
          </a-space>
        </div>

        <!-- 基本信息 -->
        <a-card size="small" class="info-card" title="基本信息">
          <a-descriptions :column="2" size="small">
            <a-descriptions-item label="部门名称">{{ currentDepartment.departmentName }}</a-descriptions-item>
            <a-descriptions-item label="部门编码">{{ currentDepartment.departmentCode || '-' }}</a-descriptions-item>
            <a-descriptions-item label="负责人">{{ currentDepartment.managerName || '未设置' }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-badge :status="currentDepartment.status === 1 ? 'success' : 'error'" :text="currentDepartment.status === 1 ? '启用' : '禁用'" />
            </a-descriptions-item>
            <a-descriptions-item label="层级">第 {{ currentDepartment.level ?? 0 }} 级</a-descriptions-item>
            <a-descriptions-item label="排序">{{ currentDepartment.sort ?? 0 }}</a-descriptions-item>
            <a-descriptions-item label="更新时间">{{ currentDepartment.updateTime ? dayjs(currentDepartment.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ currentDepartment.createTime ? dayjs(currentDepartment.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}</a-descriptions-item>
          </a-descriptions>
          <div v-if="currentDepartment.remark" class="remark-section">
            <span class="remark-label">描述：</span>
            <span class="remark-text">{{ currentDepartment.remark }}</span>
          </div>
        </a-card>

        <!-- 下级部门 -->
        <a-card size="small" class="info-card" :title="`下级部门 (${subDepartments.length})`">
          <a-table
            ref="tableRef"
            v-if="subDepartments.length > 0"
            size="small"
            :columns="subColumns"
            :data-source="subDepartments"
            :pagination="false"
            rowKey="departmentId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <a-space :size="0">
                  <a-button v-privilege="'system:department:add'" type="link" size="small" @click="addDepartment(record)">添加下级</a-button>
                  <a-button v-privilege="'system:department:update'" type="link" size="small" @click="updateDepartment(record)">编辑</a-button>
                  <a-button v-privilege="'system:department:delete'" type="link" danger size="small" @click="deleteDepartment(record.departmentId)">删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
          <a-empty v-else description="暂无下级部门" :image-style="{ height: '60px' }" />
        </a-card>
      </template>

      <!-- 空状态 -->
      <div class="no-selection" v-else>
        <a-empty description="请从左侧选择一个部门">
          <a-button type="primary" v-privilege="'system:department:add'" @click="addTopDepartment">
            <PlusOutlined /> 新建一级部门
          </a-button>
        </a-empty>
      </div>
    </div>

    <DepartmentFormModal ref="departmentFormModal" @refresh="handleRefresh" />

    <!-- 右键菜单 -->
    <div
      v-if="contextMenu.visible"
      class="context-menu-overlay"
      @click="closeContextMenu"
      @contextmenu.prevent="closeContextMenu"
    >
      <div class="context-menu" :style="{ left: adjustMenuX(contextMenu.x) + 'px', top: adjustMenuY(contextMenu.y) + 'px' }">
        <div v-privilege="'system:department:add'" class="context-menu-item" @click.stop="handleContextMenuAction('add')">
          <PlusOutlined /><span>添加下级</span>
        </div>
        <div v-privilege="'system:department:update'" class="context-menu-item" @click.stop="handleContextMenuAction('edit')">
          <EditOutlined /><span>编辑部门</span>
        </div>
        <div
          v-if="contextMenu.node?.departmentId != topDepartmentId"
          v-privilege="'system:department:delete'"
          class="context-menu-item danger"
          @click.stop="handleContextMenuAction('delete')"
        >
          <DeleteOutlined /><span>删除部门</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { onMounted, ref, reactive, computed, createVNode, onUnmounted } from 'vue';
  import { departmentApi } from '/@/api/system/department-api';
  import { Modal } from 'ant-design-vue';
  import {
    SearchOutlined,
    PlusOutlined,
    ApartmentOutlined,
    FolderOutlined,
    EditOutlined,
    DeleteOutlined,
    ExclamationCircleOutlined,
  } from '@ant-design/icons-vue';
  import _ from 'lodash';
  import dayjs from 'dayjs';
  import { Loading } from '/@/components/framework/loading';
  import DepartmentFormModal from './components/department-form-modal.vue';
  import { sentry } from '/@/lib/sentry';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  const DEPARTMENT_PARENT_ID = 0;

  const keywords = ref('');
  const treeLoading = ref(false);
  const departmentList = ref([]);
  const departmentTreeData = ref([]);
  const idInfoMap = ref(new Map());
  const topDepartmentId = ref();
  const selectedKeys = ref([]);
  const expandedKeys = ref([]);
  const currentDepartment = ref(null);

  // 右键菜单
  const contextMenu = reactive({
    visible: false,
    x: 0,
    y: 0,
    node: null,
  });

  const menuWidth = 140;
  const menuItemHeight = 36;

  function onContextMenu(event, nodeData) {
    contextMenu.visible = false;
    // 先选中该节点
    selectDepartment(nodeData.departmentId);
    contextMenu.node = nodeData;
    // 延迟计算位置，在 nextTick 后设置
    setTimeout(() => {
      contextMenu.x = event.clientX;
      contextMenu.y = event.clientY;
      contextMenu.visible = true;
    }, 0);
  }

  function adjustMenuX(x) {
    const maxX = window.innerWidth - menuWidth - 10;
    return Math.min(x, maxX);
  }

  function adjustMenuY(y) {
    const maxY = window.innerHeight - menuItemHeight * 3 - 20;
    return Math.min(y, maxY);
  }

  function closeContextMenu() {
    contextMenu.visible = false;
    contextMenu.node = null;
  }

  function handleContextMenuAction(action) {
    const node = contextMenu.node;
    if (!node) return;
    closeContextMenu();
    if (action === 'add') {
      const dept = idInfoMap.value.get(node.departmentId);
      addDepartment(dept);
    } else if (action === 'edit') {
      const dept = idInfoMap.value.get(node.departmentId);
      updateDepartment(dept);
    } else if (action === 'delete') {
      deleteDepartment(node.departmentId);
    }
  }

  onUnmounted(() => {
    closeContextMenu();
  });

  const subDepartments = computed(() => {
    if (!currentDepartment.value) return [];
    return departmentList.value.filter((e) => e.parentId == currentDepartment.value.departmentId);
  });

  const subColumns = [
    { title: '部门名称', dataIndex: 'departmentName', key: 'departmentName' },
    { title: '负责人', dataIndex: 'managerName', key: 'managerName', width: 100 },
    { title: '排序', dataIndex: 'sort', key: 'sort', width: 80, align: 'center' },
    { title: '操作', key: 'action', width: 200, align: 'center' },
  ];

  const tableRef = ref();
  useColumnResize(tableRef, subColumns);

  onMounted(() => {
    queryDepartmentTree();
  });

  async function queryDepartmentTree() {
    try {
      treeLoading.value = true;
      let res = await departmentApi.queryAllDepartment();
      let data = res.data;
      idInfoMap.value.clear();
      data.forEach((e) => idInfoMap.value.set(e.departmentId, e));
      departmentList.value = data;
      departmentTreeData.value = buildDepartmentTree(data, DEPARTMENT_PARENT_ID);
      if (!_.isEmpty(departmentTreeData.value) && departmentTreeData.value.length > 0) {
        topDepartmentId.value = departmentTreeData.value[0].departmentId;
        selectDepartment(topDepartmentId.value);
        expandedKeys.value = [topDepartmentId.value];
      }
    } catch (e) {
      sentry.captureError(e);
    } finally {
      treeLoading.value = false;
    }
  }

  function buildDepartmentTree(data, parentId) {
    let children = data.filter((e) => e.parentId == parentId) || [];
    if (!_.isEmpty(children)) {
      children.forEach((e) => {
        e.children = buildDepartmentTree(data, e.departmentId);
      });
      return children;
    }
    return null;
  }

  function onTreeSelect(keys) {
    if (_.isEmpty(keys)) {
      selectedKeys.value = [currentDepartment.value?.departmentId].filter(Boolean);
      return;
    }
    selectDepartment(keys[0]);
  }

  function selectDepartment(id) {
    selectedKeys.value = [id];
    currentDepartment.value = departmentList.value.find((e) => e.departmentId == id) || null;
    if (currentDepartment.value) {
      expandAncestors(currentDepartment.value.parentId);
    }
  }

  function expandAncestors(parentId) {
    if (!parentId || parentId == 0) return;
    if (!expandedKeys.value.includes(parentId)) {
      expandedKeys.value.push(parentId);
    }
    const parent = idInfoMap.value.get(parentId);
    if (parent && parent.parentId) {
      expandAncestors(parent.parentId);
    }
  }

  function onSearch() {
    if (!keywords.value) {
      departmentTreeData.value = buildDepartmentTree(departmentList.value, DEPARTMENT_PARENT_ID);
      return;
    }
    const filterDepartment = departmentList.value.filter((e) => e.departmentName.indexOf(keywords.value) > -1);
    const filterDepartmentList = [];
    filterDepartment.forEach((e) => {
      recursionFilterDepartment(filterDepartmentList, e.departmentId, false);
    });
    departmentTreeData.value = buildDepartmentTree(filterDepartmentList, DEPARTMENT_PARENT_ID);
    if (keywords.value && departmentTreeData.value.length > 0) {
      const allIds = [];
      const collect = (nodes) => {
        if (!nodes) return;
        nodes.forEach((n) => { allIds.push(n.departmentId); collect(n.children); });
      };
      collect(departmentTreeData.value);
      expandedKeys.value = allIds;
    }
  }

  function recursionFilterDepartment(resList, id, unshift) {
    let info = idInfoMap.value.get(id);
    if (!info || resList.some((e) => e.departmentId == id)) return;
    unshift ? resList.unshift(info) : resList.push(info);
    if (info.parentId && info.parentId != 0) {
      recursionFilterDepartment(resList, info.parentId, unshift);
    }
  }

  const departmentFormModal = ref();

  function addTopDepartment() {
    departmentFormModal.value.showModal({ departmentId: 0, departmentName: '', parentId: null });
  }
  function addSubDepartment() {
    addDepartment(currentDepartment.value);
  }
  function addDepartment(parentDept) {
    departmentFormModal.value.showModal({ departmentId: 0, departmentName: '', parentId: parentDept?.departmentId || null });
  }
  function updateDepartment(dept) {
    departmentFormModal.value.showModal(dept);
  }
  function editDepartment() {
    updateDepartment(currentDepartment.value);
  }

  function deleteDepartment(id) {
    Modal.confirm({
      title: '提醒',
      icon: createVNode(ExclamationCircleOutlined),
      content: '确定要删除该部门吗？',
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      async onOk() {
        Loading.show();
        try {
          await departmentApi.deleteDepartment(id);
          await handleRefresh();
        } catch (error) {
          sentry.captureError(error);
        } finally {
          Loading.hide();
        }
      },
    });
  }

  async function handleRefresh() {
    const prevSelectedId = currentDepartment.value?.departmentId;
    await queryDepartmentTree();
    if (prevSelectedId && idInfoMap.value.has(prevSelectedId)) {
      selectDepartment(prevSelectedId);
    }
  }
</script>

<style scoped lang="less">
  @theme-color: #2F54EB;
  @theme-bg: #f0f5ff;
  @border-color: #f0f0f0;

  .department-manager {
    display: flex;
    height: calc(100vh - 170px);
    min-height: 560px;
    background: #fff;
    border-radius: 6px;
    overflow: hidden;
  }

  // ========== 左侧面板 ==========
  .dept-left-panel {
    width: 260px;
    min-width: 260px;
    border-right: 1px solid @border-color;
    display: flex;
    flex-direction: column;

    .left-panel-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 14px 16px 10px;
      border-bottom: 1px solid @border-color;

      .panel-title {
        font-size: 14px;
        font-weight: 600;
        color: #1d2129;
        display: flex;
        align-items: center;
        gap: 6px;

        .anticon {
          color: @theme-color;
          font-size: 16px;
        }
      }
    }

    .tree-search-box {
      padding: 10px 12px;
    }

    .tree-wrapper {
      flex: 1;
      overflow-y: auto;
      padding: 0 8px 8px;

      :deep(.ant-tree) {
        background: transparent;

        .ant-tree-node-content-wrapper {
          border-radius: 4px;
          &:hover {
            background: @theme-bg;
          }
        }
        .ant-tree-node-selected {
          background: transparent !important;
        }
      }

      .tree-node-title {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 2px 0;
        cursor: pointer;

        .node-icon {
          font-size: 14px;
          color: #faad14;
          flex-shrink: 0;
        }
        .node-label {
          font-size: 13px;
          color: #1d2129;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        &.active {
          .node-icon {
            color: @theme-color;
          }
          .node-label {
            color: @theme-color;
            font-weight: 500;
          }
        }
      }

      .tree-empty {
        text-align: center;
        color: #999;
        padding: 40px 0;
        font-size: 13px;
      }
    }
  }

  // ========== 右侧面板 ==========
  .dept-right-panel {
    flex: 1;
    overflow-y: auto;
    padding: 16px 20px;
    background: #fafafa;
  }

  .detail-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;

    .toolbar-title {
      font-size: 16px;
      font-weight: 600;
      color: #1d2129;
      position: relative;
      padding-left: 12px;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 16px;
        background: @theme-color;
        border-radius: 2px;
      }
    }
  }

  .info-card {
    margin-bottom: 16px;

    :deep(.ant-card-head) {
      min-height: 40px;
      padding: 0 16px;
      .ant-card-head-title {
        font-size: 14px;
        font-weight: 500;
        padding: 10px 0;
      }
    }
    :deep(.ant-card-body) {
      padding: 16px;
    }

    .remark-section {
      margin-top: 8px;
      padding-top: 8px;
      border-top: 1px dashed @border-color;
      display: flex;
      gap: 8px;
      .remark-label {
        color: #999;
        font-size: 12px;
        flex-shrink: 0;
      }
      .remark-text {
        color: #666;
        font-size: 13px;
        line-height: 1.6;
      }
    }
  }

  .no-selection {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
  }

  // ========== 右键菜单 ==========
  .context-menu-overlay {
    position: fixed;
    inset: 0;
    z-index: 1000;
    background: transparent;
  }

  .context-menu {
    position: fixed;
    z-index: 1001;
    min-width: 140px;
    background: #fff;
    border-radius: 6px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    padding: 4px 0;
    overflow: hidden;
    animation: contextMenuIn 0.15s ease;

    .context-menu-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      font-size: 13px;
      color: #1d2129;
      cursor: pointer;
      transition: all 0.15s;
      white-space: nowrap;

      .anticon {
        font-size: 14px;
        color: #666;
      }

      &:hover {
        background: @theme-bg;
        color: @theme-color;

        .anticon {
          color: @theme-color;
        }
      }

      &.danger {
        &:hover {
          background: #fff1f0;
          color: #ff4d4f;

          .anticon {
            color: #ff4d4f;
          }
        }
      }
    }
  }

  @keyframes contextMenuIn {
    from {
      opacity: 0;
      transform: scale(0.95);
    }
    to {
      opacity: 1;
      transform: scale(1);
    }
  }
</style>
