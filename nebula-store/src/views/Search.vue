<template>
  <div class="search-page">
    <div class="container">
      <div class="search-hero">
        <div class="search-title">
          <h2>Handpicked for You</h2>
          <p v-if="keyword">Keyword: <span class="highlight">{{ keyword }}</span></p>
          <p v-else>Discover more inspiration</p>
        </div>
        <div class="search-meta">
          <span>Found <strong>{{ displayTotal }}</strong> products</span>
          <span class="muted">Updated at {{ lastUpdated }}</span>
        </div>
      </div>

      <div class="filter-card">
        <div class="filter-row" v-if="categoryList.length > 0">
          <span class="label">Category:</span>
          <div class="options">
            <span class="option-item" :class="{ active: !categoryId }" @click="handleCategory(undefined)">
              All
            </span>
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

        <el-divider style="margin: 16px 0" />

        <div class="filter-row">
          <span class="label">Price:</span>
          <div class="price-range">
            <el-input-number v-model="priceMin" :min="0" :controls="false" placeholder="Min price" />
            <span class="split">-</span>
            <el-input-number v-model="priceMax" :min="0" :controls="false" placeholder="Max price" />
            <el-button type="primary" plain size="small" @click="applyFilters">Apply</el-button>
          </div>

          <div class="stock-filter">
            <el-switch v-model="inStockOnly" />
            <span>In stock only</span>
          </div>

          <div class="rating-filter">
            <span>Rating:</span>
            <el-rate v-model="minRating" allow-half show-score score-template="{value}+" />
          </div>
        </div>

        <el-divider style="margin: 16px 0" />

        <div class="sort-row">
          <div class="sort-options">
            <span class="label">Sort:</span>
            <span class="sort-item" :class="{ active: sortType === 'default' }" @click="handleSort('default')">
              Relevance
            </span>
            <span class="sort-item" :class="{ active: sortType === 'sale' }" @click="handleSort('sale')">
              Sales
            </span>
            <span class="sort-item price-sort" :class="{ active: sortType === 'price' }" @click="handleSort('price')">
              Price
              <el-icon v-if="sortOrder === 'asc' && sortType === 'price'"><CaretTop /></el-icon>
              <el-icon v-else-if="sortOrder === 'desc' && sortType === 'price'"><CaretBottom /></el-icon>
              <el-icon v-else><DArrowRight style="transform: rotate(90deg)" /></el-icon>
            </span>
          </div>

          <div class="active-filters" v-if="hasActiveFilters">
            <el-tag v-if="keyword" closable @close="clearKeyword" class="tag">Keyword: {{ keyword }}</el-tag>
            <el-tag v-if="categoryName" closable @close="handleCategory(undefined)" class="tag">
              Category: {{ categoryName }}
            </el-tag>
            <el-tag v-if="priceMin !== null" closable @close="priceMin = null" class="tag">Min ¥{{ priceMin }}</el-tag>
            <el-tag v-if="priceMax !== null" closable @close="priceMax = null" class="tag">Max ¥{{ priceMax }}</el-tag>
            <el-tag v-if="inStockOnly" closable @close="inStockOnly = false" class="tag">In stock only</el-tag>
            <el-tag v-if="minRating > 0" closable @close="minRating = 0" class="tag">Rating {{ minRating }}+</el-tag>

            <el-button link type="primary" size="small" @click="resetFilters">Clear filters</el-button>
          </div>
        </div>
      </div>

      <div class="product-grid" v-if="!loading">
        <div v-for="item in displayList" :key="item.id" class="product-item" @click="router.push(`/product/${item.id}`)">
          <div class="img-box">
            <el-image :src="item.mainImage" fit="cover" lazy>
              <template #placeholder>
                <div class="image-slot">Loading...</div>
              </template>
            </el-image>
          </div>
          <div class="info">
            <div class="name" :title="item.name" v-html="highlightKeyword(item.name)"></div>
            <div class="price-row">
              <span class="price">¥{{ item.price }}</span>
              <span class="sales">{{ item.sale || 0 }} purchases</span>
            </div>
          </div>
        </div>
      </div>

      <div class="product-grid" v-else>
        <el-skeleton v-for="item in 8" :key="item" animated class="product-skeleton">
          <template #template>
            <div class="skeleton-card"></div>
          </template>
        </el-skeleton>
      </div>

      <EmptyState
        v-if="!loading && displayList.length === 0"
        description="No matching products found. Try another keyword?"
        action-text="Browse home"
        action-to="/"
      >
        <template #image>
          <svg viewBox="0 0 220 160" aria-hidden="true" class="empty-illustration">
            <defs>
              <linearGradient id="searchGradient" x1="0" x2="1" y1="0" y2="1">
                <stop offset="0%" stop-color="#34d399" />
                <stop offset="100%" stop-color="#60a5fa" />
              </linearGradient>
            </defs>
            <rect x="20" y="24" width="180" height="112" rx="18" fill="#ecfeff" />
            <circle cx="95" cy="82" r="28" fill="url(#searchGradient)" opacity="0.9" />
            <path d="M122 102l26 22" stroke="#64748b" stroke-width="8" stroke-linecap="round" />
          </svg>
        </template>

        <div class="suggestions">
          <span>Suggested keywords:</span>
          <el-button v-for="item in recommendedKeywords" :key="item" size="small" @click="searchKeyword(item)">
            {{ item }}
          </el-button>
        </div>
      </EmptyState>

      <div class="pagination-box" v-if="total > 0 && !hasActiveFilters">
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
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchProducts, getCategoryList } from '@/api/store'
import { CaretTop, CaretBottom, DArrowRight } from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'

interface Product {
  id: number
  name: string
  mainImage: string
  price: number
  sale?: number
  stock?: number
  rating?: number
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
const lastUpdated = ref('')

const priceMin = ref<number | null>(null)
const priceMax = ref<number | null>(null)
const inStockOnly = ref(false)
const minRating = ref(0)

const recommendedKeywords = ['Headphones', 'Smartwatch', 'Home essentials', 'Trending styles']

const categoryName = computed(() => {
  if (!categoryId.value) return ''
  return categoryList.value.find((item) => item.id === categoryId.value)?.name || ''
})

const hasActiveFilters = computed(() => {
  return Boolean(
    keyword.value ||
      categoryId.value ||
      priceMin.value !== null ||
      priceMax.value !== null ||
      inStockOnly.value ||
      minRating.value > 0
  )
})

const filteredList = computed(() => {
  let list = [...productList.value]
  if (priceMin.value !== null) {
    list = list.filter((item) => item.price >= (priceMin.value as number))
  }
  if (priceMax.value !== null) {
    list = list.filter((item) => item.price <= (priceMax.value as number))
  }
  if (inStockOnly.value) {
    list = list.filter((item) => (item.stock ?? 0) > 0)
  }
  if (minRating.value > 0) {
    list = list.filter((item) => (item.rating ?? 0) >= minRating.value)
  }
  if (sortType.value === 'sale') {
    list = list.sort((a, b) => (b.sale ?? 0) - (a.sale ?? 0))
  }
  return list
})

const displayList = computed(() => {
  if (sortType.value === 'sale' || hasActiveFilters.value) {
    return filteredList.value
  }
  return productList.value
})
const displayTotal = computed(() => (hasActiveFilters.value ? filteredList.value.length : total.value))

const syncFromRoute = () => {
  const queryKeyword = route.query.keyword as string | undefined
  keyword.value = queryKeyword?.trim() || ''
}

const loadCategories = async () => {
  try {
    const res: any = await getCategoryList()
    categoryList.value = res || []
  } catch (e) {
    console.error('Failed to load categories', e)
  }
}

const doSearch = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined
    }

    if (sortType.value === 'price' && sortOrder.value) {
      params.sort = sortOrder.value === 'asc' ? 'price_asc' : 'price_desc'
    }

    const res: any = await searchProducts(params)
    productList.value = res.records || res.data || []
    total.value = res.total || 0
    lastUpdated.value = new Date().toLocaleTimeString()
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
      sortOrder.value = 'asc'
    }
  } else {
    sortOrder.value = ''
  }
  sortType.value = type
  currentPage.value = 1
  doSearch()
}

const handlePageChange = () => {
  doSearch()
}

const applyFilters = () => {
  currentPage.value = 1
  doSearch()
}

const resetFilters = () => {
  priceMin.value = null
  priceMax.value = null
  inStockOnly.value = false
  minRating.value = 0
  doSearch()
}

const clearKeyword = () => {
  keyword.value = ''
  router.replace({ path: '/search', query: {} })
  currentPage.value = 1
  doSearch()
}

const searchKeyword = (value: string) => {
  keyword.value = value
  router.replace({ path: '/search', query: { keyword: value } })
  currentPage.value = 1
  doSearch()
}

onMounted(() => {
  syncFromRoute()
  loadCategories()
  doSearch()
})

watch(
  () => route.query.keyword,
  () => {
    syncFromRoute()
    currentPage.value = 1
    doSearch()
  }
)
</script>

<style scoped lang="scss">
.search-page {
  padding: 24px 0 60px;
  background: #f5f7fa;
  min-height: 80vh;
}

.container {
  width: 1200px;
  margin: 0 auto;
}

.search-hero {
  background: linear-gradient(120deg, #dbeafe 0%, #f0f9ff 100%);
  padding: 28px 32px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);

  h2 {
    margin: 0 0 8px;
    font-size: 26px;
    color: #1e293b;
  }

  .highlight {
    color: #2563eb;
    font-weight: 600;
  }

  .search-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
    text-align: right;
    color: #64748b;
  }

  .muted {
    font-size: 12px;
  }
}

.filter-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  margin-bottom: 20px;
}

.filter-row,
.sort-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.label {
  color: #64748b;
  font-size: 13px;
  min-width: 48px;
}

.options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.option-item {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  border: 1px solid transparent;
  background: #f8fafc;

  &.active,
  &:hover {
    background: #dbeafe;
    color: #2563eb;
    border-color: #bfdbfe;
  }
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.split {
  color: #94a3b8;
}

.stock-filter,
.rating-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 14px;
}

.sort-item {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f1f5f9;
  cursor: pointer;
  color: #475569;
  display: inline-flex;
  align-items: center;
  gap: 4px;

  &.active {
    background: #2563eb;
    color: #fff;
  }
}

.active-filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-left: auto;
}

.tag {
  border-radius: 999px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.product-item {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 26px rgba(15, 23, 42, 0.12);
  }

  .img-box {
    width: 100%;
    height: 210px;
    overflow: hidden;
  }

  .info {
    padding: 12px 14px 16px;
  }

  .name {
    font-weight: 600;
    font-size: 14px;
    color: #1f2937;
    margin-bottom: 10px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #64748b;
    font-size: 12px;
  }

  .price {
    color: #ef4444;
    font-weight: 700;
    font-size: 16px;
  }
}

.product-skeleton {
  border-radius: 14px;
}

.skeleton-card {
  height: 280px;
  background: #eef2f7;
  border-radius: 14px;
}

.image-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #94a3b8;
  font-size: 12px;
}

.pagination-box {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.suggestions {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  color: #64748b;
}

@media (max-width: 1280px) {
  .container {
    width: 100%;
    padding: 0 20px;
  }
  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .search-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
