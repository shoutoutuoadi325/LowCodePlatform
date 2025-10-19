import axios from 'axios'

const DEVICE_SERVICE_URL = process.env.VUE_APP_DEVICE_SERVICE_URL || 'http://localhost:8081'
const SCENE_SERVICE_URL = process.env.VUE_APP_SCENE_SERVICE_URL || 'http://localhost:8082'
const SYSTEM_SERVICE_URL = process.env.VUE_APP_SYSTEM_SERVICE_URL || 'http://localhost:8081'
const SIMULATOR_SERVICE_URL = process.env.VUE_APP_SIMULATOR_SERVICE_URL || 'http://localhost:8081'

// 模拟数据 - 设备列表
const mockDevices = [
  {
    id: 1,
    deviceId: 'smart_light_001',
    name: '客厅智能灯泡',
    type: 'smart_light',
    typeId: 1,
    location: '客厅',
    building: 'A栋',
    floor: '1楼',
    room: '客厅',
    status: 'online',
    state: { brightness: 80, color: '#FFD700', power: true },
    lastSeen: new Date().toISOString(),
    properties: [
      { name: 'brightness', value: 80, unit: '%' },
      { name: 'color', value: '#FFD700', unit: '' },
      { name: 'power', value: true, unit: '' }
    ]
  },
  {
    id: 2,
    deviceId: 'temp_sensor_001',
    name: '卧室温度传感器',
    type: 'temperature_sensor',
    typeId: 2,
    location: '主卧',
    building: 'A栋',
    floor: '2楼',
    room: '主卧',
    status: 'online',
    state: { temperature: 23.5, humidity: 65 },
    lastSeen: new Date().toISOString(),
    properties: [
      { name: 'temperature', value: 23.5, unit: '°C' },
      { name: 'humidity', value: 65, unit: '%' }
    ]
  },
  {
    id: 3,
    deviceId: 'smart_lock_001',
    name: '前门智能门锁',
    type: 'smart_lock',
    typeId: 3,
    location: '前门',
    building: 'A栋',
    floor: '1楼',
    room: '玄关',
    status: 'online',
    state: { locked: true, battery: 85 },
    lastSeen: new Date().toISOString(),
    properties: [
      { name: 'locked', value: true, unit: '' },
      { name: 'battery', value: 85, unit: '%' }
    ]
  },
  {
    id: 4,
    deviceId: 'smart_light_002',
    name: '厨房智能灯带',
    type: 'smart_light',
    typeId: 1,
    location: '厨房',
    building: 'A栋',
    floor: '1楼',
    room: '厨房',
    status: 'online',
    state: { brightness: 60, color: '#FFFFFF', power: false },
    lastSeen: new Date().toISOString(),
    properties: [
      { name: 'brightness', value: 60, unit: '%' },
      { name: 'color', value: '#FFFFFF', unit: '' },
      { name: 'power', value: false, unit: '' }
    ]
  },
  {
    id: 5,
    deviceId: 'air_conditioner_001',
    name: '客厅空调',
    type: 'air_conditioner',
    typeId: 4,
    location: '客厅',
    building: 'A栋',
    floor: '1楼',
    room: '客厅',
    status: 'online',
    state: { temperature: 26, mode: 'cool', power: true, fanSpeed: 2 },
    lastSeen: new Date().toISOString(),
    properties: [
      { name: 'temperature', value: 26, unit: '°C' },
      { name: 'mode', value: 'cool', unit: '' },
      { name: 'power', value: true, unit: '' },
      { name: 'fanSpeed', value: 2, unit: '' }
    ]
  }
]

// 模拟数据 - 设备类型
const mockDeviceTypes = [
  {
    id: 1,
    identifier: 'smart_light',
    name: '智能灯泡',
    category: '照明设备',
    description: '支持调光调色的智能LED灯泡',
    manufacturer: 'SmartHome Inc.',
    model: 'SH-LED-001',
    version: '1.0.0',
    properties: [
      { name: 'brightness', displayName: '亮度', dataType: 'integer', unit: '%', min: 0, max: 100 },
      { name: 'color', displayName: '颜色', dataType: 'string', unit: '' },
      { name: 'power', displayName: '电源', dataType: 'boolean', unit: '' }
    ],
    operations: [
      { name: 'turnOn', displayName: '开灯', parameters: [] },
      { name: 'turnOff', displayName: '关灯', parameters: [] },
      { name: 'setBrightness', displayName: '设置亮度', parameters: [{ name: 'value', type: 'integer' }] },
      { name: 'setColor', displayName: '设置颜色', parameters: [{ name: 'color', type: 'string' }] }
    ],
    events: [
      { name: 'powerChanged', displayName: '电源状态变化' },
      { name: 'brightnessChanged', displayName: '亮度变化' }
    ]
  },
  {
    id: 2,
    identifier: 'temperature_sensor',
    name: '温度传感器',
    category: '传感器',
    description: '高精度温湿度传感器',
    manufacturer: 'SensorTech Ltd.',
    model: 'ST-TH-002',
    version: '2.1.0',
    properties: [
      { name: 'temperature', displayName: '温度', dataType: 'float', unit: '°C', min: -40, max: 80 },
      { name: 'humidity', displayName: '湿度', dataType: 'float', unit: '%', min: 0, max: 100 }
    ],
    operations: [
      { name: 'calibrate', displayName: '校准传感器', parameters: [] }
    ],
    events: [
      { name: 'temperatureAlert', displayName: '温度报警' },
      { name: 'humidityAlert', displayName: '湿度报警' }
    ]
  },
  {
    id: 3,
    identifier: 'smart_lock',
    name: '智能门锁',
    category: '安防设备',
    description: '指纹识别智能门锁',
    manufacturer: 'SecureTech Co.',
    model: 'SC-LOCK-003',
    version: '1.5.0',
    properties: [
      { name: 'locked', displayName: '锁定状态', dataType: 'boolean', unit: '' },
      { name: 'battery', displayName: '电池电量', dataType: 'integer', unit: '%', min: 0, max: 100 }
    ],
    operations: [
      { name: 'lock', displayName: '上锁', parameters: [] },
      { name: 'unlock', displayName: '解锁', parameters: [] },
      { name: 'addFingerprint', displayName: '添加指纹', parameters: [{ name: 'userId', type: 'string' }] }
    ],
    events: [
      { name: 'lockStateChanged', displayName: '锁定状态变化' },
      { name: 'lowBattery', displayName: '低电量报警' },
      { name: 'unauthorizedAccess', displayName: '非法访问' }
    ]
  },
  {
    id: 4,
    identifier: 'air_conditioner',
    name: '智能空调',
    category: '环境控制',
    description: '变频智能空调',
    manufacturer: 'CoolAir Corp.',
    model: 'CA-AC-004',
    version: '3.0.0',
    properties: [
      { name: 'temperature', displayName: '设定温度', dataType: 'integer', unit: '°C', min: 16, max: 30 },
      { name: 'mode', displayName: '运行模式', dataType: 'string', unit: '' },
      { name: 'power', displayName: '电源', dataType: 'boolean', unit: '' },
      { name: 'fanSpeed', displayName: '风速', dataType: 'integer', unit: '', min: 1, max: 5 }
    ],
    operations: [
      { name: 'turnOn', displayName: '开机', parameters: [] },
      { name: 'turnOff', displayName: '关机', parameters: [] },
      { name: 'setTemperature', displayName: '设置温度', parameters: [{ name: 'temp', type: 'integer' }] },
      { name: 'setMode', displayName: '设置模式', parameters: [{ name: 'mode', type: 'string' }] }
    ],
    events: [
      { name: 'temperatureReached', displayName: '达到设定温度' },
      { name: 'filterNeedsReplacement', displayName: '滤网需要更换' }
    ]
  }
]

// 模拟数据 - 场景列表
const mockScenes = [
  {
    id: 1,
    sceneId: 'scene_home_001',
    name: '回家模式',
    description: '到家时自动执行的场景',
    isActive: true,
    triggers: [
      { type: 'time', condition: '18:00', description: '每天18:00触发' },
      { type: 'device', deviceId: 'smart_lock_001', condition: 'unlock', description: '前门解锁时触发' }
    ],
    actions: [
      { deviceId: 'smart_light_001', action: 'turnOn', parameters: { brightness: 80 }, description: '打开客厅灯' },
      { deviceId: 'air_conditioner_001', action: 'turnOn', parameters: { temperature: 24 }, description: '打开空调' }
    ],
    lastExecuted: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    executionCount: 15
  },
  {
    id: 2,
    sceneId: 'scene_away_001',
    name: '离家模式',
    description: '离开家时自动执行的场景',
    isActive: true,
    triggers: [
      { type: 'device', deviceId: 'smart_lock_001', condition: 'lock', description: '前门上锁时触发' }
    ],
    actions: [
      { deviceId: 'smart_light_001', action: 'turnOff', parameters: {}, description: '关闭所有灯光' },
      { deviceId: 'smart_light_002', action: 'turnOff', parameters: {}, description: '关闭厨房灯' },
      { deviceId: 'air_conditioner_001', action: 'turnOff', parameters: {}, description: '关闭空调' }
    ],
    lastExecuted: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
    executionCount: 8
  },
  {
    id: 3,
    sceneId: 'scene_sleep_001',
    name: '睡眠模式',
    description: '睡觉时的环境设置',
    isActive: true,
    triggers: [
      { type: 'time', condition: '22:30', description: '每天22:30触发' }
    ],
    actions: [
      { deviceId: 'smart_light_001', action: 'setBrightness', parameters: { brightness: 10 }, description: '调暗客厅灯' },
      { deviceId: 'smart_light_002', action: 'turnOff', parameters: {}, description: '关闭厨房灯' },
      { deviceId: 'air_conditioner_001', action: 'setTemperature', parameters: { temperature: 26 }, description: '调整空调温度' }
    ],
    lastExecuted: new Date(Date.now() - 10 * 60 * 60 * 1000).toISOString(),
    executionCount: 22
  },
  {
    id: 4,
    sceneId: 'scene_party_001',
    name: '聚会模式',
    description: '聚会时的灯光氛围',
    isActive: false,
    triggers: [],
    actions: [
      { deviceId: 'smart_light_001', action: 'setColor', parameters: { color: '#FF6B6B', brightness: 100 }, description: '设置彩色灯光' },
      { deviceId: 'smart_light_002', action: 'setColor', parameters: { color: '#4ECDC4', brightness: 100 }, description: '设置厨房彩灯' }
    ],
    lastExecuted: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
    executionCount: 3
  }
]

// 模拟数据 - 模拟器列表
const mockSimulators = [
  {
    id: 1,
    name: '智能灯泡模拟器',
    type: 'smart_light',
    status: 'running',
    deviceCount: 3,
    lastUpdate: new Date().toISOString(),
    config: {
      updateInterval: 5000,
      randomEvents: true,
      autoResponse: true
    }
  },
  {
    id: 2,
    name: '温度传感器模拟器',
    type: 'temperature_sensor',
    status: 'running',
    deviceCount: 2,
    lastUpdate: new Date().toISOString(),
    config: {
      updateInterval: 10000,
      temperatureRange: { min: 20, max: 30 },
      humidityRange: { min: 40, max: 80 }
    }
  },
  {
    id: 3,
    name: '门锁模拟器',
    type: 'smart_lock',
    status: 'stopped',
    deviceCount: 1,
    lastUpdate: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
    config: {
      updateInterval: 15000,
      randomEvents: false
    }
  }
]

// 模拟数据 - 监控数据
const generateHistoryData = (hours = 24) => {
  const data = []
  const now = new Date()
  for (let i = hours; i >= 0; i--) {
    const time = new Date(now.getTime() - i * 60 * 60 * 1000)
    data.push({
      timestamp: time.toISOString(),
      temperature: 20 + Math.random() * 10,
      humidity: 40 + Math.random() * 40,
      brightness: Math.floor(Math.random() * 100),
      power: Math.random() > 0.3
    })
  }
  return data
}

const mockAlerts = [
  {
    id: 1,
    deviceId: 'temp_sensor_001',
    deviceName: '卧室温度传感器',
    type: 'warning',
    message: '温度过高：当前温度 28.5°C',
    timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    acknowledged: false
  },
  {
    id: 2,
    deviceId: 'smart_lock_001',
    deviceName: '前门智能门锁',
    type: 'info',
    message: '电池电量低：当前电量 15%',
    timestamp: new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString(),
    acknowledged: true
  },
  {
    id: 3,
    deviceId: 'smart_light_001',
    deviceName: '客厅智能灯泡',
    type: 'error',
    message: '设备离线超过5分钟',
    timestamp: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString(),
    acknowledged: false
  }
]

// 错误处理函数
const handleApiError = (error, fallbackData = null) => {
  console.warn('API调用失败，使用模拟数据:', error.message)
  if (fallbackData !== null) {
    return Promise.resolve({ data: fallbackData })
  }
  return Promise.reject(error)
}

const deviceApi = axios.create({
  baseURL: DEVICE_SERVICE_URL,
  timeout: 10000
})

const sceneApi = axios.create({
  baseURL: SCENE_SERVICE_URL,
  timeout: 10000
})

const systemApi = axios.create({
  baseURL: SYSTEM_SERVICE_URL,
  timeout: 10000
})

const simulatorApi = axios.create({
  baseURL: SIMULATOR_SERVICE_URL,
  timeout: 10000
})

export const deviceService = {
  getAll: () => Promise.resolve({ data: mockDevices }),
  getAllDevices: () => Promise.resolve({ data: mockDevices }),
  getById: (id) => {
    const device = mockDevices.find(d => d.id == id)
    return Promise.resolve({ data: device || null })
  },
  getDeviceById: (id) => {
    const device = mockDevices.find(d => d.id == id)
    return Promise.resolve({ data: device || null })
  },
  getByDeviceId: (deviceId) => {
    const device = mockDevices.find(d => d.deviceId === deviceId)
    return Promise.resolve({ data: device || null })
  },
  create: (device) => {
    const newDevice = { ...device, id: mockDevices.length + 1, status: 'online' }
    mockDevices.push(newDevice)
    return Promise.resolve({ data: newDevice })
  },
  createDevice: (device) => {
    const newDevice = { ...device, id: mockDevices.length + 1, status: 'online' }
    mockDevices.push(newDevice)
    return Promise.resolve({ data: newDevice })
  },
  update: (id, device) => {
    const index = mockDevices.findIndex(d => d.id == id)
    if (index !== -1) {
      mockDevices[index] = { ...mockDevices[index], ...device }
      return Promise.resolve({ data: mockDevices[index] })
    }
    return Promise.reject(new Error('设备未找到'))
  },
  updateDevice: (id, device) => {
    const index = mockDevices.findIndex(d => d.id == id)
    if (index !== -1) {
      mockDevices[index] = { ...mockDevices[index], ...device }
      return Promise.resolve({ data: mockDevices[index] })
    }
    return Promise.reject(new Error('设备未找到'))
  },
  delete: (id) => {
    const index = mockDevices.findIndex(d => d.id == id)
    if (index !== -1) {
      mockDevices.splice(index, 1)
      return Promise.resolve({ data: { success: true } })
    }
    return Promise.reject(new Error('设备未找到'))
  },
  deleteDevice: (id) => {
    const index = mockDevices.findIndex(d => d.id == id)
    if (index !== -1) {
      mockDevices.splice(index, 1)
      return Promise.resolve({ data: { success: true } })
    }
    return Promise.reject(new Error('设备未找到'))
  },
  control: (deviceId, action, parameters = {}) => {
    const device = mockDevices.find(d => d.deviceId === deviceId)
    if (device) {
      // 模拟控制操作
      if (action === 'turnOn') device.state.power = true
      if (action === 'turnOff') device.state.power = false
      if (action === 'setBrightness') device.state.brightness = parameters.brightness || parameters.value
      if (action === 'setColor') device.state.color = parameters.color
      return Promise.resolve({ data: { success: true, state: device.state } })
    }
    return Promise.reject(new Error('设备未找到'))
  },
  controlDevice: (id, command) => {
    const device = mockDevices.find(d => d.id == id)
    if (device) {
      return Promise.resolve({ data: { success: true, message: '控制命令已发送' } })
    }
    return Promise.reject(new Error('设备未找到'))
  },
  getState: (deviceId) => {
    const device = mockDevices.find(d => d.deviceId === deviceId)
    return Promise.resolve({ data: device ? device.state : {} })
  },
  getDeviceStatus: (id) => {
    const device = mockDevices.find(d => d.id == id)
    return Promise.resolve({ data: { status: device ? device.status : 'unknown' } })
  },
  getDeviceTypes: () => Promise.resolve({ data: mockDeviceTypes }),
  getByLocation: (building, floor) => {
    const devices = mockDevices.filter(d => d.building === building && d.floor === floor)
    return Promise.resolve({ data: devices })
  },
  getByRoom: (room) => {
    const devices = mockDevices.filter(d => d.room === room)
    return Promise.resolve({ data: devices })
  }
}

export const sceneService = {
  getAll: () => Promise.resolve({ data: mockScenes }),
  getActive: () => {
    const activeScenes = mockScenes.filter(s => s.isActive)
    return Promise.resolve({ data: activeScenes })
  },
  getById: (id) => {
    const scene = mockScenes.find(s => s.id == id)
    return Promise.resolve({ data: scene || null })
  },
  getBySceneId: (sceneId) => {
    const scene = mockScenes.find(s => s.sceneId === sceneId)
    return Promise.resolve({ data: scene || null })
  },
  create: (scene) => {
    const newScene = { ...scene, id: mockScenes.length + 1, executionCount: 0 }
    mockScenes.push(newScene)
    return Promise.resolve({ data: newScene })
  },
  update: (id, scene) => {
    const index = mockScenes.findIndex(s => s.id == id)
    if (index !== -1) {
      mockScenes[index] = { ...mockScenes[index], ...scene }
      return Promise.resolve({ data: mockScenes[index] })
    }
    return Promise.reject(new Error('场景未找到'))
  },
  delete: (id) => {
    const index = mockScenes.findIndex(s => s.id == id)
    if (index !== -1) {
      mockScenes.splice(index, 1)
      return Promise.resolve({ data: { success: true } })
    }
    return Promise.reject(new Error('场景未找到'))
  },
  execute: (sceneId) => {
    const scene = mockScenes.find(s => s.sceneId === sceneId || s.id == sceneId)
    if (scene) {
      scene.lastExecuted = new Date().toISOString()
      scene.executionCount = (scene.executionCount || 0) + 1
      return Promise.resolve({ data: { success: true, message: `场景 "${scene.name}" 执行成功` } })
    }
    return Promise.reject(new Error('场景未找到'))
  }
}

// 设备类型管理服务
export const deviceTypeService = {
  getAll: () => Promise.resolve({ data: mockDeviceTypes }),
  getById: (id) => {
    const deviceType = mockDeviceTypes.find(dt => dt.id == id)
    return Promise.resolve({ data: deviceType || null })
  },
  getByIdentifier: (identifier) => {
    const deviceType = mockDeviceTypes.find(dt => dt.identifier === identifier)
    return Promise.resolve({ data: deviceType || null })
  },
  create: (deviceType) => {
    const newDeviceType = { ...deviceType, id: mockDeviceTypes.length + 1 }
    mockDeviceTypes.push(newDeviceType)
    return Promise.resolve({ data: newDeviceType })
  },
  update: (id, deviceType) => {
    const index = mockDeviceTypes.findIndex(dt => dt.id == id)
    if (index !== -1) {
      mockDeviceTypes[index] = { ...mockDeviceTypes[index], ...deviceType }
      return Promise.resolve({ data: mockDeviceTypes[index] })
    }
    return Promise.reject(new Error('设备类型未找到'))
  },
  delete: (id) => {
    const index = mockDeviceTypes.findIndex(dt => dt.id == id)
    if (index !== -1) {
      mockDeviceTypes.splice(index, 1)
      return Promise.resolve({ data: { success: true } })
    }
    return Promise.reject(new Error('设备类型未找到'))
  },
  getProperties: (id) => {
    const deviceType = mockDeviceTypes.find(dt => dt.id == id)
    return Promise.resolve({ data: deviceType ? deviceType.properties : [] })
  },
  getOperations: (id) => {
    const deviceType = mockDeviceTypes.find(dt => dt.id == id)
    return Promise.resolve({ data: deviceType ? deviceType.operations : [] })
  },
  getEvents: (id) => {
    const deviceType = mockDeviceTypes.find(dt => dt.id == id)
    return Promise.resolve({ data: deviceType ? deviceType.events : [] })
  }
}

// 设备监控服务
export const monitorService = {
  getDeviceState: (deviceId) => {
    const device = mockDevices.find(d => d.deviceId === deviceId || d.id == deviceId)
    return Promise.resolve({ data: device ? device.state : {} })
  },
  getDeviceHistory: (deviceId, startTime, endTime) => {
    const historyData = generateHistoryData(24)
    return Promise.resolve({ data: historyData })
  },
  getDeviceAlerts: (deviceId) => {
    const alerts = mockAlerts.filter(a => a.deviceId === deviceId)
    return Promise.resolve({ data: alerts })
  },
  getAllAlerts: () => Promise.resolve({ data: mockAlerts }),
  acknowledgeAlert: (alertId) => {
    const alert = mockAlerts.find(a => a.id == alertId)
    if (alert) {
      alert.acknowledged = true
      return Promise.resolve({ data: { success: true } })
    }
    return Promise.reject(new Error('报警未找到'))
  },
  getDeviceMetrics: (deviceId, metric, timeRange) => {
    const historyData = generateHistoryData(24)
    const metricData = historyData.map(d => ({
      timestamp: d.timestamp,
      value: d[metric] || Math.random() * 100
    }))
    return Promise.resolve({ data: metricData })
  }
}

// 系统配置服务
export const systemService = {
  getSystemInfo: () => Promise.resolve({ 
    data: {
      version: '1.0.0',
      buildTime: '2024-01-15',
      environment: 'production',
      uptime: '5 days 12 hours',
      totalDevices: mockDevices.length,
      onlineDevices: mockDevices.filter(d => d.status === 'online').length,
      totalScenes: mockScenes.length,
      activeScenes: mockScenes.filter(s => s.isActive).length
    }
  }),
  getSystemConfig: () => Promise.resolve({ 
    data: {
      systemName: '智能设备管理平台',
      version: '1.0.0',
      dataRetentionDays: 30,
      debugMode: false,
      autoBackup: true,
      backupInterval: 'daily',
      maxDevices: 1000,
      maxScenes: 100,
      alertRetentionDays: 7,
      logLevel: 'INFO'
    }
  }),
  updateSystemConfig: (config) => Promise.resolve({ data: { success: true, message: '系统配置已更新' } }),
  getSystemLogs: (params) => {
    const logs = [
      { timestamp: new Date().toISOString(), level: 'INFO', message: '系统启动完成', module: 'SYSTEM' },
      { timestamp: new Date(Date.now() - 60000).toISOString(), level: 'INFO', message: '设备连接成功: smart_light_001', module: 'DEVICE' },
      { timestamp: new Date(Date.now() - 120000).toISOString(), level: 'WARN', message: '设备响应超时: temp_sensor_001', module: 'DEVICE' },
      { timestamp: new Date(Date.now() - 180000).toISOString(), level: 'INFO', message: '场景执行成功: 回家模式', module: 'SCENE' }
    ]
    return Promise.resolve({ data: logs })
  },
  getSystemStatus: () => Promise.resolve({ 
    data: {
      status: 'running',
      uptime: '5 days 12 hours',
      memory: '512MB / 2GB',
      cpu: '15%',
      disk: '2.5GB / 10GB',
      network: 'connected',
      database: 'connected',
      mqtt: 'connected'
    }
  }),
  restartSystem: () => Promise.resolve({ data: { success: true, message: '系统重启命令已发送' } }),
  backupSystem: () => Promise.resolve({ data: { success: true, message: '系统备份已开始' } }),
  restoreSystem: (backupId) => Promise.resolve({ data: { success: true, message: `系统恢复已开始，备份ID: ${backupId}` } }),
  
  getMqttConfig: () => Promise.resolve({ 
    data: {
      host: 'localhost',
      port: 1883,
      username: '',
      password: '',
      clientId: 'lowcode-platform',
      keepAlive: true,
      cleanSession: true,
      reconnectPeriod: 1000,
      status: 'connected',
      lastConnected: new Date().toISOString()
    }
  }),
  updateMqttConfig: (config) => Promise.resolve({ data: { success: true, message: 'MQTT配置已更新' } }),
  testMqttConnection: (config) => Promise.resolve({ data: { success: true, message: 'MQTT连接测试成功' } }),
  
  // 新增安全配置方法
  getSecurityConfig: () => Promise.resolve({
    data: {
      authenticationEnabled: true,
      sessionTimeout: 3600, // 秒
      maxLoginAttempts: 5,
      passwordPolicy: {
        minLength: 8,
        requireUppercase: true,
        requireLowercase: true,
        requireNumbers: true,
        requireSpecialChars: true
      },
      twoFactorAuth: {
        enabled: false,
        method: 'totp'
      },
      apiSecurity: {
        rateLimitEnabled: true,
        maxRequestsPerMinute: 100,
        corsEnabled: true,
        allowedOrigins: ['http://localhost:8080', 'https://yourdomain.com']
      },
      deviceSecurity: {
        encryptionEnabled: true,
        certificateValidation: true,
        secureProtocols: ['TLS 1.2', 'TLS 1.3']
      },
      auditLog: {
        enabled: true,
        retentionDays: 90,
        logLevel: 'INFO'
      },
      firewall: {
        enabled: true,
        blockedIPs: [],
        allowedIPs: ['192.168.1.0/24', '10.0.0.0/8']
      },
      lastUpdated: new Date().toISOString()
    }
  }),
  updateSecurityConfig: (config) => Promise.resolve({ data: { success: true, message: '安全配置已更新' } }),
  
  // 日志配置方法
  getLogsConfig: () => Promise.resolve({
    data: {
      level: 'info',
      retentionDays: 30,
      maxFileSize: 100,
      enableAccessLog: true,
      enableErrorLog: true,
      enableAuditLog: true,
      logPath: '/var/log/lowcode-platform',
      rotationEnabled: true,
      rotationSize: '10MB',
      rotationCount: 5,
      lastUpdated: new Date().toISOString()
    }
  }),
  updateLogsConfig: (config) => Promise.resolve({ data: { success: true, message: '日志配置已更新' } }),
  
  // 用户管理方法
  getUsers: () => Promise.resolve({
    data: [
      {
        id: 1,
        username: 'admin',
        email: 'admin@example.com',
        role: 'admin',
        status: 'active',
        lastLogin: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
        createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString()
      },
      {
        id: 2,
        username: 'operator',
        email: 'operator@example.com',
        role: 'operator',
        status: 'active',
        lastLogin: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(),
        createdAt: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000).toISOString()
      },
      {
        id: 3,
        username: 'viewer',
        email: 'viewer@example.com',
        role: 'viewer',
        status: 'active',
        lastLogin: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
        createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString()
      }
    ]
  }),
  createUser: (user) => Promise.resolve({ data: { success: true, message: '用户创建成功', id: Date.now() } }),
  updateUser: (id, user) => Promise.resolve({ data: { success: true, message: '用户更新成功' } }),
  deleteUser: (id) => Promise.resolve({ data: { success: true, message: '用户删除成功' } })
}

// 模拟器管理服务
export const simulatorService = {
  getAll: () => Promise.resolve({ data: mockSimulators }),
  getById: (id) => {
    const simulator = mockSimulators.find(s => s.id == id)
    return Promise.resolve({ data: simulator || null })
  },
  create: (simulator) => {
    const newSimulator = { ...simulator, id: mockSimulators.length + 1, status: 'stopped' }
    mockSimulators.push(newSimulator)
    return Promise.resolve({ data: newSimulator })
  },
  update: (id, simulator) => {
    const index = mockSimulators.findIndex(s => s.id == id)
    if (index !== -1) {
      mockSimulators[index] = { ...mockSimulators[index], ...simulator }
      return Promise.resolve({ data: mockSimulators[index] })
    }
    return Promise.reject(new Error('模拟器未找到'))
  },
  delete: (id) => {
    const index = mockSimulators.findIndex(s => s.id == id)
    if (index !== -1) {
      mockSimulators.splice(index, 1)
      return Promise.resolve({ data: { success: true } })
    }
    return Promise.reject(new Error('模拟器未找到'))
  },
  start: (id) => {
    const simulator = mockSimulators.find(s => s.id == id)
    if (simulator) {
      simulator.status = 'running'
      simulator.lastUpdate = new Date().toISOString()
      return Promise.resolve({ data: { success: true, message: '模拟器已启动' } })
    }
    return Promise.reject(new Error('模拟器未找到'))
  },
  stop: (id) => {
    const simulator = mockSimulators.find(s => s.id == id)
    if (simulator) {
      simulator.status = 'stopped'
      return Promise.resolve({ data: { success: true, message: '模拟器已停止' } })
    }
    return Promise.reject(new Error('模拟器未找到'))
  },
  getStatus: (id) => {
    const simulator = mockSimulators.find(s => s.id == id)
    return Promise.resolve({ data: simulator ? { status: simulator.status } : { status: 'unknown' } })
  }
}

// 数据分析服务
export const analyticsService = {
  getDeviceUsageStats: () => {
    const stats = mockDevices.map(device => ({
      deviceId: device.deviceId,
      deviceName: device.name,
      usageHours: Math.floor(Math.random() * 24),
      energyConsumption: Math.floor(Math.random() * 100),
      operationCount: Math.floor(Math.random() * 50)
    }))
    return Promise.resolve({ data: stats })
  },
  getSceneExecutionStats: () => {
    const stats = mockScenes.map(scene => ({
      sceneId: scene.sceneId,
      sceneName: scene.name,
      executionCount: scene.executionCount,
      successRate: 95 + Math.random() * 5,
      avgExecutionTime: Math.floor(Math.random() * 5000) + 1000
    }))
    return Promise.resolve({ data: stats })
  },
  getSystemMetrics: (timeRange = '24h') => {
    const metrics = {
      deviceOnlineRate: 95 + Math.random() * 5,
      sceneSuccessRate: 98 + Math.random() * 2,
      avgResponseTime: 150 + Math.random() * 100,
      totalOperations: Math.floor(Math.random() * 1000) + 500,
      errorRate: Math.random() * 2
    }
    return Promise.resolve({ data: metrics })
  },
  getEnergyConsumption: (timeRange = '24h') => {
    const data = generateHistoryData(24).map(d => ({
      timestamp: d.timestamp,
      consumption: Math.random() * 10 + 5
    }))
    return Promise.resolve({ data })
  },
  // 新增缺失的方法
  getDeviceStatistics: () => {
    const totalDevices = mockDevices.length
    const onlineDevices = mockDevices.filter(d => d.status === 'online').length
    const offlineDevices = totalDevices - onlineDevices
    const devicesByType = mockDeviceTypes.map(type => ({
      type: type.name,
      count: mockDevices.filter(d => d.type === type.identifier).length
    }))
    
    return Promise.resolve({
      data: {
        totalDevices,
        onlineDevices,
        offlineDevices,
        onlineRate: ((onlineDevices / totalDevices) * 100).toFixed(1),
        devicesByType,
        lastUpdated: new Date().toISOString()
      }
    })
  },
  getDeviceTypeDistribution: () => {
    const distribution = mockDeviceTypes.map(type => {
      const count = mockDevices.filter(d => d.type === type.identifier).length
      return {
        name: type.name,
        value: count,
        percentage: ((count / mockDevices.length) * 100).toFixed(1)
      }
    })
    return Promise.resolve({ data: distribution })
  },
  getDeviceStatusDistribution: () => {
    const statusCounts = mockDevices.reduce((acc, device) => {
      acc[device.status] = (acc[device.status] || 0) + 1
      return acc
    }, {})
    
    const distribution = Object.entries(statusCounts).map(([status, count]) => ({
      name: status === 'online' ? '在线' : '离线',
      value: count,
      percentage: ((count / mockDevices.length) * 100).toFixed(1)
    }))
    
    return Promise.resolve({ data: distribution })
  },
  getPopularScenes: () => {
    const popularScenes = mockScenes
      .sort((a, b) => b.executionCount - a.executionCount)
      .slice(0, 5)
      .map(scene => ({
        sceneId: scene.sceneId,
        name: scene.name,
        executions: scene.executionCount,
        successRate: 0.95 + Math.random() * 0.05,
        lastExecuted: scene.lastExecuted,
        isActive: scene.isActive
      }))
    
    return Promise.resolve({ data: popularScenes })
  },
  getActiveDevices: () => {
    const activeDevices = mockDevices
      .filter(device => device.status === 'online')
      .map(device => ({
        deviceId: device.deviceId,
        name: device.name,
        type: device.type,
        operations: Math.floor(Math.random() * 500) + 50,
        location: device.location,
        lastSeen: device.lastSeen,
        uptime: Math.floor(Math.random() * 168) + 1 // 随机1-168小时，单位：小时
      }))
    
    return Promise.resolve({ data: activeDevices })
  },
  getDetailedReport: (params = {}) => {
    const { type = 'device-usage' } = params
    let reportData = []
    
    switch (type) {
      case 'device-usage':
        reportData = mockDevices.map(device => ({
          deviceName: device.name,
          deviceType: device.type,
          totalOperations: Math.floor(Math.random() * 1000) + 100,
          avgResponseTime: Math.floor(Math.random() * 500) + 50,
          uptime: device.status === 'online' ? `${Math.floor(Math.random() * 168) + 1}h` : '0h',
          lastActive: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString()
        }))
        break
      case 'scene-execution':
        reportData = mockScenes.map(scene => ({
          sceneName: scene.name,
          totalExecutions: scene.executionCount,
          successCount: Math.floor(scene.executionCount * (0.95 + Math.random() * 0.05)),
          failureCount: Math.floor(scene.executionCount * Math.random() * 0.05),
          successRate: 0.95 + Math.random() * 0.05,
          avgExecutionTime: Math.floor(Math.random() * 3000) + 1000,
          lastExecution: scene.lastExecuted
        }))
        break
      case 'system-performance':
        reportData = Array.from({ length: 24 }, (_, i) => ({
          timestamp: new Date(Date.now() - (23 - i) * 60 * 60 * 1000).toISOString(),
          cpuUsage: Math.floor(Math.random() * 50) + 10,
          memoryUsage: Math.floor(Math.random() * 40) + 30,
          diskUsage: Math.floor(Math.random() * 30) + 20,
          networkIn: Math.floor(Math.random() * 100) + 10,
          networkOut: Math.floor(Math.random() * 80) + 5
        }))
        break
      case 'error-statistics':
        reportData = [
          {
            errorType: '设备连接超时',
            errorMessage: 'Device connection timeout',
            count: Math.floor(Math.random() * 20) + 5,
            firstOccurrence: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
            lastOccurrence: new Date(Date.now() - Math.random() * 24 * 60 * 60 * 1000).toISOString(),
            severity: 'medium'
          },
          {
            errorType: '场景执行失败',
            errorMessage: 'Scene execution failed',
            count: Math.floor(Math.random() * 15) + 2,
            firstOccurrence: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
            lastOccurrence: new Date(Date.now() - Math.random() * 12 * 60 * 60 * 1000).toISOString(),
            severity: 'high'
          },
          {
            errorType: '网络异常',
            errorMessage: 'Network anomaly detected',
            count: Math.floor(Math.random() * 10) + 1,
            firstOccurrence: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
            lastOccurrence: new Date(Date.now() - Math.random() * 6 * 60 * 60 * 1000).toISOString(),
            severity: 'low'
          }
        ]
        break
      default:
        reportData = []
    }
    
    return Promise.resolve({ data: reportData })
  },
  getSceneStatistics: () => {
    const totalScenes = mockScenes.length
    const activeScenes = mockScenes.filter(s => s.isActive).length
    const totalExecutions = mockScenes.reduce((sum, scene) => sum + scene.executionCount, 0)
    
    return Promise.resolve({
      data: {
        total: totalScenes,
        executions: totalExecutions,
        change: '+' + Math.floor(Math.random() * 10),
        changeType: 'positive',
        executionChange: '+' + Math.floor(Math.random() * 50),
        executionChangeType: 'positive',
        lastUpdated: new Date().toISOString()
      }
    })
  },
  exportReport: (params = {}) => {
    // 模拟导出功能，返回一个简单的 CSV 格式数据
    const csvContent = 'Name,Type,Value\nSample Data,Export,123\nTest Data,Report,456'
    const blob = new Blob([csvContent], { type: 'text/csv' })
    
    return Promise.resolve({ data: blob })
  }
}

export default {
  deviceService,
  sceneService,
  deviceTypeService,
  monitorService,
  systemService,
  simulatorService,
  analyticsService
}
