/** 会员配置分组类型（对应 sys_config.param_type） */
export const VIP_PARAM_TYPE = 'vip'
export const VIP_PARAM_TYPE_DESC = '会员'

/** 会员 App 接口（uniapp 调用） */
export const APP_VIP_PLAN_API = 'GET /app/vipPlan'
export const APP_VIP_PAY_API = 'POST /app/pay'

/** 会员配置项定义（param_key 写入 sys_config） */
export const VIP_CONFIG_DEFS = [
  {
    key: 'vip_renew_notice_days',
    label: '续费提醒提前天数',
    remark: '会员到期前 N 天推送 notice-member 站内消息（对应 sys_config.vip_renew_notice_days，由服务端 VipRenewConfigReader 读取）',
    defaultValue: '3',
    type: 'number',
    min: 1,
    max: 30,
    step: 1,
  },
]
