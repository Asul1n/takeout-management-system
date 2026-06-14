<template>
  <div class="table-page">
    <h3 style="margin-bottom: 16px;">商家入驻审核</h3>
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商家名称" width="180" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column label="营业地址" min-width="200">
        <template #default="{ row }">{{ row.province }}{{ row.city }}{{ row.district }}{{ row.addressDetail }}</template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag type="warning">{{ row.auditStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handleAudit(row, '已通过')">通过</el-button>
          <el-button size="small" type="danger" @click="handleAudit(row, '已驳回')">驳回</el-button>
        </template>
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
import { getAuditList, auditMerchant } from '@/api/merchant'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function fetchData() {
  loading.value = true
  try {
    const res = await getAuditList({ page: page.value, size: size.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleAudit(row: any, auditStatus: string) {
  await auditMerchant(row.id, { auditStatus })
  ElMessage.success(`审核${auditStatus === '已通过' ? '通过' : '驳回'}`)
  fetchData()
}

fetchData()
</script>
