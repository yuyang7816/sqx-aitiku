/**
 * 知识点练习列表：GET /app/question/knowledge/list
 * 失败返回空列表
 */
import { get, PAGE_LOAD_REQUEST_OPTIONS } from '@/common/api';
import { getUserProfile } from '@/common/app-data';
import { resolveProfileFilterQuery, resolveSubjectApiCode } from '@/common/profile-api-params';

/** 将服务端 AppQuestionKnowledgePointVO 规范为列表项 */
export const normalizeKnowledgePoint = (item = {}) => ({
	code: String(item.code || '').trim(),
	title: String(item.title || '').trim(),
	meta: String(item.meta || '').trim() || (item.questionCount != null ? `共 ${item.questionCount} 题` : ''),
	subject: String(item.subject || '').trim(),
	grade: String(item.grade || '').trim(),
	gradeShort: String(item.gradeShort || '').trim(),
	questionCount: Number(item.questionCount) || 0
});

/**
 * 拉取知识点列表
 * @param {{ grade?: string, subject?: string }} [query]
 * @returns {Promise<{ list: object[], fromApi: boolean }>}
 */
export const fetchKnowledgePointList = async (query = {}) => {
	const profile = getUserProfile();
	const subjectName = String(query.subject || '').trim();
	const subjectCode = resolveSubjectApiCode(subjectName);
	const params = {};
	// 指定学科 Chip 时仅按学科筛选，避免 profile 年级与种子年级不一致导致空列表
	if (subjectCode) {
		params.subject = subjectCode;
	} else {
		const filterQuery = resolveProfileFilterQuery(profile);
		if (filterQuery.grade) {
			params.grade = filterQuery.grade;
		}
		if (filterQuery.subject) {
			params.subject = filterQuery.subject;
		}
	}

	try {
		const res = await get('/app/question/knowledge/list', params, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && Array.isArray(res.data)) {
			return {
				list: res.data.map(normalizeKnowledgePoint).filter((item) => item.code && item.title),
				fromApi: true
			};
		}
	} catch (e) {
		// 网络异常时返回空列表
	}

	return {
		list: [],
		fromApi: false
	};
};
