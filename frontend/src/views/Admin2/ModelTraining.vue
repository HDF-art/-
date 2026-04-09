<template>
  <div class="model-training-container">
    <el-card class="training-card">
      <template slot="header">
        <div class="card-header">
          <span>模型训练任务创建</span>
        </div>
      </template>
      
      <el-form ref="trainingForm" :model="trainingForm" :rules="trainingRules" label-width="120px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="trainingForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="trainingForm.taskType" placeholder="请选择任务类型">
            <el-option label="图像识别" value="图像识别" />
            <el-option label="目标检测" value="目标检测" />
            <el-option label="时序预测" value="时序预测" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="联邦学习算法" prop="federatedAlgorithm">
          <el-select v-model="trainingForm.federatedAlgorithm" placeholder="请选择联邦学习算法">
            <el-option label="FedAvg" value="FedAvg" />
            <el-option label="FedProx" value="FedProx" />
            <el-option label="SCAFFOLD" value="SCAFFOLD" />
            <el-option label="FedOpt" value="FedOpt" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="训练模型" prop="trainingModel">
          <el-select v-model="trainingForm.trainingModel" placeholder="请选择训练模型">
            <el-option label="SepResNet" value="SepResNet" />
            <el-option label="ResNet50" value="ResNet50" />
            <el-option label="MobileNetV2" value="MobileNetV2" />
            <el-option label="LSTM" value="LSTM" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="选择数据集" prop="datasetId">
          <el-select v-model="trainingForm.datasetId" placeholder="请选择数据集">
            <el-option 
              v-for="dataset in datasetList" 
              :key="dataset.id" 
              :label="dataset.name" 
              :value="dataset.id"
            />
          </el-select>
          <el-button type="primary" size="small" style="margin-left: 10px" @click="showDatasetDialog">管理数据集</el-button>
        </el-form-item>
        
        <el-form-item label="预定参与数量" prop="expectedParticipants">
          <el-input-number v-model="trainingForm.expectedParticipants" :min="1" :max="100" />
        </el-form-item>
        
        <el-form-item label="任务截止时间" prop="deadline">
          <el-date-picker
            v-model="trainingForm.deadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="trainingForm.remark"
            type="textarea"
            placeholder="请详细介绍本次训练任务的目的、数据要求等信息"
            rows="4"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">提交任务</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="task-list-card" style="margin-top: 20px">
      <template slot="header">
        <div class="card-header">
          <span>我的训练任务</span>
        </div>
      </template>
      
      <el-table :data="taskList" style="width: 100%">
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="taskType" label="任务类型" width="120" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getAuditStatusType(scope.row.auditStatus)">
              {{ scope.row.auditStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="任务状态" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button size="small" @click="viewTask(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 数据集管理对话框 -->
    <el-dialog
      title="数据集管理"
      :visible.sync="datasetDialogVisible"
      width="80%"
    >
      <DatasetManage @select-dataset="handleSelectDataset" />
    </el-dialog>
  </div>
</template>

<script>
import DatasetManage from './DatasetManage.vue'
import { getDatasetsByUploaderId } from '../../api/user'

export default {
  name: 'ModelTraining',
  components: {
    DatasetManage
  },
  data() {
    return {
      trainingForm: {
        name: '',
        taskType: '',
        federatedAlgorithm: '',
        trainingModel: '',
        datasetId: '',
        expectedParticipants: 2,
        deadline: new Date(),
        remark: ''
      },
      trainingRules: {
        name: [
          { required: true, message: '请输入任务名称', trigger: 'blur' },
          { min: 3, max: 50, message: '任务名称长度在 3 到 50 个字符', trigger: 'blur' }
        ],
        taskType: [
          { required: true, message: '请选择任务类型', trigger: 'change' }
        ],
        federatedAlgorithm: [
          { required: true, message: '请选择联邦学习算法', trigger: 'change' }
        ],
        trainingModel: [
          { required: true, message: '请选择训练模型', trigger: 'change' }
        ],
        datasetId: [
          { required: true, message: '请选择数据集', trigger: 'change' }
        ],
        expectedParticipants: [
          { required: true, message: '请输入预定参与数量', trigger: 'blur' }
        ],
        deadline: [
          { required: true, message: '请选择任务截止时间', trigger: 'change' }
        ],
        remark: [
          { required: true, message: '请填写任务备注', trigger: 'blur' },
          { min: 10, message: '备注长度不能少于 10 个字符', trigger: 'blur' }
        ]
      },
      taskList: [],
      datasetList: [],
      datasetDialogVisible: false
    }
  },
  mounted() {
    this.loadMyTasks()
    this.loadDatasets()
  },
  methods: {
    submitForm() {
      this.$refs.trainingForm.validate((valid) => {
        if (valid) {
          // 获取当前用户信息
          const user = JSON.parse(localStorage.getItem('user') || '{}')
          const taskData = {
            ...this.trainingForm,
            createdBy: user.id || 1
          }
          
          // 调用后端API创建任务
          this.$http.post('/training-tasks', taskData)
            .then(response => {
              if (response.code === 200) {
                this.$message.success('任务创建成功，等待一级管理员审核')
                this.resetForm()
                this.loadMyTasks()
              } else {
                this.$message.error('任务创建失败：' + response.message)
              }
            })
            .catch(() => {
              this.$message.error('网络错误，请稍后重试')
            })
        }
      })
    },
    resetForm() {
      this.$refs.trainingForm.resetFields()
      this.trainingForm.expectedParticipants = 2
      this.trainingForm.deadline = new Date()
    },
    loadMyTasks() {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      const userId = user.id || 1
      
      this.$http.get(`/training-tasks/created-by/${userId}`)
        .then(response => {
          if (response.code === 200) {
            this.taskList = response.data
          }
        })
        .catch(error => {
          console.error('加载任务列表失败', error)
        })
    },
    loadDatasets() {
      const userStr = localStorage.getItem('user')
      let userId = 1 // 默认用户ID
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          userId = user.id || 1
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
      
      getDatasetsByUploaderId(userId).then(response => {
        if (response.code === 200) {
          this.datasetList = response.data
        }
      }).catch(error => {
        console.error('获取数据集列表失败:', error)
      })
    },
    showDatasetDialog() {
      this.datasetDialogVisible = true
    },
    handleSelectDataset(dataset) {
      this.trainingForm.datasetId = dataset.id
      this.datasetDialogVisible = false
      this.$message.success(`已选择数据集: ${dataset.name}`)
    },
    viewTask(task) {
      // 查看任务详情
      this.$alert(`任务ID: ${task.id}\n任务名称: ${task.name}\n任务类型: ${task.taskType}\n联邦学习算法: ${task.federatedAlgorithm}\n训练模型: ${task.trainingModel}\n审核状态: ${task.auditStatus}\n任务状态: ${task.status}\n创建时间: ${task.createdAt}\n备注: ${task.remark}`, '任务详情', {
        confirmButtonText: '确定'
      })
    },
    getAuditStatusType(status) {
      switch (status) {
        case '待审核':
          return 'warning'
        case '已通过':
          return 'success'
        case '已拒绝':
          return 'danger'
        default:
          return 'info'
      }
    }
  }
}
</script>

<style scoped>
.model-training-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.training-card {
  margin-bottom: 20px;
}

.task-list-card {
  margin-top: 20px;
}
</style>
