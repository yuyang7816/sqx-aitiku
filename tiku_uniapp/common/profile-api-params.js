/**
 * 用户 profile / Chip 筛选 → 接口 query 参数（仅 ASCII 业务编码，禁止传中文）
 * 与 server SetupQueryResolveSupport 约定一致
 */
import { getPracticeSubjectChips, isPracticeSubjectChip } from '@/common/app-data';

const GRADE_VALUE_TO_CODE = {
	'小学一年级': 'primary_1',
	'小学二年级': 'primary_2',
	'小学三年级': 'primary_3',
	'小学四年级': 'primary_4',
	'小学五年级': 'primary_5',
	'小学六年级': 'primary_6',
	'初中一年级': 'junior_1',
	'初中二年级': 'junior_2',
	'初中三年级': 'junior_3',
	'高中一年级': 'senior_1',
	'高中二年级': 'senior_2',
	'高中三年级': 'senior_3'
};

const GRADE_SHORT_TO_CODE = {
	小一: 'primary_1',
	小二: 'primary_2',
	小三: 'primary_3',
	小四: 'primary_4',
	小五: 'primary_5',
	小六: 'primary_6',
	初一: 'junior_1',
	初二: 'junior_2',
	初三: 'junior_3',
	高一: 'senior_1',
	高二: 'senior_2',
	高三: 'senior_3'
};

const SUBJECT_NAME_TO_CODE = {
	数学: 'math',
	语文: 'chinese',
	英语: 'english',
	物理: 'physics',
	化学: 'chemistry',
	生物: 'biology',
	历史: 'history',
	地理: 'geography',
	政治: 'politics'
};

/** 学科 API 编码 → 中文展示名（与 server SetupQueryResolveSupport 一致） */
const SUBJECT_CODE_TO_LABEL = {
	math: '数学',
	chinese: '语文',
	english: '英语',
	physics: '物理',
	chemistry: '化学',
	biology: '生物',
	history: '历史',
	geography: '地理',
	politics: '政治'
};

const VERSION_NAME_TO_CODE = {
	人教版: 'renjiao',
	北师大版: 'beishida',
	苏教版: 'sujiao',
	沪教版: 'hujiao'
};

const ASCII_PARAM_PATTERN = /^[\w.-]+$/;

/** 是否为合法 ASCII 接口参数（不含中文） */
export const isAsciiApiParam = (value = '') => ASCII_PARAM_PATTERN.test(String(value || '').trim());

const normalizeVersionLabel = (version = '') => String(version || '').replace(/（默认）/g, '').trim();

/** 年级完整名 / 简称 → API 编码 */
export const resolveGradeApiCode = (grade = '', gradeShort = '') => {
	const full = String(grade || '').trim();
	if (full && GRADE_VALUE_TO_CODE[full]) {
		return GRADE_VALUE_TO_CODE[full];
	}
	const shortText = String(gradeShort || '').trim();
	if (shortText && GRADE_SHORT_TO_CODE[shortText]) {
		return GRADE_SHORT_TO_CODE[shortText];
	}
	if (isAsciiApiParam(full)) {
		return full;
	}
	if (isAsciiApiParam(shortText)) {
		return shortText;
	}
	return '';
};

/** 学科展示名 → API 编码 */
export const resolveSubjectApiCode = (subject = '') => {
	const name = String(subject || '').trim();
	if (!name) {
		return '';
	}
	if (SUBJECT_NAME_TO_CODE[name]) {
		return SUBJECT_NAME_TO_CODE[name];
	}
	if (isAsciiApiParam(name)) {
		return name;
	}
	return '';
};

/** 学科 API 编码 / 别名 → 中文展示名（错题本筛选、Chip 展示用） */
export const resolveSubjectDisplayName = (subject = '') => {
	const raw = String(subject || '').trim();
	if (!raw) {
		return '';
	}
	if (SUBJECT_NAME_TO_CODE[raw]) {
		return raw;
	}
	if (SUBJECT_CODE_TO_LABEL[raw]) {
		return SUBJECT_CODE_TO_LABEL[raw];
	}
	const head = raw.split(/\s*[·・]\s*/)[0]?.trim() || raw;
	if (SUBJECT_NAME_TO_CODE[head]) {
		return head;
	}
	if (SUBJECT_CODE_TO_LABEL[head]) {
		return SUBJECT_CODE_TO_LABEL[head];
	}
	return raw;
};

/** 教材版本展示名 → API 编码 */
export const resolveVersionApiCode = (version = '') => {
	const name = normalizeVersionLabel(version);
	if (!name) {
		return '';
	}
	if (VERSION_NAME_TO_CODE[name]) {
		return VERSION_NAME_TO_CODE[name];
	}
	if (isAsciiApiParam(name)) {
		return name;
	}
	return '';
};

/**
 * 根据 profile 与刷题 Chip 解析章节/知识点列表 query（grade/subject/version 均为编码）
 * @param {object} profile
 * @param {string} [activeChip]
 */
export const resolveProfileFilterQuery = (profile = {}, activeChip = '') => {
	const subjects = getPracticeSubjectChips(profile);
	let subjectName = subjects[0] || '';
	if (activeChip && isPracticeSubjectChip(activeChip, profile)) {
		subjectName = activeChip;
	}

	const gradeCode = resolveGradeApiCode(profile.grade, profile.gradeShort);
	const subjectCode = resolveSubjectApiCode(subjectName);
	const versionCode = resolveVersionApiCode(profile.version);

	const query = {};
	if (gradeCode) {
		query.grade = gradeCode;
	}
	if (subjectCode) {
		query.subject = subjectCode;
	}
	if (versionCode) {
		query.version = versionCode;
	}
	return query;
};
