/**
 * 练习规则：切题 / 交卷 / 错题本同步
 * - 允许跳过未答题，先刷后面的再回来补
 * - 完成练习时若有未答题，提示后可选择仍要交卷（未答按错题计分）
 */
import { hasQuestionAnswer } from './question-answer.js';

/** 完成练习时未答题弹窗文案 */
export const buildUnansweredCompleteModalContent = (remainingCount = 0) => {
	const count = Math.max(0, Number(remainingCount) || 0);
	if (count <= 0) {
		return '';
	}
	return `还有 ${count} 题未作答，未作答题将按错题计分。是否仍要交卷？`;
};

/** @deprecated 保留兼容旧校验脚本，切题不再强制先作答 */
export const ANSWER_REQUIRED_TIP = '请先选择答案';

/**
 * 首道未作答题序号（0-based），全部已答返回 -1
 * @param {object[]} questionList
 * @param {Record<number, string>} selectedMap
 */
export const findFirstUnansweredIndex = (questionList = [], selectedMap = {}) => (
	questionList.findIndex((item, index) => !hasQuestionAnswer(selectedMap[index], item))
);

/**
 * 已作答题数量
 */
export const countAnsweredQuestions = (questionList = [], selectedMap = {}) => (
	questionList.filter((item, index) => hasQuestionAnswer(selectedMap[index], item)).length
);

/**
 * 完成练习前校验
 * @returns {{ action: 'submit'|'confirm-unanswered', unansweredCount?: number, firstPendingIndex?: number }}
 */
export const resolveCompletePracticeAction = ({
	questionList = [],
	selectedMap = {}
} = {}) => {
	const firstPendingIndex = findFirstUnansweredIndex(questionList, selectedMap);
	if (firstPendingIndex < 0) {
		return { action: 'submit' };
	}
	const unansweredCount = questionList.length - countAnsweredQuestions(questionList, selectedMap);
	return {
		action: 'confirm-unanswered',
		unansweredCount,
		firstPendingIndex
	};
};

/**
 * 非末题点击「下一题」是否允许（允许跳过未答题）
 */
export const canProceedToNextQuestion = () => true;

/**
 * 本地交卷后错题本同步（submit 失败 fallback：答对掌握 + 答错/未答 wrongCount+1）
 */
export const applyLocalNotebookAfterSubmit = async (
	reviewList = [],
	practiceSource = {},
	{ markNotebookMastered, syncWrongNotebook } = {}
) => {
	const wrongList = reviewList.filter((item) => !item.isCorrect);
	if (typeof markNotebookMastered === 'function') {
		await markNotebookMastered(reviewList.filter((item) => item.isCorrect));
	}
	if (typeof syncWrongNotebook === 'function') {
		await syncWrongNotebook(wrongList, practiceSource);
	}
	return wrongList;
};
