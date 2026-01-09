<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <div class="banner-section">
      <el-carousel height="380px" class="banner" :interval="5000" arrow="always">
        <el-carousel-item v-for="item in 3" :key="item">
          <div class="banner-item" :class="`bg-${item}`">
            <div class="banner-content">
              <h1>Nebula Commerce 2026</h1>
              <p>虚拟线程驱动 · 下一代购物体验</p>
              <el-button type="primary" size="large" round @click="$router.push('/search')">立即探索</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 公告栏 -->
    <div class="notice-bar" v-if="noticeList.length > 0">
      <el-icon color="#E6A23C" class="icon"><Bell /></el-icon>
      <div class="notice-content">
        <span class="tag">最新</span>
        <span class="title">{{ noticeList[0].title }}</span>
      </div>
      <el-link type="primary" @click="$router.push('/notice')">查看更多</el-link>
    </div>

    <!-- 限时秒杀 (接口异常时不显示此区域) -->
    <div class="section-container seckill-section" v-if="seckillList.length > 0">
      <div class="section-header">
        <div class="left">
          <h2>⚡ 限时秒杀</h2>
          <span class="subtitle">手慢无 · 每日精选</span>
        </div>
      </div>
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in seckillList" :key="item.id">
          <el-card :body-style="{ padding: '0px' }" shadow="hover" class="seckill-card">
            <div class="image-wrapper" @click="$router.push(`/product/${item.productId}`)">
              <el-image :src="item.mainImage" class="image" loading="lazy" />
              <div class="discount-tag">秒杀</div>
            </div>
            <div class="info">
              <div class="name" :title="item.productName">{{ item.productName }}</div>
              <div class="price-row">
                <div class="seckill-price">¥{{ item.seckillPrice }}</div>
                <div class="original-price">¥{{ item.originalPrice }}</div>
              </div>
              <div class="stock-row">
                <el-progress :percentage="item.stockCount > 10 ? 50 : 90" :show-text="false" status="exception" />
                <span class="stock-text">仅剩 {{ item.stockCount }} 件</span>
              </div>
              <el-button type="danger" size="small" style="width: 100%; margin-top: 10px" @click="$router.push(`/product/${item.productId}`)">立即抢购</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 热销商品 -->
    <div class="section-container">
      <div class="section-header">
        <div class="left">
          <h2>🔥 热销商品</h2>
          <span class="subtitle">大家都在买的好物</span>
        </div>
        <el-link type="primary" @click="$router.push('/search')">查看全部 <el-icon><ArrowRight /></el-icon></el-link>
      </div>

      <el-row :gutter="25">
        <el-col :span="6" v-for="item in productList" :key="item.id" style="margin-bottom: 25px;">
          <el-card :body-style="{ padding: '0px' }" shadow="hover" class="product-card">
            <div class="image-wrapper" @click="$router.push(`/product/${item.id}`)">
              <el-image :src="item.mainImage" class="image" loading="lazy" />
              <div class="hover-mask">
                <el-button type="primary" round :icon="ShoppingCart" @click.stop="handleAddToCart(item)">加入购物车</el-button>
              </div>
            </div>
            <div class="product-info">
              <div class="product-name" :title="item.name">{{ item.name }}</div>
              <div class="product-subtitle">{{ item.subtitle || '暂无描述' }}</div>
              <div class="bottom">
                <span class="price">
                  <small>¥</small> {{ item.price }}
                </span>
                <span class="sales">库存: {{ item.stock }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && productList.length === 0" description="暂无商品，请在后台发布" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { searchProducts, addToCart, getNotices, getCurrentSeckills } from '@/api/store'
import { ArrowRight, ShoppingCart, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const productList = ref<any[]>([])
const noticeList = ref<any[]>([])
const seckillList = ref<any[]>([])
const loading = ref(false)

const loadData = async () => {
  loading.value = true

  // 1. 加载热销商品
  try {
    const pRes: any = await searchProducts({ page: 1, size: 12 })
    productList.value = pRes.records || []
  } catch (e) {
    console.error('商品列表加载失败') // 仅打印简略信息，避免刷屏
  }

  // 2. 加载公告
  try {
    const nRes: any = await getNotices()
    noticeList.value = nRes || []
  } catch (e) {
    // 公告加载失败不影响核心体验
  }

  // 3. 加载秒杀 (即使后端500也不影响主页其他内容)
  try {
    const sRes: any = await getCurrentSeckills()
    seckillList.value = sRes || []
  } catch (e) {
    console.warn('秒杀活动服务暂时不可用') // 降级处理
    seckillList.value = []
  } finally {
    loading.value = false
  }
}

const handleAddToCart = async (item: any) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addToCart({ productId: item.id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (e) {
    // request.ts 已处理错误提示
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.home-page { padding-bottom: 40px; }

.banner-section {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}
.banner-item {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  text-align: center;

  h1 { font-size: 48px; margin: 0 0 10px; font-weight: 800; text-shadow: 0 2px 10px rgba(0,0,0,0.2); }
  p { font-size: 18px; margin: 0 0 30px; opacity: 0.9; }

  &.bg-1 { background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%); }
  &.bg-2 { background: linear-gradient(120deg, #a18cd1 0%, #fbc2eb 100%); }
  &.bg-3 { background: linear-gradient(120deg, #fccb90 0%, #d57eeb 100%); }
}

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
}

.seckill-card {
  border: none;
  .image-wrapper { height: 180px; position: relative; }
  .discount-tag { position: absolute; top: 10px; left: 10px; background: #f56c6c; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
  .info { padding: 12px; }
  .name { font-weight: bold; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .price-row { display: flex; align-items: baseline; gap: 8px; margin-bottom: 8px; }
  .seckill-price { color: #f56c6c; font-size: 20px; font-weight: bold; }
  .original-price { text-decoration: line-through; color: #999; font-size: 12px; }
  .stock-row { display: flex; align-items: center; gap: 8px; .el-progress { flex: 1; } .stock-text { font-size: 12px; color: #999; width: 60px; text-align: right; } }
}

.product-card {
  border: none;
  border-radius: 12px;
  transition: all 0.3s;
  &:hover { transform: translateY(-5px); box-shadow: 0 15px 30px rgba(0,0,0,0.1); .image-wrapper .hover-mask { opacity: 1; } }
}

.image-wrapper {
  height: 240px;
  overflow: hidden;
  position: relative;
  background: #f8f8f8;
  cursor: pointer;
  .image { width: 100%; height: 100%; object-fit: cover; }
  .hover-mask {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(0,0,0,0.3);
    display: flex;
    justify-content: center;
    align-items: center;
    opacity: 0;
    transition: opacity 0.3s;
  }
}

.product-info {
  padding: 16px;
  .product-name { font-weight: bold; font-size: 16px; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #333; }
  .product-subtitle { font-size: 12px; color: #999; margin-bottom: 12px; height: 18px; overflow: hidden; }
  .bottom {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    .price { color: #f56c6c; font-size: 22px; font-weight: 800; small { font-size: 14px; } }
    .sales { font-size: 12px; color: #999; }
  }
}
</style>