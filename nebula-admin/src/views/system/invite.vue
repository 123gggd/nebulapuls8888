<template>
  <div class="page-container invite-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">管理员邀请码</div>
            <div class="subtitle">资产票据化 · Digital Token</div>
          </div>
          <el-button type="primary" @click="fetchCode" :loading="loading">刷新令牌</el-button>
        </div>
      </template>

      <div class="invite-body">
        <div class="token-row">
          <div
            class="token-card"
            :class="{ empty: !inviteCode, copied: copied }"
            role="button"
            @click="handleCopy"
          >
            <div class="token-status">{{ inviteCode ? '未使用' : '已失效' }}</div>
            <div class="token-code">{{ inviteCode || '暂无可用邀请码' }}</div>
            <div class="token-action">点击复制邀请码</div>
          </div>
          <el-button type="success" plain @click="handleCopy" :disabled="!inviteCode">复制</el-button>
        </div>
        <div class="invite-tip">该邀请码用于管理员注册，请妥善保管并及时分发。</div>
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
const copied = ref(false)

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
    copied.value = true
    ElMessage.success('已复制到剪贴板')
    setTimeout(() => {
      copied.value = false
    }, 1200)
  } catch (e) {
    ElMessage.error('复制失败，请手动复制')
  }
}

onMounted(fetchCode)
</script>

<style scoped>
.invite-page {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.subtitle {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
.invite-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.token-row {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}
.token-card {
  flex: 1;
  min-width: 280px;
  padding: 22px;
  border-radius: 18px;
  background: radial-gradient(circle at top left, rgba(59, 130, 246, 0.4), rgba(15, 23, 42, 0.9));
  color: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.4);
  box-shadow: 0 18px 36px rgba(30, 64, 175, 0.35);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.token-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 40px rgba(30, 64, 175, 0.45);
}
.token-card.empty {
  background: linear-gradient(135deg, rgba(148, 163, 184, 0.4), rgba(30, 41, 59, 0.8));
  color: rgba(226, 232, 240, 0.85);
}
.token-card.copied {
  box-shadow: 0 0 24px rgba(34, 197, 94, 0.6);
}
.token-status {
  font-size: 12px;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: rgba(226, 232, 240, 0.85);
  margin-bottom: 10px;
}
.token-code {
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 2px;
  margin-bottom: 12px;
}
.token-action {
  font-size: 12px;
  color: rgba(226, 232, 240, 0.8);
}
.invite-tip {
  color: #909399;
  font-size: 12px;
}
</style>
