<template>
  <div class="app">
    <header>
      <h1>📝 Todo App</h1>
      <p class="subtitle">Spring Boot + Vue3 + MySQL</p>
    </header>

    <main>
      <!-- Add Todo -->
      <div class="add-form">
        <input
          v-model="newTitle"
          @keyup.enter="addTodo"
          placeholder="输入待办事项，按 Enter 添加..."
          class="todo-input"
        />
        <button @click="addTodo" class="btn btn-primary">添加</button>
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
          <span class="count">{{ getCount(tab.value) }}</span>
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading">加载中...</div>

      <!-- Error -->
      <div v-if="error" class="error">{{ error }}</div>

      <!-- Todo List -->
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
          <span v-if="editingId !== todo.id" class="todo-title" @dblclick="startEdit(todo)">
            {{ todo.title }}
          </span>
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
      <div v-if="!loading && filteredTodos.length === 0" class="empty-state">
        <p>暂无待办事项 ✨</p>
      </div>

      <!-- Footer Stats -->
      <div v-if="todos.length > 0" class="footer">
        <span>共 {{ todos.length }} 项，已完成 {{ completedCount }} 项</span>
        <button v-if="completedCount > 0" @click="clearCompleted" class="btn btn-link">
          清除已完成
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import axios from 'axios'

const todos = ref([])
const newTitle = ref('')
const loading = ref(false)
const error = ref('')
const currentFilter = ref('all')
const editingId = ref(null)
const editTitle = ref('')
const editInput = ref(null)

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待完成', value: 'active' },
  { label: '已完成', value: 'completed' },
]

const completedCount = computed(() => todos.value.filter((t) => t.completed).length)

const filteredTodos = computed(() => {
  if (currentFilter.value === 'active') return todos.value.filter((t) => !t.completed)
  if (currentFilter.value === 'completed') return todos.value.filter((t) => t.completed)
  return todos.value
})

function getCount(filter) {
  if (filter === 'all') return todos.value.length
  if (filter === 'active') return todos.value.filter((t) => !t.completed).length
  return todos.value.filter((t) => t.completed).length
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

async function fetchTodos() {
  loading.value = true
  error.value = ''
  try {
    const res = await axios.get('/api/todos')
    todos.value = res.data
  } catch (e) {
    error.value = '获取数据失败，请检查后端服务是否启动'
  } finally {
    loading.value = false
  }
}

async function addTodo() {
  const title = newTitle.value.trim()
  if (!title) return
  try {
    const res = await axios.post('/api/todos', { title })
    todos.value.unshift(res.data)
    newTitle.value = ''
  } catch (e) {
    error.value = '添加失败'
  }
}

async function toggleTodo(todo) {
  try {
    const res = await axios.put(`/api/todos/${todo.id}`, {
      title: todo.title,
      completed: !todo.completed,
    })
    const index = todos.value.findIndex((t) => t.id === todo.id)
    todos.value[index] = res.data
  } catch (e) {
    error.value = '更新失败'
  }
}

async function deleteTodo(id) {
  try {
    await axios.delete(`/api/todos/${id}`)
    todos.value = todos.value.filter((t) => t.id !== id)
  } catch (e) {
    error.value = '删除失败'
  }
}

async function clearCompleted() {
  const completed = todos.value.filter((t) => t.completed)
  await Promise.all(completed.map((t) => axios.delete(`/api/todos/${t.id}`)))
  todos.value = todos.value.filter((t) => !t.completed)
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
  if (!title) {
    cancelEdit()
    return
  }
  try {
    const res = await axios.put(`/api/todos/${todo.id}`, {
      title,
      completed: todo.completed,
    })
    const index = todos.value.findIndex((t) => t.id === todo.id)
    todos.value[index] = res.data
  } catch (e) {
    error.value = '更新失败'
  } finally {
    cancelEdit()
  }
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
  max-width: 600px;
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
  margin-bottom: 20px;
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
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tab.active {
  background: white;
  color: #667eea;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.count {
  background: #e2e8f0;
  border-radius: 10px;
  padding: 1px 7px;
  font-size: 0.75rem;
  min-width: 20px;
  text-align: center;
}

.tab.active .count {
  background: #667eea;
  color: white;
}

.loading {
  text-align: center;
  color: #718096;
  padding: 20px;
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
  gap: 12px;
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
</style>
