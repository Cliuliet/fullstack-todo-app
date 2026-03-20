import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const message =
      err.response?.data?.message || err.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export const todoApi = {
  getPage(page = 0, size = 10, keyword = '') {
    return http.get('/todos', { params: { page, size, keyword } })
  },
  getStats() {
    return http.get('/todos/stats')
  },
  getById(id) {
    return http.get(`/todos/${id}`)
  },
  create(data) {
    return http.post('/todos', data)
  },
  update(id, data) {
    return http.put(`/todos/${id}`, data)
  },
  remove(id) {
    return http.delete(`/todos/${id}`)
  },
}
