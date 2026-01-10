<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 左侧：个人信息卡片 -->
      <el-col :span="8" :xs="24">
        <el-card class="box-card" shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>个人信息</span>
            </div>
          </template>

          <div class="text-center profile-header" :style="{ '--hero-image': user.avatar ? `url(${user.avatar})` : 'none' }">
            <!-- 头像上传组件 -->
            <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                :before-upload="beforeAvatarUpload"
            >
              <div class="avatar-wrapper">
                <img v-if="user.avatar" :src="user.avatar" class="user-avatar hover-zoom" />
                <el-avatar v-else :size="100" class="user-avatar-placeholder">
                  {{ user.nickname?.charAt(0)?.toUpperCase() || user.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <!-- 悬停遮罩 -->
                <div class="avatar-mask">
                  <el-icon><Camera /></el-icon>
                </div>
              </div>
            </el-upload>

            <h3 class="user-name">{{ user.nickname || user.username }}</h3>
            <p class="user-role">{{ getRoleName(user.role) }}</p>
            <div class="hero-stats">
              <div class="stat">
                <div class="stat-value">{{ registerDays }}</div>
                <div class="stat-label">注册天数</div>
              </div>
              <div class="divider"></div>
              <div class="stat">
                <div class="stat-value">{{ user.merchantId ? '商户' : '个人' }}</div>
                <div class="stat-label">账号类型</div>
              </div>
            </div>
          </div>

          <el-divider />

          <div class="user-info">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span class="label">登录账号：</span>
              <span>{{ user.username }}</span>
            </div>
            <div class="info-item" v-if="user.merchantId">
              <el-icon><Shop /></el-icon>
              <span class="label">商户编号：</span>
              <span>{{ user.merchantId }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span class="label">注册时间：</span>
              <span>{{ user.createTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：资料修改与安全 -->
      <el-col :span="16" :xs="24">
        <el-card shadow="hover">
          <el-tabs v-model="activeTab">

            <!-- 基本资料 Tab -->
            <el-tab-pane label="基本资料" name="info">
              <el-form label-width="100px" style="margin-top: 20px; max-width: 500px;">
                <el-form-item label="用户昵称">
                  <el-input v-model="form.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="用户ID">
                  <el-input v-model="user.id" disabled />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updateProfile" :loading="loading">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 Tab -->
            <el-tab-pane label="修改密码" name="password">
              <el-form
                  ref="pwdFormRef"
                  :model="pwdForm"
                  :rules="pwdRules"
                  label-width="100px"
                  style="margin-top: 20px; max-width: 500px;"
              >
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码 (至少6位)" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="danger" @click="changePassword" :loading="loading">确认修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
// [关键修复] 修正 Store 路径
import { useUserStore } from '@/stores/modules/user'
import { ElMessage } from 'element-plus'
import { Camera, User, Shop, Clock } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/product' // 复用文件上传
import { getUserInfo, updatePassword } from '@/api/user'
import request from '@/utils/request'

const userStore = useUserStore()
const activeTab = ref('info')
const loading = ref(false)
const user = ref<any>({})

const form = reactive({
  nickname: '',
  avatar: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdFormRef = ref()

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

const loadInfo = async () => {
  try {
    const res: any = await getUserInfo()
    if (res.data) {
      user.value = res.data
      form.nickname = res.data.nickname
      form.avatar = res.data.avatar
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

const getRoleName = (role: string) => {
  if (role === 'ADMIN') return '超级管理员'
  if (role === 'MERCHANT') return '入驻商家'
  return '普通用户'
}

const registerDays = computed(() => {
  if (!user.value?.createTime) return '--'
  const created = new Date(user.value.createTime).getTime()
  if (Number.isNaN(created)) return '--'
  const diff = Date.now() - created
  return Math.max(1, Math.floor(diff / (1000 * 60 * 60 * 24)))
})

const beforeAvatarUpload = (file: File) => {
  const isImg = ['image/jpeg', 'image/png'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImg) ElMessage.error('头像只能是 JPG 或 PNG 格式!')
  if (!isLt2M) ElMessage.error('头像大小不能超过 2MB!')
  return isImg && isLt2M
}

const handleAvatarUpload = async (options: any) => {
  try {
    const res: any = await uploadFile(options.file)
    form.avatar = res.data
    user.value.avatar = res.data
    await updateProfile(true)
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

const updateProfile = async (silent = false) => {
  loading.value = true
  try {
    await request.post('/member/update', form)
    if (!silent) ElMessage.success('资料更新成功')
    userStore.name = form.nickname
    userStore.avatar = form.avatar
  } catch (e) {
    // error
  } finally {
    loading.value = false
  }
}

const changePassword = async () => {
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
        await userStore.logout()
        location.reload()
      } catch (e) {
        // error
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
.page-container {
  padding: 20px;
}
.profile-header {
  padding: 20px 0;
  text-align: center;
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.75);
}
.profile-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: var(--hero-image);
  background-size: cover;
  background-position: center;
  filter: blur(18px);
  opacity: 0.25;
  transform: scale(1.1);
}
.profile-header > * {
  position: relative;
  z-index: 1;
}
.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #ebeef5;
  cursor: pointer;
}
.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.user-avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #409EFF;
  color: #fff;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-mask {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}
.user-name { margin: 15px 0 5px; font-size: 22px; font-weight: 600; color: #303133; }
.user-role { font-size: 14px; color: #909399; }
.hero-stats {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}
.hero-stats .stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.hero-stats .stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}
.hero-stats .stat-label {
  font-size: 12px;
  color: #94a3b8;
}
.hero-stats .divider {
  width: 1px;
  height: 30px;
  background: rgba(148, 163, 184, 0.4);
}
.user-info { padding: 10px 0; }
.info-item {
  display: flex; align-items: center; margin-bottom: 15px;
  font-size: 14px; color: #606266; border-bottom: 1px dashed #f0f2f5; padding-bottom: 10px;
}
.info-item .el-icon { margin-right: 10px; font-size: 16px; color: #409EFF; }
.info-item .label { font-weight: 500; margin-right: 5px; min-width: 70px; }
</style>
