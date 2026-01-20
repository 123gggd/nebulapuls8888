<template>
  <div class="order-page">
    <div class="container">
      <el-card shadow="never" class="order-card">
        <template #header>
          <div class="card-header">
            <span>My Orders</span>
            <span class="header-tip">Order status synced in real time</span>
          </div>
        </template>

        <div class="summary-grid">
          <div class="summary-item">
            <div class="label">Pending payment</div>
            <div class="value">{{ statusStats.pending }}</div>
          </div>
          <div class="summary-item">
            <div class="label">Awaiting shipment</div>
            <div class="value">{{ statusStats.toDeliver }}</div>
          </div>
          <div class="summary-item">
            <div class="label">Shipped</div>
            <div class="value">{{ statusStats.shipped }}</div>
          </div>
          <div class="summary-item">
            <div class="label">Completed</div>
            <div class="value">{{ statusStats.completed }}</div>
          </div>
        </div>

        <div class="filter-row">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="to"
            start-placeholder="Start date"
            end-placeholder="End date"
            value-format="YYYY-MM-DD"
          />
          <el-select v-model="statusFilter" placeholder="Order status" clearable style="width: 160px">
            <el-option label="Pending payment" :value="0" />
            <el-option label="Awaiting shipment" :value="1" />
            <el-option label="Shipped" :value="2" />
            <el-option label="Completed" :value="3" />
            <el-option label="After-sales" :value="5" />
            <el-option label="Refunded" :value="6" />
          </el-select>
          <el-button type="primary" @click="applyFilters">Filter</el-button>
          <el-button @click="resetFilters">Reset</el-button>
        </div>

        <el-tabs v-model="activeTab" class="status-tabs" @tab-click="handleTabClick">
          <el-tab-pane label="All orders" name="-1" />
          <el-tab-pane label="Pending payment" name="0" />
          <el-tab-pane label="Awaiting shipment" name="1" />
          <el-tab-pane label="Shipped" name="2" />
          <el-tab-pane label="Completed" name="3" />
          <el-tab-pane label="After-sales/Refund" name="5" />
        </el-tabs>

        <el-table :data="tableData" border stripe size="large" v-loading="loading">
          <el-table-column prop="orderNo" label="Order No." width="220" />

          <el-table-column label="Product Info" min-width="250">
            <template #default="{ row }: { row: Order }">
              <div v-if="row.items && row.items.length > 0" class="order-goods-preview">
                <el-image :src="row.items[0].mainImage" class="thumb" fit="cover" />
                <div class="info">
                  <div class="name">{{ row.items[0].productName }}</div>
                  <div class="more" v-if="row.items.length > 1">{{ row.items.length }} items total</div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Amount Paid" width="150" align="center">
            <template #default="{ row }: { row: Order }">
              <span class="amount">¥{{ row.finalTotalAmount || row.totalAmount }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="Order time" width="180" align="center" />

          <el-table-column label="Status" width="100" align="center">
            <template #default="{ row }: { row: Order }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Actions" width="200" align="center" fixed="right">
            <template #default="{ row }: { row: Order }">
              <template v-if="row.status === 0">
                <el-button type="primary" size="small" @click="handlePay(row)">Pay now</el-button>
                <el-button type="info" size="small" link @click="handleCancel(row)">Cancel</el-button>
              </template>

              <el-button v-if="row.status === 1" type="danger" size="small" link @click="openRefund(row)">Request refund</el-button>

              <el-button v-if="row.status === 2" type="success" size="small" @click="handleReceive(row)">Confirm receipt</el-button>

              <el-button v-if="row.status === 3" type="warning" size="small" link @click="openReviewDialog(row)">Review</el-button>

              <el-button size="small" link @click="handleViewDetail(row)">Details</el-button>
            </template>
          </el-table-column>
        </el-table>

        <EmptyState v-if="!loading && tableData.length === 0" description="No orders yet. Visit the homepage to start shopping." action-text="Go to home" action-to="/">
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
            <div class="recommend-title">Recommended for you</div>
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

      <el-dialog v-model="detailVisible" title="Order Details" width="600px" destroy-on-close>
        <div v-if="currentOrderVo" class="detail-box">
          <div class="section-title">Order progress</div>
          <el-steps :active="orderStepActive" align-center>
            <el-step title="Placed" :description="currentOrderVo?.order?.createTime || 'Pending'" />
            <el-step title="Paid" :description="currentOrderVo?.order?.payTime || 'Pending payment'" />
            <el-step title="Shipped" :description="currentOrderVo?.order?.deliverTime || 'Awaiting shipment'" />
            <el-step title="Received" :description="currentOrderVo?.order?.receiveTime || 'Awaiting receipt'" />
          </el-steps>

          <div class="section-title">Shipping info</div>
          <div class="info-grid">
            <div class="item"><span class="label">Recipient:</span>{{ currentOrderVo?.order?.receiverName }}</div>
            <div class="item"><span class="label">Phone:</span>{{ currentOrderVo?.order?.receiverPhone }}</div>
            <div class="item full"><span class="label">Address:</span>{{ currentOrderVo?.order?.receiverAddress }}</div>
          </div>

          <div v-if="currentOrderVo?.order?.refundReason" class="refund-info">
            <span class="label">Refund reason:</span>{{ currentOrderVo.order.refundReason }}
          </div>

          <div class="section-title" style="margin-top: 25px;">Item list</div>
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
            <div class="row"><span>Items total:</span>¥ {{ currentOrderVo?.order?.originalTotalAmount || currentOrderVo?.order?.totalAmount }}</div>
            <div class="row coupon"><span>Coupon discount:</span>- ¥ {{ currentOrderVo?.order?.couponDiscountAmount || 0 }}</div>
            <div class="row total"><span>Amount paid:</span>¥ {{ currentOrderVo?.order?.finalTotalAmount || currentOrderVo?.order?.totalAmount }}</div>
          </div>
        </div>
      </el-dialog>

      <el-dialog v-model="reviewVisible" title="Review Product" width="500px">
        <el-form :model="reviewForm" label-width="80px">
          <el-form-item label="Select product" required>
            <el-select v-model="reviewForm.productId" placeholder="Select a product to review" style="width: 100%">
              <el-option
                  v-for="item in reviewOrderItems"
                  :key="item.productId"
                  :label="item.productName"
                  :value="item.productId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Rating" required>
            <el-rate v-model="reviewForm.rating" show-text />
          </el-form-item>
          <el-form-item label="Feedback">
            <el-input
                v-model="reviewForm.content"
                type="textarea"
                :rows="4"
                placeholder="How was the product quality? Share your experience."
                maxlength="200"
                show-word-limit
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reviewVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitReviewForm">Submit review</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="refundVisible" title="Request Refund" width="450px">
        <el-form :model="refundForm" label-width="80px" label-position="top">
          <el-alert title="After submission, the merchant will process the request within 24 hours." type="info" :closable="false" style="margin-bottom: 15px" />
          <el-form-item label="Refund reason" required>
            <el-select v-model="refundForm.reason" placeholder="Select a valid reason" style="width: 100%">
              <el-option label="Ordered by mistake / duplicate order / no longer needed" value="Ordered by mistake / duplicate order / no longer needed" />
              <el-option label="Out of stock" value="Out of stock" />
              <el-option label="Incorrect address or phone" value="Incorrect address or phone" />
              <el-option label="Product quality issue" value="Product quality issue" />
              <el-option label="Other" value="Other" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="refundVisible = false">Not now</el-button>
          <el-button type="danger" @click="submitRefund">Submit request</el-button>
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

// Interface definitions: keep fields aligned with the template
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

// Details & reviews
const detailVisible = ref(false)
const currentOrderVo = ref<OrderVO | null>(null)
const reviewVisible = ref(false)
const reviewOrderItems = ref<OrderItem[]>([])
const reviewForm = reactive({ orderId: undefined, productId: undefined, rating: 5, content: '' })

// Refund
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
    // Use any to bypass pagination type checks or define a PageResult interface
    const res: any = await getMyOrders(queryParams)
    // [Fix] Ensure res exists when reading records
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
    0: 'Pending payment', 1: 'Awaiting shipment', 2: 'Shipped', 3: 'Completed', 4: 'Cancelled', 5: 'After-sales', 6: 'Refunded'
  }
  return map[status] || 'Unknown'
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
  ElMessageBox.confirm('Cancel this order? This action cannot be undone.', 'Confirm', {
    type: 'warning',
    confirmButtonText: 'Cancel order'
  }).then(async () => {
    await cancelOrder(row.orderNo)
    ElMessage.success('Order cancelled')
    await loadData()
  })
}

const handleReceive = (row: Order) => {
  ElMessageBox.confirm('Please confirm you received the items and they are in good condition.', 'Confirm receipt', {
    type: 'success',
    confirmButtonText: 'Confirm receipt'
  }).then(async () => {
    await receiveOrder(row.orderNo)
    ElMessage.success('Order completed!')
    await loadData()
  })
}

const handleViewDetail = async (row: Order) => {
  try {
    // [Fix] Remove redundant local variable and assign directly
    currentOrderVo.value = await getOrderDetailByNo(row.orderNo) as unknown as OrderVO
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('Failed to load details')
  }
}

const openRefund = (row: Order) => {
  refundForm.orderNo = row.orderNo
  refundForm.reason = ''
  refundVisible.value = true
}

const submitRefund = async () => {
  if (!refundForm.reason) return ElMessage.warning('Please select a refund reason')
  try {
    await applyRefund(refundForm)
    ElMessage.success('Request submitted. Please wait for merchant review.')
    refundVisible.value = false
    await loadData()
  } catch (e) {
    // request.ts handles errors
  }
}

const openReviewDialog = async (row: Order) => {
  reviewForm.orderId = row.id as any
  reviewForm.productId = undefined
  reviewForm.content = ''
  reviewForm.rating = 5

  try {
    // We need the res variable to branch on logic, so keep it here
    const res = await getOrderDetailByNo(row.orderNo) as unknown as OrderVO
    reviewOrderItems.value = res.items || []
    if (res.items.length > 0) reviewForm.productId = res.items[0].productId as any
    reviewVisible.value = true
  } catch (e) {
    ElMessage.error('Failed to load order items')
  }
}

const submitReviewForm = async () => {
  if (!reviewForm.productId) return ElMessage.warning('Please select a product to review')
  try {
    await submitReview(reviewForm)
    ElMessage.success('Review submitted successfully!')
    reviewVisible.value = false
  } catch (e) {
    // Error handling
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

/* Product preview */
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


/* Details dialog */
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
