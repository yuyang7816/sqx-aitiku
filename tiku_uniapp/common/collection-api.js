/**
 * 模块二十二 P1#11 · 收藏 API
 * GET/POST/DELETE /app/collection（需登录；autoSync 关闭或未登录时 uniapp 本地 Storage fallback）
 */
import { del, get, postJson } from '@/common/api';

/** 将服务端 AppCollectionItemVO 规范为本地收藏结构 */
export const normalizeCollectionItem = (item = {}) => ({
	id: item.id || item.questionCode || '',
	questionId: item.questionId != null && item.questionId !== '' ? String(item.questionId) : '',
	questionCode: item.questionCode || item.id || '',
	subject: item.subject || '通用题目',
	date: item.date || '',
	title: item.title || '',
	myAnswer: item.myAnswer || '',
	rightAnswer: item.rightAnswer || '',
	analysis: item.analysis || '暂无解析',
	aiSummary: item.aiSummary || '暂无 AI 讲解',
	options: Array.isArray(item.options) ? item.options : [],
	tags: Array.isArray(item.tags) ? item.tags : ['#已收藏']
});

const assertApiSuccess = (res, fallbackMsg) => {
	if (res && res.code === 0) {
		return res.data;
	}
	const err = new Error((res && res.msg) || fallbackMsg);
	err.msg = (res && res.msg) || fallbackMsg;
	throw err;
};

/**
 * 拉取收藏列表并规范为 collection.vue 结构
 * @returns {Promise<object[]>}
 */
export const fetchCollectionList = async () => {
	const res = await get('/app/collection');
	const data = assertApiSuccess(res, '收藏列表加载失败');
	const list = Array.isArray(data) ? data : [];
	return list.map(normalizeCollectionItem);
};

/**
 * 新增收藏（POST /app/collection）
 * @param {string} questionId - 题目 id 或 question_code
 * @param {string} subject - 学科展示文案
 */
export const addCollection = async (questionId, subject) => {
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
	const res = await postJson('/app/collection', {
		questionId: id,
		subject: subjectText
	});
	const data = assertApiSuccess(res, '收藏失败');
	return {
		collected: Boolean(data && data.collected),
		item: data && data.item ? normalizeCollectionItem(data.item) : null
	};
};

/**
 * 取消收藏（DELETE /app/collection?questionId=）
 * @param {string} questionId
 */
export const removeCollection = async (questionId) => {
	const id = String(questionId || '').trim();
	if (!id) {
		const err = new Error('题目标识不能为空');
		err.msg = '题目标识不能为空';
		throw err;
	}
	const res = await del('/app/collection', { questionId: id });
	assertApiSuccess(res, '取消收藏失败');
};
