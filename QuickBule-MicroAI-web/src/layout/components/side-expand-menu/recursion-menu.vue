<!--
  * 递归菜单
  * 
  * @Author:
  * @Date:      2022-09-06 20:29:12 
  * @Wechat:
  * @Email:
  * @Copyright
-->
<template>
  <div class="recursion-container" v-show="topMenu.children && topMenu.children.length > 0">
    <!-- 顶部顶级菜单名称 -->
    <div class="top-menu">
      <span class="ant-menu">{{ topMenu.menuName }}</span>
    </div>
    <!-- 次级菜单展示 -->
    <div class="bottom-menu">
      <a-menu  :selectedKeys="selectedKeys" :openKeys="openKeys" mode="inline">
        <template v-for="item in topMenu.children" :key="item.menuId.toString()">
          <template v-if="item.visibleFlag">
            <template v-if="$lodash.isEmpty(item.children)">
              <a-menu-item :key="item.menuId.toString()" @click="turnToPage(item)">
                <template #icon v-if="item.icon">
                  <component :is="$antIcons[item.icon]" />
                </template>
                {{ item.menuName }}
              </a-menu-item>
            </template>
            <template v-else>
              <SubMenu :menu-info="item" :key="item.menuId.toString()" @turnToPage="turnToPage" />
            </template>
          </template>
        </template>
      </a-menu>
    </div>
  </div>
</template>
<script setup>
  import { ref } from 'vue';
  import { HOME_PAGE_NAME } from '/@/constants/system/home-const';
  import SubMenu from './sub-menu.vue';
  import { router } from '/@/router';
  import _ from 'lodash';
  import menuEmitter from './side-expand-menu-mitt';
  import { useUserStore } from '/@/store/modules/system/user';

  // 选中的顶级菜单
  let topMenu = ref({});
  menuEmitter.on('selectTopMenu', onSelectTopMenu);

  // 监听选中顶级菜单事件
  function onSelectTopMenu(selectedTopMenu) {
    topMenu.value = selectedTopMenu;
    if (selectedTopMenu.children && selectedTopMenu.children.length > 0) {
      openKeys.value = _.map(selectedTopMenu.children, 'menuId').map((e) => e.toString());
    } else {
      openKeys.value = [];
    }
    selectedKeys.value = [];
  }

  //展开的菜单
  const selectedKeys = ref([]);
  const openKeys = ref([]);

  function updateSelectKeyAndOpenKey(parentList, currentSelectKey) {
    if (!parentList) {
      return;
    }
    //获取需要展开的menu key集合
    openKeys.value = _.map(parentList, 'name');
    selectedKeys.value = [currentSelectKey];
  }

  // 页面跳转
  function turnToPage(route) {
    useUserStore().deleteKeepAliveIncludes(route.menuId.toString());
    router.push({ name: route.menuId.toString() });
  }

  function goHome() {
    router.push({ name: HOME_PAGE_NAME });
  }

  defineExpose({ updateSelectKeyAndOpenKey });
</script>
<style scoped lang="less">
  .recursion-container {
    height: 100vh;
    background: #ffffff;
    box-shadow: 1px 0 8px rgba(0, 0, 0, 0.04);
  }

  .top-menu {
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: var(--header-height, 40px);
    font-size: 14px;
    font-weight: 600;
    color: #1a1a2e;
    border-bottom: 1px solid #f0f0f0;
    border-right: 1px solid #f0f0f0;
    background: #ffffff;
    letter-spacing: 0.3px;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 16px;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 14px;
      background-color: #1890ff;
      border-radius: 0 2px 2px 0;
    }

    .ant-menu {
      font-size: 14px;
      font-weight: 600;
      color: #1a1a2e;
    }
  }

  .bottom-menu {
    overflow: auto;
    display: flex;
    height: calc(100% - var(--header-height, 40px));
    color: #515a6e;

    scrollbar-width: thin;
    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.1);
      border-radius: 2px;
    }

    :deep(.ant-menu) {
      width: 100%;
      border-right: none;
      background: transparent;
      padding: 6px 0;

      .ant-menu-item {
        height: 36px;
        line-height: 36px;
        margin: 1px 8px !important;
        border-radius: 6px;
        padding: 0 12px !important;
        font-size: 13px;
        font-weight: 400;
        transition: background-color 0.15s ease, color 0.15s ease;
        color: #515a6e;

        &:hover {
          color: #1890ff;
          background-color: rgba(24, 144, 255, 0.06);

          .anticon {
            color: #1890ff;
          }
        }

        &.ant-menu-item-selected {
          color: #1890ff;
          font-weight: 500;
          background-color: rgba(24, 144, 255, 0.10);

          &::before {
            content: '';
            position: absolute;
            left: -8px;
            top: 50%;
            transform: translateY(-50%);
            width: 3px;
            height: 16px;
            background-color: #1890ff;
            border-radius: 0 2px 2px 0;
          }

          &::after {
            display: none;
          }

          .anticon {
            color: #1890ff;
          }
        }

        .anticon {
          font-size: 14px;
          color: #8c8c8c;
          margin-right: 10px;
          transition: color 0.15s ease;
        }
      }

      .ant-menu-submenu {
        .ant-menu-submenu-title {
          height: 36px;
          line-height: 36px;
          margin: 1px 8px !important;
          border-radius: 6px;
          padding: 0 12px !important;
          font-size: 13px;
          font-weight: 400;
          transition: background-color 0.15s ease, color 0.15s ease;
          color: #515a6e;

          &:hover {
            color: #1890ff;
            background-color: rgba(24, 144, 255, 0.06);

            .anticon {
              color: #1890ff;
            }
          }

          &.ant-menu-submenu-open {
            color: #1890ff;
            font-weight: 500;
            background-color: rgba(24, 144, 255, 0.10);

            &::before {
              content: '';
              position: absolute;
              left: -8px;
              top: 50%;
              transform: translateY(-50%);
              width: 3px;
              height: 16px;
              background-color: #1890ff;
              border-radius: 0 2px 2px 0;
            }

            .ant-menu-submenu-arrow {
              color: #1890ff;
            }
          }

          .anticon {
            font-size: 14px;
            color: #8c8c8c;
            margin-right: 10px;
            transition: color 0.15s ease;
          }
        }

        .ant-menu-submenu-arrow {
          transition: color 0.15s ease !important;
          color: #8c8c8c;
        }
      }

      .ant-menu-sub {
        background: transparent !important;
      }

      .ant-menu-sub .ant-menu-item {
        height: 34px;
        line-height: 34px;
        padding-left: 44px !important;
        margin: 1px 12px !important;
        border-radius: 5px;
        font-size: 12.5px;

        &.ant-menu-item-selected {
          &::before {
            left: 28px !important;
            top: 50%;
            transform: translateY(-50%);
            width: 2px;
            height: 14px;
          }
        }
      }

      .ant-menu-sub .ant-menu-sub .ant-menu-item {
        padding-left: 56px !important;
        margin: 1px 16px !important;
        font-size: 12px;
      }
    }
  }
</style>
