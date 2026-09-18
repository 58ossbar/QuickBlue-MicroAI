/*
 * 数据库备份常量
 */
export const BACKUP_TYPE_ENUM = {
  AUTO: {
    value: 1,
    desc: '自动备份',
  },
  MANUAL: {
    value: 2,
    desc: '手动备份',
  },
};

export const BACKUP_STATUS_ENUM = {
  BACKUPING: {
    value: 0,
    desc: '备份中',
  },
  SUCCESS: {
    value: 1,
    desc: '备份成功',
  },
  FAILED: {
    value: 2,
    desc: '备份失败',
  },
};

export default {
  BACKUP_TYPE_ENUM,
  BACKUP_STATUS_ENUM,
};
