import { request } from '@/common/api';

const httpPlugin = {
	install(app) {
		app.config.globalProperties.$http = request;
	}
};
export default httpPlugin;
