<template>
	<view class="wechat-page">
		<view class="wechat-body">
			<view v-if="qrPreview" class="qr-card">
				<image :src="qrPreview" mode="aspectFit" class="qr-image" show-menu-by-longpress />
				<text class="qr-tip">长按识别二维码添加客服微信</text>
			</view>
			<view v-if="wechatId" class="id-card">
				<text class="id-label">客服微信号</text>
				<text class="id-value">{{ wechatId }}</text>
				<view class="copy-btn" @tap="copyWechatId">复制微信号</view>
			</view>
			<view v-if="!qrPreview && !wechatId" class="empty-box">
				<text>暂未配置微信客服信息</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { getCustomerConfig } from '@/common/app-data';
	import { buildApiUrl } from '@/common/api';
	import { computed } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';
	const qrPreview = computed(() => {
		const raw = String(getCustomerConfig().wechatQrUrl || '').trim();
		if (!raw) return '';
		if (/^https?:\/\//i.test(raw)) return raw;
		return buildApiUrl(raw.startsWith('/') ? raw : `/${raw}`);
	});

	const wechatId = computed(() => String(getCustomerConfig().wechatId || '').trim());

	const copyWechatId = () => {
		if (!wechatId.value) return;
		uni.setClipboardData({
			data: wechatId.value,
			success: () => uni.showToast({ title: '微信号已复制', icon: 'none' })
		});
	};

	onLoad(() => {
		if (!wechatId.value && !qrPreview.value) {
			uni.showToast({ title: '微信客服未配置', icon: 'none' });
		}
	});
</script>

<style scoped lang="scss">
.wechat-page {
	min-height: 100vh;
	background: #f5f7fb;
}

.wechat-body {
	padding: 32rpx;
}

.qr-card,
.id-card,
.empty-box {
	background: #fff;
	border-radius: 24rpx;
	padding: 32rpx;
	margin-bottom: 24rpx;
	text-align: center;
}

.qr-image {
	width: 420rpx;
	height: 420rpx;
	margin: 0 auto;
}

.qr-tip,
.id-label {
	display: block;
	margin-top: 16rpx;
	font-size: 26rpx;
	color: #667085;
}

.id-value {
	display: block;
	margin: 20rpx 0;
	font-size: 40rpx;
	font-weight: 700;
	color: #1f2937;
}

.copy-btn {
	display: inline-flex;
	padding: 16rpx 40rpx;
	border-radius: 999rpx;
	background: #386df5;
	color: #fff;
	font-size: 28rpx;
}

.empty-box {
	color: #98a2b3;
	font-size: 28rpx;
}
</style>
