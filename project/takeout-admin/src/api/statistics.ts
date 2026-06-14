import request from './request'

export function getOverview() {
  return request.get('/admin/statistics/overview')
}

export function getOrderStats(params?: any) {
  return request.get('/admin/statistics/orders', { params })
}

export function getRevenueStats(params?: any) {
  return request.get('/admin/statistics/revenue', { params })
}

export function getRiderStats() {
  return request.get('/admin/statistics/riders')
}
