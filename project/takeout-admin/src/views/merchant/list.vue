<template>
  <div class="table-page">
    <el-form :inline="true" :model="query" class="search-form">
      <el-form-item label="商家名称">
        <el-input v-model="query.keyword" placeholder="请输入商家名称" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchData">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商家名称" width="180" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column label="营业地址" min-width="200">
        <template #default="{ row }">{{ row.province }}{{ row.city }}{{ row.district }}{{ row.addressDetail }}</template>
      </el-table-column>
      <el-table-column prop="bizStatus" label="营业状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.bizStatus === '营业中' ? 'success' : 'info'">{{ row.bizStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.auditStatus === '已通过' ? 'success' : row.auditStatus === '待审核' ? 'warning' : 'danger'">
            {{ row.auditStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="monthlySales" label="月销量" width="100" />
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
import { getMerchantList } from '@/api/merchant'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, size: 10 })

async function fetchData() {
  loading.value = true
  try {
    const res = await getMerchantList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

fetchData()
</script>
