/*
 Navicat Premium Dump SQL

 Source Server         : 本地开发服务器
 Source Server Type    : MySQL
 Source Schema         : quickblue_ai

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 18/08/2026 17:51:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for airag_agent
-- ----------------------------
DROP TABLE IF EXISTS `airag_agent`;
CREATE TABLE `airag_agent`  (
                                `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                                `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
                                `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                                `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
                                `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                                `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                                `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '智能体名称',
                                `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '智能体描述',
                                `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
                                `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '系统提示词',
                                `model_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联模型ID',
                                `knowledge_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联知识库ID(逗号分隔)',
                                `tool_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联工具ID(逗号分隔)',
                                `max_iterations` int NULL DEFAULT 10 COMMENT '最大迭代次数',
                                `temperature` decimal(3, 2) NULL DEFAULT 0.70 COMMENT '温度参数',
                                `max_tokens` int NULL DEFAULT 4096 COMMENT '最大Token数',
                                `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'draft' COMMENT '状态(draft=草稿,published=已发布,archived=已归档)',
                                `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '扩展元数据',
                                `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_model_id`(`model_id` ASC) USING BTREE,
                                INDEX `idx_status`(`status` ASC) USING BTREE,
                                INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI智能体' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_agent
-- ----------------------------

-- ----------------------------
-- Table structure for airag_app
-- ----------------------------
DROP TABLE IF EXISTS `airag_app`;
CREATE TABLE `airag_app`  (
                              `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                              `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                              `create_time` datetime NULL DEFAULT NULL COMMENT '创建日期',
                              `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                              `update_time` datetime NULL DEFAULT NULL COMMENT '更新日期',
                              `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                              `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                              `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '应用名称',
                              `descr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '应用描述',
                              `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '应用图标',
                              `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'chat' COMMENT '应用类型(chat=普通聊天,flow=流程应用)',
                              `prologue` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '开场白',
                              `preset_question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '预设问题',
                              `prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提示词',
                              `model_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型配置',
                              `msg_num` int NULL DEFAULT 10 COMMENT '历史消息数',
                              `knowledge_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库',
                              `flow_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '流程',
                              `quick_command` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '快捷指令',
                              `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'disable' COMMENT '状态（enable=启用、disable=禁用、release=发布）',
                              `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '元数据',
                              `plugins` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '插件',
                              `iz_open_memory` int NULL DEFAULT 0 COMMENT '是否开启记忆(0 不开启，1开启)',
                              `memory_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '记忆库',
                              `variables` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '变量',
                              `memory_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '记忆和变量提示词',
                              `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `idx_name`(`name` ASC) USING BTREE,
                              INDEX `idx_status`(`status` ASC) USING BTREE,
                              INDEX `idx_create_by`(`create_by` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI应用' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_app
-- ----------------------------

-- ----------------------------
-- Table structure for airag_flow
-- ----------------------------
DROP TABLE IF EXISTS `airag_flow`;
CREATE TABLE `airag_flow`  (
                               `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                               `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                               `create_time` datetime NULL DEFAULT NULL COMMENT '创建日期',
                               `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '更新日期',
                               `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                               `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                               `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '流程名称',
                               `descr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '流程描述',
                               `flow_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '流程数据',
                               `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'disable' COMMENT '状态(enable=启用,disable=禁用)',
                               `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `idx_name`(`name` ASC) USING BTREE,
                               INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI流程' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_flow
-- ----------------------------

-- ----------------------------
-- Table structure for airag_knowledge
-- ----------------------------
DROP TABLE IF EXISTS `airag_knowledge`;
CREATE TABLE `airag_knowledge`  (
                                    `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                    `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                                    `create_time` datetime NULL DEFAULT NULL COMMENT '创建日期',
                                    `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                                    `update_time` datetime NULL DEFAULT NULL COMMENT '更新日期',
                                    `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                                    `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                                    `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库名称',
                                    `embed_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '向量模型id',
                                    `descr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
                                    `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'enable' COMMENT '状态',
                                    `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'knowledge' COMMENT '类型(knowledge知识 memory 记忆)',
                                    `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `idx_name`(`name` ASC) USING BTREE,
                                    INDEX `idx_status`(`status` ASC) USING BTREE,
                                    INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_knowledge
-- ----------------------------

-- ----------------------------
-- Table structure for airag_knowledge_doc
-- ----------------------------
DROP TABLE IF EXISTS `airag_knowledge_doc`;
CREATE TABLE `airag_knowledge_doc`  (
                                        `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                        `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                                        `create_time` datetime NULL DEFAULT NULL COMMENT '创建日期',
                                        `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                                        `update_time` datetime NULL DEFAULT NULL COMMENT '更新日期',
                                        `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                                        `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                                        `knowledge_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识库id',
                                        `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
                                        `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类型',
                                        `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
                                        `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '元数据',
                                        `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态(pending=待处理,processing=处理中,completed=已完成,failed=失败)',
                                        `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                                        PRIMARY KEY (`id`) USING BTREE,
                                        INDEX `idx_knowledge_id`(`knowledge_id` ASC) USING BTREE,
                                        INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI知识库文档' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_knowledge_doc
-- ----------------------------

-- ----------------------------
-- Table structure for airag_mcp
-- ----------------------------
DROP TABLE IF EXISTS `airag_mcp`;
CREATE TABLE `airag_mcp`  (
                              `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                              `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                              `create_time` datetime NULL DEFAULT NULL COMMENT '创建日期',
                              `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                              `update_time` datetime NULL DEFAULT NULL COMMENT '更新日期',
                              `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                              `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                              `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '插件名称',
                              `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '插件描述',
                              `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'stdio' COMMENT '插件类型(stdio,sse,http)',
                              `command` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '命令/URL',
                              `args` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '参数',
                              `env` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '环境变量',
                              `status` int NULL DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
                              `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `idx_name`(`name` ASC) USING BTREE,
                              INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'MCP插件配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_mcp
-- ----------------------------

-- ----------------------------
-- Table structure for airag_message
-- ----------------------------
DROP TABLE IF EXISTS `airag_message`;
CREATE TABLE `airag_message`  (
                                  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
                                  `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                                  `session_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
                                  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色(system/user/assistant/tool)',
                                  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
                                  `reasoning_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '推理内容(DeepSeek等支持)',
                                  `tool_calls` json NULL COMMENT '工具调用JSON',
                                  `tool_call_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工具调用ID',
                                  `tool_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工具名称',
                                  `input_tokens` int NULL DEFAULT NULL COMMENT '输入Token数',
                                  `output_tokens` int NULL DEFAULT NULL COMMENT '输出Token数',
                                  `total_tokens` int NULL DEFAULT NULL COMMENT '总Token数',
                                  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
                                  `response_time` bigint NULL DEFAULT NULL COMMENT '响应时间(毫秒)',
                                  `status` tinyint NULL DEFAULT 0 COMMENT '消息状态(0=成功,1=失败,2=中断)',
                                  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
                                  `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '扩展元数据',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
                                  INDEX `idx_role`(`role` ASC) USING BTREE,
                                  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
                                  INDEX `idx_session_create`(`session_id` ASC, `create_time` ASC) USING BTREE,
                                  CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `airag_session` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_message
-- ----------------------------

-- ----------------------------
-- Table structure for airag_model
-- ----------------------------
DROP TABLE IF EXISTS `airag_model`;
CREATE TABLE `airag_model`  (
                                `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                                `create_time` datetime NULL DEFAULT NULL COMMENT '创建日期',
                                `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                                `update_time` datetime NULL DEFAULT NULL COMMENT '更新日期',
                                `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                                `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                                `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
                                `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应者(openai,ollama,zhipu,qianfan,dashscope,anthropic,deepseek)',
                                `model_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'LLM' COMMENT '模型类型(LLM=大语言模型,EMBED=向量模型)',
                                `model_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
                                `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'API域名',
                                `credential` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '凭证信息',
                                `model_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '模型参数',
                                `max_context_length` int NULL DEFAULT 4096 COMMENT '最大上下文长度',
                                `supports_streaming` tinyint NULL DEFAULT 1 COMMENT '是否支持流式输出',
                                `supports_function_call` tinyint NULL DEFAULT 0 COMMENT '是否支持函数调用',
                                `supports_vision` tinyint NULL DEFAULT 0 COMMENT '是否支持视觉能力',
                                `activate_flag` int NULL DEFAULT 0 COMMENT '是否激活(0=未激活,1=已激活)',
                                `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `idx_provider`(`provider` ASC) USING BTREE,
                                INDEX `idx_model_type`(`model_type` ASC) USING BTREE,
                                INDEX `idx_activate`(`activate_flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI模型配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_model
-- ----------------------------
INSERT INTO `airag_model` VALUES ('1', NULL, '2026-03-28 22:50:52', NULL, NULL, NULL, NULL, 'DeepSeek Chat', 'deepseek', 'LLM', 'deepseek-chat', 'https://api.deepseek.com', NULL, NULL, 4096, 1, 0, 0, 1, 0);
INSERT INTO `airag_model` VALUES ('2', NULL, '2026-03-28 22:50:52', NULL, NULL, NULL, NULL, 'DeepSeek Coder', 'deepseek', 'LLM', 'deepseek-coder', 'https://api.deepseek.com', NULL, NULL, 4096, 1, 0, 0, 0, 0);
INSERT INTO `airag_model` VALUES ('3', NULL, '2026-03-28 22:50:52', NULL, NULL, NULL, NULL, 'Ollama Llama3', 'ollama', 'LLM', 'llama3', 'http://localhost:11434', NULL, NULL, 4096, 1, 0, 0, 0, 0);
INSERT INTO `airag_model` VALUES ('4', NULL, '2026-03-28 22:50:52', NULL, NULL, NULL, NULL, 'OpenAI GPT-4', 'openai', 'LLM', 'gpt-4', 'https://api.openai.com', NULL, NULL, 4096, 1, 0, 0, 0, 0);
INSERT INTO `airag_model` VALUES ('5', NULL, '2026-03-28 22:50:52', NULL, NULL, NULL, NULL, '智谱 GLM-4', 'zhipu', 'LLM', 'glm-4', 'https://open.bigmodel.cn', NULL, NULL, 4096, 1, 0, 0, 0, 0);
INSERT INTO `airag_model` VALUES ('6', NULL, '2026-03-28 22:50:52', NULL, NULL, NULL, NULL, '通义千问', 'dashscope', 'LLM', 'qwen-max', 'https://dashscope.aliyuncs.com', NULL, NULL, 4096, 1, 0, 0, 0, 0);
INSERT INTO `airag_model` VALUES ('7', NULL, '2026-03-28 22:50:52', NULL, NULL, NULL, NULL, '百度千帆', 'qianfan', 'LLM', 'ERNIE-Bot-4', 'https://aip.baidubce.com', NULL, NULL, 4096, 1, 0, 0, 0, 0);
INSERT INTO `airag_model` VALUES ('8', NULL, '2026-03-28 22:50:52', NULL, NULL, NULL, NULL, 'Claude', 'anthropic', 'LLM', 'claude-3-opus', 'https://api.anthropic.com', NULL, NULL, 4096, 1, 0, 0, 0, 0);

-- ----------------------------
-- Table structure for airag_prompts
-- ----------------------------
DROP TABLE IF EXISTS `airag_prompts`;
CREATE TABLE `airag_prompts`  (
                                  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                                  `create_time` datetime NULL DEFAULT NULL COMMENT '创建日期',
                                  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                                  `update_time` datetime NULL DEFAULT NULL COMMENT '更新日期',
                                  `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                                  `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                                  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提示词名称',
                                  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '提示词内容',
                                  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提示词类型',
                                  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '描述',
                                  `status` int NULL DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
                                  `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_name`(`name` ASC) USING BTREE,
                                  INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI提示词' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_prompts
-- ----------------------------

-- ----------------------------
-- Table structure for airag_session
-- ----------------------------
DROP TABLE IF EXISTS `airag_session`;
CREATE TABLE `airag_session`  (
                                  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                                  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
                                  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                                  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
                                  `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                                  `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                                  `app_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '应用ID',
                                  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
                                  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '新会话' COMMENT '会话标题',
                                  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话摘要',
                                  `msg_count` int NULL DEFAULT 0 COMMENT '消息数量',
                                  `total_tokens` int NULL DEFAULT 0 COMMENT '总Token数',
                                  `last_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '最后一条消息',
                                  `last_active_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
                                  `status` tinyint NULL DEFAULT 0 COMMENT '状态(0=正常,1=已删除,2=已归档)',
                                  `metadata` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '扩展元数据',
                                  `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                  INDEX `idx_app_id`(`app_id` ASC) USING BTREE,
                                  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
                                  INDEX `idx_last_active`(`last_active_time` ASC) USING BTREE,
                                  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_session
-- ----------------------------

-- ----------------------------
-- Table structure for airag_tool
-- ----------------------------
DROP TABLE IF EXISTS `airag_tool`;
CREATE TABLE `airag_tool`  (
                               `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                               `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
                               `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
                               `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
                               `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
                               `sys_org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
                               `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '租户id',
                               `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工具名称',
                               `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'custom' COMMENT '工具类型(builtin/http/script/mcp/custom)',
                               `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工具描述',
                               `input_schema` json NULL COMMENT '输入参数JSON Schema',
                               `output_schema` json NULL COMMENT '输出参数JSON Schema',
                               `config` json NULL COMMENT '工具配置',
                               `enabled` tinyint NULL DEFAULT 1 COMMENT '是否启用(0=禁用,1=启用)',
                               `deleted_flag` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0=正常,1=已删除)',
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_name`(`name` ASC, `tenant_id` ASC) USING BTREE,
                               INDEX `idx_type`(`type` ASC) USING BTREE,
                               INDEX `idx_enabled`(`enabled` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI工具定义' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of airag_tool
-- ----------------------------
INSERT INTO `airag_tool` VALUES ('1', NULL, '2026-03-28 22:50:52', NULL, '2026-03-28 22:50:52', NULL, NULL, 'web_search', 'builtin', '网络搜索工具，用于搜索互联网信息', '{\"type\": \"object\", \"required\": [\"query\"], \"properties\": {\"query\": {\"type\": \"string\", \"description\": \"搜索关键词\"}}}', NULL, NULL, 1, 0);
INSERT INTO `airag_tool` VALUES ('2', NULL, '2026-03-28 22:50:52', NULL, '2026-03-28 22:50:52', NULL, NULL, 'calculator', 'builtin', '数学计算工具，用于执行数学运算', '{\"type\": \"object\", \"required\": [\"expression\"], \"properties\": {\"expression\": {\"type\": \"string\", \"description\": \"数学表达式\"}}}', NULL, NULL, 1, 0);
INSERT INTO `airag_tool` VALUES ('3', NULL, '2026-03-28 22:50:52', NULL, '2026-03-28 22:50:52', NULL, NULL, 'code_interpreter', 'builtin', '代码解释器，用于执行Python代码', '{\"type\": \"object\", \"required\": [\"code\"], \"properties\": {\"code\": {\"type\": \"string\", \"description\": \"Python代码\"}}}', NULL, NULL, 1, 0);
INSERT INTO `airag_tool` VALUES ('4', NULL, '2026-03-28 22:50:52', NULL, '2026-03-28 22:50:52', NULL, NULL, 'database_query', 'builtin', '数据库查询工具，用于执行SQL查询', '{\"type\": \"object\", \"required\": [\"sql\"], \"properties\": {\"sql\": {\"type\": \"string\", \"description\": \"SQL查询语句\"}}}', NULL, NULL, 0, 0);

SET FOREIGN_KEY_CHECKS = 1;
