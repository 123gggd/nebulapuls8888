import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'

// 静态路由 (无需权限即可访问的基础页面)
export const constantRoutes: RouteRecordRaw[] = [
    {
        path: '/login',
        component: () => import('@/views/login/index.vue'),
        meta: { hidden: true }
    },
    {
        path: '/404',
        component: () => import('@/views/error/404.vue'),
        meta: { hidden: true }
    },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '控制台', icon: 'Odometer' }
            },
            // [关键修复] 添加个人中心路由，否则点击头像菜单会 404
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/profile/index.vue'),
                meta: { title: '个人中心', hidden: true } // hidden:true 确保不在侧边栏显示
            }
        ]
    }
]

// 动态路由 (需要根据角色权限加载)
export const asyncRoutes: RouteRecordRaw[] = [
    // 1. 商品模块
    {
        path: '/product',
        component: Layout,
        redirect: '/product/list',
        meta: { title: '商品管理', icon: 'Goods', roles: ['ADMIN', 'MERCHANT'] },
        children: [
            {
                path: 'list',
                name: 'ProductList',
                component: () => import('@/views/product/list.vue'),
                meta: { title: '商品列表', icon: 'List' }
            },
            {
                path: 'category',
                name: 'Category',
                component: () => import('@/views/product/category.vue'),
                meta: { title: '分类管理', icon: 'Menu', roles: ['ADMIN'] }
            },
            {
                path: 'edit/:id?',
                name: 'ProductEdit',
                component: () => import('@/views/product/edit.vue'),
                meta: { title: '编辑商品', hidden: true, activeMenu: '/product/list' }
            }
        ]
    },
    // 2. 订单模块
    {
        path: '/order',
        component: Layout,
        redirect: '/order/list',
        meta: { title: '订单管理', icon: 'Tickets', roles: ['ADMIN', 'MERCHANT'] },
        children: [
            {
                path: 'list',
                name: 'OrderList',
                component: () => import('@/views/order/list.vue'),
                meta: { title: '订单列表', icon: 'List' }
            }
        ]
    },
    // 3. 营销模块
    {
        path: '/marketing',
        component: Layout,
        redirect: '/marketing/coupon',
        meta: { title: '营销中心', icon: 'Present', roles: ['ADMIN', 'MERCHANT'] },
        children: [
            {
                path: 'coupon',
                name: 'Coupon',
                component: () => import('@/views/marketing/coupon.vue'),
                meta: { title: '优惠券' }
            },
            {
                path: 'seckill',
                name: 'Seckill',
                component: () => import('@/views/marketing/seckill.vue'),
                meta: { title: '秒杀活动', roles: ['ADMIN', 'MERCHANT'] }
            }
        ]
    },
    // 4. 会员模块 (管理员专用)
    {
        path: '/member',
        component: Layout,
        redirect: '/member/list',
        meta: { title: '会员管理', icon: 'User', roles: ['ADMIN'] },
        children: [
            {
                path: 'list',
                name: 'MemberList',
                component: () => import('@/views/member/list.vue'),
                meta: { title: '会员列表' }
            },
            {
                path: 'merchant',
                name: 'MerchantList',
                component: () => import('@/views/member/merchant.vue'),
                meta: { title: '商家管理' }
            }
        ]
    },
    // 5. 系统模块
    {
        path: '/system',
        component: Layout,
        redirect: '/system/log',
        meta: { title: '系统设置', icon: 'Setting', roles: ['ADMIN'] },
        children: [
            {
                path: 'log',
                name: 'LogManage',
                component: () => import('@/views/system/log.vue'),
                meta: { title: '操作日志' }
            }
        ]
    },
    // 404 路由 (必须放在最后)
    {
        path: '/:pathMatch(.*)*',
        redirect: '/404',
        meta: { hidden: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes: constantRoutes
})

export default router