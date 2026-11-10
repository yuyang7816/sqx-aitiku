<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen export-screen">
				<view v-if="exporting" class="export-loading">
					<view class="state-shell">
						<view class="state-icon state-icon--loading">
							<up-loading-icon mode="circle" color="#386df5"></up-loading-icon>
						</view>
						<text class="state-title">正在生成导出文件</text>
						<text class="state-sub">{{ payload.includeAnswerAnalysis === false ? '正在整理纯题目导出内容，请稍候。' : '正在整理错题内容与解析预览，请稍候。' }}</text>
					</view>
				</view>

				<view v-else-if="!payload.items.length" class="export-empty">
					<view class="state-shell">
						<view class="state-icon">
							<up-icon name="file-text" size="46" color="#AAB4C6"></up-icon>
						</view>
						<text class="state-title">暂无可导出的错题</text>
						<text class="state-sub">请返回错题本调整筛选条件后，再进入此页生成导出内容。</text>
					</view>
				</view>

				<template v-else>
					<view class="preview-list">
						<view v-for="item in payload.items" :key="item.index" class="preview-item">
							<view class="preview-head">
								<view class="preview-index-pill">
									<text class="preview-index">第 {{ item.index }} 题</text>
								</view>
								<text class="preview-subject">{{ item.subject }}</text>
							</view>
							<view class="preview-section">
								<text class="section-label">题目</text>
								<text class="preview-question">{{ item.question }}</text>
							</view>
							<view v-if="payload.includeAnswerAnalysis !== false" class="preview-answers">
								<view class="answer-card wrong">
									<text class="answer-label">错误答案</text>
									<text class="answer-copy">{{ item.wrong }}</text>
								</view>
								<view class="answer-card right">
									<text class="answer-label">正确答案</text>
									<text class="answer-copy">{{ item.right }}</text>
								</view>
							</view>
							<view v-if="payload.includeAnswerAnalysis !== false" class="analysis-block">
								<text class="section-label analysis-label">详细解析</text>
								<text class="analysis-copy">{{ item.analysis }}</text>
							</view>
						</view>
					</view>
				</template>
			</view>
		</view>

		<view v-if="payload.items.length" class="bottom-bar" :class="{ 'bottom-bar--compact': !showPdfAction }">
			<view class="ghost-button bottom-action" :class="{ disabled: exporting }" @tap="saveImage">
				<up-icon name="photo" size="16" color="#386df5"></up-icon>
				<text>保存图片</text>
			</view>
			<view v-if="showPdfAction" class="ghost-button bottom-action" :class="{ disabled: exporting }" @tap="exportPdf">
				<up-icon name="file-text" size="16" color="#386df5"></up-icon>
				<text>导出 PDF</text>
			</view>
			<view class="solid-button small bottom-action bottom-action--primary" :class="{ disabled: exporting }" @tap="shareImage">
				<up-icon name="share" size="16" color="#FFFFFF"></up-icon>
				<text>分享</text>
			</view>
		</view>

		<canvas
			canvas-id="notebookExportCanvas"
			id="notebookExportCanvas"
			class="hidden-canvas"
			:style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }"
		></canvas>
	</view>
</template>

<script setup>
	import { computed, getCurrentInstance, ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import {
		getNotebookExportPayload,
		measureNotebookExportHeight,
		printNotebookExportPdf,
		renderNotebookExportImage,
		saveNotebookExportImage,
		shareNotebookExportImage
	} from '@/common/notebook-export';

	const instance = getCurrentInstance();
	const payload = ref(getNotebookExportPayload());
	const exporting = ref(false);
	const canvasWidth = ref(750);
	const canvasHeight = ref(900);
	const imagePath = ref('');
	const showPdfAction = computed(() => {
		// #ifdef H5
		if (typeof window === 'undefined') {
			return false;
		}
		const ua = String(window.navigator?.userAgent || '');
		const isWechat = /MicroMessenger/i.test(ua);
		const isMobile = /Android|webOS|iPhone|iPad|iPod|Mobile|HarmonyOS/i.test(ua);
		return !isWechat && !isMobile;
		// #endif
		return false;
	});

	const refreshPayload = () => {
		payload.value = getNotebookExportPayload();
		canvasHeight.value = measureNotebookExportHeight(payload.value);
		imagePath.value = '';
	};

	onShow(refreshPayload);

	const ensureImage = async () => {
		if (imagePath.value) {
			return imagePath.value;
		}
		exporting.value = true;
		try {
			canvasHeight.value = measureNotebookExportHeight(payload.value);
			await new Promise((resolve) => setTimeout(resolve, 80));
			const path = await renderNotebookExportImage(
				'notebookExportCanvas',
				payload.value,
				instance?.proxy
			);
			imagePath.value = path;
			return path;
		} finally {
			exporting.value = false;
		}
	};

	const saveImage = async () => {
		if (exporting.value) {
			return;
		}
		try {
			const path = await ensureImage();
			await saveNotebookExportImage(path);
			uni.showToast({ title: '图片已保存', icon: 'none' });
		} catch (e) {
			uni.showToast({ title: e?.message || '保存失败，请重试', icon: 'none' });
		}
	};

	const exportPdf = async () => {
		if (exporting.value) {
			return;
		}
		if (!showPdfAction.value) {
			uni.showToast({ title: '当前环境请使用保存图片', icon: 'none' });
			return;
		}
		try {
			printNotebookExportPdf(payload.value);
			uni.showToast({ title: '请在打印对话框选择「另存为 PDF」', icon: 'none', duration: 2500 });
		} catch (e) {
			uni.showToast({ title: e?.message || '导出失败，请重试', icon: 'none' });
		}
	};

	const shareImage = async () => {
		if (exporting.value) {
			return;
		}
		try {
			const path = await ensureImage();
			await shareNotebookExportImage(path);
		} catch (e) {
			uni.showToast({ title: e?.message || '分享失败，请先保存图片', icon: 'none' });
		}
	};
</script>

<style lang="scss" scoped>
	.export-screen {
		padding: 16rpx 30rpx calc(160rpx + env(safe-area-inset-bottom));
		background:
			radial-gradient(circle at top right, rgba(96, 146, 255, 0.1), transparent 24%),
			linear-gradient(180deg, #f7f9fd 0%, #edf2fa 100%);
	}

	.state-shell {
		margin-top: 24rpx;
		padding: 88rpx 44rpx;
		border-radius: 36rpx;
		background: rgba(255, 255, 255, 0.92);
		box-shadow: 0 22rpx 54rpx rgba(27, 47, 94, 0.08);
		border: 1rpx solid rgba(212, 222, 240, 0.7);
		display: flex;
		flex-direction: column;
		align-items: center;
		text-align: center;
	}

	.state-icon {
		width: 112rpx;
		height: 112rpx;
		border-radius: 32rpx;
		background: linear-gradient(180deg, #eef4ff 0%, #f8fbff 100%);
		box-shadow: inset 0 0 0 2rpx rgba(213, 225, 248, 0.75);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.state-icon--loading {
		background: linear-gradient(180deg, #eaf1ff 0%, #f7faff 100%);
	}

	.state-title {
		margin-top: 28rpx;
		font-size: 36rpx;
		font-weight: 700;
		line-height: 1.35;
		color: #1f2d3d;
	}

	.state-sub {
		margin-top: 18rpx;
		font-size: 24rpx;
		line-height: 1.7;
		color: #6b778c;
	}

	.preview-list {
		margin-top: 0;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.preview-item {
		padding: 24rpx 22rpx 22rpx;
		border-radius: 26rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 12rpx 28rpx rgba(28, 48, 92, 0.06);
		border: 1rpx solid rgba(215, 225, 241, 0.8);
	}

	.preview-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.preview-index-pill {
		display: inline-flex;
		align-items: center;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: #edf4ff;
	}

	.preview-index {
		font-size: 20rpx;
		font-weight: 700;
		line-height: 1.2;
		color: #386df5;
	}

	.preview-subject {
		max-width: 48%;
		padding: 8rpx 14rpx;
		border-radius: 999rpx;
		background: #fff1eb;
		font-size: 18rpx;
		line-height: 1.3;
		color: #ef6b57;
		text-align: center;
	}

	.preview-section {
		margin-top: 18rpx;
	}

	.section-label {
		display: flex;
		align-items: center;
		padding: 0 0 0 16rpx;
		font-size: 18rpx;
		font-weight: 700;
		line-height: 1.4;
		color: #72809a;
		position: relative;
	}

	.section-label::before {
		content: '';
		position: absolute;
		left: 0;
		top: 50%;
		transform: translateY(-50%);
		width: 6rpx;
		height: 24rpx;
		border-radius: 999rpx;
		background: #7ea9ff;
	}

	.preview-question {
		margin-top: 12rpx;
		display: block;
		font-size: 28rpx;
		font-weight: 600;
		color: #1f2d3d;
		line-height: 1.6;
		word-break: break-word;
	}

	.preview-answers {
		margin-top: 18rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 12rpx;
	}

	.answer-card {
		padding: 14rpx 16rpx;
		border-radius: 18rpx;
		border: 1rpx solid transparent;
	}

	.answer-card.wrong {
		background: #fff4f5;
		border-color: #ffd8dc;
	}

	.answer-card.right {
		background: #f3fbf5;
		border-color: #d5f1dc;
	}

	.answer-label {
		display: block;
		font-size: 18rpx;
		font-weight: 700;
		line-height: 1.4;
	}

	.answer-card.wrong .answer-label,
	.answer-card.wrong .answer-copy {
		color: #e25562;
	}

	.answer-card.right .answer-label,
	.answer-card.right .answer-copy {
		color: #24995b;
	}

	.answer-copy {
		display: block;
		margin-top: 6rpx;
		font-size: 22rpx;
		line-height: 1.45;
		word-break: break-word;
	}

	.analysis-block {
		margin-top: 18rpx;
		padding: 18rpx 18rpx 16rpx;
		border-radius: 18rpx;
		background: #f7faff;
		box-shadow: inset 0 0 0 2rpx rgba(226, 234, 246, 0.85);
	}

	.analysis-label {
		color: #386df5;
	}

	.analysis-copy {
		margin-top: 12rpx;
		display: block;
		font-size: 22rpx;
		color: #506079;
		line-height: 1.7;
		word-break: break-word;
	}

	.bottom-bar {
		position: fixed;
		left: 20rpx;
		right: 20rpx;
		bottom: calc(18rpx + env(safe-area-inset-bottom));
		padding: 12rpx;
		background: rgba(255, 255, 255, 0.92);
		backdrop-filter: blur(16rpx);
		border-radius: 24rpx;
		box-shadow: 0 14rpx 34rpx rgba(19, 33, 60, 0.12);
		border: 1rpx solid rgba(216, 225, 240, 0.86);
		display: flex;
		gap: 16rpx;
		z-index: 20;
	}

	.bottom-action {
		height: 78rpx;
		border-radius: 18rpx;
		font-size: 22rpx;
	}

	.bottom-bar .ghost-button {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		background: #f5f8ff;
		border: 1rpx solid #d5e2fb;
		color: #386df5;
		font-size: 22rpx;
		font-weight: 600;
	}

	.bottom-bar .small {
		width: 200rpx;
		flex: none;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 22rpx;
	}

	.bottom-bar--compact .ghost-button,
	.bottom-bar--compact .small {
		flex: 1;
		width: auto;
	}

	.bottom-action--primary {
		box-shadow: 0 14rpx 30rpx rgba(56, 109, 245, 0.24);
	}

	.bottom-bar .disabled {
		opacity: 0.5;
		pointer-events: none;
	}

	.hidden-canvas {
		position: fixed;
		left: -9999px;
		top: -9999px;
		opacity: 0;
		pointer-events: none;
	}

	/* #ifdef MP-WEIXIN */
	.preview-answers {
		display: flex;
		flex-wrap: wrap;
	}

	.preview-answers .answer-card {
		flex: 0 0 calc(50% - 6rpx);
	}

	.bottom-bar {
		backdrop-filter: none;
		background: rgba(255, 255, 255, 0.98);
	}
	/* #endif */

	@media screen and (max-width: 420px) {
		.bottom-bar {
			gap: 10rpx;
		}
	}

	@media screen and (min-width: 768px) {
		.export-screen {
			padding-left: 30rpx;
			padding-right: 30rpx;
		}

		.bottom-bar {
			left: 50%;
			right: auto;
			width: min(710rpx, calc(100vw - 40rpx));
			transform: translateX(-50%);
		}
	}
</style>
