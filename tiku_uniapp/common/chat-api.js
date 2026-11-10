/**
 * AI 对话：POST /app/ai/chat 首条与多轮追问（真实 LLM，无 Mock 回退）；H5 优先 SSE，否则 POST + 打字轮播展示
 * 模块二十四 24.3 · AI 次数用尽：本地 assertAiChatQuota + 服务端 1050010 → syncAiQuotaFromApi + 页面 showAiQuotaExhaustedModal
 */
import config from '@/common/config';
import { handleUnauthorized, postJson, SILENT_REQUEST_OPTIONS } from '@/common/api';
import {
	checkAiChatQuota,
	getAuthState,
	getMemberState,
	showAiQuotaExhaustedModal,
	syncAiQuotaFromApi
} from '@/common/app-data';
import { AI_QUOTA_EXHAUSTED_CODE, buildAiQuotaExhaustedPatch } from '@/common/member-rules';
import { buildChatInitialContext } from '@/common/chat-entry';
import { buildAuthHeader, getToken } from '@/common/auth-token';
import { isH5Wechat } from '@/common/platform';

/** 未登录时禁止发起 AI 对话（需 JWT，与 server @Login 一致） */
export const AI_CHAT_LOGIN_REQUIRED_CODE = 401001;

export { AI_QUOTA_EXHAUSTED_CODE } from '@/common/member-rules';
/** 用户输入命中敏感词 */
export const AI_SENSITIVE_INPUT_CODE = 1050013;
/** AI 回复未通过内容审核 */
export const AI_SENSITIVE_REPLY_CODE = 1050014;

/** 是否为服务端 AI 业务错误（次数用尽 / 敏感词） */
export const isAiChatBizError = (error) =>
	Boolean(
		error &&
			(error.code === AI_QUOTA_EXHAUSTED_CODE ||
				error.code === AI_SENSITIVE_INPUT_CODE ||
				error.code === AI_SENSITIVE_REPLY_CODE)
	);

/** 发送前校验 getRemainingAiTimes；余量为 0 时抛出 1050010 */
const assertAiChatQuota = () => {
	const quota = checkAiChatQuota();
	if (!quota.ok && !quota.notReady) {
		const err = new Error('今日 AI 次数已用尽');
		err.msg = '今日 AI 次数已用尽';
		err.code = AI_QUOTA_EXHAUSTED_CODE;
		throw err;
	}
	return quota;
};

/** 发送成功后同步服务端 quota 快照 */
const applyAiQuotaAfterSuccess = (quota = null) => {
	if (quota) {
		syncAiQuotaFromApi(quota);
	}
};

/** 发送前校验已登录（POST /app/ai/chat 需 JWT） */
const assertChatLoggedIn = () => {
	const auth = getAuthState();
	if (!auth.loggedIn) {
		const err = new Error('请先登录后使用 AI 答疑');
		err.msg = '请先登录后使用 AI 答疑';
		err.code = AI_CHAT_LOGIN_REQUIRED_CODE;
		throw err;
	}
};

/**
 * 页面发送前预检：未登录 / 次数用尽时返回 false，避免先展示消息再撤回
 */
export const ensureChatSendAllowed = ({
	showQuotaModal = true,
	showLoginToast = true
} = {}) => {
	const quota = checkAiChatQuota();
	if (!quota.ok && !quota.notReady) {
		if (showQuotaModal) {
			showAiQuotaExhaustedModal();
		}
		return false;
	}
	const auth = getAuthState();
	if (!auth.loggedIn) {
		if (showLoginToast) {
			uni.showToast({
				title: '请先登录后使用 AI 答疑',
				icon: 'none'
			});
		}
		return false;
	}
	return true;
};

const SSE_CHAT_PATH = '/app/ai/chat/stream';
const TYPEWRITER_INTERVAL_MS = 20;
const SSE_TIMEOUT_MS = 8000;

/** 移动端微信 / iOS Safari 对 fetch 流式 SSE 支持不稳定，优先走 POST */
const shouldPreferPostChatTransport = () => {
	// #ifdef H5
	try {
		const ua = window.navigator.userAgent || '';
		if (isH5Wechat() || /iphone|ipad|ipod/i.test(ua)) {
			return true;
		}
	} catch (error) {
		return true;
	}
	// #endif
	return false;
};

/** 将服务端 AI 回复规范为展示文案 */
export const normalizeChatReply = (data = {}) => {
	const reply = data.reply || data.content || data.message || data.text || '';
	return String(reply).trim();
};

/** 将页面 messages 转为 API history（排除 loading 与空文案） */
export const buildChatHistory = (messages = []) => {
	return (Array.isArray(messages) ? messages : [])
		.filter((item) => item && !item.loading)
		.map((item) => ({
			role: item.role === 'ai' ? 'ai' : 'user',
			text: String(item.text || '').trim()
		}))
		.filter((item) => item.text);
};

/** POST 完成后按字轮播展示（非 SSE 时的流式体验） */
const emitReplyDelta = async (reply, onDelta) => {
	if (!onDelta || typeof onDelta !== 'function') {
		return reply;
	}
	let accumulated = '';
	for (const char of reply) {
		accumulated += char;
		onDelta(accumulated);
		// eslint-disable-next-line no-await-in-loop
		await new Promise((resolve) => setTimeout(resolve, TYPEWRITER_INTERVAL_MS));
	}
	return reply;
};

/**
 * H5：尝试 SSE 流式读取；端点不可用或非 event-stream 时返回 null
 */
const trySseChatReply = async ({ topic = '', message = '', history = [], onDelta = null } = {}) => {
	// #ifdef H5
	if (shouldPreferPostChatTransport()) {
		return null;
	}
	try {
		const token = getToken();
		if (!token) {
			return null;
		}
		const controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
		const timeoutId = controller
			? setTimeout(() => controller.abort(), SSE_TIMEOUT_MS)
			: null;
		const response = await fetch(`${config.APIHOST}${SSE_CHAT_PATH}`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				Accept: 'text/event-stream',
				...buildAuthHeader(token),
				token
			},
			body: JSON.stringify({
				topic: String(topic || '').trim(),
				message: String(message || '').trim(),
				history: Array.isArray(history) ? history : []
			}),
			signal: controller?.signal
		});
		if (timeoutId) {
			clearTimeout(timeoutId);
		}
		if (response.status === 401) {
			handleUnauthorized('登录已失效，请重新登录');
			return null;
		}
		const contentType = response.headers.get('content-type') || '';
		if (!response.ok || !contentType.includes('text/event-stream') || !response.body) {
			return null;
		}

		const reader = response.body.getReader();
		const decoder = new TextDecoder();
		let buffer = '';
		let fullReply = '';

		while (true) {
			const { done, value } = await reader.read();
			if (done) {
				break;
			}
			buffer += decoder.decode(value, { stream: true });
			const lines = buffer.split('\n');
			buffer = lines.pop() || '';
			for (const line of lines) {
				const trimmed = line.trim();
				if (!trimmed.startsWith('data:')) {
					continue;
				}
				const data = trimmed.slice(5).trim();
				if (!data || data === '[DONE]') {
					continue;
				}
				let chunk = '';
				try {
					const parsed = JSON.parse(data);
					chunk = normalizeChatReply(parsed);
				} catch (error) {
					chunk = data;
				}
				if (chunk) {
					fullReply += chunk;
					if (onDelta) {
						onDelta(fullReply);
					}
				}
			}
		}

		if (fullReply) {
			return { reply: fullReply, fromApi: true, quota: null };
		}
	} catch (error) {
		return null;
	}
	// #endif
	return null;
};

/**
 * 请求 AI 单轮回复（POST /app/ai/chat）
 * @param {{ topic?: string, message?: string, history?: object[] }} payload
 */
export const requestChatReply = async ({ topic = '', message = '', history = [] } = {}) => {
	const res = await postJson('/app/ai/chat', {
		topic: String(topic || '').trim(),
		message: String(message || '').trim(),
		history: Array.isArray(history) ? history : []
	}, SILENT_REQUEST_OPTIONS);
	if (res && res.code === 0) {
		const reply = normalizeChatReply(res.data);
		if (reply) {
			return {
				reply,
				fromApi: true,
				quota: (res.data && res.data.quota) || null
			};
		}
	}
	const err = new Error((res && res.msg) || 'AI 回复失败');
	err.msg = (res && res.msg) || 'AI 回复失败';
	if (res && res.code != null) {
		err.code = res.code;
	}
	throw err;
};

/** SSE 优先，否则 POST；POST 成功且提供 onDelta 时按字轮播 */
const requestChatReplyWithTransport = async ({
	topic = '',
	message = '',
	history = [],
	onDelta = null
} = {}) => {
	const sseResult = await trySseChatReply({ topic, message, history, onDelta });
	if (sseResult && sseResult.reply) {
		return sseResult;
	}

	const { reply, quota } = await requestChatReply({ topic, message, history });
	if (onDelta && reply) {
		await emitReplyDelta(reply, onDelta);
	}
	return { reply, fromApi: true, quota };
};

/**
 * 多轮追问：POST /app/ai/chat（SSE/POST），返回服务端真实 LLM 回复
 * @param {{ topic?: string, message?: string, history?: object[], onDelta?: (text: string) => void }} payload
 */
export const sendChatFollowUp = async ({
	topic = '',
	message = '',
	history = [],
	onDelta = null
} = {}) => {
	assertAiChatQuota();
	assertChatLoggedIn();

	try {
		const result = await requestChatReplyWithTransport({
			topic,
			message,
			history,
			onDelta
		});
		applyAiQuotaAfterSuccess(result.quota);
		return result;
	} catch (error) {
		if (error && error.code === AI_QUOTA_EXHAUSTED_CODE) {
			syncAiQuotaFromApi(buildAiQuotaExhaustedPatch(getMemberState()));
		}
		throw error;
	}
};

/**
 * 加载对话页初始消息：用户追问 + AI 首条回复（POST /app/ai/chat 真实 LLM）
 * @returns {Promise<{ topic: string, tip: string, draft: string, userMessage: string, aiMessage: string, fromApi: boolean }>}
 */
export const loadChatInitialMessages = async ({ topic = '', tip = '' } = {}) => {
	const context = buildChatInitialContext({ topic, tip });
	assertAiChatQuota();
	assertChatLoggedIn();

	try {
		const { reply, quota } = await requestChatReply({
			topic: context.topic,
			message: context.userMessage,
			history: []
		});
		applyAiQuotaAfterSuccess(quota);
		return {
			topic: context.topic,
			tip: context.tip,
			draft: context.draft,
			userMessage: context.userMessage,
			aiMessage: reply,
			fromApi: true
		};
	} catch (error) {
		if (error && error.code === AI_QUOTA_EXHAUSTED_CODE) {
			syncAiQuotaFromApi(buildAiQuotaExhaustedPatch(getMemberState()));
		}
		throw error;
	}
};
