import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken, getUserInfo } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      // ========== 通用页面（所有角色可见） ==========
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },

      // ========== 管理员专用 ==========
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'merchants',
        name: 'Merchants',
        component: () => import('@/views/merchant/list.vue'),
        meta: { title: '商家管理', icon: 'Shop', roles: ['ADMIN'] }
      },
      {
        path: 'merchants/audit',
        name: 'MerchantAudit',
        component: () => import('@/views/merchant/audit.vue'),
        meta: { title: '商家审核', icon: 'Checked', roles: ['ADMIN'] }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/order/list.vue'),
        meta: { title: '订单管理', icon: 'Document', roles: ['ADMIN'] }
      },
      {
        path: 'orders/:orderNo',
        name: 'OrderDetail',
        component: () => import('@/views/order/detail.vue'),
        meta: { title: '订单详情', roles: ['ADMIN'] }
      },
      {
        path: 'deliveries',
        name: 'Deliveries',
        component: () => import('@/views/delivery/list.vue'),
        meta: { title: '配送管理', icon: 'Van', roles: ['ADMIN'] }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/sales.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis', roles: ['ADMIN'] }
      },

      // ========== 商家专用 ==========
      {
        path: 'merchant/dishes',
        name: 'MerchantDishes',
        component: () => import('@/views/merchant/dish-manage.vue'),
        meta: { title: '菜品管理', icon: 'Food', roles: ['MERCHANT'] }
      },
      {
        path: 'merchant/my-orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/order-manage.vue'),
        meta: { title: '订单处理', icon: 'Document', roles: ['MERCHANT'] }
      },
      {
        path: 'merchant/stats',
        name: 'MerchantStats',
        component: () => import('@/views/merchant/stats.vue'),
        meta: { title: '营业统计', icon: 'DataAnalysis', roles: ['MERCHANT'] }
      },

      // ========== 顾客专用 ==========
      {
        path: 'customer/shops',
        name: 'CustomerShops',
        component: () => import('@/views/customer/shops.vue'),
        meta: { title: '浏览商家', icon: 'Shop', roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/my-orders',
        name: 'CustomerOrders',
        component: () => import('@/views/customer/orders.vue'),
        meta: { title: '我的订单', icon: 'Document', roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/addresses',
        name: 'CustomerAddresses',
        component: () => import('@/views/customer/addresses.vue'),
        meta: { title: '收货地址', icon: 'Location', roles: ['CUSTOMER'] }
      },

      // ========== 骑手专用 ==========
      {
        path: 'rider/tasks',
        name: 'RiderTasks',
        component: () => import('@/views/rider/tasks.vue'),
        meta: { title: '配送任务', icon: 'Van', roles: ['RIDER'] }
      },
      {
        path: 'rider/history',
        name: 'RiderHistory',
        component: () => import('@/views/rider/history.vue'),
        meta: { title: '配送记录', icon: 'Clock', roles: ['RIDER'] }
      },

      // ========== 403 无权限 ==========
      {
        path: '403',
        name: 'Forbidden',
        component: () => import('@/views/403.vue'),
        meta: { title: '无权限' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = getToken()
  const userInfo = getUserInfo()

  // 未登录 → 跳转登录
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
    return
  }

  // 已登录 → 不能访问登录页
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }

  // 角色权限校验
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && requiredRoles.length > 0) {
    const userRole = userInfo?.role
    if (!userRole || !requiredRoles.includes(userRole)) {
      ElMessage.warning('您没有权限访问该页面')
      next('/403')
      return
    }
  }

  next()
})

export default router
