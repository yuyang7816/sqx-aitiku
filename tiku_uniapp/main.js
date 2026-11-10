import App from './App'
//引入uview plus
import uviewPlus from '@/uni_modules/uview-plus';
import { getUviewInstallOptions } from '@/common/uviewConfig';
import { registerUviewComponents } from '@/common/registerUviewComponents';
import fontUtil from '@/uni_modules/uview-plus/components/u-icon/util.js';
import httpPlugin from '/common/httpPlugin.js';
import {
	getWxAppid,
	getInvitation,
	getAppDownUrl,
	publicYuMing
} from '/common/queue.js';
// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
	...App
})
app.$mount()
// #endif

// #ifdef VUE3
import {
	createSSRApp
} from 'vue'
import { pinia } from '@/stores/pinia';
export function createApp() {
	const app = createSSRApp(App)
	app.use(pinia)
	app.config.globalProperties.$getWxAppid = getWxAppid;
	app.config.globalProperties.$getInvitation = getInvitation;
	app.config.globalProperties.$getAppDownUrl = getAppDownUrl;
	app.config.globalProperties.$publicYuMing = publicYuMing;
	// H5 需先全局注册 uview 组件，避免打包后 <up-icon> 变成空标签
	// #ifdef H5
	registerUviewComponents(app);
	fontUtil.params.loaded = true;
	// #endif
	//使用uview plus
	app.use(uviewPlus, getUviewInstallOptions)
	//挂载http请求
	app.use(httpPlugin)
	return {
		app
	}
}
// #endif