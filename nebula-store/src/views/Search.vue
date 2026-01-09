<template>
  <div class="search-page">
    <div class="container">
      <div class="search-hero">
        <div class="search-title">
          <h2>为你精选好物</h2>
          <p v-if="keyword">关键词：<span class="highlight">{{ keyword }}</span></p>
          <p v-else>探索更多灵感好物</p>
        </div>
        <div class="search-meta">
          <span>共找到 <strong>{{ displayTotal }}</strong> 件商品</span>
          <span class="muted">更新于 {{ lastUpdated }}</span>
        </div>
      </div>

      <!-- 顶部过滤栏 -->
      <div class="filter-bar">
        <!-- 分类筛选 -->
        <div class="filter-row" v-if="categoryList.length > 0">
          <span class="label">分类：</span>
          <div class="options">
            <span class="option-item" :class="{ active: !categoryId }" @click="handleCategory(undefined)"
              >全部</span
            >
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

        <div class="filter-row">
          <span class="label">价格：</span>
          <div class="price-range">
            <el-input-number v-model="priceMin" :min="0" :controls="false" placeholder="最低价" />
            <span class="split">-</span>
            <el-input-number v-model="priceMax" :min="0" :controls="false" placeholder="最高价" />
            <el-button type="primary" plain size="small" @click="applyFilters">应用</el-button>
          </div>

          <div class="stock-filter">
            <el-switch v-model="inStockOnly" />
            <span>仅看有货</span>
          </div>

          <div class="rating-filter">
            <span>评分：</span>
            <el-rate v-model="minRating" allow-half show-score score-template="{value}+" />
          </div>
        </div>

        <el-divider style="margin: 15px 0" />

        <!-- 排序选项 -->
        <div class="sort-row">
          <div class="sort-options">
            <span class="label">排序：</span>
            <span class="sort-item" :class="{ active: sortType === 'default' }" @click="handleSort('default')"
              >综合</span
            >
            <span class="sort-item" :class="{ active: sortType === 'sale' }" @click="handleSort('sale')"
              >销量</span
            >
            <span class="sort-item price-sort" :class="{ active: sortType === 'price' }" @click="handleSort('price')">
              价格
              <el-icon v-if="sortOrder === 'asc' && sortType === 'price'"><CaretTop /></el-icon>
              <el-icon v-else-if="sortOrder === 'desc' && sortType === 'price'"><CaretBottom /></el-icon>
              <el-icon v-else><DArrowRight style="transform: rotate(90deg)" /></el-icon>
            </span>
          </div>

          <!-- 激活筛选标签（合并：用 el-tag 可关闭） -->
          <div class="active-filters" v-if="hasActiveFilters">
            <el-tag v-if="keyword" closable @close="clearKeyword" class="tag">关键词：{{ keyword }}</el-tag>
            <el-tag v-if="categoryName" closable @close="handleCategory(undefined)" class="tag"
              >分类：{{ categoryName }}</el-tag
            >
            <el-tag v-if="priceMin !== null" closable @close="priceMin = null" class="tag">最低 ¥{{ priceMin }}</el-tag>
            <el-tag v-if="priceMax !== null" closable @close="priceMax = null" class="tag">最高 ¥{{ priceMax }}</el-tag>
            <el-tag v-if="inStockOnly" closable @close="inStockOnly = false" class="tag">仅看有货</el-tag>
            <el-tag v-if="minRating > 0" closable @close="minRating = 0" class="tag">评分 {{ minRating }}+</el-tag>

            <el-button link type="primary" size="small" @click="resetFilters">清空筛选</el-button>
          </div>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="product-grid" v-if="!loading">
        <div v-for="item in displayList" :key="item.id" class="product-item" @click="router.push(`/product/${item.id}`)">
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

      <div class="product-grid" v-else>
        <el-skeleton v-for="item in 8" :key="item" animated class="product-skeleton">
          <template #template>
            <div class="skeleton-card"></div>
          </template>
        </el-skeleton>
      </div>

      <!-- 空状态：使用 EmptyState（合并 codex） -->
      <EmptyState
        v-if="!loading && displayList.length === 0"
        description="未找到相关商品，换个词试试？"
        action-text="去首页逛逛"
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
          <span>推荐关键词：</span>
          <el-button v-for="item in recommendedKeywords" :key="item" size="small" @click="searchKeyword(item)">
            {{ item }}
          </el-button>
        </div>
      </EmptyState>

      <!-- 分页：有筛选时隐藏（保留你原逻辑） -->
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
  sale: number
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

/** 合并 codex：更多筛选条件 */
const priceMin = ref<number | null>(null)
const priceMax = ref<number | null>(null)
const inStockOnly = ref(false)
const minRating = ref(0)

const recommendedKeywords = ['耳机', '智能手表', '家居好物', '潮流穿搭']

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

/** 前端本地过滤（用于展示用） */
const filteredList = computed(() => {
  let list = [...productList.value]
  if (priceMin.value !== null) list = list.filter((item) => item.price >= (priceMin.value || 0))
  if (priceMax.value !== null) list = list.filter((item) => item.price <= (priceMax.value || 0))
  if (inStockOnly.value) list = list.filter((item) => (item.stock ?? 1) > 0)
  if (minRating.value > 0) list = list.filter((item) => (item.rating ?? 0) >= minRating.value)
  return list
})

const displayList = computed(() => (hasActiveFilters.value ? filteredList.value : productList.value))
const displayTotal = computed(() => (hasActiveFilters.value ? filteredList.value.length : total.value))

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
      categoryId: categoryId.value,
      minPrice: priceMin.value,
      maxPrice: priceMax.value,
      inStock: inStockOnly.value,
      minRating: minRating.value
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
    }
