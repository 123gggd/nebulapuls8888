<template>
  <div class="cart-page">
    <div class="container">
      <el-card shadow="never" class="cart-card">
        <template #header>
          <div class="card-header">
            <span>购物车 ({{ cartList.length }})</span>
            <span class="tip" v-if="cartList.length > 0">
              <el-icon><InfoFilled /></el-icon> 即使未选中，商品价格变动也会实时更新
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
          <!-- [修复] row 显式类型 -->
          <el-table-column
              type="selection"
              width="55"
              align="center"
              :selectable="(row: CartItem) => row.stock > 0 && (row.status === 1 || row.status === undefined)"
          />

          <el-table-column label="商品信息" min-width="400">
            <!-- [修复] row 显式类型 -->
            <template #default="{ row }: { row: CartItem }">
              <div class="product-info" @click="router.push(`/product/${row.productId}`)">
                <el-image :src="row.mainImage" class="p-img" fit="cover" />
                <div class="p-detail">
                  <div class="name">{{ row.productName }}</div>
                  <div class="status-tags">
                    <el-tag v-if="row.status !== undefined && row.status !== 1" type="danger" size="small" effect="plain">已下架</el-tag>
                    <el-tag v-else-if="row.stock <= 0" type="info" size="small" effect="plain">已售罄</el-tag>
                    <el-tag v-else-if="row.stock < 10" type="warning" size="small" effect="plain">库存紧张: {{ row.stock }}</el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="price" label="单价" align="center" width="150">
            <template #default="{ row }: { row: CartItem }">
              <span class="unit-price">¥{{ row.price }}</span>
            </template>
          </el-table-column>

          <el-table-column label="数量" align="center" width="180">
            <template #default="{ row }: { row: CartItem }">
              <!-- [修复] val 显式类型 -->
              <el-input-number
                  v-if="row.stock > 0 && (row.status === 1 || row.status === undefined)"
                  v-model="row.quantity"
                  :min="1"
                  :max="row.stock"
                  size="small"
                  @change="(val: number | undefined) => handleQuantityChange(row, val)"
              />
              <span v-else class="disabled-text">不可购买</span>
            </template>
          </el-table-column>

          <el-table-column label="小计" align="center" width="150">
            <template #default="{ row }: { row: CartItem }">
              <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" align="center" width="120">
            <template #default="{ row }: { row: CartItem }">
              <el-button link type="danger" @click="handleDelete(row.id)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="cart-footer" v-if="cartList.length > 0">
          <div class="left-ops">
            <el-button link @click="clearInvalid">清理失效商品</el-button>
          </div>
          <div class="right-calc">
            <div class="total-info">
              已选 <span class="count">{{ selectedCount }}</span> 件商品，
              合计：<span class="price"><small>¥</small> {{ totalPrice }}</span>
            </div>
            <el-button
                type="primary"
                size="large"
                class="checkout-btn"
                @click="openCheckout"
                :disabled="selectedCount === 0"
            >
              去结算
            </el-button>
          </div>
        </div>

        <el-empty v-else description="购物车空空如也，快去逛逛吧">
          <el-button type="primary" @click="router.push('/')">去首页</el-button>
        </el-empty>
      </el-card>

      <!-- 结算弹窗 -->
      <el-dialog v-model="orderVisible" title="确认订单" width="700px" append-to-body destroy-on-close>
        <div v-loading="checkoutLoading" class="checkout-container">
          <!-- 1. 收货地址 -->
          <div class="section-title">收货地址</div>
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
            <el-button link type="primary" class="manage-addr-btn" @click="router.push('/address')">管理收货地址</el-button>
          </div>
          <el-empty v-else description="暂无收货地址" :image-size="60">
            <el-button type="primary" size="small" @click="router.push('/address')">去添加地址</el-button>
          </el-empty>

          <!-- 2. 优惠券 -->
          <div class="section-title" style="margin-top: 25px;">优惠券</div>
          <el-select v-model="selectedCouponId" placeholder="请选择优惠券" style="width: 100%" clearable>
            <el-option
                v-for="item in availableCoupons"
                :key="item.id"
                :label="`${item.name} (省 ¥${item.amount}, 满${item.minPoint}可用)`"
                :value="item.id"
            />
          </el-select>
          <div class="coupon-tip" v-if="availableCoupons.length === 0">
            <el-icon><InfoFilled /></el-icon> 暂无可用优惠券
          </div>

          <!-- 3. 金额明细 -->
          <div class="price-summary">
            <div class="row"><span>商品总额：</span><span>¥ {{ totalPrice }}</span></div>
            <div class="row coupon-row" v-if="selectedCouponAmount > 0">
              <span>优惠券抵扣：</span><span>- ¥ {{ selectedCouponAmount }}</span>
            </div>
            <div class="row total-row">
              <span>实付金额：</span><span class="final-price">¥ {{ finalPrice }}</span>
            </div>
          </div>
        </div>

        <template #footer>
          <el-button @click="orderVisible = false">取消</el-button>
          <el-button type="primary" @click="submitOrder" :disabled="!selectedAddressId" :loading="submitting">
            提交订单
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { getCartList, deleteCartItem, createOrder, getAddressList, updateCart } from '@/api/store'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { InfoFilled } from '@element-plus/icons-vue'

// [核心修复] 定义明确的 CartItem 接口
interface CartItem {
  id: number
  productId: number
  productName: string
  mainImage: string
  price: number
  quantity: number
  stock: number
  selected: boolean
  status?: number // 1: 上架, 0: 下架
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
        // 先计算需要更新的行
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

        // 批量或逐个处理取消选中状态
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
    // 如果失败，建议重新加载购物车恢复状态
    await loadCart()
  }
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定将该商品移出购物车?', '提示', { type: 'warning' }).then(async () => {
    await deleteCartItem(id)
    ElMessage.success('已移除')
    await loadCart()
  })
}

// 清理无效商品
const clearInvalid = () => {
  const invalidIds = cartList.value
      .filter(row => row.stock <= 0 || (row.status !== undefined && row.status !== 1))
      .map(row => row.id)

  if (invalidIds.length === 0) return ElMessage.info('暂无无效商品')

  ElMessageBox.confirm(`确定清空 ${invalidIds.length} 件失效商品吗？`, '提示').then(async () => {
    // 串行删除，防止并发问题
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

    // 手动调用请求以获取优惠券
    const couponRes = await request({
      url: '/marketing/coupon/usable',
      method: 'get',
      params: { orderAmount: totalPrice.value }
    }) as unknown as Coupon[]

    availableCoupons.value = couponRes || []

  } catch (e) {
    console.error(e)
    ElMessage.error('结算信息加载失败')
  } finally {
    checkoutLoading.value = false
  }
}

const submitOrder = async () => {
  if (!selectedAddressId.value) return ElMessage.warning('请选择收货地址')

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

    ElMessage.success('订单提交成功')
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
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .right-calc {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .total-info {
    font-size: 14px;
    color: #606266;
    .count { color: #f56c6c; font-weight: bold; margin: 0 4px; }
    .price {
      font-size: 28px;
      color: #f56c6c;
      font-weight: 800;
      margin-left: 10px;
      small { font-size: 16px; }
    }
  }

  .checkout-btn {
    width: 140px;
    font-weight: bold;
    font-size: 16px;
    letter-spacing: 1px;
  }
}

/* 弹窗样式 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  .address-item {
    width: 100%;
    margin-right: 0;
    height: auto;
    padding: 12px;
    border-radius: 6px;
    &.is-checked {
      background-color: #ecf5ff;
      border-color: #409EFF;
    }
    .addr-content {
      line-height: 1.6;
      .name { font-weight: bold; margin-right: 10px; font-size: 14px; }
      .phone { color: #666; font-size: 13px; }
      .detail { color: #909399; font-size: 12px; margin-top: 2px; }
    }
  }
}
.manage-addr-btn { margin-top: 8px; font-size: 12px; }

.price-summary {
  background: #f8f8f8;
  padding: 15px 20px;
  border-radius: 6px;
  margin-top: 25px;
  text-align: right;

  .row {
    margin-bottom: 8px;
    font-size: 14px;
    color: #606266;
    display: flex;
    justify-content: space-between;

    &.coupon-row { color: #f56c6c; }
    &.total-row {
      margin-top: 15px;
      padding-top: 15px;
      border-top: 1px dashed #e4e7ed;
      font-size: 16px;
      color: #303133;
      font-weight: bold;
      align-items: center;
      .final-price {
        color: #f56c6c;
        font-size: 24px;
      }
    }
  }
}
.coupon-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.section-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  &::before {
    content: '';
    display: block;
    width: 4px;
    height: 14px;
    background: #409EFF;
    margin-right: 8px;
    border-radius: 2px;
  }
}
</style>