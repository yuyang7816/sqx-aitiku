/**
 * 模块二十四 · 空错题本 wrong 模式边界校验（文档 24.2）
 * 运行：node scripts/verify-edge-empty-wrong.mjs
 */
import fs from 'fs';
import path from 'path';
import { getDefaultQuestionBankPracticeList } from '../common/practice-fallback.js';
import { NOTEBOOK_STATUS_MASTERED } from '../common/notebook-rules.js';

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

assertIncludes('practice-fallback module', 'common/practice-fallback.js', [
	'getDefaultQuestionBankPracticeList',
	'返回空'
]);
assertIncludes('question-list wrong fallback', 'common/question-list.js', [
	'resolveWrongModeQuestions',
	'getDefaultQuestionBankPracticeList'
]);
assertIncludes('impl.js wrong fallback', 'common/app-data/impl.js', [
	'getDefaultQuestionBankPracticeList',
	'wrongQuestions.length ? wrongQuestions : getDefaultQuestionBankPracticeList()'
]);

const defaultList = getDefaultQuestionBankPracticeList();

assert(
	'default question bank is empty (no mock fallback)',
	defaultList.length === 0,
	`expected 0, got ${defaultList.length}`
);

const mapNotebookItemToPracticeQuestion = (item = {}) => ({
	type: '错题重做',
	difficulty: '复习',
	title: item.question || item.title || '',
	rightAnswer: item.right || item.rightAnswer || '',
	analysis: item.analysis || '',
	options: Array.isArray(item.options) ? item.options : []
});

const resolveNotebookItemsForWrongPractice = (list = [], { batchKeys = [] } = {}) => {
	if (batchKeys.length) {
		return list.filter((item) => batchKeys.includes(item.id || item.question));
	}
	return list.filter((item) => !item.mastered && item.status !== NOTEBOOK_STATUS_MASTERED);
};

const resolveWrongModeQuestions = (notebook = [], source = {}) => {
	const notebookQuestions = resolveNotebookItemsForWrongPractice(notebook, source)
		.map(mapNotebookItemToPracticeQuestion)
		.filter((item) => item.title && item.options.length);
	if (notebookQuestions.length) {
		return notebookQuestions;
	}
	return getDefaultQuestionBankPracticeList();
};

assert(
	'empty notebook wrong mode returns empty list',
	resolveWrongModeQuestions([], { mode: 'wrong' }).length === 0,
	'empty notebook should not inject mock questions'
);

assert(
	'all mastered notebook wrong mode returns empty list',
	resolveWrongModeQuestions([
		{ question: '测试题', mastered: true, options: [{ label: 'A', value: '1' }] }
	], { mode: 'wrong' }).length === 0,
	'all mastered should not inject mock questions'
);

assert(
	'unmastered notebook wrong mode uses notebook items',
	resolveWrongModeQuestions([
		{
			question: '本地错题',
			mastered: false,
			options: [{ label: 'A', value: '1' }, { label: 'B', value: '2' }]
		}
	], { mode: 'wrong' }).length === 1,
	'should prefer notebook over fallback'
);

const failed = checks.filter((item) => !item.ok);
console.log(`\n模块二十四 · 空错题本 wrong 模式边界校验：${checks.length - failed.length}/${checks.length} 通过\n`);
checks.forEach((item) => {
	console.log(`${item.ok ? '✓' : '✗'} ${item.name}${item.detail ? ` — ${item.detail}` : ''}`);
});

if (failed.length) {
	process.exit(1);
}

console.log('\nOK: 文档 24.2 空错题本 wrong 模式无 mock 兜底校验通过\n');
