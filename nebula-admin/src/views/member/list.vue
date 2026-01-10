<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-form inline :model="queryParams" class="filter-form">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch"/>
        </el-form-item>
      </el-form>
      <div class="filter-actions">
        <el-button class="icon-action" circle :icon="Search" @click="handleSearch" />
        <el-button class="icon-action" circle :icon="Refresh" @click="resetQuery" />
      </div>
    </div>

    <!-- 列表 -->
    <el-card shadow="never" class="table-card" style="margin-top: 20px;">
      <el-table :data="tableData" v-loading="loading" :row-class-name="getRowClassName">
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          </template>
        </el-table-column>

        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />

        <el-table-column label="注册时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <span class="status-pill" :class="row.status === 1 ? 'is-active' : 'is-muted'">
              <span class="dot"></span>
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
                class="icon-action"
                text
                circle
                :icon="row.status === 1 ? Lock : Unlock"
                @click="handleStatus(row)"
            />
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right;">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMemberList, updateMemberStatus } from '@/api/member'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, Refresh, Search, Unlock } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  username: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getMemberList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.page = 1; loadData() }
const resetQuery = () => { queryParams.username = ''; handleSearch() }

const handleStatus = (row: any) => {
  const actionText = row.status === 1 ? '禁用' : '启用'
  const newStatus = row.status === 1 ? 0 : 1

  ElMessageBox.confirm(`确认${actionText}用户 [${row.username}] 吗?`, '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await updateMemberStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadData()
  })
}

const formatTime = (time: string) => time ? time.replace('T', ' ') : ''

const getRowClassName = () => 'floating-row'

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.12);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
  align-items: center;
  flex: 1;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-action {
  opacity: 0.6;
  transition: all 0.2s ease;
}

.icon-action:hover {
  opacity: 1;
  color: #409eff;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  color: #64748b;
  background: rgba(255, 255, 255, 0.6);
}

.status-pill .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #94a3b8;
  box-shadow: 0 0 6px rgba(148, 163, 184, 0.8);
}

.status-pill.is-active .dot {
  background: #22c55e;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.8);
}

.status-pill.is-muted .dot {
  background: #f97316;
  box-shadow: 0 0 8px rgba(249, 115, 22, 0.6);
}

:deep(.el-table) {
  --el-table-border-color: transparent;
  background: transparent;
}

:deep(.el-table__header th) {
  background: transparent;
  color: #475569;
  font-weight: 600;
  font-size: 12px;
}

:deep(.el-table__body) {
  border-collapse: separate;
  border-spacing: 0 12px;
}

:deep(.floating-row td) {
  background: rgba(255, 255, 255, 0.75);
  border-top: 1px solid rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

:deep(.floating-row:hover td) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
}

:deep(.floating-row td:first-child) {
  border-radius: 12px 0 0 12px;
}

:deep(.floating-row td:last-child) {
  border-radius: 0 12px 12px 0;
}
</style>
