<!--
  * 菜单列表
  *
  * @Author:
  * @Date:      2022-06-12 20:11:39
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <div>
    <a-form class="qb-query-form">
      <a-row class="qb-query-form-row">
        <a-form-item label="关键字" class="qb-query-form-item">
          <a-input style="width: 300px" v-model:value="queryForm.keywords" placeholder="菜单名称/路由地址/组件路径/权限字符串" />
        </a-form-item>

        <a-form-item label="类型" class="qb-query-form-item">
          <EnumSelect width="120px" v-model:value="queryForm.menuType" placeholder="请选择类型" enum-name="MENU_TYPE_ENUM" />
        </a-form-item>

        <a-form-item label="禁用" class="qb-query-form-item">
          <EnumSelect width="120px" enum-name="FLAG_NUMBER_ENUM" v-model:value="queryForm.disabledFlag" />
        </a-form-item>

        <a-form-item class="qb-query-form-item qb-margin-left10">
          <a-button-group>
            <a-button type="primary" @click="query">
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
          <a-button class="qb-margin-left20" @click="moreQueryConditionFlag = !moreQueryConditionFlag">
            <template #icon>
              <MoreOutlined />
            </template>
            {{ moreQueryConditionFlag ? '收起' : '展开' }}
          </a-button>
        </a-form-item>
      </a-row>

      <a-row class="qb-query-form-row" v-show="moreQueryConditionFlag">
        <a-form-item label="外链" class="qb-query-form-item">
          <EnumSelect width="120px" enum-name="FLAG_NUMBER_ENUM" v-model:value="queryForm.frameFlag" />
        </a-form-item>

        <a-form-item label="缓存" class="qb-query-form-item">
          <EnumSelect width="120px" enum-name="FLAG_NUMBER_ENUM" v-model:value="queryForm.cacheFlag" />
        </a-form-item>

        <a-form-item label="显示" class="qb-query-form-item">
          <EnumSelect width="120px" enum-name="FLAG_NUMBER_ENUM" v-model:value="queryForm.visibleFlag" />
        </a-form-item>
      </a-row>
    </a-form>

    <a-card size="small" :bordered="false" :hoverable="true">
      <a-row class="qb-table-btn-block">
        <div class="qb-table-operate-block">
          <a-button v-privilege="'system:menu:add'" type="primary" @click="showDrawer">
            <template #icon>
              <PlusOutlined />
            </template>
            添加菜单
          </a-button>

          <a-button v-privilege="'system:menu:batchDelete'" type="primary" danger @click="batchDelete" :disabled="!hasSelected">
            <template #icon>
              <DeleteOutlined />
            </template>
            批量删除
          </a-button>
        </div>
        <div class="qb-table-setting-block">
          <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.SYSTEM.MENU" :refresh="query" />
        </div>
      </a-row>

      <a-table
        ref="tableRef"
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        size="small"
        :scroll="{ y: 800 }"
        :defaultExpandAllRows="true"
        :dataSource="tableData"
        bordered
        :columns="columns"
        :loading="tableLoading"
        rowKey="menuId"
        :pagination="false"
      >
        <template #bodyCell="{ text, record, column }">
          <template v-if="column.dataIndex === 'menuType'">
            <a-tag :color="menuTypeColorArray[text]">{{ $enumPlugin.getDescByValue('MENU_TYPE_ENUM', text) }}</a-tag>
          </template>

          <template v-if="column.dataIndex === 'component'">
            <span>{{ record.frameFlag ? record.frameUrl : record.component }}</span>
          </template>

          <template v-if="column.dataIndex === 'frameFlag'">
            <span>{{ $enumPlugin.getDescByValue('FLAG_NUMBER_ENUM', text) }}</span>
          </template>

          <template v-if="column.dataIndex === 'permsType'">
            <span>{{ $enumPlugin.getDescByValue('MENU_PERMS_TYPE_ENUM', text) }}</span>
          </template>

          <template v-if="column.dataIndex === 'cacheFlag'">
            <span>{{ $enumPlugin.getDescByValue('FLAG_NUMBER_ENUM', text) }}</span>
          </template>

          <template v-if="column.dataIndex === 'visibleFlag'">
            <span>{{ $enumPlugin.getDescByValue('FLAG_NUMBER_ENUM', text) }}</span>
          </template>

          <template v-if="column.dataIndex === 'disabledFlag'">
            <span>{{ $enumPlugin.getDescByValue('FLAG_NUMBER_ENUM', text) }}</span>
          </template>

          <template v-if="column.dataIndex === 'icon'">
            <component :is="$antIcons[text]" />
          </template>

          <template v-if="column.dataIndex === 'operate'">
            <div class="qb-table-operate">
              <a-button
                v-if="record.menuType !== MENU_TYPE_ENUM.POINTS.value"
                v-privilege="'system:menu:update'"
                type="link"
                size="small"
                @click="showAddSub(record)"
              >
                添加下级
              </a-button>
              <a-button v-privilege="'system:menu:update'" type="link" size="small" @click="showDrawer(record)">编辑</a-button>
              <a-button v-privilege="'system:menu:batchDelete'" danger type="link" @click="singleDelete(record)">删除</a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <MenuOperateModal ref="menuOperateModal" @reloadList="query" />
  </div>
</template>
<script setup>
  import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
  import { message, Modal } from 'ant-design-vue';
  import _ from 'lodash';
  import { computed, createVNode, onMounted, reactive, ref } from 'vue';
  import MenuOperateModal from './components/menu-operate-modal.vue';
  import { buildMenuTableTree, filterMenuByQueryForm } from './menu-data-handler';
  import { columns } from './menu-list-table-columns';
  import { menuApi } from '/@/api/system/menu-api';
  import EnumSelect from '/@/components/framework/enum-select/index.vue';
  import { Loading } from '/@/components/framework/loading';
  import { sentry } from '/@/lib/sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import { MENU_TYPE_ENUM } from '/@/constants/system/menu-const';
  import { useColumnResize } from '/@/hooks/useColumnResize';

  // ------------------------ 表格渲染 ------------------------
  const tableRef = ref();
  useColumnResize(tableRef, columns);
  const menuTypeColorArray = ['red', 'blue', 'orange', 'green'];

  // ------------------------ 查询表单 ------------------------
  const queryFormState = {
    keywords: '',
    menuType: undefined,
    frameFlag: undefined,
    cacheFlag: undefined,
    visibleFlag: undefined,
    disabledFlag: undefined,
  };
  const queryForm = reactive({ ...queryFormState });
  //展开更多查询参数
  const moreQueryConditionFlag = ref(true);

  // ------------------------ table表格数据和查询方法 ------------------------

  const tableLoading = ref(false);
  const tableData = ref([]);

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    query();
  }

  onMounted(query);

  async function query() {
    try {
      tableLoading.value = true;
      let responseModel = await menuApi.queryMenu();
      // 过滤搜索条件
      const filtedMenuList = filterMenuByQueryForm(responseModel.data, queryForm);
      // 递归构造树形结构，并付给 TableTree组件
      tableData.value = buildMenuTableTree(filtedMenuList);
    } catch (e) {
      sentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // -------------- 多选操作 --------------
  const selectedRowKeys = ref([]);
  let selectedRows = [];
  const hasSelected = computed(() => selectedRowKeys.value.length > 0);

  function onSelectChange(keyArray, selectRows) {
    selectedRowKeys.value = keyArray;
    selectedRows = selectRows;
  }

  function singleDelete(record) {
    confirmBatchDelete([record]);
  }

  function batchDelete() {
    confirmBatchDelete(selectedRows);
  }

  function confirmBatchDelete(menuArray) {
    const menuNameArray = menuArray.map((e) => e.menuName);
    Modal.confirm({
      title: '确定要删除如下菜单吗?',
      icon: createVNode(ExclamationCircleOutlined),
      content: _.join(menuNameArray, '、'),
      okText: '删除',
      okType: 'danger',
      onOk() {
        console.log('OK');
        const menuIdList = menuArray.map((e) => e.menuId);
        requestBatchDelete(menuIdList);
        selectedRows = [];
      },
      cancelText: '取消',
      onCancel() {},
    });

    async function requestBatchDelete(menuIdList) {
      Loading.show();
      try {
        await menuApi.batchDeleteMenu(menuIdList);
        message.success('删除成功!');
        query();
      } catch (e) {
        sentry.captureError(e);
      } finally {
        Loading.hide();
      }
    }
  }

  // -------------- 添加、修改 右侧抽屉 --------------
  const menuOperateModal = ref();
  function showDrawer(rowData) {
    menuOperateModal.value.showDrawer(rowData);
  }

  function showAddSub(rowData) {
    const subData = {
      parentId: rowData.menuId,
      menuType: rowData.menuType === MENU_TYPE_ENUM.CATALOG.value ? MENU_TYPE_ENUM.MENU.value : MENU_TYPE_ENUM.POINTS.value,
      contextMenuId: rowData.menuType === MENU_TYPE_ENUM.MENU.value ? rowData.menuId : undefined,
    };
    menuOperateModal.value.showDrawer(subData);
  }
</script>
