import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import DeviceManagement from '../views/DeviceManagement.vue'
import SceneManagement from '../views/SceneManagement.vue'
import SceneDesigner from '../views/SceneDesigner.vue'

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/devices',
    name: 'DeviceManagement',
    component: DeviceManagement
  },
  {
    path: '/scenes',
    name: 'SceneManagement',
    component: SceneManagement
  },
  {
    path: '/scene-designer',
    name: 'SceneDesigner',
    component: SceneDesigner
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
