<template>
  <div class="data-statistics">
    <el-card>
      <div slot="header" class="card-header">
        <span>数据统计分析</span>
      </div>
      <div class="statistics-cards">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ totalCount }}</div>
            <div class="stat-label">总识别次数</div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ avgConfidence }}%</div>
            <div class="stat-label">平均准确率</div>
          </div>
        </el-card>
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ todayCount }}</div>
            <div class="stat-label">今日识别次数</div>
          </div>
        </el-card>
      </div>
      <div class="charts-container">
        <el-card class="chart-card">
          <div slot="header" class="chart-header">识别类型分布</div>
          <div id="typeChart" style="height: 300px;"></div>
        </el-card>
        <el-card class="chart-card">
          <div slot="header" class="chart-header">每日识别数量趋势</div>
          <div id="trendChart" style="height: 300px;"></div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getRecognitionStats } from '@/api/user'

export default {
  name: 'DataStatistics',
  data() {
    return {
      totalCount: 0,
      avgConfidence: 0,
      todayCount: 0,
      recognitionStats: null,
      typeChart: null,
      trendChart: null
    }
  },
  mounted() {
    this.loadData()
  },
  beforeDestroy() {
    if (this.typeChart) this.typeChart.dispose()
    if (this.trendChart) this.trendChart.dispose()
  },
  methods: {
    async loadData() {
      try {
        const res = await getRecognitionStats()
        if (res && res.data && res.data.data) {
          this.recognitionStats = res.data.data
          this.totalCount = this.recognitionStats.totalCount || 0
          this.todayCount = this.recognitionStats.todayCount || 0
          this.avgConfidence = ((this.recognitionStats.averageConfidence || 0) * 100).toFixed(1)
          this.initCharts()
        }
      } catch (e) {
        console.error('加载数据失败', e)
      }
    },
    initCharts() {
      this.initTypeChart()
      this.initTrendChart()
    },
    initTypeChart() {
      const chart = echarts.init(document.getElementById('typeChart'))
      const dist = (this.recognitionStats && this.recognitionStats.diseaseDistribution) || {}
      const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
      const chartData = Object.entries(dist).length > 0
        ? Object.entries(dist).map(([name, value], i) => ({ value, name, itemStyle: { color: colors[i % colors.length] } }))
        : [{ value: 0, name: '暂无数据', itemStyle: { color: '#909399' } }]
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 10 },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false, position: 'center' },
          emphasis: { label: { show: true, fontSize: '18', fontWeight: 'bold' } },
          labelLine: { show: false },
          data: chartData
        }]
      })
      this.typeChart = chart
    },
    initTrendChart() {
      const chart = echarts.init(document.getElementById('trendChart'))
      const dailyCount = (this.recognitionStats && this.recognitionStats.dailyCount) || {}
      const dates = Object.keys(dailyCount).length > 0 ? Object.keys(dailyCount) : ['暂无']
      const values = Object.keys(dailyCount).length > 0 ? Object.values(dailyCount) : [0]
      chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: dates },
        yAxis: { type: 'value' },
        series: [{
          type: 'line',
          data: values,
          smooth: true,
          areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
          itemStyle: { color: '#409eff' }
        }]
      })
      this.trendChart = chart
    }
  }
}
</script>

<style scoped>
.data-statistics { padding: 10px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.statistics-cards { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-card { flex: 1; }
.stat-content { text-align: center; padding: 20px 0; }
.stat-number { font-size: 32px; font-weight: bold; color: #409eff; margin-bottom: 10px; }
.stat-label { font-size: 14px; color: #606266; }
.charts-container { display: flex; gap: 20px; }
.chart-card { flex: 1; }
.chart-header { font-size: 14px; font-weight: bold; padding: 10px 0; }
</style>
