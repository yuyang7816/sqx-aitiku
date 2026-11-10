/**
 * 模块十九 · app-data / auth
 * Storage: AUTH_KEY（aiQuestionAuthState）· Pinia userStore 同步
 * API: 无直接请求；logout 清 token（auth-token.js）
 */
export {
	getAuthState,
	saveAuthState,
	loginUser,
	logoutUser,
	performLogoutAndRedirect
} from './impl.js';
