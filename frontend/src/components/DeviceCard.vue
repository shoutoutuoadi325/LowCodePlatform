<template>
  <div class="device-card card">
    <div class="device-header">
      <h3 class="device-name">{{ device.name }}</h3>
      <span :class="['status-badge', device.status === 'ONLINE' ? 'status-online' : 'status-offline']">
        {{ device.status === 'ONLINE' ? '在线' : '离线' }}
      </span>
    </div>
    <div class="device-info">
      <p><strong>类型:</strong> {{ getDeviceTypeLabel(device.type) }}</p>
      <p><strong>位置:</strong> {{ device.floor }} - {{ device.room }}</p>
    </div>
    <div class="device-state">
      <h4>当前状态</h4>
      <div class="state-grid">
        <div v-for="(value, key) in device.state" :key="key" class="state-item">
          <span class="state-key">{{ formatStateKey(key) }}:</span>
          <span class="state-value">{{ formatStateValue(key, value) }}</span>
        </div>
      </div>
    </div>
    <div class="device-controls">
      <template v-if="device.type === 'LIGHT'">
        <button @click="togglePower" class="btn btn-primary btn-sm">
          {{ device.state.power ? '关灯' : '开灯' }}
        </button>
        <button @click="showBrightnessControl = !showBrightnessControl" class="btn btn-secondary btn-sm">
          调节亮度
        </button>
        <div v-if="showBrightnessControl" class="brightness-control">
          <input type="range" min="0" max="100" v-model="brightness" @change="setBrightness" />
          <span>{{ brightness }}%</span>
        </div>
      </template>
      <template v-else-if="device.type === 'AIR_CONDITIONER'">
        <button @click="togglePower" class="btn btn-primary btn-sm">
          {{ device.state.power ? '关闭' : '开启' }}
        </button>
        <button @click="showTempControl = !showTempControl" class="btn btn-secondary btn-sm">
          调节温度
        </button>
        <div v-if="showTempControl" class="temp-control">
          <input type="number" min="16" max="30" v-model="temperature" @change="setTemperature" />
          <span>°C</span>
        </div>
      </template>
      <template v-else-if="device.type === 'PROJECTOR'">
        <button @click="togglePower" class="btn btn-primary btn-sm">
          {{ device.state.power ? '关闭' : '开启' }}
        </button>
      </template>
      <template v-else-if="device.type === 'CURTAIN'">
        <button @click="openCurtain" class="btn btn-success btn-sm">打开</button>
        <button @click="closeCurtain" class="btn btn-danger btn-sm">关闭</button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  device: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

const showBrightnessControl = ref(false)
const showTempControl = ref(false)
const brightness = ref(props.device.state.brightness || 0)
const temperature = ref(props.device.state.temperature || 26)

watch(() => props.device.state.brightness, (newVal) => {
  brightness.value = newVal || 0
})

watch(() => props.device.state.temperature, (newVal) => {
  temperature.value = newVal || 26
})

const getDeviceTypeLabel = (type) => {
  const labels = {
    'LIGHT': '灯光',
    'AIR_CONDITIONER': '空调',
    'PROJECTOR': '投影仪',
    'CURTAIN': '窗帘',
    'DOOR': '门',
    'WINDOW': '窗户'
  }
  return labels[type] || type
}

const formatStateKey = (key) => {
  const labels = {
    'power': '电源',
    'brightness': '亮度',
    'temperature': '温度',
    'mode': '模式',
    'fanSpeed': '风速',
    'input': '输入源',
    'position': '位置',
    'color': '颜色'
  }
  return labels[key] || key
}

const formatStateValue = (key, value) => {
  if (key === 'power') return value ? '开' : '关'
  if (key === 'brightness') return value + '%'
  if (key === 'temperature') return value + '°C'
  if (key === 'position') return value + '%'
  return value
}

const togglePower = () => {
  const action = props.device.state.power ? 'turnOff' : 'turnOn'
  emit('action', {
    deviceId: props.device.id,
    action,
    parameters: {}
  })
}

const setBrightness = () => {
  emit('action', {
    deviceId: props.device.id,
    action: 'setBrightness',
    parameters: { brightness: parseInt(brightness.value) }
  })
}

const setTemperature = () => {
  emit('action', {
    deviceId: props.device.id,
    action: 'setTemperature',
    parameters: { temperature: parseInt(temperature.value) }
  })
}

const openCurtain = () => {
  emit('action', {
    deviceId: props.device.id,
    action: 'open',
    parameters: {}
  })
}

const closeCurtain = () => {
  emit('action', {
    deviceId: props.device.id,
    action: 'close',
    parameters: {}
  })
}
</script>

<style scoped>
.device-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.device-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.device-name {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
}

.device-info p {
  margin: 0.25rem 0;
  font-size: 0.9rem;
  color: #6b7280;
}

.device-state {
  border-top: 1px solid #e5e7eb;
  padding-top: 1rem;
}

.device-state h4 {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.state-grid {
  display: grid;
  gap: 0.5rem;
}

.state-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
}

.state-key {
  color: #6b7280;
}

.state-value {
  font-weight: 600;
  color: #1f2937;
}

.device-controls {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn-sm {
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
}

.brightness-control,
.temp-control {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.brightness-control input[type="range"],
.temp-control input[type="number"] {
  flex: 1;
}

.temp-control input[type="number"] {
  width: 80px;
}
</style>
