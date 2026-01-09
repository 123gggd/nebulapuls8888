<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form inline :model="queryParams">
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
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never" class="table-card" style="margin-top: 20px;">
      <el-table :data="tableData" border stripe v-loading="loading">
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
            <el-button link type="primary" @click="handleDetail(row)">查看参数</el-button>
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

onMounted(loadData)
</script>

<style scoped>
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
</style>