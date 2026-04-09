<template>
  <div class="image-identify">
    <el-card class="identify-card">
      <div slot="header" class="card-header">
        <span>智能图像识别</span>
        <el-button type="primary" size="small" @click="clearAll">清空</el-button>
      </div>
      
      <div class="identify-content">
        <!-- 左侧：图像上传和预览 -->
        <div class="left-section">
          <!-- 任务和模型选择 -->
          <div class="task-model-select">
            <el-form :inline="true" :model="selectForm" class="select-form">
              <el-form-item label="任务类型">
                <el-select v-model="selectForm.taskType" placeholder="请选择任务类型" class="select-item">
                  <el-option label="病虫害识别" value="pest_disease"></el-option>
                  <el-option label="作物识别" value="crop"></el-option>
                  <el-option label="土壤分析" value="soil"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="模型选择">
                <el-select v-model="selectForm.modelId" placeholder="请选择模型" class="select-item">
                  <el-option label="默认模型" value="default"></el-option>
                  <el-option label="ResNet50" value="resnet50"></el-option>
                  <el-option label="EfficientNet" value="efficientnet"></el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="upload-section">
            <el-upload
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
              <img :src="imageUrl" alt="预览图" class="preview-img" />
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
          
          <el-button 
            type="primary" 
            class="identify-btn" 
            @click="startIdentify"
            :loading="loading"
            :disabled="!imageUrl || loading || !selectForm.taskType || !selectForm.modelId"
          >
            开始识别
          </el-button>
        </div>
        
        <!-- 右侧：识别结果 -->
        <div class="right-section">
          <div v-if="!identifyResult" class="no-result">
            <i class="el-icon-picture-outline"></i>
            <p>识别结果将显示在这里</p>
          </div>
          
          <div v-else class="result-content" v-loading="loading">
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
                <el-table :data="identifyResult.details" style="width: 100%">
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
import { identifyImage } from '../../api/identify'
import { analyzeDiseaseResult } from '../../api/deepseek-api'

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
      }
    }
  },
  methods: {
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
      if (!this.file) {
        this.$message.error('请先上传图片')
        return
      }
      
      if (!this.selectForm.taskType || !this.selectForm.modelId) {
        this.$message.error('请选择任务类型和模型')
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
      
      // 打印环境变量用于调试
      console.log('环境变量检查:')
      console.log('VUE_APP_DEEPSEEK_API_KEY:', process.env.VUE_APP_DEEPSEEK_API_KEY)
      console.log('VUE_APP_DEEPSEEK_API_BASE_URL:', process.env.VUE_APP_DEEPSEEK_API_BASE_URL)
      console.log('所有环境变量:', process.env)
      
      this.analyzing = true
      this.deepSeekAnalysis = null // 确保初始状态为null
      
      try {
        // 调用新的DeepSeek API进行深度分析
        console.log('调用深度分析API，参数:', this.identifyResult)
        
        // 检查API密钥是否存在
        const apiKey = process.env.VUE_APP_DEEPSEEK_API_KEY
        console.log('API密钥是否存在:', !!apiKey)
        if (!apiKey) {
          throw new Error('API密钥未配置')
        }
        
        // 先测试网络连接
        try {
          const testResponse = await fetch('https://httpbin.org/get')
          console.log('网络测试响应:', testResponse.status)
        } catch (testError) {
          console.error('网络测试失败:', testError)
        }
        
        const analysisResult = await analyzeDiseaseResult(this.identifyResult)
        
        // 记录API返回的结果
        console.log('DeepSeek API分析结果:', analysisResult)
        
        // 处理分析结果
        if (analysisResult) {
          // 保存原始分析结果
          this.deepSeekAnalysis = analysisResult
          this.$message.success('深度分析完成')
        } else {
          console.error('API返回的分析结果为空')
          this.$message.error('深度分析调用失败：未返回分析结果')
        }
      } catch (error) {
        console.error('深度分析API调用失败:', error)
        // 显示具体错误信息
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