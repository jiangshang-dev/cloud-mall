<template>
  <div class="orders-page">
    <div class="alert">
      <span>！部分商品激活可能存在延迟，请留意到账情况。</span>
      <a-button size="small">请留意</a-button>
    </div>

    <div class="toolbar">
      <div class="tabs">
        <button
          class="tab"
          :class="{ active: category === 'AI_SUB' }"
          @click="switchTab('AI_SUB')"
        >
          AI 订阅
        </button>
        <button
          class="tab"
          :class="{ active: category === 'READY_ACCOUNT' }"
          @click="switchTab('READY_ACCOUNT')"
        >
          成品账号
        </button>
      </div>
      <a-button @click="load">刷新</a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      row-key="id"
      :pagination="false"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'product'">
          <div class="p-name">{{ record.productName }}</div>
          <div class="p-sub">{{ categoryLabel(record.category) }}</div>
          <div v-if="record.installmentTip" class="p-tip">{{ record.installmentTip }}</div>
        </template>
        <template v-else-if="column.key === 'order'">
          <div class="mono">{{ record.orderNo }}</div>
          <div class="p-sub">{{ record.createTime }}</div>
        </template>
        <template v-else-if="column.key === 'amount'">
          ¥{{ record.amount }}
        </template>
        <template v-else-if="column.key === 'status'">
          <span class="tag" :class="statusClass(record.status)">{{ statusText(record.status) }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="onView(record)">查看</a-button>
            <a-button
              v-if="['PAID', 'PROCESSING', 'COMPLETED'].includes(record.status)"
              size="small"
              @click="onRefund(record)"
            >
              退款
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <div class="pager">
      <div>共 {{ total }} 条，显示 {{ rangeText }}</div>
      <a-pagination
        v-model:current="pageNo"
        v-model:page-size="pageSize"
        :total="total"
        show-size-changer
        @change="load"
        @showSizeChange="load"
      />
    </div>

    <a-modal v-model:open="detailOpen" title="订单详情" :footer="null">
      <pre v-if="detail">{{ JSON.stringify(detail, null, 2) }}</pre>
    </a-modal>

    <button class="fab">在线客服</button>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { applyRefund, getOrderDetail, getOrderList } from '@/api/mall'

const columns = [
  { title: '订单·类型', key: 'product' },
  { title: '订单号·创建时间', key: 'order' },
  { title: '金额', key: 'amount', width: 120 },
  { title: '状态', key: 'status', width: 120 },
  { title: '操作', key: 'action', width: 180 },
]

const category = ref('AI_SUB')
const loading = ref(false)
const rows = ref([])
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailOpen = ref(false)
const detail = ref(null)

const rangeText = computed(() => {
  if (!total.value) return '0-0'
  const start = (pageNo.value - 1) * pageSize.value + 1
  const end = Math.min(pageNo.value * pageSize.value, total.value)
  return `${start}-${end}`
})

function categoryLabel(c) {
  return c === 'READY_ACCOUNT' ? '成品账号' : 'AI 订阅'
}

function statusText(s) {
  const map = {
    PAID: '已支付',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUNDING: '退款中',
    REFUNDED: '已退款',
  }
  return map[s] || s
}

function statusClass(s) {
  if (s === 'COMPLETED') return 'ok'
  if (s === 'PAID' || s === 'PROCESSING') return 'pay'
  if (s === 'CANCELLED' || s === 'REFUNDED') return 'mute'
  return 'pay'
}

async function load() {
  loading.value = true
  try {
    const res = await getOrderList({
      category: category.value,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    })
    rows.value = res.result?.records || []
    total.value = res.result?.total || 0
  } finally {
    loading.value = false
  }
}

function switchTab(c) {
  category.value = c
  pageNo.value = 1
  load()
}

async function onView(record) {
  const res = await getOrderDetail(record.orderNo)
  detail.value = res.result
  detailOpen.value = true
}

function onRefund(record) {
  Modal.confirm({
    title: '确认申请退款？',
    content: `订单 ${record.orderNo}`,
    async onOk() {
      await applyRefund(record.orderNo)
      message.success('已提交退款申请')
      load()
    },
  })
}

onMounted(load)
</script>

<style scoped>
.orders-page {
  padding: 20px 24px 80px;
}

.alert {
  background: #1e3a5f;
  color: #fff;
  border-radius: 10px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 13px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tabs {
  display: flex;
  gap: 18px;
}

.tab {
  border: none;
  background: transparent;
  padding: 8px 2px;
  cursor: pointer;
  color: #64748b;
  border-bottom: 2px solid transparent;
}

.tab.active {
  color: #2563eb;
  border-bottom-color: #2563eb;
  font-weight: 600;
}

.p-name {
  font-weight: 600;
}

.p-sub {
  color: #94a3b8;
  font-size: 12px;
}

.p-tip {
  color: #2563eb;
  font-size: 12px;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 6px;
  font-size: 12px;
}

.tag.pay {
  background: #dbeafe;
  color: #1d4ed8;
}

.tag.ok {
  background: #dcfce7;
  color: #15803d;
}

.tag.mute {
  background: #f1f5f9;
  color: #64748b;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  color: #64748b;
  font-size: 13px;
}

.fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  border: none;
  background: #7c3aed;
  color: #fff;
  border-radius: 999px;
  padding: 12px 18px;
  box-shadow: 0 8px 24px rgba(124, 58, 237, 0.35);
  cursor: pointer;
}
</style>
