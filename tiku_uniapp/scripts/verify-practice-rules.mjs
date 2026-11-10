/**
 * 练习规则静态与边界校验
 * 运行：node scripts/verify-practice-rules.mjs
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

assertIncludes('answer.vue imports practice-rules', 'package/question/answer.vue', [
	"from '@/common/practice-rules'",
	'buildUnansweredCompleteModalContent',
	'resolveCompletePracticeAction'
]);
assertIncludes('completePractice confirm modal', 'package/question/answer.vue', [
	'confirm-unanswered',
	'仍要交卷',
	'继续作答',
	'finishPracticeSession'
]);
assertIncludes('nextQuestion allows skip', 'package/question/answer.vue', [
	'currentIndex.value += 1',
	'允许跳过未答题'
]);
assertIncludes('answer-sheet footer actions', 'package/question/answer-sheet.vue', [
	'sheet-footer',
	'completePractice',
	'continueAnswer',
	'markAnswerSheetReturn'
]);
assertIncludes('practice-rules module exports', 'common/practice-rules.js', [
	'findFirstUnansweredIndex',
	'resolveCompletePracticeAction',
	'buildUnansweredCompleteModalContent',
	'applyLocalNotebookAfterSubmit'
]);

const hasAnswer = (userAnswer = '') => Boolean(String(userAnswer || '').trim());

const findFirstUnansweredIndex = (questionList = [], selectedMap = {}) => (
	questionList.findIndex((_, index) => !hasAnswer(selectedMap[index]))
);

const countAnsweredQuestions = (questionList = [], selectedMap = {}) => (
	questionList.filter((_, index) => hasAnswer(selectedMap[index])).length
);

const resolveCompletePracticeAction = ({ questionList = [], selectedMap = {} } = {}) => {
	const firstPendingIndex = findFirstUnansweredIndex(questionList, selectedMap);
	if (firstPendingIndex < 0) {
		return { action: 'submit' };
	}
	return {
		action: 'confirm-unanswered',
		unansweredCount: questionList.length - countAnsweredQuestions(questionList, selectedMap),
		firstPendingIndex
	};
};

const canProceedToNextQuestion = () => true;

const buildUnansweredCompleteModalContent = (remainingCount = 0) => {
	const count = Math.max(0, Number(remainingCount) || 0);
	if (count <= 0) {
		return '';
	}
	return `还有 ${count} 题未作答，未作答题将按错题计分。是否仍要交卷？`;
};

const questions = [{ id: 1 }, { id: 2 }, { id: 3 }];

assert('findFirstUnanswered: all empty -> index 0', findFirstUnansweredIndex(questions, {}) === 0);
assert('findFirstUnanswered: q0 answered -> index 1', findFirstUnansweredIndex(questions, { 0: 'A' }) === 1);
assert(
	'findFirstUnanswered: all answered -> -1',
	findFirstUnansweredIndex(questions, { 0: 'A', 1: 'B', 2: 'C' }) === -1
);

const confirmUnanswered = resolveCompletePracticeAction({
	questionList: questions,
	selectedMap: { 0: 'A', 1: 'B' }
});
assert(
	'complete: has unanswered -> confirm-unanswered',
	confirmUnanswered.action === 'confirm-unanswered'
		&& confirmUnanswered.firstPendingIndex === 2
		&& confirmUnanswered.unansweredCount === 1
);

const confirmSkipMiddle = resolveCompletePracticeAction({
	questionList: questions,
	selectedMap: { 1: 'B', 2: 'C' }
});
assert(
	'complete: skip first -> confirm with index 0',
	confirmSkipMiddle.action === 'confirm-unanswered' && confirmSkipMiddle.firstPendingIndex === 0
);

const submitAll = resolveCompletePracticeAction({
	questionList: questions,
	selectedMap: { 0: 'A', 1: 'B', 2: 'C' }
});
assert('complete: all answered -> submit', submitAll.action === 'submit');

assert('next: always allowed without answer', canProceedToNextQuestion());
assert(
	'modal copy mentions remaining count',
	buildUnansweredCompleteModalContent(2).includes('还有 2 题未作答')
);

const applyLocalNotebookAfterSubmit = async (reviewList, _source, { markNotebookMastered, syncWrongNotebook }) => {
	const wrongList = reviewList.filter((item) => !item.isCorrect);
	if (typeof markNotebookMastered === 'function') {
		await markNotebookMastered(reviewList.filter((item) => item.isCorrect));
	}
	if (typeof syncWrongNotebook === 'function') {
		await syncWrongNotebook(wrongList, _source);
	}
	return wrongList;
};

let masteredInput = null;
let wrongInput = null;
await applyLocalNotebookAfterSubmit(
	[
		{ title: 'Q1', isCorrect: true },
		{ title: 'Q2', isCorrect: false },
		{ title: 'Q3', isCorrect: false, myAnswer: '' }
	],
	{ mode: 'all' },
	{
		markNotebookMastered: async (list) => { masteredInput = list; },
		syncWrongNotebook: async (list) => { wrongInput = list; }
	}
);
assert(
	'local notebook: mastered only correct',
	masteredInput?.length === 1 && masteredInput[0].title === 'Q1'
);
assert(
	'local notebook: wrong includes unanswered',
	wrongInput?.length === 2 && wrongInput.some((item) => item.title === 'Q3')
);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Practice rules verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'practice rule checks passed');
checks.forEach((c) => console.log('  ✓', c.name));
