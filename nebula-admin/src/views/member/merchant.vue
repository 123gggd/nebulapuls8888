<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>商家列表</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新建商家</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="登录账号" min-width="120" />
        <el-table-column prop="nickname" label="店铺名称" min-width="150" />

        <el-table-column label="入驻时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" v-if="row.status === 1">正常</el-tag>
            <el-tag type="danger" v-else>禁用</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right;">
        <el-pagination
            layout="total, prev, pager, next"
            :total="total"
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新建商家弹窗 -->
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
const tableData = ref([])
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