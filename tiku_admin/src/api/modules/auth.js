import { get, post } from '@/api/http'

export const authApi = {
  login: '/sys/login',
  nav: '/sys/menu/nav',
  logout: '/replace-with-java/auth/logout',
  refreshToken: '/replace-with-java/auth/refresh-token',
}

export function loginApi(data) {
  return post(authApi.login, {
    username: data.username,
    password: data.password,
    uuid: data.uuid,
    captcha: data.captcha,
  }, {
    silentError: true,
  }).then((response) => {
    const token = response?.token || response?.accessToken || response?.data?.token || response?.data?.accessToken || ''
    const expire = Number(response?.expire || response?.data?.expire || 0)
    if (!token) {
      return Promise.reject(new Error('登录成功但未返回 token'))
    }
    return {
      token,
      refreshToken: response?.refreshToken || response?.data?.refreshToken || '',
      expiresAt: Date.now() + (expire > 0 ? expire * 1000 : 2 * 60 * 60 * 1000),
      profile: response?.user || response?.userInfo || response?.data?.user || response?.data?.userInfo || {
        name: data.username,
        roleCode: '',
        roleName: '',
        email: '',
      },
      permissions: response?.permissions || response?.data?.permissions || [],
      raw: response,
    }
  })
}

export function logoutApi() {
  return Promise.resolve(true)
}

export function getNavApi() {
  return get(authApi.nav, {}, { silentError: true })
}
