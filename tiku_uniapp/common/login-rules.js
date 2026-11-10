/**
 * 模块二十三 · 登录规则（文档 23.1）
 * - 协议必选：登录请求须传 agreedProtocol: true
 * - profile.ready 门禁：由 getLaunchRoute / App.vue 路由守卫实现
 * - 退出保留学习数据：logoutUser 不清理 LEARNING_STORAGE_KEYS
 */
import { LEARNING_STORAGE_KEYS } from '@/common/storage-keys';

/** 未勾选协议时的前端提示（与 login-error 350019 文案一致） */
export const AGREEMENT_TIP = '请先同意协议';

/** 服务端未同意协议错误码（AppError / login-error-codes.md） */
export const PROTOCOL_NOT_AGREED_CODE = 350019;

/**
 * 构建含 agreedProtocol 的登录请求体
 * @param {object} fields - phone/code/invite 等业务字段
 * @param {boolean} agreedProtocol - 用户已勾选协议，须为 true 才发起登录
 */
export const buildAuthLoginBody = (fields = {}, agreedProtocol = true) => ({
	...fields,
	agreedProtocol: agreedProtocol === true
});

/**
 * 校验退出登录未清除学习数据 Storage 键（供 verify-login-rules.mjs 静态引用）
 */
export const LOGOUT_PRESERVED_STORAGE_KEYS = LEARNING_STORAGE_KEYS;
