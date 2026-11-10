/**
 * 模块十九 · 错题本 Store（已登录时由 GET /app/notebook 填充，未登录为空）
 */
import { defineStore } from 'pinia';

export const useNotebookStore = defineStore('notebook', {
	state: () => ({
		hydrated: false,
		list: []
	}),
	getters: {
		pendingCount: (state) => state.list.filter((item) => !item.mastered).length,
		totalCount: (state) => state.list.length
	},
	actions: {
		hydrateFromStorage() {
			this.list = [];
			this.hydrated = true;
		},
		setList(list = []) {
			this.list = Array.isArray(list) ? list : [];
		},
		replaceList(list = []) {
			this.setList(list);
		}
	}
});
