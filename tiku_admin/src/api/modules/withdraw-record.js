import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

export const withdrawRecordApi = createPageApi('admin/withdrawRecord')

/** 审核通过：status=2 */
export function approveWithdrawRecordApi(data) {
  return post('admin/withdrawRecord/confirm', { ...data, status: 2 }).then(resolveBusinessResponse)
}

/** 审核拒绝：status=3，需传 reason */
export function rejectWithdrawRecordApi(data) {
  return post('admin/withdrawRecord/confirm', { ...data, status: 3 }).then(resolveBusinessResponse)
}

/** @deprecated 请使用 approveWithdrawRecordApi */
export function confirmWithdrawRecordApi(data) {
  return approveWithdrawRecordApi(data)
}
