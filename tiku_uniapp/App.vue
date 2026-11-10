<template></template>

<script setup>
	import {
		onLaunch,
		onShow,
		onHide
	} from '@dcloudio/uni-app';
	import {
		ENTRY_ROUTES,
		LAUNCH_ROUTE_SETUP,
		getAuthState,
		getLaunchRoute,
		getUserProfile,
		refreshMemberExpireState,
		shouldRedirectToSetupPlan,
		shouldSkipLaunchRedirectForSetupPlanEdit
	} from '@/common/app-data';
	import { getToken } from '@/common/auth-token';
	import { fetchAndMergeCustomerConfig } from '@/common/customer-config';
	import { fetchWechatPublicConfig } from '@/common/wechat-config';
	import { syncNotebookAndCollectionFromApi } from '@/common/cloud-sync';
	import { fetchAndMergeUserInfo } from '@/common/user-info';
	import { captureInviteFromLaunchOptions } from '@/common/invite-bind';
	import {
		captureDeepLinkFromLaunchOptions,
		isDeepLinkRoute
	} from '@/common/deep-link';
	import { runStorageMigration } from '@/common/storage-migration';
	import { hydrateAllStores } from '@/stores/index';
	import fontUtil from '@/uni_modules/uview-plus/components/u-icon/util.js';

	/** 文档 4.3：onLaunch/onShow 路由守卫；未就绪态禁止跳过计划页 */
	const syncEntryRoute = () => {
		const pages = getCurrentPages();
		const currentRoute = pages[pages.length - 1]?.route;
		if (!currentRoute) {
			return;
		}
		// 深链落地页（海报分享/扫码）放行，避免被入口守卫 reLaunch 走
		if (isDeepLinkRoute(currentRoute)) {
			return;
		}
		const authState = getAuthState();
		const userProfile = getUserProfile();
		// 已登录但未就绪：除计划页外一律 reLaunch 至 plan
		if (shouldRedirectToSetupPlan(currentRoute, authState, userProfile)) {
			uni.reLaunch({
				url: LAUNCH_ROUTE_SETUP
			});
			return;
		}
		const pageOptions = pages[pages.length - 1]?.options || {};
		// 模块二十二 P1#14 · 已就绪用户编辑计划时不被入口守卫误拦回首页
		if (shouldSkipLaunchRedirectForSetupPlanEdit(currentRoute, pageOptions, userProfile)) {
			return;
		}
		const targetRoute = getLaunchRoute().replace(/^\//, '');
		if (!ENTRY_ROUTES.includes(currentRoute) || currentRoute === targetRoute) {
			return;
		}
		uni.reLaunch({
			url: `/${targetRoute}`
		});
	};

	onLaunch(async (options) => {
		// H5 仅依赖下方 @font-face，避免 loadFontFace 与 CSS 字体双通道冲突
		// #ifndef H5
		fontUtil.loadFont();
		// #endif
		// 文档 19.3：旧 Storage 结构升级后再 hydrate，避免 Pinia 读到 v0 脏数据
		runStorageMigration();
		hydrateAllStores();
		captureInviteFromLaunchOptions(options || {});
		captureDeepLinkFromLaunchOptions(options || {});
		// 有 token 即拉取服务端 profile（含仅留 token、profile 被清空的换机/重装场景）
		if (getToken()) {
			await fetchAndMergeUserInfo();
			await syncNotebookAndCollectionFromApi();
		}
		// 启动拉取客服配置（无需登录），缓存至 aiQuestionCustomerConfig
		fetchAndMergeCustomerConfig();
		fetchWechatPublicConfig().catch(() => {});
		setTimeout(syncEntryRoute, 0);
	});
	onShow((options) => {
		captureInviteFromLaunchOptions(options || {});
		captureDeepLinkFromLaunchOptions(options || {});
		// 模块二十二 P1#13 · 回前台校验 expireAt，过期降级 isVip 并同步 Hero/广告/次数 UI
		refreshMemberExpireState();
		syncEntryRoute();
	});
	onHide(() => {
	});
</script>

<style lang="scss">
	/*每个页面公共css */
	//uview-plus公共样式
	@import "@/uni_modules/uview-plus/index.scss";
	//自定义公共样式
	@import "@/static/css/main.css";
	@import "@/static/css/iconfont.css";
	@import "@/static/css/theme.scss";

	page {
		background-color: #F5F7FF;
	}
</style>
