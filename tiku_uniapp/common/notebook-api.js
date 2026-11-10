/**
 * 模块二十二 P1#11 · 错题本 API
 * GET/POST/PATCH/DELETE /app/notebook（须登录；列表以 GET 为准）
 */
import { del, get, patchJson, postJson } from '@/common/api';
import { resolveNotebookItemSubject } from '@/common/notebook-rules';

/** 将服务端错题本项规范为本地 notebook 结构 */
export const normalizeNotebookItem = (item = {}) => {
	const resolvedSubject = resolveNotebookItemSubject(item) || String(item.subject || '').trim();
	return {
	id: item.id || item.questionCode || `notebook-${item.questionId || Date.now()}`,
	questionId: item.questionId,
	questionCode: item.questionCode || item.id || '',
	subject: resolvedSubject || '章节练习',
	date: item.date || '',
	question: item.question || item.title || '',
	wrong: item.wrong || item.userAnswer || item.myAnswer || '',
	right: item.right || item.rightAnswer || '',
	analysis: item.analysis || '暂无解析',
	aiSummary: item.aiSummary || '暂无 AI 讲解',
	options: Array.isArray(item.options) ? item.options : [],
	tags: Array.isArray(item.tags) && item.tags.length ? item.tags : ['#错题回顾'],
	wrongCount: Number(item.wrongCount || 1),
	mastered: Boolean(item.mastered),
	lastPracticeAt: item.lastPracticeAt || item.date || ''
	};
};

const assertApiSuccess = (res, fallbackMsg) => {
	if (res && res.code === 0) {
		return res.data;
	}
	const err = new Error((res && res.msg) || fallbackMsg);
	err.msg = (res && res.msg) || fallbackMsg;
	throw err;
};

/**
 * 拉取错题本列表
 * @returns {Promise<object[]>}
 */
export const fetchNotebookList = async () => {
	const res = await get('/app/notebook');
	const data = assertApiSuccess(res, '错题本加载失败');
	const list = Array.isArray(data) ? data : (Array.isArray(data && data.list) ? data.list : []);
	return list.map(normalizeNotebookItem);
};

/**
 * 手动加入错题本（POST /app/notebook）
 * @param {string} questionId - 题目 id 或 question_code
 * @param {string} subject - 学科展示文案
 * @param {string} [userAnswer] - 用户答案（OCR 手动加入可为空）
 */
export const addNotebook = async (questionId, subject, userAnswer = '') => {
	const id = String(questionId || '').trim();
	const subjectText = String(subject || '').trim();
	if (!id) {
		const err = new Error('题目标识不能为空');
		err.msg = '题目标识不能为空';
		throw err;
	}
	if (!subjectText) {
		const err = new Error('学科不能为空');
		err.msg = '学科不能为空';
		throw err;
	}
	const payload = {
		questionId: id,
		subject: subjectText
	};
	const normalizedAnswer = String(userAnswer || '').trim();
	if (normalizedAnswer) {
		payload.userAnswer = normalizedAnswer;
	}
	const res = await postJson('/app/notebook', payload);
	const data = assertApiSuccess(res, '加入错题本失败');
	return {
		added: Boolean(data && data.added !== false),
		duplicate: Boolean(data && data.duplicate),
		item: data && data.item ? normalizeNotebookItem(data.item) : null
	};
};

/**
 * 移除单题错题（DELETE /app/notebook?questionId=）
 * @param {string} questionId - 题目 id 或 question_code
 */
export const removeNotebook = async (questionId) => {
	const id = String(questionId || '').trim();
	if (!id) {
		const err = new Error('题目标识不能为空');
		err.msg = '题目标识不能为空';
		throw err;
	}
	const res = await del('/app/notebook', { questionId: id });
	assertApiSuccess(res, '移除错题失败');
};

/** 清空当前用户全部错题（DELETE /app/notebook） */
export const clearNotebook = async () => {
	const res = await del('/app/notebook');
	assertApiSuccess(res, '清空错题本失败');
};

/**
 * 更新错题掌握态（PATCH /app/notebook）
 * @param {string} questionId - 题目 id 或 question_code
 * @param {boolean} mastered - true 已掌握，false 待复习
 */
export const updateNotebookMastered = async (questionId, mastered) => {
	const id = String(questionId || '').trim();
	if (!id) {
		const err = new Error('题目标识不能为空');
		err.msg = '题目标识不能为空';
		throw err;
	}
	const res = await patchJson('/app/notebook', {
		questionId: id,
		mastered: Boolean(mastered)
	});
	const data = assertApiSuccess(res, '更新掌握状态失败');
	return normalizeNotebookItem(data);
};
