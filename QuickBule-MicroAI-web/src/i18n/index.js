/*
 * 国际化入口文件
 *

 */

import en_US from './lang/en-US/index';
import zh_CN from './lang/zh-CN/index';
import { createI18n } from 'vue-i18n';
import { getInitializedLanguage } from '/@/store/modules/system/app-config';

// 语言选择数组
export const i18nList = [
  {
    text: '简体中文',
    value: 'zh_CN',
  },
  {
    text: 'English',
    value: 'en_US',
  },
];

export const messages = {
  zh_CN: zh_CN,
  en_US: en_US,
};

const i18n = createI18n({
  fallbackLocale: 'zh_CN', //预设语言环境
  globalInjection: true,
  legacy: false, //
  locale: getInitializedLanguage(), //默认初始化的语言
  messages, //
});

export default i18n;
