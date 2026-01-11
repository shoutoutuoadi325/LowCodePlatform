<template>
  <div class="scene-designer">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>场景编排设计器</h2>
          <div>
            <el-button @click="$router.push('/scenes')">返回</el-button>
            <el-button type="primary" @click="saveScene">保存场景</el-button>
          </div>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="never" class="config-panel">
            <template #header>
              <span>场景配置</span>
            </template>
            
            <el-form label-width="80px">
              <el-form-item label="场景名称">
                <el-input v-model="scene.name" placeholder="输入场景名称" />
              </el-form-item>
              <el-form-item label="场景描述">
                <el-input
                  v-model="scene.description"
                  type="textarea"
                  :rows="3"
                  placeholder="输入场景描述"
                />
              </el-form-item>
              <el-form-item label="状态">
                <el-switch
                  v-model="scene.status"
                  active-value="ACTIVE"
                  inactive-value="INACTIVE"
                  active-text="启用"
                  inactive-text="禁用"
                />
              </el-form-item>
            </el-form>
            
            <el-divider />
            
            <h4>可用设备</h4>
            <div class="device-list">
              <div
                v-for="device in devices"
                :key="device.id"
                class="device-item"
                draggable="true"
                @dragstart="onDeviceDragStart(device)"
              >
                <div class="device-info">
                  <div class="device-name">
                    <el-icon><Monitor /></el-icon>
                    <span>{{ device.name }}</span>
                    <el-tag 
                      :type="getStatusType(device.status)" 
                      size="small"
                      style="margin-left: 8px"
                    >
                      {{ device.status }}
                    </el-tag>
                  </div>
                  <div class="device-location">
                    <el-text size="small" type="info">
                      {{ device.room || '未设置房间' }} - {{ device.location || '未设置位置' }}
                    </el-text>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="18">
          <el-card shadow="never" class="designer-canvas">
            <template #header>
              <el-tabs v-model="activeTab">
                <el-tab-pane label="触发器" name="triggers"></el-tab-pane>
                <el-tab-pane label="动作流程" name="actions"></el-tab-pane>
              </el-tabs>
            </template>
            
            <div v-show="activeTab === 'triggers'" class="triggers-panel">
              <el-button type="primary" @click="addTrigger" size="small">
                <el-icon><Plus /></el-icon>
                添加触发器
              </el-button>
              
              <div class="trigger-list">
                <el-card
                  v-for="(trigger, index) in scene.triggers"
                  :key="index"
                  shadow="hover"
                  class="trigger-card"
                >
                  <template #header>
                    <div class="trigger-header">
                      <span>触发器 {{ index + 1 }}</span>
                      <el-button
                        type="danger"
                        size="small"
                        @click="removeTrigger(index)"
                      >
                        删除
                      </el-button>
                    </div>
                  </template>
                  
                  <el-form label-width="100px">
                    <el-form-item label="触发类型">
                      <el-select v-model="trigger.type">
                        <el-option label="手动触发" value="MANUAL" />
                        <el-option label="定时触发" value="SCHEDULE" />
                        <el-option label="设备状态" value="DEVICE_STATE" />
                        <el-option label="传感器数值" value="SENSOR_VALUE" />
                      </el-select>
                    </el-form-item>
                    
                    <el-form-item
                      v-if="trigger.type !== 'MANUAL'"
                      label="设备"
                    >
                      <el-select v-model="trigger.deviceId" placeholder="选择设备">
                        <el-option
                          v-for="device in devices"
                          :key="device.deviceId"
                          :label="device.name"
                          :value="device.deviceId"
                        />
                      </el-select>
                    </el-form-item>
                    
                    <el-form-item
                      v-if="trigger.type === 'SCHEDULE'"
                      label="时间"
                    >
                      <el-input
                        v-model="trigger.condition"
                        placeholder="如：08:00"
                      />
                    </el-form-item>
                    
                    <el-form-item
                      v-if="trigger.type === 'DEVICE_STATE' || trigger.type === 'SENSOR_VALUE'"
                      label="条件"
                    >
                      <el-input
                        v-model="trigger.condition"
                        placeholder="如：temperature > 25"
                      />
                    </el-form-item>
                  </el-form>
                </el-card>
              </div>
            </div>
            
            <div v-show="activeTab === 'actions'" class="actions-panel">
              <el-button type="primary" @click="addAction" size="small">
                <el-icon><Plus /></el-icon>
                添加动作
              </el-button>
              
              <div class="action-list">
                <el-card
                  v-for="(action, index) in scene.actions"
                  :key="index"
                  shadow="hover"
                  class="action-card"
                >
                  <template #header>
                    <div class="action-header">
                      <span>动作 {{ index + 1 }}</span>
                      <div>
                        <el-button
                          size="small"
                          :disabled="index === 0"
                          @click="moveAction(index, -1)"
                        >
                          上移
                        </el-button>
                        <el-button
                          size="small"
                          :disabled="index === scene.actions.length - 1"
                          @click="moveAction(index, 1)"
                        >
                          下移
                        </el-button>
                        <el-button
                          type="danger"
                          size="small"
                          @click="removeAction(index)"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>
                  </template>
                  
                  <el-form label-width="100px">
                    <el-form-item label="设备">
                      <el-select v-model="action.deviceId" placeholder="选择设备">
                        <el-option
                          v-for="device in devices"
                          :key="device.deviceId"
                          :label="device.name"
                          :value="device.deviceId"
                        />
                      </el-select>
                    </el-form-item>
                    
                    <el-form-item label="操作">
                      <el-select v-model="action.action">
                        <el-option label="打开" value="turn_on" />
                        <el-option label="关闭" value="turn_off" />
                        <el-option label="设置状态" value="set_state" />
                      </el-select>
                    </el-form-item>
                    
                    <el-form-item
                      v-if="action.action === 'set_state'"
                      label="参数"
                    >
                      <el-input
                        v-model="action.parametersText"
                        type="textarea"
                        :rows="2"
                        placeholder='JSON格式，如：{"brightness": 80}'
                      />
                    </el-form-item>
                    
                    <el-form-item label="延迟(秒)">
                      <el-input-number
                        v-model="action.delaySeconds"
                        :min="0"
                        :max="300"
                      />
                    </el-form-item>
                  </el-form>
                </el-card>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { deviceService, sceneService } from '../services/api'

const router = useRouter()
const activeTab = ref('triggers')
const devices = ref([])

const scene = ref({
  name: '',
  description: '',
  status: 'INACTIVE',
  triggers: [],
  actions: []
})

const loadDevices = async () => {
  try {
    const response = await deviceService.getAll()
    devices.value = response.data
  } catch (error) {
    console.error('Failed to load devices:', error)
    ElMessage.error('加载设备列表失败')
  }
}

const getStatusType = (status) => {
  const types = {
    ONLINE: 'success',
    OFFLINE: 'info',
    ERROR: 'danger',
    DISABLED: 'warning'
  }
  return types[status] || 'info'
}

const addTrigger = () => {
  scene.value.triggers.push({
    type: 'MANUAL',
    deviceId: '',
    condition: '',
    parameters: {}
  })
}

const removeTrigger = (index) => {
  scene.value.triggers.splice(index, 1)
}

const addAction = () => {
  scene.value.actions.push({
    deviceId: '',
    action: 'turn_on',
    parameters: {},
    parametersText: '',
    delaySeconds: 0,
    order: scene.value.actions.length
  })
}

const removeAction = (index) => {
  scene.value.actions.splice(index, 1)
  scene.value.actions.forEach((action, idx) => {
    action.order = idx
  })
}

const moveAction = (index, direction) => {
  const newIndex = index + direction
  if (newIndex >= 0 && newIndex < scene.value.actions.length) {
    const temp = scene.value.actions[index]
    scene.value.actions[index] = scene.value.actions[newIndex]
    scene.value.actions[newIndex] = temp
    
    scene.value.actions.forEach((action, idx) => {
      action.order = idx
    })
  }
}

const onDeviceDragStart = (device) => {
  console.log('Drag started:', device.name)
}

const saveScene = async () => {
  if (!scene.value.name) {
    ElMessage.warning('请输入场景名称')
    return
  }
  
  try {
    const sceneData = {
      ...scene.value,
      actions: scene.value.actions.map(action => {
        const actionData = { ...action }
        
        if (action.parametersText) {
          try {
            const params = JSON.parse(action.parametersText)
            actionData.parameters = Object.fromEntries(
              Object.entries(params).map(([k, v]) => [k, String(v)])
            )
          } catch (e) {
            console.error('Invalid JSON in action parameters:', e)
          }
        }
        
        delete actionData.parametersText
        return actionData
      })
    }
    
    await sceneService.create(sceneData)
    ElMessage.success('场景保存成功')
    router.push('/scenes')
  } catch (error) {
    console.error('Failed to save scene:', error)
    ElMessage.error('保存场景失败')
  }
}

onMounted(() => {
  loadDevices()
})
</script>

<style scoped>
.scene-designer {
  max-width: 1600px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}

.config-panel {
  min-height: 600px;
}

.device-list {
  max-height: 400px;
  overflow-y: auto;
}

.device-item {
  display: flex;
  flex-direction: column;
  padding: 10px;
  margin: 5px 0;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: move;
  transition: background 0.2s;
}

.device-item:hover {
  background: #e0e0e0;
}

.device-info {
  width: 100%;
}

.device-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-weight: 500;
}

.device-location {
  margin-left: 24px;
}

.designer-canvas {
  min-height: 600px;
}

.triggers-panel,
.actions-panel {
  padding: 20px 0;
}

.trigger-list,
.action-list {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.trigger-card,
.action-card {
  margin-bottom: 10px;
}

.trigger-header,
.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
