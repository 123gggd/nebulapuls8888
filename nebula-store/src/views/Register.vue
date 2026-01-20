<template>
  <div class="register-container">
    <div class="register-box">
      <div class="title">Nebula Store 用户注册</div>
      <div class="subtitle">创建您的个人账号，开始购物之旅</div>

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
              placeholder="设置用户名 (作为登录账号)"
              :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
              v-model="form.nickname"
              placeholder="设置昵称 (可选)"
              :prefix-icon="Avatar"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              placeholder="设置登录密码"
              type="password"
              show-password
              :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              placeholder="确认登录密码"
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
          立即注册
        </el-button>

        <div class="links">
          <span class="text-gray">已有账号?</span>
          <el-button link type="primary" @click="$router.push('/login')">直接登录</el-button>
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
import { registerApi } from '@/api/store' // [修复] 使用统一 API

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

// 自定义校验规则：确认密码
const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
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
        // [修复] 调用封装好的 API，自动处理 BaseURL 和拦截器
        await registerApi({
          username: form.username,
          password: form.password,
          nickname: form.nickname
        })

        ElMessage.success('注册成功，请登录')
        // 注册成功后跳转回登录页
        setTimeout(() => {
          router.push('/login')
        }, 1000)
      } catch (e: any) {
        // 错误已由 request.ts 拦截器统一处理 (ElMessage.error)
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