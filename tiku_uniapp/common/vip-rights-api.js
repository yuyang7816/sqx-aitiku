/**
 * 会员权益对比表：GET /app/vipRights，失败返回空列表
 */
import { get } from '@/common/api';

/** 将服务端 AppVipRightsCompareItemVO 规范为会员中心表格行 */
export const normalizeVipRightsCompareItem = (item = {}) => ({
	code: item.code || '',
	label: item.label || '',
	free: item.free != null ? String(item.free) : '',
	vip: item.vip != null ? String(item.vip) : ''
});

/** 从权益对比表提取 VIP 预览文案（首页/我的卡片，最多 N 条） */
export const buildVipBenefitPreview = (list = [], max = 2) =>
	(Array.isArray(list) ? list : [])
		.filter((item) => item?.label && item?.vip)
		.slice(0, Math.max(1, Number(max) || 2))
		.map((item) => `${item.label} ${item.vip}`.trim());

/** 空权益列表 */
export const buildEmptyMemberRights = () => [];

/**
	 * 拉取权益对比表；接口失败或空列表时返回空列表
 * @returns {Promise<{ list: Array<{ code: string, label: string, free: string, vip: string }>, fromApi: boolean }>}
 */
export const fetchMemberRightsCompare = async () => {
	try {
		const res = await get('/app/vipRights');
		if (res && res.code === 0 && Array.isArray(res.data) && res.data.length) {
			return {
				list: res.data.map(normalizeVipRightsCompareItem),
				fromApi: true
			};
		}
	} catch (e) {
		// 网络异常或 401 时静默回退 mock
	}

	return {
		list: buildEmptyMemberRights(),
		fromApi: false
	};
};
