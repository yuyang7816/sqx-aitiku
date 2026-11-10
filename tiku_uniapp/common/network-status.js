/**
 * 全局网络状态监听：启动时读取一次，后续由 uni.onNetworkStatusChange 推送。
 * H5 额外监听 window online/offline，便于开发者工具断网与飞行模式模拟。
 */

const normalizeConnected = (networkType = '') => String(networkType).trim().toLowerCase() !== 'none';

/**
 * 订阅网络变化，返回取消订阅函数。
 * @param {(connected: boolean, networkType: string) => void} onChange
 */
export const subscribeNetworkStatus = (onChange) => {
	const emit = (connected, networkType = 'unknown') => {
		if (typeof onChange === 'function') {
			onChange(Boolean(connected), String(networkType || 'unknown'));
		}
	};

	const handleNetworkChange = (res = {}) => {
		const networkType = res.networkType || 'unknown';
		const connected = typeof res.isConnected === 'boolean'
			? res.isConnected
			: normalizeConnected(networkType);
		emit(connected, networkType);
	};

	uni.onNetworkStatusChange(handleNetworkChange);

	uni.getNetworkType({
		success: (res) => {
			handleNetworkChange({
				isConnected: normalizeConnected(res.networkType),
				networkType: res.networkType || 'unknown'
			});
		},
		fail: () => {
			emit(true, 'unknown');
		}
	});

	// #ifdef H5
	const handleOnline = () => emit(true, 'unknown');
	const handleOffline = () => emit(false, 'none');
	if (typeof window !== 'undefined') {
		window.addEventListener('online', handleOnline);
		window.addEventListener('offline', handleOffline);
	}
	// #endif

	return () => {
		if (typeof uni.offNetworkStatusChange === 'function') {
			uni.offNetworkStatusChange(handleNetworkChange);
		}
		// #ifdef H5
		if (typeof window !== 'undefined') {
			window.removeEventListener('online', handleOnline);
			window.removeEventListener('offline', handleOffline);
		}
		// #endif
	};
};
