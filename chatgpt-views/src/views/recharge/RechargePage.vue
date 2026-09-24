<template>
  <div class="recharge-page">
    <div class="badge-row">
      <span v-for="b in badges" :key="b" class="badge">{{ b }}</span>
    </div>

    <div class="main-card">
      <div class="tabs">
        <button
          v-for="t in tabs"
          :key="t.key"
          class="tab"
          :class="{ active: activeTab === t.key }"
          @click="activeTab = t.key"
        >
          {{ t.label }}
        </button>
      </div>

      <div class="stepper">
        <div
          v-for="(s, idx) in steps"
          :key="s"
          class="step"
          :class="{ active: currentStep === idx + 1, done: currentStep > idx + 1 }"
        >
          <div class="dot">{{ idx + 1 }}</div>
          <div class="label">{{ s }}</div>
          <div v-if="idx < steps.length - 1" class="line" />
        </div>
      </div>

      <!-- Step 1 -->
      <div v-if="currentStep === 1" class="panel">
        <h2>输入套餐卡密</h2>
        <p class="sub">系统将根据卡密自动匹配对应套餐渠道</p>
        <label class="field-label">CDK 卡密</label>
        <a-input
          v-model:value="code"
          size="large"
          placeholder="请输入卡密"
          allow-clear
        />
        <p class="hint">请粘贴商城订单详情里发放的卡密。也可从订单「使用说明」链接带参跳转。</p>
        <a-button type="primary" class="primary-btn" :loading="loading" @click="onVerify">
          验证卡密
        </a-button>
      </div>

      <!-- Step 2 -->
      <div v-else-if="currentStep === 2" class="panel">
        <h2>读取登录状态</h2>
        <p class="sub">读取本站账号登录状态。不收集、不使用 ChatGPT accessToken/session。</p>
        <div class="status-box">
          <div><b>状态：</b>{{ loginInfo.loggedIn ? '已登录' : '演示模式（未登录）' }}</div>
          <div><b>用户：</b>{{ loginInfo.username }}（{{ loginInfo.userId }}）</div>
          <div class="muted">{{ loginInfo.tip }}</div>
        </div>
        <div class="actions">
          <a-button @click="currentStep = 1">上一步</a-button>
          <a-button type="primary" class="primary-btn" :loading="loading" @click="onReadLogin">
            确认登录状态
          </a-button>
        </div>
      </div>

      <!-- Step 3 -->
      <div v-else-if="currentStep === 3" class="panel">
        <h2>确认提交</h2>
        <p class="sub">请确认套餐信息后提交兑换</p>
        <div class="status-box" v-if="verifyInfo">
          <div><b>卡密：</b>{{ verifyInfo.code }}</div>
          <div><b>商品：</b>{{ verifyInfo.productName }}（{{ verifyInfo.productType }}）</div>
          <div><b>金额：</b>¥{{ verifyInfo.price }}</div>
          <div v-if="verifyInfo.durationDays"><b>时长：</b>{{ verifyInfo.durationDays }} 天</div>
        </div>
        <div class="actions">
          <a-button @click="currentStep = 2">上一步</a-button>
          <a-button type="primary" class="primary-btn" :loading="loading" @click="onRedeem">
            确认提交
          </a-button>
        </div>
      </div>

      <!-- Step 4 -->
      <div v-else class="panel">
        <h2>充值结果</h2>
        <p class="sub">订单 {{ orderNo }}</p>
        <a-progress :percent="progress" :status="progressStatus" />
        <div class="status-box">
          <div><b>订单状态：</b>{{ delivery?.orderStatus || '-' }}</div>
          <div><b>任务状态：</b>{{ delivery?.taskStatus || '-' }}</div>
          <div>{{ delivery?.resultMsg || '处理中…' }}</div>
        </div>
        <div class="actions">
          <a-button type="primary" class="primary-btn" @click="$router.push('/orders')">
            查看我的订单
          </a-button>
          <a-button @click="resetFlow">再兑一张</a-button>
        </div>
      </div>
    </div>

    <div class="query-card" @click="$router.push('/orders')">
      <div class="qi">🔍</div>
      <div class="qt">
        <div class="title">AI 订阅查询</div>
        <div class="desc">查询当前套餐、订阅状态、到期时间与订阅渠道</div>
      </div>
      <a-button type="primary" class="mini">立即查询</a-button>
    </div>

    <footer class="footer">© 2026 AI Recharge Service · Fully Automated · Safe and Reliable</footer>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getDeliveryStatus, getLoginStatus, redeemCdk, verifyCdk } from '@/api/mall'

const route = useRoute()
const badges = ['安全加密', '极速到账', '7x24小时', '全自动化']
const tabs = [
  { key: 'self', label: '自助充值' },
  { key: 'express', label: '极速充值' },
  { key: 'query', label: '卡密查询' },
  { key: 'guide', label: '使用教程' },
]
const steps = ['验证卡密', '读取登录状态', '确认提交', '充值结果']

const activeTab = ref('self')
const currentStep = ref(1)
const code = ref('')
const loading = ref(false)
const verifyInfo = ref(null)
const loginInfo = reactive({
  loggedIn: false,
  userId: 'demo',
  username: '演示用户',
  tip: '',
})
const orderNo = ref('')
const delivery = ref(null)
let timer = null

onMounted(() => {
  if (route.query.code) {
    code.value = String(route.query.code)
  }
})

const progress = computed(() => delivery.value?.progress ?? 0)
const progressStatus = computed(() => {
  const s = delivery.value?.taskStatus
  if (s === 'FAILED') return 'exception'
  if (s === 'SUCCESS') return 'success'
  return 'active'
})

async function onVerify() {
  loading.value = true
  try {
    const res = await verifyCdk(code.value)
    verifyInfo.value = res.result
    message.success('卡密验证通过')
    currentStep.value = 2
  } finally {
    loading.value = false
  }
}

async function onReadLogin() {
  loading.value = true
  try {
    const res = await getLoginStatus()
    Object.assign(loginInfo, res.result || {})
    message.success('已读取本站登录状态')
    currentStep.value = 3
  } finally {
    loading.value = false
  }
}

async function onRedeem() {
  loading.value = true
  try {
    const res = await redeemCdk(code.value)
    orderNo.value = res.result.orderNo
    currentStep.value = 4
    message.success('已提交兑换')
    startPoll()
  } finally {
    loading.value = false
  }
}

function startPoll() {
  stopPoll()
  const tick = async () => {
    if (!orderNo.value) return
    try {
      const res = await getDeliveryStatus(orderNo.value)
      delivery.value = res.result
      if (['SUCCESS', 'FAILED'].includes(res.result?.taskStatus)) {
        stopPoll()
      }
    } catch (e) {
      // ignore transient
    }
  }
  tick()
  timer = setInterval(tick, 2000)
}

function stopPoll() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function resetFlow() {
  stopPoll()
  currentStep.value = 1
  code.value = ''
  verifyInfo.value = null
  orderNo.value = ''
  delivery.value = null
}

onBeforeUnmount(stopPoll)
</script>

<style scoped>
.recharge-page {
  max-width: 760px;
  margin: 0 auto;
  padding: 32px 16px 48px;
}

.badge-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-bottom: 18px;
}

.badge {
  background: #fff;
  border: 1px solid #d1fae5;
  color: var(--brand-dark);
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.main-card {
  background: var(--card);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(16, 185, 129, 0.08);
  padding: 20px 24px 28px;
}

.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 22px;
}

.tab {
  border: none;
  background: #f8fafc;
  color: #475569;
  border-radius: 999px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
}

.tab.active {
  background: var(--brand);
  color: #fff;
}

.stepper {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin: 8px 0 28px;
  gap: 4px;
}

.step {
  flex: 1;
  position: relative;
  text-align: center;
}

.dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  margin: 0 auto 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e2e8f0;
  color: #64748b;
  font-weight: 600;
  font-size: 13px;
}

.step.active .dot,
.step.done .dot {
  background: var(--brand);
  color: #fff;
}

.label {
  font-size: 12px;
  color: #94a3b8;
}

.step.active .label {
  color: var(--brand-dark);
  font-weight: 600;
}

.line {
  position: absolute;
  top: 14px;
  left: calc(50% + 18px);
  width: calc(100% - 36px);
  height: 2px;
  background: #e2e8f0;
}

.panel h2 {
  margin: 0 0 8px;
  font-size: 22px;
}

.sub {
  margin: 0 0 20px;
  color: var(--muted);
  font-size: 14px;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
}

.hint {
  margin: 10px 0 20px;
  color: #94a3b8;
  font-size: 13px;
}

.primary-btn {
  width: 100%;
  height: 44px;
  background: var(--brand) !important;
  border-color: var(--brand) !important;
}

.primary-btn:hover {
  background: var(--brand-dark) !important;
  border-color: var(--brand-dark) !important;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.actions .primary-btn {
  flex: 1;
}

.status-box {
  background: var(--brand-soft);
  border: 1px solid #a7f3d0;
  border-radius: 12px;
  padding: 14px 16px;
  line-height: 1.8;
  font-size: 14px;
}

.muted {
  color: var(--muted);
}

.query-card {
  margin-top: 18px;
  background: #fff;
  border-radius: 14px;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  cursor: pointer;
}

.qi {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--brand-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.qt {
  flex: 1;
}

.qt .title {
  font-weight: 700;
}

.qt .desc {
  color: var(--muted);
  font-size: 13px;
  margin-top: 2px;
}

.mini {
  background: var(--brand) !important;
  border-color: var(--brand) !important;
}

.footer {
  text-align: center;
  color: #94a3b8;
  font-size: 12px;
  margin-top: 28px;
}
</style>
