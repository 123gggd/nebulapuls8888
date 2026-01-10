<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>秒杀活动管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新建秒杀</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="商品图" width="80" align="center">
          <template #default="{ row }">
            <el-image :src="row.mainImage" style="width: 50px; height: 50px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="价格信息" width="180" align="center">
          <template #default="{ row }">
            <div>秒杀价: <span style="color: red; font-weight: bold;">¥{{ row.seckillPrice }}</span></div>
            <div style="text-decoration: line-through; color: #999;">原价: ¥{{ row.originalPrice }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="stockCount" label="秒杀库存" width="100" align="center" />
        <el-table-column label="活动时间" width="320" align="center">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} <br/>至<br/> {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">{{ getStatusText(row) }}</el-tag>
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

    <!-- 新建秒杀弹窗 -->
    <el-dialog title="发布秒杀活动" v-model="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">

        <el-form-item label="选择商品" prop="productId">
          <div v-if="selectedProduct" class="selected-product">
            <img :src="selectedProduct.mainImage" class="thumb hover-zoom" />
            <div class="info">
              <div class="name">{{ selectedProduct.name }}</div>
              <div class="price">原价: ¥{{ selectedProduct.price }}</div>
            </div>
            <el-button type="danger" link @click="clearSelectedProduct">更换</el-button>
          </div>
          <el-button v-else type="primary" plain @click="productDialogVisible = true">点击选择商品</el-button>
        </el-form-item>

        <el-form-item label="秒杀价格" prop="seckillPrice">
          <el-input-number v-model="form.seckillPrice" :min="0" :precision="2" style="width: 180px">
            <template #prefix>¥</template>
          </el-input-number>
        </el-form-item>

        <el-form-item label="活动库存" prop="stockCount">
          <el-input-number v-model="form.stockCount" :min="1" style="width: 180px" />
          <span class="tips" v-if="selectedProduct"> (当前总库存: {{ selectedProduct.stock }})</span>
        </el-form-item>

        <el-form-item label="活动时间" prop="dateRange">
          <el-date-picker
              v-model="form.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">发布活动</el-button>
      </template>
    </el-dialog>

    <!-- 商品选择器弹窗 -->
    <el-dialog title="选择商品" v-model="productDialogVisible" width="700px">
      <div style="margin-bottom: 15px; display: flex; gap: 10px;">
        <el-input v-model="productKeyword" placeholder="搜索商品名称" clearable @keyup.enter="loadProductList" />
        <el-button type="primary" @click="loadProductList">搜索</el-button>
      </div>
      <el-table :data="productList" border stripe highlight-current-row @current-change="handleProductSelect">
        <el-table-column width="50" align="center">
          <template #default="{ row }">
            <el-radio :model-value="tempSelectedId" :label="row.id">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column label="图片" width="70">
          <template #default="{ row }">
            <el-image :src="row.mainImage" style="width: 40px; height: 40px" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="stock" label="库存" width="100" />
      </el-table>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmProductSelect" :disabled="!tempSelectedRow">确定选择</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSeckillList, createSeckill } from '@/api/marketing'
import { getProductList } from '@/api/product'

// --- 主列表逻辑 ---
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10 })

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getSeckillList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// --- 新建表单逻辑 ---
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const selectedProduct = ref<any>(null)

const form = reactive({
  productId: null,
  seckillPrice: 0,
  stockCount: 1,
  dateRange: [] as string[]
})

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  seckillPrice: [{ required: true, message: '请输入秒杀价', trigger: 'blur' }],
  stockCount: [{ required: true, message: '请输入活动库存', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择时间范围', trigger: 'change' }]
}

const handleAdd = () => {
  form.productId = null
  form.seckillPrice = 0
  form.stockCount = 1
  form.dateRange = []
  selectedProduct.value = null
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (selectedProduct.value && form.stockCount > selectedProduct.value.stock) {
        ElMessage.warning(`活动库存不能超过当前商品总库存 (${selectedProduct.value.stock})`)
        return
      }

      submitting.value = true
      try {
        const payload = {
          productId: form.productId,
          seckillPrice: form.seckillPrice,
          stockCount: form.stockCount,
          startTime: form.dateRange[0],
          endTime: form.dateRange[1]
        }
        await createSeckill(payload)
        ElMessage.success('发布成功')
        dialogVisible.value = false
        loadData()
      } finally {
        submitting.value = false
      }
    }
  })
}

// --- 商品选择器逻辑 ---
const productDialogVisible = ref(false)
const productKeyword = ref('')
const productList = ref([])
const tempSelectedId = ref(null)
const tempSelectedRow = ref<any>(null)

const loadProductList = async () => {
  const res: any = await getProductList({
    page: 1,
    size: 20,
    keyword: productKeyword.value,
    status: 1 // 只能选上架商品
  })
  productList.value = res.data.records
}

const handleProductSelect = (row: any) => {
  if (row) {
    tempSelectedId.value = row.id
    tempSelectedRow.value = row
  }
}

const confirmProductSelect = () => {
  if (tempSelectedRow.value) {
    selectedProduct.value = tempSelectedRow.value
    form.productId = tempSelectedRow.value.id
    // 自动填充建议价格 (例如原价的8折，仅作建议)
    form.seckillPrice = Number((tempSelectedRow.value.price * 0.8).toFixed(2))
    productDialogVisible.value = false
  }
}

const clearSelectedProduct = () => {
  selectedProduct.value = null
  form.productId = null
}

// --- 工具方法 ---
const formatTime = (time: string) => time ? time.replace('T', ' ') : ''

const getStatusText = (row: any) => {
  const now = new Date().getTime()
  const start = new Date(row.startTime).getTime()
  const end = new Date(row.endTime).getTime()

  if (now < start) return '未开始'
  if (now > end) return '已结束'
  if (now >= start && now <= end) return '进行中'
  return '未知'
}

const getStatusType = (row: any) => {
  const text = getStatusText(row)
  if (text === '进行中') return 'danger'
  if (text === '未开始') return 'success'
  return 'info'
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.selected-product {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  width: 100%;
}
.selected-product .thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}
.selected-product .info {
  flex: 1;
}
.selected-product .name {
  font-weight: bold;
  font-size: 14px;
}
.selected-product .price {
  color: #999;
  font-size: 12px;
}
.tips {
  color: #999;
  font-size: 12px;
  margin-left: 5px;
}
</style>
