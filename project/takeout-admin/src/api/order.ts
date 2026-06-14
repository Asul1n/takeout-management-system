import request from './request'

export function getAdminOrders(params?: any) {
  return request.get('/admin/orders', { params })
}

export function getOrderDetail(orderNo: string) {
  return request.get(`/orders/${orderNo}`)
}
