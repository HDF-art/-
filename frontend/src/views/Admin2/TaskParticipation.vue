<template>
  <div class="task-participation-container">
    <el-card class="participation-card">
      <template slot="header">
        <div class="card-header">
          <span>任务参与管理</span>
        </div>
      </template>
      
      <el-table :data="participationList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="参与ID" width="80" />
        <el-table-column prop="taskId" label="任务ID" width="80" />
        <el-table-column prop="status" label="参与状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="participateTime" label="参与时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.participateTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === '待确认'"
              size="small"
              type="primary"
              @click="agreeParticipation(scope.row)"
            >
              同意
            </el-button>
            <el-button
              v-if="scope.row.status === '待确认'"
              size="small"
              type="danger"
              @click="rejectParticipation(scope.row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && participationList.length === 0" description="暂无参与记录" />
    </el-card>
  </div>
</template>

<script>
import { getTaskParticipations } from '@/api/user'

export default {
  name: 'TaskParticipation',
  data() {
    return {
      participationList: [],
      loading: false
    }
  },
  mounted() {
    this.loadParticipations()
  },
  methods: {
    async loadParticipations() {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      const userId = user.id || 2
      
      this.loading = true
      try {
        const res = await getTaskParticipations(userId)
        if (res && res.data && res.data.data) {
          this.participationList = res.data.data
        }
      } catch (e) {
        console.error('加载参与记录失败', e)
        this.participationList = []
      } finally {
        this.loading = false
      }
    },
    getStatusType(status) {
      const types = {
        '待确认': 'warning',
        '已同意': 'success',
        '已拒绝': 'danger'
      }
      return types[status] || 'info'
    },
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
    },
    agreeParticipation(participation) {
      this.$confirm('确定要同意参与此任务吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await this.$http.put(`/task-participations/${participation.id}/status`, null, { params: { status: '已同意' } })
          if (res.code === 200) {
            this.$message.success('已同意参与任务')
            this.loadParticipations()
          } else {
            this.$message.error('操作失败：' + res.message)
          }
        } catch (e) {
          this.$message.error('网络错误，请稍后重试')
        }
      }).catch(() => {})
    },
    rejectParticipation(participation) {
      this.$confirm('确定要拒绝参与此任务吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await this.$http.put(`/task-participations/${participation.id}/status`, null, { params: { status: '已拒绝' } })
          if (res.code === 200) {
            this.$message.success('已拒绝参与任务')
            this.loadParticipations()
          } else {
            this.$message.error('操作失败：' + res.message)
          }
        } catch (e) {
          this.$message.error('网络错误，请稍后重试')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.task-participation-container { padding: 10px 0; }
.card-header { font-size: 16px; font-weight: bold; }
</style>
