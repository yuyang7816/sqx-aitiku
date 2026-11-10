/**
 * 章节题目列表：GET /app/question/list，失败返回空列表
 */
import { get, PAGE_LOAD_REQUEST_OPTIONS } from '@/common/api';
import {
	applyChapterAnswerOverlay,
	getAuthState,
	getNotebookFilteredList,
	getNotebookItemKey,
	getNotebookList,
	getUserProfile,
	resolveNotebookQuestionToken
} from '@/common/app-data';
import { resolveNotebookItemsForWrongPractice } from '@/common/notebook-rules';
import { getDefaultQuestionBankPracticeList } from '@/common/practice-fallback';
import { buildQuestionMetaLabels } from '@/common/question-labels';

/** 章节详情页默认分页大小（与服务端约定一致） */
export const QUESTION_LIST_PAGE_SIZE = 50;

/** 章节题目列表排序：按题号顺序 */
export const CHAPTER_SORT_DEFAULT = 'default';
/** 章节题目列表排序：错题/未做优先（做错 > 未做 > 做对，同组内保持原序） */
export const CHAPTER_SORT_WRONG_FIRST = 'wrong-first';

/** 错题优先模式下各做题状态权重（越小越靠前） */
const WRONG_FIRST_TYPE_WEIGHT = {
	wrong: 0,
	todo: 1,
	right: 2
};

/** 排序模式对应右上角链接文案 */
export const getChapterSortLabel = (mode = CHAPTER_SORT_DEFAULT) => (
	mode === CHAPTER_SORT_WRONG_FIRST ? '错题优先' : '顺序排列'
);

/** 切换排序模式 */
export const toggleChapterSortMode = (mode = CHAPTER_SORT_DEFAULT) => (
	mode === CHAPTER_SORT_DEFAULT ? CHAPTER_SORT_WRONG_FIRST : CHAPTER_SORT_DEFAULT
);

/**
 * 章节详情题目列表排序（前端本地，不改 API 顺序）
 * @param {object[]} list
 * @param {'default'|'wrong-first'} mode
 */
export const sortChapterQuestions = (list, mode = CHAPTER_SORT_DEFAULT) => {
	if (!Array.isArray(list) || !list.length || mode === CHAPTER_SORT_DEFAULT) {
		return list;
	}
	return list
		.map((item, index) => ({ item, index }))
		.sort((a, b) => {
			const weightA = WRONG_FIRST_TYPE_WEIGHT[a.item.type] ?? WRONG_FIRST_TYPE_WEIGHT.todo;
			const weightB = WRONG_FIRST_TYPE_WEIGHT[b.item.type] ?? WRONG_FIRST_TYPE_WEIGHT.todo;
			if (weightA !== weightB) {
				return weightA - weightB;
			}
			return a.index - b.index;
		})
		.map(({ item }) => item);
};

/** practiceSource.mode 与服务端 GET /app/question/list mode 一致 */
const PRACTICE_LIST_MODES = ['all', 'wrong', 'single'];

/** 错题本条目转为答题页题目结构（API 不可用时的本地兜底） */
const mapNotebookItemToPracticeQuestion = (item = {}) => ({
	type: '错题重做',
	difficulty: '复习',
	title: item.question || item.title || '',
	rightAnswer: item.right || item.rightAnswer || '',
	analysis: item.analysis || '',
	aiSummary: item.aiSummary || item.analysis || '',
	options: Array.isArray(item.options) ? item.options : [],
	questionId: item.questionId,
	questionCode: item.questionCode
});

/**
 * 按 practiceSource 筛选错题本条目（与 getPracticeQuestionList wrong 分支一致）
 * @param {object} source - practiceSource
 */
const resolveNotebookItemsForWrongSource = (source = {}) => {
	const profile = getUserProfile();
	return resolveNotebookItemsForWrongPractice(getNotebookList(), {
		filterChip: String(source.notebookFilterChip || '').trim(),
		batchKeys: Array.isArray(source.notebookQuestionKeys) ? source.notebookQuestionKeys : [],
		applyChipFilter: (chip, list) => getNotebookFilteredList(chip, list, profile)
	});
};

/** 错题模式：从错题本解析 questionIds（逗号分隔，供 GET /app/question/list） */
const resolveNotebookQuestionIdsForSource = (source = {}) => {
	const ids = resolveNotebookItemsForWrongSource(source)
		.map(resolveNotebookQuestionToken)
		.filter(Boolean);
	return [...new Set(ids)].join(',');
};

/** 单题重做：按题干在错题本中解析 questionId */
const resolveQuestionIdByNotebookTitle = (title = '') => {
	const text = String(title || '').trim();
	if (!text) {
		return '';
	}
	const item = getNotebookList().find((entry) => (
		String(entry.question || '').trim() === text
		|| String(entry.title || '').trim() === text
	));
	return item ? resolveNotebookQuestionToken(item) : '';
};

/** 将服务端 AppQuestionListItemVO 规范为列表项结构（章节详情 + 答题页共用） */
export const normalizeQuestionListItem = (item = {}) => ({
	no: item.no || '',
	text: item.text || item.title || '',
	status: item.status || '未做',
	type: item.type || 'todo',
	questionId: item.questionId,
	questionCode: item.questionCode,
	title: item.title || item.text || '',
	questionType: item.questionType || '',
	difficulty: item.difficulty || '',
	rightAnswer: item.rightAnswer || '',
	analysis: item.analysis || '',
	options: Array.isArray(item.options) ? item.options : []
});

/**
 * 根据 practiceSource 构建 GET /app/question/list 查询参数
 * @param {object} source - practiceSource（mode/chapterId/paperId/questionId）
 * @returns {object|null} 无法调用 API 时返回 null（错题模式可走错题本本地兜底）
 */
export const buildPracticeListQuery = (source = {}) => {
	const rawMode = String(source.mode || 'all').trim();
	const mode = PRACTICE_LIST_MODES.includes(rawMode) ? rawMode : 'all';
	const chapterId = String(source.chapterId || '').trim();
	const paperId = String(source.paperId || '').trim();
	const questionId = String(source.questionId || '').trim();
	const knowledgePoint = String(source.knowledgePoint || '').trim();

	if (paperId) {
		return {
			paperId,
			mode: mode === 'wrong' ? 'wrong' : 'all'
		};
	}
	if (mode === 'single') {
		const targetQuestionId = questionId || resolveQuestionIdByNotebookTitle(source.questionTitle);
		if (targetQuestionId) {
			return {
				questionIds: targetQuestionId,
				mode: 'single',
				...(chapterId ? { chapterId } : {})
			};
		}
		return null;
	}
	if (mode === 'wrong' && !chapterId && !paperId) {
		const questionIds = resolveNotebookQuestionIdsForSource(source);
		if (questionIds) {
			return { questionIds, mode: 'wrong' };
		}
		return null;
	}
	if (chapterId) {
		return { chapterId, mode };
	}
	if (knowledgePoint) {
		return { knowledgePoint, mode: 'all' };
	}
	return null;
};

const emptyQuestionListPage = (page, size) => ({
	list: [],
	total: 0,
	current: page,
	size,
	fromApi: false,
	hasMore: false
});

/** 空题目列表 */
export const getFallbackChapterQuestions = () => [];

/**
 * 拉取练习题目分页（GET /app/question/list，支持 chapterId/paperId/questionIds/mode）
 * @param {object} queryParams
 * @param {{ page?: number, size?: number }} [options]
 */
export const fetchQuestionListPage = async (queryParams = {}, { page = 1, size = QUESTION_LIST_PAGE_SIZE } = {}) => {
	const chapterId = String(queryParams.chapterId || '').trim();
	const paperId = String(queryParams.paperId || '').trim();
	const questionIds = String(queryParams.questionIds || '').trim();
	const knowledgePoint = String(queryParams.knowledgePoint || '').trim();
	const mode = String(queryParams.mode || 'all').trim();

	if (!chapterId && !paperId && !questionIds && !knowledgePoint) {
		return emptyQuestionListPage(page, size);
	}

	const auth = getAuthState();
	if (!auth.loggedIn) {
		return emptyQuestionListPage(page, size);
	}

	try {
		const params = { page, size, mode };
		if (chapterId) {
			params.chapterId = chapterId;
		}
		if (paperId) {
			params.paperId = paperId;
		}
		if (questionIds) {
			params.questionIds = questionIds;
		}
		if (knowledgePoint) {
			params.knowledgePoint = knowledgePoint;
		}
		const res = await get('/app/question/list', params, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && res.data) {
			const data = res.data;
			const rawRecords = Array.isArray(data.records) ? data.records : null;
			if (rawRecords) {
				const list = applyChapterAnswerOverlay(rawRecords.map(normalizeQuestionListItem));
				const total = Number(data.total) || 0;
				const current = Number(data.current) || page;
				const pageSize = Number(data.size) || size;
				return {
					list,
					total,
					current,
					size: pageSize,
					fromApi: true,
					hasMore: current * pageSize < total
				};
			}
		}
	} catch (e) {
		// 网络异常时返回空列表
	}

	return emptyQuestionListPage(page, size);
};

/**
 * 拉取章节题目分页列表；已登录优先 API，失败时返回空列表
 * @param {string} chapterId - 章业务编码（chapter_code）
 * @param {{ page?: number, size?: number }} [options]
 * @returns {Promise<{ list: object[], total: number, current: number, size: number, fromApi: boolean, hasMore: boolean }>}
 */
export const fetchQuestionList = async (chapterId, { page = 1, size = QUESTION_LIST_PAGE_SIZE } = {}) => {
	const id = String(chapterId || '').trim();
	if (!id) {
		return emptyQuestionListPage(page, size);
	}
	return fetchQuestionListPage({ chapterId: id, mode: 'all' }, { page, size });
};

/** 答题页占位选项（列表项无法匹配详情时使用） */
const PLACEHOLDER_OPTIONS = [
	{ label: 'A', value: '选项 A' },
	{ label: 'B', value: '选项 B' },
	{ label: 'C', value: '选项 C' },
	{ label: 'D', value: '选项 D' }
];

/** 按 API 列表项补全为答题页题目结构 */
export const mapListItemToPracticeQuestion = (item = {}) => {
	const title = item.title || item.text || '';
	const apiOptions = Array.isArray(item.options) && item.options.length ? item.options : null;
	const metaLabels = buildQuestionMetaLabels(item, {});
	return {
		type: metaLabels.type,
		difficulty: metaLabels.difficulty,
		title: title || '',
		rightAnswer: item.rightAnswer || '',
		analysis: item.analysis || '',
		aiSummary: item.aiSummary || item.analysis || '',
		tags: Array.isArray(item.tags) && item.tags.length ? item.tags : [],
		options: apiOptions || PLACEHOLDER_OPTIONS,
		questionId: item.questionId,
		questionCode: item.questionCode
	};
};

/** 分页拉取全部题目列表项（自动翻页） */
const fetchAllQuestionListItems = async (queryParams = {}) => {
	const allList = [];
	let page = 1;
	let hasMore = true;

	while (hasMore) {
		const result = await fetchQuestionListPage(queryParams, { page });
		allList.push(...result.list);
		hasMore = result.hasMore;
		page += 1;
	}

	return allList;
};

/** 错题模式 API 无结果时，用错题本本地条目兜底 */
const fetchNotebookWrongQuestionsFallback = (source = {}) => (
	resolveNotebookItemsForWrongSource(source)
		.map(mapNotebookItemToPracticeQuestion)
		.filter((item) => item.title && Array.isArray(item.options) && item.options.length)
);

/** wrong 模式：错题本 → 无则 fallback 默认题库（文档 24.2） */
const resolveWrongModeQuestions = (source = {}) => {
	const notebookQuestions = fetchNotebookWrongQuestionsFallback(source);
	if (notebookQuestions.length) {
		return notebookQuestions;
	}
	return getDefaultQuestionBankPracticeList();
};

/**
 * 按 practiceSource 拉取答题页题目（GET /app/question/list，与章节详情列表同源）
 * @param {object} source - practiceSource
 * @returns {Promise<object[]>}
 */
export const fetchPracticeQuestionsBySource = async (source = {}) => {
	const mode = String(source.mode || 'all').trim();
	const query = buildPracticeListQuery(source);
	if (!query) {
		if (mode === 'wrong') {
			return resolveWrongModeQuestions(source);
		}
		return [];
	}

	const allList = await fetchAllQuestionListItems(query);
	const mapped = allList.map(mapListItemToPracticeQuestion).filter((item) => item.title);
	if (mapped.length) {
		return mapped;
	}

	if (mode === 'wrong') {
		return resolveWrongModeQuestions(source);
	}
	return [];
};

/**
 * 拉取章节全部题目并转为答题页结构（与章节详情列表同源 GET /app/question/list）
 * @param {string} chapterId - 章业务编码（chapter_code）
 */
export const fetchChapterPracticeQuestions = async (chapterId) => fetchPracticeQuestionsBySource({
	mode: 'all',
	chapterId: String(chapterId || '').trim()
});

/**
 * 章节单题预览：按 questionId 或 questionCode 拉取单题答题结构
 * @param {string} chapterId - 章业务编码
 * @param {string|number} questionId - 题目 id 或 question_code
 */
export const fetchSinglePracticeQuestion = async (chapterId, questionId) => {
	const targetId = String(questionId || '').trim();
	if (!targetId) {
		return null;
	}

	const list = await fetchPracticeQuestionsBySource({
		mode: 'single',
		questionId: targetId,
		chapterId: String(chapterId || '').trim()
	});
	if (list.length) {
		return list[0];
	}
	return null;
};
