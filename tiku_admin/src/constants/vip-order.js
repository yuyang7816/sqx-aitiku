export const VIP_ORDER_PAY_STATUS_OPTIONS = [
  { label: '待支付', value: 0, tagType: 'warning' },
  { label: '已支付', value: 1, tagType: 'success' },
  { label: '已关闭', value: 2, tagType: 'info' },
]

export function getVipOrderPayStatusLabel(status) {
  return VIP_ORDER_PAY_STATUS_OPTIONS.find((item) => item.value === status)?.label || '未知'
}

export function getVipOrderPayStatusTagType(status) {
  return VIP_ORDER_PAY_STATUS_OPTIONS.find((item) => item.value === status)?.tagType || 'info'
}

export const VIP_ORDER_DETAIL_FIELDS = [
  { key: 'id', label: '订单 ID' },
  { key: 'orderNo', label: '订单号' },
  { key: 'userId', label: '用户 ID' },
  { key: 'userName', label: '用户名' },
  { key: 'nickname', label: '昵称' },
  { key: 'phone', label: '手机号' },
  { key: 'planName', label: '套餐名称' },
  { key: 'planDays', label: '有效天数' },
  { key: 'amount', label: '订单金额', formatter: (value) => (value == null ? '—' : `¥${value}`) },
  { key: 'payChannel', label: '支付渠道' },
  { key: 'payStatus', label: '支付状态', formatter: (value) => getVipOrderPayStatusLabel(value) },
  { key: 'tradeNo', label: '第三方流水号' },
  { key: 'createTime', label: '创建时间' },
  { key: 'payTime', label: '支付时间' },
  { key: 'updateTime', label: '更新时间' },
  { key: 'userVipExpireAt', label: '用户会员到期' },
]
