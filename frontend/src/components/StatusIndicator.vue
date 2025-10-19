<template>
  <div class="status-indicator">
    <el-tag 
      :type="statusType" 
      :effect="effect"
      :size="size"
      class="status-tag"
    >
      <el-icon v-if="showIcon" class="status-icon">
        <component :is="statusIcon" />
      </el-icon>
      {{ statusText }}
    </el-tag>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { 
  CircleCheckFilled, 
  CircleCloseFilled, 
  WarningFilled, 
  QuestionFilled,
  Loading
} from '@element-plus/icons-vue'

const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: (value) => ['online', 'offline', 'warning', 'error', 'unknown', 'loading'].includes(value)
  },
  text: {
    type: String,
    default: ''
  },
  showIcon: {
    type: Boolean,
    default: true
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  },
  effect: {
    type: String,
    default: 'light',
    validator: (value) => ['dark', 'light', 'plain'].includes(value)
  }
})

const statusConfig = {
  online: {
    type: 'success',
    text: '在线',
    icon: CircleCheckFilled
  },
  offline: {
    type: 'info',
    text: '离线',
    icon: CircleCloseFilled
  },
  warning: {
    type: 'warning',
    text: '警告',
    icon: WarningFilled
  },
  error: {
    type: 'danger',
    text: '错误',
    icon: CircleCloseFilled
  },
  unknown: {
    type: 'info',
    text: '未知',
    icon: QuestionFilled
  },
  loading: {
    type: 'primary',
    text: '加载中',
    icon: Loading
  }
}

const statusType = computed(() => statusConfig[props.status]?.type || 'info')
const statusText = computed(() => props.text || statusConfig[props.status]?.text || props.status)
const statusIcon = computed(() => statusConfig[props.status]?.icon || QuestionFilled)
</script>

<style scoped>
.status-indicator {
  display: inline-block;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.status-icon {
  font-size: 12px;
}
</style>