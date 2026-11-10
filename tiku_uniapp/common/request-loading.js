/**
 * 全局请求 Loading：引用计数合并并发请求，避免闪烁与提前关闭
 */
let loadingCount = 0;
let loadingVisible = false;
let currentTitle = '加载中...';

/** 递增并展示 Loading（仅首次展示） */
export const beginRequestLoading = (title = '加载中...') => {
	loadingCount += 1;
	if (!loadingVisible) {
		loadingVisible = true;
		currentTitle = title || '加载中...';
		uni.showLoading({
			title: currentTitle,
			mask: true
		});
	}
};

/** 递减并在全部完成后关闭 Loading */
export const endRequestLoading = () => {
	if (loadingCount <= 0) {
		return;
	}
	loadingCount -= 1;
	if (loadingCount <= 0) {
		loadingCount = 0;
		if (loadingVisible) {
			loadingVisible = false;
			uni.hideLoading();
		}
	}
};

/** 强制重置（异常兜底，正常流程勿调用） */
export const resetRequestLoading = () => {
	loadingCount = 0;
	if (loadingVisible) {
		loadingVisible = false;
		uni.hideLoading();
	}
};
