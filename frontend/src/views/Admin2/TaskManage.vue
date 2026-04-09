<template>
  <div class="task-manage">
    <el-card>
      <div slot="header" class="card-header">
        <span>任务状态管理</span>
      </div>
      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="taskList"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="任务ID" width="80"></el-table-column>
        <el-table-column prop="taskName" label="任务名称" width="200"></el-table-column>
        <el-table-column prop="taskType" label="任务类型" width="120">
          <template slot-scope="scope">
            {{ getTaskTypeText(scope.row.taskType) }}
          </template>
        </el-table-column>
        <el-table-column prop="federatedAlgorithm" label="联邦算法" width="150">
          <template slot-scope="scope">
            {{ getAlgorithmText(scope.row.federatedAlgorithm) }}
          </template>
        </el-table-column>
        <el-table-column prop="trainingModel" label="训练模型" width="150"></el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getAuditStatusType(scope.row.auditStatus)">
              {{ getAuditStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="任务状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="participationStatus" label="参与状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getParticipationStatusType(scope.row.participationStatus)">
              {{ getParticipationStatusText(scope.row.participationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="180">
          <template slot-scope="scope">
            <el-progress :percentage="scope.row.progress || 0" :status="getProgressStatus(scope.row.progress || 0)"></el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="180"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'TaskManage',
  data() {
    return {
      loading: false,
      taskList: []
    }
  },
  created() {
    this.loadTasks()
  },
  methods: {
    loadTasks() {
      this.loading = true
      // 从API加载真实数据
      const token = localStorage.getItem('token')
      fetch('/api/task-participations/list', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      .then(res => res.json())
      .then(data => {
        this.taskList = data.data || []
        this.loading = false
      })
      .catch(() => {
        this.taskList = []
        this.loading = false
      })
    },
    getTaskTypeText(taskType) {
      const typeMap = {
        'image_recognition': '图像识别',
        'object_detection': '目标检测',
        'time_series': '时序预测'
      }
      return typeMap[taskType] || '未知'
    },
    getAlgorithmText(algorithm) {
      const algorithmMap = {
        'FedAvg': 'FedAvg',
        'FedProx': 'FedProx',
        'SCAFFOLD': 'SCAFFOLD',
        'FedOpt': 'FedOpt'
      }
      return algorithmMap[algorithm] || '未知'
    },
    getAuditStatusType(auditStatus) {
      switch (auditStatus) {
        case '已通过': return 'success'
        case '已拒绝': return 'danger'
        case '待审核': return 'warning'
        default: return 'info'
      }
    },
    getAuditStatusText(auditStatus) {
      return auditStatus || '未知'
    },
    getStatusType(status) {
      switch (status) {
        case '进行中': return 'primary'
        case '已完成': return 'success'
        case '已失败': return 'danger'
        case '待开始': return 'info'
        default: return 'info'
      }
    },
    getStatusText(status) {
      return status || '未知'
    },
    getParticipationStatusType(participationStatus) {
      switch (participationStatus) {
        case '已参与': return 'success'
        case '未参与': return 'info'
        case '待确认': return 'warning'
        default: return 'info'
      }
    },
    getParticipationStatusText(participationStatus) {
      return participationStatus || '未知'
    },
    getProgressStatus(progress) {
      return progress === 100 ? 'success' : 'primary'
    }
  }
}
</script>

<style scoped>
.task-manage { padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
</style>