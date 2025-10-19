<template>
  <div class="json-editor">
    <el-input
      v-model="jsonText"
      type="textarea"
      :rows="rows"
      :placeholder="placeholder"
      @input="handleInput"
      :class="{ 'error': hasError }"
    />
    <div v-if="hasError" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: [Object, Array, String],
    default: () => ({})
  },
  rows: {
    type: Number,
    default: 6
  },
  placeholder: {
    type: String,
    default: '请输入JSON格式数据'
  }
})

const emit = defineEmits(['update:modelValue', 'error'])

const jsonText = ref('')
const errorMessage = ref('')

const hasError = computed(() => !!errorMessage.value)

// 初始化JSON文本
const initJsonText = () => {
  try {
    jsonText.value = JSON.stringify(props.modelValue, null, 2)
  } catch (error) {
    jsonText.value = ''
  }
}

// 处理输入
const handleInput = () => {
  try {
    const parsed = JSON.parse(jsonText.value)
    errorMessage.value = ''
    emit('update:modelValue', parsed)
    emit('error', null)
  } catch (error) {
    errorMessage.value = `JSON格式错误: ${error.message}`
    emit('error', error)
  }
}

// 监听外部值变化
watch(() => props.modelValue, () => {
  initJsonText()
}, { immediate: true })

// 监听JSON文本变化
watch(jsonText, () => {
  if (!jsonText.value.trim()) {
    errorMessage.value = ''
    emit('update:modelValue', {})
    emit('error', null)
  }
})
</script>

<style scoped>
.json-editor {
  width: 100%;
}

.json-editor .el-textarea.error :deep(.el-textarea__inner) {
  border-color: #f56c6c;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}
</style>