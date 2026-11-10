import { post, resolveBusinessResponse } from '@/api/http'
import { createPageApi } from '@/api/admin'

export const bannerApi = createPageApi('admin/base/banner')

export function updateBannerStateApi(data) {
  return post('admin/base/banner/updateState', data).then(resolveBusinessResponse)
}
