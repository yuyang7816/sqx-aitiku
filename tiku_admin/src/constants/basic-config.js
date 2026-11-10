/** 基础系统配置（对应 sys_config.param_type = sys / pay 部分键） */
export const BASIC_PARAM_TYPE = 'sys'
export const BASIC_PARAM_TYPE_DESC = '系统配置'

export const BASIC_CONFIG_SECTIONS = [
  {
    id: 'service',
    title: '服务信息',
    items: [
      {
        key: 'sys_server_name',
        label: '服务名称',
        defaultValue: 'AI智能提分',
        type: 'text',
        required: true,
        maxLength: 32,
      },
    ],
  },
  {
    id: 'domain',
    title: '域名配置',
    items: [
      {
        key: 'sys_h5_domain',
        label: '用户端域名',
        defaultValue: '',
        type: 'text',
        required: true,
        maxLength: 256,
      },
      {
        key: 'sys_admin_domain',
        label: '管理端域名',
        defaultValue: '',
        type: 'text',
        maxLength: 256,
      },
      {
        key: 'file_public_base_url',
        label: 'API 公网前缀',
        defaultValue: '',
        type: 'text',
        maxLength: 256,
      },
    ],
  },
]

export const BASIC_CONFIG_DEFS = BASIC_CONFIG_SECTIONS.flatMap((section) => section.items)
