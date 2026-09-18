<!--
  * 部门树形结构
  *
-->
<template>
  <a-card class="tree-container">
    <a-row class="qb-margin-bottom10">
      <a-input v-model:value.trim="keywords" placeholder="请输入部门名称" />
    </a-row>
    <a-tree
      v-if="!_.isEmpty(departmentTreeData)"
      v-model:selectedKeys="selectedKeys"
      v-model:checkedKeys="checkedKeys"
      class="tree"
      :treeData="departmentTreeData"
      :fieldNames="{ title: 'departmentName', key: 'departmentId', value: 'departmentId' }"
      :checkable="props.checkable"
      :checkStrictly="props.checkStrictly"
      :selectable="!props.checkable"
      :defaultExpandAll="true"
      :showLine="{ showLeafIcon: false }"
      blockNode
      @select="treeSelectChange"
    >
      <template #title="item">
        <div class="tree-node-title" :class="{ active: selectedKeys[0] === item.departmentId }">
          <FolderOutlined class="node-icon" />
          <span class="node-label">{{ item.departmentName }}</span>
        </div>
      </template>
    </a-tree>
    <div class="no-data" v-else>暂无结果</div>
  </a-card>
</template>
<script setup>
  import { onMounted, onUnmounted, ref, watch } from 'vue';
  import _ from 'lodash';
  import { FolderOutlined } from '@ant-design/icons-vue';
  import { departmentApi } from '/@/api/system/department-api';
  import departmentEmitter from '../../department-mitt';
  import { sentry } from '/@/lib/sentry';

  const DEPARTMENT_PARENT_ID = 0;

  // ----------------------- 组件参数 ---------------------

  const props = defineProps({
    // 是否可以选中
    checkable: {
      type: Boolean,
      default: false,
    },
    // 父子节点选中状态不再关联
    checkStrictly: {
      type: Boolean,
      default: false,
    },
    // 树高度 超出出滚动条
    height: Number,
    // 显示菜单
    showMenu: {
      type: Boolean,
      default: false,
    },
  });

  // ----------------------- 部门树的展示 ---------------------
  const topDepartmentId = ref();
  // 所有部门列表
  const departmentList = ref([]);
  // 部门树形数据
  const departmentTreeData = ref([]);
  // 存放部门id和部门，用于查找
  const idInfoMap = ref(new Map());

  onMounted(() => {
    queryDepartmentTree();
  });

  onUnmounted(() => {
    // 只移除 selectTree 事件监听器
    departmentEmitter.off('selectTree');
  });

  // 刷新
  async function refresh() {
    await queryDepartmentTree();
    if (currentSelectedDepartmentId.value) {
      selectTree(currentSelectedDepartmentId.value);
    }
  }

  // 查询部门列表并构建 部门树
  async function queryDepartmentTree() {
    let res = await departmentApi.queryAllDepartment();
    let data = res.data;
    departmentList.value = data;
    departmentTreeData.value = buildDepartmentTree(data, DEPARTMENT_PARENT_ID);

    data.forEach((e) => {
      idInfoMap.value.set(e.departmentId, e);
    });

    // 默认显示 最顶级ID为列表中返回的第一条数据的ID
    if (!_.isEmpty(departmentTreeData.value) && departmentTreeData.value.length > 0) {
      topDepartmentId.value = departmentTreeData.value[0].departmentId;
    }

    selectTree(departmentTreeData.value[0].departmentId);
  }

  // 构建部门树
  function buildDepartmentTree(data, parentId) {
    let children = data.filter((e) => e.parentId == parentId) || [];
    children.forEach((e) => {
      e.children = buildDepartmentTree(data, e.departmentId);
    });
    updateDepartmentPreIdAndNextId(children);
    return children;
  }

  // 更新树的前置id和后置id
  function updateDepartmentPreIdAndNextId(data) {
    for (let index = 0; index < data.length; index++) {
      if (index === 0) {
        data[index].nextId = data.length > 1 ? data[1].departmentId : undefined;
        continue;
      }

      if (index === data.length - 1) {
        data[index].preId = data[index - 1].departmentId;
        data[index].nextId = undefined;
        continue;
      }

      data[index].preId = data[index - 1].departmentId;
      data[index].nextId = data[index + 1].departmentId;
    }
  }

  // ----------------------- 树的选中 ---------------------
  const selectedKeys = ref([]);
  const checkedKeys = ref([]);
  const breadcrumb = ref([]);
  const currentSelectedDepartmentId = ref();
  const selectedDepartmentChildren = ref([]);

  departmentEmitter.on('selectTree', selectTree);

  function selectTree(id) {
    selectedKeys.value = [id];
    treeSelectChange(selectedKeys.value);
  }

  function treeSelectChange(idList) {
    if (_.isEmpty(idList)) {
      breadcrumb.value = [];
      selectedDepartmentChildren.value = [];
      return;
    }
    let id = idList[0];
    console.log('treeSelectChange - id:', id, 'idList:', idList);
    // 阻止取消选择，保持选中状态
    if (selectedKeys.value[0] !== id) {
      selectedKeys.value = [id];
    }
    currentSelectedDepartmentId.value = id;
    selectedDepartmentChildren.value = departmentList.value.filter((e) => e.parentId == id);
    let filterDepartmentList = [];
    recursionFilterDepartment(filterDepartmentList, id, true);
    breadcrumb.value = filterDepartmentList.map((e) => e.departmentName);
    // 通知员工列表刷新并重置开关状态
    console.log('emitting departmentChange event with id:', id);
    departmentEmitter.emit('departmentChange', id);
  }

  // -----------------------  筛选 ---------------------
  const keywords = ref('');
  watch(
    () => keywords.value,
    () => {
      onSearch();
    }
  );

  // 筛选
  function onSearch() {
    if (!keywords.value) {
      departmentTreeData.value = buildDepartmentTree(departmentList.value, DEPARTMENT_PARENT_ID);
      return;
    }
    let originData = departmentList.value.concat();
    if (!originData) {
      return;
    }
    // 筛选出名称符合的部门
    let filterDepartment = originData.filter((e) => e.departmentName.indexOf(keywords.value) > -1);
    let filterDepartmentList = [];
    // 循环筛选出的部门 构建部门树
    filterDepartment.forEach((e) => {
      recursionFilterDepartment(filterDepartmentList, e.departmentId, false);
    });

    departmentTreeData.value = buildDepartmentTree(filterDepartmentList, DEPARTMENT_PARENT_ID);
  }

  // 根据ID递归筛选部门
  function recursionFilterDepartment(resList, id, unshift) {
    let info = idInfoMap.value.get(id);
    if (!info || resList.some((e) => e.departmentId == id)) {
      return;
    }
    if (unshift) {
      resList.unshift(info);
    } else {
      resList.push(info);
    }
    if (info.parentId && info.parentId != 0) {
      recursionFilterDepartment(resList, info.parentId, unshift);
    }
  }

  onUnmounted(() => {
    departmentEmitter.all.clear();
  });

  // ----------------------- 以下是暴露的方法内容 ----------------------------
  defineExpose({
    queryDepartmentTree,
    selectedDepartmentChildren,
    breadcrumb,
    selectedKeys,
    checkedKeys,
    keywords,
  });
</script>
<style scoped lang="less">
  @theme-color: #2F54EB;

  .tree-container {
    height: 100%;
    display: flex;
    flex-direction: column;
    .tree {
      flex: 1;
      margin-top: 10px;
      overflow: hidden;

      :deep(.ant-tree) {
        background: transparent;

        .ant-tree-node-content-wrapper {
          border-radius: 4px;
          &:hover {
            background: #f0f5ff;
          }
        }
        .ant-tree-node-selected {
          background: transparent !important;
        }
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

    .sort-flag-row {
      margin-top: 10px;
      margin-bottom: 10px;
    }

    .sort-span {
      margin-left: 5px;
    }
    .no-data {
      margin: 10px;
    }
  }
</style>
