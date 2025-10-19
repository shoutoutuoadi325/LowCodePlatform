<template>
  <div class="data-chart" :style="{ height }">
    <v-chart
      :option="chartOption"
      :style="{ height: '100%', width: '100%' }"
      autoresize
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent
])

// Props
const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  metric: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'line', // line, bar, pie
    validator: (value) => ['line', 'bar', 'pie'].includes(value)
  },
  height: {
    type: String,
    default: '400px'
  },
  title: {
    type: String,
    default: ''
  },
  showDataZoom: {
    type: Boolean,
    default: true
  },
  showToolbox: {
    type: Boolean,
    default: false
  }
})

// 计算属性
const chartOption = computed(() => {
  if (!props.data || props.data.length === 0) {
    return getEmptyOption()
  }

  switch (props.type) {
    case 'line':
      return getLineOption()
    case 'bar':
      return getBarOption()
    case 'pie':
      return getPieOption()
    default:
      return getLineOption()
  }
})

// 获取空图表配置
const getEmptyOption = () => ({
  title: {
    text: '暂无数据',
    left: 'center',
    top: 'center',
    textStyle: {
      color: '#909399',
      fontSize: 16
    }
  }
})

// 获取折线图配置
const getLineOption = () => {
  const xAxisData = props.data.map(item => 
    dayjs(item.timestamp).format('MM-DD HH:mm')
  )
  const seriesData = props.data.map(item => item[props.metric] || 0)

  return {
    title: props.title ? {
      text: props.title,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'normal'
      }
    } : undefined,
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0]
        return `
          <div>
            <div>${param.name}</div>
            <div>
              <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:${param.color};"></span>
              ${props.metric}: ${param.value}
            </div>
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: props.showDataZoom ? '15%' : '3%',
      containLabel: true
    },
    toolbox: props.showToolbox ? {
      feature: {
        saveAsImage: { title: '保存为图片' },
        dataZoom: { title: { zoom: '区域缩放', back: '区域缩放还原' } },
        restore: { title: '还原' }
      }
    } : undefined,
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: props.metric
    },
    dataZoom: props.showDataZoom ? [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        start: 0,
        end: 100,
        height: 30
      }
    ] : undefined,
    series: [
      {
        name: props.metric,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2
        },
        areaStyle: {
          opacity: 0.3
        },
        data: seriesData,
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
}

// 获取柱状图配置
const getBarOption = () => {
  const xAxisData = props.data.map(item => 
    dayjs(item.timestamp).format('MM-DD HH:mm')
  )
  const seriesData = props.data.map(item => item[props.metric] || 0)

  return {
    title: props.title ? {
      text: props.title,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'normal'
      }
    } : undefined,
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: props.showDataZoom ? '15%' : '3%',
      containLabel: true
    },
    toolbox: props.showToolbox ? {
      feature: {
        saveAsImage: { title: '保存为图片' },
        dataZoom: { title: { zoom: '区域缩放', back: '区域缩放还原' } },
        restore: { title: '还原' }
      }
    } : undefined,
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: props.metric
    },
    dataZoom: props.showDataZoom ? [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        start: 0,
        end: 100,
        height: 30
      }
    ] : undefined,
    series: [
      {
        name: props.metric,
        type: 'bar',
        data: seriesData,
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
}

// 获取饼图配置
const getPieOption = () => {
  // 对于饼图，我们需要聚合数据
  const aggregatedData = aggregateDataForPie()

  return {
    title: props.title ? {
      text: props.title,
      left: 'center',
      top: '5%',
      textStyle: {
        fontSize: 16,
        fontWeight: 'normal'
      }
    } : undefined,
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [
      {
        name: props.metric,
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: aggregatedData
      }
    ]
  }
}

// 为饼图聚合数据
const aggregateDataForPie = () => {
  if (!props.data || props.data.length === 0) return []

  // 简单的数据聚合逻辑，可以根据实际需求调整
  const valueMap = new Map()
  
  props.data.forEach(item => {
    const value = item[props.metric]
    if (typeof value === 'number') {
      // 将数值分组（这里简单按范围分组）
      let range
      if (value < 20) range = '0-20'
      else if (value < 40) range = '20-40'
      else if (value < 60) range = '40-60'
      else if (value < 80) range = '60-80'
      else range = '80+'

      valueMap.set(range, (valueMap.get(range) || 0) + 1)
    }
  })

  return Array.from(valueMap.entries()).map(([name, value]) => ({
    name,
    value
  }))
}
</script>

<style scoped>
.data-chart {
  width: 100%;
}
</style>