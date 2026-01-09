<template>
  <div class="store-layout">
    <div class="nav-bar">
      <div class="container nav-content">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <el-icon class="icon"><Goods /></el-icon> Nebula Store
        </div>

        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
              v-model="keyword"
              placeholder="搜索商品..."
              class="search-input"
              :prefix-icon="Search"
              @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <!-- 菜单 -->
        <div class="menus">
          <router-link to="/" class="nav-link" active-class="active">首页</router-link>
          <router-link to="/coupon" class="nav-link" active-class="active">领券中心</router-link>
          <router-link to="/cart" class="nav-link" active-class="active">购物车</router-link>
          <router-link to="/order" class="nav-link" active-class="active">我的订单</router-link>

          <!-- 登录状态判断 -->
          <div class="user-actions" v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click">
              <span class="el-dropdown-link">
                <!-- 优先显示头像，没有则显示首字母 -->
                <el-avatar :size="30" class="avatar" :src="userStore.avatar">
                   {{ userStore.username ? userStore.username.charAt(0).toUpperCase() : 'U' }}
                </el-avatar>
                <span class="username">{{ userStore.username }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/address')">地址管理</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/my-coupon')">我的优惠券</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="user-actions" v-else>
            <el-button type="primary" size="small" @click="$router.push('/login')" round>登录 / 注册</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="container main-content">
      <!-- 路由占位符，添加 key 确保路由参数变化时组件刷新 -->
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
    </div>

    <div class="footer">
      <p>© 2026 Nebula Commerce - 极速电商体验</p>
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
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已安全退出')
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.store-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}
.nav-bar {
  background: white;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 100;
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
  .icon { color: #409EFF; }
}
.search-box {
  flex: 1;
  max-width: 400px;
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
  padding-bottom: 4px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;

  &:hover { color: #409EFF; }

  // 激活状态
  &.router-link-active, &.active {
    color: #409EFF;
    border-bottom-color: #409EFF;
  }
}
.user-actions {
  margin-left: 20px;
  display: flex;
  align-items: center;
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
  padding: 30px 0;
  width: 1200px; /* 确保内容区也是定宽居中 */
  margin: 0 auto;
}
.footer {
  background: #2b323c;
  color: #909399;
  text-align: center;
  padding: 30px;
  margin-top: auto;
}

/* 简单的淡入淡出过渡 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>