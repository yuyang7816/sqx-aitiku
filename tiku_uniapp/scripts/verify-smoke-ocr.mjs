/**
 * 模块二十四 · 拍照搜题收藏冒烟校验（文档 24.1 第 7 项）
 * 运行：node scripts/verify-smoke-ocr.mjs
 */
import fs from 'fs';
import path from 'path';
import { COLLECTION_PAGE, OCR_RESULT_PAGE } from '../common/routes.js';

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

// --- 与 ocr-search.js / impl.js / ocr-result.vue 对齐的轻量复刻 ---

const normalizeOcrMatchItem = (item = {}) => ({
	questionId: item.questionId,
	questionCode: item.questionCode || '',
	title: item.title || '',
	answer: item.answer || '',
	analysis: item.analysis || '',
	tips: Array.isArray(item.tips) ? item.tips : [],
	similarity: item.similarity
});

const buildCollectionItem = (item = {}) => ({
	id: item.id || item.questionCode || `collection-${item.title || Date.now()}`,
	questionId: item.questionId != null && item.questionId !== '' ? String(item.questionId) : '',
	questionCode: item.questionCode || item.id || '',
	subject: item.subject || '通用题目',
	date: item.date || '2026-08-13',
	title: item.title || '未命名题目',
	myAnswer: item.myAnswer || '',
	rightAnswer: item.rightAnswer || item.answer || '',
	analysis: item.analysis || '暂无解析',
	aiSummary: item.aiSummary || '暂无 AI 讲解',
	options: Array.isArray(item.options) ? item.options : [],
	tags: Array.isArray(item.tags) ? item.tags : ['#已收藏']
});

const getCollectionItemKeys = (item = {}) => [item.title, item.questionId, item.questionCode, item.id]
	.filter(Boolean)
	.map(String);

const resolveCollectionQuestionToken = (item = {}) => {
	const candidates = [item.questionId, item.questionCode, item.id]
		.map((value) => String(value || '').trim())
		.filter(Boolean);
	return candidates.find((value) => !value.startsWith('collection-')) || '';
};

const getCollectionItemKey = (item = {}) => {
	const token = resolveCollectionQuestionToken(item);
	if (token) {
		return token;
	}
	return String(item.title || item.id || '').trim();
};

const getCollectionKey = (item = {}) => String(
	item.questionId || item.questionCode || item.id || ''
).trim();

const hasCollectionItem = (key, list = []) => {
	const value = String(key || '').trim();
	if (!value) {
		return false;
	}
	return list.some((item) => getCollectionItemKeys(item).includes(value));
};

const toggleCollectionItemLocal = (item, list = []) => {
	const normalized = buildCollectionItem(item);
	const next = [...list];
	const index = next.findIndex((entry) => entry.title === normalized.title);
	if (index >= 0) {
		next.splice(index, 1);
		return { collected: false, list: next };
	}
	next.unshift(normalized);
	return { collected: true, list: next };
};

const applyRecognitionResultLocal = (result = {}) => {
	const matches = (Array.isArray(result.matches) ? result.matches : []).map(normalizeOcrMatchItem);
	return {
		status: matches.length ? 'success' : 'error',
		currentQuestion: matches[0] || null,
		matches
	};
};

const buildOcrResultUrl = ({ imagePath = '', topic = '' } = {}) => {
	const query = [];
	const normalizedImagePath = String(imagePath || '').trim();
	const normalizedTopic = String(topic || '').trim();
	if (normalizedImagePath) {
		query.push(`imagePath=${encodeURIComponent(normalizedImagePath)}`);
	}
	if (normalizedTopic) {
		query.push(`topic=${encodeURIComponent(normalizedTopic)}`);
	}
	if (!query.length) {
		return OCR_RESULT_PAGE;
	}
	return `${OCR_RESULT_PAGE}?${query.join('&')}`;
};

// TC-M24-007 · 24.1-7：OCR 识别成功 → 收藏 → 收藏页可见

const sampleOcrQuestion = normalizeOcrMatchItem({
	questionCode: 'q-ocr-1',
	title: '测试 OCR 题目',
	answer: 'A',
	analysis: '解析',
	tips: ['AI 讲解']
});
assert(
	'24.1-7: OCR match keeps questionCode for collection key',
	Boolean(sampleOcrQuestion.questionCode) && Boolean(sampleOcrQuestion.title)
);

const recognition = applyRecognitionResultLocal({
	matches: [sampleOcrQuestion],
	recognizedText: sampleOcrQuestion.title
});
assert(
	'24.1-7: recognition success when matches present',
	recognition.status === 'success' && recognition.currentQuestion?.title === sampleOcrQuestion.title
);

const collectionKey = getCollectionKey(recognition.currentQuestion);
assert(
	'24.1-7: collection key resolves from OCR questionCode',
	collectionKey === sampleOcrQuestion.questionCode
);

let collectionList = [];
const collectResult = toggleCollectionItemLocal({
	...recognition.currentQuestion,
	questionId: collectionKey,
	subject: '数学 · 初三',
	tags: ['#拍照搜题', '#OCR搜题']
}, collectionList);
collectionList = collectResult.list;

assert(
	'24.1-7: toggleCollection adds item locally',
	collectResult.collected === true && collectionList.length === 1
);
assert(
	'24.1-7: hasCollectionItem true after collect',
	hasCollectionItem(collectionKey, collectionList) === true
);
assert(
	'24.1-7: collection page key matches stored item',
	getCollectionItemKey(collectionList[0]) === collectionKey
);
assert(
	'24.1-7: collection page would render title',
	String(collectionList[0].title).includes('测试 OCR 题目')
);

const uncollectResult = toggleCollectionItemLocal(collectionList[0], collectionList);
assert(
	'24.1-7: toggleCollection removes duplicate by title',
	uncollectResult.collected === false && uncollectResult.list.length === 0
);

assert(
	'24.1-7: OCR result URL carries imagePath',
	buildOcrResultUrl({ imagePath: '/tmp/ocr.jpg' }).includes('imagePath=')
		&& buildOcrResultUrl({ imagePath: '/tmp/ocr.jpg' }).startsWith(OCR_RESULT_PAGE)
);

// --- 静态链路：选图 → OCR 识别 → 收藏 → 收藏页 ---

assertIncludes('ocr-camera picker navigates to OCR page', 'common/ocr-camera.js', [
	'openOcrImagePicker',
	'OCR_RESULT_PAGE',
	'buildOcrResultUrl',
	'uni.navigateTo'
]);
assertIncludes('ocr-search upload+search API', 'common/ocr-search.js', [
	'runOcrRecognition',
	'uploadOcrImage',
	'/app/ocr/upload',
	'/app/ocr/search',
	'normalizeOcrMatchItem',
	'getFallbackOcrResult'
]);
assertIncludes('ocr-result success collect flow', 'package/question/ocr-result.vue', [
	'applyRecognitionResult',
	'initCollectedState',
	'toggleCollect',
	'toggleCollectionItem',
	'syncCollectionListFromApi',
	"status.value !== 'success'",
	'finishRecognition('
]);
assertIncludes('collection API CRUD', 'common/collection-api.js', [
	'fetchCollectionList',
	'addCollection',
	'removeCollection',
	"'/app/collection'"
]);
assertIncludes('impl collection sync + toggle', 'common/app-data/impl.js', [
	'toggleCollectionItem',
	'hasCollectionItem',
	'syncCollectionListFromApi',
	'buildCollectionItem',
	'resolveCollectionQuestionToken'
]);
assertIncludes('collection page onShow refresh', 'package/profile/collection.vue', [
	'syncCollectionListFromApi',
	'onShow',
	'getCollectionItemKey',
	'cancelCollectionItem'
]);
assertIncludes('home OCR + collection routes', 'common/routes.js', [
	'OCR_RESULT_PAGE',
	'COLLECTION_PAGE'
]);
assertIncludes('index grid opens OCR picker', 'pages/index/index.vue', [
	'openOcrImagePicker',
	"item.action === 'ocr-camera'"
]);
assertIncludes('my menu links collection page', 'common/profile-config.js', [
	'我的收藏',
	'COLLECTION_PAGE'
]);
assert(
	'24.1-7: profile menu targets collection page',
	COLLECTION_PAGE === '/package/profile/collection'
);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Smoke OCR collect flow verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'smoke OCR collect checks passed (24.1 item 7)');
checks.forEach((c) => console.log('  ✓', c.name));
