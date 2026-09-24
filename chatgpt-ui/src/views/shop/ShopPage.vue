<template>
  <div class="shop">
    <h2>AI 订阅套餐</h2>
    <p class="sub">下单后自动发放激活卡密，请到激活站完成充值</p>
    <div class="grid">
      <div v-for="p in products" :key="p.id" class="card">
        <div class="type">{{ p.productType }}</div>
        <div class="name">{{ p.name }}</div>
        <div class="price">¥{{ p.price }}</div>
        <div class="meta">
          <span v-if="p.durationDays">{{ p.durationDays }} 天</span>
          <span>分 {{ p.periods || 1 }} 期发货</span>
        </div>
        <a-button type="primary" block :loading="buying === p.id" @click="buy(p)">立即购买</a-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { createOrder, listProducts } from '@/api/mall'

const router = useRouter()
const products = ref([])
const buying = ref('')

async function load() {
  const res = await listProducts()
  products.value = res.result || []
}

async function buy(p) {
  buying.value = p.id
  try {
    const res = await createOrder(p.id)
    message.success('下单成功，第 1 期卡密已发放')
    router.push({ name: 'orders', query: { orderNo: res.result?.order?.orderNo } })
  } finally {
    buying.value = ''
  }
}

onMounted(load)
</script>

<style scoped>
.shop { padding:24px; }
h2 { margin:0 0 6px; }
.sub { color:#64748b; margin:0 0 20px; }
.grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(220px,1fr)); gap:16px; }
.card { background:#fff; border-radius:14px; padding:18px; box-shadow:0 4px 16px rgba(0,0,0,.04); }
.type { color:#2563eb; font-size:12px; font-weight:600; }
.name { font-size:18px; font-weight:700; margin:8px 0; }
.price { font-size:22px; color:#0f172a; margin-bottom:10px; }
.meta { display:flex; gap:10px; color:#94a3b8; font-size:12px; margin-bottom:14px; }
</style>
