import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const service = axios.create({
    baseURL: '/api', // Vite proxy base URL
    timeout: 10000
})

NProgress.configure({ showSpinner: false })

// Request interceptor
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        NProgress.start()
        // Get the store inside the function to ensure pinia is installed
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    (error: any) => {
        NProgress.done()
        return Promise.reject(error)
    }
)

// Response interceptor
service.interceptors.response.use(
    (response: AxiosResponse) => {
        NProgress.done()
        // Backend standard response shape: { code: 200, msg: 'success', data: ... }
        const res = response.data

        // Compatibility: some endpoints return a binary stream (Blob)
        if (response.config.responseType === 'blob') {
            return res
        }

        if (res.code === 200) {
            return res.data // Return core data payload directly
        } else {
            ElMessage.error(res.msg || res.message || 'The system is busy')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    (error: any) => {
        NProgress.done()
        const userStore = useUserStore()
        const { response } = error

        if (response) {
            // 401: Token expired or unauthenticated
            if (response.status === 401) {
                // Only logout if we are not already on the login page to avoid loops
                if (router.currentRoute.value.path !== '/login') {
                    ElMessage.warning('Your session has expired. Please sign in again.')
                    userStore.logout()
                }
            } else if (response.status === 403) {
                ElMessage.error('You do not have permission to perform this action')
            } else if (response.status === 404) {
                ElMessage.error('The requested resource was not found')
            } else {
                ElMessage.error(response.data?.msg || response.data?.message || 'Server error')
            }
        } else {
            ElMessage.error('Network error. Please check your connection.')
        }
        return Promise.reject(error)
    }
)

export default service
