<template>
  <div class="login-container">
    <div class="top-right-link">
      <a href="/client.html" target="_blank">📱 APP下载</a>
    </div>
    
    <!-- 动态背景 -->
    <div class="dynamic-bg">
      <div class="sky"></div>
      <div class="data-flow"></div>
      <div class="grid-lines"></div>
      <div class="particles"></div>
      <div class="field"></div>
    </div>
    
    <div class="login-form-wrapper">
      <div class="login-brand">
        <img src="../assets/设计农业大数据平台 logo.png" alt="农业大数据平台" class="platform-logo" />
        <h1>农业大数据联合建模平台</h1>
        <p>智能农业，数据驱动</p>
      </div>
      <el-card class="login-form">
        <div class="login-tabs">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="账号密码登录" name="password"></el-tab-pane>
            <el-tab-pane label="邮箱验证码登录" name="email"></el-tab-pane>
          </el-tabs>
        </div>
        
        <el-form v-if="activeTab === 'password'" ref="loginForm" :model="loginForm" :rules="loginRules" label-position="left">
          <div class="form-header">
            <h2>欢迎登录</h2>
            <p>请输入您的账号信息</p>
          </div>
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="用户名" 
              prefix-icon="el-icon-user" 
              autocomplete="off"
              @keyup.enter.native="handleLogin"
              class="form-input"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="密码" 
              prefix-icon="el-icon-lock"
              autocomplete="off"
              show-password
              @keyup.enter.native="handleLogin"
              class="form-input"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
              <span class="link-group">
                <router-link to="/register">免费注册</router-link>
                <router-link to="/reset-password">忘记密码？</router-link>
              </span>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">安全登录</el-button>
          </el-form-item>
        </el-form>
        
        <el-form v-else ref="emailLoginForm" :model="emailLoginForm" :rules="emailLoginRules" label-position="left">
          <div class="form-header">
            <h2>邮箱验证码登录</h2>
            <p>请输入您的邮箱获取验证码</p>
          </div>
          <el-form-item prop="email">
            <el-input 
              v-model="emailLoginForm.email" 
              placeholder="邮箱" 
              prefix-icon="el-icon-message" 
              autocomplete="off"
              @keyup.enter.native="handleEmailLogin"
              class="form-input"
            ></el-input>
          </el-form-item>
          <el-form-item prop="code">
            <el-row :gutter="10">
              <el-col :span="16">
                <el-input 
                  v-model="emailLoginForm.code" 
                  placeholder="验证码" 
                  prefix-icon="el-icon-message" 
                  autocomplete="off"
                  @keyup.enter.native="handleEmailLogin"
                  class="form-input"
                ></el-input>
              </el-col>
              <el-col :span="8">
                <el-button 
                  type="warning" 
                  @click="sendVerifyCode" 
                  :disabled="codeBtnDisabled"
                  class="code-btn"
                >
                  {{ codeBtnText }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-btn" 
              @click="handleEmailLogin" 
              :loading="loading"
            >
              验证登录
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
import { sendCode, emailLogin } from '../api/user'

export default {
  name: 'Login',
  data() {
    return {
      activeTab: 'password',
      loginForm: {
        username: '',
        password: '',
        remember: false
      },
      emailLoginForm: {
        email: '',
        code: ''
      },
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      emailLoginRules: {
        email: [
          { required: true, message: '请输入正确的邮箱地址', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$/, message: '邮箱格式不正确', trigger: 'blur' }
        ],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      loading: false,
      codeBtnDisabled: false,
      codeBtnText: '获取验证码',
      countdown: 60
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          this.$store.dispatch('login', this.loginForm)
            .then(() => {
              this.$message.success('登录成功');
              this.$router.push('/home/data-panel').catch(() => {});
            })
            .catch((error) => {
              this.$message.error(error.message || '登录失败，请检查用户名或密码');
            })
            .finally(() => {
              this.loading = false;
            });
        }
      });
    },
    sendVerifyCode() {
      if (!this.emailLoginForm.email) {
        this.$message.warning('请输入邮箱')
        return
      }
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
      if (!emailRegex.test(this.emailLoginForm.email)) {
        this.$message.warning('邮箱格式不正确')
        return
      }
      sendCode({ email: this.emailLoginForm.email }).then(() => {
        this.$message.success('验证码已发送')
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
      }).catch(err => {
        this.$message.error(err.message || '发送失败')
      })
    },
    handleEmailLogin() {
      this.$refs.emailLoginForm.validate(valid => {
        if (valid) {
          this.loading = true
          emailLogin(this.emailLoginForm).then(res => {
            localStorage.setItem('token', res.data.token)
            localStorage.setItem('user', JSON.stringify(res.data.userInfo))
            this.$message.success('登录成功')
            this.$router.push('/home/data-panel').catch(() => {})
          }).catch(err => {
            if (err.message && err.message.includes('用户不存在')) {
              this.$message.warning('用户不存在，请先注册')
              setTimeout(() => {
                this.$router.push('/register')
              }, 1500)
            } else {
              this.$message.error(err.message || '登录失败')
            }
          }).finally(() => {
            this.loading = false
          })
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

/* 动态背景 */
.dynamic-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  background: linear-gradient(180deg, 
    #0a1628 0%, 
    #0d2137 30%, 
    #1a3a5c 60%, 
    #0d4a3a 100%);
}

/* 天空 */
.sky {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 60%;
  background: 
    radial-gradient(ellipse at 20% 20%, rgba(0, 200, 150, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 30%, rgba(100, 80, 200, 0.1) 0%, transparent 40%),
    linear-gradient(180deg, #0a1628 0%, #1a3a5c 100%);
  animation: skyGlow 8s ease-in-out infinite;
}

@keyframes skyGlow {
  0%, 100% { opacity: 0.8; }
  50% { opacity: 1; }
}

/* 数据流 */
.data-flow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    repeating-linear-gradient(
      90deg,
      transparent 0,
      transparent 100px,
      rgba(0, 255, 200, 0.03) 100px,
      rgba(0, 255, 200, 0.03) 101px
    ),
    repeating-linear-gradient(
      0deg,
      transparent 0,
      transparent 100px,
      rgba(0, 255, 200, 0.03) 100px,
      rgba(0, 255, 200, 0.03) 101px
    );
  animation: dataFlow 20s linear infinite;
}

@keyframes dataFlow {
  0% { transform: translateY(0); }
  100% { transform: translateY(100px); }
}

/* 网格线 */
.grid-lines {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 40%;
  background: 
    linear-gradient(90deg, rgba(0, 255, 200, 0.05) 1px, transparent 1px),
    linear-gradient(rgba(0, 255, 200, 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
  transform: perspective(500px) rotateX(60deg);
  transform-origin: bottom;
  animation: gridPulse 4s ease-in-out infinite;
}

@keyframes gridPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

/* 粒子 */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(2px 2px at 20% 30%, rgba(0, 255, 200, 0.8), transparent),
    radial-gradient(2px 2px at 40% 70%, rgba(100, 200, 255, 0.6), transparent),
    radial-gradient(1px 1px at 60% 20%, rgba(0, 255, 150, 0.7), transparent),
    radial-gradient(2px 2px at 80% 50%, rgba(150, 100, 255, 0.5), transparent),
    radial-gradient(1px 1px at 10% 80%, rgba(0, 200, 200, 0.6), transparent),
    radial-gradient(1px 1px at 70% 10%, rgba(255, 255, 255, 0.5), transparent),
    radial-gradient(2px 2px at 90% 90%, rgba(0, 255, 200, 0.4), transparent),
    radial-gradient(1px 1px at 30% 50%, rgba(100, 150, 255, 0.5), transparent);
  animation: particleFloat 15s ease-in-out infinite;
}

@keyframes particleFloat {
  0%, 100% { transform: translateY(0); opacity: 0.8; }
  50% { transform: translateY(-20px); opacity: 1; }
}

/* 田野 */
.field {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 35%;
  background: linear-gradient(180deg, 
    transparent 0%, 
    rgba(20, 80, 60, 0.3) 30%,
    rgba(15, 60, 45, 0.5) 100%
  );
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

.form-header {
  text-align: center;
  margin-bottom: 25px;
}

.form-header h2 {
  margin: 0 0 8px 0;
  color: #333;
}

.form-header p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.form-input {
  font-size: 14px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-checkbox {
  margin: 0;
}

.forgot-password {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  padding: 12px 0;
}

.code-btn {
  width: 100%;
  font-size: 14px;
}
</style>