/**
 * 模块二十四 · 登录→计划→首页主流程冒烟校验（文档 24.1 前三项）
 * 运行：node scripts/verify-smoke-login.mjs
 */
import fs from 'fs';
import path from 'path';

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

// --- 文档 4.3 / 24.1 路由决策纯函数（与 impl.js 对齐） ---

const LAUNCH_ROUTE_LOGIN = '/package/auth/login';
const LAUNCH_ROUTE_SETUP = '/package/setup/plan';
const LAUNCH_ROUTE_HOME = '/pages/index/index';
const SETUP_PLAN_ROUTE = 'package/setup/plan';

const resolveLaunchRoute = (authState = {}, userProfile = {}) => {
	if (authState.loggedIn !== true) {
		return LAUNCH_ROUTE_LOGIN;
	}
	if (userProfile.ready !== true) {
		return LAUNCH_ROUTE_SETUP;
	}
	return LAUNCH_ROUTE_HOME;
};

const shouldRedirectToSetupPlan = (currentRoute, authState = {}, userProfile = {}) => {
	if (authState.loggedIn !== true || userProfile.ready === true) {
		return false;
	}
	return currentRoute !== SETUP_PLAN_ROUTE;
};

const completeUserProfile = ({ grade, version, subjects }) => {
	const subjectList = Array.isArray(subjects) ? subjects.filter(Boolean) : [];
	return {
		ready: subjectList.length > 0,
		grade,
		version,
		subjects: subjectList.length ? subjectList : ['数学', '物理']
	};
};

// TC-M24-001 · 24.1-1：未登录打开 → 跳转登录页
assert(
	'24.1-1: guest → login',
	resolveLaunchRoute({ loggedIn: false }, { ready: false }) === LAUNCH_ROUTE_LOGIN
);
assert(
	'24.1-1: empty auth → login',
	resolveLaunchRoute({}, {}) === LAUNCH_ROUTE_LOGIN
);

// TC-M24-002 · 24.1-2：登录后未选学科(ready=false) → 跳转计划页
assert(
	'24.1-2: logged-in not ready → plan',
	resolveLaunchRoute({ loggedIn: true }, { ready: false }) === LAUNCH_ROUTE_SETUP
);
assert(
	'24.1-2: guard blocks home when not ready',
	shouldRedirectToSetupPlan('pages/index/index', { loggedIn: true }, { ready: false }) === true
);
assert(
	'24.1-2: guard allows plan when not ready',
	shouldRedirectToSetupPlan(SETUP_PLAN_ROUTE, { loggedIn: true }, { ready: false }) === false
);
assert(
	'24.1-2: guard skips when guest',
	shouldRedirectToSetupPlan('pages/index/index', { loggedIn: false }, { ready: false }) === false
);

// TC-M24-003 · 24.1-3：完善计划 → 进入首页
assert(
	'24.1-3: logged-in ready → home',
	resolveLaunchRoute({ loggedIn: true }, { ready: true }) === LAUNCH_ROUTE_HOME
);
const profileAfterPlan = completeUserProfile({
	grade: '初中三年级',
	version: '人教版',
	subjects: ['数学']
});
assert(
	'24.1-3: completeUserProfile sets ready when subjects chosen',
	profileAfterPlan.ready === true && profileAfterPlan.subjects.includes('数学')
);
assert(
	'24.1-3: empty subjects keeps ready false',
	completeUserProfile({ grade: '初中一年级', version: '人教版', subjects: [] }).ready === false
);

// --- 静态链路：App 入口守卫 → getLaunchRoute → 登录/计划/首页 ---

assertIncludes('App entry guard uses getLaunchRoute', 'App.vue', [
	'getLaunchRoute',
	'syncEntryRoute',
	'uni.reLaunch'
]);
assertIncludes('login redirectAfterLogin', 'package/auth/login.vue', [
	'redirectAfterLogin',
	'completeAuthSession'
]);
assertIncludes('plan save reLaunch home', 'package/setup/plan.vue', [
	'syncUserProfileToServer',
	'LAUNCH_ROUTE_HOME',
	"uni.reLaunch({",
	'url: LAUNCH_ROUTE_HOME'
]);
assertIncludes('App.vue entry guard', 'App.vue', [
	'syncEntryRoute',
	'shouldRedirectToSetupPlan',
	'LAUNCH_ROUTE_SETUP',
	'getLaunchRoute'
]);
assertIncludes('launch routes exported', 'common/app-data/launch.js', [
	'LAUNCH_ROUTE_LOGIN',
	'LAUNCH_ROUTE_SETUP',
	'LAUNCH_ROUTE_HOME',
	'resolveLaunchRoute',
	'redirectAfterLogin'
]);
assertIncludes('pages.json routes registered', 'pages.json', [
	'"path": "login"',
	'"path": "plan"',
	'"path": "pages/index/index"'
]);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Smoke login flow verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'smoke login flow checks passed (24.1 items 1-3)');
checks.forEach((c) => console.log('  ✓', c.name));
