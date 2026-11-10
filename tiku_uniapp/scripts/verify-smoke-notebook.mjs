/**
 * 模块二十四 · 错题重做掌握冒烟校验（文档 24.1 第 6 项）
 * 运行：node scripts/verify-smoke-notebook.mjs
 */
import fs from 'fs';
import path from 'path';
import { NOTEBOOK_REDO_TITLE } from '../common/routes.js';
import {
	NOTEBOOK_STATUS_MASTERED,
	getNotebookItemStatusClass,
	getNotebookItemStatusText,
	isNotebookMasteredItem
} from '../common/notebook-rules.js';

/** 冒烟用错题 fixture（非生产 mock，仅脚本内联） */
const notebookList = [
	{
		id: 'nb-1',
		question: '测试错题一',
		subject: '数学',
		mastered: false,
		wrongCount: 2,
		wrong: 'B',
		right: 'A',
		options: [{ label: 'A', value: '1' }, { label: 'B', value: '2' }]
	},
	{
		id: 'nb-2',
		question: '测试错题二',
		subject: '物理',
		mastered: true,
		wrongCount: 1,
		wrong: 'C',
		right: 'A',
		options: [{ label: 'A', value: '1' }, { label: 'C', value: '3' }]
	}
];

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

// --- 与 answer.vue / impl.js 对齐的轻量复刻 ---

const normalizeOptionLabel = (value) => String(value || '').trim().toUpperCase();

const isQuestionAnswerCorrect = (userAnswer = '', question = {}) => {
	const rightAnswer = question.rightAnswer || '';
	return normalizeOptionLabel(userAnswer) === normalizeOptionLabel(rightAnswer);
};

const mapNotebookItemToPracticeQuestion = (item = {}) => ({
	type: '错题重做',
	difficulty: '复习',
	title: item.question || item.title || '',
	rightAnswer: item.right || item.rightAnswer || '',
	analysis: item.analysis || '',
	options: Array.isArray(item.options) ? item.options : []
});

const markNotebookMasteredLocal = (questions = [], notebook = []) => {
	const titles = questions.map((item) => item.title || item.question);
	return notebook.map((item) => (
		titles.includes(item.question)
			? { ...item, mastered: true, date: '2026-08-13', lastPracticeAt: '2026-08-13' }
			: item
	));
};

const resolveSyncWrongNotebookSubject = (practiceSource = {}) => {
	const mode = String(practiceSource.mode || '').trim();
	if (mode === 'single') {
		return '错题重做';
	}
	return '章节练习';
};

const syncWrongNotebookLocal = (wrongList = [], practiceSource = {}, notebook = []) => {
	const subject = resolveSyncWrongNotebookSubject(practiceSource);
	const list = [...notebook];
	wrongList.forEach((item) => {
		const questionTitle = String(item.title || item.question || '').trim();
		const existingIndex = list.findIndex((entry) => entry.question === questionTitle);
		if (existingIndex >= 0) {
			const existing = list[existingIndex];
			list[existingIndex] = {
				...existing,
				subject,
				wrong: item.myAnswer,
				wrongCount: Number(existing.wrongCount || 0) + 1,
				mastered: false
			};
			return;
		}
		list.unshift({
			id: `notebook-${questionTitle}`,
			subject,
			question: questionTitle,
			wrong: item.myAnswer,
			right: item.rightAnswer,
			analysis: item.analysis,
			options: item.options,
			wrongCount: 1,
			mastered: false
		});
	});
	return list;
};

const applyLocalNotebookAfterSubmit = async (
	reviewList = [],
	_source = {},
	{ markNotebookMastered, syncWrongNotebook } = {}
) => {
	const wrongList = reviewList.filter((item) => !item.isCorrect);
	if (typeof markNotebookMastered === 'function') {
		await markNotebookMastered(reviewList.filter((item) => item.isCorrect));
	}
	if (typeof syncWrongNotebook === 'function') {
		await syncWrongNotebook(wrongList, _source);
	}
	return wrongList;
};

const buildRedoPracticeSource = (item = {}) => ({
	mode: 'single',
	title: NOTEBOOK_REDO_TITLE,
	questionTitle: String(item.question || '').trim(),
	questionId: String(item.questionId || item.id || '').trim()
});

const resolveSingleQuestionByTitle = (questionTitle = '', notebook = []) => {
	const title = String(questionTitle || '').trim();
	if (!title) {
		return null;
	}
	const notebookItem = notebook.find((entry) => entry.question === title);
	if (notebookItem && Array.isArray(notebookItem.options) && notebookItem.options.length) {
		return mapNotebookItemToPracticeQuestion(notebookItem);
	}
	return null;
};

// --- 文档 24.1 第 6 项：错题本重做 → 答对 → 标记已掌握 ---

assertIncludes('notebook redo button wiring', 'pages/notebook/notebook.vue', [
	'redoItem',
	"mode: 'single'",
	'NOTEBOOK_REDO_TITLE',
	'QUESTION_ANSWER_PAGE',
	'setPracticeSource',
	'questionTitle'
]);
assertIncludes('answer single mode submit back', 'package/question/answer.vue', [
	"practiceSource.value.mode === 'single'",
	'uni.navigateBack',
	'markNotebookMastered',
	'applyLocalNotebookAfterSubmit'
]);
assertIncludes('impl markNotebookMastered mastered flag', 'common/app-data/impl.js', [
	'export const markNotebookMastered',
	'mastered: true',
	'updateNotebookMastered'
]);
assertIncludes('notebook card status display', 'components/notebook-card/notebook-card.vue', [
	'getNotebookItemStatusText',
	'getNotebookItemStatusClass'
]);
assertIncludes('single mode question list', 'common/question-list.js', [
	"mode === 'single'",
	'mapNotebookItemToPracticeQuestion',
	'resolveQuestionIdByNotebookTitle'
]);
assertIncludes('notebook api patch mastered', 'common/notebook-api.js', [
	'updateNotebookMastered',
	'PATCH'
]);

const seedItem = notebookList.find((item) => !item.mastered) || notebookList[0];
assert(
	'24.1-6: mock notebook has unmastered item',
	Boolean(seedItem) && !seedItem.mastered
);

const redoSource = buildRedoPracticeSource(seedItem);
assert(
	'24.1-6: redo practiceSource mode=single title=错题重做',
	redoSource.mode === 'single'
		&& redoSource.title === NOTEBOOK_REDO_TITLE
		&& redoSource.questionTitle === seedItem.question
);

let notebookState = notebookList.map((item) => ({ ...item, mastered: Boolean(item.mastered) }));
const practiceQuestion = resolveSingleQuestionByTitle(redoSource.questionTitle, notebookState);

assert(
	'24.1-6: redo loads exactly 1 question from notebook',
	Boolean(practiceQuestion) && practiceQuestion.type === '错题重做'
);

const correctAnswer = practiceQuestion.rightAnswer;
const reviewListCorrect = [{
	order: 1,
	title: practiceQuestion.title,
	myAnswer: correctAnswer,
	rightAnswer: practiceQuestion.rightAnswer,
	analysis: practiceQuestion.analysis,
	options: practiceQuestion.options,
	isCorrect: isQuestionAnswerCorrect(correctAnswer, practiceQuestion)
}];

assert(
	'24.1-6: correct answer detected for redo question',
	reviewListCorrect.length === 1 && reviewListCorrect[0].isCorrect === true
);

await applyLocalNotebookAfterSubmit(
	reviewListCorrect,
	redoSource,
	{
		markNotebookMastered: async (list) => {
			notebookState = markNotebookMasteredLocal(list, notebookState);
		},
		syncWrongNotebook: async () => {}
	}
);

const masteredItem = notebookState.find((item) => item.question === seedItem.question);
assert(
	'24.1-6: redo correct → mastered=true',
	Boolean(masteredItem) && masteredItem.mastered === true
);
assert(
	'24.1-6: status text shows 已掌握',
	getNotebookItemStatusText(masteredItem) === NOTEBOOK_STATUS_MASTERED
);
assert(
	'24.1-6: status class is mastered',
	getNotebookItemStatusClass(masteredItem) === 'mastered'
);
assert(
	'24.1-6: isNotebookMasteredItem true',
	isNotebookMasteredItem(masteredItem) === true
);

// 重做答错：wrongCount+1 且 mastered 保持 false
let wrongRedoState = notebookList.map((item) => ({
	...item,
	mastered: false,
	wrongCount: Number(item.wrongCount || 1)
}));
const wrongAnswer = 'Z';
const reviewListWrong = [{
	order: 1,
	title: practiceQuestion.title,
	myAnswer: wrongAnswer,
	rightAnswer: practiceQuestion.rightAnswer,
	isCorrect: isQuestionAnswerCorrect(wrongAnswer, practiceQuestion)
}];

assert(
	'24.1-6: intentional wrong answer on redo',
	reviewListWrong[0].isCorrect === false
);

await applyLocalNotebookAfterSubmit(
	reviewListWrong,
	redoSource,
	{
		markNotebookMastered: async (list) => {
			wrongRedoState = markNotebookMasteredLocal(list, wrongRedoState);
		},
		syncWrongNotebook: async (wrongList, source) => {
			wrongRedoState = syncWrongNotebookLocal(wrongList, source, wrongRedoState);
		}
	}
);

const wrongRedoItem = wrongRedoState.find((item) => item.question === seedItem.question);
assert(
	'24.1-6: redo wrong keeps mastered=false',
	Boolean(wrongRedoItem) && wrongRedoItem.mastered === false
);
assert(
	'24.1-6: redo wrong increments wrongCount',
	Number(wrongRedoItem?.wrongCount) === Number(seedItem.wrongCount || 1) + 1
);
assert(
	'24.1-6: redo wrong subject is 错题重做',
	wrongRedoItem?.subject === '错题重做'
);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Smoke notebook redo flow verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'smoke notebook redo checks passed (24.1 item 6)');
checks.forEach((c) => console.log('  ✓', c.name));
