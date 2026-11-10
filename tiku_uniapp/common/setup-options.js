/**
 * 计划页选项：GET /app/setup/options
 */
import { get, PAGE_LOAD_REQUEST_OPTIONS } from '@/common/api';

/** 接口不可用时的空结构（须重试拉取，不使用本地硬编码选项） */
export const getFallbackSetupOptions = () => ({
	gradeStages: [],
	grades: [],
	subjects: [],
	versions: []
});

const inferStageCodeFromGradeValue = (gradeValue = '') => {
	const text = String(gradeValue || '').trim();
	if (text.startsWith('小学')) return 'primary';
	if (text.startsWith('高中')) return 'senior';
	if (text.startsWith('初中')) return 'junior';
	return 'junior';
};

const inferStageNameFromCode = (stageCode = '') => {
	if (stageCode === 'primary') return '小学';
	if (stageCode === 'senior') return '高中';
	return '初中';
};

const normalizeGradeStages = (list) =>
	(Array.isArray(list) ? list : [])
		.filter((item) => item && item.label && item.value)
		.map((item) => ({ label: item.label, value: item.value }));

const normalizeGrades = (list, gradeStages = []) => {
	const stageNameMap = new Map(
		gradeStages.map((item) => [item.value, item.label])
	);
	return (Array.isArray(list) ? list : [])
		.filter((item) => item && item.label && item.value)
		.map((item) => {
			const stageCode = item.stageCode || inferStageCodeFromGradeValue(item.value);
			return {
				label: item.label,
				value: item.value,
				stageCode,
				stageName: item.stageName || stageNameMap.get(stageCode) || inferStageNameFromCode(stageCode)
			};
		});
};

const KNOWN_SUBJECT_ICONS = new Set([
	'file-text', 'edit-pen', 'email', 'reload', 'more-circle', 'grid',
	'bookmark', 'star', 'photo', 'calendar', 'map', 'tags'
]);

/** 字典 remark 可能是中文说明，仅保留合法 uview 图标名 */
export const resolveSubjectIcon = (icon = '') => {
	const value = String(icon || '').trim();
	if (KNOWN_SUBJECT_ICONS.has(value)) {
		return value;
	}
	return 'file-text';
};

const DEFAULT_STAGES_BY_SUBJECT = {
	数学: ['primary', 'junior', 'senior'],
	语文: ['primary', 'junior', 'senior'],
	英语: ['primary', 'junior', 'senior'],
	物理: ['junior', 'senior'],
	化学: ['junior', 'senior'],
	生物: ['junior', 'senior'],
	历史: ['junior', 'senior'],
	地理: ['junior', 'senior'],
	政治: ['junior', 'senior']
};

const defaultStagesForSubject = (subjectName = '') => {
	const name = String(subjectName || '').trim();
	return DEFAULT_STAGES_BY_SUBJECT[name] || ['junior', 'senior'];
};

const normalizeStageCodes = (stageCodes, subjectName = '') => {
	const parsed = (Array.isArray(stageCodes) ? stageCodes : [])
		.map((item) => String(item || '').trim())
		.filter((item) => ['primary', 'junior', 'senior'].includes(item));
	return parsed.length ? [...new Set(parsed)] : defaultStagesForSubject(subjectName);
};

const normalizeSubjects = (list) =>
	list
		.filter((item) => item && item.name)
		.map((item) => ({
			name: item.name,
			icon: resolveSubjectIcon(item.icon),
			stageCodes: normalizeStageCodes(item.stageCodes, item.name),
			active: Boolean(item.active)
		}));

const normalizeVersions = (list) =>
	list
		.filter((item) => item && item.label && item.value)
		.map((item) => ({ label: item.label, value: item.value }));

export const buildGradeStagesFromGrades = (grades = []) => {
	const ordered = [];
	const seen = new Set();
	grades.forEach((item) => {
		if (!item?.stageCode || seen.has(item.stageCode)) {
			return;
		}
		seen.add(item.stageCode);
		ordered.push({
			label: item.stageName || inferStageNameFromCode(item.stageCode),
			value: item.stageCode
		});
	});
	return ordered.length ? ordered : [];
};

const isValidOptions = (data) =>
	data
	&& Array.isArray(data.grades) && data.grades.length
	&& Array.isArray(data.subjects) && data.subjects.length
	&& Array.isArray(data.versions) && data.versions.length;

/**
 * 拉取学习计划选项；成功返回 API 数据，失败或结构无效时返回兜底列表
 */
export const fetchSetupOptions = async () => {
	try {
		const res = await get('/app/setup/options', undefined, PAGE_LOAD_REQUEST_OPTIONS);
		if (res && res.code === 0 && isValidOptions(res.data)) {
			const gradeStages = normalizeGradeStages(res.data.gradeStages);
			const grades = normalizeGrades(
				res.data.grades,
				gradeStages.length ? gradeStages : buildGradeStagesFromGrades(res.data.grades)
			);
			return {
				gradeStages: gradeStages.length ? gradeStages : buildGradeStagesFromGrades(grades),
				grades,
				subjects: normalizeSubjects(res.data.subjects),
				versions: normalizeVersions(res.data.versions),
				fromApi: true
			};
		}
	} catch (e) {
		// 401 由 httpRequest 全局处理
	}
	return {
		...getFallbackSetupOptions(),
		fromApi: false
	};
};

export const filterGradesByStage = (grades = [], stageCode = '') => {
	if (!stageCode) {
		return [];
	}
	return grades.filter((item) => item.stageCode === stageCode);
};

/** 按当前学段过滤可选学科（如小学不展示化学） */
export const filterSubjectsByStage = (subjects = [], stageCode = '') => {
	if (!stageCode) {
		return [];
	}
	return subjects.filter((item) => (item.stageCodes || defaultStagesForSubject(item.name)).includes(stageCode));
};

/** 切换学段后，取消当前学段不可选的已选科目 */
export const pruneSubjectSelectionByStage = (subjects = [], stageCode = '') => {
	const allowed = new Set(filterSubjectsByStage(subjects, stageCode).map((item) => item.name));
	return subjects.map((item) => (
		allowed.has(item.name) ? item : { ...item, active: false }
	));
};
