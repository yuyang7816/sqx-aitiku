import { get, resolveBusinessResponse } from '@/api/http'

export function getSysLogPage(params = {}) {
  const query = {
    page: params.page || params.pageNum || 1,
    limit: params.limit || params.pageSize || 10,
    key: params.key || params.keyword || undefined,
  }
  return get('/sys/log/list', query)
    .then(resolveBusinessResponse)
    .then((data) => {
      const page = data?.page || data || {}
      return {
        list: page.list || [],
        total: Number(page.totalCount || page.total || 0),
      }
    })
}
