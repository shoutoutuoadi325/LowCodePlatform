import { defineStore } from 'pinia'
import { deviceApi } from '../utils/api'

export const useDeviceStore = defineStore('device', {
  state: () => ({
    devices: [],
    loading: false,
    error: null
  }),

  actions: {
    async fetchDevices() {
      this.loading = true
      this.error = null
      try {
        const response = await deviceApi.getAll()
        this.devices = response.data
      } catch (error) {
        this.error = error.message
        console.error('Failed to fetch devices:', error)
      } finally {
        this.loading = false
      }
    },

    async executeDeviceAction(deviceId, action, parameters) {
      try {
        const response = await deviceApi.executeAction(deviceId, action, parameters)
        await this.fetchDevices()
        return response.data
      } catch (error) {
        console.error('Failed to execute action:', error)
        throw error
      }
    },

    getDeviceById(id) {
      return this.devices.find(d => d.id === id)
    },

    getDevicesByRoom(room) {
      return this.devices.filter(d => d.room === room)
    }
  }
})
