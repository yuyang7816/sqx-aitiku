/**
 * 练习逐题解析：非模考场景作答后立即展示解析，模考仍交卷后统一回顾
 */
import { isQuestionAnswerCorrect } from './question-answer.js';

/** 是否启用「做一题看一题解析」 */
export const isInstantReviewEnabled = (practiceSource = {}) => !Boolean(practiceSource.examMode);

/** 由当前题目与用户答案构建解析页/面板所需结构 */
export const buildReviewItemFromQuestion = (question = {}, myAnswer = '') => {
	const answer = String(myAnswer || '').trim();
	return {
		title: String(question.title || '').trim(),
		options: Array.isArray(question.options) ? question.options : [],
		myAnswer: answer,
		rightAnswer: String(question.rightAnswer || '').trim(),
		analysis: String(question.analysis || '').trim(),
		aiSummary: String(question.aiSummary || '').trim(),
		tags: Array.isArray(question.tags) ? question.tags.filter(Boolean) : [],
		questionId: String(question.questionId || question.id || '').trim(),
		questionCode: String(question.questionCode || question.questionId || '').trim(),
		subject: String(question.subject || '').trim(),
		isCorrect: isQuestionAnswerCorrect(answer, question)
	};
};

/** 练习态底部主按钮文案 */
export const resolvePracticePrimaryActionLabel = ({
	instantReview = false,
	showReview = false,
	isLastQuestion = false
} = {}) => {
	if (!instantReview) {
		return isLastQuestion ? '完成练习' : '下一题';
	}
	if (showReview) {
		return isLastQuestion ? '完成练习' : '继续下一题';
	}
	return isLastQuestion ? '查看解析' : '下一题';
};
