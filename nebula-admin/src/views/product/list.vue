<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-form inline :model="queryParams" class="filter-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryParams.keyword" placeholder="输入名称或副标题" clearable @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" style="width: 140px" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="filter-actions">
        <el-button class="icon-action" circle :icon="Search" @click="handleSearch" />
        <el-button class="primary-pill" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          发布商品
        </el-button>
      </div>
    </div>

    <!-- 商品表格 -->
    <el-card shadow="never" class="table-card" style="margin-top: 20px;">
      <el-table :data="tableData" style="width: 100%" v-loading="loading" :row-class-name="getRowClassName">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="商品图片" width="100" align="center">
          <template #default="{ row }">
            <el-image
                :src="row.mainImage"
                class="product-thumb"
                fit="cover"
                :preview-src-list="[row.mainImage]"
                preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" align="center" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <span class="status-pill" :class="row.status === 1 ? 'is-active' : 'is-muted'">
              <span class="dot"></span>
              {{ row.status === 1 ? '上架' : '下架' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button class="icon-action" text circle :icon="Edit" @click="handleEdit(row)" />
            <el-button
                class="icon-action"
                text
                circle
                :icon="row.status === 1 ? Download : Upload"
                @click="toggleStatus(row)"
            />
            <el-popconfirm title="确认删除该商品吗? 此操作不可恢复!" @confirm="handleDelete(row)">
              <template #reference>
                <el-button class="icon-action danger" text circle :icon="Delete" />
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right;">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="total"
            layout="total, prev, pager, next, jumper"
            @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Download, Edit, Plus, Search, Upload } from '@element-plus/icons-vue'
import { getProductList, updateProductStatus, deleteProduct } from '@/api/product'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: null as number | null
})

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getProductList(queryParams)
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

const handleAdd = () => {
  router.push('/product/edit')
}

const handleEdit = (row: any) => {
  router.push(`/product/edit/${row.id}`)
}

const toggleStatus = (row: any) => {
  const targetStatus = row.status === 1 ? 0 : 1
  const actionText = targetStatus === 1 ? '上架' : '下架'

  ElMessageBox.confirm(`确认${actionText}该商品吗?`, '提示', {
    type: 'warning'
  }).then(async () => {
    // 后端接收 List<Long> ids
    await updateProductStatus([row.id], targetStatus)
    ElMessage.success('操作成功')
    loadData()
  })
}

const handleDelete = async (row: any) => {
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  loadData()
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
  gap: 12px;
}

.icon-action {
  opacity: 0.6;
  transition: all 0.2s ease;
}

.icon-action:hover {
  opacity: 1;
  color: #409eff;
}

.icon-action.danger:hover {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.12);
}

.primary-pill {
  border-radius: 999px;
  padding: 0 18px;
  height: 36px;
  color: #fff;
  border: none;
  background: linear-gradient(135deg, #38bdf8, #6366f1);
  box-shadow: 0 10px 24px rgba(56, 189, 248, 0.35);
}

.product-thumb {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-thumb:hover {
  transform: scale(1.5);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.2);
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

.status-pill.is-active {
  color: #0f172a;
  border-color: rgba(56, 189, 248, 0.4);
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
