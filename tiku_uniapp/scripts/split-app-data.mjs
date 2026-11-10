/**
 * 拆分 app-data impl.js 为领域模块（m19-f06）
 * 运行：node scripts/split-app-data.mjs
 */
import fs from 'fs';
import path from 'path';

const dir = path.resolve('common/app-data');
const implPath = path.join(dir, 'impl.js');
const impl = fs.readFileSync(implPath, 'utf8');

/** 文档 19.2 核心函数（验收清单） */
export const CORE_FUNCTIONS_19_2 = [
	'getLaunchRoute',
	'loginUser',
	'logoutUser',
	'completeUserProfile',
	'saveUserProfile',
	'getMemberState',
	'activateMemberPlan',
	'getRemainingAiTimes',
	'syncWrongNotebook',
	'markNotebookMastered',
	'getPracticeQuestionList',
	'toggleCollectionItem',
	'hasCollectionItem',
	'getNoticeList',
	'markNoticeRead',
	'appendSearchRecord',
	'getHomeStudyStats',
	'getHomeRecommendList'
];

/** 领域模块 → 从 impl 再导出的符号 */
const DOMAIN_EXPORTS = {
	'storage-keys.js': {
		type: 'reexport',
		from: '@/common/storage-keys',
		names: [
			'AUTH_KEY', 'CHAPTER_ANSWER_KEY', 'COLLECTION_KEY', 'CUSTOMER_CONFIG_KEY',
			'LEARNING_STORAGE_KEYS', 'MEMBER_KEY', 'NOTEBOOK_KEY', 'NOTICE_KEY',
			'PRACTICE_SOURCE_KEY', 'PROFILE_KEY', 'QUESTION_REVIEW_KEY', 'SEARCH_RECORD_KEY', 'SETTINGS_KEY'
		]
	},
	'utils.js': {
		from: './impl.js',
		names: [] // populated below - internal only, not re-exported from impl
	},
	'auth.js': {
		from: './impl.js',
		names: ['getAuthState', 'saveAuthState', 'loginUser', 'logoutUser', 'performLogoutAndRedirect']
	},
	'profile.js': {
		from: './impl.js',
		names: [
			'gradeToShort', 'toAvatarText', 'resolveProfileAvatarUrl', 'formatProfileSubjects',
			'formatProfileMetaLine', 'formatMyProfileMetaLine', 'getUserProfile', 'saveUserProfile',
			'completeUserProfile', 'applyPostLoginProfile', 'getPracticeSubjectChips'
		]
	},
	'launch.js': {
		from: './impl.js',
		names: [
			'LAUNCH_ROUTE_LOGIN', 'LAUNCH_ROUTE_SETUP', 'LAUNCH_ROUTE_HOME', 'ENTRY_ROUTES',
			'SETUP_PLAN_ROUTE', 'SETUP_PLAN_EDIT_MODE', 'SETUP_PLAN_EDIT_PAGE',
			'shouldSkipLaunchRedirectForSetupPlanEdit', 'shouldRedirectToSetupPlan',
			'resolveLaunchRoute', 'getLaunchRoute', 'redirectAfterLogin'
		]
	},
	'settings.js': {
		from: './impl.js',
		names: [
			'getSettings', 'saveSettings', 'isAutoSyncEnabled', 'isPushEnabled', 'isSoundEnabled',
			'canSyncNotebookToApi', 'canSyncCollectionToApi', 'getCustomerConfig', 'saveCustomerConfig'
		]
	},
	'member.js': {
		from: './impl.js',
		names: [
			'VIP_RENEW_NOTICE_DAYS', 'AI_NOTICE_REMAINING_THRESHOLD', 'getMemberRemainingDays',
			'buildDynamicNotices', 'getMemberState', 'refreshMemberExpireState', 'saveMemberState',
			'activateMemberPlan', 'getRemainingAiTimes', 'checkAiChatQuota', 'consumeAiTimes',
			'showAiQuotaExhaustedModal', 'getRemainingOcrTimes', 'checkOcrSearchQuota',
			'syncAiQuotaFromApi', 'syncOcrQuotaFromApi', 'showOcrQuotaExhaustedModal',
			'getMemberPlanByName'
		]
	},
	'collection.js': {
		from: './impl.js',
		names: [
			'getCollectionList', 'setCollectionList', 'buildCollectionItem', 'resolveCollectionSubject',
			'resolveCollectionQuestionToken', 'getCollectionItemKey', 'cancelCollectionItem',
			'syncCollectionListFromApi', 'toggleCollectionItem', 'hasCollectionItem'
		]
	},
	'notice.js': {
		from: './impl.js',
		names: [
			'resolveNoticeReadKey', 'NOTICE_READ_STATE_EVENT', 'applyStoredNoticeReadState',
			'getNoticeList', 'setNoticeList', 'markAllNoticesRead', 'markNoticeRead'
		]
	},
	'search.js': {
		from: './impl.js',
		names: [
			'SEARCH_RECORD_MAX', 'SEARCH_RECORD_SOURCES', 'getSearchRecords', 'setSearchRecords',
			'clearSearchRecords', 'appendSearchRecord', 'syncSearchRecordsFromApi'
		]
	},
	'notebook.js': {
		from: './impl.js',
		names: [
			'getNotebookList', 'setNotebookList', 'buildNotebookItem', 'buildReviewItemFromNotebook',
			'resolveNotebookQuestionToken', 'hasNotebookItem', 'buildOcrNotebookPayload',
			'syncNotebookListFromApi', 'addNotebookItem', 'resolveSyncWrongNotebookSubject',
			'syncWrongNotebook', 'markNotebookMastered', 'toggleNotebookMastered',
			'NOTEBOOK_CHIP_ALL_SUBJECTS', 'NOTEBOOK_CHIP_THIS_WEEK', 'NOTEBOOK_CHIP_THIS_MONTH',
			'NOTEBOOK_CHIP_HIGH_FREQ', 'NOTEBOOK_WEEK_FILTER_DAYS', 'NOTEBOOK_MONTH_FILTER_DAYS',
			'NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT', 'isNotebookAllSubjectsChip', 'getNotebookItemSubjectName',
			'getNotebookSubjectChips', 'isNotebookSubjectChip', 'filterNotebookBySubject',
			'isNotebookThisWeekChip', 'isNotebookThisMonthChip', 'isNotebookHighFreqChip',
			'isNotebookHighFreqItem', 'NOTEBOOK_STATUS_MASTERED', 'NOTEBOOK_STATUS_HIGH_FREQ',
			'NOTEBOOK_STATUS_PENDING', 'getNotebookItemStatusText', 'getNotebookItemStatusClass',
			'formatNotebookLastPracticeAt', 'formatNotebookWrongCount', 'formatNotebookAnswer',
			'filterNotebookByLastPracticeWithinDays', 'filterNotebookByThisWeek', 'filterNotebookByThisMonth',
			'filterNotebookByHighFreq', 'getNotebookChips', 'cycleNotebookChip', 'getNotebookFilteredList',
			'getNotebookItemKey', 'getNotebookFilteredItemKeys', 'buildNotebookBatchRedoSource',
			'deleteNotebookItemsByKeys', 'clearNotebookFilteredItems', 'getNotebookPendingCount',
			'getNotebookMasteryRateText'
		]
	},
	'practice.js': {
		from: './impl.js',
		names: [
			'PRACTICE_SOURCE_MODES', 'getPracticeSource', 'setPracticeSource', 'getChapterAnswerMap',
			'syncChapterQuestionAnswer', 'applyChapterAnswerOverlay', 'clearPracticeSource',
			'getPracticeQuestionList', 'PRACTICE_CHIP_ALL_GRADES', 'PRACTICE_CHIP_CHAPTER_MODE',
			'PRACTICE_SUBJECT_NAMES', 'getPracticeChips', 'getPracticeVersionChipLabel',
			'isPracticeVersionChip', 'isPracticeChapterModeChip', 'isPracticeSubjectChip',
			'isPracticeUserGradeChip', 'getPracticeFilteredCards', 'getPracticeFilteredChapters',
			'buildWeakPointWrongPracticeSource', 'buildReportWrongPracticeSource'
		]
	},
	'review.js': {
		from: './impl.js',
		names: ['buildReviewItemFromCollection', 'setQuestionReviewItem']
	},
	'home.js': {
		from: './impl.js',
		names: [
			'getSuggestedDurationMinutes', 'getHomeStudyStats', 'getHomeHeroData',
			'getHomeRecommendList', 'getWeakPointRecommendations'
		]
	},
	'navigation.js': {
		from: './impl.js',
		names: [
			'openWeakPointWrongPractice', 'openReportWrongPractice', 'openReportSharePoster',
			'openReportHistory', 'openReportByMonth', 'openReportHome', 'openReportAiPlan',
			'buildWeakPointOcrPracticeUrl', 'openWeakPointOcrPractice', 'openWeakPointAnalysisChat'
		]
	}
};

// Collect all assigned exports
const assigned = new Set();
for (const [file, cfg] of Object.entries(DOMAIN_EXPORTS)) {
	if (cfg.names) cfg.names.forEach((n) => assigned.add(n));
}
if (DOMAIN_EXPORTS['storage-keys.js'].names) {
	DOMAIN_EXPORTS['storage-keys.js'].names.forEach((n) => assigned.add(n));
}

// Find all exports in impl
const implExports = new Set();
for (const m of impl.matchAll(/^export const ([A-Za-z_][A-Za-z0-9_]*) /gm)) implExports.add(m[1]);
for (const m of impl.matchAll(/^export (?:async )?function ([a-zA-Z0-9_]+)/gm)) implExports.add(m[1]);

const missing = [...implExports].filter((n) => !assigned.has(n));
const extra = [...assigned].filter((n) => !implExports.has(n));

console.log('impl exports:', implExports.size);
console.log('assigned:', assigned.size);
if (missing.length) console.log('MISSING from domains:', missing.join(', '));
if (extra.length) console.log('EXTRA not in impl:', extra.join(', '));

// Generate domain re-export files
for (const [file, cfg] of Object.entries(DOMAIN_EXPORTS)) {
	if (cfg.type === 'reexport') {
		const content = `/** 模块十九 · Storage Key 再导出（文档 19.1） */\nexport {\n\t${cfg.names.join(',\n\t')}\n} from '${cfg.from}';\n`;
		fs.writeFileSync(path.join(dir, file), content);
		continue;
	}
	if (!cfg.names?.length) continue;
	const label = file.replace('.js', '');
	const content = `/** 模块十九 · app-data / ${label}（Storage + API 对齐） */\nexport {\n\t${cfg.names.join(',\n\t')}\n} from '${cfg.from}';\n`;
	fs.writeFileSync(path.join(dir, file), content);
}

// core.js - 19.2 manifest
const coreImports = {
	'./launch.js': ['getLaunchRoute'],
	'./auth.js': ['loginUser', 'logoutUser'],
	'./profile.js': ['completeUserProfile', 'saveUserProfile'],
	'./member.js': ['getMemberState', 'activateMemberPlan', 'getRemainingAiTimes'],
	'./notebook.js': ['syncWrongNotebook', 'markNotebookMastered'],
	'./practice.js': ['getPracticeQuestionList'],
	'./collection.js': ['toggleCollectionItem', 'hasCollectionItem'],
	'./notice.js': ['getNoticeList', 'markNoticeRead'],
	'./search.js': ['appendSearchRecord'],
	'./home.js': ['getHomeStudyStats', 'getHomeRecommendList']
};

let coreBody = `/**
 * 文档 19.2 核心函数清单（API + Storage 对齐）
 * @see tiku_uniapp项目需求与流程梳理.md §19.2
 */
export const APP_DATA_CORE_FUNCTIONS = ${JSON.stringify(CORE_FUNCTIONS_19_2, null, '\t')};\n\n`;
for (const [from, names] of Object.entries(coreImports)) {
	coreBody += `export { ${names.join(', ')} } from '${from}';\n`;
}
fs.writeFileSync(path.join(dir, 'core.js'), coreBody);

// index.js - barrel
const moduleFiles = Object.keys(DOMAIN_EXPORTS).concat(['core.js']);
let indexBody = `/**
 * 模块十九 · app-data 统一入口
 * 领域模块：auth / profile / launch / member / notebook / collection / practice / notice / search / home
 * 核心函数见 core.js（文档 19.2）
 */
export * from './impl.js';\n`;
for (const f of moduleFiles) {
	if (f === 'utils.js') continue;
	indexBody += `export * from './${f.replace('.js', '')}';\n`;
}
fs.writeFileSync(path.join(dir, 'index.js'), indexBody);

console.log('Generated domain modules in', dir);
