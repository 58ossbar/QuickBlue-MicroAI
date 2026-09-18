/*
 Navicat Premium Dump SQL

 Source Server         : 本地开发服务器
 Source Server Type    : MySQL
 Source Schema         : quickblue_support

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 18/08/2026 17:54:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_change_log
-- ----------------------------
DROP TABLE IF EXISTS `t_change_log`;
CREATE TABLE `t_change_log`  (
                                 `change_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '更新日志id',
                                 `update_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '版本',
                                 `type` int NOT NULL COMMENT '更新类型:[1:特大版本功能更新;2:功能更新;3:bug修复]',
                                 `publish_author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发布人',
                                 `public_date` date NOT NULL COMMENT '发布日期',
                                 `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '更新内容',
                                 `link` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '跳转链接',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`change_log_id`) USING BTREE,
                                 UNIQUE INDEX `version_unique`(`update_version` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统更新日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_change_log
-- ----------------------------
INSERT INTO `t_change_log` VALUES (8, '专业版v3.0', 1, '管理员', '2026-01-26', '长影票务专业版 v3.0.0 版本（20260126）正式更新上线，更新内容如下：\n1.【新增】票券核销：支持输码、单张扫码（连续）核销\n2.【新增】核销语音播报或震动反馈提醒和连续扫码设置\n3.【新增】上传核销员头像、修改密码功能\n4.【新增】通知公告功能\n5.【新增】核销历史记录查询\n6.【新增】首页统计功能', '', '2022-10-04 21:33:50', '2026-01-27 13:31:36');


-- ----------------------------
-- Table structure for t_config
-- ----------------------------
DROP TABLE IF EXISTS `t_config`;
CREATE TABLE `t_config`  (
                             `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参数名字',
                             `config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参数key',
                             `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
                             `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                             `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '上次修改时间',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `config_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'system' COMMENT '配置分组',
                             `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'text' COMMENT '配置类型:text/number/boolean/json/select',
                             `options_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '选项配置(select类型用)',
                             `is_encrypted` tinyint NULL DEFAULT 0 COMMENT '是否加密:0否1是',
                             `sort_order` int NULL DEFAULT 0 COMMENT '排序',
                             PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_config
-- ----------------------------
INSERT INTO `t_config` VALUES (1, '万能密码', 'super_password', '2048nq963369#', '执行示例任务2', '2026-01-27 12:58:01', '2021-12-16 23:32:46', 'system', 'text', NULL, 0, 0);
INSERT INTO `t_config` VALUES (2, '三级等保', 'level3_protect_config', '{\n	\"fileDetectFlag\":true,\n	\"loginActiveTimeoutMinutes\":30,\n	\"loginFailLockMinutes\":30,\n	\"loginFailMaxTimes\":3,\n	\"maxUploadFileSizeMb\":100,\n	\"passwordComplexityEnabled\":true,\n	\"regularChangePasswordMonths\":3,\n	\"regularChangePasswordNotAllowRepeatTimes\":3,\n	\"twoFactorLoginEnabled\":false\n}', 'JobTask Sample2 update', '2026-01-30 09:28:27', '2024-08-13 11:44:49', 'system', 'text', NULL, 0, 0);

-- ----------------------------
-- Table structure for t_config_group
-- ----------------------------
DROP TABLE IF EXISTS `t_config_group`;
CREATE TABLE `t_config_group`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `group_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组编码',
                                   `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组名称',
                                   `group_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组图标',
                                   `sort_order` int NULL DEFAULT 0 COMMENT '排序',
                                   `status` tinyint NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE INDEX `uk_group_code`(`group_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '配置分组表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_config_group
-- ----------------------------
INSERT INTO `t_config_group` VALUES (1, 'security', '安全配置', 'SafetyOutlined', 1, 1);
INSERT INTO `t_config_group` VALUES (2, 'file', '文件配置', 'FileOutlined', 2, 1);
INSERT INTO `t_config_group` VALUES (3, 'mail', '邮件配置', 'MailOutlined', 3, 1);
INSERT INTO `t_config_group` VALUES (4, 'sms', '短信配置', 'MessageOutlined', 4, 1);
INSERT INTO `t_config_group` VALUES (5, 'storage', '存储配置', 'CloudServerOutlined', 5, 1);
INSERT INTO `t_config_group` VALUES (6, 'system', '系统配置', 'SettingOutlined', 6, 1);

-- ----------------------------
-- Table structure for t_data_tracer
-- ----------------------------
DROP TABLE IF EXISTS `t_data_tracer`;
CREATE TABLE `t_data_tracer`  (
                                  `data_tracer_id` bigint NOT NULL AUTO_INCREMENT,
                                  `data_id` bigint NOT NULL COMMENT '各种单据的id',
                                  `type` int NOT NULL COMMENT '单据类型',
                                  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '操作内容',
                                  `diff_old` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '差异：旧的数据',
                                  `diff_new` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '差异：新的数据',
                                  `extra_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '额外信息',
                                  `user_id` bigint NOT NULL COMMENT '用户id',
                                  `user_type` int NOT NULL COMMENT '用户类型：1 后管用户 ',
                                  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名称',
                                  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ip',
                                  `ip_region` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ip地区',
                                  `user_agent` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ua',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  PRIMARY KEY (`data_tracer_id`) USING BTREE,
                                  INDEX `order_id_order_type`(`data_id` ASC, `type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '各种单据操作记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_data_tracer
-- ----------------------------

-- ----------------------------
-- Table structure for t_database_backup
-- ----------------------------
DROP TABLE IF EXISTS `t_database_backup`;
CREATE TABLE `t_database_backup`  (
                                      `backup_id` bigint NOT NULL AUTO_INCREMENT COMMENT '备份ID',
                                      `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '备份文件名',
                                      `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '备份文件路径',
                                      `file_size` bigint NULL DEFAULT NULL COMMENT '备份文件大小(字节)',
                                      `backup_type` tinyint NOT NULL COMMENT '备份类型(1-自动备份 2-手动备份)',
                                      `backup_status` tinyint NOT NULL DEFAULT 0 COMMENT '备份状态(0-备份中 1-备份成功 2-备份失败)',
                                      `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份描述/备注',
                                      `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
                                      `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人',
                                      `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                      `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                      PRIMARY KEY (`backup_id`) USING BTREE,
                                      INDEX `idx_backup_status`(`backup_status` ASC) USING BTREE,
                                      INDEX `idx_backup_type`(`backup_type` ASC) USING BTREE,
                                      INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 187 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据库备份记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_database_backup
-- ----------------------------
INSERT INTO `t_database_backup` VALUES (186, 'tickethub_20260213_182815.sql', '/data/backup\\tickethub_20260213_182815.sql', 422195, 2, 1, NULL, NULL, NULL, NULL, '2026-02-13 18:28:16');

-- ----------------------------
-- Table structure for t_database_backup_config
-- ----------------------------
DROP TABLE IF EXISTS `t_database_backup_config`;
CREATE TABLE `t_database_backup_config`  (
                                             `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
                                             `db_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据库主机',
                                             `db_port` int NOT NULL COMMENT '数据库端口',
                                             `db_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据库名称',
                                             `db_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据库用户名',
                                             `db_password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据库密码',
                                             `backup_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '备份文件存储路径',
                                             `auto_backup_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否启用自动备份(0-否 1-是)',
                                             `auto_backup_cron` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自动备份cron表达式',
                                             `retention_days` int NULL DEFAULT 30 COMMENT '备份保留天数',
                                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                             `mysqldump_path` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
                                             PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据库备份配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_database_backup_config
-- ----------------------------
INSERT INTO `t_database_backup_config` VALUES (5, '81.71.158.147', 33241, 'tickethub', 'root', 'creatorblue', '/data/backup', 1, '0 0 6 * * ?', 30, NULL, '2026-02-13 18:28:06', NULL, 'F:\\database\\mysql-8.0.29-winx64\\bin\\mysqldump.exe');

-- ----------------------------
-- Table structure for t_dict
-- ----------------------------
DROP TABLE IF EXISTS `t_dict`;
CREATE TABLE `t_dict`  (
                           `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典id',
                           `dict_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典名字',
                           `dict_code` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典编码',
                           `remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典备注',
                           `disabled_flag` tinyint NOT NULL DEFAULT 0 COMMENT '禁用状态',
                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`dict_id`) USING BTREE,
                           UNIQUE INDEX `unique_code`(`dict_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_dict
-- ----------------------------
INSERT INTO `t_dict` VALUES (4, '是否删除', 'deleteFlag', '', 0, '2025-11-29 16:37:49', '2025-11-29 16:37:49');
INSERT INTO `t_dict` VALUES (5, '状态', 'status', '', 0, '2025-11-29 18:38:44', '2025-11-29 18:38:44');
INSERT INTO `t_dict` VALUES (6, 'AI模型类型', 'AI_MODEL_TYPE_ENUM', 'AI模板类型', 0, '2026-03-10 19:59:55', '2026-03-10 21:22:12');
INSERT INTO `t_dict` VALUES (7, 'AI模型供应商', 'AI_MODEL_PROVIDER_ENUM', '', 0, '2026-03-10 21:23:52', '2026-03-10 21:23:52');

-- ----------------------------
-- Table structure for t_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `t_dict_data`;
CREATE TABLE `t_dict_data`  (
                                `dict_data_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典数据id',
                                `dict_id` bigint NOT NULL COMMENT '字典id',
                                `data_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典项值',
                                `data_label` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字典项显示名称',
                                `parent_code` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父级编码（用于树形字典）',
                                `remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                `sort_order` int NOT NULL COMMENT '排序（越大越靠前）',
                                `disabled_flag` tinyint NOT NULL DEFAULT 0 COMMENT '禁用状态',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`dict_data_id`) USING BTREE,
                                INDEX `idx_dict_id`(`dict_id` ASC) USING BTREE,
                                INDEX `idx_parent_code`(`parent_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_dict_data
-- ----------------------------
INSERT INTO `t_dict_data` VALUES (9, 4, '1', '是', NULL, '', 0, 0, '2025-11-29 16:38:08', '2025-11-29 16:38:08');
INSERT INTO `t_dict_data` VALUES (10, 4, '0', '否', NULL, '', 0, 0, '2025-11-29 16:41:08', '2025-11-29 16:41:08');
INSERT INTO `t_dict_data` VALUES (11, 5, '1', '启用', NULL, '', 1, 0, '2025-11-29 18:38:56', '2025-11-29 18:38:56');
INSERT INTO `t_dict_data` VALUES (12, 5, '2', '禁用', NULL, '', 2, 0, '2025-11-29 18:39:07', '2025-12-07 13:20:56');
INSERT INTO `t_dict_data` VALUES (13, 6, '1', '大语言模型', NULL, '', 0, 0, '2026-03-10 20:00:29', '2026-03-10 21:22:47');
INSERT INTO `t_dict_data` VALUES (14, 6, '2', '向量模型', NULL, '', 0, 0, '2026-03-10 20:00:39', '2026-03-10 20:00:39');
INSERT INTO `t_dict_data` VALUES (15, 6, '3', '图像模型', NULL, '', 0, 0, '2026-03-10 20:00:49', '2026-03-10 20:00:49');
INSERT INTO `t_dict_data` VALUES (16, 6, '4', '对话', NULL, '', 0, 0, '2026-03-10 21:22:40', '2026-03-10 21:22:40');
INSERT INTO `t_dict_data` VALUES (17, 6, '5', '文本补全', NULL, '', 0, 0, '2026-03-10 21:23:01', '2026-03-10 21:23:01');
INSERT INTO `t_dict_data` VALUES (18, 7, '1', 'OpenAI', NULL, '', 0, 0, '2026-03-10 21:24:14', '2026-03-10 21:24:14');
INSERT INTO `t_dict_data` VALUES (19, 7, '2', ' Ollama', NULL, '', 0, 0, '2026-03-10 21:24:27', '2026-03-10 21:24:27');
INSERT INTO `t_dict_data` VALUES (20, 7, '3', '智谱 AI', NULL, '', 0, 0, '2026-03-10 21:24:38', '2026-03-10 21:24:38');
INSERT INTO `t_dict_data` VALUES (21, 7, '4', '百度文心', NULL, '', 0, 0, '2026-03-10 21:24:50', '2026-03-10 21:24:50');
INSERT INTO `t_dict_data` VALUES (22, 7, '5', '通义千问', NULL, '', 0, 0, '2026-03-10 21:25:02', '2026-03-10 21:25:02');
INSERT INTO `t_dict_data` VALUES (23, 7, '6', 'DeepSeek', NULL, '', 0, 0, '2026-03-10 21:25:14', '2026-03-10 21:25:14');

-- ----------------------------
-- Table structure for t_dict_table_whitelist
-- ----------------------------
DROP TABLE IF EXISTS `t_dict_table_whitelist`;
CREATE TABLE `t_dict_table_whitelist`  (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                           `table_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表名',
                                           `key_field` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '键字段',
                                           `label_field` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签字段',
                                           `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                           `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
                                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           PRIMARY KEY (`id`) USING BTREE,
                                           UNIQUE INDEX `uk_table_key_label`(`table_name` ASC, `key_field` ASC, `label_field` ASC) USING BTREE,
                                           INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '表字典白名单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_dict_table_whitelist
-- ----------------------------
INSERT INTO `t_dict_table_whitelist` VALUES (1, 't_department', 'department_id', 'name', NULL, 1, '2026-03-28 22:50:03', '2026-03-28 22:50:03');

-- ----------------------------
-- Table structure for t_feedback
-- ----------------------------
DROP TABLE IF EXISTS `t_feedback`;
CREATE TABLE `t_feedback`  (
                               `feedback_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `feedback_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '反馈内容',
                               `feedback_attachment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '反馈图片',
                               `user_id` bigint NOT NULL COMMENT '创建人id',
                               `user_type` int NOT NULL COMMENT '创建人用户类型',
                               `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人姓名',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`feedback_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '意见反馈' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_feedback
-- ----------------------------

-- ----------------------------
-- Table structure for t_heart_beat_record
-- ----------------------------
DROP TABLE IF EXISTS `t_heart_beat_record`;
CREATE TABLE `t_heart_beat_record`  (
                                        `heart_beat_record_id` int NOT NULL AUTO_INCREMENT COMMENT '自增id',
                                        `project_path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目名称',
                                        `server_ip` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务器ip',
                                        `process_no` int NOT NULL COMMENT '进程号',
                                        `process_start_time` datetime NOT NULL COMMENT '进程开启时间',
                                        `heart_beat_time` datetime NOT NULL COMMENT '心跳时间',
                                        PRIMARY KEY (`heart_beat_record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公用服务 - 服务心跳' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_heart_beat_record
-- ----------------------------

-- ----------------------------
-- Table structure for t_help_doc
-- ----------------------------
DROP TABLE IF EXISTS `t_help_doc`;
CREATE TABLE `t_help_doc`  (
                               `help_doc_id` bigint NOT NULL AUTO_INCREMENT,
                               `help_doc_catalog_id` bigint NOT NULL COMMENT '类型1公告 2动态',
                               `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
                               `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文本内容',
                               `content_html` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'html内容',
                               `attachment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '附件',
                               `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
                               `page_view_count` int NOT NULL DEFAULT 0 COMMENT '页面浏览量，传说中的pv',
                               `user_view_count` int NOT NULL DEFAULT 0 COMMENT '用户浏览量，传说中的uv',
                               `author` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作者',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`help_doc_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帮助文档' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_help_doc
-- ----------------------------
INSERT INTO `t_help_doc` VALUES (36, 6, '系统概述', '一、系统建设背景与目标\n为提升集团票务分公司整体管理效率、强化票券防伪能力、规范票券进销存全流程管理，根据集团统一部署与领导要求，票务分公司定制开发了小程序票务管理系统。该系统以“全链路数字化、核销高效化、数据实时化、防伪精准化”为核心目标，构建从票券设计到核销归档的完整闭环管理体系。\n二、系统核心价值\n1. 强化防伪能力\n采用一票一码机制，每张票券生成唯一加密二维码，绑定票种、批次、影城等多维度信息。\n支持动态核验，防止截图、复制、重复核销，杜绝假票流通。\n2. 提升核销效率\n影城前台通过小程序扫码即可快速完成核销，减少操作误差。\n支持手工录入票券编码核销\n支持单张扫码和连续扫码\n3. 实现数据协同\n核销数据实时同步至公司后台管理系统，管理人员可随时查看票券状态、核销进度、影城销售情况。\n支持多维度统计与预警，为经营决策提供实时数据支撑。\n三、票券全生命周期管理环节\n系统围绕票券“生、发、销、核、存、析”六个环节构建闭环流程：\n\n环节功能模块说明1. 设置票种票种管理定义票券类型、价格、使用规则、适用影城等基础属性2. 设计批次批次管理按需生成票券批次，设置有效期、发行数量、关联票种3. 生成票券票券管理系统自动生成票券及唯一二维码，支持批量导出与打印4. 生成二维码二维码管理每张票券对应一个动态加密二维码，防伪可追溯5. 票券核销核销管理影城扫码核销，实时更新状态，支持核销员权限管理6. 票券统计统计分析多维数据报表、同环比分析、核销员统计、异常预警等\n四、系统功能架构概览\n系统分为后台管理端与小程序核销端：\n✅ 后台管理端（PC）\n票务管理：票种、批次、票券的全流程配置与查询\n影城管理：影城信息、区域分配、状态管理\n统计分析：多维度报表、排名、预警、同环比分析\n组织架构：人员权限、角色分配、核销员管理\n系统设置：基础配置、文档中心、数据安全策略\n✅ 小程序核销端（移动端）\n扫码核销：快速识别票券二维码，完成核销\n核销记录：查看当日/历史核销明细\n消息通知：接收系统通知、核销异常提醒', '<h2 style=\"text-align: start;\">一、系统建设背景与目标</h2><p style=\"text-align: start;\">为提升集团票务分公司整体管理效率、强化票券防伪能力、规范票券进销存全流程管理，根据集团统一部署与领导要求，票务分公司定制开发了<strong>小程序票务管理系统</strong>。该系统以“全链路数字化、核销高效化、数据实时化、防伪精准化”为核心目标，构建从票券设计到核销归档的完整闭环管理体系。</p><h2 style=\"text-align: start;\">二、系统核心价值</h2><h3 style=\"text-align: start;\">1. <strong>强化防伪能力</strong></h3><ul><li style=\"text-align: start;\">采用一票一码机制，每张票券生成唯一加密二维码，绑定票种、批次、影城等多维度信息。</li><li style=\"text-align: start;\">支持动态核验，防止截图、复制、重复核销，杜绝假票流通。</li></ul><h3 style=\"text-align: start;\">2. <strong>提升核销效率</strong></h3><ul><li style=\"text-align: start;\">影城前台通过小程序扫码即可快速完成核销，减少操作误差。</li><li style=\"text-align: start;\">支持手工录入票券编码核销</li><li style=\"text-align: start;\">支持单张扫码和连续扫码</li></ul><h3 style=\"text-align: start;\">3. <strong>实现数据协同</strong></h3><ul><li style=\"text-align: start;\">核销数据实时同步至公司后台管理系统，管理人员可随时查看票券状态、核销进度、影城销售情况。</li><li style=\"text-align: start;\">支持多维度统计与预警，为经营决策提供实时数据支撑。</li></ul><h2 style=\"text-align: start;\">三、票券全生命周期管理环节</h2><p style=\"text-align: start;\">系统围绕票券“生、发、销、核、存、析”六个环节构建闭环流程：</p><p style=\"text-align: start;\"><br></p><table style=\"width: 100%;table-layout: fixed;height:196\"><colgroup contentEditable=\"false\"><col width=90></col><col width=90></col><col width=90></col></colgroup><tbody><tr><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">环节</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">功能模块</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">说明</th></tr><tr><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">1. <strong>设置票种</strong></td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">票种管理</td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">定义票券类型、价格、使用规则、适用影城等基础属性</td></tr><tr><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">2. <strong>设计批次</strong></td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">批次管理</td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">按需生成票券批次，设置有效期、发行数量、关联票种</td></tr><tr><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">3. <strong>生成票券</strong></td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">票券管理</td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">系统自动生成票券及唯一二维码，支持批量导出与打印</td></tr><tr><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">4. <strong>生成二维码</strong></td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">二维码管理</td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">每张票券对应一个动态加密二维码，防伪可追溯</td></tr><tr><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">5. <strong>票券核销</strong></td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">核销管理</td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">影城扫码核销，实时更新状态，支持核销员权限管理</td></tr><tr><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">6. <strong>票券统计</strong></td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">统计分析</td><td colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"border-width: 1px; border-style: solid; border-color: rgb(204, 204, 204);\">多维数据报表、同环比分析、核销员统计、异常预警等</td></tr></tbody></table><h2 style=\"text-align: start;\">四、系统功能架构概览</h2><p style=\"text-align: start;\">系统分为<strong>后台管理端</strong>与<strong>小程序核销端</strong>：</p><h3 style=\"text-align: start;\">✅ 后台管理端（PC）</h3><ul><li style=\"text-align: start;\">票务管理：票种、批次、票券的全流程配置与查询</li><li style=\"text-align: start;\">影城管理：影城信息、区域分配、状态管理</li><li style=\"text-align: start;\">统计分析：多维度报表、排名、预警、同环比分析</li><li style=\"text-align: start;\">组织架构：人员权限、角色分配、核销员管理</li><li style=\"text-align: start;\">系统设置：基础配置、文档中心、数据安全策略</li></ul><h3 style=\"text-align: start;\">✅ 小程序核销端（移动端）</h3><ul><li style=\"text-align: start;\">扫码核销：快速识别票券二维码，完成核销</li><li style=\"text-align: start;\">核销记录：查看当日/历史核销明细</li><li style=\"text-align: start;\">消息通知：接收系统通知、核销异常提醒</li></ul>', '', 0, 41, 2, '长影票务', '2026-02-12 14:53:53', '2025-12-14 22:28:01');
INSERT INTO `t_help_doc` VALUES (43, 26, '部门管理模块', '一、模块概述\n部门管理模块是票务分公司小程序系统的组织架构核心，负责建立和维护公司的部门层级体系。本模块支持树形部门结构管理，用于员工归属、权限分配、工作流程和组织管理，确保企业内部组织架构清晰、权责分明。\n二、模块入口\n访问路径：登录Web管理系统 → 左侧导航栏 → 组织架构 → 部门管理\n权限要求：需具备“组织架构管理员”或“系统管理员”及以上权限角色\n三、功能详解\n1. 部门列表与查询\n1.1 列表展示\n以树形表格形式展示所有部门信息，清晰展示上下级关系\n默认展示所有部门数据，支持展开/收起下级部门\n显示字段包括：部门名称（完整部门名称）负责人（部门负责人姓名）排序（显示顺序权重）创建时间（部门创建时间）更新时间（最后修改时间）操作（添加下级/编辑/删除）\n1.2 查询筛选\n提供以下筛选条件：\n部门名称：支持模糊查询部门名称\n操作按钮：\n查询：根据筛选条件刷新部门列表\n重置：清空筛选条件，显示全部部门数据\n2. 新建部门\n操作流程：\n点击列表右上方的 “+新建” 按钮\n在弹出的“添加部门”表单中填写以下信息：\n2.1 部门信息\n上级部门（必填）：从下拉菜单中选择上级部门顶级部门：选择“请选择部门”（即无上级，作为公司级部门）下级部门：选择对应的上级部门\n部门名称（必填）：输入完整部门名称（如“票务分公司”）命名规范：建议使用公司标准部门名称避免重复：确保同级部门名称不重复\n2.2 管理信息\n部门负责人（可选）：从员工列表中选择部门负责人负责人必须为已创建的系统用户负责人自动获得该部门的管理权限\n部门排序（必填）：输入排序值排序规则：值越大显示越靠前默认值：0建议设置：同级部门设置不同排序值，如10、20、30...\n点击 “确定” 完成部门创建\n点击 “取消” 放弃操作\n注意：\n部门名称在同一层级下必须唯一\n排序值影响部门在列表中的显示顺序\n部门负责人选择非必填，可后期补充\n3. 添加下级部门\n操作流程：\n在部门列表中找到需要添加下级部门的记录\n点击该记录“操作”列中的 “添加下级” 按钮\n系统自动预填“上级部门”字段\n填写下级部门的其他信息\n点击 “确定” 完成创建\n特点：\n下级部门自动继承上级部门的组织属性\n方便快速建立部门树形结构\n保持组织架构的完整性\n4. 编辑部门\n操作流程：\n在部门列表中找到需要修改的记录\n点击该记录“操作”列中的 “编辑” 按钮\n在弹出的编辑表单中修改信息：\n可修改字段：\n部门名称：可修改（同级部门中需保持唯一）\n部门负责人：可从员工列表中重新选择或清空\n部门排序：可修改排序值\n不可修改字段：\n上级部门：创建后不可直接修改（需通过特殊流程）\n创建时间：系统自动记录，不可修改\n部门编码（如有）：系统生成，不可修改\n点击 “确定” 完成修改\n点击 “取消” 放弃修改\n5. 删除部门\n操作流程：\n在部门列表中找到目标记录\n点击“操作”列中的 “删除” 按钮\n系统弹出确认对话框\n确认后删除该部门\n删除规则：\n只能删除没有下级部门的节点\n如果部门下有子部门，必须先删除所有子部门\n部门下有员工时不可删除\n部门被业务模块引用时不可删除（如权限分配、工作流等）\n删除操作不可逆，请谨慎操作\n6. 部门层级管理\n6.1 层级结构示例\ntext\n湖北长江电影集团有限责任公司（顶级部门）\n├── 票务分公司\n│   ├── 运营部（示例）\n│   ├── 技术部（示例）\n│   └── 市场部（示例）\n├── 集团公共事业管理中心\n├── 湖北银兴院线影业有限责任公司\n├── 湖北电影制片有限责任公司\n└── 其他子公司/部门\n6.2 层级深度\n系统支持无限级部门层级（理论上）\n实际应用中建议不超过5级，保证管理效率\n层级过深可能影响权限分配和数据处理效率\n7. 排序功能\n7.1 排序规则\n排序值越大，显示越靠前\n同级部门按排序值降序排列\n默认排序值为0\n支持负数排序值\n7.2 排序策略\n重要部门：设置较大排序值（如100、200）\n常规部门：设置中等排序值（如10、20、30）\n临时部门：设置较小排序值或负值\n预留空间：排序值设置间隔，方便后续调整\n四、部门负责人管理\n1. 负责人权限\n部门负责人自动获得该部门的管理权限\n可查看部门下所有员工信息\n可审批部门相关业务流程\n可分配部门内权限\n2. 负责人变更\n通过编辑部门信息修改负责人\n变更后原负责人权限自动撤销\n新负责人权限自动生效\n建议及时更新负责人信息，确保权限准确\n3. 负责人空缺处理\n部门可暂时不设置负责人\n负责人空缺时，上级部门负责人代管\n或由系统管理员指定临时负责人\n五、业务应用场景\n1. 员工归属管理\n每个员工必须归属于一个具体部门\n员工列表可按部门筛选\n部门负责人可管理下属员工\n2. 权限分配\n按部门分配系统功能权限\n部门级数据权限控制\n工作流程按部门配置\n3. 组织架构展示\n企业组织架构图生成\n部门人员统计报表\n部门层级关系展示\n4. 业务流程\n按部门配置审批流程\n部门间协作流程管理\n跨部门工作流支持\n六、数据规范与约束\n1. 命名规范\n部门名称：使用公司官方名称\n名称长度：建议不超过50个字符\n避免使用特殊字符和空格\n2. 层级约束\n部门必须明确上级部门（顶级部门除外）\n不允许循环引用（A的上级是B，B的上级是A）\n层级深度应有合理限制\n3. 数据一致性\n部门信息与其他模块（员工、角色、权限）保持同步\n部门变更时，相关数据应自动更新或提示处理\n定期检查部门数据的完整性\n', '<h2 style=\"text-align: start;\">一、模块概述</h2><p style=\"text-align: start;\">部门管理模块是票务分公司小程序系统的组织架构核心，负责建立和维护公司的部门层级体系。本模块支持树形部门结构管理，用于员工归属、权限分配、工作流程和组织管理，确保企业内部组织架构清晰、权责分明。</p><h2 style=\"text-align: start;\">二、模块入口</h2><ul><li style=\"text-align: start;\">访问路径：登录Web管理系统 → 左侧导航栏 → 组织架构 → 部门管理</li><li style=\"text-align: start;\">权限要求：需具备“组织架构管理员”或“系统管理员”及以上权限角色</li></ul><h2 style=\"text-align: start;\">三、功能详解</h2><h3 style=\"text-align: start;\">1. 部门列表与查询</h3><h4 style=\"text-align: start;\">1.1 列表展示</h4><ul><li style=\"text-align: start;\">以树形表格形式展示所有部门信息，清晰展示上下级关系</li><li style=\"text-align: start;\">默认展示所有部门数据，支持展开/收起下级部门</li><li style=\"text-align: start;\">显示字段包括：部门名称（完整部门名称）负责人（部门负责人姓名）排序（显示顺序权重）创建时间（部门创建时间）更新时间（最后修改时间）操作（添加下级/编辑/删除）</li></ul><h4 style=\"text-align: start;\">1.2 查询筛选</h4><p style=\"text-align: start;\">提供以下筛选条件：</p><ul><li style=\"text-align: start;\">部门名称：支持模糊查询部门名称</li></ul><p style=\"text-align: start;\">操作按钮：</p><ul><li style=\"text-align: start;\">查询：根据筛选条件刷新部门列表</li><li style=\"text-align: start;\">重置：清空筛选条件，显示全部部门数据</li></ul><h3 style=\"text-align: start;\">2. 新建部门</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">点击列表右上方的 “+新建” 按钮</li><li style=\"text-align: start;\">在弹出的“添加部门”表单中填写以下信息：</li></ol><h4 style=\"text-align: start;\">2.1 部门信息</h4><ul><li style=\"text-align: start;\">上级部门（必填）：从下拉菜单中选择上级部门顶级部门：选择“请选择部门”（即无上级，作为公司级部门）下级部门：选择对应的上级部门</li><li style=\"text-align: start;\">部门名称（必填）：输入完整部门名称（如“票务分公司”）命名规范：建议使用公司标准部门名称避免重复：确保同级部门名称不重复</li></ul><h4 style=\"text-align: start;\">2.2 管理信息</h4><ul><li style=\"text-align: start;\">部门负责人（可选）：从员工列表中选择部门负责人负责人必须为已创建的系统用户负责人自动获得该部门的管理权限</li><li style=\"text-align: start;\">部门排序（必填）：输入排序值排序规则：值越大显示越靠前默认值：0建议设置：同级部门设置不同排序值，如10、20、30...</li></ul><ol><li style=\"text-align: start;\">点击 “确定” 完成部门创建</li><li style=\"text-align: start;\">点击 “取消” 放弃操作</li></ol><p style=\"text-align: start;\"><strong>注意</strong>：</p><ul><li style=\"text-align: start;\">部门名称在同一层级下必须唯一</li><li style=\"text-align: start;\">排序值影响部门在列表中的显示顺序</li><li style=\"text-align: start;\">部门负责人选择非必填，可后期补充</li></ul><h3 style=\"text-align: start;\">3. 添加下级部门</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在部门列表中找到需要添加下级部门的记录</li><li style=\"text-align: start;\">点击该记录“操作”列中的 “添加下级” 按钮</li><li style=\"text-align: start;\">系统自动预填“上级部门”字段</li><li style=\"text-align: start;\">填写下级部门的其他信息</li><li style=\"text-align: start;\">点击 “确定” 完成创建</li></ol><p style=\"text-align: start;\"><strong>特点</strong>：</p><ul><li style=\"text-align: start;\">下级部门自动继承上级部门的组织属性</li><li style=\"text-align: start;\">方便快速建立部门树形结构</li><li style=\"text-align: start;\">保持组织架构的完整性</li></ul><h3 style=\"text-align: start;\">4. 编辑部门</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在部门列表中找到需要修改的记录</li><li style=\"text-align: start;\">点击该记录“操作”列中的 “编辑” 按钮</li><li style=\"text-align: start;\">在弹出的编辑表单中修改信息：</li></ol><p style=\"text-align: start;\"><strong>可修改字段</strong>：</p><ul><li style=\"text-align: start;\">部门名称：可修改（同级部门中需保持唯一）</li><li style=\"text-align: start;\">部门负责人：可从员工列表中重新选择或清空</li><li style=\"text-align: start;\">部门排序：可修改排序值</li></ul><p style=\"text-align: start;\"><strong>不可修改字段</strong>：</p><ul><li style=\"text-align: start;\">上级部门：创建后不可直接修改（需通过特殊流程）</li><li style=\"text-align: start;\">创建时间：系统自动记录，不可修改</li><li style=\"text-align: start;\">部门编码（如有）：系统生成，不可修改</li></ul><ol><li style=\"text-align: start;\">点击 “确定” 完成修改</li><li style=\"text-align: start;\">点击 “取消” 放弃修改</li></ol><h3 style=\"text-align: start;\">5. 删除部门</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在部门列表中找到目标记录</li><li style=\"text-align: start;\">点击“操作”列中的 “删除” 按钮</li><li style=\"text-align: start;\">系统弹出确认对话框</li><li style=\"text-align: start;\">确认后删除该部门</li></ol><p style=\"text-align: start;\"><strong>删除规则</strong>：</p><ul><li style=\"text-align: start;\">只能删除没有下级部门的节点</li><li style=\"text-align: start;\">如果部门下有子部门，必须先删除所有子部门</li><li style=\"text-align: start;\">部门下有员工时不可删除</li><li style=\"text-align: start;\">部门被业务模块引用时不可删除（如权限分配、工作流等）</li><li style=\"text-align: start;\">删除操作不可逆，请谨慎操作</li></ul><h3 style=\"text-align: start;\">6. 部门层级管理</h3><h4 style=\"text-align: start;\">6.1 层级结构示例</h4><p><span style=\"color: rgb(15, 17, 21); font-size: 12px; font-family: Menlo, Monaco, Consolas, &quot;Cascadia Mono&quot;, &quot;Ubuntu Mono&quot;, &quot;DejaVu Sans Mono&quot;, &quot;Liberation Mono&quot;, &quot;JetBrains Mono&quot;, &quot;Fira Code&quot;, Cousine, &quot;Roboto Mono&quot;, &quot;Courier New&quot;, Courier, sans-serif, system-ui;\">text</span></p><pre><code >湖北长江电影集团有限责任公司（顶级部门）\n├── 票务分公司\n│   ├── 运营部（示例）\n│   ├── 技术部（示例）\n│   └── 市场部（示例）\n├── 集团公共事业管理中心\n├── 湖北银兴院线影业有限责任公司\n├── 湖北电影制片有限责任公司\n└── 其他子公司/部门</code></pre><h4 style=\"text-align: start;\">6.2 层级深度</h4><ul><li style=\"text-align: start;\">系统支持无限级部门层级（理论上）</li><li style=\"text-align: start;\">实际应用中建议不超过5级，保证管理效率</li><li style=\"text-align: start;\">层级过深可能影响权限分配和数据处理效率</li></ul><h3 style=\"text-align: start;\">7. 排序功能</h3><h4 style=\"text-align: start;\">7.1 排序规则</h4><ul><li style=\"text-align: start;\">排序值越大，显示越靠前</li><li style=\"text-align: start;\">同级部门按排序值降序排列</li><li style=\"text-align: start;\">默认排序值为0</li><li style=\"text-align: start;\">支持负数排序值</li></ul><h4 style=\"text-align: start;\">7.2 排序策略</h4><ul><li style=\"text-align: start;\">重要部门：设置较大排序值（如100、200）</li><li style=\"text-align: start;\">常规部门：设置中等排序值（如10、20、30）</li><li style=\"text-align: start;\">临时部门：设置较小排序值或负值</li><li style=\"text-align: start;\">预留空间：排序值设置间隔，方便后续调整</li></ul><h2 style=\"text-align: start;\">四、部门负责人管理</h2><h3 style=\"text-align: start;\">1. 负责人权限</h3><ul><li style=\"text-align: start;\">部门负责人自动获得该部门的管理权限</li><li style=\"text-align: start;\">可查看部门下所有员工信息</li><li style=\"text-align: start;\">可审批部门相关业务流程</li><li style=\"text-align: start;\">可分配部门内权限</li></ul><h3 style=\"text-align: start;\">2. 负责人变更</h3><ul><li style=\"text-align: start;\">通过编辑部门信息修改负责人</li><li style=\"text-align: start;\">变更后原负责人权限自动撤销</li><li style=\"text-align: start;\">新负责人权限自动生效</li><li style=\"text-align: start;\">建议及时更新负责人信息，确保权限准确</li></ul><h3 style=\"text-align: start;\">3. 负责人空缺处理</h3><ul><li style=\"text-align: start;\">部门可暂时不设置负责人</li><li style=\"text-align: start;\">负责人空缺时，上级部门负责人代管</li><li style=\"text-align: start;\">或由系统管理员指定临时负责人</li></ul><h2 style=\"text-align: start;\">五、业务应用场景</h2><h3 style=\"text-align: start;\">1. 员工归属管理</h3><ul><li style=\"text-align: start;\">每个员工必须归属于一个具体部门</li><li style=\"text-align: start;\">员工列表可按部门筛选</li><li style=\"text-align: start;\">部门负责人可管理下属员工</li></ul><h3 style=\"text-align: start;\">2. 权限分配</h3><ul><li style=\"text-align: start;\">按部门分配系统功能权限</li><li style=\"text-align: start;\">部门级数据权限控制</li><li style=\"text-align: start;\">工作流程按部门配置</li></ul><h3 style=\"text-align: start;\">3. 组织架构展示</h3><ul><li style=\"text-align: start;\">企业组织架构图生成</li><li style=\"text-align: start;\">部门人员统计报表</li><li style=\"text-align: start;\">部门层级关系展示</li></ul><h3 style=\"text-align: start;\">4. 业务流程</h3><ul><li style=\"text-align: start;\">按部门配置审批流程</li><li style=\"text-align: start;\">部门间协作流程管理</li><li style=\"text-align: start;\">跨部门工作流支持</li></ul><h2 style=\"text-align: start;\">六、数据规范与约束</h2><h3 style=\"text-align: start;\">1. 命名规范</h3><ul><li style=\"text-align: start;\">部门名称：使用公司官方名称</li><li style=\"text-align: start;\">名称长度：建议不超过50个字符</li><li style=\"text-align: start;\">避免使用特殊字符和空格</li></ul><h3 style=\"text-align: start;\">2. 层级约束</h3><ul><li style=\"text-align: start;\">部门必须明确上级部门（顶级部门除外）</li><li style=\"text-align: start;\">不允许循环引用（A的上级是B，B的上级是A）</li><li style=\"text-align: start;\">层级深度应有合理限制</li></ul><h3 style=\"text-align: start;\">3. 数据一致性</h3><ul><li style=\"text-align: start;\">部门信息与其他模块（员工、角色、权限）保持同步</li><li style=\"text-align: start;\">部门变更时，相关数据应自动更新或提示处理</li><li style=\"text-align: start;\">定期检查部门数据的完整性</li></ul><p><br></p>', '', 10, 5, 1, '长影票务', '2026-01-30 11:05:08', '2026-01-28 16:06:32');
INSERT INTO `t_help_doc` VALUES (44, 26, '员工管理模块', '\n一、模块概述\n员工管理模块是票务分公司小程序系统的人员管理核心，负责公司所有员工的账户创建、信息维护、状态管理和组织归属。本模块确保员工信息的准确性和及时性，为权限分配、工作流程和组织管理提供基础数据支持。\n二、模块入口\n访问路径：登录Web管理系统 → 左侧导航栏 → 组织架构 → 员工管理\n权限要求：需具备“员工管理员”或“组织架构管理员”及以上权限角色\n三、功能详解\n1. 员工列表与查询\n1.1 列表展示\n以部门分组形式展示所有员工信息，左侧为部门树，右侧为员工列表\n每页默认显示10条数据，支持分页选择\n显示字段包括：姓名（员工姓名）性别（男/女）登录账号（系统登录用户名）手机号（联系电话）邮箱（工作邮箱）状态（启用/禁用）职务（员工职务名称）角色（系统角色，如超管、核销员等）部门（完整部门路径）操作（编辑/重置密码/禁用）\n1.2 查询筛选\n提供多种查询方式：\n部门筛选：左侧部门树：点击部门名称筛选该部门员工部门搜索框：输入部门名称模糊查询\n状态筛选：标签选择（全部/启用/禁用）\n综合查询：输入姓名、手机号或登录账号进行模糊查询\n操作按钮：\n查询：根据筛选条件刷新员工列表\n重置：清空所有筛选条件，显示全部员工\n2. 添加员工\n操作流程：\n点击列表右上方的 “添加成员” 按钮\n在弹出的“添加”表单中填写以下信息：\n2.1 基本信息\n姓名（必填）：输入员工真实姓名\n性别（必填）：单选选择（男/女）\n手机号（必填）：输入11位手机号码系统验证格式正确性手机号将作为重要联系方式和验证方式\n邮箱（必填）：输入工作邮箱地址系统验证邮箱格式用于系统通知和密码找回\n2.2 账户信息\n登录名（必填）：输入系统登录用户名命名规则：建议使用英文或拼音，便于记忆唯一性要求：全系统唯一，不可重复长度限制：4-20个字符\n初始密码：系统自动生成随机密码密码规则：包含大小写字母、数字、特殊字符密码长度：8-16位首次登录需修改密码\n2.3 组织信息\n部门（必填）：从下拉菜单中选择所属部门支持部门树形选择员工必须归属于一个具体部门\n状态（必填）：单选选择启用：账户正常使用，可登录系统禁用：账户暂停使用，无法登录\n保存选项：取消：放弃添加操作保存：保存当前员工信息并返回列表保存并继续添加：保存当前员工信息，继续添加下一个员工\n注意：\n带 * 号为必填项\n手机号和登录名必须唯一\n初始密码将通过短信或邮件发送给员工\n建议在添加员工前准备好所有必要信息\n3. 编辑员工信息\n操作流程：\n在员工列表中找到需要修改的员工\n点击该员工“操作”列中的 “编辑” 按钮\n在弹出的编辑表单中修改信息\n可修改字段：\n姓名：可修改\n性别：可修改\n手机号：可修改（需验证唯一性）\n邮箱：可修改（需验证格式）\n部门：可修改（从部门列表中选择）\n状态：可修改（启用/禁用）\n不可直接修改字段：\n登录名：创建后不可修改\n初始密码：不可查看，只能重置\n点击 “保存” 完成修改\n4. 重置密码\n操作流程：\n在员工列表中找到需要重置密码的员工\n点击该员工“操作”列中的 “重置密码” 按钮\n系统弹出确认对话框\n确认后系统生成新随机密码\n新密码将通过短信或邮件发送给员工\n注意事项：\n只有管理员可以重置密码\n重置后员工需使用新密码登录\n建议员工首次登录后立即修改密码\n密码重置记录会记入操作日志\n5. 启用/禁用账户\n操作流程：\n在员工列表中找到目标员工\n点击该员工“操作”列中的 “禁用” 按钮（启用状态时）或 “启用” 按钮（禁用状态时）\n系统弹出确认对话框\n确认后更改账户状态\n状态影响：\n启用状态：员工可正常登录系统可执行分配的权限任务接收系统通知\n禁用状态：员工无法登录系统所有权限暂停当前登录会话强制退出可随时重新启用\n6. 调整部门\n批量调整操作流程：\n在员工列表中勾选一个或多个员工\n点击列表上方的 “调整部门” 按钮\n在弹出的部门选择框中选择目标部门\n点击 “提交” 完成部门调整\n点击 “取消” 放弃操作\n单个调整操作流程：\n通过编辑员工信息修改部门字段\n保存后完成部门调整\n调整影响：\n员工部门变更后，相关权限可能受影响\n部门负责人权限自动调整\n工作流程审批路径可能变化\n建议调整后检查相关设置\n7. 批量删除\n操作流程：\n在员工列表中勾选一个或多个员工\n点击列表上方的 “批量删除” 按钮\n系统弹出确认对话框，显示待删除员工数量\n确认后批量删除选中员工\n删除规则：\n只能删除“禁用”状态的员工\n员工有未完成的业务记录时不可删除\n删除前系统会检查依赖关系\n删除操作不可逆，请谨慎操作\n删除影响：\n员工账户永久删除，无法恢复\n员工历史操作记录保留（审计需要）\n相关权限分配自动清理\n工作流程中的待办任务需重新分配\n四、员工状态管理\n1. 状态类型\n启用：正常在职员工\n禁用：离职、调岗或暂停使用的员工\n锁定：密码错误次数超限自动锁定（系统自动管理）\n2. 状态流转\ntext\n新建 → 启用（正常使用） → 禁用（离职/调岗） → [可重新启用] → 删除（清理数据）\n3. 状态同步\n员工状态变更时，系统自动同步到相关模块：禁用 → 所有系统权限暂停启用 → 恢复原有权限删除 → 清理所有权限和数据关联\n五、权限与角色管理\n1. 角色分配\n员工创建时默认不分配角色\n需在“角色管理”模块为员工分配角色\n角色决定员工的系统功能权限\n2. 权限继承\n部门负责人自动获得部门管理权限\n上级部门可查看下级部门员工信息\n按角色分配的权限优先于部门权限\n3. 权限审核\n定期审核员工权限分配\n离职员工及时禁用账户\n调岗员工及时调整权限\n六、数据安全与隐私\n1. 敏感信息保护\n密码加密存储，不可逆查看\n手机号部分脱敏显示\n操作日志记录所有敏感操作\n2. 数据备份\n员工信息每日自动备份\n离职员工数据保留期限：根据公司政策设定\n数据导出需审批授权\n3. 访问控制\n员工只能查看本部门及下级部门员工\n敏感操作需管理员权限\n操作日志完整记录\n', '<p><br></p><h2 style=\"text-align: start;\">一、模块概述</h2><p style=\"text-align: start;\">员工管理模块是票务分公司小程序系统的人员管理核心，负责公司所有员工的账户创建、信息维护、状态管理和组织归属。本模块确保员工信息的准确性和及时性，为权限分配、工作流程和组织管理提供基础数据支持。</p><h2 style=\"text-align: start;\">二、模块入口</h2><ul><li style=\"text-align: start;\">访问路径：登录Web管理系统 → 左侧导航栏 → 组织架构 → 员工管理</li><li style=\"text-align: start;\">权限要求：需具备“员工管理员”或“组织架构管理员”及以上权限角色</li></ul><h2 style=\"text-align: start;\">三、功能详解</h2><h3 style=\"text-align: start;\">1. 员工列表与查询</h3><h4 style=\"text-align: start;\">1.1 列表展示</h4><ul><li style=\"text-align: start;\">以部门分组形式展示所有员工信息，左侧为部门树，右侧为员工列表</li><li style=\"text-align: start;\">每页默认显示10条数据，支持分页选择</li><li style=\"text-align: start;\">显示字段包括：姓名（员工姓名）性别（男/女）登录账号（系统登录用户名）手机号（联系电话）邮箱（工作邮箱）状态（启用/禁用）职务（员工职务名称）角色（系统角色，如超管、核销员等）部门（完整部门路径）操作（编辑/重置密码/禁用）</li></ul><h4 style=\"text-align: start;\">1.2 查询筛选</h4><p style=\"text-align: start;\">提供多种查询方式：</p><ul><li style=\"text-align: start;\">部门筛选：左侧部门树：点击部门名称筛选该部门员工部门搜索框：输入部门名称模糊查询</li><li style=\"text-align: start;\">状态筛选：标签选择（全部/启用/禁用）</li><li style=\"text-align: start;\">综合查询：输入姓名、手机号或登录账号进行模糊查询</li></ul><p style=\"text-align: start;\">操作按钮：</p><ul><li style=\"text-align: start;\">查询：根据筛选条件刷新员工列表</li><li style=\"text-align: start;\">重置：清空所有筛选条件，显示全部员工</li></ul><h3 style=\"text-align: start;\">2. 添加员工</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">点击列表右上方的 “添加成员” 按钮</li><li style=\"text-align: start;\">在弹出的“添加”表单中填写以下信息：</li></ol><h4 style=\"text-align: start;\">2.1 基本信息</h4><ul><li style=\"text-align: start;\">姓名（必填）：输入员工真实姓名</li><li style=\"text-align: start;\">性别（必填）：单选选择（男/女）</li><li style=\"text-align: start;\">手机号（必填）：输入11位手机号码系统验证格式正确性手机号将作为重要联系方式和验证方式</li><li style=\"text-align: start;\">邮箱（必填）：输入工作邮箱地址系统验证邮箱格式用于系统通知和密码找回</li></ul><h4 style=\"text-align: start;\">2.2 账户信息</h4><ul><li style=\"text-align: start;\">登录名（必填）：输入系统登录用户名命名规则：建议使用英文或拼音，便于记忆唯一性要求：全系统唯一，不可重复长度限制：4-20个字符</li><li style=\"text-align: start;\">初始密码：系统自动生成随机密码密码规则：包含大小写字母、数字、特殊字符密码长度：8-16位首次登录需修改密码</li></ul><h4 style=\"text-align: start;\">2.3 组织信息</h4><ul><li style=\"text-align: start;\">部门（必填）：从下拉菜单中选择所属部门支持部门树形选择员工必须归属于一个具体部门</li><li style=\"text-align: start;\">状态（必填）：单选选择启用：账户正常使用，可登录系统禁用：账户暂停使用，无法登录</li></ul><ol><li style=\"text-align: start;\">保存选项：取消：放弃添加操作保存：保存当前员工信息并返回列表保存并继续添加：保存当前员工信息，继续添加下一个员工</li></ol><p style=\"text-align: start;\"><strong>注意</strong>：</p><ul><li style=\"text-align: start;\">带 * 号为必填项</li><li style=\"text-align: start;\">手机号和登录名必须唯一</li><li style=\"text-align: start;\">初始密码将通过短信或邮件发送给员工</li><li style=\"text-align: start;\">建议在添加员工前准备好所有必要信息</li></ul><h3 style=\"text-align: start;\">3. 编辑员工信息</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在员工列表中找到需要修改的员工</li><li style=\"text-align: start;\">点击该员工“操作”列中的 “编辑” 按钮</li><li style=\"text-align: start;\">在弹出的编辑表单中修改信息</li></ol><p style=\"text-align: start;\"><strong>可修改字段</strong>：</p><ul><li style=\"text-align: start;\">姓名：可修改</li><li style=\"text-align: start;\">性别：可修改</li><li style=\"text-align: start;\">手机号：可修改（需验证唯一性）</li><li style=\"text-align: start;\">邮箱：可修改（需验证格式）</li><li style=\"text-align: start;\">部门：可修改（从部门列表中选择）</li><li style=\"text-align: start;\">状态：可修改（启用/禁用）</li></ul><p style=\"text-align: start;\"><strong>不可直接修改字段</strong>：</p><ul><li style=\"text-align: start;\">登录名：创建后不可修改</li><li style=\"text-align: start;\">初始密码：不可查看，只能重置</li></ul><ol><li style=\"text-align: start;\">点击 “保存” 完成修改</li></ol><h3 style=\"text-align: start;\">4. 重置密码</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在员工列表中找到需要重置密码的员工</li><li style=\"text-align: start;\">点击该员工“操作”列中的 “重置密码” 按钮</li><li style=\"text-align: start;\">系统弹出确认对话框</li><li style=\"text-align: start;\">确认后系统生成新随机密码</li><li style=\"text-align: start;\">新密码将通过短信或邮件发送给员工</li></ol><p style=\"text-align: start;\"><strong>注意事项</strong>：</p><ul><li style=\"text-align: start;\">只有管理员可以重置密码</li><li style=\"text-align: start;\">重置后员工需使用新密码登录</li><li style=\"text-align: start;\">建议员工首次登录后立即修改密码</li><li style=\"text-align: start;\">密码重置记录会记入操作日志</li></ul><h3 style=\"text-align: start;\">5. 启用/禁用账户</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在员工列表中找到目标员工</li><li style=\"text-align: start;\">点击该员工“操作”列中的 “禁用” 按钮（启用状态时）或 “启用” 按钮（禁用状态时）</li><li style=\"text-align: start;\">系统弹出确认对话框</li><li style=\"text-align: start;\">确认后更改账户状态</li></ol><p style=\"text-align: start;\"><strong>状态影响</strong>：</p><ul><li style=\"text-align: start;\">启用状态：员工可正常登录系统可执行分配的权限任务接收系统通知</li><li style=\"text-align: start;\">禁用状态：员工无法登录系统所有权限暂停当前登录会话强制退出可随时重新启用</li></ul><h3 style=\"text-align: start;\">6. 调整部门</h3><p style=\"text-align: start;\"><strong>批量调整操作流程</strong>：</p><ol><li style=\"text-align: start;\">在员工列表中勾选一个或多个员工</li><li style=\"text-align: start;\">点击列表上方的 “调整部门” 按钮</li><li style=\"text-align: start;\">在弹出的部门选择框中选择目标部门</li><li style=\"text-align: start;\">点击 “提交” 完成部门调整</li><li style=\"text-align: start;\">点击 “取消” 放弃操作</li></ol><p style=\"text-align: start;\"><strong>单个调整操作流程</strong>：</p><ol><li style=\"text-align: start;\">通过编辑员工信息修改部门字段</li><li style=\"text-align: start;\">保存后完成部门调整</li></ol><p style=\"text-align: start;\"><strong>调整影响</strong>：</p><ul><li style=\"text-align: start;\">员工部门变更后，相关权限可能受影响</li><li style=\"text-align: start;\">部门负责人权限自动调整</li><li style=\"text-align: start;\">工作流程审批路径可能变化</li><li style=\"text-align: start;\">建议调整后检查相关设置</li></ul><h3 style=\"text-align: start;\">7. 批量删除</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在员工列表中勾选一个或多个员工</li><li style=\"text-align: start;\">点击列表上方的 “批量删除” 按钮</li><li style=\"text-align: start;\">系统弹出确认对话框，显示待删除员工数量</li><li style=\"text-align: start;\">确认后批量删除选中员工</li></ol><p style=\"text-align: start;\"><strong>删除规则</strong>：</p><ul><li style=\"text-align: start;\">只能删除“禁用”状态的员工</li><li style=\"text-align: start;\">员工有未完成的业务记录时不可删除</li><li style=\"text-align: start;\">删除前系统会检查依赖关系</li><li style=\"text-align: start;\">删除操作不可逆，请谨慎操作</li></ul><p style=\"text-align: start;\"><strong>删除影响</strong>：</p><ul><li style=\"text-align: start;\">员工账户永久删除，无法恢复</li><li style=\"text-align: start;\">员工历史操作记录保留（审计需要）</li><li style=\"text-align: start;\">相关权限分配自动清理</li><li style=\"text-align: start;\">工作流程中的待办任务需重新分配</li></ul><h2 style=\"text-align: start;\">四、员工状态管理</h2><h3 style=\"text-align: start;\">1. 状态类型</h3><ul><li style=\"text-align: start;\">启用：正常在职员工</li><li style=\"text-align: start;\">禁用：离职、调岗或暂停使用的员工</li><li style=\"text-align: start;\">锁定：密码错误次数超限自动锁定（系统自动管理）</li></ul><h3 style=\"text-align: start;\">2. 状态流转</h3><p><span style=\"color: rgb(15, 17, 21); font-size: 12px; font-family: Menlo, Monaco, Consolas, &quot;Cascadia Mono&quot;, &quot;Ubuntu Mono&quot;, &quot;DejaVu Sans Mono&quot;, &quot;Liberation Mono&quot;, &quot;JetBrains Mono&quot;, &quot;Fira Code&quot;, Cousine, &quot;Roboto Mono&quot;, &quot;Courier New&quot;, Courier, sans-serif, system-ui;\">text</span></p><pre><code >新建 → 启用（正常使用） → 禁用（离职/调岗） → [可重新启用] → 删除（清理数据）</code></pre><h3 style=\"text-align: start;\">3. 状态同步</h3><ul><li style=\"text-align: start;\">员工状态变更时，系统自动同步到相关模块：禁用 → 所有系统权限暂停启用 → 恢复原有权限删除 → 清理所有权限和数据关联</li></ul><h2 style=\"text-align: start;\">五、权限与角色管理</h2><h3 style=\"text-align: start;\">1. 角色分配</h3><ul><li style=\"text-align: start;\">员工创建时默认不分配角色</li><li style=\"text-align: start;\">需在“角色管理”模块为员工分配角色</li><li style=\"text-align: start;\">角色决定员工的系统功能权限</li></ul><h3 style=\"text-align: start;\">2. 权限继承</h3><ul><li style=\"text-align: start;\">部门负责人自动获得部门管理权限</li><li style=\"text-align: start;\">上级部门可查看下级部门员工信息</li><li style=\"text-align: start;\">按角色分配的权限优先于部门权限</li></ul><h3 style=\"text-align: start;\">3. 权限审核</h3><ul><li style=\"text-align: start;\">定期审核员工权限分配</li><li style=\"text-align: start;\">离职员工及时禁用账户</li><li style=\"text-align: start;\">调岗员工及时调整权限</li></ul><h2 style=\"text-align: start;\">六、数据安全与隐私</h2><h3 style=\"text-align: start;\">1. 敏感信息保护</h3><ul><li style=\"text-align: start;\">密码加密存储，不可逆查看</li><li style=\"text-align: start;\">手机号部分脱敏显示</li><li style=\"text-align: start;\">操作日志记录所有敏感操作</li></ul><h3 style=\"text-align: start;\">2. 数据备份</h3><ul><li style=\"text-align: start;\">员工信息每日自动备份</li><li style=\"text-align: start;\">离职员工数据保留期限：根据公司政策设定</li><li style=\"text-align: start;\">数据导出需审批授权</li></ul><h3 style=\"text-align: start;\">3. 访问控制</h3><ul><li style=\"text-align: start;\">员工只能查看本部门及下级部门员工</li><li style=\"text-align: start;\">敏感操作需管理员权限</li><li style=\"text-align: start;\">操作日志完整记录</li></ul><p><br></p>', '', 1, 7, 1, '长影票务', '2026-02-12 14:45:15', '2026-01-28 16:06:53');
INSERT INTO `t_help_doc` VALUES (45, 26, '职务管理模块', '一、模块概述\n职务管理模块是票务分公司小程序系统的人员组织管理组件，负责定义和维护公司内部的职务体系。本模块通过建立标准化的职务层级和职级体系，为员工岗位分配、权限配置、薪资管理和职业发展提供基础框架，确保组织架构的规范化和系统化。\n二、模块入口\n访问路径：登录Web管理系统 → 左侧导航栏 → 组织架构 → 职务管理\n权限要求：需具备“组织架构管理员”或“人力资源专员”及以上权限角色\n三、功能详解\n1. 职务列表与查询\n\n1.1 列表展示\n以表格形式展示所有职务信息\n每页默认显示10条数据，支持分页选择\n显示字段包括：职务名称（如：核销员、票务运营）职级（职务级别编号）排序（显示顺序权重）备注（职务说明）创建时间（职务创建时间）操作（编辑/删除）\n1.2 查询功能\n关键字查询：支持模糊查询职务名称或备注信息\n查询按钮：根据关键字刷新职务列表\n重置按钮：清空查询条件，显示全部职务\n2. 新建职务\n\n操作流程：\n点击列表右上方的 “+新建” 按钮\n在弹出的“添加”表单中填写以下信息：\n2.1 基本信息\n职务名称（必填）：输入职务的正式名称命名规范：使用公司标准的职务命名长度限制：建议不超过20个字符唯一性要求：职务名称需在公司内唯一示例：核销员、票务运营、影城经理、区域总监等\n2.2 管理信息\n职级（必填）：输入职务级别编号编号规则：通常使用数字表示层级，数字越小级别越高示例：0（高层管理）、1（中层管理）、2（基层管理）、3（普通员工）关联性：职级与薪资等级、权限范围相关联\n排序（必填）：输入显示顺序值排序规则：数值越小显示越靠前默认值：0建议设置：为后续新增职务预留排序空间\n备注（可选）：填写职务的详细说明内容建议：职责描述、任职要求、发展路径等长度限制：建议不超过200字\n保存选项：取消：放弃添加操作，返回职务列表保存：保存职务信息，返回列表页面\n注意：\n职务名称必须唯一，不可重复\n职级编号建议与公司职级体系保持一致\n排序值影响职务在列表和下拉选择中的显示顺序\n3. 编辑职务\n操作流程：\n在职务列表中找到需要修改的职务\n点击该职务“操作”列中的 “编辑” 按钮\n在弹出的编辑表单中修改信息：\n可修改字段：\n职务名称：可修改（需保持唯一性）\n职级：可修改（注意关联影响）\n排序：可修改\n备注：可修改\n不可修改字段：\n创建时间：系统自动记录，不可修改\n创建人：系统记录，不可修改\n点击 “保存” 完成修改\n点击 “取消” 放弃修改\n编辑注意事项：\n职务名称修改后，相关员工的职务信息会自动更新\n职级修改可能影响权限分配和薪资等级\n建议在非工作时间进行重要职务信息修改\n4. 删除职务\n4.1 单个删除\n操作流程：\n在职务列表中找到目标职务\n点击“操作”列中的 “删除” 按钮\n系统弹出确认对话框\n确认后删除该职务\n4.2 批量删除\n操作流程：\n在职务列表中勾选一个或多个职务\n点击列表上方的 “批量删除” 按钮\n系统弹出确认对话框，显示待删除职务数量\n确认后批量删除选中职务\n删除规则：\n职务下有员工关联时不可删除\n职务被角色或权限系统引用时不可删除\n系统内置职务（如核销员）不可删除\n删除操作不可逆，请谨慎操作\n删除影响：\n职务删除后，关联员工的职务字段将清空\n相关权限配置可能需要重新调整\n建议删除前迁移关联员工到其他职务\n5. 职务排序管理\n5.1 排序规则\n排序值：数值越小，显示越靠前\n同级排序：相同职级的职务按排序值升序排列\n默认排序：新职务默认排序值为0\n5.2 排序调整策略\n高层职务：设置较小排序值（如1、2、3）\n中层职务：设置中等排序值（如10、20、30）\n基层职务：设置较大排序值（如100、200、300）\n预留空间：排序值设置间隔，方便后续插入', '<h2 style=\"text-align: start;\">一、模块概述</h2><p style=\"text-align: start;\">职务管理模块是票务分公司小程序系统的人员组织管理组件，负责定义和维护公司内部的职务体系。本模块通过建立标准化的职务层级和职级体系，为员工岗位分配、权限配置、薪资管理和职业发展提供基础框架，确保组织架构的规范化和系统化。</p><h2 style=\"text-align: start;\">二、模块入口</h2><ul><li style=\"text-align: start;\">访问路径：登录Web管理系统 → 左侧导航栏 → 组织架构 → 职务管理</li><li style=\"text-align: start;\">权限要求：需具备“组织架构管理员”或“人力资源专员”及以上权限角色</li></ul><h2 style=\"text-align: start;\">三、功能详解</h2><h3 style=\"text-align: start;\">1. 职务列表与查询</h3><p><img src=\"https://www.hbcypw.com/api/public/common/c2791f24791f43fa9aa12e7573631006_20260130095332.png\" alt=\"\" data-href=\"[object Object]\" width=\"\" height=\"\" style=\"width: 990.39px;height: 333.96px;\"/></p><h4 style=\"text-align: start;\">1.1 列表展示</h4><ul><li style=\"text-align: start;\">以表格形式展示所有职务信息</li><li style=\"text-align: start;\">每页默认显示10条数据，支持分页选择</li><li style=\"text-align: start;\">显示字段包括：职务名称（如：核销员、票务运营）职级（职务级别编号）排序（显示顺序权重）备注（职务说明）创建时间（职务创建时间）操作（编辑/删除）</li></ul><h4 style=\"text-align: start;\">1.2 查询功能</h4><ul><li style=\"text-align: start;\">关键字查询：支持模糊查询职务名称或备注信息</li><li style=\"text-align: start;\">查询按钮：根据关键字刷新职务列表</li><li style=\"text-align: start;\">重置按钮：清空查询条件，显示全部职务</li></ul><h3 style=\"text-align: start;\">2. 新建职务</h3><p><img src=\"https://www.hbcypw.com/api/public/common/503c26067f9a45269e33b822ee2c413a_20260130095351.png\" alt=\"\" data-href=\"[object Object]\" width=\"\" height=\"\" style=\"width: 554.80px;height: 431.13px;\"/></p><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">点击列表右上方的 “+新建” 按钮</li><li style=\"text-align: start;\">在弹出的“添加”表单中填写以下信息：</li></ol><h4 style=\"text-align: start;\">2.1 基本信息</h4><ul><li style=\"text-align: start;\">职务名称（必填）：输入职务的正式名称命名规范：使用公司标准的职务命名长度限制：建议不超过20个字符唯一性要求：职务名称需在公司内唯一示例：核销员、票务运营、影城经理、区域总监等</li></ul><h4 style=\"text-align: start;\">2.2 管理信息</h4><ul><li style=\"text-align: start;\">职级（必填）：输入职务级别编号编号规则：通常使用数字表示层级，数字越小级别越高示例：0（高层管理）、1（中层管理）、2（基层管理）、3（普通员工）关联性：职级与薪资等级、权限范围相关联</li><li style=\"text-align: start;\">排序（必填）：输入显示顺序值排序规则：数值越小显示越靠前默认值：0建议设置：为后续新增职务预留排序空间</li><li style=\"text-align: start;\">备注（可选）：填写职务的详细说明内容建议：职责描述、任职要求、发展路径等长度限制：建议不超过200字</li></ul><ol><li style=\"text-align: start;\">保存选项：取消：放弃添加操作，返回职务列表保存：保存职务信息，返回列表页面</li></ol><p style=\"text-align: start;\"><strong>注意</strong>：</p><ul><li style=\"text-align: start;\">职务名称必须唯一，不可重复</li><li style=\"text-align: start;\">职级编号建议与公司职级体系保持一致</li><li style=\"text-align: start;\">排序值影响职务在列表和下拉选择中的显示顺序</li></ul><h3 style=\"text-align: start;\">3. 编辑职务</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在职务列表中找到需要修改的职务</li><li style=\"text-align: start;\">点击该职务“操作”列中的 “编辑” 按钮</li><li style=\"text-align: start;\">在弹出的编辑表单中修改信息：</li></ol><p style=\"text-align: start;\"><strong>可修改字段</strong>：</p><ul><li style=\"text-align: start;\">职务名称：可修改（需保持唯一性）</li><li style=\"text-align: start;\">职级：可修改（注意关联影响）</li><li style=\"text-align: start;\">排序：可修改</li><li style=\"text-align: start;\">备注：可修改</li></ul><p style=\"text-align: start;\"><strong>不可修改字段</strong>：</p><ul><li style=\"text-align: start;\">创建时间：系统自动记录，不可修改</li><li style=\"text-align: start;\">创建人：系统记录，不可修改</li></ul><ol><li style=\"text-align: start;\">点击 “保存” 完成修改</li><li style=\"text-align: start;\">点击 “取消” 放弃修改</li></ol><p style=\"text-align: start;\"><strong>编辑注意事项</strong>：</p><ul><li style=\"text-align: start;\">职务名称修改后，相关员工的职务信息会自动更新</li><li style=\"text-align: start;\">职级修改可能影响权限分配和薪资等级</li><li style=\"text-align: start;\">建议在非工作时间进行重要职务信息修改</li></ul><h3 style=\"text-align: start;\">4. 删除职务</h3><h4 style=\"text-align: start;\">4.1 单个删除</h4><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在职务列表中找到目标职务</li><li style=\"text-align: start;\">点击“操作”列中的 “删除” 按钮</li><li style=\"text-align: start;\">系统弹出确认对话框</li><li style=\"text-align: start;\">确认后删除该职务</li></ol><h4 style=\"text-align: start;\">4.2 批量删除</h4><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在职务列表中勾选一个或多个职务</li><li style=\"text-align: start;\">点击列表上方的 “批量删除” 按钮</li><li style=\"text-align: start;\">系统弹出确认对话框，显示待删除职务数量</li><li style=\"text-align: start;\">确认后批量删除选中职务</li></ol><p style=\"text-align: start;\"><strong>删除规则</strong>：</p><ul><li style=\"text-align: start;\">职务下有员工关联时不可删除</li><li style=\"text-align: start;\">职务被角色或权限系统引用时不可删除</li><li style=\"text-align: start;\">系统内置职务（如核销员）不可删除</li><li style=\"text-align: start;\">删除操作不可逆，请谨慎操作</li></ul><p style=\"text-align: start;\"><strong>删除影响</strong>：</p><ul><li style=\"text-align: start;\">职务删除后，关联员工的职务字段将清空</li><li style=\"text-align: start;\">相关权限配置可能需要重新调整</li><li style=\"text-align: start;\">建议删除前迁移关联员工到其他职务</li></ul><h3 style=\"text-align: start;\">5. 职务排序管理</h3><h4 style=\"text-align: start;\">5.1 排序规则</h4><ul><li style=\"text-align: start;\">排序值：数值越小，显示越靠前</li><li style=\"text-align: start;\">同级排序：相同职级的职务按排序值升序排列</li><li style=\"text-align: start;\">默认排序：新职务默认排序值为0</li></ul><h4 style=\"text-align: start;\">5.2 排序调整策略</h4><ul><li style=\"text-align: start;\">高层职务：设置较小排序值（如1、2、3）</li><li style=\"text-align: start;\">中层职务：设置中等排序值（如10、20、30）</li><li style=\"text-align: start;\">基层职务：设置较大排序值（如100、200、300）</li><li style=\"text-align: start;\">预留空间：排序值设置间隔，方便后续插入</li></ul>', '', 3, 7, 1, '长影票务', '2026-01-30 11:00:55', '2026-01-28 16:07:12');
INSERT INTO `t_help_doc` VALUES (46, 26, '角色管理', '一、模块概述\n角色管理模块是票务分公司小程序系统的权限控制核心，负责定义和维护系统中的各类角色及其对应的权限集合。本模块通过角色-权限的分配机制，实现细粒度的功能权限和数据范围控制，确保不同职责的员工获得恰当的访问和操作权限，保障系统安全与数据隔离。\n二、模块入口\n访问路径：登录Web管理系统 → 左侧导航栏 → 组织架构 → 角色管理\n权限要求：需具备“系统管理员”或“超级管理员”角色\n三、功能详解\n1. 角色管理界面\n1.1 界面布局\n采用三标签页设计，清晰分离不同管理维度：\n角色-功能权限：配置角色可操作的功能模块及具体操作权限\n角色-数据范围：配置角色可访问的数据范围（如影城、区域等）\n角色-员工列表：查看和管理拥有该角色的员工\n1.2 角色列表\n左侧固定显示角色列表\n当前系统内置角色：核销员、管理端（可自定义添加更多）\n点击角色名称切换不同角色的配置\n2. 添加角色\n操作流程：\n点击角色列表上方的 “添加” 按钮\n在弹出的“添加角色”表单中填写以下信息：\n2.1 基本信息\n角色名称（必填）：输入角色名称（如“影城管理员”、“财务专员”等）命名规范：清晰表达角色职责长度限制：建议不超过20字符避免重复：角色名称需唯一\n角色编码（必填）：输入角色编码（如“CINEMA_MANAGER”、“FINANCE_STAFF”）编码规则：建议使用英文大写和下划线系统唯一：编码不可重复便于识别：编码应反映角色含义\n角色备注（可选）：填写角色说明、职责描述等长度限制：200字以内内容建议：说明角色适用人群和权限范围\n保存选项：取消：放弃添加操作提交：保存角色基本信息，进入权限配置界面\n3. 功能权限配置\n操作流程：\n在角色列表中选择需要配置的角色\n点击 “角色-功能权限” 标签页\n在权限树形结构中勾选需要的权限\n3.1 权限结构\n系统按模块组织权限树：\n影城管理\n├── 区域管理\n│   ├── 查询\n│   ├── 新增\n│   ├── 修改\n│   ├── 删除\n│   └── 批量删除\n├── 影城管理\n│   ├── 分配核销员\n│   ├── 查询\n│   ├── 批量删除\n│   ├── 更新状态\n│   ├── 添加\n│   ├── 编辑\n│   └── 删除\n└── 通知消息\n    ├── 通知公告\n    │   ├── 新建\n    │   ├── 编辑\n    │   ├── 删除\n    │   └── 查询\n    └── 消息管理\n\n票务管理\n├── 票种管理\n│   ├── 新增\n│   ├── 修改\n│   ├── 删除\n│   ├── 更新状态\n│   ├── 查询\n│   └── 查看\n├── 批次管理\n│   ├── 新增\n│   ├── 修改\n│   ├── 删除\n│   ├── 批量删除\n│   ├── 查询\n│   ├── 生成票券\n│   ├── 查看\n│   ├── 生成二维码\n│   ├── 导出票券\n│   └── 导出二维码\n├── 票券管理\n│   ├── 作废票券\n│   ├── 查看\n│   ├── 查询\n│   ├── 导出票券\n│   └── 重新生成票券二维码\n└── 统计分析\n    ├── 批次票券统计\n    ├── 核销员统计\n    └── 影院分析\n3.2 权限类型\n模块级权限：整个模块的访问权限（如“影城管理”）\n功能级权限：具体功能的操作权限（如“新增”、“修改”、“删除”）\n数据级权限：数据访问范围控制（在“角色-数据范围”中配置）\n3.3 配置建议\n最小权限原则：只分配必要的权限\n职责分离：不同角色权限互斥\n定期审核：定期检查权限分配的合理性\n4. 数据范围配置\n操作流程：\n在角色列表中选择需要配置的角色\n点击 “角色-数据范围” 标签页\n配置角色可访问的数据范围\n4.1 数据范围类型\n全部数据：可访问系统所有数据\n部门数据：只能访问本部门及下级部门数据\n个人数据：只能访问自己创建的数据\n指定范围：手动选择可访问的区域、影城等\n4.2 配置示例\n核销员：只能访问分配给自己影城的数据\n区域经理：可访问指定区域内所有影城数据\n财务专员：可访问所有财务相关数据\n5. 员工分配管理\n操作流程：\n在角色列表中选择需要管理的角色\n点击 “角色-员工列表” 标签页\n查看当前拥有该角色的员工列表\n5.1 员工列表功能\n搜索功能：通过姓名、手机号、登录账号快速查找员工\n列表展示：显示员工基本信息、所属部门、状态\n操作功能：移除：从角色中移除单个员工批量移除：批量移除选中的员工添加员工：为角色添加新员工\n5.2 添加员工到角色\n操作流程：\n点击 “添加员工” 按钮\n在弹出的员工选择界面中：可通过部门树筛选员工可通过搜索框快速查找支持多选员工\n选择员工后点击确认\n系统将为选中员工分配该角色\n5.3 移除员工从角色\n单个移除：\n在员工列表中找到目标员工\n点击该员工“操作”列中的 “移除” 按钮\n系统弹出确认对话框\n确认后从角色中移除该员工\n批量移除：\n勾选一个或多个员工\n点击 “批量移除” 按钮\n系统弹出确认对话框，显示待移除员工数量\n确认后批量移除选中员工\n6. 权限保存与应用\n操作流程：\n在功能权限或数据范围配置完成后\n点击页面下方的 “保存” 按钮\n系统保存权限配置并立即生效\n生效机制：\n权限保存后立即生效\n已登录员工的权限需要重新登录后生效\n系统记录权限变更日志\n7. 角色编辑与删除\n编辑角色：\n在角色列表中点击角色名称\n直接在各标签页中修改配置\n修改后点击保存\n删除角色：\n在角色管理界面提供删除功能（界面未展示但系统支持）\n删除前需确保没有员工使用该角色\n系统内置角色不可删除（如“核销员”）\n四、权限模型详解\n1. 权限继承机制\n员工权限 = 个人权限 + 角色权限 + 部门权限\n冲突解决：采用最大权限原则（如有任一来源授予权限则有效）\n否定权限：支持特殊场景下的权限排除\n2. 权限验证流程\n用户请求 → 系统验证用户状态 → 检查功能权限 → 检查数据权限 → 执行操作\n3. 权限缓存机制\n用户权限信息缓存在会话中\n权限变更后需重新登录或刷新缓存\n缓存时间可配置，默认30分钟\n五、内置角色说明\n1. 核销员\n职责描述：负责在影城现场核销票券\n核心权限：票券管理：查看、查询、重新生成二维码仅限于分配给自己影城的操作无后台管理权限\n适用人群：影城前台工作人员\n2. 管理端\n职责描述：系统管理员，拥有全面管理权限\n核心权限：所有模块的完整权限角色管理权限系统配置权限\n适用人群：IT管理员、系统运维人员\n六、最佳实践建议\n1. 角色设计原则\n职责明确：每个角色对应清晰的业务职责\n权限适度：避免权限过度集中或过于分散\n可扩展性：预留角色扩展空间，适应业务变化\n2. 权限分配策略\n按需分配：根据实际工作需要分配权限\n定期审查：每季度审查角色权限配置\n变更记录：记录所有权限变更操作\n3. 员工角色管理\n一人多角：支持员工拥有多个角色\n角色互斥：设置互斥角色，避免权限冲突\n离职清理：员工离职及时移除所有角色\n4. 安全控制措施\n敏感操作日志：记录所有权限相关操作\n权限变更审批：重要权限变更需审批流程\n定期权限审计：定期进行权限使用审计\n', '<h2 style=\"text-align: start;\">一、模块概述</h2><p style=\"text-align: start;\">角色管理模块是票务分公司小程序系统的权限控制核心，负责定义和维护系统中的各类角色及其对应的权限集合。本模块通过角色-权限的分配机制，实现细粒度的功能权限和数据范围控制，确保不同职责的员工获得恰当的访问和操作权限，保障系统安全与数据隔离。</p><h2 style=\"text-align: start;\">二、模块入口</h2><ul><li style=\"text-align: start;\">访问路径：登录Web管理系统 → 左侧导航栏 → 组织架构 → 角色管理</li><li style=\"text-align: start;\">权限要求：需具备“系统管理员”或“超级管理员”角色</li></ul><h2 style=\"text-align: start;\">三、功能详解</h2><h3 style=\"text-align: start;\">1. 角色管理界面</h3><h4 style=\"text-align: start;\">1.1 界面布局</h4><p style=\"text-align: start;\">采用三标签页设计，清晰分离不同管理维度：</p><ul><li style=\"text-align: start;\">角色-功能权限：配置角色可操作的功能模块及具体操作权限</li><li style=\"text-align: start;\">角色-数据范围：配置角色可访问的数据范围（如影城、区域等）</li><li style=\"text-align: start;\">角色-员工列表：查看和管理拥有该角色的员工</li></ul><h4 style=\"text-align: start;\">1.2 角色列表</h4><ul><li style=\"text-align: start;\">左侧固定显示角色列表</li><li style=\"text-align: start;\">当前系统内置角色：核销员、管理端（可自定义添加更多）</li><li style=\"text-align: start;\">点击角色名称切换不同角色的配置</li></ul><h3 style=\"text-align: start;\">2. 添加角色</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">点击角色列表上方的 “添加” 按钮</li><li style=\"text-align: start;\">在弹出的“添加角色”表单中填写以下信息：</li></ol><h4 style=\"text-align: start;\">2.1 基本信息</h4><ul><li style=\"text-align: start;\">角色名称（必填）：输入角色名称（如“影城管理员”、“财务专员”等）命名规范：清晰表达角色职责长度限制：建议不超过20字符避免重复：角色名称需唯一</li><li style=\"text-align: start;\">角色编码（必填）：输入角色编码（如“CINEMA_MANAGER”、“FINANCE_STAFF”）编码规则：建议使用英文大写和下划线系统唯一：编码不可重复便于识别：编码应反映角色含义</li><li style=\"text-align: start;\">角色备注（可选）：填写角色说明、职责描述等长度限制：200字以内内容建议：说明角色适用人群和权限范围</li></ul><ol><li style=\"text-align: start;\">保存选项：取消：放弃添加操作提交：保存角色基本信息，进入权限配置界面</li></ol><h3 style=\"text-align: start;\">3. 功能权限配置</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在角色列表中选择需要配置的角色</li><li style=\"text-align: start;\">点击 “角色-功能权限” 标签页</li><li style=\"text-align: start;\">在权限树形结构中勾选需要的权限</li></ol><h4 style=\"text-align: start;\">3.1 权限结构</h4><p style=\"text-align: start;\">系统按模块组织权限树：</p><pre><code >影城管理\n├── 区域管理\n│   ├── 查询\n│   ├── 新增\n│   ├── 修改\n│   ├── 删除\n│   └── 批量删除\n├── 影城管理\n│   ├── 分配核销员\n│   ├── 查询\n│   ├── 批量删除\n│   ├── 更新状态\n│   ├── 添加\n│   ├── 编辑\n│   └── 删除\n└── 通知消息\n    ├── 通知公告\n    │   ├── 新建\n    │   ├── 编辑\n    │   ├── 删除\n    │   └── 查询\n    └── 消息管理\n\n票务管理\n├── 票种管理\n│   ├── 新增\n│   ├── 修改\n│   ├── 删除\n│   ├── 更新状态\n│   ├── 查询\n│   └── 查看\n├── 批次管理\n│   ├── 新增\n│   ├── 修改\n│   ├── 删除\n│   ├── 批量删除\n│   ├── 查询\n│   ├── 生成票券\n│   ├── 查看\n│   ├── 生成二维码\n│   ├── 导出票券\n│   └── 导出二维码\n├── 票券管理\n│   ├── 作废票券\n│   ├── 查看\n│   ├── 查询\n│   ├── 导出票券\n│   └── 重新生成票券二维码\n└── 统计分析\n    ├── 批次票券统计\n    ├── 核销员统计\n    └── 影院分析</code></pre><h4 style=\"text-align: start;\">3.2 权限类型</h4><ul><li style=\"text-align: start;\">模块级权限：整个模块的访问权限（如“影城管理”）</li><li style=\"text-align: start;\">功能级权限：具体功能的操作权限（如“新增”、“修改”、“删除”）</li><li style=\"text-align: start;\">数据级权限：数据访问范围控制（在“角色-数据范围”中配置）</li></ul><h4 style=\"text-align: start;\">3.3 配置建议</h4><ul><li style=\"text-align: start;\">最小权限原则：只分配必要的权限</li><li style=\"text-align: start;\">职责分离：不同角色权限互斥</li><li style=\"text-align: start;\">定期审核：定期检查权限分配的合理性</li></ul><h3 style=\"text-align: start;\">4. 数据范围配置</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在角色列表中选择需要配置的角色</li><li style=\"text-align: start;\">点击 “角色-数据范围” 标签页</li><li style=\"text-align: start;\">配置角色可访问的数据范围</li></ol><h4 style=\"text-align: start;\">4.1 数据范围类型</h4><ul><li style=\"text-align: start;\">全部数据：可访问系统所有数据</li><li style=\"text-align: start;\">部门数据：只能访问本部门及下级部门数据</li><li style=\"text-align: start;\">个人数据：只能访问自己创建的数据</li><li style=\"text-align: start;\">指定范围：手动选择可访问的区域、影城等</li></ul><h4 style=\"text-align: start;\">4.2 配置示例</h4><ul><li style=\"text-align: start;\">核销员：只能访问分配给自己影城的数据</li><li style=\"text-align: start;\">区域经理：可访问指定区域内所有影城数据</li><li style=\"text-align: start;\">财务专员：可访问所有财务相关数据</li></ul><h3 style=\"text-align: start;\">5. 员工分配管理</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在角色列表中选择需要管理的角色</li><li style=\"text-align: start;\">点击 “角色-员工列表” 标签页</li><li style=\"text-align: start;\">查看当前拥有该角色的员工列表</li></ol><h4 style=\"text-align: start;\">5.1 员工列表功能</h4><ul><li style=\"text-align: start;\">搜索功能：通过姓名、手机号、登录账号快速查找员工</li><li style=\"text-align: start;\">列表展示：显示员工基本信息、所属部门、状态</li><li style=\"text-align: start;\">操作功能：移除：从角色中移除单个员工批量移除：批量移除选中的员工添加员工：为角色添加新员工</li></ul><h4 style=\"text-align: start;\">5.2 添加员工到角色</h4><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">点击 “添加员工” 按钮</li><li style=\"text-align: start;\">在弹出的员工选择界面中：可通过部门树筛选员工可通过搜索框快速查找支持多选员工</li><li style=\"text-align: start;\">选择员工后点击确认</li><li style=\"text-align: start;\">系统将为选中员工分配该角色</li></ol><h4 style=\"text-align: start;\">5.3 移除员工从角色</h4><p style=\"text-align: start;\"><strong>单个移除</strong>：</p><ol><li style=\"text-align: start;\">在员工列表中找到目标员工</li><li style=\"text-align: start;\">点击该员工“操作”列中的 “移除” 按钮</li><li style=\"text-align: start;\">系统弹出确认对话框</li><li style=\"text-align: start;\">确认后从角色中移除该员工</li></ol><p style=\"text-align: start;\"><strong>批量移除</strong>：</p><ol><li style=\"text-align: start;\">勾选一个或多个员工</li><li style=\"text-align: start;\">点击 “批量移除” 按钮</li><li style=\"text-align: start;\">系统弹出确认对话框，显示待移除员工数量</li><li style=\"text-align: start;\">确认后批量移除选中员工</li></ol><h3 style=\"text-align: start;\">6. 权限保存与应用</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在功能权限或数据范围配置完成后</li><li style=\"text-align: start;\">点击页面下方的 “保存” 按钮</li><li style=\"text-align: start;\">系统保存权限配置并立即生效</li></ol><p style=\"text-align: start;\"><strong>生效机制</strong>：</p><ul><li style=\"text-align: start;\">权限保存后立即生效</li><li style=\"text-align: start;\">已登录员工的权限需要重新登录后生效</li><li style=\"text-align: start;\">系统记录权限变更日志</li></ul><h3 style=\"text-align: start;\">7. 角色编辑与删除</h3><p style=\"text-align: start;\"><strong>编辑角色</strong>：</p><ol><li style=\"text-align: start;\">在角色列表中点击角色名称</li><li style=\"text-align: start;\">直接在各标签页中修改配置</li><li style=\"text-align: start;\">修改后点击保存</li></ol><p style=\"text-align: start;\"><strong>删除角色</strong>：</p><ol><li style=\"text-align: start;\">在角色管理界面提供删除功能（界面未展示但系统支持）</li><li style=\"text-align: start;\">删除前需确保没有员工使用该角色</li><li style=\"text-align: start;\">系统内置角色不可删除（如“核销员”）</li></ol><h2 style=\"text-align: start;\">四、权限模型详解</h2><h3 style=\"text-align: start;\">1. 权限继承机制</h3><ul><li style=\"text-align: start;\">员工权限 = 个人权限 + 角色权限 + 部门权限</li><li style=\"text-align: start;\">冲突解决：采用最大权限原则（如有任一来源授予权限则有效）</li><li style=\"text-align: start;\">否定权限：支持特殊场景下的权限排除</li></ul><h3 style=\"text-align: start;\">2. 权限验证流程</h3><p>用户请求 → 系统验证用户状态 → 检查功能权限 → 检查数据权限 → 执行操作</p><h3 style=\"text-align: start;\">3. 权限缓存机制</h3><ul><li style=\"text-align: start;\">用户权限信息缓存在会话中</li><li style=\"text-align: start;\">权限变更后需重新登录或刷新缓存</li><li style=\"text-align: start;\">缓存时间可配置，默认30分钟</li></ul><h2 style=\"text-align: start;\">五、内置角色说明</h2><h3 style=\"text-align: start;\">1. 核销员</h3><ul><li style=\"text-align: start;\">职责描述：负责在影城现场核销票券</li><li style=\"text-align: start;\">核心权限：票券管理：查看、查询、重新生成二维码仅限于分配给自己影城的操作无后台管理权限</li><li style=\"text-align: start;\">适用人群：影城前台工作人员</li></ul><h3 style=\"text-align: start;\">2. 管理端</h3><ul><li style=\"text-align: start;\">职责描述：系统管理员，拥有全面管理权限</li><li style=\"text-align: start;\">核心权限：所有模块的完整权限角色管理权限系统配置权限</li><li style=\"text-align: start;\">适用人群：IT管理员、系统运维人员</li></ul><h2 style=\"text-align: start;\">六、最佳实践建议</h2><h3 style=\"text-align: start;\">1. 角色设计原则</h3><ul><li style=\"text-align: start;\">职责明确：每个角色对应清晰的业务职责</li><li style=\"text-align: start;\">权限适度：避免权限过度集中或过于分散</li><li style=\"text-align: start;\">可扩展性：预留角色扩展空间，适应业务变化</li></ul><h3 style=\"text-align: start;\">2. 权限分配策略</h3><ul><li style=\"text-align: start;\">按需分配：根据实际工作需要分配权限</li><li style=\"text-align: start;\">定期审查：每季度审查角色权限配置</li><li style=\"text-align: start;\">变更记录：记录所有权限变更操作</li></ul><h3 style=\"text-align: start;\">3. 员工角色管理</h3><ul><li style=\"text-align: start;\">一人多角：支持员工拥有多个角色</li><li style=\"text-align: start;\">角色互斥：设置互斥角色，避免权限冲突</li><li style=\"text-align: start;\">离职清理：员工离职及时移除所有角色</li></ul><h3 style=\"text-align: start;\">4. 安全控制措施</h3><ul><li style=\"text-align: start;\">敏感操作日志：记录所有权限相关操作</li><li style=\"text-align: start;\">权限变更审批：重要权限变更需审批流程</li><li style=\"text-align: start;\">定期权限审计：定期进行权限使用审计</li></ul><p><br></p>', '', 4, 2, 1, '长影票务', '2026-01-30 11:00:53', '2026-01-28 16:07:29');
INSERT INTO `t_help_doc` VALUES (47, 24, '区域管理模块', '一、模块概述\n区域管理模块是票务分公司小程序系统的地理管理核心，负责建立和维护影城所在的地理区域层级体系。本模块支持多级区域管理（省/市/区县等），用于影城的区域划分、统计分析、权限分配和业务管理，确保影城能够按区域进行有效组织和运营。\n二、模块入口\n访问路径：登录Web管理系统 → 左侧导航栏 → 影城管理 → 区域管理\n权限要求：需具备“区域管理员”或“系统管理员”及以上权限角色\n三、功能详解\n1. 区域列表与查询\n1.1 列表展示\n以树形表格形式展示所有区域信息，清晰展示上下级关系\n每页默认显示10条数据，支持分页选择\n显示字段包括：区域名称（显示完整名称及简称，如“湖北省（湖北）”）区域编码（系统唯一编码，如420000）区域层级（省/市/区县等）负责人（区域负责人姓名）联系电话（负责人联系方式）排序（显示顺序权重）状态（启用/禁用）创建时间（区域创建时间）操作（添加下级/编辑/删除）\n\n1.2 查询筛选\n提供以下筛选条件：\n区域名称：支持模糊查询区域名称或简称\n状态：下拉选择（全部/启用/禁用）\n区域负责人：支持模糊查询负责人姓名\n联系电话：支持模糊查询联系电话\n操作按钮：\n查询：根据筛选条件刷新区域列表\n重置：清空所有筛选条件，显示全部区域数据\n2. 新建区域\n\n操作流程：\n点击列表右上方的 “+新建” 按钮\n在弹出的“添加区域”表单中填写以下信息：\n2.1 基本信息\n区域编码（必填）：输入区域唯一编码（如420100）编码规则：通常采用国家标准行政区划代码编码长度：6位数字，前2位为省级，中间2位为市级，后2位为区县级\n区域名称（必填）：输入完整区域名称（如“武汉市”）\n区域简称（必填）：输入区域简称（如“武汉”）\n上级区域（必填）：从下拉菜单中选择上级区域省级区域：选择“请选择上级区域”（即无上级）市级区域：选择对应的省份区县级区域：选择对应的城市\n区域层级（自动计算）：系统根据上级区域自动计算层级1级：国家级（预留）2级：省/直辖市/自治区3级：市/州/盟4级：区/县/县级市\n排序（必填）：输入显示顺序数字，数字越小排序越靠前\n2.2 管理信息\n状态（必填）：单选选择启用：区域正常使用，下属影城可正常运营禁用：区域暂停使用，下属影城暂停运营\n联系电话（可选）：填写区域负责人联系电话\n区域负责人（可选）：填写区域负责人姓名\n备注（可选）：填写区域备注信息，200字以内\n点击 “保存” 完成区域创建\n点击 “取消” 放弃操作\n注意：\n区域编码必须唯一，不可重复\n区域层级由系统根据上级区域自动确定，不可手动修改\n排序数值建议预留间隔，方便后续插入新区域\n3. 添加下级区域\n\n操作流程：\n在区域列表中找到需要添加下级区域的记录\n点击该记录“操作”列中的 “添加下级” 按钮\n系统自动预填“上级区域”字段\n填写下级区域的其他信息\n点击 “保存” 完成创建\n特点：\n下级区域自动继承上级区域的部分属性\n层级自动递增（如省级下添加市级，层级自动设为3）\n方便快速建立区域树形结构\n4. 编辑区域\n\n操作流程：\n在区域列表中找到需要修改的记录\n点击该记录“操作”列中的 “编辑” 按钮\n在弹出的编辑表单中修改信息：\n可修改字段：\n区域名称：可修改\n区域简称：可修改\n上级区域：可修改（注意层级变化）\n排序：可修改\n状态：可修改（启用/禁用）\n联系电话：可修改\n区域负责人：可修改\n备注：可修改\n不可修改字段：\n区域编码：创建后不可修改\n区域层级：由上级区域决定，不可直接修改\n点击 “保存” 完成修改\n点击 “取消” 放弃修改\n5. 删除区域\n操作流程：\n在区域列表中找到目标记录\n点击“操作”列中的 “删除” 按钮\n系统弹出确认对话框\n确认后删除该区域\n删除规则：\n只能删除没有下级区域的节点\n如果区域下有子区域，必须先删除所有子区域\n区域下有影城时不可删除\n删除操作不可逆，请谨慎操作\n6. 区域层级管理\n6.1 层级定义\n1级：国家级（系统预留，通常不使用）\n2级：省/直辖市/自治区（如湖北省）\n3级：市/州/盟（如武汉市、恩施土家族苗族自治州）\n4级：区/县/县级市（可根据业务需要扩展）\n6.2 层级关系\n每个区域必须有且只有一个上级区域（除了顶级区域）\n下级区域自动继承上级区域的部分业务属性\n区域层级影响影城的归属和统计维度\n7. 排序功能\n7.1 排序规则\n排序值越小，显示越靠前\n同级区域按排序值升序排列\n默认排序值为0，新建时可自定义\n7.2 排序调整\n通过编辑功能修改排序值\n建议设置间隔值（如10、20、30），方便后续调整\n批量调整需要逐个编辑或联系管理员\n四、区域状态管理\n1. 状态影响\n启用状态：区域正常使用下属影城可正常运营可分配核销员可进行票券核销\n禁用状态：区域暂停使用下属影城暂停运营核销员无法登录票券核销功能暂停\n2. 状态同步\n区域状态变更时，系统自动同步到下属影城：区域禁用 → 所有下属影城自动禁用区域启用 → 下属影城保持原有状态\n五、业务应用场景\n1. 影城归属管理\n每个影城必须归属于一个具体区域\n影城列表可按区域筛选\n区域负责人可管理下属所有影城\n2. 统计分析\n可按区域维度统计票券核销数据\n区域销售排行榜\n区域用户分布分析\n3. 权限分配\n区域负责人权限可管理下属区域\n核销员按区域分配管理权限\n区域数据隔离，确保数据安全\n六、数据规范与约束\n1. 编码规范\n采用国家标准行政区划代码\n编码必须唯一\n编码长度6位，不足补0\n2. 命名规范\n区域名称：官方完整名称\n区域简称：常用简称，不超过10个字符\n名称和简称需保持一致性\n3. 层级约束\n最多支持4级区域管理\n层级必须连续，不能跳级\n每个区域必须有明确的层级', '<h2 style=\"text-align: start;\">一、模块概述</h2><p style=\"text-align: start;\">区域管理模块是票务分公司小程序系统的地理管理核心，负责建立和维护影城所在的地理区域层级体系。本模块支持多级区域管理（省/市/区县等），用于影城的区域划分、统计分析、权限分配和业务管理，确保影城能够按区域进行有效组织和运营。</p><h2 style=\"text-align: start;\">二、模块入口</h2><ul><li style=\"text-align: start;\">访问路径：登录Web管理系统 → 左侧导航栏 → 影城管理 → 区域管理</li><li style=\"text-align: start;\">权限要求：需具备“区域管理员”或“系统管理员”及以上权限角色</li></ul><h2 style=\"text-align: start;\">三、功能详解</h2><h3 style=\"text-align: start;\">1. 区域列表与查询</h3><h4 style=\"text-align: start;\">1.1 列表展示</h4><ul><li style=\"text-align: start;\">以树形表格形式展示所有区域信息，清晰展示上下级关系</li><li style=\"text-align: start;\">每页默认显示10条数据，支持分页选择</li><li style=\"text-align: start;\">显示字段包括：区域名称（显示完整名称及简称，如“湖北省（湖北）”）区域编码（系统唯一编码，如420000）区域层级（省/市/区县等）负责人（区域负责人姓名）联系电话（负责人联系方式）排序（显示顺序权重）状态（启用/禁用）创建时间（区域创建时间）操作（添加下级/编辑/删除）</li></ul><p style=\"text-align: start;\"><img src=\"https://www.hbcypw.com/api/public/common/15c5479a558744b89ae3786af117d1c9_20260128161211.png\" alt=\"\" data-href=\"[object Object]\" width=\"\" height=\"\" style=\"width: 935.39px;height: 394.44px;\"></p><h4 style=\"text-align: start;\">1.2 查询筛选</h4><p style=\"text-align: start;\">提供以下筛选条件：</p><ul><li style=\"text-align: start;\">区域名称：支持模糊查询区域名称或简称</li><li style=\"text-align: start;\">状态：下拉选择（全部/启用/禁用）</li><li style=\"text-align: start;\">区域负责人：支持模糊查询负责人姓名</li><li style=\"text-align: start;\">联系电话：支持模糊查询联系电话</li></ul><p style=\"text-align: start;\">操作按钮：</p><ul><li style=\"text-align: start;\">查询：根据筛选条件刷新区域列表</li><li style=\"text-align: start;\">重置：清空所有筛选条件，显示全部区域数据</li></ul><h3 style=\"text-align: start;\">2. 新建区域</h3><p><img src=\"https://www.hbcypw.com/api/public/common/f07a0e032c424b26b543be2b326a3c5d_20260128161241.png\" alt=\"\" data-href=\"[object Object]\" width=\"\" height=\"\" style=\"width: 661.80px;height: 560.90px;\"/></p><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">点击列表右上方的 “+新建” 按钮</li><li style=\"text-align: start;\">在弹出的“添加区域”表单中填写以下信息：</li></ol><h4 style=\"text-align: start;\">2.1 基本信息</h4><ul><li style=\"text-align: start;\">区域编码（必填）：输入区域唯一编码（如420100）编码规则：通常采用国家标准行政区划代码编码长度：6位数字，前2位为省级，中间2位为市级，后2位为区县级</li><li style=\"text-align: start;\">区域名称（必填）：输入完整区域名称（如“武汉市”）</li><li style=\"text-align: start;\">区域简称（必填）：输入区域简称（如“武汉”）</li><li style=\"text-align: start;\">上级区域（必填）：从下拉菜单中选择上级区域省级区域：选择“请选择上级区域”（即无上级）市级区域：选择对应的省份区县级区域：选择对应的城市</li><li style=\"text-align: start;\">区域层级（自动计算）：系统根据上级区域自动计算层级1级：国家级（预留）2级：省/直辖市/自治区3级：市/州/盟4级：区/县/县级市</li><li style=\"text-align: start;\">排序（必填）：输入显示顺序数字，数字越小排序越靠前</li></ul><h4 style=\"text-align: start;\">2.2 管理信息</h4><ul><li style=\"text-align: start;\">状态（必填）：单选选择启用：区域正常使用，下属影城可正常运营禁用：区域暂停使用，下属影城暂停运营</li><li style=\"text-align: start;\">联系电话（可选）：填写区域负责人联系电话</li><li style=\"text-align: start;\">区域负责人（可选）：填写区域负责人姓名</li><li style=\"text-align: start;\">备注（可选）：填写区域备注信息，200字以内</li></ul><ol><li style=\"text-align: start;\">点击 “保存” 完成区域创建</li><li style=\"text-align: start;\">点击 “取消” 放弃操作</li></ol><p style=\"text-align: start;\"><strong>注意</strong>：</p><ul><li style=\"text-align: start;\">区域编码必须唯一，不可重复</li><li style=\"text-align: start;\">区域层级由系统根据上级区域自动确定，不可手动修改</li><li style=\"text-align: start;\">排序数值建议预留间隔，方便后续插入新区域</li></ul><h3 style=\"text-align: start;\">3. 添加下级区域</h3><p><img src=\"https://www.hbcypw.com/api/public/common/6ade6f95bd4a4c31870c530012a1134d_20260128161331.png\" alt=\"\" data-href=\"[object Object]\" width=\"\" height=\"\" style=\"width: 615.80px;height: 521.91px;\"/></p><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在区域列表中找到需要添加下级区域的记录</li><li style=\"text-align: start;\">点击该记录“操作”列中的 “添加下级” 按钮</li><li style=\"text-align: start;\">系统自动预填“上级区域”字段</li><li style=\"text-align: start;\">填写下级区域的其他信息</li><li style=\"text-align: start;\">点击 “保存” 完成创建</li></ol><p style=\"text-align: start;\"><strong>特点</strong>：</p><ul><li style=\"text-align: start;\">下级区域自动继承上级区域的部分属性</li><li style=\"text-align: start;\">层级自动递增（如省级下添加市级，层级自动设为3）</li><li style=\"text-align: start;\">方便快速建立区域树形结构</li></ul><h3 style=\"text-align: start;\">4. 编辑区域</h3><p><img src=\"https://www.hbcypw.com/api/public/common/f217ba4d775247619b27c21744b9f1c7_20260128161356.png\" alt=\"\" data-href=\"[object Object]\" width=\"\" height=\"\" style=\"width: 614.80px;height: 527.36px;\"/></p><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在区域列表中找到需要修改的记录</li><li style=\"text-align: start;\">点击该记录“操作”列中的 “编辑” 按钮</li><li style=\"text-align: start;\">在弹出的编辑表单中修改信息：</li></ol><p style=\"text-align: start;\"><strong>可修改字段</strong>：</p><ul><li style=\"text-align: start;\">区域名称：可修改</li><li style=\"text-align: start;\">区域简称：可修改</li><li style=\"text-align: start;\">上级区域：可修改（注意层级变化）</li><li style=\"text-align: start;\">排序：可修改</li><li style=\"text-align: start;\">状态：可修改（启用/禁用）</li><li style=\"text-align: start;\">联系电话：可修改</li><li style=\"text-align: start;\">区域负责人：可修改</li><li style=\"text-align: start;\">备注：可修改</li></ul><p style=\"text-align: start;\"><strong>不可修改字段</strong>：</p><ul><li style=\"text-align: start;\">区域编码：创建后不可修改</li><li style=\"text-align: start;\">区域层级：由上级区域决定，不可直接修改</li></ul><ol><li style=\"text-align: start;\">点击 “保存” 完成修改</li><li style=\"text-align: start;\">点击 “取消” 放弃修改</li></ol><h3 style=\"text-align: start;\">5. 删除区域</h3><p style=\"text-align: start;\"><strong>操作流程</strong>：</p><ol><li style=\"text-align: start;\">在区域列表中找到目标记录</li><li style=\"text-align: start;\">点击“操作”列中的 “删除” 按钮</li><li style=\"text-align: start;\">系统弹出确认对话框</li><li style=\"text-align: start;\">确认后删除该区域</li></ol><p style=\"text-align: start;\"><strong>删除规则</strong>：</p><ul><li style=\"text-align: start;\">只能删除没有下级区域的节点</li><li style=\"text-align: start;\">如果区域下有子区域，必须先删除所有子区域</li><li style=\"text-align: start;\">区域下有影城时不可删除</li><li style=\"text-align: start;\">删除操作不可逆，请谨慎操作</li></ul><h3 style=\"text-align: start;\">6. 区域层级管理</h3><h4 style=\"text-align: start;\">6.1 层级定义</h4><ul><li style=\"text-align: start;\">1级：国家级（系统预留，通常不使用）</li><li style=\"text-align: start;\">2级：省/直辖市/自治区（如湖北省）</li><li style=\"text-align: start;\">3级：市/州/盟（如武汉市、恩施土家族苗族自治州）</li><li style=\"text-align: start;\">4级：区/县/县级市（可根据业务需要扩展）</li></ul><h4 style=\"text-align: start;\">6.2 层级关系</h4><ul><li style=\"text-align: start;\">每个区域必须有且只有一个上级区域（除了顶级区域）</li><li style=\"text-align: start;\">下级区域自动继承上级区域的部分业务属性</li><li style=\"text-align: start;\">区域层级影响影城的归属和统计维度</li></ul><h3 style=\"text-align: start;\">7. 排序功能</h3><h4 style=\"text-align: start;\">7.1 排序规则</h4><ul><li style=\"text-align: start;\">排序值越小，显示越靠前</li><li style=\"text-align: start;\">同级区域按排序值升序排列</li><li style=\"text-align: start;\">默认排序值为0，新建时可自定义</li></ul><h4 style=\"text-align: start;\">7.2 排序调整</h4><ul><li style=\"text-align: start;\">通过编辑功能修改排序值</li><li style=\"text-align: start;\">建议设置间隔值（如10、20、30），方便后续调整</li><li style=\"text-align: start;\">批量调整需要逐个编辑或联系管理员</li></ul><h2 style=\"text-align: start;\">四、区域状态管理</h2><h3 style=\"text-align: start;\">1. 状态影响</h3><ul><li style=\"text-align: start;\">启用状态：区域正常使用下属影城可正常运营可分配核销员可进行票券核销</li><li style=\"text-align: start;\">禁用状态：区域暂停使用下属影城暂停运营核销员无法登录票券核销功能暂停</li></ul><h3 style=\"text-align: start;\">2. 状态同步</h3><ul><li style=\"text-align: start;\">区域状态变更时，系统自动同步到下属影城：区域禁用 → 所有下属影城自动禁用区域启用 → 下属影城保持原有状态</li></ul><h2 style=\"text-align: start;\">五、业务应用场景</h2><h3 style=\"text-align: start;\">1. 影城归属管理</h3><ul><li style=\"text-align: start;\">每个影城必须归属于一个具体区域</li><li style=\"text-align: start;\">影城列表可按区域筛选</li><li style=\"text-align: start;\">区域负责人可管理下属所有影城</li></ul><h3 style=\"text-align: start;\">2. 统计分析</h3><ul><li style=\"text-align: start;\">可按区域维度统计票券核销数据</li><li style=\"text-align: start;\">区域销售排行榜</li><li style=\"text-align: start;\">区域用户分布分析</li></ul><h3 style=\"text-align: start;\">3. 权限分配</h3><ul><li style=\"text-align: start;\">区域负责人权限可管理下属区域</li><li style=\"text-align: start;\">核销员按区域分配管理权限</li><li style=\"text-align: start;\">区域数据隔离，确保数据安全</li></ul><h2 style=\"text-align: start;\">六、数据规范与约束</h2><h3 style=\"text-align: start;\">1. 编码规范</h3><ul><li style=\"text-align: start;\">采用国家标准行政区划代码</li><li style=\"text-align: start;\">编码必须唯一</li><li style=\"text-align: start;\">编码长度6位，不足补0</li></ul><h3 style=\"text-align: start;\">2. 命名规范</h3><ul><li style=\"text-align: start;\">区域名称：官方完整名称</li><li style=\"text-align: start;\">区域简称：常用简称，不超过10个字符</li><li style=\"text-align: start;\">名称和简称需保持一致性</li></ul><h3 style=\"text-align: start;\">3. 层级约束</h3><ul><li style=\"text-align: start;\">最多支持4级区域管理</li><li style=\"text-align: start;\">层级必须连续，不能跳级</li><li style=\"text-align: start;\">每个区域必须有明确的层级</li></ul>', '', 9, 13, 2, '长影票务', '2026-02-12 14:53:49', '2026-01-28 16:10:46');

-- ----------------------------
-- Table structure for t_help_doc_catalog
-- ----------------------------
DROP TABLE IF EXISTS `t_help_doc_catalog`;
CREATE TABLE `t_help_doc_catalog`  (
                                       `help_doc_catalog_id` bigint NOT NULL AUTO_INCREMENT COMMENT '帮助文档目录',
                                       `name` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
                                       `sort` int NOT NULL DEFAULT 0 COMMENT '排序字段',
                                       `parent_id` bigint NOT NULL COMMENT '父级id',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`help_doc_catalog_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帮助文档-目录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_help_doc_catalog
-- ----------------------------
INSERT INTO `t_help_doc_catalog` VALUES (6, '票务系统管理端操作手册', 0, 0, '2022-11-05 10:52:40', '2026-01-28 14:14:45');
INSERT INTO `t_help_doc_catalog` VALUES (24, '区域管理', 2, 6, '2026-01-28 15:50:48', '2026-01-28 15:50:48');
INSERT INTO `t_help_doc_catalog` VALUES (26, '组织架构', 8, 6, '2026-01-28 16:04:05', '2026-01-28 16:04:05');

-- ----------------------------
-- Table structure for t_help_doc_relation
-- ----------------------------
DROP TABLE IF EXISTS `t_help_doc_relation`;
CREATE TABLE `t_help_doc_relation`  (
                                        `relation_id` bigint NOT NULL COMMENT '关联id',
                                        `relation_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联名称',
                                        `help_doc_id` bigint NOT NULL COMMENT '文档id',
                                        `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                        `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        PRIMARY KEY (`relation_id`, `help_doc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帮助文档-关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_help_doc_relation
-- ----------------------------
INSERT INTO `t_help_doc_relation` VALUES (0, '首页', 36, '2026-01-28 14:38:03', '2026-01-28 14:38:03');
INSERT INTO `t_help_doc_relation` VALUES (46, '员工管理', 44, '2026-01-30 10:04:02', '2026-01-30 10:04:02');
INSERT INTO `t_help_doc_relation` VALUES (76, '角色管理', 46, '2026-01-28 16:25:59', '2026-01-28 16:25:59');
INSERT INTO `t_help_doc_relation` VALUES (219, '部门管理', 43, '2026-01-30 11:05:08', '2026-01-30 11:05:08');
INSERT INTO `t_help_doc_relation` VALUES (228, '职务管理', 45, '2026-01-30 10:04:08', '2026-01-30 10:04:08');
INSERT INTO `t_help_doc_relation` VALUES (343, '区域管理', 47, '2026-01-30 11:05:01', '2026-01-30 11:05:01');

-- ----------------------------
-- Table structure for t_help_doc_view_record
-- ----------------------------
DROP TABLE IF EXISTS `t_help_doc_view_record`;
CREATE TABLE `t_help_doc_view_record`  (
                                           `help_doc_id` bigint NOT NULL COMMENT '通知公告id',
                                           `user_id` bigint NOT NULL COMMENT '用户id',
                                           `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名称',
                                           `page_view_count` int NULL DEFAULT 0 COMMENT '查看次数',
                                           `first_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '首次ip',
                                           `first_user_agent` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '首次用户设备等标识',
                                           `last_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后一次ip',
                                           `last_user_agent` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后一次用户设备等标识',
                                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                           PRIMARY KEY (`help_doc_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '帮助文档-查看记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_help_doc_view_record
-- ----------------------------
INSERT INTO `t_help_doc_view_record` VALUES (36, 1, '长影票务', 1, NULL, NULL, NULL, NULL, '2026-02-12 14:53:53', '2026-02-12 14:53:53');
INSERT INTO `t_help_doc_view_record` VALUES (47, 1, '长影票务', 1, NULL, NULL, NULL, NULL, '2026-02-12 14:53:49', '2026-02-12 14:53:49');

-- ----------------------------
-- Table structure for t_mail_template
-- ----------------------------
DROP TABLE IF EXISTS `t_mail_template`;
CREATE TABLE `t_mail_template`  (
                                    `template_code` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
                                    `template_subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
                                    `template_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板内容',
                                    `template_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '解析类型 string，freemarker',
                                    `disable_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否禁用',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    PRIMARY KEY (`template_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邮件模板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_mail_template
-- ----------------------------
INSERT INTO `t_mail_template` VALUES ('login_verification_code', '登录验证码', '<!DOCTYPE HTML>\r\n<html>\r\n<head>\r\n  <title>登录提醒</title>\r\n  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\r\n  <style>\r\n      * {\r\n          font-family: SimSun;\r\n          /* 4号字体 */\r\n          font-size: 18px;\r\n          /* 22磅行间距 */\r\n          line-height: 29px;\r\n      }\r\n\r\n      .main_font_size {\r\n          font-size: 12.0pt;\r\n      }\r\n\r\n      .mainContent {\r\n          line-height: 28px;\r\n      }\r\n\r\n      p {\r\n          margin: 0 auto;\r\n          text-align: justify;\r\n      }\r\n  </style>\r\n\r\n</head>\r\n<body>\r\n<div>\r\n  <div style=\"margin: 0px auto;width: 690px;\">\r\n    <div class=\"mainContent\">\r\n      <h1>验证码</h1>\r\n      <p>请在验证页面输入此验证码</p>\r\n      <p><b>${code}</b></p>\r\n      <p>验证码将于此电子邮件发出 5 分钟后过期。</p>\r\n      <p>如果你未曾提出此请求，可以忽略这封电子邮件。</p>\r\n    </div>\r\n\r\n  </div>\r\n</div>\r\n</body>\r\n</html>', 'freemarker', 0, '2024-08-06 09:13:08', '2024-07-28 13:56:06');

-- ----------------------------
-- Table structure for t_message
-- ----------------------------
DROP TABLE IF EXISTS `t_message`;
CREATE TABLE `t_message`  (
                              `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息id',
                              `message_type` smallint NOT NULL COMMENT '消息类型',
                              `receiver_user_type` int NOT NULL COMMENT '接收者用户类型',
                              `receiver_user_id` bigint NOT NULL COMMENT '接收者用户id',
                              `data_id` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '相关数据id',
                              `title` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
                              `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
                              `read_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
                              `read_time` datetime NULL DEFAULT NULL COMMENT '已读时间',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`message_id`) USING BTREE,
                              INDEX `idx_msg`(`message_type` ASC, `receiver_user_type` ASC, `receiver_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_message
-- ----------------------------

-- ----------------------------
-- Table structure for t_nacos_config_template
-- ----------------------------
DROP TABLE IF EXISTS `t_nacos_config_template`;
CREATE TABLE `t_nacos_config_template`  (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                            `template_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板编码',
                                            `data_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置ID',
                                            `group_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'DEFAULT_GROUP' COMMENT '分组ID',
                                            `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置内容',
                                            `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'yaml' COMMENT '配置类型: yaml, properties, json, xml, text, html',
                                            `database_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'common' COMMENT '数据库类型: common, mysql, postgresql',
                                            `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
                                            `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
                                            `sort_order` int NULL DEFAULT 0 COMMENT '排序',
                                            `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            UNIQUE INDEX `uk_template_code`(`template_code` ASC, `database_type` ASC) USING BTREE,
                                            INDEX `idx_data_id`(`data_id` ASC) USING BTREE,
                                            INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
                                            INDEX `idx_database_type`(`database_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Nacos配置模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_nacos_config_template
-- ----------------------------
INSERT INTO `t_nacos_config_template` VALUES (1, 'support_datasource', 'datasource', 'DEFAULT_GROUP', 'spring:\n  datasource:\n    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%%2B8\n    username: ${MYSQL_USER}\n    password: ${MYSQL_PASSWORD}\n    driver-class-name: com.mysql.cj.jdbc.Driver', 'yaml', 'mysql', 'Support服务数据源配置', 1, 1, '2026-03-28 22:49:57', '2026-03-28 22:49:57');
INSERT INTO `t_nacos_config_template` VALUES (2, 'system_datasource', 'datasource', 'DEFAULT_GROUP', 'spring:\n  datasource:\n    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%%2B8\n    username: ${MYSQL_USER}\n    password: ${MYSQL_PASSWORD}\n    driver-class-name: com.mysql.cj.jdbc.Driver', 'yaml', 'mysql', 'System服务数据源配置', 1, 2, '2026-03-28 22:49:57', '2026-03-28 22:49:57');
INSERT INTO `t_nacos_config_template` VALUES (3, 'business_datasource', 'datasource', 'DEFAULT_GROUP', 'spring:\n  datasource:\n    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%%2B8\n    username: ${MYSQL_USER}\n    password: ${MYSQL_PASSWORD}\n    driver-class-name: com.mysql.cj.jdbc.Driver', 'yaml', 'mysql', 'Business服务数据源配置', 1, 3, '2026-03-28 22:49:57', '2026-03-28 22:49:57');
INSERT INTO `t_nacos_config_template` VALUES (4, 'ai_datasource', 'datasource', 'DEFAULT_GROUP', 'spring:\n  datasource:\n    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%%2B8\n    username: ${MYSQL_USER}\n    password: ${MYSQL_PASSWORD}\n    driver-class-name: com.mysql.cj.jdbc.Driver', 'yaml', 'mysql', 'AI服务数据源配置', 1, 4, '2026-03-28 22:49:57', '2026-03-28 22:49:57');
INSERT INTO `t_nacos_config_template` VALUES (5, 'support_datasource_pg', 'datasource', 'DEFAULT_GROUP', 'spring:\n  datasource:\n    url: jdbc:postgresql://${PG_HOST}:${PG_PORT}/${PG_DATABASE}\n    username: ${PG_USER}\n    password: ${PG_PASSWORD}\n    driver-class-name: org.postgresql.Driver', 'yaml', 'postgresql', 'Support服务PostgreSQL数据源配置', 1, 5, '2026-03-28 22:49:57', '2026-03-28 22:49:57');
INSERT INTO `t_nacos_config_template` VALUES (6, 'system_datasource_pg', 'datasource', 'DEFAULT_GROUP', 'spring:\n  datasource:\n    url: jdbc:postgresql://${PG_HOST}:${PG_PORT}/${PG_DATABASE}\n    username: ${PG_USER}\n    password: ${PG_PASSWORD}\n    driver-class-name: org.postgresql.Driver', 'yaml', 'postgresql', 'System服务PostgreSQL数据源配置', 1, 6, '2026-03-28 22:49:57', '2026-03-28 22:49:57');
INSERT INTO `t_nacos_config_template` VALUES (7, 'business_datasource_pg', 'datasource', 'DEFAULT_GROUP', 'spring:\n  datasource:\n    url: jdbc:postgresql://${PG_HOST}:${PG_PORT}/${PG_DATABASE}\n    username: ${PG_USER}\n    password: ${PG_PASSWORD}\n    driver-class-name: org.postgresql.Driver', 'yaml', 'postgresql', 'Business服务PostgreSQL数据源配置', 1, 7, '2026-03-28 22:49:57', '2026-03-28 22:49:57');
INSERT INTO `t_nacos_config_template` VALUES (8, 'ai_datasource_pg', 'datasource', 'DEFAULT_GROUP', 'spring:\n  datasource:\n    url: jdbc:postgresql://${PG_HOST}:${PG_PORT}/${PG_DATABASE}\n    username: ${PG_USER}\n    password: ${PG_PASSWORD}\n    driver-class-name: org.postgresql.Driver', 'yaml', 'postgresql', 'AI服务PostgreSQL数据源配置', 1, 8, '2026-03-28 22:49:57', '2026-03-28 22:49:57');

-- ----------------------------
-- Table structure for t_reload_item
-- ----------------------------
DROP TABLE IF EXISTS `t_reload_item`;
CREATE TABLE `t_reload_item`  (
                                  `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项名称',
                                  `args` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数 可选',
                                  `identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '运行标识',
                                  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`tag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'reload项目' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_reload_item
-- ----------------------------

-- ----------------------------
-- Table structure for t_reload_result
-- ----------------------------
DROP TABLE IF EXISTS `t_reload_result`;
CREATE TABLE `t_reload_result`  (
                                    `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
                                    `identification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '运行标识',
                                    `args` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                    `result` tinyint UNSIGNED NOT NULL COMMENT '是否成功 ',
                                    `exception` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'reload结果' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_reload_result
-- ----------------------------

-- ----------------------------
-- Table structure for t_serial_number
-- ----------------------------
DROP TABLE IF EXISTS `t_serial_number`;
CREATE TABLE `t_serial_number`  (
                                    `serial_number_id` int NOT NULL COMMENT '序列号ID',
                                    `business_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务名称',
                                    `format` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '格式[yyyy]表示年,[mm]标识月,[dd]表示日,[nnn]表示三位数字',
                                    `rule_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则格式。none没有周期, year 年周期, month月周期, day日周期',
                                    `init_number` bigint NOT NULL COMMENT '初始值',
                                    `step_random_range` bigint NOT NULL COMMENT '步长随机数',
                                    `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                    `last_number` bigint NULL DEFAULT NULL COMMENT '上次产生的单号, 默认为空',
                                    `last_time` datetime NULL DEFAULT NULL COMMENT '上次产生的单号时间',
                                    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `secure_mode` tinyint NOT NULL DEFAULT 0 COMMENT '安全模式:0-普通,1-随机,2-时间戳,3-加密',
                                    `random_range` bigint NULL DEFAULT 100 COMMENT '随机跳跃范围',
                                    `encrypt_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '加密密钥（可空）',
                                    PRIMARY KEY (`serial_number_id`) USING BTREE,
                                    UNIQUE INDEX `key_name`(`business_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '单号生成器定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_serial_number
-- ----------------------------

-- ----------------------------
-- Table structure for t_serial_number_record
-- ----------------------------
DROP TABLE IF EXISTS `t_serial_number_record`;
CREATE TABLE `t_serial_number_record`  (
                                           `serial_number_id` int NOT NULL COMMENT '序列号ID',
                                           `record_date` date NOT NULL COMMENT '记录日期',
                                           `last_number` bigint NOT NULL DEFAULT 0 COMMENT '最后更新值',
                                           `last_time` datetime NOT NULL COMMENT '最后更新时间',
                                           `count` bigint NOT NULL DEFAULT 0 COMMENT '更新次数',
                                           `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `original_sequence` bigint NULL DEFAULT NULL COMMENT '原始序列号（安全模式使用）',
                                           INDEX `uk_generator`(`serial_number_id` ASC, `record_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'serial_number记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_serial_number_record
-- ----------------------------

-- ----------------------------
-- Table structure for t_quickblue_job
-- ----------------------------
DROP TABLE IF EXISTS `t_quickblue_job`;
CREATE TABLE `t_quickblue_job`  (
                                `job_id` int NOT NULL AUTO_INCREMENT COMMENT '任务id',
                                `job_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
                                `job_class` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务执行类',
                                `trigger_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发类型',
                                `trigger_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发配置',
                                `enabled_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启',
                                `param` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数',
                                `last_execute_time` datetime NULL DEFAULT NULL COMMENT '最后一次执行时间',
                                `last_execute_log_id` int NULL DEFAULT NULL COMMENT '最后一次执行记录id',
                                `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
                                `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
                                `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除状态',
                                `update_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '更新人',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`job_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务配置 @listen' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_quickblue_job
-- ----------------------------
INSERT INTO `t_quickblue_job` VALUES (6, '数据库自动备份', 'com.budaos.support.jobtask.job.DatabaseAutoBackupJob', 'cron', '0 0 6 * * ?', 1, '', '2026-02-10 06:00:00', 9179, 3, '每天凌晨2点自动备份数据库', 0, '长影票务', '2026-01-31 08:07:58', '2026-02-13 17:54:40');

-- ----------------------------
-- Table structure for t_quickblue_job_log
-- ----------------------------
DROP TABLE IF EXISTS `t_quickblue_job_log`;
CREATE TABLE `t_quickblue_job_log`  (
                                    `log_id` int NOT NULL AUTO_INCREMENT,
                                    `job_id` int NOT NULL COMMENT '任务id',
                                    `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
                                    `param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行参数',
                                    `success_flag` tinyint(1) NOT NULL COMMENT '是否成功',
                                    `execute_start_time` datetime NOT NULL COMMENT '执行开始时间',
                                    `execute_time_millis` int NULL DEFAULT NULL COMMENT '执行时长',
                                    `execute_end_time` datetime NULL DEFAULT NULL COMMENT '执行结束时间',
                                    `execute_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                    `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ip',
                                    `process_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '进程id',
                                    `program_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '程序目录',
                                    `create_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`log_id`) USING BTREE,
                                    INDEX `idx_job_id`(`job_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务-执行记录 @listen' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_quickblue_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for t_sys_config_audit
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_config_audit`;
CREATE TABLE `t_sys_config_audit`  (
                                       `audit_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `data_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置ID',
                                       `group_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置分组',
                                       `tenant_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '命名空间ID',
                                       `config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置名称(用于展示)',
                                       `old_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '修改前内容',
                                       `new_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '修改后内容',
                                       `op_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作类型: CREATE/UPDATE/DELETE',
                                       `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
                                       `operator_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人名称',
                                       `operator_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作IP',
                                       `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       PRIMARY KEY (`audit_id`) USING BTREE,
                                       INDEX `idx_data_id`(`data_id` ASC) USING BTREE,
                                       INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
                                       INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
                                       INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Nacos配置变更审计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_config_audit
-- ----------------------------

-- ----------------------------
-- Table structure for t_table_column
-- ----------------------------
DROP TABLE IF EXISTS `t_table_column`;
CREATE TABLE `t_table_column`  (
                                   `table_column_id` bigint NOT NULL AUTO_INCREMENT,
                                   `user_id` bigint NOT NULL COMMENT '用户id',
                                   `user_type` int NOT NULL COMMENT '用户类型',
                                   `table_id` int NOT NULL COMMENT '表格id',
                                   `columns` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '具体的表格列，存入的json',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`table_column_id`) USING BTREE,
                                   UNIQUE INDEX `uni_employee_table`(`user_id` ASC, `table_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '表格的自定义列存储' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_table_column
-- ----------------------------
INSERT INTO `t_table_column` VALUES (6, 1, 1, 40001, '[{\"columnKey\":\"cinemaName\",\"showFlag\":true,\"sort\":1,\"width\":180},{\"columnKey\":\"cinemaShortName\",\"showFlag\":true,\"sort\":2,\"width\":100},{\"columnKey\":\"cinemaCode\",\"showFlag\":true,\"sort\":3,\"width\":100},{\"columnKey\":\"status\",\"showFlag\":true,\"sort\":4,\"width\":80},{\"columnKey\":\"cinemaAddress\",\"showFlag\":true,\"sort\":5,\"width\":200},{\"columnKey\":\"cinemaMan\",\"showFlag\":true,\"sort\":6,\"width\":100},{\"columnKey\":\"cinemaMobile\",\"showFlag\":true,\"sort\":7,\"width\":120},{\"columnKey\":\"updateTime\",\"showFlag\":true,\"sort\":8,\"width\":140},{\"columnKey\":\"updateUserName\",\"showFlag\":true,\"sort\":9,\"width\":100},{\"columnKey\":\"action\",\"showFlag\":true,\"sort\":10,\"width\":100}]', '2025-12-06 15:06:48', '2025-12-06 15:06:48');
INSERT INTO `t_table_column` VALUES (7, 1, 1, 40003, '[{\"columnKey\":\"batchNo\",\"showFlag\":true,\"sort\":1,\"width\":130},{\"columnKey\":\"batchName\",\"showFlag\":true,\"sort\":2,\"width\":100},{\"columnKey\":\"ticketTypeId\",\"showFlag\":true,\"sort\":3,\"width\":80},{\"columnKey\":\"cinemaNames\",\"showFlag\":true,\"sort\":4},{\"columnKey\":\"quantity\",\"showFlag\":true,\"sort\":5},{\"columnKey\":\"startCode\",\"showFlag\":true,\"sort\":6},{\"columnKey\":\"endCode\",\"showFlag\":true,\"sort\":7},{\"columnKey\":\"generatedQuantity\",\"showFlag\":true,\"sort\":8},{\"columnKey\":\"customFaceValue\",\"showFlag\":true,\"sort\":9},{\"columnKey\":\"customValidityDays\",\"showFlag\":true,\"sort\":10},{\"columnKey\":\"purpose\",\"showFlag\":true,\"sort\":11},{\"columnKey\":\"batchStatus\",\"showFlag\":true,\"sort\":12,\"width\":100},{\"columnKey\":\"createUserName\",\"showFlag\":true,\"sort\":13},{\"columnKey\":\"createTime\",\"showFlag\":true,\"sort\":14},{\"columnKey\":\"action\",\"showFlag\":true,\"sort\":15,\"width\":100}]', '2025-12-14 20:51:34', '2025-12-14 20:51:34');
INSERT INTO `t_table_column` VALUES (8, 1, 1, 40004, '[{\"columnKey\":\"ticketCode\",\"showFlag\":true,\"sort\":1,\"width\":150},{\"columnKey\":\"qrcodeContent\",\"showFlag\":false,\"sort\":2},{\"columnKey\":\"ticketTypeId\",\"showFlag\":true,\"sort\":3,\"width\":120},{\"columnKey\":\"ticketStatus\",\"showFlag\":true,\"sort\":4,\"width\":100},{\"columnKey\":\"writeOffCinemaId\",\"showFlag\":true,\"sort\":5,\"width\":120},{\"columnKey\":\"writeOffTime\",\"showFlag\":true,\"sort\":6,\"width\":150},{\"columnKey\":\"createTime\",\"showFlag\":true,\"sort\":7,\"width\":150},{\"columnKey\":\"batchName\",\"showFlag\":true,\"sort\":8,\"width\":140},{\"columnKey\":\"validityPeriodBeginTime\",\"showFlag\":true,\"sort\":9,\"width\":150},{\"columnKey\":\"validityPeriodEndTime\",\"showFlag\":true,\"sort\":10,\"width\":150},{\"columnKey\":\"validityDays\",\"showFlag\":true,\"sort\":11,\"width\":100},{\"columnKey\":\"action\",\"showFlag\":true,\"sort\":12,\"width\":100}]', '2025-12-19 16:52:58', '2025-12-19 16:52:58');

SET FOREIGN_KEY_CHECKS = 1;
