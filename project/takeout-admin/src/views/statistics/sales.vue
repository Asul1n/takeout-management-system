<template>
  <div class="statistics-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span>订单统计</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总订单数">{{ orderStats.totalOrders }}</el-descriptions-item>
            <el-descriptions-item label="已完成">{{ orderStats.completedOrders }}</el-descriptions-item>
            <el-descriptions-item label="已取消">{{ orderStats.cancelledOrders }}</el-descriptions-item>
            <el-descriptions-item label="完成率">{{ orderStats.completionRate }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>营收统计</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总营收">¥{{ revenueStats.totalRevenue }}</el-descriptions-item>
            <el-descriptions-item label="日均营收">¥{{ revenueStats.avgDailyRevenue }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 骑手绩效 -->
    <el-card style="margin-top: 20px;">
      <template #header><span>骑手绩效</span></template>
      <el-table :data="riderList" border stripe>
        <el-table-column prop="riderName" label="骑手姓名" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '空闲' ? 'success' : row.status === '配送中' ? 'warning' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalDeliveries" label="累计配送" width="120" />
        <el-table-column prop="todayDeliveries" label="今日配送" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOrderStats, getRevenueStats, getRiderStats } from '@/api/statistics'

const orderStats = reactive({ totalOrders: 0, completedOrders: 0, cancelledOrders: 0, completionRate: '0%' })
const revenueStats = reactive({ totalRevenue: 0, avgDailyRevenue: 0 })
const riderList = ref([])

onMounted(async () => {
  try {
    const [oRes, rRes, rdRes] = await Promise.all([
      getOrderStats({ period: 'week' }),
      getRevenueStats({ period: 'week' }),
      getRiderStats()
    ])
    Object.assign(orderStats, oRes.data)
    Object.assign(revenueStats, rRes.data)
    riderList.value = rdRes.data
  } catch {}
})
</script>
