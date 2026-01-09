<template>
  <div class="profile-page">
    <el-card shadow="never" style="border-radius: 12px;">
      <template #header>
        <div style="font-weight: bold; font-size: 18px;">个人中心</div>
      </template>

      <el-tabs tab-position="left" style="height: 400px;">
        <el-tab-pane label="基本资料">
          <div class="form-wrapper">
            <el-form :model="userInfo" label-width="80px">
              <el-form-item label="账号">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="userInfo.nickname" placeholder="设置您的昵称" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="userInfo.phone" placeholder="绑定手机号" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userInfo.email" placeholder="绑定邮箱" />
              </el-form-item>

              <!-- 头像上传 -->
              <el-form-item label="头像">
                <el-upload
                    class="avatar-uploader"
                    action="#"
                    :http-request="handleAvatarUpload"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                >
                  <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div style="font-size: 12px; color: #999; line-height: 1.5; margin-top: 5px;">
                  点击图片更换头像 (JPG/PNG, &lt;2MB)
                </div>
              </el-form-item>

              <el-form-item label="注册时间">
                <span>{{ userInfo.createTime }}</span>
              </el-form-item>
              <el-form-item>
                <!-- [修复] 方法名与 Script 保持一致 -->
                <el-button type="primary" @click="handleUpdateProfile" :loading="loading">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="安全设置">
          <div class="form-wrapper">
            <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="danger" @click="handleUpdatePwd" :loading="loading">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserInfo, updateUserInfo, updatePassword } from '@/api/store'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

interface UserInfo {
  username: string
  nickname: string
  avatar: string
  phone: string
  email: string
  createTime?: string
}

const userStore = useUserStore()
const loading = ref(false)
const pwdFormRef = ref()
const userInfo = ref<UserInfo>({
  username: '',
  nickname: '',
  avatar: '',
  phone: '',
  email: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadInfo = async () => {
  try {
    const res = await getUserInfo() as unknown as UserInfo
    userInfo.value = res
  } catch(e) {
    console.error(e)
  }
}

// 图片格式校验
const beforeAvatarUpload = (file: any) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const handleAvatarUpload = async (options: any) => {
  const formData = new FormData()
  formData.append('file', options.file)

  let token = userStore.token
  if (token && !token.startsWith('Bearer ')) {
    token = 'Bearer ' + token
  }

  try {
    const res = await axios.post('/api/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': token
      }
    })

    const result = res.data
    if (result.code === 200) {
      userInfo.value.avatar = result.data // 假设后端返回图片URL
      ElMessage.success('头像上传成功')
      // 立即更新用户信息中的头像字段，避免刷新丢失
      await updateUserInfo({ avatar: result.data })
      userStore.username = userInfo.value.nickname // 触发 store 更新
    } else {
      ElMessage.error(result.msg || '上传失败')
    }
  } catch (error: any) {
    console.error(error)
    ElMessage.error('上传出错，请检查网络')
  }
}

// [修复] 重命名为 handleUpdateProfile 以匹配模板
const handleUpdateProfile = async () => {
  loading.value = true
  try {
    await updateUserInfo(userInfo.value)
    ElMessage.success('资料更新成功')
    // 更新 Store 状态
    // 如果 userStore 中有同步用户信息的 action 可以在这里调用
  } catch (e) {
    // request.ts 已处理错误
  } finally {
    loading.value = false
  }
}

const handleUpdatePwd = async () => {
  if (!pwdFormRef.value) return

  await pwdFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        await updatePassword({
          oldPassword: pwdForm.oldPassword,
          newPassword: pwdForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        userStore.logout()
      } catch(e) {
        // err
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  loadInfo()
})
</script>

<style scoped>
.form-wrapper {
  max-width: 500px;
  padding: 20px;
}
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 80px;
  height: 80px;
}
.avatar-uploader:hover { border-color: #409EFF; }
.avatar-uploader-icon {
  font-size: 24px;
  color: #8c939d;
  width: 80px;
  height: 80px;
  text-align: center;
  line-height: 80px;
}
.avatar { width: 80px; height: 80px; display: block; object-fit: cover; }
</style>