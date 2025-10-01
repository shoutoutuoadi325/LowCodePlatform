import { createRouter, createWebHistory } from 'vue-router'
import DeviceView from './views/DeviceView.vue'
import SceneView from './views/SceneView.vue'

const routes = [
  {
    path: '/',
    name: 'devices',
    component: DeviceView
  },
  {
    path: '/scenes',
    name: 'scenes',
    component: SceneView
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
