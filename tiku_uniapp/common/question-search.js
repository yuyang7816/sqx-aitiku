/**
 * 题库关键词搜索：GET /app/search/questions
 */
import { get } from '@/common/api';
import { getAuthState } from '@/common/app-data';

/** 规范化搜索结果项 */
export const normalizeSearchQuestionItem = (item = {}) => ({
	questionId: item.questionId,
	questionCode: String(item.questionCode || '').trim(),
	title: String(item.title || '').trim(),
	subject: String(item.subject || '').trim(),
	chapterTitle: String(item.chapterTitle || '').trim(),
	questionType: String(item.questionType || '单选题').trim(),
	difficulty: String(item.difficulty || '中等').trim()
});

/**
 * 关键词搜索题目
 * @param {{ keyword: string, page?: number, size?: number }} params
 */
export const searchQuestions = async ({ keyword = '', page = 1, size = 10 } = {}) => {
	const key = String(keyword || '').trim();
	if (!key) {
		return { list: [], total: 0, fromApi: false };
	}
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return { list: [], total: 0, fromApi: false };
	}
	try {
		const res = await get('/app/search/questions', { keyword: key, page, size });
		if (res && res.code === 0 && res.data) {
			const records = Array.isArray(res.data.records) ? res.data.records : [];
			return {
				list: records.map(normalizeSearchQuestionItem).filter((item) => item.title),
				total: Number(res.data.total) || records.length,
				fromApi: true
			};
		}
	} catch (e) {
		// 静默回退
	}
	return { list: [], total: 0, fromApi: false };
};
