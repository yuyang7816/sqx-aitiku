import { get, post, resolveBusinessResponse } from '@/api/http'

export function getDictPage(params = {}) {
  const query = {
    page: params.page || params.pageNum || 1,
    limit: params.limit || params.pageSize || 10,
    name: params.name || params.keyword || undefined,
    type: params.type || undefined,
  }
  return get('sys/dict/list', query)
    .then(resolveBusinessResponse)
    .then((data) => {
      const page = data?.page || data || {}
      return {
        list: page.list || [],
        total: Number(page.totalCount || page.total || 0),
      }
    })
}

export function getDictInfoApi(id) {
  return get(`sys/dict/info/${id}`).then(resolveBusinessResponse)
}

export function createDictApi(data) {
  return post('sys/dict/save', data).then(resolveBusinessResponse)
}

export function updateDictApi(data) {
  return post('sys/dict/update', data).then(resolveBusinessResponse)
}

export function deleteDictApi(ids) {
  const payload = Array.isArray(ids) ? ids : [ids]
  return post('sys/dict/delete', payload).then(resolveBusinessResponse)
}
