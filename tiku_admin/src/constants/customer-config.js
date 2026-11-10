/** 客服配置分组类型（对应 sys_config.param_type） */
export const CUSTOMER_PARAM_TYPE = 'app_customer'
export const CUSTOMER_PARAM_TYPE_DESC = '客服配置'

/** 移动端结构化拉取接口 */
export const APP_CONFIG_CUSTOMER_API = 'GET /app/config/customer'

/** 客服类型选项（与 server CustomerConfigVO.type 一致） */
export const KEFU_TYPE_OPTIONS = [
  { value: 'wework', label: '企业微信在线客服' },
  { value: 'phone', label: '电话客服' },
  { value: 'im', label: '在线 IM 聊天' },
  { value: 'wechat', label: '微信客服（二维码/微信号）' },
]

/** 客服入口开关配置键（sys_config，原启动配置项） */
export const KEFU_ENABLED_KEY = 'app_startup_kefu_enabled'

export const CUSTOMER_CONFIG_SECTIONS = [
  {
    id: 'basic',
    title: '客服配置',
    items: [
      {
        key: KEFU_ENABLED_KEY,
        label: '客服入口开关',
        defaultValue: '1',
        type: 'switch',
      },
      {
        key: 'app_kefu_type',
        label: '默认客服方式',
        defaultValue: 'wework',
        type: 'select',
        options: KEFU_TYPE_OPTIONS,
        required: true,
      },
    ],
  },
  {
    id: 'contact',
    title: '电话 / 企微',
    items: [
      {
        key: 'app_kefu_phone',
        label: '客服电话',
        defaultValue: '',
        type: 'text',
        maxLength: 32,
        getRules: (form) => (
          form.app_kefu_type === 'phone'
            ? [{ required: true, message: '请填写客服电话', trigger: 'blur' }]
            : []
        ),
      },
      {
        key: 'app_kefu_wework_url',
        label: '企微客服链接',
        defaultValue: '',
        type: 'text',
        maxLength: 512,
        getRules: (form) => (
          form.app_kefu_type === 'wework'
            ? [{ required: true, message: '请填写企微客服链接', trigger: 'blur' }]
            : []
        ),
      },
      {
        key: 'app_kefu_wework_appid',
        label: '企微 AppId',
        defaultValue: '',
        type: 'text',
        maxLength: 64,
      },
    ],
  },
  {
    id: 'im',
    title: '在线 IM',
    items: [
      {
        key: 'app_kefu_im_enabled',
        label: '在线 IM 开关',
        defaultValue: '0',
        type: 'switch',
      },
      {
        key: 'app_kefu_im_welcome',
        label: 'IM 欢迎语',
        defaultValue: '您好，我是在线客服，请问有什么可以帮您？',
        type: 'textarea',
        rows: 2,
        maxLength: 200,
      },
    ],
  },
  {
    id: 'wechat',
    title: '微信客服',
    items: [
      {
        key: 'app_kefu_wechat_id',
        label: '客服微信号',
        defaultValue: '',
        type: 'text',
        maxLength: 64,
        getRules: (form) => (
          form.app_kefu_type === 'wechat' && !String(form.app_kefu_wechat_qr_url || '').trim()
            ? [{ required: true, message: '请填写微信号或上传二维码', trigger: 'blur' }]
            : []
        ),
      },
      {
        key: 'app_kefu_wechat_qr_url',
        label: '微信二维码',
        defaultValue: '',
        type: 'image',
      },
    ],
  },
]

export const CUSTOMER_CONFIG_DEFS = CUSTOMER_CONFIG_SECTIONS.flatMap((section) => section.items)
