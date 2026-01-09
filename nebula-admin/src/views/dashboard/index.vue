<template>
  <div class="dashboard-container">
    <!-- 数据统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <template #header>
            <div class="card-header">
              <span>今日销售额</span>
              <el-tag type="success">Today</el-tag>
            </div>
          </template>
          <div class="card-content">
            <div class="card-value">¥ {{ formatNumber(stats.todaySales) }}</div>
            <div class="card-desc">总销售额: ¥ {{ formatNumber(stats.totalSales) }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <template #header>
            <div class="card-header">
              <span>待发货订单</span>
              <el-tag type="danger">待办</el-tag>
            </div>
          </template>
          <div class="card-content">
            <div class="card-value">{{ stats.pendingOrders }}</div>
            <div class="card-desc">订单总量: {{ stats.totalOrders }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <template #header>
            <div class="card-header">
              <span>商品库存</span>
              <el-tag type="warning">Goods</el-tag>
            </div>
          </template>
          <div class="card-content">
            <div class="card-value">{{ stats.totalProducts }}</div>
            <div class="card-desc">当前上架商品数</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <template #header>
            <div class="card-header">
              <span>{{ isAdmin ? '用户总数' : '店铺评级' }}</span>
              <el-tag type="info">Info</el-tag>
            </div>
          </template>
          <div class="card-content">
            <div class="card-value" v-if="isAdmin">{{ stats.totalUsers }}</div>
            <div class="card-value" v-else>5.0</div> <!-- 商家展示评分 -->
            <div class="card-desc">{{ isAdmin ? '平台注册会员' : '当前店铺评分' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 销售趋势图表 -->
    <el-card shadow="never" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>近 7 日销售趋势 (实时数据)</span>
        </div>
      </template>
      <div ref="chartRef" style="height: 350px; width: 100%;"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/system'
import { useUserStore } from '@/stores/modules/user'

const chartRef = ref()
const userStore = useUserStore()
let myChart: echarts.ECharts | null = null

const isAdmin = computed(() => userStore.roles.includes('ADMIN'))

const stats = reactive({
  totalSales: 0,
  todaySales: 0,
  totalOrders: 0,
  pendingOrders: 0,
  totalProducts: 0,
  totalUsers: 0
})

const formatNumber = (num: number) => {
  return Number(num).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const initChart = (dates: string[], values: number[]) => {
  if (!chartRef.value) return
  myChart = echarts.init(chartRef.value)

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: dates },
    yAxis: { type: 'value' },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.01)' }
          ])
        },
        data: values
      }
    ]
  }
  myChart.setOption(option)
}

const resizeChart = () => myChart?.resize()

onMounted(async () => {
  window.addEventListener('resize', resizeChart)
  try {
    const res: any = await getDashboardStats()
    const data = res.data
    Object.assign(stats, data)

    if (data.trendDates && data.trendValues) {
      initChart(data.trendDates, data.trendValues)
    }
  } catch (e) {
    console.error('加载看板失败', e)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  myChart?.dispose()
})
</script>

<style scoped>
.dashboard-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-content { text-align: center; padding: 10px 0; }
.card-value { font-size: 28px; font-weight: bold; color: #303133; margin-bottom: 8px; }
.card-desc { font-size: 13px; color: #909399; }
</style>