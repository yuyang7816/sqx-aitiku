import fs from 'fs';
import path from 'path';

const root = path.resolve('common');
const src = fs.readFileSync(path.join(root, 'app-data.js'), 'utf8');
const names = new Set();
for (const m of src.matchAll(/^export const ([A-Za-z_][A-Za-z0-9_]*) /gm)) names.add(m[1]);
for (const m of src.matchAll(/^export (?:async )?function ([a-zA-Z0-9_]+)/gm)) names.add(m[1]);
const reExp = src.match(/export \{([^}]+)\} from '@\/common\/storage-keys'/);
if (reExp) {
	reExp[1].split(',').map((s) => s.trim()).filter(Boolean).forEach((n) => names.add(n.split(' as ')[0].trim()));
}
console.log('export count:', names.size);
console.log([...names].sort().join('\n'));
