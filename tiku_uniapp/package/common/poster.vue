<template>
	<view class="app-page poster-page">
		<!-- 月度报告海报 -->
		<view v-if="scene === 'report'" class="screen poster-screen">
			<view v-if="loading" class="poster-state-card card">
				<up-loading-icon mode="circle" color="#386df5"></up-loading-icon>
				<text class="poster-state-title">正在生成海报</text>
				<text class="poster-state-sub">请稍候，学习报告海报马上就好</text>
			</view>

			<view v-else-if="loadError" class="poster-state-card card poster-state-card--error">
				<up-icon name="info-circle" size="48" color="#f05963"></up-icon>
				<text class="poster-state-title">海报生成失败</text>
				<text class="poster-state-sub">请检查网络后重试</text>
				<view class="poster-btn poster-btn--outline poster-btn--compact" @tap="reloadReportPoster">
					<text>重新生成</text>
				</view>
			</view>

			<template v-else>
				<view class="poster-result-head">
					<text class="poster-result-title">海报已生成</text>
					<text class="poster-result-sub">点击海报可预览大图，保存或分享给好友</text>
				</view>
				<view class="poster-preview-card card" @tap="previewPoster">
					<image
						v-if="previewPath"
						:src="previewPath"
						mode="widthFix"
						class="poster-image"
					></image>
					<view v-else class="poster-placeholder">
						<text>海报预览加载中</text>
					</view>
					<text v-if="previewPath" class="poster-preview-tip">点击预览大图</text>
				</view>
			</template>

			<canvas
				:canvas-id="REPORT_POSTER_CANVAS_ID"
				:id="REPORT_POSTER_CANVAS_ID"
				class="hidden-canvas"
				:style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }"
			></canvas>

			<up-qrcode
				ref="qrCodeRef"
				:cid="REPORT_POSTER_QR_CANVAS_ID"
				class="hidden-qrcode"
				:val="qrText"
				:size="qrSize"
				:show-loading="false"
				@result="handleReportQrResult"
			></up-qrcode>
		</view>

		<!-- 邀请海报 -->
		<view v-else-if="scene === 'promo'" class="screen poster-screen">
			<view v-if="loading" class="poster-state-card card">
				<up-loading-icon mode="circle" color="#386df5"></up-loading-icon>
				<text class="poster-state-title">{{ exportGenerating ? '正在合成用户信息' : '正在生成邀请海报' }}</text>
				<text class="poster-state-sub">{{ exportGenerating ? '正在叠加昵称、邀请码与二维码' : '请稍候，专属海报马上就好' }}</text>
			</view>

			<view v-else-if="loadError" class="poster-state-card card poster-state-card--error">
				<up-icon name="info-circle" size="48" color="#f05963"></up-icon>
				<text class="poster-state-title">邀请海报生成失败</text>
				<text class="poster-state-sub">{{ loadErrorMessage || '请检查网络后重试' }}</text>
				<view class="poster-btn poster-btn--outline poster-btn--compact" @tap="reloadPromoPoster">
					<text>重新生成</text>
				</view>
			</view>

			<template v-else>
				<view class="poster-result-head">
					<text class="poster-result-title">海报已生成</text>
					<text class="poster-result-sub">保存或分享给好友，扫码注册后邀请才会生效</text>
				</view>
				<view class="poster-preview-card card" @tap="previewPromoPoster">
					<image
						v-if="previewPath"
						:src="previewPath"
						mode="widthFix"
						class="poster-image"
					></image>
					<view v-else class="poster-placeholder">
						<up-loading-icon mode="circle" color="#386df5" size="18"></up-loading-icon>
						<text>正在合成邀请海报...</text>
					</view>
					<text v-if="previewPath" class="poster-preview-tip">{{ promoPayload.qrHint }}</text>
				</view>
			</template>

			<canvas
				:canvas-id="PROMO_POSTER_CANVAS_ID"
				:id="PROMO_POSTER_CANVAS_ID"
				class="hidden-canvas"
				:style="{ width: promoCanvasWidth + 'px', height: promoCanvasHeight + 'px' }"
				:width="promoCanvasWidth"
				:height="promoCanvasHeight"
			></canvas>

			<up-qrcode
				ref="promoQrCodeRef"
				:cid="PROMO_POSTER_QR_CANVAS_ID"
				class="hidden-qrcode"
				:val="promoQrText"
				:size="promoQrSize"
				:show-loading="false"
				@result="handlePromoQrResult"
			></up-qrcode>
		</view>

		<!-- 底部操作栏：保存（描边） / 分享（实心主色） -->
		<view v-if="!loading && !loadError" class="poster-action-bar">
			<view
				class="poster-btn poster-btn--save"
				:class="{ disabled: actionDisabled || saving }"
				@tap="handleSavePoster"
			>
				<up-icon name="download" size="18" color="#386df5"></up-icon>
				<text>{{ saving ? '保存中...' : '保存到相册' }}</text>
			</view>
			<view
				class="poster-btn poster-btn--share"
				:class="{ disabled: actionDisabled || sharing }"
				@tap="handleSharePoster"
			>
				<up-icon name="share" size="18" color="#FFFFFF"></up-icon>
				<text>{{ sharing ? '分享中...' : '立即分享' }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { computed, getCurrentInstance, nextTick, ref } from 'vue';
	import { onLoad, onShow, onShareAppMessage } from '@dcloudio/uni-app';
	import { usePageNavigationTitle } from '@/common/navigation-bar';
	import {
		REPORT_POSTER_CANVAS_ID,
		REPORT_POSTER_QR_CANVAS_ID,
		buildReportPosterPayload,
		buildReportPosterQrText,
		fetchReportPosterData,
		renderReportPosterImage,
		saveReportPosterImage,
		shareReportPosterImage
	} from '@/common/report-poster';
	import {
		PROMO_POSTER_CANVAS_ID,
		PROMO_POSTER_QR_CANVAS_ID,
		buildPromoPosterPayload,
		buildPromoPosterQrText,
		fetchPromoPosterData,
		isMiniProgramPosterQr,
		preparePromoPosterAssets,
		renderPromoPosterImage,
		savePromoPosterImage,
		sharePromoPosterImage
	} from '@/common/promo-poster';
	import { buildPosterShareAppMessage } from '@/common/poster-save-share';

	const instance = getCurrentInstance();
	const scene = ref('report');
	const selectedMonth = ref('');
	const loading = ref(false);
	const loadError = ref(false);
	const loadErrorMessage = ref('');
	const exportGenerating = ref(false);
	const saving = ref(false);
	const sharing = ref(false);
	const previewPath = ref('');
	const posterPayload = ref(buildReportPosterPayload());
	const promoPayload = ref(buildPromoPosterPayload());
	const qrText = ref(buildReportPosterQrText());
	const promoQrText = ref('');
	const qrCodeRef = ref(null);
	const promoQrCodeRef = ref(null);
	const reportQrResultPath = ref('');
	const promoQrResultPath = ref('');
	const qrSize = 100;
	const promoQrSize = 120;
	const canvasWidth = 750;
	const canvasHeight = 1100;
	const promoCanvasWidth = 750;
	const promoCanvasHeight = ref(1334);

	onLoad((options) => {
		if (options?.scene === 'promo') {
			scene.value = 'promo';
		} else {
			scene.value = 'report';
		}
		selectedMonth.value = String(options?.month || '').trim();
	});

	onShow(() => {
		if (scene.value === 'report') {
			reloadReportPoster();
			return;
		}
		if (scene.value === 'promo') {
			reloadPromoPoster();
		}
	});

	const pageTitle = computed(() => {
		if (scene.value === 'report') {
			return posterPayload.value.pageTitle || '学习报告';
		}
		return promoPayload.value.pageTitle || '邀请海报';
	});
	usePageNavigationTitle(pageTitle);

	const actionDisabled = computed(() => !previewPath.value || exportGenerating.value);

	const handleSavePoster = () => {
		if (scene.value === 'promo') {
			savePromoPoster();
			return;
		}
		savePoster();
	};

	const handleSharePoster = () => {
		if (scene.value === 'promo') {
			sharePromoPoster();
			return;
		}
		sharePoster();
	};

	/** 同步微信小程序 onShareAppMessage 转发卡片（海报图 + 标题 + 路径） */
	const syncShareAppMessage = () => {
		if (scene.value === 'promo') {
			return buildPosterShareAppMessage({
				scene: 'promo',
				imageUrl: previewPath.value,
				inviteCode: promoPayload.value.inviteCode || '',
				title: promoPayload.value.inviteText || promoPayload.value.title || ''
			});
		}
		return buildPosterShareAppMessage({
			scene: 'report',
			imageUrl: previewPath.value,
			month: selectedMonth.value || posterPayload.value.month || '',
			title: posterPayload.value.title || ''
		});
	};

	// #ifdef MP-WEIXIN
	onShareAppMessage(() => syncShareAppMessage());
	// #endif

	const handleReportQrResult = (result) => {
		reportQrResultPath.value = String(result || '').trim();
	};

	const handlePromoQrResult = (result) => {
		promoQrResultPath.value = String(result || '').trim();
	};

	const ensureQrImage = (qrRef, cachedResult = '') => new Promise((resolve) => {
		const cached = String(cachedResult || '').trim();
		if (cached) {
			resolve(cached);
			return;
		}
		setTimeout(() => {
			const qrComponent = qrRef.value;
			const componentResult = String(qrComponent?.result || '').trim();
			if (componentResult) {
				resolve(componentResult);
				return;
			}
			if (!qrComponent || typeof qrComponent.toTempFilePath !== 'function') {
				resolve('');
				return;
			}
			qrComponent.toTempFilePath({
				success: (res) => resolve(res.tempFilePath || ''),
				fail: () => resolve('')
			});
		}, 800);
	});

	const waitCanvasReady = (ms = 150) => new Promise((resolve) => setTimeout(resolve, ms));

	const reloadReportPoster = async () => {
		if (scene.value !== 'report') {
			return;
		}
		loading.value = true;
		loadError.value = false;
		previewPath.value = '';
		try {
			const report = await fetchReportPosterData(selectedMonth.value);
			posterPayload.value = buildReportPosterPayload(report);
			qrText.value = posterPayload.value.qrText || buildReportPosterQrText();
			reportQrResultPath.value = '';
			await nextTick();
			await new Promise((resolve) => setTimeout(resolve, 220));
			let fallbackQrPath = '';
			if (!posterPayload.value.qrCodeUrl) {
				fallbackQrPath = await ensureQrImage(qrCodeRef, reportQrResultPath.value);
			}
			const path = await renderReportPosterImage(
				REPORT_POSTER_CANVAS_ID,
				posterPayload.value,
				fallbackQrPath,
				instance?.proxy
			);
			previewPath.value = path;
		} catch (e) {
			loadError.value = true;
		} finally {
			loading.value = false;
		}
	};

	const reloadPromoPoster = async () => {
		if (scene.value !== 'promo') {
			return;
		}
		loading.value = true;
		loadError.value = false;
		loadErrorMessage.value = '';
		exportGenerating.value = false;
		previewPath.value = '';
		try {
			const { payload, inviteFromApi } = await fetchPromoPosterData();
			promoPayload.value = payload;
			if (!inviteFromApi) {
				loadError.value = true;
				loadErrorMessage.value = '请先登录后再生成邀请海报';
				return;
			}
			if (!payload.posterImageUrl) {
				loadError.value = true;
				loadErrorMessage.value = '请先在管理后台「系统配置 → 推广佣金 → 邀请海报」上传底图，并点击「刷新配置缓存」';
				return;
			}
			exportGenerating.value = true;
			promoQrText.value = isMiniProgramPosterQr()
				? (payload.inviteCode || '')
				: (payload.localQrText || buildPromoPosterQrText(payload.inviteCode));
			promoQrResultPath.value = '';
			await nextTick();
			await waitCanvasReady(200);
			let fallbackQrPath = '';
			if (isMiniProgramPosterQr()) {
				if (!payload.qrCodeUrl) {
					fallbackQrPath = await ensureQrImage(promoQrCodeRef, promoQrResultPath.value);
				}
			} else {
				fallbackQrPath = await ensureQrImage(promoQrCodeRef, promoQrResultPath.value);
			}
			const prepared = await preparePromoPosterAssets(payload, fallbackQrPath);
			promoCanvasHeight.value = prepared.size.height;
			await nextTick();
			await waitCanvasReady(200);
			const result = await renderPromoPosterImage(
				PROMO_POSTER_CANVAS_ID,
				payload,
				fallbackQrPath,
				instance?.proxy,
				prepared
			);
			previewPath.value = result.path || '';
			if (result.height) {
				promoCanvasHeight.value = result.height;
			}
		} catch (e) {
			loadError.value = true;
			const message = e?.message || e?.msg || '';
			if (/底图|图片|下载/i.test(message)) {
				loadErrorMessage.value = message || '海报底图加载失败，请确认 CDN 域名已在文件存储配置中';
			} else {
				loadErrorMessage.value = message || '请检查网络后重试';
			}
		} finally {
			loading.value = false;
			exportGenerating.value = false;
		}
	};

	const previewPoster = () => {
		if (!previewPath.value) {
			return;
		}
		uni.previewImage({ urls: [previewPath.value] });
	};

	const previewPromoPoster = () => {
		previewPoster();
	};

	const savePoster = async () => {
		if (loading.value || saving.value || !previewPath.value) {
			return;
		}
		saving.value = true;
		try {
			await saveReportPosterImage(previewPath.value);
			uni.showToast({ title: '海报已保存到相册', icon: 'none' });
		} catch (e) {
			uni.showToast({ title: e?.message || '保存失败，请重试', icon: 'none' });
		} finally {
			saving.value = false;
		}
	};

	const sharePoster = async () => {
		if (loading.value || sharing.value || !previewPath.value) {
			return;
		}
		sharing.value = true;
		try {
			await shareReportPosterImage(previewPath.value);
		} catch (e) {
			uni.showToast({ title: e?.message || '分享失败，请先保存图片', icon: 'none' });
		} finally {
			sharing.value = false;
		}
	};

	const savePromoPoster = async () => {
		if (loading.value || saving.value || !previewPath.value) {
			if (!previewPath.value) {
				uni.showToast({ title: '分享海报尚未生成', icon: 'none' });
			}
			return;
		}
		saving.value = true;
		try {
			await savePromoPosterImage(previewPath.value);
			uni.showToast({ title: '海报已保存到相册', icon: 'none' });
		} catch (e) {
			uni.showToast({ title: e?.message || '保存失败，请重试', icon: 'none' });
		} finally {
			saving.value = false;
		}
	};

	const sharePromoPoster = async () => {
		if (loading.value || sharing.value || !previewPath.value) {
			if (!previewPath.value) {
				uni.showToast({ title: '分享海报尚未生成', icon: 'none' });
			}
			return;
		}
		sharing.value = true;
		try {
			await sharePromoPosterImage(previewPath.value, promoPayload.value);
		} catch (e) {
			uni.showToast({ title: e?.message || '分享失败，请先保存图片', icon: 'none' });
		} finally {
			sharing.value = false;
		}
	};
</script>

<style lang="scss" scoped>
	.poster-page {
		min-height: 100%;
		padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
		background: var(--app-bg);
	}

	.poster-screen {
		padding-top: 8rpx;
	}

	.poster-state-card {
		margin-top: 16rpx;
		padding: 56rpx 32rpx 48rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		text-align: center;
	}

	.poster-state-card--error {
		padding-top: 48rpx;
	}

	.poster-state-title {
		font-size: var(--text-xl);
		font-weight: 700;
		color: var(--text-main);
	}

	.poster-state-sub {
		font-size: var(--text-base);
		line-height: 1.6;
		color: var(--text-sub);
		max-width: 560rpx;
	}

	.poster-result-head {
		margin-top: 8rpx;
		margin-bottom: 20rpx;
		padding: 0 8rpx;
		text-align: center;
	}

	.poster-result-title {
		display: block;
		font-size: 34rpx;
		font-weight: 700;
		color: var(--text-main);
		line-height: 1.4;
	}

	.poster-result-sub {
		display: block;
		margin-top: 10rpx;
		font-size: var(--text-sm);
		line-height: 1.65;
		color: var(--text-sub);
	}

	.poster-preview-card {
		padding: 20rpx;
		box-shadow: var(--shadow-card);
	}

	.poster-image {
		width: 100%;
		border-radius: var(--radius-lg);
		display: block;
		background: var(--primary-light);
	}

	.poster-placeholder {
		min-height: 720rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		color: var(--text-sub);
		font-size: var(--text-lg);
		background: var(--primary-light);
		border-radius: var(--radius-lg);
	}

	.poster-preview-tip {
		display: block;
		margin-top: 16rpx;
		text-align: center;
		font-size: var(--text-sm);
		color: var(--text-sub);
	}

	.poster-action-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 10;
		display: flex;
		gap: 20rpx;
		padding: 20rpx 32rpx calc(20rpx + env(safe-area-inset-bottom));
		background: linear-gradient(180deg, rgba(245, 247, 251, 0) 0%, rgba(245, 247, 251, 0.92) 24%, #f5f7fb 100%);
		backdrop-filter: blur(8px);
	}

	.poster-btn {
		flex: 1;
		height: 88rpx;
		border-radius: var(--radius-xl);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		font-size: var(--text-lg);
		font-weight: 600;
		transition: transform 0.15s ease, opacity 0.15s ease;
	}

	.poster-btn:active:not(.disabled) {
		transform: scale(0.98);
	}

	/* 保存：白底 + 主色描边 */
	.poster-btn--save {
		background: #ffffff;
		border: 2rpx solid var(--primary);
		color: var(--primary);
		box-shadow: 0 8rpx 24rpx rgba(36, 48, 65, 0.06);
	}

	/* 分享：主色实心，视觉权重更高 */
	.poster-btn--share {
		background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
		color: #ffffff;
		box-shadow: var(--shadow-button);
	}

	.poster-btn--outline {
		background: #ffffff;
		border: 2rpx solid var(--primary);
		color: var(--primary);
	}

	.poster-btn--compact {
		flex: none;
		min-width: 240rpx;
		margin-top: 12rpx;
		padding: 0 36rpx;
		height: 76rpx;
		font-size: var(--text-base);
	}

	.disabled {
		opacity: 0.45;
		pointer-events: none;
	}

	.hidden-canvas,
	.hidden-qrcode {
		position: fixed;
		left: -10000px;
		top: -10000px;
		z-index: -1;
		opacity: 0;
		pointer-events: none;
	}

	/* #ifdef MP-WEIXIN */
	.poster-page {
		background: #f5f7fb;
	}

	.poster-state-title,
	.poster-result-title {
		color: #243041;
	}

	.poster-state-sub,
	.poster-result-sub,
	.poster-placeholder,
	.poster-preview-tip {
		color: #97a0af;
	}

	.poster-state-title {
		font-size: 30rpx;
	}

	.poster-state-sub,
	.poster-btn--compact {
		font-size: 24rpx;
	}

	.poster-result-sub,
	.poster-preview-tip {
		font-size: 22rpx;
	}

	.poster-preview-card {
		box-shadow: 0 16rpx 48rpx rgba(36, 48, 65, 0.05);
	}

	.poster-image,
	.poster-placeholder {
		border-radius: 24rpx;
		background: #eef4ff;
	}

	.poster-action-bar {
		backdrop-filter: none;
		background: linear-gradient(180deg, rgba(245, 247, 251, 0) 0%, rgba(245, 247, 251, 0.96) 24%, #f5f7fb 100%);
	}

	.poster-btn {
		border-radius: 30rpx;
		font-size: 28rpx;
	}

	.poster-btn--save,
	.poster-btn--outline {
		border-color: #386df5;
		color: #386df5;
	}

	.poster-btn--share {
		background: #386df5;
		box-shadow: 0 12rpx 24rpx rgba(56, 109, 245, 0.18);
		color: #ffffff;
	}

	.poster-btn--share text {
		color: #ffffff;
	}
	/* #endif */
</style>
