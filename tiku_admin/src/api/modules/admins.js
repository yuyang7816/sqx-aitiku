import { get, post, resolveBusinessResponse } from '@/api/http'

export const adminApi = {
  list: '/sys/user/list',
  info: '/sys/user/info',
  create: '/sys/user/save',
  update: '/sys/user/update',
  delete: '/sys/user/delete',
  roleSelect: '/sys/role/select',
}

export function getAdminPage(params = {}) {
  const query = {
    page: params.page || params.pageNum || 1,
    limit: params.limit || params.pageSize || 10,
    username: params.username || params.userName || undefined,
  }
  return get(adminApi.list, query)
    .then(resolveBusinessResponse)
    .then((data) => {
      const page = data?.page || data || {}
      return {
        list: page.list || [],
        total: Number(page.totalCount || page.total || 0),
      }
    })
}

export function getAdminInfoApi(userId) {
  return get(`${adminApi.info}/${userId}`)
    .then(resolveBusinessResponse)
    .then((data) => data?.user || data || {})
}

export function getRoleSelectApi() {
  return get(adminApi.roleSelect)
    .then(resolveBusinessResponse)
    .then((data) => data?.list || data || [])
}

export function createAdminApi(data) {
  return post(adminApi.create, data).then(resolveBusinessResponse)
}

export function updateAdminApi(data) {
  return post(adminApi.update, data).then(resolveBusinessResponse)
}

export function deleteAdminApi(userIds) {
  const payload = Array.isArray(userIds) ? userIds : [userIds]
  return post(adminApi.delete, payload).then(resolveBusinessResponse)
}
