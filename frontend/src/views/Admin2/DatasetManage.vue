<template>
  <div class="dataset-manage">
    <el-card>
      <div slot="header" class="card-header">
        <span>数据集管理</span>
        <el-button type="primary" size="small" @click="showUploadDialog">上传数据集</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="datasets"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="数据集名称"></el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="120">
          <template slot-scope="scope">
            {{ formatSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="viewDataset(scope.row)">查看</el-button>
            <el-button type="text" style="color: #F56C6C;" @click="deleteDataset(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <el-dialog title="上传数据集" :visible.sync="uploadDialogVisible" width="500px">
      <el-form :model="uploadForm" :rules="uploadRules" ref="uploadForm" label-width="100px">
        <el-form-item label="数据集名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入数据集名称"></el-input>
        </el-form-item>
        <el-form-item label="数据集类型" prop="type">
          <el-select v-model="uploadForm.type" placeholder="请选择类型" style="width: 100%;">
            <el-option label="图像" value="图像"></el-option>
            <el-option label="文本" value="文本"></el-option>
            <el-option label="时间序列" value="时间序列"></el-option>
            <el-option label="表格数据" value="表格数据"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="uploadForm.description" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="选择文件" prop="file">
          <el-upload
            ref="upload"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :file-list="fileList">
            <el-button size="small" type="primary">选择文件</el-button>
            <div slot="tip" class="el-upload__tip">支持上传 CSV、JSON、ZIP 等格式文件</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploading">上传</el-button>
      </span>
    </el-dialog>

    <el-dialog title="数据集详情" :visible.sync="detailDialogVisible" width="500px">
      <el-descriptions :column="1" border v-if="currentDataset">
        <el-descriptions-item label="ID">{{ currentDataset.id }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ currentDataset.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentDataset.type }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{ formatSize(currentDataset.size) }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentDataset.description }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ formatDate(currentDataset.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getDatasetList, uploadDataset, deleteDataset } from '@/api/user'

export default {
  name: 'DatasetManage',
  data() {
    return {
      datasets: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      uploadDialogVisible: false,
      detailDialogVisible: false,
      currentDataset: null,
      uploading: false,
      fileList: [],
      uploadForm: {
        name: '',
        type: '',
        description: '',
        file: null
      },
      uploadRules: {
        name: [{ required: true, message: '请输入数据集名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择数据集类型', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadDatasets()
  },
  methods: {
    async loadDatasets() {
      this.loading = true
      try {
        const res = await getDatasetList()
        if (res.data && res.data.data) {
          this.datasets = res.data.data
          this.total = this.datasets.length
        }
      } catch (error) {
        this.$message.error('加载数据集列表失败')
      } finally {
        this.loading = false
      }
    },
    showUploadDialog() {
      this.uploadDialogVisible = true
      this.uploadForm = {
        name: '',
        type: '',
        description: '',
        file: null
      }
      this.fileList = []
    },
    handleFileChange(file) {
      this.uploadForm.file = file.raw
    },
    async submitUpload() {
      this.$refs.uploadForm.validate(async (valid) => {
        if (!valid) return
        
        if (!this.uploadForm.file) {
          this.$message.warning('请选择要上传的文件')
          return
        }

        this.uploading = true
        try {
          const formData = new FormData()
          formData.append('file', this.uploadForm.file)
          formData.append('name', this.uploadForm.name)
          formData.append('type', this.uploadForm.type)
          formData.append('description', this.uploadForm.description || '')
          formData.append('uploaderId', this.$store.state.user?.userInfo?.id || 1)
          formData.append('farmId', this.$store.state.user?.userInfo?.farmId || 1)

          await uploadDataset(formData)
          this.$message.success('上传成功')
          this.uploadDialogVisible = false
          this.loadDatasets()
        } catch (error) {
          this.$message.error('上传失败: ' + (error.message || '未知错误'))
        } finally {
          this.uploading = false
        }
      })
    },
    viewDataset(row) {
      this.currentDataset = row
      this.detailDialogVisible = true
    },
    async deleteDataset(row) {
      try {
        await this.$confirm(`确定要删除数据集 "${row.name}" 吗？`, '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await deleteDataset(row.id)
        this.$message.success('删除成功')
        this.loadDatasets()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadDatasets()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadDatasets()
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
    }
  }
}
</script>

<style scoped>
.dataset-manage { padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.pagination { margin-top: 20px; text-align: right; }
</style>
