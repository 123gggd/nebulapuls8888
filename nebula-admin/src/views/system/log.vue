<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-form inline :model="queryParams" class="filter-form">
        <el-form-item label="操作人">
          <el-input
              v-model="queryParams.username"
              placeholder="请输入用户名"
              clearable
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
              v-model="dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <div class="filter-actions">
        <el-button class="icon-action" circle :icon="Search" @click="handleSearch" />
        <el-button class="icon-action" circle :icon="Refresh" @click="resetQuery" />
      </div>
    </div>

    <!-- 日志列表 -->
    <el-card shadow="never" class="table-card" style="margin-top: 20px;">
      <el-table :data="tableData" v-loading="loading" :row-class-name="getRowClassName">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="operation" label="操作描述" width="150" />
        <el-table-column prop="method" label="请求路径" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" align="center" />
        <el-table-column prop="createTime" label="操作时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="详情" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button class="icon-action" text circle :icon="View" @click="handleDetail(row)" />
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 详情弹窗 -->
    <el-dialog title="日志详情" v-model="dialogVisible" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="请求参数">
          <div class="code-block">{{ currentLog?.params || '无参数' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLogList } from '@/api/system'
import { Refresh, Search, View } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref([])
const dialogVisible = ref(false)
const currentLog = ref<any>(null)

const queryParams = reactive({
  page: 1,
  size: 10,
  username: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { ...queryParams }
    // 处理时间范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res: any = await getLogList(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

const resetQuery = () => {
  queryParams.username = ''
  dateRange.value = []
  handleSearch()
}

const handleDetail = (row: any) => {
  currentLog.value = row
  dialogVisible.value = true
}

const formatTime = (time: string) => {
  return time ? time.replace('T', ' ') : ''
}

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

.code-block {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace; /* 等宽字体 */
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
  font-size: 12px;
  color: #666;
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
