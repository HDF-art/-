<template>
  <div class="register-container">
    <div class="register-form-wrapper">
      <div class="register-brand">
        <img src="../assets/设计农业大数据平台 logo.png" alt="农业大数据平台" class="platform-logo" />
        <h1>用户注册</h1>
        <p>农业大数据联合建模平台</p>
      </div>
      
      <el-card class="register-form">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="注册普通用户" name="user"></el-tab-pane>
          <el-tab-pane label="注册二级管理员" name="admin2"></el-tab-pane>
        </el-tabs>
        
        <el-form ref="registerForm" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <div class="email-input-group">
              <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
              <el-button @click="sendCode" :disabled="codeBtnDisabled" class="code-btn">
                {{ codeBtnText }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item label="邮箱验证码" prop="code">
            <el-input v-model="form.code" placeholder="请输入6位验证码"></el-input>
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input type="password" v-model="form.password" placeholder="请输入密码"></el-input>
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input type="password" v-model="form.confirmPassword" placeholder="请再次输入密码"></el-input>
          </el-form-item>
          
          <!-- 二级管理员需要输入单位 -->
          <el-form-item v-if="activeTab === 'admin2'" label="单位" prop="organization">
            <el-input v-model="form.organization" placeholder="请输入单位名称"></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleRegister" :loading="loading" class="register-btn">
              注册
            </el-button>
          </el-form-item>
          
          <div class="login-link-container">
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
        this.codeBtnText = `${this.countdown}秒后重发`
        
        const timer = setInterval(() => {
          this.countdown--
          this.codeBtnText = `${this.countdown}秒后重发`
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
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-form-wrapper {
  width: 500px;
}

.register-brand {
  text-align: center;
  margin-bottom: 30px;
}

.platform-logo {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 15px;
}

.register-brand h1 {
  color: white;
  font-size: 28px;
  margin: 0 0 10px 0;
}

.register-brand p {
  color: rgba(255,255,255,0.8);
  font-size: 14px;
}

.register-form {
  border-radius: 10px;
}

.register-btn {
  width: 100%;
  margin-bottom: 10px;
}

.login-link {
  color: #666;
  font-size: 14px;
}

.login-link a {
  color: #409eff;
}

.email-input-group {
  display: flex;
  gap: 10px;
}

.email-input-group .el-input {
  flex: 1;
}

.code-btn {
  flex-shrink: 0;
  min-width: 120px;
}

.register-form /deep/ .el-tabs__header {
  margin: 0 0 15px 0;
}

.register-form /deep/ .el-tabs__nav-wrap::after {
  height: 1px;
}

.register-form /deep/ .el-tabs__nav {
  width: 100%;
  display: flex;
}

.register-form /deep/ .el-tabs__item {
  flex: 1;
  text-align: center;
  font-size: 16px;
  padding: 0 20px;
}

.login-link-container {
  text-align: center;
  margin-top: 10px;
}

.login-link {
  color: #666;
  font-size: 14px;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.register-btn {
  width: 100%;
}
</style>
