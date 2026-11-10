<template>
	<view class="app-page"></view>
</template>

<script setup>
	/**
	 * 兼容旧链接：/package/member/pay 重定向到会员中心并自动弹出支付弹框
	 */
	import { onLoad } from '@dcloudio/uni-app';
	import { buildMemberCenterPayUrl } from '@/common/routes';

	onLoad((options = {}) => {
		uni.redirectTo({ url: buildMemberCenterPayUrl(options) });
	});
</script>
