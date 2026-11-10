import fs from 'fs';

const path = 'D:/ideaproject/tiku_all_work/tiku_uniapp/package/analysis/report.vue';
let text = fs.readFileSync(path, 'utf8');
text = text.replace(/from '@dcloudio\/uni-app';\r?\s*import/g, "from '@dcloudio/uni-app';\n\timport");
if (!text.includes("from '@/common/navigation-bar'")) {
	text = text.replace(
		"from '@/common/app-data';",
		"from '@/common/app-data';\n\timport { usePageNavigationTitle } from '@/common/navigation-bar';"
	);
}
fs.writeFileSync(path, text);
