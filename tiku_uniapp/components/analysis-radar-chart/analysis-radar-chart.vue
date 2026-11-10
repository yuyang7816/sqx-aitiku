<template>
	<view class="radar-chart-wrap">
		<view v-if="hasData" class="radar-chart-panel">
			<qiun-data-charts
				:key="chartRenderKey"
				type="radar"
				class="radar-chart"
				:canvasId="canvasId"
				:reshow="reshowReady"
				:chartData="chartData"
				:opts="chartOpts"
				:ontap="false"
			/>
		</view>
		<view v-else class="radar-empty">
			<text class="radar-empty-text">暂无维度数据</text>
		</view>
	</view>
</template>

<script setup>
	import { computed, nextTick, ref, watch } from 'vue';

	const props = defineProps({
		/** 雷达维度：学科名 + 得分 0~100 */
		dimensions: {
			type: Array,
			default: () => []
		}
	});

	const canvasId = `analysis-radar-${Math.random().toString(36).slice(2, 10)}`;
	const reshowReady = ref(false);

	const normalizedDimensions = computed(() => (
		Array.isArray(props.dimensions)
			? props.dimensions.filter((item) => item && item.name)
			: []
	));

	const hasData = computed(() => normalizedDimensions.value.length > 0);

	const chartRenderKey = computed(() => (
		normalizedDimensions.value.map((item) => `${item.name}:${item.score}`).join('|') || 'empty'
	));

	watch(hasData, async (value) => {
		reshowReady.value = false;
		if (!value) {
			return;
		}
		await nextTick();
		reshowReady.value = true;
	}, { immediate: true });

	const chartData = computed(() => ({
		categories: normalizedDimensions.value.map((item) => item.name),
		series: [{
			name: '能力得分',
			data: normalizedDimensions.value.map((item) => Number(item.score) || 0)
		}]
	}));

	const chartOpts = computed(() => ({
		color: ['#386DF5'],
		padding: [20, 20, 20, 20],
		fontSize: 11,
		dataLabel: true,
		enableScroll: false,
		legend: {
			show: false
		},
		extra: {
			radar: {
				gridType: 'radar',
				gridColor: '#D8DEE8',
				gridCount: 4,
				opacity: 0.18,
				max: 100,
				labelShow: true,
				border: true,
				borderWidth: 2,
				borderColor: '#386DF5'
			}
		}
	}));
</script>

<style lang="scss" scoped>
	.radar-chart-wrap {
		width: 100%;
	}

	.radar-chart-panel {
		width: 100%;
		height: 420rpx;
	}

	.radar-chart {
		width: 100%;
		height: 100%;
		display: block;
	}

	.radar-chart-wrap :deep(.chartsview) {
		width: 100%;
		height: 100%;
	}

	.radar-empty {
		width: 100%;
		height: 420rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.radar-empty-text {
		font-size: var(--text-sm);
		color: #97a0af;
	}

	/* #ifdef MP-WEIXIN */
	.radar-empty-text {
		font-size: 22rpx;
	}
	/* #endif */
</style>
