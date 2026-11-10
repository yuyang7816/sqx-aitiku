/**
 * 模块二十三 · 错题本规则（文档 23.5）
 * - wrongCount ≥ 2 且未掌握 → 「高频错题」
 * - mastered=true → 「已掌握」，不参与 wrong 模式练习
 * - 清空仅删当前筛选结果
 */
import { resolveSubjectDisplayName } from '@/common/profile-api-params';

/** 「高频错题」筛选阈值：累计错题次数 ≥ 2 且未掌握 */
export const NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT = 2;

/** 错题卡片掌握状态：已掌握 */
export const NOTEBOOK_STATUS_MASTERED = '已掌握';

/** 错题卡片掌握状态：高频错题（未掌握且 wrongCount 达阈值） */
export const NOTEBOOK_STATUS_HIGH_FREQ = '高频错题';

/** 错题卡片掌握状态：待复习 */
export const NOTEBOOK_STATUS_PENDING = '待复习';

/** 无具体学科信息的占位 subject（筛选时需结合章节学科推断） */
export const NOTEBOOK_GENERIC_SUBJECTS = ['章节练习', '错题重做', '通用题目'];

/** 学科展示文案分隔符：数学 · 初三 / 数学・初三 */
export const NOTEBOOK_SUBJECT_SEPARATORS = /\s*[·・]\s*/;

/** 常见学科展示名（questionCode / 题干推断兜底） */
export const NOTEBOOK_KNOWN_SUBJECT_NAMES = ['数学', '语文', '英语', '物理', '化学', '生物', '历史', '地理', '政治'];

/** questionCode / chapter_code 前缀 → 学科（占位 subject 筛选推断） */
const NOTEBOOK_QUESTION_CODE_SUBJECT_RULES = [
	[/^(rational-numbers|polynomial|coord-translate|math-)/i, '数学'],
	[/^(english)/i, '英语'],
	[/^(mechanics|physics)/i, '物理'],
	[/^(chinese)/i, '语文'],
	[/^(chemistry|chem-)/i, '化学'],
	[/^(biology|bio-)/i, '生物'],
	[/^(history)/i, '历史'],
	[/^(geography|geo-)/i, '地理'],
	[/^(politics)/i, '政治']
];

/** 从 questionCode / id 推断学科（章节练习入库 subject 常为占位文案时使用） */
export const inferNotebookSubjectFromQuestionCode = (code = '') => {
	const token = String(code || '').trim();
	if (!token) {
		return '';
	}
	for (const [pattern, subject] of NOTEBOOK_QUESTION_CODE_SUBJECT_RULES) {
		if (pattern.test(token)) {
			return subject;
		}
	}
	return '';
};

/** 从错题本 subject 展示文案提取学科名（如「数学 · 初三」→「数学」） */
export const getNotebookItemSubjectName = (subject = '') => {
	const text = String(subject || '').trim();
	if (!text) {
		return '';
	}
	const [name] = text.split(NOTEBOOK_SUBJECT_SEPARATORS);
	return resolveSubjectDisplayName((name || text).trim());
};

/** 是否为无具体学科的占位 subject */
export const isNotebookGenericSubject = (subject = '') => {
	const text = String(subject || '').trim();
	if (!text) {
		return true;
	}
	if (NOTEBOOK_GENERIC_SUBJECTS.includes(text)) {
		return true;
	}
	if (text.startsWith('错题重做')) {
		return true;
	}
	const head = getNotebookItemSubjectName(text);
	return !head || NOTEBOOK_GENERIC_SUBJECTS.includes(head);
};

/** 规范化学科展示名（兼容 math / 数学 · 初一 / 数学） */
export const normalizeNotebookSubjectLabel = (subject = '') => (
	resolveSubjectDisplayName(getNotebookItemSubjectName(subject) || String(subject || '').trim())
);

const findKnownSubjectInText = (text = '') => {
	const haystack = String(text || '').trim();
	if (!haystack) {
		return '';
	}
	return NOTEBOOK_KNOWN_SUBJECT_NAMES.find((name) => haystack.includes(name)) || '';
};

/**
 * 解析错题条目的有效学科（筛选 / 展示用）
 * 优先 subject 字段；占位 subject 时从 questionCode、tags、题干推断
 */
export const resolveNotebookItemSubject = (item = {}) => {
	const rawSubject = String(item?.subject || '').trim();
	if (rawSubject && !isNotebookGenericSubject(rawSubject)) {
		return normalizeNotebookSubjectLabel(rawSubject);
	}
	const fromCode = inferNotebookSubjectFromQuestionCode(
		item?.questionCode || item?.questionId || item?.id || ''
	);
	if (fromCode) {
		return fromCode;
	}
	const fromTags = (Array.isArray(item?.tags) ? item.tags : [])
		.map((tag) => findKnownSubjectInText(tag))
		.find(Boolean);
	if (fromTags) {
		return fromTags;
	}
	const fromQuestion = findKnownSubjectInText(item?.question || item?.title || '');
	if (fromQuestion) {
		return fromQuestion;
	}
	if (rawSubject) {
		const normalized = normalizeNotebookSubjectLabel(rawSubject);
		if (normalized && !isNotebookGenericSubject(normalized)) {
			return normalized;
		}
	}
	return '';
};

/** 错题卡片展示用学科文案（占位 subject 时尽量展示推断结果） */
export const formatNotebookItemSubject = (item = {}) => {
	const resolved = resolveNotebookItemSubject(item);
	if (resolved) {
		return resolved;
	}
	const rawSubject = String(item?.subject || '').trim();
	return rawSubject || '通用题目';
};

/** 错题条目是否命中学科 Chip（兼容编码、年级后缀、占位 subject 推断） */
export const notebookSubjectMatchesChip = (item = {}, chip = '') => {
	const target = normalizeNotebookSubjectLabel(chip);
	if (!target) {
		return true;
	}
	const itemSubject = resolveNotebookItemSubject(item);
	return Boolean(itemSubject) && itemSubject === target;
};

/** 按学科过滤错题本 */
export const filterNotebookBySubject = (list = [], chip = '') => {
	const target = normalizeNotebookSubjectLabel(chip);
	if (!target) {
		return Array.isArray(list) ? list : [];
	}
	return (Array.isArray(list) ? list : []).filter((item) => notebookSubjectMatchesChip(item, target));
};

/** 错题本条目业务键：优先 id，fallback 题干 */
export const getNotebookItemKey = (item = {}) => {
	const id = String(item.id || '').trim();
	if (id) {
		return id;
	}
	return String(item.question || '').trim();
};

/** 规则 23.5-2：是否已掌握 */
export const isNotebookMasteredItem = (item = {}) => Boolean(item?.mastered);

/**
 * 规则 23.5-1：是否高频错题（未掌握且 wrongCount ≥ 阈值）
 */
export const isNotebookHighFreqItem = (item = {}, minWrongCount = NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT) => {
	if (!item || isNotebookMasteredItem(item)) {
		return false;
	}
	const threshold = Math.max(1, Number(minWrongCount) || NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT);
	return Number(item.wrongCount || 0) >= threshold;
};

/** 规则 23.5-1：高频错题列表 */
export const filterNotebookByHighFreq = (list = [], minWrongCount = NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT) => (
	(Array.isArray(list) ? list : []).filter((item) => isNotebookHighFreqItem(item, minWrongCount))
);

/** 规则 23.5-2：wrong 模式始终排除已掌握条目 */
export const filterNotebookForWrongMode = (list = []) => (
	(Array.isArray(list) ? list : []).filter((item) => !isNotebookMasteredItem(item))
);

/** 错题卡片 status 文案：已掌握 / 高频错题 / 待复习 */
export const getNotebookItemStatusText = (item = {}) => {
	if (isNotebookMasteredItem(item)) {
		return NOTEBOOK_STATUS_MASTERED;
	}
	if (isNotebookHighFreqItem(item)) {
		return NOTEBOOK_STATUS_HIGH_FREQ;
	}
	return NOTEBOOK_STATUS_PENDING;
};

/** 错题卡片 status 样式类名：mastered / high / pending */
export const getNotebookItemStatusClass = (item = {}) => {
	if (isNotebookMasteredItem(item)) {
		return 'mastered';
	}
	if (isNotebookHighFreqItem(item)) {
		return 'high';
	}
	return 'pending';
};

/**
 * 规则 23.5-3：清空仅删当前筛选 — 纯函数，不含持久化
 * @param {object[]} list - 完整错题本列表
 * @param {string[]} removeKeys - 当前筛选命中条目的业务键
 */
export const resolveClearNotebookFilteredList = (list = [], removeKeys = []) => {
	const keySet = new Set(
		(Array.isArray(removeKeys) ? removeKeys : [])
			.map((key) => String(key || '').trim())
			.filter(Boolean)
	);
	if (!keySet.size) {
		return Array.isArray(list) ? [...list] : [];
	}
	return (Array.isArray(list) ? list : []).filter(
		(item) => !keySet.has(getNotebookItemKey(item))
	);
};

/**
 * wrong 模式题目范围（规则 23.5-2：Chip/批量键预筛后仍排除已掌握）
 * @param {object[]} allItems - 错题本全量或调用方传入的源列表
 * @param {object} [options]
 * @param {string} [options.filterChip] - 错题本 Chip 筛选
 * @param {string[]} [options.batchKeys] - 批量重做指定业务键
 * @param {(chip: string, list: object[]) => object[]} [options.applyChipFilter] - Chip 过滤（由 app-data 注入）
 */
export const resolveNotebookItemsForWrongPractice = (
	allItems = [],
	{ filterChip = '', batchKeys = [], applyChipFilter } = {}
) => {
	let items = Array.isArray(allItems) ? allItems : [];
	const chip = String(filterChip || '').trim();
	const keys = (Array.isArray(batchKeys) ? batchKeys : [])
		.map((key) => String(key || '').trim())
		.filter(Boolean);

	if (chip && typeof applyChipFilter === 'function') {
		items = applyChipFilter(chip, items);
	}
	if (keys.length) {
		const keySet = new Set(keys);
		items = items.filter((item) => keySet.has(getNotebookItemKey(item)));
	}
	return filterNotebookForWrongMode(items);
};
