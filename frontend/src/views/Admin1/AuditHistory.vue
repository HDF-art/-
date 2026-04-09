<template>
  <div class="audit-history">
    <el-card>
      <div slot="header">
        <span>已处理审核记录</span>
        <el-tag type="success" style="margin-left: 10px;">已通过: {{ passedCount }} 人</el-tag>
        <el-tag type="danger" style="margin-left: 10px;">已拒绝: {{ rejectedCount }} 人</el-tag>
      </div>
      
      <el-table :data="processedUsers" stripe border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="organization" label="单位" width="180"></el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="处理时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)">
              {{ getStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getProcessedAuditUsers } from '../../api/user'

export default {
  name: 'AuditHistory',
  data() {
    return {
      loading: false,
      processedUsers: [],
      passedCount: 0,
      rejectedCount: 0
    }
  },
  created() {
    this.loadProcessedUsers()
  },
  methods: {
    async loadProcessedUsers() {
      this.loading = true
      try {
        const res = await getProcessedAuditUsers()
        this.processedUsers = res.data || []
        this.passedCount = this.processedUsers.filter(u => u.auditStatus === 1).length
        this.rejectedCount = this.processedUsers.filter(u => u.auditStatus === 2).length
      } catch (e) {
        console.error('加载已处理审核记录失败', e)
        this.$message.error('加载已处理审核记录失败')
      } finally {
        this.loading = false
      }
    },
    getStatusType(status) {
      switch (status) {
        case 1: return 'success'
        case 2: return 'danger'
        default: return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 1: return '已通过'
        case 2: return '已拒绝'
        default: return '未知'
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.audit-history {
  padding: 10px;
}
</style>
