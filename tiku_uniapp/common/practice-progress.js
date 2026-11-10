/**
 * 模块二十二 P1#10 · 章节练习进度与交卷
 * GET /app/practice/progress、POST /app/practice/submit
 * 交卷由 package/question/answer.vue saveSession 调用 submitChapterPractice
 * fetchChapterTree 经 enrichChaptersWithProgress 批量 enrichment 章节卡片 meta
 * 失败时保留章节树原有 meta，未登录或单章失败时展示「已学 0% | 共N题」兜底文案
 */
import { get, postJson } from '@/common/api';
import { getAuthState } from '@/common/app-data';

/** 章节卡片副标题：已学百分比 + 总题数（与 server AppPracticeProgressVO 对齐） */
export const formatChapterProgressMeta = (percent = 0, totalCount = 0) => {
	const safePercent = Math.max(0, Math.min(100, Math.round(Number(percent) || 0)));
	const safeTotal = Math.max(0, Number(totalCount) || 0);
	return `已学 ${safePercent}% | 共${safeTotal}题`;
};

/** 将服务端进度 VO 规范为前端结构 */
export const normalizeChapterProgress = (data = {}, fallbackChapterId = '') => ({
	chapterId: data.chapterId || fallbackChapterId,
	percent: Number(data.percent) || 0,
	doneCount: Number(data.doneCount) || 0,
	totalCount: Number(data.totalCount) || 0
});

/**
 * 拉取单章练习进度（需登录）
 * @param {string} chapterId - 章业务编码（chapter_code）
 * @returns {Promise<object|null>}
 */
export const fetchChapterProgress = async (chapterId) => {
	const id = String(chapterId || '').trim();
	if (!id) {
		return null;
	}

	const auth = getAuthState();
	if (!auth.loggedIn) {
		return null;
	}

	try {
		const res = await get('/app/practice/progress', { chapterId: id });
		if (res && res.code === 0 && res.data) {
			return normalizeChapterProgress(res.data, id);
		}
	} catch (e) {
		// 网络异常时静默回退，保留章节树 meta
	}

	return null;
};

/**
 * 将进度写入章节卡片（percent/totalCount 优先取 API，缺省用 questionCount）
 * @param {object} chapter
 * @param {object|null} progress
 */
export const applyProgressToChapter = (chapter = {}, progress = null) => {
	if (!chapter || !chapter.id) {
		return chapter;
	}

	const totalFromChapter = Number(chapter.questionCount) || 0;
	if (!progress) {
		if (totalFromChapter > 0 && !String(chapter.meta || '').includes('已学')) {
			return {
				...chapter,
				meta: formatChapterProgressMeta(0, totalFromChapter)
			};
		}
		return chapter;
	}

	const totalCount = progress.totalCount || totalFromChapter;
	return {
		...chapter,
		meta: formatChapterProgressMeta(progress.percent, totalCount),
		progressPercent: progress.percent,
		progressDoneCount: progress.doneCount,
		progressTotalCount: totalCount
	};
};

/**
 * 批量拉取章节进度并更新 meta（并行请求，单章失败不影响其余）
 * @param {object[]} chapters
 * @returns {Promise<object[]>}
 */
export const enrichChaptersWithProgress = async (chapters = []) => {
	const list = Array.isArray(chapters) ? [...chapters] : [];
	if (!list.length) {
		return list;
	}

	const auth = getAuthState();
	if (!auth.loggedIn) {
		return list;
	}

	const results = await Promise.allSettled(
		list.map(async (chapter) => {
			const progress = await fetchChapterProgress(chapter.id);
			return applyProgressToChapter(chapter, progress);
		})
	);

	return results.map((result, index) => (
		result.status === 'fulfilled' ? result.value : list[index]
	));
};

/**
 * 提交章节练习答卷（需登录；失败时本地 session 仍可用，交卷场景会 Toast 提示）
 * @param {string} chapterId - 章业务编码（chapter_code）
 * @param {{ questionCode: string, userAnswer: string }[]} answers
 * @param {{ durationSeconds?: number, sourceMode?: string, subject?: string, silent?: boolean }} [options]
 * @returns {Promise<{ ok: boolean, progress?: object, result?: object, message?: string }>}
 */
export const submitChapterPractice = async (chapterId, answers = [], options = {}) => {
	const id = String(chapterId || '').trim();
	const payload = (Array.isArray(answers) ? answers : [])
		.map((item) => ({
			questionCode: String(item?.questionCode || '').trim(),
			userAnswer: String(item?.userAnswer || '').trim()
		}))
		.filter((item) => item.questionCode && item.userAnswer);

	if (!id || !payload.length) {
		return { ok: false, message: '缺少章节或答题明细，未同步服务端' };
	}

	const auth = getAuthState();
	if (!auth.loggedIn) {
		return { ok: false, message: '未登录，练习记录仅保存在本机' };
	}

	const requestBody = {
		chapterId: id,
		answers: payload,
		durationSeconds: Math.max(0, Number(options.durationSeconds) || 0),
		sourceMode: String(options.sourceMode || 'all').trim() || 'all'
	};
	const subject = String(options.subject || '').trim();
	if (subject) {
		requestBody.subject = subject;
	}

	try {
		const res = await postJson('/app/practice/submit', requestBody, {
			loading: !options.silent,
			toast: false
		});
		if (res && res.code === 0 && res.data) {
			const progress = res.data.progress
				? normalizeChapterProgress(res.data.progress, id)
				: null;
			return {
				ok: true,
				progress,
				result: res.data
			};
		}
		const message = (res && res.msg) || '练习记录同步失败，已保存本地结果';
		if (!options.silent) {
			uni.showToast({ title: message, icon: 'none' });
		}
		return { ok: false, message };
	} catch (e) {
		const message = e?.msg || '网络异常，练习记录已保存本地';
		if (!options.silent) {
			uni.showToast({ title: message, icon: 'none' });
		}
		return { ok: false, message };
	}
};
