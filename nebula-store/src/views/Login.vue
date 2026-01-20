<template>
  <div class="login-container">
    <div class="login-box">
      <div class="title">Nebula Store Sign In</div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="Username or phone" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              placeholder="Password"
              type="password"
              show-password
              :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">Sign In</el-button>
        <div class="links">
          <el-button link type="primary" @click="$router.push('/register')">Create account</el-button>
          <el-button link @click="handleForget">Forgot password?</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Please enter your account', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter your password', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        // [Fix] Use userStore.login instead of axios directly to keep logic consistent
        // Any non-200 response or network error will be caught here
        await userStore.login(form)

        ElMessage.success('Signed in successfully. Welcome back!')

        // Check for redirect
        const redirect = route.query.redirect as string
        if (redirect) {
          router.replace(redirect)
        } else {
          router.replace('/')
        }

      } catch (error: any) {
        // Errors are usually shown by request.ts interceptors
        // Log here for special handling if needed
        console.warn('Sign-in flow interrupted or failed', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleForget = () => {
  ElMessage.info('Please contact support to reset your password')
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  background-image: radial-gradient(#e6eaf0 1px, transparent 1px);
  background-size: 20px 20px;
}
.login-box {
  width: 400px;
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}
.title {
  text-align: center;
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 30px;
  color: #303133;
  letter-spacing: 1px;
}
.login-btn {
  width: 100%;
  margin-top: 10px;
  font-weight: bold;
  letter-spacing: 2px;
}
.links {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}
</style>
