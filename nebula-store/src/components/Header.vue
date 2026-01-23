<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { computed } from 'vue'

const router = useRouter()

// 动态获取用户信息
const user = computed(() => {
  const u = localStorage.getItem('user')
  return u ? JSON.parse(u) : null
})

// [关键修复] 退出登录逻辑
const handleLogout = () => {
  // 1. 清除数据
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  sessionStorage.clear()

  ElMessage.success('已退出登录')

  // 2. [关键] 强制刷新并跳转到登录页
  // 使用 window.location.href 替代 router.push，确保状态彻底重置
  setTimeout(() => {
    window.location.href = '/login'
  }, 500)
}
</script>

<template>
  <div class="store-header">
    <div class="inner-container">
      <div class="logo" @click="router.push('/')">
        Nebula Store
      </div>

      <div class="nav-actions">
        <!-- 已登录 -->
        <div v-if="user" class="user-panel">
          <span class="welcome">Hi, {{ user.nickname || user.username }}</span>
          <el-divider direction="vertical" />
          <el-button link @click="router.push('/order')">我的订单</el-button>
          <el-button link @click="router.push('/address')">地址管理</el-button>
          <el-button link type="danger" @click="handleLogout">退出</el-button>
        </div>

        <!-- 未登录 -->
        <div v-else class="auth-panel">
          <el-button type="primary" link @click="router.push('/login')">登录</el-button>
          <el-divider direction="vertical" />
          <el-button type="primary" link @click="router.push('/register')">注册</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.store-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 999;
}
.inner-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.logo {
  font-size: 22px;
  font-weight: 800;
  color: #409EFF;
  cursor: pointer;
}
.nav-actions {
  display: flex;
  align-items: center;
}
.user-panel, .auth-panel {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}
.welcome {
  color: #606266;
  font-weight: bold;
}
</style>
