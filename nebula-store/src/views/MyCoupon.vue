<template>
  <div class="my-coupon-page">
    <el-card shadow="never" style="border-radius: 12px; min-height: 500px;">
      <template #header>
        <div style="font-weight: bold; font-size: 18px;">My Coupons</div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="Unused" name="unused">
          <div class="coupon-list">
            <div v-for="item in unusedList" :key="item.id" class="coupon-item unused">
              <div class="left">
                <div class="amount"><small>¥</small>{{ item.amount }}</div>
                <div class="condition">Spend ¥{{ item.minPoint }} to use</div>
              </div>
              <div class="right">
                <div class="info">
                  <div class="name">{{ item.name }}</div>
                  <div class="date">{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</div>
                </div>
                <el-button type="primary" size="small" round @click="goHome">Use now</el-button>
              </div>
              <div class="circle-top"></div>
              <div class="circle-bottom"></div>
            </div>
            <el-empty v-if="unusedList.length === 0" description="No available coupons" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="Used" name="used">
          <div class="coupon-list">
            <div v-for="item in usedList" :key="item.id" class="coupon-item used">
              <div class="left">
                <div class="amount"><small>¥</small>{{ item.amount }}</div>
                <div class="condition">Spend ¥{{ item.minPoint }} to use</div>
              </div>
              <div class="right">
                <div class="info">
                  <div class="name">{{ item.name }}</div>
                  <div class="date">Used on: {{ formatTime(item.useTime) }}</div>
                </div>
                <div class="status-stamp">Used</div>
              </div>
              <div class="circle-top"></div>
              <div class="circle-bottom"></div>
            </div>
            <el-empty v-if="usedList.length === 0" description="No records" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="Expired" name="expired">
          <div class="coupon-list">
            <div v-for="item in expiredList" :key="item.id" class="coupon-item expired">
              <div class="left">
                <div class="amount"><small>¥</small>{{ item.amount }}</div>
                <div class="condition">Spend ¥{{ item.minPoint }} to use</div>
              </div>
              <div class="right">
                <div class="info">
                  <div class="name">{{ item.name }}</div>
                  <div class="date">Valid until: {{ formatTime(item.endTime) }}</div>
                </div>
                <div class="status-stamp">Expired</div>
              </div>
              <div class="circle-top"></div>
              <div class="circle-bottom"></div>
            </div>
            <el-empty v-if="expiredList.length === 0" description="No records" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getMyCoupons } from '@/api/store'
import { useRouter } from 'vue-router'

interface MyCoupon {
  id: number
  name: string
  amount: number
  minPoint: number
  startTime: string
  endTime: string
  useTime?: string
  status: number // 0: unused 1: used 2: expired
}

const router = useRouter()
const activeTab = ref('unused')
const allCoupons = ref<MyCoupon[]>([])

// Status 0: unused 1: used 2: expired
const unusedList = computed(() => allCoupons.value.filter(c => c.status === 0))
const usedList = computed(() => allCoupons.value.filter(c => c.status === 1))
const expiredList = computed(() => allCoupons.value.filter(c => c.status === 2))

const loadData = async () => {
  try {
    const res = await getMyCoupons() as unknown as MyCoupon[]
    allCoupons.value = res || []
  } catch (error) {
    console.error(error)
  }
}

const goHome = () => {
  router.push('/')
}

const formatTime = (time: string) => {
  return time ? time.split('T')[0] : ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.my-coupon-page { padding: 20px 0; }
.coupon-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.coupon-item {
  width: 360px;
  height: 100px;
  display: flex;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  position: relative;
  transition: all 0.3s;

  // Semicircle decorations on both sides
  .circle-top, .circle-bottom {
    position: absolute;
    width: 20px;
    height: 20px;
    background: #fff;
    border-radius: 50%;
    left: 100px;
    z-index: 2;
  }
  .circle-top { top: -10px; }
  .circle-bottom { bottom: -10px; }

  // Left amount section
  .left {
    width: 110px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;
    position: relative;
    border-right: 1px dashed rgba(255,255,255,0.3);

    .amount { font-size: 30px; font-weight: bold; small { font-size: 14px; margin-right: 2px; } }
    .condition { font-size: 12px; margin-top: 5px; opacity: 0.9; }
  }

  // Right info section
  .right {
    flex: 1;
    padding: 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;

    .info {
      .name { font-weight: bold; color: #333; margin-bottom: 8px; font-size: 15px; }
      .date { font-size: 12px; color: #999; }
    }
  }

  // Status styles: unused
  &.unused {
    .left { background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%); }
    &:hover { transform: translateY(-3px); }
  }

  // Status styles: used
  &.used {
    .left { background: #dcdfe6; color: #909399; }
    .status-stamp {
      position: absolute;
      right: 10px;
      top: 10px;
      border: 2px solid #ccc;
      color: #ccc;
      padding: 2px 5px;
      border-radius: 4px;
      transform: rotate(-15deg);
      font-weight: bold;
      opacity: 0.6;
    }
  }

  // Status styles: expired
  &.expired {
    .left { background: #e9e9eb; color: #a8abb2; }
    .status-stamp {
      position: absolute;
      right: 10px;
      top: 10px;
      border: 2px solid #a8abb2;
      color: #a8abb2;
      padding: 2px 5px;
      border-radius: 4px;
      transform: rotate(-15deg);
      font-weight: bold;
      opacity: 0.6;
    }
  }
}
</style>
