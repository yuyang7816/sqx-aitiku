/**
 * 用户勋章：GET /app/user/badges（需登录；失败时返回空列表）
 */
import { get } from '@/common/api';
import { getAuthState } from '@/common/app-data';

/** 勋章展示图标与配色（按 badgeCode 映射，非自增 ID） */
const BADGE_VISUAL_MAP = {
	first_practice: { icon: 'edit-pen-fill', color: '#386df5', bg: '#eef3ff' },
	study_3_days: { icon: 'calendar-fill', color: '#16a34a', bg: '#e8fbee' },
	study_7_days: { icon: 'calendar-fill', color: '#0891b2', bg: '#e6f7fb' },
	study_30_days: { icon: 'star-fill', color: '#f59e0b', bg: '#fff6e8' },
	question_50: { icon: 'order', color: '#8a62f8', bg: '#f3efff' },
	question_100: { icon: 'order', color: '#6366f1', bg: '#eef0ff' },
	question_500: { icon: 'integral-fill', color: '#d49e13', bg: '#fff8e8' },
	perfect_score: { icon: 'checkmark-circle-fill', color: '#ff5b62', bg: '#ffecee' }
};

const DEFAULT_BADGE_VISUAL = { icon: 'star-fill', color: '#386df5', bg: '#eef3ff' };

/** 将服务端勋章项规范为本地结构 */
export const normalizeUserBadgeItem = (item = {}) => {
	const badgeCode = String(item.badgeCode || '').trim();
	const targetValue = Math.max(0, Number(item.targetValue) || 0);
	const progressValue = Math.max(0, Number(item.progressValue) || 0);
	const unlocked = Boolean(item.unlocked);
	return {
		badgeCode,
		name: String(item.name || '').trim(),
		conditionText: String(item.conditionText || '').trim(),
		unlocked,
		progressValue,
		targetValue,
		progressPercent: targetValue > 0 ? Math.min(100, Math.round((progressValue / targetValue) * 100)) : (unlocked ? 100 : 0)
	};
};

/** 获取勋章图标与背景色 */
export const getBadgeVisual = (badgeCode = '') => BADGE_VISUAL_MAP[badgeCode] || DEFAULT_BADGE_VISUAL;

/** 未解锁时展示进度文案 */
export const formatBadgeProgressText = (item = {}) => {
	if (item.unlocked) {
		return '已解锁';
	}
	const progress = Math.max(0, Number(item.progressValue) || 0);
	const target = Math.max(0, Number(item.targetValue) || 0);
	if (item.badgeCode === 'perfect_score') {
		return progress >= target ? '已达成' : '尚未获得满分';
	}
	if (target <= 0) {
		return '未解锁';
	}
	return `进度 ${progress}/${target}`;
};

/** 我的页用户名下方最多展示的已解锁勋章数 */
export const BADGE_PREVIEW_LIMIT = 5;

/** 筛选已解锁勋章 */
export const filterUnlockedBadges = (list = []) => (Array.isArray(list) ? list.filter((item) => item.unlocked) : []);

/**
 * 拉取用户勋章列表
 * @returns {Promise<{ list: object[], unlockedCount: number, totalCount: number, fromApi: boolean, message?: string }>}
 */
export const fetchUserBadges = async () => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return {
			list: [],
			unlockedCount: 0,
			totalCount: 0,
			fromApi: false
		};
	}
	try {
		const res = await get('/app/user/badges');
		if (res && res.code === 0 && res.data) {
			const rawList = Array.isArray(res.data.list) ? res.data.list : [];
			const list = rawList.map(normalizeUserBadgeItem);
			const unlockedCount = typeof res.data.unlockedCount === 'number'
				? Math.max(0, res.data.unlockedCount)
				: list.filter((item) => item.unlocked).length;
			return {
				list,
				unlockedCount,
				totalCount: list.length,
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理
	}
	return {
		list: [],
		unlockedCount: 0,
		totalCount: 0,
		fromApi: false,
		message: '勋章数据加载失败'
	};
};
