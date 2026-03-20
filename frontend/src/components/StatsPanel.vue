<template>
  <div class="stats-panel">
    <div class="stat-card">
      <span class="stat-number">{{ stats.total }}</span>
      <span class="stat-label">全部</span>
    </div>
    <div class="stat-card stat-completed">
      <span class="stat-number">{{ stats.completed }}</span>
      <span class="stat-label">已完成</span>
    </div>
    <div class="stat-card stat-active">
      <span class="stat-number">{{ stats.active }}</span>
      <span class="stat-label">进行中</span>
    </div>
    <div class="stat-card stat-high">
      <span class="stat-number">{{ stats.highPriority }}</span>
      <span class="stat-label">高优先级</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  stats: {
    type: Object,
    default: () => ({ total: 0, completed: 0, active: 0, highPriority: 0 }),
  },
})
</script>

<style scoped>
.stats-panel {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border-radius: 10px;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
}

.stat-number {
  font-size: 1.6rem;
  font-weight: 700;
  color: #2d3748;
  line-height: 1;
}

.stat-label {
  font-size: 0.75rem;
  color: #718096;
  margin-top: 4px;
}

.stat-completed .stat-number { color: #38a169; }
.stat-active    .stat-number { color: #667eea; }
.stat-high      .stat-number { color: #e53e3e; }

@media (max-width: 768px) {
  .stats-panel {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
