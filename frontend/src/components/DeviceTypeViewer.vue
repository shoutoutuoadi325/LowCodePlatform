<template>
  <el-dialog
    v-model="dialogVisible"
    title="设备类型详情"
    width="70%"
    destroy-on-close
  >
    <div v-if="deviceType" class="device-type-viewer">
      <!-- 基本信息 -->
      <el-card class="info-section">
        <template #header>
          <span>基本信息</span>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="类型名称">
            {{ deviceType.name }}
          </el-descriptions-item>
          <el-descriptions-item label="标识符">
            <el-tag>{{ deviceType.identifier }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ deviceType.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(deviceType.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDate(deviceType.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 属性定义 -->
      <el-card class="info-section">
        <template #header>
          <div class="section-header">
            <span>属性定义</span>
            <el-tag size="small">{{ deviceType.properties?.length || 0 }} 个属性</el-tag>
          </div>
        </template>
        
        <div v-if="!deviceType.properties || deviceType.properties.length === 0" class="empty-state">
          <el-empty description="暂无属性定义" :image-size="80" />
        </div>
        
        <div v-else>
          <el-table :data="deviceType.properties" stripe>
            <el-table-column prop="name" label="属性名称" width="150" />
            <el-table-column prop="identifier" label="标识符" width="150" />
            <el-table-column label="数据类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getDataTypeColor(row.dataType)" size="small">
                  {{ getDataTypeText(row.dataType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="是否必需" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                  {{ row.required ? '必需' : '可选' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="defaultValue" label="默认值" width="120">
              <template #default="{ row }">
                <span v-if="row.defaultValue">{{ row.defaultValue }}</span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200">
              <template #default="{ row }">
                <span v-if="row.description">{{ row.description }}</span>
                <span v-else class="text-muted">暂无描述</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <!-- 操作定义 -->
      <el-card class="info-section">
        <template #header>
          <div class="section-header">
            <span>操作定义</span>
            <el-tag size="small" type="success">{{ deviceType.operations?.length || 0 }} 个操作</el-tag>
          </div>
        </template>
        
        <div v-if="!deviceType.operations || deviceType.operations.length === 0" class="empty-state">
          <el-empty description="暂无操作定义" :image-size="80" />
        </div>
        
        <div v-else class="operations-list">
          <div
            v-for="(operation, index) in deviceType.operations"
            :key="index"
            class="operation-item"
          >
            <el-card>
              <div class="operation-header">
                <div class="operation-info">
                  <h4>{{ operation.name }}</h4>
                  <el-tag size="small">{{ operation.identifier }}</el-tag>
                </div>
              </div>
              
              <div v-if="operation.description" class="operation-description">
                <p>{{ operation.description }}</p>
              </div>
              
              <div v-if="operation.parameters && operation.parameters.length > 0" class="operation-parameters">
                <h5>参数列表：</h5>
                <el-table :data="operation.parameters" size="small">
                  <el-table-column prop="name" label="参数名" width="120" />
                  <el-table-column prop="dataType" label="类型" width="100" />
                  <el-table-column label="必需" width="80" align="center">
                    <template #default="{ row }">
                      <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                        {{ row.required ? '是' : '否' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="描述" min-width="150" />
                </el-table>
              </div>
              
              <div v-else class="no-parameters">
                <span class="text-muted">无参数</span>
              </div>
            </el-card>
          </div>
        </div>
      </el-card>

      <!-- 事件定义 -->
      <el-card class="info-section">
        <template #header>
          <div class="section-header">
            <span>事件定义</span>
            <el-tag size="small" type="warning">{{ deviceType.events?.length || 0 }} 个事件</el-tag>
          </div>
        </template>
        
        <div v-if="!deviceType.events || deviceType.events.length === 0" class="empty-state">
          <el-empty description="暂无事件定义" :image-size="80" />
        </div>
        
        <div v-else class="events-list">
          <div
            v-for="(event, index) in deviceType.events"
            :key="index"
            class="event-item"
          >
            <el-card>
              <div class="event-header">
                <div class="event-info">
                  <h4>{{ event.name }}</h4>
                  <el-tag size="small" type="warning">{{ event.identifier }}</el-tag>
                </div>
              </div>
              
              <div v-if="event.description" class="event-description">
                <p>{{ event.description }}</p>
              </div>
              
              <div v-if="event.properties && event.properties.length > 0" class="event-properties">
                <h5>事件属性：</h5>
                <el-table :data="event.properties" size="small">
                  <el-table-column prop="name" label="属性名" width="120" />
                  <el-table-column prop="dataType" label="类型" width="100" />
                  <el-table-column label="必需" width="80" align="center">
                    <template #default="{ row }">
                      <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                        {{ row.required ? '是' : '否' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="描述" min-width="150" />
                </el-table>
              </div>
              
              <div v-else class="no-properties">
                <span class="text-muted">无事件属性</span>
              </div>
            </el-card>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  deviceType: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 方法
const formatDate = (dateString) => {
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
}

const getDataTypeColor = (dataType) => {
  const colorMap = {
    string: 'primary',
    number: 'success',
    boolean: 'warning',
    object: 'info'
  }
  return colorMap[dataType] || 'info'
}

const getDataTypeText = (dataType) => {
  const textMap = {
    string: '字符串',
    number: '数字',
    boolean: '布尔值',
    object: '对象'
  }
  return textMap[dataType] || dataType
}
</script>

<style scoped>
.device-type-viewer {
  max-height: 70vh;
  overflow-y: auto;
}

.info-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.operations-list,
.events-list {
  max-height: 400px;
  overflow-y: auto;
}

.operation-item,
.event-item {
  margin-bottom: 16px;
}

.operation-header,
.event-header {
  margin-bottom: 12px;
}

.operation-info,
.event-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.operation-info h4,
.event-info h4 {
  margin: 0;
  color: #303133;
}

.operation-description,
.event-description {
  margin-bottom: 12px;
  color: #606266;
}

.operation-parameters,
.event-properties {
  margin-top: 12px;
}

.operation-parameters h5,
.event-properties h5 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
}

.no-parameters,
.no-properties {
  padding: 12px 0;
  text-align: center;
}

.text-muted {
  color: #909399;
}

.dialog-footer {
  text-align: right;
}
</style>