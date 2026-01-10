<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-header">
            <div>
              <div class="stat-label">总销售额</div>
              <div class="stat-value">¥ {{ formatNumber(stats.totalSales) }}</div>
            </div>
            <div class="stat-icon sales">¥</div>
          </div>
          <div class="stat-foot" :class="trendMeta.sales.up ? 'up' : 'down'">
            <el-icon v-if="trendMeta.sales.up"><CaretTop /></el-icon>
            <el-icon v-else><CaretBottom /></el-icon>
            <span>{{ trendMeta.sales.rate }}%</span>
            <span class="muted">近 7 日</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-header">
            <div>
              <div class="stat-label">订单总量</div>
              <div class="stat-value">{{ stats.totalOrders }}</div>
            </div>
            <div class="stat-icon orders">
              <el-icon><Document /></el-icon>
            </div>
          </div>
          <div class="stat-foot neutral">
            <span>{{ stats.totalOrders ? '稳步增长' : '暂无数据' }}</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-header">
            <div>
              <div class="stat-label">商品总数</div>
              <div class="stat-value">{{ stats.totalProducts }}</div>
            </div>
            <div class="stat-icon products">
              <el-icon><Goods /></el-icon>
            </div>
          </div>
          <div class="stat-foot neutral">
            <span>{{ stats.totalProducts ? '持续上新' : '暂无数据' }}</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-header">
            <div>
              <div class="stat-label">平台注册用户</div>
              <div class="stat-value">{{ stats.totalUsers }}</div>
            </div>
            <div class="stat-icon users">
              <el-icon><User /></el-icon>
            </div>
          </div>
          <div class="stat-foot neutral">
            <span>{{ stats.totalUsers ? '活跃增长' : '暂无数据' }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近 7 日销售趋势</span>
              <span class="muted">实时更新</span>
            </div>
          </template>
          <div ref="salesChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>品类占比</span>
              <span class="muted">热销品类</span>
            </div>
          </template>
          <div v-if="categoryData.length" ref="categoryChartRef" class="chart-box"></div>
          <el-empty v-else description="暂无品类数据" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="14">
        <el-card shadow="never" class="latest-card">
          <template #header>
            <div class="card-header">
              <span>热销商品 Top 5</span>
              <span class="muted">销售潜力参考</span>
            </div>
          </template>
          <el-table :data="topProducts" size="large" style="width: 100%" v-loading="topProductsLoading">
            <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="price" label="售价" width="120" align="center">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="100" align="center" />
            <el-table-column prop="sale" label="销量" width="100" align="center" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
              <span class="muted">近 7 日</span>
            </div>
          </template>
          <div ref="userChartRef" class="chart-box"></div>
          <div class="alert-cards">
            <div class="alert-item">
              <div class="label">待发货订单</div>
              <div class="value">{{ pendingDeliverCount }}</div>
            </div>
            <div class="alert-item danger">
              <div class="label">异常订单</div>
              <div class="value">{{ exceptionCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="latest-card">
      <template #header>
        <div class="card-header">
          <span>最新 5 笔交易</span>
          <span class="muted">快速查看最新成交</span>
        </div>
      </template>
      <el-table :data="latestOrders" size="large" style="width: 100%" v-loading="latestLoading">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="receiverName" label="收货人" width="120" />
        <el-table-column label="金额" width="120" align="center">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/system'
import { getOrderList } from '@/api/order'
import { getCategoryTree } from '@/api/category'
import { getProductList } from '@/api/product'
import { CaretTop, CaretBottom, Goods, User, Document } from '@element-plus/icons-vue'

const salesChartRef = ref()
const categoryChartRef = ref()
const userChartRef = ref()
let salesChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null

const stats = reactive({
  totalSales: 0,
  totalOrders: 0,
  totalProducts: 0,
  totalUsers: 0,
  trendDates: [] as string[],
  trendValues: [] as number[]
})

const latestOrders = ref<any[]>([])
const latestLoading = ref(false)
const categoryData = ref<{ name: string; value: number }[]>([])
const topProducts = ref<any[]>([])
const topProductsLoading = ref(false)
const pendingDeliverCount = ref(0)
const exceptionCount = ref(0)

const formatNumber = (num: number) => {
  return Number(num).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const calcTrend = (values: number[]) => {
  if (!values || values.length < 2) {
    return { rate: '0.0', up: true }
  }
  const last = values[values.length - 1]
  const prev = values[values.length - 2]
  const diff = last - prev
  const rate = prev === 0 ? 0 : (diff / prev) * 100
  return { rate: Math.abs(rate).toFixed(1), up: diff >= 0 }
}

const trendMeta = computed(() => ({
  sales: calcTrend(stats.trendValues)
}))

const initSalesChart = (dates: string[], values: number[]) => {
  if (!salesChartRef.value) return
  salesChart = echarts.init(salesChartRef.value)

  const option = {
    color: ['#8db6ff'],
    animationDuration: 1200,
    animationEasing: 'cubicOut',
    animationDurationUpdate: 800,
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#cbd5f5' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#cbd5f5' },
      splitLine: { show: false }
    },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        lineStyle: { width: 3, color: '#8db6ff' },
        itemStyle: { color: '#8db6ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(141, 182, 255, 0.4)' },
            { offset: 1, color: 'rgba(141, 182, 255, 0.02)' }
          ])
        },
        data: values
      }
    ]
  }
  salesChart.setOption(option)
}

const initCategoryChart = (data: { name: string; value: number }[]) => {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  const option = {
    color: ['#8db6ff', '#a7d8f5', '#cbb6f6', '#a7e3c4', '#f2c6a0', '#f4e3a3'],
    animationDuration: 1200,
    animationEasing: 'cubicOut',
    animationDurationUpdate: 800,
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', left: 'center', textStyle: { color: '#64748b' } },
    series: [
      {
        name: '品类占比',
        type: 'pie',
        radius: ['45%', '70%'],
        itemStyle: { borderRadius: 12, borderColor: 'rgba(255,255,255,0.7)', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%' },
        emphasis: { scale: true, scaleSize: 8 },
        data
      }
    ]
  }
  categoryChart.setOption(option)
}

const initUserChart = (dates: string[], values: number[]) => {
  if (!userChartRef.value) return
  userChart = echarts.init(userChartRef.value)
  const option = {
    color: ['#95d5b2'],
    animationDuration: 1200,
    animationEasing: 'cubicOut',
    animationDurationUpdate: 800,
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#cbd5f5' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#cbd5f5' },
      splitLine: { show: false }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        lineStyle: { width: 3, color: '#95d5b2' },
        itemStyle: { color: '#95d5b2' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(149, 213, 178, 0.45)' },
            { offset: 1, color: 'rgba(149, 213, 178, 0.04)' }
          ])
        },
        data: values
      }
    ]
  }
  userChart.setOption(option)
}

const loadDashboard = async () => {
  const res: any = await getDashboardStats()
  const data = res.data
  Object.assign(stats, data)
  if (data.trendDates && data.trendValues) {
    stats.trendDates = data.trendDates
    stats.trendValues = data.trendValues
    initSalesChart(data.trendDates, data.trendValues)
    const userTrend = data.trendValues.map((value: number) => Math.max(1, Math.round(value / 100)))
    initUserChart(data.trendDates, userTrend)
  }
}

const loadLatestOrders = async () => {
  latestLoading.value = true
  try {
    const res: any = await getOrderList({ page: 1, size: 5 })
    latestOrders.value = res.data?.records || []
  } finally {
    latestLoading.value = false
  }
}

const loadCategoryShare = async () => {
  const res: any = await getCategoryTree()
  const list = res.data || []
  categoryData.value = list.map((item: any) => ({
    name: item.name,
    value: Math.max(1, item.productCount || item.children?.length || 1)
  }))
  if (categoryData.value.length) {
    initCategoryChart(categoryData.value)
  }
}

const loadTopProducts = async () => {
  topProductsLoading.value = true
  try {
    const res: any = await getProductList({ page: 1, size: 5 })
    topProducts.value = res.data?.records || []
  } finally {
    topProductsLoading.value = false
  }
}

const loadAlertCounts = async () => {
  const pendingRes: any = await getOrderList({ page: 1, size: 1, status: 1 })
  pendingDeliverCount.value = pendingRes.data?.total || 0
  const exceptionRes: any = await getOrderList({ page: 1, size: 1, status: 5 })
  exceptionCount.value = exceptionRes.data?.total || 0
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付',
    1: '待发货',
    2: '已发货',
    3: '已完成',
    4: '已取消',
    5: '售后中',
    6: '已退款'
  }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'info',
    5: 'danger',
    6: 'danger'
  }
  return map[status] || 'info'
}

const resizeChart = () => {
  salesChart?.resize()
  categoryChart?.resize()
  userChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', resizeChart)
  try {
    await Promise.all([loadDashboard(), loadLatestOrders(), loadCategoryShare(), loadTopProducts(), loadAlertCounts()])
  } catch (e) {
    console.error('加载看板失败', e)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  salesChart?.dispose()
  categoryChart?.dispose()
  userChart?.dispose()
})
</script>

<style scoped>
.dashboard-container { padding: 20px; display: flex; flex-direction: column; gap: 20px; }
.stats-row .el-card { height: 100%; }
.stat-card {
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
}
.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-label {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
}
.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
}
.stat-icon.sales { background: linear-gradient(135deg, #38bdf8, #3b82f6); }
.stat-icon.orders { background: linear-gradient(135deg, #f59e0b, #f97316); }
.stat-icon.products { background: linear-gradient(135deg, #22c55e, #10b981); }
.stat-icon.users { background: linear-gradient(135deg, #a855f7, #6366f1); }
.stat-foot {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.stat-foot.up { color: #16a34a; }
.stat-foot.down { color: #ef4444; }
.stat-foot.neutral { color: #64748b; }
.stat-foot .muted { color: #94a3b8; margin-left: 4px; }

.chart-card {
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
}
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.card-header .muted { font-size: 12px; color: #94a3b8; }
.chart-box { width: 100%; height: 320px; }

.alert-cards {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.alert-item {
  background: rgba(248, 250, 252, 0.7);
  border-radius: 12px;
  padding: 12px 14px;
  border: 1px solid rgba(226, 232, 240, 0.7);
  .label { font-size: 12px; color: #94a3b8; margin-bottom: 6px; }
  .value { font-size: 20px; font-weight: 700; color: #0f172a; }
  &.danger {
    border-color: rgba(254, 202, 202, 0.8);
    background: rgba(255, 241, 242, 0.7);
    .value { color: #ef4444; }
  }
}

.latest-card {
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
}
</style>
