<template>
  <div class="history-record">
    <el-card class="record-card">
      <div slot="header" class="card-header">
        <span>识别记录</span>
      </div>
      
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="识别时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="病虫害类型">
            <el-select v-model="searchForm.diseaseType" placeholder="请选择" clearable>
              <el-option label="稻瘟病" value="稻瘟病"></el-option>
              <el-option label="纹枯病" value="纹枯病"></el-option>
              <el-option label="稻飞虱" value="稻飞虱"></el-option>
              <el-option label="螟虫" value="螟虫"></el-option>
              <el-option label="细菌性条斑病" value="细菌性条斑病"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 批量操作 -->
        <div class="batch-actions">
          <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
            批量删除
          </el-button>
          <el-button @click="handleExport">导出</el-button>
        </div>
      </div>
      
      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="recordsData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="记录ID" width="80"></el-table-column>
        <el-table-column prop="createdAt" label="识别时间" width="180" :formatter="formatDate"></el-table-column>
        <el-table-column prop="result" label="识别结果" width="150"></el-table-column>
        <el-table-column prop="confidence" label="置信度" width="120">
          <template slot-scope="scope">
            <div class="confidence-item">
              <span>{{ (scope.row.confidence * 100).toFixed(2) }}%</span>
              <el-progress 
                :percentage="(scope.row.confidence * 100).toFixed(0)" 
                :color="getProgressColor(scope.row.confidence)"
                :stroke-width="8"
                :show-text="false"
              ></el-progress>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="imagePath" label="识别图片" width="100">
          <template slot-scope="scope">
            <el-image
              :src="getFullImageUrl(scope.row.imagePath)"
              :preview-src-list="[getFullImageUrl(scope.row.imagePath)]"
              style="width: 60px; height: 60px; border-radius: 4px"
              fit="cover"
            ></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="viewDetail(scope.row)">查看</el-button>
            <el-button type="text" danger @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
      
      <!-- 详情对话框 -->
      <el-dialog
        title="识别记录详情"
        :visible.sync="dialogVisible"
        width="70%"
        :close-on-click-modal="false"
      >
        <div v-if="currentRecord" class="record-detail">
          <div class="detail-row">
            <div class="detail-label">记录ID：</div>
            <div class="detail-value">{{ currentRecord.id }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">识别时间：</div>
            <div class="detail-value">{{ formatDate(currentRecord.createdAt) }}</div>
          </div>
          <div class="detail-row">
            <div class="detail-label">主要识别结果：</div>
            <div class="detail-value">
              <span class="disease-name">{{ currentRecord.result }}</span>
              <span class="confidence">{{ (currentRecord.confidence * 100).toFixed(2) }}%</span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>识别图片</h4>
            <el-image
              :src="getFullImageUrl(currentRecord.imagePath)"
              style="max-width: 100%; max-height: 400px; border-radius: 4px"
              fit="contain"
            ></el-image>
          </div>
          
          <div class="detail-section">
            <h4>详细识别结果</h4>
            <el-table :data="parseDetails(currentRecord.resultJson)" style="width: 100%">
              <el-table-column prop="diseaseName" label="病虫害名称" width="180"></el-table-column>
              <el-table-column prop="confidence" label="置信度">
                <template slot-scope="scope">
                  <div class="confidence-item">
                    <span class="confidence-value">{{ (scope.row.confidence * 100).toFixed(2) }}%</span>
                    <el-progress 
                      :percentage="(scope.row.confidence * 100).toFixed(0)" 
                      :color="getProgressColor(scope.row.confidence)"
                      :stroke-width="10"
                      :show-text="false"
                    ></el-progress>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div class="detail-section">
            <h4>防治建议</h4>
            <div class="suggestion-content">
              <p>{{ currentRecord.preventionAdvice }}</p>
            </div>
          </div>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
// 导入识别API
import { getIdentifyRecords, deleteIdentifyRecord } from '@/api/identify'

export default {
  name: 'HistoryRecord',
  data() {
    return {
      loading: false,
      searchForm: {
        dateRange: null,
        diseaseType: ''
      },
      recordsData: [],
      selectedIds: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      currentRecord: null
    }
  },
  created() {
    this.loadRecords()
  },
  methods: {
    // 加载记录数据
    async loadRecords() {
      this.loading = true
      try {
        // 构建查询参数
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize
        }
        
        if (this.searchForm.dateRange) {
          params.startDate = this.searchForm.dateRange[0]
          params.endDate = this.searchForm.dateRange[1]
        }
        
        if (this.searchForm.diseaseType) {
          params.diseaseType = this.searchForm.diseaseType
        }
        
        // 从localStorage获取用户信息
        const userStr = localStorage.getItem('user');
        let userId = 1; // 默认用户ID
        
        if (userStr) {
          try {
            const user = JSON.parse(userStr);
            userId = user.id || user.userId || 1;
          } catch (e) {
            console.warn('解析用户信息失败，使用默认用户ID');
          }
        }
        
        // 添加用户ID到参数
        params.userId = userId;
        
        // 调用接口获取数据
        const response = await getIdentifyRecords(params)
        
        // 使用真实数据
        this.recordsData = response.data.records || response.data || [];
        this.total = response.data.total || 0;
      } catch (error) {
        this.$message.error('加载记录失败：' + (error.message || '未知错误'))
        // 出错时返回空数据
        this.recordsData = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    
    // 处理查询
    handleSearch() {
      this.currentPage = 1
      this.loadRecords()
    },
    
    // 重置查询
    resetSearch() {
      this.searchForm = {
        dateRange: null,
        diseaseType: ''
      }
      this.currentPage = 1
      this.loadRecords()
    },
    
    // 处理选择变化
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    
    // 处理分页大小变化
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadRecords()
    },
    
    // 处理当前页变化
    handleCurrentChange(current) {
      this.currentPage = current
      this.loadRecords()
    },
    
    // 获取进度条颜色
    getProgressColor(confidence) {
      if (confidence >= 0.9) return '#67c23a'
      if (confidence >= 0.7) return '#e6a23c'
      if (confidence >= 0.5) return '#f56c6c'
      return '#909399'
    },
    
    // 获取状态类型
    getStatusType(status) {
      switch (status) {
        case 'normal':
          return 'success'
        case 'abnormal':
          return 'warning'
        default:
          return 'info'
      }
    },
    
    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 'normal':
          return '正常'
        case 'abnormal':
          return '异常'
        default:
          // 如果没有状态字段，返回默认值
          return status || '正常'
      }
    },
    
    // 解析详细信息
    parseDetails(resultJson) {
      if (!resultJson) return []
      try {
        const parsed = typeof resultJson === 'string' ? JSON.parse(resultJson) : resultJson
        return parsed.details || []
      } catch (error) {
        console.error('解析详细信息失败:', error)
        return []
      }
    },
    
    // 获取完整的图片URL
    getFullImageUrl(imagePath) {
      if (!imagePath) return ''
      
      // 如果已经是完整URL，直接返回
      if (imagePath.startsWith('http')) {
        return imagePath
      }
      
      // 如果是相对路径，添加完整的基地址
      // 假设后端运行在 localhost:8083
      const baseHost = 'http://localhost:8083'
      
      // 如果imagePath以/开头，直接拼接
      if (imagePath.startsWith('/')) {
        return baseHost + imagePath
      }
      
      // 否则添加/
      return baseHost + '/' + imagePath
    },
    
    // 格式化日期
    formatDate(date) {
      // 处理无效日期
      if (!date) {
        return '未知时间'
      }
      
      // 如果date是一个对象，尝试提取createdAt字段
      if (typeof date === 'object') {
        if (date.createdAt) {
          date = date.createdAt
        } else {
          // 如果没有createdAt字段，尝试转换为字符串
          date = JSON.stringify(date)
        }
      }
      
      // 确保我们有一个字符串
      let dateStr = date
      if (typeof date !== 'string') {
        dateStr = String(date)
      }
      
      // 最简单的处理方式：直接替换T为一个空格
      if (typeof dateStr === 'string') {
        return dateStr.replace('T', ' ')
      }
      
      return '无效时间'
    },
    
    // 查看详情
    viewDetail(record) {
      this.currentRecord = { ...record }
      this.dialogVisible = true
    },
    
    // 处理删除
    handleDelete(id) {
      this.$confirm('确定要删除这条记录吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteIdentifyRecord(id)
          // 删除成功后更新本地数据
          this.recordsData = this.recordsData.filter(item => item.id !== id)
          this.total--
          this.$message.success('删除成功')
        } catch (error) {
          this.$message.error('删除失败：' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 取消删除
      })
    },
    
    // 处理批量删除
    handleBatchDelete() {
      if (this.selectedIds.length === 0) {
        this.$message.warning('请选择要删除的记录')
        return
      }
      
      this.$confirm(`确定要删除选中的 ${this.selectedIds.length} 条记录吗？`, '批量删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 逐个删除选中的记录
          const deletePromises = this.selectedIds.map(id => deleteIdentifyRecord(id))
          await Promise.all(deletePromises)
          
          // 删除成功后更新本地数据
          this.recordsData = this.recordsData.filter(item => !this.selectedIds.includes(item.id))
          this.total -= this.selectedIds.length
          this.selectedIds = []
          this.$message.success('批量删除成功')
        } catch (error) {
          this.$message.error('批量删除失败：' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 取消删除
      })
    },
    
    // 处理导出
    handleExport() {
      this.$message('导出功能开发中')
    }
  },
  filters: {
    dateFormat(value) {
      if (!value) return ''
      
      // 如果value是一个对象，尝试提取createdAt字段
      if (typeof value === 'object') {
        if (value.createdAt) {
          value = value.createdAt
        } else {
          // 如果没有createdAt字段，尝试转换为字符串
          value = JSON.stringify(value)
        }
      }
      
      // 确保我们有一个字符串
      let dateStr = value
      if (typeof value !== 'string') {
        dateStr = String(value)
      }
      
      // 最简单的处理方式：直接替换T为一个空格，并只返回日期和小时:分钟
      if (typeof dateStr === 'string') {
        const formatted = dateStr.replace('T', ' ')
        // 只返回日期和小时:分钟
        return formatted.substring(0, 16)
      }
      
      return '无效时间'
    }
  }
}
</script>

<style scoped>
.history-record {
  padding: 20px 0;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
}

.search-filter {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 15px;
}

.batch-actions {
  display: flex;
  gap: 10px;
}

.confidence-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.confidence-value {
  font-size: 14px;
  color: #606266;
  min-width: 60px;
  text-align: right;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 详情对话框样式 */
.record-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-row {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.detail-label {
  font-weight: 500;
  color: #606266;
  width: 120px;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #303133;
}

.disease-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-right: 20px;
}

.confidence {
  font-size: 16px;
  color: #67c23a;
  font-weight: bold;
}

.detail-section {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.suggestion-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  border-left: 3px solid #e6a23c;
}

.suggestion-item {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .search-form .el-form-item {
    margin-bottom: 10px;
  }
  
  .batch-actions {
    justify-content: center;
  }
  
  .pagination {
    justify-content: center;
  }
}
</style>