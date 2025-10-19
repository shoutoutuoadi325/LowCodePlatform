<template>
  <div class="simulator-management">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>设备模拟器管理</h2>
          <p>管理和控制设备模拟器，用于测试和开发</p>
        </div>
        <div class="action-section">
          <el-button @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            创建模拟器
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 模拟器列表 -->
    <el-card class="simulator-list">
      <template #header>
        <div class="list-header">
          <span>模拟器列表</span>
          <div class="list-controls">
            <el-input
              v-model="searchText"
              placeholder="搜索模拟器"
              style="width: 200px"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px" clearable>
              <el-option label="运行中" value="running" />
              <el-option label="已停止" value="stopped" />
              <el-option label="错误" value="error" />
            </el-select>
          </div>
        </div>
      </template>
      
      <div v-loading="loading">
        <el-table :data="filteredSimulators" style="width: 100%">
          <el-table-column prop="name" label="模拟器名称" min-width="150" />
          <el-table-column prop="deviceType" label="设备类型" width="120" />
          <el-table-column prop="deviceCount" label="设备数量" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <StatusIndicator :status="row.status" />
            </template>
          </el-table-column>
          <el-table-column prop="uptime" label="运行时长" width="120">
            <template #default="{ row }">
              {{ formatUptime(row.uptime) }}
            </template>
          </el-table-column>
          <el-table-column prop="messagesSent" label="发送消息数" width="120" />
          <el-table-column prop="lastActivity" label="最后活动" width="150">
            <template #default="{ row }">
              {{ formatDate(row.lastActivity) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'stopped'"
                size="small"
                type="success"
                @click="startSimulator(row)"
                :loading="row.starting"
              >
                启动
              </el-button>
              <el-button
                v-else-if="row.status === 'running'"
                size="small"
                type="warning"
                @click="stopSimulator(row)"
                :loading="row.stopping"
              >
                停止
              </el-button>
              <el-button
                size="small"
                @click="viewSimulator(row)"
              >
                查看
              </el-button>
              <el-button
                size="small"
                @click="editSimulator(row)"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deleteSimulator(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 创建/编辑模拟器对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingSimulator ? '编辑模拟器' : '创建模拟器'"
      width="600px"
    >
      <el-form
        ref="simulatorFormRef"
        :model="simulatorForm"
        :rules="simulatorRules"
        label-width="100px"
      >
        <el-form-item label="模拟器名称" prop="name">
          <el-input v-model="simulatorForm.name" />
        </el-form-item>
        
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="simulatorForm.deviceType" style="width: 100%" @change="onDeviceTypeChange">
            <el-option
              v-for="type in deviceTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="设备数量" prop="deviceCount">
          <el-input-number
            v-model="simulatorForm.deviceCount"
            :min="1"
            :max="100"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="消息间隔(秒)" prop="messageInterval">
          <el-input-number
            v-model="simulatorForm.messageInterval"
            :min="1"
            :max="3600"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="MQTT主题" prop="mqttTopic">
          <el-input v-model="simulatorForm.mqttTopic" />
        </el-form-item>
        
        <el-form-item label="数据模板" prop="dataTemplate">
          <JsonEditor
            v-model="simulatorForm.dataTemplate"
            height="200px"
          />
        </el-form-item>
        
        <el-form-item label="自动启动">
          <el-switch v-model="simulatorForm.autoStart" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSimulator" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 模拟器详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="模拟器详情"
      width="800px"
    >
      <div v-if="selectedSimulator" class="simulator-detail">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="名称">
            {{ selectedSimulator.name }}
          </el-descriptions-item>
          <el-descriptions-item label="设备类型">
            {{ selectedSimulator.deviceType }}
          </el-descriptions-item>
          <el-descriptions-item label="设备数量">
            {{ selectedSimulator.deviceCount }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <StatusIndicator :status="selectedSimulator.status" />
          </el-descriptions-item>
          <el-descriptions-item label="运行时长">
            {{ formatUptime(selectedSimulator.uptime) }}
          </el-descriptions-item>
          <el-descriptions-item label="发送消息数">
            {{ selectedSimulator.messagesSent }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 实时日志 -->
        <div class="log-section">
          <div class="log-header">
            <h4>实时日志</h4>
            <div class="log-controls">
              <el-switch
                v-model="autoRefreshLogs"
                active-text="自动刷新"
                @change="toggleLogRefresh"
              />
              <el-button size="small" @click="clearLogs">清空日志</el-button>
            </div>
          </div>
          
          <div class="log-container" ref="logContainer">
            <div
              v-for="(log, index) in simulatorLogs"
              :key="index"
              class="log-item"
              :class="log.level"
            >
              <span class="log-time">{{ formatTime(log.timestamp) }}</span>
              <span class="log-level">{{ log.level.toUpperCase() }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </div>

        <!-- 性能监控 -->
        <div class="performance-section">
          <h4>性能监控</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-value">{{ performanceMetrics.messagesPerSecond }}</div>
                <div class="metric-label">消息/秒</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-value">{{ performanceMetrics.cpuUsage }}%</div>
                <div class="metric-label">CPU使用率</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-value">{{ performanceMetrics.memoryUsage }}MB</div>
                <div class="metric-label">内存使用</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { simulatorService, deviceTypeService } from '@/services/api'
import StatusIndicator from '@/components/StatusIndicator.vue'
import JsonEditor from '@/components/JsonEditor.vue'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const searchText = ref('')
const statusFilter = ref('')
const simulators = ref([])
const deviceTypes = ref([])
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const editingSimulator = ref(null)
const selectedSimulator = ref(null)
const simulatorLogs = ref([])
const autoRefreshLogs = ref(false)
const logRefreshTimer = ref(null)
const logContainer = ref(null)

// 表单数据
const simulatorForm = reactive({
  name: '',
  deviceType: '',
  deviceCount: 1,
  messageInterval: 10,
  mqttTopic: '',
  dataTemplate: {},
  autoStart: false
})

// 性能指标
const performanceMetrics = reactive({
  messagesPerSecond: 0,
  cpuUsage: 0,
  memoryUsage: 0
})

// 表单引用
const simulatorFormRef = ref()

// 表单验证规则
const simulatorRules = {
  name: [{ required: true, message: '请输入模拟器名称', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  deviceCount: [{ required: true, message: '请输入设备数量', trigger: 'blur' }],
  messageInterval: [{ required: true, message: '请输入消息间隔', trigger: 'blur' }],
  mqttTopic: [{ required: true, message: '请输入MQTT主题', trigger: 'blur' }]
}

// 计算属性
const filteredSimulators = computed(() => {
  let filtered = simulators.value

  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(sim => 
      sim.name.toLowerCase().includes(search) ||
      sim.deviceType.toLowerCase().includes(search)
    )
  }

  if (statusFilter.value) {
    filtered = filtered.filter(sim => sim.status === statusFilter.value)
  }

  return filtered
})

// 方法
const loadSimulators = async () => {
  loading.value = true
  try {
    const response = await simulatorService.getAll()
    // 确保response.data是数组
    const data = Array.isArray(response.data) ? response.data : []
    simulators.value = data.map(sim => ({
      ...sim,
      starting: false,
      stopping: false
    }))
  } catch (error) {
    ElMessage.error('加载模拟器列表失败: ' + error.message)
    // 确保在错误情况下也设置为空数组
    simulators.value = []
  } finally {
    loading.value = false
  }
}

const loadDeviceTypes = async () => {
  try {
    const response = await deviceTypeService.getAll()
    // 确保response.data是数组
    deviceTypes.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    ElMessage.error('加载设备类型失败: ' + error.message)
    // 确保在错误情况下也设置为空数组
    deviceTypes.value = []
  }
}

const refreshData = () => {
  loadSimulators()
}

const startSimulator = async (simulator) => {
  simulator.starting = true
  try {
    await simulatorService.start(simulator.id)
    simulator.status = 'running'
    ElMessage.success('模拟器启动成功')
    loadSimulators() // 重新加载以获取最新状态
  } catch (error) {
    ElMessage.error('启动模拟器失败: ' + error.message)
  } finally {
    simulator.starting = false
  }
}

const stopSimulator = async (simulator) => {
  simulator.stopping = true
  try {
    await simulatorService.stop(simulator.id)
    simulator.status = 'stopped'
    ElMessage.success('模拟器停止成功')
    loadSimulators() // 重新加载以获取最新状态
  } catch (error) {
    ElMessage.error('停止模拟器失败: ' + error.message)
  } finally {
    simulator.stopping = false
  }
}

const viewSimulator = async (simulator) => {
  selectedSimulator.value = simulator
  showDetailDialog.value = true
  await loadSimulatorLogs()
  await loadPerformanceMetrics()
}

const editSimulator = (simulator) => {
  editingSimulator.value = simulator
  Object.assign(simulatorForm, {
    name: simulator.name,
    deviceType: simulator.deviceType,
    deviceCount: simulator.deviceCount,
    messageInterval: simulator.messageInterval,
    mqttTopic: simulator.mqttTopic,
    dataTemplate: simulator.dataTemplate || {},
    autoStart: simulator.autoStart || false
  })
  showCreateDialog.value = true
}

const deleteSimulator = async (simulator) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模拟器 "${simulator.name}" 吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await simulatorService.delete(simulator.id)
    ElMessage.success('模拟器删除成功')
    loadSimulators()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除模拟器失败: ' + error.message)
    }
  }
}

const saveSimulator = async () => {
  try {
    await simulatorFormRef.value.validate()
    saving.value = true
    
    if (editingSimulator.value) {
      await simulatorService.update(editingSimulator.value.id, simulatorForm)
      ElMessage.success('模拟器更新成功')
    } else {
      await simulatorService.create(simulatorForm)
      ElMessage.success('模拟器创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    loadSimulators()
  } catch (error) {
    if (error.message) {
      ElMessage.error('保存模拟器失败: ' + error.message)
    }
  } finally {
    saving.value = false
  }
}

const onDeviceTypeChange = (typeId) => {
  const deviceType = deviceTypes.value.find(type => type.id === typeId)
  if (deviceType) {
    // 根据设备类型设置默认的数据模板和MQTT主题
    simulatorForm.mqttTopic = `devices/${deviceType.name}/data`
    simulatorForm.dataTemplate = deviceType.properties || {}
  }
}

const resetForm = () => {
  editingSimulator.value = null
  Object.assign(simulatorForm, {
    name: '',
    deviceType: '',
    deviceCount: 1,
    messageInterval: 10,
    mqttTopic: '',
    dataTemplate: {},
    autoStart: false
  })
}

const loadSimulatorLogs = async () => {
  if (!selectedSimulator.value) return
  
  try {
    const response = await simulatorService.getLogs(selectedSimulator.value.id)
    simulatorLogs.value = response.data || []
    
    // 滚动到底部
    await nextTick()
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  } catch (error) {
    ElMessage.error('加载日志失败: ' + error.message)
  }
}

const loadPerformanceMetrics = async () => {
  if (!selectedSimulator.value) return
  
  try {
    const response = await simulatorService.getMetrics(selectedSimulator.value.id)
    Object.assign(performanceMetrics, response.data)
  } catch (error) {
    ElMessage.error('加载性能指标失败: ' + error.message)
  }
}

const toggleLogRefresh = (enabled) => {
  if (enabled) {
    logRefreshTimer.value = setInterval(() => {
      loadSimulatorLogs()
      loadPerformanceMetrics()
    }, 2000) // 每2秒刷新一次
  } else {
    if (logRefreshTimer.value) {
      clearInterval(logRefreshTimer.value)
      logRefreshTimer.value = null
    }
  }
}

const clearLogs = () => {
  simulatorLogs.value = []
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
}

const formatTime = (dateString) => {
  if (!dateString) return '-'
  return dayjs(dateString).format('HH:mm:ss')
}

const formatUptime = (seconds) => {
  if (!seconds) return '-'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${hours}h ${minutes}m ${secs}s`
}

// 生命周期
onMounted(() => {
  loadSimulators()
  loadDeviceTypes()
})

onUnmounted(() => {
  if (logRefreshTimer.value) {
    clearInterval(logRefreshTimer.value)
  }
})
</script>

<style scoped>
.simulator-management {
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

.action-section {
  display: flex;
  gap: 12px;
}

.simulator-list {
  margin-bottom: 0;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-controls {
  display: flex;
  gap: 12px;
}

.simulator-detail {
  max-height: 600px;
  overflow-y: auto;
}

.log-section {
  margin-top: 20px;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.log-header h4 {
  margin: 0;
}

.log-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.log-container {
  height: 200px;
  overflow-y: auto;
  background-color: #1e1e1e;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-item {
  margin-bottom: 4px;
  line-height: 1.4;
}

.log-time {
  color: #888;
  margin-right: 8px;
}

.log-level {
  margin-right: 8px;
  font-weight: bold;
}

.log-item.info .log-level {
  color: #409eff;
}

.log-item.warn .log-level {
  color: #e6a23c;
}

.log-item.error .log-level {
  color: #f56c6c;
}

.log-item.debug .log-level {
  color: #909399;
}

.log-message {
  color: #fff;
}

.performance-section {
  margin-top: 20px;
}

.performance-section h4 {
  margin: 0 0 12px 0;
}

.metric-card {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 14px;
  color: #909399;
}
</style>