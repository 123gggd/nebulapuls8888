<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="header-content">
          <!-- 替换为你的 Logo 地址 -->
          <img src="https://element-plus.org/images/element-plus-logo.svg" alt="logo" class="logo hover-zoom">
          <h2 class="login-title">Nebula Admin</h2>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="custom-tabs" stretch>

        <!-- 登录 Tab -->
        <el-tab-pane label="账号登录" name="login">
          <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              size="large"
              @submit.prevent
          >
            <el-form-item prop="username">
              <el-input
                  v-model="loginForm.username"
                  placeholder="管理员 / 商家账号"
                  :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  :prefix-icon="Lock"
                  @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                  type="primary"
                  :loading="loading"
                  class="full-btn"
                  @click="handleLogin"
              >
                立即登录
              </el-button>
            </el-form-item>

            <div class="form-footer">
              <el-button link type="primary" @click="activeTab = 'register'">商家入驻申请</el-button>
              <el-button link @click="handleForgetPwd">忘记密码?</el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 商家注册 Tab -->
        <el-tab-pane label="商家入驻" name="register">
          <el-form
              ref="regFormRef"
              :model="regForm"
              :rules="regRules"
              size="large"
              @submit.prevent
          >
            <el-form-item prop="username">
              <el-input v-model="regForm.username" placeholder="设置登录账号 (至少4位)" :prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="regForm.password" type="password" placeholder="设置登录密码 (至少6位)" show-password :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item prop="storeName">
              <el-input v-model="regForm.storeName" placeholder="店铺名称" :prefix-icon="Shop" />
            </el-form-item>
            <el-form-item prop="contactPhone">
              <el-input v-model="regForm.contactPhone" placeholder="联系电话 (选填)" :prefix-icon="Phone" />
            </el-form-item>

            <el-form-item>
              <el-button type="success" :loading="regLoading" class="full-btn" @click="handleRegister">
                提交申请
              </el-button>
            </el-form-item>

            <div class="form-footer center-footer">
              <span style="color: #999">已有账号? </span>
              <el-button link type="primary" @click="activeTab = 'login'">立即登录</el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 管理员注册 Tab -->
        <el-tab-pane label="管理员注册" name="admin">
          <el-form
              ref="adminFormRef"
              :model="adminForm"
              :rules="adminRules"
              size="large"
              @submit.prevent
          >
            <el-form-item prop="username">
              <el-input v-model="adminForm.username" placeholder="管理员账号 (至少4位)" :prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="adminForm.password" type="password" placeholder="管理员密码 (至少6位)" show-password :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item prop="inviteCode">
              <el-input v-model="adminForm.inviteCode" placeholder="管理员邀请码" :prefix-icon="Key" />
            </el-form-item>

            <el-form-item>
              <el-button type="warning" :loading="adminLoading" class="full-btn" @click="handleAdminRegister">
                注册管理员
              </el-button>
            </el-form-item>

            <div class="form-footer center-footer">
              <span style="color: #999">已有账号? </span>
              <el-button link type="primary" @click="activeTab = 'login'">立即登录</el-button>
            </div>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Shop, Phone, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import { registerMerchant, registerAdmin } from '@/api/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('login')
const loginFormRef = ref<FormInstance>()
const regFormRef = ref<FormInstance>()
const adminFormRef = ref<FormInstance>()
const loading = ref(false)
const regLoading = ref(false)
const adminLoading = ref(false)

// --- 登录逻辑 ---
const loginForm = reactive({ username: '', password: '' })
const loginRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success('登录成功')
        router.push((route.query.redirect as string) || '/')
      } catch (error: any) {
        console.error(error)
        // 错误信息已经在 request.ts 拦截器中处理，此处不需要重复弹窗
      } finally {
        loading.value = false
      }
    }
  })
}

// --- 注册逻辑 ---
const regForm = reactive({ username: '', password: '', storeName: '', contactPhone: '' })
const regRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号长度至少4位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  storeName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }]
})

// --- 管理员注册逻辑 ---
const adminForm = reactive({ username: '', password: '', inviteCode: '' })
const adminRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号长度至少4位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  inviteCode: [{ required: true, message: '请输入管理员邀请码', trigger: 'blur' }]
})

const handleRegister = async () => {
  if (!regFormRef.value) return
  await regFormRef.value.validate(async (valid) => {
    if (valid) {
      regLoading.value = true
      try {
        await registerMerchant(regForm)
        ElMessage.success('注册成功，请登录')
        // 切换回登录 Tab 并预填账号
        loginForm.username = regForm.username
        activeTab.value = 'login'
        // 清空注册表单
        regForm.username = ''
        regForm.password = ''
        regForm.storeName = ''
        regForm.contactPhone = ''
      } catch (e) {
        // error handled by request interceptor
      } finally {
        regLoading.value = false
      }
    }
  })
}

const handleAdminRegister = async () => {
  if (!adminFormRef.value) return
  await adminFormRef.value.validate(async (valid) => {
    if (valid) {
      adminLoading.value = true
      try {
        await registerAdmin(adminForm)
        ElMessage.success('管理员注册成功，请登录')
        loginForm.username = adminForm.username
        activeTab.value = 'login'
        adminForm.username = ''
        adminForm.password = ''
        adminForm.inviteCode = ''
      } catch (e) {
        // error handled by request interceptor
      } finally {
        adminLoading.value = false
      }
    }
  })
}

// --- 忘记密码 ---
const handleForgetPwd = () => {
  ElMessageBox.alert(
      '<p>如需重置密码，请联系平台管理员。</p><p>管理员邮箱: <strong>admin@nebula.com</strong></p>',
      '忘记密码',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '知道了',
        type: 'info'
      }
  )
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #2d3a4b; /* 深色背景 */
  background-image: linear-gradient(135deg, #2d3a4b 0%, #1a232e 100%);
  position: relative;
  overflow: hidden;
}

/* 简单的背景装饰点缀 */
.login-container::before {
  content: "";
  position: absolute;
  top: -10%;
  left: -10%;
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, rgba(64,158,255,0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.login-card {
  width: 420px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.header-content {
  text-align: center;
  padding: 20px 0 10px;
}

.logo {
  height: 48px;
  margin-bottom: 12px;
}

.login-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.full-btn {
  width: 100%;
  padding: 20px 0;
  font-size: 16px;
  margin-top: 10px;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0px;
}

.center-footer {
  justify-content: center;
}

/* 自定义 Tabs 样式微调 */
:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #eee;
}
:deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 20px;
}
</style>
