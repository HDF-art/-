<template>
  <el-container class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="home-header">
      <div class="header-left">
        <el-button 
          type="text" 
          icon="el-icon-menu" 
          @click="toggleSidebar"
          class="menu-toggle"
        ></el-button>
        <div class="logo">
          <img src="../assets/设计农业大数据平台 logo.png" alt="农业大数据平台" class="platform-logo" />
          <span class="logo-text">农业大数据联合建模平台</span>
        </div>
      </div>
      <div class="header-center">
        <div class="system-status">
          <el-tag size="small" type="success" effect="plain">系统正常</el-tag>
          <span class="current-time">{{ currentTime }}</span>
        </div>
      </div>
      <div class="header-right">
        <div class="header-actions">
          <el-dropdown trigger="click">
            <el-button size="small" type="text" class="header-action-btn">
              <i class="el-icon-bell"></i>
              <span class="notification-badge" v-if="notificationCount > 0">{{ notificationCount }}</span>
            </el-button>
            <el-dropdown-menu slot="dropdown" class="notification-menu">
              <el-dropdown-item>
                <div class="notification-header">
                  <span>通知中心</span>
                  <el-button size="text" type="primary">全部标为已读</el-button>
                </div>
              </el-dropdown-item>
              <el-dropdown-item disabled v-if="notificationCount === 0">
                <p class="notification-empty">暂无通知</p>
              </el-dropdown-item>
              <el-dropdown-item v-else>
                <div class="notification-item">
                  <div class="notification-icon success"><i class="el-icon-circle-check"></i></div>
                  <div class="notification-content">
                    <p class="notification-title">模型训练完成</p>
                    <p class="notification-time">10分钟前</p>
                  </div>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="36" :src="userAvatar" class="user-avatar"></el-avatar>
              <span class="user-name">{{ effectiveUser.username || '管理员' }}</span>
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown" class="user-menu">
              <el-dropdown-item icon="el-icon-user" @click.native="toProfile">
                <span>个人信息</span>
              </el-dropdown-item>
              <el-dropdown-item icon="el-icon-key" @click.native="toChangePassword">
                <span>修改密码</span>
              </el-dropdown-item>
              <el-dropdown-item divided @click.native="handleLogout">
                <span class="logout-text">退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <el-container>
      <!-- 左侧菜单栏 -->
      <el-aside :width="sidebarWidth" class="home-aside" :class="{ 'collapse': isCollapse }">
        <div class="sidebar-header" v-if="!isCollapse && user">
          <div class="sidebar-user">
            <el-avatar :size="48" :src="userAvatar"></el-avatar>
            <div class="user-details">
              <p class="sidebar-username">{{ effectiveUser.username || '管理员' }}</p>
              <p class="sidebar-role">{{ getRoleName(effectiveUser.role) }}</p>
            </div>
          </div>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo sidebar-menu"
          @select="handleMenuSelect"
          :collapse="isCollapse"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#409EFF"
          router
        >
          <!-- 数据面板 -->
          <el-menu-item index="/home/data-panel" class="menu-item">
            <i class="el-icon-document menu-icon"></i>
            <span slot="title">数据面板</span>
          </el-menu-item>
          
          <!-- 审计日志 -->
          <el-menu-item index="/home/audit-log" class="menu-item">
            <i class="el-icon-s-order menu-icon"></i>
            <span slot="title">审计日志</span>
          </el-menu-item>
          
          <!-- 系统设置 -->
          <el-menu-item index="/home/settings" class="menu-item">
            <i class="el-icon-s-tools menu-icon"></i>
            <span slot="title">系统设置</span>
          </el-menu-item>
          
          <!-- 一级管理员专属菜单 -->
          <template v-if="effectiveUser && (effectiveUser.role === 'admin1' || effectiveUser.role === 1)">
            <!-- 系统管理菜单 -->
            <el-submenu index="/home/admin1">
              <template slot="title">
                <i class="el-icon-setting menu-icon"></i>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/home/admin1/user-manage">用户管理</el-menu-item>
              <el-menu-item index="/home/admin1/model-manage">模型管理</el-menu-item>
              <el-menu-item index="/home/admin1/send-notification">发送通知</el-menu-item>
            </el-submenu>
        
            
            <!-- 数据管理菜单 -->
            <el-submenu index="/home/admin1/data">
              <template slot="title">
                <i class="el-icon-data-analysis menu-icon"></i>
                <span>数据管理</span>
              </template>
              <el-menu-item index="/home/admin1/data/data-collection">数据采集</el-menu-item>
              <el-menu-item index="/home/admin1/data/import-export">数据导入导出</el-menu-item>
            </el-submenu>
        
            
            <!-- 训练管理菜单 -->
            <el-submenu index="/home/admin1/training">
              <template slot="title">
                <i class="el-icon-s-flag menu-icon"></i>
                <span>训练管理</span>
              </template>
              <el-menu-item index="/home/federated-learning">
                <i class="el-icon-s-marketing menu-icon"></i>
                <span>模型训练</span>
              </el-menu-item>
              <el-menu-item index="/home/admin1/task-audit">任务审核</el-menu-item>
              <el-menu-item index="/home/admin2/task-manage">训练任务管理</el-menu-item>
              <el-menu-item index="/home/admin2/send-notification">发送通知</el-menu-item>
            </el-submenu>
        
            
            <!-- 审核管理菜单 -->
            <el-submenu index="/home/admin1/audit">
              <template slot="title">
                <i class="el-icon-check menu-icon"></i>
                <span>审核管理</span>
              </template>
              <el-menu-item index="/home/admin1/audit-manage">待审核</el-menu-item>
              <el-menu-item index="/home/admin1/audit-history">已处理</el-menu-item>
            </el-submenu>
        
            
            <!-- 通知管理菜单 -->
            <el-submenu index="/home/admin1/notification">
              <template slot="title">
                <i class="el-icon-bell menu-icon"></i>
                <span>通知管理</span>
              </template>
              <el-menu-item index="/home/user/notification-list">通知列表</el-menu-item>
            </el-submenu>
        
          </template>
          
          <!-- 二级管理员专属菜单 -->
          <template v-if="effectiveUser && (effectiveUser.role === 'admin2' || effectiveUser.role === 2)">
            <!-- 模型训练菜单 -->
            <el-submenu index="/home/admin2">
              <template slot="title">
                <i class="el-icon-connection menu-icon"></i>
                <span>模型训练</span>
              </template>
              <el-menu-item index="/home/admin2/model-training">模型训练</el-menu-item>
              <el-menu-item index="/home/admin2/task-participation">任务参与</el-menu-item>
              <el-menu-item index="/home/admin2/task-manage">任务状态管理</el-menu-item>
              <el-menu-item index="/home/admin2/data-statistics">数据统计分析</el-menu-item>
            </el-submenu>
        
            
            <!-- 数据管理菜单 -->
            <el-submenu index="/home/admin2/data">
              <template slot="title">
                <i class="el-icon-data-analysis menu-icon"></i>
                <span>数据管理</span>
              </template>
              <el-menu-item index="/home/admin2/dataset-manage">数据集管理</el-menu-item>
              <el-menu-item index="/home/admin2/data-circulation">数据流通</el-menu-item>
            </el-submenu>
        
            
            <!-- 通知管理菜单 -->
            <el-submenu index="/home/admin2/notification">
              <template slot="title">
                <i class="el-icon-bell menu-icon"></i>
                <span>通知管理</span>
              </template>
              <el-menu-item index="/home/user/notification-list">通知列表</el-menu-item>
            </el-submenu>
        
          </template>
          
          <!-- 普通用户菜单 - 所有用户角色都可访问 -->
          <template v-if="effectiveUser">
          <!-- 管理员也可以看到以下内容 -->
            <el-menu-item index="/home/model/list" class="menu-item">
              <i class="el-icon-cpu menu-icon"></i>
              <span slot="title">模型列表</span>
            </el-menu-item>
            <el-submenu index="/home/user">
              <template slot="title">
                <i class="el-icon-crop menu-icon"></i>
                <span>智能识别</span>
              </template>
              <el-menu-item index="/home/user/image-identify">图像识别</el-menu-item>
              <el-menu-item index="/home/user/history-record">识别记录</el-menu-item>
            </el-submenu>
            <el-submenu index="/home/user/center">
              <template slot="title">
                <i class="el-icon-user menu-icon"></i>
                <span>个人中心</span>
              </template>
              <el-menu-item index="/home/user/profile">个人信息</el-menu-item>
              <el-menu-item index="/home/user/change-password">修改密码</el-menu-item>
              <el-menu-item index="/home/user/join-farm">加入农场</el-menu-item>
              <el-menu-item index="/home/user/notification-list">通知列表</el-menu-item>
            </el-submenu>
            <el-menu-item index="/home/help" class="menu-item">
              <i class="el-icon-question menu-icon"></i>
              <span slot="title">帮助中心</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区域 -->
      <el-main class="home-main" :style="mainStyle">
        <div class="page-header">
          <el-breadcrumb separator-class="el-icon-arrow-right" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/home/data-panel' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageName }}</el-breadcrumb-item>
          </el-breadcrumb>
          
          <div class="page-actions">
            <el-button type="primary" size="small" @click="refreshContent">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
        </div>
        
        <div class="content-wrapper">
          <transition name="fade" mode="out-in">
            <router-view :key="$route.fullPath" />
          </transition>
        </div>
      
    <!-- 全局底部版权 -->
    <div class="global-footer">
      <div class="footer-content">
        <p>© 安徽农业大学 | 地址：安徽省合肥市长江西路130号 | 邮编：230036<br>技术支持：安徽省北斗精准农业信息工程研究中心</p>
        <p>📧 邮箱：admpchina@yeah.net </p>
      </div>
    </div>
</el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex'

export default {
  name: 'Home',
  data() {
    return {
      isCollapse: false,
      sidebarWidth: '200px',
      activeMenu: '',
      currentPath: '数据面板',
      currentPageName: '数据面板',
      currentTime: '',
      notificationCount: 1,
      userAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    }
  },
  computed: {
    ...mapState(['user']),
    ...mapGetters(['isAuthenticated', 'isAdmin1', 'isAdmin2', 'isUser']),
    // 确保有默认用户角色，避免权限控制失效
    effectiveUser() {
      return this.user || { role: 2 } // 默认为二级管理员
    },
    mainStyle() {
      const marginLeft = this.isCollapse ? '64px' : '240px'
      return {
        marginLeft: marginLeft,
        transition: 'margin-left 0.3s ease'
      }
    }
  },
  created() {
      this.updatePageName(this.$route.path);
      this.$watch('$route', (to) => {
        this.updatePageName(to.path);
      });
    // 初始化用户信息，确保有权限数据
    if (!this.user) {
      // 如果没有用户信息，尝试从localStorage中获取
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          this.$store.commit('SET_USER', user)
        } catch (e) {
          console.error('解析用户信息失败:', e)
          // 解析失败时清除无效数据并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          this.$router.push('/login')
        }
      } else {
        // 如果localStorage中也没有用户信息，跳转到登录页
        this.$router.push('/login')
      }
    }
    
    // 设置当前激活的菜单
    this.setActiveMenu()
    
    // 监听路由变化，更新当前激活的菜单
    this.$router.afterEach(() => {
      this.setActiveMenu()
      this.updateCurrentPath()
    })
  },
  mounted() {
    // 初始化时间
    this.updateCurrentTime()
    // 设置定时器更新时间
    this.timer = setInterval(() => {
      this.updateCurrentTime()
    }, 1000)
    
    // 监听滚动事件，实现导航栏阴影效果
    window.addEventListener('scroll', this.handleScroll)
  },
  beforeDestroy() {
    // 清除定时器和事件监听
    if (this.timer) {
      clearInterval(this.timer)
    }
    window.removeEventListener('scroll', this.handleScroll)
  },
  methods: {
    updatePageName(path) {
      const pageNames = {
        "/home/data-panel": "数据面板",
        "/home/federated-learning": "模型训练",
        "/home/browser-training": "浏览器训练",
        "/home/audit-log": "审计日志",
        "/home/settings": "系统设置",
        "/home/admin1/user-manage": "用户管理",
        "/home/admin1/model-manage": "模型管理",
        "/home/admin1/send-notification": "发送通知",
        "/home/admin1/task-audit": "任务审核",
        "/home/admin2/task-manage": "任务管理",
        "/home/admin2/model-training": "模型训练",
        "/home/admin2/task-participation": "任务参与",
        "/home/user/image-identify": "图像识别"
      };
      this.currentPageName = pageNames[path] || "数据面板";
    },
    ...mapActions(['logout']),
    
    updateCurrentTime() {
      const now = new Date()
      this.currentTime = now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    
    toggleSidebar() {
      this.isCollapse = !this.isCollapse
      this.sidebarWidth = this.isCollapse ? '64px' : '240px'
    },
    
    // 设置当前激活的菜单
    setActiveMenu() {
      const path = this.$route.path
      // 根据当前路径设置激活的菜单
      this.activeMenu = path
    },
    
    updateCurrentPath() {
      const path = this.$route.path
      // 根据路径更新当前页面标题
      if (path.includes('data-panel')) {
        this.currentPath = '数据面板'
      } else if (path.includes('user-manage')) {
        this.currentPath = '用户管理'
      } else if (path.includes('model-manage')) {
        this.currentPath = '模型管理'
      } else if (path.includes('task-audit')) {
        this.currentPath = '任务审核'
      } else if (path.includes('model-training')) {
        this.currentPath = '模型训练'
      } else if (path.includes('task-participation')) {
        this.currentPath = '任务参与'
      } else if (path.includes('task-manage')) {
        this.currentPath = '训练任务管理'
      } else if (path.includes('data-statistics')) {
        this.currentPath = '数据统计分析'
      } else if (path.includes('dataset-manage')) {
        this.currentPath = '数据集管理'
      } else if (path.includes('data-circulation')) {
        this.currentPath = '数据流通'
      } else if (path.includes('image-identify')) {
        this.currentPath = '图像识别'
      } else if (path.includes('history-record')) {
        this.currentPath = '识别记录'
      } else if (path.includes('profile')) {
        this.currentPath = '个人信息'
      } else if (path.includes('change-password')) {
        this.currentPath = '修改密码'
      } else if (path.includes('help')) {
        this.currentPath = '帮助中心'
      } else {
        this.currentPath = '数据面板'
      }
    },
    
    // 获取角色中文名称
    getRoleName(role) {
      const roleMap = {
        1: '一级管理员',
        2: '二级管理员',
        3: '普通用户',
        'admin1': '管理员1',
        'admin2': '管理员2',
        'user': '普通用户'
      }
      return roleMap[role] || '未知角色'
    },
      
      // 处理菜单选择
      handleMenuSelect(index) {
        // 确保路径正确，并使用Vue Router进行跳转
        console.log('选择的菜单路径:', index);
        
        // 清除activeMenu的默认设置，让选中的菜单项正常高亮
        this.activeMenu = index;
        
        // 使用Vue Router进行跳转，这是正确的方式
        try {
          this.$router.push(index);
        } catch (error) {
          console.error('路由跳转错误:', error);
        }
    },
    
    // 刷新内容
    refreshContent() {
      // 触发路由重新加载
      this.$router.go(0)
    },
    
    // 跳转到个人信息页面
    toProfile() {
      // 参考handleMenuSelect方法的实现方式
      const path = '/home/user/profile';
      console.log('选择的菜单路径:', path);
      
      // 设置activeMenu以确保菜单项高亮
      this.activeMenu = path;
      
      // 使用Vue Router进行跳转
      try {
        this.$router.push(path);
      } catch (error) {
        console.error('跳转到个人信息页面失败:', error)
        this.$message({
          message: '个人信息页面未找到，请稍后再试',
          type: 'warning'
        })
      }
    },
    
    // 跳转到修改密码页面
    toChangePassword() {
      // 参考handleMenuSelect方法的实现方式
      const path = '/home/user/change-password';
      console.log('选择的菜单路径:', path);
      
      // 设置activeMenu以确保菜单项高亮
      this.activeMenu = path;
      
      // 使用Vue Router进行跳转
      try {
        this.$router.push(path);
      } catch (error) {
        console.error('跳转到修改密码页面失败:', error)
        this.$message({
          message: '修改密码页面未找到，请稍后再试',
          type: 'warning'
        })
      }
    },
    
    // 处理退出登录
    handleLogout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        try {
          // 清除localStorage
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          // 使用Vue Router跳转到登录页，使用正确的路径格式
          this.$router.push({ path: '/login' })
        } catch (error) {
          console.error('退出登录失败:', error)
          // 如果路由跳转失败，使用window.location.reload强制刷新到登录页
          window.location.reload()
        }
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消退出'
        })
      })
    },
    
    // 处理滚动事件
    handleScroll() {
      const header = document.querySelector('.home-header')
      if (window.scrollY > 10) {
        header.classList.add('header-shadow')
      } else {
        header.classList.remove('header-shadow')
      }
    }
  }
}
</script>

<style scoped>
/* 全局容器 */
.home-container {
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 顶部导航栏 */
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background-color: #ffffff;
  border-bottom: 1px solid #e6e6e6;
  position: relative;
  z-index: 1000;
  transition: all 0.3s ease;
}

.header-shadow {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  min-width: 240px;
}

.menu-toggle {
  margin-right: 20px;
  font-size: 20px;
  color: #606266;
  transition: color 0.3s ease;
}

.menu-toggle:hover {
  color: #409EFF;
}

/* Logo样式 */
.logo {
  display: flex;
  align-items: center;
}

.platform-logo {
    width: 40px;
    height: 40px;
    object-fit: contain;
    margin-right: 12px;
    border-radius: 8px;
  }

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

/* 头部中间区域 */
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-time {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 头部右侧区域 */
.header-right {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-action-btn {
  position: relative;
  font-size: 18px;
  color: #606266;
  transition: color 0.3s ease;
}

.header-action-btn:hover {
  color: #409EFF;
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -8px;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 9px;
  background-color: #f56c6c;
  color: white;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  user-select: none;
}

.user-info:hover {
  background-color: #f5f7fa;
  transform: translateY(-1px);
}

.user-avatar {
  margin-right: 12px;
  border: 2px solid #e6e6e6;
  transition: border-color 0.3s ease;
}

.user-info:hover .user-avatar {
  border-color: #409EFF;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin-right: 8px;
}

.user-info i {
  font-size: 12px;
  color: #909399;
  transition: transform 0.3s ease;
}

.user-info:hover i {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.user-menu,
.notification-menu {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border: none;
}

.user-menu .el-dropdown-item,
.notification-menu .el-dropdown-item {
  padding: 12px 20px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.user-menu .el-dropdown-item:hover,
.notification-menu .el-dropdown-item:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.logout-text {
  color: #f56c6c;
}

/* 通知菜单样式 */
.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notification-empty {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin: 10px 0;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
}

.notification-icon.success {
  background-color: #f0f9eb;
  color: #67c23a;
}

.notification-icon.warning {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

/* 左侧边栏 */
.home-aside {
  background-color: #001529;
  overflow-y: auto;
  transition: all 0.3s ease;
  border-right: 1px solid #1f2d3d;
  height: calc(100vh - 64px);
}

.home-aside.collapse {
  width: 64px !important;
}

.home-aside::-webkit-scrollbar {
  width: 6px;
}

.home-aside::-webkit-scrollbar-track {
  background: #1f2d3d;
}

.home-aside::-webkit-scrollbar-thumb {
  background: #495060;
  border-radius: 3px;
}

.home-aside::-webkit-scrollbar-thumb:hover {
  background: #606266;
}

/* 侧边栏头部 */
.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #1f2d3d;
  transition: all 0.3s ease;
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-username {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 4px 0;
}

.sidebar-role {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

/* 侧边栏菜单 */
.sidebar-menu {
  border-right: none;
  background-color: transparent;
}

.menu-item {
  transition: all 0.3s ease;
}

.menu-icon {
  font-size: 18px;
  margin-right: 10px;
}

.el-menu-item {
  height: 50px;
  line-height: 50px;
  padding: 0 20px;
  color: rgba(255, 255, 255, 0.65);
  transition: all 0.3s ease;
}

.el-menu-item:hover {
  background-color: #1890ff;
  color: #ffffff;
}

.el-menu-item.is-active {
  background-color: #1890ff;
  color: #ffffff;
  font-weight: 600;
}

.el-submenu__title {
  height: 50px;
  line-height: 50px;
  padding: 0 20px;
  color: rgba(255, 255, 255, 0.65);
  transition: all 0.3s ease;
}

.el-submenu__title:hover {
  background-color: #1f2d3d;
  color: #ffffff;
}

.el-submenu__title i {
  color: rgba(255, 255, 255, 0.65);
}

.el-submenu__title:hover i {
  color: #ffffff;
}

.el-submenu .el-menu {
  background-color: #000c17;
}

.el-submenu .el-menu-item {
  height: 44px;
  line-height: 44px;
  padding-left: 50px;
  color: rgba(255, 255, 255, 0.65);
}

.el-submenu .el-menu-item:hover {
  background-color: #1890ff;
  color: #ffffff;
}

/* 主内容区 */
a.global-footer {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  text-align: center;
  margin-top: auto;
}
.global-footer p {
  margin: 5px 0;
  font-size: 13px;
}

.home-main {
  background-color: #f0f2f5;
  overflow-y: auto;
  transition: all 0.3s ease;
  height: calc(100vh - 64px);
  margin: 0;
  padding: 0;
}

.home-main::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.home-main::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.home-main::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.home-main::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background-color: #ffffff;
  border-bottom: 1px solid #e6e6e6;
  margin-bottom: 12px;
}

.breadcrumb {
  margin: 0;
}

.breadcrumb .el-breadcrumb__item {
  font-size: 14px;
}

.breadcrumb .el-breadcrumb__item__inner {
  color: #606266;
  font-weight: 500;
}

.breadcrumb .el-breadcrumb__item__inner.is-link:hover {
  color: #409EFF;
}

.breadcrumb .el-breadcrumb__item:last-child .el-breadcrumb__item__inner {
  color: #409EFF;
  font-weight: 600;
}

/* 页面操作按钮 */
.page-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-actions .el-button {
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.page-actions .el-button--primary {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  border: none;
}

.page-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #66b1ff, #409EFF);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 内容包装器 */
.content-wrapper {
  padding: 0 8px 8px;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .home-header {
    padding: 0 16px;
  }
  
  .header-center {
    display: none;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .page-actions {
    align-self: flex-end;
  }
}

@media (max-width: 768px) {
  .logo-text {
    display: none;
  }
  
  .header-left {
    min-width: auto;
  }
  
  .home-aside {
    position: fixed;
    left: 0;
    top: 64px;
    bottom: 0;
    z-index: 999;
  }
  
  .home-aside.collapse {
    transform: translateX(-100%);
  }
  
  a.global-footer {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  text-align: center;
  margin-top: auto;
}
.global-footer p {
  margin: 5px 0;
  font-size: 13px;
}

.home-main {
    margin-left: 0 !important;
  }
  
  .page-header {
    padding: 16px;
  }
  
  .content-wrapper {
    padding: 0 16px 16px;
  }
}

.global-footer {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  text-align: center;
  margin-top: auto;
}
.global-footer p {
  margin: 5px 0;
  font-size: 13px;
}
</style>