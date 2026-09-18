/*
 * 通用常量
 *

 */

export const PAGE_SIZE = 10;

export const PAGE_SIZE_OPTIONS = ['5', '10', '15', '20', '30', '40', '50', '75', '100', '150', '200', '300', '500'];

//登录页面名字
export const PAGE_PATH_LOGIN = '/login';
//首页页面名字
export const HOME_PAGE_PATH  = '/home';

//404页面名字
export const PAGE_PATH_404 = '/404';

//帮助文档页面名字（免登录）
export const PAGE_PATH_HELP_DOC = '/help-doc';
//安装向导页面名字（免登录）
export const PAGE_PATH_INSTALL = '/install';

export const showTableTotal = function (total) {
  return `共${total}条`;
};

export const FLAG_NUMBER_ENUM = {
  TRUE: {
    value: 1,
    desc: '是',
  },
  FALSE: {
    value: 0,
    desc: '否',
  },
};

export const GENDER_ENUM = {
  UNKNOWN: {
    value: 0,
    desc: '未知',
  },
  MAN: {
    value: 1,
    desc: '男',
  },
  WOMAN: {
    value: 2,
    desc: '女',
  },
};

export const USER_TYPE_ENUM = {
  ADMIN_EMPLOYEE: {
    value: 1,
    desc: '员工',
  },

};

export const DATA_TYPE_ENUM = {
  NORMAL: {
    value: 1,
    desc: '普通',
  },
  ENCRYPT: {
    value: 10,
    desc: '加密',
  },

};

