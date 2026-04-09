<template>
  <div class="federation-page">
    <el-card class="header-card">
      <h2>🤖 联邦学习训练平台</h2>
      <p>一键启动分布式模型训练，无需了解底层算法</p>
    </el-card>
    
    <!-- 创建训练任务 -->
    <el-card class="task-card">
      <div slot="header">
        <span class="card-title">🚀 创建训练任务</span>
      </div>
      
      <el-form :model="taskForm" label-width="120px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.taskName" placeholder="请输入任务名称"></el-input>
        </el-form-item>
        
        <el-form-item label="选择算法">
          <el-select v-model="taskForm.algorithm" placeholder="请选择算法">
            <el-option label="FedAvg (联邦平均)" value="FedAvg"></el-option>
            <el-option label="FedProx" value="FedProx"></el-option>
            <el-option label="SCAFFOLD" value="SCAFFOLD"></el-option>
            <el-option label="FedNova" value="FedNova"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="训练轮数">
          <el-slider v-model="taskForm.rounds" :min="1" :max="100" :step="1" show-stops></el-slider>
          <span>{{ taskForm.rounds }} 轮</span>
        </el-form-item>
        
        <el-form-item label="参与客户端">
          <el-slider v-model="taskForm.clients" :min="2" :max="20" :step="1" show-stops></el-slider>
          <span>{{ taskForm.clients }} 个客户端</span>
        </el-form-item>
        
        <el-form-item label="隐私保护">
          <el-switch v-model="taskForm.enablePrivacy"></el-switch>
          <span class="help-text">开启差分隐私保护</span>
        </el-form-item>
        
        <el-form-item v-if="taskForm.enablePrivacy" label="隐私预算 ε">
          <el-slider v-model="taskForm.epsilon" :min="0.1" :max="10" :step="0.1"></el-slider>
          <span>ε = {{ taskForm.epsilon }}</span>
          <p class="help-text">越小越隐私，推荐 1.0-2.0</p>
        </el-form-item>
        
        <el-form-item label="安全聚合">
          <el-switch v-model="taskForm.enableSecure"></el-switch>
          <span class="help-text">开启加密传输聚合</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="createTask" :loading="creating">
            <i class="el-icon-cpu"></i> 一键启动训练
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 训练进度 -->
    <el-card v-if="currentTask" class="progress-card">
      <div slot="header">
        <span class="card-title">📊 训练进度</span>
      </div>
      
      <el-progress :percentage="progressPercent" :status="progressStatus"></el-progress>
      
      <div class="task-info">
        <el-tag>任务: {{ currentTask.taskName }}</el-tag>
        <el-tag type="success">算法: {{ currentTask.algorithm }}</el-tag>
        <el-tag type="warning">轮次: {{ currentTask.currentRound }}/{{ currentTask.totalRounds }}</el-tag>
        <el-tag type="info">状态: {{ currentTask.status }}</el-tag>
      </div>
      
      <el-button v-if="currentTask.status === 'COMPLETED'" type="success" @click="downloadModel">
        <i class="el-icon-download"></i> 下载模型
      </el-button>
    </el-card>
    
    <!-- 任务列表 -->
    <el-card class="history-card">
      <div slot="header">
        <span class="card-title">📋 训练历史</span>
      </div>
      
      <el-table :data="taskList" style="width: 100%">
        <el-table-column prop="taskName" label="任务名称" width="150"></el-table-column>
        <el-table-column prop="algorithm" label="算法" width="100"></el-table-column>
        <el-table-column prop="totalRounds" label="总轮数" width="80"></el-table-column>
        <el-table-column prop="currentRound" label="当前" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'COMPLETED' ? 'success' : 'warning'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'FederatedLearning',
  data() {
    return {
      taskForm: {
        taskName: '',
        algorithm: 'FedAvg',
        rounds: 10,
        clients: 5,
        enablePrivacy: true,
        epsilon: 1.0,
        enableSecure: true
      },
      creating: false,
      currentTask: null,
      taskList: []
    }
  },
  computed: {
    progressPercent() {
      if (!this.currentTask) return 0
      return Math.round((this.currentTask.currentRound / this.currentTask.totalRounds) * 100)
    },
    progressStatus() {
      if (!this.currentTask) return ''
      return this.currentTask.status === 'COMPLETED' ? 'success' : ''
    }
  },
  mounted() {
    this.loadTasks()
  },
  methods: {
    async createTask() {
      if (!this.taskForm.taskName) {
        this.$message.error('请输入任务名称')
        return
      }
      
      this.creating = true
      try {
        const res = await this.$http.post('/api/federation/task/create', this.taskForm)
        if (res.data.success) {
          this.$message.success('任务创建成功！')
          this.currentTask = res.data.task
          this.loadTasks()
        } else {
          this.$message.error(res.data.message || '创建失败')
        }
      } catch (e) {
        this.$message.error('创建失败: ' + e.message)
      }
      this.creating = false
    },
    async loadTasks() {
      try {
        const res = await this.$http.get('/api/federation/tasks')
        this.taskList = res.data.tasks || []
      } catch (e) {
        console.error(e)
      }
    },
    async downloadModel() {
      this.$message.success('模型下载开始！')
    }
  }
}
</script>

<style scoped>
.federation-page {
  padding: 20px;
}

.header-card {
  text-align: center;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-card h2 {
  margin: 0;
  font-size: 28px;
}

.task-card, .progress-card, .history-card {
  margin-bottom: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
}

.task-info {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.help-text {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}
</style>
