/**
 * 模块十九 · app-data / launch
 * Storage: 读 AUTH_KEY + PROFILE_KEY 决策路由（文档 4.3）
 * 核心：getLaunchRoute
 */
export {
	LAUNCH_ROUTE_LOGIN,
	LAUNCH_ROUTE_SETUP,
	LAUNCH_ROUTE_HOME,
	ENTRY_ROUTES,
	SETUP_PLAN_ROUTE,
	SETUP_PLAN_EDIT_MODE,
	SETUP_PLAN_EDIT_PAGE,
	shouldSkipLaunchRedirectForSetupPlanEdit,
	shouldRedirectToSetupPlan,
	resolveLaunchRoute,
	getLaunchRoute,
	redirectAfterLogin
} from './impl.js';
