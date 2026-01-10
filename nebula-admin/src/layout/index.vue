<template>
  <div class="app-wrapper">
    <!-- 左侧侧边栏 -->
    <div class="sidebar-container">
      <div class="logo-container">
        <!-- 请替换为您的 Logo -->
        <div class="logo-badge">N</div>
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
  background: linear-gradient(140deg, rgba(255, 255, 255, 0.9), rgba(236, 242, 255, 0.9));
}

/* 侧边栏样式 */
.sidebar-container {
  width: 210px;
  background: linear-gradient(180deg, #2a3142 0%, #1f2533 100%);
  height: 100%;
  transition: width 0.28s;
  display: flex;
  flex-direction: column;
  box-shadow: 10px 0 30px rgba(15, 23, 42, 0.18);
}

.logo-container {
  height: 50px;
  line-height: 50px;
  background: rgba(15, 23, 42, 0.4);
  text-align: center;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.logo-badge {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409EFF, #36cfc9);
  color: #fff;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 12px rgba(64, 158, 255, 0.25);
}

.logo-text {
  color: #fff;
  font-weight: 600;
  font-size: 18px;
  margin: 0;
  font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
  letter-spacing: 0.4px;
}

/* 主体容器 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: linear-gradient(180deg, #f7f9fc 0%, #eef1f6 100%);
}

/* 顶部导航栏样式 */
.navbar {
  height: 56px;
  overflow: hidden;
  position: relative;
  background: rgba(255, 255, 255, 0.78);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  backdrop-filter: blur(12px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
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
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.avatar-wrapper:hover {
  background: rgba(64, 158, 255, 0.08);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.15);
  transform: translateY(-1px);
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

/* 内容区域 */
.app-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* 动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.45s cubic-bezier(0.22, 1, 0.36, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.98);
  filter: blur(4px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-12px) scale(0.98);
  filter: blur(4px);
}
</style>
