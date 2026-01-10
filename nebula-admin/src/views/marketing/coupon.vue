<template>
  <div class="page-container">
    <div class="ticket-toolbar">
      <div class="toolbar-title">
        <div class="title">优惠券列表</div>
        <div class="subtitle">拟物化票据视图</div>
      </div>
      <el-button class="primary-pill" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建优惠券
      </el-button>
    </div>

    <!-- 列表 -->
    <div class="ticket-grid" v-loading="loading">
      <div v-for="row in tableData" :key="row.id" class="ticket-card" :class="{ 'is-inactive': row.status !== 1 }">
        <div class="ticket-content">
          <div class="ticket-left">
            <div class="amount">¥{{ row.amount }}</div>
            <div class="threshold">满 {{ row.minPoint }} 可用</div>
            <div class="ticket-image" v-if="row.image">
              <el-image :src="row.image" fit="cover" :preview-src-list="[row.image]" preview-teleported />
            </div>
          </div>
          <div class="ticket-right">
            <div class="ticket-name">{{ row.name }}</div>
            <div class="ticket-rule">
              <span>有效期</span>
              <span>{{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}</span>
            </div>
            <div class="ticket-rule">
              <span>发放情况</span>
              <el-progress
                  :percentage="Math.min(100, Math.round((row.receiveCount || 0) / row.publishCount * 100))"
                  :format="() => `${row.receiveCount}/${row.publishCount}`"
              />
            </div>
            <div class="ticket-actions">
              <el-button
                  class="icon-action"
                  text
                  circle
                  :icon="row.status === 1 ? Download : Upload"
                  @click="handleStatus(row)"
              />
              <el-popconfirm title="确认删除该优惠券吗?" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button class="icon-action danger" text circle :icon="Delete" />
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
        <div class="ticket-stamp" v-if="row.status !== 1">已下架</div>
      </div>
    </div>

    <div style="margin-top: 20px; text-align: right;">
      <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
      />
    </div>

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
        <el-form-item label="封面图">
          <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleUploadImage"
              :before-upload="beforeUpload"
          >
            <img v-if="form.image" :src="form.image" class="avatar hover-zoom" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
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
import { ElMessage } from 'element-plus'
import { Delete, Download, Plus, Upload } from '@element-plus/icons-vue'
import { getCouponList, createCoupon, updateCouponStatus, deleteCoupon } from '@/api/marketing'
import { uploadFile } from '@/api/product'

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
  image: '',
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
  form.image = ''
  form.publishCount = 100
  form.perLimit = 1
  form.dateRange = []
  dialogVisible.value = true
}

const handleUploadImage = async (options: any) => {
  try {
    const res: any = await uploadFile(options.file)
    form.image = res.data
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const beforeUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) ElMessage.error('只能上传 JPG/PNG 格式!')
  if (!isLt2M) ElMessage.error('图片大小不能超过 2MB!')
  return isJPG && isLt2M
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

const handleDelete = async (row: any) => {
  await deleteCoupon(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const formatTime = (time: string) => time ? time.replace('T', ' ') : ''

onMounted(loadData)
</script>

<style scoped>
.ticket-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.12);
  margin-bottom: 20px;
}

.toolbar-title .title {
  font-weight: 600;
  font-size: 16px;
  color: #0f172a;
}

.toolbar-title .subtitle {
  font-size: 12px;
  color: #94a3b8;
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

.ticket-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.ticket-card {
  position: relative;
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.12);
  overflow: hidden;
}

.ticket-card::before,
.ticket-card::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 18px;
  height: 18px;
  background: #f8fafc;
  border-radius: 50%;
  transform: translateY(-50%);
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.2);
}

.ticket-card::before {
  left: -9px;
}

.ticket-card::after {
  right: -9px;
}

.ticket-card.is-inactive {
  filter: grayscale(0.6);
}

.ticket-content {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 16px;
  align-items: center;
}

.ticket-left {
  text-align: center;
}

.amount {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #38bdf8, #818cf8);
  -webkit-background-clip: text;
  color: transparent;
}

.threshold {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.ticket-image :deep(img) {
  margin-top: 10px;
  width: 90px;
  height: 90px;
  border-radius: 14px;
  object-fit: cover;
}

.ticket-right {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ticket-name {
  font-weight: 600;
  color: #0f172a;
}

.ticket-rule {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

.ticket-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
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

.ticket-stamp {
  position: absolute;
  right: 16px;
  top: 16px;
  padding: 4px 10px;
  border: 1px solid rgba(239, 68, 68, 0.5);
  color: #ef4444;
  font-weight: 600;
  border-radius: 999px;
  font-size: 12px;
  transform: rotate(8deg);
}
.avatar-uploader .avatar {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  text-align: center;
  line-height: 148px;
  border: 1px dashed #d9d9d9;
}
</style>
