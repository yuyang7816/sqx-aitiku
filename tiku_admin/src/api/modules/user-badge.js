import { post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

export const userBadgeApi = {
  page(data) {
    return post('admin/userBadge/page', data).then(resolveBusinessResponse).then(resolvePageData)
  },
  info(data) {
    return post('admin/userBadge/info', data).then(resolveBusinessResponse)
  },
  save(data) {
    return post('admin/userBadge/save', data).then(resolveBusinessResponse)
  },
  update(data) {
    return post('admin/userBadge/update', data).then(resolveBusinessResponse)
  },
  delete(data) {
    return post('admin/userBadge/delete', data).then(resolveBusinessResponse)
  },
}
