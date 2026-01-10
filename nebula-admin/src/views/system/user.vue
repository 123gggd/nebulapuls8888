<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <template #header>
        <div class="card-header">
          <span>船员名册 · 管理员权限</span>
        </div>
      </template>
      <el-form inline :model="queryParams" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="roster-wrapper" v-loading="loading">
      <div v-if="tableData.length" class="roster-list">
        <div v-for="row in tableData" :key="row.id" class="roster-card">
          <div class="roster-id">
            <div class="id-tag">ID CARD</div>
            <el-avatar :src="row.avatar" :size="64" class="roster-avatar">
              {{ row.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="id-number">#{{ row.id }}</div>
          </div>

          <div class="roster-info">
            <div class="info-header">
              <div class="name-block">
                <h3>{{ row.username }}</h3>
                <span class="nickname">{{ row.nickname || '未设置昵称' }}</span>
              </div>
              <span class="role-badge" :class="resolveRoleMeta(row).className">
                {{ resolveRoleMeta(row).label }}
              </span>
            </div>
            <div class="info-meta">
              <span>商户ID：{{ row.merchantId || '无' }}</span>
              <span>注册时间：{{ row.createTime || '-' }}</span>
              <span>
                状态：
                <strong :class="row.status === 1 ? 'status-ok' : 'status-ban'">
                  {{ row.status === 1 ? '可操作' : '已冻结' }}
                </strong>
              </span>
            </div>
          </div>

          <div class="roster-actions">
            <div class="switch-row">
              <span class="switch-label">{{ row.status === 1 ? '已解锁' : '已锁定' }}</span>
              <el-switch
                v-model="row.status"
                class="security-switch"
                :active-value="1"
                :inactive-value="0"
                @change="(value: number) => handleToggleStatus(row, value)"
              />
            </div>
            <el-button class="detail-button" type="primary" plain @click="handleDetail(row)">详情</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无管理员信息" />
    </div>

    <div class="pagination">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        @current-change="loadData"
      />
    </div>

    <el-dialog v-model="detailVisible" title="用户详情" width="520px" class="glass-dialog">
      <div v-if="currentMember" class="member-detail">
        <div class="header">
          <el-avatar :src="currentMember.avatar" :size="80" />
          <h3>{{ currentMember.username }}</h3>
          <el-tag size="small">{{ currentMember.role || currentMember.roleCode }}</el-tag>
        </div>
        <el-descriptions border :column="1" style="margin-top: 20px">
          <el-descriptions-item label="用户ID">{{ currentMember.id }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentMember.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentMember.createTime }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <span :style="{ color: currentMember.status === 1 ? '#67c23a' : '#f56c6c' }">
              {{ currentMember.status === 1 ? '正常' : '已冻结' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentMember.merchantId" label="关联店铺ID">
            {{ currentMember.merchantId }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMemberList, updateMemberStatus, getMemberDetail } from '@/api/member'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const detailVisible = ref(false)
const currentMember = ref<any>(null)

const queryParams = reactive({
  page: 1,
  size: 10,
  username: ''
})

const roleMetaMap = {
  ADMIN: { label: '超级管理员', className: 'badge-admin' },
  EDITOR: { label: '运营人员', className: 'badge-editor' },
  STAFF: { label: '值班人员', className: 'badge-staff' },
  CUSTOMER_SERVICE: { label: '客服', className: 'badge-service' },
  MERCHANT: { label: '商家', className: 'badge-merchant' }
}

const resolveRoleMeta = (row: any) => {
  const code = row.roleCode || row.role || 'STAFF'
  return roleMetaMap[code] || { label: '系统成员', className: 'badge-default' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getMemberList(queryParams)
    // 兼容 Page 结构或直接 List 结构
    const data = res.data || res
    tableData.value = data.records || (Array.isArray(data) ? data : [])
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

const handleToggleStatus = (row: any, value: number) => {
  const action = value === 1 ? '解冻' : '冻结'
  const previousStatus = value === 1 ? 0 : 1

  ElMessageBox.confirm(`确认要${action}用户 "${row.username}" 吗?`, '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await updateMemberStatus(row.id, value)
      ElMessage.success('操作成功')
      loadData()
    } catch (e) {
      row.status = previousStatus
      ElMessage.error('操作失败')
    }
  }).catch(() => {
    row.status = previousStatus
  })
}

const handleDetail = async (row: any) => {
  try {
    // 尝试获取更详细的信息，如果 API 没就绪，降级显示行数据
    try {
      const res: any = await getMemberDetail(row.id)
      currentMember.value = res.data || res
    } catch (err) {
      currentMember.value = row
    }
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: 600;
  color: #0f172a;
}

.search-form {
  margin-top: 10px;
}

.roster-wrapper {
  min-height: 260px;
}

.roster-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.roster-card {
  display: grid;
  grid-template-columns: 160px 1fr 220px;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.55);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.16);
  backdrop-filter: blur(12px);
  align-items: center;
}

.roster-id {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  border-radius: 14px;
  background: linear-gradient(140deg, rgba(15, 23, 42, 0.8), rgba(37, 99, 235, 0.45));
  color: #fff;
  box-shadow: inset 0 0 10px rgba(255, 255, 255, 0.2);
}

.id-tag {
  font-size: 11px;
  letter-spacing: 2px;
  text-transform: uppercase;
  opacity: 0.8;
}

.roster-avatar {
  margin: 12px 0;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 16px rgba(255, 255, 255, 0.35);
}

.id-number {
  font-size: 13px;
  opacity: 0.9;
}

.roster-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.name-block h3 {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
}

.nickname {
  font-size: 13px;
  color: #64748b;
}

.role-badge {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.4);
}

.badge-admin {
  background: linear-gradient(120deg, #f59e0b, #f97316);
  box-shadow: 0 0 12px rgba(245, 158, 11, 0.6);
}

.badge-editor {
  background: linear-gradient(120deg, #38bdf8, #3b82f6);
  box-shadow: 0 0 12px rgba(56, 189, 248, 0.6);
}

.badge-staff {
  background: linear-gradient(120deg, #8b5cf6, #6366f1);
  box-shadow: 0 0 12px rgba(139, 92, 246, 0.5);
}

.badge-service {
  background: linear-gradient(120deg, #14b8a6, #22c55e);
  box-shadow: 0 0 12px rgba(20, 184, 166, 0.5);
}

.badge-merchant {
  background: linear-gradient(120deg, #f472b6, #ec4899);
  box-shadow: 0 0 12px rgba(236, 72, 153, 0.5);
}

.badge-default {
  background: linear-gradient(120deg, #64748b, #94a3b8);
}

.info-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 13px;
  color: #475569;
}

.status-ok {
  color: #22c55e;
}

.status-ban {
  color: #f97316;
}

.roster-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #475569;
}

.security-switch {
  --el-switch-on-color: #22c55e;
  --el-switch-off-color: #f97316;
}

.detail-button {
  width: 100%;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.member-detail .header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.member-detail h3 {
  margin: 10px 0 5px;
}

:deep(.glass-dialog .el-dialog) {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(14px);
}

:deep(.glass-dialog .el-dialog__header) {
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

@media (max-width: 1200px) {
  .roster-card {
    grid-template-columns: 140px 1fr;
  }

  .roster-actions {
    grid-column: span 2;
    flex-direction: row;
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .roster-card {
    grid-template-columns: 1fr;
  }

  .roster-id {
    flex-direction: row;
    justify-content: space-between;
  }

  .roster-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
