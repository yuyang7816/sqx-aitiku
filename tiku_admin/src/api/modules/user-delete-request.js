import { post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

export function getUserDeleteRequestPageApi(data) {
  return post('admin/userDeleteRequest/page', data)
    .then(resolveBusinessResponse)
    .then(resolvePageData)
}

export function cancelUserDeleteRequestApi(data) {
  return post('admin/userDeleteRequest/cancel', data).then(resolveBusinessResponse)
}

export function executeUserDeleteRequestApi(data) {
  return post('admin/userDeleteRequest/execute', data).then(resolveBusinessResponse)
}
