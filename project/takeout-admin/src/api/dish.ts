import request from './request'

export function getDishList(params?: any) {
  return request.get('/dishes', { params })
}

export function getDishDetail(id: number) {
  return request.get(`/dishes/${id}`)
}
