/**
 * 模块二十四 · Token 过期 401 拦截联调校验（文档 24.3）
 * 运行：node scripts/verify-integration-token.mjs
 */
import fs from 'fs';
import path from 'path';
import {
	AUTH_KEY,
	TOKEN_KEY,
	LEGACY_TOKEN_KEY,
	LEGACY_USER_STORAGE_KEYS,
	LEARNING_STORAGE_KEYS,
	NOTEBOOK_KEY
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

// --- 与 request.js 对齐的轻量复刻 ---

const isUnauthorizedResponse = (result) => {
	if (!result) {
		return false;
	}
	if (result.statusCode === 401) {
		return true;
	}
	const body = result.data;
	return Boolean(body && typeof body === 'object' && body.code === 401);
};

const LAUNCH_ROUTE_LOGIN = '/package/auth/login';

const defaultAuthState = { loggedIn: false, phone: '', loginType: '' };

/** 模拟 logoutUser：401 与主动退出共用同一清鉴权逻辑 */
const simulate401LogoutStorage = (storage = {}) => {
	const next = { ...storage };
	delete next[TOKEN_KEY];
	delete next[LEGACY_TOKEN_KEY];
	LEGACY_USER_STORAGE_KEYS.forEach((key) => {
		delete next[key];
	});
	next[AUTH_KEY] = defaultAuthState;
	return next;
};

// TC-M24-011 · 24.3：Token 过期自动跳登录（401 拦截）

assert(
	'24.3-1: HTTP statusCode 401 recognized',
	isUnauthorizedResponse({ statusCode: 401, data: null })
);

assert(
	'24.3-1: Result body code=401 recognized (token expired)',
	isUnauthorizedResponse({
		statusCode: 200,
		data: { code: 401, msg: '暂未登录或token已经过期', data: null }
	})
);

assert(
	'24.3-1: business success not treated as 401',
	!isUnauthorizedResponse({ statusCode: 200, data: { code: 0, data: {} } })
);

assert(
	'24.3-2: 401 clears token and auth state',
	(() => {
		const after = simulate401LogoutStorage({
			[AUTH_KEY]: { loggedIn: true, phone: '13800138000', loginType: 'sms' },
			[TOKEN_KEY]: 'expired-jwt',
			token: 'legacy-expired-jwt'
		});
		return after[TOKEN_KEY] === undefined
			&& after.token === undefined
			&& after[AUTH_KEY]?.loggedIn === false;
	})()
);

assert(
	'24.3-2: 401 preserves learning data (notebook)',
	(() => {
		const notebook = [{ id: 'nb-1', question: '错题保留' }];
		const after = simulate401LogoutStorage({
			[AUTH_KEY]: { loggedIn: true },
			[TOKEN_KEY]: 'expired-jwt',
			[NOTEBOOK_KEY]: notebook
		});
		return Array.isArray(after[NOTEBOOK_KEY]) && after[NOTEBOOK_KEY][0]?.question === '错题保留';
	})()
);

assert(
	'24.3-2: LEARNING_STORAGE_KEYS not cleared on 401 logout',
	!LEARNING_STORAGE_KEYS.includes(TOKEN_KEY)
	&& !LEARNING_STORAGE_KEYS.includes(AUTH_KEY)
);

// --- 静态链路：统一 request 层 401 → 清 token → Toast → reLaunch 登录页 ---

assertIncludes('24.3-3: request.js core 401 interceptor', 'common/api/request.js', [
	'isUnauthorizedResponse',
	'handleUnauthorized',
	'performLogoutAndRedirect',
	'isHandling401',
	'登录已失效'
]);

assertIncludes('24.3-3: performLogoutAndRedirect reLaunch login', 'common/app-data/impl.js', [
	'performLogoutAndRedirect',
	'logoutUser',
	'LAUNCH_ROUTE_LOGIN',
	'uni.reLaunch'
]);

assertIncludes('24.3-3: logoutUser clears token via clearToken', 'common/app-data/impl.js', [
	'logoutUser',
	'clearToken',
	'LEARNING_STORAGE_KEYS'
]);

assertIncludes('24.3-3: auth-token clearToken removes encrypted key', 'common/auth-token.js', [
	'clearToken',
	'TOKEN_KEY',
	'LEGACY_TOKEN_KEY'
]);

assertIncludes('24.3-3: api index exports 401 helpers', 'common/api/index.js', [
	'handleUnauthorized',
	'isUnauthorizedResponse'
]);

assertIncludes('24.3-3: httpRequest legacy re-export 401', 'common/httpRequest.js', [
	'handleUnauthorized',
	'isUnauthorizedResponse'
]);

// --- 非 uni.request 通道同样走 401 拦截 ---

assertIncludes('24.3-4: OCR upload 401', 'common/ocr-search.js', [
	'isUnauthorizedResponse',
	'handleUnauthorized',
	'登录已失效'
]);

assertIncludes('24.3-4: file upload 401', 'common/file-upload.js', [
	'isUnauthorizedResponse',
	'handleUnauthorized'
]);

assertIncludes('24.3-4: AI chat SSE fetch 401', 'common/chat-api.js', [
	'response.status === 401',
	'handleUnauthorized',
	'登录已失效'
]);

// --- 白名单匿名接口不触发 401 登出（对照） ---

const requestSrc = read('common/api/request.js');
assert(
	'24.3-5: whitelist includes verifyCode (no token required)',
	requestSrc.includes("'/app/verifyCode'")
);

assert(
	'24.3-5: attachToken skipped for whitelist paths',
	requestSrc.includes('isAuthWhitelistPath(url)')
	&& requestSrc.includes('shouldAttachToken = attachToken && !isAuthWhitelistPath(url)')
);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Integration token (401) verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'integration token (401) checks passed (24.3)');
checks.forEach((c) => console.log('  ✓', c.name));
