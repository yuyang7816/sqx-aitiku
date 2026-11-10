<template>
	<view class="hero-section">
		<view class="hero-content">
			<view class="hero-brand-row">
				<view class="hero-brand-mark">
					<up-icon name="kefu-ermai" size="28" color="#FFFFFF"></up-icon>
				</view>
				<view class="hero-brand-copy">
					<text class="hero-brand-name">AI智能提分</text>
					<text class="hero-brand-sub">中学练习与学情辅助平台</text>
				</view>
			</view>
			<view class="hero-main-copy">
				<text class="hero-title">{{ title }}</text>
				<text class="hero-subtitle">{{ subtitle }}</text>
			</view>
			<view class="learning-scene">
				<view class="scene-chip scene-chip-a">
					<up-icon name="checkmark-circle-fill" size="16" color="#22c55e"></up-icon>
					<text>今日题单</text>
				</view>
				<view class="question-preview">
					<view class="preview-top">
						<view class="preview-badge">
							<up-icon name="grid-fill" size="15" color="#2457d6"></up-icon>
							<text>函数综合</text>
						</view>
					</view>
					<text class="preview-question">已知函数 y=ax²+bx+c，图像经过 (1,2)、(2,5)，求单调区间。</text>
					<view class="preview-bars">
						<view class="preview-bar preview-bar-main"></view>
						<view class="preview-bar preview-bar-sub"></view>
						<view class="preview-bar preview-bar-sub short"></view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	defineProps({
		title: {
			type: String,
			default: '欢迎登录'
		},
		subtitle: {
			type: String,
			default: '同步学习记录、错题本和会员权益'
		}
	});
</script>

<style lang="scss" scoped>
	@import './auth-page.scss';
</style>
