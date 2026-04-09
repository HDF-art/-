<template>
  <div class="change-password-container">
    <el-card>
      <div slot="header" class="card-header">
        <span>修改密码</span>
      </div>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="120px" class="password-form">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" type="password" placeholder="请输入当前密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">确认修改</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { updatePassword } from '../../api/user'

export default {
  name: 'ChangePassword',
  data() {
    // 验证确认密码
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的新密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        currentPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' },
          {
            pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{6,}$/,
            message: '密码必须包含字母和数字',
            trigger: 'blur'
          }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    // 提交修改
    async handleSubmit() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (valid) {
          try {
            // 调用API修改密码
            const response = await updatePassword({
              currentPassword: this.passwordForm.currentPassword,
              newPassword: this.passwordForm.newPassword
            })
            
            if (response.code === 200) {
              this.$message.success('密码修改成功')
              this.handleReset() // 重置表单
              
              // 密码修改成功后可以提示用户重新登录
              this.$confirm('密码已修改成功，是否重新登录?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'info'
              }).then(() => {
                // 清除登录信息并跳转到登录页
                localStorage.removeItem('token')
                localStorage.removeItem('user')
                window.location.href = '/login'
              }).catch(() => {
                // 用户取消重新登录
              })
            } else {
              this.$message.error('密码修改失败: ' + (response.message || '未知错误'))
            }
          } catch (error) {
            console.error('修改密码失败:', error)
            this.$message.error('密码修改失败: ' + (error.message || '未知错误'))
          }
        }
      })
    },
    
    // 重置表单
    handleReset() {
      this.$refs.passwordForm.resetFields()
    }
  }
}
</script>

<style scoped>
.change-password-container {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.password-form {
  margin-top: 20px;
  max-width: 600px;
}
</style>