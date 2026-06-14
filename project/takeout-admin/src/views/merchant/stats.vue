<template>
  <div class="table-page">
    <h3 style="margin-bottom: 16px;">营业统计</h3>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card><template #header>订单概览</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总订单数">{{ stats.totalOrders }}</el-descriptions-item>
            <el-descriptions-item label="已完成">{{ stats.completedOrders }}</el-descriptions-item>
            <el-descriptions-item label="已取消">{{ stats.cancelledOrders }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card><template #header>营收概况</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总营收">¥{{ revenue.totalRevenue }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'

const stats = reactive({ totalOrders: 0, completedOrders: 0, cancelledOrders: 0 })
const revenue = reactive({ totalRevenue: 0 })
onMounted(async () => {
  const [o, r] = await Promise.all([request.get('/merchant/statistics/orders'), request.get('/merchant/statistics/revenue')])
  Object.assign(stats, o.data); Object.assign(revenue, r.data)
})
</script>
