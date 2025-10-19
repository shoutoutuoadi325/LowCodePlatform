import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import DeviceManagement from '../views/DeviceManagement.vue'
import DeviceTypes from '../views/DeviceTypes.vue'
import DeviceMonitor from '../views/DeviceMonitor.vue'
import SceneManagement from '../views/SceneManagement.vue'
import SceneDesigner from '../views/SceneDesigner.vue'
import SystemConfig from '../views/SystemConfig.vue'
import DataAnalysis from '../views/DataAnalysis.vue'
import SimulatorManagement from '../views/SimulatorManagement.vue'

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
    path: '/device-types',
    name: 'DeviceTypes',
    component: DeviceTypes
  },
  {
    path: '/device-monitor',
    name: 'DeviceMonitor',
    component: DeviceMonitor
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
  },
  {
    path: '/system-config',
    name: 'SystemConfig',
    component: SystemConfig
  },
  {
    path: '/data-analysis',
    name: 'DataAnalysis',
    component: DataAnalysis
  },
  {
    path: '/simulator-management',
    name: 'SimulatorManagement',
    component: SimulatorManagement
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
