<template>
  <div class="theme-switch">
    <el-switch
      v-model="isDark"
      @change="toggleTheme"
      active-color="#409EFF"
      inactive-color="#DCDFE6"
    >
    </el-switch>
    <span class="theme-label">{{ isDark ? '🌙' : '☀️' }}</span>
  </div>
</template>

<script>
export default {
  name: 'ThemeSwitch',
  data() {
    return {
      isDark: false
    }
  },
  mounted() {
    // 读取保存的主题
    const savedTheme = localStorage.getItem('theme') || 'light'
    this.isDark = savedTheme === 'dark'
    this.applyTheme()
  },
  methods: {
    toggleTheme() {
      this.applyTheme()
      localStorage.setItem('theme', this.isDark ? 'dark' : 'light')
      this.$message.success(this.isDark ? '已切换到深色模式' : '已切换到浅色模式')
    },
    applyTheme() {
      if (this.isDark) {
        document.body.classList.add('dark-theme')
        document.body.classList.remove('light-theme')
      } else {
        document.body.classList.add('light-theme')
        document.body.classList.remove('dark-theme')
      }
    }
  }
}
</script>

<style scoped>
.theme-switch {
  display: flex;
  align-items: center;
  gap: 8px;
}
.theme-label {
  font-size: 18px;
}
</style>
