<template>
	<view class="chat-page">
		<view class="chat-scroll">
			<view class="chat-body">
				<view v-if="welcomeText" class="welcome-card">
					<text>{{ welcomeText }}</text>
				</view>
				<view
					v-for="(item, index) in messages"
					:key="`${item.id || item.time}-${index}`"
					class="message-row"
					:class="item.senderRole === 'user' ? 'from-user' : 'from-staff'"
				>
					<view v-if="item.senderRole === 'staff'" class="mini-logo">
						<up-icon name="kefu-ermai" size="16" color="#FFFFFF"></up-icon>
					</view>
					<view class="message" :class="item.senderRole === 'user' ? 'right' : 'left'">
						<text>{{ item.content }}</text>
						<text class="message-time">{{ item.time }}</text>
					</view>
				</view>
			</view>
		</view>
		<view class="input-bar">
			<view class="input-shell">
				<textarea
					:value="draft"
					class="chat-input"
					auto-height
					maxlength="500"
					:disabled="sending"
					placeholder="输入消息，最多 500 字"
					placeholder-class="chat-placeholder"
					@input="handleDraftInput"
					@confirm="handleSend"
				/>
			</view>
			<view class="send-shell" :class="{ disabled: !canSend }" @tap="handleSend">
				<up-icon name="arrow-upward" size="18" color="#FFFFFF"></up-icon>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { getCustomerConfig } from '@/common/app-data';
	import {
		fetchKefuChatMessages,
		markKefuChatRead,
		sendKefuChatMessage
	} from '@/common/kefu-chat-api';
	import { computed, ref } from 'vue';
	import { onHide, onShow, onUnload } from '@dcloudio/uni-app';

	const messages = ref([]);
	const draft = ref('');
	const sending = ref(false);
	const welcomeText = ref('');
	let pollTimer = 0;

	const canSend = computed(() => Boolean(String(draft.value || '').trim()) && !sending.value);

	const handleDraftInput = (event) => {
		draft.value = String(event?.detail?.value ?? '');
	};

	const loadMessages = async () => {
		try {
			messages.value = await fetchKefuChatMessages();
			await markKefuChatRead();
		} catch (error) {
			const msg = error?.msg || error?.message || '加载消息失败';
			uni.showToast({ title: msg, icon: 'none' });
		}
	};

	const handleSend = async () => {
		const content = String(draft.value || '').trim();
		if (!content || sending.value) return;
		sending.value = true;
		try {
			const sent = await sendKefuChatMessage(content);
			draft.value = '';
			if (sent) {
				messages.value = [...messages.value, sent];
			}
			await loadMessages();
		} catch (error) {
			uni.showToast({ title: error?.msg || error?.message || '发送失败', icon: 'none' });
		} finally {
			sending.value = false;
		}
	};

	const schedulePoll = () => {
		clearInterval(pollTimer);
		pollTimer = setInterval(() => {
			loadMessages();
		}, 12000);
	};

	onShow(async () => {
		const config = getCustomerConfig();
		welcomeText.value = String(config.imWelcome || '').trim();
		if (!config.imEnabled) {
			uni.showToast({ title: '在线客服未开启', icon: 'none' });
			setTimeout(() => uni.navigateBack(), 800);
			return;
		}
		await loadMessages();
		schedulePoll();
	});

	onHide(() => clearInterval(pollTimer));
	onUnload(() => clearInterval(pollTimer));
</script>

<style scoped lang="scss">
.chat-page {
	min-height: 100vh;
	background: #f5f7fb;
	display: flex;
	flex-direction: column;
}

.chat-scroll {
	flex: 1;
	overflow: hidden;
}

.chat-body {
	padding: 24rpx;
}

.welcome-card {
	margin-bottom: 24rpx;
	padding: 20rpx 24rpx;
	border-radius: 20rpx;
	background: #eef4ff;
	color: #475467;
	font-size: 26rpx;
	line-height: 1.6;
}

.message-row {
	display: flex;
	align-items: flex-end;
	margin-bottom: 20rpx;

	&.from-user {
		justify-content: flex-end;
	}
}

.mini-logo {
	width: 56rpx;
	height: 56rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #386df5, #5b8cff);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 12rpx;
	flex-shrink: 0;
}

.message {
	max-width: 72%;
	padding: 20rpx 24rpx;
	border-radius: 24rpx;
	font-size: 28rpx;
	line-height: 1.6;
	word-break: break-word;

	&.left {
		background: #fff;
		color: #1f2937;
		border-top-left-radius: 8rpx;
	}

	&.right {
		background: #386df5;
		color: #fff;
		border-top-right-radius: 8rpx;
	}
}

.message-time {
	display: block;
	margin-top: 8rpx;
	font-size: 20rpx;
	opacity: 0.7;
}

.input-bar {
	display: flex;
	align-items: flex-end;
	gap: 16rpx;
	padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
	background: #fff;
	border-top: 1rpx solid #eef2f6;
}

.input-shell {
	flex: 1;
	background: #f5f7fb;
	border-radius: 24rpx;
	padding: 16rpx 20rpx;
}

.chat-input {
	width: 100%;
	min-height: 72rpx;
	max-height: 200rpx;
	font-size: 28rpx;
	line-height: 1.5;
}

.send-shell {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	background: #386df5;
	display: flex;
	align-items: center;
	justify-content: center;

	&.disabled {
		opacity: 0.45;
	}
}
</style>
