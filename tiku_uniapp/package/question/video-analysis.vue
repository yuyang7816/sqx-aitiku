<template>
	<view class="app-page">
		<view class="screen detail-screen">
			<view v-if="!hasQuestion" class="empty-card card">
				<view class="empty-icon-wrap">
					<up-icon name="play-circle" size="48" color="#97a0af"></up-icon>
				</view>
				<text class="empty-title">暂无题目视频</text>
				<text class="empty-copy">请从题目解析页进入，或先完成练习后查看错题讲解。</text>
			</view>

			<template v-else>
				<view class="question-card card">
					<view class="question-head">
						<text v-if="videoInfo.subject" class="subject-tag">{{ videoInfo.subject }}</text>
						<view v-if="member.isVip" class="vip-tag">
							<up-icon name="coupon-fill" size="12" color="#d7a421"></up-icon>
							<text>会员专享</text>
						</view>
					</view>
					<text class="question-text">{{ videoInfo.title }}</text>
					<text v-if="videoInfo.rightAnswer" class="answer-line">正确答案：{{ videoInfo.rightAnswer }}</text>
				</view>

				<view class="player-card card">
					<view class="player-header">
						<up-icon name="play-circle-fill" size="18" color="#f59e0b"></up-icon>
						<text class="player-title">名师视频讲解</text>
						<text class="player-duration">{{ videoInfo.duration }}</text>
					</view>

					<view class="player-shell">
						<video
							v-if="canPlay && videoInfo.videoUrl"
							id="questionVideoPlayer"
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

						<view v-else class="vip-lock">
							<view class="lock-icon-wrap">
								<up-icon name="lock-fill" size="28" color="#FFFFFF"></up-icon>
							</view>
							<text class="lock-title">高清视频解析为会员权益</text>
							<text class="lock-copy">开通 AI 至尊会员后可观看完整名师讲解视频</text>
							<view class="solid-button lock-btn" @tap="goMemberCenter">
								<up-icon name="coupon-fill" size="16" color="#FFFFFF"></up-icon>
								<text>立即开通会员</text>
							</view>
						</view>
					</view>
				</view>

				<view class="meta-card card">
					<view class="meta-row">
						<text class="meta-label">讲解老师</text>
						<text class="meta-value">{{ videoInfo.teacher }}</text>
					</view>
					<view class="meta-row">
						<text class="meta-label">视频时长</text>
						<text class="meta-value">{{ videoInfo.duration }}</text>
					</view>
					<text class="summary-copy">{{ videoInfo.summary }}</text>
					<text v-if="videoInfo.analysis" class="analysis-copy">文字解析：{{ videoInfo.analysis }}</text>
				</view>

				<view v-if="canPlay" class="bottom-row">
					<view class="ghost-button" @tap="goTextAnalysis">
						<up-icon name="file-text" size="16" color="#5d6777"></up-icon>
						<text>文字解析</text>
					</view>
					<view class="solid-button green" @tap="replayVideo">
						<up-icon name="reload" size="16" color="#FFFFFF"></up-icon>
						<text>重新播放</text>
					</view>
				</view>
			</template>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { getMemberState } from '@/common/app-data';
	import { QUESTION_ANALYSIS_PAGE } from '@/common/routes';
	import { loadQuestionVideoFromReview } from '@/common/video-analysis';

	const hasQuestion = ref(false);
	const canPlay = ref(false);
	const videoInfo = ref({
		title: '',
		subject: '',
		videoUrl: '',
		posterUrl: '',
		duration: '',
		teacher: '',
		summary: '',
		analysis: '',
		rightAnswer: ''
	});
	const member = ref(getMemberState());

	const refreshPage = async () => {
		member.value = getMemberState();
		const payload = await loadQuestionVideoFromReview();
		hasQuestion.value = payload.hasQuestion;
		canPlay.value = payload.canPlay;
		videoInfo.value = payload.video;
	};

	onShow(refreshPage);

	const onVideoError = () => {
		uni.showToast({ title: '视频加载失败，请稍后重试', icon: 'none' });
	};

	const goMemberCenter = () => {
		uni.navigateTo({ url: '/package/member/center' });
	};

	const goTextAnalysis = () => {
		uni.navigateTo({ url: QUESTION_ANALYSIS_PAGE });
	};

	const replayVideo = () => {
		// #ifdef MP-WEIXIN || APP-PLUS || H5
		const ctx = uni.createVideoContext('questionVideoPlayer');
		if (ctx) {
			ctx.seek(0);
			ctx.play();
			return;
		}
		// #endif
		uni.showToast({ title: '请点击播放器重新播放', icon: 'none' });
	};
</script>

<style lang="scss" scoped>
	.detail-screen {
		padding-top: 12rpx;
		padding-bottom: 32rpx;
	}

	.question-card,
	.player-card,
	.meta-card,
	.empty-card {
		margin-top: 16rpx;
		padding: 32rpx;
	}

	.question-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
		flex-wrap: wrap;
	}

	.subject-tag {
		padding: 8rpx 14rpx;
		border-radius: var(--radius-sm);
		font-size: var(--text-xs);
		color: var(--primary);
		background: var(--primary-light);
	}

	.vip-tag {
		display: flex;
		align-items: center;
		gap: 6rpx;
		padding: 8rpx 14rpx;
		border-radius: var(--radius-full);
		font-size: var(--text-xs);
		color: #b8860b;
		background: #fff7db;
	}

	.question-text {
		margin-top: 20rpx;
		display: block;
		font-size: 34rpx;
		font-weight: 700;
		line-height: 1.5;
		color: var(--text-main);
	}

	.answer-line {
		margin-top: 16rpx;
		display: block;
		font-size: var(--text-base);
		color: var(--success);
		font-weight: 600;
	}

	.player-header {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.player-title {
		flex: 1;
		font-size: var(--text-xl);
		font-weight: 700;
		color: #92400e;
	}

	.player-duration {
		font-size: var(--text-sm);
		color: var(--text-sub);
	}

	.player-shell {
		margin-top: 24rpx;
		border-radius: var(--radius-xl);
		overflow: hidden;
		background: #0f172a;
		min-height: 400rpx;
	}

	.video-player {
		width: 100%;
		height: 400rpx;
	}

	.vip-lock {
		min-height: 400rpx;
		padding: 48rpx 32rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		text-align: center;
		background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
	}

	.lock-icon-wrap {
		width: 88rpx;
		height: 88rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.12);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.lock-title {
		margin-top: 24rpx;
		font-size: var(--text-xl);
		font-weight: 700;
		color: #ffffff;
	}

	.lock-copy {
		margin-top: 12rpx;
		font-size: var(--text-base);
		line-height: 1.6;
		color: rgba(255, 255, 255, 0.72);
	}

	.lock-btn {
		margin-top: 28rpx;
		padding: 0 36rpx;
		height: 80rpx;
		display: flex;
		align-items: center;
		gap: 10rpx;
		background: linear-gradient(90deg, #f59e0b 0%, #d97706 100%);
	}

	.meta-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		padding: 12rpx 0;
	}

	.meta-label {
		font-size: var(--text-base);
		color: var(--text-sub);
	}

	.meta-value {
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--text-main);
	}

	.summary-copy {
		margin-top: 16rpx;
		display: block;
		font-size: var(--text-lg);
		line-height: 1.7;
		color: #415065;
	}

	.analysis-copy {
		margin-top: 20rpx;
		display: block;
		font-size: var(--text-base);
		line-height: 1.7;
		color: var(--text-sub);
	}

	.bottom-row {
		margin-top: 28rpx;
		display: flex;
		gap: 18rpx;
	}

	.ghost-button,
	.solid-button {
		flex: 1;
		height: 88rpx;
		border-radius: var(--radius-full);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		font-size: var(--text-base);
		font-weight: 600;
	}

	.ghost-button {
		border: 2rpx solid #e5e9f0;
		color: #5d6777;
		background: #fff;
	}

	.solid-button.green {
		background: var(--success);
		color: #fff;
	}

	.empty-card {
		text-align: center;
	}

	.empty-icon-wrap {
		display: flex;
		justify-content: center;
		margin-bottom: 24rpx;
	}

	.empty-title {
		display: block;
		font-size: var(--text-xl);
		font-weight: 700;
		color: var(--text-main);
	}

	.empty-copy {
		margin-top: 12rpx;
		display: block;
		font-size: var(--text-base);
		line-height: 1.6;
		color: var(--text-sub);
	}

	/* #ifdef MP-WEIXIN */
	.subject-tag {
		border-radius: 14rpx;
		font-size: 20rpx;
		color: #386df5;
		background: #eef4ff;
	}

	.vip-tag,
	.ghost-button,
	.solid-button {
		border-radius: 999rpx;
		font-size: 20rpx;
	}

	.question-text,
	.meta-value,
	.empty-title {
		color: #243041;
	}

	.answer-line,
	.meta-label,
	.player-duration,
	.analysis-copy,
	.empty-copy {
		font-size: 24rpx;
	}

	.answer-line,
	.solid-button.green {
		color: #16a34a;
	}

	.solid-button.green {
		background: #16a34a;
		color: #fff;
	}

	.player-title,
	.lock-title,
	.empty-title {
		font-size: 30rpx;
	}

	.player-duration,
	.meta-label,
	.analysis-copy,
	.empty-copy {
		color: #97a0af;
	}

	.player-shell {
		border-radius: 30rpx;
	}
	/* #endif */
</style>
