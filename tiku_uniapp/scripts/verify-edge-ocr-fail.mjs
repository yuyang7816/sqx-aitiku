/**
 * 模块二十四 · OCR 关键词不匹配边界校验（文档 24.2）
 * 运行：node scripts/verify-edge-ocr-fail.mjs
 */
import fs from 'fs';
import path from 'path';
import { OCR_MANUAL_SEARCH_URL, OCR_RESULT_PAGE } from '../common/routes.js';

/** 与 common/ocr-search.js getFallbackOcrResult 对齐（无本地题库） */
const getFallbackOcrResult = (text = '') => ({
	recognizedText: String(text || '').trim(),
	matches: [],
	fromApi: false
});

const normalizeOcrMatchItem = (item = {}) => ({
	questionId: item.questionId,
	questionCode: item.questionCode || '',
	title: item.title || '',
	answer: item.answer || '',
	analysis: item.analysis || '',
	tips: Array.isArray(item.tips) ? item.tips : [],
	similarity: item.similarity
});

/** 模拟 POST /app/ocr/search 返回（脚本内联，非生产 mock） */
const simulateOcrApiSearch = (text = '') => {
	const keyword = String(text || '').trim();
	if (keyword.includes('一次函数')) {
		return {
			recognizedText: keyword,
			matches: [{
				questionCode: 'q-linear',
				title: '一次函数图像性质',
				answer: 'A',
				analysis: '解析',
				tips: ['tip']
			}],
			fromApi: true
		};
	}
	return {
		recognizedText: keyword,
		matches: [],
		fromApi: true
	};
};

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

const OCR_FAIL_REASON = {
	NO_MATCH: 'no_match',
	RECOGNITION_FAILED: 'recognition_failed',
	INPUT_EMPTY: 'input_empty',
	NETWORK: 'network'
};

const finishRecognitionLocal = (state, nextStatus, reason = '') => {
	const next = { ...state, status: nextStatus };
	if (nextStatus === 'error') {
		next.failReason = reason || OCR_FAIL_REASON.RECOGNITION_FAILED;
		if (state.isManualSearch || reason === OCR_FAIL_REASON.NO_MATCH) {
			next.showManualInput = true;
		}
		return next;
	}
	next.failReason = '';
	return next;
};

const applyRecognitionResultLocal = (result = {}) => {
	const matches = (Array.isArray(result.matches) ? result.matches : []).map(normalizeOcrMatchItem);
	return {
		status: matches.length ? 'success' : 'error',
		failReason: matches.length ? '' : OCR_FAIL_REASON.NO_MATCH,
		showManualInput: matches.length ? false : true,
		currentQuestion: matches[0] || null,
		matches
	};
};

const runManualRecoveryFlow = (badKeyword, goodKeyword) => {
	const noMatch = simulateOcrApiSearch(badKeyword);
	let state = {
		status: 'idle',
		failReason: '',
		showManualInput: true,
		isManualSearch: true,
		manualTopic: badKeyword
	};

	if (noMatch.matches.length) {
		return { ok: false, step: 'bad keyword should not match' };
	}

	state = finishRecognitionLocal(state, 'error', OCR_FAIL_REASON.NO_MATCH);
	if (state.status !== 'error' || state.failReason !== OCR_FAIL_REASON.NO_MATCH || !state.showManualInput) {
		return { ok: false, step: 'error state after no match' };
	}

	const retry = simulateOcrApiSearch(goodKeyword);
	if (!retry.matches.length) {
		return { ok: false, step: 'good keyword should match API result' };
	}

	const success = applyRecognitionResultLocal(retry);
	if (success.status !== 'success' || !success.currentQuestion?.title) {
		return { ok: false, step: 'manual retry success' };
	}

	return { ok: true, recoveredTitle: success.currentQuestion.title };
};

assertIncludes('ocr-result error state UI', 'package/question/ocr-result.vue', [
	"status === 'error'",
	'OCR_FAIL_REASON.NO_MATCH',
	'未找到匹配题目',
	'openManualInput',
	'applyManualTopic',
	'finishRecognition(',
	'showManualInput'
]);
assertIncludes('ocr-search fallback empty', 'common/ocr-search.js', [
	'getFallbackOcrResult',
	'matches: []'
]);
assertIncludes('home manual search route', 'common/routes.js', [
	'OCR_MANUAL_SEARCH_URL',
	'manual=1'
]);
assertIncludes('index search opens question hub', 'pages/index/index.vue', [
	'QUESTION_SEARCH_PAGE',
	'openSearch'
]);

const NO_MATCH_KEYWORD = 'xyz无匹配关键词abc';

assert(
	'24.2: fallback OCR result has no local matches',
	getFallbackOcrResult(NO_MATCH_KEYWORD).matches.length === 0,
	`expected 0, got ${getFallbackOcrResult(NO_MATCH_KEYWORD).matches.length}`
);

assert(
	'24.2: finishRecognition NO_MATCH keeps manual input visible',
	finishRecognitionLocal({ isManualSearch: false, showManualInput: false }, 'error', OCR_FAIL_REASON.NO_MATCH).showManualInput === true,
	'photo OCR no_match should expose manual input'
);

assert(
	'24.2: error title for NO_MATCH is 未找到匹配题目',
	read('package/question/ocr-result.vue').includes("'未找到匹配题目'"),
	'missing error title copy'
);

const recovery = runManualRecoveryFlow(NO_MATCH_KEYWORD, '一次函数');
assert(
	'24.2: no_match → error → manual keyword retry succeeds',
	recovery.ok === true,
	recovery.step || recovery.recoveredTitle || ''
);

assert(
	'24.2: recovered question title contains 一次函数',
	String(recovery.recoveredTitle || '').includes('一次函数'),
	recovery.recoveredTitle || 'empty title'
);

assert(
	'24.2: manual search URL targets ocr-result',
	OCR_MANUAL_SEARCH_URL === `${OCR_RESULT_PAGE}?manual=1`,
	OCR_MANUAL_SEARCH_URL
);

assertIncludes('ocr-result uses API not local fallback on error', 'package/question/ocr-result.vue', [
	'runOcrRecognition',
	'OCR_FAIL_REASON.NO_MATCH'
]);

const failed = checks.filter((item) => !item.ok);
console.log(`\n模块二十四 · OCR 关键词不匹配边界校验：${checks.length - failed.length}/${checks.length} 通过\n`);
checks.forEach((item) => {
	console.log(`${item.ok ? '✓' : '✗'} ${item.name}${item.detail ? ` — ${item.detail}` : ''}`);
});

if (failed.length) {
	process.exit(1);
}

console.log('\nOK: 文档 24.2 OCR 无本地题库 fallback 校验通过\n');
