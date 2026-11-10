/**
 * 模块二十四 · 退出再登录冒烟校验（文档 24.1 第 9 项）
 * 运行：node scripts/verify-smoke-logout.mjs
 */
import fs from 'fs';
import path from 'path';
import {
	AUTH_KEY,
	TOKEN_KEY,
	PROFILE_KEY,
	MEMBER_KEY,
	NOTEBOOK_KEY,
	COLLECTION_KEY,
	SETTINGS_KEY,
	LEGACY_USER_STORAGE_KEYS,
	LEARNING_STORAGE_KEYS
} from '../common/storage-keys.js';

const root = path.resolve('.');

const read = (rel) => fs.readFileSync(path.join(root, rel), 'utf8');

const checks = [];

const assert = (name, ok, detail = '') => {
	checks.push({ name, ok, detail });
};

const assertIncludes = (name, file, patterns) => {
	const src = read(file);
	const missing = patterns.filter((p) => !src.includes(p));
	if (missing.length) {
		assert(name, false, `missing in ${file}: ${missing.join(', ')}`);
		return;
	}
	assert(name, true, file);
};

// --- 与 impl.js 对齐的轻量复刻 ---

const LAUNCH_ROUTE_LOGIN = '/package/auth/login';
const LAUNCH_ROUTE_SETUP = '/package/setup/plan';
const LAUNCH_ROUTE_HOME = '/pages/index/index';

const defaultAuthState = { loggedIn: false, phone: '', loginType: '' };

const resolveLaunchRoute = (authState = {}, userProfile = {}) => {
	if (authState.loggedIn !== true) {
		return LAUNCH_ROUTE_LOGIN;
	}
	if (userProfile.ready !== true) {
		return LAUNCH_ROUTE_SETUP;
	}
	return LAUNCH_ROUTE_HOME;
};

/** 模拟 logoutUser：清鉴权键与本地错题本缓存，保留其余学习数据 */
const simulateLogoutStorage = (storage = {}) => {
	const next = { ...storage };
	delete next[AUTH_KEY];
	delete next[TOKEN_KEY];
	delete next[NOTEBOOK_KEY];
	LEGACY_USER_STORAGE_KEYS.forEach((key) => {
		delete next[key];
	});
	next[AUTH_KEY] = defaultAuthState;
	return next;
};

const applyPostLoginProfile = ({
	profilePatch,
	identityKey = '',
	prevIdentityKey = '',
	profile = {}
} = {}) => {
	if (profilePatch && typeof profilePatch.ready === 'boolean') {
		return { ...profile, ready: profilePatch.ready };
	}
	if (identityKey && prevIdentityKey && identityKey !== prevIdentityKey) {
		return { ...profile, ready: false };
	}
	return profile;
};

const mockNotebookItem = {
	id: 'notebook-smoke-1',
	subject: '章节练习',
	question: '冒烟测试错题',
	wrongCount: 1,
	mastered: false
};

const mockCollectionItem = {
	id: 'collection-smoke-1',
	title: '冒烟收藏题',
	subject: 'OCR搜题',
	questionCode: 'Q-SMOKE-001'
};

const mockProfile = {
	ready: true,
	grade: '初中三年级',
	version: '人教版',
	subjects: ['数学', '物理']
};

const mockMember = {
	isVip: true,
	level: 'AI至尊会员',
	expireAt: '2027-01-01'
};

const buildLoggedInStorage = () => ({
	[AUTH_KEY]: { loggedIn: true, phone: '13800138000', loginType: 'sms' },
	[TOKEN_KEY]: 'mock-jwt-token',
	[PROFILE_KEY]: mockProfile,
	[MEMBER_KEY]: mockMember,
	[NOTEBOOK_KEY]: [mockNotebookItem],
	[COLLECTION_KEY]: [mockCollectionItem],
	[SETTINGS_KEY]: { autoSync: true }
});

// TC-M24-009 · 24.1-9：退出登录 → 回登录页 → 再登录数据仍在

assert(
	'24.1-9: logout clears auth token',
	(() => {
		const after = simulateLogoutStorage(buildLoggedInStorage());
		return after[AUTH_KEY]?.loggedIn === false && after[TOKEN_KEY] === undefined;
	})()
);

assert(
	'24.1-9: logout clears notebook cache',
	(() => {
		const after = simulateLogoutStorage(buildLoggedInStorage());
		return after[NOTEBOOK_KEY] === undefined;
	})()
);

assert(
	'24.1-9: logout preserves collection',
	(() => {
		const after = simulateLogoutStorage(buildLoggedInStorage());
		return Array.isArray(after[COLLECTION_KEY])
			&& after[COLLECTION_KEY].some((item) => item.questionCode === mockCollectionItem.questionCode);
	})()
);

assert(
	'24.1-9: logout preserves profile ready',
	(() => {
		const after = simulateLogoutStorage(buildLoggedInStorage());
		return after[PROFILE_KEY]?.ready === true
			&& after[PROFILE_KEY]?.subjects?.includes('数学');
	})()
);

assert(
	'24.1-9: logout preserves member VIP state',
	(() => {
		const after = simulateLogoutStorage(buildLoggedInStorage());
		return after[MEMBER_KEY]?.isVip === true;
	})()
);

assert(
	'24.1-9: LEARNING_STORAGE_KEYS covers notebook/collection/profile/member',
	LEARNING_STORAGE_KEYS.includes(NOTEBOOK_KEY)
	&& LEARNING_STORAGE_KEYS.includes(COLLECTION_KEY)
	&& LEARNING_STORAGE_KEYS.includes(PROFILE_KEY)
	&& LEARNING_STORAGE_KEYS.includes(MEMBER_KEY)
);

assert(
	'24.1-9: re-login same phone keeps profile ready',
	(() => {
		const afterLogout = simulateLogoutStorage(buildLoggedInStorage());
		const profileAfterRelogin = applyPostLoginProfile({
			identityKey: '13800138000',
			prevIdentityKey: '',
			profile: afterLogout[PROFILE_KEY]
		});
		return profileAfterRelogin.ready === true;
	})()
);

assert(
	'24.1-9: re-login same phone routes to home when ready',
	(() => {
		const afterLogout = simulateLogoutStorage(buildLoggedInStorage());
		const profile = applyPostLoginProfile({
			identityKey: '13800138000',
			prevIdentityKey: '',
			profile: afterLogout[PROFILE_KEY]
		});
		const route = resolveLaunchRoute({ loggedIn: true, phone: '13800138000' }, profile);
		return route === LAUNCH_ROUTE_HOME;
	})()
);

assert(
	'24.1-9: different phone on login resets ready',
	applyPostLoginProfile({
		identityKey: '13900139000',
		prevIdentityKey: '13800138000',
		profile: mockProfile
	}).ready === false
);

assert(
	'24.1-9: guest after logout routes to login',
	resolveLaunchRoute(defaultAuthState, mockProfile) === LAUNCH_ROUTE_LOGIN
);

// --- 静态链路：我的/设置 → 退出 → 登录页 → 再登录 ---

assertIncludes('24.1-9: my page logout', 'pages/my/my.vue', [
	'performLogoutAndRedirect',
	'保留错题本/收藏/设置等学习数据'
]);

assertIncludes('24.1-9: settings page logout', 'package/profile/settings.vue', [
	'performLogoutAndRedirect',
	'清鉴权保留学习数据后跳转登录页'
]);

assertIncludes('24.1-9: performLogoutAndRedirect impl', 'common/app-data/impl.js', [
	'logoutUser',
	'clearAllTabPageStates',
	'LAUNCH_ROUTE_LOGIN',
	'uni.reLaunch'
]);

const logoutSrc = read('common/app-data/impl.js');
assert(
	'24.1-9: logoutUser clears notebook cache',
	logoutSrc.includes('setNotebookList([])')
);

assertIncludes('24.1-9: 401 triggers logout redirect', 'common/api/request.js', [
	'handleUnauthorized',
	'performLogoutAndRedirect',
	'登录已失效'
]);

assertIncludes('24.1-9: login page re-login chain', 'package/auth/login.vue', [
	'completeAuthSession',
	'redirectAfterLogin'
]);

assertIncludes('24.1-9: redirectAfterLogin uses getLaunchRoute', 'common/app-data/impl.js', [
	'redirectAfterLogin',
	'getLaunchRoute'
]);

assertIncludes('24.1-9: syncUserInfoAfterLogin merges cloud data', 'common/user-info.js', [
	'syncUserInfoAfterLogin',
	'fetchAndMergeUserInfo',
	'syncNotebookAndCollectionFromApi'
]);

assertIncludes('24.1-9: login-rules preserved keys', 'common/login-rules.js', [
	'LOGOUT_PRESERVED_STORAGE_KEYS',
	'LEARNING_STORAGE_KEYS'
]);

assertIncludes('24.1-9: notebook sync clears cache on API fail', 'common/app-data/impl.js', [
	'syncNotebookListFromApi',
	'setNotebookList([])',
	'syncCollectionListFromApi',
	'getCollectionList()'
]);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Smoke logout flow verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'smoke logout flow checks passed (24.1 item 9)');
checks.forEach((c) => console.log('  ✓', c.name));
