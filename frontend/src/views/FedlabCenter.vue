<template>
  <div class="fedlab-container">
    <div class="header">
      <h2>🤖 FedLab 联邦学习中心</h2>
      <p class="subtitle">真实分布式训练平台</p>
    </div>
    
    <!-- 创建任务区域 -->
    <div class="card">
      <h3>📝 创建训练任务</h3>
      <el-form :model="taskForm" label-width="140px">
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
          </el-select>
        </el-form-item>
        
        <el-divider content-position="left">⚙️ 分布式配置</el-divider>
        
        <el-form-item label="中心服务器 IP">
          <el-input v-model="taskForm.serverIp" placeholder="例如: 192.168.1.100" />
        </el-form-item>
        <el-form-item label="中心服务器端口">
          <el-input-number v-model="taskForm.serverPort" :min="1024" :max="65535" />
        </el-form-item>
        <el-form-item label="全局通信轮次">
          <el-input-number v-model="taskForm.communicationRounds" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="本地训练 Epoch">
          <el-input-number v-model="taskForm.localEpochs" :min="1" :max="10" />
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
        <el-form-item>
          <el-button type="success" @click="registerParticipant">
            添加参与客户端
          </el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="participants" style="width: 100%; margin-top: 15px;">
        <el-table-column prop="ip" label="IP地址" width="180" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'online' ? 'success' : 'info'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 节点状态展示 -->
    <div class="card" v-if="currentTask && participants.length > 0">
      <h3>🌐 节点连通状态</h3>
      <p class="info-text">Rank 0: 中心服务器, Rank 1~N: 边缘节点</p>
      
      <el-table :data="nodeStatusList" style="width: 100%; margin-top: 15px;">
        <el-table-column prop="rank" label="Rank" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.rank === 0 ? 'danger' : 'primary'">
              {{ scope.row.rank }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="120">
          <template slot-scope="scope">
            <span>
              <i :class="scope.row.rank === 0 ? 'el-icon-s-platform' : 'el-icon-cpu'"></i>
              {{ scope.row.role }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'online' ? 'success' : 'warning'">
              {{ scope.row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastSeen" label="最后通信" />
      </el-table>
    </div>
    
    <!-- 任务控制 -->
    <div class="card" v-if="currentTask">
      <h3>▶️ 任务控制</h3>
      <el-button 
        type="primary" 
        @click="downloadScript" 
        :disabled="participants.length < 1"
      >
        下载客户端配置脚本
      </el-button>
      <el-button type="success" @click="startTask" :disabled="participants.length < 1 || !taskForm.serverIp">
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
        <p>当前轮次: {{ currentRound }} / {{ taskForm.communicationRounds }}</p>
        <p>准确率: {{ accuracy.toFixed(2) }}%</p>
      </div>
    </div>
    
    <!-- 任务列表 -->
    <div class="card">
      <h3>📋 历史任务</h3>
      <el-table :data="taskList" style="width: 100%">
        <el-table-column prop="taskId" label="任务ID" width="200" />
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="algorithm" label="算法" />
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
import { createTask, getTasks, getTaskDetail, registerParticipant, startTask, getTaskStatus, getParticipants, downloadScript } from '@/api/fedlab'

export default {
  name: 'FedlabCenter',
  data() {
    return {
      taskForm: {
        taskName: '',
        dataset: 'mnist',
        algorithm: 'fedavg',
        serverIp: '',
        serverPort: 3002,
        communicationRounds: 10,
        localEpochs: 1
      },
      participantForm: {
        clientIp: ''
      },
      currentTask: null,
      participants: [],
      nodeStatusList: [],
      taskList: [],
      creating: false,
      running: false,
      taskStatus: 'CREATED',
      currentRound: 0,
      accuracy: 0,
      statusTimer: null
    }
  },
  computed: {
    progressPercent() {
      return Math.round((this.currentRound / this.taskForm.communicationRounds) * 100)
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
          clientIp: this.participantForm.clientIp
        })
        if (res.success) {
          this.participants.push({
            ip: this.participantForm.clientIp,
            name: this.participantForm.clientIp,
            status: 'online'
          })
          this.participantForm.clientIp = ''
          this.updateNodeStatusList()
          this.$message.success('客户端注册成功')
        }
      } catch (e) {
        this.$message.error('注册失败: ' + e.message)
      }
    },
    
    updateNodeStatusList() {
      const list = []
      
      list.push({
        rank: 0,
        role: '中心服务器',
        ip: this.taskForm.serverIp || '未配置',
        status: this.taskForm.serverIp ? 'online' : 'offline',
        lastSeen: '刚刚'
      })
      
      this.participants.forEach((p, idx) => {
        list.push({
          rank: idx + 1,
          role: '边缘节点',
          ip: p.ip,
          status: p.status,
          lastSeen: '刚刚'
        })
      })
      
      this.nodeStatusList = list
    },
    
    async downloadScript() {
      if (!this.currentTask) return
      try {
        const res = await downloadScript(this.currentTask.taskId)
        if (res.success) {
          const config = res.scriptConfig
          const script = this.generateClientScript(config)
          
          const blob = new Blob([script], { type: 'text/plain' })
          const url = URL.createObjectURL(blob)
          const a = document.createElement('a')
          a.href = url
          a.download = `fedlab_client_${this.currentTask.taskId}.sh`
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
FedLab 客户端配置脚本
任务ID: ${config.taskId}
服务器IP: ${config.serverIp}
服务器端口: ${config.serverPort}
"""

import os
import sys
import json
import socket

# ==================== 配置参数 ====================
TASK_ID = "${config.taskId}"
SERVER_IP = "${config.serverIp}"
SERVER_PORT = ${config.serverPort}
WORLD_SIZE = ${config.worldSize || 2}
RANK = ${config.rank || 1}
COMMUNICATION_ROUNDS = ${config.communicationRounds || 10}
LOCAL_EPOCHS = ${config.localEpochs || 1}
CLIENT_IP = socket.gethostbyname(socket.gethostname())

print("=" * 60)
print("FedLab 客户端配置")
print("=" * 60)
print(f"任务ID: {TASK_ID}")
print(f"服务器: {SERVER_IP}:{SERVER_PORT}")
print(f"本机IP: {CLIENT_IP}")
print(f"Rank: {RANK} / {WORLD_SIZE}")
print(f"通信轮次: {COMMUNICATION_ROUNDS}")
print(f"本地Epochs: {LOCAL_EPOCHS}")
print("=" * 60)

# ==================== 检查环境 ====================
print("\\n[1/2] 检查环境...")
try:
    import fedlab
    print("  ✓ FedLab 已安装")
except ImportError:
    print("  ✗ FedLab 未安装")
    print("  请运行: pip install fedlab")
    sys.exit(1)

print("\\n[2/2] 配置完成!")
print("\\n请运行: python fedlab_client_run.py")

# ==================== 生成客户端运行脚本 ====================
client_script = f'''#!/usr/bin/env python3
"""
FedLab 客户端运行脚本
任务ID: {TASK_ID}
"""

import sys
import logging
import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import datasets, transforms
from torch.utils.data import DataLoader

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("FedLabClient")

try:
    from fedlab.core.client.manager import ClientPassiveManager
    from fedlab.core.client.trainer import ClientTrainer
    from fedlab.core.network import DistNetwork
    from fedlab.utils.functional import AverageMeter
except ImportError as e:
    logger.error(f"FedLab 模块导入失败: {{e}}")
    sys.exit(1)

class CNN(nn.Module):
    def __init__(self):
        super(CNN, self).__init__()
        self.conv1 = nn.Conv2d(1, 32, kernel_size=3, padding=1)
        self.conv2 = nn.Conv2d(32, 64, kernel_size=3, padding=1)
        self.pool = nn.MaxPool2d(2, 2)
        self.fc1 = nn.Linear(64 * 7 * 7, 128)
        self.fc2 = nn.Linear(128, 10)
        
    def forward(self, x):
        x = self.pool(F.relu(self.conv1(x)))
        x = self.pool(F.relu(self.conv2(x)))
        x = x.view(-1, 64 * 7 * 7)
        x = F.relu(self.fc1(x))
        x = self.fc2(x)
        return x

class FedLabClientTrainer(ClientTrainer):
    def __init__(self, model, data_loader, epochs=1, lr=0.01):
        super().__init__(model, cuda=torch.cuda.is_available())
        self.data_loader = data_loader
        self.epochs = epochs
        self.lr = lr
        self.criterion = nn.CrossEntropyLoss()
        self.optimizer = torch.optim.SGD(self._model.parameters(), lr=lr)
        
    def train(self, model_parameters, epoch):
        self.set_model(model_parameters)
        self._model.train()
        
        for epoch in range(self.epochs):
            loss_meter = AverageMeter()
            for data, target in self.data_loader:
                if self.cuda:
                    data, target = data.cuda(), target.cuda()
                
                self.optimizer.zero_grad()
                output = self._model(data)
                loss = self.criterion(output, target)
                loss.backward()
                self.optimizer.step()
                
                loss_meter.update(loss.item(), data.size(0))
            
            logger.info(f"Epoch {{epoch+1}}: Loss={{loss_meter.avg:.4f}}")
        
        return self.model_parameters

def main():
    logger.info("=" * 60)
    logger.info("FedLab 客户端启动")
    logger.info("=" * 60)
    
    transform = transforms.Compose([
        transforms.ToTensor(),
        transforms.Normalize((0.1307,), (0.3081,))
    ])
    
    dataset = datasets.MNIST('./data', train=True, download=True, transform=transform)
    data_loader = DataLoader(dataset, batch_size=64, shuffle=True)
    
    model = CNN()
    trainer = FedLabClientTrainer(model, data_loader, epochs={LOCAL_EPOCHS})
    
    network = DistNetwork(
        address=("{SERVER_IP}", {SERVER_PORT}),
        world_size={WORLD_SIZE},
        rank={RANK},
        dist_backend="gloo"
    )
    
    manager = ClientPassiveManager(
        network=network,
        trainer=trainer
    )
    
    logger.info("客户端已启动，等待服务器指令...")
    manager.run()

if __name__ == "__main__":
    main()
'''

with open("fedlab_client_run.py", "w") as f:
    f.write(client_script)

print("\\n客户端运行脚本已生成！")
print("运行: python fedlab_client_run.py")
`
    },
    
    async startTask() {
      if (this.participants.length < 1) {
        this.$message.warning('至少需要1个参与者')
        return
      }
      if (!this.taskForm.serverIp) {
        this.$message.warning('请配置服务器IP')
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
        await this.$http.post(`/api/fedlab/task/${this.currentTask.taskId}/stop`)
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
        taskName: task.taskName || '',
        dataset: task.dataset || 'mnist',
        algorithm: task.algorithm || 'fedavg',
        serverIp: task.serverIp || '',
        serverPort: task.serverPort || 3002,
        communicationRounds: task.communicationRounds || 10,
        localEpochs: task.localEpochs || 1
      }
      this.taskStatus = task.status || 'CREATED'
      this.loadParticipants()
    },
    
    async loadParticipants() {
      if (!this.currentTask) return
      try {
        const res = await getParticipants(this.currentTask.taskId)
        if (res.success) {
          this.participants = (res.participants || []).map(ip => ({
            ip: ip,
            name: ip,
            status: 'online'
          }))
          this.updateNodeStatusList()
        }
      } catch (e) {
        console.error('加载参与者失败', e)
      }
    },
    
    startStatusPolling() {
      this.statusTimer = setInterval(async () => {
        if (this.currentTask && this.taskStatus === 'RUNNING') {
          try {
            const res = await getTaskStatus(this.currentTask.taskId)
            if (res.success) {
              this.currentRound = res.status.currentRound || this.currentRound
              this.accuracy = res.status.accuracy || 0
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
.fedlab-container {
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h2 {
  color: #00539B;
  font-size: 28px;
}

.subtitle {
  color: #909399;
}

.card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.card h3 {
  margin-bottom: 24px;
  color: #303133;
  font-size: 18px;
}

.info-text {
  color: #909399;
  margin-bottom: 15px;
}

.status-info {
  margin-top: 20px;
}

.status-info p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}
</style>
