import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export const deviceApi = {
  getAll: () => apiClient.get('/devices'),
  getById: (id) => apiClient.get(`/devices/${id}`),
  getByRoom: (room) => apiClient.get(`/devices/room/${room}`),
  executeAction: (id, action, parameters) => 
    apiClient.post(`/devices/${id}/action`, { action, parameters }),
  getState: (id) => apiClient.get(`/devices/${id}/state`)
}

export const sceneApi = {
  getAll: () => apiClient.get('/scenes'),
  getById: (id) => apiClient.get(`/scenes/${id}`),
  create: (scene) => apiClient.post('/scenes', scene),
  update: (id, scene) => apiClient.put(`/scenes/${id}`, scene),
  delete: (id) => apiClient.delete(`/scenes/${id}`),
  execute: (id) => apiClient.post(`/scenes/${id}/execute`)
}
