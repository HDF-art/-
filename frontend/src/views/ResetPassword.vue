<template>
  <div class="reset-password-page">
    <div class="reset-card">
      <div class="reset-header">
        <h1>🔐 找回密码</h1>
        <p>请输入您的账号信息来重置密码</p>
      </div>
      
      <el-steps :active="step" finish-status="success" align-center class="steps">
        <el-step title="验证身份"></el-step>
        <el-step title="重置密码"></el-step>
        <el-step title="完成"></el-step>
      </el-steps>
      
      <!-- 步骤1: 验证身份 -->
      <el-form v-if="step === 0" :model="verifyForm" :rules="verifyRules" ref="verifyForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="verifyForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="verifyForm.phone" placeholder="请输入注册时的手机号">
            <template slot="append">
              <el-button @click="sendCode" :disabled="countdown > 0">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="验证码" prop="code">
          <el-input v-model="verifyForm.code" placeholder="请输入6位验证码" maxlength="6"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="verifyIdentity" :loading="loading" style="width: 100%;">
            验证身份
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 步骤2: 重置密码 -->
      <el-form v-if="step === 1" :model="resetForm" :rules="resetRules" ref="resetForm" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码" show-password></el-input>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="resetPassword" :loading="loading" style="width: 100%;">
            重置密码
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 步骤3: 完成 -->
      <div v-if="step === 2" class="success-section">
        <div class="success-icon">✅</div>
        <h2>密码重置成功！</h2>
        <p>您的新密码已设置成功，请使用新密码登录。</p>
        <el-button type="primary" @click="goToLogin" style="width: 100%; margin-top: 20px;">
          立即登录
        </el-button>
      </div>
      
      <!-- 返回登录 -->
      <div v-if="step < 2" class="back-login">
        <router-link to="/login">← 返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResetPassword',
  data() {
    // 密码验证
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'))
      } else {
        callback()
      }
    }
    
    // 确认密码验证
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
      
      // 模拟发送验证码
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
          // 模拟验证
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
          // 模拟重置密码
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
.reset-password-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.reset-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.reset-header {
  text-align: center;
  margin-bottom: 30px;
}

.reset-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.reset-header p {
  color: #666;
  font-size: 14px;
}

.steps {
  margin-bottom: 30px;
}

.back-login {
  text-align: center;
  margin-top: 20px;
}

.back-login a {
  color: #409eff;
  text-decoration: none;
}

.back-login a:hover {
  text-decoration: underline;
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
}
</style>

<style>
/* 移动端找回密码页优化 */
@media (max-width: 768px) {
  .reset-card {
    padding: 20px;
    width: 90%;
  }
  
  .reset-header h1 {
    font-size: 22px;
  }
  
  .steps {
    font-size: 12px;
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
