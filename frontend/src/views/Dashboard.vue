<template>
  <div class="dashboard">
    <!-- 欢迎信息 - 渐变背景设计 -->
    <div class="welcome-section bg-gradient">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2 class="welcome-title">欢迎回来，{{ user ? user.username : '' }}！</h2>
          <p class="welcome-date">今天是 {{ formatDate(new Date()) }}</p>
          <div class="welcome-time">{{ currentTime }}</div>
        </div>
        <div class="welcome-avatar">
          <el-avatar :size="80" :src="getUserAvatar()" class="user-avatar pulse-animation"></el-avatar>
          <div class="user-role-badge" :class="getRoleClass()">{{ getRoleName() }}</div>
        </div>
      </div>
    </div>
    
    <!-- 数据统计卡片 -->
    <div class="stats-cards">
      <!-- 根据用户角色显示不同的统计卡片 -->
      <template v-if="user && user.role === 'admin1'">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon farm-icon">
              <i class="el-icon-office-building"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ farmCount }}</div>
              <div class="stat-label">农场总数</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon model-icon">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ modelCount }}</div>
              <div class="stat-label">模型总数</div>
            </div>
          </div>
        </el-card>
      </template>
      
      <template v-else-if="user && user.role === 'admin2'">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon task-icon">
              <i class="el-icon-s-order"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ taskCount }}</div>
              <div class="stat-label">训练任务数</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon success-icon">
              <i class="el-icon-circle-check"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ successTaskCount }}</div>
              <div class="stat-label">成功任务数</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon accuracy-icon">
              <i class="el-icon-data-line"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ avgAccuracy }}%</div>
              <div class="stat-label">平均准确率</div>
            </div>
          </div>
        </el-card>
      </template>
      
      <template v-else-if="user && user.role === 'user'">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon identify-icon">
              <i class="el-icon-camera"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ identifyCount }}</div>
              <div class="stat-label">识别总数</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon today-icon">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ todayIdentifyCount }}</div>
              <div class="stat-label">今日识别</div>
            </div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon farm-icon">
              <i class="el-icon-office-building"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ userFarmCount }}</div>
              <div class="stat-label">我的农场</div>
            </div>
          </div>
        </el-card>
      </template>
    </div>
    
    <!-- 图表区域 - 优化设计 -->
    <div class="charts-section">
      <el-card class="chart-card hover-lift">
        <div slot="header" class="chart-header">
          <span class="chart-title">{{ chartTitle }}</span>
          <div class="chart-actions">
            <el-button type="text" size="small" @click="refreshChart" class="refresh-btn">
              <i class="el-icon-refresh"></i>
            </el-button>
            <el-dropdown trigger="click">
              <el-button type="text" size="small">
                <i class="el-icon-setting"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click="exportChart">导出图表</el-dropdown-item>
                <el-dropdown-item @click="resetChart">重置图表</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
        <div id="main-chart" class="chart-container"></div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { getUserList, getAvailableFarms, getModelList, getTaskList, getTaskSuccessCount, getAverageAccuracy, getTotalRecognitionCount, getTodayRecognitionCount, getRecognitionStats, getTaskStatusStats, getUserRoleStats } from '../api/user'

export default {
  name: 'Dashboard',
  data() {
    return {
      // 统计数据
      userCount: 0,
      farmCount: 0,
      modelCount: 0,
      taskCount: 0,
      successTaskCount: 0,
      avgAccuracy: 0,
      identifyCount: 0,
      todayIdentifyCount: 0,
      userFarmCount: 0,
      chartTitle: '',
      chartInstance: null,
      currentTime: '',
      timeTimer: null,
      recognitionStats: null,
      taskStatusStats: null,
      userRoleStats: null
    }
  },
  computed: {
    ...mapState(['user'])
  },
  created() {
    // 根据用户角色设置图表标题
    this.setChartTitle()
  },
  mounted() {
    // 初始化实时时间
    this.updateTime()
    this.timeTimer = setInterval(() => {
      this.updateTime()
    }, 1000)
    
    // 延迟初始化图表，确保DOM已渲染
    setTimeout(() => {
      this.initChart()
    }, 100)
  },
  beforeDestroy() {
    // 清除定时器
    if (this.timeTimer) {
      clearInterval(this.timeTimer)
    }
    
    // 销毁图表实例
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const [userRes, farmRes, modelRes, taskRes, successTaskRes, accuracyRes, totalRecognitionRes, todayRecognitionRes, recognitionStatsRes, taskStatusRes, userRoleRes] = await Promise.all([
          getUserList(),
          getAvailableFarms(),
          getModelList(),
          getTaskList(),
          getTaskSuccessCount(),
          getAverageAccuracy(),
          getTotalRecognitionCount(),
          getTodayRecognitionCount(),
          getRecognitionStats().catch(() => null),
          getTaskStatusStats().catch(() => null),
          getUserRoleStats().catch(() => null)
        ])
        if (userRes.data && userRes.data.data) {
          this.userCount = userRes.data.data.length
        }
        if (farmRes.data && farmRes.data.data) {
          this.farmCount = farmRes.data.data.length
        }
        if (modelRes.data && modelRes.data.data) {
          this.modelCount = modelRes.data.data.total
        }
        if (taskRes.data && taskRes.data.data) {
          this.taskCount = taskRes.data.data.total
        }
        if (successTaskRes.data && successTaskRes.data.data) {
          this.successTaskCount = successTaskRes.data.data
        }
        if (accuracyRes.data && accuracyRes.data.data) {
          this.avgAccuracy = accuracyRes.data.data.averageAccuracy
        }
        if (totalRecognitionRes.data && totalRecognitionRes.data.data) {
          this.identifyCount = totalRecognitionRes.data.data
        }
        if (todayRecognitionRes.data && todayRecognitionRes.data.data) {
          this.todayIdentifyCount = todayRecognitionRes.data.data
        }
        if (recognitionStatsRes && recognitionStatsRes.data && recognitionStatsRes.data.data) {
          this.recognitionStats = recognitionStatsRes.data.data
        }
        if (taskStatusRes && taskStatusRes.data && taskStatusRes.data.data) {
          this.taskStatusStats = taskStatusRes.data.data
        }
        if (userRoleRes && userRoleRes.data && userRoleRes.data.data) {
          this.userRoleStats = userRoleRes.data.data
        }
      } catch (e) {
        console.error('加载数据失败', e)
      }
    },
    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      const day = date.getDate()
      const weekDays = ['日', '一', '二', '三', '四', '五', '六']
      const weekDay = weekDays[date.getDay()]
      return `${year}年${month}月${day}日 星期${weekDay}`
    },
    
    // 更新实时时间
    updateTime() {
      const now = new Date()
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const seconds = String(now.getSeconds()).padStart(2, '0')
      this.currentTime = `${hours}:${minutes}:${seconds}`
    },
    
    // 获取用户头像
    getUserAvatar() {
      // 实际项目中可以根据用户信息返回真实头像
      const role = this.user ? this.user.role : 'user'
      return `/static/avatars/${role}-avatar.png`
    },
    
    // 获取用户角色名称
    getRoleName() {
      if (!this.user) return '未知角色'
      switch (this.user.role) {
        case 'admin1': return '超级管理员'
        case 'admin2': return '运维管理员'
        case 'user': return '普通用户'
        default: return '未知角色'
      }
    },
    
    // 获取角色样式类
    getRoleClass() {
      if (!this.user) return 'role-unknown'
      switch (this.user.role) {
        case 'admin1': return 'role-admin1'
        case 'admin2': return 'role-admin2'
        case 'user': return 'role-user'
        default: return 'role-unknown'
      }
    },
    
    // 刷新图表
    refreshChart() {
      if (this.chartInstance) {
        this.chartInstance.dispose()
        this.initChart()
        this.$message.success('图表已刷新')
      }
    },
    
    // 重置图表
    resetChart() {
      if (this.chartInstance) {
        this.chartInstance.setOption(this.getUserRoleChartOption())
        this.$message.success('图表已重置')
      }
    },
    
    // 导出图表
    exportChart() {
      if (this.chartInstance) {
        // 获取图表的base64数据
        const url = this.chartInstance.getDataURL({
          type: 'png',
          pixelRatio: 2,
          backgroundColor: '#fff'
        })
        // 创建下载链接
        const link = document.createElement('a')
        link.download = `${this.chartTitle}-${this.formatDate(new Date()).replace(/[\s:]/g, '-')}.png`
        link.href = url
        link.click()
        this.$message.success('图表已导出')
      }
    },
    
    // 设置图表标题
    setChartTitle() {
      if (this.user) {
        switch (this.user.role) {
          case 'admin1':
            this.chartTitle = '用户增长趋势'
            break
          case 'admin2':
            this.chartTitle = '训练任务完成率'
            break
          case 'user':
            this.chartTitle = '识别记录统计'
            break
          default:
            this.chartTitle = '数据统计'
        }
      }
    },
    
    // 初始化图表
    initChart() {
      // 检查是否已加载ECharts
      if (window.echarts) {
        const chartDom = document.getElementById('main-chart')
        if (chartDom) {
          this.chartInstance = window.echarts.init(chartDom)
          
          // 根据用户角色设置不同的图表配置
          const option = this.getUserRoleChartOption()
          
          this.chartInstance.setOption(option)
          
          // 监听窗口大小变化，调整图表大小
          window.addEventListener('resize', () => {
            if (this.chartInstance) {
              this.chartInstance.resize()
            }
          })
        }
      }
    },
    
    // 根据用户角色获取图表配置
    getUserRoleChartOption() {
      if (this.user && this.user.role === 'admin1') {
        const roleStats = this.userRoleStats || { admin1Count: 0, admin2Count: 0, userCount: 0, total: 0 }
        return {
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: { bottom: 10, left: 'center' },
          series: [{
            name: '用户角色分布',
            type: 'pie',
            radius: ['40%', '70%'],
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            label: { show: false, position: 'center' },
            emphasis: { label: { show: true, fontSize: '18', fontWeight: 'bold' } },
            labelLine: { show: false },
            data: [
              { value: roleStats.admin1Count, name: '一级管理员', itemStyle: { color: '#e94560' } },
              { value: roleStats.admin2Count, name: '二级管理员', itemStyle: { color: '#409eff' } },
              { value: roleStats.userCount, name: '普通用户', itemStyle: { color: '#67c23a' } }
            ].filter(d => d.value > 0),
            animationDuration: 1500, animationType: 'scale', animationEasing: 'elasticOut'
          }]
        }
      } else if (this.user && this.user.role === 'admin2') {
        const statusStats = this.taskStatusStats || { statusDistribution: {}, total: 0 }
        const dist = statusStats.statusDistribution || {}
        const statusNames = { PENDING: '待开始', RUNNING: '进行中', COMPLETED: '已完成', FAILED: '已失败' }
        const statusColors = { PENDING: '#e6a23c', RUNNING: '#409eff', COMPLETED: '#67c23a', FAILED: '#f56c6c' }
        return {
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'category', data: Object.keys(dist).map(k => statusNames[k] || k) },
          yAxis: { type: 'value' },
          series: [{
            data: Object.entries(dist).map(([k, v]) => ({
              value: v,
              itemStyle: { color: statusColors[k] || '#409eff', borderRadius: [4, 4, 0, 0] }
            })),
            type: 'bar', barWidth: '60%',
            label: { show: true, position: 'top', fontSize: 12 },
            animationDuration: 1000, animationEasing: 'elasticOut'
          }]
        }
      } else {
        const recStats = this.recognitionStats || { diseaseDistribution: {} }
        const diseaseDist = recStats.diseaseDistribution || {}
        const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
        return {
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: { orient: 'horizontal', bottom: 10, left: 'center' },
          series: [{
            name: '病虫害类型',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            label: { show: false, position: 'center' },
            emphasis: {
              label: { show: true, fontSize: '18', fontWeight: 'bold' },
              itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
            },
            labelLine: { show: false },
            data: Object.entries(diseaseDist).map(([name, value], i) => ({
              value, name, itemStyle: { color: colors[i % colors.length] }
            })),
            animationDuration: 1500, animationType: 'scale', animationEasing: 'elasticOut'
          }]
        }
      }
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px 0;
  animation: fadeIn 0.6s ease-in-out;
}

/* 淡入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 脉冲动画 */
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

.pulse-animation {
  animation: pulse 2s infinite;
}

/* 渐变背景欢迎区 */
.welcome-section {
  border-radius: 12px;
  margin-bottom: 24px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.bg-gradient {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  position: relative;
}

.welcome-content {
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text {
  flex: 1;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  opacity: 0.95;
}

.welcome-date {
  font-size: 16px;
  margin: 0 0 12px 0;
  opacity: 0.85;
}

.welcome-time {
  font-size: 24px;
  font-weight: 500;
  font-family: 'Courier New', monospace;
  opacity: 0.9;
}

.welcome-avatar {
  position: relative;
}

.user-avatar {
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-role-badge {
  position: absolute;
  bottom: -5px;
  right: -5px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.9);
}

.role-admin1 {
  color: #e6a23c;
}

.role-admin2 {
  color: #409eff;
}

.role-user {
  color: #67c23a;
}



.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px !important;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  border: none !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409eff, #67c23a);
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.01);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

/* 为不同类型的卡片添加特定的顶部边框颜色 */
.stat-card:nth-child(1)::before {
  background: linear-gradient(90deg, #409eff, #409eff);
}

.stat-card:nth-child(2)::before {
  background: linear-gradient(90deg, #67c23a, #67c23a);
}

.stat-card:nth-child(3)::before {
  background: linear-gradient(90deg, #e6a23c, #e6a23c);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 24px;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24px;
  font-size: 28px;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.user-icon {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.2));
  color: #409eff;
}

.farm-icon {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1), rgba(103, 194, 58, 0.2));
  color: #67c23a;
}

.model-icon {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1), rgba(103, 194, 58, 0.2));
  color: #67c23a;
}

.task-icon {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1), rgba(103, 194, 58, 0.2));
  color: #67c23a;
}

.success-icon {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1), rgba(103, 194, 58, 0.2));
  color: #67c23a;
}

.accuracy-icon {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.2));
  color: #409eff;
}

.identify-icon {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.2));
  color: #409eff;
}

.today-icon {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1), rgba(230, 162, 60, 0.2));
  color: #e6a23c;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
  margin: 0;
  transition: color 0.3s ease;
}

.stat-card:hover .stat-number {
  color: #409eff;
}

.stat-label {
  font-size: 15px;
  color: #606266;
  margin-top: 10px;
  font-weight: 500;
}

.charts-section {
  margin-top: 24px;
}

.chart-card {
  border-radius: 12px !important;
  overflow: hidden;
  border: none !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.hover-lift {
  transition: all 0.3s ease;
}

.hover-lift:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px !important;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.chart-actions {
  display: flex;
  gap: 8px;
}

.refresh-btn i {
  transition: transform 0.3s ease;
}

.refresh-btn:hover i {
  transform: rotate(180deg);
}

.chart-container {
  height: 450px;
  width: 100%;
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    height: 300px;
  }
}
</style>