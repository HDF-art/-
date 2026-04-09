<template>
  <div class="training-log">
    <el-card class="log-card">
      <div slot="header" class="card-header">
        <span>训练日志管理</span>
        <el-button type="primary" size="small" @click="refreshLogs">刷新日志</el-button>
      </div>
      
      <div class="log-content">
        <!-- 任务选择 -->
        <div class="task-selector">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="任务ID">
              <el-input v-model="searchForm.taskId" placeholder="请输入任务ID" class="task-id-input"></el-input>
            </el-form-item>
            <el-form-item label="日志类型">
              <el-select v-model="searchForm.logType" placeholder="选择日志类型" class="log-type-select">
                <el-option label="全部" value=""></el-option>
                <el-option label="信息" value="info"></el-option>
                <el-option label="警告" value="warning"></el-option>
                <el-option label="错误" value="error"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchLogs">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 日志列表 -->
        <div class="log-list">
          <el-table :data="logs" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="日志ID" width="80"></el-table-column>
            <el-table-column prop="logType" label="类型" width="100">
              <template slot-scope="scope">
                <el-tag :type="getLogTypeTag(scope.row.logType)">{{ scope.row.logType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" min-width="400"></el-table-column>
            <el-table-column prop="nodeId" label="节点ID" width="150"></el-table-column>
            <el-table-column prop="round" label="轮次" width="80"></el-table-column>
            <el-table-column prop="timestamp" label="时间" width="200">
              <template slot-scope="scope">
                {{ formatDate(scope.row.timestamp) }}
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination" v-if="logs.length > 0">
            <el-pagination
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="currentPage"
              @current-change="handleCurrentChange"
            ></el-pagination>
          </div>
          
          <!-- 无数据提示 -->
          <div class="no-data" v-if="!loading && logs.length === 0">
            <el-empty description="暂无日志数据"></el-empty>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="log-actions" v-if="logs.length > 0">
          <el-button type="danger" @click="confirmClearLogs">清空日志</el-button>
          <el-button type="info" @click="exportLogs">导出日志</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getTrainingLogs, getTrainingLogsByType, clearTrainingLogs } from '../../api/user'

export default {
  name: 'TrainingLog',
  data() {
    return {
      searchForm: {
        taskId: '',
        logType: ''
      },
      logs: [],
      loading: false,
      total: 0,
      pageSize: 20,
      currentPage: 1
    }
  },
  methods: {
    // 获取日志类型对应的标签类型
    getLogTypeTag(logType) {
      switch (logType) {
        case 'info':
          return 'info'
        case 'warning':
          return 'warning'
        case 'error':
          return 'danger'
        default:
          return 'default'
      }
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hour = String(d.getHours()).padStart(2, '0')
      const minute = String(d.getMinutes()).padStart(2, '0')
      const second = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    },
    
    // 搜索日志
    async searchLogs() {
      if (!this.searchForm.taskId) {
        this.$message.error('请输入任务ID')
        return
      }
      
      this.loading = true
      try {
        let response
        if (this.searchForm.logType) {
          response = await getTrainingLogsByType(this.searchForm.taskId, this.searchForm.logType)
        } else {
          response = await getTrainingLogs(this.searchForm.taskId)
        }
        
        this.logs = response.data || []
        this.total = this.logs.length
        this.currentPage = 1
      } catch (error) {
        this.$message.error('获取日志失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    
    // 刷新日志
    refreshLogs() {
      if (this.searchForm.taskId) {
        this.searchLogs()
      }
    },
    
    // 重置搜索
    resetSearch() {
      this.searchForm.taskId = ''
      this.searchForm.logType = ''
      this.logs = []
      this.total = 0
    },
    
    // 处理分页
    handleCurrentChange(page) {
      this.currentPage = page
    },
    
    // 确认清空日志
    confirmClearLogs() {
      if (!this.searchForm.taskId) {
        this.$message.error('请输入任务ID')
        return
      }
      
      this.$confirm('确定要清空该任务的所有日志吗？此操作不可恢复。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await clearTrainingLogs(this.searchForm.taskId)
          this.$message.success('日志清空成功')
          this.logs = []
          this.total = 0
        } catch (error) {
          this.$message.error('清空日志失败：' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 取消操作
      })
    },
    
    // 导出日志
    exportLogs() {
      if (!this.searchForm.taskId) {
        this.$message.error('请输入任务ID')
        return
      }
      
      // 简单的导出功能，实际项目中可能需要后端支持
      const logsToExport = this.logs.map(log => ({
        日志ID: log.id,
        类型: log.logType,
        内容: log.content,
        节点ID: log.nodeId,
        轮次: log.round || '-',
        时间: this.formatDate(log.timestamp)
      }))
      
      const csvContent = this.convertToCSV(logsToExport)
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      link.setAttribute('download', `training-logs-task-${this.searchForm.taskId}-${new Date().getTime()}.csv`)
      link.style.visibility = 'hidden'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },
    
    // 转换为CSV格式
    convertToCSV(data) {
      const headers = Object.keys(data[0])
      const csv = [
        headers.join(','),
        ...data.map(row => headers.map(field => {
          const value = row[field]
          return typeof value === 'string' && value.includes(',') ? `"${value}"` : value
        }).join(','))
      ]
      return csv.join('\n')
    }
  }
}
</script>

<style scoped>
.training-log {
  padding: 10px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.log-content {
  margin-top: 20px;
}

.task-selector {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.search-form {
  width: 100%;
}

.task-id-input {
  width: 200px;
  margin-right: 10px;
}

.log-type-select {
  width: 150px;
  margin-right: 10px;
}

.log-list {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.no-data {
  padding: 40px 0;
  text-align: center;
}

.log-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .task-id-input,
  .log-type-select {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .log-actions {
    flex-direction: column;
  }
}
</style>
