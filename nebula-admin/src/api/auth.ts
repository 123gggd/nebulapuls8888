import request from '@/utils/request'

// 登录参数接口
export interface LoginData {
    username: string
    password: string
    loginType: string // 必填：'admin' | 'store'
}

// 商家注册参数
export interface MerchantRegisterData {
    username: string
    password: string
    storeName: string
    contactPhone?: string
}

/**
 * 登录接口
 */
export function login(data: LoginData) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

/**
 * 商家注册接口
 */
export function registerMerchant(data: MerchantRegisterData) {
    return request({
        url: '/auth/register/merchant',
        method: 'post',
        data
    })
}

/**
 * 退出登录 (前端清除Token即可，后端无状态)
 */
export function logout() {
    return Promise.resolve()
}