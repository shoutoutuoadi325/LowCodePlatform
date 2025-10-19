<template>
  <div class="scene-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>场景管理</h2>
          <el-button type="primary" @click="$router.push('/scene-designer')">
            <el-icon><MagicStick /></el-icon>
            创建场景
          </el-button>
        </div>
      </template>
      
      <el-table :data="scenes" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="场景名称" width="200" />
        <el-table-column prop="sceneId" label="场景ID" width="250" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="触发器" width="100">
          <template #default="{ row }">
            {{ row.triggers?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="动作" width="100">
          <template #default="{ row }">
            {{ row.actions?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="success"
              @click="executeScene(row)"
              :disabled="row.status !== 'ACTIVE'"
            >
              执行
            </el-button>
            <el-button
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteScene(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sceneService } from '../services/api'

const loading = ref(false)
const scenes = ref([])

const getStatusType = (status) => {
  const types = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    ERROR: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    ACTIVE: '启用',
    INACTIVE: '未启用',
    ERROR: '错误'
  }
  return texts[status] || status
}

const loadScenes = async () => {
  loading.value = true
  try {
    const response = await sceneService.getAll()
    // 确保response.data是数组
    scenes.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('Failed to load scenes:', error)
    ElMessage.error('加载场景列表失败')
    // 确保在错误情况下也设置为空数组
    scenes.value = []
  } finally {
    loading.value = false
  }
}

const executeScene = async (scene) => {
  try {
    await sceneService.execute(scene.sceneId)
    ElMessage.success('场景执行成功')
  } catch (error) {
    console.error('Failed to execute scene:', error)
    ElMessage.error('场景执行失败')
  }
}

const toggleStatus = async (scene) => {
  try {
    const newStatus = scene.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    await sceneService.update(scene.id, {
      ...scene,
      status: newStatus
    })
    ElMessage.success(`场景已${newStatus === 'ACTIVE' ? '启用' : '禁用'}`)
    loadScenes()
  } catch (error) {
    console.error('Failed to toggle scene status:', error)
    ElMessage.error('更新场景状态失败')
  }
}

const deleteScene = async (scene) => {
  try {
    await ElMessageBox.confirm('确定要删除这个场景吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await sceneService.delete(scene.id)
    ElMessage.success('场景删除成功')
    loadScenes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete scene:', error)
      ElMessage.error('删除场景失败')
    }
  }
}

onMounted(() => {
  loadScenes()
})
</script>

<style scoped>
.scene-management {
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
