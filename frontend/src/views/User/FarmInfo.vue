<template>
  <div class="farm-info">
    <el-card>
      <div slot="header" class="card-header">
        <span>农场信息</span>
      </div>
      <!-- 农场基本信息 -->
      <el-descriptions :column="2" border>
        <el-descriptions-item label="农场名称">希望农场</el-descriptions-item>
        <el-descriptions-item label="所属用户">farmer</el-descriptions-item>
        <el-descriptions-item label="地理位置">江苏省南京市</el-descriptions-item>
        <el-descriptions-item label="面积">100亩</el-descriptions-item>
        <el-descriptions-item label="作物类型">水稻</el-descriptions-item>
        <el-descriptions-item label="创建时间">2024-01-01 00:00:00</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 农场种植信息 -->
    <el-card class="mt-20">
      <div slot="header" class="card-header">
        <span>种植信息</span>
      </div>
      <el-table
        v-loading="loading"
        :data="plantingList"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="种植ID" width="80"></el-table-column>
        <el-table-column prop="plantingTime" label="种植时间" width="180"></el-table-column>
        <el-table-column prop="seedType" label="种子类型" width="150"></el-table-column>
        <el-table-column prop="area" label="种植面积(亩)" width="120"></el-table-column>
        <el-table-column prop="expectedOutput" label="预期产量(kg)" width="120"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'FarmInfo',
  data() {
    return {
      loading: false,
      plantingList: []
    }
  },
  created() {
    this.loadPlantingInfo()
  },
  methods: {
    loadPlantingInfo() {
      this.loading = true
      // 从API加载真实数据
      const token = localStorage.getItem('token')
      fetch('/api/farms/planting/list', {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      .then(res => res.json())
      .then(data => {
        this.plantingList = data.data || []
        this.loading = false
      })
      .catch(() => {
        this.plantingList = []
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
.farm-info { padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.mt-20 { margin-top: 20px; }
</style>