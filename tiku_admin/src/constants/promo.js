/** 提现记录状态：与 withdraw_record.status 一致 */
export const WITHDRAW_STATUS_OPTIONS = [
  { label: '待审核', value: 1 },
  { label: '打款成功', value: 2 },
  { label: '已拒绝', value: 3 },
]

/** 提现渠道：与 withdraw_record.account_type 一致 */
export const WITHDRAW_ACCOUNT_TYPE_OPTIONS = [
  { label: '微信零钱', value: 1 },
  { label: '支付宝', value: 2 },
]

/** 下级成员付费状态筛选项 */
export const REFERRAL_MEMBER_PAY_OPTIONS = [
  { label: '已付费', value: 1 },
  { label: '未付费', value: 0 },
]

export function getWithdrawStatusLabel(status) {
  return WITHDRAW_STATUS_OPTIONS.find((item) => item.value === status)?.label || '未知'
}

export function getWithdrawStatusTagType(status) {
  if (status === 1) return 'warning'
  if (status === 2) return 'success'
  if (status === 3) return 'danger'
  return 'info'
}

export function getWithdrawAccountTypeLabel(type) {
  return WITHDRAW_ACCOUNT_TYPE_OPTIONS.find((item) => item.value === type)?.label || '未知'
}

export function resolveMemberPayStatus(row) {
  return row?.isVip === 1 ? '已付费' : '未付费'
}

export function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '-'
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}
