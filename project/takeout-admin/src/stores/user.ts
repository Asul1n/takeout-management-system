import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo, clearAuth } from '@/utils/auth'
import request from '@/api/request'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userId = ref<number | null>(null)
  const role = ref<string>('')
  const name = ref<string>('')
  const phone = ref<string>('')

  const isLoggedIn = computed(() => !!token.value)

  function hasRole(r: string): boolean {
    return role.value === r
  }

  async function login(loginData: { phone: string; password: string }) {
    const res = await request.post('/auth/login', loginData)
    const data = res.data
    token.value = data.token
    userId.value = data.userId
    role.value = data.role
    name.value = data.name
    setToken(data.token)
    setUserInfo({ userId: data.userId, role: data.role, name: data.name })
    return data
  }

  async function fetchUserInfo() {
    const res = await request.get('/user/me')
    const data = res.data
    userId.value = data.id
    role.value = data.role
    name.value = data.name
    phone.value = data.phone
    setUserInfo({ userId: data.id, role: data.role, name: data.name, phone: data.phone })
  }

  function logout() {
    clearAuth()
    token.value = null
    userId.value = null
    role.value = ''
    name.value = ''
    phone.value = ''
  }

  // 从 localStorage 恢复
  const saved = getUserInfo()
  if (saved) {
    userId.value = saved.userId
    role.value = saved.role
    name.value = saved.name
  }

  return { token, userId, role, name, phone, isLoggedIn, hasRole, login, fetchUserInfo, logout }
})
