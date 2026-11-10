/** 做题状态 type（与题型 questionType 区分，避免误展示为题型标签） */
const ANSWER_STATUS_TYPES = new Set(['right', 'wrong', 'todo']);

/** 难度文案 → 标签样式类（答题页难度 Tag 配色） */
const DIFFICULTY_CLASS_MAP = {
	简单: 'easy',
	中等: 'medium',
	困难: 'hard',
	复习: 'review'
};

/**
 * 规范题型展示文案（优先 questionType，排除做题状态 type）
 * @param {object} item - 题目项（含 type / questionType）
 * @param {string} [fallback='单选题']
 */
export const resolveQuestionTypeLabel = (item = {}, fallback = '单选题') => {
	const questionType = String(item.questionType || '').trim();
	if (questionType) {
		return questionType;
	}
	const rawType = String(item.type || '').trim();
	if (rawType && !ANSWER_STATUS_TYPES.has(rawType)) {
		return rawType;
	}
	return fallback;
};

/**
 * 规范难度展示文案
 * @param {object|string} itemOrText
 * @param {string} [fallback='中等']
 */
export const resolveDifficultyLabel = (itemOrText = {}, fallback = '中等') => {
	if (typeof itemOrText === 'string') {
		const text = itemOrText.trim();
		return text || fallback;
	}
	const difficulty = String(itemOrText.difficulty || '').trim();
	return difficulty || fallback;
};

/** 难度 Tag 样式类名（无匹配时默认 medium） */
export const getDifficultyTagClass = (difficulty = '') => {
	const label = resolveDifficultyLabel(difficulty);
	return DIFFICULTY_CLASS_MAP[label] || 'medium';
};

/** 将列表项或详情合并为答题页题型/难度标签 */
export const buildQuestionMetaLabels = (item = {}, detail = {}) => ({
	type: resolveQuestionTypeLabel({ ...detail, ...item }),
	difficulty: resolveDifficultyLabel({ ...detail, ...item })
});
