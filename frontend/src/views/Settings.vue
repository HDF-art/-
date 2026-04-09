<template>
  <div class="settings-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span class="card-title">👤 个人信息</span>
      </div>
      
      <el-form :model="profileForm" label-width="100px" class="profile-form" v-loading="profileLoading">
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-tag :type="getRoleType(profileForm.role)">{{ getRoleText(profileForm.role) }}</el-tag>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="单位" v-if="profileForm.role === 2 || profileForm.role === 'admin2'">
          <el-input v-model="profileForm.organization"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="box-card" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span class="card-title">⚙️ 系统设置</span>
      </div>
      
      <el-form label-width="120px" class="setting-form">
        <el-divider content-position="left">🎨 主题设置</el-divider>
        
        <el-form-item label="深色模式">
          <el-switch
            v-model="settings.darkMode"
            @change="toggleTheme"
            active-color="#409EFF"
            inactive-color="#DCDFE6">
          </el-switch>
          <span class="setting-desc">{{ settings.darkMode ? '🌙 已开启深色模式' : '☀️ 已开启浅色模式' }}</span>
        </el-form-item>
        
        <el-divider content-position="left">🔔 通知设置</el-divider>
        
        <el-form-item label="邮件通知">
          <el-switch v-model="settings.emailNotify" @change="saveSettings"></el-switch>
        </el-form-item>
        
        <el-form-item label="训练完成提醒">
          <el-switch v-model="settings.trainingNotify" @change="saveSettings"></el-switch>
        </el-form-item>
        
        <el-divider content-position="left">🔒 安全设置</el-divider>
        
        <el-form-item label="操作审计">
          <el-switch v-model="settings.auditEnabled" @change="saveSettings"></el-switch>
          <span class="setting-desc">记录所有操作日志</span>
        </el-form-item>
        
        <el-divider content-position="left">⚡ 性能设置</el-divider>
        
        <el-form-item label="数据缓存">
          <el-switch v-model="settings.cacheEnabled" @change="saveSettings"></el-switch>
          <span class="setting-desc">启用Redis缓存热点数据</span>
        </el-form-item>
        
        <el-divider content-position="left">📊 联邦学习设置</el-divider>
        
        <el-form-item label="默认算法">
          <el-select v-model="settings.defaultAlgorithm" @change="saveSettings">
            <el-option label="FedAvg" value="FedAvg"></el-option>
            <el-option label="FedProx" value="FedProx"></el-option>
            <el-option label="SCAFFOLD" value="SCAFFOLD"></el-option>
            <el-option label="FedNova" value="FedNova"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="默认模型">
          <el-select v-model="settings.defaultModel" @change="saveSettings">
            <el-option label="CNN" value="CNN"></el-option>
            <el-option label="LSTM" value="LSTM"></el-option>
            <el-option label="ResNet" value="ResNet"></el-option>
            <el-option label="XGBoost" value="XGBoost"></el-option>
            <el-option label="LinearRegression" value="LinearRegression"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="默认轮次">
          <el-input-number v-model="settings.defaultRounds" :min="1" :max="100" @change="saveSettings"></el-input-number>
          <span class="setting-desc">联邦学习默认轮次</span>
        </el-form-item>
        
        <el-divider content-position="left">💾 缓存管理</el-divider>
        
        <el-form-item>
          <el-button @click="clearCache" type="warning">清除缓存</el-button>
          <el-button @click="clearAllData" type="danger">清除所有数据</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="box-card" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span class="card-title">ℹ️ 系统信息</span>
      </div>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="部署时间">2026-03-11</el-descriptions-item>
        <el-descriptions-item label="前端框架">Vue 2 + Element UI</el-descriptions-item>
        <el-descriptions-item label="后端框架">Spring Boot 2.7</el-descriptions-item>
        <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
        <el-descriptions-item label="缓存">Redis 6.0</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { updateCurrentUser } from '@/api/user'

export default {
  name: 'Settings',
  data() {
    return {
      profileLoading: false,
      profileForm: {
        username: '',
        role: '',
        email: '',
        phone: '',
        organization: ''
      },
      settings: {
        darkMode: false,
        emailNotify: true,
        trainingNotify: true,
        auditEnabled: true,
        cacheEnabled: true,
        defaultAlgorithm: 'FedAvg',
        defaultModel: 'CNN',
        defaultRounds: 10
      }
    }
  },
  mounted() {
    this.loadSettings()
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      this.profileLoading = true
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        this.profileForm = {
          username: user.username || '',
          role: user.role,
          email: user.email || '',
          phone: user.phone || '',
          organization: user.organization || ''
        }
      } catch (e) {
        console.error('加载用户信息失败', e)
      } finally {
        this.profileLoading = false
      }
    },
    async saveProfile() {
      try {
        await updateCurrentUser({
          email: this.profileForm.email,
          phone: this.profileForm.phone,
          organization: this.profileForm.organization
        })
        
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        user.email = this.profileForm.email
        user.phone = this.profileForm.phone
        user.organization = this.profileForm.organization
        localStorage.setItem('user', JSON.stringify(user))
        
        this.$message.success('个人信息已更新')
      } catch (e) {
        this.$message.error(e.message || '更新失败')
      }
    },
    getRoleType(role) {
      switch (role) {
        case 1:
        case 'admin1': return 'primary'
        case 2:
        case 'admin2': return 'success'
        case 3:
        case 'user': return 'info'
        default: return 'info'
      }
    },
    getRoleText(role) {
      const roleMap = {
        1: '一级管理员',
        2: '二级管理员',
        3: '普通用户',
        'admin1': '一级管理员',
        'admin2': '二级管理员',
        'user': '普通用户'
      }
      return roleMap[role] || '未知角色'
    },
    loadSettings() {
      const saved = localStorage.getItem('userSettings')
      if (saved) {
        this.settings = { ...this.settings, ...JSON.parse(saved) }
      }
      
      this.settings.darkMode = localStorage.getItem('theme') === 'dark'
    },
    toggleTheme() {
      if (this.settings.darkMode) {
        document.body.classList.add('dark-theme')
        localStorage.setItem('theme', 'dark')
      } else {
        document.body.classList.remove('dark-theme')
        localStorage.setItem('theme', 'light')
      }
      this.saveSettings()
      this.$message.success(this.settings.darkMode ? '已切换到深色模式' : '已切换到浅色模式')
    },
    saveSettings() {
      localStorage.setItem('userSettings', JSON.stringify(this.settings))
    },
    async clearCache() {
      try {
        await this.$confirm('确定要清除缓存吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        this.$message.success('缓存已清除')
      } catch (e) {
        // 用户取消操作
      }
    },
    async clearAllData() {
      try {
        await this.$confirm('确定要清除所有数据吗? 此操作不可恢复!', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'error'
        })
        
        this.$message.warning('数据已清除')
      } catch (e) {
        // 用户取消操作
      }
    }
  }
}
</script>

<style scoped>
.settings-container {
  padding: 10px;
}
.profile-form {
  max-width: 500px;
}
.setting-form {
  max-width: 800px;
}
.setting-desc {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
.card-title {
  font-size: 18px;
  font-weight: bold;
}
</style>
