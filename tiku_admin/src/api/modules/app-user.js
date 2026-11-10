import { get, post, resolveBusinessResponse } from '@/api/http'
import { resolvePageData } from '@/api/admin'

export function getAppUserPage(params = {}) {
  return post('admin/base/user/list', params)
    .then(resolveBusinessResponse)
    .then(resolvePageData)
}

export function getAppUserInfo(userId) {
  return get('admin/base/user/info', { userId }).then(resolveBusinessResponse)
}

export function updateAppUserApi(data) {
  return post('admin/base/user/update', data).then(resolveBusinessResponse)
}

export function deleteAppUserApi(data) {
  return post('admin/base/user/delete', data).then(resolveBusinessResponse)
}
