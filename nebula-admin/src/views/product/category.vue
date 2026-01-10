<template>
  <div class="page-container category-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">分类管理</div>
            <div class="subtitle">结构清晰化 · Glass Tree</div>
          </div>
          <el-button type="primary" icon="Plus" @click="handleAddRoot">新增一级分类</el-button>
        </div>
      </template>

      <div class="category-board" v-loading="loading">
        <el-empty v-if="!tableData.length" description="暂无分类" />
        <div v-else class="category-grid">
          <div v-for="category in tableData" :key="category.id" class="category-panel">
            <div class="panel-header">
              <div>
                <div class="panel-title">{{ category.name }}</div>
                <div class="panel-meta">排序 {{ category.sort ?? '-' }}</div>
              </div>
              <div class="panel-actions">
                <el-tag :type="category.status ? 'success' : 'info'">{{ category.status ? '启用' : '禁用' }}</el-tag>
                <el-button size="small" link type="primary" @click="handleAddChild(category)">新增子项</el-button>
                <el-button size="small" link type="primary" @click="handleEdit(category)">编辑</el-button>
                <el-button size="small" link type="danger" @click="handleDelete(category)">删除</el-button>
              </div>
            </div>

            <div class="panel-children">
              <div v-if="!category.children || !category.children.length" class="child-empty">
                暂无子分类，可拖拽添加
              </div>
              <div v-else class="child-tags">
                <div v-for="child in category.children" :key="child.id" class="child-chip">
                  <div class="child-title">{{ child.name }}</div>
                  <div class="child-meta">排序 {{ child.sort ?? '-' }}</div>
                  <div class="child-actions">
                    <el-tag :type="child.status ? 'success' : 'info'" size="small">
                      {{ child.status ? '启用' : '禁用' }}
                    </el-tag>
                    <el-button size="small" link type="primary" @click="handleEdit(child)">编辑</el-button>
                    <el-button size="small" link type="danger" @click="handleDelete(child)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

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
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const parentName = ref('无')

const form = reactive({
  id: null as number | null,
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
.category-page {
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
.category-board {
  min-height: 320px;
}
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}
.category-panel {
  padding: 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(10px);
}
.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  align-items: flex-start;
  margin-bottom: 16px;
}
.panel-title {
  font-weight: 600;
  font-size: 16px;
}
.panel-meta {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
.panel-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}
.panel-children {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.child-empty {
  font-size: 13px;
  color: #94a3b8;
  border: 1px dashed rgba(148, 163, 184, 0.4);
  padding: 12px;
  border-radius: 12px;
  text-align: center;
}
.child-tags {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.child-chip {
  padding: 12px 14px;
  border-radius: 999px;
  background: rgba(226, 232, 240, 0.6);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.child-title {
  font-weight: 600;
}
.child-meta {
  font-size: 12px;
  color: #64748b;
}
.child-actions {
  display: flex;
  gap: 6px;
  align-items: center;
}
</style>
