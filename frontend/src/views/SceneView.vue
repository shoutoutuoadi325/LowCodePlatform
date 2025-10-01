<template>
  <div class="scene-view">
    <div class="card">
      <div class="scene-header">
        <h2 class="card-title">场景编排</h2>
        <button @click="showCreateModal = true" class="btn btn-primary">创建新场景</button>
      </div>

      <div v-if="loading">加载中...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else class="scenes-grid">
        <SceneCard
          v-for="scene in scenes"
          :key="scene.id"
          :scene="scene"
          @execute="executeScene"
          @edit="editScene"
          @delete="deleteScene"
        />
      </div>
    </div>

    <!-- Create/Edit Scene Modal -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ showEditModal ? '编辑场景' : '创建场景' }}</h3>
          <button @click="closeModal" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">场景名称</label>
            <input type="text" v-model="currentScene.name" placeholder="例如：上课模式" />
          </div>
          <div class="form-group">
            <label class="form-label">场景描述</label>
            <textarea v-model="currentScene.description" rows="2" placeholder="描述场景的用途"></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">场景动作</label>
            <div class="actions-list">
              <div v-for="(action, index) in currentScene.actions" :key="index" class="action-item">
                <div class="action-row">
                  <select v-model="action.deviceId">
                    <option value="">选择设备</option>
                    <option v-for="device in devices" :key="device.id" :value="device.id">
                      {{ device.name }}
                    </option>
                  </select>
                  <select v-model="action.action">
                    <option value="">选择动作</option>
                    <option v-for="act in getDeviceActions(action.deviceId)" :key="act.value" :value="act.value">
                      {{ act.label }}
                    </option>
                  </select>
                  <input
                    type="number"
                    v-model.number="action.delaySeconds"
                    placeholder="延迟(秒)"
                    min="0"
                    style="width: 100px"
                  />
                  <button @click="removeAction(index)" class="btn btn-danger btn-sm">删除</button>
                </div>
                <div v-if="needsParameters(action.action)" class="action-params">
                  <input
                    v-for="param in getActionParameters(action.action)"
                    :key="param.key"
                    :type="param.type"
                    v-model="action.parameters[param.key]"
                    :placeholder="param.label"
                  />
                </div>
              </div>
            </div>
            <button @click="addAction" class="btn btn-secondary btn-sm">添加动作</button>
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" v-model="currentScene.enabled" />
              启用场景
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeModal" class="btn btn-secondary">取消</button>
          <button @click="saveScene" class="btn btn-primary">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useSceneStore } from '../stores/sceneStore'
import { useDeviceStore } from '../stores/deviceStore'
import SceneCard from '../components/SceneCard.vue'

const sceneStore = useSceneStore()
const deviceStore = useDeviceStore()

const showCreateModal = ref(false)
const showEditModal = ref(false)
const currentScene = ref({
  name: '',
  description: '',
  actions: [],
  triggers: [],
  enabled: true
})

const loading = computed(() => sceneStore.loading)
const error = computed(() => sceneStore.error)
const scenes = computed(() => sceneStore.scenes)
const devices = computed(() => deviceStore.devices)

const getDeviceActions = (deviceId) => {
  const device = deviceStore.getDeviceById(deviceId)
  if (!device) return []

  const actions = {
    'LIGHT': [
      { label: '打开', value: 'turnOn' },
      { label: '关闭', value: 'turnOff' },
      { label: '设置亮度', value: 'setBrightness' },
      { label: '设置颜色', value: 'setColor' }
    ],
    'AIR_CONDITIONER': [
      { label: '打开', value: 'turnOn' },
      { label: '关闭', value: 'turnOff' },
      { label: '设置温度', value: 'setTemperature' },
      { label: '设置模式', value: 'setMode' },
      { label: '设置风速', value: 'setFanSpeed' }
    ],
    'PROJECTOR': [
      { label: '打开', value: 'turnOn' },
      { label: '关闭', value: 'turnOff' },
      { label: '设置输入源', value: 'setInput' }
    ],
    'CURTAIN': [
      { label: '打开', value: 'open' },
      { label: '关闭', value: 'close' },
      { label: '设置位置', value: 'setPosition' }
    ]
  }
  return actions[device.type] || []
}

const needsParameters = (action) => {
  return ['setBrightness', 'setColor', 'setTemperature', 'setMode', 'setFanSpeed', 'setInput', 'setPosition'].includes(action)
}

const getActionParameters = (action) => {
  const params = {
    'setBrightness': [{ key: 'brightness', label: '亮度 (0-100)', type: 'number' }],
    'setColor': [{ key: 'color', label: '颜色 (#RRGGBB)', type: 'text' }],
    'setTemperature': [{ key: 'temperature', label: '温度 (°C)', type: 'number' }],
    'setMode': [{ key: 'mode', label: '模式 (cool/heat/fan)', type: 'text' }],
    'setFanSpeed': [{ key: 'fanSpeed', label: '风速 (low/medium/high/auto)', type: 'text' }],
    'setInput': [{ key: 'input', label: '输入源 (HDMI1/HDMI2/VGA)', type: 'text' }],
    'setPosition': [{ key: 'position', label: '位置 (0-100)', type: 'number' }]
  }
  return params[action] || []
}

const addAction = () => {
  currentScene.value.actions.push({
    deviceId: '',
    action: '',
    parameters: {},
    delaySeconds: 0
  })
}

const removeAction = (index) => {
  currentScene.value.actions.splice(index, 1)
}

const closeModal = () => {
  showCreateModal.value = false
  showEditModal.value = false
  currentScene.value = {
    name: '',
    description: '',
    actions: [],
    triggers: [],
    enabled: true
  }
}

const saveScene = async () => {
  try {
    if (showEditModal.value) {
      await sceneStore.updateScene(currentScene.value.id, currentScene.value)
    } else {
      await sceneStore.createScene(currentScene.value)
    }
    closeModal()
  } catch (error) {
    alert('保存失败: ' + error.message)
  }
}

const editScene = (scene) => {
  currentScene.value = JSON.parse(JSON.stringify(scene))
  showEditModal.value = true
}

const deleteScene = async (sceneId) => {
  if (confirm('确定要删除这个场景吗？')) {
    try {
      await sceneStore.deleteScene(sceneId)
    } catch (error) {
      alert('删除失败: ' + error.message)
    }
  }
}

const executeScene = async (sceneId) => {
  try {
    await sceneStore.executeScene(sceneId)
    alert('场景执行成功！')
    await deviceStore.fetchDevices()
  } catch (error) {
    alert('场景执行失败: ' + error.message)
  }
}

onMounted(() => {
  sceneStore.fetchScenes()
  deviceStore.fetchDevices()
})
</script>

<style scoped>
.scene-view {
  max-width: 1400px;
}

.scene-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.scenes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.error {
  color: #dc2626;
  padding: 1rem;
  background: #fee2e2;
  border-radius: 4px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  line-height: 1;
  cursor: pointer;
  color: #6b7280;
}

.btn-close:hover {
  color: #1f2937;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.actions-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1rem;
}

.action-item {
  padding: 1rem;
  background: #f9fafb;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.action-row {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.action-row select,
.action-row input {
  flex: 1;
  min-width: 120px;
}

.action-params {
  margin-top: 0.5rem;
  display: flex;
  gap: 0.5rem;
}

.action-params input {
  flex: 1;
}
</style>
