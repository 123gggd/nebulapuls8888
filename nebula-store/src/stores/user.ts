import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, getUserInfo } from '@/api/store'
import router from '@/router'

// 定义登录接口返回的数据类型
interface LoginResult {
    token: string
    user: any
}

export const useUserStore = defineStore('user', () => {
    // 状态 State
    const token = ref(localStorage.getItem('token') || '')
    // 尝试解析本地存储的用户对象，防止 JSON.parse 报错
    const userInfo = ref<any>({})
    try {
        const storedUser = localStorage.getItem('user')
        if (storedUser) {
            userInfo.value = JSON.parse(storedUser)
        }
    } catch (e) {
        userInfo.value = {}
    }

    // 计算属性 Getters
    const isLoggedIn = computed(() => !!token.value)
    const username = computed(() => userInfo.value.username || userInfo.value.nickname || '用户')
    const avatar = computed(() => userInfo.value.avatar)

    // 动作 Actions

    // 1. 登录动作
    async function login(loginForm: any) {
        try {
            // 显式断言返回类型为 LoginResult (或 any，取决于 request.ts 的泛型定义)
            // 假设 request.ts 拦截器已经解包了 res.data，这里直接拿到 { token, user }
            const res = await loginApi({ ...loginForm, loginType: 'store' }) as unknown as LoginResult

            const accessToken = res.token
            const userObj = res.user

            // 更新状态
            token.value = accessToken
            userInfo.value = userObj

            // 持久化
            localStorage.setItem('token', accessToken)
            localStorage.setItem('user', JSON.stringify(userObj))

            return true
        } catch (error) {
            throw error
        }
    }

    // 2. 获取最新用户信息
    async function fetchUserInfo() {
        if (!token.value) return
        try {
            const res = await getUserInfo()
            userInfo.value = res
            localStorage.setItem('user', JSON.stringify(res))
        } catch (error) {
            console.error('更新用户信息失败', error)
        }
    }

    // 3. 登出
    function logout() {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('token')
        localStorage.removeItem('user')

        // 跳转登录页，并携带当前页面路径，以便登录后跳回
        if (router.currentRoute.value.path !== '/login') {
            router.push(`/login?redirect=${router.currentRoute.value.fullPath}`)
        }
    }

    return {
        token,
        userInfo,
        username,
        avatar,
        isLoggedIn,
        login,
        fetchUserInfo,
        logout
    }
})