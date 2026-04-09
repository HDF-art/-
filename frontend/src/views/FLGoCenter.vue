<template>
  <div class="flgo-container">
    <div class="header">
      <h2>🤖 联邦学习中心</h2>
      <p class="subtitle">多机分布式训练平台</p>
    </div>
    
    <!-- 创建任务区域 -->
    <div class="card">
      <h3>📝 创建训练任务</h3>
      <el-form :model="taskForm" label-width="120px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.taskName" placeholder="输入任务名称" />
        </el-form-item>
        <el-form-item label="数据集">
          <el-select v-model="taskForm.dataset" placeholder="选择数据集">
            <el-option label="MNIST (手写数字)" value="mnist" />
            <el-option label="CIFAR-10 (图像)" value="cifar10" />
            <el-option label="FashionMNIST (服饰)" value="fashionmnist" />
          </el-select>
        </el-form-item>
        <el-form-item label="算法">
          <el-select v-model="taskForm.algorithm" placeholder="选择算法">
            <el-option label="FedAvg (联邦平均)" value="fedavg" />
            <el-option label="FedProx (近端项)" value="fedprox" />
            <el-option label="Scaffold" value="scaffold" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户端数量">
          <el-input-number v-model="taskForm.numClients" :min="2" :max="100" />
        </el-form-item>
        <el-form-item label="训练轮次">
          <el-input-number v-model="taskForm.numRounds" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="本地轮次">
          <el-input-number v-model="taskForm.numEpochs" :min="1" :max="10" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="createTask" :loading="creating">
            创建任务
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 参与客户端管理 -->
    <div class="card" v-if="currentTask">
      <h3>👥 参与客户端管理</h3>
      <el-form :inline="true" :model="participantForm">
        <el-form-item label="客户端IP">
          <el-input v-model="participantForm.clientIp" placeholder="192.168.x.x" />
        </el-form-item>
        <el-form-item label="客户端名称">
          <el-input v-model="participantForm.clientName" placeholder="可选名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="registerParticipant">
            添加参与客户端
          </el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="participants" style="width: 100%; margin-top: 15px;">
        <el-table-column prop="ip" label="IP地址" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'online' ? 'success' : 'info'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" @click="selectAsServer(scope.$index)">
              选为服务器
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 服务器选择 -->
    <div class="card" v-if="participants.length > 0">
      <h3>🖥️ 服务器配置</h3>
      <p class="info-text">从参与客户端中选择一个作为服务器端</p>
      
      <el-select v-model="selectedServerIndex" placeholder="选择服务器" @change="onServerChange">
        <el-option
          v-for="(p, index) in participants"
          :key="index"
          :label="p.ip + (p.name ? ' - ' + p.name : '')"
          :value="index"
        />
      </el-select>
      
      <div v-if="serverIp" class="server-info">
        <el-tag type="success" size="large">当前服务器: {{ serverIp }}</el-tag>
      </div>
    </div>
    
    <!-- 任务控制 -->
    <div class="card" v-if="currentTask">
      <h3>▶️ 任务控制</h3>
      <el-button 
        type="primary" 
        @click="downloadScript" 
        :disabled="participants.length < 2"
      >
        下载客户端配置脚本
      </el-button>
      <el-button type="success" @click="startTask" :disabled="participants.length < 2 || !serverIp">
        开始训练
      </el-button>
      <el-button type="warning" @click="stopTask" :disabled="taskStatus !== 'RUNNING'">
        停止任务
      </el-button>
    </div>
    
    <!-- 训练状态 -->
    <div class="card" v-if="currentTask">
      <h3>📊 训练状态</h3>
      <el-progress 
        :percentage="progressPercent" 
        :status="progressStatus"
        :stroke-width="20"
      />
      <div class="status-info">
        <p>当前轮次: {{ currentRound }} / {{ taskForm.numRounds }}</p>
        <p>准确率: {{ accuracy.toFixed(2) }}%</p>
        <p>损失: {{ loss.toFixed(4) }}</p>
      </div>
    </div>
    
    <!-- 任务列表 -->
    <div class="card">
      <h3>📋 历史任务</h3>
      <el-table :data="taskList" style="width: 100%">
        <el-table-column prop="taskId" label="任务ID" width="200" />
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="algorithm" label="算法" />
        <el-table-column prop="numClients" label="客户端数" />
        <el-table-column prop="status" label="状态">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button type="text" @click="selectTask(scope.row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { createTask, getTasks, getTaskDetail, registerParticipant, selectServer, startTask, getTaskStatus } from '@/api/flgo'

export default {
  name: 'FLGoCenter',
  data()
    {
      return {
        clientStatusList: [],
        myClientStatus: '', {
    return {
      taskForm: {
        taskName: '',
        dataset: 'mnist',
        algorithm: 'fedavg',
        numClients: 5,
        numRounds: 10,
        numEpochs: 1
      },
      participantForm: {
        clientIp: '',
        clientName: ''
      },
      currentTask: null,
      participants: [],
      serverIp: '',
      selectedServerIndex: -1,
      taskList: [],
      creating: false,
      running: false,
      taskStatus: 'CREATED',
      currentRound: 0,
      accuracy: 0,
      loss: 0,
      statusTimer: null
    }
  },
  computed: {
    progressPercent() {
      return Math.round((this.currentRound / this.taskForm.numRounds) * 100)
    },
    progressStatus() {
      if (this.taskStatus === 'COMPLETED') return 'success'
      if (this.taskStatus === 'FAILED') return 'exception'
      if (this.taskStatus === 'RUNNING') return 'active'
      return ''
    }
  },
  mounted() {
    this.loadTasks()
    this.startStatusPolling()
  },
  beforeDestroy() {
    if (this.statusTimer) clearInterval(this.statusTimer)
  },
  methods: {
    async createTask() {
      this.creating = true
      try {
        const res = await createTask(this.taskForm)
        if (res.success) {
          this.currentTask = res.task
          this.$message.success('任务创建成功')
          this.loadTasks()
        }
      } catch (e) {
        this.$message.error('创建失败: ' + e.message)
      }
      this.creating = false
    },
    
    async registerParticipant() {
      if (!this.currentTask || !this.participantForm.clientIp) {
        this.$message.warning('请先创建任务并输入客户端IP')
        return
      }
      try {
        const res = await registerParticipant({
          taskId: this.currentTask.taskId,
          clientIp: this.participantForm.clientIp,
          clientName: this.participantForm.clientName
        })
        if (res.success) {
          this.participants.push({
            ip: this.participantForm.clientIp,
            name: this.participantForm.clientName || this.participantForm.clientIp,
            status: 'online'
          })
          this.participantForm.clientIp = ''
          this.participantForm.clientName = ''
          this.$message.success('客户端注册成功')
        }
      } catch (e) {
        this.$message.error('注册失败: ' + e.message)
      }
    },
    
    async selectAsServer(index) {
      this.selectedServerIndex = index
      await this.onServerChange(index)
    },
    
    async onServerChange(index) {
      if (!this.currentTask) return
      try {
        const res = await selectServer({
          taskId: this.currentTask.taskId,
          serverIndex: index
        })
        if (res.success) {
          this.serverIp = res.serverIp
          this.$message.success('服务器已选择: ' + res.serverIp)
        }
      } catch (e) {
        this.$message.error('选择失败: ' + e.message)
      }
    },
    
    
    async downloadScript() {
      try {
        const res = await this.$http.get(`/api/flgo/task/${this.currentTask.taskId}/download-script`)
        if (res.success) {
          // 生成并下载脚本
          const config = res.scriptConfig
          const script = this.generateClientScript(config)
          
          // 创建下载
          const blob = new Blob([script], { type: 'text/plain' })
          const url = URL.createObjectURL(blob)
          const a = document.createElement('a')
          a.href = url
          a.download = `flgo_client_${this.currentTask.taskId}.sh`
          a.click()
          URL.revokeObjectURL(url)
          
          this.$message.success('配置脚本已下载')
        }
      } catch (e) {
        this.$message.error('下载失败: ' + e.message)
      }
    },
    
    generateClientScript(config) {
      return `#!/usr/bin/env python3
"""
FLGo 客户端配置脚本
任务ID: ${config.taskId}
服务器IP: ${config.serverIp}
"""

import os
import sys
import json
import socket
import subprocess

# ==================== 配置参数 ====================
TASK_ID = "${config.taskId}"
SERVER_IP = "${config.serverIp}"
PARTICIPANTS = ${JSON.stringify(config.participants || [])}
ALGORITHM = "${config.algorithm || 'fedavg'}"
NUM_ROUNDS = ${config.numRounds || 10}
NUM_EPOCHS = ${config.numEpochs || 1}
CLIENT_IP = socket.gethostbyname(socket.gethostname())
CLIENT_NAME = socket.gethostname()

print("=" * 50)
print("FLGo 客户端配置")
print("=" * 50)
print(f"任务ID: {TASK_ID}")
print(f"服务器IP: {SERVER_IP}")
print(f"本机IP: {CLIENT_IP}")
print("=" * 50)

# ==================== 检查环境 ====================
print("\n[1/3] 检查环境...")
try:
    import flgo
    print("  ✓ FLGo 已安装")
except ImportError:
    print("  安装 FLGo...")
    subprocess.run([sys.executable, "-m", "pip", "install", "flgo", "-q"])

print("\n[2/3] 配置完成!")
print(f"  参与者列表: {PARTICIPANTS}")

print("\n[3/3] 运行训练...")
print("请运行: python flgo_train.py")

# ==================== 生成训练脚本 ====================
train_script = f"""
import flgo
import flgo.benchmark.mnist_classification as mnist
import flgo.benchmark.partition as fbp
import flgo.algorithm.{ALGORITHM} as fedalg
import os
import json

# 加载配置
config = {{
    "task_id": "{TASK_ID}",
    "server_ip": "{SERVER_IP}",
    "participants": {JSON.stringify(config.participants || [])},
}}

# 创建任务目录
task_dir = f"/tmp/flgo_task_{{CLIENT_IP.replace('.', '_')}}"
os.makedirs(task_dir, exist_ok=True)

# 生成数据
flgo.gen_task_by_(
    mnist,
    fbp.IIDPartitioner(num_clients=len(PARTICIPANTS)),
    task_dir
)

# 配置
runner_config = {{
    'gpu': [0] if os.path.exists('/dev/nvidia0') else [],
    'num_rounds': {NUM_ROUNDS},
    'num_epochs': {NUM_EPOCHS},
    'learning_rate': 0.01,
    'server_ip': "{SERVER_IP}",
    'participants': {JSON.stringify(config.participants || [])},
}}

# 初始化并运行
runner = flgo.init(task_dir, fedalg, runner_config)
runner.run()
print("训练完成!")
"""

with open("flgo_train.py", "w") as f:
    f.write(train_script)

print("\n脚本生成完成！")
print("运行: python flgo_train.py")
`
    },
    async startTask() {
      if (this.participants.length < 2) {
        this.$message.warning('至少需要2个参与者')
        return
      }
      if (!this.serverIp) {
        this.$message.warning('请选择服务器')
        return
      }
      
      this.running = true
      this.taskStatus = 'RUNNING'
      try {
        const res = await startTask(this.currentTask.taskId)
        if (res.success) {
          this.$message.success('任务已开始训练')
        }
      } catch (e) {
        this.$message.error('启动失败: ' + e.message)
      }
      this.running = false
    },
    
    async stopTask() {
      try {
        await this.$http.post(`/api/flgo/task/${this.currentTask.taskId}/stop`)
        this.taskStatus = 'STOPPED'
        this.$message.success('任务已停止')
      } catch (e) {
        this.$message.error('停止失败')
      }
    },
    
    async loadTasks() {
      try {
        const res = await getTasks()
        if (res.success) {
          this.taskList = res.tasks || []
        }
      } catch (e) {
        console.error('加载任务失败', e)
      }
    },
    
    selectTask(task) {
      this.currentTask = task
      this.taskForm = {
        taskName: task.taskName,
        dataset: task.dataset,
        algorithm: task.algorithm,
        numClients: task.numClients,
        numRounds: task.numRounds,
        numEpochs: task.numEpochs || 1
      }
      this.taskStatus = task.status
    },
    
    startStatusPolling() {
      this.statusTimer = setInterval(async () => {
        if (this.currentTask && this.taskStatus === 'RUNNING') {
          try {
            const res = await getTaskStatus(this.currentTask.taskId)
            if (res.success) {
              this.currentRound = res.status.currentRound || this.currentRound
              this.accuracy = res.status.accuracy || 0
              this.loss = res.status.loss || 0
            }
          } catch (e) {
            console.error('获取状态失败', e)
          }
        }
      }, 3000)
    },
    
    getStatusType(status) {
      const types = {
        'CREATED': 'info',
        'RUNNING': 'primary',
        'COMPLETED': 'success',
        'FAILED': 'danger',
        'STOPPED': 'warning'
      }
      return types[status] || 'info'
    }
  }
}
</script>

<style scoped>
.flgo-container {
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  color: #409eff;
  font-size: 28px;
}

.subtitle {
  color: #909399;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.card h3 {
  margin-bottom: 20px;
  color: #303133;
}

.info-text {
  color: #909399;
  margin-bottom: 10px;
}

.server-info {
  margin-top: 15px;
}

.status-info {
  margin-top: 15px;
}

.status-info p {
  margin: 5px 0;
  color: #606266;
}
</style>
