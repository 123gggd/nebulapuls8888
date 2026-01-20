<template>
  <div class="product-detail-page">
    <div class="container">
      <!-- Breadcrumbs -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/search' }">Products</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name || 'Product Details' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card shadow="never" v-loading="loading" class="main-card">
        <div class="product-container">
          <!-- Left gallery -->
          <div class="left-gallery">
            <el-image
              v-if="activeImage"
              :src="activeImage"
              class="main-image"
              fit="cover"
              :preview-src-list="galleryImages"
            />
            <div v-else class="image-placeholder">No image available</div>

            <div class="thumb-list" v-if="galleryImages.length > 1">
              <div
                v-for="(img, index) in galleryImages"
                :key="img + index"
                class="thumb-item"
                :class="{ active: activeImage === img }"
                @click="activeImage = img"
              >
                <img :src="img" alt="thumbnail" />
              </div>
            </div>
          </div>

          <!-- Right info -->
          <div class="right-info">
            <h1 class="title">{{ product.name }}</h1>
            <p class="subtitle">{{ product.subtitle || 'No detailed description' }}</p>

            <div class="service-tags">
              <span>Authentic products</span>
              <span>Fast shipping</span>
              <span>7-day hassle-free returns</span>
            </div>

            <!-- Spec selection (merged from codex) -->
            <div class="specs-block" v-if="specEntries.length">
              <div class="spec-row" v-for="[specName, options] in specEntries" :key="specName">
                <span class="spec-label">{{ specName }}</span>
                <div class="spec-options">
                  <span
                    v-for="option in options"
                    :key="option"
                    class="spec-option"
                    :class="{ active: selectedSpecs[specName] === option }"
                    @click="selectedSpecs[specName] = option"
                  >
                    {{ option }}
                  </span>
                </div>
              </div>
            </div>

            <div class="price-box">
              <span class="label">Price</span>
              <div class="price-wrap">
                <span class="currency">¥</span>
                <span class="price">{{ product.price }}</span>
                <span class="origin" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
              </div>
            </div>

            <div class="meta-info">
              <div class="meta-item">
                <span class="label">Stock</span>
                <span class="value">{{ product.stock }} units</span>
              </div>
              <div class="meta-item">
                <span class="label">Sales</span>
                <span class="value">{{ product.sale || 0 }}</span>
              </div>
              <div class="meta-item">
                <span class="label">Reviews</span>
                <span class="value">{{ totalReviews }}</span>
              </div>
            </div>

            <el-divider border-style="dashed" />

            <div class="action-area">
              <div class="quantity-box">
                <span class="label">Quantity</span>
                <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" />
              </div>

              <div class="btn-group">
                <el-button type="primary" size="large" :icon="ShoppingCart" @click="handleAddToCart">
                  Add to Cart
                </el-button>
                <el-button type="danger" plain size="large" :icon="Wallet" @click="handleBuyNow">
                  Buy Now
                </el-button>
              </div>

              <div class="tip-row">
                <span>Ships within 24 hours · E-invoice available</span>
              </div>

              <!-- Service notes (merged from codex) -->
              <div class="service-notice">
                <div>
                  <strong>Shipping:</strong> Default SF/JD Logistics, estimated 2-5 days.
                </div>
                <div>
                  <strong>After-sales:</strong> 7-day hassle-free returns for quality issues.
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- Details & reviews tabs -->
      <div class="detail-content">
        <el-tabs v-model="activeTab" class="product-tabs" type="border-card">
          <el-tab-pane label="Details" name="detail">
            <div class="product-desc" v-if="product.description" v-html="product.description"></div>
            <el-empty v-else description="The seller hasn't provided details yet" />
          </el-tab-pane>

          <el-tab-pane label="Reviews" name="review">
            <div v-loading="reviewsLoading" class="review-container">
              <div v-if="reviews.length > 0" class="review-list">
                <div v-for="item in reviews" :key="item.id" class="review-item">
                  <div class="user-info">
                    <el-avatar
                      :size="40"
                      :src="
                        item.userAvatar ||
                        'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
                      "
                    />
                    <div class="user-meta">
                      <div class="username">{{ item.userName || 'Anonymous' }}</div>
                    </div>
                  </div>
                  <div class="review-content">
                    <div class="rating-row">
                      <el-rate v-model="item.rating" disabled size="small" text-color="#ff9900" />
                      <span class="time">{{ item.createTime }}</span>
                    </div>
                    <div class="text">{{ item.content }}</div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="No reviews yet. Be the first to review!" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, addToCart, getProductReviews } from '@/api/store'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { ShoppingCart, Wallet } from '@element-plus/icons-vue'

// [Fix] Define interfaces to resolve unresolved variable
interface ProductDetail {
  id: number
  name: string
  subtitle?: string
  mainImage: string
  price: number
  stock: number
  sale?: number
  description?: string
  originalPrice?: number
  subImages?: string[]
  // Merged from codex: specs
  specifications?: Record<string, string[]>
  [key: string]: any
}

interface ReviewItem {
  id: number
  userAvatar?: string
  userName?: string
  rating: number
  createTime: string
  content: string
  [key: string]: any
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const reviewsLoading = ref(false)
const product = ref<ProductDetail>({} as ProductDetail)
const reviews = ref<ReviewItem[]>([])
const totalReviews = ref(0)
const quantity = ref(1)
const activeTab = ref('detail')
const galleryImages = ref<string[]>([])
const activeImage = ref('')

// Merged from codex: spec selection state
const selectedSpecs = ref<Record<string, string>>({})

// Spec entries: [[specName, options], ...]
const specEntries = computed<[string, string[]][]>(() => {
  const specs = product.value.specifications || {}
  return Object.entries(specs).map(([key, value]) => [key, value as string[]])
})

const loadData = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res: any = await getProductDetail(id)
    product.value = res
    galleryImages.value = [res.mainImage, ...(res.subImages || [])].filter(Boolean)
    activeImage.value = galleryImages.value[0] || ''

    // Initialize default specs (select the first option for each spec)
    selectedSpecs.value = {}
    specEntries.value.forEach(([specName, options]) => {
      if (options?.length) selectedSpecs.value[specName] = options[0]
    })

    loadReviews(id)
  } catch (error) {
    console.error(error)
    ElMessage.error('Product not found or unavailable')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const loadReviews = async (productId: number) => {
  reviewsLoading.value = true
  try {
    const res: any = await getProductReviews({
      productId: productId,
      page: 1,
      size: 20
    })
    reviews.value = res.records || res.data || []
    totalReviews.value = res.total || 0
  } catch (e) {
    console.error('Failed to load reviews', e)
  } finally {
    reviewsLoading.value = false
  }
}

const handleAddToCart = async () => {
  if (!userStore.token) {
    ElMessage.warning('Please sign in first')
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  try {
    // If the backend needs spec parameters, include selectedSpecs:
    // await addToCart({ productId: product.value.id, quantity: quantity.value, specifications: selectedSpecs.value })
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success('Added to cart')
  } catch (e) {
    // Errors are handled by request.ts
  }
}

const handleBuyNow = async () => {
  if (!userStore.token) {
    ElMessage.warning('Please sign in first')
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  try {
    // Same as above: include specifications if needed.
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    router.push('/cart')
  } catch (e) {
    // Errors are handled by request.ts
  }
}

onMounted(() => {
  loadData()
})

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      quantity.value = 1
      loadData()
    }
  }
)
</script>

<style scoped lang="scss">
.product-detail-page {
  padding: 20px 0 60px;
  background: #f5f7fa;
  min-height: 80vh;
}
.container {
  width: 1200px;
  margin: 0 auto;
}
.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
}
.main-card {
  margin-bottom: 20px;
  border-radius: 12px;
}
.product-container {
  display: flex;
  gap: 40px;
}
.left-gallery {
  width: 420px;
  .main-image {
    width: 100%;
    height: 420px;
    border-radius: 12px;
    border: 1px solid #eee;
  }
  .image-placeholder {
    width: 100%;
    height: 420px;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
  }
  .thumb-list {
    display: flex;
    gap: 10px;
    margin-top: 12px;
  }
  .thumb-item {
    width: 72px;
    height: 72px;
    border-radius: 10px;
    overflow: hidden;
    border: 2px solid transparent;
    cursor: pointer;
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    &.active {
      border-color: #409eff;
      box-shadow: 0 6px 14px rgba(64, 158, 255, 0.2);
    }
  }
}
.right-info {
  flex: 1;
  .title {
    font-size: 26px;
    color: #303133;
    margin: 0 0 10px;
    font-weight: 600;
  }
  .subtitle {
    color: #909399;
    margin-bottom: 20px;
    font-size: 14px;
    line-height: 1.5;
  }
  .service-tags {
    display: flex;
    gap: 10px;
    margin-bottom: 18px;
    span {
      background: #f1f5f9;
      color: #475569;
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 999px;
    }
  }

  /* Merged from codex: spec styles */
  .specs-block {
    background: #f8fafc;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 18px;
    .spec-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;
      &:last-child {
        margin-bottom: 0;
      }
    }
    .spec-label {
      width: 70px;
      color: #64748b;
      font-size: 13px;
    }
    .spec-options {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }
    .spec-option {
      padding: 6px 12px;
      border-radius: 999px;
      border: 1px solid #e2e8f0;
      cursor: pointer;
      font-size: 13px;
      color: #475569;
      transition: all 0.2s ease;
      &:hover {
        border-color: #409eff;
        color: #409eff;
      }
      &.active {
        background: #409eff;
        border-color: #409eff;
        color: #fff;
      }
    }
  }

  .price-box {
    background: #fff5f5;
    padding: 20px;
    border-radius: 12px;
    margin-bottom: 25px;
    display: flex;
    align-items: baseline;
    gap: 15px;
    .label {
      font-size: 14px;
      color: #606266;
    }
    .price-wrap {
      color: #f56c6c;
      font-weight: bold;
      display: flex;
      align-items: baseline;
      gap: 12px;
      .currency {
        font-size: 16px;
        margin-right: 2px;
      }
      .price {
        font-size: 32px;
      }
      .origin {
        font-size: 14px;
        color: #94a3b8;
        text-decoration: line-through;
      }
    }
  }
  .meta-info {
    display: flex;
    gap: 40px;
    margin-bottom: 20px;
    .meta-item {
      font-size: 14px;
      color: #606266;
      .label {
        margin-right: 10px;
        color: #909399;
      }
    }
  }
  .action-area {
    margin-top: 30px;
    .quantity-box {
      margin-bottom: 25px;
      display: flex;
      align-items: center;
      gap: 15px;
      .label {
        font-size: 14px;
        color: #606266;
      }
    }
    .btn-group {
      display: flex;
      gap: 15px;
    }
    .tip-row {
      margin-top: 16px;
      font-size: 12px;
      color: #94a3b8;
    }

    /* Merged from codex: service note styles */
    .service-notice {
      margin-top: 12px;
      font-size: 12px;
      color: #64748b;
      line-height: 1.7;
      background: #f8fafc;
      padding: 12px;
      border-radius: 10px;
      border: 1px dashed #cbd5f5;
    }
  }
}
.detail-content {
  background: #fff;
  border-radius: 12px;
  min-height: 400px;
  .product-desc {
    padding: 20px;
    line-height: 1.8;
    :deep(img) {
      max-width: 100%;
    }
  }
}
.review-item {
  border-bottom: 1px solid #eee;
  padding: 20px;
  display: flex;
  gap: 20px;
  &:last-child {
    border-bottom: none;
  }
  .user-info {
    width: 100px;
    text-align: center;
    .username {
      font-size: 12px;
      color: #666;
      margin-top: 5px;
    }
  }
  .review-content {
    flex: 1;
    .rating-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      .time {
        color: #ccc;
        font-size: 12px;
      }
    }
    .text {
      color: #333;
      font-size: 14px;
      line-height: 1.6;
    }
  }
}
</style>
