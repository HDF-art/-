<template>
  <div class="model-list-container">
    <el-card>
      <div slot="header" class="card-header">
        <span>模型列表</span>
        <el-button type="primary" size="small" @click="loadModels">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>
      
      <el-table :data="modelList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模型名称" width="180" />
        <el-table-column prop="type" label="模型类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.type === '全局模型' ? 'primary' : 'success'">
              {{ scope.row.type || '全局模型' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskType" label="任务类型" width="140">
          <template slot-scope="scope">
            <el-tag :type="getTaskTagType(scope.row.taskType)" size="small">
              {{ getTaskText(scope.row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inputType" label="输入类型" width="100">
          <template slot-scope="scope">
            {{ getInputText(scope.row.inputType) }}
          </template>
        </el-table-column>
        <el-table-column prop="accuracy" label="准确率" width="100">
          <template slot-scope="scope">
            {{ scope.row.accuracy ? (scope.row.accuracy * 100).toFixed(2) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="precision" label="精确率" width="100">
          <template slot-scope="scope">
            {{ scope.row.precision ? (scope.row.precision * 100).toFixed(2) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="recall" label="召回率" width="100">
          <template slot-scope="scope">
            {{ scope.row.recall ? (scope.row.recall * 100).toFixed(2) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="f1Score" label="F1分数" width="100">
          <template slot-scope="scope">
            {{ scope.row.f1Score ? (scope.row.f1Score * 100).toFixed(2) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="isDefault" label="默认模型" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isDefault === 1" type="danger">默认</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
      </el-table>
      
      <el-pagination
        v-if="total > 0"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      />
      
      <el-empty v-if="!loading && modelList.length === 0" description="暂无模型数据" />
    </el-card>
  </div>
</template>

<script>
import { getAllModels } from '@/api/user'

export default {
  name: 'ModelList',
  data() {
    return {
      allModels: [],
      modelList: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  mounted() {
    this.loadModels()
  },
  methods: {
    async loadModels() {
      this.loading = true
      try {
        const res = await getAllModels()
        const data = res.data || res || []
        this.allModels = Array.isArray(data) ? data : []
        this.total = this.allModels.length
        this.applyPagination()
      } catch (e) {
        console.error('加载模型列表失败', e)
        this.allModels = []
        this.modelList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    applyPagination() {
      const start = (this.currentPage - 1) * this.pageSize
      this.modelList = this.allModels.slice(start, start + this.pageSize)
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.applyPagination()
    },
    handlePageChange(val) {
      this.currentPage = val
      this.applyPagination()
    },
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
    },
    getTaskText(taskType) {
      const map = {
        pest_disease: '病虫害识别',
        strawberry_ripeness: '草莓成熟度检测',
        env_prediction: '环境预测'
      }
      return map[taskType] || taskType || '-'
    },
    getTaskTagType(taskType) {
      const map = {
        pest_disease: 'danger',
        strawberry_ripeness: 'warning',
        env_prediction: 'primary'
      }
      return map[taskType] || 'info'
    },
    getInputText(inputType) {
      const map = { image: '图片', time_series: '时间序列' }
      return map[inputType] || inputType || '-'
    }
  }
}
</script>

<style scoped>
.model-list-container { padding: 10px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.pagination { margin-top: 20px; text-align: right; }
</style>
