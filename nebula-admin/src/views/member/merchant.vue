<template>
  <div class="page-container merchant-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">商家名片墙</div>
            <div class="subtitle">商业价值凸显 · Business Card Grid</div>
          </div>
          <el-button type="primary" icon="Plus" @click="handleAdd">新建商家</el-button>
        </div>
      </template>

      <div class="merchant-grid" v-loading="loading">
        <el-empty v-if="!tableData.length" description="暂无商家" />
        <div v-else class="merchant-grid-inner">
          <div v-for="merchant in tableData" :key="merchant.id" class="merchant-card">
            <div class="merchant-card-inner">
              <div class="merchant-front">
                <div class="merchant-top">
                  <el-avatar :src="merchant.logo" :size="56">{{ getInitial(merchant.nickname || merchant.storeName) }}</el-avatar>
                  <div class="merchant-info">
                    <div class="merchant-name">{{ merchant.nickname || merchant.storeName || '-' }}</div>
                    <div class="merchant-account">账号 {{ merchant.username || '-' }}</div>
                  </div>
                </div>
                <div class="merchant-badges">
                  <el-tag type="success" effect="dark">信用 {{ merchant.level || 'A' }}</el-tag>
                  <el-tag type="info" effect="light">入驻 {{ formatTime(merchant.createTime) || '-' }}</el-tag>
                </div>
                <div class="merchant-actions">
                  <el-button size="small" type="primary" plain>查看商品</el-button>
                  <el-button size="small" type="success" plain>查看订单</el-button>
                </div>
              </div>
              <div class="merchant-back">
                <div class="kpi-title">经营概况</div>
                <div class="kpi-list">
                  <div class="kpi-item">
                    <span class="kpi-label">总销量</span>
                    <span class="kpi-value">{{ formatNumber(merchant.totalSales) }}</span>
                  </div>
                  <div class="kpi-item">
                    <span class="kpi-label">在售商品</span>
                    <span class="kpi-value">{{ formatNumber(merchant.productCount) }}</span>
                  </div>
                  <div class="kpi-item">
                    <span class="kpi-label">累计订单</span>
                    <span class="kpi-value">{{ formatNumber(merchant.orderCount) }}</span>
                  </div>
                  <div class="kpi-item">
                    <span class="kpi-label">客诉率</span>
                    <span class="kpi-value">{{ merchant.complaintRate ? `${merchant.complaintRate}%` : '--' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination-wrap">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog title="新建商家账号" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入商家登录账号" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password />
        </el-form-item>
        <el-form-item label="店铺名称" prop="storeName">
          <el-input v-model="form.storeName" placeholder="请输入店铺名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantList, createMerchant } from '@/api/member'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10
})

const form = reactive({
  username: '',
  password: '',
  storeName: ''
})

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  storeName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getMerchantList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.username = ''
  form.password = ''
  form.storeName = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        await createMerchant(form)
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadData()
      } finally {
        submitting.value = false
      }
    }
  })
}

const formatTime = (time: string) => (time ? time.replace('T', ' ') : '')

const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '--'
  return value.toLocaleString()
}

const getInitial = (name?: string) => {
  if (!name) return 'M'
  return name.trim().slice(0, 1).toUpperCase()
}

onMounted(loadData)
</script>

<style scoped>
.merchant-page {
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
.merchant-grid {
  min-height: 320px;
}
.merchant-grid-inner {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}
.merchant-card {
  perspective: 1000px;
}
.merchant-card-inner {
  position: relative;
  height: 260px;
  transform-style: preserve-3d;
  transition: transform 0.6s;
}
.merchant-card:hover .merchant-card-inner {
  transform: rotateY(180deg);
}
.merchant-front,
.merchant-back {
  position: absolute;
  inset: 0;
  backface-visibility: hidden;
  border-radius: 18px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.merchant-back {
  transform: rotateY(180deg);
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.9), rgba(30, 41, 59, 0.9));
  color: #f8fafc;
}
.merchant-top {
  display: flex;
  gap: 12px;
  align-items: center;
}
.merchant-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.merchant-name {
  font-weight: 600;
  font-size: 16px;
}
.merchant-account {
  font-size: 12px;
  color: #64748b;
}
.merchant-badges {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.merchant-actions {
  display: flex;
  gap: 10px;
}
.kpi-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}
.kpi-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.kpi-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
}
.kpi-label {
  font-size: 12px;
  color: rgba(226, 232, 240, 0.8);
}
.kpi-value {
  font-size: 16px;
  font-weight: 600;
}
.pagination-wrap {
  margin-top: 20px;
  text-align: right;
}
</style>
