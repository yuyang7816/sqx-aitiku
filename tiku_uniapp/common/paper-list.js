/**
 * 专项/真题题集列表：GET /app/question/paper/list?type=special|exam
 * 失败时返回空列表
 */
import { get } from '@/common/api';
import { getAuthState } from '@/common/app-data';

/** 题集类型：专项 */
export const PAPER_TYPE_SPECIAL = 'special';
/** 题集类型：真题 */
export const PAPER_TYPE_EXAM = 'exam';

/** 将服务端 AppQuestionPaperItemVO 规范为前端结构 */
export const normalizePaperItem = (item = {}) => ({
	paperId: String(item.paperId || '').trim(),
	title: item.title || '',
	type: item.type || '',
	description: item.description || '',
	grade: item.grade || '',
	gradeShort: item.gradeShort || '',
	subject: item.subject || '',
	questionCount: Number(item.questionCount) || 0
});

/**
 * 按 type 拉取上架题集列表；需登录，失败返回空数组
 * @param {'special'|'exam'} type
 * @returns {Promise<object[]>}
 */
export const fetchPaperList = async (type) => {
	const paperType = String(type || '').trim();
	if (!paperType) {
		return [];
	}

	const auth = getAuthState();
	if (!auth.loggedIn) {
		return [];
	}

	try {
		const res = await get('/app/question/paper/list', { type: paperType });
		if (res && res.code === 0 && Array.isArray(res.data)) {
			return res.data.map(normalizePaperItem).filter((item) => item.paperId);
		}
	} catch (e) {
		// 网络异常
	}

	return [];
};

/** 从题集列表取首条 paperId */
export const pickFirstPaperId = (list = []) => {
	const first = (Array.isArray(list) ? list : []).find((item) => item?.paperId);
	return first?.paperId || '';
};

/**
 * 刷题中心专项/真题卡片使用的 paperId（仅 API）
 * @returns {Promise<{ special: string, exam: string, fromApi: boolean }>}
 */
export const fetchPracticePaperIds = async () => {
	const empty = { special: '', exam: '', fromApi: false };

	const auth = getAuthState();
	if (!auth.loggedIn) {
		return empty;
	}

	try {
		const [specialList, examList] = await Promise.all([
			fetchPaperList(PAPER_TYPE_SPECIAL),
			fetchPaperList(PAPER_TYPE_EXAM)
		]);
		return {
			special: pickFirstPaperId(specialList),
			exam: pickFirstPaperId(examList),
			fromApi: Boolean(specialList.length || examList.length)
		};
	} catch (e) {
		return empty;
	}
};
