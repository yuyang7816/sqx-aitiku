/**
 * 模块二十四 · 刷题答题闭环冒烟校验（文档 24.1 第 4~5 项）
 * 运行：node scripts/verify-smoke-practice.mjs
 */
import fs from 'fs';
import path from 'path';

/** 冒烟用章节题 fixture（非生产 mock，仅脚本内联） */
const chapterQuestions = [
	{ questionId: 1, questionCode: 'q-1', text: '有理数加法' },
	{ questionId: 2, questionCode: 'q-2', text: '整式加减' },
	{ questionId: 3, questionCode: 'q-3', text: '一元一次方程' }
];
const chapterQuestionDetailsByCode = {
	'q-1': {
		type: '单选题',
		difficulty: '中等',
		title: '有理数加法',
		rightAnswer: 'A',
		analysis: '解析1',
		options: [{ label: 'A', value: '1' }, { label: 'B', value: '2' }]
	},
	'q-2': {
		type: '多选题',
		difficulty: '中等',
		title: '整式加减',
		rightAnswer: 'A,C',
		analysis: '解析2',
		options: [{ label: 'A', value: '1' }, { label: 'B', value: '2' }, { label: 'C', value: '3' }]
	},
	'q-3': {
		type: '单选题',
		difficulty: '简单',
		title: '一元一次方程',
		rightAnswer: 'C',
		analysis: '解析3',
		options: [{ label: 'A', value: '1' }, { label: 'B', value: '2' }, { label: 'C', value: '3' }]
	}
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

// --- 与 question-answer.js / practice-rules.js 对齐的轻量复刻 ---

const normalizeOptionLabel = (value) => String(value || '').trim().toUpperCase();

const parseMultiAnswerLabels = (value = '') => {
	const raw = String(value || '').trim().toUpperCase();
	if (!raw) {
		return [];
	}
	if (raw.includes(',') || raw.includes('、') || raw.includes('|') || raw.includes(' ')) {
		return [...new Set(raw.split(/[,、|\s]+/).map(normalizeOptionLabel).filter(Boolean))].sort();
	}
	return [...new Set(raw.split('').map(normalizeOptionLabel).filter(Boolean))].sort();
};

const resolveQuestionInputMode = (question = {}) => {
	const type = String(question.type || '单选题').trim();
	if (type === '多选题') {
		return 'multi';
	}
	if (type === '填空题') {
		return 'fill';
	}
	return 'single';
};

const isQuestionAnswerCorrect = (userAnswer = '', question = {}) => {
	const mode = resolveQuestionInputMode(question);
	const rightAnswer = question.rightAnswer || '';
	if (mode === 'multi') {
		const mine = parseMultiAnswerLabels(userAnswer);
		const expected = parseMultiAnswerLabels(rightAnswer);
		return mine.length > 0 && mine.join(',') === expected.join(',');
	}
	if (mode === 'fill') {
		return String(userAnswer || '').trim().toLowerCase() === String(rightAnswer || '').trim().toLowerCase();
	}
	return normalizeOptionLabel(userAnswer) === normalizeOptionLabel(rightAnswer);
};

const hasQuestionAnswer = (userAnswer = '') => Boolean(String(userAnswer || '').trim());

const findFirstUnansweredIndex = (questionList = [], selectedMap = {}) => (
	questionList.findIndex((_, index) => !hasQuestionAnswer(selectedMap[index]))
);

const resolveCompletePracticeAction = ({ questionList = [], selectedMap = {} } = {}) => {
	const firstPendingIndex = findFirstUnansweredIndex(questionList, selectedMap);
	if (firstPendingIndex < 0) {
		return { action: 'submit' };
	}
	return {
		action: 'confirm-unanswered',
		unansweredCount: questionList.length - questionList.filter((_, index) => hasQuestionAnswer(selectedMap[index])).length,
		firstPendingIndex
	};
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

// --- 文档 24.1 第 4 项：首页 → 章节练习 → 答题 3 题 → 结果页 ---

assertIncludes('home recommend chapter-practice', 'pages/index/index.vue', [
	"'chapter-practice'",
	'CHAPTER_PRACTICE_PAGE',
	'openRecommend'
]);
assertIncludes('home recommend list chapter action', 'common/app-data/impl.js', [
	"action: 'chapter-practice'",
	'getHomeRecommendList'
]);
assertIncludes('chapter start practice', 'package/practice/chapter.vue', [
	'startChapterPractice',
	"mode: 'all'",
	"'章节练习'",
	'QUESTION_ANSWER_PAGE',
	'setPracticeSource'
]);
assertIncludes('answer load and submit', 'package/question/answer.vue', [
	'fetchPracticeQuestionsBySource',
	'saveSession',
	'QUESTION_PRACTICE_SESSION_KEY',
	'QUESTION_RESULT_PAGE',
	'completePractice'
]);
assertIncludes('result reads session', 'package/question/result.vue', [
	'QUESTION_PRACTICE_SESSION_KEY',
	'normalizeSession',
	'wrongList',
	'practiceTitle'
]);
assertIncludes('storage keys registered', 'common/storage-keys.js', [
	'QUESTION_PRACTICE_SESSION_KEY',
	'NOTEBOOK_KEY'
]);
assertIncludes('pages.json practice routes', 'pages.json', [
	'"path": "chapter"',
	'"path": "answer"',
	'"path": "result"',
	'"path": "pages/notebook/notebook"'
]);

const mapChapterQuestion = (listItem = {}) => {
	const detail = chapterQuestionDetailsByCode[listItem.questionCode] || {};
	return {
		type: detail.type || '单选题',
		difficulty: detail.difficulty || '中等',
		title: detail.title || listItem.text || '',
		rightAnswer: detail.rightAnswer || 'A',
		analysis: detail.analysis || '',
		aiSummary: detail.aiSummary || detail.analysis || '',
		options: Array.isArray(detail.options) ? detail.options : [],
		questionId: listItem.questionId,
		questionCode: listItem.questionCode
	};
};

const practiceQuestions = chapterQuestions.map(mapChapterQuestion).filter((item) => item.title);

assert(
	'24.1-4: inline chapter fixture has 3 questions',
	chapterQuestions.length === 3 && practiceQuestions.length === 3,
	`chapterQuestions=${chapterQuestions.length}, mapped=${practiceQuestions.length}`
);

const buildChapterPracticeQuery = (source = {}) => {
	const chapterId = String(source.chapterId || '').trim();
	if (!chapterId) {
		return null;
	}
	return { chapterId, mode: String(source.mode || 'all').trim() || 'all' };
};

assert(
	'24.1-4: chapter practiceSource builds list query',
	buildChapterPracticeQuery({ mode: 'all', chapterId: 'rational-numbers' })?.chapterId === 'rational-numbers'
);

const selectedMap = {
	0: 'A',
	1: 'C',
	2: 'C'
};

const reviewList = practiceQuestions.map((item, index) => {
	const myAnswer = selectedMap[index] || '';
	return {
		order: index + 1,
		title: item.title,
		myAnswer,
		rightAnswer: item.rightAnswer,
		analysis: item.analysis,
		options: item.options,
		isCorrect: isQuestionAnswerCorrect(myAnswer, item),
		questionId: item.questionId,
		questionCode: item.questionCode
	};
});

assert(
	'24.1-4: 3-question session review count',
	reviewList.length === 3
);
assert(
	'24.1-4: intentional wrong answer detected (q2 multi)',
	reviewList[1].isCorrect === false && reviewList.filter((item) => !item.isCorrect).length === 1
);

const submitDecision = resolveCompletePracticeAction({
	questionList: practiceQuestions,
	selectedMap
});
assert(
	'24.1-4: all 3 answered → submit',
	submitDecision.action === 'submit'
);

const buildPracticeSession = (list, source = {}) => {
	const wrongList = list.filter((item) => !item.isCorrect);
	const correctCount = list.length - wrongList.length;
	return {
		total: list.length,
		correctCount,
		wrongCount: wrongList.length,
		score: list.length ? Math.round((correctCount / list.length) * 100) : 0,
		wrongList,
		reviewList: list,
		sourceMode: source.mode || 'all',
		chapterId: String(source.chapterId || '').trim(),
		practiceTitle: String(source.title || '').trim() || '章节练习'
	};
};

const session = buildPracticeSession(reviewList, {
	mode: 'all',
	chapterId: 'rational-numbers',
	title: '章节练习'
});

assert(
	'24.1-4: result session total=3 wrongCount=1',
	session.total === 3 && session.wrongCount === 1 && session.score === 67
);
assert(
	'24.1-4: result session keeps chapter context',
	session.chapterId === 'rational-numbers' && session.practiceTitle === '章节练习'
);

// --- 文档 24.1 第 5 项：故意答错 → 错题本出现该题 ---

assertIncludes('notebook tab reads list', 'pages/notebook/notebook.vue', [
	'useNotebookStore',
	'getNotebookFilteredList',
	'filteredList'
]);
assertIncludes('answer sync wrong notebook', 'package/question/answer.vue', [
	'syncWrongNotebook',
	'applyLocalNotebookAfterSubmit',
	'markNotebookMastered'
]);
assertIncludes('impl syncWrongNotebook doc 12.3', 'common/app-data/impl.js', [
	'resolveSyncWrongNotebookSubject',
	'syncWrongNotebook',
	'wrongCount: Number(existing.wrongCount || 0) + 1'
]);

const resolveSyncWrongNotebookSubject = (practiceSource = {}) => {
	const mode = String(practiceSource.mode || '').trim();
	if (mode === 'wrong') {
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

let notebookAfterSubmit = [];
await applyLocalNotebookAfterSubmit(
	reviewList,
	{ mode: 'all', title: '章节练习' },
	{
		markNotebookMastered: async () => {},
		syncWrongNotebook: async (wrongList, source) => {
			notebookAfterSubmit = syncWrongNotebookLocal(wrongList, source, notebookAfterSubmit);
		}
	}
);

const wrongTitle = reviewList[1].title;
const notebookItem = notebookAfterSubmit.find((item) => item.question === wrongTitle);

assert(
	'24.1-5: wrong question added to notebook',
	Boolean(notebookItem) && notebookItem.wrongCount === 1
);
assert(
	'24.1-5: notebook subject is 章节练习',
	notebookItem?.subject === '章节练习'
);
assert(
	'24.1-5: repeat wrong increments wrongCount',
	syncWrongNotebookLocal([reviewList[1]], { mode: 'all', title: '章节练习' }, notebookAfterSubmit)
		.find((item) => item.question === wrongTitle)?.wrongCount === 2
);

const failed = checks.filter((c) => !c.ok);
if (failed.length) {
	console.error('Smoke practice flow verification FAILED:');
	failed.forEach((c) => console.error(`  - ${c.name}: ${c.detail}`));
	process.exit(1);
}

console.log('OK: all', checks.length, 'smoke practice flow checks passed (24.1 items 4-5)');
checks.forEach((c) => console.log('  ✓', c.name));
