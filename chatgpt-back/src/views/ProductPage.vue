<template>
  <div>
    <div class="hd">
      <h2>商品上架</h2>
      <a-button type="primary" @click="openEdit()">新建商品</a-button>
    </div>
    <a-table :columns="columns" :data-source="rows" row-key="id" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="openEdit(record)">编辑</a-button>
            <a-button size="small" @click="gen(record)">生成卡密</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="open" :title="form.id ? '编辑商品' : '新建商品'" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="名称"><a-input v-model:value="form.name" /></a-form-item>
        <a-form-item label="类型"><a-input v-model:value="form.productType" placeholder="Plus/Go/..." /></a-form-item>
        <a-form-item label="分类">
          <a-select v-model:value="form.category" :options="[{value:'AI_SUB',label:'AI订阅'},{value:'READY_ACCOUNT',label:'成品账号'}]" />
        </a-form-item>
        <a-form-item label="价格"><a-input-number v-model:value="form.price" :min="0" style="width:100%" /></a-form-item>
        <a-form-item label="天数"><a-input-number v-model:value="form.durationDays" :min="0" style="width:100%" /></a-form-item>
        <a-form-item label="分期期数"><a-input-number v-model:value="form.periods" :min="1" style="width:100%" /></a-form-item>
        <a-form-item label="激活站 URL"><a-input v-model:value="form.redeemUrl" /></a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="[{value:'ON',label:'上架'},{value:'OFF',label:'下架'}]" />
        </a-form-item>
        <a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { batchCdk, listProducts, saveProduct } from '@/api/admin'

const rows = ref([])
const open = ref(false)
const form = reactive({})
const columns = [
  { title: '名称', dataIndex: 'name' },
  { title: '类型', dataIndex: 'productType', width: 90 },
  { title: '价格', dataIndex: 'price', width: 90 },
  { title: '期数', dataIndex: 'periods', width: 70 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '操作', key: 'action', width: 180 },
]

async function load() {
  const res = await listProducts()
  rows.value = res.result || []
}

function openEdit(row) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row || {
    name: '', productType: 'Plus', category: 'AI_SUB', price: 128, durationDays: 30,
    periods: 1, status: 'ON', redeemUrl: 'http://127.0.0.1:5173/recharge', remark: '',
  })
  open.value = true
}

async function save() {
  await saveProduct({ ...form })
  message.success('已保存')
  open.value = false
  load()
}

function gen(record) {
  Modal.confirm({
    title: `为「${record.name}」生成 5 张卡密？`,
    async onOk() {
      const res = await batchCdk({ productId: record.id, count: 5, expireDays: 365 })
      message.success(`已生成：${(res.result || []).join(', ')}`)
    },
  })
}

onMounted(load)
</script>

<style scoped>
.hd { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
h2 { margin:0; }
</style>
