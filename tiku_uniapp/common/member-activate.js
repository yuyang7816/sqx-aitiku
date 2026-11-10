/**
 * 模块二十四 24.3 · 支付回调后会员状态刷新
 * 支付成功后刷新本地 memberState（文档 14.4 + 23.3-4 开通重置次数）
 * 先 GET /app/user 合并服务端 vip/vipExpireAt；回调未落库时仍强制 isVip=true 保证 VIP 即时生效
 */
import { getMemberState, saveMemberState } from '@/common/app-data';
import {
	VIP_LEVEL_LABEL,
	buildActivationQuotaReset,
	getTodayString
} from '@/common/member-rules';
import { fetchAndMergeUserInfo } from '@/common/user-info';

/**
 * 支付回调后同步会员态：先拉服务端 vip/expireAt，再按 23.3-4 重置当日 AI/OCR 次数
 * @param {{ name?: string, days?: number }} payload - 套餐名（expireAt 以服务端为准）
 * @returns {Promise<object>} 写入后的 memberState
 */
export const refreshMemberStateAfterPay = async ({ name = '月卡' } = {}) => {
	await fetchAndMergeUserInfo();
	const current = getMemberState();
	const reset = buildActivationQuotaReset(getTodayString());
	return saveMemberState({
		...current,
		isVip: true,
		level: VIP_LEVEL_LABEL,
		...reset,
		currentPlanName: String(name || '月卡').trim() || '月卡',
		orderCount: Number(current.orderCount || 0) + 1
	});
};
