import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, getUserInfo } from '@/api/store'
import router from '@/router'

// Define response type for the login API
interface LoginResult {
    token: string
    user: any
}

export const useUserStore = defineStore('user', () => {
    // State
    const token = ref(localStorage.getItem('token') || '')
    // Parse stored user safely to avoid JSON.parse errors
    const userInfo = ref<any>({})
    try {
        const storedUser = localStorage.getItem('user')
        if (storedUser) {
            userInfo.value = JSON.parse(storedUser)
        }
    } catch (e) {
        userInfo.value = {}
    }

    // Getters
    const isLoggedIn = computed(() => !!token.value)
    const username = computed(() => userInfo.value.username || userInfo.value.nickname || 'User')
    const avatar = computed(() => userInfo.value.avatar)

    // Actions

    // 1. Login
    async function login(loginForm: any) {
        try {
            // Explicitly cast the response to LoginResult (or any, depending on request.ts generics)
            // Assume request.ts interceptor unwraps res.data so we get { token, user }
            const res = await loginApi({ ...loginForm, loginType: 'store' }) as unknown as LoginResult

            const accessToken = res.token
            const userObj = res.user

            // Update state
            token.value = accessToken
            userInfo.value = userObj

            // Persist
            localStorage.setItem('token', accessToken)
            localStorage.setItem('user', JSON.stringify(userObj))

            return true
        } catch (error) {
            throw error
        }
    }

    // 2. Fetch latest user info
    async function fetchUserInfo() {
        if (!token.value) return
        try {
            const res = await getUserInfo()
            userInfo.value = res
            localStorage.setItem('user', JSON.stringify(res))
        } catch (error) {
            console.error('Failed to update user info', error)
        }
    }

    // 3. Logout
    function logout() {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('token')
        localStorage.removeItem('user')

        // Redirect to login with the current path for return
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
