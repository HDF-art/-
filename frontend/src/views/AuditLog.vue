<template>
  <div class="audit-log">
    <el-card>
      <div slot="header">
        <span>操作日志</span>
        <el-tag type="info" style="margin-left: 10px;">只读记录，无法删除或修改</el-tag>
      </div>
      
      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="请输入操作人" clearable></el-input>
        </el-form-item>
        
        <el-form-item label="用户角色">
          <el-select v-model="searchForm.userRole" placeholder="全部" clearable @change="handleRoleChange">
            <el-option label="全部" :value="null"></el-option>
            <el-option label="一级管理员" :value="1"></el-option>
            <el-option label="二级管理员" :value="2"></el-option>
            <el-option label="普通用户" :value="3"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="全部" clearable>
            <el-option
              v-for="type in operationTypes"
              :key="type"
              :label="getOperationTypeLabel(type)"
              :value="type"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="模块">
          <el-select v-model="searchForm.module" placeholder="全部" clearable>
            <el-option
              v-for="mod in modules"
              :key="mod"
              :label="mod"
              :value="mod"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 日志列表 -->
      <el-table :data="logList" stripe border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60"></el-table-column>
        <el-table-column prop="username" label="操作人" width="120"></el-table-column>
        <el-table-column prop="userRole" label="角色" width="100">
          <template slot-scope="scope">
            <el-tag :type="getUserRoleTag(scope.row.userRole)">
              {{ getUserRoleLabel(scope.row.userRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)">
              {{ getOperationTypeLabel(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="100"></el-table-column>
        <el-table-column prop="description" label="操作描述" show-overflow-tooltip></el-table-column>
        <el-table-column prop="requestUri" label="请求路径" width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="130"></el-table-column>
        <el-table-column prop="result" label="结果" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ scope.row.result === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="响应时间(ms)" width="110"></el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="160"></el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right;"
      ></el-pagination>
    </el-card>
  </div>
</template>

<script>
import { getOperationLogs, getOperationTypes, getLogModules } from '../api/user'
import { mapGetters } from 'vuex'

export default {
  name: 'AuditLog',
  data() {
    return {
      logList: [],
      loading: false,
      operationTypes: [],
      modules: [],
      searchForm: {
        username: '',
        userRole: null,
        operationType: '',
        module: '',
        dateRange: []
      },
      pagination: {
        pageNum: 1,
        pageSize: 20,
        total: 0
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  created() {
    this.loadOptions()
    this.loadLogs()
  },
  methods: {
    async loadOptions() {
      try {
        const [typesRes, modulesRes] = await Promise.all([
          getOperationTypes(),
          getLogModules()
        ])
        if (typesRes && typesRes.data) {
          this.operationTypes = typesRes.data || []
        }
        if (modulesRes && modulesRes.data) {
          this.modules = modulesRes.data || []
        }
      } catch (e) {
        console.error('加载选项失败', e)
      }
    },
    
    async loadLogs() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.pageNum,
          size: this.pagination.pageSize,
          username: this.searchForm.username || undefined,
          userRole: this.searchForm.userRole || undefined,
          operationType: this.searchForm.operationType || undefined,
          module: this.searchForm.module || undefined
        }
        
        if (this.searchForm.dateRange && this.searchForm.dateRange.length === 2) {
          params.startDate = this.searchForm.dateRange[0]
          params.endDate = this.searchForm.dateRange[1]
        }
        
        const res = await getOperationLogs(params)
        if (res && res.code === 200 && res.data) {
          this.logList = res.data.list || []
          this.pagination.total = res.data.total || 0
        }
      } catch (e) {
        console.error('加载日志失败', e)
        this.$message.error('加载日志失败')
      } finally {
        this.loading = false
      }
    },
    
    handleSearch() {
      this.pagination.pageNum = 1
      this.loadLogs()
    },
    
    handleReset() {
      this.searchForm = {
        username: '',
        userRole: null,
        operationType: '',
        module: '',
        dateRange: []
      }
      this.handleSearch()
    },
    
    handleRoleChange() {
      this.handleSearch()
    },
    
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.loadLogs()
    },
    
    handleCurrentChange(val) {
      this.pagination.pageNum = val
      this.loadLogs()
    },
    
    getUserRoleTag(role) {
      const tags = {
        1: 'danger',
        2: 'warning',
        3: 'info'
      }
      return tags[role] || 'info'
    },
    
    getUserRoleLabel(role) {
      const labels = {
        1: '一级管理员',
        2: '二级管理员',
        3: '普通用户'
      }
      return labels[role] || '未知'
    },
    
    getOperationTypeTag(type) {
      const tags = {
        'LOGIN': 'success',
        'LOGOUT': 'info',
        'CREATE': 'primary',
        'UPDATE': 'warning',
        'DELETE': 'danger',
        'UPLOAD': 'success',
        'DOWNLOAD': 'info',
        'VIEW': ''
      }
      return tags[type] || 'info'
    },
    
    getOperationTypeLabel(type) {
      const labels = {
        'LOGIN': '登录',
        'LOGOUT': '登出',
        'CREATE': '创建',
        'UPDATE': '更新',
        'DELETE': '删除',
        'UPLOAD': '上传',
        'DOWNLOAD': '下载',
        'VIEW': '查看'
      }
      return labels[type] || type
    }
  }
}
</script>

<style scoped>
.audit-log {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.el-form-item {
  margin-bottom: 10px;
}
</style>