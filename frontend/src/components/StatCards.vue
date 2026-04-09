<template>
  <div class="stat-cards-container">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" v-for="(stat, index) in stats" :key="index">
        <div class="stat-card-wrapper" :style="{ animationDelay: index * 0.1 + 's' }">
          <el-card class="stat-card" :class="'stat-card-' + stat.type">
            <div class="stat-card-content">
              <div class="stat-icon" :style="{ background: stat.bgColor }">
                <i :class="stat.icon"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
                  <i :class="stat.trend > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                  {{ Math.abs(stat.trend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'StatCards',
  props: {
    stats: {
      type: Array,
      default: () => [
        { label: '用户总数', value: '1,234', icon: 'el-icon-user', type: 'primary', bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', trend: 12 },
        { label: '农场总数', value: '567', icon: 'el-icon-office-building', type: 'success', bgColor: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)', trend: 8 },
        { label: '模型总数', value: '89', icon: 'el-icon-cpu', type: 'warning', bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', trend: -3 },
        { label: '今日识别', value: '2,456', icon: 'el-icon-view', type: 'info', bgColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', trend: 25 }
      ]
    }
  }
}
</script>

<style scoped>
.stat-cards-container {
  padding: 10px 0;
}

.stat-card-wrapper {
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-card-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.stat-icon i {
  font-size: 28px;
  color: white;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  margin-top: 4px;
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 10px;
}

.stat-trend.up {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.stat-trend.down {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}
</style>
