/**
 * H5 打包时 uni_modules 内的 import.meta.glob 可能不会被 Vite 展开，
 * 导致 uview-plus install 注册 0 个组件，页面出现空的 <up-icon> 标签。
 * 在项目源码侧做一次 eager glob，确保 u-* / up-* 组件全局可用。
 */
export function registerUviewComponents(app) {
	if (!app || typeof app.component !== 'function') return;

	const modules = import.meta.glob('../uni_modules/uview-plus/components/u-*/u-*.vue', {
		eager: true,
	});

	Object.values(modules).forEach((mod) => {
		const component = mod?.default;
		if (!component?.name || component.name.indexOf('u--') === 0) return;

		app.component(component.name, component);

		const upName = component.name.replace(/^u-/, 'up-');
		if (upName !== component.name) {
			app.component(upName, component);
		}
	});
}
