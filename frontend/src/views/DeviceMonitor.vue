<template>
  <div class="device-monitor">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>设备监控</h2>
          <p>实时监控设备状态，查看历史数据和告警信息</p>
        </div>
        <div class="action-section">
          <el-button @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 设备列表 -->
      <el-col :span="8">
        <el-card class="device-list-card">
          <template #header>
            <div class="card-header">
              <span>设备列表</span>
              <el-input
                v-model="searchText"
                placeholder="搜索设备"
                size="small"
                style="width: 150px"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </template>
          
          <div class="device-list" v-loading="loading">
            <div
              v-for="device in filteredDevices"
              :key="device.id"
              class="device-item"
              :class="{ active: selectedDevice?.id === device.id }"
              @click="selectDevice(device)"
            >
              <div class="device-info">
                <div class="device-name">{{ device.name }}</div>
                <div class="device-location">{{ device.location }} - {{ device.room }}</div>
              </div>
              <div class="device-status">
                <StatusIndicator :status="device.status" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 设备详情和监控 -->
      <el-col :span="16">
        <div v-if="!selectedDevice" class="no-selection">
          <el-empty description="请选择一个设备查看监控信息" />
        </div>
        
        <div v-else class="device-monitor-content">
          <!-- 设备基本信息 -->
          <el-card class="device-info-card">
            <template #header>
              <div class="card-header">
                <span>{{ selectedDevice.name }} - 基本信息</span>
                <el-tag :type="getStatusType(selectedDevice.status)">
                  {{ selectedDevice.status }}
                </el-tag>
              </div>
            </template>
            
            <el-descriptions :column="3" border>
              <el-descriptions-item label="设备ID">
                {{ selectedDevice.deviceId }}
              </el-descriptions-item>
              <el-descriptions-item label="设备类型">
                {{ selectedDevice.type }}
              </el-descriptions-item>
              <el-descriptions-item label="位置">
                {{ selectedDevice.location }}
              </el-descriptions-item>
              <el-descriptions-item label="房间">
                {{ selectedDevice.room }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <StatusIndicator :status="selectedDevice.status" />
              </el-descriptions-item>
              <el-descriptions-item label="最后更新">
                {{ formatDate(deviceState.lastUpdate) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 实时状态 -->
          <el-card class="real-time-card">
            <template #header>
              <div class="card-header">
                <span>实时状态</span>
                <el-switch
                  v-model="autoRefresh"
                  active-text="自动刷新"
                  @change="toggleAutoRefresh"
                />
              </div>
            </template>
            
            <div v-loading="stateLoading" class="state-content">
              <div v-if="!deviceState.data" class="no-data">
                <el-empty description="暂无状态数据" :image-size="80" />
              </div>
              
              <div v-else class="state-grid">
                <div
                  v-for="(value, key) in deviceState.data"
                  :key="key"
                  class="state-item"
                >
                  <div class="state-label">{{ key }}</div>
                  <div class="state-value">{{ formatValue(value) }}</div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 历史数据图表 -->
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>历史数据</span>
                <div class="chart-controls">
                  <el-select v-model="selectedMetric" placeholder="选择指标" style="width: 120px">
                    <el-option
                      v-for="metric in availableMetrics"
                      :key="metric"
                      :label="metric"
                      :value="metric"
                    />
                  </el-select>
                  <el-select v-model="timeRange" @change="loadHistoryData" style="width: 120px">
                    <el-option label="1小时" value="1h" />
                    <el-option label="6小时" value="6h" />
                    <el-option label="24小时" value="24h" />
                    <el-option label="7天" value="7d" />
                  </el-select>
                </div>
              </div>
            </template>
            
            <div class="chart-container" v-loading="chartLoading">
              <DataChart
                v-if="chartData.length > 0"
                :data="chartData"
                :metric="selectedMetric"
                height="300px"
              />
              <div v-else class="no-chart-data">
                <el-empty description="暂无历史数据" :image-size="80" />
              </div>
            </div>
          </el-card>

          <!-- 告警信息 -->
          <el-card class="alerts-card">
            <template #header>
              <div class="card-header">
                <span>告警信息</span>
                <el-badge :value="alerts.length" :max="99" type="danger">
                  <el-icon><Warning /></el-icon>
                </el-badge>
              </div>
            </template>
            
            <div v-loading="alertsLoading">
              <div v-if="alerts.length === 0" class="no-alerts">
                <el-empty description="暂无告警信息" :image-size="80" />
              </div>
              
              <div v-else class="alerts-list">
                <div
                  v-for="alert in alerts"
                  :key="alert.id"
                  class="alert-item"
                  :class="alert.level"
                >
                  <div class="alert-content">
                    <div class="alert-title">{{ alert.title }}</div>
                    <div class="alert-message">{{ alert.message }}</div>
                    <div class="alert-time">{{ formatDate(alert.timestamp) }}</div>
                  </div>
                  <div class="alert-actions">
                    <el-button
                      v-if="!alert.acknowledged"
                      size="small"
                      @click="acknowledgeAlert(alert)"
                    >
                      确认
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { deviceService, monitorService } from '@/services/api'
import StatusIndicator from '@/components/StatusIndicator.vue'
import DataChart from '@/components/DataChart.vue'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const stateLoading = ref(false)
const chartLoading = ref(false)
const alertsLoading = ref(false)
const searchText = ref('')
const devices = ref([])
const selectedDevice = ref(null)
const deviceState = ref({ data: null, lastUpdate: null })
const chartData = ref([])
const alerts = ref([])
const autoRefresh = ref(false)
const refreshTimer = ref(null)
const selectedMetric = ref('')
const timeRange = ref('1h')

// 计算属性
const filteredDevices = computed(() => {
  if (!searchText.value) return devices.value
  const search = searchText.value.toLowerCase()
  return devices.value.filter(device => 
    device.name.toLowerCase().includes(search) ||
    device.location.toLowerCase().includes(search) ||
    device.room.toLowerCase().includes(search)
  )
})

const availableMetrics = computed(() => {
  if (!deviceState.value.data) return []
  return Object.keys(deviceState.value.data).filter(key => 
    typeof deviceState.value.data[key] === 'number'
  )
})

// 方法
const loadDevices = async () => {
  loading.value = true
  try {
    const response = await deviceService.getAll()
    // 确保response.data是数组
    devices.value = Array.isArray(response.data) ? response.data : []
    if (devices.value.length > 0 && !selectedDevice.value) {
      selectDevice(devices.value[0])
    }
  } catch (error) {
    ElMessage.error('加载设备列表失败: ' + error.message)
    // 确保在错误情况下也设置为空数组
    devices.value = []
  } finally {
    loading.value = false
  }
}

const selectDevice = (device) => {
  selectedDevice.value = device
  loadDeviceState()
  loadHistoryData()
  loadAlerts()
}

const loadDeviceState = async () => {
  if (!selectedDevice.value) return
  
  stateLoading.value = true
  try {
    const response = await monitorService.getDeviceState(selectedDevice.value.deviceId)
    deviceState.value = {
      data: response.data,
      lastUpdate: new Date().toISOString()
    }
    
    // 设置默认指标
    if (availableMetrics.value.length > 0 && !selectedMetric.value) {
      selectedMetric.value = availableMetrics.value[0]
    }
  } catch (error) {
    ElMessage.error('加载设备状态失败: ' + error.message)
    deviceState.value = { data: null, lastUpdate: null }
  } finally {
    stateLoading.value = false
  }
}

const loadHistoryData = async () => {
  if (!selectedDevice.value || !selectedMetric.value) return
  
  chartLoading.value = true
  try {
    const endTime = new Date()
    const startTime = new Date()
    
    switch (timeRange.value) {
      case '1h':
        startTime.setHours(startTime.getHours() - 1)
        break
      case '6h':
        startTime.setHours(startTime.getHours() - 6)
        break
      case '24h':
        startTime.setDate(startTime.getDate() - 1)
        break
      case '7d':
        startTime.setDate(startTime.getDate() - 7)
        break
    }
    
    const response = await monitorService.getDeviceHistory(
      selectedDevice.value.deviceId,
      startTime.toISOString(),
      endTime.toISOString()
    )
    
    chartData.value = response.data || []
  } catch (error) {
    ElMessage.error('加载历史数据失败: ' + error.message)
    chartData.value = []
  } finally {
    chartLoading.value = false
  }
}

const loadAlerts = async () => {
  if (!selectedDevice.value) return
  
  alertsLoading.value = true
  try {
    const response = await monitorService.getDeviceAlerts(selectedDevice.value.deviceId)
    alerts.value = response.data || []
  } catch (error) {
    ElMessage.error('加载告警信息失败: ' + error.message)
    alerts.value = []
  } finally {
    alertsLoading.value = false
  }
}

const acknowledgeAlert = async (alert) => {
  try {
    await monitorService.acknowledgeAlert(alert.id)
    alert.acknowledged = true
    ElMessage.success('告警已确认')
  } catch (error) {
    ElMessage.error('确认告警失败: ' + error.message)
  }
}

const refreshData = () => {
  loadDevices()
  if (selectedDevice.value) {
    loadDeviceState()
    loadHistoryData()
    loadAlerts()
  }
}

const toggleAutoRefresh = (enabled) => {
  if (enabled) {
    refreshTimer.value = setInterval(() => {
      if (selectedDevice.value) {
        loadDeviceState()
      }
    }, 5000) // 每5秒刷新一次
  } else {
    if (refreshTimer.value) {
      clearInterval(refreshTimer.value)
      refreshTimer.value = null
    }
  }
}

const getStatusType = (status) => {
  const typeMap = {
    online: 'success',
    offline: 'info',
    warning: 'warning',
    error: 'danger'
  }
  return typeMap[status] || 'info'
}

const formatValue = (value) => {
  if (typeof value === 'number') {
    return value.toFixed(2)
  }
  return value
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
}

// 生命周期
onMounted(() => {
  loadDevices()
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
})
</script>

<style scoped>
.device-monitor {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.title-section p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.device-list-card {
  height: calc(100vh - 200px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.device-list {
  max-height: calc(100vh - 280px);
  overflow-y: auto;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.device-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.device-item.active {
  border-color: #409eff;
  background-color: #e6f7ff;
}

.device-info {
  flex: 1;
}

.device-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.device-location {
  font-size: 12px;
  color: #909399;
}

.no-selection {
  height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.device-monitor-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.device-info-card,
.real-time-card,
.chart-card,
.alerts-card {
  margin-bottom: 0;
}

.state-content {
  min-height: 120px;
}

.state-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.state-item {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
  text-align: center;
}

.state-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.state-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.chart-controls {
  display: flex;
  gap: 12px;
}

.chart-container {
  min-height: 300px;
}

.no-data,
.no-chart-data,
.no-alerts {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
}

.alerts-list {
  max-height: 300px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-left: 4px solid #e6a23c;
  background-color: #fdf6ec;
  border-radius: 4px;
  margin-bottom: 8px;
}

.alert-item.error {
  border-left-color: #f56c6c;
  background-color: #fef0f0;
}

.alert-item.warning {
  border-left-color: #e6a23c;
  background-color: #fdf6ec;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.alert-message {
  color: #606266;
  margin-bottom: 4px;
}

.alert-time {
  font-size: 12px;
  color: #909399;
}
</style>