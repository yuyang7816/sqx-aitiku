/** 支付配置（对应 sys_config.param_type = pay） */
export const PAY_PARAM_TYPE = 'pay'
export const PAY_PARAM_TYPE_DESC = '支付配置'

export const PAY_ALI_MODE_OPTIONS = [
  { value: '1', label: '公钥模式' },
  { value: '2', label: '证书模式' },
]

export const PAY_ALI_SANDBOX_OPTIONS = [
  { value: '1', label: '沙箱环境' },
  { value: '2', label: '正式环境' },
]

export const PAY_CONFIG_SECTIONS = [
  {
    id: 'wechat',
    title: '微信支付',
    items: [
      {
        key: 'pay_wechat_mch_id',
        label: '微信商户号',
        defaultValue: '',
        type: 'text',
        required: true,
        maxLength: 32,
      },
      {
        key: 'pay_wechat_mch_key',
        label: '微信商户 API Key',
        defaultValue: '',
        type: 'password',
        required: true,
        maxLength: 128,
      },
      {
        key: 'sys_wx_key_path',
        label: '微信 p12 证书路径',
        defaultValue: '',
        type: 'text',
        maxLength: 256,
      },
    ],
  },
  {
    id: 'alipay',
    title: '支付宝',
    items: [
      {
        key: 'pay_ali_appid',
        label: '支付宝 AppId',
        defaultValue: '',
        type: 'text',
        maxLength: 64,
      },
      {
        key: 'pay_ali_mode',
        label: '接入模式',
        defaultValue: '1',
        type: 'select',
        options: PAY_ALI_MODE_OPTIONS,
      },
      {
        key: 'pay_ali_sandbox',
        label: '运行环境',
        defaultValue: '2',
        type: 'select',
        options: PAY_ALI_SANDBOX_OPTIONS,
      },
      {
        key: 'pay_ali_public_key',
        label: '支付宝公钥',
        defaultValue: '',
        type: 'textarea',
        rows: 4,
      },
      {
        key: 'pay_ali_private_key',
        label: '应用私钥',
        defaultValue: '',
        type: 'textarea',
        rows: 6,
      },
    ],
  },
]

export const PAY_CONFIG_DEFS = PAY_CONFIG_SECTIONS.flatMap((section) => section.items)
