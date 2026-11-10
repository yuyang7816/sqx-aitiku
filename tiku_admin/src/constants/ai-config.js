/** AI 答疑配置分组类型（对应 sys_config.param_type） */
export const AI_PARAM_TYPE = 'ai'
export const AI_PARAM_TYPE_DESC = 'AI 答疑'

/** AI 对话接口（uniapp 调用） */
export const APP_AI_CHAT_API = 'POST /app/ai/chat'

/** AI 供应商选项（与 server AiPlatformCodeEnums 一致） */
export const AI_PLATFORM_OPTIONS = [
  { value: 'local_template', label: '本地模板回复（开发/测试）' },
  { value: 'doubao', label: '豆包大模型' },
]

export const AI_CONFIG_SECTIONS = [
  {
    id: 'ai',
    title: 'AI 答疑',
    items: [
      {
        key: 'ai_platform',
        label: 'AI 供应商',
        defaultValue: 'local_template',
        type: 'select',
        options: AI_PLATFORM_OPTIONS,
        required: true,
      },
      {
        key: 'ai_daily_limit',
        label: '每日免费次数',
        defaultValue: '5',
        type: 'number',
        min: 0,
        max: 999,
        step: 1,
        required: true,
      },
      {
        key: 'ai_sensitive_words',
        label: '敏感词列表',
        defaultValue: '赌博,色情,暴力,诈骗',
        type: 'textarea',
        maxLength: 2000,
        required: true,
      },
      {
        key: 'ai_doubao_api_key',
        label: '豆包 API Key',
        defaultValue: '',
        type: 'password',
        maxLength: 256,
        requiredWhen: (form) => form.ai_platform === 'doubao',
        showWhen: (form) => form.ai_platform === 'doubao',
      },
      {
        key: 'ai_doubao_model',
        label: '豆包模型 ID',
        defaultValue: 'doubao-pro-32k',
        type: 'text',
        maxLength: 128,
        showWhen: (form) => form.ai_platform === 'doubao',
      },
      {
        key: 'ai_doubao_endpoint',
        label: '豆包接口地址',
        defaultValue: 'https://ark.cn-beijing.volces.com/api/v3/chat/completions',
        type: 'text',
        maxLength: 512,
        showWhen: (form) => form.ai_platform === 'doubao',
      },
    ],
  },
]

export const AI_CONFIG_DEFS = AI_CONFIG_SECTIONS.flatMap((section) => section.items)
