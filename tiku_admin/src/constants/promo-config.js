/** 推广配置分组类型（对应 sys_config.param_type） */
export const PROMO_PARAM_TYPE = 'referral'
export const PROMO_PARAM_TYPE_DESC = '推广'

/** 推广 App 接口（uniapp 调用，与 server Controller 一致） */
export const APP_REFERRAL_SUMMARY_API = 'GET /app/referral/userCommission'
export const APP_WITHDRAW_APPLY_API = 'POST /app/withdrawRecord'

export const PROMO_CONFIG_SECTIONS = [
  {
    id: 'commission',
    title: '邀请奖励',
    items: [
      {
        key: 'biz_referral_commission_rate_level1',
        fallbackKey: 'biz_referral_commission_rate',
        label: '一级佣金',
        unit: '%',
        display: 'percent',
        defaultValue: '0.10',
        type: 'number',
        min: 0,
        max: 100,
        step: 0.1,
        precision: 1,
        required: true,
      },
      {
        key: 'biz_referral_commission_rate_level2',
        label: '二级佣金',
        unit: '%',
        display: 'percent',
        defaultValue: '0.05',
        type: 'number',
        min: 0,
        max: 100,
        step: 0.1,
        precision: 1,
        required: true,
      },
    ],
  },
  {
    id: 'poster',
    title: '邀请海报',
    items: [
      {
        key: 'biz_referral_poster_image',
        label: '海报底图',
        defaultValue: '',
        type: 'image',
        required: true,
        remark: '推广海报背景图（含主视觉与卖点，建议底部预留约 220px 浅色区域供客户端合成昵称/邀请码/二维码）',
      },
      {
        key: 'sys_invite_text',
        label: '邀请语',
        defaultValue: '扫码体验 AI 智能提分',
        type: 'textarea',
        rows: 2,
        maxLength: 80,
        required: true,
        remark: '展示在海报底部与分享文案中',
      },
    ],
  },
]

export const PROMO_CONFIG_DEFS = PROMO_CONFIG_SECTIONS.flatMap((section) => section.items)

export function percentToStored(value, display) {
  const num = Number(value)
  if (Number.isNaN(num)) return '0'
  if (display === 'percent') {
    return String(num / 100)
  }
  return String(num)
}

export function storedToPercent(value, display) {
  const num = Number(value)
  if (Number.isNaN(num)) return 0
  if (display === 'percent') {
    return Number((num * 100).toFixed(4))
  }
  return num
}
