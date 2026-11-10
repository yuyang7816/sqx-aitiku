/**
 * 月度学习报告：GET /app/analysis/report，失败返回空/零值
 */
import { get } from '@/common/api';
import { getAuthState } from '@/common/app-data';

/** 接口无数据时的默认展示 */
export const REPORT_MONTH_TITLE_FALLBACK = '学习报告';
export const REPORT_RANK_TITLE_FALLBACK = '暂无排名数据';
export const REPORT_RANK_SUBTITLE_FALLBACK = '完成更多练习后可生成同龄对比';
export const REPORT_RANK_PERCENT_FALLBACK = 0;

/** AI 学习建议空列表 */
export const REPORT_ADVICES_FALLBACK = [];

/** 学科正确率柱状图空列表 */
export const REPORT_BARS_FALLBACK = [];

/**
 * 由超越百分比组装排名主文案（服务端 rankTitle 缺失时兜底）
 * @param {number} percent
 * @returns {string}
 */
export const buildRankTitle = (percent = REPORT_RANK_PERCENT_FALLBACK) => {
	const safe = Math.max(0, Math.min(100, Number(percent) || REPORT_RANK_PERCENT_FALLBACK));
	return `超越了 ${safe}% 的同龄人`;
};

/**
 * 从月报推送标题解析业务月份 yyyy-MM（与 server AdminAnalysisServiceImpl.parseReportMonth 对齐）
 * @param {string} title - 如「2026年8月学习报告已生成」
 * @returns {string}
 */
export const parseReportMonthFromTitle = (title = '') => {
	const matched = String(title || '').trim().match(/(\d{4})年(\d{1,2})月/);
	if (!matched) {
		return '';
	}
	const month = parseInt(matched[2], 10);
	if (!month || month > 12) {
		return '';
	}
	return `${matched[1]}-${String(month).padStart(2, '0')}`;
};

/** 由 yyyy-MM 组装报告标题（与服务端 monthTitle 格式一致） */
export const formatReportMonthTitle = (monthKey = '') => {
	const key = String(monthKey || '').trim();
	const matched = key.match(/^(\d{4})-(\d{1,2})$/);
	if (!matched) {
		return REPORT_MONTH_TITLE_FALLBACK;
	}
	const year = matched[1];
	const month = parseInt(matched[2], 10);
	if (!month || month > 12) {
		return REPORT_MONTH_TITLE_FALLBACK;
	}
	return `${year}年${month}月 · 学习报告`;
};

/** 生成最近若干自然月的业务键 yyyy-MM（含当月，倒序） */
export const buildRecentMonthKeys = (count = 12) => {
	const safeCount = Math.max(1, Math.min(24, Number(count) || 12));
	const months = [];
	const now = new Date();
	for (let index = 0; index < safeCount; index += 1) {
		const date = new Date(now.getFullYear(), now.getMonth() - index, 1);
		const year = date.getFullYear();
		const month = String(date.getMonth() + 1).padStart(2, '0');
		months.push(`${year}-${month}`);
	}
	return months;
};

/** 将服务端 AppAnalysisReportVO 规范为本地排名展示字段 */
export const normalizeReportRank = (data = {}) => {
	const rankPercent = typeof data.rankPercent === 'number'
		? Math.max(0, Math.min(100, data.rankPercent))
		: (typeof data.peerComparison?.beatPercent === 'number'
			? Math.max(0, Math.min(100, data.peerComparison.beatPercent))
			: REPORT_RANK_PERCENT_FALLBACK);

	const month = String(data.month || '').trim();
	const rankTitle = String(data.rankTitle || '').trim() || buildRankTitle(rankPercent);
	const rankSubtitle = String(data.rankSubtitle || '').trim() || REPORT_RANK_SUBTITLE_FALLBACK;
	const monthTitle = String(data.monthTitle || '').trim()
		|| (month ? formatReportMonthTitle(month) : REPORT_MONTH_TITLE_FALLBACK);

	return {
		month,
		monthTitle,
		rankTitle,
		rankSubtitle,
		rankPercent,
		monthQuestionCount: typeof data.monthQuestionCount === 'number'
			? Math.max(0, data.monthQuestionCount)
			: 0,
		correctRate: typeof data.correctRate === 'number'
			? Math.max(0, Math.min(100, data.correctRate))
			: 0
	};
};

/** 历史报告列表单项（业务月份 + 摘要字段） */
export const normalizeReportHistoryItem = (data = {}) => {
	const rank = normalizeReportRank(data);
	return {
		month: rank.month,
		monthTitle: rank.monthTitle,
		rankTitle: rank.rankTitle,
		rankSubtitle: rank.rankSubtitle,
		monthQuestionCount: rank.monthQuestionCount,
		correctRate: rank.correctRate
	};
};

/**
 * 规范月度报告学科正确率柱状图（subjectBars → 柱状高度 value 为百分比）
 * @param {Array<{ label?: string, correctRate?: number, percent?: string }>} list
 * @returns {Array<{ label: string, value: string, correctRate: number, percent: string }>}
 */
export const normalizeReportBars = (list = []) => {
	if (!Array.isArray(list) || !list.length) {
		return [];
	}

	const normalized = list
		.filter((item) => item && item.label)
		.map((item) => {
			const correctRate = typeof item.correctRate === 'number'
				? Math.max(0, Math.min(100, item.correctRate))
				: parseInt(String(item.percent || item.value || '').replace('%', ''), 10) || 0;
			const percent = String(item.percent || '').trim() || `${correctRate}%`;
			return {
				label: String(item.label).trim(),
				value: `${correctRate}%`,
				correctRate,
				percent
			};
		})
		.filter((item) => item.label);

	return normalized;
};

/**
 * 规范月度报告 AI 学习建议（aiAdvices → 固定 3 条，含序号与推荐动作）
 * @param {Array<{ order?: number, content?: string, action?: string }>} list
 * @returns {Array<{ order: number, content: string, action: string }>}
 */
export const normalizeReportAdvices = (list = []) => {
	if (!Array.isArray(list) || !list.length) {
		return [];
	}

	return list
		.filter((item) => item && String(item.content || '').trim())
		.map((item, index) => ({
			order: typeof item.order === 'number' ? item.order : index + 1,
			content: String(item.content).trim(),
			action: String(item.action || '').trim()
		}))
		.sort((a, b) => a.order - b.order)
		.slice(0, 3)
		.map((item, index) => ({ ...item, order: index + 1 }));
};

/** 将服务端 AppAnalysisReportVO 规范为本地报告展示字段（含柱状图与 AI 建议） */
export const normalizeReportAnalysis = (data = {}) => ({
	...normalizeReportRank(data),
	subjectBars: normalizeReportBars(data.subjectBars),
	aiAdvices: normalizeReportAdvices(data.aiAdvices)
});

/** 空报告结构（未登录或接口失败） */
const buildReportRankFallback = () => normalizeReportAnalysis({});

/**
 * 拉取月度报告排名文案；未登录或失败时返回空数据
 * @param {{ month?: string }} [options] - month 格式 yyyy-MM，默认当月
 * @returns {Promise<{ fromApi: boolean, month?: string, monthTitle?: string, rankTitle?: string, rankSubtitle?: string, rankPercent?: number, subjectBars?: Array<{ label: string, value: string, correctRate: number, percent: string }>, aiAdvices?: Array<{ order: number, content: string, action: string }> }>}
 */
export const fetchReportRank = async (options = {}) => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return {
			...buildReportRankFallback(),
			fromApi: false
		};
	}

	const month = String(options.month || '').trim();
	const query = month ? { month } : undefined;

	try {
		const res = await get('/app/analysis/report', query);
		if (res && res.code === 0 && res.data) {
			return {
				...normalizeReportAnalysis(res.data),
				fromApi: true
			};
		}
	} catch (e) {
		// 网络异常时静默回退 mock
	}

	return {
		...buildReportRankFallback(),
		fromApi: false
	};
};

/** 历史报告列表空结构 */
export const buildReportHistoryFallback = () => [];

/**
 * 拉取历史月份报告列表；登录态下并行请求 GET /app/analysis/report?month=yyyy-MM
 * @param {{ limit?: number }} [options]
 * @returns {Promise<{ fromApi: boolean, list: Array<{ month: string, monthTitle: string, rankTitle: string, rankSubtitle: string, monthQuestionCount: number, correctRate: number }> }>}
 */
export const fetchReportHistory = async (options = {}) => {
	const auth = getAuthState();
	const limit = options.limit || 12;
	if (!auth.loggedIn) {
		return {
			list: buildReportHistoryFallback(),
			fromApi: false
		};
	}

	const monthKeys = buildRecentMonthKeys(limit);
	try {
		const settled = await Promise.allSettled(monthKeys.map(async (month) => {
			const res = await get('/app/analysis/report', { month });
			if (res && res.code === 0 && res.data) {
				return normalizeReportHistoryItem(res.data);
			}
			return null;
		}));
		const list = settled
			.map((item) => (item.status === 'fulfilled' ? item.value : null))
			.filter(Boolean)
			.sort((a, b) => String(b.month).localeCompare(String(a.month)));

		if (list.length) {
			return {
				list,
				fromApi: true
			};
		}
	} catch (e) {
		// 网络异常时静默回退 mock
	}

	return {
		list: buildReportHistoryFallback(),
		fromApi: false
	};
};
