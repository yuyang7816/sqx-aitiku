import { get, post, resolveBusinessResponse } from '@/api/http'

export const menuApi = {
  list: '/sys/menu/list',
  select: '/sys/menu/select',
  info: '/sys/menu/info',
  create: '/sys/menu/save',
  update: '/sys/menu/update',
  delete: '/sys/menu/delete',
}

function normalizeMenuList(data) {
  if (Array.isArray(data?.data)) {
    return data.data
  }
  if (Array.isArray(data)) {
    return data
  }
  return data?.menuList || data?.list || []
}

function resolveMenuListResponse(result) {
  if (Array.isArray(result)) {
    return result
  }
  return resolveBusinessResponse(result)
}

export function getMenuListApi(params = {}) {
  return get(menuApi.list, params).then(resolveMenuListResponse).then(normalizeMenuList)
}

export function getMenuSelectApi(params = {}) {
  return get(menuApi.select, params).then(resolveBusinessResponse).then(normalizeMenuList)
}

export function getMenuInfoApi(id) {
  return get(`${menuApi.info}/${id}`).then(resolveBusinessResponse).then((data) => data?.menu || data || {})
}

export function createMenuApi(data) {
  return post(menuApi.create, data).then(resolveBusinessResponse)
}

export function updateMenuApi(data) {
  return post(menuApi.update, data).then(resolveBusinessResponse)
}

export function deleteMenuApi(id) {
  return post(`${menuApi.delete}/${id}`, {}).then(resolveBusinessResponse)
}
