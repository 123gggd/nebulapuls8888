import request from '@/utils/request'

// --- 普通会员管理 ---

// 获取会员列表 (仅普通用户)
export function getMemberList(params: any) {
    return request({
        url: '/admin/member/list',
        method: 'get',
        params
    })
}

// 更新会员状态 (封号/解封)
export function updateMemberStatus(id: number, status: number) {
    return request({
        url: `/admin/member/status/${id}/${status}`,
        method: 'post'
    })
}

// --- 商家管理 ---

// 获取商家列表
export function getMerchantList(params: any) {
    return request({
        url: '/admin/merchant/list',
        method: 'get',
        params
    })
}

// 创建商家账号
export function createMerchant(data: any) {
    return request({
        url: '/admin/merchant/create',
        method: 'post',
        data
    })
}