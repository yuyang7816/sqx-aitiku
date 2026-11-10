/**
 * 模块十九 · Storage 数据迁移（文档 19.3）
 * App onLaunch 最先执行，再 hydrate Pinia / 读取业务 Storage，保证旧用户升级不丢数据。
 */
import {
	encryptSensitiveText,
	isEncryptedStorageValue
} from './storage-crypto.js';
import {
	AUTH_KEY,
	CHAPTER_ANSWER_KEY,
	COLLECTION_KEY,
	CURRENT_STORAGE_VERSION,
	LEGACY_BUSINESS_STORAGE_KEY_MAP,
	LEGACY_TOKEN_KEY,
	LEGACY_USER_PHONE_KEY,
	MEMBER_KEY,
	NOTEBOOK_KEY,
	NOTICE_KEY,
	PRACTICE_DRAFT_KEY,
	PRACTICE_SOURCE_KEY,
	PROFILE_KEY,
	SEARCH_RECORD_KEY,
	SETTINGS_KEY,
	STORAGE_VERSION_KEY,
	TOKEN_KEY
} from './storage-keys.js';
import { getTodayString } from './member-rules.js';

const isEmptyStorageValue = (value) => (
	value === undefined
	|| value === null
	|| value === ''
);

const isPlaintextTokenValue = (value) => {
	if (isEmptyStorageValue(value)) {
		return false;
	}
	return !isEncryptedStorageValue(String(value));
};

/** 解析 Storage 中的对象（兼容 JSON 字符串） */
export const parseStorageObject = (raw) => {
	if (raw === undefined || raw === null || raw === '') {
		return null;
	}
	if (typeof raw === 'object' && !Array.isArray(raw)) {
		return { ...raw };
	}
	if (typeof raw === 'string') {
		try {
			const parsed = JSON.parse(raw);
			if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
				return { ...parsed };
			}
		} catch (e) {
			// ignore
		}
	}
	return null;
};

/** 解析 Storage 中的数组（兼容 JSON 字符串） */
export const parseStorageArray = (raw) => {
	if (raw === undefined || raw === null || raw === '') {
		return null;
	}
	if (Array.isArray(raw)) {
		return [...raw];
	}
	if (typeof raw === 'string') {
		try {
			const parsed = JSON.parse(raw);
			return Array.isArray(parsed) ? [...parsed] : null;
		} catch (e) {
			return null;
		}
	}
	return null;
};

const normalizeVersionText = (version = '') => String(version).replace(/（默认）/g, '').trim();

const toAvatarText = (name = '') => {
	const text = String(name || '').trim();
	return text ? text.charAt(0) : '学';
};

/** v0 → v1：旧键名复制到 aiQuestion* 标准键 */
export const migrateLegacyBusinessKeys = (adapter) => {
	Object.entries(LEGACY_BUSINESS_STORAGE_KEY_MAP).forEach(([legacyKey, currentKey]) => {
		const legacyValue = adapter.get(legacyKey);
		if (isEmptyStorageValue(legacyValue)) {
			return;
		}
		const currentValue = adapter.get(currentKey);
		if (!isEmptyStorageValue(currentValue)) {
			return;
		}
		adapter.set(currentKey, legacyValue);
	});
};

/** v0 → v1：legacy token 写入 aiQuestionToken */
export const migrateLegacyToken = (adapter) => {
	const token = adapter.get(TOKEN_KEY);
	if (!isEmptyStorageValue(token)) {
		return;
	}
	const legacyToken = adapter.get(LEGACY_TOKEN_KEY);
	if (isEmptyStorageValue(legacyToken)) {
		return;
	}
	adapter.set(TOKEN_KEY, String(legacyToken));
};

/** v0 → v1：由 legacy phone / token 补全 aiQuestionAuthState */
export const migrateLegacyAuthState = (adapter) => {
	const auth = parseStorageObject(adapter.get(AUTH_KEY)) || {};
	const legacyPhone = adapter.get(LEGACY_USER_PHONE_KEY);
	const token = adapter.get(TOKEN_KEY) || adapter.get(LEGACY_TOKEN_KEY);
	const hasToken = !isEmptyStorageValue(token);
	const phone = String(auth.phone || legacyPhone || '').trim();

	if (auth.loggedIn === true) {
		if (phone && auth.phone !== phone) {
			adapter.set(AUTH_KEY, { ...auth, phone });
		}
		return;
	}

	if (!hasToken && !phone) {
		return;
	}

	adapter.set(AUTH_KEY, {
		loggedIn: true,
		phone,
		loginType: auth.loginType || 'sms'
	});
};

/** v0 → v1：profile 字段归一（nickname/isReady/subject 等旧字段） */
export const normalizeProfileStorage = (adapter) => {
	const profile = parseStorageObject(adapter.get(PROFILE_KEY));
	if (!profile) {
		return;
	}

	const next = { ...profile };
	if (!next.name && next.nickname) {
		next.name = String(next.nickname).trim();
	}
	if (next.isReady != null && next.ready == null) {
		next.ready = Boolean(next.isReady);
	}
	if (typeof next.subject === 'string' && next.subject.trim()) {
		const subjectText = next.subject.trim();
		if (!Array.isArray(next.subjects) || !next.subjects.length) {
			next.subjects = [subjectText];
		}
	}
	if (next.version != null) {
		next.version = normalizeVersionText(next.version);
	}
	if (next.name && !next.avatarText) {
		next.avatarText = toAvatarText(next.name);
	}

	delete next.nickname;
	delete next.isReady;
	delete next.subject;

	adapter.set(PROFILE_KEY, next);
};

/** v0 → v1：member 字段归一（vip/planName/expireDate 等旧字段） */
export const normalizeMemberStorage = (adapter) => {
	const member = parseStorageObject(adapter.get(MEMBER_KEY));
	if (!member) {
		return;
	}

	const next = { ...member };
	if (next.vip != null && next.isVip == null) {
		next.isVip = Boolean(next.vip);
	}
	if (!next.currentPlanName && next.planName) {
		next.currentPlanName = String(next.planName).trim();
	}
	if (!next.currentPlanName && next.currentPlan) {
		next.currentPlanName = String(next.currentPlan).trim();
	}
	if (!next.expireAt && next.expireDate) {
		next.expireAt = String(next.expireDate).trim();
	}
	if (next.aiLimit != null && next.aiDailyLimit == null) {
		next.aiDailyLimit = Number(next.aiLimit) || 0;
	}
	if (next.aiUsed != null && next.aiUsedToday == null) {
		next.aiUsedToday = Number(next.aiUsed) || 0;
	}
	if (next.ocrLimit != null && next.ocrDailyLimit == null) {
		next.ocrDailyLimit = Number(next.ocrLimit) || 0;
	}
	if (next.ocrUsed != null && next.ocrUsedToday == null) {
		next.ocrUsedToday = Number(next.ocrUsed) || 0;
	}
	if (!next.aiQuotaDate) {
		next.aiQuotaDate = getTodayString();
	}
	if (!next.ocrQuotaDate) {
		next.ocrQuotaDate = getTodayString();
	}

	delete next.vip;
	delete next.planName;
	delete next.currentPlan;
	delete next.expireDate;
	delete next.aiLimit;
	delete next.aiUsed;
	delete next.ocrLimit;
	delete next.ocrUsed;

	adapter.set(MEMBER_KEY, next);
};

/** v0 → v1：错题本条目标准化 + JSON 字符串转数组 */
export const normalizeNotebookStorage = (adapter) => {
	const list = parseStorageArray(adapter.get(NOTEBOOK_KEY));
	if (!list) {
		return;
	}

	const normalized = list.map((item) => {
		if (!item || typeof item !== 'object') {
			return item;
		}
		const next = { ...item };
		if (!next.question && next.title) {
			next.question = String(next.title).trim();
		}
		if (!next.wrong && next.myAnswer != null) {
			next.wrong = String(next.myAnswer);
		}
		if (!next.right && next.rightAnswer != null) {
			next.right = String(next.rightAnswer);
		}
		if (next.isMastered != null && next.mastered == null) {
			next.mastered = Boolean(next.isMastered);
		}
		if (next.questionId == null && next.questionCode != null) {
			next.questionId = String(next.questionCode);
		}
		if (next.questionCode == null && next.questionId != null) {
			next.questionCode = String(next.questionId);
		}
		delete next.isMastered;
		return next;
	});

	adapter.set(NOTEBOOK_KEY, normalized);
};

/** v0 → v1：收藏条目标准化 + JSON 字符串转数组 */
export const normalizeCollectionStorage = (adapter) => {
	const list = parseStorageArray(adapter.get(COLLECTION_KEY));
	if (!list) {
		return;
	}

	const normalized = list.map((item) => {
		if (!item || typeof item !== 'object') {
			return item;
		}
		const next = { ...item };
		if (!next.title && next.question) {
			next.title = String(next.question).trim();
		}
		if (next.questionId == null && next.questionCode != null) {
			next.questionId = String(next.questionCode);
		}
		if (next.questionCode == null && next.questionId != null) {
			next.questionCode = String(next.questionId);
		}
		return next;
	});

	adapter.set(COLLECTION_KEY, normalized);
};

/** v0 → v1：数组类 Storage 统一为数组对象（非 JSON 字符串） */
export const normalizeArrayStorageKeys = (adapter, keys = []) => {
	keys.forEach((key) => {
		const list = parseStorageArray(adapter.get(key));
		if (list) {
			adapter.set(key, list);
		}
	});
};

/** v0 → v1：对象类 Storage 统一为对象（非 JSON 字符串） */
export const normalizeObjectStorageKeys = (adapter, keys = []) => {
	keys.forEach((key) => {
		const obj = parseStorageObject(adapter.get(key));
		if (obj) {
			adapter.set(key, obj);
		}
	});
};

/** 迁移完成后移除 v0 旧业务键（标准键已写入） */
export const removeLegacyBusinessKeys = (adapter) => {
	Object.keys(LEGACY_BUSINESS_STORAGE_KEY_MAP).forEach((legacyKey) => {
		try {
			adapter.remove(legacyKey);
		} catch (e) {
			// ignore
		}
	});
};

/** v1 → v2：明文 token 加密并移除 legacy token 键 */
export const migratePlainTokenToEncrypted = (adapter) => {
	const encryptKeyIfPlain = (key) => {
		const raw = adapter.get(key);
		if (isEmptyStorageValue(raw)) {
			return;
		}
		const text = String(raw);
		if (isEncryptedStorageValue(text)) {
			return;
		}
		if (!isPlaintextTokenValue(text)) {
			return;
		}
		adapter.set(key, encryptSensitiveText(text));
	};

	encryptKeyIfPlain(TOKEN_KEY);

	const legacyToken = adapter.get(LEGACY_TOKEN_KEY);
	if (!isEmptyStorageValue(legacyToken)) {
		const legacyText = String(legacyToken);
		const mainToken = adapter.get(TOKEN_KEY);
		if (isEmptyStorageValue(mainToken) && isPlaintextTokenValue(legacyText)) {
			adapter.set(TOKEN_KEY, encryptSensitiveText(legacyText));
		}
		try {
			adapter.remove(LEGACY_TOKEN_KEY);
		} catch (e) {
			// ignore
		}
	}
};

/** v0 → v1 完整迁移流程 */
export const migrateStorageV0ToV1 = (adapter) => {
	migrateLegacyBusinessKeys(adapter);
	migrateLegacyToken(adapter);
	migrateLegacyAuthState(adapter);
	normalizeProfileStorage(adapter);
	normalizeMemberStorage(adapter);
	normalizeNotebookStorage(adapter);
	normalizeCollectionStorage(adapter);
	normalizeArrayStorageKeys(adapter, [
		NOTICE_KEY,
		SEARCH_RECORD_KEY
	]);
	normalizeObjectStorageKeys(adapter, [
		SETTINGS_KEY,
		PRACTICE_SOURCE_KEY,
		CHAPTER_ANSWER_KEY,
		PRACTICE_DRAFT_KEY
	]);
	removeLegacyBusinessKeys(adapter);
};

/** v2 → v3：清除本地错题本 mock/缓存，改由 GET /app/notebook 拉取 */
export const migrateClearLocalNotebookCache = (adapter) => {
	try {
		adapter.remove(NOTEBOOK_KEY);
	} catch (e) {
		// ignore
	}
};

const MIGRATION_STEPS = [
	{
		from: 0,
		to: 1,
		migrate: migrateStorageV0ToV1
	},
	{
		from: 1,
		to: 2,
		migrate: migratePlainTokenToEncrypted
	},
	{
		from: 2,
		to: 3,
		migrate: migrateClearLocalNotebookCache
	}
];

export const readStorageVersion = (adapter) => {
	const raw = adapter.get(STORAGE_VERSION_KEY);
	const version = Number(raw);
	if (!Number.isFinite(version) || version < 0) {
		return 0;
	}
	return version;
};

export const writeStorageVersion = (adapter, version) => {
	adapter.set(STORAGE_VERSION_KEY, version);
};

const createUniStorageAdapter = () => ({
	get: (key) => uni.getStorageSync(key),
	set: (key, value) => uni.setStorageSync(key, value),
	remove: (key) => {
		try {
			uni.removeStorageSync(key);
		} catch (e) {
			// ignore
		}
	}
});

/**
 * 按 STORAGE_VERSION 逐步升级本地 Storage
 * @param {object} [adapter] - 可注入 mock，便于单测
 * @returns {{ fromVersion: number, toVersion: number, migrated: boolean }}
 */
export const runStorageMigration = (adapter = createUniStorageAdapter()) => {
	let version = readStorageVersion(adapter);
	const fromVersion = version;

	while (version < CURRENT_STORAGE_VERSION) {
		const step = MIGRATION_STEPS.find((item) => item.from === version);
		if (!step) {
			break;
		}
		step.migrate(adapter);
		version = step.to;
		writeStorageVersion(adapter, version);
	}

	return {
		fromVersion,
		toVersion: version,
		migrated: version > fromVersion
	};
};
