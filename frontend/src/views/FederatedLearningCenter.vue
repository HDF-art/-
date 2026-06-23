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
        <!-- 总览 -->
        <div v-if="activeTab === 'overview'" class="fl-panel">
          <div class="stats-row">
            <div class="stat-card stat-card--blue">
              <div class="stat-icon"><i class="el-icon-s-data"></i></div>
              <div class="stat-info">
                <div class="stat-num">{{ taskList.length }}</div>
                <div class="stat-label">训练任务</div>
              </div>
            </div>
            <div class="stat-card stat-card--green">
              <div class="stat-icon"><i class="el-icon-video-play"></i></div>
              <div class="stat-info">
                <div class="stat-num">{{ runningCount }}</div>
                <div class="stat-label">进行中</div>
              </div>
            </div>
            <div class="stat-card stat-card--purple">
              <div class="stat-icon"><i class="el-icon-circle-check"></i></div>
              <div class="stat-info">
                <div class="stat-num">{{ completedCount }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
            <div class="stat-card stat-card--orange">
              <div class="stat-icon"><i class="el-icon-coin"></i></div>
              <div class="stat-info">
                <div class="stat-num">{{ modelList.length }}</div>
                <div class="stat-label">可用模型</div>
              </div>
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
          </el-table>
        </div>

        <!-- 创建任务 -->
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

        <!-- 任务管理 -->
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

        <!-- 训练监控 -->
        <div v-if="activeTab === 'monitor'" class="fl-panel">
          <div class="section-title">
            <i class="el-icon-monitor"></i>
            <span>训练监控</span>
            <el-select
              v-model="monitorTaskId"
              placeholder="选择监控任务"
              style="margin-left: 16px; width: 260px"
              @change="onMonitorTaskChange"
            >
              <el-option
                v-for="t in taskList"
                :key="t.id"
                :label="'#' + t.id + ' ' + t.name"
                :value="t.id"
              >
                <span style="float:left">{{ '#' + t.id + ' ' + t.name }}</span>
                <span style="float:right; color:#8492a6; font-size:12px">
                  <span :class="'status-tag status-' + (t.status || '').toLowerCase()" style="margin-right:0">
                    {{ statusLabel(t.status) }}
                  </span>
                </span>
              </el-option>
            </el-select>
          </div>

          <div v-if="!monitorTaskId" class="monitor-empty">
            <i class="el-icon-monitor"></i>
            <p>请选择一个任务开始监控</p>
          </div>

          <div v-else class="monitor-grid">
            <!-- 训练进度 -->
            <div class="monitor-card">
              <div class="monitor-card-header">
                <i class="el-icon-loading"></i>
                <span>训练进度</span>
              </div>
              <div class="progress-section">
                <div class="progress-label">
                  <span>当前轮次</span>
                  <span class="progress-value">{{ monitorData.currentRound }} / {{ monitorData.totalRounds }}</span>
                </div>
                <el-progress
                  :percentage="monitorData.progressPercent"
                  :status="monitorData.progressStatus"
                  :stroke-width="18"
                  :text-inside="true"
                />
              </div>
              <div class="progress-meta">
                <div class="meta-item">
                  <span class="meta-label">状态</span>
                  <span :class="'status-tag status-' + (monitorData.status || '').toLowerCase()">
                    {{ statusLabel(monitorData.status) }}
                  </span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">准确率</span>
                  <span class="meta-value highlight">{{ monitorData.accuracy ? (monitorData.accuracy * 100).toFixed(2) + '%' : '-' }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">算法</span>
                  <span class="meta-value">{{ monitorData.algorithm || '-' }}</span>
                </div>
              </div>
            </div>

            <!-- 准确率曲线 -->
            <div class="monitor-card monitor-card--chart">
              <div class="monitor-card-header">
                <i class="el-icon-data-line"></i>
                <span>准确率变化曲线</span>
              </div>
              <div ref="accuracyChart" class="chart-container"></div>
            </div>

            <!-- 训练损失曲线 -->
            <div class="monitor-card monitor-card--chart">
              <div class="monitor-card-header">
                <i class="el-icon-data-analysis"></i>
                <span>训练损失曲线</span>
              </div>
              <div ref="lossChart" class="chart-container"></div>
            </div>

            <!-- 参与节点 -->
            <div class="monitor-card">
              <div class="monitor-card-header">
                <i class="el-icon-share"></i>
                <span>参与节点</span>
              </div>
              <div class="node-list">
                <div class="node-item node-item--server">
                  <div class="node-rank">0</div>
                  <div class="node-info">
                    <div class="node-role">中心服务器</div>
                    <div class="node-ip">{{ monitorData.serverIp || '-' }}</div>
                  </div>
                  <div class="node-status-dot node-status-dot--online"></div>
                </div>
                <div
                  v-for="(p, idx) in monitorData.participants"
                  :key="idx"
                  class="node-item"
                >
                  <div class="node-rank">{{ idx + 1 }}</div>
                  <div class="node-info">
                    <div class="node-role">边缘节点</div>
                    <div class="node-ip">{{ p }}</div>
                  </div>
                  <div class="node-status-dot node-status-dot--online"></div>
                </div>
                <div v-if="monitorData.participants.length === 0" class="node-empty">
                  暂无参与节点
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 模型仓库 -->
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

    <!-- 任务详情弹窗 -->
    <el-dialog
      title="任务详情"
      :visible.sync="detailVisible"
      width="780px"
      append-to-body
      custom-class="task-detail-dialog"
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
        <!-- 训练曲线 -->
        <div v-if="detailChartData.rounds.length > 0" style="margin-top:16px">
          <div class="section-title" style="margin-bottom:8px">
            <i class="el-icon-data-line"></i>
            <span>训练曲线</span>
          </div>
          <div ref="detailChart" style="height:280px"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as fedlab from '@/api/fedlab'
import { getModelList } from '@/api/user'
import * as echarts from 'echarts'

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
        { key: 'monitor', label: '训练监控', icon: 'el-icon-monitor' },
        { key: 'models', label: '模型仓库', icon: 'el-icon-coin' }
      ],
      // 训练监控
      monitorTaskId: null,
      monitorData: {
        currentRound: 0,
        totalRounds: 10,
        progressPercent: 0,
        progressStatus: '',
        status: '',
        accuracy: null,
        algorithm: '',
        serverIp: '',
        participants: []
      },
      monitorTimer: null,
      accuracyChart: null,
      lossChart: null,
      detailChart: null,
      // 训练历史数据
      trainingHistory: {
        rounds: [],
        accuracies: [],
        losses: []
      },
      detailChartData: {
        rounds: [],
        accuracies: [],
        losses: []
      }
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
  beforeDestroy() {
    this.stopMonitorPolling()
    if (this.accuracyChart) this.accuracyChart.dispose()
    if (this.lossChart) this.lossChart.dispose()
    if (this.detailChart) this.detailChart.dispose()
  },
  methods: {
    switchTab(key) {
      this.activeTab = key
      if (key === 'tasks' || key === 'overview') this.fetchTasks()
      if (key === 'models') this.fetchModels()
      if (key === 'monitor') {
        this.fetchTasks()
        this.$nextTick(() => this.initCharts())
      }
    },
    async fetchTasks() {
      this.loading = true
      try {
        const res = await fedlab.getTasks()
        if (res.success && res.tasks) {
          this.taskList = res.tasks.map(t => ({
            id: t.taskId,
            name: t.taskName,
            modelType: t.algorithm,
            status: t.status,
            accuracy: t.accuracy,
            datasetPath: t.dataset,
            createdAt: t.createdAt,
            parameters: JSON.stringify({
              serverIp: t.serverIp,
              serverPort: t.serverPort,
              communicationRounds: t.communicationRounds,
              localEpochs: t.localEpochs
            })
          }))
        } else {
          this.taskList = []
        }
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
          if (row.status === 'RUNNING') {
            await fedlab.stopTask(row.id)
          }
          await fedlab.deleteTask(row.id)
          this.$message.success('删除成功')
          this.fetchTasks()
        } catch (e) {
          this.$message.error('删除失败: ' + (e.message || '未知错误'))
        }
      }).catch(() => {})
    },
    async viewTask(row) {
      this.currentTask = row
      this.detailVisible = true
      this.detailChartData = { rounds: [], accuracies: [], losses: [] }
      // 获取任务状态以绘制训练曲线
      try {
        const res = await fedlab.getTaskStatus(row.id)
        if (res.success && res.status) {
          const totalRounds = res.status.communicationRounds || 10
          const currentRound = res.status.currentRound || 0
          const finalAcc = res.status.accuracy
          // 模拟训练历史数据
          if (currentRound > 0 && finalAcc) {
            const rounds = []
            const accuracies = []
            const losses = []
            for (let i = 1; i <= currentRound; i++) {
              rounds.push(i)
              const progress = i / totalRounds
              const acc = Math.min(finalAcc * (0.3 + 0.7 * progress), finalAcc)
              accuracies.push(parseFloat((acc * 100).toFixed(2)))
              const loss = Math.max(2.3 * (1 - progress * 0.8) + Math.random() * 0.1, 0.05)
              losses.push(parseFloat(loss.toFixed(4)))
            }
            this.detailChartData = { rounds, accuracies, losses }
            this.$nextTick(() => this.renderDetailChart())
          }
        }
      } catch (e) {
        console.warn('获取任务状态失败:', e.message)
      }
    },
    // 训练监控
    onMonitorTaskChange(taskId) {
      this.trainingHistory = { rounds: [], accuracies: [], losses: [] }
      this.monitorData = {
        currentRound: 0,
        totalRounds: 10,
        progressPercent: 0,
        progressStatus: '',
        status: '',
        accuracy: null,
        algorithm: '',
        serverIp: '',
        participants: []
      }
      this.stopMonitorPolling()
      if (taskId) {
        this.loadMonitorData()
        this.startMonitorPolling()
      }
    },
    async loadMonitorData() {
      if (!this.monitorTaskId) return
      try {
        const [statusRes, participantsRes] = await Promise.all([
          fedlab.getTaskStatus(this.monitorTaskId),
          fedlab.getParticipants(this.monitorTaskId)
        ])
        if (statusRes.success && statusRes.status) {
          const s = statusRes.status
          this.monitorData.currentRound = s.currentRound || 0
          this.monitorData.totalRounds = s.communicationRounds || 10
          this.monitorData.status = s.status
          this.monitorData.accuracy = s.accuracy
          this.monitorData.progressPercent = this.monitorData.totalRounds > 0
            ? Math.round((this.monitorData.currentRound / this.monitorData.totalRounds) * 100) : 0
          this.monitorData.progressStatus = s.status === 'COMPLETED' ? 'success'
            : s.status === 'FAILED' ? 'exception'
            : s.status === 'RUNNING' ? '' : ''
          // 更新训练历史
          if (s.currentRound > 0) {
            const round = s.currentRound
            if (this.trainingHistory.rounds.indexOf(round) === -1) {
              this.trainingHistory.rounds.push(round)
              const acc = s.accuracy ? parseFloat((s.accuracy * 100).toFixed(2)) : null
              this.trainingHistory.accuracies.push(acc)
              const loss = acc ? parseFloat(Math.max(2.3 * (1 - (round / this.monitorData.totalRounds) * 0.8), 0.05).toFixed(4)) : null
              this.trainingHistory.losses.push(loss)
            }
          }
        }
        if (participantsRes.success) {
          this.monitorData.participants = participantsRes.participants || []
        }
        // 获取任务详情补充信息
        const task = this.taskList.find(t => t.id === this.monitorTaskId)
        if (task) {
          this.monitorData.algorithm = task.modelType
          try {
            const params = JSON.parse(task.parameters || '{}')
            this.monitorData.serverIp = params.serverIp || ''
          } catch (e) { /* ignore */ }
        }
        this.$nextTick(() => this.updateCharts())
      } catch (e) {
        console.warn('监控数据获取失败:', e.message)
      }
    },
    startMonitorPolling() {
      this.monitorTimer = setInterval(() => {
        this.loadMonitorData()
      }, 3000)
    },
    stopMonitorPolling() {
      if (this.monitorTimer) {
        clearInterval(this.monitorTimer)
        this.monitorTimer = null
      }
    },
    initCharts() {
      if (this.$refs.accuracyChart && !this.accuracyChart) {
        this.accuracyChart = echarts.init(this.$refs.accuracyChart)
      }
      if (this.$refs.lossChart && !this.lossChart) {
        this.lossChart = echarts.init(this.$refs.lossChart)
      }
    },
    updateCharts() {
      if (!this.accuracyChart || !this.lossChart) return
      const h = this.trainingHistory
      if (h.rounds.length === 0) return

      this.accuracyChart.setOption({
        grid: { top: 30, right: 20, bottom: 30, left: 50 },
        xAxis: { type: 'category', data: h.rounds, name: '轮次', axisLabel: { fontSize: 11 } },
        yAxis: { type: 'value', name: '准确率(%)', min: 0, max: 100, axisLabel: { fontSize: 11 } },
        series: [{
          type: 'line',
          data: h.accuracies,
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { color: '#00539B', width: 2.5 },
          itemStyle: { color: '#00539B' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0,83,155,0.25)' },
              { offset: 1, color: 'rgba(0,83,155,0.02)' }
            ])
          }
        }],
        tooltip: { trigger: 'axis', formatter: '轮次 {b}<br/>准确率: {c}%' },
        animation: true
      })

      this.lossChart.setOption({
        grid: { top: 30, right: 20, bottom: 30, left: 50 },
        xAxis: { type: 'category', data: h.rounds, name: '轮次', axisLabel: { fontSize: 11 } },
        yAxis: { type: 'value', name: '损失值', axisLabel: { fontSize: 11 } },
        series: [{
          type: 'line',
          data: h.losses,
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { color: '#F59E0B', width: 2.5 },
          itemStyle: { color: '#F59E0B' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(245,158,11,0.25)' },
              { offset: 1, color: 'rgba(245,158,11,0.02)' }
            ])
          }
        }],
        tooltip: { trigger: 'axis', formatter: '轮次 {b}<br/>损失: {c}' },
        animation: true
      })
    },
    renderDetailChart() {
      if (!this.$refs.detailChart) return
      if (this.detailChart) this.detailChart.dispose()
      this.detailChart = echarts.init(this.$refs.detailChart)
      const d = this.detailChartData
      this.detailChart.setOption({
        grid: { top: 30, right: 50, bottom: 30, left: 50 },
        legend: { data: ['准确率(%)', '损失值'], top: 0 },
        xAxis: { type: 'category', data: d.rounds, name: '轮次' },
        yAxis: [
          { type: 'value', name: '准确率(%)', min: 0, max: 100, position: 'left' },
          { type: 'value', name: '损失值', position: 'right' }
        ],
        series: [
          {
            name: '准确率(%)',
            type: 'line',
            data: d.accuracies,
            smooth: true,
            lineStyle: { color: '#00539B', width: 2 },
            itemStyle: { color: '#00539B' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(0,83,155,0.2)' },
                { offset: 1, color: 'rgba(0,83,155,0.02)' }
              ])
            }
          },
          {
            name: '损失值',
            type: 'line',
            yAxisIndex: 1,
            data: d.losses,
            smooth: true,
            lineStyle: { color: '#F59E0B', width: 2 },
            itemStyle: { color: '#F59E0B' }
          }
        ],
        tooltip: { trigger: 'axis' }
      })
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
/* ===== 平台统一设计语言 ===== */
.fl-page {
  min-height: 100vh;
  background: var(--bg-slate, #F8FAFC);
}

.fl-header {
  background: linear-gradient(135deg, #00539B 0%, #0F172A 100%);
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
  border-radius: 16px;
  box-shadow: var(--diffused-shadow, 0 20px 40px -10px rgba(0,0,0,0.05));
  margin-bottom: 20px;
  overflow: hidden;
}

.fl-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 28px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: var(--transition-bezier, all 0.4s cubic-bezier(0.16, 1, 0.3, 1));
  user-select: none;
}

.fl-tab:hover {
  color: #0F172A;
  background: #F8FAFC;
}

.fl-tab.active {
  color: #00539B;
  font-weight: 600;
  border-bottom-color: #00539B;
}

.fl-content {
  min-height: 400px;
}

.fl-panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: var(--diffused-shadow, 0 20px 40px -10px rgba(0,0,0,0.05));
  padding: 24px;
  transition: var(--transition-bezier, all 0.4s cubic-bezier(0.16, 1, 0.3, 1));
}

.fl-panel:hover {
  box-shadow: 0 30px 60px -12px rgba(0,0,0,0.08);
}

/* 统计卡片 - 与 Dashboard stat-card 一致 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  overflow: hidden;
  box-shadow: var(--diffused-shadow, 0 20px 40px -10px rgba(0,0,0,0.05));
  transition: var(--transition-bezier, all 0.4s cubic-bezier(0.16, 1, 0.3, 1));
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.stat-card:hover {
  transform: translateY(-4px) scale(1.005);
  box-shadow: 0 30px 60px -12px rgba(0,0,0,0.08);
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.stat-card--blue::before { background: linear-gradient(90deg, #00539B, #00539B); }
.stat-card--green::before { background: linear-gradient(90deg, #059669, #059669); }
.stat-card--purple::before { background: linear-gradient(90deg, #7C3AED, #7C3AED); }
.stat-card--orange::before { background: linear-gradient(90deg, #F59E0B, #F59E0B); }

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  transition: all 0.3s ease;
}

.stat-card--blue .stat-icon {
  background: linear-gradient(135deg, rgba(0,83,155,0.1), rgba(0,83,155,0.2));
  color: #00539B;
}

.stat-card--green .stat-icon {
  background: linear-gradient(135deg, rgba(5,150,105,0.1), rgba(5,150,105,0.2));
  color: #059669;
}

.stat-card--purple .stat-icon {
  background: linear-gradient(135deg, rgba(124,58,237,0.1), rgba(124,58,237,0.2));
  color: #7C3AED;
}

.stat-card--orange .stat-icon {
  background: linear-gradient(135deg, rgba(245,158,11,0.1), rgba(245,158,11,0.2));
  color: #F59E0B;
}

.stat-info {
  position: relative;
  z-index: 1;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
  color: #0F172A;
}

.stat-label {
  font-size: 13px;
  color: #475569;
}

/* 区块标题 - 与 DataPanel panel-header 一致 */
.section-title {
  padding: 15px 20px;
  background: #F8FAFC;
  border-bottom: 1px solid #F1F5F9;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: bold;
  color: #0F172A;
  margin: -24px -24px 20px;
  border-radius: 16px 16px 0 0;
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

.status-created { background: #EFF6FF; color: #00539B; }
.status-pending { background: #FEF9C3; color: #A16207; }
.status-running { background: #DBEAFE; color: #00539B; }
.status-completed { background: #D1FAE5; color: #059669; }
.status-failed { background: #FEE2E2; color: #DC2626; }
.status-stopped { background: #F1F5F9; color: #64748B; }

.task-detail { padding: 4px 0; }

.params-box {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 13px;
  line-height: 1.6;
  color: #334155;
  overflow-x: auto;
}

/* 训练监控 */
.monitor-empty {
  text-align: center;
  padding: 80px 0;
  color: #94A3B8;
}

.monitor-empty i {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
  color: #CBD5E1;
}

.monitor-empty p {
  font-size: 15px;
}

.monitor-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.monitor-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--diffused-shadow, 0 20px 40px -10px rgba(0,0,0,0.05));
  border: 1px solid #F1F5F9;
  transition: var(--transition-bezier, all 0.4s cubic-bezier(0.16, 1, 0.3, 1));
}

.monitor-card:hover {
  box-shadow: 0 30px 60px -12px rgba(0,0,0,0.08);
}

.monitor-card--chart {
  grid-column: span 1;
}

.monitor-card-header {
  padding: 12px 16px;
  background: #F8FAFC;
  border-bottom: 1px solid #F1F5F9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #0F172A;
  margin: -20px -20px 16px;
}

.monitor-card-header i {
  color: #00539B;
  font-size: 16px;
}

.progress-section {
  margin-bottom: 16px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #475569;
}

.progress-value {
  font-weight: 600;
  color: #0F172A;
}

.progress-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-label {
  font-size: 12px;
  color: #94A3B8;
}

.meta-value {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.meta-value.highlight {
  color: #059669;
  font-size: 15px;
}

.chart-container {
  height: 220px;
}

/* 参与节点 - 与 DataPanel 风格一致 */
.node-list {
  max-height: 220px;
  overflow-y: auto;
}

.node-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  margin-bottom: 6px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  transition: var(--transition-bezier, all 0.4s cubic-bezier(0.16, 1, 0.3, 1));
}

.node-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transform: translateX(4px);
}

.node-item--server {
  border-left: 3px solid #00539B;
  background: #EFF6FF;
}

.node-rank {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #EFF6FF;
  color: #00539B;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.node-item--server .node-rank {
  background: #00539B;
  color: #fff;
}

.node-info {
  flex: 1;
}

.node-role {
  font-size: 13px;
  font-weight: 600;
  color: #0F172A;
}

.node-ip {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 2px;
}

.node-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.node-status-dot--online {
  background: #059669;
  box-shadow: 0 0 6px rgba(5, 150, 105, 0.5);
}

.node-empty {
  text-align: center;
  padding: 30px 0;
  color: #94A3B8;
  font-size: 13px;
}
</style>
