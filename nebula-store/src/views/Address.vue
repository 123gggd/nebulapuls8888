<template>
  <div class="address-page">
    <el-card shadow="never" style="border-radius: 12px;">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; font-size: 18px;">Shipping Addresses</span>
          <el-button type="primary" @click="handleAdd">Add Address</el-button>
        </div>
      </template>

      <el-table :data="addressList" border stripe v-loading="loading">
        <el-table-column prop="receiverName" label="Recipient" width="120" />
        <el-table-column prop="receiverPhone" label="Phone" width="150" />
        <el-table-column label="Region" width="200">
          <template #default="{ row }">
            {{ row.province }} {{ row.city }} {{ row.region }}
          </template>
        </el-table-column>
        <el-table-column prop="detailAddress" label="Street Address" />
        <el-table-column label="Default" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.defaultStatus === 1" type="success" size="small">Default</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="220" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.defaultStatus !== 1"
              link
              type="success"
              @click="handleSetDefault(row.id)"
            >
              Set Default
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">Edit</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Address dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? 'Edit Address' : 'Add Address'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="Recipient" required>
          <el-input v-model="form.receiverName" placeholder="Full name" />
        </el-form-item>
        <el-form-item label="Phone" required>
          <el-input v-model="form.receiverPhone" placeholder="Phone number" />
        </el-form-item>
        <el-form-item label="Region" required>
          <div style="display: flex; gap: 10px;">
            <el-input v-model="form.province" placeholder="State/Province" />
            <el-input v-model="form.city" placeholder="City" />
            <el-input v-model="form.region" placeholder="District" />
          </div>
        </el-form-item>
        <el-form-item label="Street Address" required>
          <el-input v-model="form.detailAddress" type="textarea" placeholder="Street and number" />
        </el-form-item>
        <el-form-item label="Default Address">
          <el-switch v-model="form.defaultStatus" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="submitForm">Save</el-button>
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
  // [Fix] Use Object.assign to avoid mutating the table data
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (id: number) => {
  if (!id) return
  ElMessageBox.confirm('Delete this address?', 'Confirm', { type: 'warning' }).then(async () => {
    await deleteAddress(id)
    ElMessage.success('Deleted successfully')
    loadData()
  })
}

const handleSetDefault = async (id: number) => {
  if (!id) return
  await setDefaultAddress(id)
  ElMessage.success('Default address updated')
  loadData()
}

const submitForm = async () => {
  if (!form.receiverName || !form.receiverPhone || !form.province || !form.city || !form.region || !form.detailAddress) {
    ElMessage.warning('Please fill in all required fields')
    return
  }
  await saveAddress(form)
  ElMessage.success('Saved successfully')
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
