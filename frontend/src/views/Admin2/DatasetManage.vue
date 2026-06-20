<template>
  <div class="dataset-manage">
    <!-- 页面标题区 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-title-row">
          <h2>本地数据集管理</h2>
          <el-tag size="small" effect="plain" type="info" class="count-tag">共 {{ datasets.length }} 个</el-tag>
        </div>
        <p class="header-desc">上传和管理本地数据集，支持图像、表格、文本等多种格式</p>
      </div>
      <el-button type="primary" icon="el-icon-upload2" @click="showUploadDialog" class="upload-btn">上传数据集</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon-wrap blue">
          <i class="el-icon-folder-opened"></i>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ datasets.length }}</span>
          <span class="stat-label">数据集总数</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-body sub">
          <span class="stat-sub-value">{{ datasets.filter(d => d.status === 0).length }}</span>
          <span class="stat-label">待使用</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrap cyan">
          <i class="el-icon-coin"></i>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ formatSize(totalSize) }}</span>
          <span class="stat-label">总存储量</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-body sub">
          <span class="stat-sub-value">{{ datasets.length > 0 ? formatSize(Math.round(totalSize / datasets.length)) : '0 B' }}</span>
          <span class="stat-label">平均大小</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrap green">
          <i class="el-icon-circle-check"></i>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ datasets.filter(d => d.status === 1).length }}</span>
          <span class="stat-label">使用中</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-body sub">
          <span class="stat-sub-value">{{ datasets.filter(d => d.status === 2).length }}</span>
          <span class="stat-label">已归档</span>
        </div>
      </div>
    </div>

    <!-- 数据集列表 -->
    <el-card class="table-card" shadow="never">
      <div slot="header" class="card-header">
        <span class="card-title">数据集列表</span>
        <div class="header-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索名称或描述"
            prefix-icon="el-icon-search"
            size="small"
            clearable
            class="search-input"
          ></el-input>
          <el-select v-model="filterType" placeholder="类型" size="small" clearable class="type-filter">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value">
              <i :class="t.icon" style="margin-right: 6px;"></i>{{ t.label }}
            </el-option>
          </el-select>
          <el-select v-model="filterStatus" placeholder="状态" size="small" clearable class="status-filter">
            <el-option label="未使用" :value="0"></el-option>
            <el-option label="使用中" :value="1"></el-option>
            <el-option label="已归档" :value="2"></el-option>
          </el-select>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && filteredDatasets.length === 0" class="empty-state">
        <div class="empty-icon">
          <i class="el-icon-folder-opened"></i>
        </div>
        <p class="empty-title">{{ datasets.length === 0 ? '暂无数据集' : '没有匹配的结果' }}</p>
        <p class="empty-desc">{{ datasets.length === 0 ? '点击右上方按钮上传您的第一个数据集' : '尝试调整搜索条件或筛选器' }}</p>
        <el-button v-if="datasets.length === 0" type="primary" size="small" @click="showUploadDialog" plain>
          <i class="el-icon-upload2"></i> 上传数据集
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table v-else v-loading="loading" :data="paginatedDatasets" style="width: 100%" stripe :row-class-name="tableRowClass">
        <el-table-column prop="name" label="数据集名称" min-width="200">
          <template slot-scope="scope">
            <div class="dataset-name-cell">
              <div class="type-badge" :class="getTypeClass(scope.row.type)">
                <i :class="getTypeIcon(scope.row.type)"></i>
              </div>
              <div class="name-body">
                <span class="name-text">{{ scope.row.name }}</span>
                <span class="name-desc" v-if="scope.row.description">{{ scope.row.description.substring(0, 40) }}{{ scope.row.description.length > 40 ? '...' : '' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="110" align="center">
          <template slot-scope="scope">
            <span class="type-chip" :class="getTypeClass(scope.row.type)">{{ scope.row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="110" align="center">
          <template slot-scope="scope">
            <span class="size-text">{{ formatSize(scope.row.size) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <span class="status-dot" :class="'status-' + scope.row.status"></span>
            <span class="status-text">{{ getStatusText(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="170" align="center">
          <template slot-scope="scope">
            <span class="time-text">{{ formatDate(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template slot-scope="scope">
            <el-tooltip content="查看详情" placement="top" :open-delay="500">
              <el-button type="text" class="action-btn view-btn" @click="viewDataset(scope.row)">
                <i class="el-icon-view"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="下载数据" placement="top" :open-delay="500">
              <el-button type="text" class="action-btn download-btn" @click="downloadDataset(scope.row)">
                <i class="el-icon-download"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top" :open-delay="500">
              <el-button type="text" class="action-btn delete-btn" @click="deleteDataset(scope.row)">
                <i class="el-icon-delete"></i>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer" v-if="filteredDatasets.length > 0">
        <span class="footer-info">显示 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, filteredDatasets.length) }} / 共 {{ filteredDatasets.length }} 条</span>
        <el-pagination
          v-if="filteredDatasets.length > pageSize"
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next"
          :total="filteredDatasets.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog title="" :visible.sync="uploadDialogVisible" width="580px" :close-on-click-modal="false" custom-class="upload-dialog">
      <div class="dialog-header">
        <div class="dialog-icon">
          <i class="el-icon-upload2"></i>
        </div>
        <div>
          <h3 class="dialog-title">上传本地数据集</h3>
          <p class="dialog-subtitle">选择文件并填写数据集信息</p>
        </div>
      </div>
      <el-form :model="uploadForm" :rules="uploadRules" ref="uploadForm" label-width="100px" class="upload-form">
        <el-form-item label="数据集名称" prop="name">
          <el-input v-model="uploadForm.name" placeholder="请输入数据集名称" maxlength="100" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="数据集类型" prop="type">
          <el-radio-group v-model="uploadForm.type" class="type-radio-group">
            <el-radio-button label="图像"><i class="el-icon-picture-outline"></i> 图像</el-radio-button>
            <el-radio-button label="文本"><i class="el-icon-document"></i> 文本</el-radio-button>
            <el-radio-button label="时间序列"><i class="el-icon-data-line"></i> 时间序列</el-radio-button>
            <el-radio-button label="表格数据"><i class="el-icon-s-grid"></i> 表格</el-radio-button>
            <el-radio-button label="其他"><i class="el-icon-more"></i> 其他</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="uploadForm.description" placeholder="请简要描述数据集内容、用途等" :rows="3" maxlength="500" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="选择文件" prop="file" required>
          <div class="upload-area" :class="{ 'has-file': uploadForm.file }" @click="triggerUpload" @dragover.prevent="onDragOver" @dragleave="onDragLeave" @drop.prevent="onDrop">
            <template v-if="!uploadForm.file">
              <i class="el-icon-upload upload-area-icon"></i>
              <p class="upload-area-text">将文件拖到此处，或<em>点击选择文件</em></p>
              <p class="upload-area-hint">支持 CSV、JSON、ZIP、RAR、Excel、图像压缩包等，单文件最大 1GB</p>
            </template>
            <template v-else>
              <i class="el-icon-document-checked upload-area-icon success"></i>
              <p class="upload-area-text selected">{{ uploadForm.file.name }}</p>
              <p class="upload-area-hint">{{ formatSize(uploadForm.file.size) }} - 点击重新选择</p>
            </template>
          </div>
          <input ref="fileInput" type="file" style="display: none;" @change="onFileInput" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="uploadDialogVisible = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploading" class="submit-btn">
          <i class="el-icon-upload2" v-if="!uploading"></i> {{ uploading ? '上传中...' : '确认上传' }}
        </el-button>
      </span>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="" :visible.sync="detailDialogVisible" width="540px" custom-class="detail-dialog">
      <div class="dialog-header" v-if="currentDataset">
        <div class="dialog-icon" :class="getTypeClass(currentDataset.type)">
          <i :class="getTypeIcon(currentDataset.type)"></i>
        </div>
        <div>
          <h3 class="dialog-title">{{ currentDataset.name }}</h3>
          <p class="dialog-subtitle">{{ currentDataset.type }} 数据集</p>
        </div>
      </div>
      <div class="detail-body" v-if="currentDataset">
        <div class="detail-row">
          <span class="detail-label">状态</span>
          <span class="detail-value"><span class="status-dot" :class="'status-' + currentDataset.status"></span>{{ getStatusText(currentDataset.status) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">文件大小</span>
          <span class="detail-value">{{ formatSize(currentDataset.size) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">描述</span>
          <span class="detail-value desc">{{ currentDataset.description || '暂无描述' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">上传时间</span>
          <span class="detail-value">{{ formatDate(currentDataset.createdAt) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">更新时间</span>
          <span class="detail-value">{{ formatDate(currentDataset.updatedAt) }}</span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="detailDialogVisible = false">关闭</el-button>
        <el-button size="small" type="primary" @click="downloadDataset(currentDataset)" plain>
          <i class="el-icon-download"></i> 下载
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getDatasetList, uploadDataset, deleteDataset, getDatasetsByUploaderId } from '@/api/user'

export default {
  name: 'DatasetManage',
  data() {
    return {
      datasets: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchKeyword: '',
      filterType: '',
      filterStatus: '',
      uploadDialogVisible: false,
      detailDialogVisible: false,
      currentDataset: null,
      uploading: false,
      uploadForm: {
        name: '',
        type: '',
        description: '',
        file: null
      },
      uploadRules: {
        name: [{ required: true, message: '请输入数据集名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择数据集类型', trigger: 'change' }]
      },
      typeOptions: [
        { label: '图像', value: '图像', icon: 'el-icon-picture-outline' },
        { label: '文本', value: '文本', icon: 'el-icon-document' },
        { label: '时间序列', value: '时间序列', icon: 'el-icon-data-line' },
        { label: '表格数据', value: '表格数据', icon: 'el-icon-s-grid' },
        { label: '其他', value: '其他', icon: 'el-icon-more' }
      ]
    }
  },
  computed: {
    filteredDatasets() {
      let list = this.datasets
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase()
        list = list.filter(d => d.name.toLowerCase().includes(kw) || (d.description && d.description.toLowerCase().includes(kw)))
      }
      if (this.filterType) {
        list = list.filter(d => d.type === this.filterType)
      }
      if (this.filterStatus !== '' && this.filterStatus !== null) {
        list = list.filter(d => d.status === this.filterStatus)
      }
      return list
    },
    paginatedDatasets() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredDatasets.slice(start, start + this.pageSize)
    },
    totalSize() {
      return this.datasets.reduce((sum, d) => sum + (d.size || 0), 0)
    }
  },
  created() {
    this.loadDatasets()
  },
  methods: {
    async loadDatasets() {
      this.loading = true
      try {
        const userInfo = this.$store.state.user?.userInfo
        const userId = userInfo?.id
        let res
        if (userId) {
          res = await getDatasetsByUploaderId(userId)
        } else {
          res = await getDatasetList()
        }
        if (res.data) {
          const data = res.data.data || res.data
          this.datasets = Array.isArray(data) ? data : []
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
      this.uploadForm = { name: '', type: '', description: '', file: null }
      this.$nextTick(() => {
        if (this.$refs.uploadForm) this.$refs.uploadForm.clearValidate()
      })
    },
    triggerUpload() {
      this.$refs.fileInput.click()
    },
    onFileInput(e) {
      const file = e.target.files[0]
      if (file) {
        if (file.size > 1024 * 1024 * 1024) {
          this.$message.error('文件大小不能超过 1GB')
          return
        }
        this.uploadForm.file = file
      }
      e.target.value = ''
    },
    onDragOver(e) {
      e.currentTarget.classList.add('drag-over')
    },
    onDragLeave(e) {
      e.currentTarget.classList.remove('drag-over')
    },
    onDrop(e) {
      e.currentTarget.classList.remove('drag-over')
      const file = e.dataTransfer.files[0]
      if (file) {
        if (file.size > 1024 * 1024 * 1024) {
          this.$message.error('文件大小不能超过 1GB')
          return
        }
        this.uploadForm.file = file
      }
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
          const userInfo = this.$store.state.user?.userInfo
          const formData = new FormData()
          formData.append('file', this.uploadForm.file)
          formData.append('name', this.uploadForm.name)
          formData.append('type', this.uploadForm.type)
          formData.append('description', this.uploadForm.description || '')
          formData.append('uploaderId', userInfo?.id || 0)
          formData.append('farmId', userInfo?.farmId || 0)
          await uploadDataset(formData)
          this.$message.success('数据集上传成功')
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
    downloadDataset(row) {
      if (!row.path) {
        this.$message.warning('文件路径不存在')
        return
      }
      const link = document.createElement('a')
      link.href = row.path
      link.download = row.name
      link.click()
    },
    async deleteDataset(row) {
      try {
        await this.$confirm(`确定要删除数据集 "${row.name}" 吗？删除后无法恢复。`, '确认删除', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: 'el-button--danger'
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
      this.currentPage = 1
    },
    handleCurrentChange(val) {
      this.currentPage = val
    },
    tableRowClass({ rowIndex }) {
      return rowIndex % 2 === 0 ? 'row-even' : 'row-odd'
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
    getTypeClass(type) {
      const map = { '图像': 'type-image', '文本': 'type-text', '时间序列': 'type-series', '表格数据': 'type-table', '其他': 'type-other' }
      return map[type] || 'type-other'
    },
    getTypeIcon(type) {
      const icons = { '图像': 'el-icon-picture-outline', '文本': 'el-icon-document', '时间序列': 'el-icon-data-line', '表格数据': 'el-icon-s-grid', '其他': 'el-icon-folder' }
      return icons[type] || 'el-icon-folder'
    },
    getStatusText(status) {
      const texts = { 0: '未使用', 1: '使用中', 2: '已归档' }
      return texts[status] || '未知'
    }
  }
}
</script>

<style scoped>
.dataset-manage { padding: 0; }

/* ===== 页面标题 ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}
.header-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: #0F172A;
  font-weight: 700;
  letter-spacing: -0.3px;
}
.count-tag { font-size: 12px; }
.header-desc {
  margin: 6px 0 0;
  font-size: 13px;
  color: #94A3B8;
}
.upload-btn {
  height: 38px;
  padding: 0 20px;
  border-radius: 8px;
  font-weight: 500;
}

/* ===== 统计卡片 ===== */
.stat-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  transition: box-shadow 0.2s;
}
.stat-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.stat-icon-wrap {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.stat-icon-wrap.blue { background: #EFF6FF; color: #00539B; }
.stat-icon-wrap.cyan { background: #F0F9FF; color: #0EA5E9; }
.stat-icon-wrap.green { background: #F0FDF4; color: #16A34A; }
.stat-body {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 3px;
}
.stat-divider {
  width: 1px;
  height: 32px;
  background: #E2E8F0;
  margin: 0 8px 0 4px;
  flex-shrink: 0;
}
.stat-body.sub {
  margin-left: 4px;
}
.stat-sub-value {
  font-size: 16px;
  font-weight: 600;
  color: #475569;
  line-height: 1.2;
}

/* ===== 表格卡片 ===== */
.table-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
}
.table-card >>> .el-card__header {
  padding: 16px 24px;
  border-bottom: 1px solid #F1F5F9;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #0F172A;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.search-input { width: 200px; }
.type-filter { width: 110px; }
.status-filter { width: 100px; }

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}
.empty-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #F8FAFC;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
.empty-icon i {
  font-size: 32px;
  color: #CBD5E1;
}
.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 6px;
}
.empty-desc {
  font-size: 13px;
  color: #94A3B8;
  margin: 0 0 20px;
}

/* ===== 表格行 ===== */
.dataset-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.type-badge {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.type-badge.type-image { background: #EFF6FF; color: #2563EB; }
.type-badge.type-text { background: #F0FDF4; color: #16A34A; }
.type-badge.type-series { background: #FFFBEB; color: #D97706; }
.type-badge.type-table { background: #F5F3FF; color: #7C3AED; }
.type-badge.type-other { background: #F8FAFC; color: #64748B; }
.name-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.name-text {
  font-size: 14px;
  font-weight: 500;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.name-desc {
  font-size: 12px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}

.type-chip {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.type-chip.type-image { background: #EFF6FF; color: #2563EB; }
.type-chip.type-text { background: #F0FDF4; color: #16A34A; }
.type-chip.type-series { background: #FFFBEB; color: #D97706; }
.type-chip.type-table { background: #F5F3FF; color: #7C3AED; }
.type-chip.type-other { background: #F8FAFC; color: #64748B; }

.size-text {
  color: #475569;
  font-variant-numeric: tabular-nums;
  font-size: 13px;
}

.status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
.status-dot.status-0 { background: #94A3B8; }
.status-dot.status-1 { background: #16A34A; }
.status-dot.status-2 { background: #D97706; }
.status-text {
  font-size: 13px;
  color: #475569;
  vertical-align: middle;
}

.time-text {
  font-size: 13px;
  color: #64748B;
}

/* ===== 操作按钮 ===== */
.action-btn {
  padding: 4px 8px;
  font-size: 16px;
}
.view-btn { color: #00539B !important; }
.view-btn:hover { color: #0EA5E9 !important; }
.download-btn { color: #475569 !important; }
.download-btn:hover { color: #00539B !important; }
.delete-btn { color: #94A3B8 !important; }
.delete-btn:hover { color: #EF4444 !important; }

/* ===== 表格底部 ===== */
.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0 4px;
}
.footer-info {
  font-size: 13px;
  color: #94A3B8;
}

/* ===== 上传对话框 ===== */
.upload-dialog >>> .el-dialog__header { display: none; }
.upload-dialog >>> .el-dialog__body { padding: 24px 28px 8px; }
.upload-dialog >>> .el-dialog__footer { padding: 12px 28px 20px; }
.dialog-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}
.dialog-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #EFF6FF;
  color: #00539B;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.dialog-icon.type-image { background: #EFF6FF; color: #2563EB; }
.dialog-icon.type-text { background: #F0FDF4; color: #16A34A; }
.dialog-icon.type-series { background: #FFFBEB; color: #D97706; }
.dialog-icon.type-table { background: #F5F3FF; color: #7C3AED; }
.dialog-icon.type-other { background: #F8FAFC; color: #64748B; }
.dialog-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #0F172A;
}
.dialog-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #94A3B8;
}

.upload-form >>> .el-form-item__label {
  font-weight: 500;
  color: #334155;
}

.type-radio-group >>> .el-radio-button__inner {
  padding: 8px 14px;
  font-size: 13px;
}
.type-radio-group >>> .el-radio-button__inner i {
  margin-right: 4px;
}

/* 自定义拖拽上传区域 */
.upload-area {
  border: 2px dashed #CBD5E1;
  border-radius: 12px;
  padding: 32px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #FAFBFC;
}
.upload-area:hover,
.upload-area.drag-over {
  border-color: #00539B;
  background: #F8FBFF;
}
.upload-area.has-file {
  border-color: #16A34A;
  background: #F0FDF4;
  border-style: solid;
}
.upload-area-icon {
  font-size: 36px;
  color: #94A3B8;
  margin-bottom: 8px;
}
.upload-area-icon.success { color: #16A34A; }
.upload-area-text {
  font-size: 14px;
  color: #475569;
  margin: 0 0 4px;
}
.upload-area-text em {
  color: #00539B;
  font-style: normal;
  font-weight: 500;
}
.upload-area-text.selected {
  color: #16A34A;
  font-weight: 500;
}
.upload-area-hint {
  font-size: 12px;
  color: #94A3B8;
  margin: 0;
}

.cancel-btn { border-radius: 8px; }
.submit-btn { border-radius: 8px; font-weight: 500; }

/* ===== 详情对话框 ===== */
.detail-dialog >>> .el-dialog__header { display: none; }
.detail-dialog >>> .el-dialog__body { padding: 24px 28px 8px; }
.detail-dialog >>> .el-dialog__footer { padding: 12px 28px 20px; }

.detail-body {
  margin-top: 20px;
}
.detail-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #F1F5F9;
}
.detail-row:last-child { border-bottom: none; }
.detail-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 13px;
  color: #94A3B8;
}
.detail-value {
  font-size: 14px;
  color: #0F172A;
  font-weight: 500;
}
.detail-value.desc {
  font-weight: 400;
  color: #475569;
  line-height: 1.6;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .stat-cards { flex-direction: column; }
  .header-actions { flex-direction: column; gap: 8px; }
  .stat-divider { display: none; }
  .stat-body.sub { display: none; }
}
</style>
