/**
 * 刷题中心 UI 配置（导航与模式卡片，非业务数据）
 * 章节/题集/知识点列表均走 API
 */
import { PAPER_CENTER_PAGE } from '@/common/routes';
import { PAPER_TYPE_EXAM, PAPER_TYPE_SPECIAL } from '@/common/paper-list';

/** 练习类型 Chip */
export const practiceChipChapterMode = '章节练习';

/** 章节练习分包页 */
export const CHAPTER_PRACTICE_PAGE = '/package/practice/chapter';

/** 试卷中心页（专项训练 / 真题模拟选题集） */
export const buildPaperCenterUrl = (tab = PAPER_TYPE_SPECIAL) => {
	const key = String(tab || PAPER_TYPE_SPECIAL).trim() || PAPER_TYPE_SPECIAL;
	return `${PAPER_CENTER_PAGE}?tab=${encodeURIComponent(key)}`;
};

/** 热门章节跳转 URL */
export const buildChapterPracticeUrl = (chapterId, title = '', chip = '') => {
	if (!chapterId) {
		return CHAPTER_PRACTICE_PAGE;
	}
	const query = [`id=${encodeURIComponent(chapterId)}`];
	const chapterTitle = String(title || '').trim();
	if (chapterTitle) {
		query.push(`title=${encodeURIComponent(chapterTitle)}`);
	}
	const chipValue = String(chip || '').trim();
	if (chipValue) {
		query.push(`chip=${encodeURIComponent(chipValue)}`);
	}
	return `${CHAPTER_PRACTICE_PAGE}?${query.join('&')}`;
};

/** 知识点练习分包页（可携带 chip） */
export const buildKnowledgePracticeUrl = (chip = '') => {
	const chipValue = String(chip || '').trim();
	if (!chipValue) {
		return KNOWLEDGE_PRACTICE_PAGE;
	}
	return `${KNOWLEDGE_PRACTICE_PAGE}?chip=${encodeURIComponent(chipValue)}`;
};

/** 知识点练习分包页 */
export const KNOWLEDGE_PRACTICE_PAGE = '/package/practice/knowledge';

/** 刷题中心「专项训练」卡片写入 practiceSource.title */
export const SPECIAL_TRAINING_TITLE = '专项训练';

/** 刷题中心「真题模拟」卡片写入 practiceSource.title */
export const EXAM_SIMULATION_TITLE = '真题模拟';

/** 真题模拟默认考试时长（分钟），模考倒计时用；题集时长优先走 API */
export const EXAM_DEFAULT_DURATION_MINUTES = 90;

/** 专项/真题 paperId 由 GET /app/question/paper/list 动态注入，此处仅占位 */
export const SPECIAL_TRAINING_PAPER_ID = '';
export const EXAM_SIMULATION_PAPER_ID = '';

/** 练习模式卡片（subject/grade 仅用于 Chip 过滤，paperId 由 fetchPracticePaperIds 动态注入） */
export const buildPracticeCards = ({ specialPaperId = '', examPaperId = '' } = {}) => [
	{
		title: '章节练习',
		sub: '同步课本进度',
		icon: 'level',
		iconColor: '#4B7AFE',
		bg: 'linear-gradient(180deg, #EEF4FF 0%, #E6F0FF 100%)',
		url: CHAPTER_PRACTICE_PAGE
	},
	{
		title: '知识点练习',
		sub: '点对点攻克薄弱',
		icon: 'star-fill',
		iconColor: '#2AAA64',
		bg: 'linear-gradient(180deg, #EEFCF2 0%, #E7FAEE 100%)',
		url: KNOWLEDGE_PRACTICE_PAGE
	},
	{
		title: SPECIAL_TRAINING_TITLE,
		sub: '攻克中考专题难点',
		icon: 'star-fill',
		iconColor: '#FF8A3D',
		bg: 'linear-gradient(180deg, #FFF5E8 0%, #FFF0DD 100%)',
		url: buildPaperCenterUrl(PAPER_TYPE_SPECIAL),
		paperId: specialPaperId
	},
	{
		title: EXAM_SIMULATION_TITLE,
		sub: '历年真题实战',
		icon: 'edit-pen',
		iconColor: '#A078F7',
		bg: 'linear-gradient(180deg, #F7F0FF 0%, #F3EBFF 100%)',
		url: buildPaperCenterUrl(PAPER_TYPE_EXAM),
		paperId: examPaperId
	}
];

/** 默认练习卡片（paperId 为空时专项/真题需 API 拉取后再组卷） */
export const practiceCards = buildPracticeCards();
