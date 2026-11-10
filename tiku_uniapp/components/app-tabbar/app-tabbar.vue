<template>
	<view class="app-tabbar-shell">
		<view class="app-tabbar-placeholder"></view>
		<view class="app-tabbar-wrap">
			<view class="app-tabbar-inner">
				<view
					v-for="item in tabItems"
					:key="item.key"
					class="app-tabbar-item"
					:class="{
						'is-active': current === item.key,
						'is-mid': item.mode === 'midButton'
					}"
					@tap="handleChange(item.key)"
				>
					<view v-if="item.mode === 'midButton'" class="mid-entry">
						<view class="mid-icon" :class="{ active: current === item.key }">
							<up-icon name="kefu-ermai" size="22" color="#FFFFFF"></up-icon>
						</view>
						<text class="tabbar-text tabbar-text--mid">{{ item.text }}</text>
					</view>
					<view v-else class="normal-entry">
						<view class="tab-icon-shell">
							<up-icon :name="item.icon" size="22" :color="current === item.key ? '#386DF5' : '#A1A9B8'"></up-icon>
							<view v-if="item.badgeCount > 0" class="tab-badge">{{ formatBadge(item.badgeCount) }}</view>
						</view>
						<text class="tabbar-text" :class="{ 'tabbar-text--active': current === item.key }">{{ item.text }}</text>
					</view>
				</view>
			</view>
			<view class="app-tabbar-safe"></view>
		</view>
	</view>
</template>

<script setup>
	import {
		computed
	} from 'vue';
	import {
		TAB_ITEMS,
		isValidTabKey,
		switchTabPage
	} from '@/common/tabbar';

	const emit = defineEmits(['change']);

	const props = defineProps({
		current: {
			type: String,
			default: 'home',
			validator: (value) => isValidTabKey(value)
		},
		badgeCount: {
			type: Number,
			default: 0
		}
	});

	const tabItems = computed(() => TAB_ITEMS.map((item) => {
		if (item.key === 'home' && props.badgeCount > 0) {
			return {
				...item,
				badgeCount: props.badgeCount
			};
		}
		return item;
	}));

	const formatBadge = (value) => {
		const count = Number(value || 0);
		if (count > 99) {
			return '99+';
		}
		return String(count);
	};

	const handleChange = (name) => {
		if (name === props.current) {
			return;
		}
		emit('change', name);
		switchTabPage(name);
	};
</script>

<style lang="scss" scoped>
	$app-tabbar-height: 118rpx;
	$app-tabbar-offset: 24rpx;

	.app-tabbar-shell {
		position: relative;
		z-index: 40;
	}

	.app-tabbar-placeholder {
		height: calc(#{$app-tabbar-height} + env(safe-area-inset-bottom));
	}

	.app-tabbar-wrap {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 1200;
		background: #FFFFFF;
		box-shadow: 0 -8rpx 40rpx rgba(19, 33, 60, 0.08);
	}

	.app-tabbar-inner {
		height: $app-tabbar-height;
		padding: 10rpx 12rpx 8rpx;
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
	}

	.app-tabbar-safe {
		height: env(safe-area-inset-bottom);
		background: #FFFFFF;
	}

	.app-tabbar-item {
		flex: 1;
		min-width: 0;
		display: flex;
		justify-content: center;
		align-items: flex-end;
	}

	.normal-entry {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: flex-end;
		gap: 8rpx;
		min-width: 0;
	}

	.tab-icon-shell {
		width: 52rpx;
		height: 52rpx;
		position: relative;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.tab-badge {
		position: absolute;
		top: -6rpx;
		right: -16rpx;
		min-width: 30rpx;
		height: 30rpx;
		padding: 0 8rpx;
		border-radius: 999rpx;
		background: #FF5E66;
		color: #FFFFFF;
		font-size: 16rpx;
		font-weight: 700;
		line-height: 30rpx;
		text-align: center;
		box-shadow: 0 4rpx 12rpx rgba(255, 94, 102, 0.22);
	}

	.tabbar-text {
		max-width: 100%;
		font-size: 22rpx;
		line-height: 1;
		color: #A1A9B8;
		font-weight: 500;
		text-align: center;
	}

	.tabbar-text--active {
		color: #386DF5;
	}

	.app-tabbar-item.is-mid {
		align-items: flex-start;
	}

	.mid-entry {
		margin-top: -$app-tabbar-offset;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: flex-start;
		gap: 10rpx;
	}

	.mid-icon {
		width: 96rpx;
		height: 96rpx;
		border-radius: 50%;
		background: linear-gradient(135deg, #6d7cff 0%, #4d59ff 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 16rpx 32rpx rgba(59, 95, 246, 0.28);
	}

	.mid-icon.active {
		background: linear-gradient(135deg, #396df5 0%, #5c43ee 100%);
		box-shadow: 0 20rpx 40rpx rgba(59, 95, 246, 0.36);
	}

	.tabbar-text--mid {
		font-weight: 600;
		color: #A1A9B8;
	}

	.app-tabbar-item.is-active .tabbar-text--mid {
		color: #386DF5;
	}
</style>
