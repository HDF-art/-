<template>
  <div class="profile-container">
    <el-card v-loading="loading">
      <div slot="header" class="card-header">
        <span>个人信息</span>
        <el-button type="primary" size="small" @click="loadUserInfo">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>
      
      <el-form :model="userForm" label-width="120px" class="profile-form">
        <el-form-item label="用户ID">
          <el-input v-model="userForm.id" disabled></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="userForm.roleName" disabled></el-input>
        </el-form-item>
        <el-form-item label="所属农场">
          <el-input v-model="userForm.farmName" disabled placeholder="暂未加入农场"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userForm.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="头像">
          <div style="display: flex; align-items: center;">
            <el-upload
              class="avatar-uploader"
              action=""
              :auto-upload="false"
              :show-file-list="false"
              :http-request="customUpload"
              accept=".jpg,.jpeg,.png"
            >
              <img 
              :src="getAvatarUrl(userForm.avatar)" 
              class="avatar"
            >
            </el-upload>
            <div style="margin-left: 15px;">
              <div style="font-size: 12px; color: #999;">点击头像更换，支持jpg/png格式，大小不超过2MB</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">保存修改</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getUserInfo, updateUserInfo, uploadAvatar } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    return {
      loading: false,
      submitting: false,
      userForm: {
        id: '',
        username: '',
        role: '',
        roleName: '',
        farmId: '',
        farmName: '',
        name: '',
        email: '',
        phone: '',
        avatar: ''
      },
      originalData: {}
    }
  },
  created() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      this.loading = true
      try {
        const res = await getUserInfo()
        if (res && res.data) {
          const user = res.data
          this.userForm = {
            id: user.id || '',
            username: user.username || '',
            role: user.role || '',
            roleName: user.roleName || this.getRoleName(user.role),
            farmId: user.farmId || '',
            farmName: user.farmName || '',
            name: user.name || '',
            email: user.email || '',
            phone: user.phone || '',
            avatar: user.avatar || ''
          }
          this.originalData = { ...this.userForm }
          
          this.updateLocalStorage(user)
        }
      } catch (e) {
        console.error('获取用户信息失败', e)
        this.$message.error('获取用户信息失败')
        this.loadFromLocalStorage()
      } finally {
        this.loading = false
      }
    },
    
    loadFromLocalStorage() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          this.userForm = {
            id: user.id || '',
            username: user.username || '',
            role: user.role || '',
            roleName: user.roleName || this.getRoleName(user.role),
            farmId: user.farmId || '',
            farmName: user.farmName || '',
            name: user.name || '',
            email: user.email || '',
            phone: user.phone || '',
            avatar: user.avatar || ''
          }
          this.originalData = { ...this.userForm }
        } catch (e) {
          console.error('解析本地用户信息失败', e)
        }
      }
    },
    
    updateLocalStorage(user) {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const localUser = JSON.parse(userStr)
          const updatedUser = { ...localUser, ...user }
          localStorage.setItem('user', JSON.stringify(updatedUser))
        } catch (e) {
          console.error('更新本地存储失败', e)
        }
      }
    },
    
    getRoleName(role) {
      const roleMap = {
        1: '一级管理员',
        2: '二级管理员',
        3: '普通用户'
      }
      return roleMap[role] || '未知角色'
    },
    
    getAvatarUrl(avatar) {
      if (avatar) {
        if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
          return avatar
        }
        return `${process.env.VUE_APP_API_URL || 'http://localhost:8100/api'}${avatar}`
      }
      const seed = this.userForm.username || this.userForm.id || 'default'
      return `https://api.dicebear.com/9.x/jdenticon/svg?seed=${encodeURIComponent(seed)}`
    },
    
    async customUpload(options) {
      const file = options.file
      
      const isValidType = ['image/jpeg', 'image/png'].includes(file.type)
      if (!isValidType) {
        this.$message.error('上传头像图片只能是 JPG/PNG 格式!')
        return
      }
      
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
        return
      }
      
      try {
        const formData = new FormData()
        formData.append('file', file)
        
        const res = await uploadAvatar(formData)
        if (res && res.data && res.data.url) {
          this.userForm.avatar = res.data.url
          this.$message.success('头像上传成功')
          this.updateLocalStorage({ avatar: res.data.url })
        } else {
          this.$message.error('头像上传失败')
        }
      } catch (e) {
        console.error('头像上传失败', e)
        this.$message.error('头像上传失败: ' + (e.message || '未知错误'))
      }
    },
    
    async handleSubmit() {
      this.submitting = true
      try {
        const updateData = {
          name: this.userForm.name,
          email: this.userForm.email,
          phone: this.userForm.phone
        }
        
        const res = await updateUserInfo(updateData)
        if (res && (res.code === 200 || res.success)) {
          this.$message.success('个人信息更新成功')
          this.originalData = { ...this.userForm }
          this.updateLocalStorage(updateData)
        } else {
          this.$message.error(res.message || '更新失败')
        }
      } catch (e) {
        console.error('更新用户信息失败', e)
        this.$message.error('更新失败: ' + (e.message || '未知错误'))
      } finally {
        this.submitting = false
      }
    },
    
    handleCancel() {
      this.userForm = { ...this.originalData }
      this.$message.info('已取消修改')
    }
  }
}
</script>

<style scoped>
.profile-container { padding: 10px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.profile-form { max-width: 600px; }
.avatar-uploader { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; }
.avatar-uploader:hover { border-color: #409EFF; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; line-height: 100px; text-align: center; }
.avatar { width: 100px; height: 100px; display: block; object-fit: cover; }
</style>
