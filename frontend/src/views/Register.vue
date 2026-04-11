<template>
  <div class="login-container">
    <div class="top-right-link">
      <a href="/client.html" target="_blank">📱 APP下载</a>
    </div>
    
    <div class="dynamic-bg"></div>
    
    <div class="login-form-wrapper">
      <div class="login-brand">
        <img src="../assets/设计农业大数据平台 logo.png" alt="农业大数据平台" class="platform-logo" />
        <h1>农业大数据联合建模平台</h1>
        <p>智慧农业，数据驱动</p>
      </div>
      
      <el-card class="login-form">
        <div class="login-tabs">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="注册普通用户" name="user"></el-tab-pane>
            <el-tab-pane label="注册二级管理员" name="admin2"></el-tab-pane>
          </el-tabs>
        </div>
        
        <el-form ref="registerForm" :model="form" :rules="rules" label-position="left">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="用户名" 
              prefix-icon="el-icon-user" 
              autocomplete="off"
              class="form-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input 
              v-model="form.email" 
              placeholder="邮箱" 
              prefix-icon="el-icon-message"
              autocomplete="off"
              class="form-input"
            >
              <el-button 
                slot="append" 
                @click="sendCode" 
                :disabled="codeBtnDisabled"
                class="code-btn"
              >
                {{ codeBtnText }}
              </el-button>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="code">
            <el-input 
              v-model="form.code" 
              placeholder="邮箱验证码" 
              prefix-icon="el-icon-key" 
              autocomplete="off"
              maxlength="6"
              class="form-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码" 
              prefix-icon="el-icon-lock"
              show-password
              class="form-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="确认密码" 
              prefix-icon="el-icon-lock"
              show-password
              class="form-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item v-if="activeTab === 'admin2'" prop="organization">
            <el-input 
              v-model="form.organization" 
              placeholder="单位名称" 
              prefix-icon="el-icon-office-building"
              class="form-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleRegister" :loading="loading">
              {{ activeTab === 'user' ? '立即注册' : '提交申请' }}
            </el-button>
          </el-form-item>
          
          <div class="form-options" style="justify-content: center;">
            <span class="login-link">已有账号？<router-link to="/login">立即登录</router-link></span>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
import { sendRegisterCode, registerUser, registerAdmin2 } from '../api/user'

export default {
  name: 'Register',
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      activeTab: 'user',
      loading: false,
      codeBtnDisabled: false,
      codeBtnText: '获取验证码',
      countdown: 60,
      form: {
        username: '',
        email: '',
        code: '',
        password: '',
        confirmPassword: '',
        organization: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码为 6 位数字', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validatePassword, trigger: 'blur' }
        ],
        organization: [
          { required: true, message: '请输入单位名称', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async sendCode() {
      if (!this.form.email) {
        this.$message.warning('请先输入邮箱')
        return
      }
      
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
      if (!emailRegex.test(this.form.email)) {
        this.$message.warning('邮箱格式不正确')
        return
      }
      
      try {
        await sendRegisterCode(this.form.email)
        this.$message.success('验证码已发送到邮箱')
        this.codeBtnDisabled = true
        this.countdown = 60
        this.codeBtnText = `${this.countdown}s`
        
        const timer = setInterval(() => {
          this.countdown--
          this.codeBtnText = `${this.countdown}s`
          if (this.countdown <= 0) {
            clearInterval(timer)
            this.codeBtnDisabled = false
            this.codeBtnText = '获取验证码'
          }
        }, 1000)
      } catch (e) {
        this.$message.error(e.message || '发送失败，请稍后重试')
      }
    },
    async handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          this.loading = true
          try {
            if (this.activeTab === 'user') {
              await registerUser({
                username: this.form.username,
                email: this.form.email,
                code: this.form.code,
                password: this.form.password
              })
              this.$message.success('注册成功！')
              setTimeout(() => {
                this.$router.push('/login')
              }, 1500)
            } else {
              await registerAdmin2({
                username: this.form.username,
                email: this.form.email,
                code: this.form.code,
                password: this.form.password,
                organization: this.form.organization
              })
              this.$message.success('注册申请已提交，等待一级管理员审核')
              setTimeout(() => {
                this.$router.push('/login')
              }, 2000)
            }
          } catch (e) {
            this.$message.error(e.message || '注册失败，请稍后重试')
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.dynamic-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  background-image: url('/images/login-bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.top-right-link {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;
}

.top-right-link a {
  color: white;
  text-decoration: none;
  font-size: 14px;
  padding: 8px 16px;
  background: rgba(0, 255, 200, 0.2);
  border: 1px solid rgba(0, 255, 200, 0.4);
  border-radius: 4px;
  transition: all 0.3s ease;
}

.top-right-link a:hover {
  background: rgba(0, 255, 200, 0.3);
  border-color: rgba(0, 255, 200, 0.6);
}

.login-form-wrapper {
  width: 420px;
  padding: 20px;
  position: relative;
  z-index: 10;
}

.login-brand {
  text-align: center;
  margin-bottom: 30px;
}

.platform-logo {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 15px;
  box-shadow: 0 0 30px rgba(0, 255, 200, 0.3);
}

.login-brand h1 {
  color: white;
  font-size: 28px;
  margin: 0 0 10px 0;
  text-shadow: 0 0 20px rgba(0, 255, 200, 0.5);
}

.login-brand p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.login-form {
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.login-tabs {
  margin-bottom: 20px;
}

.login-tabs /deep/ .el-tabs__header {
  margin: 0 0 15px 0;
}

.login-tabs /deep/ .el-tabs__nav-wrap::after {
  height: 1px;
}

.login-tabs /deep/ .el-tabs__nav {
  width: 100%;
  display: flex;
}

.login-tabs /deep/ .el-tabs__item {
  flex: 1;
  text-align: center;
  font-size: 16px;
  padding: 0 20px;
}

.form-input {
  font-size: 14px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.login-link {
  color: #666;
  font-size: 14px;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  padding: 12px 0;
}

.code-btn {
  font-size: 14px;
}
</style>

<style>
@media (max-width: 768px) {
  .login-form-wrapper {
    width: 90%;
    padding: 10px;
  }
  
  .login-brand h1 {
    font-size: 22px;
  }
  
  .platform-logo {
    width: 60px;
    height: 60px;
  }
}
</style>
