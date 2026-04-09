<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-message">抱歉，您访问的页面不存在</div>
      <div class="error-description">可能是页面已删除或您输入了错误的地址</div>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  methods: {
    goHome() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 20px;
}

.error-content {
  text-align: center;
  background-color: #fff;
  padding: 60px 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 20px;
  line-height: 1;
}

.error-message {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.error-description {
  font-size: 14px;
  color: #909399;
  margin-bottom: 30px;
  line-height: 1.5;
}

.el-button {
  padding: 10px 30px;
  font-size: 16px;
}

/* 动画效果 */
.error-container {
  animation: fadeIn 0.5s ease-in-out;
}

.error-content {
  animation: slideUp 0.5s ease-in-out;
}

.error-code {
  animation: pulse 1s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes pulse {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }
  
  .error-message {
    font-size: 20px;
  }
  
  .error-content {
    padding: 40px 20px;
  }
}
</style>