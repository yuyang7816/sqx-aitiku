import config from '@/common/config';
import {
	NOTEBOOK_REDO_TITLE,
	OCR_RESULT_PAGE,
	QUESTION_ANSWER_PAGE,
	REPORT_HISTORY_PAGE,
	REPORT_WRONG_TITLE,
	buildReportPageUrl,
	buildReportPosterUrl
} from '@/common/routes';
import {
	practiceCards,
	practiceChipChapterMode
} from '@/common/practice-config';
import { getDefaultQuestionBankPracticeList } from '@/common/practice-fallback';
import { clearToken, getToken } from '@/common/auth-token';
import { clearAllTabPageStates } from '@/common/tab-state-cache';
import { addCollection, fetchCollectionList, removeCollection } from '@/common/collection-api';
import { resolveSubjectDisplayName } from '@/common/profile-api-params';
import { addNotebook, clearNotebook, fetchNotebookList, removeNotebook, updateNotebookMastered } from '@/common/notebook-api';
import {
	clearSearchRecordsRemote,
	fetchSearchRecordsRemote,
	upsertSearchRecordRemote
} from '@/common/search-records-api';
import { buildChatPageUrl, buildReportChatEntry, buildWeakPointChatEntry } from '@/common/chat-entry';
import {
	AUTH_KEY,
	CHAPTER_ANSWER_KEY,
	COLLECTION_KEY,
	CUSTOMER_CONFIG_KEY,
	LEARNING_STORAGE_KEYS,
	LEGACY_USER_STORAGE_KEYS,
	MEMBER_KEY,
	NOTEBOOK_KEY,
	NOTICE_KEY,
	PRACTICE_SOURCE_KEY,
	PROFILE_KEY,
	QUESTION_REVIEW_KEY,
	SEARCH_RECORD_KEY,
	SETTINGS_KEY
} from '@/common/storage-keys';
import {
	readMemberStore,
	readNotebookStoreList,
	readUserStoreAuth,
	readUserStoreProfile,
	syncMemberStore,
	syncNotebookStoreList,
	syncUserStoreAuth,
	syncUserStoreProfile,
	syncUserStoreResetAuth
} from '@/common/store-sync';
import {
	VIP_LEVEL_LABEL,
	buildActivationQuotaReset,
	buildAiConsumePatch,
	buildAiQuotaExhaustedPatch,
	buildAiQuotaSyncPatch,
	buildOcrQuotaExhaustedPatch,
	buildOcrQuotaSyncPatch,
	buildQuotaCheckResult,
	calculateVipExpireAt,
	formatDateOnly,
	getTodayString as getMemberRulesTodayString,
	resolveMemberQuotaState
} from '@/common/member-rules';
import {
	NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT,
	NOTEBOOK_STATUS_HIGH_FREQ,
	NOTEBOOK_STATUS_MASTERED,
	NOTEBOOK_STATUS_PENDING,
	filterNotebookByHighFreq,
	filterNotebookBySubject,
	filterNotebookForWrongMode,
	formatNotebookItemSubject,
	getNotebookItemKey,
	getNotebookItemSubjectName,
	getNotebookItemStatusClass,
	getNotebookItemStatusText,
	isNotebookGenericSubject,
	isNotebookHighFreqItem,
	isNotebookMasteredItem,
	resolveNotebookItemSubject,
	resolveNotebookItemsForWrongPractice
} from '@/common/notebook-rules';

export {
	NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT,
	NOTEBOOK_STATUS_HIGH_FREQ,
	NOTEBOOK_STATUS_MASTERED,
	NOTEBOOK_STATUS_PENDING,
	filterNotebookByHighFreq,
	filterNotebookBySubject,
	filterNotebookForWrongMode,
	formatNotebookItemSubject,
	getNotebookItemKey,
	getNotebookItemSubjectName,
	getNotebookItemStatusClass,
	getNotebookItemStatusText,
	isNotebookHighFreqItem,
	isNotebookMasteredItem,
	resolveNotebookItemSubject,
	resolveNotebookItemsForWrongPractice
};

/** 搜题记录本地条数上限 */
export const SEARCH_RECORD_MAX = 20;
/** 搜题记录来源（拍/ 手动 / AI*/
export const SEARCH_RECORD_SOURCES = {
	PHOTO: '拍照搜题',
	MANUAL: '手动搜题',
	AI: 'AI答疑'
};

const defaultCollections = [];

const defaultNotices = [];

const defaultSettings = {
	pushEnabled: true,
	soundEnabled: true,
	autoSync: true
};

const defaultAuthState = {
	loggedIn: false,
	phone: '',
	loginType: ''
};

const defaultUserProfile = {
	ready: false,
	name: '',
	avatarText: '学',
	grade: '',
	gradeShort: '',
	version: '',
	subjects: []
};

const defaultMemberState = {
	isVip: false,
	level: '普通会员',
	expireAt: '',
	aiDailyLimit: 0,
	aiUsedToday: 0,
	aiQuotaDate: getMemberRulesTodayString(),
	ocrDailyLimit: 0,
	ocrUsedToday: 0,
	ocrQuotaDate: getMemberRulesTodayString(),
	currentPlanName: '未开通',
	orderCount: 0
};

/** 练习来源 mode 枚举（文9.2*/
export const PRACTICE_SOURCE_MODES = ['all', 'wrong', 'single'];

const defaultPracticeSource = {
	mode: 'all',
	title: '章节练习',
	/** 章节练习业务编码（chapter_code），开始刷题时写入 */
	chapterId: '',
	/** 章节所属学科（开始刷题时写入，错题入库 subject 推断用） */
	subject: '',
	/** 章节单题预览：题目业务主键（tb_question.id question_code*/
	questionId: '',
	/** 错题本单题重做：题干文本，与 notebook.question 匹配 */
	questionTitle: '',
	/** 错题本批量重做：当前 Chip 筛选名（与 getNotebookFilteredList 对齐*/
	notebookFilterChip: '',
	/** 错题本批量重删除：条目业务键（id 或题干），空数组表示 Chip 下全*/
	notebookQuestionKeys: [],
	knowledgePoint: '',
	/** 专项/真题题集业务编码（paper_code），刷题中心模式卡片写入 */
	paperId: '',
	/** 模考计时模式（真题模拟启用*/
	examMode: false,
	/** 模考时长（分钟），0 表示使用 EXAM_DEFAULT_DURATION_MINUTES */
	examDurationMinutes: 0
};

const CHAPTER_ANSWER_STATUS = {
	right: { type: 'right', status: '已做对' },
	wrong: { type: 'wrong', status: '已做错' }
};

/** 客服配置默认值（启动后由 GET /app/config/customer 覆盖*/
const defaultCustomerConfig = {
	enabled: true,
	type: 'wework',
	phone: '',
	weworkUrl: '',
	weworkAppId: '',
	imEnabled: false,
	imWelcome: '',
	wechatId: '',
	wechatQrUrl: ''
};

const clone = (value) => JSON.parse(JSON.stringify(value));

const getTodayString = () => {
	const now = new Date();
	return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
};

/** 距今 N 天前yyyy-MM-dd（Mock seed lastPracticeDaysAgo 生成稳定可验的最近练习日期） */
const getDateDaysAgo = (days = 0) => {
	const target = new Date();
	target.setDate(target.getDate() - Math.max(0, Number(days) || 0));
	return `${target.getFullYear()}-${String(target.getMonth() + 1).padStart(2, '0')}-${String(target.getDate()).padStart(2, '0')}`;
};

const parseDate = (value) => {
	if (!value) return null;
	const normalized = new Date(value.replace(/-/g, '/'));
	return Number.isNaN(normalized.getTime()) ? null : normalized;
};

const isWithinDays = (value, days) => {
	const target = parseDate(value);
	if (!target) return false;
	const diff = Date.now() - target.getTime();
	return diff >= 0 && diff <= days * 24 * 60 * 60 * 1000;
};

/**
 * 年级完整名称 → 简称（与 server UserServiceImpl.gradeToShort 规则一致）
 */
export const gradeToShort = (grade = '') => {
	const text = String(grade || '').trim();
	if (!text) return '';
	if (text.startsWith('小学')) {
		if (text.includes('一')) return '小一';
		if (text.includes('二')) return '小二';
		if (text.includes('三')) return '小三';
		if (text.includes('四')) return '小四';
		if (text.includes('五')) return '小五';
		if (text.includes('六')) return '小六';
		return text.replace('小学', '小');
	}
	if (text.startsWith('高中')) {
		if (text.includes('一')) return '高一';
		if (text.includes('二')) return '高二';
		if (text.includes('三')) return '高三';
		return text.replace('高中', '高');
	}
	if (text.includes('三')) return '初三';
	if (text.includes('二')) return '初二';
	if (text.includes('一')) return '初一';
	return text.replace('初中', '初');
};

/** profile grade 时同步写gradeShort */
const applyGradeShort = (profile = {}) => {
	if (profile.grade == null || profile.grade === '') {
		return profile;
	}
	return {
		...profile,
		gradeShort: gradeToShort(profile.grade)
	};
};

/** 昵称首字作为头像文字（与 GET /app/user nickname 映射规则一致） */
export const toAvatarText = (name = '') => {
	const text = String(name || '').trim();
	return text ? text.charAt(0) : '学';
};

const isRemoteUrl = (path = '') => /^https?:\/\//i.test(String(path).trim());

/** profile.avatar 转为可展示的图片地址（相对路径补APIHOST*/
export const resolveProfileAvatarUrl = (avatar = '') => {
	const normalized = String(avatar || '').trim();
	if (!normalized) {
		return '';
	}
	if (isRemoteUrl(normalized)) {
		return normalized;
	}
	if (normalized.startsWith('/')) {
		return `${config.APIHOST}${normalized}`;
	}
	return `${config.APIHOST}/${normalized}`;
};

const normalizeVersion = (version = '') => String(version).replace(/（默认）/g, '').trim();

/** 首页/我的页学科展示：最多取max 门，/ 连接 */
export const formatProfileSubjects = (subjects = [], max = 2) => {
	const list = Array.isArray(subjects) ? subjects.filter(Boolean) : [];
	if (!list.length) {
		return '';
	}
	return list.slice(0, max).join(' / ');
};

/** 年级简· 版本 · 学科（首页顶meta 行） */
export const formatProfileMetaLine = (profile = {}) => {
	const gradeShort = profile.gradeShort || gradeToShort(profile.grade);
	const version = profile.version ? normalizeVersion(profile.version) : '';
	const subjects = formatProfileSubjects(profile.subjects);
	return [gradeShort, version, subjects].filter(Boolean).join(' · ');
};

/** 我的页顶meta：年级简· 版本 · 用户 ID（GET /app/user userId*/
export const formatMyProfileMetaLine = (profile = {}) => {
	const gradeShort = profile.gradeShort || gradeToShort(profile.grade);
	const version = profile.version ? normalizeVersion(profile.version) : '';
	const userId = profile.userId != null && profile.userId !== '' ? String(profile.userId) : '';
	const parts = [gradeShort, version].filter(Boolean);
	if (userId) {
		parts.push(`ID: ${userId}`);
	}
	return parts.join(' · ');
};

/** name 有值时同步 avatarText，避免仅nickname 后头像字未更*/
const syncAvatarFromName = (profile = {}) => {
	const name = String(profile.name || '').trim();
	if (!name) {
		return profile;
	}
	return {
		...profile,
		avatarText: toAvatarText(name)
	};
};

/** 消息已读Storage 键（jumpId msg-{messageId}*/
export const resolveNoticeReadKey = (item = {}) => {
	if (item.messageId != null) {
		return `msg-${item.messageId}`;
	}
	return String(item.id || '').trim();
};

export const NOTICE_READ_STATE_EVENT = 'noticeReadStateChanged';

const notifyNoticeReadStateChanged = () => {
	if (typeof uni !== 'undefined' && typeof uni.$emit === 'function') {
		uni.$emit(NOTICE_READ_STATE_EVENT);
	}
};

const mergeNoticeReadState = (list, saved = []) => {
	const readMap = new Map();
	(Array.isArray(saved) ? saved : []).forEach((item) => {
		const read = Boolean(item.read);
		const readKey = resolveNoticeReadKey(item);
		if (readKey) {
			readMap.set(readKey, read);
		}
		const jumpId = String(item.id || '').trim();
		if (jumpId && jumpId !== readKey) {
			readMap.set(jumpId, read);
		}
	});
	return list.map((item) => {
		const readKey = resolveNoticeReadKey(item);
		const jumpId = String(item.id || '').trim();
		let read = Boolean(item.read);
		if (readKey && readMap.has(readKey)) {
			read = readMap.get(readKey);
		} else if (jumpId && readMap.has(jumpId)) {
			read = readMap.get(jumpId);
		}
		return {
			...item,
			read
		};
	});
};

/** 将本Storage 已读态合并进消息列表（API 列表mock 共用*/
export const applyStoredNoticeReadState = (list = []) => {
	const saved = uni.getStorageSync(NOTICE_KEY);
	return mergeNoticeReadState(list, saved);
};

/** 会员到期N 天推notice-member（与 server VipRenewConfigReader 默认 3 天对齐） */
export const VIP_RENEW_NOTICE_DAYS = 3;

/** 免费 AI 剩余次数 该阈值时生成 notice-vip-guide */
export const AI_NOTICE_REMAINING_THRESHOLD = 2;

/**
 * 会员剩余有效天数（与 server VipRenewNoticeServiceImpl.resolveRemainingDays 对齐 * @param {string} expireAt - yyyy-MM-dd
 * @param {string} [todayStr] - yyyy-MM-dd，便于单测注 */
export const getMemberRemainingDays = (expireAt = '', todayStr = getTodayString()) => {
	const expire = parseDate(String(expireAt || '').trim());
	const today = parseDate(todayStr);
	if (!expire || !today) {
		return 0;
	}
	const expireDay = new Date(expire.getFullYear(), expire.getMonth(), expire.getDate());
	const todayDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());
	const diffDays = Math.floor((expireDay.getTime() - todayDay.getTime()) / (24 * 60 * 60 * 1000));
	return Math.max(diffDays, 1);
};

/**
 * 按错题数、会员态、AI 剩余次数生成动态消息（纯函数，供单测与 getDynamicNotices 共用 * @param {object} options
 * @param {number} options.pendingCount - 待复习错题数
 * @param {object} options.member - getMemberState 快照
 * @param {number|string} [options.remainingAiTimes] - VIP 剩余 AI 次数；VIP 传「无限 */
export const buildDynamicNotices = ({
	pendingCount = 0,
	member = {},
	remainingAiTimes = 0,
	renewNoticeDays = VIP_RENEW_NOTICE_DAYS,
	aiNoticeThreshold = AI_NOTICE_REMAINING_THRESHOLD,
	todayStr = getTodayString()
} = {}) => {
	const notices = [];
	const isVip = Boolean(member.isVip);

	if (pendingCount > 0) {
		notices.push({
			id: 'notice-review',
			title: '待复习错题提醒',
			content: `你还${pendingCount} 道错题待复习，建议优先完成错题回顾强化。`,
			time: '刚刚',
			read: false
		});
	}

	if (isVip) {
		const remainingDays = getMemberRemainingDays(member.expireAt, todayStr);
		if (remainingDays > 0 && remainingDays <= renewNoticeDays) {
			notices.push({
				id: 'notice-member',
				title: '会员即将到期提醒',
				content: `你的会员还有 ${remainingDays} 天到期，续费后可继续使用无限AI 搜题与答疑。`,
				time: '今天',
				read: false
			});
		} else {
			notices.push({
				id: 'notice-vip-active',
				title: '会员权益生效中',
				content: `你的 ${member.currentPlanName || '会员'} 权益已生效，有效期至 ${member.expireAt}。`,
				time: '今天',
				read: false
			});
		}
	} else {
		const remaining = Number(remainingAiTimes);
		if (!Number.isNaN(remaining) && remaining <= aiNoticeThreshold) {
			notices.push({
				id: 'notice-vip-guide',
				title: 'AI 次数即将用尽',
				content: remaining <= 0
					? '今日免费 AI 次数已用尽，开通会员后可无限次答疑与搜题。'
					: `今日剩余 AI 免费次数 ${remaining} 次，开通会员后可无限次答疑与搜题。`,
				time: '今天',
				read: false
			});
		}
	}

	return notices;
};

const readCollectionStorageList = () => {
	const raw = uni.getStorageSync(COLLECTION_KEY);
	if (raw === undefined || raw === null || raw === '') {
		return null;
	}
	if (Array.isArray(raw)) {
		return raw;
	}
	if (typeof raw === 'string') {
		try {
			const parsed = JSON.parse(raw);
			return Array.isArray(parsed) ? parsed : null;
		} catch (e) {
			return null;
		}
	}
	return null;
};

export const getCollectionList = () => {
	const saved = readCollectionStorageList();
	if (Array.isArray(saved)) {
		return saved;
	}
	return clone(defaultCollections);
};

export const setCollectionList = (list) => {
	uni.setStorageSync(COLLECTION_KEY, list);
	return list;
};

export const buildCollectionItem = (item = {}) => ({
	id: item.id || item.questionCode || `collection-${item.title || Date.now()}`,
	questionId: item.questionId != null && item.questionId !== '' ? String(item.questionId) : '',
	questionCode: item.questionCode || item.id || '',
	subject: item.subject || '通用题目',
	date: item.date || '2026-06-12',
	title: item.title || '未命名题目',
	myAnswer: item.myAnswer || '',
	rightAnswer: item.rightAnswer || '',
	analysis: item.analysis || '暂无解析',
	aiSummary: item.aiSummary || '暂无 AI 讲解',
	options: Array.isArray(item.options) ? item.options : [],
	tags: Array.isArray(item.tags) ? item.tags : ['#已收藏']
});

/** 收藏答题页学科展示：用户首门学科 · 年级简称（数学 · 初三*/
export const resolveCollectionSubject = (source = {}, profile = getUserProfile()) => {
	const gradeShort = profile.gradeShort || gradeToShort(profile.grade);
	if (source.mode === 'wrong') {
		return gradeShort ? `错题重做 · ${gradeShort}` : '错题重做';
	}
	const explicitSubject = String(source.subject || '').trim();
	if (explicitSubject && !isNotebookGenericSubject(explicitSubject)) {
		const subjectName = resolveNotebookItemSubject({ subject: explicitSubject }) || explicitSubject;
		return gradeShort ? `${subjectName} · ${gradeShort}` : subjectName;
	}
	const subjects = getPracticeSubjectChips(profile);
	const subject = subjects[0] || '数学';
	const paperTitle = String(source.title || '').trim();
	if (source.paperId && paperTitle) {
		return gradeShort ? `${paperTitle} · ${gradeShort}` : paperTitle;
	}
	return gradeShort ? `${subject} · ${gradeShort}` : subject;
};

const getCollectionItemKeys = (item = {}) => [item.title, item.questionId, item.questionCode, item.id]
	.filter(Boolean)
	.map(String);

/** 云端同步用题目标识：优先 questionId/questionCode，排除本mock 前缀 */
export const resolveCollectionQuestionToken = (item = {}) => {
	const candidates = [item.questionId, item.questionCode, item.id]
		.map((value) => String(value || '').trim())
		.filter(Boolean);
	return candidates.find((value) => !value.startsWith('collection-')) || '';
};

/** 收藏条目业务键（列表 key / 取消防重复） */
export const getCollectionItemKey = (item = {}) => {
	const token = resolveCollectionQuestionToken(item);
	if (token) {
		return token;
	}
	return String(item.title || item.id || '').trim();
};

/**
 * 取消收藏：已登录autoSync DELETE /app/collection，再 filter 本地列表setCollectionList
 * @param {object} item - 收藏条目
 * @param {object[]} [sourceList] - 当前页列表（可选）
 */
export const cancelCollectionItem = async (item, sourceList = getCollectionList()) => {
	const normalized = buildCollectionItem(item);
	const questionId = resolveCollectionQuestionToken(normalized);
	const matchKeys = new Set(getCollectionItemKeys(normalized).filter(Boolean));

	if (!matchKeys.size) {
		const err = new Error('题目信息不完整');
		err.msg = '题目信息不完整';
		throw err;
	}

	if (canSyncCollectionToApi() && questionId) {
		try {
			await removeCollection(questionId);
		} catch (e) {
			const err = e instanceof Error ? e : new Error('取消收藏失败');
			err.msg = e.msg || e.message || '取消收藏失败';
			throw err;
		}
	}

	const list = Array.isArray(sourceList) ? sourceList : getCollectionList();
	const next = list.filter((entry) => {
		const entryKeys = getCollectionItemKeys(entry);
		return !entryKeys.some((key) => matchKeys.has(key));
	});
	setCollectionList(next);
	return { list: next };
};

/** 从服务端同步收藏列表到本Storage（autoSync 关闭或未登录时保留本地；API 失败 fallback*/
export const syncCollectionListFromApi = async () => {
	if (!canSyncCollectionToApi()) {
		return {
			list: getCollectionList(),
			fromApi: false
		};
	}
	try {
		const list = await fetchCollectionList();
		const normalized = Array.isArray(list) ? list.map((item) => buildCollectionItem(item)) : [];
		setCollectionList(normalized);
		return {
			list: normalized,
			fromApi: true
		};
	} catch (e) {
		return {
			list: getCollectionList(),
			fromApi: false,
			error: e
		};
	}
};

export const toggleCollectionItem = async (item) => {
	const normalized = buildCollectionItem(item);
	const questionId = String(normalized.questionId || normalized.questionCode || normalized.id || '').trim();

	if (canSyncCollectionToApi() && questionId && !questionId.startsWith('collection-')) {
		const currentlyCollected = hasCollectionItem(questionId);
		try {
			if (currentlyCollected) {
				const result = await cancelCollectionItem(normalized);
				return { collected: false, list: result.list };
			}
			const subject = String(normalized.subject || resolveCollectionSubject()).trim() || '通用题目';
			const apiResult = await addCollection(questionId, subject);
			const savedItem = apiResult.item || { ...normalized, subject, questionId };
			const list = getCollectionList().filter((entry) => !getCollectionItemKeys(entry).includes(questionId));
			list.unshift(savedItem);
			setCollectionList(list);
			return { collected: true, list, item: savedItem };
		} catch (e) {
			const err = e instanceof Error ? e : new Error('收藏操作失败');
			err.msg = e.msg || e.message || '收藏操作失败';
			throw err;
		}
	}

	const list = getCollectionList();
	const index = list.findIndex((entry) => entry.title === normalized.title);
	if (index >= 0) {
		list.splice(index, 1);
		setCollectionList(list);
		return { collected: false, list };
	}
	list.unshift(normalized);
	setCollectionList(list);
	return { collected: true, list };
};

export const hasCollectionItem = (key) => {
	const value = String(key || '').trim();
	if (!value) {
		return false;
	}
	return getCollectionList().some((item) => getCollectionItemKeys(item).includes(value));
};

export const getNoticeList = () => {
	const saved = uni.getStorageSync(NOTICE_KEY);
	if (Array.isArray(saved) && saved.length) {
		return mergeNoticeReadState(saved, saved);
	}
	return [];
};

export const setNoticeList = (list) => {
	uni.setStorageSync(NOTICE_KEY, list);
	return list;
};

export const markAllNoticesRead = (currentList) => {
	const base = Array.isArray(currentList) && currentList.length ? currentList : getNoticeList();
	const list = base.map((item) => ({
		...item,
		read: true
	}));
	setNoticeList(list);
	notifyNoticeReadStateChanged();
	return list;
};

export const markNoticeRead = (id, currentList) => {
	const key = String(id || '');
	const base = Array.isArray(currentList) && currentList.length ? currentList : getNoticeList();
	const list = base.map((item) => {
		const itemKey = resolveNoticeReadKey(item);
		return itemKey === key ? {
			...item,
			read: true
		} : item;
	});
	setNoticeList(list);
	notifyNoticeReadStateChanged();
	return list;
};

export const getSettings = () => {
	const saved = uni.getStorageSync(SETTINGS_KEY);
	if (saved && typeof saved === 'object') {
		return {
			...defaultSettings,
			...saved
		};
	}
	return {
		...defaultSettings
	};
};

export const saveSettings = (settings) => {
	const value = {
		...defaultSettings,
		...settings
	};
	uni.setStorageSync(SETTINGS_KEY, value);
	return value;
};

/** 是否开启「自动同步学习记录」（设置autoSync 开关） */
export const isAutoSyncEnabled = () => getSettings().autoSync !== false;

/** 是否开启「学习提醒」（设置pushEnabled 开关，持久SETTINGS_KEY*/
export const isPushEnabled = () => getSettings().pushEnabled !== false;

/** 是否开启「提示音」（设置soundEnabled 开关，持久SETTINGS_KEY*/
export const isSoundEnabled = () => getSettings().soundEnabled !== false;

/** 已登录（有 token）时错题本读写走云端 API */
export const canSyncNotebookToApi = () => Boolean(getToken());

/** 已登录且开启自动同步时，收藏读写优先走云端 API */
export const canSyncCollectionToApi = () => {
	const auth = getAuthState();
	return Boolean(auth.loggedIn && isAutoSyncEnabled());
};

/** 读取本地客服配置（首页「在线客服」等入口使用*/
export const getCustomerConfig = () => {
	const saved = uni.getStorageSync(CUSTOMER_CONFIG_KEY);
	if (saved && typeof saved === 'object') {
		return {
			...defaultCustomerConfig,
			...saved
		};
	}
	return {
		...defaultCustomerConfig
	};
};

/** 写入客服配置Storage */
export const saveCustomerConfig = (config) => {
	const value = {
		...defaultCustomerConfig,
		...config
	};
	uni.setStorageSync(CUSTOMER_CONFIG_KEY, value);
	return value;
};

export const getAuthState = () => {
	const fromStore = readUserStoreAuth();
	if (fromStore) {
		return fromStore;
	}
	const saved = uni.getStorageSync(AUTH_KEY);
	if (saved && typeof saved === 'object') {
		return {
			...defaultAuthState,
			...saved
		};
	}
	return {
		...defaultAuthState
	};
};

export const saveAuthState = (state) => {
	const value = {
		...defaultAuthState,
		...state
	};
	uni.setStorageSync(AUTH_KEY, value);
	syncUserStoreAuth(value);
	return value;
};

export const loginUser = ({ phone = '', loginType = 'sms' } = {}) => saveAuthState({
	loggedIn: true,
	phone,
	loginType
});

const clearLegacyUserStorage = () => {
	LEGACY_USER_STORAGE_KEYS.forEach((key) => {
		try {
			uni.removeStorageSync(key);
		} catch (e) {
			// ignore
		}
	});
};

/** 退出登录：清除登录态与本地错题本缓存（错题本以服务端为准，换账号须重新拉取） */
export const logoutUser = () => {
	clearToken();
	clearLegacyUserStorage();
	syncUserStoreResetAuth();
	setNotebookList([]);
	return saveAuthState(defaultAuthState);
};

export const getUserProfile = () => {
	const fromStore = readUserStoreProfile();
	if (fromStore) {
		return fromStore;
	}
	const saved = uni.getStorageSync(PROFILE_KEY);
	const merged = {
		...defaultUserProfile,
		...(saved && typeof saved === 'object' ? saved : {})
	};
	let profile = applyGradeShort(merged);
	profile = syncAvatarFromName(profile);
	if (profile.version != null) {
		profile.version = normalizeVersion(profile.version);
	}
	return profile;
};

export const saveUserProfile = (profile) => {
	const saved = uni.getStorageSync(PROFILE_KEY);
	const value = applyGradeShort({
		...defaultUserProfile,
		...(saved && typeof saved === 'object' ? saved : {}),
		...(profile && typeof profile === 'object' ? profile : {})
	});
	if (value.version != null) {
		value.version = normalizeVersion(value.version);
	}
	uni.setStorageSync(PROFILE_KEY, value);
	syncUserStoreProfile(value);
	return value;
};

/**
 * 计划页确认：写入年级/版本/学科，subjects 非空时标profile.ready=true
 * @param {{ grade?: string, version?: string, subjects?: string[] }} payload
 */
export const completeUserProfile = ({ grade, version, subjects }) => {
	const subjectList = Array.isArray(subjects) ? subjects.filter(Boolean) : [];
	return saveUserProfile({
		ready: subjectList.length > 0,
		grade,
		version: normalizeVersion(version),
		subjects: subjectList.length ? subjectList : defaultUserProfile.subjects
	});
};

/**
 * 模块二十二 P1#13 · 判断会员是否已过期（expireAt yyyy-MM-dd，当日仍有效，次日 0 点起视为过期）
 * @param {string} expireAt
 */
const isMemberExpired = (expireAt = '') => {
	const expire = parseDate(String(expireAt || '').trim());
	if (!expire) {
		return false;
	}
	const today = parseDate(getTodayString());
	return today.getTime() > expire.getTime();
};

/** isVip 且 expireAt 已过，降级为普通会员；跨日重置 ai/ocr 已用次数（文档 23.3-1） */
const normalizeMemberState = (state = {}) => {
	const merged = resolveMemberQuotaState({
		...defaultMemberState,
		...state
	});
	if (!merged.isVip || !isMemberExpired(merged.expireAt)) {
		return merged;
	}
	return resolveMemberQuotaState({
		...merged,
		isVip: false,
		level: '普通会员'
	});
};

export const getMemberState = () => {
	const fromStore = readMemberStore();
	const saved = uni.getStorageSync(MEMBER_KEY);
	const raw = fromStore || (saved && typeof saved === 'object'
		? {
			...defaultMemberState,
			...saved
		}
		: {
			...defaultMemberState
		});
	const normalized = normalizeMemberState(raw);
	if (normalized.isVip !== raw.isVip || normalized.level !== raw.level) {
		uni.setStorageSync(MEMBER_KEY, normalized);
	}
	syncMemberStore(normalized);
	return normalized;
};

/** 模块二十二 P1#13 · App onShow 等入口主动触发 expireAt 校验并写 Storage + Pinia */
export const refreshMemberExpireState = () => getMemberState();

export const saveMemberState = (state) => {
	const value = normalizeMemberState(state);
	uni.setStorageSync(MEMBER_KEY, value);
	syncMemberStore(value);
	return value;
};

export const activateMemberPlan = ({ name = '月卡', days = 31, expireAt: serverExpireAt } = {}) => {
	const current = getMemberState();
	const today = getMemberRulesTodayString();
	const reset = buildActivationQuotaReset(today);
	const expireAt = serverExpireAt
		? String(serverExpireAt).trim().slice(0, 10)
		: formatDateOnly(calculateVipExpireAt(current.expireAt, days));
	return saveMemberState({
		...current,
		isVip: true,
		level: VIP_LEVEL_LABEL,
		expireAt,
		...reset,
		currentPlanName: name,
		orderCount: Number(current.orderCount || 0) + 1
	});
};

export const getRemainingAiTimes = () => buildQuotaCheckResult(getMemberState(), 'ai').remaining;

/** AI 对话发送前校验剩余次数（VIP 不限；余量为 0 ok:false，实际扣减在发送成功后 */
export const checkAiChatQuota = () => buildQuotaCheckResult(getMemberState(), 'ai');

/** 答题 AI 讲解：扣减一次免费 AI 次数（VIP 不扣减）；余量为 0 时返回 ok:false */
export const consumeAiTimes = () => {
	const member = getMemberState();
	const quota = checkAiChatQuota();
	if (quota.notReady) {
		return { ok: true, remaining: null, notReady: true };
	}
	if (!quota.ok) {
		return quota;
	}
	if (member.isVip) {
		return { ok: true, remaining: '无限' };
	}
	const patch = buildAiConsumePatch(member, getMemberRulesTodayString());
	saveMemberState({
		...member,
		aiUsedToday: patch.aiUsedToday,
		aiQuotaDate: patch.aiQuotaDate
	});
	return {
		ok: true,
		remaining: patch.remaining
	};
};

/** 模块二十四 24.3 · AI 次数用尽弹窗引导会员中心 */
export const showAiQuotaExhaustedModal = () => {
	const limit = Number(getMemberState().aiDailyLimit) || 0;
	const limitText = limit > 0 ? `免费用户每日 ${limit} 次 AI 讲解` : '免费用户每日 AI 讲解次数有限';
	uni.showModal({
		title: '今日 AI 次数已用尽',
		content: `${limitText}，开通会员后可无限次答疑与搜题。`,
		confirmText: '开通会员',
		cancelText: '知道了',
		success: (res) => {
			if (res.confirm) {
				uni.navigateTo({ url: '/package/member/center' });
			}
		}
	});
};

export const getRemainingOcrTimes = () => buildQuotaCheckResult(getMemberState(), 'ocr').remaining;

/** OCR 搜题前校验剩余次数（VIP 不限；余量为 0 ok:false，实际扣减以服务端为准） */
export const checkOcrSearchQuota = () => buildQuotaCheckResult(getMemberState(), 'ocr');

/** 同步 POST /app/ai/chat 返回 quota 快照至本地会员态 */
export const syncAiQuotaFromApi = (quota = {}) => {
	if (!quota || typeof quota !== 'object') {
		return getMemberState();
	}
	const member = getMemberState();
	const patch = buildAiQuotaSyncPatch(quota, member, getMemberRulesTodayString());
	return saveMemberState({ ...member, ...patch });
};

/** 同步 POST /app/ocr/search 返回 quota 快照至本地会员态 */
export const syncOcrQuotaFromApi = (quota = {}) => {
	if (!quota || typeof quota !== 'object') {
		return getMemberState();
	}
	const member = getMemberState();
	const patch = buildOcrQuotaSyncPatch(quota, member, getMemberRulesTodayString());
	return saveMemberState({ ...member, ...patch });
};

/** OCR 次数用尽：弹窗引导开通会*/
export const showOcrQuotaExhaustedModal = () => {
	const limit = Number(getMemberState().ocrDailyLimit) || 0;
	const limitText = limit > 0 ? `免费用户每日 ${limit} 次拍照搜题` : '免费用户每日拍照搜题次数有限';
	uni.showModal({
		title: '今日搜题次数已用尽',
		content: `${limitText}，开通会员后可无限次答疑与搜题。`,
		confirmText: '开通会员',
		cancelText: '知道了',
		success: (res) => {
			if (res.confirm) {
				uni.navigateTo({ url: '/package/member/center' });
			}
		}
	});
};

const readNotebookStorageList = () => {
	const raw = uni.getStorageSync(NOTEBOOK_KEY);
	if (raw === undefined || raw === null || raw === '') {
		return null;
	}
	if (Array.isArray(raw)) {
		return raw;
	}
	if (typeof raw === 'string') {
		try {
			const parsed = JSON.parse(raw);
			return Array.isArray(parsed) ? parsed : null;
		} catch (e) {
			return null;
		}
	}
	return null;
};

export const getNotebookList = () => {
	if (!canSyncNotebookToApi()) {
		return [];
	}
	const fromStore = readNotebookStoreList();
	if (fromStore) {
		return fromStore;
	}
	const saved = readNotebookStorageList();
	if (Array.isArray(saved)) {
		return saved;
	}
	return [];
};
export const setNotebookList = (list) => {
	uni.setStorageSync(NOTEBOOK_KEY, list);
	syncNotebookStoreList(list);
	return list;
};

export const buildNotebookItem = (item = {}) => {
	const resolvedSubject = resolveNotebookItemSubject(item) || String(item.subject || '').trim();
	return {
	id: item.id || `notebook-${item.title || item.question || Date.now()}`,
	questionId: item.questionId || item.questionCode || '',
	questionCode: item.questionCode || item.questionId || '',
	subject: resolvedSubject || '章节练习',
	date: item.date || '2026-06-12',
	question: item.question || item.title || '未命名题目',
	wrong: item.wrong || item.myAnswer || '',
	right: item.right || item.rightAnswer || '',
	analysis: item.analysis || '暂无解析',
	aiSummary: item.aiSummary || '暂无 AI 讲解',
	options: Array.isArray(item.options) ? item.options : [],
	tags: Array.isArray(item.tags) && item.tags.length ? item.tags : ['#错题回顾'],
	wrongCount: Number(item.wrongCount || 1),
	mastered: Boolean(item.mastered),
	lastPracticeAt: item.lastPracticeAt || item.date || getTodayString()
	};
};

/**
 * 错题本条解析reviewItem（写QUESTION_REVIEW_KEY * 字段answer.vue saveSession wrongList / analysis.vue normalizeReviewItem 对齐
 */
export const buildReviewItemFromNotebook = (item = {}) => {
	const questionTitle = String(item.question || item.title || '').trim();
	let analysis = String(item.analysis || '').trim();
	let aiSummary = String(item.aiSummary || '').trim();
	let options = Array.isArray(item.options) ? item.options : [];
	let tags = Array.isArray(item.tags) ? item.tags.filter(Boolean) : [];

	return {
		title: questionTitle || '未命名题',
		subject: String(item.subject || '').trim(),
		questionId: item.questionId || item.questionCode || '',
		questionCode: item.questionCode || item.questionId || '',
		myAnswer: String(item.wrong || item.myAnswer || '').trim(),
		rightAnswer: String(item.right || item.rightAnswer || '').trim(),
		analysis: analysis || '暂无详细解析',
		aiSummary: aiSummary || analysis || '暂无 AI 讲解',
		options,
		tags: tags.length ? tags : ['#错题回顾']
	};
};

/**
 * 收藏条目 解析reviewItem（写QUESTION_REVIEW_KEY * 字段GET /app/collection、analysis.vue normalizeReviewItem 对齐
 */
export const buildReviewItemFromCollection = (item = {}) => {
	const questionTitle = String(item.title || item.question || '').trim();
	let analysis = String(item.analysis || '').trim();
	let aiSummary = String(item.aiSummary || '').trim();
	let options = Array.isArray(item.options) ? item.options : [];
	let tags = Array.isArray(item.tags) ? item.tags.filter(Boolean) : [];
	let myAnswer = String(item.myAnswer || item.wrong || '').trim();
	let rightAnswer = String(item.rightAnswer || item.right || '').trim();

	return {
		title: questionTitle || '未命名题',
		subject: String(item.subject || '').trim(),
		questionId: item.questionId != null && item.questionId !== '' ? String(item.questionId) : '',
		questionCode: item.questionCode || item.id || '',
		myAnswer,
		rightAnswer,
		analysis: analysis || '暂无详细解析',
		aiSummary: aiSummary || analysis || '暂无 AI 讲解',
		options,
		tags: tags.length ? tags : ['#已收藏']
	};
};

/** 写入解析reviewItem Storage */
export const setQuestionReviewItem = (item = {}) => {
	uni.setStorageSync(QUESTION_REVIEW_KEY, item);
	return item;
};

/** 错题本条目匹配键：优questionId/questionCode，fallback 题干 */
const getNotebookMatchKeys = (item = {}) => [
	item.questionId,
	item.questionCode,
	item.question,
	item.title
].filter(Boolean).map(String);

/** 云端同步用题目标识：优先 question_code，其次 questionId，排除本地 mock 前缀 */
export const resolveNotebookQuestionToken = (item = {}) => {
	const questionCode = String(item.questionCode || '').trim();
	const questionId = String(item.questionId || '').trim();
	const id = String(item.id || '').trim();
	const isMockPrefix = (value) => value.startsWith('notebook-') || value.startsWith('collection-');
	if (questionCode && !isMockPrefix(questionCode)) {
		return questionCode;
	}
	if (questionId && !isMockPrefix(questionId)) {
		return questionId;
	}
	if (id && !isMockPrefix(id)) {
		return id;
	}
	return '';
};

/** 判断错题本是否已含该题（questionId / questionCode / 题干*/
export const hasNotebookItem = (keyOrItem) => {
	const keys = typeof keyOrItem === 'object'
		? getNotebookMatchKeys(keyOrItem)
		: [String(keyOrItem || '').trim()].filter(Boolean);
	if (!keys.length) {
		return false;
	}
	return getNotebookList().some((entry) => (
		getNotebookMatchKeys(entry).some((key) => keys.includes(key))
	));
};

/**
 * OCR 识别结果页手动入payload（非答题场景：无 wrong/myAnswer * @param {object} question - OCR 匹配题目
 * @param {{ searchSource?: string, subject?: string }} [options]
 */
export const buildOcrNotebookPayload = (question = {}, options = {}) => {
	const questionId = String(
		question.questionId || question.questionCode || question.id || ''
	).trim();
	const title = String(question.title || question.question || '').trim();
	const answer = String(question.answer || question.rightAnswer || '').trim();
	const analysis = String(question.analysis || '').trim();
	const tips = Array.isArray(question.tips)
		? question.tips.map((item) => String(item || '').trim()).filter(Boolean)
		: [];
	const searchSource = String(options.searchSource || SEARCH_RECORD_SOURCES.PHOTO).trim()
		|| SEARCH_RECORD_SOURCES.PHOTO;
	const displayAnswer = answer || '暂无标准答案';
	const displayAnalysis = analysis || '暂无详细解析';

	return {
		...question,
		questionId,
		questionCode: question.questionCode || questionId,
		subject: String(options.subject || resolveCollectionSubject()).trim() || '章节练习',
		question: title,
		wrong: '',
		myAnswer: '',
		right: displayAnswer === '暂无标准答案' ? '' : answer,
		rightAnswer: displayAnswer === '暂无标准答案' ? '' : answer,
		analysis: displayAnalysis === '暂无详细解析' ? '' : analysis,
		aiSummary: tips.join(' '),
		options: Array.isArray(question.options) ? question.options : [],
		tags: [`#${searchSource}`, '#OCR搜题', '#待复习'],
		wrongCount: 1,
		mastered: false
	};
};

/** 从服务端拉取当前登录用户错题本并写入本地缓存（未登录返回空列表） */
export const syncNotebookListFromApi = async () => {
	if (!canSyncNotebookToApi()) {
		setNotebookList([]);
		return {
			list: [],
			fromApi: false
		};
	}
	try {
		const list = await fetchNotebookList();
		const normalized = Array.isArray(list) ? list.map((item) => buildNotebookItem(item)) : [];
		setNotebookList(normalized);
		return {
			list: normalized,
			fromApi: true
		};
	} catch (e) {
		setNotebookList([]);
		return {
			list: [],
			fromApi: false,
			error: e
		};
	}
};

/**
 * 去重加入错题本：已登录须 POST /app/notebook 后以服务端列表为准
 * @returns {Promise<{ added: boolean, duplicate: boolean, list: object[], item?: object }>}
 */
export const addNotebookItem = async (item = {}) => {
	if (!canSyncNotebookToApi()) {
		const err = new Error('请先登录后再加入错题本');
		err.msg = '请先登录后再加入错题本';
		throw err;
	}
	const today = getTodayString();
	const normalized = buildNotebookItem({
		...item,
		question: item.question || item.title,
		date: item.date || today,
		lastPracticeAt: item.lastPracticeAt || today
	});
	const questionId = resolveNotebookQuestionToken(normalized);
	const subject = String(normalized.subject || resolveCollectionSubject()).trim() || '章节练习';
	if (!questionId) {
		const err = new Error('题目标识无效，无法加入错题本');
		err.msg = '题目标识无效，无法加入错题本';
		throw err;
	}
	const apiResult = await addNotebook(questionId, subject, normalized.wrong);
	if (apiResult.duplicate) {
		const synced = await syncNotebookListFromApi();
		const existing = synced.list.find((entry) => (
			getNotebookMatchKeys(entry).some((key) => getNotebookMatchKeys(normalized).includes(key))
		));
		return {
			added: false,
			duplicate: true,
			list: synced.list,
			item: existing || null,
			synced: true
		};
	}
	const synced = await syncNotebookListFromApi();
	const savedItem = synced.list.find((entry) => (
		getNotebookMatchKeys(entry).some((key) => getNotebookMatchKeys(normalized).includes(key))
	)) || (apiResult.item ? buildNotebookItem(apiResult.item) : null);
	return {
		added: true,
		duplicate: false,
		list: synced.list,
		item: savedItem,
		synced: true
	};
};

/**
 * 错题入库 subject（文12.3 * - mode=wrong 错题重做
 * - mode=single title=错题重做（错题本重做）→ 错题重做
 * - 其余 章节练习
 */
export const resolveSyncWrongNotebookSubject = (practiceSource = {}) => {
	const mode = String(practiceSource.mode || '').trim();
	const title = String(practiceSource.title || '').trim();
	if (mode === 'wrong') {
		return NOTEBOOK_REDO_TITLE;
	}
	if (mode === 'single' && title === NOTEBOOK_REDO_TITLE) {
		return NOTEBOOK_REDO_TITLE;
	}
	return resolveCollectionSubject(practiceSource);
};

/**
 * 错题入库（文档 12.3）：已登录 POST /app/notebook 后以 GET /app/notebook 刷新列表
 */
export const syncWrongNotebook = async (wrongList = [], practiceSource = {}) => {
	if (!Array.isArray(wrongList) || !wrongList.length) {
		return getNotebookList();
	}
	if (!canSyncNotebookToApi()) {
		return [];
	}
	const subject = typeof practiceSource === 'string'
		? practiceSource
		: resolveSyncWrongNotebookSubject(practiceSource);

	await Promise.allSettled(wrongList.map(async (item) => {
		const questionId = resolveNotebookQuestionToken(item);
		if (!questionId) {
			return false;
		}
		const itemSubject = String(item.subject || '').trim() || subject;
		try {
			await addNotebook(
				questionId,
				itemSubject,
				String(item.myAnswer || '').trim()
			);
			return true;
		} catch (e) {
			return false;
		}
	}));

	const synced = await syncNotebookListFromApi();
	return synced.list;
};

export const markNotebookMastered = async (questions = []) => {
	if (!Array.isArray(questions) || !questions.length) {
		return getNotebookList();
	}
	if (!canSyncNotebookToApi()) {
		return [];
	}
	await Promise.allSettled(questions.map(async (item) => {
		const questionId = resolveNotebookQuestionToken(item);
		if (!questionId) {
			return;
		}
		await updateNotebookMastered(questionId, true);
	}));
	const synced = await syncNotebookListFromApi();
	return synced.list;
};

/** 切换错题掌握态：已登录 PATCH /app/notebook 后以服务端列表为准 */
export const toggleNotebookMastered = async (questionTitle) => {
	const title = String(questionTitle || '').trim();
	if (!title) {
		return getNotebookList();
	}
	if (!canSyncNotebookToApi()) {
		return [];
	}
	const targetItem = getNotebookList().find((item) => String(item.question || '').trim() === title);
	if (!targetItem) {
		return getNotebookList();
	}
	const questionId = resolveNotebookQuestionToken(targetItem);
	if (!questionId) {
		return getNotebookList();
	}
	await updateNotebookMastered(questionId, !targetItem.mastered);
	const synced = await syncNotebookListFromApi();
	return synced.list;
};

/**
 * 规范练习来源字段（文9.2：mode=all|wrong|single * @param {object} [source]
 */
const normalizePracticeSource = (source = {}) => {
	const rawMode = String(source.mode || defaultPracticeSource.mode).trim();
	const mode = PRACTICE_SOURCE_MODES.includes(rawMode) ? rawMode : defaultPracticeSource.mode;
	return {
		...defaultPracticeSource,
		...source,
		mode,
		title: String(source.title ?? defaultPracticeSource.title).trim(),
		chapterId: String(source.chapterId || '').trim(),
		subject: String(source.subject || '').trim(),
		questionId: String(source.questionId || '').trim(),
		questionTitle: String(source.questionTitle || '').trim(),
		notebookFilterChip: String(source.notebookFilterChip || '').trim(),
		notebookQuestionKeys: Array.isArray(source.notebookQuestionKeys)
			? source.notebookQuestionKeys.map((key) => String(key || '').trim()).filter(Boolean)
			: [],
		knowledgePoint: String(source.knowledgePoint || '').trim(),
		paperId: String(source.paperId || '').trim(),
		examMode: Boolean(source.examMode),
		examDurationMinutes: Math.max(0, Number(source.examDurationMinutes) || 0)
	};
};

export const getPracticeSource = () => {
	const saved = uni.getStorageSync(PRACTICE_SOURCE_KEY);
	if (saved && typeof saved === 'object') {
		return normalizePracticeSource(saved);
	}
	return normalizePracticeSource(defaultPracticeSource);
};

/** 写入练习来源并规mode 与业务字段（文档 9.2*/
export const setPracticeSource = (source) => {
	const value = normalizePracticeSource(source);
	uni.setStorageSync(PRACTICE_SOURCE_KEY, value);
	return value;
};

/** 读取章节题目本地做题状态映射（key questionId questionCode 字符串） */
export const getChapterAnswerMap = () => {
	const saved = uni.getStorageSync(CHAPTER_ANSWER_KEY);
	if (saved && typeof saved === 'object') {
		return saved;
	}
	return {};
};

/**
 * 单题作答完成后写入本地状态，章节详情 onShow 刷新时合并展 * @param {{ questionId?: string|number, isCorrect?: boolean }} payload
 */
export const syncChapterQuestionAnswer = ({ questionId, isCorrect } = {}) => {
	const id = String(questionId || '').trim();
	if (!id) {
		return getChapterAnswerMap();
	}
	const statusKey = isCorrect ? 'right' : 'wrong';
	const map = getChapterAnswerMap();
	map[id] = {
		...CHAPTER_ANSWER_STATUS[statusKey],
		updatedAt: getTodayString()
	};
	uni.setStorageSync(CHAPTER_ANSWER_KEY, map);
	return map;
};

/** 将本地做题状态合并进章节题目列表项（API/mock 共用*/
export const applyChapterAnswerOverlay = (list = []) => {
	if (!Array.isArray(list) || !list.length) {
		return list;
	}
	const map = getChapterAnswerMap();
	return list.map((item) => {
		const keys = [item.questionId, item.questionCode].filter(Boolean).map(String);
		for (let i = 0; i < keys.length; i += 1) {
			const overlay = map[keys[i]];
			if (overlay) {
				return {
					...item,
					type: overlay.type,
					status: overlay.status
				};
			}
		}
		return item;
	});
};

export const clearPracticeSource = () => {
	uni.removeStorageSync(PRACTICE_SOURCE_KEY);
	return {
		...defaultPracticeSource
	};
};

export const getSearchRecords = () => {
	const saved = uni.getStorageSync(SEARCH_RECORD_KEY);
	if (Array.isArray(saved)) {
		return saved;
	}
	return [];
};

export const setSearchRecords = (list) => {
	uni.setStorageSync(SEARCH_RECORD_KEY, list);
	return list;
};

/** 清空本地搜题记录；已登录时同DELETE /app/search/records（服务端未上线时忽略失败*/
export const clearSearchRecords = async () => {
	const cleared = setSearchRecords([]);
	const auth = getAuthState();
	if (auth.loggedIn) {
		try {
			await clearSearchRecordsRemote();
		} catch (e) {
			// 本地已清空，云端同步失败不阻UI
		}
	}
	return cleared;
};

const normalizeSearchRecordSource = (source = '') => {
	const text = String(source || '').trim();
	const allowed = Object.values(SEARCH_RECORD_SOURCES);
	return allowed.includes(text) ? text : SEARCH_RECORD_SOURCES.PHOTO;
};

/** 写入搜题记录：title 题干预览、time 展示时间、source 来源、imageUrl 原图地址；同 title 去重并置顶，最SEARCH_RECORD_MAX */
export const appendSearchRecord = ({
	title = '',
	source = SEARCH_RECORD_SOURCES.PHOTO,
	imageUrl = ''
} = {}) => {
	const normalizedTitle = String(title || '').trim();
	if (!normalizedTitle) {
		return getSearchRecords();
	}
	const time = '刚刚';
	const normalizedSource = normalizeSearchRecordSource(source);
	const normalizedImageUrl = String(imageUrl || '').trim();
	const records = getSearchRecords().filter((item) => item.title !== normalizedTitle);
	const record = {
		title: normalizedTitle,
		time,
		source: normalizedSource
	};
	if (normalizedImageUrl) {
		record.imageUrl = normalizedImageUrl;
	}
	records.unshift(record);
	const saved = setSearchRecords(records.slice(0, SEARCH_RECORD_MAX));
	const auth = getAuthState();
	if (auth.loggedIn) {
		upsertSearchRecordRemote({
			title: normalizedTitle,
			source: normalizedSource,
			imageUrl: normalizedImageUrl
		}).catch(() => {
			// 本地已写入，云端同步失败不阻UI
		});
	}
	return saved;
};

/** 已登录时GET /app/search/records 同步至本Storage；失败保留本地数*/
export const syncSearchRecordsFromApi = async () => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		return getSearchRecords();
	}
	try {
		const remote = await fetchSearchRecordsRemote();
		if (Array.isArray(remote) && remote.length) {
			return setSearchRecords(remote.slice(0, SEARCH_RECORD_MAX));
		}
		return setSearchRecords([]);
	} catch (e) {
		return getSearchRecords();
	}
};

/** 错题本条目转为答题页题目结构 */
const notebookToQuestion = (item) => ({
	type: '错题重做',
	difficulty: '复习',
	title: item.question,
	rightAnswer: item.right,
	analysis: item.analysis,
	aiSummary: item.aiSummary,
	options: item.options
});

/** single 模式：按 questionTitle 在错题本中定位单题 */
const findSingleQuestionByTitle = (questionTitle = '') => {
	const title = String(questionTitle || '').trim();
	if (!title) {
		return null;
	}
	const notebookItem = getNotebookList().find((item) => item.question === title);
	if (notebookItem && Array.isArray(notebookItem.options) && notebookItem.options.length) {
		return notebookToQuestion(notebookItem);
	}
	return null;
};

/**
 * practiceSource.mode 取题目列表（同步兜底，答题页优先 fetchPracticeQuestionsBySource API）
 * - all：无 paperId/chapterId 时返回空（须走 API）
 * - wrong：未掌握错题；无则 fallback 默认题库（文档 24.2）
 * - single：questionTitle 匹配错题本，questionId 须走 API
 */
export const getPracticeQuestionList = (source = getPracticeSource()) => {
	const normalized = normalizePracticeSource(source);

	if (normalized.mode === 'wrong') {
		const profile = getUserProfile();
		const notebookItems = resolveNotebookItemsForWrongPractice(getNotebookList(), {
			filterChip: String(normalized.notebookFilterChip || '').trim(),
			batchKeys: Array.isArray(normalized.notebookQuestionKeys)
				? normalized.notebookQuestionKeys
				: [],
			applyChipFilter: (chip, list) => getNotebookFilteredList(chip, list, profile)
		});

		const wrongQuestions = notebookItems
			.map(notebookToQuestion)
			.filter((item) => Array.isArray(item.options) && item.options.length);
		return wrongQuestions.length ? wrongQuestions : getDefaultQuestionBankPracticeList();
	}

	if (normalized.mode === 'single') {
		// 错题本重做：优先 questionTitle 匹配本地错题本，fallback questionId（章节单题预览）
		if (normalized.questionTitle) {
			const byTitle = findSingleQuestionByTitle(normalized.questionTitle);
			if (byTitle) {
				return [byTitle];
			}
		}
		if (normalized.questionId) {
			return [];
		}
		return [];
	}

	return [];
};

/** 启动路由：未登录 登录页（文档 4.3*/
export const LAUNCH_ROUTE_LOGIN = '/package/auth/login';
/** 启动路由：已登录但资料未就绪 学习计划页（文档 4.3*/
export const LAUNCH_ROUTE_SETUP = '/package/setup/plan';
/** 启动路由：已登录且资料就首页（文4.3*/
export const LAUNCH_ROUTE_HOME = '/pages/index/index';

/**
 * 退出登录并 reLaunch 至登录页（我的页 / 设置/ 401 拦截共用 * @param {{ toastTitle?: string, redirectDelay?: number, onComplete?: () => void }} [options]
 */
export const performLogoutAndRedirect = ({
	toastTitle = '已退出登录',
	redirectDelay = 300,
	onComplete
} = {}) => {
	logoutUser();
	clearAllTabPageStates();
	if (toastTitle) {
		uni.showToast({
			title: toastTitle,
			icon: 'none'
		});
	}
	setTimeout(() => {
		uni.reLaunch({
			url: LAUNCH_ROUTE_LOGIN,
			complete: () => {
				if (typeof onComplete === 'function') {
					onComplete();
				}
			}
		});
	}, redirectDelay);
};

/** 需路由守卫校验的入口页路径（不含闪屏，与文4.3 entryRoutes 一致） */
export const ENTRY_ROUTES = [
	LAUNCH_ROUTE_HOME.replace(/^\//, ''),
	LAUNCH_ROUTE_LOGIN.replace(/^\//, ''),
	LAUNCH_ROUTE_SETUP.replace(/^\//, '')
];

/** 学习计划route（与 getCurrentPages[].route 格式一致，无前/*/
export const SETUP_PLAN_ROUTE = LAUNCH_ROUTE_SETUP.replace(/^\//, '');

/** 模块二十二 P1#14 · 设置页进入计划页编辑模式 query */
export const SETUP_PLAN_EDIT_MODE = 'edit';

/** 模块二十二 P1#14 · 用户信息页「修改学习计划」跳转路径 */
export const SETUP_PLAN_EDIT_PAGE = `${LAUNCH_ROUTE_SETUP}?mode=${SETUP_PLAN_EDIT_MODE}`;

/**
 * 模块二十二 P1#14 · 已就绪用户从设置进入计划页编辑时，入口守卫不 reLaunch 至首页
 * @param {string} currentRoute
 * @param {{ mode?: string }} pageOptions
 * @param {{ ready?: boolean }} userProfile
 */
export const shouldSkipLaunchRedirectForSetupPlanEdit = (
	currentRoute,
	pageOptions = {},
	userProfile = {}
) =>
	userProfile.ready === true
	&& currentRoute === SETUP_PLAN_ROUTE
	&& String(pageOptions.mode || '').trim() === SETUP_PLAN_EDIT_MODE;

/**
 * 已登录但 profile 未就绪时，是否应强制拦回计划页（禁止跳过 setup * @param {string} currentRoute - 当前route
 * @param {{ loggedIn?: boolean }} authState
 * @param {{ ready?: boolean }} userProfile
 */
export const shouldRedirectToSetupPlan = (currentRoute, authState = {}, userProfile = {}) => {
	if (authState.loggedIn !== true || userProfile.ready === true) {
		return false;
	}
	return currentRoute !== SETUP_PLAN_ROUTE;
};

/**
 * 根据鉴权态解析启动目标路由（纯函数，便于组合态校验）
 * @param {{ loggedIn?: boolean }} authState - 本地 Storage `aiQuestionAuthState`
 * @param {{ ready?: boolean }} userProfile - 本地 Storage `aiQuestionUserProfile`
 * @returns {string} 启动目标页路 */
export const resolveLaunchRoute = (authState = {}, userProfile = {}) => {
	if (authState.loggedIn !== true) {
		return LAUNCH_ROUTE_LOGIN;
	}
	if (userProfile.ready !== true) {
		return LAUNCH_ROUTE_SETUP;
	}
	return LAUNCH_ROUTE_HOME;
};

/**
 * 读取本地 Storage 鉴权态并返回启动目标路由（文4.3 * - `aiQuestionAuthState.loggedIn !== true` 登录 * - 已登录且 `aiQuestionUserProfile.ready !== true` 计划 * - 否则 首页
 */
export const getLaunchRoute = () => resolveLaunchRoute(getAuthState(), getUserProfile());

/**
 * 登录成功后合并服务端 profile 态；无返回且明确换号时重ready，避免沿用上一账号
 * @param {{ profilePatch?: { ready?: boolean }, identityKey?: string, prevIdentityKey?: string }} options
 */
export const applyPostLoginProfile = ({
	profilePatch,
	identityKey = '',
	prevIdentityKey = ''
} = {}) => {
	if (profilePatch && typeof profilePatch.ready === 'boolean') {
		saveUserProfile({ ready: profilePatch.ready });
		return;
	}
	// prev 有手机号且与当前不同视为换号；登出后再登 prev 为空，保留本ready
	if (identityKey && prevIdentityKey && identityKey !== prevIdentityKey) {
		saveUserProfile({ ready: false });
	}
};

/** 登录成功后按 getLaunchRoute 规则 reLaunch（短微信共用*/
export const redirectAfterLogin = () => {
	uni.reLaunch({ url: getLaunchRoute() });
};

export const getMemberPlanByName = () => null;

/** 薄弱知识Top3：对接 GET /app/analysis/overview.weakPoints */
export const getWeakPointRecommendations = async () => {
	const { getWeakPointRecommendations: fetchWeakPoints } = await import('@/common/analysis-overview');
	return fetchWeakPoints();
};

/** 刷题中心年级 Chip：全部年级（默认不按 profile.grade 过滤） */
export const PRACTICE_CHIP_ALL_GRADES = '全部年级';

/** 刷题中心练习类型 Chip：章节练习 */
export const PRACTICE_CHIP_CHAPTER_MODE = practiceChipChapterMode;

/** 常见学科名称（Chip 识别兜底，实际学科来自 profile.subjects 或 GET /app/setup/options） */
export const PRACTICE_SUBJECT_NAMES = ['数学', '语文', '英语', '物理', '化学', '生物'];

/** profile.subjects 生成学科 Chip 列表；无选科时回退数学（编码如 math 会转为中文） */
export const getPracticeSubjectChips = (profile = getUserProfile()) => {
	const subjects = Array.isArray(profile.subjects) ? profile.subjects.filter(Boolean) : [];
	const normalized = [...new Set(subjects.map((item) => resolveSubjectDisplayName(item)).filter(Boolean))];
	return normalized.length ? normalized : ['数学'];
};

/** 教材版本 Chip 文案：与 profile.version 联动，默认人教版 */
export const getPracticeVersionChipLabel = (profile = getUserProfile()) => {
	const version = normalizeVersion(profile.version);
	return version || '人教版';
};

/** 刷题中心完整 Chip 行：全部年级 + 用户学科 + 教材版本 + 章节练习 */
export const getPracticeChips = (profile = getUserProfile()) => {
	return [PRACTICE_CHIP_ALL_GRADES, ...getPracticeSubjectChips(profile), getPracticeVersionChipLabel(profile), PRACTICE_CHIP_CHAPTER_MODE];
};

/** 是否为教材版Chip（文8.2.1：章节全量、练习卡片仅首条*/
export const isPracticeVersionChip = (chip = '', profile = getUserProfile()) => {
	if (!chip) {
		return false;
	}
	return chip === getPracticeVersionChipLabel(profile);
};

/** 是否为练习类Chip：章节练习（仅展示标题含「章节」的练习卡片*/
export const isPracticeChapterModeChip = (chip = '') => {
	return chip === PRACTICE_CHIP_CHAPTER_MODE;
};

/** 是否为学Chip（与 profile.subjects 或六科名称联动） */
export const isPracticeSubjectChip = (chip = '', profile = getUserProfile()) => {
	if (!chip) {
		return false;
	}
	const normalized = resolveSubjectDisplayName(chip);
	return getPracticeSubjectChips(profile).includes(normalized) || PRACTICE_SUBJECT_NAMES.includes(normalized);
};

/** 是否为当前用户年Chip（与 profile.grade / gradeShort 联动*/
export const isPracticeUserGradeChip = (chip = '', profile = {}) => {
	const gradeShort = profile.gradeShort || gradeToShort(profile.grade);
	const grade = String(profile.grade || '').trim();
	if (!chip || !grade) {
		return false;
	}
	return chip === gradeShort || chip === grade;
};

/** Chip 过滤练习卡片：年/ 学科 / 默认全量 */
export const getPracticeFilteredCards = (chip = PRACTICE_CHIP_ALL_GRADES, sourceCards = practiceCards, profile = getUserProfile()) => {
	const list = Array.isArray(sourceCards) ? sourceCards : practiceCards;
	if (chip === PRACTICE_CHIP_ALL_GRADES) {
		return list;
	}
	if (isPracticeUserGradeChip(chip, profile)) {
		const gradeShort = profile.gradeShort || gradeToShort(profile.grade);
		return list.filter((item) => !item.grade || item.grade === profile.grade || item.gradeShort === gradeShort);
	}
	if (isPracticeSubjectChip(chip, profile)) {
		return list;
	}
	if (isPracticeVersionChip(chip, profile)) {
		return list;
	}
	if (isPracticeChapterModeChip(chip)) {
		return list.filter((item) => String(item.title || '').includes('章节'));
	}
	return list;
};

/** Chip 过滤章节列表：年/ 学科 / 教材版本（全量）/ 默认全量 */
export const getPracticeFilteredChapters = (chip = PRACTICE_CHIP_ALL_GRADES, sourceChapters = [], profile = getUserProfile()) => {
	const list = Array.isArray(sourceChapters) ? sourceChapters : [];
	if (chip === PRACTICE_CHIP_ALL_GRADES) {
		return list;
	}
	if (isPracticeUserGradeChip(chip, profile)) {
		const gradeShort = profile.gradeShort || gradeToShort(profile.grade);
		return list.filter((item) => !item.grade || item.grade === profile.grade || item.gradeShort === gradeShort);
	}
	if (isPracticeSubjectChip(chip, profile)) {
		return list;
	}
	return list;
};

/** 错题Chip：全部学科（默认不做学科/时间/频率过滤*/
export const NOTEBOOK_CHIP_ALL_SUBJECTS = '全部学科';

/** 错题Chip：本周（lastPracticeAt 7 天过滤） */
export const NOTEBOOK_CHIP_THIS_WEEK = '本周';

/** 错题Chip：本月（lastPracticeAt 30 天过滤） */
export const NOTEBOOK_CHIP_THIS_MONTH = '本月';

/** 错题Chip：高频错题（未掌握且 wrongCount 阈值） */
export const NOTEBOOK_CHIP_HIGH_FREQ = '高频错题';

/** 「本周」筛选窗口：最近练习日期距今不超过 7 */
export const NOTEBOOK_WEEK_FILTER_DAYS = 7;

/** 「本月」筛选窗口：最近练习日期距今不超过 30 */
export const NOTEBOOK_MONTH_FILTER_DAYS = 30;

/** 是否为「全部学科」Chip（默认展示全部错题，不做学科过滤*/
export const isNotebookAllSubjectsChip = (chip = '') => chip === NOTEBOOK_CHIP_ALL_SUBJECTS;

/** 错题本学Chip：与 profile.subjects 联动，无选科时回退数学 */
export const getNotebookSubjectChips = (profile = getUserProfile()) => getPracticeSubjectChips(profile);

/** 是否为错题本学科 Chip（与 profile.subjects 或六科名称联动） */
export const isNotebookSubjectChip = (chip = '', profile = getUserProfile()) => isPracticeSubjectChip(chip, profile);

/** 是否为「本周」Chip */
export const isNotebookThisWeekChip = (chip = '') => chip === NOTEBOOK_CHIP_THIS_WEEK;

/** 是否为「本月」Chip */
export const isNotebookThisMonthChip = (chip = '') => chip === NOTEBOOK_CHIP_THIS_MONTH;

/** 是否为「高频错题」Chip */
export const isNotebookHighFreqChip = (chip = '') => chip === NOTEBOOK_CHIP_HIGH_FREQ;

/** 错题卡片最近练习日期展示（优先 lastPracticeAt，fallback date*/
export const formatNotebookLastPracticeAt = (item = {}) => {
	const text = String(item.lastPracticeAt || item.date || '').trim();
	return text || '暂无记录';
};

/** 错题卡片累计错题次数（至少为 1*/
export const formatNotebookWrongCount = (item = {}) => Math.max(1, Number(item.wrongCount || 0) || 1);

/** 错题卡片答案展示（空值显示占位符*/
export const formatNotebookAnswer = (value = '') => {
	const text = String(value ?? '').trim();
	return text || '—';
};

/** lastPracticeAt（fallback date）过滤近 N 天内练习过的错题 */
export const filterNotebookByLastPracticeWithinDays = (list = [], days = NOTEBOOK_WEEK_FILTER_DAYS) => {
	const windowDays = Math.max(1, Number(days) || NOTEBOOK_WEEK_FILTER_DAYS);
	return (Array.isArray(list) ? list : []).filter((item) => isWithinDays(item.lastPracticeAt || item.date, windowDays));
};

/** 「本周」错题：lastPracticeAt 7 天内 */
export const filterNotebookByThisWeek = (list = getNotebookList()) => filterNotebookByLastPracticeWithinDays(list, NOTEBOOK_WEEK_FILTER_DAYS);

/** 「本月」错题：lastPracticeAt 30 天内 */
export const filterNotebookByThisMonth = (list = getNotebookList()) => filterNotebookByLastPracticeWithinDays(list, NOTEBOOK_MONTH_FILTER_DAYS);

/** 错题Chip 行：全部学科 + 用户学科/列表学科 + 时间/频率筛*/
export const getNotebookChips = (profile = getUserProfile()) => {
	const fromProfile = getNotebookSubjectChips(profile);
	const fromList = getNotebookList()
		.map((item) => resolveNotebookItemSubject(item))
		.filter(Boolean);
	const subjectChips = [...new Set([...fromProfile, ...fromList])];
	return [
		NOTEBOOK_CHIP_ALL_SUBJECTS,
		...subjectChips,
		NOTEBOOK_CHIP_THIS_WEEK,
		NOTEBOOK_CHIP_THIS_MONTH,
		NOTEBOOK_CHIP_HIGH_FREQ
	];
};

/** 错题Chip 轮换：按 Chip 行顺序切换下一项（末项后回到首项） */
export const cycleNotebookChip = (currentChip = NOTEBOOK_CHIP_ALL_SUBJECTS, chipList = getNotebookChips()) => {
	const list = Array.isArray(chipList) ? chipList : getNotebookChips();
	if (!list.length) {
		return NOTEBOOK_CHIP_ALL_SUBJECTS;
	}
	const currentIndex = list.indexOf(currentChip);
	const nextIndex = currentIndex >= 0 ? (currentIndex + 1) % list.length : 0;
	return list[nextIndex];
};

/** Chip 过滤错题本：全部学科全量；学科按 subject 过滤；本本月/高频错题走时间或频率规则 */
export const getNotebookFilteredList = (chip = NOTEBOOK_CHIP_ALL_SUBJECTS, sourceList = getNotebookList(), profile = getUserProfile()) => {
	const list = Array.isArray(sourceList) ? sourceList : getNotebookList();
	if (isNotebookAllSubjectsChip(chip)) {
		return list;
	}
	if (isNotebookSubjectChip(chip, profile)) {
		return filterNotebookBySubject(list, chip);
	}
	if (isNotebookThisWeekChip(chip)) {
		return filterNotebookByThisWeek(list);
	}
	if (isNotebookThisMonthChip(chip)) {
		return filterNotebookByThisMonth(list);
	}
	if (isNotebookHighFreqChip(chip)) {
		return filterNotebookByHighFreq(list);
	}
	return list;
};

/** 当前 Chip 筛选下错题条目业务键列表（批量操作范围校验用） */
export const getNotebookFilteredItemKeys = (
	chip = NOTEBOOK_CHIP_ALL_SUBJECTS,
	sourceList = getNotebookList(),
	profile = getUserProfile()
) => getNotebookFilteredList(chip, sourceList, profile)
	.map(getNotebookItemKey)
	.filter(Boolean);

/**
 * 薄弱知识点「错题强化」practiceSource（mode=wrong，可选章业务编码 chapterCode chapterId * @param {{ title?: string, chapterCode?: string }} weakPoint
 */
export const buildWeakPointWrongPracticeSource = (weakPoint = {}) => {
	const title = String(weakPoint.title || '').trim();
	const chapterCode = String(weakPoint.chapterCode || '').trim();
	return {
		mode: 'wrong',
		title: title ? `${title}错题强化` : '错题强化',
		...(chapterCode ? { chapterId: chapterCode } : {})
	};
};

/** 薄弱action=wrong-practice：写practiceSource 并跳转答题页 */
export const openWeakPointWrongPractice = (weakPoint = {}) => {
	setPracticeSource(buildWeakPointWrongPracticeSource(weakPoint));
	uni.navigateTo({ url: QUESTION_ANSWER_PAGE });
};

/** 月度报告「今晚先重做错题」practiceSource（mode=wrong，全量错题本范围*/
export const buildReportWrongPracticeSource = () => ({
	mode: 'wrong',
	title: REPORT_WRONG_TITLE
});

/** 报告页「今晚先重做错题」：写入 practiceSource 并跳转答题页 */
export const openReportWrongPractice = () => {
	setPracticeSource(buildReportWrongPracticeSource());
	uni.navigateTo({ url: QUESTION_ANSWER_PAGE });
};

/** 月度报告「分享海报」：跳转海报scene=report，可携带 month=yyyy-MM */
export const openReportSharePoster = (options = {}) => {
	const month = String(options.month || '').trim();
	uni.navigateTo({ url: buildReportPosterUrl(month) });
};

/** 历史月度报告列表*/
export const openReportHistory = () => {
	uni.navigateTo({ url: REPORT_HISTORY_PAGE });
};

/** 按业务月份（yyyy-MM）打开月度学习报告 */
export const openReportByMonth = (month = '') => {
	uni.navigateTo({ url: buildReportPageUrl(month) });
};

/** 月度报告「返回首页」：清空页面栈并 reLaunch 至首*/
export const openReportHome = () => {
	uni.reLaunch({ url: LAUNCH_ROUTE_HOME });
};

/** 月度报告「让 AI 拆计划」：校验 AI 配额后跳chat，携带报topic/tip */
export const openReportAiPlan = (report = {}) => {
	const { topic, tip } = buildReportChatEntry(report);
	const quota = checkAiChatQuota();
	if (!quota.ok && !quota.notReady) {
		showAiQuotaExhaustedModal();
		return;
	}
	uni.navigateTo({ url: buildChatPageUrl({ topic, tip }) });
};

/**
 * 薄弱知识点「OCR 强化」跳URL（携带薄弱点 title 作为搜题关键词）
 * @param {{ title?: string }} weakPoint
 */
export const buildWeakPointOcrPracticeUrl = (weakPoint = {}) => {
	const title = String(weakPoint.title || '').trim();
	if (!title) {
		return OCR_RESULT_PAGE;
	}
	return `${OCR_RESULT_PAGE}?topic=${encodeURIComponent(title)}`;
};

/** 薄弱action=ocr-practice：跳OCR 搜题页并自动以薄弱点标题识别 */
export const openWeakPointOcrPractice = (weakPoint = {}) => {
	uni.navigateTo({ url: buildWeakPointOcrPracticeUrl(weakPoint) });
};

/** 薄弱action=analysis-chat：校AI 配额后跳chat，携带薄弱点 topic/tip */
export const openWeakPointAnalysisChat = (weakPoint = {}) => {
	const { topic, tip } = buildWeakPointChatEntry(weakPoint);
	if (!topic) {
		uni.showToast({ title: '薄弱点信息不完整', icon: 'none' });
		return;
	}
	const quota = checkAiChatQuota();
	if (!quota.ok && !quota.notReady) {
		showAiQuotaExhaustedModal();
		return;
	}
	uni.navigateTo({ url: buildChatPageUrl({ topic, tip }) });
};

/** 构建错题本批量重practiceSource（mode=wrong，仅当前 Chip 筛选范围） */
export const buildNotebookBatchRedoSource = (
	chip = NOTEBOOK_CHIP_ALL_SUBJECTS,
	questionKeys = [],
	profile = getUserProfile()
) => {
	const allowedKeys = new Set(getNotebookFilteredItemKeys(chip, getNotebookList(), profile));
	const keys = (Array.isArray(questionKeys) ? questionKeys : [])
		.map((key) => String(key || '').trim())
		.filter((key) => allowedKeys.has(key));
	return {
		mode: 'wrong',
		title: NOTEBOOK_REDO_TITLE,
		notebookFilterChip: String(chip || NOTEBOOK_CHIP_ALL_SUBJECTS).trim(),
		notebookQuestionKeys: keys
	};
};

/** 按业务键批量删除错题：已登录 DELETE /app/notebook 后以服务端列表为准 */
export const deleteNotebookItemsByKeys = async (
	keys = [],
	sourceList = getNotebookList()
) => {
	const removeKeys = new Set(
		(Array.isArray(keys) ? keys : [])
			.map((key) => String(key || '').trim())
			.filter(Boolean)
	);
	if (!removeKeys.size) {
		return Array.isArray(sourceList) ? sourceList : getNotebookList();
	}
	if (!canSyncNotebookToApi()) {
		return [];
	}
	const list = Array.isArray(sourceList) ? sourceList : getNotebookList();
	const removed = list.filter((item) => removeKeys.has(getNotebookItemKey(item)));
	await Promise.allSettled(removed.map(async (item) => {
		const questionId = resolveNotebookQuestionToken(item);
		if (!questionId) {
			return;
		}
		await removeNotebook(questionId);
	}));
	const synced = await syncNotebookListFromApi();
	return synced.list;
};

/** 清空当前 Chip 筛选下的错题：已登录 DELETE /app/notebook 后以服务端列表为准 */
export const clearNotebookFilteredItems = async (
	chip = NOTEBOOK_CHIP_ALL_SUBJECTS,
	sourceList = getNotebookList(),
	profile = getUserProfile()
) => {
	if (!canSyncNotebookToApi()) {
		return [];
	}
	const list = Array.isArray(sourceList) ? sourceList : getNotebookList();
	const filtered = getNotebookFilteredList(chip, list, profile);
	if (!filtered.length) {
		return list;
	}
	await Promise.allSettled(filtered.map(async (item) => {
		const questionId = resolveNotebookQuestionToken(item);
		if (!questionId) {
			return;
		}
		await removeNotebook(questionId);
	}));
	const synced = await syncNotebookListFromApi();
	return synced.list;
};

/** 错题本待复习数量（未掌握*/
export const getNotebookPendingCount = (list = getNotebookList()) => {
	const notebook = Array.isArray(list) ? list : getNotebookList();
	return notebook.filter((item) => !item.mastered).length;
};

/** 错题掌握率文案：已掌总错× 100%，最35%；无错题100% */
export const getNotebookMasteryRateText = (list = getNotebookList()) => {
	const notebook = Array.isArray(list) ? list : getNotebookList();
	const totalWrong = notebook.length;
	if (!totalWrong) {
		return '100%';
	}
	const masteredCount = notebook.filter((item) => item.mastered).length;
	return `${Math.max(35, Math.round((masteredCount / totalWrong) * 100))}%`;
};

/** 建议时长公式：基础 15 min + 每道待复习错5 min（API 不可用时fallback*/
export const getSuggestedDurationMinutes = (pendingCount = 0) => 15 + pendingCount * 5;

/**
 * 首页今日学习状态卡片：建议时长/今日已做、待复习错题、掌握率或本周正确率
 * @param {object|null} todayAnalysis - fetchTodayAnalysis 结果
 * @param {object|null} overviewAnalysis - fetchAnalysisOverview 结果
 */
export const getHomeStudyStats = (todayAnalysis = null, overviewAnalysis = null) => {
	const notebook = getNotebookList();
	const pendingCount = getNotebookPendingCount(notebook);
	const hasTodayApi = Boolean(todayAnalysis && todayAnalysis.fromApi);
	const hasOverviewApi = Boolean(overviewAnalysis && overviewAnalysis.fromApi);

	const durationMinutes = hasTodayApi && typeof todayAnalysis.durationMinutes === 'number'
		? todayAnalysis.durationMinutes
		: null;
	const todayQuestionCount = hasTodayApi && typeof todayAnalysis.questionCount === 'number'
		? todayAnalysis.questionCount
		: null;
	const overviewCorrectRate = hasOverviewApi && typeof overviewAnalysis.correctRate === 'number'
		? overviewAnalysis.correctRate
		: null;

	return [
		{
			value: durationMinutes != null ? `${durationMinutes} min` : '—',
			label: hasTodayApi ? '今日时长' : '建议时长'
		},
		{
			value: todayQuestionCount != null ? `${todayQuestionCount} 道` : `${pendingCount} 道`,
			label: todayQuestionCount != null ? '今日已做' : '待复习错题'
		},
		{
			value: overviewCorrectRate != null ? `${overviewCorrectRate}%` : getNotebookMasteryRateText(notebook),
			label: overviewCorrectRate != null ? '本周正确率' : '当前掌握率'
		}
	];
};
/** 首页 Hero Banner 文案：按 isVip 分支，含待复习错题数 */
export const getHomeHeroData = () => {
	const member = getMemberState();
	const pendingCount = getNotebookList().filter((item) => !item.mastered).length;
	if (member.isVip) {
		return {
			badge: '会员进行中',
			titleLines: ['你的AI至尊权益已生效', pendingCount ? `还有 ${pendingCount} 道错题待复习` : '继续保持当前学习节奏'],
			buttonText: '查看权益'
		};
	}
	return {
		badge: '限时特惠',
		titleLines: ['开通AI至尊会员', pendingCount ? `解锁无限次搜题并优先消灭 ${pendingCount} 道错题` : '享无限次搜题'],
		buttonText: '立即查看'
	};
};

export const getHomeRecommendList = () => {
	const profile = getUserProfile();
	const notebook = getNotebookList();
	const subject = profile.subjects?.[0] || '';
	const wrongCount = notebook.filter((item) => !item.mastered).length;
	const wrongTitle = subject ? `${subject}错题回顾强化` : '错题回顾强化';
	const chapterTitle = subject ? `${subject}本周提分练习` : '本周提分练习';
	const gradeMeta = [profile.gradeShort, profile.version].filter(Boolean).join(' | ') || '请先完善学习计划';
	return [{
		title: wrongTitle,
		meta: wrongCount ? `${wrongCount} 题 | 优先补齐薄弱项` : '暂无待复习错题',
		tag: wrongCount ? '今晚优先' : '保持状态',
		icon: 'bookmark-fill',
		iconBg: '#FFEDEE',
		iconColor: '#FF5E66',
		action: 'wrong-practice'
	}, {
		title: chapterTitle,
		meta: gradeMeta,
		tag: '个性化推荐',
		icon: 'file-text-fill',
		iconBg: '#EEF4FF',
		iconColor: '#4B7AFE',
		action: 'chapter-practice'
	}];
};
