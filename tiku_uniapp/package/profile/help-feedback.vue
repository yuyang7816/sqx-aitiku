<template>
	<view class="app-page">
		<view class="page-scroll">
			<view class="screen help-screen">
				<view class="tip-card">
					<up-icon name="info-circle" size="16" color="#386df5"></up-icon>
					<text class="tip-text">题目讲解请前往 AI Tab 使用「AI 答疑」；本页用于常见问题查阅与产品问题反馈。</text>
				</view>

				<view class="chip-row">
					<view
						v-for="item in tabs"
						:key="item.key"
						class="chip"
						:class="{ active: activeTab === item.key }"
						@tap="activeTab = item.key"
					>{{ item.label }}</view>
				</view>

				<!-- 常见问题 -->
				<view v-if="activeTab === 'faq'" class="faq-panel">
					<text class="panel-title">常见问题</text>
					<view v-if="faqLoading" class="faq-loading">
						<up-loading-icon mode="circle" size="20"></up-loading-icon>
						<text>加载中...</text>
					</view>
					<view v-else-if="!faqList.length" class="empty-ticket card">
						<up-icon name="file-text" size="40" color="#C0C6D4"></up-icon>
						<text class="empty-text">暂无常见问题，运营人员将在管理端补充</text>
					</view>
					<view v-else class="faq-card card">
						<up-collapse accordion>
							<up-collapse-item
								v-for="item in faqList"
								:key="item.id"
								:name="item.id"
								:title="item.question"
							>
								<text class="faq-answer">{{ item.answer }}</text>
							</up-collapse-item>
						</up-collapse>
					</view>
				</view>

				<!-- 提交反馈 -->
				<view v-else class="feedback-panel">
					<text class="panel-title">提交反馈</text>
					<view class="form-card card">
						<view class="form-row">
							<text class="form-label">反馈类型</text>
							<picker :range="categoryLabels" :value="categoryIndex" @change="onCategoryChange">
								<view class="picker-value">
									<text>{{ categoryLabels[categoryIndex] || '请选择' }}</text>
									<up-icon name="arrow-down" size="14" color="#C0C6D4"></up-icon>
								</view>
							</picker>
						</view>

						<view class="form-row column">
							<text class="form-label">问题描述</text>
							<textarea
								v-model="form.content"
								class="feedback-textarea"
								:maxlength="500"
								placeholder="请详细描述您遇到的问题或建议，至少 10 个字"
								placeholder-class="placeholder"
							></textarea>
							<text class="char-count">{{ form.content.length }}/500</text>
						</view>

						<view class="form-row">
							<text class="form-label">联系电话（选填）</text>
							<input
								v-model="form.contactPhone"
								class="feedback-input"
								type="number"
								maxlength="11"
								placeholder="便于我们联系您"
								placeholder-class="placeholder"
							/>
						</view>

						<view class="form-row column">
							<text class="form-label">截图（选填，最多 3 张）</text>
							<up-upload
								:fileList="imageList"
								name="feedbackImage"
								accept="image"
								:maxCount="3"
								:previewFullImage="true"
								:deletable="true"
								uploadText="上传截图"
								width="84"
								height="84"
								@afterRead="afterReadImage"
								@delete="deleteImage"
							></up-upload>
						</view>

						<view class="solid-button" :class="{ disabled: submitting }" @tap="submitForm">
							<text>{{ submitting ? '提交中...' : '提交反馈' }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onShow } from '@dcloudio/uni-app';
	import { feedbackCategories, fetchHelpFaqList } from '@/common/help-faq-api';
	import { submitFeedbackTicket } from '@/common/feedback-api';

	const tabs = [
		{ key: 'faq', label: '常见问题' },
		{ key: 'feedback', label: '提交反馈' }
	];

	const activeTab = ref('faq');
	const faqList = ref([]);
	const faqLoading = ref(false);
	const categoryLabels = feedbackCategories.map((item) => item.label);
	const categoryIndex = ref(0);
	const submitting = ref(false);

	const form = ref({
		content: '',
		contactPhone: ''
	});

	const imageList = ref([]);

	const refreshFaqList = async () => {
		faqLoading.value = true;
		try {
			const result = await fetchHelpFaqList();
			faqList.value = result.list;
		} finally {
			faqLoading.value = false;
		}
	};

	onShow(() => {
		refreshFaqList();
	});

	const onCategoryChange = (event) => {
		categoryIndex.value = Number(event.detail.value) || 0;
	};

	const afterReadImage = (event) => {
		const files = Array.isArray(event.file) ? event.file : [event.file];
		files.forEach((file) => {
			if (imageList.value.length >= 3) {
				return;
			}
			imageList.value.push({
				...file,
				status: 'success',
				message: ''
			});
		});
	};

	const deleteImage = (event) => {
		imageList.value.splice(event.index, 1);
	};

	const resetForm = () => {
		form.value = {
			content: '',
			contactPhone: ''
		};
		categoryIndex.value = 0;
		imageList.value = [];
	};

	const submitForm = async () => {
		if (submitting.value) {
			return;
		}
		submitting.value = true;
		try {
			const category = feedbackCategories[categoryIndex.value]?.value || 'other';
			await submitFeedbackTicket({
				category,
				content: form.value.content,
				contactPhone: form.value.contactPhone,
				images: imageList.value.map((item) => item.url || item.thumb || '').filter(Boolean)
			});
			uni.showToast({ title: '反馈已提交，我们会尽快处理', icon: 'success' });
			resetForm();
		} catch (e) {
			uni.showToast({ title: e.message || '提交失败，请稍后重试', icon: 'none' });
		} finally {
			submitting.value = false;
		}
	};
</script>

<style lang="scss" scoped>
	.help-screen {
		padding-top: 12rpx;
		padding-bottom: 28rpx;
	}

	.tip-card {
		margin-top: 16rpx;
		padding: 24rpx 28rpx;
		border-radius: var(--radius-xl);
		background: linear-gradient(180deg, #f2f6ff 0%, #eef4ff 100%);
		border: 2rpx solid rgba(56, 109, 245, 0.08);
		box-shadow: 0 12rpx 28rpx rgba(56, 109, 245, 0.08);
		display: flex;
		align-items: flex-start;
		gap: 12rpx;
	}

	.tip-text {
		flex: 1;
		font-size: var(--text-base);
		color: #4d5666;
		line-height: 1.6;
	}

	.chip-row {
		margin-top: 24rpx;
		display: flex;
		gap: 16rpx;
		padding: 8rpx;
		border-radius: var(--radius-xl);
		background: rgba(255, 255, 255, 0.9);
		box-shadow: 0 10rpx 22rpx rgba(31, 45, 61, 0.05);
	}

	.chip {
		flex: 1;
		height: 72rpx;
		border-radius: var(--radius-full);
		background: transparent;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: var(--text-base);
		color: #5d6777;
		font-weight: 600;
		transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
	}

	.chip.active {
		background: linear-gradient(180deg, #ffffff 0%, #f4f8ff 100%);
		color: var(--primary);
		box-shadow: 0 8rpx 18rpx rgba(56, 109, 245, 0.12);
	}

	.panel-title {
		display: block;
		margin-top: 28rpx;
		font-size: var(--text-xl);
		font-weight: 700;
		color: var(--text-main);
		line-height: 1.3;
	}

	.faq-card {
		margin-top: 18rpx;
		padding: 2rpx 22rpx 8rpx;
		overflow: hidden;
	}

	:deep(.faq-card .u-collapse) {
		background: transparent;
	}

	:deep(.faq-card .u-collapse-item) {
		background: transparent;
	}

	:deep(.faq-card .u-collapse-item__content) {
		padding: 0;
	}

	:deep(.faq-card .u-cell) {
		padding: 20rpx 0;
		background: transparent;
	}

	:deep(.faq-card .u-cell__body__content text) {
		color: #22324b !important;
	}

	:deep(.faq-card .u-cell__title-text) {
		font-size: 27rpx;
		font-weight: 600;
		line-height: 1.38;
		color: #22324b !important;
	}

	:deep(.faq-card .u-icon__icon) {
		color: #7b8798 !important;
		font-size: 28rpx !important;
	}

	:deep(.faq-card .u-line) {
		border-color: #edf1f6 !important;
	}

	.faq-answer {
		display: block;
		padding: 0 0 16rpx;
		font-size: var(--text-base);
		color: #5d6777;
		line-height: 1.65;
	}

	.form-card {
		margin-top: 18rpx;
		padding: 24rpx;
	}

	.form-row {
		margin-bottom: 22rpx;
	}

	.form-row:last-of-type {
		margin-bottom: 18rpx;
	}

	.form-row.column {
		display: flex;
		flex-direction: column;
	}

	.form-label {
		display: block;
		margin-bottom: 10rpx;
		font-size: var(--text-base);
		font-weight: 700;
		color: var(--text-main);
	}

	.picker-value {
		height: 80rpx;
		padding: 0 24rpx;
		border-radius: var(--radius-lg);
		background: #f7f9fc;
		border: 2rpx solid #edf1f6;
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: var(--text-base);
		color: #4d5666;
	}

	.feedback-textarea {
		width: 92%;
		height: 164rpx;
		padding: 18rpx 22rpx;
		border-radius: var(--radius-lg);
		background: #f7f9fc;
		border: 2rpx solid #edf1f6;
		font-size: var(--text-base);
		color: #1f2937;
		line-height: 1.55;
	}

	.feedback-input {
		height: 80rpx;
		padding: 0 24rpx;
		border-radius: var(--radius-lg);
		background: #f7f9fc;
		border: 2rpx solid #edf1f6;
		font-size: var(--text-base);
		color: #1f2937;
	}

	.char-count {
		margin-top: 8rpx;
		text-align: right;
		font-size: var(--text-sm);
		color: #97a0af;
	}

	:deep(.form-card .u-upload__wrap) {
		margin: -6rpx;
	}

	:deep(.form-card .u-upload__wrap__preview) {
		margin: 6rpx;
		border-radius: 20rpx;
		overflow: hidden;
	}

	:deep(.form-card .u-upload__button) {
		background: #f7f9fc !important;
		border-width: 2rpx !important;
		border-style: dashed !important;
		border-color: #d7dfeb !important;
		border-radius: 20rpx;
		box-shadow: none !important;
		display: flex !important;
		flex-direction: column !important;
		align-items: center !important;
		justify-content: center !important;
	}

	:deep(.form-card .u-upload__button__text) {
		margin-top: 12rpx;
		font-size: 22rpx !important;
		color: #8a94a6 !important;
		line-height: 1.3;
	}

	:deep(.form-card .u-upload__button .u-icon__icon) {
		color: #9aa6bb !important;
	}

	.form-card .solid-button {
		margin-top: 4rpx;
	}

	.solid-button.disabled {
		opacity: 0.6;
	}

	.empty-ticket {
		margin-top: 18rpx;
		padding: 28rpx 28rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 12rpx;
		border: 2rpx dashed #e4e9f1;
		background: linear-gradient(180deg, #ffffff 0%, #fbfcfe 100%);
	}

	.empty-text {
		font-size: var(--text-base);
		color: #97a0af;
		text-align: center;
		line-height: 1.6;
	}

	.placeholder {
		color: #c0c6d4;
	}

	/* #ifdef MP-WEIXIN */
	.tip-card,
	.chip-row {
		border-radius: 30rpx;
	}

	.tip-text,
	.chip,
	.faq-answer,
	.form-label,
	.picker-value,
	.feedback-textarea,
	.feedback-input,
	.empty-text {
		font-size: 24rpx;
	}

	.chip {
		border-radius: 999rpx;
	}

	.chip.active {
		color: #386df5;
	}

	.panel-title,
	.form-label {
		color: #243041;
	}

	.panel-title {
		font-size: 30rpx;
	}

	.picker-value,
	.feedback-textarea,
	.feedback-input {
		border-radius: 24rpx;
	}

	.char-count {
		font-size: 22rpx;
	}
	/* #endif */
</style>
