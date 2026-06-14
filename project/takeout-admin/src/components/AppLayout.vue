<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <span v-if="!sidebarCollapsed">🍕 外卖管理系统</span>
        <span v-else>🍕</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- ===== 管理员菜单 ===== -->
        <template v-if="userStore.hasRole('ADMIN')">
          <el-menu-item index="/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
          <el-menu-item index="/merchants"><el-icon><Shop /></el-icon><span>商家管理</span></el-menu-item>
          <el-menu-item index="/merchants/audit"><el-icon><Checked /></el-icon><span>商家审核</span></el-menu-item>
          <el-menu-item index="/orders"><el-icon><Document /></el-icon><span>订单管理</span></el-menu-item>
          <el-menu-item index="/deliveries"><el-icon><Van /></el-icon><span>配送管理</span></el-menu-item>
          <el-menu-item index="/statistics"><el-icon><DataAnalysis /></el-icon><span>数据统计</span></el-menu-item>
        </template>

        <!-- ===== 商家菜单 ===== -->
        <template v-if="userStore.hasRole('MERCHANT')">
          <el-menu-item index="/merchant/dishes"><el-icon><Food /></el-icon><span>菜品管理</span></el-menu-item>
          <el-menu-item index="/merchant/my-orders"><el-icon><Document /></el-icon><span>订单处理</span></el-menu-item>
          <el-menu-item index="/merchant/stats"><el-icon><DataAnalysis /></el-icon><span>营业统计</span></el-menu-item>
        </template>

        <!-- ===== 顾客菜单 ===== -->
        <template v-if="userStore.hasRole('CUSTOMER')">
          <el-menu-item index="/customer/shops"><el-icon><Shop /></el-icon><span>浏览商家</span></el-menu-item>
          <el-menu-item index="/customer/my-orders"><el-icon><Document /></el-icon><span>我的订单</span></el-menu-item>
          <el-menu-item index="/customer/addresses"><el-icon><Location /></el-icon><span>收货地址</span></el-menu-item>
        </template>

        <!-- ===== 骑手菜单 ===== -->
        <template v-if="userStore.hasRole('RIDER')">
          <el-menu-item index="/rider/tasks"><el-icon><Van /></el-icon><span>配送任务</span></el-menu-item>
          <el-menu-item index="/rider/history"><el-icon><Clock /></el-icon><span>配送记录</span></el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar" :size="20">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <!-- 一键切换角色 -->
          <el-dropdown>
            <el-button size="small" type="warning" plain>
              <el-icon><Switch /></el-icon> 切换角色
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="a in testAccounts" :key="a.role" @click="quickSwitch(a)">
                  <el-tag :type="a.tag" size="small" style="margin-right:6px">{{ a.label }}</el-tag>
                  {{ a.phone }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
          <el-dropdown>
            <span class="user-info">
              <el-tag :type="roleTag" size="small" style="margin-right:4px">{{ roleLabel }}</el-tag>
              {{ userStore.name || '管理员' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getUnreadCount } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
const unreadCount = ref(0)

// ===== 一键切换角色 =====
const testAccounts = [
  { phone: '13800000000', password: '123456', role: 'ADMIN', label: '管理员', tag: '' as any },
  { phone: '13900000002', password: '123456', role: 'MERCHANT', label: '商家', tag: 'warning' as any },
  { phone: '13800000015', password: '123456', role: 'CUSTOMER', label: '顾客', tag: '' as any },
  { phone: '13900030001', password: '123456', role: 'RIDER', label: '骑手', tag: 'success' as any },
]
const roleLabel = computed(() => {
  const r = userStore.role
  return r === 'ADMIN' ? '管理员' : r === 'MERCHANT' ? '商家' : r === 'CUSTOMER' ? '顾客' : r === 'RIDER' ? '骑手' : r
})
const roleTag = computed(() => {
  const r = userStore.role
  return r === 'MERCHANT' ? 'warning' : r === 'RIDER' ? 'success' : r === 'CUSTOMER' ? 'info' : r === 'ADMIN' ? 'danger' : ''
})

async function quickSwitch(account: typeof testAccounts[0]) {
  try {
    await userStore.login({ phone: account.phone, password: account.password })
    ElMessage.success(`已切换为 ${account.label}`)
    router.push('/dashboard')
  } catch { /* token already set in store */ }
}

function toggleSidebar() {
  appStore.toggleSidebar()
}

async function loadUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch {}
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
  ElMessage.success('已退出登录')
}

loadUnreadCount()
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .el-menu {
    border-right: none;
  }
}

.layout-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  height: 60px;

  .header-left {
    .collapse-btn {
      cursor: pointer;
      &:hover { color: #409eff; }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .notification-badge {
      cursor: pointer;
    }

    .user-info {
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.el-main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
