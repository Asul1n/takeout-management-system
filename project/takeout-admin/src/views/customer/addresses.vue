<template>
  <div class="table-page">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <div style="display:flex;align-items:center;gap:12px;">
        <h3>收货地址</h3>
        <el-button size="small" @click="showProfile">修改资料</el-button>
      </div>
      <el-button type="primary" @click="showAdd">新增地址</el-button>
    </div>
    <el-table :data="addresses" v-loading="loading" border stripe>
      <el-table-column prop="province" label="省" width="100" />
      <el-table-column prop="city" label="市" width="100" />
      <el-table-column prop="district" label="区" width="100" />
      <el-table-column prop="detail" label="详细地址" min-width="200" />
      <el-table-column prop="isDefault" label="默认" width="80">
        <template #default="{ row }"><el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="!row.isDefault" size="small" @click="setDefault(row)">设为默认</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="profileVisible" title="修改个人资料" width="400px">
      <el-form label-width="80px">
        <el-form-item label="姓名"><el-input v-model="profileForm.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="profileForm.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileVisible=false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" title="新增地址" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="省"><el-input v-model="form.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="form.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.detail" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const addresses = ref<any[]>([])
const dialogVisible = ref(false)
const profileVisible = ref(false)
const profileForm = reactive({ name: '', phone: '' })
const form = reactive({ province: '', city: '', district: '', detail: '' })

async function showProfile() {
  const res = await request.get('/user/me')
  profileForm.name = res.data.name; profileForm.phone = res.data.phone
  profileVisible.value = true
}
async function saveProfile() {
  await request.put('/user/me', { name: profileForm.name, phone: profileForm.phone })
  ElMessage.success('资料已更新'); profileVisible.value = false
}
async function fetchData() {
  loading.value = true
  try { const res = await request.get('/customer/addresses'); addresses.value = res.data } finally { loading.value = false }
}

function showAdd() { form.province = ''; form.city = ''; form.district = ''; form.detail = ''; dialogVisible.value = true }
async function handleAdd() { await request.post('/customer/addresses', form); dialogVisible.value = false; ElMessage.success('已添加'); fetchData() }
async function setDefault(row: any) { await request.put(`/customer/addresses/${row.id}/default`); ElMessage.success('已设为默认'); fetchData() }
async function handleDelete(row: any) { await request.delete(`/customer/addresses/${row.id}`); ElMessage.success('已删除'); fetchData() }

fetchData()
</script>
