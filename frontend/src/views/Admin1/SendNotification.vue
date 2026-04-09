<template>
  <div class="send-notification">
    <el-card>
      <div slot="header" class="card-header">
        <span>发送通知</span>
      </div>
      
      <el-form :model="form" label-width="120px" class="send-form">
        <el-form-item label="通知标题" required>
          <el-input v-model="form.title" placeholder="请输入通知标题"></el-input>
        </el-form-item>
        
        <el-form-item label="通知内容" required>
          <el-input type="textarea" v-model="form.content" :rows="6" placeholder="请输入通知内容"></el-input>
        </el-form-item>
        
        <el-form-item label="接收者类型" required>
          <el-radio-group v-model="form.receiverType">
            <el-radio :label="1">所有用户</el-radio>
            <el-radio :label="2">一级管理员</el-radio>
            <el-radio :label="3">二级管理员</el-radio>
            <el-radio :label="4">普通用户</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="特定用户" v-if="form.receiverType === 5">
          <el-select
            v-model="form.specificUserIds"
            multiple
            placeholder="请选择用户"
            style="width: 100%"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">发送通知</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { sendNotificationToUsers, getUserList } from '../../api/user'

export default {
  name: 'SendNotification',
  data() {
    return {
      loading: false,
      form: {
        title: '',
        content: '',
        receiverType: 1,
        specificUserIds: [],
        senderId: null,
        farmId: null
      },
      userList: []
    }
  },
  mounted() {
    this.loadUserList()
    this.loadCurrentUser()
  },
  methods: {
    async loadUserList() {
      try {
        const res = await getUserList()
        if (res.data && res.data.data) {
          this.userList = res.data.data
        }
      } catch (e) {
        console.error('加载用户列表失败', e)
      }
    },
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
      
      this.loading = true
      
      try {
        const res = await sendNotificationToUsers({
          title: this.form.title,
          content: this.form.content,
          senderId: this.form.senderId,
          receiverType: this.form.receiverType,
          farmId: this.form.farmId,
          specificUserIds: this.form.specificUserIds
        })
        
        if (res.data.code === 200) {
          this.$message.success('通知发送成功')
          this.form.title = ''
          this.form.content = ''
          this.form.receiverType = 1
          this.form.specificUserIds = []
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
  padding: 10px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.send-form {
  max-width: 600px;
}
</style>
