<template>
  <div class="table-page">
    <h3 style="margin-bottom: 16px;">待接配送任务</h3>
    <el-table :data="tasks" v-loading="loading" border stripe>
      <el-table-column prop="id" label="配送ID" width="80" />
      <el-table-column prop="orderNo" label="订单编号" width="160" />
      <el-table-column prop="merchantName" label="取餐商家" width="150" />
      <el-table-column prop="merchantAddress" label="商家地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="customerAddress" label="送达地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }"><el-tag type="warning">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleAccept(row)">抢单</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" layout="total, prev, pager, next" @change="fetchData" style="margin-top:20px; justify-content:flex-end;" />

    <!-- 当前配送中 -->
    <h3 style="margin: 24px 0 16px;">进行中的配送</h3>
    <el-empty v-if="activeTasks.length === 0" description="暂无进行中的配送" />
    <el-table v-else :data="activeTasks" border stripe>
      <el-table-column prop="orderNo" label="订单编号" width="160" />
      <el-table-column prop="merchantName" label="商家" width="150" />
      <el-table-column prop="customerAddress" label="送达地址" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status==='待取餐'" size="small" type="warning" @click="handlePickup(row)">确认取餐</el-button>
          <el-button v-if="row.status==='配送中'" size="small" type="success" @click="handleDeliver(row)">确认送达</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 最近配送记录 -->
    <h3 style="margin: 24px 0 16px;">最近配送记录</h3>
    <el-table :data="recentHistory" v-loading="myLoading" border stripe>
      <el-table-column prop="orderNo" label="订单编号" width="160" />
      <el-table-column prop="merchantName" label="商家" width="150" />
      <el-table-column prop="customerAddress" label="送达地址" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }"><el-tag :type="row.status==='已送达'?'success':'info'">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="deliverTime" label="完成时间" width="180" />
    </el-table>
    <div style="text-align:center;margin-top:10px">
      <el-button type="primary" @click="$router.push('/rider/history')">查看全部历史 →</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const loading = ref(false), myLoading = ref(false)
const tasks = ref<any[]>([]), myTasks = ref<any[]>([])
const total = ref(0), page = ref(1)

// 分离进行中和已完成的配送
const activeTasks = computed(() => myTasks.value.filter(d => ['待取餐','配送中'].includes(d.status)))
const recentHistory = computed(() => myTasks.value.filter(d => d.status === '已送达').slice(0, 5))

async function fetchData() {
  loading.value = true; myLoading.value = true
  try {
    const [t, m] = await Promise.all([
      request.get('/rider/tasks', { params: { page: page.value, size: 10 } }),
      request.get('/rider/deliveries', { params: { page: 1, size: 20 } })
    ])
    tasks.value = t.data.records; total.value = t.data.total
    myTasks.value = m.data.records
  } finally { loading.value = false; myLoading.value = false }
}

async function handleAccept(row: any) { await request.put(`/rider/tasks/${row.id}/accept`); ElMessage.success('接单成功'); fetchData() }
async function handlePickup(row: any) { await request.put(`/rider/tasks/${row.id}/pickup`); ElMessage.success('已取餐'); fetchData() }
async function handleDeliver(row: any) { await request.put(`/rider/tasks/${row.id}/deliver`); ElMessage.success('已送达'); fetchData() }

fetchData()
</script>
