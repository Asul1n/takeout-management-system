import request from './request'

export function register(data: { phone: string; password: string; role: string; name?: string }) {
  return request.post('/auth/register', data)
}

export function login(data: { phone: string; password: string }) {
  return request.post('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getCurrentUser() {
  return request.get('/user/me')
}

export function updatePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/user/password', data)
}

export function getUserList(params: any) {
  return request.get('/admin/users', { params })
}

export function toggleUserStatus(userId: number, status: string) {
  return request.put(`/admin/users/${userId}/status`, { status })
}

export function resetUserPassword(userId: number, password: string) {
  return request.put(`/admin/users/${userId}/password`, { password })
}

export function createUser(data: { phone: string; password: string; role: string; name: string }) {
  return request.post('/admin/users', data)
}

export function deleteUser(userId: number) {
  return request.delete(`/admin/users/${userId}`)
}

export function getNotifications(params?: any) {
  return request.get('/notifications', { params })
}

export function markNotificationRead(id: number) {
  return request.put(`/notifications/${id}/read`)
}

export function markAllNotificationsRead() {
  return request.put('/notifications/read-all')
}

export function getUnreadCount() {
  return request.get('/notifications/unread-count')
}
