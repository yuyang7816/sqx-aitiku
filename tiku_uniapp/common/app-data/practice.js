/** 模块十九 · app-data / practice（Storage + API 对齐） */
export {
	PRACTICE_SOURCE_MODES,
	getPracticeSource,
	setPracticeSource,
	getChapterAnswerMap,
	syncChapterQuestionAnswer,
	applyChapterAnswerOverlay,
	clearPracticeSource,
	getPracticeQuestionList,
	PRACTICE_CHIP_ALL_GRADES,
	PRACTICE_CHIP_CHAPTER_MODE,
	PRACTICE_SUBJECT_NAMES,
	getPracticeChips,
	getPracticeVersionChipLabel,
	isPracticeVersionChip,
	isPracticeChapterModeChip,
	isPracticeSubjectChip,
	isPracticeUserGradeChip,
	getPracticeFilteredCards,
	getPracticeFilteredChapters,
	buildWeakPointWrongPracticeSource,
	buildReportWrongPracticeSource
} from './impl.js';
