/** OCR 配置分组类型（对应 sys_config.param_type） */
export const OCR_PARAM_TYPE = 'ocr'
export const OCR_PARAM_TYPE_DESC = 'OCR 搜题'

/** OCR 搜题接口（uniapp 调用） */
export const APP_OCR_SEARCH_API = 'POST /app/ocr/search'

/** OCR 供应商选项（与 server OcrPlatformCodeEnums 一致） */
export const OCR_PLATFORM_OPTIONS = [
  { value: 'local_sidecar', label: '本地 sidecar（开发/测试）' },
  { value: 'baidu', label: '百度通用 OCR' },
]

export const OCR_CONFIG_SECTIONS = [
  {
    id: 'ocr',
    title: 'OCR 配置',
    items: [
      {
        key: 'ocr_platform',
        label: 'OCR 供应商',
        defaultValue: 'local_sidecar',
        type: 'select',
        options: OCR_PLATFORM_OPTIONS,
        required: true,
      },
      {
        key: 'ocr_daily_limit',
        label: '每日免费次数',
        defaultValue: '5',
        type: 'number',
        min: 0,
        max: 999,
        step: 1,
        required: true,
      },
      {
        key: 'ocr_similarity_threshold',
        label: '相似度阈值',
        defaultValue: '0.08',
        type: 'number',
        min: 0,
        max: 1,
        step: 0.01,
        precision: 2,
        required: true,
      },
      {
        key: 'ocr_baidu_api_key',
        label: '百度 API Key',
        defaultValue: '',
        type: 'text',
        maxLength: 128,
        requiredWhen: (form) => form.ocr_platform === 'baidu',
        showWhen: (form) => form.ocr_platform === 'baidu',
      },
      {
        key: 'ocr_baidu_secret_key',
        label: '百度 Secret Key',
        defaultValue: '',
        type: 'password',
        maxLength: 128,
        requiredWhen: (form) => form.ocr_platform === 'baidu',
        showWhen: (form) => form.ocr_platform === 'baidu',
      },
    ],
  },
]

export const OCR_CONFIG_DEFS = OCR_CONFIG_SECTIONS.flatMap((section) => section.items)
