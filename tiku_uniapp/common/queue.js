import { LEGACY_APP_URL_KEY, LEGACY_PUBLIC_RELATION_KEY } from '@/common/storage-keys';

export const getWxAppid = () => {
	return 'wxbf1429781694727b';
};
export const getInvitation = () => {
	return uni.getStorageSync(LEGACY_PUBLIC_RELATION_KEY);
};
export const getAppDownUrl = () => {
	return uni.getStorageSync(LEGACY_APP_URL_KEY);
};
export const publicYuMing = () => {
	return 'https://tiku.xianmaxiong.com';
};