<template>
  <div class="table-page">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <div style="display:flex;align-items:center;gap:12px;">
        <h3>菜品管理</h3>
        <el-switch v-model="isOpen" active-text="营业中" inactive-text="休息中" @change="toggleBizStatus" />
        <el-button size="small" @click="showProfile">修改资料</el-button>
      </div>
      <el-button type="primary" @click="showAdd">添加菜品</el-button>
    </div>

    <el-table :data="dishes" v-loading="loading" border stripe>
      <el-table-column prop="name" label="菜品名称" width="150" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '上架' ? 'success' : 'info'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleToggle(row)" :type="row.status==='上架'?'warning':'success'">
            {{ row.status === '上架' ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" @click="handleStock(row)">库存</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 修改资料对话框 -->
    <el-dialog v-model="profileVisible" title="修改商家资料" width="400px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="profileForm.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="profileForm.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileVisible=false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加菜品对话框 -->
    <el-dialog v-model="dialogVisible" title="添加菜品" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="菜品名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="adding">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dishes = ref<any[]>([])
const categories = ref<any[]>([])
const dialogVisible = ref(false)
const adding = ref(false)
const isOpen = ref(true)
const profileVisible = ref(false)
const profileForm = reactive({ name: '', phone: '' })
const form = reactive({ name: '', price: 0.01, categoryId: 0, stock: 0, description: '' })

async function toggleBizStatus(val: boolean) {
  await request.put('/merchant/me/biz-status', { bizStatus: val ? '营业中' : '休息中' })
  ElMessage.success(val ? '已设为营业中' : '已设为休息中')
}

async function showProfile() {
  const res = await request.get('/user/me')
  profileForm.name = res.data.name
  profileForm.phone = res.data.phone
  profileVisible.value = true
}

async function saveProfile() {
  await request.put('/user/me', { name: profileForm.name, phone: profileForm.phone })
  ElMessage.success('资料已更新')
  profileVisible.value = false
}

async function fetchData() {
  loading.value = true
  try {
    const [dRes, cRes] = await Promise.all([
      request.get('/dishes', { params: { page: 1, size: 100 } }),
      request.get('/merchant/categories')
    ])
    dishes.value = dRes.data.records
    categories.value = cRes.data
  } finally { loading.value = false }
}

function showAdd() {
  form.name = ''; form.price = 0.01; form.categoryId = categories.value[0]?.id || 0; form.stock = 0; form.description = ''
  dialogVisible.value = true
}

async function handleAdd() {
  adding.value = true
  try {
    await request.post('/merchant/dishes', form)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchData()
  } finally { adding.value = false }
}

async function handleToggle(row: any) {
  const newStatus = row.status === '上架' ? '下架' : '上架'
  await request.put(`/merchant/dishes/${row.id}/status`, { status: newStatus })
  ElMessage.success(`已${newStatus}`)
  fetchData()
}

async function handleStock(row: any) {
  try {
    const { value } = await ElMessageBox.prompt('新库存量', '调整库存', { inputValue: String(row.stock) })
    if (value) {
      await request.put(`/merchant/dishes/${row.id}/stock`, { stock: parseInt(value) })
      ElMessage.success('库存已更新'); fetchData()
    }
  } catch {}
}

onMounted(fetchData)
</script>
