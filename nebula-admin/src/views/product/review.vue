<template>
  <div class="app-container" style="padding: 20px;">
    <el-card shadow="never">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>评价管理</span>
        </div>
      </template>

      <el-form inline :model="queryParams" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="商品名称">
          <el-input v-model="queryParams.productName" placeholder="请输入商品名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px;">
            <el-option label="显示" :value="1" />
            <el-option label="隐藏" :value="0" />
            <el-option label="精选" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="商品信息" min-width="220">
          <template #default="{ row }">
            <div class="product-info">
              <el-image v-if="row.productImage" :src="row.productImage" class="product-image" fit="cover" />
              <div>
                <div class="product-name">{{ row.productName || '-' }}</div>
                <div class="product-specs">{{ row.specs || '默认规格' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="140" />
        <el-table-column prop="rating" label="评分" width="120" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="220" />
        <el-table-column prop="replyContent" label="商家回复" min-width="200">
          <template #default="{ row }">
            <span v-if="row.replyContent">{{ row.replyContent }}</span>
            <span v-else class="muted">未回复</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">显示</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning">精选</el-tag>
            <el-tag v-else type="info">隐藏</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openReply(row)">回复</el-button>
            <el-dropdown v-if="isAdmin" @command="(command: number) => handleStatus(row, command)">
              <el-button link type="warning">审核</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="1">显示</el-dropdown-item>
                  <el-dropdown-item :command="0">隐藏</el-dropdown-item>
                  <el-dropdown-item :command="2">精选</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="replyVisible" title="回复评价" width="520px" destroy-on-close>
      <el-form :model="replyForm" label-width="90px">
        <el-form-item label="商品">
          <span>{{ currentRow?.productName || '-' }}</span>
        </el-form-item>
        <el-form-item label="评价内容">
          <div class="reply-content">{{ currentRow?.content || '-' }}</div>
        </el-form-item>
        <el-form-item label="回复内容" required>
          <el-input v-model="replyForm.content" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewList, replyReview, updateReviewStatus } from '@/api/review'
import { useUserStore } from '@/stores/modules/user'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const replyVisible = ref(false)
const replyLoading = ref(false)
const currentRow = ref<any>(null)
const userStore = useUserStore()
const isAdmin = computed(() => userStore.roles.includes('ADMIN'))

const queryParams = reactive({
  page: 1,
  size: 10,
  productName: '',
  status: undefined as number | undefined
})

const replyForm = reactive({
  id: 0,
  content: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getReviewList(queryParams)
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

const handleReset = () => {
  queryParams.productName = ''
  queryParams.status = undefined
  queryParams.page = 1
  loadData()
}

const openReply = (row: any) => {
  currentRow.value = row
  replyForm.id = row.id
  replyForm.content = row.replyContent || ''
  replyVisible.value = true
}

const submitReply = async () => {
  if (!replyForm.content) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replyLoading.value = true
  try {
    await replyReview({ id: replyForm.id, content: replyForm.content })
    ElMessage.success('回复成功')
    replyVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    replyLoading.value = false
  }
}

const handleStatus = async (row: any, status: number) => {
  const label = status === 1 ? '显示' : status === 2 ? '精选' : '隐藏'
  ElMessageBox.confirm(`确认将该评价设置为【${label}】吗？`, '提示', { type: 'warning' }).then(async () => {
    await updateReviewStatus(row.id, status)
    ElMessage.success('状态更新成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.product-info {
  display: flex;
  gap: 12px;
  align-items: center;
}
.product-image {
  width: 40px;
  height: 40px;
  border-radius: 6px;
}
.product-name {
  font-weight: 600;
}
.product-specs {
  font-size: 12px;
  color: #999;
}
.reply-content {
  background: #f5f7fa;
  padding: 8px 10px;
  border-radius: 6px;
  color: #606266;
}
.muted {
  color: #aaa;
}
</style>
