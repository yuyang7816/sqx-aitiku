/**
 * 模块十九 · Storage Key 常量（文档 19.1）
 * 全项目 uni.getStorageSync / setStorageSync / removeStorageSync 须引用本文件，禁止散落字符串。
 */

/** Storage 数据迁移版本号（文档 19.3，App onLaunch 最先执行迁移脚本） */
export const STORAGE_VERSION_KEY = 'aiQuestionStorageVersion';
/** 当前 Storage 结构版本；新增迁移步时递增并在 storage-migration.js 注册 */
export const CURRENT_STORAGE_VERSION = 3;

/** 敏感字段 AES 密文前缀（storage-crypto.js） */
export const ENCRYPTED_STORAGE_PREFIX = 'enc1:';

/** 登录态 */
export const AUTH_KEY = 'aiQuestionAuthState';
/** 用户资料 */
export const PROFILE_KEY = 'aiQuestionUserProfile';
/** 会员状态 */
export const MEMBER_KEY = 'aiQuestionMemberState';
/** 错题本 */
export const NOTEBOOK_KEY = 'aiQuestionNotebook';
/** 收藏 */
export const COLLECTION_KEY = 'aiQuestionCollections';
/** 消息已读状态 */
export const NOTICE_KEY = 'aiQuestionNotices';
/** 设置 */
export const SETTINGS_KEY = 'aiQuestionSettings';
/** 练习来源 */
export const PRACTICE_SOURCE_KEY = 'aiQuestionPracticeSource';
/** 刷题中心当前 Chip 筛选（学科/版本等，子页读取） */
export const PRACTICE_ACTIVE_CHIP_KEY = 'aiQuestionPracticeActiveChip';
/** 搜题记录 */
export const SEARCH_RECORD_KEY = 'aiQuestionSearchRecords';
/** 当次练习结果（临时） */
export const QUESTION_PRACTICE_SESSION_KEY = 'questionPracticeSession';
/** 当前查看解析的题目（临时） */
export const QUESTION_REVIEW_KEY = 'questionReviewItem';
/** 答题页 ↔ 答题卡页临时桥接态 */
export const QUESTION_ANSWER_SHEET_KEY = 'questionAnswerSheetBridge';

/** JWT 持久化（模块二） */
export const TOKEN_KEY = 'aiQuestionToken';
/** 老项目 legacy token 键，读写时向下兼容 */
export const LEGACY_TOKEN_KEY = 'token';

/** 客服配置（企业微信链接/AppId、类型、电话、入口开关） */
export const CUSTOMER_CONFIG_KEY = 'aiQuestionCustomerConfig';
/** 章节题目本地做题状态 */
export const CHAPTER_ANSWER_KEY = 'chapterQuestionAnswerMap';
/** 练习答题暂存（杀进程续答） */
export const PRACTICE_DRAFT_KEY = 'aiQuestionPracticeDraft';
/** 学习日历本机打卡/练习日期（API 失败或未同步时兜底） */
export const STUDY_CALENDAR_KEY = 'aiQuestionStudyCalendar';
/** 帮助与反馈工单本地 fallback */
export const FEEDBACK_STORAGE_KEY = 'aiQuestionFeedbackTickets';
/** 错题本导出暂存 */
export const NOTEBOOK_EXPORT_KEY = 'aiQuestionNotebookExport';
/** 会员订单历史本地 fallback */
export const MEMBER_ORDER_HISTORY_KEY = 'memberOrderHistory';
/** 待绑定上级邀请码 */
export const PENDING_INVITER_ID_KEY = 'pendingInviterId';
/** 闪屏结束后待跳转深链 URL（poster / invite 登录页） */
export const PENDING_DEEP_LINK_URL_KEY = 'pendingDeepLinkUrl';
/** 登录错误联调 Mock */
export const LOGIN_ERROR_MOCK_STORAGE_KEY = 'LOGIN_ERROR_MOCK';

/** legacy 用户标识 Storage 键（退出时清除，与 httpRequest 401 拦截一致） */
export const LEGACY_USER_ID_KEY = 'userId';
export const LEGACY_USER_PHONE_KEY = 'phone';
export const LEGACY_USER_OPEN_ID_KEY = 'openId';
export const LEGACY_USER_STORAGE_KEYS = [
	LEGACY_USER_ID_KEY,
	LEGACY_USER_PHONE_KEY,
	LEGACY_USER_OPEN_ID_KEY
];

/** 老项目 legacy 键（queue.js 等） */
export const LEGACY_PUBLIC_RELATION_KEY = 'publicRelation';
export const LEGACY_APP_URL_KEY = 'appurl';

/**
 * v0 旧版业务 Storage 键 → 文档 19.1 标准键（升级时复制数据，不丢学习记录）
 * 仅用于 storage-migration.js，禁止业务代码直接读写左侧 legacy 键
 */
export const LEGACY_BUSINESS_STORAGE_KEY_MAP = {
	authState: AUTH_KEY,
	userProfile: PROFILE_KEY,
	memberState: MEMBER_KEY,
	wrongNotebook: NOTEBOOK_KEY,
	collections: COLLECTION_KEY,
	notices: NOTICE_KEY,
	settings: SETTINGS_KEY,
	practiceSource: PRACTICE_SOURCE_KEY,
	searchRecords: SEARCH_RECORD_KEY
};

/**
 * 退出登录保留的学习/业务数据 Storage 键（文档 19.1 / 23.1，禁止在 logoutUser 中清除）
 */
export const LEARNING_STORAGE_KEYS = [
	NOTEBOOK_KEY,
	COLLECTION_KEY,
	NOTICE_KEY,
	SETTINGS_KEY,
	PROFILE_KEY,
	MEMBER_KEY,
	PRACTICE_SOURCE_KEY,
	SEARCH_RECORD_KEY,
	CUSTOMER_CONFIG_KEY,
	STUDY_CALENDAR_KEY
];
