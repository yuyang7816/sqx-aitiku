/**
 * 搜题记录：GET/POST/DELETE /app/search/records（需登录；失败时 uniapp 本地 Storage fallback）
 */
import { del, get, postJson } from '@/common/api';

const assertApiSuccess = (res, fallbackMsg) => {
	if (res && res.code === 0) {
		return res.data;
	}
	const err = new Error((res && res.msg) || fallbackMsg);
	err.msg = (res && res.msg) || fallbackMsg;
	throw err;
};

/** 将服务端列表项规范为本地 Storage 结构 */
export const normalizeSearchRecordItem = (item = {}) => ({
	title: String(item.title || '').trim(),
	time: String(item.time || '刚刚').trim() || '刚刚',
	source: String(item.source || '').trim(),
	imageUrl: String(item.imageUrl || '').trim()
});

/**
 * 拉取云端搜题记录（GET /app/search/records）
 */
export const fetchSearchRecordsRemote = async () => {
	const res = await get('/app/search/records');
	const data = assertApiSuccess(res, '获取搜题记录失败');
	return (Array.isArray(data) ? data : []).map(normalizeSearchRecordItem).filter((item) => item.title);
};

/**
 * 写入/更新云端搜题记录（POST /app/search/records）
 */
export const upsertSearchRecordRemote = async ({ title = '', source = '', imageUrl = '' } = {}) => {
	const res = await postJson('/app/search/records', {
		title: String(title || '').trim(),
		source: String(source || '').trim(),
		imageUrl: String(imageUrl || '').trim() || undefined
	});
	const data = assertApiSuccess(res, '写入搜题记录失败');
	return normalizeSearchRecordItem(data);
};

/**
 * 清空云端搜题记录（DELETE /app/search/records）
 */
export const clearSearchRecordsRemote = async () => {
	const res = await del('/app/search/records');
	return assertApiSuccess(res, '清空搜题记录失败');
};
