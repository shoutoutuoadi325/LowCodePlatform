<template>
  <div class="device-types">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>设备类型管理</h2>
          <p>管理设备类型定义，包括属性、操作和事件配置</p>
        </div>
        <div class="action-section">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新建设备类型
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="content-card">
      <div class="toolbar">
        <el-input
          v-model="searchText"
          placeholder="搜索设备类型名称或标识符"
          style="width: 300px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button @click="loadDeviceTypes" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-table 
        :data="filteredDeviceTypes" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="类型名称" min-width="150">
          <template #default="{ row }">
            <div class="type-name">
              <strong>{{ row.name }}</strong>
              <div class="type-description">{{ row.description || '暂无描述' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="identifier" label="标识符" width="200" />
        <el-table-column label="属性数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.properties?.length || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="success">{{ row.operations?.length || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="事件数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ row.events?.length || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDeviceType(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" type="primary" @click="editDeviceType(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteDeviceType(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑设备类型对话框 -->
    <DeviceTypeEditor
      v-model:visible="editorVisible"
      :device-type="currentDeviceType"
      :mode="editorMode"
      @saved="handleDeviceTypeSaved"
    />

    <!-- 设备类型预览对话框 -->
    <DeviceTypeViewer
      v-model:visible="viewerVisible"
      :device-type="currentDeviceType"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deviceTypeService } from '@/services/api'
import DeviceTypeEditor from '@/components/DeviceTypeEditor.vue'
import DeviceTypeViewer from '@/components/DeviceTypeViewer.vue'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const searchText = ref('')
const deviceTypes = ref([])
const editorVisible = ref(false)
const viewerVisible = ref(false)
const currentDeviceType = ref(null)
const editorMode = ref('create') // 'create' | 'edit'

// 计算属性
const filteredDeviceTypes = computed(() => {
  if (!searchText.value) return deviceTypes.value
  const search = searchText.value.toLowerCase()
  return deviceTypes.value.filter(type => 
    type.name.toLowerCase().includes(search) ||
    type.identifier.toLowerCase().includes(search)
  )
})

// 方法
const loadDeviceTypes = async () => {
  loading.value = true
  try {
    const response = await deviceTypeService.getAll()
    // 确保response.data是数组
    deviceTypes.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    ElMessage.error('加载设备类型失败: ' + error.message)
    // 确保在错误情况下也设置为空数组
    deviceTypes.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const showCreateDialog = () => {
  currentDeviceType.value = null
  editorMode.value = 'create'
  editorVisible.value = true
}

const viewDeviceType = (deviceType) => {
  currentDeviceType.value = deviceType
  viewerVisible.value = true
}

const editDeviceType = (deviceType) => {
  currentDeviceType.value = { ...deviceType }
  editorMode.value = 'edit'
  editorVisible.value = true
}

const deleteDeviceType = async (deviceType) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备类型 "${deviceType.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deviceTypeService.delete(deviceType.id)
    ElMessage.success('删除成功')
    loadDeviceTypes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const handleDeviceTypeSaved = () => {
  editorVisible.value = false
  loadDeviceTypes()
}

const formatDate = (dateString) => {
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
}

// 生命周期
onMounted(() => {
  loadDeviceTypes()
})
</script>

<style scoped>
.device-types {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.title-section p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.content-card {
  min-height: 600px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.type-name {
  line-height: 1.4;
}

.type-description {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-table :deep(.el-table__header) {
  background-color: #fafafa;
}
</style>