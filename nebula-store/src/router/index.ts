import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { title: '用户登录' }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { title: '用户注册' }
    },
    {
        path: '/',
        component: Layout,
        redirect: '/home',
        children: [
            {
                path: 'home',
                name: 'Home',
                component: () => import('@/views/Home.vue'),
                meta: { title: '首页' }
            },
            {
                path: 'search',
                name: 'Search',
                component: () => import('@/views/Search.vue'),
                meta: { title: '商品搜索' }
            },
            {
                path: 'product/:id',
                name: 'ProductDetail',
                component: () => import('@/views/ProductDetail.vue'),
                meta: { title: '商品详情' }
            },
            {
                path: 'notice',
                name: 'NoticeList',
                component: () => import('@/views/NoticeList.vue'),
                meta: { title: '公告列表' }
            },
            {
                path: 'notice/:id',
                name: 'NoticeDetail',
                component: () => import('@/views/NoticeDetail.vue'),
                meta: { title: '公告详情' }
            },
            // --- 需要登录的页面 ---
            {
                path: 'cart',
                name: 'Cart',
                component: () => import('@/views/Cart.vue'),
                meta: { title: '购物车', requiresAuth: true }
            },
            {
                path: 'order',
                name: 'OrderList',
                component: () => import('@/views/Order.vue'),
                meta: { title: '我的订单', requiresAuth: true }
            },
            {
                path: 'address',
                name: 'Address',
                component: () => import('@/views/Address.vue'),
                meta: { title: '地址管理', requiresAuth: true }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/Profile.vue'),
                meta: { title: '个人中心', requiresAuth: true }
            },
            {
                path: 'pay',
                name: 'Pay',
                component: () => import('@/views/Pay.vue'),
                meta: { title: '订单支付', requiresAuth: true }
            },
            {
                path: 'coupon',
                name: 'CouponCenter',
                component: () => import('@/views/CouponCenter.vue'),
                meta: { title: '领券中心', requiresAuth: true }
            },
            {
                path: 'my-coupon',
                name: 'MyCoupon',
                component: () => import('@/views/MyCoupon.vue'),
                meta: { title: '我的优惠券', requiresAuth: true }
            },{
                path: '/:pathMatch(.*)*',
                name: 'NotFound',
                component: () => import('@/views/404.vue'),
                meta: { title: '404' }
            }
        ]
    },
    // 404 页面
    {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    // 将 to, from 改为 _to, _from 以忽略未使用警告
    scrollBehavior(_to, _from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

// 全局路由守卫
// 将 from 改为 _from 以忽略未使用警告
router.beforeEach((to, _from, next) => {
    const userStore = useUserStore()

    // 设置页面标题
    document.title = (to.meta.title as string) ? `${to.meta.title} - Nebula Store` : 'Nebula Store'

    // 鉴权逻辑
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        next({
            path: '/login',
            query: { redirect: to.fullPath }
        })
    } else {
        next()
    }
})

export default router
