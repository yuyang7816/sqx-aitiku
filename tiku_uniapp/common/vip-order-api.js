/**
 * 会员订单列表：GET /app/vipOrder，失败时合并本地缓存
 */
import { get } from '@/common/api';
import {
	MEMBER_ORDER_CHANNEL_LABELS,
	MEMBER_ORDER_PAY_STATUS,
	MEMBER_ORDER_STATUS_LABELS
} from '@/common/member-constants';
import { MEMBER_ORDER_HISTORY_KEY } from '@/common/storage-keys';

/** 支付状态筛选项（业务字段 payStatus） */
export const MEMBER_ORDER_STATUS_FILTERS = [
	{ label: '全部', value: '' },
	{ label: '待支付', value: MEMBER_ORDER_PAY_STATUS.PENDING },
	{ label: '已支付', value: MEMBER_ORDER_PAY_STATUS.PAID },
	{ label: '已关闭', value: MEMBER_ORDER_PAY_STATUS.CLOSED }
];

/** 套餐名称筛选项（业务字段 planName） */
export const MEMBER_ORDER_PLAN_FILTERS = [
	{ label: '全部套餐', value: '' },
	{ label: '月卡', value: '月卡' },
	{ label: '季卡', value: '季卡' }
];

/** 解析支付状态文案 */
export const resolveOrderPayStatusLabel = (payStatus) =>
	MEMBER_ORDER_STATUS_LABELS[payStatus] || '未知状态';

/** 解析支付渠道文案 */
export const resolveOrderPayChannelLabel = (payChannel = '') =>
	MEMBER_ORDER_CHANNEL_LABELS[String(payChannel || '').trim()] || '在线支付';

/** 金额展示（保留两位小数） */
export const formatOrderAmount = (amount) => {
	const num = Number(amount);
	if (!Number.isFinite(num)) {
		return '0.00';
	}
	return num.toFixed(2);
};

/** 将服务端 AppVipOrderItemVO 规范为列表卡片字段 */
export const normalizeVipOrderItem = (item = {}) => {
	const payStatus = Number(item.payStatus);
	return {
		orderNo: String(item.orderNo || '').trim(),
		planName: String(item.planName || '').trim(),
		planDays: Number(item.planDays) || 0,
		amount: Number(item.amount) || 0,
		payChannel: String(item.payChannel || '').trim(),
		payStatus: Number.isFinite(payStatus) ? payStatus : MEMBER_ORDER_PAY_STATUS.PENDING,
		createTime: String(item.createTime || '').trim(),
		payTime: String(item.payTime || '').trim(),
		payStatusLabel: resolveOrderPayStatusLabel(payStatus),
		payChannelLabel: resolveOrderPayChannelLabel(item.payChannel)
	};
};

/** 空订单列表 */
export const buildMemberOrderListFallback = () => [];

/** 读取本地缓存订单（支付成功时写入，接口不可用时补充展示） */
export const getLocalMemberOrders = () => {
	try {
		const saved = uni.getStorageSync(MEMBER_ORDER_HISTORY_KEY);
		if (!Array.isArray(saved)) {
			return [];
		}
		return saved.map((item) => normalizeVipOrderItem(item)).filter((item) => item.orderNo);
	} catch (e) {
		return [];
	}
};

/** 支付成功后写入本地订单缓存（按 orderNo 去重，最新在前） */
export const appendLocalMemberOrder = (order = {}) => {
	const normalized = normalizeVipOrderItem({
		...order,
		payStatus: order.payStatus != null ? order.payStatus : MEMBER_ORDER_PAY_STATUS.PAID,
		payTime: order.payTime || formatNowDateTime()
	});
	if (!normalized.orderNo) {
		return normalized;
	}
	const current = getLocalMemberOrders();
	const merged = [normalized, ...current.filter((item) => item.orderNo !== normalized.orderNo)];
	try {
		uni.setStorageSync(MEMBER_ORDER_HISTORY_KEY, merged.slice(0, 50));
	} catch (e) {
		// ignore storage quota
	}
	return normalized;
};

const formatNowDateTime = () => {
	const now = new Date();
	const pad = (value) => String(value).padStart(2, '0');
	return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
};

/** 合并接口/本地列表并按创建时间倒序 */
const mergeOrderSources = ({ apiList = [], localList = [] } = {}) => {
	const map = new Map();
	[...apiList, ...localList].forEach((item) => {
		if (item && item.orderNo) {
			map.set(item.orderNo, item);
		}
	});
	return Array.from(map.values()).sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')));
};

/** 前端二次筛选（orderNo 模糊、planName、payStatus） */
export const filterMemberOrders = (list = [], filters = {}) => {
	const orderNoKeyword = String(filters.orderNo || '').trim();
	const planName = String(filters.planName || '').trim();
	const payStatus = filters.payStatus;
	return (Array.isArray(list) ? list : []).filter((item) => {
		if (orderNoKeyword && !String(item.orderNo || '').includes(orderNoKeyword)) {
			return false;
		}
		if (planName && item.planName !== planName) {
			return false;
		}
		if (payStatus !== '' && payStatus != null && Number(item.payStatus) !== Number(payStatus)) {
			return false;
		}
		return true;
	});
};

/**
 * 拉取会员订单列表；接口失败时合并本地缓存
 * @param {{ orderNo?: string, planName?: string, payStatus?: number|string }} query
 * @returns {Promise<{ list: Array, fromApi: boolean, message?: string }>}
 */
export const fetchMemberOrderList = async (query = {}) => {
	const params = {};
	const orderNo = String(query.orderNo || '').trim();
	const planName = String(query.planName || '').trim();
	if (orderNo) {
		params.orderNo = orderNo;
	}
	if (planName) {
		params.planName = planName;
	}
	if (query.payStatus !== '' && query.payStatus != null) {
		params.payStatus = Number(query.payStatus);
	}

	try {
		const res = await get('/app/vipOrder', params);
		if (res && res.code === 0 && Array.isArray(res.data)) {
			const apiList = res.data.map(normalizeVipOrderItem);
			const localList = getLocalMemberOrders();
			return {
				list: mergeOrderSources({ apiList, localList }),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理；其余场景回退本地/mock
	}

	const localList = getLocalMemberOrders();
	return {
		list: mergeOrderSources({ localList }),
		fromApi: false,
		message: localList.length ? '订单列表加载失败，已展示本地记录' : '暂无订单记录'
	};
};
