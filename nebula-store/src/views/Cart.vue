<template>
  <div class="cart-page">
    <div class="container">
      <el-card shadow="never" class="cart-card">
        <template #header>
          <div class="card-header">
            <span>Cart ({{ cartList.length }})</span>
            <span class="tip" v-if="cartList.length > 0">
              <el-icon><InfoFilled /></el-icon> Prices update in real time even if items are not selected
            </span>
          </div>
        </template>

        <el-table
            :data="cartList"
            style="width: 100%"
            size="large"
            @selection-change="handleSelectionChange"
            ref="multipleTableRef"
            row-key="id"
            v-loading="loading"
        >
          <!-- [Fix] Explicit row typing -->
          <el-table-column
              type="selection"
              width="55"
              align="center"
              :selectable="(row: CartItem) => row.stock > 0 && (row.status === 1 || row.status === undefined)"
          />

          <el-table-column label="Product Info" min-width="400">
            <!-- [Fix] Explicit row typing -->
            <template #default="{ row }: { row: CartItem }">
              <div class="product-info" @click="router.push(`/product/${row.productId}`)">
                <el-image :src="row.mainImage" class="p-img" fit="cover" />
                <div class="p-detail">
                  <div class="name">{{ row.productName }}</div>
                  <div class="status-tags">
                    <el-tag v-if="row.status !== undefined && row.status !== 1" type="danger" size="small" effect="plain">Unavailable</el-tag>
                    <el-tag v-else-if="row.stock <= 0" type="info" size="small" effect="plain">Sold out</el-tag>
                    <el-tag v-else-if="row.stock < 10" type="warning" size="small" effect="plain">Low stock: {{ row.stock }}</el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="price" label="Unit Price" align="center" width="150">
            <template #default="{ row }: { row: CartItem }">
              <span class="unit-price">¥{{ row.price }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Quantity" align="center" width="180">
            <template #default="{ row }: { row: CartItem }">
              <!-- [Fix] Explicit val typing -->
              <el-input-number
                  v-if="row.stock > 0 && (row.status === 1 || row.status === undefined)"
                  v-model="row.quantity"
                  :min="1"
                  :max="row.stock"
                  size="small"
                  @change="(val: number | undefined) => handleQuantityChange(row, val)"
              />
              <span v-else class="disabled-text">Unavailable</span>
            </template>
          </el-table-column>

          <el-table-column label="Subtotal" align="center" width="150">
            <template #default="{ row }: { row: CartItem }">
              <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="Actions" align="center" width="120">
            <template #default="{ row }: { row: CartItem }">
              <el-button link type="danger" @click="handleDelete(row.id)">Remove</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="cart-footer" v-if="cartList.length > 0">
          <div class="left-ops">
            <el-button link @click="clearInvalid">Remove unavailable items</el-button>
          </div>
          <div class="right-calc">
            <div class="total-info">
              Selected <span class="count">{{ selectedCount }}</span> items,
              Total: <span class="price"><small>¥</small> {{ totalPrice }}</span>
            </div>
            <el-button
                type="primary"
                size="large"
                class="checkout-btn"
                @click="openCheckout"
                :disabled="selectedCount === 0"
            >
              Checkout
            </el-button>
          </div>
        </div>

        <EmptyState v-else description="Your cart is empty. Start shopping!" action-text="Go to home" action-to="/">
          <template #image>
            <svg viewBox="0 0 220 160" aria-hidden="true" class="empty-illustration">
              <defs>
                <linearGradient id="cartGradient" x1="0" x2="1" y1="0" y2="1">
                  <stop offset="0%" stop-color="#60a5fa" />
                  <stop offset="100%" stop-color="#38bdf8" />
                </linearGradient>
              </defs>
              <rect x="18" y="20" width="184" height="120" rx="18" fill="#f1f5f9" />
              <path d="M52 60h106l-8 50H62z" fill="url(#cartGradient)" opacity="0.9" />
              <circle cx="78" cy="118" r="10" fill="#94a3b8" />
              <circle cx="142" cy="118" r="10" fill="#94a3b8" />
              <path d="M74 42h72" stroke="#94a3b8" stroke-width="6" stroke-linecap="round" />
            </svg>
          </template>
        </EmptyState>
      </el-card>

      <!-- Checkout dialog -->
      <el-dialog v-model="orderVisible" title="Confirm Order" width="700px" append-to-body destroy-on-close>
        <div v-loading="checkoutLoading" class="checkout-container">
          <!-- 1. Shipping address -->
          <div class="section-title">Shipping Address</div>
          <div v-if="addressList.length > 0">
            <el-radio-group v-model="selectedAddressId" class="address-list">
              <el-radio border v-for="addr in addressList" :key="addr.id" :value="addr.id" class="address-item">
                <div class="addr-content">
                  <span class="name">{{ addr.receiverName }}</span>
                  <span class="phone">{{ addr.receiverPhone }}</span>
                  <div class="detail">{{ addr.province }} {{ addr.city }} {{ addr.region }} {{ addr.detailAddress }}</div>
                </div>
              </el-radio>
            </el-radio-group>
            <el-button link type="primary" class="manage-addr-btn" @click="router.push('/address')">Manage addresses</el-button>
          </div>
          <el-empty v-else description="No shipping address yet" :image-size="60">
            <el-button type="primary" size="small" @click="router.push('/address')">Add an address</el-button>
          </el-empty>

          <!-- 2. Coupons -->
          <div class="section-title" style="margin-top: 25px;">Coupons</div>
          <el-select v-model="selectedCouponId" placeholder="Select a coupon" style="width: 100%" clearable>
            <el-option
                v-for="item in availableCoupons"
                :key="item.id"
                :label="`${item.name} (Save ¥${item.amount}, spend ¥${item.minPoint} to use)`"
                :value="item.id"
            />
          </el-select>
          <div class="coupon-tip" v-if="availableCoupons.length === 0">
            <el-icon><InfoFilled /></el-icon> No available coupons
          </div>

          <!-- 3. Amount summary -->
          <div class="price-summary">
            <div class="row"><span>Items total:</span><span>¥ {{ totalPrice }}</span></div>
            <div class="row coupon-row" v-if="selectedCouponAmount > 0">
              <span>Coupon discount:</span><span>- ¥ {{ selectedCouponAmount }}</span>
            </div>
            <div class="row total-row">
              <span>Amount due:</span><span class="final-price">¥ {{ finalPrice }}</span>
            </div>
          </div>
        </div>

        <template #footer>
          <el-button @click="orderVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitOrder" :disabled="!selectedAddressId" :loading="submitting">
            Place Order
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { getCartList, deleteCartItem, createOrder, getAddressList, updateCart, getUsableCoupons } from '@/api/store'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { InfoFilled } from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'

// [Core fix] Define a clear CartItem interface
interface CartItem {
  id: number
  productId: number
  productName: string
  mainImage: string
  price: number
  quantity: number
  stock: number
  selected: boolean
  status?: number // 1: available, 0: unavailable
  merchantId?: number
  [key: string]: any
}

interface Address {
  id: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  region: string
  detailAddress: string
}

interface Coupon {
  id: number
  name: string
  amount: number
  minPoint: number
}

const router = useRouter()
const loading = ref(false)
const cartList = ref<CartItem[]>([])
const addressList = ref<Address[]>([])
const availableCoupons = ref<Coupon[]>([])
const orderVisible = ref(false)
const checkoutLoading = ref(false)
const submitting = ref(false)
const selectedAddressId = ref<number | undefined>(undefined)
const selectedCouponId = ref<number | undefined>(undefined)
const multipleTableRef = ref()
const selectedRows = ref<CartItem[]>([])

const totalPrice = computed(() => {
  return selectedRows.value.reduce((sum, item) => sum + (item.price * item.quantity), 0).toFixed(2)
})

const selectedCount = computed(() => selectedRows.value.length)

const selectedCouponAmount = computed(() => {
  if (!selectedCouponId.value) return 0
  const coupon = availableCoupons.value.find(c => c.id === selectedCouponId.value)
  return coupon ? coupon.amount : 0
})

const finalPrice = computed(() => {
  const total = parseFloat(totalPrice.value)
  const discount = selectedCouponAmount.value
  return Math.max(0, total - discount).toFixed(2)
})

const loadCart = async () => {
  loading.value = true
  try {
    const res = await getCartList() as unknown as CartItem[]
    cartList.value = res || []

    nextTick(async () => {
      if (multipleTableRef.value) {
        // First compute rows to update
        const rowsToUnselect: CartItem[] = []

        for (const row of cartList.value) {
          const isOffShelf = row.status !== undefined && row.status !== 1
          const isOutOfStock = row.stock <= 0
          const isValid = !isOffShelf && !isOutOfStock

          if (row.selected && isValid) {
            multipleTableRef.value.toggleRowSelection(row, true)
          } else if (row.selected && !isValid) {
            rowsToUnselect.push(row)
            multipleTableRef.value.toggleRowSelection(row, false)
          }
        }

        // Unselect rows in batch or one by one
        for (const row of rowsToUnselect) {
          try {
            await updateCart({ id: row.id, selected: false })
          } catch(e) { console.error(e) }
        }
      }
    })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (val: CartItem[]) => {
  selectedRows.value = val
}

const handleQuantityChange = async (row: CartItem, val: number | undefined) => {
  if (!val) return
  try {
    await updateCart({ id: row.id, quantity: val })
  } catch (e) {
    // If it fails, reload the cart to restore state
    await loadCart()
  }
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('Remove this item from the cart?', 'Confirm', { type: 'warning' }).then(async () => {
    await deleteCartItem(id)
    ElMessage.success('Removed')
    await loadCart()
  })
}

// Remove invalid items
const clearInvalid = () => {
  const invalidIds = cartList.value
      .filter(row => row.stock <= 0 || (row.status !== undefined && row.status !== 1))
      .map(row => row.id)

  if (invalidIds.length === 0) return ElMessage.info('No invalid items')

  ElMessageBox.confirm(`Remove ${invalidIds.length} invalid items?`, 'Confirm').then(async () => {
    // Delete serially to avoid concurrency issues
    for (const id of invalidIds) {
      await deleteCartItem(id)
    }
    await loadCart()
  })
}

const openCheckout = async () => {
  checkoutLoading.value = true
  orderVisible.value = true
  selectedCouponId.value = undefined
  availableCoupons.value = []

  try {
    const addrRes = await getAddressList() as unknown as Address[]
    addressList.value = addrRes || []

    if (addressList.value.length > 0) {
      selectedAddressId.value = addressList.value[0].id
    } else {
      selectedAddressId.value = undefined
    }

    // Fetch available coupons
    const couponRes = await getUsableCoupons({ orderAmount: totalPrice.value }) as unknown as Coupon[]

    availableCoupons.value = couponRes || []

  } catch (e) {
    console.error(e)
    ElMessage.error('Failed to load checkout info')
  } finally {
    checkoutLoading.value = false
  }
}

const submitOrder = async () => {
  if (!selectedAddressId.value) return ElMessage.warning('Please select a shipping address')

  submitting.value = true
  const addr = addressList.value.find(item => item.id === selectedAddressId.value)
  if (!addr) return

  try {
    const res: any = await createOrder({
      receiverName: addr.receiverName,
      receiverPhone: addr.receiverPhone,
      receiverAddress: `${addr.province} ${addr.city} ${addr.region} ${addr.detailAddress}`,
      userCouponId: selectedCouponId.value
    })

    const tradeNo = typeof res === 'string' ? res : (res.tradeNo || res.orderNo || res)

    ElMessage.success('Order placed successfully')
    orderVisible.value = false

    router.push({
      path: '/pay',
      query: {
        tradeNo: tradeNo,
        amount: finalPrice.value
      }
    })

    await loadCart()
  } catch (e: any) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped lang="scss">
.cart-page {
  padding: 20px 0;
  background: #f5f7fa;
  min-height: 80vh;
}
.container {
  width: 1200px;
  margin: 0 auto;
}
.cart-card {
  border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
  .tip {
    font-size: 12px;
    font-weight: normal;
    color: #909399;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.product-info {
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  .p-img {
    width: 80px;
    height: 80px;
    border-radius: 6px;
    border: 1px solid #eee;
    flex-shrink: 0;
  }
  .p-detail {
    .name {
      font-weight: bold;
      font-size: 14px;
      margin-bottom: 6px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }
}

.unit-price { font-weight: bold; color: #303133; }
.subtotal { color: #f56c6c; font-weight: bold; }

.disabled-text { font-size: 12px; color: #999; }

.cart-footer {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.left-ops {
  color: #909399;
  font-size: 13px;
}

.right-calc {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-info {
  font-size: 14px;
  color: #606266;
  .count {
    color: #409eff;
    font-weight: 600;
    margin: 0 4px;
  }
  .price {
    color: #f56c6c;
    font-weight: 700;
    margin-left: 6px;
  }
}

.checkout-btn {
  padding: 10px 26px;
  border-radius: 20px;
}

.checkout-container {
  min-height: 200px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  width: 100%;
}

.addr-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  .name {
    font-weight: 600;
  }
  .phone {
    color: #606266;
  }
  .detail {
    color: #909399;
    font-size: 12px;
  }
}

.manage-addr-btn {
  padding: 0;
}

.coupon-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-summary {
  margin-top: 20px;
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  .row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 6px;
    font-size: 13px;
  }
  .coupon-row {
    color: #f56c6c;
  }
  .total-row {
    font-weight: 600;
  }
  .final-price {
    color: #f56c6c;
  }
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 20px;
  }
}
</style>
