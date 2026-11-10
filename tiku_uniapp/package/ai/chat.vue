<template>
	<view class="chat-page">
		<view class="chat-bg">
			<view class="bg-orb bg-orb-a"></view>
			<view class="bg-orb bg-orb-b"></view>
		</view>

		<!-- #ifdef H5 -->
		<view class="chat-header">
			<view class="header-glass">
				<view class="back-shell" @tap="back">
					<up-icon name="arrow-left" size="18" color="#1F2937"></up-icon>
				</view>
				<view class="chat-logo">
					<up-icon name="kefu-ermai" size="22" color="#FFFFFF"></up-icon>
					<view class="logo-pulse"></view>
				</view>
				<view class="chat-head-copy">
					<text class="chat-title">AI 答疑</text>
					<view class="status-row">
						<view class="status-dot"></view>
						<text class="chat-status">智能助手 · 在线</text>
					</view>
				</view>
			</view>
		</view>
		<!-- #endif -->

		<scroll-view
			scroll-y
			class="chat-scroll"
			:style="scrollViewStyle"
			:scroll-top="scrollTop"
			:scroll-with-animation="true"
			:show-scrollbar="false"
		>
			<view class="chat-body">
				<view v-if="topic" class="topic-card">
					<view class="topic-badge">当前题目</view>
					<text class="topic-text">{{ topic }}</text>
					<view v-if="tip" class="topic-tip">
						<up-icon name="info-circle" size="12" color="#6366F1"></up-icon>
						<text>{{ tip }}</text>
					</view>
				</view>

				<view
					v-for="(item, index) in messages"
					:key="`${item.role}-${index}`"
					class="message-wrap"
					:class="item.role === 'ai' ? 'is-ai' : 'is-user'"
				>
					<view v-if="item.role === 'ai'" class="avatar ai-avatar">
						<up-icon name="kefu-ermai" size="16" color="#FFFFFF"></up-icon>
					</view>
					<view class="bubble-col">
						<text class="bubble-label">{{ item.role === 'ai' ? 'AI 助手' : '我' }}</text>
						<view class="message" :class="[item.role === 'ai' ? 'left' : 'right', item.loading ? 'loading' : '']">
							<view v-if="item.loading" class="typing-dots">
								<view class="dot"></view>
								<view class="dot"></view>
								<view class="dot"></view>
							</view>
							<text v-else class="message-text">{{ item.text }}</text>
						</view>
					</view>
					<view v-if="item.role === 'user'" class="avatar user-avatar">
						<up-icon name="account-fill" size="16" color="#386DF5"></up-icon>
					</view>
				</view>

				<view class="chat-footer-space"></view>
			</view>
		</scroll-view>

		<view class="input-bar">
			<view class="input-panel">
				<view class="tool-row">
					<view class="cam-shell" @tap="goOcr">
						<up-icon name="camera" size="18" color="#386DF5"></up-icon>
						<text>拍题</text>
					</view>
					<text class="input-hint">追问将结合上方题目上下文回答</text>
				</view>
				<view class="composer-row">
					<view class="input-shell">
						<textarea
							:value="draft"
							class="chat-input"
							auto-height
							:maxlength="CHAT_INPUT_MAX_LENGTH"
							:disabled="initialLoading || followUpLoading"
							confirm-type="send"
							placeholder="输入你的追问，例如：能再详细讲讲解题思路吗？"
							placeholder-class="chat-placeholder"
							@input="handleDraftInput"
							@confirm="sendFollowUp"
						/>
						<text class="input-count" :class="{ warn: draftCharCount >= CHAT_INPUT_MAX_LENGTH }">{{ draftCharCount }}/{{ CHAT_INPUT_MAX_LENGTH }}</text>
					</view>
					<view
						class="send-shell"
						:class="{ disabled: !canSend, active: canSend }"
						@tap.stop="sendFollowUp"
					>
						<up-icon name="arrow-upward" size="20" color="#FFFFFF"></up-icon>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		AI_CHAT_LOGIN_REQUIRED_CODE,
		AI_QUOTA_EXHAUSTED_CODE,
		AI_SENSITIVE_INPUT_CODE,
		AI_SENSITIVE_REPLY_CODE,
		buildChatHistory,
		ensureChatSendAllowed,
		loadChatInitialMessages,
		sendChatFollowUp
	} from '@/common/chat-api';
	import {
		buildChatInitialContext,
		parseChatEntryOptions
	} from '@/common/chat-entry';
	import { LAUNCH_ROUTE_LOGIN, showAiQuotaExhaustedModal, getAuthState } from '@/common/app-data';
	import { openOcrImagePicker } from '@/common/ocr-camera';
	import {
		computed,
		nextTick,
		ref,
		watch
	} from 'vue';
	import {
		onLoad,
		onReady
	} from '@dcloudio/uni-app';

	/** 追问输入框最大字数 */
	const CHAT_INPUT_MAX_LENGTH = 200;

	const topic = ref('');
	const tip = ref('');
	const messages = ref([]);
	const draft = ref('');
	const initialLoading = ref(false);
	const followUpLoading = ref(false);
	const scrollTop = ref(0);
	const scrollViewHeight = ref('');
	let scrollTick = 0;

	/** 小程序 scroll-view 必须显式高度，flex+height:0 会导致消息区域不可见 */
	const scrollViewStyle = computed(() => {
		if (scrollViewHeight.value) {
			return { height: scrollViewHeight.value };
		}
		return {};
	});

	const measureScrollViewHeight = () => {
		try {
			const sys = uni.getSystemInfoSync();
			const statusBarHeight = Number(sys.statusBarHeight || 0);
			const safeBottom = Number(sys.safeAreaInsets?.bottom || 0);
			// #ifdef H5
			const navBarHeight = statusBarHeight + 88;
			// #endif
			// #ifndef H5
			const navBarHeight = statusBarHeight + 44;
			// #endif
			const bottomBarHeight = 120 + safeBottom;
			const height = Math.max(Number(sys.windowHeight || 0) - navBarHeight - bottomBarHeight, 240);
			scrollViewHeight.value = `${height}px`;
		} catch (error) {
			scrollViewHeight.value = '70vh';
		}
	};

	const appendMessage = (item) => {
		messages.value = [...messages.value, item];
	};

	const patchMessageAt = (index, patch) => {
		if (index < 0 || index >= messages.value.length) {
			return;
		}
		const next = [...messages.value];
		next[index] = {
			...next[index],
			...patch
		};
		messages.value = next;
	};

	const scrollToBottom = () => {
		nextTick(() => {
			scrollTick += 1;
			scrollTop.value = scrollTick * 10000;
		});
	};

	watch(messages, () => scrollToBottom(), { deep: true });

	/** 截断超长输入，避免粘贴绕过 maxlength */
	const clampDraftText = (text = '') => {
		const normalized = String(text ?? '');
		if (normalized.length <= CHAT_INPUT_MAX_LENGTH) {
			return normalized;
		}
		return normalized.slice(0, CHAT_INPUT_MAX_LENGTH);
	};

	const draftCharCount = computed(() => String(draft.value || '').length);

	/** 输入变更：超长时截断并提示 */
	const handleDraftInput = (event) => {
		const value = String(event?.detail?.value ?? '');
		if (value.length > CHAT_INPUT_MAX_LENGTH) {
			draft.value = value.slice(0, CHAT_INPUT_MAX_LENGTH);
			uni.showToast({ title: '追问内容最多 200 字', icon: 'none' });
			return;
		}
		draft.value = value;
	};

	/** 解析路由参数，立即展示用户追问 + AI 加载占位，再请求 POST /app/ai/chat 真实 LLM 回复 */
	const applyChatEntryContext = async (options = {}) => {
		const entry = parseChatEntryOptions(options);
		const preview = buildChatInitialContext(entry);
		topic.value = preview.topic;
		tip.value = preview.tip;
		draft.value = clampDraftText(preview.draft);

		if (preview.emptyEntry) {
			messages.value = [];
			initialLoading.value = false;
			return;
		}

		messages.value = [
			{ role: 'user', text: preview.userMessage },
			{ role: 'ai', text: '', loading: true }
		];
		initialLoading.value = true;

		try {
			const initial = await loadChatInitialMessages(entry);
			topic.value = initial.topic;
			tip.value = initial.tip;
			draft.value = clampDraftText(initial.draft);
			messages.value = [
				{ role: 'user', text: initial.userMessage },
				{ role: 'ai', text: initial.aiMessage }
			];
		} catch (error) {
			if (error && error.code === AI_QUOTA_EXHAUSTED_CODE) {
				messages.value = [
					{ role: 'user', text: preview.userMessage }
				];
				showAiQuotaExhaustedModal();
				return;
			}
			if (error && error.code === AI_CHAT_LOGIN_REQUIRED_CODE) {
				messages.value = [
					{ role: 'user', text: preview.userMessage }
				];
				uni.showToast({
					title: (error && error.msg) || '请先登录后使用 AI 答疑',
					icon: 'none'
				});
				setTimeout(() => {
					uni.navigateTo({ url: LAUNCH_ROUTE_LOGIN });
				}, 600);
				return;
			}
			if (error && (error.code === AI_SENSITIVE_INPUT_CODE || error.code === AI_SENSITIVE_REPLY_CODE)) {
				messages.value = [
					{ role: 'user', text: preview.userMessage },
					{ role: 'ai', text: (error && error.msg) || '内容未通过审核，请修改后重试' }
				];
				return;
			}
			messages.value = [
				{ role: 'user', text: preview.userMessage },
				{ role: 'ai', text: (error && error.msg) || 'AI 回复失败，请稍后重试' }
			];
		} finally {
			initialLoading.value = false;
		}
	};

	onLoad((options) => {
		applyChatEntryContext(options);
	});

	onReady(() => {
		measureScrollViewHeight();
	});

	const canSend = computed(() => Boolean(draft.value.trim()) && !initialLoading.value && !followUpLoading.value);

	const back = () => uni.navigateBack();
	const goOcr = () => openOcrImagePicker({
		topic: topic.value,
		fromChat: true
	});
	/** 多轮追问：POST /app/ai/chat（H5 优先 SSE），展示服务端真实 LLM 回复 */
	const sendFollowUp = async () => {
		if (!canSend.value) {
			return;
		}
		const value = clampDraftText(draft.value).trim();
		if (!value) {
			return;
		}
		if (value.length > CHAT_INPUT_MAX_LENGTH) {
			uni.showToast({ title: '追问内容最多 200 字', icon: 'none' });
			return;
		}
		if (!ensureChatSendAllowed()) {
			const auth = getAuthState();
			if (!auth.loggedIn) {
				setTimeout(() => {
					uni.navigateTo({ url: LAUNCH_ROUTE_LOGIN });
				}, 600);
			}
			return;
		}

		const history = buildChatHistory(messages.value);
		appendMessage({ role: 'user', text: value });
		draft.value = '';
		const aiIndex = messages.value.length;
		appendMessage({ role: 'ai', text: '', loading: true });
		followUpLoading.value = true;

		try {
			const { reply } = await sendChatFollowUp({
				topic: topic.value,
				message: value,
				history,
				onDelta: (partial) => {
					patchMessageAt(aiIndex, { role: 'ai', text: partial, loading: false });
				}
			});
			patchMessageAt(aiIndex, { role: 'ai', text: reply, loading: false });
		} catch (error) {
			if (error && error.code === AI_QUOTA_EXHAUSTED_CODE) {
				patchMessageAt(aiIndex, {
					role: 'ai',
					text: (error && error.msg) || '今日 AI 次数已用尽',
					loading: false
				});
				showAiQuotaExhaustedModal();
				return;
			}
			if (error && error.code === AI_CHAT_LOGIN_REQUIRED_CODE) {
				patchMessageAt(aiIndex, {
					role: 'ai',
					text: (error && error.msg) || '请先登录后使用 AI 答疑',
					loading: false
				});
				setTimeout(() => {
					uni.navigateTo({ url: LAUNCH_ROUTE_LOGIN });
				}, 600);
				return;
			}
			if (error && (error.code === AI_SENSITIVE_INPUT_CODE || error.code === AI_SENSITIVE_REPLY_CODE)) {
				patchMessageAt(aiIndex, {
					role: 'ai',
					text: (error && error.msg) || '内容未通过审核，请修改后重试',
					loading: false
				});
				return;
			}
			patchMessageAt(aiIndex, {
				role: 'ai',
				text: (error && error.msg) || '发送失败，请稍后重试',
				loading: false
			});
		} finally {
			followUpLoading.value = false;
		}
	};
</script>

<style lang="scss" scoped>
	.chat-page {
		position: relative;
		min-height: 100vh;
		background: #f3f6fc;
		display: flex;
		flex-direction: column;
		overflow: hidden;
	}

	.chat-bg {
		position: absolute;
		inset: 0;
		pointer-events: none;
		overflow: hidden;
	}

	.bg-orb {
		position: absolute;
		border-radius: 50%;
		filter: blur(80rpx);
		opacity: 0.55;
	}

	.bg-orb-a {
		width: 420rpx;
		height: 420rpx;
		top: -120rpx;
		right: -80rpx;
		background: rgba(99, 102, 241, 0.28);
	}

	.bg-orb-b {
		width: 360rpx;
		height: 360rpx;
		left: -100rpx;
		bottom: 180rpx;
		background: rgba(56, 109, 245, 0.18);
	}

	.chat-header {
		position: fixed;
		left: 0;
		right: 0;
		top: 0;
		z-index: 2;
		padding: calc(16rpx + env(safe-area-inset-top)) 24rpx 12rpx;
	}

	.header-glass {
		display: flex;
		align-items: center;
		gap: 18rpx;
		padding: 16rpx 18rpx;
		border-radius: 28rpx;
		background: rgba(255, 255, 255, 0.78);
		border: 2rpx solid rgba(255, 255, 255, 0.92);
		box-shadow: 0 16rpx 40rpx rgba(36, 48, 65, 0.06);
		backdrop-filter: blur(18rpx);
	}

	.back-shell {
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: rgba(243, 246, 252, 0.96);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.chat-logo {
		position: relative;
		width: 72rpx;
		height: 72rpx;
		border-radius: 22rpx;
		background: linear-gradient(145deg, #4f46e5 0%, #386df5 48%, #6366f1 100%);
		box-shadow: 0 18rpx 32rpx rgba(56, 109, 245, 0.28);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.logo-pulse {
		position: absolute;
		right: 6rpx;
		bottom: 6rpx;
		width: 14rpx;
		height: 14rpx;
		border-radius: 50%;
		background: #22c55e;
		border: 3rpx solid #fff;
		box-shadow: 0 0 0 6rpx rgba(34, 197, 94, 0.18);
	}

	.chat-head-copy {
		min-width: 0;
		flex: 1;
	}

	.chat-title {
		display: block;
		font-size: 32rpx;
		font-weight: 800;
		line-height: 1.2;
		color: #182230;
		letter-spacing: 0.5rpx;
	}

	.status-row {
		margin-top: 8rpx;
		display: flex;
		align-items: center;
		gap: 8rpx;
	}

	.status-dot {
		width: 10rpx;
		height: 10rpx;
		border-radius: 50%;
		background: #22c55e;
		animation: pulse 1.8s ease-in-out infinite;
	}

	.chat-status {
		font-size: 22rpx;
		line-height: 1.3;
		color: #64748b;
	}

	.chat-scroll {
		position: relative;
		z-index: 1;
		flex: 1;
		min-height: 0;
	}

	/* #ifdef H5 */
	.chat-scroll {
		height: 0;
	}
	/* #endif */

	.chat-body {
		padding: 8rpx 24rpx 0;
	}

	/* #ifdef H5 */
	.chat-body {
		padding-top: 176rpx;
	}
	/* #endif */

	.topic-card {
		margin-bottom: 28rpx;
		padding: 24rpx 26rpx;
		border-radius: 28rpx;
		background: linear-gradient(135deg, rgba(255, 255, 255, 0.96) 0%, rgba(246, 249, 255, 0.96) 100%);
		border: 2rpx solid rgba(219, 228, 248, 0.9);
		box-shadow: 0 14rpx 36rpx rgba(56, 109, 245, 0.08);
	}

	.topic-badge {
		display: inline-flex;
		align-items: center;
		padding: 6rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(56, 109, 245, 0.1);
		color: #386df5;
		font-size: 20rpx;
		font-weight: 700;
		line-height: 1.3;
	}

	.topic-text {
		display: block;
		margin-top: 14rpx;
		font-size: 28rpx;
		font-weight: 700;
		line-height: 1.65;
		color: #1e293b;
	}

	.topic-tip {
		margin-top: 14rpx;
		padding: 14rpx 16rpx;
		border-radius: 18rpx;
		background: rgba(238, 244, 255, 0.88);
		display: flex;
		align-items: flex-start;
		gap: 8rpx;
		font-size: 22rpx;
		line-height: 1.6;
		color: #475569;
	}

	.topic-tip text {
		flex: 1;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 3;
		overflow: hidden;
	}

	.message-wrap {
		display: flex;
		align-items: flex-start;
		gap: 14rpx;
		margin-bottom: 28rpx;
	}

	.message-wrap.is-user {
		justify-content: flex-end;
	}

	.avatar {
		width: 52rpx;
		height: 52rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.ai-avatar {
		background: linear-gradient(145deg, #4f46e5 0%, #6366f1 100%);
		box-shadow: 0 10rpx 20rpx rgba(79, 70, 229, 0.22);
	}

	.user-avatar {
		background: rgba(255, 255, 255, 0.96);
		border: 2rpx solid rgba(219, 228, 248, 0.95);
		box-shadow: 0 8rpx 18rpx rgba(36, 48, 65, 0.05);
	}

	.bubble-col {
		max-width: calc(100% - 132rpx);
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.is-user .bubble-col {
		align-items: flex-end;
	}

	.bubble-label {
		font-size: 20rpx;
		line-height: 1.2;
		color: #94a3b8;
		padding: 0 6rpx;
	}

	.message {
		padding: 20rpx 24rpx;
		border-radius: 24rpx;
		font-size: 26rpx;
		line-height: 1.68;
		word-break: break-word;
	}

	.message-text {
		display: block;
		white-space: pre-wrap;
	}

	.message.left {
		background: rgba(255, 255, 255, 0.96);
		border: 2rpx solid rgba(226, 232, 240, 0.95);
		box-shadow: 0 12rpx 28rpx rgba(15, 23, 42, 0.05);
		color: #1f2937;
		border-top-left-radius: 8rpx;
	}

	.message.right {
		background: linear-gradient(135deg, #386df5 0%, #4f46e5 100%);
		box-shadow: 0 14rpx 28rpx rgba(56, 109, 245, 0.22);
		color: #ffffff;
		border-top-right-radius: 8rpx;
	}

	.message.loading {
		min-width: 120rpx;
		padding: 24rpx 28rpx;
	}

	.typing-dots {
		display: flex;
		align-items: center;
		gap: 10rpx;
		height: 24rpx;
	}

	.dot {
		width: 12rpx;
		height: 12rpx;
		border-radius: 50%;
		background: #94a3b8;
		animation: bounce 1.2s ease-in-out infinite;
	}

	.dot:nth-child(2) {
		animation-delay: 0.15s;
	}

	.dot:nth-child(3) {
		animation-delay: 0.3s;
	}

	.chat-footer-space {
		height: calc(240rpx + env(safe-area-inset-bottom));
	}

	.input-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 3;
		padding: 12rpx 20rpx calc(16rpx + env(safe-area-inset-bottom));
		background: linear-gradient(180deg, rgba(243, 246, 252, 0) 0%, rgba(243, 246, 252, 0.92) 24%, #f3f6fc 100%);
	}

	.input-panel {
		padding: 16rpx 18rpx 18rpx;
		border-radius: 32rpx;
		background: rgba(255, 255, 255, 0.94);
		border: 2rpx solid rgba(226, 232, 240, 0.92);
		box-shadow: 0 18rpx 48rpx rgba(36, 48, 65, 0.08);
		backdrop-filter: blur(16rpx);
	}

	.tool-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		margin-bottom: 14rpx;
	}

	.cam-shell {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 10rpx 18rpx;
		border-radius: 999rpx;
		background: rgba(238, 244, 255, 0.96);
		color: #386df5;
		font-size: 22rpx;
		font-weight: 700;
		flex-shrink: 0;
	}

	.input-hint {
		flex: 1;
		min-width: 0;
		font-size: 20rpx;
		line-height: 1.4;
		color: #94a3b8;
		text-align: right;
	}

	.composer-row {
		display: flex;
		align-items: flex-end;
		gap: 12rpx;
	}

	.input-shell {
		flex: 1;
		min-width: 0;
		padding: 16rpx 20rpx 12rpx;
		border-radius: 24rpx;
		background: #f8fafc;
		border: 2rpx solid rgba(226, 232, 240, 0.92);
	}

	.chat-input {
		width: 100%;
		min-height: 72rpx;
		max-height: 200rpx;
		padding: 0;
		background: transparent;
		color: #1f2937;
		font-size: 26rpx;
		line-height: 1.55;
	}

	.chat-placeholder {
		color: #b1b9c7;
	}

	.input-count {
		display: block;
		margin-top: 8rpx;
		text-align: right;
		font-size: 20rpx;
		line-height: 1.2;
		color: #b1b9c7;
	}

	.input-count.warn {
		color: #f59e0b;
	}

	.send-shell {
		width: 76rpx;
		height: 76rpx;
		border-radius: 22rpx;
		background: #cbd5e1;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
	}

	.send-shell.active {
		background: linear-gradient(145deg, #386df5 0%, #4f46e5 100%);
		box-shadow: 0 16rpx 28rpx rgba(56, 109, 245, 0.28);
	}

	.send-shell.disabled {
		opacity: 0.72;
	}

	@keyframes pulse {
		0%, 100% {
			opacity: 1;
			transform: scale(1);
		}

		50% {
			opacity: 0.55;
			transform: scale(0.88);
		}
	}

	@keyframes bounce {
		0%, 80%, 100% {
			transform: translateY(0);
			opacity: 0.45;
		}

		40% {
			transform: translateY(-8rpx);
			opacity: 1;
		}
	}

	@media screen and (max-width: 375px) {
		.chat-body {
			padding-left: 18rpx;
			padding-right: 18rpx;
		}

		.chat-header {
			padding-left: 18rpx;
			padding-right: 18rpx;
		}

		.message {
			font-size: 24rpx;
		}

		.input-bar {
			padding-left: 16rpx;
			padding-right: 16rpx;
		}
	}
</style>
