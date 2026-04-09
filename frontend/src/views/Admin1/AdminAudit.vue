<template>
  <div class="admin-audit">
    <el-card>
      <div slot="header">
        <span>二级管理员注册审核</span>
        <el-tag type="info" style="margin-left: 10px;">待审核: {{ pendingCount }} 人</el-tag>
      </div>
      
      <el-table :data="pendingUsers" stripe border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="organization" label="单位" width="180"></el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)">
              {{ getStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <template v-if="scope.row.auditStatus === 0">
              <el-button type="success" size="small" @click="handleApprove(scope.row)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(scope.row)">拒绝</el-button>
            </template>
            <span v-else style="color: #999;">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog title="审核拒绝原因" :visible.sync="rejectDialogVisible" width="500px">
      <el-form>
        <el-form-item label="拒绝原因">
          <el-input type="textarea" v-model="rejectReason" :rows="4" placeholder="请输入拒绝原因"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getPendingAuditUsers, auditAdmin2 } from '../../api/user'

export default {
  name: 'AdminAudit',
  data() {
    return {
      loading: false,
      pendingUsers: [],
      pendingCount: 0,
      rejectDialogVisible: false,
      rejectReason: '',
      currentUser: null
    }
  },
  created() {
    this.loadPendingUsers()
  },
  methods: {
    async loadPendingUsers() {
      this.loading = true
      try {
        const res = await getPendingAuditUsers()
        this.pendingUsers = res.data || []
        this.pendingCount = this.pendingUsers.filter(u => u.auditStatus === 0).length
      } catch (e) {
        console.error('加载待审核用户失败', e)
        this.$message.error('加载待审核用户失败')
      } finally {
        this.loading = false
      }
    },
    async handleApprove(user) {
      try {
        await this.$confirm('确认通过该用户的注册申请？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await auditAdmin2(user.id, 1)
        this.$message.success('审核通过，已发送邮件通知')
        this.loadPendingUsers()
      } catch (e) {
        if (e !== 'cancel') {
          this.$message.error(e.message || '审核失败')
        }
      }
    },
    handleReject(user) {
      this.currentUser = user
      this.rejectReason = ''
      this.rejectDialogVisible = true
    },
    async confirmReject() {
      if (!this.rejectReason.trim()) {
        this.$message.warning('请输入拒绝原因')
        return
      }
      
      try {
        await auditAdmin2(this.currentUser.id, 2, this.rejectReason)
        this.$message.success('已拒绝该申请，已发送邮件通知')
        this.rejectDialogVisible = false
        this.loadPendingUsers()
      } catch (e) {
        this.$message.error(e.message || '操作失败')
      }
    },
    getStatusType(status) {
      switch (status) {
        case 0: return 'warning'
        case 1: return 'success'
        case 2: return 'danger'
        default: return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 0: return '待审核'
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
.admin-audit {
  padding: 10px;
}
</style>
