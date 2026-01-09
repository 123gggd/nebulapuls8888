import { defineStore } from 'pinia'
import { RouteRecordRaw } from 'vue-router'
import { constantRoutes, asyncRoutes } from '@/router'

// 判断该角色是否有权限访问该路由
const hasPermission = (roles: string[], route: RouteRecordRaw) => {
    if (route.meta && route.meta.roles) {
        // 路由定义了 roles 数组，检查用户角色是否命中
        return roles.some(role => (route.meta?.roles as string[]).includes(role))
    }
    // 没有定义 roles，说明是通用页面，均可访问
    return true
}

// 递归过滤路由表
export const filterAsyncRoutes = (routes: RouteRecordRaw[], roles: string[]) => {
    const res: RouteRecordRaw[] = []

    routes.forEach(route => {
        const tmp = { ...route }
        if (hasPermission(roles, tmp)) {
            if (tmp.children) {
                tmp.children = filterAsyncRoutes(tmp.children, roles)
            }
            res.push(tmp)
        }
    })

    return res
}

export const usePermissionStore = defineStore('permission', {
    state: () => ({
        routes: [] as RouteRecordRaw[], // 完整路由表 (用于侧边栏渲染)
        addRoutes: [] as RouteRecordRaw[] // 本次动态添加的路由
    }),
    actions: {
        generateRoutes(roles: string[]) {
            return new Promise<RouteRecordRaw[]>((resolve) => {
                let accessedRoutes

                // 如果是超级管理员，拥有所有权限
                if (roles.includes('ADMIN')) {
                    accessedRoutes = asyncRoutes || []
                } else {
                    // 否则根据角色过滤
                    accessedRoutes = filterAsyncRoutes(asyncRoutes, roles)
                }

                this.addRoutes = accessedRoutes
                // 拼接静态路由 + 动态路由，形成最终侧边栏
                this.routes = constantRoutes.concat(accessedRoutes)
                resolve(accessedRoutes)
            })
        }
    }
})