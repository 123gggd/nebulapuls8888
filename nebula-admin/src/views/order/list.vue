<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="filter-bar">
      <el-form inline :model="queryParams" class="filter-form">
        <el-form-item label="订单号">
          <el-input
              v-model="queryParams.orderNo"
              placeholder="请输入订单号"
              clearable
              @keyup.enter="handleSearch"
              style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <div class="filter-actions">
        <el-button class="icon-action" circle :icon="Search" @click="handleSearch" />
        <el-button class="icon-action" circle :icon="Refresh" @click="resetQuery" />
        <el-button class="ghost-pill" @click="applyQuickRange('today')">今日</el-button>
        <el-button class="ghost-pill" @click="applyQuickRange('week')">本周</el-button>
        <el-popover placement="bottom-end" width="240" trigger="click">
          <template #reference>
            <el-button class="icon-action" circle :icon="Filter" />
          </template>
          <div class="advanced-panel">
            <div class="advanced-title">高级筛选</div>
            <el-form label-position="top">
              <el-form-item label="订单状态">
                <el-select v-model="queryParams.status" placeholder="全部" clearable>
                  <el-option label="待支付" :value="0" />
                  <el-option label="待发货" :value="1" />
                  <el-option label="已发货" :value="2" />
                  <el-option label="已完成" :value="3" />
                  <el-option label="已取消" :value="4" />
                  <el-option label="售后中" :value="5" />
                  <el-option label="已退款" :value="6" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- 列表内容 -->
    <el-card shadow="never" class="table-card" style="margin-top: 20px;">
      <!-- 状态 Tabs -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="-1"></el-tab-pane>
        <el-tab-pane label="待发货" name="1"></el-tab-pane>
        <el-tab-pane label="已发货" name="2"></el-tab-pane>
        <el-tab-pane label="售后/退款" name="5"></el-tab-pane>
        <el-tab-pane label="已完成" name="3"></el-tab-pane>
      </el-tabs>

      <div class="stats-row">
        <div class="stat-item">
          <div class="label">待支付</div>
          <div class="value">{{ statusStats.pending }}</div>
        </div>
        <div class="stat-item">
          <div class="label">待发货</div>
          <div class="value">{{ statusStats.toDeliver }}</div>
        </div>
        <div class="stat-item">
          <div class="label">售后中</div>
          <div class="value">{{ statusStats.refunding }}</div>
        </div>
        <div class="stat-item">
          <div class="label">已完成</div>
          <div class="value">{{ statusStats.completed }}</div>
        </div>
      </div>

      <div class="status-chart">
        <div class="chart-title">订单状态分布</div>
        <div class="chart-bars">
          <div class="bar-row" v-for="item in statusBars" :key="item.label">
            <span class="bar-label">{{ item.label }}</span>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: item.percent + '%' }"></div>
            </div>
            <span class="bar-value">{{ item.count }}</span>
          </div>
        </div>
      </div>

      <div class="table-toolbar">
        <div class="selected-info">已选择 {{ selectedRows.length }} 笔订单</div>
        <div class="toolbar-actions">
          <el-button type="primary" :disabled="selectedRows.length === 0" @click="openBatchDeliver">批量发货</el-button>
          <el-button @click="exportOrders">导出</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" :row-class-name="getRowClassName" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />

        <el-table-column label="订单金额" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="收货信息" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div>{{ row.receiverName }} <span style="color: #999">|</span> {{ row.receiverPhone }}</div>
            <div style="font-size: 12px; color: #909399;">{{ row.receiverAddress }}</div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <span class="status-pill" :class="`status-${row.status}`">
              <span class="dot"></span>
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="下单时间" width="170" align="center" />

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button class="icon-action" text circle :icon="View" @click="handleDetail(row)" />
            <el-button
                v-if="row.status === 1"
                class="icon-action"
                text
                circle
                :icon="Upload"
                @click="openDeliverDialog(row)"
            />
            <el-button
                v-if="row.status === 5"
                class="icon-action warning"
                text
                circle
                :icon="Select"
                @click="openAuditDialog(row)"
            />
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无订单数据" />

      <!-- 分页 -->
      <div style="margin-top: 20px; text-align: right;">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog title="订单详情" v-model="detailVisible" width="800px">
      <div v-if="detailData.order" class="detail-container">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ detailData.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailData.order.status)">
              {{ getStatusText(detailData.order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付金额">¥{{ detailData.order.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detailData.order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailData.order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detailData.order.receiverAddress }}</el-descriptions-item>

          <template v-if="detailData.order.logisticsCompany">
            <el-descriptions-item label="物流公司">{{ detailData.order.logisticsCompany }}</el-descriptions-item>
            <el-descriptions-item label="物流单号">{{ detailData.order.trackingNo }}</el-descriptions-item>
          </template>
        </el-descriptions>

        <!-- 售后信息 -->
        <div v-if="detailData.order.status === 5 || detailData.order.status === 6" style="margin-top: 20px;">
          <el-alert
              :title="'退款原因: ' + detailData.order.refundReason"
              type="warning"
              :closable="false"
              show-icon
          />
        </div>

        <!-- 商品清单 -->
        <div style="margin-top: 20px; font-weight: bold; margin-bottom: 10px;">商品清单</div>
        <el-table :data="detailData.items" border style="width: 100%">
          <el-table-column label="商品图片" width="80" align="center">
            <template #default="{ row }">
              <el-image
                  :src="row.mainImage"
                  style="width: 50px; height: 50px"
                  fit="cover"
                  preview-teleported
                  :preview-src-list="[row.mainImage]"
              />
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
          <el-table-column prop="currentUnitPrice" label="单价" width="100" align="center">
            <template #default="{ row }">¥{{ row.currentUnitPrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column prop="totalPrice" label="小计" width="100" align="center">
            <template #default="{ row }">
              <span style="color: #f56c6c;">¥{{ row.totalPrice }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog title="订单发货" v-model="deliverVisible" width="500px">
      <el-form :model="deliverForm" label-width="80px">
        <el-form-item label="订单号">
          <el-input v-model="deliverForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="物流公司">
          <el-select v-model="deliverForm.logisticsCompany" placeholder="请选择物流" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="邮政EMS" value="邮政EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="deliverForm.trackingNo" placeholder="请输入运单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDeliver">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 批量发货弹窗 -->
    <el-dialog title="批量发货" v-model="batchVisible" width="700px">
      <el-form label-width="80px">
        <el-form-item label="物流公司">
          <el-select v-model="batchLogisticsCompany" placeholder="请选择物流" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="邮政EMS" value="邮政EMS" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="batchDeliverList" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column label="物流单号">
          <template #default="{ row }">
            <el-input v-model="row.trackingNo" placeholder="请输入运单号" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchDeliver">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 售后审核弹窗 -->
    <el-dialog title="售后审核" v-model="auditVisible" width="500px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="订单号">
          <el-input v-model="auditForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="退款原因" v-if="currentOrder">
          <el-input type="textarea" :model-value="currentOrder.refundReason" disabled :rows="2"/>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.pass">
            <el-radio :value="true">同意退款</el-radio>
            <el-radio :value="false">拒绝申请</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="拒绝理由" v-if="!auditForm.pass">
          <el-input v-model="auditForm.remark" type="textarea" placeholder="请填写拒绝理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">提交结果</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Filter, Refresh, Search, Select, Upload, View } from '@element-plus/icons-vue'
import { getOrderList, getOrderDetail, deliverOrder, auditRefund } from '@/api/order'

// --- 列表逻辑 ---
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const activeTab = ref('-1')
const dateRange = ref<string[]>([])
const selectedRows = ref<any[]>([])

const queryParams = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  status: null as number | null
})

const loadData = async () => {
  loading.value = true
  try {
    if (dateRange.value.length === 2) {
      queryParams.startDate = dateRange.value[0]
      queryParams.endDate = dateRange.value[1]
    } else {
      delete (queryParams as any).startDate
      delete (queryParams as any).endDate
    }
    const res: any = await getOrderList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const statusStats = computed(() => {
  const stats = { pending: 0, toDeliver: 0, refunding: 0, completed: 0 }
  tableData.value.forEach((order: any) => {
    if (order.status === 0) stats.pending += 1
    if (order.status === 1) stats.toDeliver += 1
    if (order.status === 5 || order.status === 6) stats.refunding += 1
    if (order.status === 3) stats.completed += 1
  })
  return stats
})

const statusBars = computed(() => {
  const stats = statusStats.value
  const max = Math.max(stats.pending, stats.toDeliver, stats.refunding, stats.completed, 1)
  return [
    { label: '待支付', count: stats.pending, percent: (stats.pending / max) * 100 },
    { label: '待发货', count: stats.toDeliver, percent: (stats.toDeliver / max) * 100 },
    { label: '售后中', count: stats.refunding, percent: (stats.refunding / max) * 100 },
    { label: '已完成', count: stats.completed, percent: (stats.completed / max) * 100 }
  ]
})

const handleSearch = () => { queryParams.page = 1; loadData() }
const resetQuery = () => {
  queryParams.orderNo = ''
  queryParams.status = null
  activeTab.value = '-1'
  dateRange.value = []
  handleSearch()
}

const applyQuickRange = (type: 'today' | 'week') => {
  const now = new Date()
  if (type === 'today') {
    const today = now.toISOString().slice(0, 10)
    dateRange.value = [today, today]
  } else {
    const day = now.getDay() || 7
    const start = new Date(now)
    start.setDate(now.getDate() - day + 1)
    const end = new Date(start)
    end.setDate(start.getDate() + 6)
    dateRange.value = [start.toISOString().slice(0, 10), end.toISOString().slice(0, 10)]
  }
  handleSearch()
}

const handleTabClick = (tab?: any) => {
  const tabName = tab?.paneName ?? tab?.props?.name ?? activeTab.value
  queryParams.status = tabName === '-1' ? null : Number(tabName)
  queryParams.page = 1
  loadData()
}

// --- 详情逻辑 ---
const detailVisible = ref(false)
const detailData = ref<{ order: any, items: any[] }>({ order: null, items: [] })

const handleDetail = async (row: any) => {
  try {
    const res: any = await getOrderDetail(row.id)
    // 后端返回 Map: { "order": {}, "items": [] }
    detailData.value = res.data
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// --- 发货逻辑 ---
const deliverVisible = ref(false)
const deliverForm = reactive({
  orderNo: '',
  logisticsCompany: '顺丰速运',
  trackingNo: ''
})

const openDeliverDialog = (row: any) => {
  deliverForm.orderNo = row.orderNo
  deliverForm.trackingNo = ''
  deliverVisible.value = true
}

const confirmDeliver = async () => {
  if (!deliverForm.trackingNo) {
    ElMessage.warning('请输入物流单号')
    return
  }
  await deliverOrder(deliverForm)
  ElMessage.success('发货成功')
  deliverVisible.value = false
  loadData()
}

const batchVisible = ref(false)
const batchLogisticsCompany = ref('顺丰速运')
const batchDeliverList = ref<{ orderNo: string; trackingNo: string }[]>([])

const openBatchDeliver = () => {
  batchDeliverList.value = selectedRows.value.map((row: any) => ({ orderNo: row.orderNo, trackingNo: '' }))
  batchVisible.value = true
}

const confirmBatchDeliver = async () => {
  if (batchDeliverList.value.some((item) => !item.trackingNo)) {
    ElMessage.warning('请填写所有物流单号')
    return
  }
  await Promise.all(
    batchDeliverList.value.map((item) =>
      deliverOrder({
        orderNo: item.orderNo,
        logisticsCompany: batchLogisticsCompany.value,
        trackingNo: item.trackingNo
      })
    )
  )
  ElMessage.success('批量发货成功')
  batchVisible.value = false
  loadData()
}

// --- 审核逻辑 ---
const auditVisible = ref(false)
const currentOrder = ref<any>(null)
const auditForm = reactive({
  orderNo: '',
  pass: true,
  remark: ''
})

const openAuditDialog = (row: any) => {
  currentOrder.value = row
  auditForm.orderNo = row.orderNo
  auditForm.pass = true
  auditForm.remark = ''
  auditVisible.value = true
}

const confirmAudit = async () => {
  if (!auditForm.pass && !auditForm.remark) {
    ElMessage.warning('拒绝申请时必须填写理由')
    return
  }
  await auditRefund(auditForm)
  ElMessage.success(auditForm.pass ? '已同意退款' : '已拒绝退款申请')
  auditVisible.value = false
  loadData()
}

const exportOrders = () => {
  const ids = selectedRows.value.map((row: any) => row.id).join(',')
  const query = ids ? `?ids=${ids}` : ''
  window.open(`/api/admin/order/export${query}`, '_blank')
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const getRowClassName = () => 'floating-row'

// --- 状态工具 ---
const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消', 5: '售后中', 6: '已退款'
  }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  if (status === 3) return 'success'
  if (status === 0 || status === 1) return 'warning'
  if (status === 2) return 'primary'
  if (status === 5) return 'danger'
  return 'info'
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.12);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
  align-items: center;
  flex: 1;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ghost-pill {
  border-radius: 999px;
  padding: 0 14px;
  height: 34px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  background: rgba(255, 255, 255, 0.6);
}

.icon-action {
  opacity: 0.6;
  transition: all 0.2s ease;
}

.icon-action:hover {
  opacity: 1;
  color: #409eff;
}

.icon-action.warning:hover {
  color: #f59e0b;
}

.advanced-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.advanced-title {
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 6px;
}

.detail-container {
  padding: 10px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px 16px;
  .label {
    font-size: 12px;
    color: #94a3b8;
    margin-bottom: 6px;
  }
  .value {
    font-size: 20px;
    font-weight: 700;
    color: #0f172a;
  }
}

.status-chart {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  .chart-title {
    font-weight: 600;
    margin-bottom: 12px;
    color: #0f172a;
  }
  .chart-bars {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  .bar-row {
    display: grid;
    grid-template-columns: 70px 1fr 40px;
    align-items: center;
    gap: 12px;
  }
  .bar-track {
    background: #e2e8f0;
    border-radius: 999px;
    height: 8px;
    overflow: hidden;
  }
  .bar-fill {
    height: 100%;
    background: linear-gradient(90deg, #60a5fa, #3b82f6);
  }
  .bar-label {
    font-size: 12px;
    color: #64748b;
  }
  .bar-value {
    font-size: 12px;
    text-align: right;
    color: #0f172a;
  }
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0 16px;
  .selected-info {
    font-size: 12px;
    color: #64748b;
  }
  .toolbar-actions {
    display: flex;
    gap: 10px;
  }
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  color: #64748b;
  background: rgba(255, 255, 255, 0.6);
}

.status-pill .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #94a3b8;
  box-shadow: 0 0 6px rgba(148, 163, 184, 0.8);
}

.status-pill.status-1 .dot {
  background: #22c55e;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.8);
}

.status-pill.status-3 .dot {
  background: #38bdf8;
  box-shadow: 0 0 8px rgba(56, 189, 248, 0.8);
}

.status-pill.status-5 .dot,
.status-pill.status-6 .dot {
  background: #f97316;
  box-shadow: 0 0 8px rgba(249, 115, 22, 0.8);
}

.status-pill.status-4 .dot {
  background: #94a3b8;
}

:deep(.el-table) {
  --el-table-border-color: transparent;
  background: transparent;
}

:deep(.el-table__header th) {
  background: transparent;
  color: #475569;
  font-weight: 600;
  font-size: 12px;
}

:deep(.el-table__body) {
  border-collapse: separate;
  border-spacing: 0 12px;
}

:deep(.floating-row td) {
  background: rgba(255, 255, 255, 0.75);
  border-top: 1px solid rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

:deep(.floating-row:hover td) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
}

:deep(.floating-row td:first-child) {
  border-radius: 12px 0 0 12px;
}

:deep(.floating-row td:last-child) {
  border-radius: 0 12px 12px 0;
}
</style>
