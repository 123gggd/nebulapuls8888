<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>管理员邀请码</span>
          <el-button type="primary" @click="fetchCode" :loading="loading">刷新</el-button>
        </div>
      </template>

      <div class="invite-body">
        <div class="invite-label">当前邀请码</div>
        <div class="invite-code">
          <el-input v-model="inviteCode" readonly />
          <el-button type="success" plain @click="handleCopy" :disabled="!inviteCode">复制</el-button>
        </div>
        <div class="invite-tip">该邀请码用于管理员注册，请妥善保管。</div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminInviteCode } from '@/api/system'

const inviteCode = ref('')
const loading = ref(false)

const fetchCode = async () => {
  loading.value = true
  try {
    const res: any = await getAdminInviteCode()
    inviteCode.value = res.data || ''
  } finally {
    loading.value = false
  }
}

const handleCopy = async () => {
  if (!inviteCode.value) return
  try {
    await navigator.clipboard.writeText(inviteCode.value)
    ElMessage.success('已复制到剪贴板')
  } catch (e) {
    ElMessage.error('复制失败，请手动复制')
  }
}

onMounted(fetchCode)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.invite-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.invite-label {
  font-weight: 600;
}
.invite-code {
  display: flex;
  gap: 12px;
  align-items: center;
}
.invite-tip {
  color: #909399;
  font-size: 12px;
}
</style>
