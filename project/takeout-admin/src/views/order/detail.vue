<template>
  <div class="table-page">
    <el-page-header @back="$router.back()" :content="'订单详情 - ' + orderNo" style="margin-bottom: 20px;" />

    <el-descriptions v-if="order" :column="2" border>
      <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="订单状态">
        <el-tag :type="statusType(order.status)">{{ order.status }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="顾客">{{ order.customerName }}</el-descriptions-item>
      <el-descriptions-item label="商家">{{ order.merchantName }}</el-descriptions-item>
      <el-descriptions-item label="总金额">¥{{ order.totalAmount }}</el-descriptions-item>
      <el-descriptions-item label="下单时间">{{ formatDate(order.createTime) }}</el-descriptions-item>
      <el-descriptions-item label="配送地址" :span="2">{{ order.address }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
    </el-descriptions>

    <!-- 配送信息 -->
    <el-descriptions v-if="order" :column="2" border style="margin-top: 20px;" title="配送信息">
      <el-descriptions-item label="配送状态">
        <el-tag>{{ order.deliveryStatus || '暂无' }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="骑手">{{ order.riderName || '未分配' }}</el-descriptions-item>
    </el-descriptions>

    <!-- 订单明细 -->
    <el-table v-if="order" :data="order.items" border stripe style="margin-top: 20px;">
      <el-table-column prop="dishName" label="菜品名称" />
      <el-table-column prop="unitPrice" label="单价">
        <template #default="{ row }">¥{{ row.unitPrice }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="subtotal" label="小计">
        <template #default="{ row }">¥{{ row.subtotal }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail } from '@/api/order'
import { formatDate } from '@/utils/format'

const route = useRoute()
const orderNo = route.params.orderNo as string
const order = ref<any>(null)

function statusType(status: string) {
  if (status === '已完成') return 'success'
  if (status === '已取消') return 'danger'
  if (status === '配送中') return 'warning'
  return 'info'
}

onMounted(async () => {
  const res = await getOrderDetail(orderNo)
  order.value = res.data
})
</script>
