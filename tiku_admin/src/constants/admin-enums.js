/** 钱包流水 · 交易类型 */
export const WALLET_TRANSACTION_TYPE_MAP = {
  1: '充值',
  2: '消费',
  3: '退款',
  4: '提现',
  5: '系统调整',
  6: '退保证金',
}

/** 钱包流水 · 业务类型 */
export const WALLET_BIZ_TYPE_MAP = {
  1: '会员购买',
  2: '订单支付',
  3: '活动奖励',
  5: '退款',
  6: '退保证金',
}

/** 钱包流水 · 资金流向 */
export const WALLET_DIRECTION_MAP = {
  1: '收入',
  2: '支出',
}

/** 钱包流水 · 状态 */
export const WALLET_TRANSACTION_STATUS_MAP = {
  1: '待处理',
  2: '已完成',
  3: '已取消',
  4: '失败',
}

/** 提现账户类型 */
export const WITHDRAWAL_ACCOUNT_TYPE_MAP = {
  1: '支付宝',
  2: '微信',
}

/** 提现记录 · 账户类型 */
export const WITHDRAW_ACCOUNT_TYPE_MAP = {
  1: '微信',
  2: '支付宝',
}

/** 提现记录 · 状态 */
export const WITHDRAW_RECORD_STATUS_MAP = {
  1: '发起提现',
  2: '提现成功',
  3: '拒绝提现',
}

/** 分销佣金 · 类型 */
export const REFERRAL_COMMISSION_TYPE_MAP = {
  0: '提现',
  1: '订单佣金',
}

/** 分销佣金 · 方向（与钱包流水一致） */
export const REFERRAL_COMMISSION_DIRECTION_MAP = {
  1: '收入',
  2: '支出',
}

export function mapEnumLabel(map, value) {
  if (value === null || value === undefined || value === '') {
    return '—'
  }
  return map[value] ?? map[String(value)] ?? '—'
}

export function buildEnumOptions(map) {
  return Object.entries(map).map(([value, label]) => ({
    label,
    value: Number.isNaN(Number(value)) ? value : Number(value),
  }))
}

export const WALLET_TRANSACTION_TYPE_OPTIONS = buildEnumOptions(WALLET_TRANSACTION_TYPE_MAP)
export const WALLET_BIZ_TYPE_OPTIONS = buildEnumOptions(WALLET_BIZ_TYPE_MAP)
export const WALLET_DIRECTION_OPTIONS = buildEnumOptions(WALLET_DIRECTION_MAP)
export const WALLET_TRANSACTION_STATUS_OPTIONS = buildEnumOptions(WALLET_TRANSACTION_STATUS_MAP)
export const WITHDRAWAL_ACCOUNT_TYPE_OPTIONS = buildEnumOptions(WITHDRAWAL_ACCOUNT_TYPE_MAP)
export const WITHDRAW_ACCOUNT_TYPE_OPTIONS = buildEnumOptions(WITHDRAW_ACCOUNT_TYPE_MAP)
export const WITHDRAW_RECORD_STATUS_OPTIONS = buildEnumOptions(WITHDRAW_RECORD_STATUS_MAP)
export const REFERRAL_COMMISSION_TYPE_OPTIONS = buildEnumOptions(REFERRAL_COMMISSION_TYPE_MAP)
export const REFERRAL_COMMISSION_DIRECTION_OPTIONS = buildEnumOptions(REFERRAL_COMMISSION_DIRECTION_MAP)

/** 通用用户筛选项 */
export const USER_FILTER_FIELDS = [
  { key: 'userName', label: '用户名', type: 'text', placeholder: '模糊匹配用户名/昵称' },
  { key: 'phone', label: '手机号', type: 'text', placeholder: '请输入手机号' },
]

export function formatEnumColumn(map) {
  return (value) => mapEnumLabel(map, value)
}
