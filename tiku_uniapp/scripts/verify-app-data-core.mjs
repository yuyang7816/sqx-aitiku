/**
 * 校验 app-data 重构后导出与文档 19.2 核心函数一致
 * 运行：node scripts/verify-app-data-core.mjs
 */
import fs from 'fs';
import path from 'path';

const CORE = [
	'getLaunchRoute', 'loginUser', 'logoutUser', 'completeUserProfile', 'saveUserProfile',
	'getMemberState', 'activateMemberPlan', 'getRemainingAiTimes', 'syncWrongNotebook',
	'markNotebookMastered', 'getPracticeQuestionList', 'toggleCollectionItem', 'hasCollectionItem',
	'getNoticeList', 'markNoticeRead', 'appendSearchRecord', 'getHomeStudyStats', 'getHomeRecommendList'
];

const indexSrc = fs.readFileSync(path.resolve('common/app-data/index.js'), 'utf8');
const domainFiles = fs.readdirSync(path.resolve('common/app-data')).filter((f) => f.endsWith('.js') && f !== 'index.js' && f !== 'impl.js' && f !== 'core.js');

const exported = new Set();
for (const file of domainFiles) {
	const src = fs.readFileSync(path.resolve('common/app-data', file), 'utf8');
	const block = src.match(/export \{([^}]+)\}/s);
	if (!block) continue;
	block[1].split(',').map((s) => s.trim()).filter(Boolean).forEach((n) => exported.add(n));
}

const missing = CORE.filter((n) => !exported.has(n));
if (missing.length) {
	console.error('19.2 core functions missing from domain exports:', missing.join(', '));
	process.exit(1);
}
console.log('OK: all', CORE.length, 'document 19.2 core functions exported via domain modules');
console.log('Domain modules:', domainFiles.length, '| Total domain exports:', exported.size);
