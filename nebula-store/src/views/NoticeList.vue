<template>
  <div class="notice-page">
    <div class="container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Announcements</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card shadow="never" class="notice-card" v-loading="loading">
        <template #header>
          <div style="font-weight: bold; font-size: 18px;">Announcement List</div>
        </template>

        <div v-if="noticeList.length > 0">
          <div v-for="item in noticeList" :key="item.id" class="notice-item">
            <div class="title-row">
              <el-tag size="small" type="warning" class="tag">Notice</el-tag>
              <span class="title" @click="goDetail(item.id)">{{ item.title }}</span>
              <span class="time">{{ item.createTime }}</span>
            </div>
            <div class="content">{{ item.content }}</div>
            <div class="actions">
              <el-button link type="primary" @click="goDetail(item.id)">View Details</el-button>
            </div>
          </div>
        </div>

        <el-empty v-else description="No announcements yet" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotices } from '@/api/store'

const loading = ref(false)
const noticeList = ref<any[]>([])
const router = useRouter()

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getNotices()
    noticeList.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const goDetail = (id: number) => {
  router.push(`/notice/${id}`)
}
</script>

<style scoped lang="scss">
.notice-page {
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
.notice-card {
  border-radius: 8px;
}
.notice-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 20px 0;
  &:last-child { border-bottom: none; }

  .title-row {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    .tag { margin-right: 12px; }
    .title { font-size: 16px; font-weight: bold; color: #303133; flex: 1; }
    .time { font-size: 13px; color: #909399; }
  }

  .content {
    color: #606266;
    line-height: 1.6;
    font-size: 14px;
    padding-left: 55px; // Indent to align with the title
    white-space: pre-wrap; // Preserve line breaks
  }
  .actions {
    padding-left: 55px;
    margin-top: 8px;
  }
}
</style>
