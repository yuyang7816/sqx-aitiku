import { getNestedValue } from '@/utils/record-detail'

/** 提现账户类型：2 = 微信收款码 */
export const WITHDRAWAL_ACCOUNT_TYPE_WECHAT = 2

export function isWechatWithdrawalType(type) {
  return Number(type) === WITHDRAWAL_ACCOUNT_TYPE_WECHAT
}

function resolveAccountContext(row = {}, fieldKey = 'accountNumber') {
  if (String(fieldKey).startsWith('withdrawalAccount.')) {
    return row?.withdrawalAccount || null
  }
  if (row?.withdrawalAccount && fieldKey === 'accountNumber') {
    return row.withdrawalAccount
  }
  return row
}

export function getWithdrawalAccountNumber(row = {}, fieldKey = 'accountNumber') {
  if (String(fieldKey).includes('.')) {
    return String(getNestedValue(row, fieldKey) || '').trim()
  }
  const account = resolveAccountContext(row, fieldKey)
  return String(account?.accountNumber || '').trim()
}

export function shouldShowWechatQrImage(row = {}, fieldKey = 'accountNumber') {
  const account = resolveAccountContext(row, fieldKey)
  if (!isWechatWithdrawalType(account?.type)) {
    return false
  }
  return Boolean(getWithdrawalAccountNumber(row, fieldKey))
}

export function formatWithdrawalAccountNumberText(row = {}, fieldKey = 'accountNumber') {
  const value = getWithdrawalAccountNumber(row, fieldKey)
  if (!value) {
    return '—'
  }
  if (shouldShowWechatQrImage(row, fieldKey)) {
    return value
  }
  return value
}
