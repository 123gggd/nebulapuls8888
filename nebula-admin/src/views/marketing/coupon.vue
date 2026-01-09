<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>优惠券列表</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新建优惠券</el-button>
        </div>
      </template>

      <!-- 列表 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="name" label="优惠券名称" min-width="150" />
        <el-table-column label="面值" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="使用门槛" width="150" align="center">
          <template #default="{ row }">
            满 {{ row.minPoint }} 可用
          </template>
        </el-table-column>
        <el-table-column label="发放情况" width="150" align="center">
          <template #default="{ row }">
            <el-progress
                :percentage="Math.min(100, Math.round((row.receiveCount || 0) / row.publishCount * 100))"
                :format="() => `${row.receiveCount}/${row.publishCount}`"
            />
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="320" align="center">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} 至 {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
                :type="row.status === 1 ? 'warning' : 'success'"
                link
                @click="handleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '发布' }}
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
            layout="total, prev, pager, next"
            @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新建弹窗 -->
    <el-dialog title="新建优惠券" v-model="dialogVisible" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="例：双11大促满减券" />
        </el-form-item>
        <el-form-item label="面额" prop="amount">
          <el-input-number v-model="form.amount" :min="1" :precision="0" style="width: 180px">
            <template #prefix>¥</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="门槛" prop="minPoint">
          <el-input-number v-model="form.minPoint" :min="0" :precision="0" style="width: 180px">
            <template #prefix>满</template>
          </el-input-number>
          <span style="margin-left: 10px; color: #999; font-size: 12px;">0元代表无门槛</span>
        </el-form-item>
        <el-form-item label="发行量" prop="publishCount">
          <el-input-number v-model="form.publishCount" :min="1" style="width: 180px" />
        </el-form-item>
        <el-form-item label="每人限领" prop="perLimit">
          <el-input-number v-model="form.perLimit" :min="1" style="width: 180px" />
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
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
        <el-button type="primary" @click="submitForm" :loading="submitting">确定创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCouponList, createCoupon, updateCouponStatus, deleteCoupon } from '@/api/marketing'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10 })

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  amount: 10,
  minPoint: 100,
  publishCount: 1000,
  perLimit: 1,
  dateRange: [] as string[]
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入面额', trigger: 'blur' }],
  publishCount: [{ required: true, message: '请输入发行量', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getCouponList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.name = ''
  form.amount = 10
  form.minPoint = 100
  form.publishCount = 100
  form.perLimit = 1
  form.dateRange = []
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (!form.dateRange || form.dateRange.length < 2) {
        ElMessage.warning('请选择完整的时间范围')
        return
      }

      const payload = {
        ...form,
        startTime: form.dateRange[0],
        endTime: form.dateRange[1]
      }

      submitting.value = true
      try {
        await createCoupon(payload)
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadData()
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleStatus = async (row: any) => {
  const target = row.status === 1 ? 0 : 1
  await updateCouponStatus(row.id, target)
  ElMessage.success('操作成功')
  loadData()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该优惠券吗?', '警告', { type: 'warning' }).then(async () => {
    await deleteCoupon(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const formatTime = (time: string) => time ? time.replace('T', ' ') : ''

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>