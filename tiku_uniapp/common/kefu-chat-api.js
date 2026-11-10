/**
 * 在线客服 IM：/app/kefu/chat
 */
import { get, postJson } from '@/common/api';

export const KEFU_CHAT_PAGE = '/package/customer/kefu-chat';

/** 拉取消息列表 */
export const fetchKefuChatMessages = async (limit = 100) => {
	const res = await get('/app/kefu/chat/messages', { limit });
	if (res && res.code === 0) {
		return Array.isArray(res.data) ? res.data : [];
	}
	throw res || new Error('加载客服消息失败');
};

/** 发送用户消息 */
export const sendKefuChatMessage = async (content = '') => {
	const res = await postJson('/app/kefu/chat/send', { content: String(content || '').trim() });
	if (res && res.code === 0) {
		return res.data || null;
	}
	throw res || new Error('发送失败');
};

/** 未读数（客服回复） */
export const fetchKefuUnreadCount = async () => {
	try {
		const res = await get('/app/kefu/chat/unread-count');
		if (res && res.code === 0) {
			return Number(res.data?.count || 0);
		}
	} catch (e) {
		// 401 由全局处理
	}
	return 0;
};

/** 标记客服消息已读 */
export const markKefuChatRead = async () => {
	try {
		await postJson('/app/kefu/chat/read', {});
	} catch (e) {
		// ignore
	}
};

/** 角标文案 */
export const formatKefuBadge = (count = 0) => {
	const value = Number(count) || 0;
	if (value <= 0) return '';
	if (value > 99) return '99+';
	return String(value);
};
