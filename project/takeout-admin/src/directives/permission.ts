import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

export const permissionDirective: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userStore = useUserStore()
    const role = binding.value as string

    if (role && !userStore.hasRole(role)) {
      el.style.display = 'none'
    }
  }
}

/**
 * 检查权限的 composable
 */
export function usePermission() {
  const userStore = useUserStore()
  return {
    hasRole: (role: string) => userStore.hasRole(role),
    role: userStore.role
  }
}
