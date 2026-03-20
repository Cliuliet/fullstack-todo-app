<template>
  <ul class="todo-list">
    <li v-for="i in 5" :key="i" class="skeleton-item">
      <div class="skeleton skeleton-checkbox"></div>
      <div class="skeleton skeleton-title" :style="{ width: widths[i - 1] }"></div>
      <div class="skeleton skeleton-date"></div>
      <div class="skeleton skeleton-btn"></div>
    </li>
  </ul>
</template>

<script setup>
const widths = ['60%', '75%', '50%', '80%', '65%']
</script>

<style scoped>
.todo-list { list-style: none; }

.skeleton-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}

.skeleton {
  background: linear-gradient(90deg, #e2e8f0 25%, #edf2f7 50%, #e2e8f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
  border-radius: 4px;
}

.skeleton-checkbox { width: 18px; height: 18px; flex-shrink: 0; border-radius: 3px; }
.skeleton-title    { height: 16px; flex: 1; }
.skeleton-date     { width: 60px; height: 12px; }
.skeleton-btn      { width: 44px; height: 28px; border-radius: 8px; }

@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
