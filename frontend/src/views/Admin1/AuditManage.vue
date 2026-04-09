<template>
  <div class="audit-manage">
    <el-card>
      <div slot="header" class="card-header">
        <span>审核管理</span>
      </div>
      <!-- 搜索和筛选 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="审核类型">
            <el-select v-model="searchForm.type" placeholder="请选择审核类型" clearable>
              <el-option label="二级管理员个人信息修改" value="1"></el-option>
              <el-option label="二级管理员账号注销" value="2"></el-option>
              <el-option label="普通用户加入农场" value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select v-model="searchForm.status" placeholder="请选择审核状态" clearable>
              <el-option label="待审核" value="0"></el-option>
              <el-option label="已通过" value="1"></el-option>
              <el-option label="已拒绝" value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="auditList"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="审核ID" width="80"></el-table-column>
        <el-table-column prop="applicantId" label="申请人ID" width="100"></el-table-column>
        <el-table-column label="申请人" width="150">
          <template slot-scope="scope">
            {{ getApplicantName(scope.row.applicantId) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="审核类型" width="180">
          <template slot-scope="scope">
            {{ getTypeText(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="审核状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="farmId" label="农场ID" width="100" v-if="isFarmJoinAudit"></el-table-column>
        <el-table-column label="农场名称" width="150" v-if="isFarmJoinAudit">
          <template slot-scope="scope">
            {{ getFarmName(scope.row.farmId) }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="申请内容" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-button type="text" @click="showContent(scope.row.content)">查看内容</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180"></el-table-column>
        <el-table-column prop="auditedAt" label="审核时间" width="180"></el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button type="primary" size="small" @click="handleApprove(scope.row)" v-if="scope.row.status === 0">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(scope.row)" v-if="scope.row.status === 0">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 内容查看对话框 -->
    <el-dialog
      title="申请内容"
      :visible.sync="contentDialogVisible"
      width="50%"
    >
      <pre>{{ dialogContent }}</pre>
      <span slot="footer" class="dialog-footer">
        <el-button @click="contentDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
    
    <!-- 审核对话框 -->
    <el-dialog
      :title="auditDialogTitle"
      :visible.sync="auditDialogVisible"
      width="50%"
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input type="textarea" v-model="auditForm.auditComment" :rows="4" placeholder="请输入审核意见"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">提交审核</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAuditRequests, processAuditRequest } from '../../api/user'

export default {
  name: 'AuditManage',
  data() {
    return {
      loading: false,
      auditList: [],
      searchForm: {
        type: '',
        status: ''
      },
      pageNum: 1,
      pageSize: 10,
      total: 0,
      contentDialogVisible: false,
      dialogContent: '',
      auditDialogVisible: false,
      auditDialogTitle: '',
      auditForm: {
        auditComment: ''
      },
      currentAudit: null,
      currentAction: '', // 'approve' or 'reject'
      users: {}, // 缓存用户信息
      farms: {} // 缓存农场信息
    }
  },
  computed: {
    isFarmJoinAudit() {
      return this.searchForm.type === '3'
    }
  },
  created() {
    this.loadAuditRequests()
  },
  methods: {
    loadAuditRequests() {
      this.loading = true
      getAuditRequests().then(response => {
        if (response.code === 200) {
          this.auditList = response.data
          this.total = response.data.length
        }
        this.loading = false
      }).catch(error => {
        console.error('获取审核请求失败:', error)
        this.loading = false
      })
    },
    handleSearch() {
      // 这里可以实现带参数的搜索
      this.loadAuditRequests()
    },
    resetSearch() {
      this.searchForm = {
        type: '',
        status: ''
      }
      this.loadAuditRequests()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.loadAuditRequests()
    },
    handleCurrentChange(current) {
      this.pageNum = current
      this.loadAuditRequests()
    },
    getTypeText(type) {
      const typeMap = {
        1: '二级管理员个人信息修改',
        2: '二级管理员账号注销',
        3: '普通用户加入农场'
      }
      return typeMap[type] || '未知类型'
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
      const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝'
      }
      return statusMap[status] || '未知状态'
    },
    getApplicantName(applicantId) {
      // 这里应该从用户服务获取用户名称，暂时返回ID
      return `用户${applicantId}`
    },
    getFarmName(farmId) {
      // 这里应该从农场服务获取农场名称，暂时返回ID
      return `农场${farmId}`
    },
    showContent(content) {
      try {
        const parsedContent = JSON.parse(content)
        this.dialogContent = JSON.stringify(parsedContent, null, 2)
      } catch (e) {
        this.dialogContent = content
      }
      this.contentDialogVisible = true
    },
    handleApprove(audit) {
      this.currentAudit = audit
      this.currentAction = 'approve'
      this.auditDialogTitle = '审核通过'
      this.auditForm.auditComment = ''
      this.auditDialogVisible = true
    },
    handleReject(audit) {
      this.currentAudit = audit
      this.currentAction = 'reject'
      this.auditDialogTitle = '审核拒绝'
      this.auditForm.auditComment = ''
      this.auditDialogVisible = true
    },
    submitAudit() {
      const status = this.currentAction === 'approve' ? 1 : 2
      const auditorId = this.$store.state.user.id || 1 // 假设当前用户是审核人
      
      processAuditRequest(this.currentAudit.id, {
        status: status,
        auditorId: auditorId,
        auditComment: this.auditForm.auditComment
      }).then(response => {
        if (response.code === 200) {
          this.$message.success('审核处理成功')
          this.auditDialogVisible = false
          this.loadAuditRequests()
        } else {
          this.$message.error('审核处理失败')
        }
      }).catch(error => {
        console.error('审核处理失败:', error)
        this.$message.error('审核处理失败')
      })
    }
  }
}
</script>

<style scoped>
.audit-manage { padding: 10px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.search-section { margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
