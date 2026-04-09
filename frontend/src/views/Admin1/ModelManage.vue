<template>
  <div class="model-manage">
    <el-card>
      <div slot="header" class="card-header">
        <span>模型管理</span>
      </div>
      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="modelList"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="模型ID" width="80"></el-table-column>
        <el-table-column prop="modelName" label="模型名称" width="200"></el-table-column>
        <el-table-column prop="modelType" label="模型类型" width="120"></el-table-column>
        <el-table-column prop="accuracy" label="准确率" width="100">
          <template slot-scope="scope">
            {{ (scope.row.accuracy * 100).toFixed(2) }}%
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
              {{ scope.row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ModelManage',
  data() {
    return {
      loading: false,
      modelList: []
    }
  },
  created() {
    this.loadModels()
  },
  methods: {
    loadModels() {
      this.loading = true
      // 从API加载真实数据
      const token = localStorage.getItem('token')
      fetch('/api/models/list', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      .then(res => res.json())
      .then(data => {
        this.modelList = data.data || []
        this.loading = false
      })
      .catch(() => {
        this.modelList = []
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
.model-manage { padding: 10px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
</style>