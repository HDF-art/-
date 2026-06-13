<template>
  <div class="fl-page">
    <div class="fl-header">
      <div class="fl-header-inner">
        <h1 class="fl-title">联邦学习中心</h1>
        <p class="fl-subtitle">分布式模型训练与协作管理</p>
      </div>
    </div>

    <div class="fl-body">
      <div class="fl-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          :class="['fl-tab', { active: activeTab === tab.key }]"
          @click="switchTab(tab.key)"
        >
          <i :class="tab.icon"></i>
          <span>{{ tab.label }}</span>
        </div>
      </div>

      <div class="fl-content">
        <div v-if="activeTab === 'overview'" class="fl-panel">
          <div class="stats-row">
            <div class="stat-card">
              <div class="stat-num">{{ taskList.length }}</div>
              <div class="stat-label">训练任务</div>
            </div>
            <div class="stat-card">
              <div class="stat-num">{{ runningCount }}</div>
              <div class="stat-label">进行中</div>
            </div>
            <div class="stat-card">
              <div class="stat-num">{{ completedCount }}</div>
              <div class="stat-label">已完成</div>
            </div>
            <div class="stat-card">
              <div class="stat-num">{{ modelList.length }}</div>
              <div class="stat-label">可用模型</div>
            </div>
          </div>

          <div class="section-title">
            <i class="el-icon-tickets"></i>
            <span>最近任务</span>
          </div>
          <el-table
            :data="taskList.slice(0, 5)"
            stripe
            style="width: 100%"
            v-loading="loading"
            empty-text="暂无训练任务"
          >
            <el-table-column prop="id" label="ID" width="70"></el-table-column>
            <el-table-column prop="name" label="任务名称" min-width="180"></el-table-column>
            <el-table-column prop="modelType" label="算法" width="120"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <span :class="'status-tag status-' + (scope.row.status || '').toLowerCase()">
                  {{ statusLabel(scope.row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="170">
              <template slot-scope="scope">
                {{ formatTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="activeTab === 'create'" class="fl-panel">
          <div class="section-title">
            <i class="el-icon-circle-plus-outline"></i>
            <span>创建训练任务</span>
          </div>
          <el-form
            ref="taskForm"
            :model="taskForm"
            :rules="taskRules"
            label-width="130px"
            class="fl-form"
          >
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="taskForm.taskName" placeholder="请输入任务名称"></el-input>
            </el-form-item>
            <el-form-item label="选择算法" prop="algorithm">
              <el-select v-model="taskForm.algorithm" placeholder="请选择" style="width:100%">
                <el-option label="FedAvg" value="FedAvg"></el-option>
                <el-option label="FedProx" value="FedProx"></el-option>
                <el-option label="SCAFFOLD" value="SCAFFOLD"></el-option>
                <el-option label="FedAdaPriv" value="FedAdaPriv"></el-option>
              </el-select>
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="通信轮次" prop="communicationRounds">
                  <el-input-number
                    v-model="taskForm.communicationRounds"
                    :min="1"
                    :max="200"
                    style="width:100%"
                  ></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="本地轮次">
                  <el-input-number
                    v-model="taskForm.localEpochs"
                    :min="1"
                    :max="50"
                    style="width:100%"
                  ></el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="参与方数量">
                  <el-input-number
                    v-model="taskForm.worldSize"
                    :min="2"
                    :max="100"
                    style="width:100%"
                  ></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="服务器端口">
                  <el-input-number
                    v-model="taskForm.serverPort"
                    :min="1024"
                    :max="65535"
                    style="width:100%"
                  ></el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="数据集">
              <el-input v-model="taskForm.dataset" placeholder="数据集路径或名称"></el-input>
            </el-form-item>
            <el-form-item label="服务器地址">
              <el-input v-model="taskForm.serverIp" placeholder="如 192.168.1.100"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="createTask" :loading="submitting">创建任务</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="activeTab === 'tasks'" class="fl-panel">
          <div class="section-title">
            <i class="el-icon-document"></i>
            <span>任务管理</span>
            <el-button
              type="primary"
              size="small"
              style="margin-left:auto"
              @click="fetchTasks"
              :loading="loading"
            >
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          <el-table
            :data="taskList"
            stripe
            style="width: 100%"
            v-loading="loading"
            empty-text="暂无训练任务"
          >
            <el-table-column prop="id" label="ID" width="70"></el-table-column>
            <el-table-column prop="name" label="任务名称" min-width="180"></el-table-column>
            <el-table-column prop="modelType" label="算法" width="120"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <span :class="'status-tag status-' + (scope.row.status || '').toLowerCase()">
                  {{ statusLabel(scope.row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="accuracy" label="准确率" width="100">
              <template slot-scope="scope">
                {{ scope.row.accuracy ? (scope.row.accuracy * 100).toFixed(2) + '%' : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="170">
              <template slot-scope="scope">
                {{ formatTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template slot-scope="scope">
                <el-button size="mini" @click="viewTask(scope.row)">详情</el-button>
                <el-button
                  size="mini"
                  type="success"
                  v-if="scope.row.status === 'CREATED'"
                  @click="startTask(scope.row)"
                >启动</el-button>
                <el-button
                  size="mini"
                  type="warning"
                  v-if="scope.row.status === 'RUNNING'"
                  @click="stopTask(scope.row)"
                >停止</el-button>
                <el-button
                  size="mini"
                  type="danger"
                  @click="deleteTask(scope.row)"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="activeTab === 'models'" class="fl-panel">
          <div class="section-title">
            <i class="el-icon-coin"></i>
            <span>模型仓库</span>
            <el-button
              type="primary"
              size="small"
              style="margin-left:auto"
              @click="fetchModels"
              :loading="loading"
            >
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          <el-table
            :data="modelList"
            stripe
            style="width: 100%"
            v-loading="loading"
            empty-text="暂无模型"
          >
            <el-table-column prop="id" label="ID" width="70"></el-table-column>
            <el-table-column prop="name" label="模型名称" min-width="180"></el-table-column>
            <el-table-column prop="type" label="类型" width="100"></el-table-column>
            <el-table-column prop="accuracy" label="准确率" width="100">
              <template slot-scope="scope">
                {{ scope.row.accuracy ? (scope.row.accuracy * 100).toFixed(2) + '%' : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="备注" min-width="150"></el-table-column>
            <el-table-column prop="isDefault" label="默认" width="70">
              <template slot-scope="scope">
                <i
                  v-if="scope.row.isDefault === 1"
                  class="el-icon-check"
                  style="color:#059669;font-size:16px"
                ></i>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="170">
              <template slot-scope="scope">
                {{ formatTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <el-dialog
      title="任务详情"
      :visible.sync="detailVisible"
      width="640px"
      append-to-body
    >
      <div v-if="currentTask" class="task-detail">
        <el-descriptions :column="2" border size="medium">
          <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
          <el-descriptions-item label="算法">{{ currentTask.modelType }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <span :class="'status-tag status-' + (currentTask.status || '').toLowerCase()">
              {{ statusLabel(currentTask.status) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="准确率">
            {{ currentTask.accuracy ? (currentTask.accuracy * 100).toFixed(2) + '%' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="数据集">{{ currentTask.datasetPath || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(currentTask.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(currentTask.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatTime(currentTask.endTime) }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentTask.parameters" style="margin-top:16px">
          <div class="section-title" style="margin-bottom:8px">
            <i class="el-icon-setting"></i>
            <span>训练参数</span>
          </div>
          <pre class="params-box">{{ formatParams(currentTask.parameters) }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as fedlab from '@/api/fedlab'
import { getModelList } from '@/api/user'

export default {
  name: 'FederatedLearningCenter',
  data() {
    return {
      activeTab: 'overview',
      loading: false,
      submitting: false,
      detailVisible: false,
      currentTask: null,
      taskList: [],
      modelList: [],
      taskForm: {
        taskName: '',
        algorithm: 'FedAvg',
        communicationRounds: 10,
        localEpochs: 1,
        worldSize: 2,
        serverPort: 3002,
        serverIp: '',
        dataset: ''
      },
      taskRules: {
        taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
        algorithm: [{ required: true, message: '请选择算法', trigger: 'change' }]
      },
      tabs: [
        { key: 'overview', label: '总览', icon: 'el-icon-data-board' },
        { key: 'create', label: '创建任务', icon: 'el-icon-circle-plus-outline' },
        { key: 'tasks', label: '任务管理', icon: 'el-icon-document' },
        { key: 'models', label: '模型仓库', icon: 'el-icon-coin' }
      ]
    }
  },
  computed: {
    runningCount() {
      return this.taskList.filter(t => t.status === 'RUNNING').length
    },
    completedCount() {
      return this.taskList.filter(t => t.status === 'COMPLETED').length
    }
  },
  mounted() {
    this.fetchTasks()
    this.fetchModels()
  },
  methods: {
    switchTab(key) {
      this.activeTab = key
      if (key === 'tasks' || key === 'overview') this.fetchTasks()
      if (key === 'models') this.fetchModels()
    },
    async fetchTasks() {
      this.loading = true
      try {
        const res = await fedlab.getTasks()
        const data = res.data || res
        this.taskList = Array.isArray(data) ? data : (data.data || [])
      } catch (e) {
        console.warn('获取任务列表失败:', e.message)
        this.taskList = []
      }
      this.loading = false
    },
    async fetchModels() {
      this.loading = true
      try {
        const res = await getModelList({ page: 1, pageSize: 100 })
        const data = res.data || res
        this.modelList = data.records || data.data || (Array.isArray(data) ? data : [])
      } catch (e) {
        console.warn('获取模型列表失败:', e.message)
        this.modelList = []
      }
      this.loading = false
    },
    createTask() {
      this.$refs.taskForm.validate(async (valid) => {
        if (!valid) return
        this.submitting = true
        try {
          await fedlab.createTask(this.taskForm)
          this.$message.success('任务创建成功')
          this.resetForm()
          this.activeTab = 'tasks'
          this.fetchTasks()
        } catch (e) {
          this.$message.error('创建失败: ' + (e.message || '未知错误'))
        }
        this.submitting = false
      })
    },
    resetForm() {
      this.taskForm = {
        taskName: '',
        algorithm: 'FedAvg',
        communicationRounds: 10,
        localEpochs: 1,
        worldSize: 2,
        serverPort: 3002,
        serverIp: '',
        dataset: ''
      }
      if (this.$refs.taskForm) this.$refs.taskForm.clearValidate()
    },
    async startTask(row) {
      try {
        await fedlab.startTask(row.id)
        this.$message.success('任务已启动')
        this.fetchTasks()
      } catch (e) {
        this.$message.error('启动失败: ' + (e.message || '未知错误'))
      }
    },
    async stopTask(row) {
      try {
        await fedlab.stopTask(row.id)
        this.$message.success('任务已停止')
        this.fetchTasks()
      } catch (e) {
        this.$message.error('停止失败: ' + (e.message || '未知错误'))
      }
    },
    deleteTask(row) {
      this.$confirm('确定删除该任务？', '确认', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await fedlab.stopTask(row.id)
          this.$message.success('删除成功')
          this.fetchTasks()
        } catch (e) {
          this.$message.error('删除失败: ' + (e.message || '未知错误'))
        }
      }).catch(() => {})
    },
    viewTask(row) {
      this.currentTask = row
      this.detailVisible = true
    },
    statusLabel(status) {
      const map = {
        CREATED: '已创建',
        PENDING: '等待中',
        RUNNING: '进行中',
        COMPLETED: '已完成',
        FAILED: '已失败',
        STOPPED: '已停止'
      }
      return map[status] || status || '-'
    },
    formatTime(t) {
      if (!t) return '-'
      if (Array.isArray(t)) {
        const [y, m, d, h, min, s] = t
        return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h || 0).padStart(2, '0')}:${String(min || 0).padStart(2, '0')}:${String(s || 0).padStart(2, '0')}`
      }
      return String(t).replace('T', ' ').substring(0, 19)
    },
    formatParams(p) {
      if (!p) return ''
      try {
        return JSON.stringify(typeof p === 'string' ? JSON.parse(p) : p, null, 2)
      } catch (e) {
        return String(p)
      }
    }
  }
}
</script>

<style scoped>
.fl-page {
  min-height: 100vh;
  background: #F1F5F9;
}

.fl-header {
  background: linear-gradient(135deg, #0F172A 0%, #1E3A5F 100%);
  padding: 40px 0 36px;
}

.fl-header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.fl-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
}

.fl-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.fl-body {
  max-width: 1200px;
  margin: -24px auto 40px;
  padding: 0 24px;
}

.fl-tabs {
  display: flex;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  overflow: hidden;
}

.fl-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 28px;
  font-size: 14px;
  color: #64748B;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  user-select: none;
}

.fl-tab:hover {
  color: #0F172A;
  background: #F8FAFC;
}

.fl-tab.active {
  color: #0F172A;
  font-weight: 600;
  border-bottom-color: #00539B;
}

.fl-content {
  min-height: 400px;
}

.fl-panel {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 24px;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
}

.stat-card {
  flex: 1;
  background: #F8FAFC;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  border: 1px solid #E2E8F0;
}

.stat-num {
  font-size: 32px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: #64748B;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #0F172A;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F1F5F9;
}

.section-title i {
  color: #00539B;
  font-size: 18px;
}

.fl-form {
  max-width: 640px;
  padding-top: 8px;
}

.status-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-created {
  background: #EFF6FF;
  color: #2563EB;
}

.status-pending {
  background: #FEF9C3;
  color: #A16207;
}

.status-running {
  background: #DBEAFE;
  color: #1D4ED8;
}

.status-completed {
  background: #D1FAE5;
  color: #059669;
}

.status-failed {
  background: #FEE2E2;
  color: #DC2626;
}

.status-stopped {
  background: #F1F5F9;
  color: #64748B;
}

.task-detail {
  padding: 4px 0;
}

.params-box {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  padding: 12px 16px;
  font-size: 13px;
  color: #334155;
  line-height: 1.6;
  overflow-x: auto;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
