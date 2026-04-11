<template>
  <div class="data-circulation">
    <el-card>
      <div slot="header" class="card-header">
        <span>数据流通</span>
        <el-button type="primary" size="small" @click="showPublishDialog">发布数据</el-button>
      </div>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="可交易数据" name="available">
          <el-table
            v-loading="loading"
            :data="availableData"
            style="width: 100%"
            stripe
          >
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="datasetName" label="数据集名称"></el-table-column>
            <el-table-column prop="datasetType" label="类型" width="100">
              <template slot-scope="scope">
                <el-tag :type="getTypeTag(scope.row.datasetType)">{{ scope.row.datasetType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="datasetSize" label="大小" width="120">
              <template slot-scope="scope">
                {{ formatSize(scope.row.datasetSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template slot-scope="scope">
                <span style="color: #F56C6C; font-weight: bold;">¥{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="180">
              <template slot-scope="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" style="color: #67C23A;" @click="buyData(scope.row)">购买</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的发布" name="published">
          <el-table
            v-loading="loading"
            :data="myPublishedData"
            style="width: 100%"
            stripe
          >
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="datasetName" label="数据集名称"></el-table-column>
            <el-table-column prop="datasetType" label="类型" width="100">
              <template slot-scope="scope">
                <el-tag :type="getTypeTag(scope.row.datasetType)">{{ scope.row.datasetType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template slot-scope="scope">
                <span style="color: #F56C6C; font-weight: bold;">¥{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusTag(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="发布时间" width="180">
              <template slot-scope="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" @click="viewDetail(scope.row)">查看</el-button>
                <el-button type="text" style="color: #F56C6C;" @click="cancelPublish(scope.row)" v-if="scope.row.status === 0">取消</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog title="发布数据" :visible.sync="publishDialogVisible" width="500px">
      <el-form :model="publishForm" :rules="publishRules" ref="publishForm" label-width="100px">
        <el-form-item label="数据集名称" prop="datasetName">
          <el-input v-model="publishForm.datasetName" placeholder="请输入数据集名称"></el-input>
        </el-form-item>
        <el-form-item label="数据集类型" prop="datasetType">
          <el-select v-model="publishForm.datasetType" placeholder="请选择类型" style="width: 100%;">
            <el-option label="图像" value="图像"></el-option>
            <el-option label="文本" value="文本"></el-option>
            <el-option label="时间序列" value="时间序列"></el-option>
            <el-option label="表格数据" value="表格数据"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据大小" prop="datasetSize">
          <el-input-number v-model="publishForm.datasetSize" :min="0" placeholder="字节" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="publishForm.price" :min="0" :precision="2" placeholder="元" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="publishForm.description" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="关联数据集">
          <el-select v-model="publishForm.datasetId" placeholder="可选：关联已有数据集" style="width: 100%;" clearable>
            <el-option v-for="item in datasets" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPublish" :loading="publishing">发布</el-button>
      </span>
    </el-dialog>

    <el-dialog title="数据详情" :visible.sync="detailDialogVisible" width="500px">
      <el-descriptions :column="1" border v-if="currentData">
        <el-descriptions-item label="ID">{{ currentData.id }}</el-descriptions-item>
        <el-descriptions-item label="数据集名称">{{ currentData.datasetName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentData.datasetType }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{ formatSize(currentData.datasetSize) }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ currentData.price }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentData.description }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(currentData.status) }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatDate(currentData.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getAvailableData, 
  getDataByPublisherId, 
  publishDataCirculation, 
  updateDataCirculationStatus,
  getDatasetList,
  createTransaction
} from '@/api/user'

export default {
  name: 'DataCirculation',
  data() {
    return {
      activeTab: 'available',
      availableData: [],
      myPublishedData: [],
      datasets: [],
      loading: false,
      publishDialogVisible: false,
      detailDialogVisible: false,
      currentData: null,
      publishing: false,
      publishForm: {
        datasetName: '',
        datasetType: '',
        datasetSize: 0,
        price: 0,
        description: '',
        datasetId: null
      },
      publishRules: {
        datasetName: [{ required: true, message: '请输入数据集名称', trigger: 'blur' }],
        datasetType: [{ required: true, message: '请选择数据集类型', trigger: 'change' }],
        price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadAvailableData()
    this.loadDatasets()
  },
  methods: {
    async loadAvailableData() {
      this.loading = true
      try {
        const res = await getAvailableData()
        if (res.data && res.data.data) {
          this.availableData = res.data.data
        }
      } catch (error) {
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    async loadMyPublishedData() {
      this.loading = true
      try {
        const userId = this.$store.state.user?.userInfo?.id
        if (userId) {
          const res = await getDataByPublisherId(userId)
          if (res.data && res.data.data) {
            this.myPublishedData = res.data.data
          }
        }
      } catch (error) {
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    async loadDatasets() {
      try {
        const res = await getDatasetList()
        if (res.data && res.data.data) {
          this.datasets = res.data.data
        }
      } catch (error) {
        console.error('加载数据集列表失败')
      }
    },
    handleTabClick(tab) {
      if (tab.name === 'published') {
        this.loadMyPublishedData()
      } else {
        this.loadAvailableData()
      }
    },
    showPublishDialog() {
      this.publishDialogVisible = true
      this.publishForm = {
        datasetName: '',
        datasetType: '',
        datasetSize: 0,
        price: 0,
        description: '',
        datasetId: null
      }
    },
    async submitPublish() {
      this.$refs.publishForm.validate(async (valid) => {
        if (!valid) return

        this.publishing = true
        try {
          const userId = this.$store.state.user?.userInfo?.id
          const farmId = this.$store.state.user?.userInfo?.farmId
          
          const data = {
            ...this.publishForm,
            publisherId: userId,
            farmId: farmId,
            status: 0
          }

          await publishDataCirculation(data)
          this.$message.success('发布成功')
          this.publishDialogVisible = false
          this.loadAvailableData()
        } catch (error) {
          this.$message.error('发布失败: ' + (error.message || '未知错误'))
        } finally {
          this.publishing = false
        }
      })
    },
    async buyData(row) {
      try {
        await this.$confirm(`确定要购买 "${row.datasetName}" 吗？价格：¥${row.price}`, '购买确认', {
          confirmButtonText: '确定购买',
          cancelButtonText: '取消',
          type: 'info'
        })

        const userId = this.$store.state.user?.userInfo?.id
        await createTransaction({
          dataCirculationId: row.id,
          buyerId: userId,
          sellerId: row.publisherId,
          amount: row.price,
          status: 0
        })
        
        this.$message.success('购买请求已发送')
        this.loadAvailableData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('购买失败')
        }
      }
    },
    viewDetail(row) {
      this.currentData = row
      this.detailDialogVisible = true
    },
    async cancelPublish(row) {
      try {
        await this.$confirm('确定要取消发布吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await updateDataCirculationStatus(row.id, 3)
        this.$message.success('已取消发布')
        this.loadMyPublishedData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败')
        }
      }
    },
    formatSize(bytes) {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    getTypeTag(type) {
      const types = {
        '图像': 'primary',
        '文本': 'success',
        '时间序列': 'warning',
        '表格数据': 'info',
        '其他': ''
      }
      return types[type] || ''
    },
    getStatusTag(status) {
      const statuses = {
        0: 'warning',
        1: 'primary',
        2: 'success',
        3: 'info'
      }
      return statuses[status] || ''
    },
    getStatusText(status) {
      const texts = {
        0: '待交易',
        1: '交易中',
        2: '已完成',
        3: '已取消'
      }
      return texts[status] || '未知'
    }
  }
}
</script>

<style scoped>
.data-circulation { padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
</style>
