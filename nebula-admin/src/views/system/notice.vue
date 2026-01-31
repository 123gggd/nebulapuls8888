<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form inline :model="queryParams">
        <el-form-item label="公告标题">
          <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">发布公告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="notice-board" v-loading="loading">
      <transition-group name="notice-fade" tag="div" class="notice-grid">
        <div
          v-for="item in tableData"
          :key="item.id"
          class="notice-card"
          :class="{ 'is-urgent': isUrgent(item), 'is-pinned': isPinned(item) }"
          @click="handleEdit(item)"
        >
          <div class="notice-card__header">
            <div class="notice-title">
              <span v-if="isPinned(item)" class="notice-pin" aria-hidden="true" />
              <h3>{{ item.title }}</h3>
            </div>
            <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
              {{ item.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </div>
          <p class="notice-content">{{ item.content }}</p>
          <div class="notice-card__meta">
            <span>排序 {{ item.sort }}</span>
            <span>{{ item.createTime }}</span>
          </div>
          <div class="notice-card__actions" @click.stop>
            <el-button link type="primary" @click="handleEdit(item)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </transition-group>

      <el-empty v-if="!tableData.length && !loading" description="暂无公告" />
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <!-- 编辑/发布弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="640px" class="glass-dialog">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告正文" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">立即发布</el-radio>
                <el-radio :value="0">存为草稿</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getNoticeList, saveNotice, deleteNotice } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

const ENABLE_MOCK = import.meta.env.VITE_ENABLE_MOCK === 'true'

// --- Mock 数据 (后端挂了也能看) ---
const MOCK_DATA = [
  { id: 1, title: '系统维护通知', content: '系统将于今晚24:00进行升级维护，预计耗时2小时，请提前做好准备。', sort: 1, status: 1, createTime: '2026-01-01 10:00:00' },
  { id: 2, title: '春节放假安排', content: '春节期间照常发货，物流时效可能略有延迟，请各位商家知悉。', sort: 2, status: 1, createTime: '2026-01-05 12:00:00' },
  { id: 3, title: '新功能上线：秒杀活动', content: '商家后台现已支持创建秒杀活动，快来配置吧！', sort: 3, status: 0, createTime: '2026-01-07 09:30:00' }
]

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)

const queryParams = reactive({
  page: 1,
  size: 10,
  title: ''
})

const form = reactive({
  id: undefined,
  title: '',
  content: '',
  sort: 0,
  status: 1
})

const isPinned = (item: any) => item.sort === 0 || item.sort === 1
const isUrgent = (item: any) => /紧急|维护|故障|升级|停机|中断/.test(item.title || item.content)

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getNoticeList(queryParams)
    // 鲁棒性处理：兼容 Result 包装或 Page 结构，如果数据无效则抛出错误进入 catch
    const data = res.data || res

    if (data && Array.isArray(data.records)) {
      tableData.value = data.records
      total.value = data.total
    } else {
      // 如果后端返回的不是预期格式（例如返回了错误对象），手动抛出异常以触发 Mock
      throw new Error('API 数据格式异常')
    }
  } catch (e) {
    if (ENABLE_MOCK) {
      console.warn('API 请求失败或权限不足，自动切换为 Mock 数据演示', e)
      tableData.value = MOCK_DATA
      total.value = MOCK_DATA.length
    } else {
      tableData.value = []
      total.value = 0
      ElMessage.error('公告数据加载失败')
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    title: '',
    content: '',
    sort: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该公告吗?', '警告', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteNotice(row.id)
      ElMessage.success('删除成功')
    } catch (e) {
      if (ENABLE_MOCK) {
        ElMessage.success('删除成功 (演示)')
      } else {
        ElMessage.error('删除失败')
      }
    }
    loadData()
  })
}

const submitForm = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  try {
    await saveNotice(form)
    ElMessage.success(form.id ? '更新成功' : '发布成功')
  } catch (e) {
    if (ENABLE_MOCK) {
      ElMessage.success(form.id ? '更新成功 (演示)' : '发布成功 (演示)')
    } else {
      ElMessage.error(form.id ? '更新失败' : '发布失败')
      return
    }
  }

  dialogVisible.value = false
  loadData()
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

.notice-board {
  min-height: 280px;
  padding: 10px;
}

.notice-grid {
  column-count: 3;
  column-gap: 20px;
}

.notice-card {
  break-inside: avoid;
  padding: 18px 20px;
  margin: 0 0 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(255, 255, 255, 0.55);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.16);
  backdrop-filter: blur(12px);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  position: relative;
}

.notice-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 28px 60px rgba(15, 23, 42, 0.25);
}

.notice-card.is-urgent {
  box-shadow: 0 0 0 1px rgba(248, 113, 113, 0.4), 0 22px 50px rgba(248, 113, 113, 0.35);
}

.notice-card.is-urgent::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 20px;
  background: radial-gradient(circle at top right, rgba(248, 113, 113, 0.35), transparent 55%);
  opacity: 0.9;
  animation: urgent-pulse 2.2s ease-in-out infinite;
  z-index: 0;
}

.notice-card.is-pinned .notice-pin {
  display: inline-flex;
}

.notice-pin {
  display: none;
  width: 16px;
  height: 16px;
  margin-right: 8px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #fff, #d1d5db 60%, #9ca3af 100%);
  box-shadow: inset 0 0 4px rgba(15, 23, 42, 0.2), 0 2px 6px rgba(15, 23, 42, 0.35);
  position: relative;
}

.notice-pin::after {
  content: '';
  position: absolute;
  width: 2px;
  height: 10px;
  background: #6b7280;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 2px;
}

.notice-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.notice-title h3 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
}

.notice-content {
  margin: 12px 0 16px;
  color: #334155;
  line-height: 1.6;
  font-size: 14px;
  position: relative;
  z-index: 1;
}

.notice-card__meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
  position: relative;
  z-index: 1;
}

.notice-card__actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.pagination {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.notice-fade-enter-active,
.notice-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.notice-fade-enter-from,
.notice-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
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
  .notice-grid {
    column-count: 2;
  }
}

@media (max-width: 768px) {
  .notice-grid {
    column-count: 1;
  }
}

@keyframes urgent-pulse {
  0% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0.5;
  }
}
</style>
