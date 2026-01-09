import request from '@/utils/request'

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
    return request({
        url: '/member/info',
        method: 'get'
    })
}

/**
 * 修改密码
 */
export function updatePassword(data: any) {
    return request({
        url: '/auth/password',
        method: 'post',
        data
    })
}