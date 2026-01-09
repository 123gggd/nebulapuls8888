<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <el-button type="primary" icon="Plus" @click="handleAddRoot">新增一级分类</el-button>
        </div>
      </template>

      <el-table
          :data="tableData"
          style="width: 100%; margin-bottom: 20px;"
          row-key="id"
          border
          default-expand-all
          v-loading="loading"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'info'">{{ row.status ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />

        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAddChild(row)">新增子项</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="父级分类">
          <el-input v-model="parentName" disabled />
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="true" :inactive-value="false" />
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryTree, addCategory, updateCategory, deleteCategory } from '@/api/product'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const parentName = ref('无')

const form = reactive({
  id: null,
  parentId: 0,
  name: '',
  sort: 0,
  status: true
})

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getCategoryTree()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAddRoot = () => {
  resetForm()
  form.parentId = 0
  parentName.value = '无 (一级分类)'
  dialogTitle.value = '新增一级分类'
  dialogVisible.value = true
}

const handleAddChild = (row: any) => {
  resetForm()
  form.parentId = row.id
  parentName.value = row.name
  dialogTitle.value = '新增子分类'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  dialogTitle.value = '编辑分类'
  // 简单的父级显示逻辑，实际可能需要遍历树查找父级名称
  parentName.value = row.parentId === 0 ? '无 (一级分类)' : '上级分类'
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除分类 "${row.name}" 吗? 如果包含子分类将无法删除。`, '警告', {
    type: 'warning'
  }).then(async () => {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const submitForm = async () => {
  try {
    const payload = { ...form }
    // 转换 boolean 为 1/0 (如果后端需要) 或者直接传 boolean (MyBatis Plus 支持)
    // payload.status = form.status ? 1 : 0

    if (form.id) {
      await updateCategory(payload)
    } else {
      await addCategory(payload)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // error handled by request.ts
  }
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.sort = 0
  form.status = true
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>