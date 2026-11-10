import { get, resolveBusinessResponse } from '@/api/http'

export async function getDashboardOverview() {
  const data = await get('admin/dashboard/overview').then(resolveBusinessResponse)
  return data || {}
}
