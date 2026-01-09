import request from '@/utils/request'

// --- 认证 ---
export const loginApi = (data: any) => request({ url: '/auth/login', method: 'post', data })
export const registerApi = (data: any) => request({ url: '/auth/register/user', method: 'post', data })

// --- 商品与分类 ---
export const searchProducts = (params: any) => request({ url: '/portal/product/search', method: 'get', params })
export const getProductDetail = (id: number) => request({ url: `/portal/product/detail/${id}`, method: 'get' })
export const getCategoryList = () => request({ url: '/category/list', method: 'get' })
export const getNotices = () => request({ url: '/portal/notice/list', method: 'get' })
export const getNoticeDetail = (id: number) => request({ url: `/portal/notice/detail/${id}`, method: 'get' })

// --- 购物车 ---
export const getCartList = () => request({ url: '/cart/list', method: 'get' })
export const addToCart = (data: { productId: number, quantity: number }) => request({ url: '/cart/add', method: 'post', data })
export const updateCart = (data: { id: number, quantity?: number, selected?: boolean }) => request({ url: '/cart/update', method: 'post', data })
export const deleteCartItem = (id: number) => request({ url: `/cart/delete/${id}`, method: 'delete' })

// --- 订单 ---
export const createOrder = (data: any) => request({ url: '/order/create', method: 'post', data })
export const getMyOrders = (params: any) => request({ url: '/order/list', method: 'get', params })
export const getOrderDetailByNo = (orderNo: string) => request({ url: `/order/detail/${orderNo}`, method: 'get' })
export const payOrder = (orderNo: string) => request({ url: `/pay/order/${orderNo}`, method: 'post' })
export const receiveOrder = (orderNo: string) => request({ url: `/order/receive/${orderNo}`, method: 'post' })
export const cancelOrder = (orderNo: string) => request({ url: `/order/cancel/${orderNo}`, method: 'post' })

// --- 地址 & 用户 ---
export const getAddressList = () => request({ url: '/member/address/list', method: 'get' })
export const saveAddress = (data: any) => request({ url: '/member/address/save', method: 'post', data })
export const deleteAddress = (id: number) => request({ url: `/member/address/delete/${id}`, method: 'delete' })
export const setDefaultAddress = (id: number) => request({ url: `/member/address/default/${id}`, method: 'post' })
export const getUserInfo = () => request({ url: '/member/info', method: 'get' })
export const updateUserInfo = (data: any) => request({ url: '/member/update', method: 'post', data })
export const updatePassword = (data: any) => request({ url: '/member/password', method: 'post', data })

// --- 评价 ---
export const getProductReviews = (params: any) => request({ url: '/portal/review/list', method: 'get', params })
export const submitReview = (data: any) => request({ url: '/portal/review/add', method: 'post', data })

// --- 营销中心 ---
export const getCouponCenter = (params: any) => request({ url: '/marketing/coupon/center', method: 'get', params })
export const receiveCoupon = (id: number) => request({ url: `/marketing/coupon/receive/${id}`, method: 'post' })
export const getMyCoupons = () => request({ url: '/marketing/coupon/my', method: 'get' })
export const getUsableCoupons = (params: { orderAmount: string }) => request({ url: '/marketing/coupon/usable', method: 'get', params })
// [新增] 获取当前秒杀活动
export const getCurrentSeckills = () => request({ url: '/marketing/portal/seckill/list', method: 'get' })
// [Phase 13 新增] 申请退款
export const applyRefund = (data: { orderNo: string, reason: string }) => request({ url: '/order/refund', method: 'post', data })
