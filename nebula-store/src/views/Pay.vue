<template>
  <div class="pay-page" v-loading="pageLoading">
    <div class="container">
      <el-card shadow="never" class="pay-card" v-if="orderInfo">
        <template #header>
          <div class="header">
            <div class="title">收银台</div>
            <div class="secure-tips">
              <span>安全支付</span>
              <span>官方保障</span>
              <span>极速到账</span>
            </div>
          </div>
        </template>

        <div class="pay-content">
          <div class="summary-banner">
            <el-icon color="#22c55e" size="50"><CircleCheckFilled /></el-icon>
            <div class="text">
              <h3>订单提交成功！请尽快完成支付</h3>
              <p class="order-no">{{ isTradePay ? '交易单号' : '订单号' }}：{{ orderInfo.orderNo }}</p>
              <p class="expire-tip" v-if="isTradePay">此交易可能包含多个子订单，支付一次即可全部完成</p>
              <p class="expire-tip" v-else>
                请在 <span class="countdown">{{ formatTime(timeLeft) }}</span> 内完成支付，否则订单将自动取消
              </p>
            </div>
            <div class="amount">
              应付金额：<span>¥ {{ orderInfo.totalAmount }}</span>
            </div>
          </div>

          <div class="pay-grid">
            <div class="left-panel">
              <div class="panel-title">选择支付方式</div>
              <div class="method-list">
                <div
                  class="method-item"
                  :class="{ active: payType === 'wechat' }"
                  @click="payType = 'wechat'"
                >
                  <el-icon color="#09bb07" size="24"><ChatDotRound /></el-icon>
                  微信支付
                  <el-icon v-if="payType === 'wechat'" class="check"><Select /></el-icon>
                </div>
                <div
                  class="method-item"
                  :class="{ active: payType === 'alipay' }"
                  @click="payType = 'alipay'"
                >
                  <el-icon color="#1677ff" size="24"><Wallet /></el-icon>
                  支付宝
                  <el-icon v-if="payType === 'alipay'" class="check"><Select /></el-icon>
                </div>
              </div>

              <div class="tips-card">
                <h4>支付须知</h4>
                <ul>
                  <li>支付后预计 30 分钟内确认到账</li>
                  <li>如遇问题可在订单页发起退款</li>
                  <li>请确保支付设备网络稳定</li>
                </ul>
              </div>
            </div>

            <div class="right-panel">
              <div class="qr-code-area">
                <div class="qr-box">
                  <div class="mock-qr">
                    <el-icon size="120" color="#333"><FullScreen /></el-icon>
                    <div class="qr-text">Nebula Pay</div>
                  </div>
                  <p>请使用{{ payType === 'wechat' ? '微信' : '支付宝' }}扫一扫</p>
                  <el-button
                    type="primary"
                    size="large"
                    :loading="paying"
                    @click="handlePay"
                    class="pay-btn"
                  >
                    {{ paying ? '支付处理中...' : `确认支付 ¥${orderInfo.totalAmount}` }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <el-empty v-else-if="!pageLoading" description="订单不存在或已失效">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { payOrder, getOrderDetailByNo } from '@/api/store'
import request from '@/utils/request' // [修复] 引入通用 request
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, ChatDotRound, Wallet, FullScreen, Select } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tradeNoParam = route.query.tradeNo as string
const orderNoParam = route.query.orderNo as string
const amountParam = route.query.amount as string

const payType = ref('wechat')
const paying = ref(false)
const pageLoading = ref(true)
const orderInfo = ref<any>(null)
const timeLeft = ref(1800) // 30分钟倒计时
let timer: any = null

const isTradePay = computed(() => !!tradeNoParam)

const initData = async () => {
  if (!tradeNoParam && !orderNoParam) {
    ElMessage.error('支付参数缺失')
    pageLoading.value = false
    return
  }

  // 1. 聚合支付模式 (购物车合并下单)
  if (tradeNoParam) {
    orderInfo.value = {
      orderNo: tradeNoParam,
      totalAmount: amountParam || '0.00',
      status: 0
    }
    startTimer()
    pageLoading.value = false
    return
  }

  // 2. 单订单模式 (订单列表去支付)
  try {
    const res: any = await getOrderDetailByNo(orderNoParam)
    const orderData = res.order || res // 兼容不同的后端返回结构

    if (orderData) {
      if (orderData.status !== 0) {
        ElMessage.warning('该订单状态不可支付')
        router.push('/order')
        return
      }
      orderInfo.value = orderData
      startTimer()
    }
  } catch (error) {
    console.error(error)
  } finally {
    pageLoading.value = false
  }
}

const startTimer = () => {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      clearInterval(timer)
    }
  }, 1000)
}

const formatTime = (seconds: number) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

const handlePay = async () => {
  if (!orderInfo.value?.orderNo) return
  paying.value = true

  try {
    // [修复] 使用 request 工具发送请求，自动携带 Token
    if (isTradePay.value) {
      // 假设后端有聚合支付接口，或者依然调用 payOrder (视后端实现而定)
      // 这里演示调用通用支付接口
      await request({ url: `/pay/trade/${orderInfo.value.orderNo}`, method: 'post' })
    } else {
      await payOrder(orderInfo.value.orderNo)
    }

    ElMessage.success('支付成功！')
    router.replace('/order')
  } catch (e: any) {
    // request.ts 已处理错误
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  initData()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
.pay-page { padding: 40px 0; background: #f5f7fa; min-height: 80vh; }
.container { width: 1200px; margin: 0 auto; }
.pay-card { border-radius: 14px; min-height: 600px; }

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  .secure-tips {
    display: flex;
    gap: 12px;
    font-size: 12px;
    color: #64748b;
    span { background: #f1f5f9; padding: 4px 10px; border-radius: 999px; }
  }
}

.pay-content { padding: 10px 20px 30px; }

.summary-banner {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  .text {
    flex: 1;
    h3 { margin: 0 0 8px; font-size: 20px; color: #333; }
    .order-no { margin: 0 0 5px; color: #666; font-size: 14px; }
    .expire-tip { margin: 0; color: #909399; font-size: 13px; }
    .countdown { color: #f56c6c; font-weight: 700; }
  }
  .amount {
    font-size: 14px; color: #666; margin-top: 10px;
    span { font-size: 30px; color: #f56c6c; font-weight: 800; font-family: Arial, sans-serif; margin-left: 5px; }
  }
}

.pay-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 20px;
  margin-top: 24px;
}

.left-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  .panel-title { font-weight: bold; margin-bottom: 20px; font-size: 16px; border-left: 4px solid #409EFF; padding-left: 10px; }
}

.method-list {
  display: flex;
  gap: 20px;
  .method-item {
    flex: 1;
    border: 2px solid #eee;
    padding: 18px 20px;
    border-radius: 10px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 16px;
    transition: all 0.2s;
    position: relative;

    &:hover { border-color: #b3d8ff; }
    &.active {
      border-color: #409EFF;
      background: #ecf5ff;
      color: #409EFF;
      font-weight: bold;
      .check { position: absolute; right: -8px; top: -8px; background: white; border-radius: 50%; color: #409EFF; }
    }
  }
}

.tips-card {
  margin-top: 20px;
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  border: 1px dashed #cbd5f5;
  h4 { margin: 0 0 10px; font-size: 14px; }
  ul { margin: 0; padding-left: 18px; color: #64748b; font-size: 13px; line-height: 1.7; }
}

.right-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
}

.qr-code-area {
  text-align: center;
  background-color: #fafafa;
  padding: 30px 0;
  border-radius: 12px;

  .qr-box {
    display: inline-flex;
    flex-direction: column;
    align-items: center;

    .mock-qr {
      background: white;
      padding: 15px;
      margin-bottom: 15px;
      border: 1px solid #eee;
      border-radius: 8px;
      position: relative;
      .qr-text { position: absolute; bottom: 5px; width: 100%; text-align: center; font-size: 10px; color: #999; }
    }
    p { color: #666; margin-bottom: 10px; }
  }
}

.pay-btn { width: 220px; margin-top: 10px; font-weight: bold; }
</style>
