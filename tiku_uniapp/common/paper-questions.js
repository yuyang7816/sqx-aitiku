/**
 * 专项/真题题集组卷：GET /app/question/paper/{id}/questions，失败返回空列表
 */
import { get } from '@/common/api';
import { getAuthState } from '@/common/app-data';

/** 答题页占位选项（接口未返回 options 时使用） */
const PLACEHOLDER_OPTIONS = [
	{ label: 'A', value: '选项 A' },
	{ label: 'B', value: '选项 B' },
	{ label: 'C', value: '选项 C' },
	{ label: 'D', value: '选项 D' }
];

/** 将服务端 AppQuestionPaperQuestionVO 规范为答题页题目结构 */
export const normalizePaperQuestion = (item = {}) => ({
	type: item.type || '单选题',
	difficulty: item.difficulty || '中等',
	title: item.title || '',
	rightAnswer: item.rightAnswer || '',
	analysis: item.analysis || '',
	aiSummary: item.aiSummary || item.analysis || '',
	options: Array.isArray(item.options) && item.options.length ? item.options : PLACEHOLDER_OPTIONS,
	questionId: item.questionId,
	questionCode: item.questionCode
});

/**
 * 按题集业务编码拉取组卷题目；已登录优先 API，失败或空列表时返回空数组
 * @param {string} paperId - 题集业务编码（paper_code）
 * @returns {Promise<object[]>}
 */
export const fetchPaperPracticeQuestions = async (paperId) => {
	const id = String(paperId || '').trim();
	if (!id) {
		return [];
	}

	const auth = getAuthState();
	if (!auth.loggedIn) {
		return [];
	}

	try {
		const res = await get(`/app/question/paper/${encodeURIComponent(id)}/questions`);
		if (res && res.code === 0 && Array.isArray(res.data) && res.data.length) {
			return res.data.map(normalizePaperQuestion).filter((item) => item.title);
		}
	} catch (e) {
		// 网络异常
	}

	return [];
};
