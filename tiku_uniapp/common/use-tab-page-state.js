/**
 * Tab 主包页 composable：onHide 写入内存缓存，onShow 恢复滚动与筛选态。
 */
import { ref, nextTick, onBeforeUnmount } from 'vue';
import { onShow, onHide, onPageScroll } from '@dcloudio/uni-app';
import { isValidTabKey } from '@/common/tabbar';
import { getTabPageState, patchTabPageState } from '@/common/tab-state-cache';

/**
 * @param {string} tabKey home | practice | ai | notebook | my
 * @param {{ activeChip?: import('vue').Ref<string>, restoreScroll?: boolean, deferScrollRestore?: boolean }} [options]
 */
export function useTabPageState(tabKey, options = {}) {
	const { activeChip, restoreScroll = true, deferScrollRestore = false } = options;
	const scrollTop = ref(0);
	let pendingScrollRestore = false;

	onPageScroll((event) => {
		scrollTop.value = Number(event?.scrollTop) || 0;
	});

	const persistState = () => {
		if (!isValidTabKey(tabKey)) {
			return;
		}
		patchTabPageState(tabKey, {
			scrollTop: scrollTop.value,
			...(activeChip ? { activeChip: activeChip.value } : {})
		});
	};

	const restoreFilterState = () => {
		if (!activeChip || !isValidTabKey(tabKey)) {
			return;
		}
		const cached = getTabPageState(tabKey);
		if (cached.activeChip) {
			activeChip.value = cached.activeChip;
		}
	};

	const restoreScrollPosition = () => {
		if (!restoreScroll || !isValidTabKey(tabKey)) {
			return;
		}
		const cached = getTabPageState(tabKey);
		if (cached.scrollTop <= 0) {
			pendingScrollRestore = false;
			return;
		}
		nextTick(() => {
			uni.pageScrollTo({
				scrollTop: cached.scrollTop,
				duration: 0
			});
			pendingScrollRestore = false;
		});
	};

	onHide(() => {
		persistState();
	});

	onBeforeUnmount(() => {
		persistState();
	});

	onShow(() => {
		restoreFilterState();
		pendingScrollRestore = restoreScroll;
		if (pendingScrollRestore && !deferScrollRestore) {
			restoreScrollPosition();
		}
	});

	/** 异步列表渲染完成后再次恢复滚动（章节/错题列表加载结束） */
	const restoreScrollAfterReady = () => {
		if (!pendingScrollRestore) {
			return;
		}
		restoreScrollPosition();
	};

	return {
		scrollTop,
		persistState,
		restoreScrollAfterReady
	};
}
