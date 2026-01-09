<template>
  <div class="login-container">
    <div class="login-box">
      <div class="title">Nebula Store 用户登录</div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名/手机号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              placeholder="密码"
              type="password"
              show-password
              :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        <div class="links">
          <el-button link type="primary" @click="$router.push('/register')">注册账号</el-button>
          <el-button link @click="handleForget">忘记密码?</el-button>
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
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        // [修复] 使用 userStore.login 而不是直接 axios，保持逻辑统一
        // 任何非 200 的响应或网络错误都会在这里被 catch 捕获
        await userStore.login(form)

        ElMessage.success('登录成功，欢迎回来')

        // 判断是否有重定向地址
        const redirect = route.query.redirect as string
        if (redirect) {
          router.replace(redirect)
        } else {
          router.replace('/')
        }

      } catch (error: any) {
        // 错误通常已经被 request.ts 拦截器弹出 ElMessage
        // 这里仅记录日志，或者处理特定的业务逻辑
        console.warn('登录流程中断或失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleForget = () => {
  ElMessage.info('请联系客服重置密码')
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