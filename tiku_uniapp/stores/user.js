/**
 * 模块十九 · 用户态 Store（登录态 + 资料，hydrate 自 Storage）
 */
import { defineStore } from 'pinia';
import {
	AUTH_KEY,
	PROFILE_KEY
} from '@/common/storage-keys';

const defaultAuthState = {
	loggedIn: false,
	phone: '',
	loginType: ''
};

const defaultUserProfile = {
	ready: false,
	name: '',
	avatarText: '学',
	grade: '',
	gradeShort: '',
	version: '',
	subjects: []
};

const normalizeVersion = (version = '') => String(version).replace(/（默认）/g, '').trim();

/** 与 app-data gradeToShort 规则一致 */
const gradeToShort = (grade = '') => {
	const text = String(grade || '').trim();
	if (!text) return '';
	if (text.includes('三')) return '初三';
	if (text.includes('二')) return '初二';
	if (text.includes('一')) return '初一';
	return text.replace('初中', '初');
};

const applyGradeShort = (profile = {}) => {
	if (profile.grade == null || profile.grade === '') {
		return profile;
	}
	return {
		...profile,
		gradeShort: gradeToShort(profile.grade)
	};
};

const toAvatarText = (name = '') => {
	const text = String(name || '').trim();
	return text ? text.charAt(0) : '学';
};

const syncAvatarFromName = (profile = {}) => {
	const name = String(profile.name || '').trim();
	if (!name) {
		return profile;
	}
	return {
		...profile,
		avatarText: toAvatarText(name)
	};
};

const readAuthFromStorage = () => {
	const saved = uni.getStorageSync(AUTH_KEY);
	if (saved && typeof saved === 'object') {
		return {
			...defaultAuthState,
			...saved
		};
	}
	return {
		...defaultAuthState
	};
};

const readProfileFromStorage = () => {
	const saved = uni.getStorageSync(PROFILE_KEY);
	const merged = {
		...defaultUserProfile,
		...(saved && typeof saved === 'object' ? saved : {})
	};
	let profile = applyGradeShort(merged);
	profile = syncAvatarFromName(profile);
	if (profile.version != null) {
		profile.version = normalizeVersion(profile.version);
	}
	return profile;
};

export const useUserStore = defineStore('user', {
	state: () => ({
		hydrated: false,
		auth: {
			...defaultAuthState
		},
		profile: {
			...defaultUserProfile
		}
	}),
	getters: {
		isLoggedIn: (state) => state.auth.loggedIn === true,
		isProfileReady: (state) => state.profile.ready === true,
		profileMetaLine: (state) => {
			const gradeShort = state.profile.gradeShort || gradeToShort(state.profile.grade);
			const version = state.profile.version ? normalizeVersion(state.profile.version) : '';
			const subjects = Array.isArray(state.profile.subjects)
				? state.profile.subjects.filter(Boolean).slice(0, 2).join(' / ')
				: '';
			return [gradeShort, version, subjects].filter(Boolean).join(' · ');
		}
	},
	actions: {
		/** 启动时从 Storage 灌入内存态 */
		hydrateFromStorage() {
			this.auth = readAuthFromStorage();
			this.profile = readProfileFromStorage();
			this.hydrated = true;
		},
		setAuth(auth = {}) {
			this.auth = {
				...defaultAuthState,
				...auth
			};
		},
		setProfile(profile = {}) {
			let next = applyGradeShort({
				...defaultUserProfile,
				...this.profile,
				...profile
			});
			next = syncAvatarFromName(next);
			if (next.version != null) {
				next.version = normalizeVersion(next.version);
			}
			this.profile = next;
		},
		patchProfile(patch = {}) {
			this.setProfile(patch);
		},
		resetAuth() {
			this.auth = {
				...defaultAuthState
			};
		}
	}
});
