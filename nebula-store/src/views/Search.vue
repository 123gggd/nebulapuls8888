<template>
  <div class="search-page">
    <div class="container">
      <!-- 顶部过滤栏 -->
      <div class="filter-bar">
        <!-- 分类筛选 -->
        <div class="filter-row" v-if="categoryList.length > 0">
          <span class="label">分类：</span>
          <div class="options">
            <span
                class="option-item"
                :class="{ active: !categoryId }"
                @click="handleCategory(undefined)"
            >全部</span>
            <span
                v-for="cat in categoryList"
                :key="cat.id"
                class="option-item"
                :class="{ active: categoryId === cat.id }"
                @click="handleCategory(cat.id)"
            >
              {{ cat.name }}
            </span>
          </div>
        </div>

        <el-divider style="margin: 15px 0" />

        <!-- 排序选项 -->
        <div class="sort-row">
          <div class="sort-options">
            <span class="label">排序：</span>
            <span
                class="sort-item"
                :class="{ active: sortType === 'default' }"
                @click="handleSort('default')"
            >综合</span>
            <span
                class="sort-item"
                :class="{ active: sortType === 'sale' }"
                @click="handleSort('sale')"
            >销量</span>
            <span
                class="sort-item price-sort"
                :class="{ active: sortType === 'price' }"
                @click="handleSort('price')"
            >
              价格
              <el-icon v-if="sortOrder === 'asc' && sortType === 'price'"><CaretTop /></el-icon>
              <el-icon v-else-if="sortOrder === 'desc' && sortType === 'price'"><CaretBottom /></el-icon>
              <el-icon v-else><DArrowRight style="transform: rotate(90deg)" /></el-icon>
            </span>
          </div>
          <div class="result-count">
            共找到 <b>{{ total }}</b> 件商品
          </div>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="product-grid" v-loading="loading">
        <!-- [修复] 使用 router.push -->
        <div
            v-for="item in productList"
            :key="item.id"
            class="product-item"
            @click="router.push(`/product/${item.id}`)"
        >
          <div class="img-box">
            <el-image :src="item.mainImage" fit="cover" lazy>
              <template #placeholder>
                <div class="image-slot">加载中...</div>
              </template>
            </el-image>
          </div>
          <div class="info">
            <div class="name" :title="item.name" v-html="highlightKeyword(item.name)"></div>
            <div class="price-row">
              <span class="price">¥{{ item.price }}</span>
              <span class="sales">{{ item.sale || 0 }}人付款</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && productList.length === 0" description="未找到相关商品，换个词试试？" />

      <!-- 分页 -->
      <div class="pagination-box" v-if="total > 0">
        <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            v-model:current-page="currentPage"
            @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchProducts, getCategoryList } from '@/api/store'
import { CaretTop, CaretBottom, DArrowRight } from '@element-plus/icons-vue'

// [修复] 定义接口
interface Product {
  id: number
  name: string
  mainImage: string
  price: number
  sale: number
  [key: string]: any
}

interface Category {
  id: number
  name: string
}

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const productList = ref<Product[]>([])
const categoryList = ref<Category[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(16)

const keyword = ref('')
const categoryId = ref<number | undefined>(undefined)
const sortType = ref('default')
const sortOrder = ref('')

const loadCategories = async () => {
  try {
    const res: any = await getCategoryList()
    categoryList.value = res || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const doSearch = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value,
      categoryId: categoryId.value
    }

    if (sortType.value === 'price') {
      params.sort = 'price'
      params.order = sortOrder.value
    } else if (sortType.value === 'sale') {
      params.sort = 'sale'
      params.order = 'desc'
    }

    const res: any = await searchProducts(params)
    productList.value = res.records || res.data || []
    total.value = res.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const highlightKeyword = (text: string) => {
  if (!keyword.value) return text
  const reg = new RegExp(keyword.value, 'gi')
  return text.replace(reg, (match) => `<span style="color: #f56c6c; font-weight:bold">${match}</span>`)
}

const handleCategory = (id: number | undefined) => {
  categoryId.value = id
  currentPage.value = 1
  doSearch()
}

const handleSort = (type: string) => {
  if (type === 'price') {
    if (sortType.value === 'price') {
      sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
    } else {
      sortType.value = 'price'
      sortOrder.value = 'asc'
    }
  } else {
    sortType.value = type
    sortOrder.value = ''
  }
  currentPage.value = 1
  doSearch()
}

const handlePageChange = (val: number) => {
  currentPage.value = val
  doSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(() => route.query.keyword, (newVal) => {
  keyword.value = (newVal as string) || ''
  currentPage.value = 1
  doSearch()
})

onMounted(() => {
  keyword.value = (route.query.keyword as string) || ''
  loadCategories()
  doSearch()
})
</script>

<style scoped lang="scss">
.search-page {
  padding: 20px 0;
  background: #f5f7fa;
  min-height: 100vh;
}
.container {
  width: 1200px;
  margin: 0 auto;
}
.filter-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #ebeef5;

  .filter-row {
    display: flex;
    align-items: flex-start;
    .label { font-weight: bold; color: #333; margin-right: 15px; width: 60px; padding-top: 2px; }
    .options {
      flex: 1;
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      .option-item {
        cursor: pointer;
        color: #666;
        font-size: 14px;
        padding: 2px 8px;
        border-radius: 4px;
        &:hover { color: #409EFF; }
        &.active { color: #fff; background: #409EFF; font-weight: 500; }
      }
    }
  }

  .sort-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .sort-options {
      display: flex;
      align-items: center;
      gap: 20px;
      .label { font-weight: bold; color: #333; margin-right: 0; }
      .sort-item {
        cursor: pointer;
        font-size: 14px;
        color: #606266;
        display: flex;
        align-items: center;
        gap: 4px;
        &:hover, &.active { color: #409EFF; }
        &.active { font-weight: bold; }
      }
    }
    .result-count {
      font-size: 12px;
      color: #999;
      b { color: #f56c6c; margin: 0 4px; }
    }
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0,0,0,0.08);
    border-color: #e4e7ed;
  }

  .img-box {
    width: 100%;
    height: 240px;
    background: #f8f8f8;
    .el-image { width: 100%; height: 100%; }
    .image-slot { display: flex; justify-content: center; align-items: center; height: 100%; color: #909399; font-size: 12px; }
  }

  .info {
    padding: 12px;
    .name {
      font-size: 14px;
      color: #303133;
      margin-bottom: 8px;
      height: 40px;
      line-height: 20px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
    .price-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      .price { color: #f56c6c; font-weight: bold; font-size: 18px; }
      .sales { font-size: 12px; color: #999; }
    }
  }
}

.pagination-box {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}
</style>