/**
 * 模块二十三 · 错题本规则静态与边界校验（文档 23.5）
 * 运行：node scripts/verify-notebook-rules.mjs
 */
import fs from 'fs';
import path from 'path';
import {
	NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT,
	NOTEBOOK_STATUS_HIGH_FREQ,
	NOTEBOOK_STATUS_MASTERED,
	NOTEBOOK_STATUS_PENDING,
	filterNotebookByHighFreq,
	filterNotebookBySubject,
	filterNotebookForWrongMode,
	getNotebookItemKey,
	getNotebookItemSubjectName,
	getNotebookItemStatusClass,
	getNotebookItemStatusText,
	inferNotebookSubjectFromQuestionCode,
	isNotebookHighFreqItem,
	isNotebookMasteredItem,
	notebookSubjectMatchesChip,
	resolveClearNotebookFilteredList,
	resolveNotebookItemSubject,
	resolveNotebookItemsForWrongPractice
} from '../common/notebook-rules.js';

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

// --- 静态引用校验 ---

assertIncludes('notebook-rules module doc 23.5', 'common/notebook-rules.js', [
	'文档 23.5',
	'NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT',
	'filterNotebookForWrongMode',
	'resolveClearNotebookFilteredList',
	'resolveNotebookItemsForWrongPractice'
]);
assertIncludes('impl.js uses notebook-rules', 'common/app-data/impl.js', [
	"from '@/common/notebook-rules'",
	'resolveNotebookItemsForWrongPractice',
	'resolveClearNotebookFilteredList'
]);
assertIncludes('question-list wrong source', 'common/question-list.js', [
	"from '@/common/notebook-rules'",
	'resolveNotebookItemsForWrongPractice'
]);
assertIncludes('notebook.vue clear filtered only', 'pages/notebook/notebook.vue', [
	'clearNotebookFilteredItems',
	'清空当前筛选',
	'未在当前筛选中的错题将保留'
]);
assertIncludes('notebook-card status from rules', 'components/notebook-card/notebook-card.vue', [
	'getNotebookItemStatusText',
	'getNotebookItemStatusClass'
]);

// --- 边界用例：文档 23.5 ---

const sampleList = [
	{ id: 'q1', question: '数学A', wrongCount: 2, mastered: false },
	{ id: 'q2', question: '英语B', wrongCount: 1, mastered: false },
	{ id: 'q3', question: '物理C', wrongCount: 3, mastered: true },
	{ id: 'q4', question: '化学D', wrongCount: 2, mastered: false }
];

assert('threshold constant is 2', NOTEBOOK_HIGH_FREQ_MIN_WRONG_COUNT === 2);

assert(
	'high freq: wrongCount=2 unmastered',
	isNotebookHighFreqItem({ wrongCount: 2, mastered: false }) === true
);
assert(
	'high freq: wrongCount=1 not high',
	isNotebookHighFreqItem({ wrongCount: 1, mastered: false }) === false
);
assert(
	'high freq: mastered excluded even if wrongCount>=2',
	isNotebookHighFreqItem({ wrongCount: 5, mastered: true }) === false
);

const highFreqItems = filterNotebookByHighFreq(sampleList);
assert(
	'filter high freq returns q1 and q4 only',
	highFreqItems.length === 2
		&& highFreqItems.every((item) => isNotebookHighFreqItem(item))
		&& !highFreqItems.some((item) => item.id === 'q2' || item.id === 'q3')
);

assert(
	'status text: mastered',
	getNotebookItemStatusText({ mastered: true, wrongCount: 5 }) === NOTEBOOK_STATUS_MASTERED
);
assert(
	'status text: high freq',
	getNotebookItemStatusText({ mastered: false, wrongCount: 2 }) === NOTEBOOK_STATUS_HIGH_FREQ
);
assert(
	'status text: pending',
	getNotebookItemStatusText({ mastered: false, wrongCount: 1 }) === NOTEBOOK_STATUS_PENDING
);
assert(
	'status class: high',
	getNotebookItemStatusClass({ mastered: false, wrongCount: 3 }) === 'high'
);

assert(
	'wrong mode excludes mastered',
	filterNotebookForWrongMode(sampleList).length === 3
		&& filterNotebookForWrongMode(sampleList).every((item) => !isNotebookMasteredItem(item))
);

const chipFilter = (chip, list) => (
	chip === '高频错题' ? filterNotebookByHighFreq(list) : list
);
const wrongWithChip = resolveNotebookItemsForWrongPractice(sampleList, {
	filterChip: '高频错题',
	applyChipFilter: chipFilter
});
assert(
	'wrong practice with chip still excludes mastered',
	wrongWithChip.length === 2 && wrongWithChip.every((item) => !item.mastered)
);

const wrongAll = resolveNotebookItemsForWrongPractice(sampleList, {});
assert(
	'wrong practice default excludes mastered',
	wrongAll.length === 3 && !wrongAll.some((item) => item.id === 'q3')
);

const cleared = resolveClearNotebookFilteredList(sampleList, ['q1', 'q4']);
assert(
	'clear filtered removes only matched keys',
	cleared.length === 2
		&& cleared.some((item) => item.id === 'q2')
		&& cleared.some((item) => item.id === 'q3')
		&& !cleared.some((item) => item.id === 'q1' || item.id === 'q4')
);
assert(
	'clear empty keys keeps list',
	resolveClearNotebookFilteredList(sampleList, []).length === sampleList.length
);

assert(
	'item key prefers id',
	getNotebookItemKey({ id: 'abc', question: '题干' }) === 'abc'
);
assert(
	'item key fallback question',
	getNotebookItemKey({ question: '题干' }) === '题干'
);

const subjectList = [
	{ id: 'm1', question: '有理数', subject: '数学 · 初一' },
	{ id: 'm2', question: '有理数2', subject: '数学' },
	{ id: 'm3', question: '英语题', subject: '英语 · 初二' },
	{ id: 'm4', question: '章节题', subject: '章节练习' }
];

assert(
	'subject name from 数学 · 初一',
	getNotebookItemSubjectName('数学 · 初一') === '数学'
);
assert(
	'subject filter matches 数学 · 初一',
	notebookSubjectMatchesChip({ subject: '数学 · 初一' }, '数学')
);
assert(
	'subject filter matches plain 数学',
	notebookSubjectMatchesChip({ subject: '数学' }, '数学')
);
assert(
	'subject filter excludes 英语',
	!notebookSubjectMatchesChip({ subject: '英语 · 初二' }, '数学')
);
assert(
	'subject filter list returns math only',
	filterNotebookBySubject(subjectList, '数学').length === 2
		&& filterNotebookBySubject(subjectList, '数学').every((item) => notebookSubjectMatchesChip(item, '数学'))
);
assert(
	'generic 章节练习 does not match 数学 chip',
	!notebookSubjectMatchesChip({ subject: '章节练习' }, '数学')
);
assert(
	'generic 章节练习 infers math from questionCode',
	notebookSubjectMatchesChip({ subject: '章节练习', questionCode: 'rational-numbers-q02' }, '数学')
);
assert(
	'infer questionCode rational-numbers',
	inferNotebookSubjectFromQuestionCode('rational-numbers-q02') === '数学'
);
assert(
	'resolveNotebookItemSubject from generic + code',
	resolveNotebookItemSubject({ subject: '章节练习', id: 'rational-numbers-q04' }) === '数学'
);
assert(
	'subject filter includes generic math by questionCode',
	filterNotebookBySubject([
		{ id: 'm1', subject: '数学 · 初一' },
		{ id: 'm5', subject: '章节练习', questionCode: 'rational-numbers-q06' },
		{ id: 'm3', subject: '英语 · 初二' }
	], '数学').length === 2
);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Notebook rules verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'notebook rule checks passed (23.5)');
checks.forEach((c) => console.log('  ✓', c.name));
