/**
 * 学情概览：GET /app/analysis/overview，失败返回空/零值
 */
import { get } from '@/common/api';
import { getAuthState } from '@/common/app-data';

/** 接口无数据时的默认展示 */
export const OVERVIEW_WEEK_QUESTION_FALLBACK = 0;
export const OVERVIEW_WEEK_QUESTION_CHANGE_FALLBACK = 0;
export const OVERVIEW_CORRECT_RATE_FALLBACK = 0;
export const OVERVIEW_CORRECT_RATE_CHANGE_FALLBACK = 0;

/** 接口无数据时的空雷达维度 / 薄弱点列表 */
export const OVERVIEW_RADAR_DIMENSIONS_FALLBACK = [];
export const OVERVIEW_WEAK_POINTS_FALLBACK = [];

/**
 * 规范雷达维度（学科名 + 得分 0~100）
 * @param {Array<{ name?: string, score?: number }>} list
 * @returns {Array<{ name: string, score: number }>}
 */
export const normalizeRadarDimensions = (list = []) => {
	if (!Array.isArray(list) || !list.length) {
		return [];
	}
	return list
		.filter((item) => item && item.name)
		.map((item) => {
			const rawScore = typeof item.score === 'number'
				? item.score
				: parseInt(String(item.score ?? '').replace(/[^\d.-]/g, ''), 10);
			const score = Number.isFinite(rawScore) ? rawScore : 0;
			return {
				name: String(item.name).trim(),
				score: Math.max(0, Math.min(100, score))
			};
		})
		.filter((item) => item.name);
};

/**
 * 规范薄弱知识点 Top3（章标题 + 正确率百分比 + 推荐动作）
 * @param {Array<{ title?: string, percent?: string, correctRate?: number, chapterCode?: string, action?: string }>} list
 * @returns {Array<{ title: string, percent: string, correctRate: number, chapterCode: string, action: string }>}
 */
export const normalizeWeakPoints = (list = []) => {
	if (!Array.isArray(list) || !list.length) {
		return [];
	}
	return list
		.filter((item) => item && item.title)
		.map((item) => {
			const correctRate = typeof item.correctRate === 'number'
				? Math.max(0, Math.min(100, item.correctRate))
				: parseInt(String(item.percent || '').replace('%', ''), 10) || 0;
			return {
				title: String(item.title).trim(),
				percent: item.percent || `${correctRate}%`,
				correctRate,
				chapterCode: String(item.chapterCode || '').trim(),
				action: String(item.action || 'ocr-practice').trim() || 'ocr-practice',
				questionCount: typeof item.questionCount === 'number' ? Math.max(0, item.questionCount) : 0
			};
		})
		.slice(0, 3);
};

/** 将服务端 AppAnalysisOverviewVO 规范为本地结构 */
export const normalizeOverviewAnalysis = (data = {}) => ({
	weekStartDate: data.weekStartDate || '',
	weekEndDate: data.weekEndDate || '',
	weekQuestionCount: typeof data.weekQuestionCount === 'number'
		? Math.max(0, data.weekQuestionCount)
		: OVERVIEW_WEEK_QUESTION_FALLBACK,
	weekQuestionChangePercent: typeof data.weekQuestionChangePercent === 'number'
		? data.weekQuestionChangePercent
		: OVERVIEW_WEEK_QUESTION_CHANGE_FALLBACK,
	correctRate: typeof data.correctRate === 'number'
		? Math.max(0, Math.min(100, data.correctRate))
		: OVERVIEW_CORRECT_RATE_FALLBACK,
	correctRateChange: typeof data.correctRateChange === 'number'
		? data.correctRateChange
		: OVERVIEW_CORRECT_RATE_CHANGE_FALLBACK,
	radarDimensions: normalizeRadarDimensions(data.radarDimensions),
	weakPoints: normalizeWeakPoints(data.weakPoints)
});

/**
 * 格式化较上周题量变化文案（weekQuestionChangePercent：12 表示 +12%）
 * @param {number} percent
 * @returns {{ text: string, trend: 'up' | 'down' | 'flat' }}
 */
export const formatWeekQuestionChange = (percent = 0) => {
	const value = Number(percent) || 0;
	if (value > 0) {
		return {
			text: `+${value}% 较上周`,
			trend: 'up'
		};
	}
	if (value < 0) {
		return {
			text: `${value}% 较上周`,
			trend: 'down'
		};
	}
	return {
		text: '与上周持平',
		trend: 'flat'
	};
};

/**
 * 格式化较上周正确率变化文案（correctRateChange：百分点，-3 表示下降 3 个百分点）
 * @param {number} points
 * @returns {{ text: string, trend: 'up' | 'down' | 'flat' }}
 */
export const formatCorrectRateChange = (points = 0) => {
	const value = Number(points) || 0;
	if (value > 0) {
		return {
			text: `+${value}% 较上周`,
			trend: 'up'
		};
	}
	if (value < 0) {
		return {
			text: `${value}% 较上周`,
			trend: 'down'
		};
	}
	return {
		text: '与上周持平',
		trend: 'flat'
	};
};

/** 组装学情概览本地展示字段（含较上周文案） */
const buildOverviewView = (normalized, fromApi) => {
	const weekChange = formatWeekQuestionChange(normalized.weekQuestionChangePercent);
	const rateChange = formatCorrectRateChange(normalized.correctRateChange);
	return {
		...normalized,
		weekQuestionChangeText: weekChange.text,
		weekQuestionChangeTrend: weekChange.trend,
		correctRateChangeText: rateChange.text,
		correctRateChangeTrend: rateChange.trend,
		fromApi
	};
};

/** 空概览结构（未登录或接口失败） */
const buildOverviewFallback = () => buildOverviewView(normalizeOverviewAnalysis({}), false);

/**
 * 拉取学情概览；未登录或失败时返回 { fromApi: false } 并使用零值
 * @returns {Promise<{ fromApi: boolean, weekQuestionCount?: number, correctRate?: number, radarDimensions?: Array, weakPoints?: Array, correctRateChangeText?: string, correctRateChangeTrend?: string }>}
 */
export const fetchOverviewAnalysis = async () => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return buildOverviewFallback();
	}

	try {
		const res = await get('/app/analysis/overview');
		if (res && res.code === 0 && res.data) {
			return buildOverviewView(normalizeOverviewAnalysis(res.data), true);
		}
	} catch (e) {
		// 网络异常
	}

	return buildOverviewFallback();
};

/**
 * 薄弱知识点 Top3：对接 GET /app/analysis/overview.weakPoints
 * @returns {Promise<Array<{ title: string, percent: string, correctRate: number, chapterCode: string, action: string }>>}
 */
export const getWeakPointRecommendations = async () => {
	const overview = await fetchOverviewAnalysis();
	return (overview.weakPoints || []).map((item) => ({ ...item }));
};
