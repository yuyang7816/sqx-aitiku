/**
 * tech-debt 看板审计：校验 P2#16~24 共 9 项齐全且 id 连续。
 * 用法：node scripts/tech-debt-audit.cjs
 */
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const boardPath = path.join(root, 'common', 'tech-debt-board.js');
const content = fs.readFileSync(boardPath, 'utf8');

const EXPECTED_IDS = [];
for (let n = 16; n <= 24; n += 1) {
	EXPECTED_IDS.push(`P2#${n}`);
}

const foundIds = [];
const idRe = /id:\s*'(P2#\d+)'/g;
let match;
while ((match = idRe.exec(content)) !== null) {
	foundIds.push(match[1]);
}

const missing = EXPECTED_IDS.filter((id) => !foundIds.includes(id));
const extra = foundIds.filter((id) => !EXPECTED_IDS.includes(id));
const duplicate = foundIds.filter((id, idx) => foundIds.indexOf(id) !== idx);

console.log('TECH-DEBT BOARD AUDIT');
console.log('Expected:', EXPECTED_IDS.join(', '));
console.log('Found:', foundIds.join(', '));

if (missing.length) {
	console.error('\nMISSING ids:', missing.join(', '));
}
if (extra.length) {
	console.error('\nEXTRA ids:', extra.join(', '));
}
if (duplicate.length) {
	console.error('\nDUPLICATE ids:', [...new Set(duplicate)].join(', '));
}

const columnRe = /column:\s*'(\w+)'/g;
const columns = new Set();
while ((match = columnRe.exec(content)) !== null) {
	columns.add(match[1]);
}
console.log('\nColumns used:', [...columns].sort().join(', '));

const sprintRe = /targetSprint:\s*'([^']+)'/g;
const sprints = [];
while ((match = sprintRe.exec(content)) !== null) {
	sprints.push(match[1]);
}
console.log('Scheduled items:', sprints.length, '/', EXPECTED_IDS.length);

const failed = missing.length || extra.length || duplicate.length || foundIds.length !== EXPECTED_IDS.length;
if (failed) {
	process.exit(1);
}
console.log('\nOK: tech-debt board contains P2#16~P2#24 with schedules.');
