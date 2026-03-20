<template>
  <div class="app">
    <ToastMessage />

    <header>
      <h1>📝 Todo App</h1>
      <p class="subtitle">Spring Boot + Vue3 + MySQL</p>
    </header>

    <main>
      <!-- Stats Panel -->
      <StatsPanel :stats="stats" />

      <!-- Add Todo Form -->
      <div class="add-form">
        <input
          v-model="newTitle"
          @keyup.enter="addTodo"
          placeholder="输入待办事项，按 Enter 添加..."
          class="todo-input"
        />
        <select v-model="newPriority" class="priority-select">
          <option :value="1">🔴 高</option>
          <option :value="2">🟡 中</option>
          <option :value="3">🟢 低</option>
        </select>
        <button @click="addTodo" class="btn btn-primary">添加</button>
      </div>

      <!-- Search -->
      <div class="search-bar">
        <input
          v-model="searchInput"
          placeholder="搜索待办..."
          class="todo-input search-input"
        />
      </div>

      <!-- Filter Tabs -->
      <div class="filter-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab', { active: currentFilter === tab.value }]"
          @click="currentFilter = tab.value"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- Skeleton Loading -->
      <TodoSkeleton v-if="loading" />

      <!-- Error -->
      <div v-else-if="error" class="error">{{ error }}</div>

      <!-- Todo List -->
      <template v-else>
        <transition-group name="todo-list" tag="ul" class="todo-list">
          <li
            v-for="todo in filteredTodos"
            :key="todo.id"
            :class="['todo-item', { completed: todo.completed }]"
          >
            <input
              type="checkbox"
              :checked="todo.completed"
              @change="toggleTodo(todo)"
              class="todo-checkbox"
            />
            <span
              :class="['priority-badge', `priority-${todo.priority}`]"
              :title="priorityLabel(todo.priority)"
            >{{ priorityIcon(todo.priority) }}</span>
            <span
              v-if="editingId !== todo.id"
              class="todo-title"
              @dblclick="startEdit(todo)"
            >{{ todo.title }}</span>
            <input
              v-else
              v-model="editTitle"
              @keyup.enter="saveEdit(todo)"
              @keyup.escape="cancelEdit"
              @blur="saveEdit(todo)"
              class="edit-input"
              ref="editInput"
            />
            <span class="todo-date">{{ formatDate(todo.createdAt) }}</span>
            <button @click="deleteTodo(todo.id)" class="btn btn-danger btn-sm">删除</button>
          </li>
        </transition-group>

        <!-- Empty State -->
        <div v-if="filteredTodos.length === 0" class="empty-state">
          <p>暂无待办事项 ✨</p>
        </div>
      </template>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button
          class="btn btn-page"
          :disabled="currentPage === 0"
          @click="goToPage(currentPage - 1)"
        >上一页</button>
        <span class="page-info">第 {{ currentPage + 1 }} / {{ totalPages }} 页（共 {{ totalElements }} 条）</span>
        <button
          class="btn btn-page"
          :disabled="currentPage >= totalPages - 1"
          @click="goToPage(currentPage + 1)"
        >下一页</button>
      </div>

      <!-- Footer -->
      <div v-if="totalElements > 0" class="footer">
        <span>共 {{ totalElements }} 项</span>
        <button v-if="stats.completed > 0" @click="clearCompleted" class="btn btn-link">
          清除已完成
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { todoApi } from './api/todo.js'
import { useToast } from './composables/useToast.js'
import ToastMessage from './components/ToastMessage.vue'
import TodoSkeleton from './components/TodoSkeleton.vue'
import StatsPanel from './components/StatsPanel.vue'

const toast = useToast()

// State
const todos = ref([])
const stats = ref({ total: 0, completed: 0, active: 0, highPriority: 0 })
const newTitle = ref('')
const newPriority = ref(2)
const loading = ref(false)
const error = ref('')
const currentFilter = ref('all')
const editingId = ref(null)
const editTitle = ref('')
const editInput = ref(null)

// Pagination
const currentPage = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

// Search with 400ms debounce
const searchInput = ref('')
const keyword = ref('')
let debounceTimer = null
watch(searchInput, (val) => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    keyword.value = val
    currentPage.value = 0
  }, 400)
})

watch([keyword, currentPage], fetchTodos)

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待完成', value: 'active' },
  { label: '已完成', value: 'completed' },
]

const filteredTodos = computed(() => {
  if (currentFilter.value === 'active') return todos.value.filter((t) => !t.completed)
  if (currentFilter.value === 'completed') return todos.value.filter((t) => t.completed)
  return todos.value
})

function priorityIcon(p) {
  return p === 1 ? '🔴' : p === 3 ? '🟢' : '🟡'
}
function priorityLabel(p) {
  return p === 1 ? '高优先级' : p === 3 ? '低优先级' : '中优先级'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

async function fetchTodos() {
  loading.value = true
  error.value = ''
  try {
    const res = await todoApi.getPage(currentPage.value, pageSize.value, keyword.value)
    const page = res.data
    todos.value = page.content
    totalElements.value = page.totalElements
    totalPages.value = page.totalPages
    await fetchStats()
  } catch (e) {
    error.value = e.message || '获取数据失败，请检查后端服务是否启动'
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  try {
    const res = await todoApi.getStats()
    stats.value = res.data
  } catch {
    // stats failure is non-critical
  }
}

async function addTodo() {
  const title = newTitle.value.trim()
  if (!title) return
  try {
    await todoApi.create({ title, priority: newPriority.value })
    newTitle.value = ''
    newPriority.value = 2
    currentPage.value = 0
    await fetchTodos()
    toast.success('添加成功')
  } catch (e) {
    toast.error(e.message || '添加失败')
  }
}

async function toggleTodo(todo) {
  try {
    await todoApi.update(todo.id, {
      title: todo.title,
      completed: !todo.completed,
      priority: todo.priority,
    })
    await fetchTodos()
    toast.success(todo.completed ? '已标为未完成' : '已标为已完成')
  } catch (e) {
    toast.error(e.message || '更新失败')
  }
}

async function deleteTodo(id) {
  try {
    await todoApi.remove(id)
    await fetchTodos()
    toast.success('已删除')
  } catch (e) {
    toast.error(e.message || '删除失败')
  }
}

async function clearCompleted() {
  const completed = todos.value.filter((t) => t.completed)
  try {
    await Promise.all(completed.map((t) => todoApi.remove(t.id)))
    currentPage.value = 0
    await fetchTodos()
    toast.success(`已清除 ${completed.length} 条已完成事项`)
  } catch (e) {
    toast.error(e.message || '清除失败')
  }
}

function startEdit(todo) {
  editingId.value = todo.id
  editTitle.value = todo.title
  nextTick(() => {
    if (editInput.value) editInput.value.focus()
  })
}

function cancelEdit() {
  editingId.value = null
  editTitle.value = ''
}

async function saveEdit(todo) {
  const title = editTitle.value.trim()
  if (!title) { cancelEdit(); return }
  try {
    await todoApi.update(todo.id, {
      title,
      completed: todo.completed,
      priority: todo.priority,
    })
    await fetchTodos()
    toast.success('已更新')
  } catch (e) {
    toast.error(e.message || '更新失败')
  } finally {
    cancelEdit()
  }
}

function goToPage(page) {
  currentPage.value = page
}

onMounted(fetchTodos)
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 40px 16px;
}

.app {
  width: 100%;
  max-width: 680px;
}

header {
  text-align: center;
  margin-bottom: 32px;
  color: white;
}

header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.subtitle {
  margin-top: 8px;
  opacity: 0.8;
  font-size: 0.9rem;
}

main {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.add-form {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.search-bar {
  margin-bottom: 16px;
}

.search-input {
  width: 100%;
}

.todo-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
  outline: none;
}

.todo-input:focus {
  border-color: #667eea;
}

.priority-select {
  padding: 10px 8px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  outline: none;
  cursor: pointer;
  background: white;
}

.priority-select:focus {
  border-color: #667eea;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  white-space: nowrap;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-danger {
  background: #fed7d7;
  color: #c53030;
}

.btn-danger:hover {
  background: #feb2b2;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 0.85rem;
}

.btn-link {
  background: none;
  color: #667eea;
  padding: 0;
  text-decoration: underline;
  font-size: 0.9rem;
}

.btn-page {
  padding: 6px 14px;
  background: #edf2f7;
  color: #4a5568;
  font-size: 0.85rem;
}

.btn-page:hover:not(:disabled) {
  background: #e2e8f0;
}

.btn-page:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.filter-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  background: #f7fafc;
  padding: 4px;
  border-radius: 8px;
}

.tab {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  font-size: 0.9rem;
  color: #718096;
  transition: all 0.2s;
}

.tab.active {
  background: white;
  color: #667eea;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.error {
  background: #fff5f5;
  color: #c53030;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 0.9rem;
}

.todo-list {
  list-style: none;
  min-height: 40px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
  color: #a0aec0;
}

.todo-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #667eea;
  flex-shrink: 0;
}

.priority-badge {
  font-size: 0.85rem;
  flex-shrink: 0;
  cursor: help;
}

.todo-title {
  flex: 1;
  font-size: 1rem;
  cursor: pointer;
  word-break: break-word;
}

.edit-input {
  flex: 1;
  padding: 4px 8px;
  border: 2px solid #667eea;
  border-radius: 4px;
  font-size: 1rem;
  outline: none;
}

.todo-date {
  font-size: 0.75rem;
  color: #cbd5e0;
  white-space: nowrap;
}

.empty-state {
  text-align: center;
  color: #a0aec0;
  padding: 40px 0;
  font-size: 1rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.page-info {
  font-size: 0.85rem;
  color: #718096;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
  font-size: 0.9rem;
  color: #718096;
}

/* Transition animations */
.todo-list-enter-active,
.todo-list-leave-active {
  transition: all 0.3s ease;
}

.todo-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.todo-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* Responsive */
@media (max-width: 768px) {
  body {
    padding: 20px 12px;
  }

  header h1 {
    font-size: 1.8rem;
  }

  main {
    padding: 16px;
  }

  .add-form {
    flex-wrap: wrap;
  }

  .todo-input {
    min-width: 0;
  }

  .priority-select {
    flex-shrink: 0;
  }

  .btn-primary {
    width: 100%;
  }

  .todo-date {
    display: none;
  }
}
</style>
