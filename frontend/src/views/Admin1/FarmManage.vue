<template>
  <div class="farm-manage">
    <el-card>
      <div slot="header" class="card-header">
        <span>农场管理</span>
      </div>
      <!-- 搜索和筛选 -->
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="农场名称">
            <el-input v-model="searchForm.farmName" placeholder="请输入农场名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属用户">
            <el-input v-model="searchForm.ownerName" placeholder="请输入所属用户" clearable></el-input>
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
        :data="farmList"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="农场ID" width="80"></el-table-column>
        <el-table-column prop="farmName" label="农场名称" width="180"></el-table-column>
        <el-table-column prop="ownerName" label="所属用户" width="120"></el-table-column>
        <el-table-column prop="location" label="地理位置" width="200"></el-table-column>
        <el-table-column prop="area" label="面积(亩)" width="100"></el-table-column>
        <el-table-column prop="cropType" label="作物类型" width="120"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
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
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'FarmManage',
  data() {
    return {
      loading: false,
      searchForm: {
        farmName: '',
        ownerName: ''
      },
      farmList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.loadFarms()
  },
  methods: {
    loadFarms() {
      this.loading = true
      // 从API加载真实数据
      const token = localStorage.getItem('token')
      fetch('/api/farms/list', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      .then(res => res.json())
      .then(data => {
        this.farmList = data.data || []
        this.total = this.farmList.length
        this.loading = false
      })
      .catch(() => {
        this.farmList = []
        this.total = 0
        this.loading = false
      })
    },
    handleSearch() {
      this.currentPage = 1
      this.loadFarms()
    },
    resetSearch() {
      this.searchForm = { farmName: '', ownerName: '' }
      this.currentPage = 1
      this.loadFarms()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.loadFarms()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.loadFarms()
    }
  }
}
</script>

<style scoped>
.farm-manage { padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.search-section { margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>