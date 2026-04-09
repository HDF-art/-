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
          <el-input v-model="searchForm.username" placeholder="请输入操作人"></el-input>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="全部" clearable>
            <el-option label="登录" value="LOGIN"></el-option>
            <el-option label="登出" value="LOGOUT"></el-option>
            <el-option label="创建" value="CREATE"></el-option>
            <el-option label="更新" value="UPDATE"></el-option>
            <el-option label="删除" value="DELETE"></el-option>
            <el-option label="上传" value="UPLOAD"></el-option>
            <el-option label="下载" value="DOWNLOAD"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 日志列表 -->
      <el-table :data="logList" stripe border>
        <el-table-column prop="id" label="ID" width="60"></el-table-column>
        <el-table-column prop="username" label="操作人" width="120"></el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)">
              {{ scope.row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationDesc" label="操作描述"></el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="130"></el-table-column>
        <el-table-column prop="userAgent" label="用户代理" width="200" show-overflow-tooltip></el-table-column>
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
        style="margin-top: 20px;"
      ></el-pagination>
    </el-card>
  </div>
</template>

<script>
import { getAuditLogs } from '../api/user'

export default {
  name: 'AuditLog',
  data() {
    return {
      logList: [],
      searchForm: {
        username: '',
        operationType: '',
        dateRange: []
      },
      pagination: {
        pageNum: 1,
        pageSize: 20,
        total: 0
      }
    }
  },
  created() {
    this.loadLogs()
  },
  methods: {
    async loadLogs() {
      try {
        const params = {
          page: this.pagination.pageNum,
          size: this.pagination.pageSize,
          username: this.searchForm.username,
          operation: this.searchForm.operationType
        }
        
        const res = await getAuditLogs(params)
        if (res && res.logs) {
          this.logList = res.logs || []
        }
      } catch (e) {
        console.error('加载日志失败', e)
      }
    },
    handleSearch() {
      this.pagination.pageNum = 1
      this.loadLogs()
    },
    handleReset() {
      this.searchForm = {
        username: '',
        operationType: '',
        dateRange: []
      }
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
    getOperationTypeTag(type) {
      const tags = {
        'LOGIN': 'success',
        'LOGOUT': 'info',
        'CREATE': 'primary',
        'UPDATE': 'warning',
        'DELETE': 'danger',
        'UPLOAD': 'success',
        'DOWNLOAD': 'info'
      }
      return tags[type] || 'info'
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
</style>
