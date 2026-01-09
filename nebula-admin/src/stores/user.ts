import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getInfo as getInfoApi } from '@/api/auth' // 假设你有对应的 api 文件，如果没有可暂时注释
import { getToken, setToken, removeToken } from '@/utils/auth' // 假设你有 auth 工具，如果没有请使用 localStorage
import { ElMessage } from 'element-plus'

// 简单的本地存储兜底 (如果你没有 @/utils/auth)
const storage = {
    get: () => localStorage.getItem('token'),
    set: (token: string) => localStorage.setItem('token', token),
    remove: () => localStorage.removeItem('token')
}

export const useUserStore = defineStore('user', {
    state: () => ({
        token: storage.get() || '',
        name: '',
        avatar: '',
        roles: [] as string[],
        merchantId: null as number | null // Phase 2: 增加商家ID状态
    }),

    actions: {
        // 登录
        async login(userInfo: any) {
            const { username, password } = userInfo
            return new Promise<void>((resolve, reject) => {
                // 模拟 API 调用 (如果后端还没好，可以用这段 Mock)
                // const data = { token: 'mock-token-admin' }
                // this.token = data.token
                // storage.set(data.token)
                // resolve()

                // 真实调用
                loginApi({ username: username.trim(), password: password }).then((res: any) => {
                    const data = res.data || res
                    this.token = data.token
                    storage.set(data.token)
                    resolve()
                }).catch((error: any) => {
                    reject(error)
                })
            })
        },

        // 获取用户信息
        async getInfo() {
            return new Promise<any>((resolve, reject) => {
                getInfoApi().then((res: any) => {
                    const data = res.data || res

                    if (!data) {
                        return reject('Verification failed, please Login again.')
                    }

                    const { roles, name, avatar, merchantId } = data

                    // 鲁棒性检查: roles 必须是数组
                    if (!roles || roles.length <= 0) {
                        reject('getInfo: roles must be a non-null array!')
                    }

                    this.roles = roles
                    this.name = name
                    this.avatar = avatar
                    this.merchantId = merchantId // 存储商家ID

                    resolve(data)
                }).catch((error: any) => {
                    reject(error)
                })
            })
        },

        // 登出
        async logout() {
            return new Promise<void>((resolve, reject) => {
                logoutApi().then(() => {
                    this.resetToken() // 调用下方的 resetToken
                    resolve()
                }).catch((error: any) => {
                    reject(error)
                })
            })
        },

        // --- 这里是你报错的方法 ---
        // 确保它在 actions 对象内部，并且上面的 logout 方法结束后有逗号
        async resetToken() {
            return new Promise<void>(resolve => {
                this.token = ''
                this.roles = []
                this.merchantId = null
                storage.remove()
                resolve()
            })
        }
    }
})