/*
 * 所有常量入口
 *

 */
import menu from './system/menu-const';
import { LOGIN_DEVICE_ENUM } from './system/login-device-const';
import { FLAG_NUMBER_ENUM, GENDER_ENUM, USER_TYPE_ENUM } from './common-const';
import { LAYOUT_ENUM } from './layout-const';
import file from './support/file-const';
import notice from '/@/constants/business/notice/notice-const';
import loginLog from './support/login-log-const';
import message from './business/message/message-const';
import changeLogConst from './support/change-log-const';
import jobConst from './support/job-const';
import databaseBackupConst from './support/database-backup-const';
import serialNumberConst from './support/serial-number-const';
import {
  AI_MODEL_PROVIDER_ENUM,
  AI_MODEL_TYPE_ENUM,
  AI_APP_TYPE_ENUM,
  AI_APP_STATUS_ENUM,
  AI_KNOWLEDGE_TYPE_ENUM,
  AI_KNOWLEDGE_STATUS_ENUM,
  AI_DOC_STATUS_ENUM,
  AI_AGENT_STATUS_ENUM,
} from './ai/ai-const';

export default {
  FLAG_NUMBER_ENUM,
  LOGIN_DEVICE_ENUM,
  GENDER_ENUM,
  USER_TYPE_ENUM,
  LAYOUT_ENUM,
  ...loginLog,
  ...menu,
  ...file,
  ...notice,
  ...message,
  ...changeLogConst,
  ...jobConst,
  ...databaseBackupConst,
  ...serialNumberConst,
  AI_MODEL_PROVIDER_ENUM,
  AI_MODEL_TYPE_ENUM,
  AI_APP_TYPE_ENUM,
  AI_APP_STATUS_ENUM,
  AI_KNOWLEDGE_TYPE_ENUM,
  AI_KNOWLEDGE_STATUS_ENUM,
  AI_DOC_STATUS_ENUM,
  AI_AGENT_STATUS_ENUM,
};
