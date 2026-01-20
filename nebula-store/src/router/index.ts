import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { title: 'Sign In' }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { title: 'Register' }
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
                meta: { title: 'Home' }
            },
            {
                path: 'search',
                name: 'Search',
                component: () => import('@/views/Search.vue'),
                meta: { title: 'Product Search' }
            },
            {
                path: 'product/:id',
                name: 'ProductDetail',
                component: () => import('@/views/ProductDetail.vue'),
                meta: { title: 'Product Details' }
            },
            {
                path: 'notice',
                name: 'NoticeList',
                component: () => import('@/views/NoticeList.vue'),
                meta: { title: 'Announcements' }
            },
            {
                path: 'notice/:id',
                name: 'NoticeDetail',
                component: () => import('@/views/NoticeDetail.vue'),
                meta: { title: 'Announcement Details' }
            },
            // --- Pages that require sign-in ---
            {
                path: 'cart',
                name: 'Cart',
                component: () => import('@/views/Cart.vue'),
                meta: { title: 'Cart', requiresAuth: true }
            },
            {
                path: 'order',
                name: 'OrderList',
                component: () => import('@/views/Order.vue'),
                meta: { title: 'My Orders', requiresAuth: true }
            },
            {
                path: 'address',
                name: 'Address',
                component: () => import('@/views/Address.vue'),
                meta: { title: 'Address Book', requiresAuth: true }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/Profile.vue'),
                meta: { title: 'Profile', requiresAuth: true }
            },
            {
                path: 'pay',
                name: 'Pay',
                component: () => import('@/views/Pay.vue'),
                meta: { title: 'Checkout', requiresAuth: true }
            },
            {
                path: 'coupon',
                name: 'CouponCenter',
                component: () => import('@/views/CouponCenter.vue'),
                meta: { title: 'Coupon Center', requiresAuth: true }
            },
            {
                path: 'my-coupon',
                name: 'MyCoupon',
                component: () => import('@/views/MyCoupon.vue'),
                meta: { title: 'My Coupons', requiresAuth: true }
            },{
                path: '/:pathMatch(.*)*',
                name: 'NotFound',
                component: () => import('@/views/404.vue'),
                meta: { title: '404' }
            }
        ]
    },
    // 404 page
    {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    // Rename to/from to _to/_from to avoid unused warnings
    scrollBehavior(_to, _from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

// Global route guard
// Rename from to _from to avoid unused warnings
router.beforeEach((to, _from, next) => {
    const userStore = useUserStore()

    // Set page title
    document.title = (to.meta.title as string) ? `${to.meta.title} - Nebula Store` : 'Nebula Store'

    // Auth logic
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        ElMessage.warning('Please sign in first')
        next({
            path: '/login',
            query: { redirect: to.fullPath }
        })
    } else {
        next()
    }
})

export default router
