<template>
  <div class="table-page">
    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="订单状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="已提交" value="已提交" />
          <el-option label="待接单" value="待接单" />
          <el-option label="备餐中" value="备餐中" />
          <el-option label="待配送" value="待配送" />
          <el-option label="配送中" value="配送中" />
          <el-option label="已送达" value="已送达" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已取消" value="已取消" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="订单编号" width="160" />
      <el-table-column prop="customerName" label="顾客" width="100" />
      <el-table-column prop="merchantName" label="商家" width="150" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/orders/${row.orderNo}`)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      layout="total, prev, pager, next"
      @change="fetchData"
      style="margin-top: 20px; justify-content: flex-end;"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getAdminOrders } from '@/api/order'
import { formatDate } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ status: '', page: 1, size: 10 })

function statusType(status: string) {
  if (status === '已完成') return 'success'
  if (status === '已取消') return 'danger'
  if (status === '配送中') return 'warning'
  return 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminOrders(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

fetchData()
</script>
