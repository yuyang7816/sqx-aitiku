/**
 * Tab 主包页 UI 状态内存缓存：reLaunch 切换 Tab 后保留滚动位置与筛选 Chip。
 * 进程内有效，退出登录时清空。
 */
import { TAB_KEYS } from '@/common/tabbar';

/** @type {Record<string, { scrollTop: number, activeChip: string }>} */
const tabStateMap = Object.create(null);

const createEmptyTabState = () => ({
	scrollTop: 0,
	activeChip: ''
});

/** 读取指定 Tab 的缓存快照（只读副本） */
export function getTabPageState(tabKey) {
	if (!TAB_KEYS.includes(tabKey)) {
		return createEmptyTabState();
	}
	if (!tabStateMap[tabKey]) {
		tabStateMap[tabKey] = createEmptyTabState();
	}
	const state = tabStateMap[tabKey];
	return {
		scrollTop: state.scrollTop,
		activeChip: state.activeChip
	};
}

/** 合并写入 Tab 页 UI 状态 */
export function patchTabPageState(tabKey, patch = {}) {
	if (!TAB_KEYS.includes(tabKey)) {
		return;
	}
	if (!tabStateMap[tabKey]) {
		tabStateMap[tabKey] = createEmptyTabState();
	}
	const state = tabStateMap[tabKey];
	if (patch.scrollTop !== undefined) {
		state.scrollTop = Math.max(0, Number(patch.scrollTop) || 0);
	}
	if (patch.activeChip !== undefined) {
		state.activeChip = String(patch.activeChip || '');
	}
}

/** 清空单个 Tab 缓存 */
export function clearTabPageState(tabKey) {
	if (!TAB_KEYS.includes(tabKey)) {
		return;
	}
	tabStateMap[tabKey] = createEmptyTabState();
}

/** 退出登录等场景：清空全部 Tab 缓存 */
export function clearAllTabPageStates() {
	TAB_KEYS.forEach((key) => {
		tabStateMap[key] = createEmptyTabState();
	});
}
