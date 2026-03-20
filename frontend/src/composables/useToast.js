import { ref } from 'vue'

const toasts = ref([])
let nextId = 0

function show(message, type = 'success', duration = 3000) {
  const id = ++nextId
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, duration)
}

export function useToast() {
  return {
    toasts,
    success(message) { show(message, 'success') },
    error(message) { show(message, 'error') },
  }
}
