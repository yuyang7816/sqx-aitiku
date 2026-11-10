/**
 * 模块二十二 P1#12 · 消息中心 API
 * API: GET /app/messages · POST /app/messages/read-all
 * 触发：首页 onShow fetchNoticeUnreadCount · 消息中心 notice.vue onShow fetchNoticeList · 全部已读 markAllNoticesReadAsync
 * 合并：API records 优先；失败/未登录返回空列表
 */
import { get, postJson } from '@/common/api';
import { parseReportMonthFromTitle } from '@/common/analysis-report';
import {
	applyStoredNoticeReadState,
	getAuthState,
	LAUNCH_ROUTE_SETUP,
	markAllNoticesRead
} from '@/common/app-data';
import { buildReportPageUrl, MEMBER_CENTER_PAGE, NOTEBOOK_TAB_PAGE } from '@/common/routes';

const DEFAULT_PAGE = 1;
const DEFAULT_LIMIT = 50;

/** 将服务端 AppMessageItemVO 规范为 notice.vue 使用的结构 */
export const normalizeMessageItem = (item = {}) => {
	const jumpId = String(
		item.id || (item.messageId != null ? `msg-${item.messageId}` : '')
	).trim();
	const reportMonth = jumpId === 'notice-report'
		? parseReportMonthFromTitle(item.title)
		: '';
	return {
		id: jumpId,
		messageId: item.messageId,
		title: item.title || '',
		content: item.content || '',
		time: item.time || '',
		read: Boolean(item.read),
		reportMonth
	};
};

/** 消息列表稳定 key（同 jumpId 多条月报时用 messageId 区分） */
export const resolveNoticeListKey = (item = {}) => {
	if (item.messageId != null) {
		return `msg-${item.messageId}`;
	}
	return String(item.id || item.title || 'notice');
};

/**
 * 解析站内消息跳转路径（与 server MessagesServiceImpl.resolveJumpId、notice.vue 分支对齐）
 * @param {{ id?: string, title?: string, reportMonth?: string }} item
 * @returns {string} uni.navigateTo 路径，空串表示无跳转
 */
export const resolveNoticeJumpUrl = (item = {}) => {
	const jumpId = String(item.id || '').trim();
	if (jumpId === 'notice-plan') {
		return LAUNCH_ROUTE_SETUP;
	}
	if (jumpId === 'notice-review') {
		return NOTEBOOK_TAB_PAGE;
	}
	if (jumpId === 'notice-vip-guide' || jumpId === 'notice-vip-active' || jumpId === 'notice-member') {
		return MEMBER_CENTER_PAGE;
	}
	if (jumpId === 'notice-report') {
		const month = String(item.reportMonth || '').trim()
			|| parseReportMonthFromTitle(item.title);
		return buildReportPageUrl(month);
	}
	return '';
};

/** 接口不可用时的空列表 */
export const getFallbackNoticeList = () => [];

/** 首页角标联动：Storage 已读态合并后的未读数（同步，无需请求 API） */
export const getNoticeUnreadCountSync = () => 0;

/** 统计未读条数 */
export const countUnreadNotices = (list = []) =>
	(Array.isArray(list) ? list : []).filter((item) => !item.read).length;

export { NOTICE_READ_STATE_EVENT, resolveNoticeReadKey } from '@/common/app-data';

/** 首页角标文案（超过 99 显示 99+） */
export const formatNoticeBadge = (count = 0) => {
	if (!count || count <= 0) {
		return '';
	}
	return count > 99 ? '99+' : String(count);
};

const parseMessageRecords = (data) => {
	if (!data || !Array.isArray(data.records)) {
		return null;
	}
	return data.records.map(normalizeMessageItem);
};

/**
 * 合并 API 消息与本地动态/默认消息：API 记录在前，本地项按 jumpId 去重补全
 * @param {object[]} apiList
 * @param {object[]} localList - getNoticeList() 动态 + 默认消息
 */
export const mergeNoticeLists = (apiList = [], localList = []) => {
	const apiRecords = Array.isArray(apiList) ? apiList : [];
	const localRecords = Array.isArray(localList) ? localList : [];
	const apiJumpIds = new Set(
		apiRecords.map((item) => String(item.id || '').trim()).filter(Boolean)
	);
	const localOnly = localRecords.filter((item) => {
		const jumpId = String(item.id || '').trim();
		if (!jumpId) {
			return true;
		}
		return !apiJumpIds.has(jumpId);
	});
	return [...apiRecords, ...localOnly];
};

/**
 * 拉取消息列表；已登录优先 API
 * @returns {Promise<{ list: object[], fromApi: boolean }>}
 */
export const fetchNoticeList = async ({ page = DEFAULT_PAGE, limit = DEFAULT_LIMIT, read = null } = {}) => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return {
			list: [],
			fromApi: false
		};
	}
	try {
		const params = {
			page,
			limit
		};
		if (read !== null) {
			params.read = read;
		}
		const res = await get('/app/messages', params);
		if (res && res.code === 0) {
			const records = parseMessageRecords(res.data);
			if (records) {
				return {
					list: applyStoredNoticeReadState(records),
					fromApi: true
				};
			}
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理
	}
	return {
		list: [],
		fromApi: false
	};
};

/**
 * 首页铃铛未读数：基于合并后的完整列表统计，与消息中心展示一致
 * @returns {Promise<number>}
 */
export const fetchNoticeUnreadCount = async () => {
	const { list } = await fetchNoticeList();
	return countUnreadNotices(list);
};

/**
 * 同步服务端全部已读（POST /app/messages/read-all）；失败时静默，由本地 Storage 兜底
 * @returns {Promise<boolean>} 是否已成功同步服务端
 */
export const syncAllNoticesReadToServer = async () => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return false;
	}
	try {
		const res = await postJson('/app/messages/read-all', {});
		return Boolean(res && res.code === 0);
	} catch (e) {
		return false;
	}
};

/**
 * 全部已读：优先同步服务端，再写入本地 Storage 并触发角标更新
 * @param {object[]} [currentList]
 * @returns {Promise<object[]>}
 */
export const markAllNoticesReadAsync = async (currentList = []) => {
	await syncAllNoticesReadToServer();
	return markAllNoticesRead(currentList);
};
