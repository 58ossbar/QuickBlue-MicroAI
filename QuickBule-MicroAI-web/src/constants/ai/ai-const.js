/**
 * AI 模块常量
 */

// AI模型供应商枚举
export const AI_MODEL_PROVIDER_ENUM = {
    OPENAI: { value: 'openai', desc: 'OpenAI' },
    OLLAMA: { value: 'ollama', desc: 'Ollama' },
    ZHIPU: { value: 'zhipu', desc: '智谱 AI' },
    QIANFAN: { value: 'qianfan', desc: '百度文心' },
    DASHSCOPE: { value: 'dashscope', desc: '通义千问' },
    ANTHROPIC: { value: 'anthropic', desc: 'Anthropic' },
    DEEPSEEK: { value: 'deepseek', desc: 'DeepSeek' },
};

// AI模型类型枚举
export const AI_MODEL_TYPE_ENUM = {
    CHAT: { value: 'chat', desc: '对话' },
    EMBEDDING: { value: 'embedding', desc: '向量' },
    COMPLETION: { value: 'completion', desc: '文本补全' },
    LLM: { value: 'LLM', desc: '大语言模型' },
    EMBED: { value: 'EMBED', desc: '向量模型' },
};

// AI应用类型枚举
export const AI_APP_TYPE_ENUM = {
    CHAT: { value: 'chat', desc: '普通聊天' },
    FLOW: { value: 'flow', desc: '流程应用' },
};

// AI应用状态枚举
export const AI_APP_STATUS_ENUM = {
    ENABLE: { value: 'enable', desc: '启用' },
    DISABLE: { value: 'disable', desc: '禁用' },
    RELEASE: { value: 'release', desc: '发布' },
};

// AI知识库类型枚举
export const AI_KNOWLEDGE_TYPE_ENUM = {
    KNOWLEDGE: { value: 'knowledge', desc: '知识' },
    MEMORY: { value: 'memory', desc: '记忆' },
};

// AI知识库状态枚举
export const AI_KNOWLEDGE_STATUS_ENUM = {
    ENABLE: { value: 'enable', desc: '启用' },
    DISABLE: { value: 'disable', desc: '禁用' },
};

// AI文档处理状态枚举
export const AI_DOC_STATUS_ENUM = {
    PENDING: { value: 'pending', desc: '待处理' },
    PROCESSING: { value: 'processing', desc: '处理中' },
    COMPLETED: { value: 'completed', desc: '已完成' },
    FAILED: { value: 'failed', desc: '失败' },
};

// AI Agent 状态枚举
export const AI_AGENT_STATUS_ENUM = {
    DRAFT: { value: 'draft', desc: '草稿' },
    PUBLISHED: { value: 'published', desc: '已发布' },
    ENABLED: { value: 'enabled', desc: '已启用' },
    DISABLED: { value: 'disabled', desc: '已禁用' },
};
