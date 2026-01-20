<template>
  <div class="store-layout">
    <div class="nav-bar">
      <div class="container nav-content">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <span class="logo-mark">
            <el-icon class="icon"><Goods /></el-icon>
          </span>
          <span class="logo-text">Nebula Store</span>
        </div>

        <!-- Search box -->
        <div class="search-box">
          <el-input
              v-model="keyword"
              placeholder="Search products..."
              class="search-input"
              :prefix-icon="Search"
              @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">Search</el-button>
            </template>
          </el-input>
        </div>

        <!-- Menu -->
        <div class="menus">
          <router-link to="/" class="nav-link" active-class="active">Home</router-link>
          <router-link to="/coupon" class="nav-link" active-class="active">Coupon Center</router-link>
          <router-link to="/cart" class="nav-link" active-class="active">Cart</router-link>
          <router-link to="/order" class="nav-link" active-class="active">My Orders</router-link>

          <!-- Auth state -->
          <div class="user-actions" v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click">
              <span class="el-dropdown-link">
                <!-- Show avatar first, fallback to initial -->
                <el-avatar :size="30" class="avatar" :src="userStore.avatar">
                   {{ userStore.username ? userStore.username.charAt(0).toUpperCase() : 'U' }}
                </el-avatar>
                <span class="username">{{ userStore.username }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">Profile</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/address')">Address Book</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/my-coupon')">My Coupons</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">Sign Out</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="user-actions" v-else>
            <el-button type="primary" size="small" @click="$router.push('/login')" round>Sign In / Register</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <!-- Router outlet with a key to refresh on param changes -->
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
    </div>

    <div class="footer">
      <p>© 2026 Nebula Commerce - Lightning-fast shopping experience</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Goods, ArrowDown, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const keyword = ref('')

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value } })
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('Are you sure you want to sign out?', 'Confirm', {
    confirmButtonText: 'Sign Out',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('Signed out safely')
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.store-layout {
  min-height: 100vh;
  background: linear-gradient(180deg, #f7faff 0%, #f5f7fa 40%, #f1f4f8 100%);
  display: flex;
  flex-direction: column;
}
.nav-bar {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(6px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
  height: 68px;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}
.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
.nav-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  -webkit-background-clip: text;
  color: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 40px;
  transition: transform 0.2s ease;
  &:hover { transform: translateY(-1px); }
  .logo-mark {
    display: inline-flex;
    width: 34px;
    height: 34px;
    border-radius: 10px;
    background: linear-gradient(135deg, #409EFF, #36cfc9);
    color: #fff;
    align-items: center;
    justify-content: center;
    box-shadow: 0 6px 14px rgba(64, 158, 255, 0.25);
  }
  .icon { color: #fff; }
  .logo-text { letter-spacing: 0.3px; }
}
.search-box {
  flex: 1;
  max-width: 400px;
  margin-right: 20px;
  :deep(.el-input__wrapper) {
    border-radius: 999px;
    box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
    transition: box-shadow 0.2s ease, transform 0.2s ease;
  }
  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 10px 26px rgba(64, 158, 255, 0.25);
    transform: translateY(-1px);
  }
  .search-input :deep(.el-input-group__append) {
    background-color: #409EFF;
    color: white;
    border-color: #409EFF;
    cursor: pointer;
    transition: background 0.3s;
    &:hover { background-color: #66b1ff; }
  }
}
.menus {
  display: flex;
  align-items: center;
  gap: 30px;
}
.nav-link {
  text-decoration: none;
  color: #606266;
  font-size: 16px;
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  transition: color 0.2s ease;

  &:after {
    content: '';
    position: absolute;
    left: 0;
    bottom: -6px;
    width: 100%;
    height: 2px;
    border-radius: 999px;
    background: linear-gradient(90deg, #409EFF, #36cfc9);
    transform: scaleX(0);
    transform-origin: left;
    transition: transform 0.25s ease;
  }

  &:hover {
    color: #409EFF;
    &:after { transform: scaleX(1); }
  }

  // Active state
  &.router-link-active, &.active {
    color: #409EFF;
    &:after { transform: scaleX(1); }
  }
}
.user-actions {
  margin-left: 20px;
  display: flex;
  align-items: center;
  :deep(.el-button) {
    box-shadow: 0 6px 14px rgba(64, 158, 255, 0.2);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }
  :deep(.el-button:hover) {
    transform: translateY(-1px);
    box-shadow: 0 10px 20px rgba(64, 158, 255, 0.28);
  }
  .el-dropdown-link {
    cursor: pointer;
    display: flex;
    align-items: center;
    outline: none;
    .avatar { background-color: #409EFF; margin-right: 8px; }
    .username { font-weight: bold; color: #303133; max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  }
}
.main-content {
  flex: 1;
  padding: 36px 0 60px;
  width: 1200px; /* Keep the content area centered with a fixed width */
  margin: 0 auto;
}
.footer {
  background: linear-gradient(90deg, #1f2a44, #2b323c);
  color: #cbd5f5;
  text-align: center;
  padding: 34px;
  margin-top: auto;
}

/* Page transition */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
