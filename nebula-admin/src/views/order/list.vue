<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form inline :model="queryParams">
        <el-form-item label="订单号">
          <el-input
              v-model="queryParams.orderNo"
              placeholder="请输入订单号"
              clearable
              @keyup.enter="handleSearch"
              style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待支付" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="售后中" :value="5" />
            <el-option label="已退款" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表内容 -->
    <el-card shadow="never" class="table-card" style="margin-top: 20px;">
      <!-- 状态 Tabs -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="-1"></el-tab-pane>
        <el-tab-pane label="待发货" name="1"></el-tab-pane>
        <el-tab-pane label="已发货" name="2"></el-tab-pane>
        <el-tab-pane label="售后/退款" name="5"></el-tab-pane>
        <el-tab-pane label="已完成" name="3"></el-tab-pane>
      </el-tabs>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" show-overflow-tooltip />

        <el-table-column label="订单金额" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="收货信息" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div>{{ row.receiverName }} <span style="color: #999">|</span> {{ row.receiverPhone }}</div>
            <div style="font-size: 12px; color: #909399;">{{ row.receiverAddress }}</div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="下单时间" width="170" align="center" />

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>

            <el-button
                v-if="row.status === 1"
                type="success"
                link
                @click="openDeliverDialog(row)"
            >发货</el-button>

            <el-button
                v-if="row.status === 5"
                type="warning"
                link
                @click="openAuditDialog(row)"
            >审核</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 20px; text-align: right;">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog title="订单详情" v-model="detailVisible" width="800px">
      <div v-if="detailData.order" class="detail-container">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ detailData.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailData.order.status)">
              {{ getStatusText(detailData.order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付金额">¥{{ detailData.order.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detailData.order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailData.order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detailData.order.receiverAddress }}</el-descriptions-item>

          <template v-if="detailData.order.logisticsCompany">
            <el-descriptions-item label="物流公司">{{ detailData.order.logisticsCompany }}</el-descriptions-item>
            <el-descriptions-item label="物流单号">{{ detailData.order.trackingNo }}</el-descriptions-item>
          </template>
        </el-descriptions>

        <!-- 售后信息 -->
        <div v-if="detailData.order.status === 5 || detailData.order.status === 6" style="margin-top: 20px;">
          <el-alert
              :title="'退款原因: ' + detailData.order.refundReason"
              type="warning"
              :closable="false"
              show-icon
          />
        </div>

        <!-- 商品清单 -->
        <div style="margin-top: 20px; font-weight: bold; margin-bottom: 10px;">商品清单</div>
        <el-table :data="detailData.items" border style="width: 100%">
          <el-table-column label="商品图片" width="80" align="center">
            <template #default="{ row }">
              <el-image
                  :src="row.mainImage"
                  style="width: 50px; height: 50px"
                  fit="cover"
                  preview-teleported
                  :preview-src-list="[row.mainImage]"
              />
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
          <el-table-column prop="currentUnitPrice" label="单价" width="100" align="center">
            <template #default="{ row }">¥{{ row.currentUnitPrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column prop="totalPrice" label="小计" width="100" align="center">
            <template #default="{ row }">
              <span style="color: #f56c6c;">¥{{ row.totalPrice }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog title="订单发货" v-model="deliverVisible" width="500px">
      <el-form :model="deliverForm" label-width="80px">
        <el-form-item label="订单号">
          <el-input v-model="deliverForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="物流公司">
          <el-select v-model="deliverForm.logisticsCompany" placeholder="请选择物流" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="邮政EMS" value="邮政EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="deliverForm.trackingNo" placeholder="请输入运单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDeliver">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 售后审核弹窗 -->
    <el-dialog title="售后审核" v-model="auditVisible" width="500px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="订单号">
          <el-input v-model="auditForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="退款原因" v-if="currentOrder">
          <el-input type="textarea" :model-value="currentOrder.refundReason" disabled :rows="2"/>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.pass">
            <el-radio :value="true">同意退款</el-radio>
            <el-radio :value="false">拒绝申请</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="拒绝理由" v-if="!auditForm.pass">
          <el-input v-model="auditForm.remark" type="textarea" placeholder="请填写拒绝理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">提交结果</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, deliverOrder, auditRefund } from '@/api/order'

// --- 列表逻辑 ---
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const activeTab = ref('-1')

const queryParams = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  status: null as number | null
})

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getOrderList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.page = 1; loadData() }
const resetQuery = () => { queryParams.orderNo = ''; queryParams.status = null; activeTab.value = '-1'; handleSearch() }

const handleTabClick = () => {
  queryParams.status = activeTab.value === '-1' ? null : Number(activeTab.value)
  queryParams.page = 1
  loadData()
}

// --- 详情逻辑 ---
const detailVisible = ref(false)
const detailData = ref<{ order: any, items: any[] }>({ order: null, items: [] })

const handleDetail = async (row: any) => {
  try {
    const res: any = await getOrderDetail(row.id)
    // 后端返回 Map: { "order": {}, "items": [] }
    detailData.value = res.data
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// --- 发货逻辑 ---
const deliverVisible = ref(false)
const deliverForm = reactive({
  orderNo: '',
  logisticsCompany: '顺丰速运',
  trackingNo: ''
})

const openDeliverDialog = (row: any) => {
  deliverForm.orderNo = row.orderNo
  deliverForm.trackingNo = ''
  deliverVisible.value = true
}

const confirmDeliver = async () => {
  if (!deliverForm.trackingNo) {
    ElMessage.warning('请输入物流单号')
    return
  }
  await deliverOrder(deliverForm)
  ElMessage.success('发货成功')
  deliverVisible.value = false
  loadData()
}

// --- 审核逻辑 ---
const auditVisible = ref(false)
const currentOrder = ref<any>(null)
const auditForm = reactive({
  orderNo: '',
  pass: true,
  remark: ''
})

const openAuditDialog = (row: any) => {
  currentOrder.value = row
  auditForm.orderNo = row.orderNo
  auditForm.pass = true
  auditForm.remark = ''
  auditVisible.value = true
}

const confirmAudit = async () => {
  if (!auditForm.pass && !auditForm.remark) {
    ElMessage.warning('拒绝申请时必须填写理由')
    return
  }
  await auditRefund(auditForm)
  ElMessage.success(auditForm.pass ? '已同意退款' : '已拒绝退款申请')
  auditVisible.value = false
  loadData()
}

// --- 状态工具 ---
const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付', 1: '待发货', 2: '已发货', 3: '已完成', 4: '已取消', 5: '售后中', 6: '已退款'
  }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  if (status === 3) return 'success'
  if (status === 0 || status === 1) return 'warning'
  if (status === 2) return 'primary'
  if (status === 5) return 'danger'
  return 'info'
}

onMounted(loadData)
</script>

<style scoped>
.detail-container {
  padding: 10px;
}
</style>