<template>
  <div>
    <h2>卡密库存</h2>
    <a-space style="margin:12px 0">
      <a-select v-model:value="status" allow-clear placeholder="状态" style="width:140px"
        :options="[{value:'UNUSED',label:'未售'},{value:'RESERVED',label:'已发出'},{value:'USED',label:'已激活'}]"
        @change="load" />
      <a-button @click="load">刷新</a-button>
    </a-space>
    <a-table :columns="columns" :data-source="rows" row-key="id" :pagination="false" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { listCdk } from '@/api/admin'

const rows = ref([])
const status = ref()
const columns = [
  { title: '卡密', dataIndex: 'code' },
  { title: '商品ID', dataIndex: 'productId', width: 140 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '持有人', dataIndex: 'usedBy', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
]

async function load() {
  const res = await listCdk({ pageNo: 1, pageSize: 50, status: status.value })
  rows.value = res.result?.records || []
}
onMounted(load)
</script>
