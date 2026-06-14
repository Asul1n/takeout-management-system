<template>
  <div class="dashboard">
    <h2 style="margin-bottom: 20px;">
      欢迎回来，{{ userStore.name }}
      <el-tag style="margin-left: 8px;" :type="roleTagType">{{ roleLabel }}</el-tag>
    </h2>

    <!-- ==================== 管理员 Dashboard ==================== -->
    <template v-if="userStore.hasRole('ADMIN')">
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6" v-for="s in adminStats" :key="s.label">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" :style="{background: s.bg}">
                <el-icon :size="28" :color="s.color"><component :is="s.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ s.label }}</div>
                <div class="stat-value">{{ s.value }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="12">
          <el-card header="近7天订单趋势">
            <el-table :data="adminStats[5]?.daily || []" size="small" max-height="260">
              <el-table-column prop="date" label="日期" />
              <el-table-column prop="count" label="订单数" />
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card header="快捷入口">
            <el-row :gutter="12">
              <el-col :span="8" v-for="q in adminQuickLinks" :key="q.path">
                <el-button style="width:100%;height:80px" @click="$router.push(q.path)">
                  <el-icon :size="24"><component :is="q.icon" /></el-icon>
                  <div style="margin-top:4px">{{ q.label }}</div>
                </el-button>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 商家 Dashboard ==================== -->
    <template v-if="userStore.hasRole('MERCHANT')">
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#e6f7ff"><el-icon :size="28" color="#1890ff"><Document /></el-icon></div>
              <div class="stat-info"><div class="stat-label">本店今日订单</div><div class="stat-value">{{ merchantStats.todayOrders }}</div></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#f6ffed"><el-icon :size="28" color="#52c41a"><Money /></el-icon></div>
              <div class="stat-info"><div class="stat-label">本周营收</div><div class="stat-value">¥{{ merchantStats.weekRevenue }}</div></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#fff7e6"><el-icon :size="28" color="#fa8c16"><Food /></el-icon></div>
              <div class="stat-info"><div class="stat-label">上架菜品</div><div class="stat-value">{{ merchantStats.dishCount }}</div></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#f0f5ff"><el-icon :size="28" color="#722ed1"><Clock /></el-icon></div>
              <div class="stat-info"><div class="stat-label">待处理订单</div><div class="stat-value">{{ merchantStats.pendingOrders }}</div></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="12">
          <el-card header="最新订单">
            <el-table :data="merchantStats.recentOrders" size="small" max-height="260">
              <el-table-column prop="orderNo" label="编号" width="160" />
              <el-table-column prop="customerName" label="顾客" />
              <el-table-column prop="totalAmount" label="金额"><template #default="{row}">¥{{ row.totalAmount }}</template></el-table-column>
              <el-table-column prop="status" label="状态" />
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card header="快捷操作">
            <el-button type="primary" style="width:100%;margin-bottom:10px" @click="$router.push('/merchant/dishes')">🍽 管理菜品</el-button>
            <el-button type="warning" style="width:100%" @click="$router.push('/merchant/my-orders')">📋 处理订单</el-button>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 顾客 Dashboard ==================== -->
    <template v-if="userStore.hasRole('CUSTOMER')">
      <el-row :gutter="20" class="stats-row">
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#e6f7ff"><el-icon :size="28" color="#1890ff"><Shop /></el-icon></div>
              <div class="stat-info"><div class="stat-label">入驻商家</div><div class="stat-value">{{ customerStats.merchantCount }}</div></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#f6ffed"><el-icon :size="28" color="#52c41a"><Document /></el-icon></div>
              <div class="stat-info"><div class="stat-label">我的订单</div><div class="stat-value">{{ customerStats.orderCount }}</div></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#fff7e6"><el-icon :size="28" color="#fa8c16"><Clock /></el-icon></div>
              <div class="stat-info"><div class="stat-label">进行中订单</div><div class="stat-value">{{ customerStats.activeOrders }}</div></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-card header="推荐商家" style="margin-top:20px">
        <el-row :gutter="16">
          <el-col :span="6" v-for="m in customerStats.merchants" :key="m.id">
            <el-card shadow="hover" @click="$router.push('/customer/shops')" style="cursor:pointer;text-align:center">
              <div style="font-size:40px">🍕</div>
              <div style="font-weight:bold;margin:8px 0">{{ m.name }}</div>
              <div style="color:#909399;font-size:13px">月销 {{ m.monthlySales || 0 }} 单</div>
            </el-card>
          </el-col>
        </el-row>
      </el-card>
    </template>

    <!-- ==================== 骑手 Dashboard ==================== -->
    <template v-if="userStore.hasRole('RIDER')">
      <el-row :gutter="20" class="stats-row">
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#e6f7ff"><el-icon :size="28" color="#1890ff"><Van /></el-icon></div>
              <div class="stat-info"><div class="stat-label">待接任务</div><div class="stat-value">{{ riderStats.pendingTasks }}</div></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#f6ffed"><el-icon :size="28" color="#52c41a"><Checked /></el-icon></div>
              <div class="stat-info"><div class="stat-label">累计配送</div><div class="stat-value">{{ riderStats.totalDeliveries }}</div></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-icon" style="background:#fff7e6"><el-icon :size="28" color="#fa8c16"><Clock /></el-icon></div>
              <div class="stat-info"><div class="stat-label">今日配送</div><div class="stat-value">{{ riderStats.todayDeliveries }}</div></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-card header="待接配送任务" style="margin-top:20px">
        <el-empty v-if="riderStats.tasks.length === 0" description="暂无待接任务，休息一下吧！" />
        <el-table v-else :data="riderStats.tasks" size="small">
          <el-table-column prop="orderNo" label="订单编号" width="160" />
          <el-table-column prop="merchantName" label="取餐商家" width="150" />
          <el-table-column prop="customerAddress" label="送达地址" show-overflow-tooltip />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="handleRiderAccept(row)">抢单</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:10px;text-align:center">
          <el-button type="primary" @click="$router.push('/rider/tasks')">查看全部任务 →</el-button>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { getOverview, getOrderStats } from '@/api/statistics'
import { getMerchantList } from '@/api/merchant'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const roleLabel = userStore.role === 'ADMIN' ? '管理员' : userStore.role === 'MERCHANT' ? '商家' : userStore.role === 'CUSTOMER' ? '顾客' : '骑手'
const roleTagType = userStore.role === 'ADMIN' ? 'danger' : userStore.role === 'MERCHANT' ? 'warning' : userStore.role === 'CUSTOMER' ? '' : 'success'

// ========== 管理员 ==========
const adminStats = reactive([
  { label:'今日订单', value:0, icon:'Document', bg:'#e6f7ff', color:'#1890ff' },
  { label:'今日营收', value:'¥0', icon:'Money', bg:'#f6ffed', color:'#52c41a' },
  { label:'总用户', value:0, icon:'User', bg:'#fff7e6', color:'#fa8c16' },
  { label:'配送中', value:0, icon:'Van', bg:'#f0f5ff', color:'#722ed1' },
  { label:'', value:'', icon:'', bg:'', color:'' } as any,
  { label:'', value:'', icon:'', bg:'', color:'', daily: [] as any[] } as any,
])
const adminQuickLinks = [
  { label:'用户管理', path:'/users', icon:'User' },
  { label:'商家审核', path:'/merchants/audit', icon:'Checked' },
  { label:'订单管理', path:'/orders', icon:'Document' },
  { label:'数据统计', path:'/statistics', icon:'DataAnalysis' },
  { label:'配送管理', path:'/deliveries', icon:'Van' },
  { label:'商家管理', path:'/merchants', icon:'Shop' },
]

// ========== 商家 ==========
const merchantStats = reactive({ todayOrders:0, weekRevenue:'0', dishCount:0, pendingOrders:0, recentOrders:[] as any[] })

// ========== 顾客 ==========
const customerStats = reactive({ merchantCount:0, orderCount:0, activeOrders:0, merchants:[] as any[] })

// ========== 骑手 ==========
const riderStats = reactive({ pendingTasks:0, totalDeliveries:0, todayDeliveries:0, tasks:[] as any[] })

async function handleRiderAccept(row: any) {
  try {
    await request.put(`/rider/tasks/${row.id}/accept`)
    ElMessage.success('接单成功')
    location.reload()
  } catch {}
}

onMounted(async () => {
  if (userStore.hasRole('ADMIN')) {
    try {
      const ov = await getOverview()
      adminStats[0].value = ov.data.todayOrders
      adminStats[1].value = '¥' + (ov.data.todayRevenue || 0).toFixed(2)
      adminStats[2].value = ov.data.totalUsers
      adminStats[3].value = ov.data.deliveringOrders
      const os = await getOrderStats({ period:'week' })
      adminStats[5].daily = os.data.dailyStats || []
    } catch {}
  }

  if (userStore.hasRole('MERCHANT')) {
    try {
      const [orders, stats, dishes] = await Promise.all([
        request.get('/merchant/orders', { params: { page:1, size:5 } }),
        request.get('/merchant/statistics/orders'),
        request.get('/dishes', { params: { page:1, size:1 } })
      ])
      merchantStats.todayOrders = orders.data.total
      merchantStats.recentOrders = orders.data.records
      merchantStats.dishCount = dishes.data.total
      merchantStats.pendingOrders = orders.data.records.filter((o:any) => ['已提交','待接单','备餐中'].includes(o.status)).length
      const rev = await request.get('/merchant/statistics/revenue')
      merchantStats.weekRevenue = (rev.data.totalRevenue || 0).toFixed(2)
    } catch {}
  }

  if (userStore.hasRole('CUSTOMER')) {
    try {
      const [merchants, orders] = await Promise.all([
        getMerchantList({ page:1, size:4 }),
        request.get('/customer/orders', { params: { page:1, size:1 } })
      ])
      customerStats.merchantCount = merchants.data.total
      customerStats.merchants = merchants.data.records
      customerStats.orderCount = orders.data.total
      customerStats.activeOrders = (await request.get('/customer/orders', { params: { page:1, size:50 } }))
        .data.records.filter((o:any) => !['已完成','已取消'].includes(o.status)).length
    } catch {}
  }

  if (userStore.hasRole('RIDER')) {
    try {
      const [tasks, deliveries, stats] = await Promise.all([
        request.get('/rider/tasks', { params: { page:1, size:3 } }),
        request.get('/rider/deliveries', { params: { page:1, size:1 } }),
        request.get('/rider/statistics')
      ])
      riderStats.pendingTasks = tasks.data.total
      riderStats.tasks = tasks.data.records
      riderStats.totalDeliveries = deliveries.data.total
      riderStats.todayDeliveries = stats.data || 0
    } catch {}
  }
})
</script>

<style scoped lang="scss">
.stats-row {
  .stat-item {
    display: flex; align-items: center; gap: 16px;
    .stat-icon { width:56px; height:56px; border-radius:12px; display:flex; align-items:center; justify-content:center; }
    .stat-info { .stat-label { color:#909399; font-size:14px; margin-bottom:4px; } .stat-value { font-size:24px; font-weight:bold; color:#303133; } }
  }
}
</style>
