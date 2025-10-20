<template>
  <div class="device-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>设备管理</h2>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加设备
          </el-button>
        </div>
      </template>
      
      <el-table :data="devices" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="设备名称" width="180" />
        <el-table-column prop="deviceId" label="设备ID" width="200" />
        <el-table-column prop="type" label="类型" width="150" />
        <el-table-column prop="location" label="位置" width="150" />
        <el-table-column prop="room" label="房间" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="最后更新" width="180" />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="viewDeviceState(row)">
              查看状态
            </el-button>
            <el-button size="small" type="primary" @click="editDevice(row)">
              编辑
            </el-button>
            <el-button size="small" type="success" @click="controlDevice(row)">
              控制
            </el-button>
            <el-button size="small" type="danger" @click="deleteDevice(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog v-model="showAddDialog" title="添加设备" width="600px">
      <el-form :model="newDevice" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="newDevice.name" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="newDevice.type" placeholder="选择设备类型">
            <el-option label="灯光" value="LIGHT" />
            <el-option label="空调" value="HVAC" />
            <el-option label="温度传感器" value="TEMPERATURE_SENSOR" />
            <el-option label="湿度传感器" value="HUMIDITY_SENSOR" />
            <el-option label="人体感应" value="MOTION_SENSOR" />
            <el-option label="门锁" value="DOOR_LOCK" />
            <el-option label="窗户" value="WINDOW" />
            <el-option label="窗帘" value="CURTAIN" />
            <el-option label="投影仪" value="PROJECTOR" />
            <el-option label="投影幕" value="SCREEN" />
            <el-option label="风扇" value="FAN" />
          </el-select>
        </el-form-item>
        <el-form-item label="教学楼">
          <el-input v-model="newDevice.building" placeholder="如：理科楼" />
        </el-form-item>
        <el-form-item label="楼层">
          <el-input v-model="newDevice.floor" placeholder="如：3F" />
        </el-form-item>
        <el-form-item label="房间">
          <el-input v-model="newDevice.room" placeholder="如：301教室" />
        </el-form-item>
        <el-form-item label="位置描述">
          <el-input v-model="newDevice.location" placeholder="如：前排左侧" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="newDevice.status">
            <el-option label="在线" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addDevice">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="showStateDialog" title="设备状态" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item 
          v-for="(value, key) in deviceState" 
          :key="key"
          :label="key"
        >
          {{ value }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <el-dialog v-model="showControlDialog" title="控制设备" width="600px">
      <el-form label-width="100px">
        <el-form-item label="设备">
          <el-text>{{ currentDevice?.name }}</el-text>
        </el-form-item>
        <el-form-item label="操作">
          <el-radio-group v-model="controlAction">
            <el-radio label="turn_on">开启</el-radio>
            <el-radio label="turn_off">关闭</el-radio>
            <el-radio label="set_state">设置状态</el-radio>
            <el-radio label="change_status">修改设备状态</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="controlAction === 'change_status'">
          <el-form-item label="设置状态">
            <el-select v-model="deviceStatusChange" placeholder="选择设备状态">
              <el-option label="在线" value="ONLINE" />
              <el-option label="离线" value="OFFLINE" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
          </el-form-item>
        </template>
        <template v-if="controlAction === 'set_state'">
          <el-form-item v-if="currentDevice?.type === 'LIGHT'" label="亮度">
            <el-slider v-model="lightBrightness" :min="0" :max="100" />
          </el-form-item>
          <el-form-item v-if="currentDevice?.type === 'LIGHT'" label="颜色">
            <el-color-picker v-model="lightColor" />
          </el-form-item>
          <el-form-item v-if="currentDevice?.type !== 'LIGHT'" label="参数">
            <el-input
              v-model="controlParams"
              type="textarea"
              :rows="3"
              placeholder='输入JSON格式，如：{"temperature": 22}'
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showControlDialog = false">取消</el-button>
        <el-button type="primary" @click="executeControl">执行</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="showEditDialog" title="编辑设备" width="600px">
      <el-form :model="editingDevice" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="editingDevice.name" />
        </el-form-item>
        <el-form-item label="教学楼">
          <el-input v-model="editingDevice.building" placeholder="如：理科楼" />
        </el-form-item>
        <el-form-item label="楼层">
          <el-input v-model="editingDevice.floor" placeholder="如：3F" />
        </el-form-item>
        <el-form-item label="房间">
          <el-input v-model="editingDevice.room" placeholder="如：301教室" />
        </el-form-item>
        <el-form-item label="位置描述">
          <el-input v-model="editingDevice.location" placeholder="如：前排左侧" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editingDevice.status">
            <el-option label="在线" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deviceService } from '../services/api'

const loading = ref(false)
const devices = ref([])
const showAddDialog = ref(false)
const showStateDialog = ref(false)
const showControlDialog = ref(false)
const showEditDialog = ref(false)
const deviceState = ref({})
const currentDevice = ref(null)
const controlAction = ref('turn_on')
const controlParams = ref('')
const lightBrightness = ref(100)
const lightColor = ref('#FFFFFF')
const deviceStatusChange = ref('ONLINE')

const editingDevice = ref({
  id: null,
  name: '',
  building: '',
  floor: '',
  room: '',
  location: '',
  status: 'OFFLINE'
})

const newDevice = ref({
  name: '',
  type: '',
  building: '',
  floor: '',
  room: '',
  location: '',
  status: 'ONLINE'
})

const getStatusType = (status) => {
  const types = {
    ONLINE: 'success',
    OFFLINE: 'info',
    ERROR: 'danger',
    DISABLED: 'warning'
  }
  return types[status] || 'info'
}

const loadDevices = async () => {
  loading.value = true
  try {
    const response = await deviceService.getAll()
    devices.value = response.data
  } catch (error) {
    console.error('Failed to load devices:', error)
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

const addDevice = async () => {
  try {
    await deviceService.create(newDevice.value)
    ElMessage.success('设备添加成功')
    showAddDialog.value = false
    newDevice.value = {
      name: '',
      type: '',
      building: '',
      floor: '',
      room: '',
      location: '',
      status: 'ONLINE'
    }
    loadDevices()
  } catch (error) {
    console.error('Failed to add device:', error)
    ElMessage.error('添加设备失败')
  }
}

const deleteDevice = async (device) => {
  try {
    await ElMessageBox.confirm('确定要删除这个设备吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deviceService.delete(device.id)
    ElMessage.success('设备删除成功')
    loadDevices()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete device:', error)
      ElMessage.error('删除设备失败')
    }
  }
}

const viewDeviceState = async (device) => {
  try {
    const response = await deviceService.getState(device.deviceId)
    deviceState.value = response.data
    showStateDialog.value = true
  } catch (error) {
    console.error('Failed to get device state:', error)
    ElMessage.error('获取设备状态失败')
  }
}

const controlDevice = (device) => {
  currentDevice.value = device
  controlAction.value = 'turn_on'
  controlParams.value = ''
  lightBrightness.value = 100
  lightColor.value = '#FFFFFF'
  deviceStatusChange.value = device.status || 'ONLINE'
  showControlDialog.value = true
}

const editDevice = (device) => {
  editingDevice.value = {
    id: device.id,
    name: device.name,
    building: device.building || '',
    floor: device.floor || '',
    room: device.room || '',
    location: device.location || '',
    status: device.status,
    type: device.type,
    deviceId: device.deviceId,
    properties: device.properties
  }
  showEditDialog.value = true
}

const saveEdit = async () => {
  try {
    await deviceService.update(editingDevice.value.id, editingDevice.value)
    ElMessage.success('设备更新成功')
    showEditDialog.value = false
    loadDevices()
  } catch (error) {
    console.error('Failed to update device:', error)
    ElMessage.error('更新设备失败')
  }
}

const executeControl = async () => {
  try {
    // Handle device status change
    if (controlAction.value === 'change_status') {
      const updateData = {
        ...currentDevice.value,
        status: deviceStatusChange.value
      }
      await deviceService.update(currentDevice.value.id, updateData)
      ElMessage.success('设备状态更新成功')
      showControlDialog.value = false
      loadDevices()
      return
    }

    // Helper to safely parse parameters entered by user
    const safeParseParams = (text) => {
      if (!text || !text.trim()) return {}
      try {
        return JSON.parse(text)
      } catch (e) {
        // Try to be lenient: allow single quotes and missing curly braces
        try {
          const normalized = (() => {
            let t = text.trim()
            // Remove surrounding parentheses if user typed ( ... )
            if (t.startsWith('(') && t.endsWith(')')) {
              t = t.slice(1, -1)
            }
            // Add curly braces if user wrote key:value without {}
            if (!t.startsWith('{') && !t.endsWith('}')) {
              t = `{${t}}`
            }
            // Replace single quotes with double quotes for JSON compatibility
            t = t
              .replace(/[‘’]/g, "'")
              .replace(/[“”]/g, '"')
              .replace(/'/g, '"')
            return t
          })()
          return JSON.parse(normalized)
        } catch (e2) {
          return null
        }
      }
    }

    let parameters = {}
    if (controlAction.value === 'set_state') {
      if (currentDevice.value?.type === 'LIGHT') {
        parameters = {
          brightness: lightBrightness.value,
          color: lightColor.value
        }
      } else if (controlParams.value) {
        const parsed = safeParseParams(controlParams.value)
        if (parsed === null) {
          ElMessage.error('参数格式错误：请输入有效的 JSON，如 {"temperature": 22}')
          return
        }
        parameters = parsed
      }
    }

    const resp = await deviceService.control(
      currentDevice.value.deviceId,
      controlAction.value,
      parameters
    )

    const ok = resp?.data?.success !== false
    if (!ok) {
      ElMessage.error('设备拒绝控制：设备离线/禁用或指令无效')
      return
    }

    ElMessage.success('控制命令执行成功')
    showControlDialog.value = false
    
    // Refresh device state if state dialog is open
    if (showStateDialog.value) {
      viewDeviceState(currentDevice.value)
    }
  } catch (error) {
    console.error('Failed to control device:', error)
    ElMessage.error('控制设备失败')
  }
}

onMounted(() => {
  loadDevices()
})
</script>

<style scoped>
.device-management {
  max-width: 1400px;
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
</style>
