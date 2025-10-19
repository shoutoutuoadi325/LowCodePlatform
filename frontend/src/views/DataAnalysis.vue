<template>
  <div class="data-analysis">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>数据分析</h2>
          <p>设备统计、场景分析和数据报表</p>
        </div>
        <div class="action-section">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleDateRangeChange"
            style="margin-right: 12px"
          />
          <el-button @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
          <el-button type="primary" @click="exportReport" :loading="exporting">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon device">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.total }}</div>
              <div class="stat-label">设备总数</div>
              <div class="stat-change" :class="deviceStats.changeType">
                {{ deviceStats.change }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon scene">
              <el-icon><Operation /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ sceneStats.total }}</div>
              <div class="stat-label">场景总数</div>
              <div class="stat-change" :class="sceneStats.changeType">
                {{ sceneStats.change }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon online">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.online }}</div>
              <div class="stat-label">在线设备</div>
              <div class="stat-change" :class="deviceStats.onlineChangeType">
                {{ deviceStats.onlineChange }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon execution">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ sceneStats.executions }}</div>
              <div class="stat-label">场景执行次数</div>
              <div class="stat-change" :class="sceneStats.executionChangeType">
                {{ sceneStats.executionChange }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 设备类型分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>设备类型分布</span>
              <el-select v-model="deviceChartType" @change="loadDeviceTypeData" style="width: 120px">
                <el-option label="饼图" value="pie" />
                <el-option label="柱状图" value="bar" />
              </el-select>
            </div>
          </template>
          
          <div class="chart-container" v-loading="chartLoading.deviceType">
            <DataChart
              v-if="deviceTypeData.length > 0"
              :data="deviceTypeData"
              :type="deviceChartType"
              metric="count"
              height="300px"
            />
            <div v-else class="no-data">
              <el-empty description="暂无数据" :image-size="80" />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 设备状态统计 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>设备状态统计</span>
          </template>
          
          <div class="chart-container" v-loading="chartLoading.deviceStatus">
            <DataChart
              v-if="deviceStatusData.length > 0"
              :data="deviceStatusData"
              type="pie"
              metric="count"
              height="300px"
            />
            <div v-else class="no-data">
              <el-empty description="暂无数据" :image-size="80" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 场景执行趋势 -->
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>场景执行趋势</span>
              <div class="chart-controls">
                <el-select v-model="trendTimeRange" @change="loadSceneTrendData" style="width: 120px">
                  <el-option label="7天" value="7d" />
                  <el-option label="30天" value="30d" />
                  <el-option label="90天" value="90d" />
                </el-select>
                <el-select v-model="trendChartType" style="width: 120px">
                  <el-option label="折线图" value="line" />
                  <el-option label="柱状图" value="bar" />
                </el-select>
              </div>
            </div>
          </template>
          
          <div class="chart-container" v-loading="chartLoading.sceneTrend">
            <DataChart
              v-if="sceneTrendData.length > 0"
              :data="sceneTrendData"
              :type="trendChartType"
              metric="executions"
              height="400px"
              :show-data-zoom="true"
              :show-toolbox="true"
            />
            <div v-else class="no-data">
              <el-empty description="暂无数据" :image-size="80" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 热门场景排行 -->
      <el-col :span="12">
        <el-card class="table-card">
          <template #header>
            <span>热门场景排行</span>
          </template>
          
          <div v-loading="tableLoading.popularScenes">
            <el-table :data="popularScenes" style="width: 100%" max-height="300">
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="name" label="场景名称" />
              <el-table-column prop="executions" label="执行次数" width="100" />
              <el-table-column prop="successRate" label="成功率" width="80">
                <template #default="{ row }">
                  {{ (row.successRate * 100).toFixed(1) }}%
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>

      <!-- 活跃设备排行 -->
      <el-col :span="12">
        <el-card class="table-card">
          <template #header>
            <span>活跃设备排行</span>
          </template>
          
          <div v-loading="tableLoading.activeDevices">
            <el-table :data="activeDevices" style="width: 100%" max-height="300">
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="name" label="设备名称" />
              <el-table-column prop="type" label="设备类型" width="100" />
              <el-table-column prop="operations" label="操作次数" width="100" />
              <el-table-column prop="uptime" label="在线时长" width="100">
                <template #default="{ row }">
                  {{ formatUptime(row.uptime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细报表 -->
    <el-card class="report-card">
      <template #header>
        <div class="card-header">
          <span>详细报表</span>
          <div class="report-controls">
            <el-select v-model="reportType" @change="loadReportData" style="width: 150px">
              <el-option label="设备使用报表" value="device-usage" />
              <el-option label="场景执行报表" value="scene-execution" />
              <el-option label="系统性能报表" value="system-performance" />
              <el-option label="错误统计报表" value="error-statistics" />
            </el-select>
            <el-button @click="loadReportData" :loading="tableLoading.report">
              查询
            </el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="tableLoading.report">
        <!-- 设备使用报表 -->
        <el-table
          v-if="reportType === 'device-usage'"
          :data="reportData"
          style="width: 100%"
        >
          <el-table-column prop="deviceName" label="设备名称" />
          <el-table-column prop="deviceType" label="设备类型" />
          <el-table-column prop="totalOperations" label="总操作次数" />
          <el-table-column prop="avgResponseTime" label="平均响应时间(ms)" />
          <el-table-column prop="uptime" label="在线时长" />
          <el-table-column prop="lastActive" label="最后活跃时间">
            <template #default="{ row }">
              {{ formatDate(row.lastActive) }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 场景执行报表 -->
        <el-table
          v-else-if="reportType === 'scene-execution'"
          :data="reportData"
          style="width: 100%"
        >
          <el-table-column prop="sceneName" label="场景名称" />
          <el-table-column prop="totalExecutions" label="总执行次数" />
          <el-table-column prop="successCount" label="成功次数" />
          <el-table-column prop="failureCount" label="失败次数" />
          <el-table-column prop="successRate" label="成功率">
            <template #default="{ row }">
              {{ (row.successRate * 100).toFixed(1) }}%
            </template>
          </el-table-column>
          <el-table-column prop="avgExecutionTime" label="平均执行时间(ms)" />
          <el-table-column prop="lastExecution" label="最后执行时间">
            <template #default="{ row }">
              {{ formatDate(row.lastExecution) }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 系统性能报表 -->
        <el-table
          v-else-if="reportType === 'system-performance'"
          :data="reportData"
          style="width: 100%"
        >
          <el-table-column prop="timestamp" label="时间">
            <template #default="{ row }">
              {{ formatDate(row.timestamp) }}
            </template>
          </el-table-column>
          <el-table-column prop="cpuUsage" label="CPU使用率(%)" />
          <el-table-column prop="memoryUsage" label="内存使用率(%)" />
          <el-table-column prop="diskUsage" label="磁盘使用率(%)" />
          <el-table-column prop="networkIn" label="网络入流量(MB)" />
          <el-table-column prop="networkOut" label="网络出流量(MB)" />
        </el-table>

        <!-- 错误统计报表 -->
        <el-table
          v-else-if="reportType === 'error-statistics'"
          :data="reportData"
          style="width: 100%"
        >
          <el-table-column prop="errorType" label="错误类型" />
          <el-table-column prop="errorMessage" label="错误信息" />
          <el-table-column prop="count" label="发生次数" />
          <el-table-column prop="firstOccurrence" label="首次发生">
            <template #default="{ row }">
              {{ formatDate(row.firstOccurrence) }}
            </template>
          </el-table-column>
          <el-table-column prop="lastOccurrence" label="最后发生">
            <template #default="{ row }">
              {{ formatDate(row.lastOccurrence) }}
            </template>
          </el-table-column>
          <el-table-column prop="severity" label="严重程度">
            <template #default="{ row }">
              <el-tag :type="getSeverityType(row.severity)">
                {{ row.severity }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { analyticsService } from '@/services/api'
import DataChart from '@/components/DataChart.vue'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const exporting = ref(false)
const dateRange = ref([
  dayjs().subtract(30, 'day').toDate(),
  dayjs().toDate()
])

// 图表加载状态
const chartLoading = reactive({
  deviceType: false,
  deviceStatus: false,
  sceneTrend: false
})

// 表格加载状态
const tableLoading = reactive({
  popularScenes: false,
  activeDevices: false,
  report: false
})

// 统计数据
const deviceStats = reactive({
  total: 0,
  online: 0,
  change: '+0',
  changeType: 'positive',
  onlineChange: '+0',
  onlineChangeType: 'positive'
})

const sceneStats = reactive({
  total: 0,
  executions: 0,
  change: '+0',
  changeType: 'positive',
  executionChange: '+0',
  executionChangeType: 'positive'
})

// 图表数据
const deviceTypeData = ref([])
const deviceStatusData = ref([])
const sceneTrendData = ref([])

// 表格数据
const popularScenes = ref([])
const activeDevices = ref([])
const reportData = ref([])

// 控制参数
const deviceChartType = ref('pie')
const trendTimeRange = ref('7d')
const trendChartType = ref('line')
const reportType = ref('device-usage')

// 方法
const handleDateRangeChange = () => {
  refreshData()
}

const refreshData = () => {
  loadStatistics()
  loadDeviceTypeData()
  loadDeviceStatusData()
  loadSceneTrendData()
  loadPopularScenes()
  loadActiveDevices()
  loadReportData()
}

const loadStatistics = async () => {
  loading.value = true
  try {
    const [deviceRes, sceneRes] = await Promise.all([
      analyticsService.getDeviceStatistics(getDateRangeParams()),
      analyticsService.getSceneStatistics(getDateRangeParams())
    ])
    
    Object.assign(deviceStats, deviceRes.data)
    Object.assign(sceneStats, sceneRes.data)
  } catch (error) {
    ElMessage.error('加载统计数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadDeviceTypeData = async () => {
  chartLoading.deviceType = true
  try {
    const response = await analyticsService.getDeviceTypeDistribution(getDateRangeParams())
    deviceTypeData.value = response.data || []
  } catch (error) {
    ElMessage.error('加载设备类型数据失败: ' + error.message)
    deviceTypeData.value = []
  } finally {
    chartLoading.deviceType = false
  }
}

const loadDeviceStatusData = async () => {
  chartLoading.deviceStatus = true
  try {
    const response = await analyticsService.getDeviceStatusDistribution()
    deviceStatusData.value = response.data || []
  } catch (error) {
    ElMessage.error('加载设备状态数据失败: ' + error.message)
    deviceStatusData.value = []
  } finally {
    chartLoading.deviceStatus = false
  }
}

const loadSceneTrendData = async () => {
  chartLoading.sceneTrend = true
  try {
    const params = {
      ...getDateRangeParams(),
      timeRange: trendTimeRange.value
    }
    const response = await analyticsService.getSceneExecutionStats(params)
    sceneTrendData.value = response.data || []
  } catch (error) {
    ElMessage.error('加载场景趋势数据失败: ' + error.message)
    sceneTrendData.value = []
  } finally {
    chartLoading.sceneTrend = false
  }
}

const loadPopularScenes = async () => {
  tableLoading.popularScenes = true
  try {
    const response = await analyticsService.getPopularScenes(getDateRangeParams())
    popularScenes.value = response.data || []
  } catch (error) {
    ElMessage.error('加载热门场景数据失败: ' + error.message)
    popularScenes.value = []
  } finally {
    tableLoading.popularScenes = false
  }
}

const loadActiveDevices = async () => {
  tableLoading.activeDevices = true
  try {
    const response = await analyticsService.getActiveDevices(getDateRangeParams())
    activeDevices.value = response.data || []
  } catch (error) {
    ElMessage.error('加载活跃设备数据失败: ' + error.message)
    activeDevices.value = []
  } finally {
    tableLoading.activeDevices = false
  }
}

const loadReportData = async () => {
  tableLoading.report = true
  try {
    const params = {
      ...getDateRangeParams(),
      type: reportType.value
    }
    const response = await analyticsService.getDetailedReport(params)
    reportData.value = response.data || []
  } catch (error) {
    ElMessage.error('加载报表数据失败: ' + error.message)
    reportData.value = []
  } finally {
    tableLoading.report = false
  }
}

const exportReport = async () => {
  exporting.value = true
  try {
    const params = {
      ...getDateRangeParams(),
      type: reportType.value
    }
    const response = await analyticsService.exportReport(params)
    
    // 创建下载链接
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `数据分析报表_${dayjs().format('YYYY-MM-DD')}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('报表导出成功')
  } catch (error) {
    ElMessage.error('导出报表失败: ' + error.message)
  } finally {
    exporting.value = false
  }
}

const getDateRangeParams = () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return {}
  }
  
  return {
    startDate: dayjs(dateRange.value[0]).format('YYYY-MM-DD'),
    endDate: dayjs(dateRange.value[1]).format('YYYY-MM-DD')
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return dayjs(dateString).format('YYYY-MM-DD HH:mm:ss')
}

const formatUptime = (hours) => {
  if (!hours) return '-'
  if (hours < 24) {
    return `${hours}h`
  } else {
    const days = Math.floor(hours / 24)
    const remainingHours = hours % 24
    return `${days}d ${remainingHours}h`
  }
}

const getSeverityType = (severity) => {
  const typeMap = {
    low: 'info',
    medium: 'warning',
    high: 'danger',
    critical: 'danger'
  }
  return typeMap[severity] || 'info'
}

// 生命周期
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.data-analysis {
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

.action-section {
  display: flex;
  align-items: center;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 0;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.device {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.scene {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.online {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.execution {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  font-weight: 500;
}

.stat-change.positive {
  color: #67c23a;
}

.stat-change.negative {
  color: #f56c6c;
}

.chart-card,
.table-card,
.report-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-controls,
.report-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chart-container {
  min-height: 300px;
}

.no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}
</style>