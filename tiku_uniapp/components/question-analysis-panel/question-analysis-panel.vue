<template>
	<view class="analysis-panel" :class="{ embedded }">
		<view v-if="showQuestionTitle && item.title" class="question-box card">
			<text class="question-text">{{ item.title }}</text>
		</view>

		<view v-if="showAnswerCompare" class="answer-grid">
			<view class="answer-tag" :class="item.isCorrect ? 'right' : 'wrong'">
				<up-icon
					:name="item.isCorrect ? 'checkmark-circle-fill' : 'close-circle-fill'"
					size="14"
					:color="item.isCorrect ? '#16a34a' : '#ef4444'"
				></up-icon>
				<text>{{ item.isCorrect ? '回答正确' : '回答错误' }} · 我的答案：{{ myAnswerText }}</text>
			</view>
			<view v-if="!item.isCorrect" class="answer-tag right">
				<up-icon name="checkmark-circle-fill" size="14" color="#16a34a"></up-icon>
				<text>正确答案：{{ rightAnswerText }}</text>
			</view>
		</view>

		<view class="analysis-card">
			<view class="analysis-header">
				<up-icon name="file-text-fill" size="18" color="#386df5"></up-icon>
				<text class="analysis-title">详细解析</text>
			</view>
			<text class="analysis-copy">{{ item.analysis || '暂无详细解析' }}</text>
			<view v-if="item.tags.length" class="tag-row">
				<text v-for="tag in item.tags" :key="tag" class="mini-tag">{{ tag }}</text>
			</view>
		</view>

		<view class="video-card">
			<view class="video-header">
				<view class="video-header-left">
					<up-icon name="play-circle-fill" size="18" color="#f59e0b"></up-icon>
					<text class="video-title">高清视频解析</text>
				</view>
				<view class="video-badge">
					<up-icon name="coupon-fill" size="12" color="#d7a421"></up-icon>
					<text>会员专享</text>
				</view>
			</view>

			<view class="player-shell">
				<video
					v-if="canPlayVideo && videoInfo.videoUrl"
					:id="playerId"
					class="video-player"
					:src="videoInfo.videoUrl"
					:poster="videoInfo.posterUrl || ''"
					:controls="true"
					:show-center-play-btn="true"
					:enable-play-gesture="true"
					:show-fullscreen-btn="true"
					object-fit="contain"
					@error="onVideoError"
				></video>

				<view v-else-if="member.isVip && videoLoading" class="video-placeholder">
					<up-loading-icon mode="circle" size="22"></up-loading-icon>
					<text>视频加载中...</text>
				</view>

				<view v-else-if="member.isVip && !videoInfo.videoUrl && !videoLoading" class="video-placeholder muted">
					<up-icon name="play-circle" size="28" color="#94a3b8"></up-icon>
					<text>暂无讲解视频，可先阅读文字解析</text>
				</view>

				<view v-else class="vip-lock">
					<up-icon name="lock-fill" size="24" color="#FFFFFF"></up-icon>
					<text class="lock-title">开通会员后可观看高清视频解析</text>
					<view class="lock-btn" @tap.stop="goMemberCenter">立即开通</view>
				</view>
			</view>

			<text v-if="videoInfo.summary" class="video-copy">{{ videoInfo.summary }}</text>
			<view v-if="videoMetaText" class="video-meta">{{ videoMetaText }}</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, ref, watch } from 'vue';
	import { getMemberState } from '@/common/app-data';
	import { checkVideoAnalysisAccess, resolveQuestionVideoAsync } from '@/common/video-analysis';

	const props = defineProps({
		item: {
			type: Object,
			default: () => ({})
		},
		showQuestionTitle: {
			type: Boolean,
			default: false
		},
		showAnswerCompare: {
			type: Boolean,
			default: true
		},
		embedded: {
			type: Boolean,
			default: false
		},
		playerId: {
			type: String,
			default: 'questionAnalysisVideo'
		}
	});

	const member = ref(getMemberState());
	const videoLoading = ref(false);
	const videoInfo = ref({
		videoUrl: '',
		posterUrl: '',
		duration: '',
		teacher: '',
		summary: ''
	});

	const canPlayVideo = computed(() => checkVideoAnalysisAccess().ok);

	const videoMetaText = computed(() => {
		const parts = [];
		if (videoInfo.value.teacher) {
			parts.push(`讲解：${videoInfo.value.teacher}`);
		}
		if (videoInfo.value.duration) {
			parts.push(`时长 ${videoInfo.value.duration}`);
		}
		return parts.join(' · ');
	});

	const getOptionText = (labelOrValue) => {
		const key = String(labelOrValue || '').trim();
		if (!key) {
			return '未作答';
		}
		const options = props.item.options || [];
		const byLabel = options.find((option) => String(option.label || '').trim().toUpperCase() === key.toUpperCase());
		if (byLabel) {
			return `${byLabel.label}. ${byLabel.value}`;
		}
		const byValue = options.find((option) => String(option.value || '').trim() === key);
		if (byValue) {
			return `${byValue.label}. ${byValue.value}`;
		}
		return key;
	};

	const myAnswerText = computed(() => getOptionText(props.item.myAnswer));
	const rightAnswerText = computed(() => getOptionText(props.item.rightAnswer));

	const loadVideo = async () => {
		member.value = getMemberState();
		videoInfo.value = {
			videoUrl: '',
			posterUrl: '',
			duration: '',
			teacher: '',
			summary: ''
		};
		if (!props.item?.title) {
			return;
		}
		if (!canPlayVideo.value) {
			return;
		}
		videoLoading.value = true;
		try {
			const resolved = await resolveQuestionVideoAsync(props.item);
			videoInfo.value = {
				videoUrl: resolved.videoUrl || '',
				posterUrl: resolved.posterUrl || '',
				duration: resolved.duration || '',
				teacher: resolved.teacher || '',
				summary: resolved.summary || ''
			};
		} finally {
			videoLoading.value = false;
		}
	};

	watch(
		() => [
			props.item?.questionCode,
			props.item?.questionId,
			props.item?.title
		],
		() => {
			loadVideo();
		},
		{ immediate: true }
	);

	const onVideoError = () => {
		uni.showToast({ title: '视频加载失败，请稍后重试', icon: 'none' });
	};

	const goMemberCenter = () => {
		uni.navigateTo({ url: '/package/member/center' });
	};
</script>

<style lang="scss" scoped>
	.analysis-panel {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.analysis-panel.embedded {
		margin-top: 24rpx;
		gap: 16rpx;
	}

	.question-box {
		padding: 30rpx;
	}

	.question-text {
		font-size: 34rpx;
		font-weight: 700;
		line-height: 1.52;
		color: #243041;
	}

	.answer-grid {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.answer-tag {
		padding: 18rpx 20rpx;
		border-radius: 24rpx;
		font-size: 27rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.answer-tag.wrong {
		color: #ef4444;
		background: #ffefef;
	}

	.answer-tag.right {
		color: #16a34a;
		background: #eaf8ef;
	}

	.analysis-card,
	.video-card {
		padding: 32rpx;
		border-radius: 36rpx;
	}

	.analysis-card {
		background: #eef4ff;
	}

	.video-card {
		background: linear-gradient(180deg, #fff9eb 0%, #fff3d6 100%);
		border: 2rpx solid #fde68a;
	}

	.analysis-header,
	.video-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	.video-header-left,
	.analysis-header {
		gap: 12rpx;
	}

	.video-header-left {
		display: flex;
		align-items: center;
		flex: 1;
		min-width: 0;
	}

	.analysis-title,
	.video-title {
		font-size: 30rpx;
		font-weight: 800;
	}

	.analysis-title {
		color: #21407b;
	}

	.video-title {
		color: #92400e;
	}

	.video-badge {
		display: flex;
		align-items: center;
		gap: 6rpx;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		font-size: 20rpx;
		font-weight: 700;
		color: #b8860b;
		background: rgba(255, 255, 255, 0.72);
		flex-shrink: 0;
	}

	.analysis-copy,
	.video-copy {
		margin-top: 18rpx;
		display: block;
		font-size: 27rpx;
		line-height: 1.72;
		font-weight: 500;
		color: #415065;
	}

	.video-copy {
		color: #7c5a16;
	}

	.video-meta {
		margin-top: 12rpx;
		font-size: 22rpx;
		color: #a16207;
	}

	.tag-row {
		margin-top: 24rpx;
		display: flex;
		gap: 12rpx;
		flex-wrap: wrap;
	}

	.mini-tag {
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: #fff;
		font-size: 22rpx;
		font-weight: 700;
		color: #386df5;
	}

	.player-shell {
		margin-top: 20rpx;
		border-radius: 30rpx;
		overflow: hidden;
		background: #0f172a;
		min-height: 360rpx;
	}

	.video-player {
		width: 100%;
		height: 360rpx;
	}

	.video-placeholder,
	.vip-lock {
		min-height: 360rpx;
		padding: 40rpx 28rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		text-align: center;
	}

	.video-placeholder {
		color: #cbd5e1;
		font-size: 24rpx;
	}

	.video-placeholder.muted {
		background: #1e293b;
		color: rgba(255, 255, 255, 0.72);
	}

	.vip-lock {
		background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
	}

	.lock-title {
		font-size: 26rpx;
		font-weight: 700;
		color: #ffffff;
		line-height: 1.6;
	}

	.lock-btn {
		margin-top: 8rpx;
		padding: 0 32rpx;
		height: 72rpx;
		border-radius: 999rpx;
		background: linear-gradient(90deg, #f59e0b 0%, #d97706 100%);
		color: #fff;
		font-size: 24rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
	}
</style>
