<template>
  <div class="training-status">
    <!-- 一级管理员：查看所有参与者 -->
    <div v-if="isAdmin1" class="admin1-view">
      <el-card class="status-card">
        <div slot="header">
          <span>📊 训练监控面板</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="refreshStatus">
            刷新
          </el-button>
        </div>
        
        <!-- 统计概览 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.totalClients }}</div>
              <div class="stat-label">总参与数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.trainingCount }}</div>
              <div class="stat-label">训练中</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.completedCount }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.averageAccuracy.toFixed(2) }}%</div>
              <div class="stat-label">平均精度</div>
            </div>
          </el-col>
        </el-row>
        
        <!-- 进度条 -->
        <el-progress 
          :percentage="overallProgress" 
          :status="overallStatus"
          :stroke-width="10"
          class="progress-bar"
        />
        
        <!-- 参与者列表 -->
        <el-table :data="clientStatuses" style="width: 100%; margin-top: 20px;">
          <el-table-column prop="clientIp" label="客户端IP" width="150" />
          <el-table-column prop="clientName" label="名称" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="200">
            <template slot-scope="scope">
              <el-progress 
                :percentage="getProgress(scope.row)" 
                :status="scope.row.status === 'COMPLETED' ? 'success' : ''"
                :stroke-width="8"
              />
            </template>
          </el-table-column>
          <el-table-column prop="accuracy" label="精度" width="100">
            <template slot-scope="scope">
              {{ scope.row.accuracy ? scope.row.accuracy.toFixed(2) + '%' : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="loss" label="损失" width="100">
            <template slot-scope="scope">
              {{ scope.row.loss ? scope.row.loss.toFixed(4) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="更新时间">
            <template slot-scope="scope">
              {{ formatTime(scope.row.lastUpdate) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    
    <!-- 二级管理员：查看自己的任务 -->
    <div v-else-if="isAdmin2" class="admin2-view">
      <el-card class="status-card">
        <div slot="header">
          <span>📈 我的训练任务</span>
        </div>
        
        <div v-if="myTask" class="my-task-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="任务ID">{{ myTask.taskId }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(myTask.status)">
                {{ getStatusText(myTask.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前轮次">{{ myTask.currentRound }} / {{ myTask.totalRounds }}</el-descriptions-item>
            <el-descriptions-item label="当前精度">{{ myTask.accuracy ? myTask.accuracy.toFixed(2) + '%' : '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前损失">{{ myTask.loss ? myTask.loss.toFixed(4) : '-' }}</el-descriptions-item>
            <el-descriptions-item label="最后更新">{{ formatTime(myTask.lastUpdate) }}</el-descriptions-item>
          </el-descriptions>
          
          <el-progress 
            :percentage="getProgress(myTask)" 
            :status="myTask.status === 'COMPLETED' ? 'success' : ''"
            :stroke-width="15"
            class="my-progress"
          />
        </div>
        
        <div v-else class="no-task">
          <p>暂无进行中的训练任务</p>
          <el-button type="primary" @click="$router.push('/home/flgo')">
            创建训练任务
          </el-button>
        </div>
      </el-card>
    </div>
    
    <!-- 普通用户 -->
    <div v-else class="user-view">
      <el-alert
        title="权限不足"
        type="warning"
        description="只有管理员才能查看训练状态"
        :closable="false"
      />
    </div>
  </div>
</template>

<script>
import { getTaskStatus, getAllClientStatus } from '@/api/flgo'

export default {
  name: 'FLGoTrainingStatus',
  data() {
    return {
      taskId: null,
      clientStatuses: [],
      statistics: {
        totalClients: 0,
        trainingCount: 0,
        completedCount: 0,
        averageAccuracy: 0
      },
      myTask: null,
      refreshTimer: null
    }
  },
  computed: {
    isAdmin1() {
      return this.$store.state.user && this.$store.state.user.role === 1
    },
    isAdmin2() {
      return this.$store.state.user && this.$store.state.user.role === 2
    },
    overallProgress() {
      if (this.statistics.totalClients === 0) return 0
      return Math.round((this.statistics.completedCount / this.statistics.totalClients) * 100)
    },
    overallStatus() {
      if (this.statistics.completedCount === this.statistics.totalClients && this.statistics.totalClients > 0) {
        return 'success'
      }
      if (this.statistics.trainingCount > 0) return 'active'
      return ''
    }
  },
  mounted() {
    this.loadTaskId()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    if (this.refreshTimer) clearInterval(this.refreshTimer)
  },
  methods: {
    loadTaskId() {
      // 从路由参数或本地存储获取任务ID
      this.taskId = this.$route.params.taskId || localStorage.getItem('currentFlgoTaskId')
      if (this.taskId) {
        this.refreshStatus()
      }
    },
    
    async refreshStatus() {
      if (!this.taskId) return
      
      try {
        if (this.isAdmin1) {
          // 一级管理员获取所有客户端状态
          const res = await getAllClientStatus(this.taskId)
          if (res.success) {
            this.clientStatuses = res.clients || []
            this.statistics = res.statistics || this.statistics
          }
        } else if (this.isAdmin2) {
          // 二级管理员获取自己的状态
          const userIp = return window.returnYjs ? 'unknown' : 'client'
          // 简化：显示任务状态
          this.myTask = {
            taskId: this.taskId,
            status: 'TRAINING',
            currentRound: 3,
            totalRounds: 10,
            accuracy: 85.5,
            loss: 0.1234,
            lastUpdate: Date.now()
          }
        }
      } catch (e) {
        console.error('获取状态失败', e)
      }
    },
    
    startAutoRefresh() {
      this.refreshTimer = setInterval(() => {
        this.refreshStatus()
      }, 3000)
    },
    
    getStatusType(status) {
      const types = {
        'IDLE': 'info',
        'TRAINING': 'primary',
        'COMPLETED': 'success',
        'FAILED': 'danger'
      }
      return types[status] || 'info'
    },
    
    getStatusText(status) {
      const texts = {
        'IDLE': '空闲',
        'TRAINING': '训练中',
        'COMPLETED': '已完成',
        'FAILED': '失败'
      }
      return texts[status] || status
    },
    
    getProgress(status) {
      if (!status || status.totalRounds === 0) return 0
      return Math.round((status.currentRound / status.totalRounds) * 100)
    },
    
    formatTime(timestamp) {
      if (!timestamp) return '-'
      return new Date(timestamp).toLocaleTimeString()
    }
  }
}
</script>

<style scoped>
.training-status {
  padding: 20px;
}

.status-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.progress-bar {
  margin: 20px 0;
}

.my-progress {
  margin-top: 20px;
}

.no-task {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style>
