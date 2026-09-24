<template>
  <div class="orders-page">
    <div class="alert">
      <span>！部分商品激活可能存在延迟，请留意到账情况。</span>
      <a-button size="small">请留意</a-button>
    </div>

    <div class="toolbar">
      <div class="tabs">
        <button class="tab" :class="{ active: category === 'AI_SUB' }" @click="switchTab('AI_SUB')">AI 订阅</button>
        <button class="tab" :class="{ active: category === 'READY_ACCOUNT' }" @click="switchTab('READY_ACCOUNT')">成品账号</button>
      </div>
      <a-button @click="load">刷新</a-button>
    </div>

    <a-table :columns="columns" :data-source="rows" :loading="loading" row-key="id" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'product'">
          <div class="p-name">{{ record.productName }}</div>
          <div class="p-sub">AI 订阅</div>
          <div v-if="record.installmentTip" class="p-tip">{{ record.installmentTip }}</div>
        </template>
        <template v-else-if="column.key === 'order'">
          <div class="mono">{{ record.orderNo }}</div>
          <div class="p-sub">{{ record.createTime }}</div>
        </template>
        <template v-else-if="column.key === 'amount'">¥{{ record.amount }}</template>
        <template v-else-if="column.key === 'status'">
          <span class="tag pay">{{ statusText(record.status) }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="onView(record)">查看</a-button>
            <a-button size="small" @click="onRefund(record)">退款</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <div class="pager">
      <div>共 {{ total }} 条</div>
      <a-pagination v-model:current="pageNo" v-model:page-size="pageSize" :total="total" @change="load" />
    </div>

    <a-modal v-model:open="detailOpen" title="订单详情" width="640px" :footer="null">
      <div v-if="detail" class="detail">
        <div class="row"><span>订单号</span><b class="mono">{{ detail.order.orderNo }}</b></div>
        <div class="row"><span>商品</span><b>{{ detail.order.productName }}</b></div>
        <div class="row"><span>类型</span><b>AI 订阅</b></div>
        <div class="row"><span>金额</span><b>¥{{ detail.order.amount }}</b></div>
        <div class="row"><span>状态</span><span class="tag pay">{{ statusText(detail.order.status) }}</span></div>
        <div class="row"><span>创建时间</span><b>{{ detail.order.createTime }}</b></div>

        <template v-if="detail.installments?.length">
          <h4 class="sec">分月发放进度</h4>
          <div class="progress-line">
            已发放 {{ detail.order.issuedPeriods || 0 }}/{{ detail.order.periods || 1 }} 期
            <span v-if="detail.nextExpectTip"> · {{ detail.nextExpectTip }}</span>
          </div>
          <div v-for="it in detail.installments" :key="it.id" class="inst">
            <div class="inst-h">
              第 {{ it.periodNo }} 期
              <span class="tag" :class="it.status === 'ISSUED' || it.status === 'ACTIVATED' ? 'ok' : 'mute'">
                {{ instStatus(it.status) }}
              </span>
            </div>
            <div class="p-sub">预计发放：{{ it.expectTime || '-' }}</div>
            <div class="p-sub">实际发放：{{ it.issueTime || '-' }}</div>

            <div v-if="it.cdkCode" class="ship-box">
              <div class="ship-title">第 {{ it.periodNo }} 期发货内容</div>
              <div class="cdk-row">
                <span>卡密</span>
                <code>{{ it.cdkCode }}</code>
                <a-button size="small" @click="copy(it.cdkCode)">复制</a-button>
              </div>
              <div class="p-sub">备注：{{ it.remark || '系统自动发货' }}</div>
              <div class="guide">
                <div>使用说明</div>
                <div>1. 打开激活站：
                  <a :href="activateLink(it.cdkCode)" target="_blank">{{ detail.redeemUrl }}</a>
                </div>
                <div>2. 粘贴卡密完成激活（本站登录态，非 ChatGPT session 代操作）</div>
                <div>3. 若权益未即时更新，请重新登录后刷新</div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </a-modal>

    <button class="fab">在线客服</button>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { applyRefund, listOrders, orderDetail } from '@/api/mall'

const route = useRoute()
const columns = [
  { title: '订单·类型', key: 'product' },
  { title: '订单号·创建时间', key: 'order' },
  { title: '金额', key: 'amount', width: 110 },
  { title: '状态', key: 'status', width: 110 },
  { title: '操作', key: 'action', width: 160 },
]

const category = ref('AI_SUB')
const loading = ref(false)
const rows = ref([])
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailOpen = ref(false)
const detail = ref(null)

function statusText(s) {
  return ({ PAID: '已支付', PROCESSING: '处理中', COMPLETED: '已完成', REFUNDING: '退款中', REFUNDED: '已退款', CANCELLED: '已取消' })[s] || s
}
function instStatus(s) {
  return ({ PENDING: '待发放', ISSUED: '已发放', ACTIVATED: '已激活' })[s] || s
}

async function load() {
  loading.value = true
  try {
    const res = await listOrders({ category: category.value, orderType: 'SALE', pageNo: pageNo.value, pageSize: pageSize.value })
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
  const res = await orderDetail(record.orderNo)
  detail.value = res.result
  detailOpen.value = true
}

function onRefund(record) {
  Modal.confirm({
    title: '确认申请退款？',
    async onOk() {
      await applyRefund(record.orderNo)
      message.success('已提交退款申请')
      load()
    },
  })
}

function copy(text) {
  navigator.clipboard.writeText(text)
  message.success('已复制')
}

function activateLink(code) {
  const base = detail.value?.redeemUrl || 'http://127.0.0.1:5173/recharge'
  return `${base}?code=${encodeURIComponent(code || '')}`
}

onMounted(async () => {
  await load()
  if (route.query.orderNo) {
    await onView({ orderNo: route.query.orderNo })
  }
})
</script>

<style scoped>
.orders-page { padding:20px 24px 80px; }
.alert { background:#1e3a5f; color:#fff; border-radius:10px; padding:12px 16px; display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; font-size:13px; }
.toolbar { display:flex; justify-content:space-between; margin-bottom:12px; }
.tabs { display:flex; gap:18px; }
.tab { border:none; background:transparent; padding:8px 2px; cursor:pointer; color:#64748b; border-bottom:2px solid transparent; }
.tab.active { color:#2563eb; border-bottom-color:#2563eb; font-weight:600; }
.p-name { font-weight:600; }
.p-sub { color:#94a3b8; font-size:12px; }
.p-tip { color:#2563eb; font-size:12px; }
.mono { font-family:ui-monospace,Menlo,monospace; font-size:12px; }
.tag { display:inline-block; padding:2px 10px; border-radius:6px; font-size:12px; }
.tag.pay { background:#dbeafe; color:#1d4ed8; }
.tag.ok { background:#dcfce7; color:#15803d; }
.tag.mute { background:#f1f5f9; color:#64748b; }
.pager { margin-top:16px; display:flex; justify-content:space-between; align-items:center; }
.detail .row { display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid #f1f5f9; gap:12px; }
.sec { margin:18px 0 8px; }
.progress-line { color:#2563eb; font-size:13px; margin-bottom:12px; }
.inst { margin-bottom:14px; }
.inst-h { display:flex; justify-content:space-between; font-weight:600; margin-bottom:4px; }
.ship-box { margin-top:10px; background:#ecfdf5; border:1px solid #a7f3d0; border-radius:12px; padding:12px 14px; }
.ship-title { font-weight:700; margin-bottom:8px; }
.cdk-row { display:flex; align-items:center; gap:10px; margin-bottom:8px; flex-wrap:wrap; }
.cdk-row code { background:#fff; padding:4px 8px; border-radius:6px; }
.guide { margin-top:10px; font-size:13px; line-height:1.7; color:#334155; }
.guide a { color:#2563eb; word-break:break-all; }
.fab { position:fixed; right:24px; bottom:24px; border:none; background:#7c3aed; color:#fff; border-radius:999px; padding:12px 18px; cursor:pointer; }
</style>
