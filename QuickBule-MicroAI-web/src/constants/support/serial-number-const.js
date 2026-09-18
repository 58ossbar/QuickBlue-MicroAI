/*
 * 单据序列号常量
 */
export const SERIAL_NUMBER_RULE_TYPE_ENUM = {
  NONE: {
    value: '',
    desc: '没有周期',
  },
  YEAR: {
    value: '[yyyy]',
    desc: '年',
  },
  MONTH: {
    value: '[mm]',
    desc: '年月',
  },
  DAY: {
    value: '[dd]',
    desc: '年月日',
  },
};

export const SERIAL_NUMBER_SECURE_MODE_ENUM = {
  NONE: {
    value: 0,
    desc: '普通模式',
  },
  RANDOM: {
    value: 1,
    desc: '随机模式',
  },
  TIMESTAMP: {
    value: 2,
    desc: '时间戳模式',
  },
  ENCRYPTED: {
    value: 3,
    desc: '加密模式',
  },
};

export default {
  SERIAL_NUMBER_RULE_TYPE_ENUM,
  SERIAL_NUMBER_SECURE_MODE_ENUM,
};
