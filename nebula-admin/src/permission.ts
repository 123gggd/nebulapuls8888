import router from '@/router'
import { useUserStore } from '@/stores/modules/user'
import { usePermissionStore } from '@/stores/modules/permission'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/404', '/register']

// 修复: 将 'from' 改为 '_from' 以避免 "declared but its value is never read" 警告
router.beforeEach(async (to, _from, next) => {
    NProgress.start()

    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    // 简单判断 token 是否存在
    const hasToken = userStore.token || localStorage.getItem('token')

    if (hasToken) {
        if (to.path === '/login') {
            next({ path: '/' })
            NProgress.done()
        } else {
            // 检查是否已拉取用户信息（包含角色）
            const hasRoles = userStore.roles && userStore.roles.length > 0

            if (hasRoles) {
                next()
            } else {
                try {
                    // 1. 重新获取用户信息 (防止刷新页面丢失 Vuex/Pinia 状态)
                    // 注意: userStore.getInfo() 需要返回 { roles: [] } 结构
                    const { roles } = await userStore.getInfo()

                    // 2. 基于角色生成动态路由
                    const accessRoutes = await permissionStore.generateRoutes(roles)

                    // 3. 动态添加到 Router
                    accessRoutes.forEach(route => {
                        router.addRoute(route)
                    })

                    // 4. replace: true 确保路由添加完毕，避免 404
                    next({ ...to, replace: true })
                } catch (error) {
                    // Token 失效或接口异常，重置状态去登录页
                    await userStore.resetToken() // 调用 resetToken 清理状态
                    ElMessage.error(error.message || 'Has Error')
                    next(`/login?redirect=${to.path}`)
                    NProgress.done()
                }
            }
        }
    } else {
        // 无 Token
        if (whiteList.indexOf(to.path) !== -1) {
            next()
        } else {
            next(`/login?redirect=${to.path}`)
            NProgress.done()
        }
    }
})

router.afterEach(() => {
    NProgress.done()
})