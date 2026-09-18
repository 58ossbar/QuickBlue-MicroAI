/*
 Navicat Premium Dump SQL

 Source Server         : 本地开发服务器
 Source Server Type    : MySQL
 Source Schema         : quickblue_business

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 18/08/2026 17:53:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_notice
-- ----------------------------
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice`  (
                             `notice_id` bigint NOT NULL AUTO_INCREMENT,
                             `notice_type_id` bigint NOT NULL COMMENT '类型1公告 2动态',
                             `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
                             `all_visible_flag` tinyint(1) NOT NULL COMMENT '是否全部可见',
                             `scheduled_publish_flag` tinyint(1) NOT NULL COMMENT '是否定时发布',
                             `publish_time` datetime NOT NULL COMMENT '发布时间',
                             `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文本内容',
                             `content_html` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'html内容',
                             `attachment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '附件',
                             `page_view_count` int NOT NULL DEFAULT 0 COMMENT '页面浏览量，传说中的pv',
                             `user_view_count` int NOT NULL DEFAULT 0 COMMENT '用户浏览量，传说中的uv',
                             `source` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '来源',
                             `author` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作者',
                             `document_number` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文号，如：XX单位发〔2022〕字第36号',
                             `deleted_flag` tinyint(1) NOT NULL DEFAULT 0,
                             `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 69 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_notice
-- ----------------------------

-- ----------------------------
-- Table structure for t_notice_receiver
-- ----------------------------
DROP TABLE IF EXISTS `t_notice_receiver`;
CREATE TABLE `t_notice_receiver`  (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                      `notice_id` bigint NOT NULL COMMENT '通知ID',
                                      `receiver_type` tinyint NOT NULL COMMENT '接收人类型(1:全部 2:指定用户 3:指定部门)',
                                      `receiver_id` bigint NULL DEFAULT NULL COMMENT '接收人ID',
                                      `read_status` tinyint NULL DEFAULT 1 COMMENT '阅读状态(1:未读 2:已读)',
                                      `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `idx_notice_id`(`notice_id` ASC) USING BTREE,
                                      INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知接收人表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_notice_receiver
-- ----------------------------

-- ----------------------------
-- Table structure for t_notice_type
-- ----------------------------
DROP TABLE IF EXISTS `t_notice_type`;
CREATE TABLE `t_notice_type`  (
                                  `notice_type_id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知类型',
                                  `notice_type_name` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型名称',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`notice_type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知类型' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_notice_type
-- ----------------------------
INSERT INTO `t_notice_type` VALUES (1, '新闻', '2022-08-16 20:29:15', '2024-09-03 21:44:42');
INSERT INTO `t_notice_type` VALUES (2, '通知', '2022-08-16 20:29:20', '2022-08-16 20:29:20');
INSERT INTO `t_notice_type` VALUES (3, '公告', '2022-08-16 20:29:25', '2022-08-16 20:29:25');

-- ----------------------------
-- Table structure for t_notice_view_record
-- ----------------------------
DROP TABLE IF EXISTS `t_notice_view_record`;
CREATE TABLE `t_notice_view_record`  (
                                         `notice_id` bigint NOT NULL COMMENT '通知公告id',
                                         `employee_id` bigint NOT NULL COMMENT '员工id',
                                         `page_view_count` int NULL DEFAULT 0 COMMENT '查看次数',
                                         `first_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '首次ip',
                                         `first_user_agent` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '首次用户设备等标识',
                                         `last_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后一次ip',
                                         `last_user_agent` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后一次用户设备等标识',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`notice_id`, `employee_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知查看记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_notice_view_record
-- ----------------------------
INSERT INTO `t_notice_view_record` VALUES (67, 1, 1, '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36', NULL, NULL, '2026-02-12 20:48:56', '2026-02-12 20:48:56');

-- ----------------------------
-- Table structure for t_notice_visible_range
-- ----------------------------
DROP TABLE IF EXISTS `t_notice_visible_range`;
CREATE TABLE `t_notice_visible_range`  (
                                           `notice_id` bigint NOT NULL COMMENT '资讯id',
                                           `data_type` tinyint NOT NULL COMMENT '数据类型1员工 2部门',
                                           `data_id` bigint NOT NULL COMMENT '员工or部门id',
                                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           UNIQUE INDEX `uk_notice_data`(`notice_id` ASC, `data_type` ASC, `data_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知可见范围' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_notice_visible_range
-- ----------------------------

-- ----------------------------
-- Table structure for t_region
-- ----------------------------
DROP TABLE IF EXISTS `t_region`;
CREATE TABLE `t_region`  (
                             `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域ID',
                             `region_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域编码（唯一）',
                             `region_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区域名称',
                             `region_short_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区域简称',
                             `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父级区域ID',
                             `parent_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '父级路径（格式: /id1/id2/id3）',
                             `level` tinyint NOT NULL DEFAULT 1 COMMENT '区域层级：1-大区 2-省/市 3-城市 4-区县',
                             `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序（同层级内）',
                             `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
                             `region_manager_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区域负责人姓名',
                             `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                             `leaf_flag` tinyint NOT NULL DEFAULT 1 COMMENT '是否叶子节点：0-否 1-是',
                             `delete_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除 1-已删除',
                             `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁',
                             `create_user_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_user_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `uk_region_code`(`region_code` ASC) USING BTREE,
                             INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
                             INDEX `idx_parent_path`(`parent_path`(255) ASC) USING BTREE,
                             INDEX `idx_level`(`level` ASC) USING BTREE,
                             INDEX `idx_status`(`status` ASC) USING BTREE,
                             INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '区域表（树形结构）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_region
-- ----------------------------
INSERT INTO `t_region` VALUES ('420000', '420000', '湖北省', '湖北', NULL, '/', 2, 1, 1, '湖北省负责人', '027-12345678', '湖北省（鄂）', 0, 0, 0, 1, '2026-01-10 09:12:55', 1, '2026-01-10 09:12:55');
INSERT INTO `t_region` VALUES ('420100', '420100', '武汉市', '武汉', '420000', '/420000', 3, 99, 1, '武汉市负责人', '027-87654321', '湖北省省会', 0, 0, 0, 1, '2026-01-10 09:12:55', 1, '2026-01-10 09:12:55');
INSERT INTO `t_region` VALUES ('420102', '420102', '江岸区', '江岸', '420100', '/420000/420100', 4, 1, 1, '江岸区负责人', '027-82820000', NULL, 1, 0, 0, 1, '2026-01-10 09:12:55', 1, '2026-01-10 09:12:55');
INSERT INTO `t_region` VALUES ('420103', '420103', '江汉区', '江汉', '420100', '/420000/420100', 4, 2, 1, '江汉区负责人', '027-85830000', NULL, 1, 0, 0, 1, '2026-01-10 09:12:55', 1, '2026-01-10 09:12:55');

SET FOREIGN_KEY_CHECKS = 1;
