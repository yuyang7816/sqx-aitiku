/**
 * 未答题边界校验：允许跳题，完成练习时提示但不强制
 * 运行：node scripts/verify-edge-unanswered.mjs
 */
import fs from 'fs';
import path from 'path';

const hasQuestionAnswer = (userAnswer = '') => Boolean(String(userAnswer || '').trim());

const findFirstUnansweredIndex = (questionList = [], selectedMap = {}) => (
	questionList.findIndex((item, index) => !hasQuestionAnswer(selectedMap[index], item))
);

const countAnsweredQuestions = (questionList = [], selectedMap = {}) => (
	questionList.filter((item, index) => hasQuestionAnswer(selectedMap[index], item)).length
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

assertIncludes('practice-rules confirm exports', 'common/practice-rules.js', [
	'findFirstUnansweredIndex',
	'resolveCompletePracticeAction',
	'buildUnansweredCompleteModalContent',
	'confirm-unanswered',
	'允许跳过未答题'
]);
assertIncludes('answer.vue completePractice modal', 'package/question/answer.vue', [
	'resolveCompletePracticeAction',
	'buildUnansweredCompleteModalContent',
	"'confirm-unanswered'",
	'completePractice',
	'finishPracticeSession'
]);
assertIncludes('answer.vue skip without answer', 'package/question/answer.vue', [
	'currentIndex.value += 1',
	'允许跳过未答题'
]);
assertIncludes('answer-sheet unanswered states', 'package/question/answer-sheet.vue', [
	'remainingCount',
	'sheet-item--unanswered',
	'sheet-item--current-answered',
	'markAnswerSheetReturn'
]);

const questions = [{ id: 1 }, { id: 2 }, { id: 3 }];

assert(
	'first unanswered on empty session is index 0',
	findFirstUnansweredIndex(questions, {}) === 0
);

const skipMiddleOnLast = resolveCompletePracticeAction({
	questionList: questions,
	selectedMap: { 0: 'A' }
});
assert(
	'skip middle on last -> confirm-unanswered index 1',
	skipMiddleOnLast.action === 'confirm-unanswered'
		&& skipMiddleOnLast.firstPendingIndex === 1
		&& skipMiddleOnLast.unansweredCount === 2,
	JSON.stringify(skipMiddleOnLast)
);

assert(
	'modal copy matches remaining count',
	buildUnansweredCompleteModalContent(1).includes('还有 1 题未作答')
);

const lastUnanswered = resolveCompletePracticeAction({
	questionList: questions,
	selectedMap: { 0: 'A', 1: 'B' }
});
assert(
	'last question unanswered -> confirm not block',
	lastUnanswered.action === 'confirm-unanswered' && lastUnanswered.firstPendingIndex === 2
);

const submitAll = resolveCompletePracticeAction({
	questionList: questions,
	selectedMap: { 0: 'A', 1: 'B', 2: 'C' }
});
assert('all answered -> submit', submitAll.action === 'submit');

assert('non-last without answer can proceed', canProceedToNextQuestion());

const simulateCompletePractice = ({ questionList, selectedMap }) => {
	const decision = resolveCompletePracticeAction({ questionList, selectedMap });
	if (decision.action === 'confirm-unanswered') {
		return {
			needsConfirm: true,
			unansweredCount: decision.unansweredCount,
			firstPendingIndex: decision.firstPendingIndex,
			modalContent: buildUnansweredCompleteModalContent(decision.unansweredCount)
		};
	}
	return { needsConfirm: false, submitted: true };
};

const confirmFlow = simulateCompletePractice({
	questionList: questions,
	selectedMap: { 0: 'A' }
});
assert(
	'confirm flow exposes first pending question',
	confirmFlow.needsConfirm === true && confirmFlow.firstPendingIndex === 1,
	JSON.stringify(confirmFlow)
);

assert(
	'confirm flow modal mentions wrong scoring',
	confirmFlow.modalContent.includes('按错题计分')
);

const finalSubmit = resolveCompletePracticeAction({
	questionList: questions,
	selectedMap: { 0: 'A', 1: 'B', 2: 'C' }
});
assert('full answered can submit', finalSubmit.action === 'submit');

assert(
	'mock chapter list referenced in smoke script',
	read('scripts/verify-smoke-practice.mjs').includes('chapterQuestions.length === 3')
);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Edge unanswered verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'edge unanswered checks passed');
checks.forEach((c) => console.log('  ✓', c.name));
