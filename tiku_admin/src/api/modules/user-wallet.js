import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

export const userWalletApi = createPageApi('admin/userWallet')

export function updateUserWalletAmountApi(data) {
  return post('admin/userWallet/updateAmount', data).then(resolveBusinessResponse)
}
