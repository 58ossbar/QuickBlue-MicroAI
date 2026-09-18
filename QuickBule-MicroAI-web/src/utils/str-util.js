/*
 * 字符串 相关操作
 *

 */

/**
 * 转为 小写中划线
 */
export function convertLowerHyphen(str) {
  if (!str) {
    return '';
  }

  return str
    .replace(/([A-Z])/g, '-$1')
    .toLowerCase()
    .substring(1);
}
/**
 * 转为驼峰
 */
export function convertUpperCamel(str) {
  if (!str) {
    return '';
  }

  str = str.replace(/_(\w)/g, (_, letter) => letter.toUpperCase());
  // 首字母大写
  return str[0].toUpperCase() + str.substring(1);
}

/**
 * 转为驼峰
 */
export function convertLowerCamel(str) {
  if (!str) {
    return '';
  }

  return str.replace(/_(\w)/g, (_, letter) => letter.toUpperCase());
}
