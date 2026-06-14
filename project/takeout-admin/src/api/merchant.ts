import request from './request'

export function getMerchantList(params?: any) {
  return request.get('/merchants', { params })
}

export function getAuditList(params?: any) {
  return request.get('/admin/merchants/audit', { params })
}

export function auditMerchant(id: number, data: { auditStatus: string; reason?: string }) {
  return request.put(`/admin/merchants/${id}/audit`, data)
}
