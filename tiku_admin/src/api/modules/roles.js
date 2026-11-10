import { get, post, resolveBusinessResponse } from '@/api/http'

export const roleApi = {
  list: '/sys/role/list',
  info: '/sys/role/info',
  create: '/sys/role/save',
  update: '/sys/role/update',
  delete: '/sys/role/delete',
}

export function getRolePage(params = {}) {
  const query = {
    page: params.page || params.pageNum || 1,
    limit: params.limit || params.pageSize || 10,
    roleName: params.roleName || undefined,
  }
  return get(roleApi.list, query)
    .then(resolveBusinessResponse)
    .then((data) => {
      const page = data?.page || data || {}
      return {
        list: page.list || [],
        total: Number(page.totalCount || page.total || 0),
      }
    })
}

export function getRoleInfoApi(roleId) {
  return get(`${roleApi.info}/${roleId}`)
    .then(resolveBusinessResponse)
    .then((data) => data?.role || data || {})
}

export function createRoleApi(data) {
  return post(roleApi.create, data).then(resolveBusinessResponse)
}

export function updateRoleApi(data) {
  return post(roleApi.update, data).then(resolveBusinessResponse)
}

export function deleteRoleApi(roleIds) {
  const payload = Array.isArray(roleIds) ? roleIds : [roleIds]
  return post(roleApi.delete, payload).then(resolveBusinessResponse)
}
