import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

export const vipOrderApi = createPageApi('admin/vipOrder')

export function getVipOrderInfo(id) {
  return post('admin/vipOrder/info', { id }).then(resolveBusinessResponse)
}
