<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form inline :model="queryParams">
        <el-form-item label="商品名称">
          <el-input v-model="queryParams.keyword" placeholder="输入名称或副标题" clearable @keyup.enter="handleSearch"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" style="width: 120px" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button type="success" icon="Plus" @click="handleAdd">发布商品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商品表格 -->
    <el-card shadow="never" class="table-card" style="margin-top: 20px;">
      <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="商品图片" width="100" align="center">
          <template #default="{ row }">
            <el-image
                :src="row.mainImage"
                style="width: 60px; height: 60px"
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
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
                :type="row.status === 1 ? 'warning' : 'success'"
                link
                @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该商品吗? 此操作不可恢复!', '警告', {
    type: 'error'
  }).then(async () => {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(loadData)
</script>