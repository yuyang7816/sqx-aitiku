import path from 'path';
import { fileURLToPath } from 'url';
import { defineConfig } from 'vite';
import uniPlugin from '@dcloudio/vite-plugin-uni';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const uni = typeof uniPlugin === 'function' ? uniPlugin : uniPlugin.default;

export default defineConfig({
	plugins: [uni()],
	transpileDependencies: ['uview-plus'],
	resolve: {
		alias: {
			'@': path.resolve(__dirname),
			// H5 按需编译 uview 子组件时部分 import 缺少 @/ 前缀，补全 uni_modules 解析
			'uni_modules/uview-plus': path.resolve(__dirname, 'uni_modules/uview-plus')
		}
	},
	server: {
		port: 5174,
		strictPort: true,
		hmr: {
			overlay: false
		}
	}
});
