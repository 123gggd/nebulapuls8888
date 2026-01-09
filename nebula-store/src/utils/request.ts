import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
    baseURL: '/api', // Vite 代理地址
    timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // 在函数内部获取 store，确保 pinia 已安装
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    (error: any) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        // 后端标准响应结构: { code: 200, msg: 'success', data: ... }
        const res = response.data

        // 兼容处理：有些接口可能直接返回二进制流(Blob)
        if (response.config.responseType === 'blob') {
            return res
        }

        if (res.code === 200) {
            return res.data // 直接返回数据核心部分
        } else {
            ElMessage.error(res.msg || res.message || '系统繁忙')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    (error: any) => {
        const userStore = useUserStore()
        const { response } = error

        if (response) {
            // 401: Token 过期或未认证
            if (response.status === 401) {
                // 如果当前不在登录页，才执行登出逻辑，防止死循环
                if (router.currentRoute.value.path !== '/login') {
                    ElMessage.warning('登录状态已过期，请重新登录')
                    userStore.logout()
                }
            } else if (response.status === 403) {
                ElMessage.error('没有权限执行此操作')
            } else if (response.status === 404) {
                ElMessage.error('请求的资源不存在')
            } else {
                ElMessage.error(response.data?.msg || '服务器错误')
            }
        } else {
            ElMessage.error('网络连接异常，请检查网络')
        }
        return Promise.reject(error)
    }
)

export default service