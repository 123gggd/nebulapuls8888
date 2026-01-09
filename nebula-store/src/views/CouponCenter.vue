<template>
  <div class="coupon-page">
    <div class="header-banner">
      <h1>领券中心</h1>
      <p>先领券，再购物，优惠享不停</p>
    </div>

    <div class="container">
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="item in couponList" :key="item.id" style="margin-bottom: 20px;">
          <div class="coupon-card">
            <div class="left">
              <div class="amount">
                <small>¥</small>{{ item.amount }}
              </div>
              <div class="condition">满 {{ item.minPoint }} 可用</div>
            </div>
            <div class="right">
              <div class="name" :title="item.name">{{ item.name }}</div>
              <div class="time">{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</div>
              <div class="progress-box">
                <el-progress
                    :percentage="getPercentage(item)"
                    :color="customColors"
                    :stroke-width="6"
                    :show-text="false"
                />
                <span class="text">已抢 {{ getPercentage(item) }}%</span>
              </div>
              <el-button
                  type="primary"
                  size="small"
                  round
                  class="btn"
                  :disabled="item.receiveCount >= item.publishCount"
                  @click="handleReceive(item.id)"
              >
                {{ item.receiveCount >= item.publishCount ? '已抢光' : '立即领取' }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && couponList.length === 0" description="暂无优惠券可领" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCouponCenter, receiveCoupon } from '@/api/store'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

interface Coupon {
  id: number
  name: string
  amount: number
  minPoint: number
  startTime: string
  endTime: string
  receiveCount: number
  publishCount: number
}

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const couponList = ref<Coupon[]>([])

const customColors = [
  { color: '#f56c6c', percentage: 80 },
  { color: '#e6a23c', percentage: 100 },
  { color: '#5cb87a', percentage: 100 },
]

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getCouponCenter({ page: 1, size: 20 })
    couponList.value = res.records || []
  } finally {
    loading.value = false
  }
}

const handleReceive = async (id: number) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await receiveCoupon(id)
    ElMessage.success('领取成功')
    loadData() // 刷新列表，更新已抢进度
  } catch (e) {
    // 拦截器处理
  }
}

const getPercentage = (item: Coupon) => {
  if (!item.publishCount || item.publishCount === 0) return 0
  const p = Math.round((item.receiveCount / item.publishCount) * 100)
  return Math.min(p, 100)
}

const formatTime = (time: string) => {
  return time ? time.split('T')[0] : ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.header-banner {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 99%, #fad0c4 100%);
  padding: 40px 0;
  text-align: center;
  color: white;
  margin-bottom: 30px;
  h1 { margin: 0; font-size: 32px; text-shadow: 0 2px 4px rgba(0,0,0,0.1); }
  p { margin: 10px 0 0; opacity: 0.9; }
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.coupon-card {
  background: white;
  border-radius: 8px;
  display: flex;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transition: transform 0.3s;
  height: 120px;

  &:hover { transform: translateY(-5px); }

  .left {
    width: 120px;
    background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
    color: white;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 15px 0;

    .amount { font-size: 28px; font-weight: bold; small { font-size: 14px; margin-right: 2px; } }
    .condition { font-size: 12px; margin-top: 5px; opacity: 0.9; }
  }

  .right {
    flex: 1;
    padding: 15px;
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: center;

    .name {
      font-weight: bold; color: #333; margin-bottom: 5px;
      white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
      max-width: 220px;
    }
    .time { font-size: 12px; color: #999; margin-bottom: 10px; }

    .progress-box {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 10px;
      .el-progress { width: 80px; }
      .text { font-size: 12px; color: #999; }
    }

    .btn {
      position: absolute;
      right: 15px;
      bottom: 15px;
      background: #ff7e5f;
      border-color: #ff7e5f;
      &:hover { background: #eb6b4d; border-color: #eb6b4d; }
      &.is-disabled { background: #fab6b6; border-color: #fab6b6; }
    }
  }
}
</style>