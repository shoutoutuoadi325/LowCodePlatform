import { defineStore } from 'pinia'
import { sceneApi } from '../utils/api'

export const useSceneStore = defineStore('scene', {
  state: () => ({
    scenes: [],
    loading: false,
    error: null
  }),

  actions: {
    async fetchScenes() {
      this.loading = true
      this.error = null
      try {
        const response = await sceneApi.getAll()
        this.scenes = response.data
      } catch (error) {
        this.error = error.message
        console.error('Failed to fetch scenes:', error)
      } finally {
        this.loading = false
      }
    },

    async createScene(scene) {
      try {
        const response = await sceneApi.create(scene)
        await this.fetchScenes()
        return response.data
      } catch (error) {
        console.error('Failed to create scene:', error)
        throw error
      }
    },

    async updateScene(id, scene) {
      try {
        const response = await sceneApi.update(id, scene)
        await this.fetchScenes()
        return response.data
      } catch (error) {
        console.error('Failed to update scene:', error)
        throw error
      }
    },

    async deleteScene(id) {
      try {
        await sceneApi.delete(id)
        await this.fetchScenes()
      } catch (error) {
        console.error('Failed to delete scene:', error)
        throw error
      }
    },

    async executeScene(id) {
      try {
        const response = await sceneApi.execute(id)
        return response.data
      } catch (error) {
        console.error('Failed to execute scene:', error)
        throw error
      }
    }
  }
})
