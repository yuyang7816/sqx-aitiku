/**
 * 模块二十二 P1#10 · 章节树与进度同步
 * GET /app/question/chapter/tree → 扁平章列表 → enrichChaptersWithProgress 拉取各章进度
 * 刷题中心热门章节与章节练习列表页共用 fetchChapterTree；失败返回空列表
 */
import { get } from '@/common/api';
import { gradeToShort, getPracticeSubjectChips, isPracticeSubjectChip, PRACTICE_CHIP_ALL_GRADES, setPracticeSource } from '@/common/app-data';
import { QUESTION_ANSWER_PAGE } from '@/common/routes';
import { resolveProfileFilterQuery } from '@/common/profile-api-params';
import { enrichChaptersWithProgress } from '@/common/practice-progress';

/** 空章节列表 */
export const getFallbackPracticeChapters = () => [];

const normalizeVersion = (version = '') => String(version).replace(/（默认）/g, '').trim();

/** 将 API 章节点映射为刷题中心/章节列表卡片结构 */
export const normalizeChapterNode = (node = {}, context = {}) => {
	const grade = context.grade || '';
	const subject = context.subject || '';
	const gradeShort = node.gradeShort || gradeToShort(grade);
	const questionCount = Number(node.questionCount) || 0;
	return {
		id: node.code,
		title: node.title,
		meta: `共${questionCount}题`,
		subject,
		grade,
		gradeShort,
		questionCount,
		sections: Array.isArray(node.sections) ? node.sections : []
	};
};

/** 章节树转扁平章列表（供热门章节/章节练习列表） */
export const mapChapterTreeToList = (tree = [], context = {}) =>
	(Array.isArray(tree) ? tree : [])
		.filter((item) => item && item.code && item.title)
		.map((item) => normalizeChapterNode(item, context));

/**
 * 根据 profile 与当前 Chip 解析章节树 API 查询参数（grade/subject/version 均为 ASCII 编码）
 * @param {object} profile
 * @param {string} [activeChip]
 */
export const resolveChapterTreeQuery = (profile = {}, activeChip = '') => {
	const query = resolveProfileFilterQuery(profile, activeChip);
	return {
		grade: query.grade || 'junior_3',
		subject: query.subject || 'math',
		version: query.version || 'renjiao'
	};
};

/** 章节卡片展示上下文（仍用 profile 中文展示字段，供本地 Chip 过滤） */
export const resolveChapterTreeDisplayContext = (profile = {}, activeChip = '') => {
	const grade = profile.grade || '初中三年级';
	const version = normalizeVersion(profile.version) || '人教版';
	const subjects = getPracticeSubjectChips(profile);
	let subject = subjects[0] || '数学';
	if (activeChip && isPracticeSubjectChip(activeChip, profile)) {
		subject = activeChip;
	}
	return { grade, subject, version };
};

/**
	 * 拉取章节树并转为列表；成功返回 API 数据，失败或结构无效时返回空列表
 * @param {object} profile
 * @param {string} [activeChip]
 * @returns {Promise<{ list: object[], fromApi: boolean }>}
 */
export const fetchChapterTree = async (profile = {}, activeChip = '') => {
	const query = resolveChapterTreeQuery(profile, activeChip);
	const displayContext = resolveChapterTreeDisplayContext(profile, activeChip);
	try {
		const res = await get('/app/question/chapter/tree', query);
		if (res && res.code === 0 && Array.isArray(res.data)) {
			const list = mapChapterTreeToList(res.data, displayContext);
			const enriched = await enrichChaptersWithProgress(list);
			return {
				list: enriched,
				fromApi: true
			};
		}
	} catch (e) {
		// 网络异常时返回空列表
	}
	return {
		list: getFallbackPracticeChapters(),
		fromApi: false
	};
};

/** 章节详情导航栏标题：基于章标题去掉「第X章：」前缀 */
export const formatChapterPracticeNavbarTitle = (fullTitle = '') => {
	const title = String(fullTitle || '').trim();
	if (!title) {
		return '章节练习';
	}
	const shortTitle = title.replace(/^第.+章：/, '').trim() || title;
	return `章节练习：${shortTitle}`;
};

/**
 * 按刷题 Chip 学科解析实际章节业务编码（主 Tab 章节卡片 id 可能来自默认学科）
 * @param {string} chapterId - 当前章 chapter_code
 * @param {string} [activeChip] - 刷题 Chip（学科 Chip 时映射到对应学科章节）
 * @param {object} [profile]
 */
export const resolveChapterIdForPracticeChip = async (chapterId = '', activeChip = '', profile = {}) => {
	const id = String(chapterId || '').trim();
	if (!id || !activeChip || !isPracticeSubjectChip(activeChip, profile)) {
		return id;
	}
	const [defaultTree, subjectTree] = await Promise.all([
		fetchChapterTree(profile, PRACTICE_CHIP_ALL_GRADES),
		fetchChapterTree(profile, activeChip)
	]);
	const defaultList = defaultTree.list || [];
	const subjectList = subjectTree.list || [];
	const index = defaultList.findIndex((item) => item.id === id);
	if (index >= 0 && subjectList[index]?.id) {
		return subjectList[index].id;
	}
	const current = findChapterById(defaultList, id);
	if (current?.title) {
		const matched = subjectList.find((item) => item.title === current.title);
		if (matched?.id) {
			return matched.id;
		}
	}
	return subjectList[0]?.id || id;
};

/**
 * 直接进入章节刷题页（跳过章节题目列表中间页）
 * @param {{ chapterId?: string, title?: string, activeChip?: string, profile?: object, questionId?: string }} options
 */
export const startChapterPracticeAnswer = async ({
	chapterId = '',
	title = '',
	subject = '',
	activeChip = '',
	profile = {},
	questionId = ''
} = {}) => {
	const rawChapterId = String(chapterId || '').trim();
	if (!rawChapterId) {
		uni.showToast({ title: '章节信息不完整', icon: 'none' });
		return false;
	}
	const resolvedChapterId = await resolveChapterIdForPracticeChip(rawChapterId, activeChip, profile);
	const resolvedSubject = String(subject || '').trim()
		|| (isPracticeSubjectChip(activeChip, profile) ? activeChip : '');
	setPracticeSource({
		mode: 'all',
		title: String(title || '').trim() || '章节练习',
		chapterId: resolvedChapterId,
		...(resolvedSubject ? { subject: resolvedSubject } : {}),
		...(questionId ? { questionId: String(questionId) } : {})
	});
	uni.navigateTo({ url: QUESTION_ANSWER_PAGE });
	return true;
};

/** 在章节列表中按业务 id（chapter_code）查找章 */
export const findChapterById = (list = [], chapterId = '') => {
	const id = String(chapterId || '').trim();
	if (!id) {
		return null;
	}
	return (Array.isArray(list) ? list : []).find((item) => item.id === id) || null;
};
