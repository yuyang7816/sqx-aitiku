/**
 * 账号注销冷静期：POST /app/user/delete、GET /app/user/delete/status、POST /app/user/delete/cancel
 */
import { getToken } from '@/common/auth-token';
import { postJson, get } from '@/common/api';

const DEFAULT_COOLING_DAYS = 7;

const normalizeDeleteStatus = (data = {}) => ({
	pending: Boolean(data.pending),
	deleted: Boolean(data.deleted),
	effectiveAt: data.effectiveAt || '',
	coolingDays: Number(data.coolingDays) || DEFAULT_COOLING_DAYS,
	remainingDays: Number(data.remainingDays) || 0
});

/** 查询当前注销冷静期状态 */
export const fetchDeleteAccountStatus = async () => {
	if (!getToken()) {
		return { success: false, msg: '请先登录' };
	}
	try {
		const res = await get('/app/user/delete/status', {}, { toast: false });
		if (res && res.code === 0 && res.data) {
			return { success: true, data: normalizeDeleteStatus(res.data) };
		}
		return { success: false, msg: (res && res.msg) || '查询注销状态失败' };
	} catch (e) {
		return { success: false, msg: (e && e.msg) || '网络异常，请稍后重试' };
	}
};

/** 提交注销申请（进入冷静期，不立即删数据） */
export const requestDeleteAccount = async () => {
	if (!getToken()) {
		return { success: false, msg: '请先登录' };
	}
	try {
		const res = await postJson('/app/user/delete', {}, {
			toast: false,
			loadingText: '提交中...'
		});
		if (res && res.code === 0 && res.data) {
			return { success: true, data: normalizeDeleteStatus(res.data), msg: res.msg };
		}
		return {
			success: false,
			msg: (res && res.msg) || '提交注销申请失败，请稍后重试'
		};
	} catch (e) {
		return {
			success: false,
			msg: (e && e.msg) || '网络异常，请稍后重试'
		};
	}
};

/** 撤销注销申请 */
export const cancelDeleteAccountRequest = async () => {
	if (!getToken()) {
		return { success: false, msg: '请先登录' };
	}
	try {
		const res = await postJson('/app/user/delete/cancel', {}, {
			toast: false,
			loadingText: '撤销中...'
		});
		if (res && res.code === 0) {
			return {
				success: true,
				data: normalizeDeleteStatus(res.data || {}),
				msg: res.msg
			};
		}
		return {
			success: false,
			msg: (res && res.msg) || '撤销失败，请稍后重试'
		};
	} catch (e) {
		return {
			success: false,
			msg: (e && e.msg) || '网络异常，请稍后重试'
		};
	}
};

const DELETE_ACCOUNT_WARN_CONTENT =
	'提交后将进入 7 天冷静期，期间您的学习记录、收藏、会员权益等数据会完整保留。冷静期结束后账号才会被永久注销，确定继续吗？';
const DELETE_ACCOUNT_FINAL_CONTENT =
	'冷静期内您可随时在设置页撤销注销。确认提交注销申请吗？';

/**
 * 设置页账号注销：二次确认 → POST /app/user/delete（进入冷静期，不退出登录）
 * @returns {Promise<object|null>} 成功时返回 status 数据
 */
export const confirmAndDeleteAccount = () =>
	new Promise((resolve) => {
		uni.showModal({
			title: '账号注销',
			content: DELETE_ACCOUNT_WARN_CONTENT,
			confirmText: '继续',
			confirmColor: '#ef4444',
			cancelText: '取消',
			success: (first) => {
				if (!first.confirm) {
					resolve(null);
					return;
				}
				uni.showModal({
					title: '最终确认',
					content: DELETE_ACCOUNT_FINAL_CONTENT,
					confirmText: '提交申请',
					confirmColor: '#ef4444',
					cancelText: '再想想',
					success: async (second) => {
						if (!second.confirm) {
							resolve(null);
							return;
						}
						try {
							const result = await requestDeleteAccount();
							if (!result.success) {
								uni.showToast({
									title: result.msg || '提交失败',
									icon: 'none'
								});
								resolve(null);
								return;
							}
							uni.showToast({
								title: '注销申请已提交',
								icon: 'success'
							});
							resolve(result.data);
						} catch (e) {
							resolve(null);
						}
					}
				});
			}
		});
	});

/**
 * 撤销注销申请确认弹窗
 * @returns {Promise<object|null>}
 */
export const confirmAndCancelDeleteAccount = () =>
	new Promise((resolve) => {
		uni.showModal({
			title: '撤销注销',
			content: '撤销后账号将恢复正常使用，学习数据不会受到影响。确定撤销吗？',
			confirmText: '确认撤销',
			confirmColor: '#386df5',
			cancelText: '取消',
			success: async (res) => {
				if (!res.confirm) {
					resolve(null);
					return;
				}
				const result = await cancelDeleteAccountRequest();
				if (!result.success) {
					uni.showToast({ title: result.msg || '撤销失败', icon: 'none' });
					resolve(null);
					return;
				}
				uni.showToast({ title: '已撤销注销申请', icon: 'success' });
				resolve(result.data);
			}
		});
	});
