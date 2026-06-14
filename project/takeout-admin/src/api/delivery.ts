import request from './request'

export function getAdminDeliveries(params?: any) {
  return request.get('/admin/deliveries', { params })
}
