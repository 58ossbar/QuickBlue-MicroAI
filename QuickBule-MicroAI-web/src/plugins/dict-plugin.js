/*
 * 字典插件


 */
import { useDictStore } from '/@/store/modules/system/dict.js';

export default {
  install: (app) => {

    const dictPlugin = {};

    /**
     * 根据枚举值获取描述
     * @param {*} dictCode   字典编码
     * @param {*} value     值
     * @returns
     */
    dictPlugin.getDataLabels = function (dictCode, value) {
      return useDictStore().getDataLabels(dictCode, value);
    };

    app.config.globalProperties.$dictPlugin = dictPlugin;
    app.provide('dictPlugin', dictPlugin);
  },
};
