<template>
  <div class="local-data-manage">
    <div class="page-header">
      <h2>本地数据管理</h2>
      <p class="subtitle">监控和管理本地客户端数据，数据永不离开你的硬盘</p>
    </div>

    <el-row :gutter="20" class="status-cards">
      <el-col :span="6">
        <el-card class="status-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon connected">
              <i class="el-icon-connection"></i>
            </div>
            <div class="card-info">
              <div class="card-value">{{ connectedClients }}</div>
              <div class="card-label">已连接客户端</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon datasets">
              <i class="el-icon-folder-opened"></i>
            </div>
            <div class="card-info">
              <div class="card-value">{{ totalDatasets }}</div>
              <div class="card-label">本地数据集</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon training">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="card-info">
              <div class="card-value">{{ runningTasks }}</div>
              <div class="card-label">运行中任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon storage">
              <i class="el-icon-files"></i>
            </div>
            <div class="card-info">
              <div class="card-value">{{ formatSize(totalStorage) }}</div>
              <div class="card-label">数据总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="客户端管理" name="clients">
        <div class="tab-header">
          <el-button type="primary" icon="el-icon-plus" @click="showConnectDialog">
            连接新客户端
          </el-button>
          <el-button icon="el-icon-refresh" @click="refreshClients">刷新</el-button>
        </div>

        <el-table :data="clients" v-loading="loading" style="width: 100%">
          <el-table-column prop="clientId" label="客户端ID" width="200">
            <template slot-scope="scope">
              <el-tag size="small">{{ scope.row.clientId }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="clientName" label="名称" width="150">
            <template slot-scope="scope">
              {{ scope.row.clientName || '未命名' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 'connected' ? 'success' : 'info'" size="small">
                {{ scope.row.status === 'connected' ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="platform" label="平台" width="120"></el-table-column>
          <el-table-column prop="datasetsCount" label="数据集" width="80">
            <template slot-scope="scope">
              <el-badge :value="scope.row.datasetsCount || 0" type="primary"></el-badge>
            </template>
          </el-table-column>
          <el-table-column prop="runningTasks" label="运行任务" width="80">
            <template slot-scope="scope">
              <el-badge :value="scope.row.runningTasks || 0" type="warning"></el-badge>
            </template>
          </el-table-column>
          <el-table-column prop="lastHeartbeat" label="最后心跳" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.lastHeartbeat) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="viewClient(scope.row)">
                详情
              </el-button>
              <el-button size="mini" type="text" @click="viewDatasets(scope.row)">
                数据集
              </el-button>
              <el-button size="mini" type="text" @click="startTraining(scope.row)">
                训练
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="数据集监控" name="datasets">
        <div class="tab-header">
          <el-input
            v-model="datasetSearch"
            placeholder="搜索数据集"
            prefix-icon="el-icon-search"
            style="width: 300px"
            clearable
          ></el-input>
          <el-button icon="el-icon-refresh" @click="refreshDatasets">刷新</el-button>
        </div>

        <el-table :data="filteredDatasets" v-loading="datasetsLoading" style="width: 100%">
          <el-table-column prop="name" label="文件名" min-width="200">
            <template slot-scope="scope">
              <i :class="getFileIcon(scope.row.fileType)" class="file-icon"></i>
              {{ scope.row.name }}
            </template>
          </el-table-column>
          <el-table-column prop="clientId" label="所属客户端" width="150">
            <template slot-scope="scope">
              <el-tag size="small" type="info">{{ scope.row.clientId }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="size" label="大小" width="100">
            <template slot-scope="scope">
              {{ formatSize(scope.row.size) }}
            </template>
          </el-table-column>
          <el-table-column prop="fileType" label="类型" width="80">
            <template slot-scope="scope">
              <el-tag size="small">{{ scope.row.fileType.toUpperCase() }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rowCount" label="行数" width="100">
            <template slot-scope="scope">
              {{ scope.row.rowCount ? scope.row.rowCount.toLocaleString() : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="columnCount" label="列数" width="80">
            <template slot-scope="scope">
              {{ scope.row.columnCount || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="modifiedAt" label="修改时间" width="160">
            <template slot-scope="scope">
              {{ formatTime(scope.row.modifiedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="previewDataset(scope.row)">
                预览
              </el-button>
              <el-button size="mini" type="text" @click="trainWithDataset(scope.row)">
                训练
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="训练任务" name="training">
        <div class="tab-header">
          <el-button type="primary" icon="el-icon-plus" @click="showTrainingDialog">
            新建训练任务
          </el-button>
          <el-button icon="el-icon-refresh" @click="refreshTraining">刷新</el-button>
        </div>

        <el-table :data="trainingTasks" v-loading="trainingLoading" style="width: 100%">
          <el-table-column prop="taskId" label="任务ID" width="200">
            <template slot-scope="scope">
              <el-tag size="small">{{ scope.row.taskId }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="clientId" label="客户端" width="150">
            <template slot-scope="scope">
              <el-tag size="small" type="info">{{ scope.row.clientId }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="progress" label="进度" width="200">
            <template slot-scope="scope">
              <el-progress
                :percentage="scope.row.progress"
                :status="scope.row.status === 'completed' ? 'success' : scope.row.status === 'failed' ? 'exception' : ''"
              ></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="currentEpoch" label="Epoch" width="100">
            <template slot-scope="scope">
              {{ scope.row.currentEpoch }} / {{ scope.row.totalEpochs }}
            </template>
          </el-table-column>
          <el-table-column prop="loss" label="Loss" width="100">
            <template slot-scope="scope">
              {{ scope.row.loss ? scope.row.loss.toFixed(4) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="accuracy" label="Accuracy" width="100">
            <template slot-scope="scope">
              {{ scope.row.accuracy ? (scope.row.accuracy * 100).toFixed(2) + '%' : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                @click="stopTraining(scope.row)"
                :disabled="scope.row.status !== 'running'"
              >
                停止
              </el-button>
              <el-button size="mini" type="text" @click="viewTrainingLog(scope.row)">
                日志
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="系统信息" name="system">
        <el-row :gutter="20" v-if="selectedClient">
          <el-col :span="12">
            <el-card class="info-card">
              <div slot="header">
                <span>系统配置</span>
              </div>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="操作系统">
                  {{ systemInfo.os }} {{ systemInfo.osVersion }}
                </el-descriptions-item>
                <el-descriptions-item label="Python版本">
                  {{ systemInfo.pythonVersion }}
                </el-descriptions-item>
                <el-descriptions-item label="CPU核心数">
                  {{ systemInfo.cpuCount }}
                </el-descriptions-item>
                <el-descriptions-item label="总内存">
                  {{ formatSize(systemInfo.memoryTotal) }}
                </el-descriptions-item>
                <el-descriptions-item label="可用内存">
                  {{ formatSize(systemInfo.memoryAvailable) }}
                </el-descriptions-item>
                <el-descriptions-item label="数据目录">
                  {{ systemInfo.dataDir }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="info-card">
              <div slot="header">
                <span>磁盘使用</span>
              </div>
              <div class="disk-usage">
                <el-progress
                  type="dashboard"
                  :percentage="diskUsagePercent"
                  :color="diskUsageColor"
                ></el-progress>
                <div class="disk-info">
                  <p>总容量: {{ formatSize(systemInfo.diskTotal) }}</p>
                  <p>已使用: {{ formatSize(systemInfo.diskTotal - systemInfo.diskFree) }}</p>
                  <p>可用空间: {{ formatSize(systemInfo.diskFree) }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="请选择一个客户端查看系统信息"></el-empty>
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="连接新客户端" :visible.sync="connectDialogVisible" width="600px">
      <div class="connect-guide">
        <el-alert
          title="连接说明"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        >
          本地客户端是一个运行在你电脑上的Python程序，它会监控本地数据并与平台通信。
        </el-alert>

        <el-steps :active="activeStep" finish-status="success" simple>
          <el-step title="安装依赖"></el-step>
          <el-step title="启动客户端"></el-step>
          <el-step title="连接成功"></el-step>
        </el-steps>

        <div class="step-content" v-if="activeStep === 0">
          <p>1. 下载客户端代码：</p>
          <el-input
            type="textarea"
            :rows="2"
            value="cd ~/农业大数据联合建模平台/local-client"
            readonly
          ></el-input>
          <p style="margin-top: 15px">2. 安装依赖：</p>
          <el-input
            type="textarea"
            :rows="2"
            value="pip install -r requirements.txt"
            readonly
          ></el-input>
        </div>

        <div class="step-content" v-if="activeStep === 1">
          <p>启动客户端并连接到平台：</p>
          <el-input
            type="textarea"
            :rows="3"
            :value="`python local_client.py --token ${userToken} --client-id ${newClientId}`"
            readonly
          ></el-input>
          <p style="margin-top: 15px">
            <el-alert type="warning" :closable="false">
              请确保本地端口 8080 未被占用
            </el-alert>
          </p>
        </div>

        <div class="step-content" v-if="activeStep === 2">
          <el-result icon="success" title="连接成功" subTitle="客户端已成功连接到平台">
          </el-result>
        </div>
      </div>

      <span slot="footer">
        <el-button @click="activeStep--" :disabled="activeStep === 0">上一步</el-button>
        <el-button type="primary" @click="activeStep++" v-if="activeStep < 2">下一步</el-button>
        <el-button type="primary" @click="connectDialogVisible = false" v-else>完成</el-button>
      </span>
    </el-dialog>

    <el-dialog title="新建训练任务" :visible.sync="trainingDialogVisible" width="600px">
      <el-form :model="trainingForm" label-width="120px">
        <el-form-item label="选择客户端">
          <el-select v-model="trainingForm.clientId" placeholder="请选择客户端" style="width: 100%">
            <el-option
              v-for="client in connectedClientsList"
              :key="client.clientId"
              :label="client.clientName || client.clientId"
              :value="client.clientId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择数据集">
          <el-select v-model="trainingForm.datasetPath" placeholder="请选择数据集" style="width: 100%">
            <el-option
              v-for="dataset in clientDatasets"
              :key="dataset.path"
              :label="dataset.name"
              :value="dataset.path"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="模型类型">
          <el-select v-model="trainingForm.modelType" placeholder="请选择模型" style="width: 100%">
            <el-option label="MLP (多层感知机)" value="mlp"></el-option>
            <el-option label="CNN (卷积神经网络)" value="cnn"></el-option>
            <el-option label="LSTM (长短期记忆网络)" value="lstm"></el-option>
            <el-option label="Random Forest (随机森林)" value="rf"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="训练轮数">
          <el-input-number v-model="trainingForm.epochs" :min="1" :max="1000"></el-input-number>
        </el-form-item>
        <el-form-item label="批次大小">
          <el-input-number v-model="trainingForm.batchSize" :min="1" :max="1024"></el-input-number>
        </el-form-item>
        <el-form-item label="学习率">
          <el-input-number
            v-model="trainingForm.learningRate"
            :min="0.0001"
            :max="1"
            :step="0.0001"
            :precision="4"
          ></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="trainingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTraining">开始训练</el-button>
      </span>
    </el-dialog>

    <el-dialog title="数据集预览" :visible.sync="previewDialogVisible" width="800px">
      <div v-if="previewData">
        <el-descriptions :column="3" border style="margin-bottom: 20px">
          <el-descriptions-item label="文件名">{{ previewData.name }}</el-descriptions-item>
          <el-descriptions-item label="大小">{{ formatSize(previewData.size) }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ previewData.fileType }}</el-descriptions-item>
          <el-descriptions-item label="行数">{{ previewData.rowCount }}</el-descriptions-item>
          <el-descriptions-item label="列数">{{ previewData.columnCount }}</el-descriptions-item>
          <el-descriptions-item label="校验和">{{ previewData.checksum }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="previewData.columns">
          <h4>列信息</h4>
          <el-tag
            v-for="col in previewData.columns"
            :key="col"
            style="margin: 5px"
          >{{ col }}</el-tag>
        </div>
      </div>
      <span slot="footer">
        <el-button type="primary" @click="previewDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import axios from 'axios'

export default {
  name: 'LocalDataManage',
  data() {
    return {
      activeTab: 'clients',
      loading: false,
      datasetsLoading: false,
      trainingLoading: false,
      clients: [],
      datasets: [],
      trainingTasks: [],
      datasetSearch: '',
      selectedClient: null,
      systemInfo: {},
      connectDialogVisible: false,
      trainingDialogVisible: false,
      previewDialogVisible: false,
      activeStep: 0,
      newClientId: '',
      previewData: null,
      trainingForm: {
        clientId: '',
        datasetPath: '',
        modelType: 'mlp',
        epochs: 10,
        batchSize: 32,
        learningRate: 0.001
      }
    }
  },
  computed: {
    ...mapState(['user', 'token']),
    userToken() {
      return this.token || localStorage.getItem('token') || ''
    },
    connectedClients() {
      return this.clients.filter(c => c.status === 'connected').length
    },
    totalDatasets() {
      return this.datasets.length
    },
    runningTasks() {
      return this.trainingTasks.filter(t => t.status === 'running').length
    },
    totalStorage() {
      return this.datasets.reduce((sum, d) => sum + (d.size || 0), 0)
    },
    filteredDatasets() {
      if (!this.datasetSearch) return this.datasets
      const search = this.datasetSearch.toLowerCase()
      return this.datasets.filter(d =>
        d.name.toLowerCase().includes(search) ||
        d.clientId.toLowerCase().includes(search)
      )
    },
    connectedClientsList() {
      return this.clients.filter(c => c.status === 'connected')
    },
    clientDatasets() {
      if (!this.trainingForm.clientId) return []
      return this.datasets.filter(d => d.clientId === this.trainingForm.clientId)
    },
    diskUsagePercent() {
      if (!this.systemInfo.diskTotal) return 0
      return Math.round((this.systemInfo.diskTotal - this.systemInfo.diskFree) / this.systemInfo.diskTotal * 100)
    },
    diskUsageColor() {
      if (this.diskUsagePercent < 50) return '#67c23a'
      if (this.diskUsagePercent < 80) return '#e6a23c'
      return '#f56c6c'
    }
  },
  created() {
    this.newClientId = 'client_' + Date.now()
    this.refreshClients()
    this.refreshDatasets()
    this.refreshTraining()
  },
  methods: {
    async refreshClients() {
      this.loading = true
      try {
        const res = await axios.get('/api/local-client/list', {
          headers: { Authorization: `Bearer ${this.userToken}` }
        })
        if (res.data.code === 200) {
          this.clients = res.data.data || []
        }
      } catch (e) {
        console.error('获取客户端列表失败:', e)
      } finally {
        this.loading = false
      }
    },
    async refreshDatasets() {
      this.datasetsLoading = true
      try {
        const res = await axios.get('/api/local-client/datasets', {
          headers: { Authorization: `Bearer ${this.userToken}` }
        })
        if (res.data.code === 200) {
          this.datasets = res.data.data || []
        }
      } catch (e) {
        console.error('获取数据集列表失败:', e)
      } finally {
        this.datasetsLoading = false
      }
    },
    async refreshTraining() {
      this.trainingLoading = true
      try {
        const res = await axios.get('/api/local-client/training', {
          headers: { Authorization: `Bearer ${this.userToken}` }
        })
        if (res.data.code === 200) {
          this.trainingTasks = res.data.data || []
        }
      } catch (e) {
        console.error('获取训练任务失败:', e)
      } finally {
        this.trainingLoading = false
      }
    },
    showConnectDialog() {
      this.activeStep = 0
      this.newClientId = 'client_' + Date.now()
      this.connectDialogVisible = true
    },
    showTrainingDialog() {
      this.trainingForm = {
        clientId: '',
        datasetPath: '',
        modelType: 'mlp',
        epochs: 10,
        batchSize: 32,
        learningRate: 0.001
      }
      this.trainingDialogVisible = true
    },
    async viewClient(client) {
      this.selectedClient = client
      this.activeTab = 'system'
      try {
        const res = await axios.post(`/api/local-client/${client.clientId}/command`, {
          type: 'get_system_info'
        }, {
          headers: { Authorization: `Bearer ${this.userToken}` }
        })
        if (res.data.code === 200) {
          this.systemInfo = res.data.system || {}
        }
      } catch (e) {
        console.error('获取系统信息失败:', e)
      }
    },
    async viewDatasets(client) {
      this.activeTab = 'datasets'
      this.datasetSearch = client.clientId
    },
    async startTraining(client) {
      this.trainingForm.clientId = client.clientId
      this.showTrainingDialog()
    },
    async submitTraining() {
      try {
        const res = await axios.post(`/api/local-client/${this.trainingForm.clientId}/command`, {
          type: 'start_training',
          data: {
            dataset_path: this.trainingForm.datasetPath,
            model_type: this.trainingForm.modelType,
            hyperparameters: {
              epochs: this.trainingForm.epochs,
              batch_size: this.trainingForm.batchSize,
              learning_rate: this.trainingForm.learningRate
            }
          }
        }, {
          headers: { Authorization: `Bearer ${this.userToken}` }
        })
        if (res.data.code === 200) {
          this.$message.success('训练任务已启动')
          this.trainingDialogVisible = false
          this.refreshTraining()
        } else {
          this.$message.error(res.data.message || '启动失败')
        }
      } catch (e) {
        this.$message.error('启动训练失败: ' + e.message)
      }
    },
    async stopTraining(task) {
      try {
        const res = await axios.post(`/api/local-client/${task.clientId}/command`, {
          type: 'stop_training',
          data: { task_id: task.taskId }
        }, {
          headers: { Authorization: `Bearer ${this.userToken}` }
        })
        if (res.data.code === 200) {
          this.$message.success('训练任务已停止')
          this.refreshTraining()
        }
      } catch (e) {
        this.$message.error('停止训练失败: ' + e.message)
      }
    },
    viewTrainingLog(task) {
      this.$message.info('查看训练日志功能开发中')
    },
    previewDataset(dataset) {
      this.previewData = dataset
      this.previewDialogVisible = true
    },
    trainWithDataset(dataset) {
      this.trainingForm.datasetPath = dataset.path
      this.trainingForm.clientId = dataset.clientId
      this.showTrainingDialog()
    },
    formatSize(bytes) {
      if (!bytes) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB', 'TB']
      let i = 0
      while (bytes >= 1024 && i < units.length - 1) {
        bytes /= 1024
        i++
      }
      return bytes.toFixed(2) + ' ' + units[i]
    },
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString()
    },
    getFileIcon(type) {
      const icons = {
        csv: 'el-icon-document',
        json: 'el-icon-document',
        txt: 'el-icon-document',
        xlsx: 'el-icon-document',
        parquet: 'el-icon-document'
      }
      return icons[type] || 'el-icon-document'
    },
    getStatusType(status) {
      const types = {
        initializing: 'info',
        running: 'warning',
        completed: 'success',
        failed: 'danger',
        stopped: 'info'
      }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = {
        initializing: '初始化',
        running: '运行中',
        completed: '已完成',
        failed: '失败',
        stopped: '已停止'
      }
      return texts[status] || status
    }
  }
}
</script>

<style scoped>
.local-data-manage {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.subtitle {
  color: #909399;
  margin: 0;
}

.status-cards {
  margin-bottom: 20px;
}

.status-card {
  height: 100px;
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  margin-right: 15px;
}

.card-icon.connected {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.card-icon.datasets {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.card-icon.training {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
}

.card-icon.storage {
  background: linear-gradient(135deg, #909399, #b1b3b8);
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

.main-tabs {
  margin-top: 20px;
}

.tab-header {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.file-icon {
  margin-right: 8px;
  color: #409eff;
}

.info-card {
  height: 100%;
}

.disk-usage {
  display: flex;
  align-items: center;
  gap: 30px;
}

.disk-info p {
  margin: 5px 0;
  color: #606266;
}

.connect-guide {
  padding: 20px 0;
}

.step-content {
  margin-top: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.step-content p {
  margin: 10px 0;
  font-weight: 500;
}
</style>
