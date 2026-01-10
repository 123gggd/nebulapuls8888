<template>
  <div class="app-container review-page">
    <el-card shadow="never" class="review-card-shell">
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">评价管理</div>
            <div class="subtitle">情感可视化 · Glass Bubbles</div>
          </div>
        </div>
      </template>

      <el-form inline :model="queryParams" class="search-form">
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

      <div class="review-stream" v-loading="loading">
        <el-empty v-if="!tableData.length" description="暂无评价" />
        <div v-else class="review-list">
          <div v-for="row in tableData" :key="row.id" class="review-item">
            <div class="review-top">
              <div class="product-info">
                <el-image v-if="row.productImage" :src="row.productImage" class="product-image" fit="cover" />
                <div class="product-text">
                  <div class="product-name">{{ row.productName || '-' }}</div>
                  <div class="product-specs">{{ row.specs || '默认规格' }}</div>
                </div>
              </div>
              <div class="review-meta">
                <el-tag v-if="row.status === 1" type="success" effect="light">显示</el-tag>
                <el-tag v-else-if="row.status === 2" type="warning" effect="light">精选</el-tag>
                <el-tag v-else type="info" effect="light">隐藏</el-tag>
                <span class="review-time">{{ row.createTime || '-' }}</span>
              </div>
            </div>

            <div class="review-body">
              <div class="user-avatar">
                <el-avatar :src="row.avatar" :size="48">{{ getInitial(row.userName) }}</el-avatar>
              </div>
              <div class="review-bubble">
                <div class="bubble-header">
                  <span class="user-name">{{ row.userName || '匿名用户' }}</span>
                  <el-rate v-model="row.rating" disabled show-score text-color="#ffb400" class="glow-rate" />
                </div>
                <div class="bubble-content">
                  <span
                    v-for="(segment, index) in highlightContent(row.content || '-')"
                    :key="index"
                    :class="{ highlight: segment.highlight }"
                  >
                    {{ segment.text }}
                  </span>
                </div>
              </div>
            </div>

            <div class="reply-area">
              <div class="reply-bubble" :class="{ empty: !row.replyContent }">
                <span class="reply-label">商家回复</span>
                <div class="reply-content">
                  {{ row.replyContent || '暂未回复，点击“回复”开始对话' }}
                </div>
              </div>
              <div class="review-actions">
                <el-button type="primary" link @click="openReply(row)">回复</el-button>
                <el-dropdown @command="(command: number) => handleStatus(row, command)">
                  <el-button link type="warning">审核</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="1">显示</el-dropdown-item>
                      <el-dropdown-item :command="0">隐藏</el-dropdown-item>
                      <el-dropdown-item :command="2">精选</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination-wrap">
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
      <div class="chat-thread">
        <div class="chat-bubble user">
          <div class="chat-label">用户评价</div>
          <div class="chat-text">{{ currentRow?.content || '-' }}</div>
        </div>
        <div class="chat-bubble merchant" v-if="currentRow?.replyContent">
          <div class="chat-label">商家回复</div>
          <div class="chat-text">{{ currentRow?.replyContent }}</div>
        </div>
      </div>
      <el-form :model="replyForm" label-width="0" class="chat-input">
        <el-form-item required>
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="3"
            placeholder="输入回复内容..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply">发送回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewList, replyReview, updateReviewStatus } from '@/api/review'

type HighlightSegment = {
  text: string
  highlight: boolean
}

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const replyVisible = ref(false)
const replyLoading = ref(false)
const currentRow = ref<any>(null)

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

const highlightKeywords = ['满意', '喜欢', '推荐', '不错', '糟糕', '失望', '差', '一般']

const highlightContent = (content: string): HighlightSegment[] => {
  if (!content) {
    return [{ text: '-', highlight: false }]
  }
  const pattern = new RegExp(`(${highlightKeywords.join('|')})`, 'g')
  return content.split(pattern).filter(Boolean).map((text) => ({
    text,
    highlight: highlightKeywords.includes(text)
  }))
}

const getInitial = (name?: string) => {
  if (!name) return 'U'
  return name.trim().slice(0, 1).toUpperCase()
}

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
.review-page {
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
.search-form {
  margin-bottom: 20px;
}
.review-stream {
  min-height: 320px;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.review-item {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(148, 163, 184, 0.2);
  backdrop-filter: blur(12px);
}
.review-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.product-info {
  display: flex;
  gap: 12px;
  align-items: center;
}
.product-image {
  width: 48px;
  height: 48px;
  border-radius: 10px;
}
.product-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.product-name {
  font-weight: 600;
}
.product-specs {
  font-size: 12px;
  color: #94a3b8;
}
.review-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #64748b;
  font-size: 12px;
}
.review-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.user-avatar {
  flex-shrink: 0;
  margin-top: 4px;
}
.review-bubble {
  flex: 1;
  padding: 16px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.6));
  box-shadow: inset 0 0 20px rgba(148, 163, 184, 0.2);
}
.bubble-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.user-name {
  font-weight: 600;
  color: #0f172a;
}
.glow-rate :deep(.el-rate__icon) {
  text-shadow: 0 0 8px rgba(255, 180, 0, 0.8);
}
.bubble-content {
  font-size: 14px;
  line-height: 1.7;
  color: #1e293b;
}
.bubble-content .highlight {
  color: #f97316;
  font-weight: 600;
  text-shadow: 0 0 8px rgba(249, 115, 22, 0.35);
}
.reply-area {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-top: 16px;
  flex-wrap: wrap;
}
.reply-bubble {
  flex: 1;
  padding: 12px 16px;
  border-radius: 14px;
  background: rgba(15, 23, 42, 0.04);
  border: 1px dashed rgba(100, 116, 139, 0.35);
}
.reply-bubble.empty {
  color: #64748b;
}
.reply-label {
  font-size: 12px;
  color: #94a3b8;
  display: block;
  margin-bottom: 4px;
}
.review-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.chat-thread {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}
.chat-bubble {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(148, 163, 184, 0.15);
}
.chat-bubble.user {
  border-left: 4px solid #38bdf8;
}
.chat-bubble.merchant {
  border-left: 4px solid #10b981;
}
.chat-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}
.chat-text {
  color: #1f2937;
}
.chat-input :deep(.el-textarea__inner) {
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.8);
}
</style>
