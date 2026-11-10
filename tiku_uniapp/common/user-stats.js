/**
 * 个人中心统计三栏：GET /app/user/stats，失败时返回空统计
 */
import { get } from '@/common/api';
import { getAuthState } from '@/common/app-data';
import { BADGE_WALL_PAGE, STUDY_CALENDAR_PAGE } from '@/common/routes';

/** 统计数字展示（题量大时加千分位，与 mock 展示一致） */
export const formatStatCount = (num) => {
	const n = Math.max(0, Number(num) || 0);
	return n >= 1000 ? n.toLocaleString('en-US') : String(n);
};

/** 将服务端 AppUserStatsVO 规范为本地统计字段 */
export const normalizeUserStats = (data = {}) => ({
	studyDays: typeof data.studyDays === 'number' ? Math.max(0, data.studyDays) : 0,
	questionCount: typeof data.questionCount === 'number' ? Math.max(0, data.questionCount) : 0,
	badgeCount: typeof data.badgeCount === 'number' ? Math.max(0, data.badgeCount) : 0
});

/** 将统计字段转为三栏展示结构 */
export const buildStatsDisplayItems = (stats = {}) => ([
	{ key: 'studyDays', value: formatStatCount(stats.studyDays), label: '学习天数', link: STUDY_CALENDAR_PAGE },
	{ key: 'questionCount', value: formatStatCount(stats.questionCount), label: '做题总数' },
	{ key: 'badgeCount', value: formatStatCount(stats.badgeCount), label: '获得勋章', link: BADGE_WALL_PAGE }
]);

/** 空统计 */
export const buildEmptyUserStats = () => buildStatsDisplayItems({
	studyDays: 0,
	questionCount: 0,
	badgeCount: 0
});

/**
	 * 拉取累计学习统计；未登录或失败时返回空统计
 * @returns {Promise<{ items: Array<{ value: string, label: string }>, fromApi: boolean, message?: string }>}
 */
export const fetchUserStats = async () => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return {
			items: buildEmptyUserStats(),
			fromApi: false
		};
	}
	try {
		const res = await get('/app/user/stats');
		if (res && res.code === 0 && res.data) {
			return {
				items: buildStatsDisplayItems(normalizeUserStats(res.data)),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理；其余场景回退 mock
	}
	return {
		items: buildEmptyUserStats(),
		fromApi: false,
		message: '统计数据加载失败'
	};
};
