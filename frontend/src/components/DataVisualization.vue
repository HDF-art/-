<template>
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
    <div class="dashboard-panel p-6">
      <div class="flex items-center justify-between mb-6">
        <h3 class="text-lg font-bold text-chinese-dark">识别趋势</h3>
        <div class="flex space-x-2">
          <button 
            v-for="period in ['周', '月', '年']"
            :key="period"
            @click="selectedPeriod = period"
            class="px-3 py-1 text-sm rounded-lg transition-all duration-200"
            :class="selectedPeriod === period ? 'bg-chinese-red text-white' : 'text-chinese-dark hover:bg-chinese-beige'"
          >
            {{ period }}
          </button>
        </div>
      </div>
      <div class="h-64">
        <v-chart 
          :option="lineChartOption" 
          :autoresize="true"
          class="w-full h-full"
        />
      </div>
    </div>

    <div class="dashboard-panel p-6">
      <div class="flex items-center justify-between mb-6">
        <h3 class="text-lg font-bold text-chinese-dark">病害类型分布</h3>
        <div class="flex items-center space-x-2 text-sm text-gray-600">
          <span class="w-3 h-3 bg-chinese-red rounded-full"></span>
          <span>总计: {{ totalRecognitions }}次</span>
        </div>
      </div>
      <div class="h-64">
        <v-chart 
          :option="pieChartOption" 
          :autoresize="true"
          class="w-full h-full"
        />
      </div>
    </div>

    <div class="dashboard-panel p-6">
      <div class="flex items-center justify-between mb-6">
        <h3 class="text-lg font-bold text-chinese-dark">任务状态分布</h3>
        <div class="text-sm text-gray-600">
          单位: 个
        </div>
      </div>
      <div class="h-64">
        <v-chart 
          :option="barChartOption" 
          :autoresize="true"
          class="w-full h-full"
        />
      </div>
    </div>

    <div class="dashboard-panel p-6">
      <div class="flex items-center justify-between mb-6">
        <h3 class="text-lg font-bold text-chinese-dark">系统运行指标</h3>
        <div class="text-sm text-gray-600">
          综合评分: {{ overallScore }}分
        </div>
      </div>
      <div class="h-64">
        <v-chart 
          :option="radarChartOption" 
          :autoresize="true"
          class="w-full h-full"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getRecognitionStats, getTaskStatusStats, getUserRoleStats } from '@/api/user'

const selectedPeriod = ref('月')
const recognitionStats = ref(null)
const taskStatusStats = ref(null)
const userRoleStats = ref(null)

const totalRecognitions = computed(() => {
  return recognitionStats.value?.totalCount || 0
})

const overallScore = computed(() => {
  if (!recognitionStats.value) return 0
  const conf = (recognitionStats.value.averageConfidence || 0) * 100
  return conf.toFixed(1)
})

const lineChartOption = computed(() => {
  const dailyCount = recognitionStats.value?.dailyCount || {}
  const dates = Object.keys(dailyCount).length > 0 ? Object.keys(dailyCount) : ['暂无']
  const values = Object.keys(dailyCount).length > 0 ? Object.values(dailyCount) : [0]
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#c3272b',
      borderWidth: 1,
      textStyle: { color: '#2c3e50', fontSize: 12 }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#c3272b' } },
      axisLabel: { color: '#2c3e50', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#789262' } },
      axisLabel: { color: '#2c3e50', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f8f3e6' } }
    },
    series: [{
      name: '识别次数',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: { color: '#c3272b' },
      lineStyle: { color: '#c3272b', width: 3 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(195, 39, 43, 0.3)' }, { offset: 1, color: 'rgba(195, 39, 43, 0.1)' }]
        }
      },
      data: values
    }]
  }
})

const pieChartOption = computed(() => {
  const dist = recognitionStats.value?.diseaseDistribution || {}
  const colors = ['#c3272b', '#789262', '#d4a574', '#2c3e50', '#e74c3c', '#3498db', '#9b59b6', '#f39c12']
  const chartData = Object.entries(dist).length > 0
    ? Object.entries(dist).map(([name, value], i) => ({ name, value, itemStyle: { color: colors[i % colors.length] } }))
    : [{ name: '暂无数据', value: 0, itemStyle: { color: '#909399' } }]
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#c3272b',
      borderWidth: 1,
      textStyle: { color: '#2c3e50', fontSize: 12 },
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: { orient: 'vertical', left: 'left', textStyle: { color: '#2c3e50', fontSize: 12 } },
    series: [{
      name: '病害类型',
      type: 'pie',
      radius: ['30%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#2c3e50' } },
      labelLine: { show: false },
      data: chartData
    }]
  }
})

const barChartOption = computed(() => {
  const taskDist = taskStatusStats.value?.statusDistribution || {}
  const statusNames = { PENDING: '待开始', RUNNING: '进行中', COMPLETED: '已完成', FAILED: '已失败' }
  const categories = Object.keys(taskDist).length > 0 ? Object.keys(taskDist).map(k => statusNames[k] || k) : ['暂无']
  const values = Object.keys(taskDist).length > 0 ? Object.values(taskDist) : [0]
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#c3272b',
      borderWidth: 1,
      textStyle: { color: '#2c3e50', fontSize: 12 }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: '#c3272b' } },
      axisLabel: { color: '#2c3e50', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#789262' } },
      axisLabel: { color: '#2c3e50', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f8f3e6' } }
    },
    series: [{
      name: '任务数',
      type: 'bar',
      barWidth: '60%',
      itemStyle: {
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#c3272b' }, { offset: 1, color: '#789262' }] },
        borderRadius: [4, 4, 0, 0]
      },
      data: values
    }]
  }
})

const radarChartOption = computed(() => {
  const roleStats = userRoleStats.value || { admin1Count: 0, admin2Count: 0, userCount: 0 }
  const total = (roleStats.admin1Count || 0) + (roleStats.admin2Count || 0) + (roleStats.userCount || 0)
  const maxVal = Math.max(total, 100)
  return {
    tooltip: {
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#c3272b',
      borderWidth: 1,
      textStyle: { color: '#2c3e50', fontSize: 12 }
    },
    radar: {
      indicator: [
        { name: '用户总数', max: maxVal },
        { name: '一级管理员', max: maxVal },
        { name: '二级管理员', max: maxVal },
        { name: '普通用户', max: maxVal },
        { name: '识别次数', max: Math.max(recognitionStats.value?.totalCount || 0, 100) },
        { name: '任务总数', max: Math.max(taskStatusStats.value?.total || 0, 100) }
      ],
      center: ['50%', '50%'],
      radius: '70%',
      splitNumber: 4,
      axisLine: { lineStyle: { color: '#c3272b' } },
      splitLine: { lineStyle: { color: '#f8f3e6' } },
      splitArea: { areaStyle: { color: ['rgba(195, 39, 43, 0.1)', 'rgba(248, 243, 230, 0.2)'] } }
    },
    series: [{
      name: '系统指标',
      type: 'radar',
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: { color: '#c3272b' },
      lineStyle: { color: '#c3272b', width: 2 },
      areaStyle: { color: 'rgba(195, 39, 43, 0.2)' },
      data: [{
        value: [
          total,
          roleStats.admin1Count || 0,
          roleStats.admin2Count || 0,
          roleStats.userCount || 0,
          recognitionStats.value?.totalCount || 0,
          taskStatusStats.value?.total || 0
        ],
        name: '当前状态'
      }]
    }]
  }
})

onMounted(async () => {
  try {
    const [recRes, taskRes, roleRes] = await Promise.all([
      getRecognitionStats().catch(() => null),
      getTaskStatusStats().catch(() => null),
      getUserRoleStats().catch(() => null)
    ])
    if (recRes?.data?.data) recognitionStats.value = recRes.data.data
    if (taskRes?.data?.data) taskStatusStats.value = taskRes.data.data
    if (roleRes?.data?.data) userRoleStats.value = roleRes.data.data
  } catch (e) {
    console.error('加载数据失败', e)
  }
})
</script>

<style scoped>
.dashboard-panel {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(248, 243, 230, 0.3);
}
</style>
