/**
 * 练习答题暂存：POST/GET /app/practice/draft + 本地 Storage 兜底（杀进程续答）
 * scopeKey 规则与 server PracticeDraftSupport 对齐
 */
import { get, postJson, SILENT_REQUEST_OPTIONS } from '@/common/api';
import { getAuthState } from '@/common/app-data';
import { PRACTICE_DRAFT_KEY } from '@/common/storage-keys';

export { PRACTICE_DRAFT_KEY } from '@/common/storage-keys';

const VALID_SOURCE_MODES = ['all', 'wrong', 'single'];

/** 与 server PracticeDraftSupport.resolveScopeKey 一致 */
export const buildDraftScopeKey = (source = {}) => {
	const chapterId = String(source.chapterId || '').trim();
	const paperId = String(source.paperId || '').trim();
	const questionId = String(source.questionId || '').trim();
	const rawMode = String(source.mode || source.sourceMode || 'all').trim();
	const sourceMode = VALID_SOURCE_MODES.includes(rawMode) ? rawMode : 'all';

	if (paperId) {
		return `paper:${paperId}:${sourceMode}`;
	}
	if (sourceMode === 'single' && questionId) {
		return `single:${questionId}`;
	}
	if (chapterId) {
		return `chapter:${chapterId}:${sourceMode}`;
	}
	return '';
};

/** GET /app/practice/draft 查询参数 */
export const buildDraftQuery = (source = {}) => {
	const query = {};
	const chapterId = String(source.chapterId || '').trim();
	const paperId = String(source.paperId || '').trim();
	const questionId = String(source.questionId || '').trim();
	const rawMode = String(source.mode || source.sourceMode || '').trim();

	if (chapterId) {
		query.chapterId = chapterId;
	}
	if (paperId) {
		query.paperId = paperId;
	}
	if (questionId) {
		query.questionId = questionId;
	}
	if (rawMode && VALID_SOURCE_MODES.includes(rawMode)) {
		query.sourceMode = rawMode;
	}
	return query;
};

const normalizeDraft = (data = {}) => ({
	hasDraft: Boolean(data.hasDraft),
	chapterId: String(data.chapterId || '').trim(),
	paperId: String(data.paperId || '').trim(),
	questionId: String(data.questionId || '').trim(),
	sourceMode: VALID_SOURCE_MODES.includes(String(data.sourceMode || '').trim())
		? String(data.sourceMode).trim()
		: 'all',
	currentIndex: Math.max(0, Number(data.currentIndex) || 0),
	durationSeconds: Math.max(0, Number(data.durationSeconds) || 0),
	answers: Array.isArray(data.answers)
		? data.answers.map((item) => ({
			questionCode: String(item?.questionCode || '').trim(),
			userAnswer: String(item?.userAnswer || '')
		})).filter((item) => item.questionCode)
		: [],
	savedAt: String(data.savedAt || '')
});

const getLocalDraftMap = () => {
	const saved = uni.getStorageSync(PRACTICE_DRAFT_KEY);
	if (saved && typeof saved === 'object') {
		return saved;
	}
	return {};
};

const setLocalDraftMap = (map) => {
	uni.setStorageSync(PRACTICE_DRAFT_KEY, map);
	return map;
};

/** 读取本地暂存（按 scopeKey） */
export const getLocalPracticeDraft = (source = {}) => {
	const scopeKey = buildDraftScopeKey(source);
	if (!scopeKey) {
		return normalizeDraft({ hasDraft: false });
	}
	const map = getLocalDraftMap();
	const saved = map[scopeKey];
	if (!saved || typeof saved !== 'object') {
		return normalizeDraft({ hasDraft: false });
	}
	return normalizeDraft({
		...saved,
		hasDraft: true
	});
};

/** 写入本地暂存 */
export const saveLocalPracticeDraft = (source = {}, draft = {}) => {
	const scopeKey = buildDraftScopeKey(source);
	if (!scopeKey) {
		return normalizeDraft({ hasDraft: false });
	}
	const normalized = normalizeDraft({
		...draft,
		hasDraft: true,
		chapterId: draft.chapterId || source.chapterId || '',
		paperId: draft.paperId || source.paperId || '',
		questionId: draft.questionId || source.questionId || '',
		sourceMode: draft.sourceMode || source.mode || source.sourceMode || 'all',
		savedAt: draft.savedAt || new Date().toISOString()
	});
	const map = getLocalDraftMap();
	map[scopeKey] = normalized;
	setLocalDraftMap(map);
	return normalized;
};

/** 清除本地暂存 */
export const clearLocalPracticeDraft = (source = {}) => {
	const scopeKey = buildDraftScopeKey(source);
	if (!scopeKey) {
		return;
	}
	const map = getLocalDraftMap();
	if (map[scopeKey]) {
		delete map[scopeKey];
		setLocalDraftMap(map);
	}
};

/**
 * 由答题页状态构建暂存 payload
 * @param {object} source - practiceSource
 * @param {number} currentIndex
 * @param {Record<number, string>} selectedMap
 * @param {object[]} questionList
 * @param {number} durationSeconds
 */
export const buildDraftPayload = (
	source = {},
	currentIndex = 0,
	selectedMap = {},
	questionList = [],
	durationSeconds = 0
) => {
	const answers = (Array.isArray(questionList) ? questionList : [])
		.map((item, index) => ({
			questionCode: String(item.questionCode || item.questionId || '').trim(),
			userAnswer: String(selectedMap[index] || '').trim()
		}))
		.filter((item) => item.questionCode && item.userAnswer);

	return normalizeDraft({
		hasDraft: answers.length > 0 || currentIndex > 0,
		chapterId: source.chapterId || '',
		paperId: source.paperId || '',
		questionId: source.questionId || '',
		sourceMode: source.mode || 'all',
		currentIndex: Math.max(0, Number(currentIndex) || 0),
		durationSeconds: Math.max(0, Number(durationSeconds) || 0),
		answers
	});
};

/**
 * 将暂存答案映射回答题页 selectedMap / currentIndex
 * @returns {{ selectedMap: Record<number, string>, currentIndex: number, durationSeconds: number }|null}
 */
export const applyDraftToAnswerState = (draft, questionList = []) => {
	const normalized = normalizeDraft(draft);
	if (!normalized.hasDraft || !questionList.length) {
		return null;
	}

	const answerByCode = new Map(
		normalized.answers.map((item) => [item.questionCode, item.userAnswer])
	);
	const selectedMap = {};
	questionList.forEach((item, index) => {
		const code = String(item.questionCode || item.questionId || '').trim();
		if (code && answerByCode.has(code)) {
			selectedMap[index] = answerByCode.get(code);
		}
	});

	const maxIndex = Math.max(0, questionList.length - 1);
	const currentIndex = Math.min(Math.max(0, normalized.currentIndex), maxIndex);

	if (!Object.keys(selectedMap).length && currentIndex === 0) {
		return null;
	}

	return {
		selectedMap,
		currentIndex,
		durationSeconds: normalized.durationSeconds
	};
};

/**
 * 拉取暂存：已登录优先服务端，否则/失败回退本地
 * @param {object} source
 * @returns {Promise<object>}
 */
export const fetchPracticeDraft = async (source = {}) => {
	const localDraft = getLocalPracticeDraft(source);
	const auth = getAuthState();

	if (!auth.loggedIn) {
		return localDraft;
	}

	const query = buildDraftQuery(source);
	if (!query.chapterId && !query.paperId && !query.questionId) {
		return localDraft;
	}

	try {
		const res = await get('/app/practice/draft', query);
		if (res && res.code === 0 && res.data) {
			const serverDraft = normalizeDraft(res.data);
			if (serverDraft.hasDraft) {
				saveLocalPracticeDraft(source, serverDraft);
				return serverDraft;
			}
		}
	} catch (e) {
		// 网络异常回退本地
	}

	return localDraft;
};

/**
 * 保存暂存：始终写本地；已登录同步 POST /app/practice/draft
 * @param {object} source
 * @param {object} draft
 * @param {{ silent?: boolean }} [options]
 */
export const savePracticeDraft = async (source = {}, draft = {}, options = {}) => {
	const normalized = normalizeDraft({
		...draft,
		hasDraft: draft.hasDraft !== false
	});
	saveLocalPracticeDraft(source, normalized);

	const scopeKey = buildDraftScopeKey(source);
	if (!scopeKey) {
		return { ok: false, message: '缺少练习范围，未保存暂存' };
	}

	if (!normalized.hasDraft) {
		clearLocalPracticeDraft(source);
	}

	const auth = getAuthState();
	if (!auth.loggedIn || !normalized.hasDraft) {
		return { ok: true, localOnly: true };
	}

	const requestBody = {
		...buildDraftQuery(source),
		sourceMode: normalized.sourceMode || source.mode || 'all',
		currentIndex: normalized.currentIndex,
		durationSeconds: normalized.durationSeconds,
		answers: normalized.answers
	};

	try {
		const res = await postJson('/app/practice/draft', requestBody, SILENT_REQUEST_OPTIONS);
		if (res && res.code === 0) {
			return { ok: true, result: res.data };
		}
		const message = (res && res.msg) || '暂存同步失败，已保存本地';
		if (!options.silent) {
			uni.showToast({ title: message, icon: 'none' });
		}
		return { ok: false, message, localOnly: true };
	} catch (e) {
		const message = e?.msg || '网络异常，暂存已保存本地';
		if (!options.silent) {
			uni.showToast({ title: message, icon: 'none' });
		}
		return { ok: false, message, localOnly: true };
	}
};

/**
 * 清除本地暂存（交卷后服务端 submit 会自动清除 DB 暂存）
 * @param {object} source
 */
export const clearPracticeDraft = (source = {}) => {
	clearLocalPracticeDraft(source);
	return { ok: true };
};

/**
 * 直接退出时恢复进入前的暂存快照（本地 + 已登录时同步服务端）
 * @param {object} source
 * @param {object|null} snapshot
 */
export const revertPracticeDraft = async (source = {}, snapshot = null) => {
	const normalized = normalizeDraft(snapshot || { hasDraft: false });
	if (!normalized.hasDraft) {
		clearLocalPracticeDraft(source);
		return { ok: true };
	}
	return savePracticeDraft(source, normalized, { silent: true });
};
