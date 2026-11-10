/** 登录认证配置（对应 sys_config.param_type = sys_auth / sys_wechat） */
export const AUTH_PARAM_TYPE = 'sys_auth'
export const AUTH_PARAM_TYPE_DESC = '登录认证'

/** 保存登录 AppId/Secret 时同步到支付与兼容键，避免重复填写 */
export const AUTH_CONFIG_MIRROR_KEYS = {
  sys_user_ma_appid: ['sys_wechat_jsapi_appid', 'pay_wx_jsapi_app_id'],
  sys_user_ma_secret: ['sys_wechat_jsapi_secret'],
  sys_user_mp_appid: ['sys_wechat_h5_appid', 'pay_wx_h5_app_id'],
  sys_user_mp_secret: ['sys_wechat_h5_secret'],
  sys_user_app_appid: ['sys_wechat_app_appid', 'pay_wx_app_id'],
}

export const AUTH_CONFIG_SECTIONS = [
  {
    id: 'miniProgram',
    title: '微信小程序',
    items: [
      {
        key: 'sys_user_ma_appid',
        label: '小程序 AppId',
        defaultValue: '',
        type: 'text',
        required: true,
        maxLength: 64,
      },
      {
        key: 'sys_user_ma_secret',
        label: '小程序 Secret',
        defaultValue: '',
        type: 'password',
        required: true,
        maxLength: 128,
      },
    ],
  },
  {
    id: 'mp',
    title: '微信公众号 / H5',
    items: [
      {
        key: 'sys_user_mp_login_enabled',
        label: '开启公众号授权登录',
        defaultValue: '0',
        type: 'switch',
        remark: 'H5 微信内置浏览器是否展示「微信一键登录」；关闭时 C 端不展示公众号登录，且微信内不展示微信支付',
      },
      {
        key: 'sys_user_mp_appid',
        label: '公众号 AppId',
        defaultValue: '',
        type: 'text',
        maxLength: 64,
      },
      {
        key: 'sys_user_mp_secret',
        label: '公众号 Secret',
        defaultValue: '',
        type: 'password',
        maxLength: 128,
      },
    ],
  },
  {
    id: 'app',
    title: '移动应用 APP',
    items: [
      {
        key: 'sys_user_app_appid',
        label: '移动应用 AppId',
        defaultValue: '',
        type: 'text',
        maxLength: 64,
      },
      {
        key: 'sys_user_app_secret',
        label: '移动应用 Secret',
        defaultValue: '',
        type: 'password',
        maxLength: 128,
      },
    ],
  },
]

export const AUTH_CONFIG_DEFS = AUTH_CONFIG_SECTIONS.flatMap((section) => section.items)
