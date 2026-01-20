<template>
  <div class="notice-detail-page">
    <div class="container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/notice' }">系统公告</el-breadcrumb-item>
        <el-breadcrumb-item>公告详情</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card shadow="never" class="detail-card" v-loading="loading">
        <template #header>
          <div class="header">
            <h2>{{ notice?.title || '公告详情' }}</h2>
            <span class="time">{{ notice?.createTime || '-' }}</span>
          </div>
        </template>

        <div v-if="notice" class="content">
          {{ notice.content }}
        </div>
        <el-empty v-else description="公告不存在或已下架" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getNoticeDetail } from '@/api/store'

const route = useRoute()
const loading = ref(false)
const notice = ref<any>(null)

const loadData = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    if (!id) return
    const res: any = await getNoticeDetail(id)
    notice.value = res?.data || res
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.notice-detail-page {
  padding: 20px 0;
  background: #f5f7fa;
  min-height: 80vh;
}
.container {
  width: 1200px;
  margin: 0 auto;
}
.breadcrumb {
  margin-bottom: 20px;
}
.detail-card {
  border-radius: 8px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 20px;
}
.header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}
.header .time {
  color: #909399;
  font-size: 13px;
}
.content {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  font-size: 15px;
}
</style>
