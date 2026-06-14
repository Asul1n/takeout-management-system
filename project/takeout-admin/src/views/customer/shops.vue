<template>
  <div class="table-page">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h3>浏览商家</h3>
      <div>
        <el-badge :value="cartCount" :hidden="cartCount===0">
          <el-button type="warning" @click="showCart">🛒 购物车 ({{ cartCount }})</el-button>
        </el-badge>
      </div>
    </div>

    <el-table :data="merchants" v-loading="loading" border stripe @row-click="showDishes" style="cursor:pointer">
      <el-table-column prop="name" label="商家" width="180" />
      <el-table-column label="地址" min-width="220">
        <template #default="{ row }">{{ row.province }}{{ row.city }}{{ row.district }}{{ row.addressDetail }}</template>
      </el-table-column>
      <el-table-column prop="bizStatus" label="状态" width="100">
        <template #default="{ row }"><el-tag :type="row.bizStatus==='营业中'?'success':'info'">{{ row.bizStatus }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="monthlySales" label="月销" width="80" />
    </el-table>

    <!-- 菜品浏览 + 分类筛选 -->
    <el-dialog v-model="dishVisible" :title="selectedMerchant?.name + ' — 菜单'" width="700px">
      <div style="margin-bottom:12px;">
        <el-radio-group v-model="selectedCategory" size="small" @change="filterDishes">
          <el-radio-button :value="0">全部</el-radio-button>
          <el-radio-button v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="filteredDishes" border stripe max-height="400">
        <el-table-column prop="name" label="菜品" width="150" />
        <el-table-column prop="price" label="单价" width="80"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="60" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-input-number v-model="quantities[row.id]" :min="0" :max="row.stock" size="small" style="width:70px" />
            <el-button size="small" type="primary" :disabled="row.stock===0" @click="addToCart(row)" style="margin-left:6px">加入</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 购物车对话框 -->
    <el-dialog v-model="cartVisible" title="🛒 我的购物车" width="600px">
      <el-table v-if="cartItems.length>0" :data="cartItems" border stripe>
        <el-table-column prop="dishId" label="菜品ID" width="70" />
        <el-table-column prop="dishName" label="菜品名称" />
        <el-table-column prop="price" label="单价" width="80"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column label="小计" width="90"><template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template></el-table-column>
        <el-table-column label="操作" width="70">
          <template #default="{ row }"><el-button size="small" type="danger" @click="removeCartItem(row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="购物车为空" />
      <template v-if="cartItems.length>0" #footer>
        <div style="text-align:right;">
          <span style="font-size:18px;margin-right:16px;">合计: <b>¥{{ cartTotal.toFixed(2) }}</b></span>
          <el-button type="primary" size="large" @click="showSubmitDialog">提交订单</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 提交订单对话框 -->
    <el-dialog v-model="submitVisible" title="确认下单" width="500px">
      <el-form label-width="80px">
        <el-form-item label="商家">{{ selectedMerchantForOrder?.name }}</el-form-item>
        <el-form-item label="配送地址">
          <el-select v-model="selectedAddressId" style="width:100%">
            <el-option v-for="a in addresses" :key="a.id" :label="a.fullAddress" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="remark" type="textarea" placeholder="如: 少辣, 不要香菜" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible=false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitOrder">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const merchants = ref<any[]>([])
const dishVisible = ref(false)
const cartVisible = ref(false)
const submitVisible = ref(false)
const selectedMerchant = ref<any>(null)
const selectedMerchantForOrder = ref<any>(null)
const dishes = ref<any[]>([])
const categories = ref<any[]>([])
const selectedCategory = ref(0)
const quantities = reactive<Record<number,number>>({})
const cartItems = ref<any[]>([])
const addresses = ref<any[]>([])
const selectedAddressId = ref<number>(0)
const remark = ref('')
const submitting = ref(false)

const filteredDishes = computed(() =>
  selectedCategory.value === 0 ? dishes.value : dishes.value.filter(d => d.categoryId === selectedCategory.value)
)
const cartCount = computed(() => cartItems.value.reduce((s,i) => s + i.quantity, 0))
const cartTotal = computed(() => cartItems.value.reduce((s,i) => s + i.price * i.quantity, 0))

async function fetchMerchants() {
  loading.value = true
  try { const res = await request.get('/merchants', { params: { page: 1, size: 20 } }); merchants.value = res.data.records }
  finally { loading.value = false }
}

async function showDishes(row: any) {
  selectedMerchant.value = row; selectedCategory.value = 0
  // 初始化数量选择器
  Object.keys(quantities).forEach(k => delete quantities[k])
  const [cRes, dRes] = await Promise.all([
    request.get(`/merchant/categories/by-merchant?merchantId=${row.id}`),
    request.get('/dishes', { params: { merchantId: row.id, page: 1, size: 50 } })
  ])
  categories.value = cRes.data; dishes.value = dRes.data.records
  dishes.value.forEach(d => { quantities[d.id] = 0 })
  dishVisible.value = true
}

function filterDishes() {} // via computed

async function addToCart(row: any) {
  const qty = quantities[row.id] || 1
  if (qty <= 0) { ElMessage.warning('请选择数量'); return }
  await request.post('/customer/cart', { dishId: row.id, quantity: qty })
  ElMessage.success(`已加入: ${row.name} x${qty}`)
  await loadCart()
}

async function loadCart() {
  const res = await request.get('/customer/cart')
  const items = res.data || []
  // enrich with dish info
  const enriched = await Promise.all(items.map(async (item:any) => {
    try { const d = await request.get(`/dishes/${item.dishId}`); return { ...item, dishName: d.data.name, price: d.data.price } }
    catch { return { ...item, dishName: '未知', price: 0 } }
  }))
  cartItems.value = enriched
}

async function removeCartItem(id: number) {
  await request.delete(`/customer/cart/${id}`); await loadCart()
}

async function showCart() {
  await Promise.all([loadCart(), fetchAddresses()])
  cartVisible.value = true
}

async function fetchAddresses() {
  const res = await request.get('/customer/addresses'); addresses.value = res.data
  selectedAddressId.value = addresses.value.find((a:any) => a.isDefault)?.id || addresses.value[0]?.id
}

function showSubmitDialog() {
  const merchantId = cartItems.value[0]?.dishId ? merchantIdFromCart() : null
  selectedMerchantForOrder.value = merchants.value.find(m => m.id === merchantId)
  if (!addresses.value.length) fetchAddresses()
  submitVisible.value = true
}

function merchantIdFromCart(): number|null {
  // In real app you'd track which merchant's cart. For now, guess from first item
  return null
}

async function handleSubmitOrder() {
  if (!selectedAddressId.value) { ElMessage.warning('请选择配送地址'); return }
  // Determine merchant from cart items
  const items = cartItems.value.map(i => ({ dishId: i.dishId, quantity: i.quantity }))
  const firstDishId = items[0]?.dishId
  if (!firstDishId) { ElMessage.warning('购物车为空'); return }
  // Fetch dish to get merchantId
  const dRes = await request.get(`/dishes/${firstDishId}`)
  const merchantId = dRes.data.merchantId
  submitting.value = true
  try {
    await request.post('/orders', { merchantId, addressId: selectedAddressId.value, remark: remark.value, items })
    ElMessage.success('下单成功！')
    submitVisible.value = false; cartVisible.value = false
    await loadCart()
  } finally { submitting.value = false }
}

loadCart(); fetchMerchants()
</script>
