import request from '@/utils/request'

// --- 优惠券相关 ---

export function getCouponList(params: any) {
    return request({
        url: '/marketing/admin/coupon/list', // 管理员查询
        method: 'get',
        params
    })
}

export function createCoupon(data: any) {
    return request({
        url: '/marketing/admin/coupon/create',
        method: 'post',
        data
    })
}

export function updateCouponStatus(id: number, status: number) {
    return request({
        url: `/marketing/admin/coupon/status/${id}/${status}`,
        method: 'post'
    })
}

export function deleteCoupon(id: number) {
    return request({
        url: `/marketing/admin/coupon/delete/${id}`,
        method: 'delete'
    })
}

// --- 秒杀相关 ---

export function getSeckillList(params: any) {
    return request({
        url: '/marketing/admin/seckill/list',
        method: 'get',
        params
    })
}

export function createSeckill(data: any) {
    return request({
        url: '/marketing/admin/seckill/create',
        method: 'post',
        data
    })
}

// 预留接口，消除未使用变量警告
export function deleteSeckill(id: number) {
    // return request({ url: `/marketing/admin/seckill/delete/${id}`, method: 'delete' })
    console.log('Delete seckill', id)
    return Promise.resolve()
}
