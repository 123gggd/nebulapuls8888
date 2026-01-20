<template>
  <div class="register-container">
    <div class="register-box">
      <div class="title">Nebula Store Registration</div>
      <div class="subtitle">Create your personal account to start shopping</div>

      <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          size="large"
          @keyup.enter="handleRegister"
          label-width="0"
      >
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="Set a username (used for sign-in)"
              :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
              v-model="form.nickname"
              placeholder="Set a display name (optional)"
              :prefix-icon="Avatar"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              placeholder="Set a password"
              type="password"
              show-password
              :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              placeholder="Confirm password"
              type="password"
              show-password
              :prefix-icon="Key"
          />
        </el-form-item>

        <el-button
            type="primary"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
        >
          Create Account
        </el-button>

        <div class="links">
          <span class="text-gray">Already have an account?</span>
          <el-button link type="primary" @click="$router.push('/login')">Sign in</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Avatar, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { registerApi } from '@/api/store' // [Fix] Use unified API

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

// Custom validation: confirm password
const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('Please confirm your password'))
  } else if (value !== form.password) {
    callback(new Error('Passwords do not match!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: 'Please enter a username', trigger: 'blur' },
    { min: 3, max: 20, message: 'Length must be 3 to 20 characters', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter a password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        // [Fix] Use the wrapped API to handle base URL and interceptors
        await registerApi({
          username: form.username,
          password: form.password,
          nickname: form.nickname
        })

        ElMessage.success('Registration successful. Please sign in.')
        // Redirect to the login page after successful registration
        setTimeout(() => {
          router.push('/login')
        }, 1000)
      } catch (e: any) {
        // Errors are handled by the request.ts interceptor (ElMessage.error)
        console.error(e)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  background-image: radial-gradient(#e6eaf0 1px, transparent 1px);
  background-size: 20px 20px;
}

.register-box {
  width: 420px;
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.title {
  text-align: center;
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 10px;
  color: #303133;
}

.subtitle {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-bottom: 30px;
}

.register-btn {
  width: 100%;
  margin-top: 10px;
  height: 40px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 1px;
}

.links {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  font-size: 14px;
}

.text-gray {
  color: #606266;
  margin-right: 5px;
}
</style>
