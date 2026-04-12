<template>
  <div class="data-panel-container">
    <!-- 动态背景 -->
    <div class="dynamic-bg">
      <div class="bg-gradient"></div>
    </div>
    
    <!-- 顶部标题栏 -->
    <div class="header-bar">
      <div class="header-left">
        <div class="stat-item">
          <span class="stat-label">在线用户</span>
          <span class="stat-value pulse">{{ onlineUsers }}</span>
        </div>
      </div>
      <div class="header-center">
        <h1 class="title-animate">{{ panelTitle }}</h1>
        <p class="subtitle">{{ currentTime }}</p>
      </div>
      <div class="header-right">
        <div class="stat-item">
          <span class="stat-label">今日活跃</span>
          <span class="stat-value">{{ todayActive }}</span>
        </div>
      </div>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <div v-if="isAdmin1" class="fade-in">
        <!-- 第一行 -->
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
              <i class="el-icon-map-location"></i>
              <span>农场分布</span>
            </div>
            <div class="panel-body">
              <div id="chinaMap" style="height: 400px;"></div>
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
        
        <!-- 第二行 -->
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
      <div class="stat-box">
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
import { getUserList, getRecognitionStats, getUserRoleStats, getTaskStatusStats, getTotalRecognitionCount, getTodayRecognitionCount } from '@/api/user'

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
      taskStatusStats: null
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
    }
  },
  mounted() {
    this.updateTime()
    setInterval(this.updateTime, 1000)
    this.loadRealData()
  },
  beforeDestroy() {
    Object.values(this.charts).forEach(chart => chart && chart.dispose())
  },
  methods: {
    async loadRealData() {
      try {
        const [userRes, recRes, roleRes, taskRes, totalRecRes, todayRecRes] = await Promise.all([
          getUserList().catch(() => null),
          getRecognitionStats().catch(() => null),
          getUserRoleStats().catch(() => null),
          getTaskStatusStats().catch(() => null),
          getTotalRecognitionCount().catch(() => null),
          getTodayRecognitionCount().catch(() => null)
        ])
        if (userRes && userRes.data && userRes.data.data) {
          const list = userRes.data.data.list || userRes.data.data
          this.userCount = Array.isArray(list) ? list.length : 0
          this.onlineUsers = this.userCount
        }
        if (totalRecRes && totalRecRes.data && totalRecRes.data.data !== undefined) {
          this.dataCount = totalRecRes.data.data
        }
        if (todayRecRes && todayRecRes.data && todayRecRes.data.data !== undefined) {
          this.todayActive = todayRecRes.data.data
        }
        if (recRes && recRes.data && recRes.data.data) {
          this.recognitionStats = recRes.data.data
        }
        if (roleRes && roleRes.data && roleRes.data.data) {
          this.userRoleStats = roleRes.data.data
        }
        if (taskRes && taskRes.data && taskRes.data.data) {
          this.taskStatusStats = taskRes.data.data
        }
      } catch (e) {
        console.error('加载数据失败', e)
      }
      this.initCharts()
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
          // 默认显示一级管理员图表
          this.initAdmin1Charts()
        }
      })
    },
    
    initAdmin1Charts() {
      this.initFarmStatsChart()
      this.initChinaMap()
      this.initDeviceStatsChart()
      this.initUserStatsChart()
      this.initFarmProductionChart()
      this.initWeeklyActivityChart()
    },
    
    initFarmStatsChart() {
      const chart = echarts.init(document.getElementById('farmStatsChart'))
      const dist = (this.recognitionStats && this.recognitionStats.diseaseDistribution) || {}
      const colors = ['#67C23A', '#E6A23C', '#409EFF', '#F56C6C', '#909399']
      const chartData = Object.entries(dist).length > 0
        ? Object.entries(dist).map(([name, value], i) => ({ value, name, itemStyle: { color: colors[i % colors.length] } }))
        : [{ value: 0, name: '暂无数据', itemStyle: { color: '#909399' } }]
      const option = {
        tooltip: { trigger: 'item' },
        legend: { bottom: '0%', textStyle: { color: '#fff' } },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '45%'],
          itemStyle: { borderRadius: 10, borderColor: '#1a1a2e', borderWidth: 2 },
          data: chartData
        }]
      }
      chart.setOption(option)
      this.charts.farmStats = chart
    },
    
    initChinaMap() {
      const chart = echarts.init(document.getElementById('chinaMap'))
      const mapUrl = 'https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json'
      
      fetch(mapUrl)
        .then(response => {
          if (!response.ok) throw new Error('Network response was not ok')
          return response.json()
        })
        .then(data => {
          echarts.registerMap('china', data)
          const option = {
            tooltip: { 
              trigger: 'item',
              formatter: '{b}<br/>产量: {c}吨'
            },
            visualMap: {
              min: 0,
              max: 100,
              text: ['高', '低'],
              realtime: false,
              calculable: true,
              inRange: { color: ['#1a3a5c', '#2d5a7b', '#3d7a9a', '#4d9ab9', '#5dbad8'] },
              textStyle: { color: '#fff' }
            },
            series: [{
              type: 'map',
              map: 'china',
              roam: true,
              label: { 
                show: true, 
                color: '#fff', 
                fontSize: 11 
              },
              itemStyle: { 
                areaColor: '#1a3a5c', 
                borderColor: '#3d7a9a', 
                borderWidth: 1 
              },
              emphasis: { 
                label: { show: true, color: '#fff', fontSize: 12 }, 
                itemStyle: { areaColor: '#e94560' } 
              },
              data: []
            }]
          }
          chart.setOption(option)
        })
        .catch(err => {
          console.error('地图加载失败:', err)
          chart.setOption({
            graphic: [{
              type: 'group',
              children: [
                { type: 'text', style: { text: '🗺️', x: 180, y: 150, fontSize: 40 } },
                { type: 'text', style: { text: '地图加载失败', x: 150, y: 200, fill: '#fff', fontSize: 16 } }
              ]
            }]
          })
        })
      this.charts.chinaMap = chart
    },
    
    initDeviceStatsChart() {
      const chart = echarts.init(document.getElementById('deviceStatsChart'))
      const taskDist = (this.taskStatusStats && this.taskStatusStats.statusDistribution) || {}
      const statusNames = { PENDING: '待开始', RUNNING: '进行中', COMPLETED: '已完成', FAILED: '已失败' }
      const statusColors = { PENDING: '#e6a23c', RUNNING: '#409eff', COMPLETED: '#67c23a', FAILED: '#f56c6c' }
      const categories = Object.keys(taskDist).length > 0 ? Object.keys(taskDist).map(k => statusNames[k] || k) : ['暂无数据']
      const values = Object.keys(taskDist).length > 0 ? Object.values(taskDist) : [0]
      const barColors = Object.keys(taskDist).length > 0
        ? Object.keys(taskDist).map(k => new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: statusColors[k] || '#e94560' }, { offset: 1, color: '#0f3460' }]))
        : [new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#909399' }, { offset: 1, color: '#0f3460' }])]
      const option = {
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: categories, axisLabel: { color: '#fff' } },
        yAxis: { type: 'value', axisLabel: { color: '#fff' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
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
            { value: roleStats.admin1Count || 0, name: '一级管理员', itemStyle: { color: '#e94560' } },
            { value: roleStats.admin2Count || 0, name: '二级管理员', itemStyle: { color: '#0f3460' } },
            { value: roleStats.userCount || 0, name: '普通用户', itemStyle: { color: '#16213e' } }
          ].filter(d => d.value > 0),
          label: { color: '#fff' }
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
        xAxis: { type: 'category', data: dates, axisLabel: { color: '#fff' } },
        yAxis: { type: 'value', axisLabel: { color: '#fff' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
        series: [{
          type: 'line', data: values, smooth: true,
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(233,69,96,0.5)' }, { offset: 1, color: 'rgba(233,69,96,0)' }]) },
          itemStyle: { color: '#e94560' }
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
        xAxis: { type: 'category', data: dates, axisLabel: { color: '#fff' } },
        yAxis: { type: 'value', axisLabel: { color: '#fff' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
        series: [{ type: 'bar', data: values, itemStyle: { color: '#409EFF' }, barWidth: '60%' }]
      }
      chart.setOption(option)
      this.charts.weeklyActivity = chart
    },
    
    initAdmin2Charts() { this.initAdmin1Charts() },
    initUserCharts() { this.initAdmin1Charts() }
  }
}
</script>

<style scoped>
.data-panel-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a1a 0%, #1a1a2e 50%, #16213e 100%);
  position: relative;
}

.dynamic-bg { position: absolute; inset: 0; pointer-events: none; }

.bg-gradient {
  width: 100%; height: 100%;
  background: radial-gradient(ellipse at 20% 20%, rgba(233,69,96,0.1) 0%, transparent 50%),
              radial-gradient(ellipse at 80% 80%, rgba(64,158,255,0.1) 0%, transparent 50%);
}

.header-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 40px;
  background: rgba(0,0,0,0.3); backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255,255,255,0.1);
  position: relative; z-index: 10;
}

.header-center h1 { color: #fff; font-size: 28px; margin: 0; text-shadow: 0 0 20px rgba(233,69,96,0.5); }
.title-animate { animation: glow 2s ease-in-out infinite alternate; }

@keyframes glow {
  from { text-shadow: 0 0 10px rgba(233,69,96,0.5); }
  to { text-shadow: 0 0 30px rgba(233,69,96,0.8); }
}

.subtitle { color: rgba(255,255,255,0.6); font-size: 14px; margin: 5px 0 0 0; }

.stat-item { text-align: center; }
.stat-label { display: block; color: rgba(255,255,255,0.6); font-size: 12px; }
.stat-value { color: #e94560; font-size: 24px; font-weight: bold; }
.pulse { animation: pulse 2s infinite; }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }

.main-content { padding: 20px; position: relative; z-index: 5; }
.row { display: flex; gap: 20px; margin-bottom: 20px; }

.panel {
  background: rgba(255,255,255,0.05);
  border-radius: 15px;
  border: 1px solid rgba(255,255,255,0.1);
  overflow: hidden; flex: 1;
  transition: transform 0.3s, box-shadow 0.3s;
}

.panel:hover { transform: translateY(-5px); box-shadow: 0 10px 30px rgba(233,69,96,0.2); }
.main-panel { flex: 2; }

.panel-header {
  padding: 15px 20px;
  background: linear-gradient(90deg, rgba(233,69,96,0.2), rgba(64,158,255,0.2));
  border-bottom: 1px solid rgba(255,255,255,0.1);
  display: flex; align-items: center; gap: 10px;
  color: #fff; font-weight: bold;
}

.panel-header i { font-size: 18px; color: #e94560; }
.panel-body { padding: 15px; }

.fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.slide-up { animation: slideUp 0.5s ease-out both; }
@keyframes slideUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }

.footer-stats {
  display: flex; justify-content: center; gap: 40px;
  padding: 30px;
  background: rgba(0,0,0,0.3);
  border-top: 1px solid rgba(255,255,255,0.1);
  position: relative; z-index: 10;
}

.stat-box {
  display: flex; align-items: center; gap: 15px;
  padding: 15px 30px;
  background: linear-gradient(135deg, rgba(233,69,96,0.1), rgba(64,158,255,0.1));
  border-radius: 15px; border: 1px solid rgba(255,255,255,0.1);
  transition: transform 0.3s;
}

.stat-box:hover { transform: scale(1.05); }
.stat-box i { font-size: 30px; color: #e94560; }
.stat-info { display: flex; flex-direction: column; }
.stat-num { color: #fff; font-size: 24px; font-weight: bold; }
.stat-name { color: rgba(255,255,255,0.6); font-size: 12px; }

@media (max-width: 1200px) { .row { flex-wrap: wrap; } .panel { min-width: calc(50% - 10px); } }
@media (max-width: 768px) { .header-bar { flex-direction: column; gap: 15px; } .panel { min-width: 100%; } .footer-stats { flex-wrap: wrap; } }
</style>
