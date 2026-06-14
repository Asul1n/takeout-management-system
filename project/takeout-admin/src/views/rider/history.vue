<template>
  <div class="table-page">
    <h3 style="margin-bottom: 16px;">配送记录</h3>
    <el-table :data="deliveries" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="订单编号" width="160" />
      <el-table-column prop="merchantName" label="商家" width="150" />
      <el-table-column prop="customerAddress" label="送达地址" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }"><el-tag :type="row.status==='已送达'?'success':'info'">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="pickupTime" label="取餐时间" width="180" />
      <el-table-column prop="deliverTime" label="送达时间" width="180" />
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" layout="total, prev, pager, next" @change="fetchData" style="margin-top:20px; justify-content:flex-end;" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '@/api/request'

const loading = ref(false), deliveries = ref<any[]>([]), total = ref(0), page = ref(1)

async function fetchData() {
  loading.value = true
  try { const res = await request.get('/rider/deliveries', { params: { page: page.value, size: 10 } }); deliveries.value = res.data.records; total.value = res.data.total }
  finally { loading.value = false }
}

fetchData()
</script>
