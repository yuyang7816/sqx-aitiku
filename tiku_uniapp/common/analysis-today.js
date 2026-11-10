/**
 * 今日学情统计：GET /app/analysis/today，失败回退首页建议时长公式
 */
import { get } from '@/common/api';
import { getAuthState } from '@/common/app-data';

/** 将服务端 AppAnalysisTodayVO 规范为本地结构 */
export const normalizeTodayAnalysis = (data = {}) => ({
	statDate: data.statDate || '',
	durationMinutes: typeof data.durationMinutes === 'number' ? Math.max(0, data.durationMinutes) : 0,
	questionCount: typeof data.questionCount === 'number' ? Math.max(0, data.questionCount) : 0
});

/**
 * 拉取今日练习时长与题量；未登录或失败时返回 { fromApi: false }
 * @returns {Promise<{ fromApi: boolean, statDate?: string, durationMinutes?: number, questionCount?: number }>}
 */
export const fetchTodayAnalysis = async () => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return {
			fromApi: false
		};
	}
	try {
		const res = await get('/app/analysis/today');
		if (res && res.code === 0 && res.data) {
			return {
				...normalizeTodayAnalysis(res.data),
				fromApi: true
			};
		}
	} catch (e) {
		// 网络异常时静默回退建议时长公式
	}
	return {
		fromApi: false
	};
};
