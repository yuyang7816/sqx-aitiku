/**
 * 题目讲解视频：GET /app/question/video
 */
import { get } from '@/common/api';
import { buildApiUrl } from '@/common/api/config';
import { getAuthState } from '@/common/app-data';

const resolveMediaUrl = (url = '') => {
	const value = String(url || '').trim();
	if (!value) {
		return '';
	}
	return buildApiUrl(value);
};

/** 规范化服务端视频数据 */
export const normalizeQuestionVideo = (data = {}) => ({
	title: String(data.title || '题目视频讲解').trim(),
	questionCode: String(data.questionCode || '').trim(),
	videoUrl: resolveMediaUrl(data.videoUrl),
	posterUrl: resolveMediaUrl(data.posterUrl),
	duration: String(data.duration || '').trim(),
	teacher: String(data.teacher || '').trim(),
	summary: String(data.summary || '').trim(),
	fromServer: Boolean(data.fromServer)
});

/**
 * 拉取题目讲解视频
 * @param {{ questionCode?: string, questionId?: string|number }} params
 */
export const fetchQuestionVideo = async ({ questionCode = '', questionId = '' } = {}) => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return { video: normalizeQuestionVideo({}), fromApi: false };
	}
	const query = {};
	const code = String(questionCode || '').trim();
	const id = String(questionId || '').trim();
	if (code) {
		query.questionCode = code;
	}
	if (id) {
		query.questionId = id;
	}
	if (!Object.keys(query).length) {
		return { video: normalizeQuestionVideo({}), fromApi: false };
	}
	try {
		const res = await get('/app/question/video', query);
		if (res && res.code === 0 && res.data) {
			return { video: normalizeQuestionVideo(res.data), fromApi: true };
		}
	} catch (e) {
		// 静默
	}
	return { video: normalizeQuestionVideo({}), fromApi: false };
};
