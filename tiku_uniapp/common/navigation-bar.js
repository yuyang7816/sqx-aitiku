/**
 * 原生导航栏辅助：App / 小程序展示系统 navigationBar，H5 全局 custom 隐藏
 */
import { watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';

/** 当前端是否展示原生 navigationBar（非 H5） */
export const isNativeNavigationVisible = () => {
	// #ifdef H5
	return false;
	// #endif
	return true;
};

/** 设置当前页原生导航栏标题（H5 无导航栏时调用无害） */
export const setPageNavigationTitle = (title = '') => {
	const text = String(title || '').trim();
	if (!text) {
		return;
	}
	uni.setNavigationBarTitle({ title: text });
};

/**
 * 同步 computed/ref 标题到原生 navigationBar（App / 小程序）
 * @param {import('vue').Ref<string>|import('vue').ComputedRef<string>} titleRef
 */
export const usePageNavigationTitle = (titleRef) => {
	const syncTitle = () => setPageNavigationTitle(titleRef.value);
	if (isNativeNavigationVisible()) {
		watch(titleRef, syncTitle, { immediate: true });
		onShow(syncTitle);
	}
};
