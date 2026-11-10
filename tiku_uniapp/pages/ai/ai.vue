<template>
	<view class="app-page ai-page">
		<view class="page-scroll">
			<view class="screen ai-screen">
				<view class="hero-panel">
					<view class="hero-decoration hero-decoration--top"></view>
					<view class="hero-decoration hero-decoration--bottom"></view>
					<view class="hero-orbit" aria-hidden="true">
						<view class="hero-orbit-core">
							<up-icon name="camera-fill" size="24" color="#FFFFFF"></up-icon>
						</view>
						<view class="hero-orbit-ring hero-orbit-ring--outer"></view>
						<view class="hero-orbit-ring hero-orbit-ring--inner"></view>
					</view>
					<view class="hero-head">
						<view>
							<text class="hero-eyebrow">AI LAB</text>
							<view class="hero-title-row">
								<text class="hero-title">AI 学习中心</text>
								<view class="hero-badge">
									<up-icon name="checkmark-circle-fill" size="12" color="#7EF0C1"></up-icon>
									<text>{{ member.isVip ? '会员模式' : '每日免费可用' }}</text>
								</view>
							</view>
							<text class="hero-sub">拍题 / 答疑 / 分析</text>
						</view>
					</view>

					<view class="hero-actions">
						<view class="hero-primary-action" @tap="openOcrCamera">
							<view class="hero-primary-copy">
								<text class="hero-primary-title">拍照搜题</text>
							</view>
							<view class="hero-primary-go">
								<text>立即开始</text>
								<up-icon name="arrow-right" size="14" color="#2E6BFF"></up-icon>
							</view>
						</view>
					</view>
				</view>

				<view class="tool-section">
					<view class="section-head">
						<view>
							<text class="section-title">学习工具</text>
						</view>
					</view>
					<view class="tool-grid">
						<view class="tool-card tool-card--chat" @tap="goToAiChat">
							<view class="tool-card-top">
								<view class="tool-icon tool-icon--green">
									<up-icon name="chat-fill" size="20" color="#20A65A"></up-icon>
								</view>
								<view class="tool-arrow">
									<up-icon name="arrow-right" size="14" color="#2A6AF5"></up-icon>
								</view>
							</view>
							<text class="tool-title">AI 答疑</text>
							<view class="tool-tag-row">
								<text class="tool-tag">即时追问</text>
							</view>
						</view>
						<view class="tool-card tool-card--analysis" @tap="go('/package/analysis/detail')">
							<view class="tool-card-top">
								<view class="tool-icon tool-icon--violet">
									<up-icon name="file-text-fill" size="20" color="#8758F6"></up-icon>
								</view>
								<view class="tool-arrow">
									<up-icon name="arrow-right" size="14" color="#2A6AF5"></up-icon>
								</view>
							</view>
							<text class="tool-title">学情分析</text>
							<view class="tool-tag-row">
								<text class="tool-tag">薄弱点</text>
							</view>
						</view>
					</view>
				</view>

				<view class="record-section">
					<view class="section-head">
						<text class="section-title">最近搜题记录</text>
						<view class="section-head-side">
							<text class="section-badge">{{ recordCountText }}</text>
							<text class="minor-link" :class="{ disabled: clearingRecords }" @tap="clearRecords">清空</text>
						</view>
					</view>

					<view v-if="records.length" class="record-list">
						<view v-for="(item, index) in records" :key="`${item.title}-${index}`" class="record-card" @tap="openRecord(item)">
							<view class="record-avatar">
								<image
									v-if="getRecordThumb(item)"
									:src="getRecordThumb(item)"
									mode="aspectFill"
									class="record-thumb"
								/>
								<up-icon v-else name="kefu-ermai" size="18" color="#FFFFFF"></up-icon>
							</view>
							<view class="record-copy">
								<view class="record-top">
									<view class="record-source-chip">{{ item.source || '拍照搜题' }}</view>
									<text class="record-time">{{ item.time }}</text>
								</view>
								<view class="record-title">{{ item.title }}</view>
							</view>
							<text class="record-link" @tap.stop="askAi(item)">继续追问</text>
						</view>
					</view>
					<view v-else class="empty-records">
						<view class="empty-mark">
							<up-icon name="search" size="34" color="#9EB0D2"></up-icon>
						</view>
						<text class="empty-title">还没有搜题记录</text>
						<view class="empty-action" @tap="openOcrCamera">
							<text>去搜题</text>
							<up-icon name="arrow-right" size="14" color="#386DF5"></up-icon>
						</view>
					</view>
				</view>
			</view>
		</view>

		<up-popup
			:show="showSourcePopup"
			mode="bottom"
			round="32"
			:safeAreaInsetBottom="true"
			:zIndex="1400"
			@close="closeSourcePopup"
		>
			<view class="source-popup">
				<view class="source-popup-head">
					<view>
						<text class="source-popup-title">选择搜题方式</text>
						<text class="source-popup-sub">拍照或导入</text>
					</view>
					<view class="source-popup-close" @tap="closeSourcePopup">
						<up-icon name="close" size="16" color="#667085"></up-icon>
					</view>
				</view>
				<view class="source-option-list">
					<view class="source-option-card" @tap="pickSource('camera')">
						<view class="source-option-icon source-option-icon--camera">
							<up-icon name="camera-fill" size="22" color="#386DF5"></up-icon>
						</view>
						<view class="source-option-copy">
							<text class="source-option-title">拍照搜题</text>
						</view>
						<up-icon name="arrow-right" size="16" color="#98A2B3"></up-icon>
					</view>
					<view class="source-option-card" @tap="pickSource('album')">
						<view class="source-option-icon source-option-icon--album">
							<up-icon name="photo" size="22" color="#7C3AED"></up-icon>
						</view>
						<view class="source-option-copy">
							<text class="source-option-title">从相册选择</text>
						</view>
						<up-icon name="arrow-right" size="16" color="#98A2B3"></up-icon>
					</view>
				</view>
			</view>
		</up-popup>
		<app-tabbar current="ai"></app-tabbar>
	</view>
</template>

<script setup>
	import AppTabbar from '@/components/app-tabbar/app-tabbar.vue';
	import { computed, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { clearSearchRecords, getAuthState, getMemberState, getSearchRecords, LAUNCH_ROUTE_LOGIN, syncSearchRecordsFromApi } from '@/common/app-data';
	import { ensureChatSendAllowed } from '@/common/chat-api';
	import { pickOcrImageBySource } from '@/common/ocr-camera';
	import { resolveOcrImageUrl } from '@/common/ocr-search';
	import { useTabPageState } from '@/common/use-tab-page-state';

	useTabPageState('ai');

	const records = ref(getSearchRecords());
	const member = ref(getMemberState());
	const clearingRecords = ref(false);
	const showSourcePopup = ref(false);

	onShow(async () => {
		member.value = getMemberState();
		records.value = await syncSearchRecordsFromApi();
	});

	const recordCountText = computed(() => {
		const count = records.value.length;
		return count ? `${count} 条` : '暂无';
	});

	const openOcrCamera = () => {
		showSourcePopup.value = true;
	};

	const closeSourcePopup = () => {
		showSourcePopup.value = false;
	};

	const pickSource = (type) => {
		closeSourcePopup();
		pickOcrImageBySource({
			sourceType: type === 'camera' ? ['camera'] : ['album']
		});
	};

	const go = (url) => {
		if (url.startsWith('/pages/')) {
			uni.reLaunch({ url });
			return;
		}
		uni.navigateTo({ url });
	};

	const goToAiChat = () => {
		if (!ensureChatSendAllowed()) {
			if (!getAuthState().loggedIn) {
				setTimeout(() => {
					uni.navigateTo({ url: LAUNCH_ROUTE_LOGIN });
				}, 600);
			}
			return;
		}
		go('/package/ai/chat');
	};

	const clearRecords = async () => {
		if (clearingRecords.value) {
			return;
		}
		clearingRecords.value = true;
		try {
			records.value = await clearSearchRecords();
			uni.showToast({ title: '记录已清空', icon: 'none' });
		} finally {
			clearingRecords.value = false;
		}
	};

	const askAi = (item) => {
		const topic = encodeURIComponent(item.title);
		go(`/package/ai/chat?topic=${topic}`);
	};

	const openRecord = (item) => {
		const query = [];
		const topicText = String(item.title || '').trim();
		if (topicText) {
			query.push(`topic=${encodeURIComponent(topicText)}`);
		}
		const imageUrl = String(item.imageUrl || '').trim();
		if (imageUrl) {
			query.push(`imageUrl=${encodeURIComponent(imageUrl)}`);
		}
		const url = query.length ? `/package/question/ocr-result?${query.join('&')}` : '/package/question/ocr-result';
		go(url);
	};

	const getRecordThumb = (item = {}) => {
		const imageUrl = String(item.imageUrl || '').trim();
		return imageUrl ? resolveOcrImageUrl(imageUrl) : '';
	};
</script>

<style lang="scss" scoped>
	.ai-page {
		background:
			radial-gradient(circle at top left, rgba(87, 132, 255, 0.14) 0, rgba(87, 132, 255, 0) 34%),
			linear-gradient(180deg, #eef4ff 0, #f6f8fc 260rpx, #f5f7fb 100%);
	}

	.ai-screen {
		padding-top: 12rpx;
		padding-bottom: calc(44rpx + env(safe-area-inset-bottom));
	}

	.hero-panel {
		position: relative;
		padding: 26rpx;
		border-radius: 34rpx;
		background: linear-gradient(135deg, #1d2f5f 0%, #365ccf 46%, #6d52ee 100%);
		box-shadow: 0 22rpx 44rpx rgba(42, 67, 142, 0.2);
		overflow: hidden;
	}

	.hero-decoration {
		position: absolute;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.08);
	}

	.hero-decoration--top {
		right: -56rpx;
		top: -70rpx;
		width: 220rpx;
		height: 220rpx;
	}

	.hero-decoration--bottom {
		left: -66rpx;
		bottom: -104rpx;
		width: 190rpx;
		height: 190rpx;
		background: rgba(147, 203, 255, 0.08);
	}

	.hero-head,
	.hero-actions {
		position: relative;
		z-index: 1;
	}

	.hero-head {
		display: flex;
		align-items: flex-start;
		justify-content: flex-start;
		padding-right: 120rpx;
	}

	.hero-eyebrow {
		display: block;
		font-size: 18rpx;
		font-weight: 700;
		letter-spacing: 1.5rpx;
		color: rgba(231, 238, 255, 0.72);
	}

	.hero-title-row {
		margin-top: 10rpx;
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	.hero-title {
		font-size: 40rpx;
		font-weight: 700;
		line-height: 1.16;
		color: #ffffff;
	}

	.hero-badge {
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(16, 24, 40, 0.16);
		box-shadow: inset 0 0 0 2rpx rgba(255, 255, 255, 0.08);
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		font-size: 20rpx;
		font-weight: 600;
		color: #ffffff;
	}

	.hero-sub {
		display: block;
		margin-top: 14rpx;
		max-width: 470rpx;
		font-size: 22rpx;
		font-weight: 600;
		line-height: 1.4;
		color: rgba(237, 242, 255, 0.84);
	}

	.hero-orbit {
		position: absolute;
		right: 24rpx;
		top: 24rpx;
		width: 104rpx;
		height: 104rpx;
		z-index: 1;
		pointer-events: none;
	}

	.hero-orbit-core {
		position: absolute;
		left: 50%;
		top: 50%;
		transform: translate(-50%, -50%);
		width: 64rpx;
		height: 64rpx;
		border-radius: 22rpx;
		background: linear-gradient(135deg, rgba(255, 255, 255, 0.34) 0%, rgba(255, 255, 255, 0.16) 100%);
		box-shadow:
			inset 0 0 0 2rpx rgba(255, 255, 255, 0.14),
			0 10rpx 20rpx rgba(14, 23, 54, 0.16);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.hero-orbit-ring {
		position: absolute;
		left: 50%;
		top: 50%;
		transform: translate(-50%, -50%);
		border-radius: 50%;
		border: 2rpx solid rgba(255, 255, 255, 0.14);
	}

	.hero-orbit-ring--outer {
		width: 104rpx;
		height: 104rpx;
	}

	.hero-orbit-ring--inner {
		width: 84rpx;
		height: 84rpx;
		border-color: rgba(182, 209, 255, 0.24);
	}

	.hero-actions {
		margin-top: 18rpx;
	}

	.hero-primary-action {
		padding: 20rpx 22rpx;
		border-radius: 30rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(246, 249, 255, 0.95) 100%);
		box-shadow: 0 16rpx 30rpx rgba(20, 36, 74, 0.12);
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 14rpx;
	}

	.hero-primary-copy {
		flex: 1;
		min-width: 0;
		display: flex;
		align-items: center;
	}

	.hero-primary-title {
		display: block;
		font-size: 28rpx;
		font-weight: 500;
		line-height: 1.25;
		color: #182230;
	}

	.hero-primary-go {
		padding: 0 20rpx;
		height: 56rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 22rpx;
		font-weight: 700;
		color: #2e6bff;
		flex-shrink: 0;
	}

	.tool-section,
	.record-section {
		margin-top: 26rpx;
	}

	.section-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.section-title {
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		line-height: 1.22;
		color: #162033;
	}

	.section-sub {
		display: block;
		margin-top: 10rpx;
		font-size: 23rpx;
		line-height: 1.55;
		color: #8c98ab;
	}

	.tool-grid {
		margin-top: 18rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
	}

	.tool-card {
		padding: 22rpx 20rpx;
		border-radius: 26rpx;
		min-height: 176rpx;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		gap: 14rpx;
		box-shadow: 0 14rpx 26rpx rgba(38, 53, 89, 0.05);
	}

	.tool-card--chat {
		background: linear-gradient(180deg, #ffffff 0%, #f5fbf7 100%);
		border: 2rpx solid rgba(221, 243, 229, 0.9);
	}

	.tool-card--analysis {
		background: linear-gradient(180deg, #ffffff 0%, #f7f4ff 100%);
		border: 2rpx solid rgba(234, 226, 255, 0.92);
	}

	.tool-card-top {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 12rpx;
	}

	.tool-icon {
		width: 72rpx;
		height: 72rpx;
		border-radius: 22rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.tool-icon--green {
		background: linear-gradient(180deg, #effbf3 0%, #dff4e7 100%);
	}

	.tool-icon--violet {
		background: linear-gradient(180deg, #f4edff 0%, #ebdeff 100%);
	}

	.tool-arrow {
		width: 34rpx;
		height: 34rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.94);
		box-shadow: 0 6rpx 16rpx rgba(56, 109, 245, 0.08);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.tool-title {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		line-height: 1.28;
		color: #182230;
	}

	.tool-tag-row {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	.tool-tag {
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.92);
		box-shadow: inset 0 0 0 2rpx rgba(56, 109, 245, 0.06);
		font-size: 20rpx;
		font-weight: 600;
		color: #5a6880;
	}

	.section-head-side {
		display: flex;
		align-items: center;
		gap: 10rpx;
		flex-shrink: 0;
	}

	.section-badge {
		padding: 6rpx 12rpx;
		border-radius: 999rpx;
		background: #ebf2ff;
		font-size: 18rpx;
		font-weight: 700;
		color: #386df5;
	}

	.minor-link {
		font-size: 20rpx;
		font-weight: 600;
		color: #97a0af;
	}

	.minor-link.disabled {
		opacity: 0.5;
	}

	.record-list {
		margin-top: 16rpx;
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.record-card {
		padding: 20rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.96);
		border: 2rpx solid rgba(230, 236, 246, 0.9);
		box-shadow: 0 12rpx 24rpx rgba(34, 50, 87, 0.05);
		display: flex;
		align-items: center;
		gap: 14rpx;
		min-height: 114rpx;
	}

	.record-avatar {
		width: 72rpx;
		height: 72rpx;
		border-radius: 22rpx;
		background: linear-gradient(135deg, #22314d 0%, #5d77ae 100%);
		box-shadow: 0 12rpx 22rpx rgba(33, 48, 76, 0.12);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		overflow: hidden;
	}

	.record-thumb {
		width: 100%;
		height: 100%;
	}

	.record-copy {
		flex: 1;
		min-width: 0;
	}

	.record-top {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	.record-source-chip {
		padding: 6rpx 12rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		font-size: 18rpx;
		font-weight: 700;
		color: #386df5;
		flex-shrink: 0;
	}

	.record-time {
		font-size: 19rpx;
		color: #97a0af;
		line-height: 1.4;
		flex-shrink: 0;
	}

	.record-title {
		margin-top: 8rpx;
		font-size: 25rpx;
		font-weight: 600;
		line-height: 1.4;
		color: #172132;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}

	.record-link {
		padding: 10rpx 14rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		font-size: 20rpx;
		font-weight: 700;
		color: #386df5;
		flex-shrink: 0;
	}

	.empty-records {
		margin-top: 16rpx;
		padding: 44rpx 28rpx;
		border-radius: 26rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(248, 251, 255, 0.96) 100%);
		border: 2rpx solid rgba(230, 236, 246, 0.9);
		box-shadow: 0 12rpx 24rpx rgba(35, 50, 87, 0.05);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		text-align: center;
	}

	.empty-mark {
		width: 78rpx;
		height: 78rpx;
		border-radius: 22rpx;
		background: linear-gradient(180deg, #edf4ff 0%, #e5efff 100%);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.empty-title {
		font-size: 26rpx;
		font-weight: 700;
		line-height: 1.3;
		color: #1b2436;
	}

	.empty-action {
		margin-top: 8rpx;
		padding: 0 20rpx;
		height: 56rpx;
		border-radius: 999rpx;
		background: #eef4ff;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 22rpx;
		font-weight: 700;
		color: #386df5;
	}

	.source-popup {
		padding: 28rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));
		border-radius: 36rpx 36rpx 0 0;
		background: #ffffff;
	}

	.source-popup-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 20rpx;
	}

	.source-popup-title {
		display: block;
		font-size: 32rpx;
		font-weight: 700;
		line-height: 1.25;
		color: #101828;
	}

	.source-popup-sub {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		line-height: 1.5;
		color: #98a2b3;
	}

	.source-popup-close {
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: #f4f6fa;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.source-option-list {
		margin-top: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.source-option-card {
		padding: 22rpx 20rpx;
		border-radius: 24rpx;
		background: linear-gradient(180deg, #fbfcfe 0%, #f8fafc 100%);
		box-shadow: inset 0 0 0 2rpx #edf2f7;
		display: flex;
		align-items: center;
		gap: 18rpx;
	}

	.source-option-card:active {
		opacity: 0.76;
	}

	.source-option-icon {
		width: 76rpx;
		height: 76rpx;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.source-option-icon--camera {
		background: #eef4ff;
	}

	.source-option-icon--album {
		background: #f4edff;
	}

	.source-option-copy {
		flex: 1;
		min-width: 0;
	}

	.source-option-title {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		line-height: 1.35;
		color: #111827;
	}

	@media screen and (max-width: 375px) {
		.ai-screen {
			padding-top: 8rpx;
		}

		.hero-panel {
			padding: 28rpx 24rpx;
		}

		.hero-head {
			gap: 18rpx;
		}

		.hero-title {
			font-size: 40rpx;
		}

		.hero-sub {
			max-width: 100%;
		}

		.hero-head {
			padding-right: 96rpx;
		}

		.hero-orbit {
			right: 20rpx;
			top: 20rpx;
			width: 88rpx;
			height: 88rpx;
		}

		.hero-orbit-core {
			width: 56rpx;
			height: 56rpx;
			border-radius: 18rpx;
		}

		.hero-orbit-ring--outer {
			width: 88rpx;
			height: 88rpx;
		}

		.hero-orbit-ring--inner {
			width: 72rpx;
			height: 72rpx;
		}

		.tool-grid {
			gap: 12rpx;
		}

		.tool-card,
		.record-card {
			padding-left: 20rpx;
			padding-right: 20rpx;
		}

		.hero-primary-action {
			padding-left: 20rpx;
			padding-right: 20rpx;
		}

		.hero-primary-action,
		.section-head,
		.record-top {
			align-items: flex-start;
			flex-direction: column;
		}

		.hero-primary-go,
		.section-head-side {
			align-self: flex-start;
		}

		.section-head-side {
			flex-direction: row;
			align-items: center;
		}

		.record-card {
			gap: 14rpx;
		}

		.record-link {
			padding-left: 12rpx;
			padding-right: 12rpx;
		}
	}

	/* #ifdef MP-WEIXIN */
	.tool-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 0;
		margin-left: -7rpx;
		margin-right: -7rpx;
	}

	.tool-card {
		width: calc(50% - 14rpx);
		margin: 0 7rpx 14rpx;
	}
	/* #endif */
</style>
