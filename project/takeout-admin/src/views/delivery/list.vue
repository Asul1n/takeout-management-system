<template>
  <div class="table-page">
    <h3 style="margin-bottom: 16px;">配送记录</h3>
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="配送ID" width="80" />
      <el-table-column prop="orderNo" label="订单编号" width="160" />
      <el-table-column prop="riderName" label="骑手" width="100">
        <template #default="{ row }">{{ row.riderName || '未分配' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="配送状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '已送达' ? 'success' : row.status === '配送中' ? 'warning' : 'info'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="merchantName" label="商家" width="150" />
      <el-table-column prop="customerAddress" label="配送地址" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @change="fetchData"
      style="margin-top: 20px; justify-content: flex-end;"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getAdminDeliveries } from '@/api/delivery'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminDeliveries({ page: page.value, size: size.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

fetchData()
</script>
