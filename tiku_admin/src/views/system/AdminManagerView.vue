<script setup>
	import {
		computed,
		nextTick,
		onMounted,
		reactive,
		ref
	} from 'vue'
	import AppDialog from '@/components/common/AppDialog.vue'
	import BasePagination from '@/components/common/BasePagination.vue'
	import FilterSummary from '@/components/common/FilterSummary.vue'
	import {
		createAdminApi,
		deleteAdminApi,
		getAdminInfoApi,
		getAdminPage,
		getRoleSelectApi,
		updateAdminApi
	} from '@/api/modules/admins'
	import {
		validateForm
	} from '@/utils/form'
	import {
		confirmAction,
		showError,
		showSuccess
	} from '@/utils/modal'
	import {
		isAuth
	} from '@/utils/tool'

	const loading = ref(false)
	const dialogVisible = ref(false)
	const dialogLoading = ref(false)
	const detailLoading = ref(false)
	const formRef = ref()
	const tableData = ref([])
	const tableSelections = ref([])
	const roleOptions = ref([])
	const pageNum = ref(1)
	const pageSize = ref(10)
	const total = ref(0)
	const editingId = ref(0)

	const filters = reactive({
		userName: '',
	})

	const FILTER_FIELDS = [
		{ key: 'userName', label: '用户名', type: 'text', placeholder: '请输入用户名' },
	]

	const initialForm = {
		id: 0,
		userName: '',
		password: '',
		comfirmPassword: '',
		salt: '',
		email: '',
		mobile: '',
		inviterCode: '',
		roleIdList: [],
		status: 1,
	}

	const form = reactive({
		...initialForm
	})

	const rules = {
		userName: [{
			required: true,
			message: '用户名不能为空',
			trigger: 'blur'
		}],
		password: [{
			validator: validatePassword,
			trigger: 'blur'
		}],
		comfirmPassword: [{
			validator: validateComfirmPassword,
			trigger: 'blur'
		}],
		email: [{
				required: true,
				message: '邮箱不能为空',
				trigger: 'blur'
			},
			{
				validator: validateEmail,
				trigger: 'blur'
			},
		],
		mobile: [{
				required: true,
				message: '手机号不能为空',
				trigger: 'blur'
			},
			{
				validator: validateMobile,
				trigger: 'blur'
			},
		],
	}

	const showInviterCode = computed(() => form.roleIdList.includes(5))

	function validatePassword(_rule, value, callback) {
		if (!editingId.value && !String(value || '').trim()) {
			callback(new Error('密码不能为空'))
			return
		}
		callback()
	}

	function validateComfirmPassword(_rule, value, callback) {
		if (!editingId.value && !String(value || '').trim()) {
			callback(new Error('确认密码不能为空'))
			return
		}
		if (String(form.password || '') !== String(value || '')) {
			callback(new Error('确认密码与密码输入不一致'))
			return
		}
		callback()
	}

	function validateEmail(_rule, value, callback) {
		const email = String(value || '').trim()
		const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
		if (!emailReg.test(email)) {
			callback(new Error('邮箱格式错误'))
			return
		}
		callback()
	}

	function validateMobile(_rule, value, callback) {
		const mobile = String(value || '').trim()
		const mobileReg = /^1\d{10}$/
		if (!mobileReg.test(mobile)) {
			callback(new Error('手机号格式错误'))
			return
		}
		callback()
	}

	function resetForm() {
		Object.assign(form, initialForm)
	}

	function normalizeAdminRow(item = {}) {
		return {
			...item,
			userId: item.userId,
			username: item.username || '',
			email: item.email || '',
			mobile: item.mobile || '',
			status: Number(item.status ?? 1),
			createTime: item.createTime || '',
		}
	}

	function normalizeRoleOptions(list = []) {
		return list.map((item) => ({
			...item,
			roleId: item.roleId,
			roleName: item.roleName || item.name || '',
		}))
	}

	function buildSubmitPayload() {
		return {
			userId: editingId.value || undefined,
			username: form.userName.trim(),
			password: form.password,
			salt: form.salt,
			email: form.email.trim(),
			mobile: form.mobile.trim(),
			inviterCode: showInviterCode.value ? form.inviterCode.trim() : '',
			status: form.status,
			roleIdList: [...form.roleIdList],
		}
	}

	async function loadRoles() {
		const roles = await getRoleSelectApi()
		roleOptions.value = normalizeRoleOptions(roles)
	}

	async function loadData() {
		loading.value = true
		try {
			const data = await getAdminPage({
				page: pageNum.value,
				limit: pageSize.value,
				username: filters.userName,
			})
			tableData.value = (data.list || []).map(normalizeAdminRow)
			total.value = data.total || 0
		} finally {
			loading.value = false
		}
	}

	function handleSearch() {
		pageNum.value = 1
		loadData()
	}

	function handleReset() {
		filters.userName = ''
		pageNum.value = 1
		loadData()
	}

	function handleSelectionChange(value) {
		tableSelections.value = value
	}

	async function openCreate() {
		editingId.value = 0
		resetForm()
		dialogVisible.value = true
		detailLoading.value = true
		try {
			await loadRoles()
			await nextTick()
			formRef.value?.clearValidate()
		} finally {
			detailLoading.value = false
		}
	}

	async function openEdit(row) {
		editingId.value = row.userId
		resetForm()
		dialogVisible.value = true
		detailLoading.value = true
		try {
			const [roles, detail] = await Promise.all([loadRoles(), getAdminInfoApi(row.userId)])
			Object.assign(form, {
				id: detail.userId || 0,
				userName: detail.username || '',
				password: '',
				comfirmPassword: '',
				salt: detail.salt || '',
				email: detail.email || '',
				mobile: detail.mobile || '',
				inviterCode: detail.inviterCode || '',
				roleIdList: Array.isArray(detail.roleIdList) ? detail.roleIdList : [],
				status: Number(detail.status ?? 1),
			})
			await nextTick()
			formRef.value?.clearValidate()
		} finally {
			detailLoading.value = false
		}
	}

	async function handleSubmit() {
		const valid = await validateForm(formRef)
		if (!valid) return
		if (!form.roleIdList.length) {
			showError('请选择角色')
			return
		}
		dialogLoading.value = true
		try {
			const payload = buildSubmitPayload()
			if (editingId.value) {
				await updateAdminApi(payload)
				showSuccess('管理员已更新')
			} else {
				await createAdminApi(payload)
				showSuccess('管理员已新增')
			}
			dialogVisible.value = false
			await loadData()
		} finally {
			dialogLoading.value = false
		}
	}

	async function handleDelete(row) {
		const userIds = row ? [row.userId] : tableSelections.value.map((item) => item.userId)
		if (!userIds.length) {
			return
		}
		const userNames = row ? [row.username] : tableSelections.value.map((item) => item.username).filter(Boolean)
		const message = row ?
			`确认删除管理员${row.username} 吗？` :
			`确认批量删除这些管理员：${userNames.join(',')} 吗？`
		await confirmAction(message)
		await deleteAdminApi(userIds)
		showSuccess('操作成功')
		await loadData()
	}

	function handleDialogVisible(value) {
		dialogVisible.value = value
		if (!value) {
			editingId.value = 0
			resetForm()
		}
	}

	onMounted(loadData)
</script>

<template>
	<el-card shadow="never" class="page-card">
		<template #header>
			<div class="card-header">
				<strong>管理员管理</strong>
				<el-space>
					<el-button v-if="isAuth('sys:user:save')" type="primary" @click="openCreate">新增管理员</el-button>
					<el-button v-if="isAuth('sys:user:delete')" type="danger" :disabled="!tableSelections.length"
						@click="handleDelete()">
						批量删除
					</el-button>
				</el-space>
			</div>
		</template>

		<el-form :inline="true" class="search-grid" @submit.prevent>
			<el-form-item label="用户名">
				<el-input v-model="filters.userName" clearable placeholder="请输入用户名" @keyup.enter="handleSearch" />
			</el-form-item>
			<el-form-item>
				<el-space>
					<el-button @click="handleSearch">查询</el-button>
					<el-button @click="handleReset">重置</el-button>
				</el-space>
			</el-form-item>
		</el-form>
		<FilterSummary :filters="filters" :fields="FILTER_FIELDS" />

		<el-table v-loading="loading" :data="tableData" border @selection-change="handleSelectionChange">
			<el-table-column type="selection" width="50" align="center" />
			<el-table-column prop="username" label="用户名" min-width="140" align="center" />
			<el-table-column prop="email" label="邮箱" min-width="180" align="center" />
			<el-table-column prop="mobile" label="手机号" min-width="140" align="center" />
			<el-table-column label="状态" width="100" align="center">
				<template #default="{ row }">
					<el-tag
						:type="row.status === 0 ? 'danger' : 'success'">{{ row.status === 0 ? '禁用' : '正常' }}</el-tag>
				</template>
			</el-table-column>
			<el-table-column prop="createTime" label="创建时间" min-width="180" align="center" />
			<el-table-column label="操作" fixed="right" min-width="150" align="center">
				<template #default="{ row }">
					<div class="action-buttons single-line">
						<el-button v-if="isAuth('sys:user:update')" size="small" type="primary"
							@click="openEdit(row)">修改</el-button>
						<el-button v-if="isAuth('sys:user:delete')" size="small" type="danger"
							@click="handleDelete(row)">删除</el-button>
					</div>
				</template>
			</el-table-column>
		</el-table>

		<BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total"
			@update:current-page="pageNum = $event; loadData()"
			@update:page-size="pageSize = $event; pageNum = 1; loadData()" />

		<AppDialog :model-value="dialogVisible" :title="editingId ? '修改管理员' : '新增管理员'" :loading="dialogLoading"
			width="680px" @update:modelValue="handleDialogVisible" @confirm="handleSubmit">
			<div v-loading="detailLoading">
				<el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
					<el-form-item label="用户名" prop="userName">
						<el-input v-model="form.userName" placeholder="登录帐号" maxlength="40" />
					</el-form-item>

					<el-form-item label="密码" prop="password" :required="!editingId">
						<el-input v-model="form.password" type="password" show-password placeholder="密码"
							maxlength="40" />
					</el-form-item>

					<el-form-item label="确认密码" prop="comfirmPassword" :required="!editingId">
						<el-input v-model="form.comfirmPassword" type="password" show-password placeholder="确认密码"
							maxlength="40" />
					</el-form-item>

					<el-form-item label="邮箱" prop="email">
						<el-input v-model="form.email" placeholder="邮箱" maxlength="80" />
					</el-form-item>

					<el-form-item label="手机号" prop="mobile">
						<el-input v-model="form.mobile" placeholder="手机号" maxlength="11" />
					</el-form-item>

					<el-form-item label="角色">
						<el-checkbox-group v-model="form.roleIdList">
							<el-checkbox v-for="role in roleOptions" :key="role.roleId" :value="role.roleId">
								{{ role.roleName }}
							</el-checkbox>
						</el-checkbox-group>
					</el-form-item>

					<el-form-item v-if="showInviterCode" label="邀请码">
						<el-input v-model="form.inviterCode" placeholder="邀请码" maxlength="40" />
					</el-form-item>

					<el-form-item label="状态">
						<el-radio-group v-model="form.status">
							<el-radio :value="0">禁用</el-radio>
							<el-radio :value="1">正常</el-radio>
						</el-radio-group>
					</el-form-item>
				</el-form>
			</div>
		</AppDialog>
	</el-card>
</template>

<style scoped>
.action-buttons.single-line {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 8px;
}

.action-buttons.single-line :deep(.el-button) {
	width: 100%;
	margin-left: 0;
}
</style>
