/**
 * 文档 19.2 核心函数清单（API + Storage 对齐）
 * @see tiku_uniapp项目需求与流程梳理.md §19.2
 */
export const APP_DATA_CORE_FUNCTIONS = [
	"getLaunchRoute",
	"loginUser",
	"logoutUser",
	"completeUserProfile",
	"saveUserProfile",
	"getMemberState",
	"activateMemberPlan",
	"getRemainingAiTimes",
	"syncWrongNotebook",
	"markNotebookMastered",
	"getPracticeQuestionList",
	"toggleCollectionItem",
	"hasCollectionItem",
	"getNoticeList",
	"markNoticeRead",
	"appendSearchRecord",
	"getHomeStudyStats",
	"getHomeRecommendList"
];

/** 校验 name 是否为文档 19.2 登记的核心函数 */
export const isAppDataCoreFunction = (name = '') => APP_DATA_CORE_FUNCTIONS.includes(String(name).trim());
