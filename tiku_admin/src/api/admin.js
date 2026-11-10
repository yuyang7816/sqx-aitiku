import { get, post, resolveBusinessResponse } from '@/api/http'

export function resolvePageData(data) {
  const page = data?.records ? data : data?.page || data || {}
  return {
    list: page.records || page.list || [],
    total: Number(page.total || page.totalCount || 0),
  }
}

export function createPageApi(prefix, { pageMethod = 'page' } = {}) {
  return {
    page(data = {}) {
      return post(`${prefix}/${pageMethod}`, data).then(resolveBusinessResponse).then(resolvePageData)
    },
    info(id) {
      return post(`${prefix}/info`, { id }).then(resolveBusinessResponse)
    },
    save(data) {
      return post(`${prefix}/save`, data).then(resolveBusinessResponse)
    },
    update(data) {
      return post(`${prefix}/update`, data).then(resolveBusinessResponse)
    },
    delete(data) {
      return post(`${prefix}/delete`, data).then(resolveBusinessResponse)
    },
  }
}
