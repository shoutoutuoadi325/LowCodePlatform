<template>
  <div class="scene-card card">
    <div class="scene-header">
      <h3 class="scene-name">{{ scene.name }}</h3>
      <span :class="['status-badge', scene.enabled ? 'status-online' : 'status-offline']">
        {{ scene.enabled ? '启用' : '禁用' }}
      </span>
    </div>
    <p class="scene-description">{{ scene.description }}</p>
    
    <div class="scene-actions">
      <h4>动作列表 ({{ scene.actions.length }}个)</h4>
      <div class="action-list">
        <div v-for="(action, index) in scene.actions" :key="index" class="action-preview">
          <span class="action-order">{{ index + 1 }}</span>
          <span class="action-text">
            {{ getActionText(action) }}
            <span v-if="action.delaySeconds > 0" class="delay-text">
              (延迟{{ action.delaySeconds }}秒)
            </span>
          </span>
        </div>
      </div>
    </div>

    <div class="scene-controls">
      <button @click="$emit('execute', scene.id)" class="btn btn-success btn-block">
        执行场景
      </button>
      <div class="control-buttons">
        <button @click="$emit('edit', scene)" class="btn btn-secondary btn-sm">编辑</button>
        <button @click="$emit('delete', scene.id)" class="btn btn-danger btn-sm">删除</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  scene: {
    type: Object,
    required: true
  }
})

defineEmits(['execute', 'edit', 'delete'])

const getActionText = (action) => {
  const actionLabels = {
    'turnOn': '打开',
    'turnOff': '关闭',
    'setBrightness': '设置亮度',
    'setColor': '设置颜色',
    'setTemperature': '设置温度',
    'setMode': '设置模式',
    'setFanSpeed': '设置风速',
    'setInput': '设置输入源',
    'open': '打开',
    'close': '关闭',
    'setPosition': '设置位置'
  }
  
  const deviceName = action.deviceId || '未知设备'
  const actionLabel = actionLabels[action.action] || action.action
  
  let params = ''
  if (action.parameters && Object.keys(action.parameters).length > 0) {
    params = ' - ' + Object.entries(action.parameters)
      .map(([k, v]) => `${v}`)
      .join(', ')
  }
  
  return `${deviceName}: ${actionLabel}${params}`
}
</script>

<style scoped>
.scene-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  height: 100%;
}

.scene-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.scene-name {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
}

.scene-description {
  margin: 0;
  font-size: 0.9rem;
  color: #6b7280;
  line-height: 1.5;
}

.scene-actions {
  flex: 1;
}

.scene-actions h4 {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.action-preview {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.5rem;
  background: #f9fafb;
  border-radius: 4px;
  font-size: 0.85rem;
}

.action-order {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  font-size: 0.75rem;
  font-weight: 600;
  flex-shrink: 0;
}

.action-text {
  flex: 1;
  color: #374151;
  line-height: 1.4;
}

.delay-text {
  color: #9ca3af;
  font-style: italic;
  font-size: 0.8rem;
}

.scene-controls {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  border-top: 1px solid #e5e7eb;
  padding-top: 1rem;
}

.control-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn-block {
  width: 100%;
}

.btn-sm {
  flex: 1;
}
</style>
