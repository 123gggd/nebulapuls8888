<template>
  <div class="home-page">
    <!-- Hero Banner -->
    <div class="hero-section full-bleed">
      <el-skeleton v-if="bannerLoading" animated class="hero-skeleton">
        <template #template>
          <div class="hero-skeleton__inner">
            <div class="hero-skeleton__badge"></div>
            <div class="hero-skeleton__title"></div>
            <div class="hero-skeleton__desc"></div>
            <div class="hero-skeleton__btn"></div>
          </div>
        </template>
      </el-skeleton>
      <Swiper
        v-else
        class="hero-swiper"
        :modules="swiperModules"
        :autoplay="{ delay: 5000, disableOnInteraction: false }"
        :pagination="{ clickable: true }"
        loop
      >
        <SwiperSlide v-for="slide in heroSlides" :key="slide.title">
          <div class="hero-slide" :class="slide.theme">
            <div class="hero-content">
              <span class="hero-badge">{{ slide.badge }}</span>
              <h1>{{ slide.title }}</h1>
              <p>{{ slide.subtitle }}</p>
              <el-button type="primary" size="large" round @click="$router.push('/search')">
                {{ slide.cta }}
              </el-button>
            </div>
            <div class="hero-decor"></div>
          </div>
        </SwiperSlide>
      </Swiper>
    </div>

    <!-- Announcement bar -->
    <div class="notice-bar" v-if="noticeList.length > 0">
      <el-icon color="#E6A23C" class="icon"><Bell /></el-icon>
      <div class="notice-content">
        <span class="tag">Latest</span>
        <span class="title">{{ noticeList[0].title }}</span>
      </div>
      <el-link type="primary" @click="$router.push('/notice')">View all</el-link>
    </div>

    <!-- Flash sale (hidden if API fails) -->
    <div class="section-container seckill-section" v-if="seckillList.length > 0">
      <div class="section-header">
        <div class="left">
          <h2>⚡ Flash Sale</h2>
          <span class="subtitle">Limited time · Daily picks</span>
        </div>
        <div class="right countdown" v-if="countdownText">
          <span>Ends in</span>
          <strong>{{ countdownText }}</strong>
        </div>
      </div>
      <div class="seckill-grid">
        <el-card v-for="item in seckillList" :key="item.id" :body-style="{ padding: '0px' }" shadow="hover" class="seckill-card">
          <div class="image-wrapper" @click="$router.push(`/product/${item.productId}`)">
            <el-image :src="item.mainImage" class="image" loading="lazy" />
            <div class="discount-tag">Flash Sale</div>
          </div>
          <div class="info">
            <div class="name" :title="item.productName">{{ item.productName }}</div>
            <div class="price-row">
              <div class="seckill-price">¥{{ item.seckillPrice }}</div>
              <div class="original-price">¥{{ item.originalPrice }}</div>
            </div>
            <div class="stock-row">
              <el-progress :percentage="getSeckillProgress(item)" :show-text="false" status="exception" />
              <span class="stock-text">{{ getSeckillProgress(item) }}% claimed</span>
            </div>
            <el-button type="danger" size="small" class="seckill-btn" @click="$router.push(`/product/${item.productId}`)">Shop now</el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- Recommended for you -->
    <div class="section-container">
      <div class="section-header">
        <div class="left">
          <h2>✨ Recommended for You</h2>
          <span class="subtitle">Curated finds worth exploring</span>
        </div>
        <el-link type="primary" @click="$router.push('/search')">View all <el-icon><ArrowRight /></el-icon></el-link>
      </div>

      <div v-if="loading" class="masonry-grid">
        <el-skeleton v-for="item in 8" :key="item" animated class="product-skeleton">
          <template #template>
            <div class="skeleton-card"></div>
          </template>
        </el-skeleton>
      </div>
      <div v-else class="masonry-grid">
        <el-card v-for="item in productList" :key="item.id" :body-style="{ padding: '0px' }" shadow="hover" class="product-card">
          <div class="image-wrapper" @click="$router.push(`/product/${item.id}`)">
            <el-image :src="item.mainImage" class="image" loading="lazy" />
            <div class="hover-mask">
              <el-button type="primary" round :icon="ShoppingCart" @click.stop="handleAddToCart(item)">Add to Cart</el-button>
            </div>
          </div>
          <div class="product-info">
            <div class="product-name" :title="item.name">{{ item.name }}</div>
            <div class="product-subtitle">{{ item.subtitle || 'No description available' }}</div>
            <div class="bottom">
              <span class="price">
                <small>¥</small> {{ item.price }}
              </span>
              <span class="sales">Stock: {{ item.stock }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <el-empty v-if="!loading && productList.length === 0" description="No products yet. Please publish in the admin console." />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { searchProducts, addToCart, getNotices, getCurrentSeckills } from '@/api/store'
import { ArrowRight, ShoppingCart, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay, Pagination } from 'swiper/modules'
import 'swiper/css'
import 'swiper/css/pagination'

const userStore = useUserStore()
const router = useRouter()
const productList = ref<any[]>([])
const noticeList = ref<any[]>([])
const seckillList = ref<any[]>([])
const loading = ref(false)
const bannerLoading = ref(true)
const countdownText = ref('')
const swiperModules = [Autoplay, Pagination]
const heroSlides = [
  {
    badge: 'Editor’s Picks',
    title: 'Nebula Commerce 2026',
    subtitle: 'Virtual threads · Next-gen shopping experience',
    cta: 'Explore now',
    theme: 'theme-a'
  },
  {
    badge: 'Limited-time perks',
    title: 'Exclusive picks, limited-time deals',
    subtitle: 'Daily drop at 10:00 · Save with coupons',
    cta: 'See deals',
    theme: 'theme-b'
  },
  {
    badge: 'Quality assured',
    title: 'Officially curated supply chain',
    subtitle: 'Fast shipping · 7-day hassle-free returns',
    cta: 'Shop now',
    theme: 'theme-c'
  }
]
let countdownTimer: number | null = null

const loadData = async () => {
  loading.value = true

  // 1. Load top-selling products
  try {
    const pRes: any = await searchProducts({ page: 1, size: 12 })
    productList.value = pRes.records || []
  } catch (e) {
    console.error('Failed to load product list') // Log briefly to avoid noise
  }

  // 2. Load announcements
  try {
    const nRes: any = await getNotices()
    noticeList.value = nRes || []
  } catch (e) {
    // Announcement failure should not block the core experience
  }

  // 3. Load flash sales (even 500s shouldn't block the homepage)
  try {
    const sRes: any = await getCurrentSeckills()
    seckillList.value = sRes || []
    initCountdown()
  } catch (e) {
    console.warn('Flash sale service is temporarily unavailable') // Fallback
    seckillList.value = []
  } finally {
    loading.value = false
    bannerLoading.value = false
  }
}

const handleAddToCart = async (item: any) => {
  if (!userStore.token) {
    ElMessage.warning('Please sign in first')
    router.push('/login')
    return
  }
  try {
    await addToCart({ productId: item.id, quantity: 1 })
    ElMessage.success('Added to cart')
  } catch (e) {
    // request.ts handles error messages
  }
}

const initCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  if (!seckillList.value.length) {
    countdownText.value = ''
    return
  }
  const endTimeRaw = seckillList.value[0]?.endTime || seckillList.value[0]?.endAt || seckillList.value[0]?.endDate
  const endTime = endTimeRaw ? new Date(endTimeRaw).getTime() : Date.now() + 3600 * 1000
  const update = () => {
    const diff = endTime - Date.now()
    if (diff <= 0) {
      countdownText.value = '00:00:00'
      return
    }
    const hours = Math.floor(diff / 3600000)
    const minutes = Math.floor((diff % 3600000) / 60000)
    const seconds = Math.floor((diff % 60000) / 1000)
    countdownText.value = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  }
  update()
  countdownTimer = window.setInterval(update, 1000)
}

const getSeckillProgress = (item: any) => {
  const total = item.totalStock ?? item.originalStock ?? item.stockCount ?? 0
  if (!total) return 0
  const sold = Math.max(0, total - (item.stockCount ?? 0))
  return Math.min(100, Math.round((sold / total) * 100))
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped lang="scss">
.home-page { padding-bottom: 40px; }

.full-bleed {
  width: 100vw;
  margin-left: calc(50% - 50vw);
}

.hero-section {
  margin-bottom: 24px;
  border-radius: 0 0 24px 24px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
}

.hero-swiper,
.hero-slide {
  height: 460px;
}

.hero-slide {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  color: #fff;

  &.theme-a { background: linear-gradient(120deg, #2563eb 0%, #38bdf8 50%, #22c55e 100%); }
  &.theme-b { background: linear-gradient(120deg, #f97316 0%, #f43f5e 60%, #ef4444 100%); }
  &.theme-c { background: linear-gradient(120deg, #7c3aed 0%, #6366f1 50%, #38bdf8 100%); }
}

.hero-content {
  text-align: left;
  max-width: 520px;
  padding: 0 40px;
  z-index: 2;

  h1 { font-size: 48px; margin: 16px 0 10px; font-weight: 800; }
  p { font-size: 18px; margin: 0 0 28px; opacity: 0.9; }
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.18);
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  letter-spacing: 1px;
}

.hero-decor {
  position: absolute;
  right: 8%;
  top: 20%;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  filter: blur(0);
}

.hero-skeleton {
  height: 460px;
  background: linear-gradient(120deg, #e5e7eb 0%, #f1f5f9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-skeleton__inner {
  width: 520px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-skeleton__badge,
.hero-skeleton__title,
.hero-skeleton__desc,
.hero-skeleton__btn {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 999px;
  height: 16px;
}

.hero-skeleton__badge { width: 120px; height: 24px; }
.hero-skeleton__title { width: 60%; height: 32px; border-radius: 12px; }
.hero-skeleton__desc { width: 80%; height: 20px; border-radius: 10px; }
.hero-skeleton__btn { width: 160px; height: 44px; border-radius: 999px; }

.notice-bar {
  background: #fdf6ec;
  padding: 10px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  color: #E6A23C;

  .icon { margin-right: 10px; font-size: 18px; }
  .notice-content { flex: 1; display: flex; align-items: center; gap: 10px; }
  .tag { background: #E6A23C; color: white; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
  .title { font-size: 14px; color: #606266; }
}

.section-container {
  margin-bottom: 40px;
  &.seckill-section {
    background: linear-gradient(to bottom, #ffeaea 0%, #fff 100%);
    padding: 20px;
    border-radius: 12px;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
  .left {
    h2 { margin: 0; font-size: 26px; color: #333; display: inline-block; margin-right: 10px; }
    .subtitle { color: #999; font-size: 14px; }
  }
  .countdown {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    background: #fff;
    padding: 6px 12px;
    border-radius: 999px;
    font-size: 13px;
    color: #ef4444;
    box-shadow: 0 6px 12px rgba(239, 68, 68, 0.2);
    strong { font-size: 16px; letter-spacing: 1px; }
  }
}

.seckill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 18px;
}

.seckill-card {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 30px rgba(239, 68, 68, 0.15);
  }
  .image-wrapper {
    position: relative;
    height: 180px;
    overflow: hidden;
    .image { width: 100%; height: 100%; object-fit: cover; }
    .discount-tag {
      position: absolute;
      top: 12px;
      left: 12px;
      background: #ef4444;
      color: white;
      padding: 4px 10px;
      border-radius: 999px;
      font-size: 12px;
    }
  }
  .info {
    padding: 14px;
    .name { font-weight: 600; margin-bottom: 6px; }
    .price-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
    .seckill-price { color: #ef4444; font-weight: 700; font-size: 18px; }
    .original-price { color: #94a3b8; text-decoration: line-through; font-size: 13px; }
    .stock-row { display: flex; flex-direction: column; gap: 6px; font-size: 12px; color: #ef4444; }
  }
  .seckill-btn { width: 100%; margin-top: 8px; }
}

.masonry-grid {
  column-count: 4;
  column-gap: 24px;
}

.product-card {
  break-inside: avoid;
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 32px rgba(15, 23, 42, 0.12);
  }
}

.product-skeleton {
  break-inside: avoid;
  margin-bottom: 24px;
}

.skeleton-card {
  height: 320px;
  border-radius: 12px;
  background: #eef2f7;
}

.image-wrapper {
  position: relative;
  height: 220px;
  overflow: hidden;
  .image { width: 100%; height: 100%; object-fit: cover; }
}

.hover-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.product-card:hover .hover-mask { opacity: 1; }

.product-info {
  padding: 14px 16px 18px;
  .product-name { font-weight: 600; font-size: 15px; margin-bottom: 4px; }
  .product-subtitle { color: #94a3b8; font-size: 13px; margin-bottom: 10px; }
  .bottom { display: flex; justify-content: space-between; align-items: center; }
  .price { color: #ef4444; font-weight: 700; font-size: 18px; }
  .sales { color: #64748b; font-size: 12px; }
}

@media (max-width: 1200px) {
  .masonry-grid { column-count: 3; }
}

@media (max-width: 960px) {
  .hero-content { text-align: center; }
  .hero-decor { display: none; }
  .masonry-grid { column-count: 2; }
}

@media (max-width: 720px) {
  .masonry-grid { column-count: 1; }
}
</style>
