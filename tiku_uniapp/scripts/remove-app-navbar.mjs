import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

const collectVueFiles = (dir, out = []) => {
	for (const name of fs.readdirSync(dir, { withFileTypes: true })) {
		if (name.name === 'node_modules' || name.name === 'unpackage' || name.name === 'dist') {
			continue;
		}
		const full = path.join(dir, name.name);
		if (name.isDirectory()) {
			collectVueFiles(full, out);
			continue;
		}
		if (name.name.endsWith('.vue') && !full.includes(`${path.sep}components${path.sep}app-navbar${path.sep}`)) {
			out.push(full);
		}
	}
	return out;
};

const importRe = /^\s*import\s+AppNavbar\s+from\s+['"]@\/components\/app-navbar\/app-navbar\.vue['"];\s*\n/gm;
const selfClosingRe = /^\s*<app-navbar[^>]*\/>\s*\n/gm;
const blockRe = /<app-navbar[\s\S]*?<\/app-navbar>\s*\n?/g;

let changed = 0;
for (const file of collectVueFiles(root)) {
	let src = fs.readFileSync(file, 'utf8');
	if (!src.includes('app-navbar') && !src.includes('AppNavbar')) {
		continue;
	}
	const orig = src;
	src = src.replace(blockRe, '');
	src = src.replace(selfClosingRe, '');
	src = src.replace(importRe, '');
	if (src !== orig) {
		fs.writeFileSync(file, src);
		changed += 1;
		console.log('updated', path.relative(root, file));
	}
}
console.log('total', changed);
