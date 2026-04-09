<template>
  <div class="send-notification">
    <el-card>
      <div slot="header" class="card-header">
        <span>发送通知</span>
      </div>
      
      <el-alert
        title="您可以向本单位（农场）的普通用户发送通知"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      ></el-alert>
      
      <el-form :model="form" label-width="120px" class="send-form">
        <el-form-item label="通知标题" required>
          <el-input v-model="form.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>
        
        <el-form-item label="通知内容" required>
          <el-input type="textarea" v-model="form.content" :rows="6" placeholder="请输入通知内容"></el-input>
        </el-form-item>
        
        <el-form-item label="接收者类型" required>
          <el-radio-group v-model="form.receiverType">
            <el-radio :label="4">本单位普通用户</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">发送通知</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { sendNotificationToUsers } from '../../api/user'

export default {
  name: 'SendNotification',
  data() {
    return {
      loading: false,
      form: {
        title: '',
        content: '',
        receiverType: 4,
        senderId: null,
        farmId: null
      }
    }
  },
  mounted() {
    this.loadCurrentUser()
  },
  methods: {
    loadCurrentUser() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          this.form.senderId = user.id
          this.form.farmId = user.farmId
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }
    },
    async handleSubmit() {
      if (!this.form.title) {
        this.$message.error('请输入通知标题')
        return
      }
      
      if (!this.form.content) {
        this.$message.error('请输入通知内容')
        return
      }
      
      if (!this.form.farmId) {
        this.$message.error('您没有关联农场，无法发送通知')
        return
      }
      
      this.loading = true
      
      try {
        const res = await sendNotificationToUsers({
          title: this.form.title,
          content: this.form.content,
          senderId: this.form.senderId,
          receiverType: this.form.receiverType,
          farmId: this.form.farmId
        })
        
        if (res.data.code === 200) {
          this.$message.success('通知发送成功')
          this.form.title = ''
          this.form.content = ''
        } else {
          this.$message.error(res.data.message || '发送失败')
        }
      } catch (e) {
        this.$message.error('发送失败: ' + e.message)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.send-notification {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.send-form {
  max-width: 600px;
}
</style>
