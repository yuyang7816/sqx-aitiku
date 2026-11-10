/**
 * 模块十九 · 会员态 Store（hydrate 自 Storage，跨页共享 VIP/配额）
 * 配额展示/扣减规则见 common/member-rules.js（文档 23.3）
 */
import { defineStore } from 'pinia';
import { MEMBER_KEY } from '@/common/storage-keys';
import {
	getRemainingAiTimesFromMember,
	getRemainingOcrTimesFromMember,
	getTodayString,
	resolveMemberQuotaState
} from '@/common/member-rules';

const defaultMemberState = {
	isVip: false,
	level: '普通会员',
	expireAt: '',
	aiDailyLimit: 0,
	aiUsedToday: 0,
	aiQuotaDate: getTodayString(),
	ocrDailyLimit: 0,
	ocrUsedToday: 0,
	ocrQuotaDate: getTodayString(),
	currentPlanName: '未开通',
	orderCount: 0
};

const parseDate = (value) => {
	if (!value) return null;
	const normalized = new Date(String(value).replace(/-/g, '/'));
	return Number.isNaN(normalized.getTime()) ? null : normalized;
};

const isMemberExpired = (expireAt = '') => {
	const expire = parseDate(String(expireAt || '').trim());
	if (!expire) {
		return false;
	}
	const today = parseDate(getTodayString());
	return today.getTime() > expire.getTime();
};

/** 与 app-data normalizeMemberState 规则一致（expireAt + 跨日配额） */
export const normalizeMemberState = (state = {}) => {
	const merged = resolveMemberQuotaState({
		...defaultMemberState,
		...state
	});
	if (!merged.isVip || !isMemberExpired(merged.expireAt)) {
		return merged;
	}
	return resolveMemberQuotaState({
		...merged,
		isVip: false,
		level: '普通会员'
	});
};

const readMemberFromStorage = () => {
	const saved = uni.getStorageSync(MEMBER_KEY);
	const raw = saved && typeof saved === 'object'
		? {
			...defaultMemberState,
			...saved
		}
		: {
			...defaultMemberState
		};
	return normalizeMemberState(raw);
};

export const useMemberStore = defineStore('member', {
	state: () => ({
		hydrated: false,
		member: {
			...defaultMemberState
		}
	}),
	getters: {
		isVip: (state) => state.member.isVip === true,
		remainingAiTimes: (state) => getRemainingAiTimesFromMember(state.member),
		remainingOcrTimes: (state) => getRemainingOcrTimesFromMember(state.member)
	},
	actions: {
		hydrateFromStorage() {
			this.member = readMemberFromStorage();
			this.hydrated = true;
		},
		setMember(state = {}) {
			this.member = normalizeMemberState(state);
		},
		patchMember(patch = {}) {
			this.setMember({
				...this.member,
				...patch
			});
		},
		/** expireAt 过期时降级并写回 Storage（由 app-data refreshMemberExpireState 触发） */
		refreshExpireState() {
			const normalized = normalizeMemberState(this.member);
			this.member = normalized;
			return normalized;
		}
	}
});
