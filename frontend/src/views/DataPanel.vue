<template>
  <div class="data-panel-container">
    <div class="dynamic-bg">
      <div class="bg-gradient"></div>
    </div>

    <div class="header-bar">
      <div class="header-left">
        <div class="stat-item">
          <span class="stat-label">在线用户</span>
          <span class="stat-value pulse">{{ onlineUsers }}</span>
        </div>
      </div>
      <div class="header-center">
        <h1>{{ panelTitle }}</h1>
        <p class="subtitle">{{ currentTime }}</p>
      </div>
      <div class="header-right">
        <div class="stat-item">
          <span class="stat-label">今日活跃</span>
          <span class="stat-value">{{ todayActive }}</span>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 一级管理员面板 -->
      <div v-if="isAdmin1" class="fade-in">
        <div class="row">
          <div class="panel slide-up" style="animation-delay: 0.1s">
            <div class="panel-header">
              <i class="el-icon-office-building"></i>
              <span>农场统计</span>
            </div>
            <div class="panel-body">
              <div id="farmStatsChart" style="height: 280px;"></div>
            </div>
          </div>
          <div class="panel main-panel slide-up" style="animation-delay: 0.2s">
            <div class="panel-header">
              <i class="el-icon-data-bar"></i>
              <span>农场分布</span>
            </div>
            <div class="panel-body">
              <div id="farmDistributionChart" style="height: 400px;"></div>
            </div>
          </div>
          <div class="panel slide-up" style="animation-delay: 0.3s">
            <div class="panel-header">
              <i class="el-icon-cpu"></i>
              <span>设备统计</span>
            </div>
            <div class="panel-body">
              <div id="deviceStatsChart" style="height: 280px;"></div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="panel slide-up" style="animation-delay: 0.4s">
            <div class="panel-header">
              <i class="el-icon-user"></i>
              <span>用户统计</span>
            </div>
            <div class="panel-body">
              <div id="userStatsChart" style="height: 280px;"></div>
            </div>
          </div>
          <div class="panel slide-up" style="animation-delay: 0.5s">
            <div class="panel-header">
              <i class="el-icon-s-data"></i>
              <span>农场产量趋势</span>
            </div>
            <div class="panel-body">
              <div id="farmProductionChart" style="height: 280px;"></div>
            </div>
          </div>
          <div class="panel slide-up" style="animation-delay: 0.6s">
            <div class="panel-header">
              <i class="el-icon-time"></i>
              <span>周活跃度</span>
            </div>
            <div class="panel-body">
              <div id="weeklyActivityChart" style="height: 280px;"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 二级管理员面板 -->
      <div v-else-if="isAdmin2" class="fade-in">
        <div class="row">
          <div class="panel slide-up" style="animation-delay: 0.1s">
            <div class="panel-header">
              <i class="el-icon-s-grid"></i>
              <span>可用模型概览</span>
            </div>
            <div class="panel-body">
              <div id="admin2ModelChart" style="height: 280px;"></div>
            </div>
          </div>
          <div class="panel slide-up" style="animation-delay: 0.2s">
            <div class="panel-header">
              <i class="el-icon-pie-chart"></i>
              <span>任务执行统计</span>
            </div>
            <div class="panel-body">
              <div id="admin2TaskChart" style="height: 280px;"></div>
            </div>
          </div>
          <div class="panel slide-up" style="animation-delay: 0.3s">
            <div class="panel-header">
              <i class="el-icon-monitor"></i>
              <span>系统资源监控</span>
            </div>
            <div class="panel-body">
              <div id="admin2ResourceChart" style="height: 280px;"></div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="panel slide-up" style="animation-delay: 0.4s">
            <div class="panel-header">
              <i class="el-icon-data-line"></i>
              <span>识别记录趋势</span>
            </div>
            <div class="panel-body">
              <div id="admin2TrendChart" style="height: 280px;"></div>
            </div>
          </div>
          <div class="panel slide-up" style="animation-delay: 0.5s">
            <div class="panel-header">
              <i class="el-icon-connection"></i>
              <span>网络速率监控</span>
            </div>
            <div class="panel-body">
              <div id="admin2NetworkChart" style="height: 280px;"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 普通用户面板 -->
      <div v-else-if="isUser" class="fade-in">
        <div class="user-row">
          <div class="user-panel slide-up" style="animation-delay: 0.1s">
            <div class="panel-header">
              <i class="el-icon-box"></i>
              <span>可用模型（权重）数量</span>
            </div>
            <div class="panel-body">
              <div id="userModelChart" style="height: 280px;"></div>
            </div>
          </div>
          <div class="user-panel slide-up" style="animation-delay: 0.2s">
            <div class="panel-header">
              <i class="el-icon-data-analysis"></i>
              <span>我的任务使用统计</span>
            </div>
            <div class="panel-body">
              <div id="userTaskChart" style="height: 280px;"></div>
            </div>
          </div>
        </div>
        <div class="user-row">
          <div class="user-panel slide-up" style="animation-delay: 0.3s">
            <div class="panel-header">
              <i class="el-icon-memory-card"></i>
              <span>内存占用监控</span>
            </div>
            <div class="panel-body">
              <div id="userMemoryChart" style="height: 250px;"></div>
              <div class="memory-realtime-info">
                <div class="memory-stat">
                  <span class="memory-label">当前占用</span>
                  <span class="memory-value">{{ memoryUsed }} MB</span>
                </div>
                <div class="memory-stat">
                  <span class="memory-label">使用率</span>
                  <span class="memory-value" :class="memoryUsageClass">{{ memoryPercent }}%</span>
                </div>
              </div>
            </div>
          </div>
          <div class="user-panel slide-up" style="animation-delay: 0.4s">
            <div class="panel-header">
              <i class="el-icon-upload2"></i>
              <span>服务器负载监控</span>
            </div>
            <div class="panel-body">
              <div id="userNetworkChart" style="height: 250px;"></div>
              <div class="network-realtime-info">
                <div class="network-stat upload-stat">
                  <i class="el-icon-top"></i>
                  <span class="network-label">上行负载</span>
                  <span class="network-value">{{ networkUploadSpeed }}</span>
                </div>
                <div class="network-stat download-stat">
                  <i class="el-icon-bottom"></i>
                  <span class="network-label">下行负载</span>
                  <span class="network-value">{{ networkDownloadSpeed }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="user-panel slide-up" style="animation-delay: 0.5s">
            <div class="panel-header">
              <i class="el-icon-document-checked"></i>
              <span>最近识别记录</span>
            </div>
            <div class="panel-body">
              <div id="userRecentRecordChart" style="height: 280px;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部统计栏 -->
    <div class="footer-stats">
      <div class="stat-box">
        <i class="el-icon-view"></i>
        <div class="stat-info">
          <span class="stat-num">{{ visitCount }}</span>
          <span class="stat-name">访问量</span>
        </div>
      </div>
      <div class="stat-box">
        <i class="el-icon-s-marketing"></i>
        <div class="stat-info">
          <span class="stat-num">{{ dataCount }}</span>
          <span class="stat-name">数据量</span>
        </div>
      </div>
      <div class="stat-box" v-if="isAdmin1">
        <i class="el-icon-s-home"></i>
        <div class="stat-info">
          <span class="stat-num">{{ farmCount }}</span>
          <span class="stat-name">农场数</span>
        </div>
      </div>
      <div class="stat-box">
        <i class="el-icon-s-custom"></i>
        <div class="stat-info">
          <span class="stat-num">{{ userCount }}</span>
          <span class="stat-name">用户数</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getRecognitionStats, getUserRoleStats, getTaskStatusStats, getTotalRecognitionCount, getTodayRecognitionCount, getModelList, getSystemMonitor, getFarmStats } from '@/api/user'
import { getIdentifyRecords } from '@/api/identify'


const TASK_TYPES = {
  pest_disease: '病虫害图像识别',
  temperature_humidity: '温湿度预测',
  maturity_harvest: '成熟度分类与采摘点定位'
}

const TASK_TYPE_COLORS = {
  pest_disease: '#00539B',
  temperature_humidity: '#059669',
  maturity_harvest: '#F59E0B'
}

export default {
  name: 'DataPanel',
  data() {
    return {
      onlineUsers: 0,
      todayActive: 0,
      visitCount: 0,
      dataCount: 0,
      farmCount: 0,
      userCount: 0,
      currentTime: '',
      charts: {},
      recognitionStats: null,
      userRoleStats: null,
      taskStatusStats: null,
      farmStatsData: null,
      modelList: [],
      userRecords: [],
      memoryTimer: null,
      networkTimer: null,
      dataRefreshTimer: null,
      monitorTimer: null,
      memoryHistory: [],
      networkUploadHistory: [],
      networkDownloadHistory: [],
      maxDataPoints: 30,
      memoryUsed: 0,
      memoryPercent: 0,
      memoryMaxMB: 0,
      networkUploadSpeed: 0,
      networkDownloadSpeed: 0,
      serverMemoryHistory: [],
      lastNetworkTime: Date.now()
    }
  },
  computed: {
    panelTitle() {
      return '农业大数据联合建模平台'
    },
    isAdmin1() {
      return this.$store.state.user && this.$store.state.user.role === 1
    },
    isAdmin2() {
      return this.$store.state.user && this.$store.state.user.role === 2
    },
    isUser() {
      return this.$store.state.user && this.$store.state.user.role === 3
    },
    currentUserId() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          return user.id || user.userId || null
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }
      return null
    },
    memoryUsageClass() {
      if (this.memoryPercent >= 80) return 'danger'
      if (this.memoryPercent >= 60) return 'warning'
      return 'normal'
    }
  },
  mounted() {
    this.updateTime()
    setInterval(this.updateTime, 1000)
    this.loadRealData()
    this.startPeriodicDataRefresh()
    this.startServerMonitor()
  },
  beforeDestroy() {
    Object.values(this.charts).forEach(chart => chart && chart.dispose())
    if (this.memoryTimer) clearInterval(this.memoryTimer)
    if (this.networkTimer) clearInterval(this.networkTimer)
    if (this.dataRefreshTimer) clearInterval(this.dataRefreshTimer)
    if (this.monitorTimer) clearInterval(this.monitorTimer)
  },
  methods: {
    startServerMonitor() {
      this.fetchServerMonitor()
      this.monitorTimer = setInterval(() => {
        this.fetchServerMonitor()
      }, 2000)
    },

    async fetchServerMonitor() {
      try {
        const res = await getSystemMonitor()
        const data = res && res.data ? res.data : null
        if (data) {
          const mem = data.memory || {}
          const jvm = data.jvm || {}

          this.memoryUsed = mem.heapUsedMB || jvm.usedMemoryMB || 0
          this.memoryMaxMB = mem.heapMaxMB || jvm.maxMemoryMB || 0
          this.memoryPercent = mem.heapUsedPercent || (this.memoryMaxMB > 0 ? Math.round(this.memoryUsed / this.memoryMaxMB * 10000) / 100 : 0)

          this.memoryHistory.push(this.memoryPercent)
          if (this.memoryHistory.length > this.maxDataPoints) {
            this.memoryHistory.shift()
          }

          const osData = data.os || {}
          const loadAvg = osData.systemLoadAverage || 0
          const processors = osData.availableProcessors || 1
          this.networkDownloadSpeed = Math.max(0, Math.round(loadAvg * 100))
          this.networkUploadSpeed = Math.max(0, Math.round((processors - loadAvg) * 50))

          this.networkUploadHistory.push(this.networkUploadSpeed)
          this.networkDownloadHistory.push(this.networkDownloadSpeed)
          if (this.networkUploadHistory.length > this.maxDataPoints) {
            this.networkUploadHistory.shift()
          }
          if (this.networkDownloadHistory.length > this.maxDataPoints) {
            this.networkDownloadHistory.shift()
          }

          this.updateMemoryChart()
          this.updateNetworkChart()
        }
      } catch (e) {
        console.warn('获取服务器监控数据失败:', e.message)
      }
    },

    updateMemoryChart() {
      const chartKey = this.isUser ? 'userMemory' : 'admin2Resource'
      const chart = this.charts[chartKey]
      if (chart) {
        chart.setOption({
          series: [{ data: [...this.memoryHistory] }]
        })
      }
    },

    updateNetworkChart() {
      const chartKey = this.isUser ? 'userNetwork' : 'admin2Network'
      const chart = this.charts[chartKey]
      if (chart) {
        chart.setOption({
          series: [
            { data: [...this.networkUploadHistory] },
            { data: [...this.networkDownloadHistory] }
          ]
        })
      }
    },

    startPeriodicDataRefresh() {
      this.dataRefreshTimer = setInterval(async () => {
        try {
          if (this.isUser || this.isAdmin2) {
            await Promise.all([
              this.loadModelList(),
              this.loadUserRecords()
            ])
            this.refreshAllCharts()
          }
        } catch (e) {
          console.error('定期刷新数据失败', e)
        }
      }, 30000)
    },

    refreshAllCharts() {
      if (this.isUser) {
        if (this.charts.userModel) this.initUserModelChart()
        if (this.charts.userTask) this.initUserTaskChart()
        if (this.charts.userRecentRecord) this.initUserRecentRecordChart()
      } else if (this.isAdmin2) {
        if (this.charts.admin2Model) this.initAdmin2ModelChart()
        if (this.charts.admin2Task) this.initAdmin2TaskChart()
      }
    },

    async loadRealData() {
      try {
        const [recRes, roleRes, taskRes, totalRecRes, todayRecRes] = await Promise.all([
          getRecognitionStats().catch(() => null),
          getUserRoleStats().catch(() => null),
          getTaskStatusStats().catch(() => null),
          getTotalRecognitionCount().catch(() => null),
          getTodayRecognitionCount().catch(() => null)
        ])
        if (roleRes && roleRes.data) {
          this.userRoleStats = roleRes.data
          this.userCount = roleRes.data.total || 0
          this.onlineUsers = this.userCount
        }
        if (totalRecRes && totalRecRes.data !== undefined) {
          this.dataCount = totalRecRes.data
        }
        if (todayRecRes && todayRecRes.data !== undefined) {
          this.todayActive = todayRecRes.data
        }
        if (recRes && recRes.data) {
          this.recognitionStats = recRes.data
        }
        if (taskRes && taskRes.data) {
          this.taskStatusStats = taskRes.data
        }

        if (this.isAdmin1) {
          try {
            const farmRes = await getFarmStats()
            if (farmRes && farmRes.data) {
              this.farmStatsData = farmRes.data
              this.farmCount = farmRes.data.totalFarms || 0
            }
          } catch (e) {
            console.warn('加载农场统计数据失败', e)
          }
        }

        if (this.isUser || this.isAdmin2) {
          await Promise.all([
            this.loadModelList(),
            this.loadUserRecords()
          ])
        }
      } catch (e) {
        console.error('加载数据失败', e)
      }
      this.initCharts()
    },

    async loadModelList() {
      try {
        let allModels = []
        let page = 1
        let shouldContinue = true

        while (shouldContinue) {
          const res = await getModelList({ page, pageSize: 100 })
          if (res) {
            const pageData = res.data || res
            const records = pageData.records || pageData.list || (Array.isArray(pageData) ? pageData : [])
            allModels = allModels.concat(records)
            const total = pageData.total || 0
            if (allModels.length >= total || records.length === 0) {
              shouldContinue = false
            } else {
              page++
            }
          } else {
            shouldContinue = false
          }
        }
        this.modelList = allModels
      } catch (e) {
        console.error('加载模型列表失败', e)
      }
    },

    async loadUserRecords() {
      if (!this.currentUserId) return
      try {
        const res = await getIdentifyRecords({
          userId: this.currentUserId,
          page: 1,
          pageSize: 999
        })
        if (res && res.data) {
          const pageData = res.data
          this.userRecords = pageData.records || pageData.list || (Array.isArray(pageData) ? pageData : [])
        }
      } catch (e) {
        console.error('加载用户记录失败', e)
      }
    },

    getModelCountsByTaskType() {
      const counts = {
        pest_disease: 0,
        temperature_humidity: 0,
        maturity_harvest: 0,
        other: 0
      }
      this.modelList.forEach(model => {
        if (!model) return
        const name = (model.name || '').toLowerCase()
        const desc = (model.description || '').toLowerCase()
        const combined = name + ' ' + desc

        if (combined.includes('病虫害') || combined.includes('pest') || combined.includes('disease') || combined.includes('识别')) {
          counts.pest_disease++
        } else if (combined.includes('温湿度') || combined.includes('temperature') || combined.includes('humidity') || combined.includes('预测')) {
          counts.temperature_humidity++
        } else if (combined.includes('成熟') || combined.includes('采摘') || combined.includes('maturity') || combined.includes('harvest') || combined.includes('分类')) {
          counts.maturity_harvest++
        } else {
          counts.other++
        }
      })
      return counts
    },

    getUserTaskCounts() {
      const counts = {
        pest_disease: 0,
        temperature_humidity: 0,
        maturity_harvest: 0
      }
      this.userRecords.forEach(record => {
        const result = record.result || ''
        if (result.toLowerCase().includes('病虫害') || result.toLowerCase().includes('pest') || result.toLowerCase().includes('disease')) {
          counts.pest_disease++
        } else if (result.toLowerCase().includes('温度') || result.toLowerCase().includes('湿度') || result.toLowerCase().includes('temperature') || result.toLowerCase().includes('humidity')) {
          counts.temperature_humidity++
        } else if (result.toLowerCase().includes('成熟') || result.toLowerCase().includes('采摘') || result.toLowerCase().includes('maturity') || result.toLowerCase().includes('harvest')) {
          counts.maturity_harvest++
        } else {
          counts.pest_disease++
        }
      })
      return counts
    },

    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },

    initCharts() {
      this.$nextTick(() => {
        if (this.isAdmin1) {
          this.initAdmin1Charts()
        } else if (this.isAdmin2) {
          this.initAdmin2Charts()
        } else if (this.isUser) {
          this.initUserCharts()
        } else {
          this.initAdmin1Charts()
        }
      })
    },

    initAdmin1Charts() {
      this.initFarmStatsChart()
      this.initFarmDistributionChart()
      this.initDeviceStatsChart()
      this.initUserStatsChart()
      this.initFarmProductionChart()
      this.initWeeklyActivityChart()
    },

    initFarmStatsChart() {
      const chart = echarts.init(document.getElementById('farmStatsChart'))
      const farmData = this.farmStatsData || {}
      const farmList = farmData.farmList || []
      const totalFarms = farmData.totalFarms || 0
      const activeFarms = farmData.activeFarms || 0
      const inactiveFarms = totalFarms - activeFarms

      const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: '0%', textStyle: { color: '#475569' } },
        graphic: [{
          type: 'text',
          left: 'center',
          top: '38%',
          style: {
            text: totalFarms + '',
            fill: '#0F172A',
            fontSize: 28,
            fontWeight: 'bold',
            textAlign: 'center'
          }
        }, {
          type: 'text',
          left: 'center',
          top: '48%',
          style: {
            text: '农场总数',
            fill: '#94A3B8',
            fontSize: 12,
            textAlign: 'center'
          }
        }],
        series: [{
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['50%', '45%'],
          itemStyle: { borderRadius: 10, borderColor: '#ffffff', borderWidth: 2 },
          label: { show: true, color: '#475569', formatter: '{b}\n{c}个' },
          data: farmList.length > 0 ? [
            { value: activeFarms, name: '已认证农场', itemStyle: { color: '#059669' } },
            { value: inactiveFarms, name: '待认证农场', itemStyle: { color: '#F59E0B' } }
          ] : [{ value: 0, name: '暂无农场', itemStyle: { color: '#94A3B8' } }]
        }]
      }
      chart.setOption(option)
      this.charts.farmStats = chart
    },

    initFarmDistributionChart() {
      const chart = echarts.init(document.getElementById('farmDistributionChart'))
      const farmData = this.farmStatsData || {}
      const farmList = farmData.farmList || []

      if (farmList.length === 0) {
        const option = {
          title: {
            text: '农场地理分布',
            left: 'center',
            top: '5%',
            textStyle: { color: '#0F172A', fontSize: 16, fontWeight: 'bold' }
          },
          graphic: [{
            type: 'text',
            left: 'center',
            top: 'middle',
            style: { text: '暂无农场数据\n请等待二级管理员注册并填写位置信息', fill: '#94A3B8', fontSize: 14, textAlign: 'center', lineHeight: 24 }
          }]
        }
        chart.setOption(option)
        this.charts.farmDistribution = chart
        return
      }

      const locationData = farmList.map(f => ({
        name: f.name || '未命名',
        location: [f.province, f.city, f.address].filter(Boolean).join(' ') || '未填写地址',
        memberCount: f.memberCount || 0
      }))

      const option = {
        title: {
          text: '农场地理分布',
          left: 'center',
          top: '2%',
          textStyle: { color: '#0F172A', fontSize: 16, fontWeight: 'bold' }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: (params) => {
            const idx = params[0].dataIndex
            const item = locationData[idx]
            return item.name + '<br/>' + item.location + '<br/>成员数: <b>' + item.memberCount + '</b>'
          }
        },
        grid: {
          left: '3%',
          right: '15%',
          bottom: '8%',
          top: '12%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          axisLabel: { color: '#475569' },
          splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } }
        },
        yAxis: {
          type: 'category',
          data: locationData.map(f => f.name),
          axisLabel: { color: '#475569', fontSize: 12, width: 100, overflow: 'truncate' }
        },
        series: [{
          type: 'bar',
          data: locationData.map((f) => ({
            value: f.memberCount,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#00539B' },
                { offset: 1, color: '#0EA5E9' }
              ]),
              borderRadius: [0, 6, 6, 0]
            }
          })),
          barWidth: '55%',
          label: {
            show: true,
            position: 'right',
            color: '#475569',
            fontSize: 13,
            fontWeight: 'bold',
            formatter: (p) => p.value + ' 人'
          }
        }]
      }
      chart.setOption(option)
      this.charts.farmDistribution = chart
    },

    initDeviceStatsChart() {
      const chart = echarts.init(document.getElementById('deviceStatsChart'))
      const taskDist = (this.taskStatusStats && this.taskStatusStats.statusDistribution) || {}
      const statusNames = { PENDING: '待开始', RUNNING: '进行中', COMPLETED: '已完成', FAILED: '已失败' }
      const statusColors = { PENDING: '#F59E0B', RUNNING: '#00539B', COMPLETED: '#059669', FAILED: '#EF4444' }
      const categories = Object.keys(taskDist).length > 0 ? Object.keys(taskDist).map(k => statusNames[k] || k) : ['暂无数据']
      const values = Object.keys(taskDist).length > 0 ? Object.values(taskDist) : [0]
      const barColors = Object.keys(taskDist).length > 0
        ? Object.keys(taskDist).map(k => statusColors[k] || '#94A3B8')
        : ['#94A3B8']
      const option = {
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: categories, axisLabel: { color: '#475569' } },
        yAxis: { type: 'value', axisLabel: { color: '#475569' }, splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } } },
        series: [{
          type: 'bar',
          data: values.map((v, i) => ({ value: v, itemStyle: { color: barColors[i] } })),
          barWidth: '50%'
        }]
      }
      chart.setOption(option)
      this.charts.deviceStats = chart
    },

    initUserStatsChart() {
      const chart = echarts.init(document.getElementById('userStatsChart'))
      const roleStats = this.userRoleStats || { admin1Count: 0, admin2Count: 0, userCount: 0 }
      const option = {
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie', radius: ['30%', '65%'], center: ['50%', '50%'],
          roseType: 'radius', itemStyle: { borderRadius: 5 },
          data: [
            { value: roleStats.admin1Count || 0, name: '一级管理员', itemStyle: { color: '#00539B' } },
            { value: roleStats.admin2Count || 0, name: '二级管理员', itemStyle: { color: '#0EA5E9' } },
            { value: roleStats.userCount || 0, name: '普通用户', itemStyle: { color: '#94A3B8' } }
          ].filter(d => d.value > 0),
          label: { color: '#475569' }
        }]
      }
      chart.setOption(option)
      this.charts.userStats = chart
    },

    initFarmProductionChart() {
      const chart = echarts.init(document.getElementById('farmProductionChart'))
      const dailyCount = (this.recognitionStats && this.recognitionStats.dailyCount) || {}
      const dates = Object.keys(dailyCount).length > 0 ? Object.keys(dailyCount) : ['暂无']
      const values = Object.keys(dailyCount).length > 0 ? Object.values(dailyCount) : [0]
      const option = {
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: dates, axisLabel: { color: '#475569' } },
        yAxis: { type: 'value', axisLabel: { color: '#475569' }, splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } } },
        series: [{
          type: 'line', data: values, smooth: true,
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,83,155,0.2)' }, { offset: 1, color: 'rgba(0,83,155,0)' }]) },
          itemStyle: { color: '#00539B' }
        }]
      }
      chart.setOption(option)
      this.charts.farmProduction = chart
    },

    initWeeklyActivityChart() {
      const chart = echarts.init(document.getElementById('weeklyActivityChart'))
      const dailyCount = (this.recognitionStats && this.recognitionStats.dailyCount) || {}
      const dates = Object.keys(dailyCount).length > 0 ? Object.keys(dailyCount) : ['暂无']
      const values = Object.keys(dailyCount).length > 0 ? Object.values(dailyCount) : [0]
      const option = {
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: dates, axisLabel: { color: '#475569' } },
        yAxis: { type: 'value', axisLabel: { color: '#475569' }, splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } } },
        series: [{ type: 'bar', data: values, itemStyle: { color: '#00539B' }, barWidth: '60%' }]
      }
      chart.setOption(option)
      this.charts.weeklyActivity = chart
    },

    initAdmin2Charts() {
      this.initAdmin2ModelChart()
      this.initAdmin2TaskChart()
      this.initAdmin2ResourceChart()
      this.initAdmin2TrendChart()
      this.initAdmin2NetworkChart()
    },

    initAdmin2ModelChart() {
      const chart = echarts.init(document.getElementById('admin2ModelChart'))
      const modelCounts = this.getModelCountsByTaskType()
      const data = Object.entries(TASK_TYPES).map(([key, label]) => ({
        name: label,
        value: modelCounts[key],
        itemStyle: { color: TASK_TYPE_COLORS[key] }
      }))
      const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c} 个模型 ({d}%)' },
        legend: { bottom: '0%', textStyle: { color: '#475569' } },
        series: [{
          type: 'pie',
          radius: ['35%', '65%'],
          center: ['50%', '45%'],
          itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
          label: { color: '#475569', formatter: '{b}\n{c}个' },
          data: data.filter(d => d.value > 0).length > 0 ? data.filter(d => d.value > 0) : [{ name: '暂无数据', value: 0, itemStyle: { color: '#94A3B8' } }]
        }]
      }
      chart.setOption(option)
      this.charts.admin2Model = chart
    },

    initAdmin2TaskChart() {
      const chart = echarts.init(document.getElementById('admin2TaskChart'))
      const taskCounts = this.getUserTaskCounts()
      const categories = Object.values(TASK_TYPES)
      const values = Object.values(taskCounts)
      const colors = Object.values(TASK_TYPE_COLORS)
      const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
        xAxis: { type: 'value', axisLabel: { color: '#475569' }, splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } } },
        yAxis: { type: 'category', data: categories, axisLabel: { color: '#475569', interval: 0 } },
        series: [{
          type: 'bar',
          data: values.map((v, i) => ({ value: v, itemStyle: { color: colors[i], borderRadius: [0, 6, 6, 0] } })),
          barWidth: '50%',
          label: { show: true, position: 'right', color: '#475569', fontWeight: 'bold' }
        }]
      }
      chart.setOption(option)
      this.charts.admin2Task = chart
    },

    initAdmin2ResourceChart() {
      const chart = echarts.init(document.getElementById('admin2ResourceChart'))
      const option = {
        tooltip: { trigger: 'axis', formatter: (data) => data.length > 0 ? `${data[0].name}<br/>JVM内存使用率: <b>${data[0].value}%</b>` : '' },
        grid: { left: '3%', right: '4%', bottom: '10%', top: '10%', containLabel: true },
        xAxis: { type: 'category', data: Array.from({ length: this.maxDataPoints }, (_, i) => `${i * 2}s`), axisLabel: { color: '#475569', interval: 4 } },
        yAxis: { type: 'value', min: 0, max: 100, axisLabel: { color: '#475569', formatter: '{value}%' }, splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } } },
        series: [{
          type: 'line',
          data: [...this.memoryHistory],
          smooth: true,
          symbol: 'none',
          lineStyle: { color: '#00539B', width: 2 },
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,83,155,0.25)' }, { offset: 1, color: 'rgba(0,83,155,0)' }]) },
          markLine: {
            silent: true,
            data: [
              { yAxis: 80, lineStyle: { color: '#EF4444', type: 'dashed' }, label: { formatter: '警戒线', color: '#EF4444' } },
              { yAxis: 60, lineStyle: { color: '#F59E0B', type: 'dashed' }, label: { formatter: '警告线', color: '#F59E0B' } }
            ]
          }
        }]
      }
      chart.setOption(option)
      this.charts.admin2Resource = chart
    },

    initAdmin2TrendChart() {
      const chart = echarts.init(document.getElementById('admin2TrendChart'))
      const dailyCount = (this.recognitionStats && this.recognitionStats.dailyCount) || {}
      const dates = Object.keys(dailyCount).length > 0 ? Object.keys(dailyCount).slice(-7) : []
      const values = dates.map(k => dailyCount[k])
      const option = {
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '10%', top: '10%', containLabel: true },
        xAxis: { type: 'category', data: dates, axisLabel: { color: '#475569', rotate: 20 } },
        yAxis: { type: 'value', axisLabel: { color: '#475569' }, splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } } },
        series: [{
          type: 'line',
          data: values,
          smooth: true,
          itemStyle: { color: '#00539B' },
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,83,155,0.2)' }, { offset: 1, color: 'rgba(0,83,155,0)' }]) }
        }]
      }
      chart.setOption(option)
      this.charts.admin2Trend = chart
    },

    initAdmin2NetworkChart() {
      const chart = echarts.init(document.getElementById('admin2NetworkChart'))
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: (params) => {
            if (!params || params.length === 0) return ''
            let str = params[0].axisValue
            params.forEach(p => {
              str += `<br/><span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${p.color};margin-right:5px;"></span>`
              str += `${p.seriesName}: <b>${p.value}</b> (负载指标)`
            })
            return str
          }
        },
        legend: { bottom: '0%', textStyle: { color: '#475569' }, data: ['下行负载', '上行负载'] },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
        xAxis: { type: 'category', data: Array.from({ length: this.maxDataPoints }, (_, i) => `${i * 2}s`), axisLabel: { color: '#475569', interval: 4 } },
        yAxis: { type: 'value', axisLabel: { color: '#475569' }, splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } } },
        series: [
          {
            name: '上行负载',
            type: 'line',
            data: [...this.networkUploadHistory],
            smooth: true,
            symbol: 'none',
            lineStyle: { color: '#059669', width: 2 },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(5,150,105,0.2)' }, { offset: 1, color: 'rgba(5,150,105,0)' }]) }
          },
          {
            name: '下行负载',
            type: 'line',
            data: [...this.networkDownloadHistory],
            smooth: true,
            symbol: 'none',
            lineStyle: { color: '#00539B', width: 2 },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,83,155,0.15)' }, { offset: 1, color: 'rgba(0,83,155,0)' }]) }
          }
        ]
      }
      chart.setOption(option)
      this.charts.admin2Network = chart
    },

    initUserCharts() {
      this.initUserModelChart()
      this.initUserTaskChart()
      this.initUserMemoryChart()
      this.initUserNetworkChart()
      this.initUserRecentRecordChart()
    },

    initUserModelChart() {
      const chart = echarts.init(document.getElementById('userModelChart'))
      const modelCounts = this.getModelCountsByTaskType()
      const totalCount = this.modelList.length
      const data = Object.entries(TASK_TYPES).map(([key, label]) => ({
        name: label,
        value: modelCounts[key] || 0,
        itemStyle: { color: TASK_TYPE_COLORS[key] }
      }))
      const hasData = data.some(d => d.value > 0)
      
      if (!hasData) {
        const option = {
          graphic: [{
            type: 'text',
            left: 'center',
            top: '40%',
            style: {
              text: '暂无可用模型',
              fill: '#94A3B8',
              fontSize: 16,
              textAlign: 'center'
            }
          }, {
            type: 'text',
            left: 'center',
            top: '50%',
            style: {
              text: '请联系管理员分配模型',
              fill: '#CBD5E1',
              fontSize: 12,
              textAlign: 'center'
            }
          }]
        }
        chart.setOption(option)
        this.charts.userModel = chart
        return
      }

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params) => `${params.name}<br/><b>${params.value}</b> 个模型 (${params.percent}%)`
        },
        legend: {
          orient: 'horizontal',
          bottom: '0%',
          left: 'center',
          itemWidth: 12,
          itemHeight: 12,
          itemGap: 15,
          textStyle: { color: '#475569', fontSize: 12 }
        },
        graphic: [{
          type: 'text',
          left: 'center',
          top: '38%',
          style: {
            text: totalCount + '',
            fill: '#0F172A',
            fontSize: 24,
            fontWeight: 'bold',
            textAlign: 'center'
          }
        }, {
          type: 'text',
          left: 'center',
          top: '47%',
          style: {
            text: '可用模型',
            fill: '#94A3B8',
            fontSize: 12,
            textAlign: 'center'
          }
        }],
        series: [{
          type: 'pie',
          radius: ['35%', '60%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 3
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}\n{c}个',
            color: '#475569',
            fontSize: 11,
            lineHeight: 16
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              color: '#0F172A'
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.2)'
            }
          },
          data: data.filter(d => d.value > 0)
        }]
      }
      chart.setOption(option)
      this.charts.userModel = chart
    },

    initUserTaskChart() {
      const chart = echarts.init(document.getElementById('userTaskChart'))
      const taskCounts = this.getUserTaskCounts()
      const categories = Object.values(TASK_TYPES)
      const values = Object.values(taskCounts)
      const colors = Object.values(TASK_TYPE_COLORS)
      const total = values.reduce((a, b) => a + b, 0)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: (params) => params[0].name + '<br/>' + `<b>${params[0].value}</b>` + ' 次提交'
        },
        grid: { left: '3%', right: '8%', bottom: '12%', top: '15%', containLabel: true },
        title: {
          text: `累计 ${total} 次任务`,
          left: 'center',
          top: '5%',
          textStyle: { color: '#475569', fontSize: 14, fontWeight: 'normal' }
        },
        xAxis: {
          type: 'value',
          axisLabel: { color: '#475569' },
          splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } }
        },
        yAxis: {
          type: 'category',
          data: categories,
          axisLabel: { color: '#475569', fontSize: 12, interval: 0 }
        },
        series: [{
          type: 'bar',
          data: values.map((v, i) => ({
            value: v,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: colors[i] },
                { offset: 1, color: colors[i] + '99' }
              ]),
              borderRadius: [0, 8, 8, 0]
            }
          })),
          barWidth: '55%',
          label: {
            show: true,
            position: 'right',
            color: '#0F172A',
            fontWeight: 'bold',
            fontSize: 14,
            formatter: '{c}'
          }
        }]
      }
      chart.setOption(option)
      this.charts.userTask = chart
    },

    initUserMemoryChart() {
      const chart = echarts.init(document.getElementById('userMemoryChart'))
      const option = {
        tooltip: { trigger: 'axis', formatter: (data) => data.length > 0 ? `${data[0].name}<br/>JVM内存使用率: <b>${data[0].value}%</b>` : '' },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
        xAxis: {
          type: 'category',
          data: Array.from({ length: this.maxDataPoints }, (_, i) => `${i * 2}s`),
          axisLabel: { color: '#475569', interval: 4, fontSize: 11 }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: { color: '#475569', formatter: '{value}%' },
          splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } }
        },
        series: [{
          type: 'line',
          data: [...this.memoryHistory],
          smooth: true,
          symbol: 'none',
          lineStyle: { color: '#00539B', width: 2.5 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0,83,155,0.3)' },
              { offset: 0.7, color: 'rgba(0,83,155,0.08)' },
              { offset: 1, color: 'rgba(0,83,155,0)' }
            ])
          },
          markLine: {
            silent: true,
            symbol: 'none',
            lineStyle: { type: 'dashed', width: 1.5 },
            data: [
              { yAxis: 80, lineStyle: { color: '#EF4444' }, label: { show: true, position: 'insideEndTop', formatter: '警戒 80%', color: '#EF4444', fontSize: 11 } },
              { yAxis: 60, lineStyle: { color: '#F59E0B' }, label: { show: true, position: 'insideEndTop', formatter: '警告 60%', color: '#F59E0B', fontSize: 11 } }
            ]
          },
          markArea: {
            silent: true,
            data: [[{ yAxis: 80, itemStyle: { color: 'rgba(239,68,68,0.06)' } }, { yAxis: 100, itemStyle: { color: 'rgba(239,68,68,0.08)' } }]]
          }
        }]
      }
      chart.setOption(option)
      this.charts.userMemory = chart
    },

    initUserNetworkChart() {
      const chart = echarts.init(document.getElementById('userNetworkChart'))
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: (params) => {
            if (!params || params.length === 0) return ''
            let str = params[0].axisValue
            params.forEach(p => {
              str += `<br/><span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${p.color};margin-right:5px;"></span>`
              str += `${p.seriesName}: <b>${p.value}</b> (负载指标)`
            })
            return str
          }
        },
        legend: { bottom: '2%', textStyle: { color: '#475569' }, data: ['上行负载', '下行负载'], itemWidth: 20, itemHeight: 10 },
        grid: { left: '3%', right: '4%', bottom: '14%', top: '8%', containLabel: true },
        xAxis: {
          type: 'category',
          data: Array.from({ length: this.maxDataPoints }, (_, i) => `${i * 2}s`),
          axisLabel: { color: '#475569', interval: 4, fontSize: 11 }
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: '#475569' },
          splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } },
          name: '负载',
          nameTextStyle: { color: '#94A3B8' }
        },
        series: [
          {
            name: '上行负载',
            type: 'line',
            data: [...this.networkUploadHistory],
            smooth: true,
            symbol: 'none',
            lineStyle: { color: '#059669', width: 2 },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(5,150,105,0.2)' }, { offset: 1, color: 'rgba(5,150,105,0)' }]) }
          },
          {
            name: '下行负载',
            type: 'line',
            data: [...this.networkDownloadHistory],
            smooth: true,
            symbol: 'none',
            lineStyle: { color: '#00539B', width: 2 },
            areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,83,155,0.15)' }, { offset: 1, color: 'rgba(0,83,155,0)' }]) }
          }
        ]
      }
      chart.setOption(option)
      this.charts.userNetwork = chart
    },

    initUserRecentRecordChart() {
      const chart = echarts.init(document.getElementById('userRecentRecordChart'))
      const recentRecords = this.userRecords.slice(-10).reverse()
      const dates = recentRecords.map(r => {
        const d = r.createdAt ? new Date(r.createdAt) : new Date()
        return `${d.getMonth()+1}/${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
      })
      const confidences = recentRecords.map(r => parseFloat(r.confidence) * 100 || 0)
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: (params) => {
            const idx = params[0].dataIndex
            const record = recentRecords[idx]
            let info = params[0].axisValue + '<br/>'
            info += `置信度: <b>${params[0].value.toFixed(1)}%</b><br/>`
            if (record) info += `结果: ${record.result || '-'}`
            return info
          }
        },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
        xAxis: {
          type: 'category',
          data: dates.length > 0 ? dates : ['暂无数据'],
          axisLabel: { color: '#475569', rotate: 20, fontSize: 10 }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: { color: '#475569', formatter: '{value}%' },
          splitLine: { lineStyle: { color: '#E2E8F0', type: 'dashed' } }
        },
        series: [{
          type: 'bar',
          data: confidences.length > 0 ? confidences.map(v => ({
            value: v,
            itemStyle: {
              color: v >= 90 ? '#059669' : v >= 70 ? '#F59E0B' : '#EF4444',
              borderRadius: [4, 4, 0, 0]
            }
          })) : [0],
          barWidth: '45%',
          label: { show: confidences.length <= 5, position: 'top', color: '#475569', fontSize: 10, formatter: '{c}%' }
        }]
      }
      chart.setOption(option)
      this.charts.userRecentRecord = chart
    },

  }
}
</script>

<style scoped>
.data-panel-container {
  min-height: 100vh;
  background-color: #F8FAFC;
  position: relative;
}

.dynamic-bg { position: absolute; inset: 0; pointer-events: none; }

.bg-gradient {
  width: 100%; height: 100%;
  background: radial-gradient(ellipse at 20% 20%, rgba(0,83,155,0.03) 0%, transparent 50%),
              radial-gradient(ellipse at 80% 80%, rgba(5,150,105,0.03) 0%, transparent 50%);
}

.header-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 40px;
  background: #ffffff;
  box-shadow: var(--diffused-shadow);
  border-bottom: 1px solid #E2E8F0;
  position: relative; z-index: 10;
}

.header-center h1 { color: #0F172A; font-size: 28px; margin: 0; }

.subtitle { color: #475569; font-size: 14px; margin: 5px 0 0 0; }

.stat-item { text-align: center; }
.stat-label { display: block; color: #475569; font-size: 12px; }
.stat-value { color: #00539B; font-size: 24px; font-weight: bold; }
.pulse { animation: pulse 2s infinite; }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }

.main-content { padding: 20px; position: relative; z-index: 5; }
.row { display: flex; gap: 20px; margin-bottom: 20px; }

.user-row { display: flex; gap: 20px; margin-bottom: 20px; }

.user-panel {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: var(--diffused-shadow);
  overflow-y: auto; overflow-x: hidden;
  flex: 1;
  min-width: 0;
  transition: transform 0.3s, box-shadow 0.3s;
}

.user-panel:hover { transform: translateY(-4px); box-shadow: 0 30px 60px -12px rgba(0,0,0,0.08); }

.panel {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: var(--diffused-shadow);
  overflow-y: auto; overflow-x: hidden; flex: 1;
  transition: transform 0.3s, box-shadow 0.3s;
}

.panel:hover { transform: translateY(-4px); box-shadow: 0 30px 60px -12px rgba(0,0,0,0.08); }
.main-panel { flex: 2; }

.panel-header {
  padding: 15px 20px;
  background: #F8FAFC;
  border-bottom: 1px solid #F1F5F9;
  display: flex; align-items: center; gap: 10px;
  color: #0F172A; font-weight: bold;
}

.panel-header i { font-size: 18px; color: #00539B; }
.panel-body { 
  padding: 15px; 
  position: relative;
  min-height: 300px;
  display: flex;
  flex-direction: column;
}

.panel-body > div {
  flex: 1;
  min-height: 250px;
}

.fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.slide-up { animation: slideUp 0.5s ease-out both; }
@keyframes slideUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }

.footer-stats {
  display: flex; justify-content: center; gap: 40px;
  padding: 30px;
  background: #ffffff;
  box-shadow: var(--diffused-shadow);
  border-top: 1px solid #E2E8F0;
  position: relative; z-index: 10;
}

.stat-box {
  display: flex; align-items: center; gap: 15px;
  padding: 15px 30px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: var(--diffused-shadow);
  transition: transform 0.3s;
}

.stat-box:hover { transform: scale(1.05); }
.stat-box i { font-size: 30px; color: #00539B; }
.stat-info { display: flex; flex-direction: column; }
.stat-num { color: #0F172A; font-size: 24px; font-weight: bold; }
.stat-name { color: #475569; font-size: 12px; }

.memory-realtime-info {
  display: flex; justify-content: space-around;
  padding: 12px 0 0 0;
  border-top: 1px solid #F1F5F9;
  margin-top: 10px;
}

.memory-stat { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.memory-label { color: #94A3B8; font-size: 12px; }
.memory-value { font-size: 22px; font-weight: bold; color: #0F172A; }
.memory-value.normal { color: #059669; }
.memory-value.warning { color: #F59E0B; }
.memory-value.danger { color: #EF4444; }

.network-realtime-info {
  display: flex; justify-content: space-around;
  padding: 12px 0 0 0;
  border-top: 1px solid #F1F5F9;
  margin-top: 10px;
}

.network-stat { display: flex; align-items: center; gap: 8px; }
.network-stat i { font-size: 20px; }
.upload-stat i { color: #059669; }
.download-stat i { color: #00539B; }
.network-label { color: #94A3B8; font-size: 12px; }
.network-value { font-size: 18px; font-weight: bold; color: #0F172A; }

@media (max-width: 1200px) { .row, .user-row { flex-wrap: wrap; } .panel, .user-panel { min-width: calc(50% - 10px); } .main-panel { flex: 1; } }
@media (max-width: 768px) { .header-bar { flex-direction: column; gap: 15px; } .panel, .user-panel { min-width: 100%; } .footer-stats { flex-wrap: wrap; } }
</style>
