<template>
  <div class="order-page">
    <div class="container">
      <el-card shadow="never" class="order-card">
        <template #header>
          <div class="card-header">
            <span>我的订单</span>
            <span class="header-tip">实时同步订单状态</span>
          </div>
        </template>

        <div class="summary-grid">
          <div class="summary-item">
            <div class="label">待支付</div>
            <div class="value">{{ statusStats.pending }}</div>
          </div>
          <div class="summary-item">
            <div class="label">待发货</div>
            <div class="value">{{ statusStats.toDeliver }}</div>
          </div>
          <div class="summary-item">
            <div class="label">已发货</div>
            <div class="value">{{ statusStats.shipped }}</div>
          </div>
          <div class="summary-item">
            <div class="label">已完成</div>
            <div class="value">{{ statusStats.completed }}</div>
          </div>
        </div>

        <div class="filter-row">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
          <el-select v-model="statusFilter" placeholder="订单状态" clearable style="width: 160px">
            <el-option label="待支付" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="售后中" :value="5" />
            <el-option label="已退款" :value="6" />
          </el-select>
          <el-button type="primary" @click="applyFilters">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>

        <el-tabs v-model="activeTab" class="status-tabs" @tab-click="handleTabClick">
          <el-tab-pane label="全部订单" name="-1" />
          <el-tab-pane label="待支付" name="0" />
          <el-tab-pane label="待发货" name="1" />
          <el-tab-pane label="已发货" name="2" />
          <el-tab-pane label="已完成" name="3" />
          <el-tab-pane label="售后/退款" name="5" />
        </el-tabs>

        <el-table :data="tableData" border stripe size="large" v-loading="loading">
          <el-table-column prop="orderNo" label="订单号" width="220" />

          <el-table-column label="商品信息" min-width="250">
            <template #default="{ row }: { row: Order }">
              <div v-if="row.items && row.items.length > 0" class="order-goods-preview">
                <el-image :src="row.items[0].mainImage" class="thumb" fit="cover" />
                <div class="info">
                  <div class="name">{{ row.items[0].productName }}</div>
                  <div class="more" v-if="row.items.length > 1">等 {{ row.items.length }} 件商品</div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="实付金额" width="150" align="center">
            <template #default="{ row }: { row: Order }">
              <span class="amount">¥{{ row.finalTotalAmount || row.totalAmount }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="下单时间" width="180" align="center" />

          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }: { row: Order }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="{ row }: { row: Order }">
              <template v-if="row.status === 0">
                <el-button type="primary" size="small" @click="handlePay(row)">去支付</el-button>
                <el-button type="info" size="small" link @click="handleCancel(row)">取消</el-button>
              </template>

              <el-button v-if="row.status === 1" type="danger" size="small" link @click="openRefund(row)">申请退款</el-button>

              <el-button v-if="row.status === 2" type="success" size="small" @click="handleReceive(row)">确认收货</el-button>

              <el-button v-if="row.status === 3" type="warning" size="small" link @click="openReviewDialog(row)">评价</el-button>

              <el-button size="small" link @click="handleViewDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <EmptyState v-if="!loading && tableData.length === 0" description="还没有订单，去首页逛逛吧" action-text="去首页" action-to="/">
          <template #image>
            <svg viewBox="0 0 220 160" aria-hidden="true" class="empty-illustration">
              <defs>
                <linearGradient id="orderGradient" x1="0" x2="1" y1="0" y2="1">
                  <stop offset="0%" stop-color="#f97316" />
                  <stop offset="100%" stop-color="#fb7185" />
                </linearGradient>
              </defs>
              <rect x="18" y="20" width="184" height="120" rx="18" fill="#fef3c7" />
              <rect x="46" y="48" width="128" height="70" rx="12" fill="url(#orderGradient)" opacity="0.9" />
              <path d="M70 66h80M70 82h60M70 98h40" stroke="#fff" stroke-width="6" stroke-linecap="round" />
            </svg>
          </template>
          <div class="recommendations" v-if="recommendedProducts.length">
            <div class="recommend-title">为你推荐</div>
            <div class="recommend-grid">
              <div
                v-for="item in recommendedProducts"
                :key="item.id"
                class="recommend-item"
                @click="router.push(`/product/${item.id}`)"
              >
                <el-image :src="item.mainImage" fit="cover" class="recommend-image" />
                <div class="recommend-name">{{ item.name }}</div>
                <div class="recommend-price">¥{{ item.price }}</div>
              </div>
            </div>
          </div>
        </EmptyState>

        <div class="pagination-box" v-if="total > 0">
          <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              v-model:current-page="queryParams.page"
              :page-size="queryParams.size"
              @current-change="loadData"
          />
        </div>
      </el-card>

      <el-dialog v-model="detailVisible" title="订单详情" width="600px" destroy-on-close>
        <div v-if="currentOrderVo" class="detail-box">
          <div class="section-title">订单进度</div>
          <el-steps :active="orderStepActive" align-center>
            <el-step title="下单" :description="currentOrderVo?.order?.createTime || '待更新'" />
            <el-step title="支付" :description="currentOrderVo?.order?.payTime || '待支付'" />
            <el-step title="发货" :description="currentOrderVo?.order?.deliverTime || '待发货'" />
            <el-step title="收货" :description="currentOrderVo?.order?.receiveTime || '待收货'" />
          </el-steps>

          <div class="section-title">收货信息</div>
          <div class="info-grid">
            <div class="item"><span class="label">收货人：</span>{{ currentOrderVo?.order?.receiverName }}</div>
            <div class="item"><span class="label">联系电话：</span>{{ currentOrderVo?.order?.receiverPhone }}</div>
            <div class="item full"><span class="label">收货地址：</span>{{ currentOrderVo?.order?.receiverAddress }}</div>
          </div>

          <div v-if="currentOrderVo?.order?.refundReason" class="refund-info">
            <span class="label">退款理由：</span>{{ currentOrderVo.order.refundReason }}
          </div>

          <div class="section-title" style="margin-top: 25px;">商品清单</div>
          <div class="goods-list">
            <div v-for="item in (currentOrderVo?.items || [])" :key="item.id" class="product-item">
              <el-image :src="item.mainImage" class="p-img" fit="cover" />
              <div class="p-info">
                <div class="name">{{ item.productName }}</div>
                <div class="price-row">
                  <span class="u-price">¥{{ item.currentUnitPrice }}</span>
                  <span class="x">x</span>
                  <span class="qty">{{ item.quantity }}</span>
                </div>
              </div>
              <div class="p-total">¥{{ item.totalPrice }}</div>
            </div>
          </div>

          <div class="price-summary">
            <div class="row"><span>商品总额：</span>¥ {{ currentOrderVo?.order?.originalTotalAmount || currentOrderVo?.order?.totalAmount }}</div>
            <div class="row coupon"><span>优惠券抵扣：</span>- ¥ {{ currentOrderVo?.order?.couponDiscountAmount || 0 }}</div>
            <div class="row total"><span>实付金额：</span>¥ {{ currentOrderVo?.order?.finalTotalAmount || currentOrderVo?.order?.totalAmount }}</div>
          </div>
        </div>
      </el-dialog>

      <el-dialog v-model="reviewVisible" title="评价商品" width="500px">
        <el-form :model="reviewForm" label-width="80px">
          <el-form-item label="选择商品" required>
            <el-select v-model="reviewForm.productId" placeholder="请选择要评价的商品" style="width: 100%">
              <el-option
                  v-for="item in reviewOrderItems"
                  :key="item.productId"
                  :label="item.productName"
                  :value="item.productId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="评分" required>
            <el-rate v-model="reviewForm.rating" show-text />
          </el-form-item>
          <el-form-item label="心得体会">
            <el-input
                v-model="reviewForm.content"
                type="textarea"
                :rows="4"
                placeholder="商品质量如何？快写下你的使用感受吧！"
                maxlength="200"
                show-word-limit
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reviewVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReviewForm">提交评价</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="refundVisible" title="申请退款" width="450px">
        <el-form :model="refundForm" label-width="80px" label-position="top">
          <el-alert title="退款申请提交后，商家将在 24 小时内处理" type="info" :closable="false" style="margin-bottom: 15px" />
          <el-form-item label="退款原因" required>
            <el-select v-model="refundForm.reason" placeholder="请选择真实原因" style="width: 100%">
              <el-option label="拍错/多拍/不想要" value="拍错/多拍/不想要" />
              <el-option label="缺货/断货" value="缺货/断货" />
              <el-option label="地址/电话填写错误" value="地址/电话填写错误" />
              <el-option label="商品质量问题" value="商品质量问题" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="refundVisible = false">暂不退款</el-button>
          <el-button type="danger" @click="submitRefund">确认申请</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getMyOrders, receiveOrder, cancelOrder, submitReview, getOrderDetailByNo, applyRefund, searchProducts } from '@/api/store'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'

// 接口定义：确保与 Template 使用的字段一致
interface OrderItem {
  id: number
  productId: number
  productName: string
  mainImage: string
  quantity: number
  currentUnitPrice: number
  totalPrice: number
}

interface Order {
  id: number
  orderNo: string
  status: number
  totalAmount: number
  finalTotalAmount?: number
  createTime: string
  items?: OrderItem[]
  [key: string]: any
}

interface OrderVO {
  order: {
    id: number
    orderNo: string
    status: number
    totalAmount: number
    finalTotalAmount: number
    originalTotalAmount?: number
    couponDiscountAmount?: number
    receiverName: string
    receiverPhone: string
    receiverAddress: string
    createTime: string
    refundReason?: string
    payTime?: string
    deliverTime?: string
    receiveTime?: string
  }
  items: OrderItem[]
}

const router = useRouter()
const tableData = ref<Order[]>([])
const loading = ref(false)
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: null as number | null, startDate: '', endDate: '' })
const activeTab = ref('-1')
const dateRange = ref<string[]>([])
const statusFilter = ref<number | null>(null)
const recommendedProducts = ref<any[]>([])

// 详情 & 评价
const detailVisible = ref(false)
const currentOrderVo = ref<OrderVO | null>(null)
const reviewVisible = ref(false)
const reviewOrderItems = ref<OrderItem[]>([])
const reviewForm = reactive({ orderId: undefined, productId: undefined, rating: 5, content: '' })

// 退款
const refundVisible = ref(false)
const refundForm = reactive({ orderNo: '', reason: '' })

const loadData = async () => {
  loading.value = true
  try {
    if (dateRange.value.length === 2) {
      queryParams.startDate = dateRange.value[0]
      queryParams.endDate = dateRange.value[1]
    } else {
      queryParams.startDate = ''
      queryParams.endDate = ''
    }
    // 使用 any 绕过分页结构类型检查，或定义 PageResult 接口
    const res: any = await getMyOrders(queryParams)
    // [修复] records 变量解析修复：确保 res 存在
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadRecommendations = async () => {
  try {
    const res: any = await searchProducts({ page: 1, size: 4 })
    recommendedProducts.value = res.records || []
  } catch (e) {
    recommendedProducts.value = []
  }
}

const statusStats = computed(() => {
  const counts = { pending: 0, toDeliver: 0, shipped: 0, completed: 0 }
  tableData.value.forEach((order) => {
    if (order.status === 0) counts.pending += 1
    if (order.status === 1) counts.toDeliver += 1
    if (order.status === 2) counts.shipped += 1
    if (order.status === 3) counts.completed += 1
  })
  return counts
})

const handleTabClick = (tab?: any) => {
  const tabName = tab?.paneName ?? tab?.props?.name ?? activeTab.value
  queryParams.status = tabName === '-1' ? null : Number(tabName)
  statusFilter.value = queryParams.status
  queryParams.page = 1
  loadData()
}

const applyFilters = () => {
  queryParams.page = 1
  queryParams.status = statusFilter.value
  activeTab.value = statusFilter.value === null ? '-1' : String(statusFilter.value)
  loadData()
}

const resetFilters = () => {
  dateRange.value = []
  statusFilter.value = null
  activeTab.value = '-1'
  applyFilters()
}

const orderStepActive = computed(() => {
  const status = currentOrderVo.value?.order?.status ?? 0
  if (status === 0) return 1
  if (status === 1) return 2
  if (status === 2) return 3
  if (status >= 3) return 4
  return 1
})

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消', 5: '售后中', 6: '已退款'
  }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = {
    0: 'danger', 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: '', 6: 'info'
  }
  return map[status] || 'info'
}

const handlePay = (row: Order) => {
  router.push({
    path: '/pay',
    query: {
      tradeNo: row.orderNo,
      amount: row.finalTotalAmount || row.totalAmount
    }
  })
}

const handleCancel = (row: Order) => {
  ElMessageBox.confirm('确定要取消该订单吗? 取消后无法恢复。', '提示', {
    type: 'warning',
    confirmButtonText: '确认取消'
  }).then(async () => {
    await cancelOrder(row.orderNo)
    ElMessage.success('订单已取消')
    await loadData()
  })
}

const handleReceive = (row: Order) => {
  ElMessageBox.confirm('请确保您已收到商品且无质量问题。', '确认收货', {
    type: 'success',
    confirmButtonText: '确认收货'
  }).then(async () => {
    await receiveOrder(row.orderNo)
    ElMessage.success('交易完成！')
    await loadData()
  })
}

const handleViewDetail = async (row: Order) => {
  try {
    // [修复] 移除冗余的局部变量 res，直接赋值给 currentOrderVo
    currentOrderVo.value = await getOrderDetailByNo(row.orderNo) as unknown as OrderVO
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const openRefund = (row: Order) => {
  refundForm.orderNo = row.orderNo
  refundForm.reason = ''
  refundVisible.value = true
}

const submitRefund = async () => {
  if (!refundForm.reason) return ElMessage.warning('请选择退款原因')
  try {
    await applyRefund(refundForm)
    ElMessage.success('申请提交成功，请等待商家审核')
    refundVisible.value = false
    await loadData()
  } catch (e) {
    // request.ts 已处理错误
  }
}

const openReviewDialog = async (row: Order) => {
  reviewForm.orderId = row.id as any
  reviewForm.productId = undefined
  reviewForm.content = ''
  reviewForm.rating = 5

  try {
    // 这里需要 res 变量来判断逻辑，故保留，与 handleViewDetail 不同
    const res = await getOrderDetailByNo(row.orderNo) as unknown as OrderVO
    reviewOrderItems.value = res.items || []
    if (res.items.length > 0) reviewForm.productId = res.items[0].productId as any
    reviewVisible.value = true
  } catch (e) {
    ElMessage.error('无法加载订单商品')
  }
}

const submitReviewForm = async () => {
  if (!reviewForm.productId) return ElMessage.warning('请选择要评价的商品')
  try {
    await submitReview(reviewForm)
    ElMessage.success('评价发布成功！')
    reviewVisible.value = false
  } catch (e) {
    // 错误处理
  }
}

onMounted(() => { loadData() })
onMounted(() => { loadRecommendations() })
</script>

<style scoped lang="scss">
.order-page { padding: 20px 0; background: #f5f7fa; min-height: 80vh; }
.container { width: 1200px; margin: 0 auto; }
.order-card { border-radius: 12px; }
.card-header {
  font-weight: bold;
  font-size: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  .header-tip { font-size: 12px; color: #94a3b8; font-weight: normal; }
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 10px;
  .summary-item {
    background: #f8fafc;
    padding: 14px 16px;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    .label { font-size: 12px; color: #94a3b8; margin-bottom: 6px; }
    .value { font-size: 20px; font-weight: 700; color: #0f172a; }
  }
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 10px;
}

.status-tabs { margin: 10px 0 20px; }

.pagination-box { margin-top: 20px; display: flex; justify-content: flex-end; }

/* 商品预览 */
.order-goods-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  .thumb { width: 50px; height: 50px; border-radius: 4px; border: 1px solid #f0f0f0; }
  .info {
    font-size: 13px;
    .name { font-weight: bold; margin-bottom: 4px; }
    .more { color: #999; font-size: 12px; }
  }
}
.amount { color: #f56c6c; font-weight: bold; }

.recommendations {
  margin-top: 20px;
  text-align: left;
  .recommend-title {
    font-weight: 600;
    margin-bottom: 10px;
    color: #0f172a;
  }
  .recommend-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
  .recommend-item {
    background: #fff;
    border-radius: 10px;
    padding: 10px;
    border: 1px solid #e2e8f0;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
    }
  }
  .recommend-image { width: 100%; height: 90px; border-radius: 8px; }
  .recommend-name {
    font-size: 12px;
    margin-top: 8px;
    color: #334155;
  }
  .recommend-price {
    margin-top: 4px;
    color: #ef4444;
    font-weight: 600;
  }
}


/* 详情弹窗 */
.detail-box {
  .section-title {
    font-weight: bold;
    font-size: 15px;
    margin-bottom: 15px;
    border-left: 4px solid #409EFF;
    padding-left: 10px;
  }
  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 15px;
    background: #fcfcfc;
    padding: 15px;
    border-radius: 6px;
    .item {
      font-size: 14px;
      color: #333;
      .label { color: #888; margin-right: 5px; }
      &.full { grid-column: span 2; }
    }
  }
  .refund-info {
    margin-top: 15px;
    background: #fef0f0;
    padding: 10px;
    border-radius: 4px;
    color: #f56c6c;
    font-size: 13px;
    .label { font-weight: bold; }
  }
}

.goods-list {
  border: 1px solid #eee;
  border-radius: 6px;
  .product-item {
    display: flex;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid #eee;
    &:last-child { border-bottom: none; }
    .p-img { width: 60px; height: 60px; border-radius: 4px; margin-right: 15px; border: 1px solid #f0f0f0; }
    .p-info {
      flex: 1;
      .name { font-weight: bold; font-size: 14px; margin-bottom: 6px; }
      .price-row { font-size: 13px; color: #666; }
    }
    .p-total { font-weight: bold; color: #333; font-size: 15px; }
  }
}

.price-summary {
  margin-top: 20px;
  text-align: right;
  .row {
    margin-bottom: 8px;
    font-size: 14px;
    &.coupon { color: #f56c6c; }
    &.total {
      margin-top: 15px;
      padding-top: 15px;
      border-top: 1px dashed #eee;
      font-size: 18px;
      font-weight: bold;
      color: #f56c6c;
    }
  }
}
</style>
