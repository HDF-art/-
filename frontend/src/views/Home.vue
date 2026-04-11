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
          background-color="transparent"
          text-color="#475569"
          active-text-color="#00539B"
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
            <el-submenu index="/home/admin1">
              <template slot="title">
                <i class="el-icon-setting menu-icon"></i>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/home/admin1/user-manage">用户管理</el-menu-item>
              <el-menu-item index="/home/admin1/model-manage">模型管理</el-menu-item>
              <el-menu-item index="/home/admin1/send-notification">发送通知</el-menu-item>
            </el-submenu>
            
            <el-submenu index="/home/admin1/audit">
              <template slot="title">
                <i class="el-icon-finished menu-icon"></i>
                <span>审批管理</span>
              </template>
              <el-menu-item index="/home/admin1/audit-manage">待审核</el-menu-item>
              <el-menu-item index="/home/admin1/audit-history">已处理</el-menu-item>
            </el-submenu>
            
            <el-submenu index="/home/admin1/data">
              <template slot="title">
                <i class="el-icon-data-analysis menu-icon"></i>
                <span>数据管理</span>
              </template>
              <el-menu-item index="/home/admin1/data">本地数据管理</el-menu-item>
              <el-menu-item index="/home/admin1/data/data-collection">数据采集</el-menu-item>
              <el-menu-item index="/home/admin1/data/import-export">数据导入导出</el-menu-item>
            </el-submenu>
            
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
            </el-submenu>
          </template>
          
          <!-- 二级管理员专属菜单 -->
          <template v-if="effectiveUser && (effectiveUser.role === 'admin2' || effectiveUser.role === 2)">
            <el-submenu index="/home/admin2">
              <template slot="title">
                <i class="el-icon-connection menu-icon"></i>
                <span>模型训练</span>
              </template>
              <el-menu-item index="/home/admin2/model-training">模型训练</el-menu-item>
              <el-menu-item index="/home/admin2/task-participation">任务参与</el-menu-item>
              <el-menu-item index="/home/admin2/task-manage">任务状态管理</el-menu-item>
            </el-submenu>
            <el-submenu index="/home/admin2/data">
              <template slot="title">
                <i class="el-icon-folder-opened menu-icon"></i>
                <span>数据与通知</span>
              </template>
              <el-menu-item index="/home/admin2/dataset-manage">数据集管理</el-menu-item>
              <el-menu-item index="/home/admin2/data-circulation">数据流通</el-menu-item>
              <el-menu-item index="/home/admin2/send-notification">发送通知</el-menu-item>
            </el-submenu>
          </template>
          
          <!-- 普通用户菜单 -->
          <template v-if="effectiveUser">
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
            <el-menu-item index="/home/user/join-farm" class="menu-item" v-if="effectiveUser && (effectiveUser.role === 'user' || effectiveUser.role === 3)">
              <i class="el-icon-office-building menu-icon"></i>
              <span slot="title">加入农场</span>
            </el-menu-item>
            <el-menu-item index="/home/help" class="menu-item">
              <i class="el-icon-question menu-icon"></i>
              <span slot="title">帮助中心</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区域 -->
      <el-main class="home-main">
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
          <p>© 安徽农业大学 | 地址：安徽省合肥市长江西路130号 | 邮编：230036</p>
          <p>技术支持：安徽省北斗精准农业信息工程研究中心 | 📧 邮箱：admpchina@yeah.net</p>
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
      sidebarWidth: '240px',
      activeMenu: '',
      currentPath: '数据面板',
      currentPageName: '数据面板',
      currentTime: '',
      notificationCount: 1
    }
  },
  computed: {
    ...mapState(['user']),
    ...mapGetters(['isAuthenticated', 'isAdmin1', 'isAdmin2', 'isUser']),
    effectiveUser() {
      return this.user || { role: 2 }
    },
    userAvatar() {
      if (this.effectiveUser && this.effectiveUser.avatar) {
        return this.effectiveUser.avatar
      }
      const seed = this.effectiveUser?.username || this.effectiveUser?.id || 'default'
      return `https://api.dicebear.com/9.x/jdenticon/svg?seed=${encodeURIComponent(seed)}`
    }
  },
  created() {
    this.updatePageName(this.$route.path);
    this.$watch('$route', (to) => {
      this.updatePageName(to.path);
    });
    
    if (!this.user) {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          this.$store.commit('SET_USER', user)
        } catch (e) {
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          this.$router.push('/login')
        }
      } else {
        this.$router.push('/login')
      }
    }
    
    this.setActiveMenu()
    this.$router.afterEach(() => {
      this.setActiveMenu()
      this.updateCurrentPath()
    })
  },
  mounted() {
    this.updateCurrentTime()
    this.timer = setInterval(() => {
      this.updateCurrentTime()
    }, 1000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    ...mapActions(['logout']),
    updatePageName(path) {
      const pageNames = {
        "/home/data-panel": "数据面板",
        "/home/federated-learning": "模型训练",
        "/home/audit-log": "审计日志",
        "/home/settings": "系统设置",
        "/home/admin1/user-manage": "用户管理",
        "/home/admin1/model-manage": "模型管理",
        "/home/admin2/model-training": "模型训练",
        "/home/user/image-identify": "图像识别"
      };
      this.currentPageName = pageNames[path] || "数据面板";
    },
    updateCurrentTime() {
      const now = new Date()
      this.currentTime = now.toLocaleString('zh-CN')
    },
    toggleSidebar() {
      this.isCollapse = !this.isCollapse
      this.sidebarWidth = this.isCollapse ? '80px' : '240px'
    },
    setActiveMenu() {
      this.activeMenu = this.$route.path
    },
    updateCurrentPath() {
      // Logic remained similar or simplified
    },
    getRoleName(role) {
      const roleMap = { 1: '一级管理员', 2: '二级管理员', 3: '普通用户' }
      return roleMap[role] || '用户'
    },
    handleMenuSelect(index) {
      try {
        this.$router.push(index);
      } catch (error) {
        console.warn('Navigation error:', error);
      }
    },
    refreshContent() {
      this.$router.go(0)
    },
    toProfile() {
      this.$router.push('/home/user/profile');
    },
    toChangePassword() {
      this.$router.push('/home/user/change-password');
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗?', '提示', { type: 'warning' }).then(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        this.$router.push('/login')
      })
    }
  }
}
</script>

<style scoped>
/* 
  Home Layout - Editorial Style 
  Floating Navigation & Scoped Transitions 
*/

.home-container {
  height: 100vh;
  background-color: #F8FAFC;
  overflow: hidden;
}

/* 顶部导航栏 - 毛玻璃特效 */
.home-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 2000;
  height: 72px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  margin-left: 20px;
}

.platform-logo {
  height: 40px;
  width: auto;
  border-radius: 8px;
  margin-right: 12px;
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  color: #0F172A;
  letter-spacing: 0.05em;
  font-family: "Outfit", sans-serif;
  background: linear-gradient(135deg, #00539B 0%, #059669 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 悬浮侧边栏 */
.home-aside {
  margin: 92px 0 20px 24px;
  height: calc(100vh - 112px);
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 20px 40px -10px rgba(0,0,0,0.05);
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1) !important;
  overflow: hidden;
}

.home-aside.collapse {
  width: 80px !important;
}

/* 侧边菜单深度自定义 */
.sidebar-menu {
  border-right: none !important;
  background: transparent !important;
  padding: 10px;
  height: 100%;
}

.sidebar-header {
  padding: 24px 16px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-username {
  font-weight: 700;
  color: #0F172A;
  margin-bottom: 2px;
}

.sidebar-role {
  font-size: 11px;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* 菜单项基础态 */
.sidebar-menu .el-menu-item, 
.sidebar-menu /deep/ .el-submenu__title {
  height: 50px !important;
  line-height: 50px !important;
  margin: 4px 0;
  border-radius: 12px;
  color: #475569 !important;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.sidebar-menu /deep/ .el-submenu__title i {
  color: #475569;
}

/* 菜单项 Hover & Active */
.sidebar-menu .el-menu-item:hover,
.sidebar-menu /deep/ .el-submenu__title:hover {
  background-color: rgba(0, 0, 0, 0.03) !important;
  transform: translateX(4px);
  color: #00539B !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: rgba(0, 83, 155, 0.1) !important; /* 胶囊状背景 */
  color: #00539B !important;
  font-weight: 700;
}

/* 主内容区域 */
.home-main {
  padding: 112px 40px 40px 40px !important;
  background: transparent;
  min-height: 100vh;
  overflow-y: auto;
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

/* 内容区域的呼吸感排版 */
.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面页眉美化 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.breadcrumb /deep/ .el-breadcrumb__inner {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.breadcrumb /deep/ .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #00539B;
  font-weight: 700;
}

/* 用户状态 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.user-info:hover {
  background: rgba(0,0,0,0.03);
}

.user-name {
  font-weight: 600;
}

/* 路由切换动画 - 向上淡入 */
.fade-enter-active {
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.fade-leave-active {
  transition: all 0.3s ease;
}
.fade-enter {
  opacity: 0;
  transform: translateY(24px);
}
.fade-leave-to {
  opacity: 0;
}

/* 全球页脚 */
.global-footer {
  margin-top: 60px;
  padding: 40px 0;
  border-top: 1px solid rgba(0,0,0,0.05);
  text-align: center;
  color: #475569;
  font-size: 13px;
  line-height: 1.8;
}

.global-footer p {
  margin: 4px 0;
}
</style>