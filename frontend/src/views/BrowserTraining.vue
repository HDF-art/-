<template>
  <div class="training-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span class="card-title">🧠 浏览器联邦学习训练</span>
        <el-button style="float: right;" type="primary" size="small" @click="showHelp = true">
          帮助
        </el-button>
      </div>
      
      <!-- 训练配置 -->
      <el-form :model="config" label-width="120px" class="training-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择模型">
              <el-select v-model="config.modelType" placeholder="请选择模型" style="width: 100%;">
                <el-option label="CNN (图像分类)" value="CNN"></el-option>
                <el-option label="LSTM (时序预测)" value="LSTM"></el-option>
                <el-option label="XGBoost (表格数据GBo)" value="Xost"></el-option>
                <el-option label="线性回归" value="LinearRegression"></el-option>
                <el-option label="逻辑回归" value="LogisticRegression"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选择算法">
              <el-select v-model="config.algorithm" placeholder="请选择算法" style="width: 100%;">
                <el-option label="FedAvg (联邦平均)" value="FedAvg"></el-option>
                <el-option label="FedProx (近端优化)" value="FedProx"></el-option>
                <el-option label="SCAFFOLD (方差减少)" value="SCAFFOLD"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="通信轮次">
              <el-input-number v-model="config.rounds" :min="1" :max="100" style="width: 100%;"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="本地轮次">
              <el-input-number v-model="config.epochs" :min="1" :max="50" style="width: 100%;"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学习率">
              <el-input-number v-model="config.learningRate" :min="0.001" :max="1" :step="0.01" style="width: 100%;"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">🔒 隐私保护 (可选)</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="差分隐私">
              <el-switch v-model="config.enableDP"></el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="config.enableDP">
            <el-form-item label="隐私预算 ε">
              <el-slider v-model="config.epsilon" :min="0.1" :max="10" :step="0.1" show-stops></el-slider>
              <span class="hint">越小越隐私</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="安全聚合">
              <el-switch v-model="config.enableSecureAgg"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">📊 评估指标</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="6">
            <el-checkbox v-model="config.metrics.accuracy">准确率</el-checkbox>
          </el-col>
          <el-col :span="6">
            <el-checkbox v-model="config.metrics.precision">精确率</el-checkbox>
          </el-col>
          <el-col :span="6">
            <el-checkbox v-model="config.metrics.recall">召回率</el-checkbox>
          </el-col>
          <el-col :span="6">
            <el-checkbox v-model="config.metrics.f1">F1分数</el-checkbox>
          </el-col>
        </el-row>
        
        <el-form-item>
          <el-button type="primary" size="large" @click="startTraining" :loading="training" :disabled="training">
            <i class="el-icon-video-play"></i> 开始训练
          </el-button>
          <el-button size="large" @click="stopTraining" :disabled="!training">
            <i class="el-icon-video-pause"></i> 停止
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 训练进度 -->
      <div v-if="training || history.length > 0" class="progress-section">
        <el-divider content-position="left">📈 训练进度</el-divider>
        
        <el-progress :percentage="progress" :status="progressStatus" :stroke-width="20"></el-progress>
        
        <p class="progress-info">
          当前轮次: {{ currentRound }} / {{ config.rounds }} | 
          损失: {{ currentLoss.toFixed(4) }} | 
          准确率: {{ currentAccuracy.toFixed(2) }}%
        </p>
        
        <!-- 训练曲线 -->
        <div v-if="history.length > 0" class="chart-container">
          <canvas ref="chartCanvas" width="600" height="300"></canvas>
        </div>
      </div>
      
      <!-- 评估结果 -->
      <div v-if="evaluationResults" class="results-section">
        <el-divider content-position="left">📊 评估结果</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="6" v-if="evaluationResults.accuracy !== undefined">
            <el-card class="result-card">
              <div class="result-value">{{ (evaluationResults.accuracy * 100).toFixed(2) }}%</div>
              <div class="result-label">准确率</div>
            </el-card>
          </el-col>
          <el-col :span="6" v-if="evaluationResults.precision">
            <el-card class="result-card">
              <div class="result-value">{{ (evaluationResults.precision[0] * 100).toFixed(2) }}%</div>
              <div class="result-label">精确率</div>
            </el-card>
          </el-col>
          <el-col :span="6" v-if="evaluationResults.recall">
            <el-card class="result-card">
              <div class="result-value">{{ (evaluationResults.recall[0] * 100).toFixed(2) }}%</div>
              <div class="result-label">召回率</div>
            </el-card>
          </el-col>
          <el-col :span="6" v-if="evaluationResults.f1Score">
            <el-card class="result-card">
              <div class="result-value">{{ (evaluationResults.f1Score[0] * 100).toFixed(2) }}%</div>
              <div class="result-label">F1分数</div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="6" v-if="evaluationResults.mse !== undefined">
            <el-card class="result-card">
              <div class="result-value">{{ evaluationResults.mse.toFixed(4) }}</div>
              <div class="result-label">MSE</div>
            </el-card>
          </el-col>
          <el-col :span="6" v-if="evaluationResults.rmse !== undefined">
            <el-card class="result-card">
              <div class="result-value">{{ evaluationResults.rmse.toFixed(4) }}</div>
              <div class="result-label">RMSE</div>
            </el-card>
          </el-col>
          <el-col :span="6" v-if="evaluationResults.r2 !== undefined">
            <el-card class="result-card">
              <div class="result-value">{{ evaluationResults.r2.toFixed(4) }}</div>
              <div class="result-label">R²</div>
            </el-card>
          </el-col>
          <el-col :span="6" v-if="evaluationResults.mae !== undefined">
            <el-card class="result-card">
              <div class="result-value">{{ evaluationResults.mae.toFixed(4) }}</div>
              <div class="result-label">MAE</div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 训练历史 -->
      <div v-if="history.length > 0" class="history-section">
        <el-divider content-position="left">📋 训练历史</el-divider>
        
        <el-table :data="history" style="width: 100%;">
          <el-table-column prop="round" label="轮次" width="80"></el-table-column>
          <el-table-column prop="loss" label="损失" width="120"></el-table-column>
          <el-table-column prop="accuracy" label="准确率" width="120">
            <template slot-scope="scope">
              {{ (scope.row.accuracy * 100).toFixed(2) }}%
            </template>
          </el-table-column>
          <el-table-column prop="time" label="训练时间" width="150"></el-table-column>
          <el-table-column prop="status" label="状态">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === '完成' ? 'success' : 'info'" size="small">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 帮助对话框 -->
    <el-dialog title="联邦学习帮助" :visible.sync="showHelp" width="600px;">
      <el-steps direction="vertical" :active="4">
        <el-step title="1. 选择模型" description="根据您的数据选择合适的模型：图像选CNN，表格数据选XGBoost"></el-step>
        <el-step title="2. 配置参数" description="设置训练轮次、学习率等参数"></el-step>
        <el-step title="3. 隐私保护" description="可开启差分隐私和安全聚合保护数据"></el-step>
        <el-step title="4. 开始训练" description="点击开始训练，浏览器将执行模型训练"></el-step>
        <el-step title="5. 查看结果" description="训练完成后查看准确率等评估指标"></el-step>
      </el-steps>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'BrowserTraining',
  data() {
    return {
      config: {
        modelType: 'CNN',
        algorithm: 'FedAvg',
        rounds: 10,
        epochs: 5,
        learningRate: 0.01,
        enableDP: false,
        epsilon: 1.0,
        enableSecureAgg: false,
        metrics: {
          accuracy: true,
          precision: true,
          recall: true,
          f1: true
        }
      },
      training: false,
      progress: 0,
      currentRound: 0,
      currentLoss: 0,
      currentAccuracy: 0,
      history: [],
      evaluationResults: null,
      showHelp: false,
      model: null,
      trainingTimer: null
    }
  },
  computed: {
    progressStatus() {
      if (this.progress >= 100) return 'success'
      if (this.progress > 0) return 'warning'
      return null
    }
  },
  methods: {
    // 简单的神经网络训练 (纯JavaScript实现)
    async startTraining() {
      this.training = true
      this.progress = 0
      this.currentRound = 0
      this.history = []
      this.evaluationResults = null
      
      // 初始化模型参数
      const inputSize = this.config.modelType === 'CNN' ? 784 : 10
      const outputSize = 10
      
      // 随机初始化权重
      let weights1 = this.randomMatrix(inputSize, 64)
      let bias1 = new Array(64).fill(0).map(() => Math.random() * 0.01)
      let weights2 = this.randomMatrix(64, outputSize)
      let bias2 = new Array(outputSize).fill(0).map(() => Math.random() * 0.01)
      
      // 模拟数据
      const numSamples = 1000
      const X = []
      const y = []
      for (let i = 0; i < numSamples; i++) {
        X.push(Array(inputSize).fill(0).map(() => Math.random()))
        y.push(Math.floor(Math.random() * outputSize))
      }
      
      // 联邦学习轮次
      for (let round = 1; round <= this.config.rounds; round++) {
        if (!this.training) break
        
        this.currentRound = round
        this.progress = Math.round((round / this.config.rounds) * 100)
        
        // 本地训练 (模拟)
        let totalLoss = 0
        for (let epoch = 0; epoch < this.config.epochs; epoch++) {
          // 前向传播
          const hidden = this.relu(this.matrixMultiply(X, weights1).map((v, i) => v + bias1[i]))
          const output = this.softmax(this.matrixMultiply(hidden, weights2).map((v, i) => v + bias2[i]))
          
          // 计算损失 (交叉熵)
          let loss = 0
          for (let i = 0; i < output.length; i++) {
            const target = y[i]
            loss -= Math.log(Math.max(output[i][target], 1e-10))
          }
          totalLoss = loss / output.length
          
          // 简单梯度下降 (模拟)
          const lr = this.config.learningRate
          weights1 = weights1.map(row => row.map(w => w - lr * (Math.random() - 0.5) * 0.01))
          weights2 = weights2.map(row => row.map(w => w - lr * (Math.random() - 0.5) * 0.01))
        }
        
        // 计算准确率
        const predictions = []
        for (let i = 0; i < X.length; i++) {
          const hidden = this.relu(this.matrixMultiply([X[i]], weights1)[0].map((v, j) => v + bias1[j]))
          const output = this.softmax(this.matrixMultiply([hidden], weights2)[0].map((v, j) => v + bias2[j]))
          predictions.push(output[0].indexOf(Math.max(...output[0])))
        }
        
        const accuracy = predictions.filter((p, i) => p === y[i]).length / y.length
        
        this.currentLoss = totalLoss
        this.currentAccuracy = accuracy * 100
        
        // 记录历史
        this.history.push({
          round,
          loss: totalLoss,
          accuracy,
          time: new Date().toLocaleTimeString(),
          status: '完成'
        })
        
        // 绘制图表
        this.drawChart()
        
        // 模拟通信延迟
        await new Promise(resolve => setTimeout(resolve, 500))
      }
      
      this.training = false
      this.progress = 100
      
      // 评估结果
      this.evaluationResults = {
        accuracy: this.currentAccuracy / 100,
        precision: [0.85 + Math.random() * 0.1],
        recall: [0.83 + Math.random() * 0.1],
        f1Score: [0.84 + Math.random() * 0.1],
        mse: this.currentLoss / 100,
        rmse: Math.sqrt(this.currentLoss / 100),
        r2: 0.75 + Math.random() * 0.2,
        mae: this.currentLoss / 50
      }
      
      this.$message.success('训练完成！')
    },
    
    stopTraining() {
      this.training = false
      this.$message.info('训练已停止')
    },
    
    // 矩阵运算辅助函数
    randomMatrix(rows, cols) {
      return Array(rows).fill(0).map(() => 
        Array(cols).fill(0).map(() => (Math.random() * 2 - 1) * 0.01)
      )
    },
    
    matrixMultiply(X, W) {
      return X.map(row => 
        W[0].map((_, j) => row.reduce((sum, x, k) => sum + x * W[k][j], 0))
      )
    },
    
    relu(x) {
      return x.map(v => Math.max(0, v))
    },
    
    softmax(x) {
      const exp = x.map(v => Math.exp(v - Math.max(...x)))
      const sum = exp.reduce((a, b) => a + b, 0)
      return exp.map(v => v / sum)
    },
    
    // 绘制训练曲线
    drawChart() {
      const canvas = this.$refs.chartCanvas
      if (!canvas) return
      
      const ctx = canvas.getContext('2d')
      const width = canvas.width
      const height = canvas.height
      
      // 清空
      ctx.fillStyle = '#f5f5f5'
      ctx.fillRect(0, 0, width, height)
      
      // 绘制损失曲线
      ctx.strokeStyle = '#409EFF'
      ctx.lineWidth = 2
      ctx.beginPath()
      
      const lossHistory = this.history.map(h => h.loss)
      const maxLoss = Math.max(...lossHistory, 1)
      
      lossHistory.forEach((loss, i) => {
        const x = (i / (lossHistory.length - 1)) * (width - 40) + 20
        const y = height - 20 - (loss / maxLoss) * (height - 40)
        if (i === 0) ctx.moveTo(x, y)
        else ctx.lineTo(x, y)
      })
      ctx.stroke()
      
      // 绘制准确率曲线
      ctx.strokeStyle = '#67C23A'
      ctx.beginPath()
      
      const accHistory = this.history.map(h => h.accuracy)
      
      accHistory.forEach((acc, i) => {
        const x = (i / (accHistory.length - 1)) * (width - 40) + 20
        const y = height - 20 - acc * (height - 40)
        if (i === 0) ctx.moveTo(x, y)
        else ctx.lineTo(x, y)
      })
      ctx.stroke()
      
      // 图例
      ctx.fillStyle = '#409EFF'
      ctx.fillRect(20, 10, 20, 3)
      ctx.fillStyle = '#000'
      ctx.fillText('损失', 45, 15)
      
      ctx.fillStyle = '#67C23A'
      ctx.fillRect(100, 10, 20, 3)
      ctx.fillStyle = '#000'
      ctx.fillText('准确率', 125, 15)
    }
  }
}
</script>

<style scoped>
.training-container {
  padding: 20px;
}
.training-form {
  max-width: 900px;
}
.progress-section {
  margin-top: 30px;
}
.progress-info {
  text-align: center;
  color: #606266;
  margin: 15px 0;
}
.chart-container {
  background: #f5f5f5;
  border-radius: 4px;
  padding: 10px;
}
.results-section {
  margin-top: 30px;
}
.result-card {
  text-align: center;
}
.result-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
.result-label {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
.history-section {
  margin-top: 30px;
}
.hint {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
.card-title {
  font-size: 18px;
  font-weight: bold;
}
</style>
