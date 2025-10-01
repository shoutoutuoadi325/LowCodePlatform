import axios from 'axios'

const DEVICE_SERVICE_URL = process.env.VUE_APP_DEVICE_SERVICE_URL || 'http://localhost:8081'
const SCENE_SERVICE_URL = process.env.VUE_APP_SCENE_SERVICE_URL || 'http://localhost:8082'

const deviceApi = axios.create({
  baseURL: DEVICE_SERVICE_URL,
  timeout: 10000
})

const sceneApi = axios.create({
  baseURL: SCENE_SERVICE_URL,
  timeout: 10000
})

export const deviceService = {
  getAll: () => deviceApi.get('/api/devices'),
  getById: (id) => deviceApi.get(`/api/devices/${id}`),
  getByDeviceId: (deviceId) => deviceApi.get(`/api/devices/by-device-id/${deviceId}`),
  create: (device) => deviceApi.post('/api/devices', device),
  update: (id, device) => deviceApi.put(`/api/devices/${id}`, device),
  delete: (id) => deviceApi.delete(`/api/devices/${id}`),
  control: (deviceId, action, parameters = {}) => 
    deviceApi.post(`/api/devices/${deviceId}/control`, { action, parameters }),
  getState: (deviceId) => deviceApi.get(`/api/devices/${deviceId}/state`),
  getByLocation: (building, floor) => 
    deviceApi.get('/api/devices/location', { params: { building, floor } }),
  getByRoom: (room) => deviceApi.get(`/api/devices/room/${room}`)
}

export const sceneService = {
  getAll: () => sceneApi.get('/api/scenes'),
  getActive: () => sceneApi.get('/api/scenes/active'),
  getById: (id) => sceneApi.get(`/api/scenes/${id}`),
  getBySceneId: (sceneId) => sceneApi.get(`/api/scenes/by-scene-id/${sceneId}`),
  create: (scene) => sceneApi.post('/api/scenes', scene),
  update: (id, scene) => sceneApi.put(`/api/scenes/${id}`, scene),
  delete: (id) => sceneApi.delete(`/api/scenes/${id}`),
  execute: (sceneId) => sceneApi.post(`/api/scenes/${sceneId}/execute`)
}
