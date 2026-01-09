<template>
  <div class="product-detail-page">
    <div class="container">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/search' }">商品列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card shadow="never" v-loading="loading" class="main-card">
        <div class="product-container">
          <!-- 左侧图片 -->
          <div class="left-gallery">
            <el-image
                v-if="product.mainImage"
                :src="product.mainImage"
                class="main-image"
                fit="cover"
                :preview-src-list="[product.mainImage]"
            />
            <div v-else class="image-placeholder">暂无图片</div>
          </div>

          <!-- 右侧信息 -->
          <div class="right-info">
            <h1 class="title">{{ product.name }}</h1>
            <p class="subtitle">{{ product.subtitle || '暂无详细描述' }}</p>

            <div class="price-box">
              <span class="label">价格</span>
              <div class="price-wrap">
                <span class="currency">¥</span>
                <span class="price">{{ product.price }}</span>
              </div>
            </div>

            <div class="meta-info">
              <div class="meta-item">
                <span class="label">库存</span>
                <span class="value">{{ product.stock }} 件</span>
              </div>
              <div class="meta-item">
                <span class="label">销量</span>
                <span class="value">{{ product.sale || 0 }}</span>
              </div>
              <div class="meta-item">
                <span class="label">评价</span>
                <span class="value">{{ totalReviews }} 条</span>
              </div>
            </div>

            <el-divider border-style="dashed" />

            <div class="action-area">
              <div class="quantity-box">
                <span class="label">数量</span>
                <el-input-number
                    v-model="quantity"
                    :min="1"
                    :max="product.stock"
                    size="large"
                />
              </div>

              <div class="btn-group">
                <!-- [修复] 使用 :icon 绑定，解决未使用导入报错 -->
                <el-button type="primary" size="large" :icon="ShoppingCart" @click="handleAddToCart">
                  加入购物车
                </el-button>
                <el-button type="danger" plain size="large" :icon="Wallet">
                  立即购买
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 详情与评价 Tabs -->
      <div class="detail-content">
        <el-tabs v-model="activeTab" class="product-tabs" type="border-card">
          <el-tab-pane label="商品详情" name="detail">
            <div class="product-desc" v-if="product.description" v-html="product.description"></div>
            <el-empty v-else description="商家暂未上传详情描述" />
          </el-tab-pane>

          <el-tab-pane label="用户评价" name="review">
            <div v-loading="reviewsLoading" class="review-container">
              <div v-if="reviews.length > 0" class="review-list">
                <div v-for="item in reviews" :key="item.id" class="review-item">
                  <div class="user-info">
                    <el-avatar :size="40" :src="item.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                    <div class="user-meta">
                      <div class="username">{{ item.userName || '匿名用户' }}</div>
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
              <el-empty v-else description="暂无评价，快来购买并发表第一条评价吧！" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, addToCart, getProductReviews } from '@/api/store'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { ShoppingCart, Wallet } from '@element-plus/icons-vue'

// [修复] 定义类型接口解决 unresolved variable
interface ProductDetail {
  id: number
  name: string
  subtitle?: string
  mainImage: string
  price: number
  stock: number
  sale?: number
  description?: string
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
// [修复] 应用接口类型
const product = ref<ProductDetail>({} as ProductDetail)
const reviews = ref<ReviewItem[]>([])
const totalReviews = ref(0)
const quantity = ref(1)
const activeTab = ref('detail')

const loadData = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res: any = await getProductDetail(id)
    product.value = res
    loadReviews(id)
  } catch (error) {
    console.error(error)
    ElMessage.error('商品不存在或已下架')
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
    console.error('加载评价失败', e)
  } finally {
    reviewsLoading.value = false
  }
}

const handleAddToCart = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录后操作')
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success('成功加入购物车')
  } catch (e) {
    // 错误已由 request.ts 处理
  }
}

onMounted(() => {
  loadData()
})

watch(() => route.params.id, (newId) => {
  if (newId) {
    quantity.value = 1
    loadData()
  }
})
</script>

<style scoped lang="scss">
.product-detail-page {
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
  font-size: 14px;
}
.main-card {
  margin-bottom: 20px;
  border-radius: 8px;
}
.product-container {
  display: flex;
  gap: 40px;
}
.left-gallery {
  width: 400px;
  .main-image {
    width: 100%;
    height: 400px;
    border-radius: 4px;
    border: 1px solid #eee;
  }
  .image-placeholder {
    width: 100%;
    height: 400px;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
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
  .price-box {
    background: #fff5f5;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 25px;
    display: flex;
    align-items: baseline;
    gap: 15px;
    .label { font-size: 14px; color: #606266; }
    .price-wrap {
      color: #f56c6c;
      font-weight: bold;
      .currency { font-size: 16px; margin-right: 2px; }
      .price { font-size: 32px; }
    }
  }
  .meta-info {
    display: flex;
    gap: 40px;
    margin-bottom: 20px;
    .meta-item {
      font-size: 14px;
      color: #606266;
      .label { margin-right: 10px; color: #909399; }
    }
  }
  .action-area {
    margin-top: 30px;
    .quantity-box {
      margin-bottom: 25px;
      display: flex;
      align-items: center;
      gap: 15px;
      .label { font-size: 14px; color: #606266; }
    }
    .btn-group {
      display: flex;
      gap: 15px;
    }
  }
}
.detail-content {
  background: #fff;
  border-radius: 8px;
  min-height: 400px;
  .product-desc {
    padding: 20px;
    line-height: 1.8;
    :deep(img) { max-width: 100%; }
  }
}
.review-item {
  border-bottom: 1px solid #eee;
  padding: 20px;
  display: flex;
  gap: 20px;
  &:last-child { border-bottom: none; }
  .user-info {
    width: 100px;
    text-align: center;
    .username { font-size: 12px; color: #666; margin-top: 5px; }
  }
  .review-content {
    flex: 1;
    .rating-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      .time { color: #ccc; font-size: 12px; }
    }
    .text { color: #333; font-size: 14px; line-height: 1.6; }
  }
}
</style>