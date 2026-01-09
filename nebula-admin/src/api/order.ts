import request from '@/utils/request'

// 订单状态枚举
export enum OrderStatus {
    PENDING_PAYMENT = 0,
    PENDING_DELIVERY = 1,
    SHIPPED = 2,
    COMPLETED = 3,
    CANCELLED = 4,
    REFUNDING = 5,
    REFUNDED = 6
}

export interface OrderDeliverData {
    orderNo: string
    logisticsCompany: string
    trackingNo: string
}

export interface OrderAuditData {
    orderNo: string
    pass: boolean
    remark?: string
}

// 获取订单列表
export function getOrderList(params: any) {
    return request({
        url: '/admin/order/list',
        method: 'get',
        params
    })
}

// 获取订单详情
// 后端返回结构: { order: Order, items: OrderItem[] }
export function getOrderDetail(id: number) {
    return request({
        url: `/admin/order/detail/${id}`,
        method: 'get'
    })
}

// 订单发货
export function deliverOrder(data: OrderDeliverData) {
    return request({
        url: '/admin/order/deliver',
        method: 'post',
        data
    })
}

// 售后审核
export function auditRefund(data: OrderAuditData) {
    return request({
        url: '/admin/order/audit',
        method: 'post',
        data
    })
}