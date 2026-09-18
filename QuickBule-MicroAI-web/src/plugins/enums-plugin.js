/*
 * 枚举插件

 */
import _ from 'lodash';
import { FLAG_NUMBER_ENUM } from '/@/constants/common-const';

export default {
  install: (app, enumWrapper) => {
    const enumPlugin = {};
    /**
     * 根据枚举值获取描述
     * @param {*} constantName 枚举名
     * @param {*} value          枚举值
     * @returns
     */
    enumPlugin.getDescByValue = function (constantName, value) {
      if (!enumWrapper || !Object.prototype.hasOwnProperty.call(enumWrapper, constantName)) {
        console.error('无法找到变量名称：' + constantName + '，请检查 /constants/index.js 文件中是否引入此变量！');
        return '';
      }
      // boolean类型需要做特殊处理
      if (constantName === 'FLAG_NUMBER_ENUM' && !_.isUndefined(value) && typeof value === 'boolean') {
        value = value ? FLAG_NUMBER_ENUM.TRUE.value : FLAG_NUMBER_ENUM.FALSE.value;
      }

      let enumData = enumWrapper[constantName];
      for (let item in enumData) {
        if (enumData[item].value === value) {
          return enumData[item].desc;
        }
      }
      return '';
    };
    /**
     * 根据枚举名获取对应的描述键值对[{value:desc}]
     * @param {*} constantName 枚举名
     * @returns
     */
    enumPlugin.getValueDescList = function (constantName) {
      if (!Object.prototype.hasOwnProperty.call(enumWrapper, constantName)) {
        console.error('无法找到变量名称：' + constantName + '，请检查 /constants/index.js 文件中是否引入此变量！');
        return [];
      }
      const result = [];
      let targetEnum = enumWrapper[constantName];
      for (let item in targetEnum) {
        result.push(targetEnum[item]);
      }
      return result;
    };

    /**
     * 根据枚举名获取对应的value描述键值对{value:desc}
     * @param {*} constantName 枚举名
     * @returns
     */
    enumPlugin.getValueDesc = function (constantName) {
      if (!Object.prototype.hasOwnProperty.call(enumWrapper, constantName)) {
        console.error('无法找到变量名称：' + constantName + '，请检查 /constants/index.js 文件中是否引入此变量！');
        return {};
      }
      let enumData = enumWrapper[constantName];
      let result = {};
      for (let item in enumData) {
        let key = enumData[item].value + '';
        result[key] = enumData[item].desc;
      }
      return result;
    };

    app.config.globalProperties.$enumPlugin = enumPlugin;
    app.provide('enumPlugin', enumPlugin);
  },
};
