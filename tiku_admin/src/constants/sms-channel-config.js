/** 短信通道配置（对应 sys_config.param_type = sys_sms） */
export const SMS_CHANNEL_PARAM_TYPE = 'sys_sms'
export const SMS_CHANNEL_PARAM_TYPE_DESC = '短信配置'

/** 与 server SmsProviderEnum 一致 */
export const SMS_PROVIDER_OPTIONS = [
  { value: '0', label: 'Mock（开发环境，验证码固定 123456）' },
  { value: '3', label: '短信宝（生产推荐）' },
  { value: '2', label: '阿里云短信' },
  { value: '1', label: '腾讯云短信' },
]

export const SMS_CHANNEL_CONFIG_SECTIONS = [
  {
    id: 'provider',
    title: '短信通道',
    items: [
      {
        key: 'sms_provider',
        label: '短信供应商',
        defaultValue: '3',
        type: 'select',
        options: SMS_PROVIDER_OPTIONS,
        required: true,
      },
      {
        key: 'sms_smsbao_username',
        label: '短信宝账号',
        defaultValue: '',
        type: 'text',
        maxLength: 64,
        showWhen: (form) => String(form.sms_provider) === '3',
      },
      {
        key: 'sms_smsbao_password',
        label: '短信宝密码',
        defaultValue: '',
        type: 'password',
        maxLength: 128,
        showWhen: (form) => String(form.sms_provider) === '3',
      },
    ],
  },
]

export const SMS_CHANNEL_CONFIG_DEFS = SMS_CHANNEL_CONFIG_SECTIONS.flatMap((section) => section.items)
