<template>
  <el-card class="chart-panel">
    <div slot="header" class="chart-header">
      <span class="chart-title">{{ title }}</span>
      <div class="chart-actions">
        <el-radio-group v-model="chartType" size="small" @change="updateChart">
          <el-radio-button label="line">折线</el-radio-button>
          <el-radio-button label="bar">柱状</el-radio-button>
          <el-radio-button label="pie">饼图</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    <div ref="chart" class="chart-container"></div>
  </el-card>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'DataChart',
  props: {
    title: {
      type: String,
      default: '数据可视化'
    },
    data: {
      type: Object,
      default: () => ({
        xAxis: ['1月', '2月', '3月', '4月', '5月', '6月'],
        series: [
          { name: '水稻', data: [120, 132, 101, 134, 90, 230] },
          { name: '小麦', data: [220, 182, 191, 234, 290, 330] },
          { name: '玉米', data: [150, 232, 201, 154, 190, 210] }
        ]
      })
    },
    height: {
      type: String,
      default: '350px'
    }
  },
  data() {
    return {
      chartType: 'line',
      chart: null
    }
  },
  mounted() {
    this.initChart()
    window.addEventListener('resize', this.resizeChart)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeChart)
    if (this.chart) {
      this.chart.dispose()
    }
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.chart)
      this.updateChart()
    },
    updateChart() {
      const option = this.getOption()
      this.chart.setOption(option)
    },
    getOption() {
      const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de']
      
      const baseOption = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: this.data.series.map(s => s.name),
          bottom: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        }
      }

      if (this.chartType === 'line') {
        return {
          ...baseOption,
          xAxis: {
            type: 'category',
            data: this.data.xAxis,
            boundaryGap: false
          },
          yAxis: {
            type: 'value'
          },
          series: this.data.series.map((s, i) => ({
            name: s.name,
            type: 'line',
            smooth: true,
            data: s.data,
            itemStyle: { color: colors[i % colors.length] },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: colors[i % colors.length] + '80' },
                { offset: 1, color: colors[i % colors.length] + '10' }
              ])
            }
          }))
        }
      } else if (this.chartType === 'bar') {
        return {
          ...baseOption,
          xAxis: {
            type: 'category',
            data: this.data.xAxis
          },
          yAxis: {
            type: 'value'
          },
          series: this.data.series.map((s, i) => ({
            name: s.name,
            type: 'bar',
            data: s.data,
            itemStyle: { color: colors[i % colors.length] },
            barWidth: '30%'
          }))
        }
      } else {
        return {
          tooltip: {
            trigger: 'item'
          },
          series: [
            {
              name: '分布',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: true,
                formatter: '{b}: {d}%'
              },
              data: this.data.xAxis.map((x, i) => ({
                name: x,
                value: this.data.series[0]?.data[i] || 0
              })),
              itemStyle: {
                color: (params) => colors[params.dataIndex % colors.length]
              }
            }
          ]
        }
      }
    },
    resizeChart() {
      if (this.chart) {
        this.chart.resize()
      }
    }
  }
}
</script>

<style scoped>
.chart-panel {
  margin-bottom: 20px;
  border-radius: 12px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: v-bind(height);
  min-height: 300px;
}
</style>
