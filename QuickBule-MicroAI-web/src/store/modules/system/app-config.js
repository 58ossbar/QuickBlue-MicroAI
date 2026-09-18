/*
 * 项目的配置信息
 *

 */
import { defineStore } from 'pinia';
import { appDefaultConfig } from '/@/config/app-config';
import localStorageKeyConst from '/@/constants/local-storage-key-const';
import { sentry } from '/@/lib/sentry';
import { localRead } from '/@/utils/local-util';

let state = {
  ...appDefaultConfig
};

let appConfigStr = localRead(localStorageKeyConst.APP_CONFIG);
let language = appDefaultConfig.language;
if (appConfigStr) {
  try {
    // 合并：localStorage 的值覆盖同名字段，新增字段保留默认值
    state = { ...state, ...JSON.parse(appConfigStr) };
    language = state.language;
  } catch (e) {
    sentry.captureError(e);
  }
}

/**
 * 获取初始化的语言
 */
export const getInitializedLanguage = function () {
  return language;
};

export const useAppConfigStore = defineStore({
  id: 'appConfig',
  state: () => ({
    // 读取config下的默认配置
    ...state,
    // 全屏
    fullScreenFlag: false,
  }),
  actions: {
    reset() {
      for (const k in appDefaultConfig) {
        this[k] = appDefaultConfig[k];
      }
    },
    showHelpDoc() {
      this.helpDocExpandFlag = true;
    },
    hideHelpDoc() {
      this.helpDocExpandFlag = false;
    },
    startFullScreen() {
      this.fullScreenFlag = true;
    },
    exitFullScreen() {
      this.fullScreenFlag = false;
    },
  },
});
