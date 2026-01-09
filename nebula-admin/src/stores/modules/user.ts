import { defineStore } from 'pinia'
import { login, LoginData } from '@/api/auth'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        name: '',
        avatar: '',
        roles: [] as string[], // ['ADMIN'] 或 ['MERCHANT']
        merchantId: null as number | null
    }),
    actions: {
        // 登录 Action
        async login(loginForm: any) {
            // 1. 构造后端需要的参数，强制 loginType = 'admin'
            const req: LoginData = {
                username: loginForm.username,
                password: loginForm.password,
                loginType: 'admin' // [关键] 只有这个类型才能登录后台
            }

            // 2. 调用真实接口
            const res: any = await login(req)
            const { token } = res.data // 后端返回结构: { code: 200, data: { token: '...', user: {...} } }

            this.token = token
            localStorage.setItem('token', token)
        },

        // 获取用户信息 Action
        async getInfo() {
            // 调用 /api/member/info
            const res: any = await getUserInfo()
            const user = res.data

            if (!user) {
                throw new Error('验证失败，请重新登录')
            }

            // 后端返回的 role 是字符串 (e.g. "ADMIN")，前端路由守卫通常需要数组
            const roles = user.role ? [user.role] : []

            // 权限校验：如果用户不是 ADMIN 或 MERCHANT，禁止进入后台
            if (!roles.includes('ADMIN') && !roles.includes('MERCHANT')) {
                throw new Error('普通用户无权访问管理后台')
            }

            this.roles = roles
            this.name = user.nickname || user.username
            this.avatar = user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
            this.merchantId = user.merchantId

            return { roles }
        },

        // 登出 Action
        async logout() {
            this.token = ''
            this.roles = []
            localStorage.removeItem('token')
        },

        // 重置 Token
        async resetToken() {
            this.token = ''
            this.roles = []
            localStorage.removeItem('token')
        }
    }
})