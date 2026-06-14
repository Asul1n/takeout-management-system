<template>
  <div class="table-page">
    <div class="search-form">
      <el-form :inline="true" :model="query" size="default">
        <el-form-item label="手机号">
          <el-input v-model="query.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="query.role" placeholder="全部" clearable style="width: 120px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="顾客" value="CUSTOMER" />
            <el-option label="骑手" value="RIDER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="正常" value="正常" />
            <el-option label="禁用" value="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="showCreateDialog">新增用户</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="role" label="身份" width="100">
        <template #default="{ row }">
          <el-tag :type="roleType(row.role)">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '正常' ? 'success' : 'danger'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="280">
        <template #default="{ row }">
          <el-button size="small" @click="handleToggleStatus(row)" :type="row.status === '正常' ? 'danger' : 'success'">
            {{ row.status === '正常' ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" @click="handleResetPassword(row)">重置密码</el-button>
          <el-button v-if="row.role !== 'ADMIN'" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      layout="total, sizes, prev, pager, next"
      :page-sizes="[10, 20, 50]"
      @change="fetchData"
      style="margin-top: 20px; justify-content: flex-end;"
    />

    <!-- 新增用户对话框 -->
    <el-dialog v-model="dialogVisible" title="新增用户" width="450px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="默认123456" />
        </el-form-item>
        <el-form-item label="身份" prop="role">
          <el-select v-model="createForm.role" placeholder="请选择" style="width: 100%">
            <el-option label="商家" value="MERCHANT" />
            <el-option label="骑手" value="RIDER" />
            <el-option label="顾客" value="CUSTOMER" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <!-- 商家专用：地址字段 -->
        <template v-if="createForm.role === 'MERCHANT'">
          <el-form-item label="省" prop="province"><el-input v-model="createForm.province" placeholder="如：广东省" /></el-form-item>
          <el-form-item label="市" prop="city"><el-input v-model="createForm.city" placeholder="如：深圳市" /></el-form-item>
          <el-form-item label="区" prop="district"><el-input v-model="createForm.district" placeholder="如：南山区" /></el-form-item>
          <el-form-item label="详细地址" prop="addressDetail"><el-input v-model="createForm.addressDetail" placeholder="如：科技园路1号" /></el-form-item>
          <el-form-item label="营业时间">
            <el-input v-model="createForm.openTime" placeholder="09:00" style="width:100px" /> —
            <el-input v-model="createForm.closeTime" placeholder="21:00" style="width:100px" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateUser" :loading="creating">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getUserList, toggleUserStatus, resetUserPassword, createUser, deleteUser } from '@/api/user'
import { formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const creating = ref(false)
const createFormRef = ref()

const query = reactive({
  phone: '',
  role: '',
  status: '',
  page: 1,
  size: 10
})

const createForm = reactive({
  phone: '',
  password: '123456',
  role: 'MERCHANT',
  name: '',
  province: '',
  city: '',
  district: '',
  addressDetail: '',
  openTime: '09:00',
  closeTime: '21:00'
})

const createRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }],
  role: [{ required: true, message: '请选择身份', trigger: 'change' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const roleMap: Record<string, string> = {
  ADMIN: '管理员', MERCHANT: '商家', CUSTOMER: '顾客', RIDER: '骑手'
}

function roleLabel(role: string) { return roleMap[role] || role }
function roleType(role: string) {
  return role === 'ADMIN' ? 'danger' : role === 'MERCHANT' ? 'warning' : role === 'RIDER' ? 'success' : ''
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.phone = ''
  query.role = ''
  query.status = ''
  query.page = 1
  fetchData()
}

function showCreateDialog() {
  createForm.phone = ''
  createForm.password = '123456'
  createForm.role = 'MERCHANT'
  createForm.name = ''
  createForm.province = ''
  createForm.city = ''
  createForm.district = ''
  createForm.addressDetail = ''
  createForm.openTime = '09:00'
  createForm.closeTime = '21:00'
  dialogVisible.value = true
}

async function handleCreateUser() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    await createUser({ ...createForm })
    ElMessage.success('用户创建成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    creating.value = false
  }
}

async function handleToggleStatus(row: any) {
  const newStatus = row.status === '正常' ? '禁用' : '正常'
  await toggleUserStatus(row.id, newStatus)
  ElMessage.success(`已${newStatus}`)
  fetchData()
}

async function handleResetPassword(row: any) {
  try {
    await ElMessageBox.prompt('请输入新密码', '重置密码', { confirmButtonText: '确定', inputType: 'password' })
    await resetUserPassword(row.id, '123456')
    ElMessage.success('密码已重置为 123456')
  } catch {}
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除用户「${row.name}(${row.phone})」吗？此操作不可恢复！`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUser(row.id)
    ElMessage.success('用户已删除')
    fetchData()
  } catch {}
}

fetchData()
</script>
