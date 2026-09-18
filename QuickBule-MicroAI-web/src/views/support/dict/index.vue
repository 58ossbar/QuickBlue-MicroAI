<!--
  * 数据字典管理
  *
  * 左侧：字典类型列表；右侧：选中类型对应的字典数据
-->
<template>
  <div class="dict-page">
    <!-- 左侧字典类型 -->
    <aside class="dict-type-sidebar">
      <div class="sidebar-toolbar">
        <a-input-search
          v-model:value="typeFilterText"
          placeholder="请输入关键字进行过滤"
          size="small"
          allow-clear
        />
        <a-button type="primary" size="small" @click="addDictType">
          <plus-outlined />
        </a-button>
      </div>
      <div class="sidebar-list">
        <div
          class="type-item"
          :class="{ active: selectedTypeId === null }"
          @click="selectType(null)"
        >
          <span class="type-name">全部字典</span>
        </div>
        <div
          v-for="item in filteredTypeList"
          :key="item.dictId"
          class="type-item"
          :class="{ active: selectedTypeId === item.dictId }"
          @click="selectType(item)"
        >
          <span class="type-name">{{ item.dictName }}</span>
          <span class="type-actions" @click.stop>
            <edit-outlined @click="editDictType(item)" />
            <a-popconfirm title="确定删除该字典类型吗？" @confirm="deleteDictType(item)">
              <delete-outlined />
            </a-popconfirm>
          </span>
        </div>
      </div>
    </aside>

    <!-- 右侧字典数据 -->
    <main class="dict-data-main">
      <a-form layout="inline" class="search-form">
        <a-form-item label="字典名称">
          <a-input
            v-model:value="queryForm.dataLabel"
            placeholder="请输入字典名称"
            allow-clear
            @pressEnter="queryDictData"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="queryDictData">
            <search-outlined /> 查询
          </a-button>
          <a-button @click="resetQuery">
            <redo-outlined /> 重置
          </a-button>
        </a-form-item>
      </a-form>

      <div class="table-toolbar">
        <a-space>
          <a-button type="primary" @click="addDictData">
            <plus-outlined /> 新建
          </a-button>
          <a-button danger :disabled="!selectedRowKeys.length" @click="batchDeleteDictData">
            <delete-outlined /> 删除
          </a-button>
        </a-space>
      </div>

      <a-table
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        :columns="columns"
        :data-source="treeData"
        :loading="tableLoading"
        :pagination="false"
        row-key="dictDataId"
        size="small"
        bordered
        :default-expand-all-rows="false"
        :expanded-row-keys="expandedRowKeys"
        :indent-size="24"
        :row-class-name="getDictRowClassName"
        @expand="handleExpand"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'seq'">{{ index + 1 }}</template>
          <template v-else-if="column.dataIndex === 'dataLabel'">
            <span class="dict-label-cell">
              <a-tag
                v-if="record.children && record.children.length"
                color="blue"
                class="dict-label-child-count"
              >
                {{ record.children.length }}
              </a-tag>
              {{ record.dataLabel }}
            </span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-badge :status="record.disabledFlag ? 'default' : 'processing'" :text="record.disabledFlag ? '禁用' : '启用'" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="addChildDictData(record)">
                <plus-outlined /> 新增子级字典
              </a>
              <a @click="editDictData(record)">编辑</a>
              <a-popconfirm title="确定删除吗？" @confirm="deleteDictData(record)">
                <a class="danger-text">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </main>

    <DictFormModal ref="dictFormModalRef" @reloadList="queryDictTypeList" />
    <DictDataFormModal ref="dictDataFormModalRef" @reloadList="queryDictData" />
  </div>
</template>
<script setup>
  import { reactive, ref, computed, onMounted, watch } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    PlusOutlined,
    SearchOutlined,
    RedoOutlined,
    EditOutlined,
    DeleteOutlined,
  } from '@ant-design/icons-vue';
  import { dictApi } from '/@/api/support/dict-api';
  import { Loading } from '/@/components/framework/loading';
  import { sentry } from '/@/lib/sentry';
  import DictFormModal from './components/dict-form-modal.vue';
  import DictDataFormModal from './components/dict-data-form-modal.vue';

  // ── 左侧字典类型 ──
  const typeList = ref([]);
  const typeFilterText = ref('');
  const selectedTypeId = ref(null);
  const selectedType = computed(() => typeList.value.find((e) => e.dictId === selectedTypeId.value) || null);

  const filteredTypeList = computed(() => {
    const keyword = typeFilterText.value?.trim();
    if (!keyword) return typeList.value;
    return typeList.value.filter(
      (e) => e.dictName?.includes(keyword) || e.dictCode?.includes(keyword)
    );
  });

  async function queryDictTypeList() {
    try {
      const res = await dictApi.getAllDict();
      typeList.value = res.data || [];
      // 若当前选中类型已不在列表，则切回全部字典
      if (selectedTypeId.value && !typeList.value.some((e) => e.dictId === selectedTypeId.value)) {
        selectedTypeId.value = null;
      }
    } catch (error) {
      sentry.captureError(error);
    }
  }

  function selectType(type) {
    selectedTypeId.value = type ? type.dictId : null;
    resetQuery();
    queryDictData();
  }

  const dictFormModalRef = ref();
  function addDictType() {
    dictFormModalRef.value.showModal();
  }
  function editDictType(rowData) {
    dictFormModalRef.value.showModal(rowData);
  }
  async function deleteDictType(rowData) {
    Loading.show();
    try {
      await dictApi.batchDeleteDict([rowData.dictId]);
      message.success('删除成功');
      await queryDictTypeList();
      queryDictData();
    } catch (error) {
      sentry.captureError(error);
    } finally {
      Loading.hide();
    }
  }

  // ── 右侧字典数据 ──
  const queryForm = reactive({
    dataLabel: '',
  });
  const tableLoading = ref(false);
  const dictDataList = ref([]);
  const selectedRowKeys = ref([]);

  const columns = [
    {
      title: '#',
      key: 'seq',
      width: 50,
      align: 'center',
    },
    {
      title: '字典类型',
      dataIndex: 'dictCode',
      width: 140,
      ellipsis: true,
    },
    {
      title: '字典标签',
      dataIndex: 'dataLabel',
      ellipsis: true,
    },
    {
      title: '字典键值',
      dataIndex: 'dataValue',
      width: 120,
      ellipsis: true,
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      width: 70,
      align: 'center',
    },
    {
      title: '状态',
      key: 'status',
      width: 80,
      align: 'center',
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      fixed: 'right',
    },
  ];

  // 将扁平字典数据按 parentCode 构建为带 children 的树形结构，
  // 供 a-table 树形展示，可展开/收起；按 sortOrder 倒序。
  // 若整体无层级关系（所有 parentCode 为空）则直接返回扁平列表，不生成 children，
  // 此时 a-table 不会渲染展开图标，退化为普通表格。
  // 每个节点同时被打上 _level 字段用于 a-table rowClassName 做层级区分；
  // 叶子节点的 children 字段会被清掉，避免 antd 渲染多余的 +/- 展开图标。
  function buildDictTreeData(list) {
    if (!list || list.length === 0) return [];

    const hasParent = list.some((item) => item.parentCode);
    if (!hasParent) {
      // 全部同级，按 sortOrder 倒序返回扁平数组即可
      const sortFn = (a, b) => (b.sortOrder || 0) - (a.sortOrder || 0);
      return [...list].sort(sortFn);
    }

    // 按 dictId/dictCode 分组，避免"全部字典"模式下跨类型误把 dataValue 当父级
    const groupByCode = new Map();
    list.forEach((item) => {
      const code = item.dictId || item.dictCode || '__default__';
      if (!groupByCode.has(code)) groupByCode.set(code, []);
      groupByCode.get(code).push(item);
    });

    const sortFn = (a, b) => (b.sortOrder || 0) - (a.sortOrder || 0);
    const result = [];

    // 递归给节点打 _level 标记（0 = 根级，越深层级越大）
    const setLevel = (nodes, level) => {
      nodes.forEach((n) => {
        n._level = level;
        if (n.children && n.children.length) setLevel(n.children, level + 1);
      });
    };

    // 清理叶子节点的 children 字段（无子级时删掉 children，避免 antd 显示展开按钮）
    const cleanLeafChildren = (nodes) => {
      nodes.forEach((n) => {
        if (n.children && n.children.length === 0) {
          delete n.children;
        } else if (n.children && n.children.length) {
          cleanLeafChildren(n.children);
        }
      });
    };

    groupByCode.forEach((items) => {
      // 记录每个 dataValue 对应的节点引用，便于按 parentCode 聚合
      const nodeMap = new Map();
      items.forEach((item) => {
        nodeMap.set(item.dataValue, { ...item, children: [] });
      });

      const roots = [];
      nodeMap.forEach((node) => {
        const parentCode = node.parentCode;
        if (parentCode && nodeMap.has(parentCode)) {
          nodeMap.get(parentCode).children.push(node);
        } else {
          // 根级，或父级不在当前列表（孤儿）统一作为根级展示
          roots.push(node);
        }
      });

      roots.sort(sortFn);
      nodeMap.forEach((node) => node.children && node.children.sort(sortFn));

      // 设置 _level + 清理叶子节点的 children
      setLevel(roots, 0);
      cleanLeafChildren(roots);

      result.push(...roots);
    });

    return result;
  }

  // 给 a-table 的每一行打 className，按 _level 区分底色/文字色，让层级差异一目了然
  function getDictRowClassName(record) {
    const level = record && typeof record._level === 'number' ? record._level : 0;
    return `dict-row-level-${level}`;
  }

  const baseTreeData = computed(() => buildDictTreeData(dictDataList.value));

  // 字典列表的树形数据；有搜索关键字时保留命中项及其完整父链，便于看到上下文。
  const treeData = computed(() => {
    const keyword = queryForm.dataLabel?.trim();
    if (!keyword) return baseTreeData.value;

    const matchedIds = new Set();
    dictDataList.value.forEach((e) => {
      if ((e.dataLabel?.includes(keyword)) || (e.dataValue?.includes(keyword))) {
        matchedIds.add(e.dictDataId);
      }
    });
    if (matchedIds.size === 0) return [];

    // 收集命中项完整父链 id
    const includeIds = new Set(matchedIds);
    function collectAncestors(nodes, chain) {
      nodes.forEach((n) => {
        const curChain = [...chain, n.dictDataId];
        if (matchedIds.has(n.dictDataId)) {
          curChain.forEach((id) => includeIds.add(id));
        }
        if (n.children?.length) collectAncestors(n.children, curChain);
      });
    }
    collectAncestors(baseTreeData.value, []);

    // 过滤整棵树，仅保留 includeIds 涉及的节点
    function filterTree(nodes) {
      return nodes
        .filter((n) => includeIds.has(n.dictDataId))
        .map((n) => ({
          ...n,
          children: n.children?.length ? filterTree(n.children) : undefined,
        }));
    }
    return filterTree(baseTreeData.value);
  });

  // 默认全部展开；用户收起/展开后，由 handleExpand 维护 expandedRowKeys，
  // 这里仅在树或搜索关键字变化时重新建立"全展开"基线，不会被手动收起触发覆盖。
  const expandedRowKeys = ref([]);
  watch(
    [treeData],
    () => {
      const val = treeData.value;
      if (!val || val.length === 0) {
        expandedRowKeys.value = [];
        return;
      }
      const keys = [];
      const walk = (nodes) =>
        nodes.forEach((n) => {
          keys.push(n.dictDataId);
          if (n.children?.length) walk(n.children);
        });
      walk(val);
      expandedRowKeys.value = keys;
    },
    { immediate: true }
  );

  function handleExpand(expanded, record) {
    const id = record.dictDataId;
    const set = new Set(expandedRowKeys.value);
    if (expanded) set.add(id);
    else set.delete(id);
    expandedRowKeys.value = Array.from(set);
  }

  function onSelectChange(keys) {
    selectedRowKeys.value = keys;
  }

  async function queryDictData() {
    tableLoading.value = true;
    selectedRowKeys.value = [];
    try {
      let data = [];
      if (!selectedTypeId.value) {
        // 全部字典
        const res = await dictApi.getAllDictData();
        data = res.data || [];
      } else {
        const res = await dictApi.queryDictData(selectedTypeId.value);
        data = (res.data || []).map((e) => ({
          ...e,
          dictCode: selectedType.value?.dictCode,
        }));
      }
      dictDataList.value = data;
    } catch (error) {
      sentry.captureError(error);
    } finally {
      tableLoading.value = false;
    }
  }

  function resetQuery() {
    queryForm.dataLabel = '';
  }

  const dictDataFormModalRef = ref();
  function addDictData() {
    const dictId = selectedType.value?.dictId || typeList.value[0]?.dictId;
    if (!dictId) {
      message.warning('请先选择或新增字典类型');
      return;
    }
    dictDataFormModalRef.value.showModal(null, dictId);
  }
  function editDictData(rowData) {
    dictDataFormModalRef.value.showModal(rowData);
  }
  function addChildDictData(rowData) {
    const dictId = selectedType.value?.dictId || rowData.dictId;
    if (!dictId) {
      message.warning('请先选择或新增字典类型');
      return;
    }
    dictDataFormModalRef.value.showModal(null, dictId, rowData);
  }
  async function deleteDictData(rowData) {
    Loading.show();
    try {
      await dictApi.batchDeleteDictData([rowData.dictDataId]);
      message.success('删除成功');
      queryDictData();
    } catch (error) {
      sentry.captureError(error);
    } finally {
      Loading.hide();
    }
  }
  async function batchDeleteDictData() {
    if (!selectedRowKeys.value.length) return;
    Modal.confirm({
      title: '确定删除吗？',
      content: `已选择 ${selectedRowKeys.value.length} 条数据，删除后不可恢复`,
      async onOk() {
        Loading.show();
        try {
          await dictApi.batchDeleteDictData(selectedRowKeys.value);
          message.success('删除成功');
          queryDictData();
        } catch (error) {
          sentry.captureError(error);
        } finally {
          Loading.hide();
        }
      },
    });
  }

  onMounted(() => {
    queryDictTypeList().then(() => {
      // 默认选中第一个类型，若无则显示全部
      if (typeList.value.length) {
        selectedTypeId.value = typeList.value[0].dictId;
      }
      queryDictData();
    });
  });
</script>
<style scoped lang="less">
  .dict-page {
    display: flex;
    height: 100%;
    background: #fff;
  }

  .dict-type-sidebar {
    width: 240px;
    min-width: 200px;
    border-right: 1px solid #f0f0f0;
    display: flex;
    flex-direction: column;

    .sidebar-toolbar {
      display: flex;
      gap: 8px;
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;

      :deep(.ant-input-search) {
        flex: 1;
      }
    }

    .sidebar-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px 0;

      .type-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 9px 16px;
        cursor: pointer;
        transition: all 0.2s;
        color: #333;

        &:hover {
          background: #f5f5f5;

          .type-actions {
            opacity: 1;
          }
        }

        &.active {
          background: #e6f7ff;
          color: #1890ff;
          border-right: 3px solid #1890ff;

          .type-actions {
            opacity: 1;
          }
        }

        .type-name {
          font-size: 14px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          flex: 1;
        }

        .type-actions {
          display: flex;
          align-items: center;
          gap: 10px;
          opacity: 0;
          transition: opacity 0.2s;
          color: #999;

          .anticon {
            font-size: 13px;
            cursor: pointer;

            &:hover {
              color: #1890ff;
            }
          }

          .anticon-delete:hover {
            color: #ff4d4f;
          }
        }
      }
    }
  }

  .dict-data-main {
    flex: 1;
    min-width: 0;
    padding: 16px;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .search-form {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 16px;
      border-bottom: 1px solid #f0f0f0;
      margin-bottom: 16px;

      :deep(.ant-form-item) {
        margin-bottom: 0;
        margin-right: 12px;

        &:last-child {
          margin-right: 0;
          margin-left: auto;
        }
      }
    }

    .table-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    .danger-text {
      color: #ff4d4f;
    }

    .dict-label-cell {
      display: inline-flex;
      align-items: center;
      vertical-align: middle;
      word-break: break-all;
    }

    .dict-label-child-count {
      margin-right: 6px;
      font-size: 12px;
    }

    :deep(.ant-table-row-expand-icon) {
      background: transparent;
    }

    // 不同层级行用不同底色/透明度区分，深层级的子级更"淡"
    :deep(.ant-table-tbody) {
      > tr.dict-row-level-0 > td {
        background-color: #ffffff;
      }
      > tr.dict-row-level-1 > td {
        background-color: #f7f9fc;
        color: rgba(0, 0, 0, 0.85);
      }
      > tr.dict-row-level-2 > td {
        background-color: #eef2f7;
        color: rgba(0, 0, 0, 0.7);
      }
      > tr.dict-row-level-3 > td {
        background-color: #e6ebf2;
        color: rgba(0, 0, 0, 0.6);
      }
      > tr.dict-row-level-1:hover > td,
      > tr.dict-row-level-2:hover > td,
      > tr.dict-row-level-3:hover > td {
        // 子级行 hover 时维持层级底色，不被 antd 默认 hover 覆盖
        background-color: inherit;
      }
    }

    :deep(.ant-table-wrapper) {
      flex: 1;
      overflow: auto;
    }
  }
</style>
