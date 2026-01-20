<template>
  <div class="profile-page">
    <el-card shadow="never" style="border-radius: 12px;">
      <template #header>
        <div style="font-weight: bold; font-size: 18px;">Profile</div>
      </template>

      <el-tabs tab-position="left" style="height: 400px;">
        <el-tab-pane label="Basic Info">
          <div class="form-wrapper">
            <el-form :model="userInfo" label-width="80px">
              <el-form-item label="Account">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>
              <el-form-item label="Display name">
                <el-input v-model="userInfo.nickname" placeholder="Set your display name" />
              </el-form-item>
              <el-form-item label="Phone">
                <el-input v-model="userInfo.phone" placeholder="Link your phone number" />
              </el-form-item>
              <el-form-item label="Email">
                <el-input v-model="userInfo.email" placeholder="Link your email address" />
              </el-form-item>

              <!-- Avatar upload -->
              <el-form-item label="Avatar">
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
                  Click the image to change your avatar (JPG/PNG, &lt;2MB)
                </div>
              </el-form-item>

              <el-form-item label="Registered">
                <span>{{ userInfo.createTime }}</span>
              </el-form-item>
              <el-form-item>
                <!-- [Fix] Method name matches the script -->
                <el-button type="primary" @click="handleUpdateProfile" :loading="loading">Save changes</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="Security">
          <div class="form-wrapper">
            <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px">
              <el-form-item label="Current password" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="New password" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="Confirm new password" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="danger" @click="handleUpdatePwd" :loading="loading">Update password</el-button>
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
  oldPassword: [{ required: true, message: 'Please enter your current password', trigger: 'blur' }],
  newPassword: [
    { required: true, message: 'Please enter a new password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm your new password', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('Passwords do not match'))
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

// Avatar format validation
const beforeAvatarUpload = (file: any) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('Avatar must be JPG or PNG!')
  }
  if (!isLt2M) {
    ElMessage.error('Avatar size must be under 2MB!')
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
      userInfo.value.avatar = result.data // Assume backend returns the image URL
      ElMessage.success('Avatar uploaded successfully')
      // Update the user info immediately to avoid refresh loss
      await updateUserInfo({ avatar: result.data })
      userStore.username = userInfo.value.nickname // Trigger store update
    } else {
      ElMessage.error(result.msg || 'Upload failed')
    }
  } catch (error: any) {
    console.error(error)
    ElMessage.error('Upload failed. Please check your network.')
  }
}

// [Fix] Rename to handleUpdateProfile to match the template
const handleUpdateProfile = async () => {
  loading.value = true
  try {
    await updateUserInfo(userInfo.value)
    ElMessage.success('Profile updated successfully')
    // Update store state
    // If userStore has an action to sync info, call it here
  } catch (e) {
    // request.ts handles errors
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
        ElMessage.success('Password updated. Please sign in again.')
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
