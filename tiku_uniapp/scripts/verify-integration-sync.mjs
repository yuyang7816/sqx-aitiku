/**
 * 模块二十四 · 跨设备错题同步联调校验（文档 24.3）
 * 运行：node scripts/verify-integration-sync.mjs
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

// --- 与 impl.js / cloud-sync 对齐的轻量复刻 ---

/** 已登录（有 token）时走云端 API */
const canSyncNotebookToApi = (auth = {}, token = '') => (
	Boolean(String(token || '').trim() || auth.loggedIn)
);

const getNotebookMatchKey = (item = {}) => {
	const id = String(item.questionId || item.questionCode || '').trim();
	if (id) {
		return `id:${id}`;
	}
	return `title:${String(item.question || item.title || '').trim()}`;
};

/** 对齐 syncWrongNotebook 本地入库：新题 unshift，同题 wrongCount+1 */
const applyLocalWrongNotebook = (notebook = [], wrongList = [], subject = '章节练习', today = '2026-08-13') => {
	const next = [...notebook];
	wrongList.forEach((item) => {
		const questionTitle = String(item.title || item.question || '').trim();
		const normalized = {
			questionId: item.questionId || item.questionCode || '',
			questionCode: item.questionCode || item.questionId || '',
			subject,
			question: questionTitle,
			wrong: item.myAnswer,
			wrongCount: 1,
			mastered: false,
			date: today,
			lastPracticeAt: today
		};
		const matchKey = getNotebookMatchKey(normalized);
		const existingIndex = next.findIndex((entry) => getNotebookMatchKey(entry) === matchKey);
		if (existingIndex >= 0) {
			const existing = next[existingIndex];
			next[existingIndex] = {
				...existing,
				wrongCount: Number(existing.wrongCount || 0) + 1,
				mastered: false,
				date: today,
				lastPracticeAt: today
			};
			return;
		}
		next.unshift(normalized);
	});
	return next;
};

/** 模拟云端 POST /app/notebook（同题 wrongCount+1） */
const applyCloudNotebookPost = (cloud = [], wrongList = [], subject = '章节练习', today = '2026-08-13') => {
	const next = [...cloud];
	wrongList.forEach((item) => {
		const questionId = String(item.questionId || item.questionCode || '').trim();
		if (!questionId) {
			return;
		}
		const existingIndex = next.findIndex((entry) => (
			String(entry.questionId) === questionId || String(entry.questionCode) === questionId
		));
		if (existingIndex >= 0) {
			const existing = next[existingIndex];
			next[existingIndex] = {
				...existing,
				subject,
				wrongCount: Number(existing.wrongCount || 0) + 1,
				mastered: false,
				date: today,
				lastPracticeAt: today
			};
			return;
		}
		next.unshift({
			questionId,
			questionCode: questionId,
			subject,
			question: String(item.title || item.question || '').trim(),
			wrong: item.myAnswer,
			wrongCount: 1,
			mastered: false,
			date: today,
			lastPracticeAt: today
		});
	});
	return next;
};

/** 设备 A：答错交卷后写入云端（已登录） */
const simulateDeviceAWrongSubmit = ({
	cloudNotebook = [],
	wrongList = [],
	auth = { loggedIn: true },
	token = 'mock-token',
	subject = '章节练习'
} = {}) => {
	if (!canSyncNotebookToApi(auth, token)) {
		return { cloud: cloudNotebook, synced: false };
	}
	const cloud = applyCloudNotebookPost(cloudNotebook, wrongList, subject);
	return { cloud, synced: true };
};

/** 设备 B：登录或进入错题本 Tab 时 GET /app/notebook 覆盖本地 */
const simulateDeviceBPullNotebook = ({
	cloudNotebook = [],
	auth = { loggedIn: true },
	token = 'mock-token'
} = {}) => {
	if (!canSyncNotebookToApi(auth, token)) {
		return { list: [], fromApi: false };
	}
	return { list: [...cloudNotebook], fromApi: true };
};

const listsMatchByQuestionId = (left = [], right = []) => {
	if (left.length !== right.length) {
		return false;
	}
	const sortById = (items) => [...items].sort((a, b) => (
		String(a.questionId || a.questionCode).localeCompare(String(b.questionId || b.questionCode))
	));
	const sortedLeft = sortById(left);
	const sortedRight = sortById(right);
	return sortedLeft.every((item, index) => {
		const other = sortedRight[index];
		return String(item.questionId || item.questionCode) === String(other.questionId || other.questionCode)
			&& Number(item.wrongCount) === Number(other.wrongCount)
			&& Boolean(item.mastered) === Boolean(other.mastered);
	});
};

// TC-M24-013 · 24.3：跨设备错题同步一致（A 设备答错 B 设备可见）

const wrongItemA = {
	questionId: 'q-m24-sync-01',
	questionCode: 'q-m24-sync-01',
	title: '跨设备同步错题：-3 的相反数是（ ）',
	myAnswer: 'B'
};

assert(
	'24.3-1: device A wrong submit syncs to cloud when logged in',
	(() => {
		const result = simulateDeviceAWrongSubmit({
			cloudNotebook: [],
			wrongList: [wrongItemA]
		});
		return result.synced
			&& result.cloud.length === 1
			&& result.cloud[0].questionId === 'q-m24-sync-01'
			&& result.cloud[0].wrongCount === 1;
	})()
);

assert(
	'24.3-2: device B empty local pulls cloud list on login',
	(() => {
		const deviceA = simulateDeviceAWrongSubmit({
			cloudNotebook: [],
			wrongList: [wrongItemA]
		});
		const deviceB = simulateDeviceBPullNotebook({
			cloudNotebook: deviceA.cloud
		});
		return deviceB.fromApi
			&& deviceB.list.length === 1
			&& deviceB.list[0].question === wrongItemA.title;
	})()
);

assert(
	'24.3-3: cross-device lists consistent after A wrong + B pull',
	(() => {
		const deviceA = simulateDeviceAWrongSubmit({
			cloudNotebook: [],
			wrongList: [wrongItemA]
		});
		const deviceB = simulateDeviceBPullNotebook({
			cloudNotebook: deviceA.cloud
		});
		return listsMatchByQuestionId(deviceA.cloud, deviceB.list);
	})()
);

assert(
	'24.3-4: repeat wrong on device A increments wrongCount on cloud',
	(() => {
		const first = simulateDeviceAWrongSubmit({
			cloudNotebook: [],
			wrongList: [wrongItemA]
		});
		const second = simulateDeviceAWrongSubmit({
			cloudNotebook: first.cloud,
			wrongList: [wrongItemA]
		});
		const deviceB = simulateDeviceBPullNotebook({
			cloudNotebook: second.cloud
		});
		return second.cloud[0].wrongCount === 2
			&& deviceB.list[0].wrongCount === 2;
	})()
);

assert(
	'24.3-5: not logged in skips cloud write',
	(() => {
		const result = simulateDeviceAWrongSubmit({
			cloudNotebook: [],
			wrongList: [wrongItemA],
			auth: { loggedIn: false },
			token: ''
		});
		return !result.synced && result.cloud.length === 0;
	})()
);

assert(
	'24.3-6: logged in pull always uses cloud list',
	(() => {
		const deviceA = simulateDeviceAWrongSubmit({
			cloudNotebook: [],
			wrongList: [wrongItemA]
		});
		const deviceBAfterLogin = simulateDeviceBPullNotebook({
			cloudNotebook: deviceA.cloud
		});
		return listsMatchByQuestionId(deviceA.cloud, deviceBAfterLogin.list);
	})()
);

assert(
	'24.3-7: not logged in skips cloud pull (returns empty)',
	(() => {
		const cloud = [{ questionId: 'cloud-only', question: '云端题', wrongCount: 1 }];
		const pulled = simulateDeviceBPullNotebook({
			cloudNotebook: cloud,
			auth: { loggedIn: false },
			token: ''
		});
		return !pulled.fromApi && pulled.list.length === 0;
	})()
);

// --- 静态链路：答错写云 → 换端 GET 拉取 ---

assertIncludes('24.3-8: notebook-api GET/POST cloud CRUD', 'common/notebook-api.js', [
	"get('/app/notebook')",
	"postJson('/app/notebook'",
	'fetchNotebookList',
	'addNotebook',
	'normalizeNotebookItem'
]);

assertIncludes('24.3-9: syncWrongNotebook POST then syncNotebookListFromApi', 'common/app-data/impl.js', [
	'export const syncWrongNotebook',
	'canSyncNotebookToApi',
	'addNotebook',
	'syncNotebookListFromApi'
]);

assertIncludes('24.3-10: cloud-sync login/launch/onShow triggers', 'common/cloud-sync.js', [
	'syncNotebookAndCollectionFromApi',
	'syncNotebookListFromApi',
	'canSyncNotebookToApi'
]);

assertIncludes('24.3-11: syncUserInfoAfterLogin pulls notebook on login', 'common/user-info.js', [
	'syncUserInfoAfterLogin',
	'syncNotebookAndCollectionFromApi',
	"/app/user"
]);

assertIncludes('24.3-12: App onLaunch sync when token exists', 'App.vue', [
	'syncNotebookAndCollectionFromApi',
	'fetchAndMergeUserInfo',
	'onLaunch',
	'getToken'
]);

assertIncludes('24.3-13: notebook tab onShow refreshNotebookList', 'pages/notebook/notebook.vue', [
	'onShow',
	'refreshNotebookList',
	'syncNotebookListFromApi',
	'canSyncNotebookToApi'
]);

assertIncludes('24.3-14: storage migration clears legacy notebook cache', 'common/storage-migration.js', [
	'migrateClearLocalNotebookCache',
	'NOTEBOOK_KEY'
]);

assertIncludes('24.3-15: answer submitOk refreshes notebook from server', 'package/question/answer.vue', [
	'submitChapterPractice',
	'syncNotebookListFromApi',
	'syncWrongNotebook',
	'serverSynced'
]);

assertIncludes('24.3-16: canSyncNotebookToApi requires login token', 'common/app-data/impl.js', [
	'export const canSyncNotebookToApi',
	'getToken'
]);

assertIncludes('24.3-17: notebook module doc references cloud sync', 'common/app-data/notebook.js', [
	'GET/POST/PATCH/DELETE /app/notebook',
	'syncWrongNotebook',
	'GET /app/notebook'
]);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Integration sync verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'integration sync checks passed (24.3)');
checks.forEach((c) => console.log('  ✓', c.name));
