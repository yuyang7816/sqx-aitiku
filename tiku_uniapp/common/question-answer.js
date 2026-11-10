import { resolveQuestionTypeLabel } from '@/common/question-labels';

/** 题型 → 答题交互模式 */
const INPUT_MODE_BY_TYPE = {
	单选题: 'single',
	判断题: 'single',
	多选题: 'multi',
	填空题: 'fill'
};

/**
 * 根据题目数据解析答题交互模式
 * @param {object} question
 * @returns {'single'|'multi'|'fill'}
 */
export const resolveQuestionInputMode = (question = {}) => {
	const typeLabel = resolveQuestionTypeLabel(question);
	return INPUT_MODE_BY_TYPE[typeLabel] || 'single';
};

/** 选项 label 规范化：判分与选中态比较共用 */
export const normalizeOptionLabel = (value) => String(value || '').trim().toUpperCase();

/**
 * 多选答案解析为去重排序后的 label 数组
 * 支持 "A,C" / "AC" / "a c" 等格式
 */
export const parseMultiAnswerLabels = (value = '') => {
	const raw = String(value || '').trim().toUpperCase();
	if (!raw) {
		return [];
	}
	if (raw.includes(',') || raw.includes('、') || raw.includes('|') || raw.includes(' ')) {
		return [...new Set(raw.split(/[,、|\s]+/).map(normalizeOptionLabel).filter(Boolean))].sort();
	}
	return [...new Set(raw.split('').map(normalizeOptionLabel).filter(Boolean))].sort();
};

/** 多选答案序列化为存储字符串（逗号分隔、字母序） */
export const formatMultiAnswerLabels = (labels = []) => (
	[...new Set(labels.map(normalizeOptionLabel).filter(Boolean))].sort().join(',')
);

/**
 * 填空题期望答案：优先使用 rightAnswer 文本；若为单字母且在选项中则取对应 value
 */
export const resolveFillExpectedAnswer = (rightAnswer = '', options = []) => {
	const raw = String(rightAnswer || '').trim();
	if (!raw) {
		return '';
	}
	const normalized = normalizeOptionLabel(raw);
	if (normalized.length === 1 && Array.isArray(options) && options.length) {
		const matched = options.find((item) => normalizeOptionLabel(item?.label) === normalized);
		if (matched) {
			return String(matched.value ?? '').trim();
		}
	}
	return raw;
};

/** 填空题答案规范化：忽略首尾空格与大小写 */
export const normalizeFillAnswer = (value = '') => String(value || '').trim().toLowerCase();

/**
 * 本地判分：按题型比较用户答案与正确答案
 * @param {string} userAnswer
 * @param {object} question - 含 type/rightAnswer/options
 */
export const isQuestionAnswerCorrect = (userAnswer = '', question = {}) => {
	const mode = resolveQuestionInputMode(question);
	const rightAnswer = question.rightAnswer || '';

	if (mode === 'multi') {
		const mine = parseMultiAnswerLabels(userAnswer);
		if (!mine.length) {
			return false;
		}
		const expected = parseMultiAnswerLabels(rightAnswer);
		if (!expected.length) {
			return false;
		}
		return mine.length === expected.length && mine.every((label, index) => label === expected[index]);
	}

	if (mode === 'fill') {
		const mine = normalizeFillAnswer(userAnswer);
		if (!mine) {
			return false;
		}
		const expectedText = normalizeFillAnswer(resolveFillExpectedAnswer(rightAnswer, question.options));
		if (!expectedText) {
			return false;
		}
		return mine === expectedText;
	}

	const mine = normalizeOptionLabel(userAnswer);
	if (!mine) {
		return false;
	}
	return mine === normalizeOptionLabel(rightAnswer);
};

/** 当前题是否已作答（各题型统一） */
export const hasQuestionAnswer = (userAnswer = '', question = {}) => {
	const mode = resolveQuestionInputMode(question);
	if (mode === 'multi') {
		return parseMultiAnswerLabels(userAnswer).length > 0;
	}
	if (mode === 'fill') {
		return Boolean(normalizeFillAnswer(userAnswer));
	}
	return Boolean(normalizeOptionLabel(userAnswer));
};
