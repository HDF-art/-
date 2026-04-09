<template>
  <div class="task-audit-container">
    <el-card class="audit-card">
      <template slot="header">
        <div class="card-header">
          <span>任务审核管理</span>
        </div>
      </template>
      
      <el-table :data="taskList" style="width: 100%">
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="taskType" label="任务类型" width="120" />
        <el-table-column prop="federatedAlgorithm" label="联邦学习算法" width="150" />
        <el-table-column prop="trainingModel" label="训练模型" width="120" />
        <el-table-column prop="expectedParticipants" label="预定参与数量" width="120" />
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="small" @click="viewTask(scope.row)">查看详情</el-button>
            <el-button
              v-if="scope.row.auditStatus === '待审核'"
              size="small"
              type="primary"
              @click="approveTask(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === '待审核'"
              size="small"
              type="danger"
              @click="rejectTask(scope.row)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === '已通过'"
              size="small"
              type="info"
              @click="publishTask(scope.row)"
            >
              发布
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === '已通过' && scope.row.status === '待开始'"
              size="small"
              type="success"
              @click="startTask(scope.row)"
            >
              启动
            </el-button>
            <el-button
              v-if="scope.row.status === '进行中'"
              size="small"
              type="warning"
              @click="monitorTask(scope.row)"
            >
              监控
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 发布任务对话框 -->
    <el-dialog
      :visible.sync="publishDialogVisible"
      title="发布任务"
      width="500px"
    >
      <el-form :model="publishForm" label-width="80px">
        <el-form-item label="选择参与用户">
          <el-select
            v-model="publishForm.userIds"
            multiple
            placeholder="请选择参与任务的二级管理员"
            style="width: 100%"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPublish">确定发布</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TaskAudit',
  data() {
    return {
      taskList: [],
      publishDialogVisible: false,
      currentTask: null,
      publishForm: {
        userIds: []
      },
      userList: [
        { id: 2, username: 'admin2' },
        { id: 3, username: 'admin3' },
        { id: 4, username: 'admin4' }
      ]
    }
  },
  mounted() {
    this.loadTasks()
  },
  methods: {
    loadTasks() {
      // 调用后端API获取待审核的任务列表
      this.$http.get('/training-tasks/audit-status/待审核')
        .then(response => {
          if (response.code === 200) {
            this.taskList = response.data
          }
        })
        .catch(() => {
          console.error('加载任务列表失败')
        })
    },
    viewTask(task) {
      // 查看任务详情
      this.$alert(`任务ID: ${task.id}\n任务名称: ${task.name}\n任务类型: ${task.taskType}\n联邦学习算法: ${task.federatedAlgorithm}\n训练模型: ${task.trainingModel}\n预定参与数量: ${task.expectedParticipants}\n截止时间: ${task.deadline}\n创建时间: ${task.createdAt}\n备注: ${task.remark}`, '任务详情', {
        confirmButtonText: '确定'
      })
    },
    approveTask(task) {
      this.$prompt('请输入审核意见', '审核通过', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入审核意见'
      }).then(({ value }) => {
        // 获取当前用户信息
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        const auditInfo = {
          auditStatus: '已通过',
          auditComment: value,
          auditedBy: user.id || 1
        }
        
        // 调用后端API审核任务
        this.$http.post(`/training-tasks/${task.id}/audit`, auditInfo)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('任务审核通过')
              this.loadTasks()
            } else {
              this.$message.error('审核失败：' + response.message)
            }
          })
          .catch(() => {
            this.$message.error('网络错误，请稍后重试')
          })
      })
    },
    rejectTask(task) {
      this.$prompt('请输入拒绝原因', '审核拒绝', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因'
      }).then(({ value }) => {
        // 获取当前用户信息
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        const auditInfo = {
          auditStatus: '已拒绝',
          auditComment: value,
          auditedBy: user.id || 1
        }
        
        // 调用后端API审核任务
        this.$http.post(`/training-tasks/${task.id}/audit`, auditInfo)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('任务审核拒绝')
              this.loadTasks()
            } else {
              this.$message.error('审核失败：' + response.message)
            }
          })
          .catch(() => {
            this.$message.error('网络错误，请稍后重试')
          })
      })
    },
    publishTask(task) {
      this.currentTask = task
      this.publishForm.userIds = []
      this.publishDialogVisible = true
    },
    submitPublish() {
      if (!this.currentTask || this.publishForm.userIds.length === 0) {
        this.$message.error('请选择参与用户')
        return
      }
      
      const publishInfo = {
        userIds: this.publishForm.userIds
      }
      
      // 调用后端API发布任务
      this.$http.post(`/training-tasks/${this.currentTask.id}/publish`, publishInfo)
        .then(response => {
          if (response.code === 200) {
            this.$message.success('任务发布成功')
            this.publishDialogVisible = false
            this.loadTasks()
          } else {
            this.$message.error('发布失败：' + response.message)
          }
        })
        .catch(() => {
          this.$message.error('网络错误，请稍后重试')
        })
    },
    startTask(task) {
      this.$confirm('确定要启动此任务吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 调用后端API启动任务
        this.$http.post(`/training-tasks/${task.id}/start`)
          .then(response => {
            if (response.code === 200) {
              this.$message.success('任务启动成功')
              this.loadTasks()
            } else {
              this.$message.error('启动失败：' + response.message)
            }
          })
          .catch(() => {
            this.$message.error('网络错误，请稍后重试')
          })
      })
    },
    monitorTask(task) {
      // 监控任务训练过程
      this.$alert(`任务ID: ${task.id}\n任务名称: ${task.name}\n当前状态: ${task.status}\n当前轮次: ${task.currentRound || 0}/${task.totalRounds || 0}\n开始时间: ${task.startTime}\n参与客户端数量: ${task.actualParticipants || 0}`, '任务监控', {
        confirmButtonText: '确定'
      })
    }
  }
}
</script>

<style scoped>
.task-audit-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.audit-card {
  margin-bottom: 20px;
}
</style>
