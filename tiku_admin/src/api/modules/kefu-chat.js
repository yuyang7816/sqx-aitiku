import { get, post, resolveBusinessResponse } from '@/api/http'

export async function getKefuChatSessionsApi(limit = 50) {
  return get('admin/kefu/chat/sessions', { limit }).then(resolveBusinessResponse)
}

export async function getKefuChatMessagesApi(userId, limit = 100) {
  return get('admin/kefu/chat/messages', { userId, limit }).then(resolveBusinessResponse)
}

export async function replyKefuChatApi(payload) {
  return post('admin/kefu/chat/reply', payload).then(resolveBusinessResponse)
}

export async function getKefuUnreadCountApi() {
  const data = await get('admin/kefu/chat/unread-count').then(resolveBusinessResponse)
  return Number(data?.count || 0)
}

export async function markKefuChatReadApi(userId) {
  return post('admin/kefu/chat/read', null, { params: { userId } }).then(resolveBusinessResponse)
}
