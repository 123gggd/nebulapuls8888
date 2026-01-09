<template>
  <div class="app-container" style="padding: 20px;">
    <el-card shadow="never">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>用户管理 (管理员权限)</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form inline :model="queryParams" class="search-form" style="margin-bottom: 20px;">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户表格 -->
      <el-table :data="tableData" border v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column label="头像" width="100" align="center">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="50">{{ row.username?.charAt(0)?.toUpperCase() }}</el-avatar>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="nickname" label="昵称" width="150">
          <template #default="{ row }">{{ row.nickname || '-' }}</template>
        </el-table-column>

        <el-table-column prop="roleCode" label="角色" width="120" align="center">
          <template #default="{ scope }">
            <!-- 兼容 role 或 roleCode 字段 -->
            <el-tag v-if="(scope?.row?.role === 'ADMIN') || (scope?.row?.roleCode === 'ADMIN')" type="danger">管理员</el-tag>
            <el-tag v-else-if="(scope?.row?.role === 'MERCHANT') || (scope?.row?.roleCode === 'MERCHANT')" type="warning">商家</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="merchantId" label="商户ID" width="100" align="center">
          <template #default="{ row }">
            {{ row.merchantId || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="注册时间" width="180" align="center" />

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
                link
                :type="row.status === 1 ? 'danger' : 'success'"
                size="small"
                @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '冻结' : '解冻' }}
            </el-button>
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
            background
            layout="total, prev, pager, next"
            :total="total"
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="500px">
      <div v-if="currentMember" class="member-detail">
        <div class="header">
          <el-avatar :src="currentMember.avatar" :size="80" />
          <h3>{{ currentMember.username }}</h3>
          <el-tag size="small">{{ currentMember.role || currentMember.roleCode }}</el-tag>
        </div>
        <el-descriptions border :column="1" style="margin-top: 20px">
          <el-descriptions-item label="用户ID">{{ currentMember.id }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentMember.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentMember.createTime }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
             <span :style="{ color: currentMember.status === 1 ? '#67c23a' : '#f56c6c' }">
               {{ currentMember.status === 1 ? '正常' : '已冻结' }}
             </span>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentMember.merchantId" label="关联店铺ID">
            {{ currentMember.merchantId }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMemberList, updateMemberStatus, getMemberDetail } from '@/api/member'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentMember = ref<any>(null)

const queryParams = reactive({
  page: 1,
  size: 10,
  username: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getMemberList(queryParams)
    // 兼容 Page 结构或直接 List 结构
    const data = res.data || res
    tableData.value = data.records || (Array.isArray(data) ? data : [])
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

const handleToggleStatus = (row: any) => {
  const action = row.status === 1 ? '冻结' : '解冻'
  const newStatus = row.status === 1 ? 0 : 1

  ElMessageBox.confirm(`确认要${action}用户 "${row.username}" 吗?`, '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await updateMemberStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadData()
  })
}

const handleDetail = async (row: any) => {
  try {
    // 尝试获取更详细的信息，如果 API 没就绪，降级显示行数据
    try {
      const res: any = await getMemberDetail(row.id)
      currentMember.value = res.data || res
    } catch (err) {
      currentMember.value = row
    }
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.member-detail .header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}
.member-detail h3 { margin: 10px 0 5px; }
</style>