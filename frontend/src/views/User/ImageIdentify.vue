<template>
  <div class="image-identify">
    <el-card class="identify-card">
      <div slot="header" class="card-header">
        <span>推理任务</span>
        <el-button type="primary" size="small" @click="clearAll">清空</el-button>
      </div>
      
      <div class="identify-content">
        <!-- 左侧：图像上传和预览 -->
        <div class="left-section">
          <!-- 任务和模型选择 -->
          <div class="task-model-select">
            <el-form :inline="true" :model="selectForm" class="select-form">
              <el-form-item label="任务类型">
                <el-select v-model="selectForm.taskType" placeholder="请选择任务类型" class="select-item" @change="onTaskTypeChange">
                  <el-option label="病虫害识别" value="pest_disease"></el-option>
                  <el-option label="草莓成熟度检测" value="strawberry_ripeness"></el-option>
                  <el-option label="环境预测" value="env_prediction"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="模型选择">
                <el-select v-model="selectForm.modelId" placeholder="请选择模型" class="select-item" :loading="modelLoading">
                  <el-option
                    v-for="m in availableModels"
                    :key="m.id"
                    :label="m.name + (m.isDefault === 1 ? '（默认）' : '')"
                    :value="m.id"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="upload-section" v-if="selectForm.taskType !== 'env_prediction'">
            <el-upload
              v-if="!imageUrl"
              class="upload-demo"
              drag
              action=""
              :auto-upload="false"
              :on-change="handleFileChange"
              :before-upload="beforeUpload"
              :show-file-list="false"
              accept="image/*"
            >
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">
                <em>点击或拖拽图片到此处上传</em>
              </div>
              <div class="el-upload__tip" slot="tip">
                请上传JPG、PNG格式的图片，大小不超过5MB
              </div>
            </el-upload>

            <!-- 图片预览 -->
            <div v-if="imageUrl" class="image-preview">
              <img v-lazy="imageUrl" alt="预览图" class="preview-img" />
              <el-button
                type="danger"
                icon="el-icon-delete"
                circle
                size="small"
                class="delete-btn"
                @click="removeImage"
              ></el-button>
            </div>
          </div>

          <!-- 环境预测：Excel数据输入 -->
          <div class="upload-section" v-else>
            <div class="env-input-section">
              <div class="env-info-bar" v-if="envModelInfo">
                <el-tag type="info" size="small">需要 {{ envModelInfo.seqLen }} 个时间步</el-tag>
                <el-tag type="success" size="small">预测 {{ envModelInfo.predLen }} 个时间步</el-tag>
                <el-tag type="warning" size="small">{{ envModelInfo.featureColumns.length }} 个特征</el-tag>
              </div>

              <!-- 数据来源选择 -->
              <el-radio-group v-model="envDataSource" size="small" style="margin-bottom: 15px;">
                <el-radio-button label="upload">上传Excel</el-radio-button>
                <el-radio-button label="platform">平台数据</el-radio-button>
                <el-radio-button label="manual">手动输入</el-radio-button>
              </el-radio-group>

              <!-- 上传Excel -->
              <div v-if="envDataSource === 'upload'">
                <el-upload
                  drag
                  accept=".xlsx,.xls"
                  :auto-upload="false"
                  :on-change="handleEnvExcelChange"
                  :on-remove="removeEnvExcel"
                  :file-list="envExcelFileList"
                  :limit="1"
                >
                  <i class="el-icon-upload"></i>
                  <div class="el-upload__text">将Excel文件拖到此处，或<em>点击上传</em></div>
                  <div class="el-upload__tip" slot="tip">
                    支持 .xlsx 格式，需包含特征列：{{ envModelInfo ? envModelInfo.featureColumns.join('、') : '' }}
                  </div>
                </el-upload>
              </div>

              <!-- 平台数据选择 -->
              <div v-if="envDataSource === 'platform'">
                <div style="margin-bottom: 10px;">
                  <el-button type="text" @click="loadExcelList" :loading="excelListLoading">
                    <i class="el-icon-refresh"></i> 刷新列表
                  </el-button>
                </div>
                <el-table
                  :data="platformExcelList"
                  border
                  size="small"
                  highlight-current-row
                  @current-change="handlePlatformExcelSelect"
                  style="width: 100%"
                  max-height="300"
                  empty-text="暂无Excel文件，请先在数据管理中上传"
                >
                  <el-table-column prop="filename" label="文件名" min-width="200"></el-table-column>
                  <el-table-column label="大小" width="100">
                    <template slot-scope="scope">
                      {{ (scope.row.size / 1024).toFixed(1) }} KB
                    </template>
                  </el-table-column>
                  <el-table-column label="上传时间" width="160">
                    <template slot-scope="scope">
                      {{ scope.row.createdAt ? new Date(scope.row.createdAt).toLocaleString() : '-' }}
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- 手动输入 -->
              <div v-if="envDataSource === 'manual'">
                <el-button type="text" @click="loadSampleEnvData">加载示例数据</el-button>
                <el-input
                  type="textarea"
                  :rows="10"
                  v-model="envHistoryInput"
                  placeholder="请输入时间序列数据，每行一个时间步，各特征用逗号分隔。例如：&#10;25.5,60,30,35,40,22,23&#10;26,58,31,36,41,23,24"
                ></el-input>
              </div>

              <div class="env-features-hint" v-if="envModelInfo">
                特征顺序：{{ envModelInfo.featureColumns.join('、') }}
              </div>
              <div v-if="envParsedInfo" class="env-features-hint" style="color: #67c23a;">
                {{ envParsedInfo }}
              </div>
            </div>
          </div>

          <el-button
            type="primary"
            class="identify-btn"
            @click="startIdentify"
            :loading="loading"
            :disabled="loading || !selectForm.taskType || !selectForm.modelId || (selectForm.taskType !== 'env_prediction' && !imageUrl) || (selectForm.taskType === 'env_prediction' && !envReady)"
          >
            {{ selectForm.taskType === 'env_prediction' ? '开始预测' : '开始识别' }}
          </el-button>
        </div>
        
        <!-- 右侧：识别结果 -->
        <div class="right-section">
          <div v-if="!identifyResult && !envPredictResult" class="no-result">
            <i class="el-icon-picture-outline"></i>
            <p>识别结果将显示在这里</p>
          </div>

          <!-- 环境预测结果 -->
          <div v-if="envPredictResult" class="result-content" v-loading="loading">
            <div class="result-header">
              <h3>环境预测结果</h3>
              <span class="result-time">{{ formatDate(envPredictResult.predictTime) }}</span>
            </div>
            <div class="result-main">
              <el-alert :title="envPredictResult.message" type="success" :closable="false" style="margin-bottom: 15px;"></el-alert>
              <div class="result-details">
                <h4>预测数据</h4>
                <el-table :data="envPredictResult.predictions" style="width: 100%" size="small">
                  <el-table-column type="index" label="步长" width="60"></el-table-column>
                  <el-table-column
                    v-for="col in envPredictResult.targetNames"
                    :key="col"
                    :prop="col"
                    :label="col"
                    min-width="120"
                  >
                    <template slot-scope="scope">
                      {{ Number(scope.row[col]).toFixed(2) }}
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div class="result-actions">
                <el-button type="success" @click="clearEnvResult">清空结果</el-button>
              </div>
            </div>
          </div>

          <!-- 图片识别结果 -->
          <div v-else-if="identifyResult" class="result-content" v-loading="loading">
            <div class="result-header">
              <h3>识别结果</h3>
              <span class="result-time">{{ formatDate(identifyResult.identifyTime) }}</span>
            </div>
            
            <div class="result-main">
              <div class="result-item primary">
                <div class="result-label">主要识别</div>
                <div class="result-value">
                  <span class="disease-name">{{ identifyResult.diseaseName }}</span>
                  <span class="confidence">{{ (identifyResult.confidence * 100).toFixed(2) }}%</span>
                </div>
              </div>
              
              <div class="result-details">
                <h4>详细信息</h4>
                <el-table :data="(identifyResult.details || []).slice(0, 5)" style="width: 100%">
                  <el-table-column prop="diseaseName" label="病虫害名称" width="180"></el-table-column>
                  <el-table-column prop="confidence" label="置信度">
                    <template slot-scope="scope">
                      <div class="confidence-info">
                        <span class="confidence-value">{{ (scope.row.confidence * 100).toFixed(2) }}%</span>
                        <el-progress 
                          :percentage="parseInt((scope.row.confidence * 100).toFixed(0))" 
                          :color="getProgressColor(scope.row.confidence)"
                          :stroke-width="10"
                          :show-text="false"
                        ></el-progress>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <!-- 防治建议 -->
              <div class="prevention-suggestion">
                <h4>
                  防治建议
                  <el-button 
                    size="mini" 
                    type="primary" 
                    :loading="analyzing"
                    v-if="!deepSeekAnalysis && !analyzing"
                    @click="analyzeAgain"
                  >
                    获取深度分析
                  </el-button>
                  <el-tag v-else-if="deepSeekAnalysis" type="success" size="small">AI深度分析已生成</el-tag>
                </h4>
                
                <div v-if="analyzing" class="analyzing-content">
                  <el-skeleton style="width: 100%" animated />
                  <el-skeleton style="width: 80%" animated />
                  <el-skeleton style="width: 90%" animated />
                </div>
                
                <div v-else-if="deepSeekAnalysis" class="deepseek-analysis">
                  <div v-html="formatAnalysisContent(deepSeekAnalysis)" class="analysis-content"></div>
                </div>
                
                <div v-else class="suggestion-content">
                  <p v-for="(item, index) in preventionSuggestions" :key="index" class="suggestion-item">
                    {{ item }}
                  </p>
                </div>
              </div>
              
              <!-- 操作按钮 -->
              <div class="result-actions">
                <el-button type="success" @click="saveRecord">保存记录</el-button>
                <el-button @click="printResult">打印结果</el-button>
                <el-button @click="shareResult">分享</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
// 导入识别API
import { identifyImage, predictEnv, getEnvModelInfo, parseEnvExcel, getExcelList, getFileUrl } from '../../api/identify'
import { analyzeDiseaseResult } from '../../api/deepseek-api'
import { getModelsByTaskType } from '../../api/user'

export default {
  name: 'ImageIdentify',
  data() {
    return {
      imageUrl: '',
      file: null,
      loading: false,
      identifyResult: null,
      preventionSuggestions: [], // 移除默认防治建议，仅在用户点击深度分析后显示内容

      deepSeekAnalysis: null,
      analyzing: false,
      
      // 任务和模型选择表单
      selectForm: {
        taskType: '',
        modelId: ''
      },
      // 可用模型列表
      availableModels: [],
      modelLoading: false,
      // 环境预测相关
      envModelInfo: null,
      envHistoryInput: '',
      envPredictResult: null,
      showEnvInput: false,
      // Excel上传相关
      envDataSource: 'upload',
      envExcelFile: null,
      envExcelFileList: [],
      envParsedInfo: '',
      envParsedHistory: null,
      // 平台数据相关
      platformExcelList: [],
      excelListLoading: false,
      selectedPlatformFile: null,
      platformFileHistory: null
    }
  },
  created() {
    // 默认加载病虫害识别模型
    this.selectForm.taskType = 'pest_disease'
    this.loadModelsByTaskType('pest_disease')
  },
  computed: {
    envReady() {
      if (this.envDataSource === 'upload') {
        return !!this.envParsedHistory
      } else if (this.envDataSource === 'platform') {
        return !!this.platformFileHistory
      } else {
        return !!this.envHistoryInput
      }
    }
  },
  methods: {
    // 根据任务类型加载模型
    async loadModelsByTaskType(taskType) {
      this.modelLoading = true
      this.selectForm.modelId = ''
      try {
        const res = await getModelsByTaskType(taskType)
        const list = Array.isArray(res) ? res : (res.data || [])
        this.availableModels = list
        // 自动选中默认模型
        const defaultModel = this.availableModels.find(m => m.isDefault === 1)
        if (defaultModel) {
          this.selectForm.modelId = defaultModel.id
        } else if (this.availableModels.length > 0) {
          this.selectForm.modelId = this.availableModels[0].id
        }
        // 环境预测时加载模型信息
        if (taskType === 'env_prediction') {
          this.loadEnvModelInfo()
        }
      } catch (e) {
        console.error('加载模型列表失败', e)
        this.availableModels = []
      } finally {
        this.modelLoading = false
      }
    },
    // 任务类型变化
    onTaskTypeChange(val) {
      this.identifyResult = null
      this.envPredictResult = null
      this.loadModelsByTaskType(val)
    },
    // 加载环境预测模型信息
    async loadEnvModelInfo() {
      try {
        const res = await getEnvModelInfo()
        this.envModelInfo = res.data || res
      } catch (e) {
        console.error('加载环境预测模型信息失败', e)
        this.envModelInfo = null
      }
    },
    // 加载示例环境数据
    loadSampleEnvData() {
      const seqLen = (this.envModelInfo && this.envModelInfo.seqLen) || 96
      const nFeatures = (this.envModelInfo && this.envModelInfo.featureColumns.length) || 7
      const lines = []
      // 生成基于正弦波的模拟数据
      for (let i = 0; i < seqLen; i++) {
        const row = []
        for (let j = 0; j < nFeatures; j++) {
          const base = 20 + j * 5
          const val = base + Math.sin(i / 10 + j) * 5 + (Math.random() - 0.5) * 2
          row.push(val.toFixed(2))
        }
        lines.push(row.join(','))
      }
      this.envHistoryInput = lines.join('\n')
      this.showEnvInput = true
      this.$message.success(`已加载 ${seqLen} 个时间步的示例数据`)
    },
    // Excel文件选择
    async handleEnvExcelChange(file) {
      this.envExcelFile = file.raw
      this.envExcelFileList = [file]
      this.envParsedInfo = '正在解析...'
      this.envParsedHistory = null
      try {
        const formData = new FormData()
        formData.append('file', file.raw)
        const res = await parseEnvExcel(formData)
        const data = res.data || res
        if (data.error) {
          this.envParsedInfo = ''
          this.$message.error(data.error)
          return
        }
        this.envParsedHistory = data.history
        this.envParsedInfo = data.message || `已解析 ${data.rows} 行数据`
        this.$message.success(`Excel解析成功，共 ${data.rows} 行数据`)
      } catch (e) {
        this.envParsedInfo = ''
        this.$message.error('Excel解析失败：' + (e.message || '未知错误'))
      }
    },
    // 移除Excel文件
    removeEnvExcel() {
      this.envExcelFile = null
      this.envExcelFileList = []
      this.envParsedHistory = null
      this.envParsedInfo = ''
    },
    // 加载平台Excel文件列表
    async loadExcelList() {
      this.excelListLoading = true
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        const userId = user.id || 1
        const res = await getExcelList(userId)
        const data = res.data || res
        if (data.success) {
          this.platformExcelList = data.files || []
        } else {
          this.$message.error(data.message || '获取文件列表失败')
        }
      } catch (e) {
        console.error('加载Excel列表失败', e)
        this.$message.error('加载Excel列表失败')
      } finally {
        this.excelListLoading = false
      }
    },
    // 选择平台Excel文件
    async handlePlatformExcelSelect(row) {
      if (!row) return
      this.selectedPlatformFile = row
      this.platformFileHistory = null
      this.$message.info('正在解析选中的文件...')
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        const userId = user.id || 1
        // 下载文件并通过后端解析
        const formData = new FormData()
        // 先下载文件
        const fileUrl = getFileUrl(row.filename, userId)
        const response = await fetch(fileUrl)
        const blob = await response.blob()
        const file = new File([blob], row.filename, { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        formData.append('file', file)
        const res = await parseEnvExcel(formData)
        const data = res.data || res
        if (data.error) {
          this.$message.error(data.error)
          return
        }
        this.platformFileHistory = data.history
        this.$message.success(`解析成功，共 ${data.rows} 行数据`)
      } catch (e) {
        this.$message.error('文件解析失败：' + (e.message || '未知错误'))
      }
    },
    // 清空环境预测结果
    clearEnvResult() {
      this.envPredictResult = null
    },
    // 处理文件选择
    handleFileChange(file) {
      this.file = file.raw
      // 创建图片预览
      const reader = new FileReader()
      reader.onload = (e) => {
        this.imageUrl = e.target.result
      }
      reader.readAsDataURL(this.file)
    },
    
    // 上传前检查
    beforeUpload(file) {
      const isImage = file.type.includes('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      
      if (!isImage) {
        this.$message.error('请上传图片文件！')
        return false
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过5MB！')
        return false
      }
      return true
    },
    
    // 移除图片
    removeImage() {
      this.imageUrl = ''
      this.file = null
      this.identifyResult = null
    },
    
    // 清空所有
    clearAll() {
      this.imageUrl = ''
      this.file = null
      this.identifyResult = null
      // 重置任务和模型选择
      this.selectForm.taskType = ''
      this.selectForm.modelId = ''
    },
    
    // 开始识别
    async startIdentify() {
      if (!this.selectForm.taskType || !this.selectForm.modelId) {
        this.$message.error('请选择任务类型和模型')
        return
      }

      // 环境预测分支
      if (this.selectForm.taskType === 'env_prediction') {
        await this.startEnvPredict()
        return
      }

      if (!this.file) {
        this.$message.error('请先上传图片')
        return
      }

      this.loading = true
      this.deepSeekAnalysis = null
      try {
        // 创建FormData对象
        const formData = new FormData()
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        formData.append('file', this.file)
        formData.append('userId', user.id || 1)
        formData.append('taskType', this.selectForm.taskType)
        formData.append('cropType', this.selectForm.taskType)
        formData.append('modelId', this.selectForm.modelId)

        // 调用识别接口
        const response = await identifyImage(formData)

        // 使用真实API返回的数据
        this.identifyResult = response.data

        this.$message.success('识别完成')
        // 不自动调用DeepSeek API，仅在用户点击深度分析按钮时调用
        // 用户可以通过点击"获取深度分析"按钮来获取详细分析
      } catch (error) {
        this.$message.error('识别失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    // 环境预测
    async startEnvPredict() {
      this.loading = true
      try {
        let history = null

        if (this.envDataSource === 'upload') {
          // 上传Excel方式
          if (!this.envExcelFile) {
            this.$message.error('请先上传Excel文件')
            this.loading = false
            return
          }
          if (!this.envParsedHistory) {
            this.$message.error('请先等待Excel解析完成')
            this.loading = false
            return
          }
          history = this.envParsedHistory
        } else if (this.envDataSource === 'platform') {
          // 平台数据方式
          if (!this.selectedPlatformFile) {
            this.$message.error('请先选择平台数据文件')
            this.loading = false
            return
          }
          if (!this.platformFileHistory) {
            this.$message.error('请先等待文件解析完成')
            this.loading = false
            return
          }
          history = this.platformFileHistory
        } else {
          // 手动输入方式
          if (!this.envHistoryInput) {
            this.$message.error('请输入时间序列数据')
            this.loading = false
            return
          }
          history = this.envHistoryInput.trim().split('\n').map(line => {
            return line.trim().split(',').map(v => parseFloat(v))
          }).filter(row => row.length > 0 && row.every(v => !isNaN(v)))
        }

        if (!history || history.length === 0) {
          this.$message.error('数据格式错误，请检查输入')
          this.loading = false
          return
        }

        const res = await predictEnv(history)
        const data = res.data || res
        this.envPredictResult = {
          predictions: data.predictions || [],
          targetNames: data.targetNames || [],
          message: data.message || '预测完成',
          predictTime: new Date().toISOString()
        }
        this.$message.success('环境预测完成')
      } catch (error) {
        this.$message.error('环境预测失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    
    // 获取进度条颜色
    getProgressColor(confidence) {
      if (confidence >= 0.9) return '#67c23a'
      if (confidence >= 0.7) return '#e6a23c'
      if (confidence >= 0.5) return '#f56c6c'
      return '#909399'
    },
    
    // 格式化日期
    formatDate(date) {
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hour = String(d.getHours()).padStart(2, '0')
      const minute = String(d.getMinutes()).padStart(2, '0')
      const second = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    },
    
    // 保存记录
    saveRecord() {
      // 实际项目中调用保存接口
      // 检查是否有识别结果
      if (!this.identifyResult || !this.identifyResult.recordId) {
        this.$message.warning('没有可保存的识别记录')
        return
      }
      
      // 在实际应用中，这里应该调用API保存记录
      // 但目前后端在识别时已经自动保存了记录，所以这里只需要提示用户
      this.$message.success('记录已保存到历史记录中')
    },
    
    // 打印结果
    printResult() {
      this.$message('打印功能开发中')
    },
    
    // 分享结果
    shareResult() {
      this.$message('分享功能开发中')
    },
    
    // 重新分析
    async analyzeAgain() {
      if (!this.identifyResult) {
        this.$message.warning('请先上传图片进行识别')
        return
      }
      
      this.analyzing = true
      this.deepSeekAnalysis = null
      
      try {
        const analysisResult = await analyzeDiseaseResult(this.identifyResult)
        
        if (analysisResult) {
          this.deepSeekAnalysis = analysisResult
          this.$message.success('深度分析完成')
        } else {
          this.$message.error('深度分析调用失败：未返回分析结果')
        }
      } catch (error) {
        console.error('深度分析API调用失败:', error)
        this.$message.error(`深度分析调用失败：${error.message || '未知错误'}`)
      } finally {
        this.analyzing = false
      }
    },
    
    // 格式化分析内容，处理结构化数据
    formatAnalysisContent(analysisData) {
      console.log('格式化分析内容:', analysisData)
      
      // 确保数据存在
      if (!analysisData || typeof analysisData !== 'object') {
        return '<p>获取到的分析数据格式不正确</p>'
      }
      
      // 提取并格式化JSON数据
      let html = ''
      
      // 基本信息
      if (analysisData['基本信息']) {
        html += '<h5>基本信息</h5>'
        html += '<div class="analysis-group">'
        Object.entries(analysisData['基本信息']).forEach(([key, value]) => {
          html += `<p class="analysis-item"><strong>${key}：</strong>${value}</p>`
        })
        html += '</div>'
      }
      
      // 症状描述
      if (analysisData['症状描述']) {
        html += '<h5>症状描述</h5>'
        html += '<div class="analysis-group">'
        Object.entries(analysisData['症状描述']).forEach(([key, value]) => {
          html += `<p class="analysis-item"><strong>${key}：</strong>${value}</p>`
        })
        html += '</div>'
      }
      
      // 防治措施
      if (analysisData['防治措施']) {
        html += '<h5>防治措施</h5>'
        html += '<div class="analysis-group">'
        Object.entries(analysisData['防治措施']).forEach(([key, value]) => {
          html += `<p class="analysis-item"><strong>${key}：</strong>${value}</p>`
        })
        html += '</div>'
      }
      
      // 预防建议
      if (analysisData['预防建议']) {
        html += '<h5>预防建议</h5>'
        html += '<div class="analysis-group">'
        Object.entries(analysisData['预防建议']).forEach(([key, value]) => {
          html += `<p class="analysis-item"><strong>${key}：</strong>${value}</p>`
        })
        html += '</div>'
      }
      
      // 用药指南
      if (analysisData['用药指南']) {
        html += '<h5>用药指南</h5>'
        html += '<div class="analysis-group">'
        Object.entries(analysisData['用药指南']).forEach(([key, value]) => {
          html += `<p class="analysis-item"><strong>${key}：</strong>${value}</p>`
        })
        html += '</div>'
      }
      
      // 识别评价
      if (analysisData['识别评价']) {
        html += '<h5>识别评价</h5>'
        html += '<div class="analysis-group">'
        Object.entries(analysisData['识别评价']).forEach(([key, value]) => {
          html += `<p class="analysis-item"><strong>${key}：</strong>${value}</p>`
        })
        html += '</div>'
      }
      
      // 处理文本格式内容
      if (analysisData.analysisType === 'text' && analysisData.content) {
        let content = analysisData.content
        // 对Markdown格式的内容进行解析和HTML格式化
        // 处理标题（以数字+点开头的行）
        content = content.replace(/^(\d+)\.\s+/gm, '<h5>$1. </h5>')
        // 处理Markdown标题
        content = content.replace(/^#+\s+([^\n]+)/gm, '<h5>$1</h5>')
        // 处理列表项（以-开头的行）
        content = content.replace(/^-\s+/gm, '<p class="analysis-item">• </p>')
        // 处理段落
        content = content.replace(/^(?!<h5>|<p class="analysis-item">)(.*)$/gm, '<p>$1</p>')
        return content
      }
      
      // 检查是否有其他内容可以显示
      if (analysisData.content) {
        return `<p>${analysisData.content}</p>`
      }
      
      // 如果没有生成任何内容，返回默认信息
      if (html === '') {
        html = '<p>获取到的分析数据为空</p>'
      }
      
      return html
    }
  }
}
</script>

<style scoped>
.image-identify {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.identify-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-top: 20px;
}

.left-section {
  display: flex;
  flex-direction: column;
}

.task-model-select {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.select-form {
  width: 100%;
}

.select-item {
  width: 180px;
  margin-right: 10px;
}


.upload-section {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  background-color: #fafafa;
  position: relative;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.upload-section:hover {
  border-color: #409eff;
}

.env-input-section {
  width: 100%;
  text-align: left;
}
.env-info-bar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.env-input-actions {
  margin-bottom: 12px;
}
.env-features-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.image-preview {
  position: relative;
  width: 100%;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
}

.delete-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(255, 255, 255, 0.9);
}

.identify-btn {
  margin-top: 20px;
  height: 44px;
  font-size: 16px;
}

.right-section {
  min-height: 450px;
  display: flex;
  flex-direction: column;
}

.no-result {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 16px;
}

.no-result i {
  font-size: 48px;
  margin-bottom: 20px;
}

.result-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.result-header h3 {
  margin: 0;
  color: #303133;
}

.result-time {
  color: #909399;
  font-size: 14px;
}

.result-main {
  flex: 1;
}

.result-item {
  padding: 20px;
  background-color: #f0f9ff;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #409eff;
}

.result-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.result-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.disease-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.confidence {
  font-size: 20px;
  color: #67c23a;
  font-weight: bold;
}

.result-details,
.prevention-suggestion {
  margin-bottom: 20px;
}

.result-details h4,
.prevention-suggestion h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.confidence-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.confidence-value {
  font-size: 14px;
  color: #606266;
  min-width: 60px;
  text-align: right;
}

.suggestion-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  border-left: 3px solid #e6a23c;
}

.suggestion-item {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}

.analyzing-content {
  padding: 15px;
}

.deepseek-analysis {
  background-color: #f0f9ff;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.analysis-content {
  color: #303133;
  line-height: 1.8;
}

.analysis-content h5 {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 15px 0 10px 0;
}

.analysis-content p {
  margin: 8px 0;
  color: #606266;
}

.analysis-item {
  padding-left: 20px;
  position: relative;
}

.prevention-suggestion h4 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.result-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .identify-content {
    grid-template-columns: 1fr;
  }
  
  .upload-section {
    padding: 20px;
  }
  
  .result-value {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .result-actions {
    flex-direction: column;
  }
}
</style>