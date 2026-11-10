/**
 * 模块二十三 · 登录规则静态校验（文档 23.1）
 * 运行：node scripts/verify-login-rules.mjs
 */
import fs from 'fs';
import path from 'path';

const root = path.resolve('.');

const read = (rel) => fs.readFileSync(path.join(root, rel), 'utf8');

const checks = [];

const assertIncludes = (name, file, patterns) => {
	const src = read(file);
	const missing = patterns.filter((p) => !src.includes(p));
	if (missing.length) {
		checks.push({ name, ok: false, detail: `missing in ${file}: ${missing.join(', ')}` });
		return;
	}
	checks.push({ name, ok: true, detail: file });
};

// 规则 1：协议必选 — 登录请求传 agreedProtocol
assertIncludes('sms-login agreedProtocol', 'package/auth/login.vue', [
	'buildAuthLoginBody',
	"postJson('/app/auth/sms'"
]);
assertIncludes('wechat-login agreedProtocol', 'common/wechat-oauth.js', [
	'buildAuthLoginBody',
	"postJson('/app/auth/wechat'",
	'agreedProtocol'
]);
assertIncludes('login-ui agreement gate', 'package/auth/login.vue', [
	'agreed.value',
	'AGREEMENT_TIP'
]);

// 规则 2：未 ready 不可进首页
assertIncludes('resolveLaunchRoute ready gate', 'common/app-data/impl.js', [
	'userProfile.ready !== true',
	'LAUNCH_ROUTE_SETUP'
]);
assertIncludes('App route guard', 'App.vue', [
	'shouldRedirectToSetupPlan',
	'LAUNCH_ROUTE_SETUP'
]);

// 规则 3：logout 保留收藏等学习数据，错题本改由服务端拉取故退出时清空本地缓存
const logoutSrc = read('common/app-data/impl.js');
const storageKeysSrc = read('common/storage-keys.js');
if (!logoutSrc.includes('setNotebookList([])')) {
	checks.push({ name: 'logout clears notebook cache', ok: false, detail: 'logoutUser must clear notebook via setNotebookList([])' });
} else if (!storageKeysSrc.includes('NOTEBOOK_KEY') || !storageKeysSrc.includes('LEARNING_STORAGE_KEYS')) {
	checks.push({ name: 'LEARNING_STORAGE_KEYS includes NOTEBOOK', ok: false, detail: 'storage-keys.js' });
} else {
	checks.push({ name: 'logout clears notebook cache', ok: true, detail: 'logoutUser + setNotebookList([])' });
}

assertIncludes('350019 error code', 'common/login-error.js', [
	'350019',
	'PROTOCOL_NOT_AGREED'
]);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Login rules verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'login rule checks passed (23.1)');
checks.forEach((c) => console.log('  ✓', c.name));
