<template>
  <div>
    <h2>交付任务（激活站）</h2>
    <a-button style="margin:12px 0" @click="load">刷新</a-button>
    <a-table :columns="columns" :data-source="rows" row-key="id" :pagination="false" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { listDelivery } from '@/api/admin'

const rows = ref([])
const columns = [
  { title: '订单号', dataIndex: 'orderNo' },
  { title: '状态', dataIndex: 'status', width: 120 },
  { title: '进度', dataIndex: 'progress', width: 80 },
  { title: '结果', dataIndex: 'resultMsg' },
  { title: '时间', dataIndex: 'updateTime', width: 180 },
]

async function load() {
  const res = await listDelivery({ pageNo: 1, pageSize: 50 })
  rows.value = res.result?.records || []
}
onMounted(load)
</script>
