/*
 * 环境相关常量
 */

// 环境类型
export const ENV_TYPE_ENUM = {
  DEV: {
    value: 'dev',
    desc: '开发环境',
    color: 'blue',
  },
  TEST: {
    value: 'test',
    desc: '测试环境',
    color: 'orange',
  },
  PROD: {
    value: 'prod',
    desc: '生产环境',
    color: 'red',
  },
  CUSTOM: {
    value: 'custom',
    desc: '自定义环境',
    color: 'purple',
  },
};

// 数据库类型
export const DB_TYPE_ENUM = {
  POSTGRESQL: {
    value: 'postgresql',
    desc: 'PostgreSQL',
    icon: 'postgresql',
  },
};
