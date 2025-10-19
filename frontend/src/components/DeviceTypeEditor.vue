<template>
  <el-dialog
    v-model="dialogVisible"
    :title="mode === 'create' ? '新建设备类型' : '编辑设备类型'"
    width="80%"
    :before-close="handleClose"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="device-type-form"
    >
      <!-- 基本信息 -->
      <el-card class="form-section">
        <template #header>
          <span>基本信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入设备类型名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标识符" prop="identifier">
              <el-input 
                v-model="form.identifier" 
                placeholder="请输入唯一标识符"
                :disabled="mode === 'edit'"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入设备类型描述"
          />
        </el-form-item>
      </el-card>

      <!-- 属性定义 -->
      <el-card class="form-section">
        <template #header>
          <div class="section-header">
            <span>属性定义</span>
            <el-button size="small" type="primary" @click="addProperty">
              <el-icon><Plus /></el-icon>
              添加属性
            </el-button>
          </div>
        </template>
        
        <div v-if="form.properties.length === 0" class="empty-state">
          <el-empty description="暂无属性定义" :image-size="80" />
        </div>
        
        <div v-else class="properties-list">
          <div
            v-for="(property, index) in form.properties"
            :key="index"
            class="property-item"
          >
            <el-card>
              <div class="property-header">
                <span class="property-title">属性 {{ index + 1 }}</span>
                <el-button
                  size="small"
                  type="danger"
                  text
                  @click="removeProperty(index)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
              
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-form-item label="属性名称" :prop="`properties.${index}.name`">
                    <el-input v-model="property.name" placeholder="属性名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="标识符" :prop="`properties.${index}.identifier`">
                    <el-input v-model="property.identifier" placeholder="属性标识符" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="数据类型" :prop="`properties.${index}.dataType`">
                    <el-select v-model="property.dataType" placeholder="选择数据类型">
                      <el-option label="字符串" value="string" />
                      <el-option label="数字" value="number" />
                      <el-option label="布尔值" value="boolean" />
                      <el-option label="对象" value="object" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="是否必需">
                    <el-switch v-model="property.required" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="默认值">
                    <el-input v-model="property.defaultValue" placeholder="默认值" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="描述">
                    <el-input v-model="property.description" placeholder="属性描述" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-card>
          </div>
        </div>
      </el-card>

      <!-- 操作定义 -->
      <el-card class="form-section">
        <template #header>
          <div class="section-header">
            <span>操作定义</span>
            <el-button size="small" type="primary" @click="addOperation">
              <el-icon><Plus /></el-icon>
              添加操作
            </el-button>
          </div>
        </template>
        
        <div v-if="form.operations.length === 0" class="empty-state">
          <el-empty description="暂无操作定义" :image-size="80" />
        </div>
        
        <div v-else class="operations-list">
          <div
            v-for="(operation, index) in form.operations"
            :key="index"
            class="operation-item"
          >
            <el-card>
              <div class="operation-header">
                <span class="operation-title">操作 {{ index + 1 }}</span>
                <el-button
                  size="small"
                  type="danger"
                  text
                  @click="removeOperation(index)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="操作名称" :prop="`operations.${index}.name`">
                    <el-input v-model="operation.name" placeholder="操作名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="标识符" :prop="`operations.${index}.identifier`">
                    <el-input v-model="operation.identifier" placeholder="操作标识符" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="描述">
                    <el-input v-model="operation.description" placeholder="操作描述" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="参数定义">
                <JsonEditor v-model="operation.parameters" :rows="4" />
              </el-form-item>
            </el-card>
          </div>
        </div>
      </el-card>

      <!-- 事件定义 -->
      <el-card class="form-section">
        <template #header>
          <div class="section-header">
            <span>事件定义</span>
            <el-button size="small" type="primary" @click="addEvent">
              <el-icon><Plus /></el-icon>
              添加事件
            </el-button>
          </div>
        </template>
        
        <div v-if="form.events.length === 0" class="empty-state">
          <el-empty description="暂无事件定义" :image-size="80" />
        </div>
        
        <div v-else class="events-list">
          <div
            v-for="(event, index) in form.events"
            :key="index"
            class="event-item"
          >
            <el-card>
              <div class="event-header">
                <span class="event-title">事件 {{ index + 1 }}</span>
                <el-button
                  size="small"
                  type="danger"
                  text
                  @click="removeEvent(index)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
              
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="事件名称" :prop="`events.${index}.name`">
                    <el-input v-model="event.name" placeholder="事件名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="标识符" :prop="`events.${index}.identifier`">
                    <el-input v-model="event.identifier" placeholder="事件标识符" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="描述">
                    <el-input v-model="event.description" placeholder="事件描述" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="事件属性">
                <JsonEditor v-model="event.properties" :rows="4" />
              </el-form-item>
            </el-card>
          </div>
        </div>
      </el-card>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ mode === 'create' ? '创建' : '保存' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { deviceTypeService } from '@/services/api'
import JsonEditor from './JsonEditor.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  deviceType: {
    type: Object,
    default: null
  },
  mode: {
    type: String,
    default: 'create',
    validator: (value) => ['create', 'edit'].includes(value)
  }
})

const emit = defineEmits(['update:visible', 'saved'])

// 响应式数据
const formRef = ref()
const saving = ref(false)

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const form = ref({
  name: '',
  identifier: '',
  description: '',
  properties: [],
  operations: [],
  events: []
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入设备类型名称', trigger: 'blur' }
  ],
  identifier: [
    { required: true, message: '请输入标识符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '标识符必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' }
  ]
}

// 方法
const initForm = () => {
  if (props.deviceType && props.mode === 'edit') {
    form.value = {
      ...props.deviceType,
      properties: props.deviceType.properties || [],
      operations: props.deviceType.operations || [],
      events: props.deviceType.events || []
    }
  } else {
    form.value = {
      name: '',
      identifier: '',
      description: '',
      properties: [],
      operations: [],
      events: []
    }
  }
}

const addProperty = () => {
  form.value.properties.push({
    name: '',
    identifier: '',
    dataType: 'string',
    required: false,
    defaultValue: '',
    description: ''
  })
}

const removeProperty = (index) => {
  form.value.properties.splice(index, 1)
}

const addOperation = () => {
  form.value.operations.push({
    name: '',
    identifier: '',
    description: '',
    parameters: []
  })
}

const removeOperation = (index) => {
  form.value.operations.splice(index, 1)
}

const addEvent = () => {
  form.value.events.push({
    name: '',
    identifier: '',
    description: '',
    properties: []
  })
}

const removeEvent = (index) => {
  form.value.events.splice(index, 1)
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    if (props.mode === 'create') {
      await deviceTypeService.create(form.value)
      ElMessage.success('设备类型创建成功')
    } else {
      await deviceTypeService.update(props.deviceType.id, form.value)
      ElMessage.success('设备类型更新成功')
    }
    
    emit('saved')
  } catch (error) {
    if (error.message) {
      ElMessage.error('保存失败: ' + error.message)
    }
  } finally {
    saving.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

// 监听器
watch(() => props.visible, (visible) => {
  if (visible) {
    initForm()
  }
})
</script>

<style scoped>
.device-type-form {
  max-height: 70vh;
  overflow-y: auto;
}

.form-section {
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

.property-item,
.operation-item,
.event-item {
  margin-bottom: 16px;
}

.property-header,
.operation-header,
.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.property-title,
.operation-title,
.event-title {
  font-weight: 600;
  color: #303133;
}

.dialog-footer {
  text-align: right;
}

.properties-list,
.operations-list,
.events-list {
  max-height: 400px;
  overflow-y: auto;
}
</style>