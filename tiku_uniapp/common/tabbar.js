/**
 * 底部 Tab 导航配置：5 项 Tab 的路由映射、reLaunch 切换与当前 Tab 识别。
 */
import {
	AI_TAB_PAGE,
	HOME_TAB_PAGE,
	MY_TAB_PAGE,
	NOTEBOOK_TAB_PAGE,
	PRACTICE_TAB_PAGE
} from '@/common/routes';

/** Tab 标识顺序与底部栏展示一致 */
export const TAB_KEYS = ['home', 'practice', 'ai', 'notebook', 'my'];

/** Tab 标识 → 主包页路由（切换时用 reLaunch 清空页面栈） */
export const TAB_ROUTE_MAP = {
	home: HOME_TAB_PAGE,
	practice: PRACTICE_TAB_PAGE,
	ai: AI_TAB_PAGE,
	notebook: NOTEBOOK_TAB_PAGE,
	my: MY_TAB_PAGE
};

/** 底部栏展示配置（与 app-tabbar 模板一致） */
export const TAB_ITEMS = [
	{ key: 'home', text: '首页', icon: 'home-fill' },
	{ key: 'practice', text: '刷题', icon: 'grid-fill' },
	{ key: 'ai', text: 'AI学习', mode: 'midButton' },
	{ key: 'notebook', text: '错题', icon: 'bookmark-fill' },
	{ key: 'my', text: '我的', icon: 'account-fill' }
];

const TAB_PAGE_ROUTE_MAP = Object.fromEntries(
	Object.entries(TAB_ROUTE_MAP).map(([key, url]) => [normalizeTabRoute(url), key])
);

/** 规范化路由路径（去 query、补前导斜杠） */
export function normalizeTabRoute(url = '') {
	const path = String(url).split('?')[0].trim();
	if (!path) {
		return '';
	}
	return path.startsWith('/') ? path : `/${path}`;
}

/** 判断 Tab 标识是否合法 */
export function isValidTabKey(key) {
	return TAB_KEYS.includes(key);
}

/** 根据 Tab 标识取主包页路由 */
export function getTabPageUrl(key) {
	return TAB_ROUTE_MAP[key] || '';
}

/** 判断路由是否为主包 Tab 页 */
export function isTabPageRoute(url) {
	return !!resolveTabKeyFromRoute(url);
}

/** 根据路由反查 Tab 标识（当前 Tab 识别） */
export function resolveTabKeyFromRoute(url) {
	return TAB_PAGE_ROUTE_MAP[normalizeTabRoute(url)] || '';
}

/** 读取当前页面栈顶路由对应的 Tab 标识 */
export function resolveCurrentTabKey() {
	const pages = getCurrentPages();
	const page = pages[pages.length - 1];
	if (!page?.route) {
		return '';
	}
	return resolveTabKeyFromRoute(`/${page.route}`);
}

/**
 * 切换至指定 Tab 页（reLaunch 清空页面栈）
 * @returns {boolean} 是否已发起跳转
 */
export function switchTabPage(key) {
	if (!isValidTabKey(key)) {
		return false;
	}
	const url = getTabPageUrl(key);
	if (!url) {
		return false;
	}
	uni.reLaunch({ url });
	return true;
}
