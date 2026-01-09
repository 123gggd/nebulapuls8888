import axios, { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
    baseURL: '/api', // 走 Vite 代理
    timeout: 10000,
    headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

// 请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const userStore = useUserStore()
        if (userStore.token) {
            // 这里的 Key 必须和后端 JwtAuthenticationFilter 中检查的 Header 一致
            config.headers['Authorization'] = `Bearer ${userStore.token}`
        }
        return config
    },
    (error: any) => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data
        // 二进制数据直接返回
        if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
            return response
        }

        // 后端约定：code === 200 为成功
        if (res.code === 200) {
            return res
        } else {
            ElMessage({
                message: res.message || '系统错误',
                type: 'error',
                duration: 5 * 1000
            })
            return Promise.reject(new Error(res.message || 'Error'))
        }
    },
    (error: any) => {
        const { response } = error
        if (response) {
            // 401: Token 过期或未登录
            if (response.status === 401) {
                ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
                    confirmButtonText: '重新登录',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    const userStore = useUserStore()
                    userStore.logout().then(() => {
                        router.push('/login')
                    })
                })
            } else if (response.status === 403) {
                ElMessage.error('拒绝访问：您没有权限执行此操作')
            } else {
                ElMessage.error(response.data?.message || '网络请求错误')
            }
        } else {
            ElMessage.error('网络连接异常')
        }
        return Promise.reject(error)
    }
)

export default service