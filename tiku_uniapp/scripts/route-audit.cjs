/**
 * 路由跳转审计：扫描 navigateTo/reLaunch/redirectTo 等路径，与 pages.json 对齐。
 * 用法：node scripts/route-audit.cjs
 */
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const pagesJson = JSON.parse(fs.readFileSync(path.join(root, 'pages.json'), 'utf8'));
const registered = new Set();
pagesJson.pages.forEach((p) => registered.add(`/${p.path}`));
pagesJson.subPackages.forEach((sp) => {
	sp.pages.forEach((p) => registered.add(`/${sp.root}/${p.path}`));
});

function walk(dir, files = []) {
	for (const ent of fs.readdirSync(dir, { withFileTypes: true })) {
		const full = path.join(dir, ent.name);
		if (ent.isDirectory()) {
			if (['node_modules', 'dist', 'uni_modules', '.git', 'scripts'].includes(ent.name)) continue;
			walk(full, files);
		} else if (/\.(vue|js|ts)$/.test(ent.name)) {
			files.push(full);
		}
	}
	return files;
}

const normalizeRoutePath = (raw = '') => {
	const pathOnly = String(raw).split('?')[0].split('#')[0].trim();
	if (!pathOnly.startsWith('/')) {
		return '';
	}
	return pathOnly.replace(/\/+$/, '') || pathOnly;
};

const routeRes = [
	/['"`](\/(?:pages|package)\/[^'"`?#]+)['"`]/g,
	/`(\/(?:pages|package)\/[^`?#]+)/g,
	/(?:^|[\s=(])(\/(?:pages|package)\/[^\s'"`?#]+)/gm
];

const files = walk(root);
const found = new Map();

for (const file of files) {
	const content = fs.readFileSync(file, 'utf8');
	for (const routeRe of routeRes) {
		let m;
		while ((m = routeRe.exec(content)) !== null) {
			const route = normalizeRoutePath(m[1]);
			if (!route) continue;
			const rel = path.relative(root, file).replace(/\\/g, '/');
			if (!found.has(route)) found.set(route, []);
			found.get(route).push(rel);
		}
	}
}

const invalid = [];
const valid = [];
for (const [route, refs] of [...found.entries()].sort()) {
	const uniq = [...new Set(refs)];
	if (registered.has(route)) valid.push({ route, refs: uniq });
	else invalid.push({ route, refs: uniq });
}

const unreferenced = [...registered].filter((route) => !found.has(route)).sort();

console.log('REGISTERED COUNT:', registered.size);
console.log('FOUND UNIQUE ROUTES:', found.size);
console.log('\n=== INVALID ROUTES ===');
if (!invalid.length) {
	console.log('(none)');
} else {
	invalid.forEach((i) => console.log(i.route, '->', i.refs.join(', ')));
}
console.log('\n=== UNREFERENCED REGISTERED ROUTES ===');
if (!unreferenced.length) {
	console.log('(none)');
} else {
	unreferenced.forEach((route) => console.log(route));
}
console.log('\n=== VALID ROUTES (' + valid.length + ') ===');
valid.forEach((v) => console.log(v.route));

if (invalid.length) process.exit(1);
