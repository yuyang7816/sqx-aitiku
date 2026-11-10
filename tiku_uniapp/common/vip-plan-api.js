/**
 * 会员套餐列表：GET /app/vipPlan
 */
import { get } from '@/common/api';

/** 将服务端 AppVipPlanItemVO 规范为会员中心套餐卡片 */
export const normalizeVipPlanItem = (item = {}, index = 0) => ({
	name: item.name || '',
	price: Number(item.price) || 0,
	days: Number(item.days) || 0,
	origin: Number(item.origin) || 0,
	recommend: item.recommend != null ? Boolean(item.recommend) : index === 0
});

/** 空套餐列表 */
export const buildEmptyMemberPlans = () => [];

/**
 * 拉取启用套餐列表；接口失败或空列表时返回空列表
 * @returns {Promise<{ list: Array<{ name: string, price: number, days: number, origin: number, recommend: boolean }>, fromApi: boolean }>}
 */
export const fetchMemberPlans = async () => {
	try {
		const res = await get('/app/vipPlan');
		if (res && res.code === 0 && Array.isArray(res.data) && res.data.length) {
			return {
				list: res.data.map(normalizeVipPlanItem),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理
	}

	return {
		list: buildEmptyMemberPlans(),
		fromApi: false
	};
};

/**
 * 支付页订单信息：优先路由 name 匹配 GET /app/vipPlan 套餐
 */
export const resolvePayOrderPlan = async (options = {}) => {
	const routeName = decodeURIComponent(String(options.name || '')).trim();
	const routePrice =
		options.price != null && options.price !== '' ? Number(options.price) : null;
	const routeDays =
		options.days != null && options.days !== '' ? Number(options.days) : null;

	const { list, fromApi } = await fetchMemberPlans();

	if (routeName) {
		const matched = list.find((item) => item.name === routeName);
		if (matched) {
			return {
				name: matched.name,
				price: matched.price,
				days: matched.days,
				fromApi
			};
		}
		return {
			name: routeName,
			price: Number.isFinite(routePrice) ? routePrice : list[0]?.price ?? 0,
			days: Number.isFinite(routeDays) ? routeDays : list[0]?.days ?? 0,
			fromApi
		};
	}

	const first = list[0];
	if (first) {
		return {
			name: first.name,
			price: first.price,
			days: first.days,
			fromApi
		};
	}

	return {
		name: routeName || '',
		price: Number.isFinite(routePrice) ? routePrice : 0,
		days: Number.isFinite(routeDays) ? routeDays : 0,
		fromApi: false
	};
};
