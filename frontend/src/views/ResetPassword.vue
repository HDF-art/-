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
        <div class="form-header">
          <h2>🔐 找回密码</h2>
          <p>请输入您的账号信息来重置密码</p>
        </div>
        
        <el-steps :active="step" finish-status="success" align-center class="steps" simple>
          <el-step title="验证身份"></el-step>
          <el-step title="重置密码"></el-step>
          <el-step title="完成"></el-step>
        </el-steps>
        
        <el-form v-if="step === 0" :model="verifyForm" :rules="verifyRules" ref="verifyForm" label-position="left">
          <el-form-item prop="username">
            <el-input 
              v-model="verifyForm.username" 
              placeholder="用户名" 
              prefix-icon="el-icon-user" 
              autocomplete="off"
              class="form-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input 
              v-model="verifyForm.phone" 
              placeholder="手机号" 
              prefix-icon="el-icon-mobile-phone"
              autocomplete="off"
              class="form-input"
            >
              <el-button 
                slot="append" 
                @click="sendCode" 
                :disabled="countdown > 0"
                class="code-btn"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="code">
            <el-input 
              v-model="verifyForm.code" 
              placeholder="验证码" 
              prefix-icon="el-icon-message" 
              autocomplete="off"
              maxlength="6"
              class="form-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="verifyIdentity" :loading="loading">
              验证身份
            </el-button>
          </el-form-item>
        </el-form>
        
        <el-form v-if="step === 1" :model="resetForm" :rules="resetRules" ref="resetForm" label-position="left">
          <el-form-item prop="newPassword">
            <el-input 
              v-model="resetForm.newPassword" 
              type="password" 
              placeholder="新密码" 
              prefix-icon="el-icon-lock"
              show-password
              class="form-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="resetForm.confirmPassword" 
              type="password" 
              placeholder="确认新密码" 
              prefix-icon="el-icon-lock"
              show-password
              class="form-input"
            ></el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="resetPassword" :loading="loading">
              重置密码
            </el-button>
          </el-form-item>
        </el-form>
        
        <div v-if="step === 2" class="success-section">
          <div class="success-icon">✅</div>
          <h2>密码重置成功！</h2>
          <p>您的新密码已设置成功，请使用新密码登录。</p>
          <el-button type="primary" class="login-btn" @click="goToLogin">
            立即登录
          </el-button>
        </div>
        
        <div v-if="step < 2" class="form-options" style="justify-content: center;">
          <router-link to="/login" class="forgot-password">← 返回登录</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResetPassword',
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'))
      } else {
        callback()
      }
    }
    
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.resetForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      step: 0,
      loading: false,
      countdown: 0,
      verifyForm: {
        username: '',
        phone: '',
        code: ''
      },
      verifyRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码为6位数字', trigger: 'blur' }
        ]
      },
      resetForm: {
        newPassword: '',
        confirmPassword: ''
      },
      resetRules: {
        newPassword: [
          { required: true, validator: validatePass, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validatePass2, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    sendCode() {
      if (!this.verifyForm.phone) {
        this.$message.warning('请先输入手机号')
        return
      }
      
      if (!/^1[3-9]\d{9}$/.test(this.verifyForm.phone)) {
        this.$message.warning('手机号格式不正确')
        return
      }
      
      this.$message.success('验证码已发送')
      this.countdown = 60
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    },
    verifyIdentity() {
      this.$refs.verifyForm.validate(valid => {
        if (valid) {
          this.loading = true
          setTimeout(() => {
            this.loading = false
            this.step = 1
            this.$message.success('身份验证成功')
          }, 1000)
        }
      })
    },
    resetPassword() {
      this.$refs.resetForm.validate(valid => {
        if (valid) {
          this.loading = true
          setTimeout(() => {
            this.loading = false
            this.step = 2
            this.$message.success('密码重置成功')
          }, 1000)
        }
      })
    },
    goToLogin() {
      this.$router.push('/login')
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

.form-header {
  text-align: center;
  margin-bottom: 20px;
}

.form-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 20px;
}

.form-header p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.steps {
  margin-bottom: 25px;
}

.form-input {
  font-size: 14px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-password {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.forgot-password:hover {
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

.success-section {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.success-section h2 {
  color: #67c23a;
  margin-bottom: 10px;
}

.success-section p {
  color: #666;
  margin-bottom: 20px;
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
  
  .el-steps--simple {
    padding: 8px;
  }
  
  .el-step__title {
    font-size: 11px;
  }
  
  .el-step__icon {
    width: 24px;
    height: 24px;
  }
}
</style>
