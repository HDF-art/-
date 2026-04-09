<template>
  <div class="join-farm">
    <el-card>
      <div slot="header" class="card-header">
        <span>加入农场</span>
      </div>
      <el-form :model="form" label-width="120px" class="join-form">
        <el-form-item label="农场ID">
          <el-input v-model="form.farmId" placeholder="请输入农场ID"></el-input>
        </el-form-item>
        <el-form-item label="农场名称">
          <el-input v-model="form.farmName" placeholder="请输入农场名称"></el-input>
        </el-form-item>
        <el-form-item label="申请理由">
          <el-input type="textarea" v-model="form.reason" :rows="4" placeholder="请输入申请理由"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交申请</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { createAuditRequest } from '../../api/user'

export default {
  name: 'JoinFarm',
  data() {
    return {
      form: {
        farmId: '',
        farmName: '',
        reason: ''
      }
    }
  },
  methods: {
    async handleSubmit() {
      if (!this.form.farmId) {
        this.$message.error('请输入农场ID')
        return
      }
      
      if (!this.form.reason) {
        this.$message.error('请输入申请理由')
        return
      }
      
      try {
        // 获取当前用户信息
        const userStr = localStorage.getItem('user')
        let user = null
        if (userStr) {
          user = JSON.parse(userStr)
        }
        
        if (!user) {
          this.$message.error('用户信息不存在')
          return
        }
        
        // 创建审核请求
        const auditRequest = {
          applicantId: user.id || 1,
          type: 3, // 普通用户加入农场
          farmId: parseInt(this.form.farmId),
          content: JSON.stringify({
            reason: this.form.reason,
            farmName: this.form.farmName
          })
        }
        
        const response = await createAuditRequest(auditRequest)
        
        if (response.code === 200) {
          this.$message.success('加入农场申请已提交，等待农场管理员审核')
          // 重置表单
          this.form = {
            farmId: '',
            farmName: '',
            reason: ''
          }
        } else {
          this.$message.error('提交申请失败: ' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('提交申请失败:', error)
        this.$message.error('提交申请失败: ' + (error.message || '未知错误'))
      }
    },
    handleCancel() {
      this.form = {
        farmId: '',
        farmName: '',
        reason: ''
      }
      this.$message.info('已取消操作')
    }
  }
}
</script>

<style scoped>
.join-farm { padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.join-form { margin-top: 20px; }
</style>
