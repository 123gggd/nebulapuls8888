<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { computed } from 'vue'

const router = useRouter()

// Dynamically fetch user info
const user = computed(() => {
  const u = localStorage.getItem('user')
  return u ? JSON.parse(u) : null
})

// [Critical fix] Logout flow
const handleLogout = () => {
  // 1. Clear data
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  sessionStorage.clear()

  ElMessage.success('Signed out successfully')

  // 2. [Critical] Force refresh and redirect to the login page
  // Use window.location.href instead of router.push to fully reset state
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
        <!-- Signed in -->
        <div v-if="user" class="user-panel">
          <span class="welcome">Hi, {{ user.nickname || user.username }}</span>
          <el-divider direction="vertical" />
          <el-button link @click="router.push('/order')">My Orders</el-button>
          <el-button link @click="router.push('/user/address')">Address Book</el-button>
          <el-button link type="danger" @click="handleLogout">Sign Out</el-button>
        </div>

        <!-- Signed out -->
        <div v-else class="auth-panel">
          <el-button type="primary" link @click="router.push('/login')">Sign In</el-button>
          <el-divider direction="vertical" />
          <el-button type="primary" link @click="router.push('/register')">Register</el-button>
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
