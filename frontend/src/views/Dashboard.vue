<template>
  <div class="dashboard">
    <h2>仪表板</h2>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" :size="40" color="#409EFF">
              <Monitor />
            </el-icon>
            <div>
              <div class="stat-value">{{ deviceCount }}</div>
              <div class="stat-label">设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" :size="40" color="#67C23A">
              <SuccessFilled />
            </el-icon>
            <div>
              <div class="stat-value">{{ onlineDevices }}</div>
              <div class="stat-label">在线设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" :size="40" color="#E6A23C">
              <Switch />
            </el-icon>
            <div>
              <div class="stat-value">{{ sceneCount }}</div>
              <div class="stat-label">场景总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" :size="40" color="#F56C6C">
              <Clock />
            </el-icon>
            <div>
              <div class="stat-value">{{ activeScenes }}</div>
              <div class="stat-label">活跃场景</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近设备</span>
              <el-button type="primary" size="small" @click="$router.push('/devices')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="recentDevices" style="width: 100%">
            <el-table-column prop="name" label="设备名称" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速场景</span>
              <el-button type="primary" size="small" @click="$router.push('/scenes')">
                查看全部
              </el-button>
            </div>
          </template>
          <div class="quick-scenes">
            <el-button
              v-for="scene in quickScenes"
              :key="scene.id"
              type="primary"
              size="large"
              @click="executeScene(scene.sceneId)"
              style="margin: 10px;"
            >
              {{ scene.name }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { deviceService, sceneService } from '../services/api'

const deviceCount = ref(0)
const onlineDevices = ref(0)
const sceneCount = ref(0)
const activeScenes = ref(0)
const recentDevices = ref([])
const quickScenes = ref([])

const getStatusType = (status) => {
  const types = {
    ONLINE: 'success',
    OFFLINE: 'info',
    ERROR: 'danger',
    DISABLED: 'warning'
  }
  return types[status] || 'info'
}

const loadData = async () => {
  try {
    const [devicesRes, scenesRes, activeScenesRes] = await Promise.all([
      deviceService.getAll(),
      sceneService.getAll(),
      sceneService.getActive()
    ])
    
    const devices = devicesRes.data
    deviceCount.value = devices.length
    onlineDevices.value = devices.filter(d => d.status === 'ONLINE').length
    recentDevices.value = devices.slice(-5).reverse()
    
    sceneCount.value = scenesRes.data.length
    activeScenes.value = activeScenesRes.data.length
    quickScenes.value = activeScenesRes.data.slice(0, 6)
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
    ElMessage.error('加载数据失败')
  }
}

const executeScene = async (sceneId) => {
  try {
    await sceneService.execute(sceneId)
    ElMessage.success('场景执行成功')
  } catch (error) {
    console.error('Failed to execute scene:', error)
    ElMessage.error('场景执行失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  flex-shrink: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-scenes {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
