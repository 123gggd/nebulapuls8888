<template>
  <div class="address-page">
    <el-card shadow="never" style="border-radius: 12px;">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; font-size: 18px;">收货地址管理</span>
          <el-button type="primary" @click="handleAdd">新增地址</el-button>
        </div>
      </template>

      <el-table :data="addressList" border stripe v-loading="loading">
        <el-table-column prop="receiverName" label="收货人" width="120" />
        <el-table-column prop="receiverPhone" label="手机号" width="150" />
        <el-table-column label="所在地区" width="200">
          <template #default="{ row }">
            {{ row.province }} {{ row.city }} {{ row.region }}
          </template>
        </el-table-column>
        <el-table-column prop="detailAddress" label="详细地址" />
        <el-table-column label="默认" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.defaultStatus === 1" type="success" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.defaultStatus !== 1"
              link
              type="success"
              @click="handleSetDefault(row.id)"
            >
              设为默认
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 地址弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑地址' : '新增地址'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="收货人" required>
          <el-input v-model="form.receiverName" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.receiverPhone" placeholder="手机号码" />
        </el-form-item>
        <el-form-item label="地区" required>
          <div style="display: flex; gap: 10px;">
            <el-input v-model="form.province" placeholder="省" />
            <el-input v-model="form.city" placeholder="市" />
            <el-input v-model="form.region" placeholder="区" />
          </div>
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="form.detailAddress" type="textarea" placeholder="街道门牌号" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.defaultStatus" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAddressList, saveAddress, deleteAddress, setDefaultAddress } from '@/api/store'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Address {
  id?: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  region: string
  detailAddress: string
  defaultStatus: number
}

const loading = ref(false)
const addressList = ref<Address[]>([])
const dialogVisible = ref(false)

const form = reactive<Address>({
  id: undefined,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  region: '',
  detailAddress: '',
  defaultStatus: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAddressList() as unknown as Address[]
    addressList.value = res || []
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.id = undefined
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.region = ''
  form.detailAddress = ''
  form.defaultStatus = 0
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Address) => {
  // [修复] 使用 Object.assign 复制数据，防止修改时直接变动表格
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (id: number) => {
  if (!id) return
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    await deleteAddress(id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSetDefault = async (id: number) => {
  if (!id) return
  await setDefaultAddress(id)
  ElMessage.success('默认地址已更新')
  loadData()
}

const submitForm = async () => {
  if (!form.receiverName || !form.receiverPhone || !form.detailAddress) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await saveAddress(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
