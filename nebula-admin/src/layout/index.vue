<template>
  <div class="app-wrapper">
    <!-- 左侧侧边栏 -->
    <div class="sidebar-container">
      <div class="logo-container">
        <!-- 请替换为您的 Logo -->
        <h1 class="logo-text" v-if="!isCollapse">Nebula Admin</h1>
      </div>
      <el-scrollbar>
        <el-menu
            :default-active="activeMenu"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
            :collapse="isCollapse"
            unique-opened
            router
            style="border-right: none;"
        >
          <!-- 动态渲染侧边栏 -->
          <template v-for="route in routes" :key="route.path">
            <!-- 1. 没有子路由或 hidden -->
            <el-menu-item
                v-if="!route.children && !route.meta?.hidden"
                :index="route.path"
            >
              <el-icon v-if="route.meta?.icon"><component :is="route.meta.icon" /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </el-menu-item>

            <!-- 2. 有子路由 -->
            <el-sub-menu
                v-if="route.children && route.children.length > 0 && !route.meta?.hidden"
                :index="route.path"
            >
              <template #title>
                <el-icon v-if="route.meta?.icon"><component :is="route.meta.icon" /></el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>

              <!-- 子菜单项 (支持一级嵌套) -->
              <template v-for="child in route.children" :key="child.path">
                <el-menu-item
                    v-if="!child.meta?.hidden"
                    :index="resolvePath(route.path, child.path)"
                >
                  <el-icon v-if="child.meta?.icon"><component :is="child.meta.icon" /></el-icon>
                  <span>{{ child.meta?.title }}</span>
                </el-menu-item>
              </template>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>
    </div>

    <!-- 右侧主体内容 -->
    <div class="main-container">
      <!-- [新增] 顶部导航栏 Navbar -->
      <div class="navbar">
        <div class="left-panel">
          <!-- 面包屑或其他左侧内容 (可选) -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="right-menu">
          <!-- 用户下拉菜单 -->
          <el-dropdown class="avatar-container" trigger="click" @command="handleCommand">
            <div class="avatar-wrapper">
              <el-avatar :size="35" :src="userStore.avatar" class="user-avatar">
                {{ userStore.name?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="user-name">{{ userStore.name }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 页面内容区域 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePermissionStore } from '@/stores/modules/permission'
import { useUserStore } from '@/stores/modules/user'
import { ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const userStore = useUserStore()

const isCollapse = ref(false) // 控制侧边栏收缩

const routes = computed(() => permissionStore.routes)
const activeMenu = computed(() => route.meta?.activeMenu || route.path)

// 路径拼接工具
const resolvePath = (basePath: string, routePath: string) => {
  if (routePath.startsWith('/')) return routePath
  return basePath === '/' ? '/' + routePath : basePath + '/' + routePath
}

// [核心] 处理下拉菜单点击
const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar-container {
  width: 210px;
  background-color: #304156;
  height: 100%;
  transition: width 0.28s;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 50px;
  line-height: 50px;
  background: #2b2f3a;
  text-align: center;
  overflow: hidden;
}

.logo-text {
  color: #fff;
  font-weight: 600;
  font-size: 18px;
  margin: 0;
  font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
}

/* 主体容器 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f0f2f5;
}

/* 顶部导航栏样式 */
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.right-menu {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 8px;
}

.avatar-wrapper:hover {
  background: rgba(0,0,0,.025);
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

/* 内容区域 */
.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all .5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>