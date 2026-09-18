<!--
  * 设置模块
  *
  * @Author:
  * @Date:      2022-09-06 20:18:20
  * @Wechat:
  * @Email:
  * @Copyright
-->

<template>
  <a-modal :title="$t('setting.title')" :open="visible" @cancel="close" :width="1000" :footer="null" :body-style="{ padding: '20px 24px 24px', maxHeight: 'calc(100vh - 120px)', overflowY: 'auto' }">
    <!-- 顶部操作栏 -->
    <div class="setting-actions">
      <span class="actions-hint"><InfoCircleOutlined /> 所有修改自动保存至本地</span>
      <div class="actions-btns">
        <a-button @click="clearCache" size="small">
          <template #icon><DeleteOutlined /></template>
          清除缓存
        </a-button>
        <a-button @click="reset" size="small" danger>
          <template #icon><SyncOutlined /></template>
          恢复默认
        </a-button>
        <a-button type="primary" @click="copy" size="small">
          <template #icon><CopyOutlined /></template>
          复制配置
        </a-button>
      </div>
    </div>

    <div class="setting-container">
      <!-- 行1：基础设置 + 页面元素 -->
      <div class="setting-row">
        <div class="setting-col">
          <div class="setting-card">
            <div class="card-header card-header--blue">
              <span class="card-icon"><BgColorsOutlined /></span>
              <span class="card-title">基础设置</span>
              <span class="card-sub">主题 · 圆角 · 字号</span>
            </div>
            <a-form layout="horizontal" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
              <a-form-item :label="$t('setting.color')">
                <div class="color-container">
                  <template v-for="(item, index) in themeColors" :key="index">
                    <div
                      v-if="index === formState.colorIndex"
                      class="color-item selected"
                      :style="{ backgroundColor: item.primaryColor }"
                    >
                      <CheckOutlined :style="{ color: '#fff', fontSize: '13px' }" />
                    </div>
                    <div
                      v-else
                      @click="changeColor(index)"
                      class="color-item"
                      :style="{ backgroundColor: item.primaryColor }"
                    ></div>
                  </template>
                </div>
              </a-form-item>
              <a-form-item :label="$t('setting.darkmode')">
                <a-switch @change="changeDarkMode" v-model:checked="formState.darkModeFlag" size="small" />
              </a-form-item>
              <a-form-item :label="$t('setting.border.radius')">
                <div class="slider-with-value">
                  <a-slider v-model:value="formState.borderRadius" :min="0" :max="6" @change="changeBorderRadius" />
                  <span class="unit-text">{{ formState.borderRadius }}px</span>
                </div>
              </a-form-item>
              <a-form-item :label="$t('setting.compact')">
                <a-radio-group v-model:value="formState.compactFlag" button-style="solid" @change="changeCompactFlag" size="small">
                  <a-radio-button :value="false">默认</a-radio-button>
                  <a-radio-button :value="true">紧凑</a-radio-button>
                </a-radio-group>
              </a-form-item>
              <a-form-item :label="$t('setting.fontSize')">
                <div class="slider-with-value">
                  <a-slider v-model:value="formState.fontSize" :min="12" :max="20" :step="1" @change="changeFontSize" />
                  <span class="unit-text">{{ formState.fontSize }}px</span>
                </div>
              </a-form-item>
            </a-form>
          </div>
        </div>

        <div class="setting-col">
          <div class="setting-card">
            <div class="card-header card-header--green">
              <span class="card-icon"><AppstoreOutlined /></span>
              <span class="card-title">页面元素</span>
              <span class="card-sub">标签页 · 面包屑 · 页脚</span>
            </div>
            <a-form layout="horizontal" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
              <a-form-item :label="$t('setting.pagetag')">
                <a-switch @change="changePageTagFlag" v-model:checked="formState.pageTagFlag" size="small" />
              </a-form-item>
              <a-form-item :label="$t('setting.pagetag.location')">
                <a-radio-group v-model:value="formState.pageTagLocation" button-style="solid" @change="changePageTagLocation" size="small">
                  <a-radio-button value="top">顶部</a-radio-button>
                  <a-radio-button value="center">中部</a-radio-button>
                </a-radio-group>
              </a-form-item>
              <a-form-item :label="$t('setting.pagetag.style')">
                <a-radio-group v-model:value="formState.pageTagStyle" button-style="solid" @change="changePageTagStyle" size="small">
                  <a-radio-button value="default">默认</a-radio-button>
                  <a-radio-button value="antd">Ant</a-radio-button>
                  <a-radio-button value="chrome">Chrome</a-radio-button>
                </a-radio-group>
              </a-form-item>
              <a-form-item :label="$t('setting.bread')">
                <a-switch
                  @change="changeBreadCrumbFlag"
                  :disabled="formState.pageTagLocation === 'top'"
                  v-model:checked="formState.breadCrumbFlag"
                  size="small"
                />
              </a-form-item>
              <a-form-item :label="$t('setting.footer')">
                <a-switch @change="changeFooterFlag" v-model:checked="formState.footerFlag" size="small" />
              </a-form-item>
            </a-form>
          </div>
        </div>
      </div>

      <!-- 行2：菜单布局（整行） -->
      <div class="setting-card setting-card--wide">
        <div class="card-header card-header--purple">
          <span class="card-icon"><LayoutOutlined /></span>
          <span class="card-title">菜单布局</span>
          <span class="card-sub">选择适合您的导航方式</span>
        </div>
        <div class="layout-selector">
          <div
            v-for="item in layoutList"
            :key="item.value"
            class="layout-card"
            :class="{ selected: formState.layout === item.value }"
            @click="changeLayoutDirect(item.value)"
          >
            <div class="layout-thumb">
              <svg viewBox="0 0 88 64" xmlns="http://www.w3.org/2000/svg">
                <!-- 垂直：左侧窄菜单 -->
                <template v-if="item.value === LAYOUT_ENUM.SIDE.value">
                  <rect x="0" y="0" width="88" height="64" rx="3" fill="#f5f5f5" stroke="#d9d9d9" stroke-width="1"/>
                  <rect x="0" y="0" width="20" height="64" rx="3" fill="#1890ff"/>
                  <rect x="4" y="6" width="12" height="2" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="4" y="11" width="12" height="2" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="4" y="16" width="12" height="2" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="4" y="21" width="12" height="2" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="24" y="6" width="58" height="52" rx="2" fill="#fff" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="28" y="10" width="20" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="52" y="10" width="26" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="28" y="18" width="50" height="20" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="28" y="42" width="24" height="14" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="56" y="42" width="22" height="14" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                </template>
                <!-- 双列菜单：左侧宽菜单 -->
                <template v-if="item.value === LAYOUT_ENUM.SIDE_EXPAND.value">
                  <rect x="0" y="0" width="88" height="64" rx="3" fill="#f5f5f5" stroke="#d9d9d9" stroke-width="1"/>
                  <rect x="0" y="0" width="30" height="64" rx="3" fill="#1890ff"/>
                  <rect x="4" y="6" width="22" height="3" rx="1" fill="rgba(255,255,255,0.6)"/>
                  <rect x="4" y="12" width="22" height="2" rx="1" fill="rgba(255,255,255,0.4)"/>
                  <rect x="4" y="17" width="22" height="2" rx="1" fill="rgba(255,255,255,0.4)"/>
                  <rect x="4" y="22" width="22" height="2" rx="1" fill="rgba(255,255,255,0.4)"/>
                  <rect x="4" y="27" width="22" height="2" rx="1" fill="rgba(255,255,255,0.4)"/>
                  <rect x="34" y="6" width="50" height="52" rx="2" fill="#fff" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="38" y="10" width="18" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="60" y="10" width="20" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="38" y="18" width="42" height="20" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="38" y="42" width="20" height="14" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="62" y="42" width="18" height="14" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                </template>
                <!-- 水平：顶部菜单栏 -->
                <template v-if="item.value === LAYOUT_ENUM.TOP.value">
                  <rect x="0" y="0" width="88" height="64" rx="3" fill="#f5f5f5" stroke="#d9d9d9" stroke-width="1"/>
                  <rect x="0" y="0" width="88" height="14" rx="3" fill="#1890ff"/>
                  <rect x="6" y="4" width="8" height="6" rx="1" fill="rgba(255,255,255,0.3)"/>
                  <rect x="18" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="34" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="50" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="66" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="4" y="18" width="80" height="42" rx="2" fill="#fff" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="8" y="22" width="24" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="36" y="22" width="24" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="64" y="22" width="16" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="8" y="30" width="36" height="26" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="48" y="30" width="32" height="26" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                </template>
                <!-- 侧边导航：极窄图标菜单 -->
                <template v-if="item.value === LAYOUT_ENUM.SIDE_ICON.value">
                  <rect x="0" y="0" width="88" height="64" rx="3" fill="#f5f5f5" stroke="#d9d9d9" stroke-width="1"/>
                  <rect x="0" y="0" width="12" height="64" rx="3" fill="#1890ff"/>
                  <circle cx="6" cy="8" r="2.5" fill="rgba(255,255,255,0.6)"/>
                  <circle cx="6" cy="16" r="2.5" fill="rgba(255,255,255,0.4)"/>
                  <circle cx="6" cy="24" r="2.5" fill="rgba(255,255,255,0.4)"/>
                  <circle cx="6" cy="32" r="2.5" fill="rgba(255,255,255,0.4)"/>
                  <circle cx="6" cy="40" r="2.5" fill="rgba(255,255,255,0.4)"/>
                  <rect x="16" y="6" width="68" height="52" rx="2" fill="#fff" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="20" y="10" width="22" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="46" y="10" width="34" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="20" y="18" width="60" height="20" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="20" y="42" width="28" height="14" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="52" y="42" width="28" height="14" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                </template>
                <!-- 混合垂直：顶部菜单 + 左侧窄菜单 -->
                <template v-if="item.value === LAYOUT_ENUM.TOP_SIDE.value">
                  <rect x="0" y="0" width="88" height="64" rx="3" fill="#f5f5f5" stroke="#d9d9d9" stroke-width="1"/>
                  <rect x="0" y="0" width="88" height="14" rx="3" fill="#1890ff"/>
                  <rect x="6" y="4" width="8" height="6" rx="1" fill="rgba(255,255,255,0.3)"/>
                  <rect x="18" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="34" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="50" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="0" y="14" width="16" height="50" fill="#1890ff"/>
                  <rect x="3" y="18" width="10" height="2" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="3" y="23" width="10" height="2" rx="1" fill="rgba(255,255,255,0.4)"/>
                  <rect x="3" y="28" width="10" height="2" rx="1" fill="rgba(255,255,255,0.4)"/>
                  <rect x="3" y="33" width="10" height="2" rx="1" fill="rgba(255,255,255,0.4)"/>
                  <rect x="18" y="18" width="66" height="42" rx="2" fill="#fff" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="22" y="22" width="20" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="46" y="22" width="34" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="22" y="30" width="58" height="26" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                </template>
                <!-- 混合双列：顶部菜单 + 左侧宽菜单 -->
                <template v-if="item.value === LAYOUT_ENUM.TOP_EXPAND.value">
                  <rect x="0" y="0" width="88" height="64" rx="3" fill="#f5f5f5" stroke="#d9d9d9" stroke-width="1"/>
                  <rect x="0" y="0" width="88" height="14" rx="3" fill="#1890ff"/>
                  <rect x="6" y="4" width="8" height="6" rx="1" fill="rgba(255,255,255,0.3)"/>
                  <rect x="18" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="34" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="50" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="66" y="5" width="12" height="4" rx="1" fill="rgba(255,255,255,0.5)"/>
                  <rect x="0" y="14" width="20" height="50" fill="#e6f7ff" stroke="#d9d9d9" stroke-width="0.5"/>
                  <rect x="4" y="18" width="12" height="2" rx="1" fill="#1890ff" opacity="0.4"/>
                  <rect x="4" y="23" width="12" height="2" rx="1" fill="#1890ff" opacity="0.4"/>
                  <rect x="4" y="28" width="12" height="2" rx="1" fill="#1890ff" opacity="0.4"/>
                  <rect x="4" y="33" width="12" height="2" rx="1" fill="#1890ff" opacity="0.4"/>
                  <rect x="22" y="18" width="62" height="42" rx="2" fill="#fff" stroke="#e8e8e8" stroke-width="0.5"/>
                  <rect x="26" y="22" width="20" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="50" y="22" width="30" height="4" rx="1" fill="#f0f0f0"/>
                  <rect x="26" y="30" width="54" height="26" rx="1" fill="#f5f5f5" stroke="#e8e8e8" stroke-width="0.5"/>
                </template>
              </svg>
            </div>
            <div class="layout-name">{{ item.desc }}</div>
            <div v-if="formState.layout === item.value" class="layout-check">
              <CheckOutlined />
            </div>
          </div>
        </div>

        <div class="layout-divider"></div>

        <div class="layout-options">
          <div class="layout-option">
            <span class="option-label">菜单主题</span>
            <a-radio-group v-model:value="formState.sideMenuTheme" button-style="solid" @change="changeMenuTheme" size="small">
              <a-radio-button value="dark">深色</a-radio-button>
              <a-radio-button value="light">浅色</a-radio-button>
              <a-radio-button value="modern">高端</a-radio-button>
            </a-radio-group>
          </div>
          <div class="layout-option" v-if="formState.layout === LAYOUT_ENUM.SIDE.value">
            <span class="option-label">菜单宽度</span>
            <div class="option-control">
              <a-input-number @change="changeSideMenuWidth" v-model:value="formState.sideMenuWidth" :min="1" :max="300" size="small" />
              <span class="unit-text">px</span>
            </div>
          </div>
          <div class="layout-option" v-if="formState.layout === LAYOUT_ENUM.TOP.value">
            <span class="option-label">页面宽度</span>
            <div class="option-control">
              <a-input @change="changePageWidth" v-model:value="formState.pageWidth" size="small" style="width: 130px" placeholder="如: 1200 或 80%" />
            </div>
          </div>
          <div class="layout-option" v-if="formState.layout === LAYOUT_ENUM.SIDE.value">
            <span class="option-label">扁平模式</span>
            <a-switch @change="changeFlatPattern" v-model:checked="formState.flatPattern" size="small" />
          </div>
          <div class="layout-option" v-if="formState.layout === LAYOUT_ENUM.SIDE.value">
            <span class="option-label">折叠后显示</span>
            <a-radio-group v-model:value="formState.menuCollapsedShow" button-style="solid" @change="changeMenuCollapsedShow" size="small">
              <a-radio-button value="icon">仅图标</a-radio-button>
              <a-radio-button value="text">仅文字</a-radio-button>
              <a-radio-button value="both">图标+文字</a-radio-button>
            </a-radio-group>
          </div>
          <div class="layout-option">
            <span class="option-label">菜单默认展开</span>
            <a-switch @change="changeMenuDefaultExpanded" v-model:checked="formState.menuDefaultExpanded" size="small" />
          </div>
        </div>
      </div>

      <!-- 行3：其他功能 -->
      <div class="setting-card">
        <div class="card-header card-header--orange">
          <span class="card-icon"><ToolOutlined /></span>
          <span class="card-title">其他功能</span>
          <span class="card-sub">水印 · 帮助文档</span>
        </div>
        <div class="other-grid">
          <div class="other-item">
            <div class="other-head">
              <span class="other-title">水印</span>
              <a-switch @change="changeWatermarkFlag" v-model:checked="formState.watermarkFlag" size="small" />
            </div>
            <transition name="fade">
              <div class="other-body" v-if="formState.watermarkFlag">
                <a-input v-model:value="formState.watermarkText" @change="changeWatermarkText" placeholder="请输入水印文字" size="small" :maxLength="50" allow-clear />
              </div>
            </transition>
          </div>
          <div class="other-item">
            <div class="other-head">
              <span class="other-title">帮助文档</span>
              <a-switch @change="changeHelpDocFlag" v-model:checked="formState.helpDocFlag" size="small" />
            </div>
            <transition name="fade">
              <div class="other-body" v-if="formState.helpDocFlag">
                <div class="other-sub-row">
                  <span class="other-sub-label">默认展开</span>
                  <a-switch
                    @change="changeHelpDocExpandFlag"
                    v-model:checked="formState.helpDocExpandFlag"
                    size="small"
                  />
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>
<script setup>
  import { ref, reactive, h, watch } from 'vue';
  import {
    CheckOutlined,
    SyncOutlined,
    CopyOutlined,
    DeleteOutlined,
    InfoCircleOutlined,
    BgColorsOutlined,
    LayoutOutlined,
    AppstoreOutlined,
    ToolOutlined,
  } from '@ant-design/icons-vue';
  import localStorageKeyConst from '/@/constants/local-storage-key-const';
  import { LAYOUT_ENUM } from '/@/constants/layout-const';
  import { localSave } from '/@/utils/local-util';
  import { useAppConfigStore } from '/@/store/modules/system/app-config';
  import { Modal } from 'ant-design-vue';
  import { appDefaultConfig } from '/@/config/app-config';
  import { themeColors } from '/@/theme/color.js';

  const layoutList = [
    { value: LAYOUT_ENUM.SIDE.value, desc: LAYOUT_ENUM.SIDE.desc },
    { value: LAYOUT_ENUM.SIDE_EXPAND.value, desc: LAYOUT_ENUM.SIDE_EXPAND.desc },
    { value: LAYOUT_ENUM.TOP.value, desc: LAYOUT_ENUM.TOP.desc },
    { value: LAYOUT_ENUM.SIDE_ICON.value, desc: LAYOUT_ENUM.SIDE_ICON.desc },
    { value: LAYOUT_ENUM.TOP_SIDE.value, desc: LAYOUT_ENUM.TOP_SIDE.desc },
    { value: LAYOUT_ENUM.TOP_EXPAND.value, desc: LAYOUT_ENUM.TOP_EXPAND.desc },
  ];

  function changeLayoutDirect(value) {
    formState.layout = value;
    appConfigStore.$patch({ layout: value });
  }

  // ----------------- modal 显示与隐藏 -----------------

  const visible = ref(false);
  defineExpose({
    show,
  });

  function close() {
    visible.value = false;
  }

  function show() {
    visible.value = true;
  }

  // ----------------- 配置信息操作 -----------------
  function copy() {
    let content = JSON.stringify(formState, null, 2);
    // 创建元素用于复制
    const aux = document.createElement('input');
    // 设置元素内容
    aux.setAttribute('value', content);
    // 将元素插入页面进行调用
    document.body.appendChild(aux);
    // 复制内容
    aux.select();
    // 将内容复制到剪贴板
    document.execCommand('copy');
    // 删除创建元素
    document.body.removeChild(aux);

    Modal.success({
      title: '复制成功',
      content: h('div', {}, [h('p', '可以直接修改 /@/config/app-config.js 文件保存此配置')]),
    });
  }

  function reset() {
    for (const k in appDefaultConfig) {
      formState[k] = appDefaultConfig[k];
    }
    appConfigStore.reset();
  }

  function clearCache() {
    Modal.confirm({
      title: '确认清除缓存',
      content: '将清除所有本地缓存数据（包括配置、标签页、通知等），但不会退出登录。是否继续？',
      okText: '确认清除',
      cancelText: '取消',
      okType: 'danger',
      onOk() {
        // 清除应用配置
        localStorage.removeItem(localStorageKeyConst.APP_CONFIG);
        // 清除用户标签导航
        localStorage.removeItem(localStorageKeyConst.USER_TAG_NAV);
        // 清除首页快捷入口
        localStorage.removeItem(localStorageKeyConst.HOME_QUICK_ENTRY);
        // 清除通知信息已读
        localStorage.removeItem(localStorageKeyConst.NOTICE_READ);
        // 清除待办事项
        localStorage.removeItem(localStorageKeyConst.TO_BE_DONE);

        // 重置应用配置到默认值
        for (const k in appDefaultConfig) {
          formState[k] = appDefaultConfig[k];
        }
        appConfigStore.reset();

        Modal.success({
          title: '清除成功',
          content: '缓存已清除，页面将自动刷新',
          onOk() {
            window.location.reload();
          }
        });
      }
    });
  }

  // ----------------- 表单数据实时保存到localstorage -----------------

  const appConfigStore = useAppConfigStore();
  useAppConfigStore().$subscribe((mutation, state) => {
    localSave(localStorageKeyConst.APP_CONFIG, JSON.stringify(state));
  });

  // ----------------- 表单 -----------------

  let formValue = {
    // 布局: side 或者 side-expand
    layout: appConfigStore.layout,
    // 页面宽度
    pageWidth: appConfigStore.pageWidth,
    // 颜色
    colorIndex: appConfigStore.colorIndex,
    // 侧边菜单宽度
    sideMenuWidth: appConfigStore.sideMenuWidth,
    // 菜单主题
    sideMenuTheme: appConfigStore.sideMenuTheme,
    // 页面紧凑
    compactFlag: appConfigStore.compactFlag,
    // 夜间模式
    darkModeFlag: appConfigStore.darkModeFlag,
    // 页面圆角
    borderRadius: appConfigStore.borderRadius,
    // 标签页
    pageTagFlag: appConfigStore.pageTagFlag,
    // 标签页
    flatPattern: appConfigStore.flatPattern,
    // 标签页 样式
    pageTagStyle: appConfigStore.pageTagStyle,
    // 面包屑
    breadCrumbFlag: appConfigStore.breadCrumbFlag,
    // 页脚
    footerFlag: appConfigStore.footerFlag,
    // 帮助文档
    helpDocFlag: appConfigStore.helpDocFlag,
    // 帮助文档 默认展开
    helpDocExpandFlag: appConfigStore.helpDocExpandFlag,
    // 水印
    watermarkFlag: appConfigStore.watermarkFlag,
    // 水印文字
    watermarkText: appConfigStore.watermarkText,
    //标签页位置
    pageTagLocation: appConfigStore.pageTagLocation,
    //字体大小
    fontSize: appConfigStore.fontSize,
    //菜单折叠后显示内容
    menuCollapsedShow: appConfigStore.menuCollapsedShow,
    //菜单默认展开
    menuDefaultExpanded: appConfigStore.menuDefaultExpanded,
  };

  let formState = reactive({ ...formValue });

  watch(
    () => formState.pageTagLocation,
    () => {
      if (formState.pageTagLocation === 'top') {
        formState.breadCrumbFlag = false;
      } else {
        formState.breadCrumbFlag = true;
      }
    },
    {
      immediate: true,
    }
  );

  function changePageTagLocation(e) {
    appConfigStore.$patch({
      pageTagLocation: e.target.value,
    });
  }

  function changeLayout(e) {
    appConfigStore.$patch({
      layout: e.target.value,
    });
  }

  function changeColor(index) {
    formState.colorIndex = index;
    appConfigStore.$patch({
      colorIndex: index,
    });
  }

  function changeDarkMode(e) {
    appConfigStore.$patch({
      darkModeFlag: e,
    });
  }

  function changeSideMenuWidth(value) {
    appConfigStore.$patch({
      sideMenuWidth: value,
    });
  }

  function changePageWidth(e) {
    appConfigStore.$patch({
      pageWidth: e.target.value,
    });
  }

  function changeMenuTheme(e) {
    appConfigStore.$patch({
      sideMenuTheme: e.target.value,
    });
  }

  function changeCompactFlag(e) {
    appConfigStore.$patch({
      compactFlag: e.target.value,
    });
  }
  function changeBorderRadius(e) {
    appConfigStore.$patch({
      borderRadius: e,
    });
  }

  function changeBreadCrumbFlag(e) {
    appConfigStore.$patch({
      breadCrumbFlag: e,
    });
  }

  function changePageTagFlag(e) {
    appConfigStore.$patch({
      pageTagFlag: e,
    });
  }
  function changeFlatPattern(e) {
    appConfigStore.$patch({
      flatPattern: e,
    });
  }

  function changePageTagStyle(e) {
    appConfigStore.$patch({
      pageTagStyle: e.target.value,
    });
  }

  function changeFooterFlag(e) {
    appConfigStore.$patch({
      footerFlag: e,
    });
  }

  function changeHelpDocFlag(e) {
    appConfigStore.$patch({
      helpDocFlag: e,
    });
  }

  function changeHelpDocExpandFlag(e) {
    appConfigStore.$patch({
      helpDocExpandFlag: e,
    });
  }

  function changeWatermarkFlag(e) {
    appConfigStore.$patch({
      watermarkFlag: e,
    });
  }

  function changeWatermarkText(e) {
    appConfigStore.$patch({
      watermarkText: e.target.value,
    });
  }

  function changeFontSize(value) {
    appConfigStore.$patch({
      fontSize: value,
    });
  }

  function changeMenuCollapsedShow(e) {
    appConfigStore.$patch({
      menuCollapsedShow: e.target.value,
    });
  }

  function changeMenuDefaultExpanded(e) {
    appConfigStore.$patch({
      menuDefaultExpanded: e,
    });
  }
</script>
<style lang="less" scoped>
  .setting-container {
    padding: 0;
  }

  /* ---------- 顶部操作栏 ---------- */
  .setting-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: 14px;
    margin-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;

    .actions-hint {
      font-size: 12px;
      color: #8c8c8c;
      display: flex;
      align-items: center;
      gap: 6px;

      .anticon {
        color: #1890ff;
      }
    }

    .actions-btns {
      display: flex;
      gap: 8px;
    }
  }

  /* ---------- 分区行 ---------- */
  .setting-row {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .setting-col {
    flex: 1;
    min-width: 0;
  }

  /* ---------- 设置卡片 ---------- */
  .setting-card {
    background: linear-gradient(180deg, #ffffff 0%, #fafafa 100%);
    border-radius: 10px;
    padding: 16px 16px 12px;
    height: 100%;
    border: 1px solid #f0f0f0;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);
    transition: all 0.3s;

    &:hover {
      border-color: #e0e0e0;
      box-shadow: 0 3px 10px rgba(0, 0, 0, 0.06);
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid #f0f0f0;
  }

  .card-icon {
    width: 30px;
    height: 30px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    flex-shrink: 0;
  }

  .card-header--blue .card-icon {
    color: #1890ff;
    background: linear-gradient(135deg, rgba(24, 144, 255, 0.14), rgba(24, 144, 255, 0.05));
  }

  .card-header--green .card-icon {
    color: #52c41a;
    background: linear-gradient(135deg, rgba(82, 196, 26, 0.14), rgba(82, 196, 26, 0.05));
  }

  .card-header--purple .card-icon {
    color: #722ed1;
    background: linear-gradient(135deg, rgba(114, 46, 209, 0.14), rgba(114, 46, 209, 0.05));
  }

  .card-header--orange .card-icon {
    color: #fa8c16;
    background: linear-gradient(135deg, rgba(250, 140, 22, 0.14), rgba(250, 140, 22, 0.05));
  }

  .card-title {
    font-size: 14px;
    font-weight: 600;
    color: #262626;
  }

  .card-sub {
    font-size: 12px;
    color: #a0a0a0;
    margin-left: auto;
  }

  :deep(.ant-form-item) {
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(.ant-form-item-label > label) {
    font-size: 13px;
    color: #595959;
    height: 28px;
  }

  :deep(.ant-form-item-control-input) {
    min-height: 28px;
  }

  :deep(.ant-radio-button-wrapper) {
    font-size: 12px;
    height: 24px;
    line-height: 22px;
    padding: 0 10px;
    border-radius: 4px;

    &:first-child {
      border-start-start-radius: 4px;
    }

    &:last-child {
      border-start-end-radius: 4px;
      border-end-end-radius: 4px;
    }
  }

  :deep(.ant-slider) {
    margin-top: 6px;
    margin-bottom: 6px;
  }

  :deep(.ant-switch) {
    min-height: 22px;
  }

  :deep(.ant-slider-rail),
  :deep(.ant-slider-track) {
    height: 4px;
  }

  :deep(.ant-slider-handle) {
    width: 14px;
    height: 14px;
    margin-top: -5px;
  }

  /* ---------- 主题颜色 ---------- */
  .color-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-start;
  }

  .color-item {
    width: 26px;
    height: 26px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.25s;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1), inset 0 0 0 1px rgba(255, 255, 255, 0.25);

    &:hover {
      transform: scale(1.12);
      box-shadow: 0 3px 8px rgba(0, 0, 0, 0.18);
    }

    &.selected {
      box-shadow: 0 0 0 2px #fff, 0 0 0 4px #1890ff, 0 3px 10px rgba(24, 144, 255, 0.35);
      transform: scale(1.1);
    }
  }

  /* ---------- 滑块 + 数值 ---------- */
  .slider-with-value {
    display: flex;
    align-items: center;
    gap: 10px;

    :deep(.ant-slider) {
      flex: 1;
      min-width: 60px;
    }
  }

  .unit-text {
    color: #8c8c8c;
    font-size: 12px;
    white-space: nowrap;
  }

  .option-control {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  /* ---------- 布局卡片选择器 ---------- */
  .layout-selector {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    gap: 12px;
  }

  .layout-card {
    position: relative;
    width: 110px;
    cursor: pointer;
    text-align: center;
    transition: transform 0.25s;

    .layout-thumb {
      width: 110px;
      height: 72px;
      border: 2px solid #e8e8e8;
      border-radius: 8px;
      overflow: hidden;
      background: #fafafa;
      transition: all 0.25s;

      svg {
        display: block;
        width: 100%;
        height: 100%;
      }
    }

    .layout-name {
      margin-top: 6px;
      font-size: 12px;
      color: #595959;
      line-height: 1.4;
      transition: color 0.2s;
    }

    .layout-check {
      position: absolute;
      top: -7px;
      right: -7px;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: #1890ff;
      color: #fff;
      font-size: 11px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 6px rgba(24, 144, 255, 0.45);
      opacity: 0;
      transform: scale(0.4);
      transition: all 0.2s;
      z-index: 1;
    }

    &:hover {
      transform: translateY(-2px);

      .layout-thumb {
        border-color: #1890ff;
        box-shadow: 0 4px 12px rgba(24, 144, 255, 0.18);
      }
    }

    &.selected {
      .layout-thumb {
        border-color: #1890ff;
        box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.15), 0 4px 12px rgba(24, 144, 255, 0.2);
      }

      .layout-name {
        color: #1890ff;
        font-weight: 500;
      }

      .layout-check {
        opacity: 1;
        transform: scale(1);
      }
    }
  }

  .layout-divider {
    height: 1px;
    background: #f0f0f0;
    margin: 16px 0 14px;
  }

  /* ---------- 布局选项 ---------- */
  .layout-options {
    display: flex;
    flex-wrap: wrap;
    gap: 14px 28px;

    .layout-option {
      display: flex;
      flex-direction: column;
      gap: 8px;
      min-width: 170px;

      .option-label {
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }

  /* ---------- 其他功能 ---------- */
  .other-grid {
    display: flex;
    gap: 16px;

    .other-item {
      flex: 1;
      min-width: 0;
      background: #fafafa;
      border: 1px solid #f0f0f0;
      border-radius: 8px;
      padding: 12px 14px;
      transition: border-color 0.2s;

      &:hover {
        border-color: #d9d9d9;
      }

      .other-head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 8px;

        .other-title {
          font-size: 13px;
          font-weight: 500;
          color: #262626;
        }
      }

      .other-body {
        margin-top: 10px;
        padding-top: 10px;
        border-top: 1px dashed #e8e8e8;

        .other-sub-row {
          display: flex;
          align-items: center;
          justify-content: space-between;

          .other-sub-label {
            font-size: 12px;
            color: #595959;
          }
        }
      }
    }
  }

  /* ---------- 过渡动画 ---------- */
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.2s ease;
  }

  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }

  /* ---------- 暗色模式适配 ---------- */
  :global([data-theme='dark']) {
    .setting-actions {
      border-bottom-color: #303030;

      .actions-hint {
        color: rgba(255, 255, 255, 0.45);
      }
    }

    .setting-card {
      background: linear-gradient(180deg, #1f1f1f 0%, #1a1a1a 100%);
      border-color: #303030;

      &:hover {
        border-color: #434343;
      }
    }

    .card-header {
      border-bottom-color: #303030;
    }

    .card-title {
      color: rgba(255, 255, 255, 0.85);
    }

    .card-sub {
      color: rgba(255, 255, 255, 0.35);
    }

    :deep(.ant-form-item-label > label) {
      color: rgba(255, 255, 255, 0.65);
    }

    .unit-text {
      color: rgba(255, 255, 255, 0.45);
    }

    .layout-divider {
      background: #303030;
    }

    .layout-options {
      .layout-option {
        .option-label {
          color: rgba(255, 255, 255, 0.45);
        }
      }
    }

    .other-grid {
      .other-item {
        background: #1f1f1f;
        border-color: #303030;

        &:hover {
          border-color: #434343;
        }

        .other-head .other-title {
          color: rgba(255, 255, 255, 0.85);
        }

        .other-body {
          border-top-color: #303030;

          .other-sub-label {
            color: rgba(255, 255, 255, 0.65);
          }
        }
      }
    }

    /* 布局缩略图 SVG 配色 */
    .layout-card {
      .layout-thumb {
        background: #1a1a1a;
        border-color: #303030;

        :deep(svg) {
          rect[fill='#f5f5f5'] {
            fill: #1f1f1f;
          }

          rect[fill='#fff'] {
            fill: #262626;
          }

          rect[fill='#f0f0f0'] {
            fill: #2f2f2f;
          }

          rect[stroke='#e8e8e8'] {
            stroke: #3a3a3a;
          }

          rect[stroke='#d9d9d9'] {
            stroke: #434343;
          }
        }
      }

      .layout-name {
        color: rgba(255, 255, 255, 0.65);
      }

      &:hover .layout-thumb {
        border-color: #1890ff;
      }

      &.selected .layout-name {
        color: #1890ff;
      }
    }
  }
</style>
