<template>
  <div class="table-page">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h3>我的订单</h3>
      <div style="display:flex;gap:12px;">
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:120px" @change="fetchData">
          <el-option label="已提交" value="已提交" /><el-option label="待接单" value="待接单" />
          <el-option label="备餐中" value="备餐中" /><el-option label="待配送" value="待配送" />
          <el-option label="配送中" value="配送中" /><el-option label="已送达" value="已送达" />
          <el-option label="已完成" value="已完成" /><el-option label="已取消" value="已取消" />
        </el-select>
        <el-button @click="fetchData">刷新</el-button>
      </div>
    </div>

    <el-table :data="orders" v-loading="loading" border stripe @row-click="showDetail" style="cursor:pointer">
      <el-table-column prop="orderNo" label="订单编号" width="160" />
      <el-table-column prop="merchantName" label="商家" width="150" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="['已提交','待接单'].includes(row.status)" size="small" type="danger" @click.stop="handleCancel(row)">取消</el-button>
          <el-button v-if="row.status==='已送达'" size="small" type="primary" @click.stop="handleConfirm(row)">确认收货</el-button>
          <el-button v-if="['备餐中','待配送','配送中'].includes(row.status)" size="small" type="warning" @click.stop="handleRequestCancel(row)">申请取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" layout="total, prev, pager, next" @change="fetchData" style="margin-top:20px; justify-content:flex-end;" />

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="订单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusType(detail.status)">{{ detail.status }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="商家">{{ detail.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ detail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ detail.address }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="detail" :data="detail.items" border stripe style="margin-top:12px">
        <el-table-column prop="dishName" label="菜品" />
        <el-table-column prop="unitPrice" label="单价" width="80"><template #default="{row}">¥{{ row.unitPrice }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" width="60" />
        <el-table-column label="小计" width="90"><template #default="{row}">¥{{ row.subtotal }}</template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const loading = ref(false), orders = ref<any[]>([]), total = ref(0), page = ref(1)
const filterStatus = ref(''), detailVisible = ref(false), detail = ref<any>(null)

function statusType(s: string) { return s === '已完成' ? 'success' : s === '已取消' ? 'danger' : s === '配送中' ? 'warning' : 'info' }

async function fetchData() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: 10 }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await request.get('/customer/orders', { params })
    orders.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}

async function showDetail(row: any) {
  try {
    const res = await request.get(`/orders/${row.orderNo}`); detail.value = res.data; detailVisible.value = true
  } catch {}
}

async function handleCancel(row: any) { await request.put(`/customer/orders/${row.orderNo}/cancel`); ElMessage.success('已取消'); fetchData() }
async function handleConfirm(row: any) { await request.put(`/customer/orders/${row.orderNo}/confirm`); ElMessage.success('已确认收货'); fetchData() }
async function handleRequestCancel(row: any) {
  await request.put(`/customer/orders/${row.orderNo}/request-cancel`)
  ElMessage.success('已提交取消申请，等待商家同意')
}

fetchData()
</script>
