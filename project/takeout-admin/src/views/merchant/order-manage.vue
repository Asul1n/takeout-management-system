<template>
  <div class="table-page">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h3>本店订单</h3>
      <div style="display:flex;gap:12px;">
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:120px" @change="fetchData">
          <el-option label="已提交" value="已提交" /><el-option label="备餐中" value="备餐中" />
          <el-option label="待配送" value="待配送" /><el-option label="配送中" value="配送中" />
          <el-option label="已送达" value="已送达" /><el-option label="已完成" value="已完成" />
          <el-option label="已取消" value="已取消" />
        </el-select>
        <el-button @click="fetchData">刷新</el-button>
      </div>
    </div>

    <el-table :data="orders" v-loading="loading" border stripe @row-click="showDetail" style="cursor:pointer">
      <el-table-column prop="orderNo" label="订单编号" width="160" />
      <el-table-column prop="customerName" label="顾客" width="100" />
      <el-table-column prop="totalAmount" label="金额" width="90"><template #default="{ row }">¥{{ row.totalAmount }}</template></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" min-width="220">
        <template #default="{ row }">
          <el-button v-if="row.status==='已提交'" size="small" type="primary" @click.stop="handleAccept(row)">接单</el-button>
          <el-button v-if="row.status==='备餐中'" size="small" type="warning" @click.stop="handlePrepare(row)">备餐完成</el-button>
          <el-button size="small" @click.stop="showDetail(row)">详情</el-button>
          <el-button v-if="row.remark?.includes('申请取消')" size="small" type="danger" @click.stop="handleCancelReview(row,true)">同意取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" layout="total, prev, pager, next" @change="fetchData" style="margin-top:20px;justify-content:flex-end;" />

    <!-- 订单详情 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="订单编号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusType(detail.status)">{{ detail.status }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="顾客">{{ detail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ detail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ detail.address }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="detail" :data="detail.items" border stripe style="margin-top:12px">
        <el-table-column prop="dishName" label="菜品" />
        <el-table-column prop="quantity" label="数量" width="60" />
        <el-table-column label="小计" width="90"><template #default="{row}">¥{{ row.subtotal }}</template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
    const res = await request.get('/merchant/orders', { params }); orders.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}

async function showDetail(row: any) {
  try { const res = await request.get(`/orders/${row.orderNo}`); detail.value = res.data; detailVisible.value = true } catch {}
}
async function handleAccept(row: any) { await request.put(`/merchant/orders/${row.orderNo}/accept`); ElMessage.success('已接单'); fetchData() }
async function handlePrepare(row: any) { await request.put(`/merchant/orders/${row.orderNo}/prepare`); ElMessage.success('备餐完成'); fetchData() }
async function handleCancelReview(row: any, approved: boolean) {
  await request.put(`/merchant/orders/${row.orderNo}/cancel-review`, { approved })
  ElMessage.success(approved ? '已同意取消' : '已拒绝取消'); fetchData()
}

onMounted(fetchData)
</script>
