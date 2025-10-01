<template>
  <div class="device-view">
    <div class="card">
      <h2 class="card-title">设备管理</h2>
      <div v-if="loading">加载中...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else>
        <div class="filter-group">
          <label class="form-label">按教室筛选：</label>
          <select v-model="selectedRoom" @change="filterDevices">
            <option value="">全部</option>
            <option value="101教室">101教室</option>
            <option value="102教室">102教室</option>
            <option value="走廊">走廊</option>
          </select>
        </div>
        <div class="grid grid-3">
          <DeviceCard
            v-for="device in filteredDevices"
            :key="device.id"
            :device="device"
            @action="handleAction"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useDeviceStore } from '../stores/deviceStore'
import DeviceCard from '../components/DeviceCard.vue'

const deviceStore = useDeviceStore()
const selectedRoom = ref('')

const loading = computed(() => deviceStore.loading)
const error = computed(() => deviceStore.error)
const devices = computed(() => deviceStore.devices)

const filteredDevices = computed(() => {
  if (!selectedRoom.value) return devices.value
  return deviceStore.getDevicesByRoom(selectedRoom.value)
})

const filterDevices = () => {
  // Filtering is reactive through computed property
}

const handleAction = async ({ deviceId, action, parameters }) => {
  try {
    await deviceStore.executeDeviceAction(deviceId, action, parameters)
  } catch (error) {
    alert('操作失败: ' + error.message)
  }
}

onMounted(() => {
  deviceStore.fetchDevices()
})
</script>

<style scoped>
.device-view {
  max-width: 1400px;
}

.filter-group {
  margin-bottom: 1.5rem;
}

.filter-group select {
  max-width: 300px;
}

.error {
  color: #dc2626;
  padding: 1rem;
  background: #fee2e2;
  border-radius: 4px;
}
</style>
