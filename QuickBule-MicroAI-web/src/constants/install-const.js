/*
 * 安装相关常量
 */

// 安装类型
export const INSTALL_TYPE_ENUM = {
  SIMPLE: {
    value: 'simple',
    desc: '快速安装',
  },
  CUSTOM: {
    value: 'custom',
    desc: '自定义安装',
  },
};

// 数据库类型
export const DB_TYPE_ENUM = {
  POSTGRESQL: {
    value: 'postgresql',
    desc: 'PostgreSQL',
    driver: 'org.postgresql.Driver',
    defaultPort: 5432,
  },
};

// 存储模式
export const STORAGE_MODE_ENUM = {
  LOCAL: {
    value: 'local',
    desc: '本地存储',
  },
  CLOUD: {
    value: 'cloud',
    desc: '云存储',
  },
};

// 云服务商
export const CLOUD_PROVIDER_ENUM = {
  ALIYUN: {
    value: 'aliyun',
    desc: '阿里云 OSS',
  },
  TENCENT: {
    value: 'tencent',
    desc: '腾讯云 COS',
  },
  QINIU: {
    value: 'qiniu',
    desc: '七牛云',
  },
  AWS: {
    value: 'aws',
    desc: 'AWS S3',
  },
};

// 安装状态
export const INSTALL_STATUS_ENUM = {
  PENDING: {
    value: 'pending',
    desc: '等待中',
  },
  RUNNING: {
    value: 'running',
    desc: '执行中',
  },
  COMPLETED: {
    value: 'completed',
    desc: '已完成',
  },
  ERROR: {
    value: 'error',
    desc: '失败',
  },
};

// 日志级别
export const LOG_LEVEL_ENUM = {
  INFO: 'INFO',
  SUCCESS: 'SUCCESS',
  WARNING: 'WARNING',
  ERROR: 'ERROR',
};

// 默认配置
export const DEFAULT_CONFIG = {
  // 数据库配置
  database: {
    dbType: DB_TYPE_ENUM.POSTGRESQL.value,
    host: '127.0.0.1',
    port: 5432,
    adminUser: 'root',
    databases: [
      {
        name: 'quickblue_support',
        username: 'support_user',
      },
      {
        name: 'quickblue_system',
        username: 'system_user',
      },
      {
        name: 'quickblue_business',
        username: 'business_user',
      },
      {
        name: 'quickblue_ai',
        username: 'ai_user',
      },
    ],
  },
  // Redis配置
  redis: {
    host: '127.0.0.1',
    port: 6379,
    password: 'Nq963369',
    database: 1,
    timeout: 10,
  },
  // 服务配置
  service: {
    gatewayUrl: 'http://localhost:8080',
    gatewayPort: 8080,
    systemPort: 8081,
    businessPort: 8082,
    supportPort: 8083,
    aiPort: 8084,
    adminPort: 9090,
    webDevPort: 5173,
  },
  // 存储配置
  storage: {
    mode: STORAGE_MODE_ENUM.LOCAL.value,
    local: {
      uploadPath: './uploads',
      urlPrefix: 'http://localhost:8080/api/files',
      maxFileSize: 100,
    },
    cloud: {
      provider: CLOUD_PROVIDER_ENUM.ALIYUN.value,
      region: 'oss-cn-hangzhou',
      bucketName: 'quickblue-files',
      endpoint: 'oss-cn-hangzhou.aliyuncs.com',
    },
  },
  // 管理员配置
  admin: {
    username: 'admin',
    realName: '系统管理员',
    department: '技术部',
    position: '系统管理员',
  },
};
