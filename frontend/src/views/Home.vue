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
          <el-dropdown trigger="click" @command="handleNotificationCommand">
            <el-button size="small" class="notification-btn">
              <i class="el-icon-bell"></i>
              <span class="notification-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </el-button>
            <el-dropdown-menu slot="dropdown" class="notification-dropdown">
              <div class="notification-dropdown-header">
                <span class="dropdown-title">通知中心</span>
                <el-button type="text" size="mini" @click.stop="handleMarkAllRead">全部标为已读</el-button>
              </div>
              <div class="notification-list-wrapper">
                <div v-if="notifications.length === 0" class="notification-empty">
                  <i class="el-icon-bell" style="font-size: 40px; color: #dcdfe6; margin-bottom: 12px;"></i>
                  <p>暂无通知</p>
                </div>
                <div v-else class="notification-scroll-list">
                  <div
                    v-for="item in notifications.slice(0, 5)"
                    :key="item.id"
                    class="notification-item"
                    :class="{ 'unread': item.status === 0 }"
                    @click.stop="showNotificationDetail(item)"
                  >
                    <div class="notification-icon-wrapper" :class="getNotificationType(item)">
                      <i :class="getNotificationIcon(item)"></i>
                    </div>
                    <div class="notification-info">
                      <p class="notification-title">{{ item.title }}</p>
                      <p class="notification-desc">{{ item.content ? item.content.substring(0, 30) + (item.content.length > 30 ? '...' : '') : '' }}</p>
                      <p class="notification-time">{{ formatNotificationTime(item.createdAt) }}</p>
                    </div>
                    <div class="unread-dot" v-if="item.status === 0"></div>
                  </div>
                </div>
              </div>
              <div class="notification-dropdown-footer" v-if="notifications.length > 0">
                <el-button type="text" @click.stop="goToNotificationList">查看全部通知</el-button>
              </div>
            </el-dropdown-menu>
          </el-dropdown>

          <el-dialog
            title="通知详情"
            :visible.sync="notificationDialogVisible"
            width="520px"
            :close-on-click-modal="true"
            custom-class="notification-detail-dialog"
            append-to-body
          >
            <div v-if="currentNotification" class="notification-detail-content">
              <div class="detail-header">
                <div class="detail-icon-wrapper" :class="getNotificationType(currentNotification)">
                  <i :class="getNotificationIcon(currentNotification)" style="font-size: 28px;"></i>
                </div>
                <div class="detail-title-section">
                  <h3 class="detail-title">{{ currentNotification.title }}</h3>
                  <span class="detail-time">{{ formatNotificationTime(currentNotification.createdAt) }}</span>
                </div>
              </div>
              <el-divider></el-divider>
              <div class="detail-body">
                <p class="detail-content">{{ currentNotification.content }}</p>
              </div>
            </div>
            <span slot="footer" class="dialog-footer">
              <el-button @click="notificationDialogVisible = false">关闭</el-button>
              <el-button type="primary" @click="goToNotificationList">查看详情</el-button>
            </span>
          </el-dialog>
          
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
import { getUserNotifications, markNotificationAsRead } from '../api/user'

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
      notifications: [],
      unreadCount: 0,
      notificationDialogVisible: false,
      currentNotification: null
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
    this.loadNotifications()
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    ...mapActions(['logout']),
    async loadNotifications() {
      try {
        const userStr = localStorage.getItem('user')
        let userId = 1
        if (userStr) {
          try {
            const user = JSON.parse(userStr)
            userId = user.id || 1
          } catch (e) {
            console.error('解析用户信息失败:', e)
          }
        }

        const response = await getUserNotifications(userId, null)
        if (response.data && response.data.code === 200) {
          this.notifications = response.data.data || []
          this.unreadCount = this.notifications.filter(n => n.status === 0).length
        }
      } catch (error) {
        console.error('获取通知列表失败:', error)
      }
    },
    showNotificationDetail(notification) {
      this.currentNotification = notification
      this.notificationDialogVisible = true

      if (notification.status === 0) {
        markNotificationAsRead(notification.id).then(() => {
          notification.status = 1
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        }).catch(error => {
          console.error('标记已读失败:', error)
        })
      }
    },
    handleMarkAllRead() {
      this.$confirm('确定要将所有通知标记为已读吗？', '确认操作', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const unreadNotifications = this.notifications.filter(n => n.status === 0)
        const markPromises = unreadNotifications.map(n =>
          markNotificationAsRead(n.id).catch(err => console.error('标记已读失败:', err))
        )

        Promise.all(markPromises).then(() => {
          this.notifications.forEach(n => n.status = 1)
          this.unreadCount = 0
          this.$message.success('所有通知已标记为已读')
        })
      }).catch(() => {})
    },
    goToNotificationList() {
      this.notificationDialogVisible = false
      this.$router.push('/home/user/notification-list')
    },
    formatNotificationTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)

      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      return date.toLocaleDateString('zh-CN')
    },
    getNotificationType(notification) {
      const title = (notification.title || '').toLowerCase()
      if (title.includes('完成') || title.includes('成功')) return 'success'
      if (title.includes('警告') || title.includes('失败')) return 'warning'
      if (title.includes('错误')) return 'danger'
      return 'info'
    },
    getNotificationIcon(notification) {
      const type = this.getNotificationType(notification)
      const iconMap = {
        success: 'el-icon-circle-check',
        warning: 'el-icon-warning',
        danger: 'el-icon-circle-close',
        info: 'el-icon-info'
      }
      return iconMap[type] || 'el-icon-bell'
    },
    handleNotificationCommand() {},
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

<style>
.notification-btn {
  position: relative;
  padding: 8px 12px;
  border-radius: 10px;
  background: rgba(0, 83, 155, 0.08);
  color: #00539B;
  font-size: 18px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  border: none;
}

.notification-btn:hover {
  background: rgba(0, 83, 155, 0.15);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 83, 155, 0.2);
}

.notification-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  background: #ff4757;
  color: white;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9px;
  padding: 0 5px;
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.notification-dropdown {
  width: 380px !important;
  padding: 0 !important;
  border-radius: 16px !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15) !important;
  overflow: hidden;
}

.notification-dropdown .el-dropdown-menu__item {
  padding: 0 !important;
  line-height: normal !important;
  height: auto !important;
}

.notification-dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.dropdown-title {
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
}

.notification-list-wrapper {
  max-height: 400px;
  overflow-y: auto;
}

.notification-scroll-list {
  padding: 8px 0;
}

.notification-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
  font-size: 14px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  border-left: 3px solid transparent;
}

.notification-item:hover {
  background-color: rgba(0, 83, 155, 0.04);
  border-left-color: #00539B;
  transform: translateX(4px);
}

.notification-item.unread {
  background-color: rgba(0, 83, 155, 0.02);
}

.notification-icon-wrapper {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.notification-icon-wrapper.success {
  background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
  color: #28a745;
}

.notification-icon-wrapper.warning {
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  color: #ff9800;
}

.notification-icon-wrapper.danger {
  background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
  color: #dc3545;
}

.notification-icon-wrapper.info {
  background: linear-gradient(135deg, #d1ecf1 0%, #bee5eb 100%);
  color: #17a2b8;
}

.notification-info {
  flex: 1;
  min-width: 0;
}

.notification-info .notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #0F172A;
  margin: 0 0 4px 0;
  line-height: 1.4;
}

.notification-info .notification-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 4px 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-info .notification-time {
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
}

.unread-dot {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background: #00539B;
  border-radius: 50%;
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.notification-dropdown-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
  background: #fafbfc;
}

.notification-detail-dialog {
  border-radius: 20px !important;
  overflow: hidden;
}

.notification-detail-dialog .el-dialog__header {
  padding: 24px 28px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.notification-detail-dialog .el-dialog__title {
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
}

.notification-detail-dialog .el-dialog__body {
  padding: 28px;
}

.notification-detail-dialog .el-dialog__footer {
  padding: 16px 28px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafbfc;
}

.notification-detail-content {
  animation: fadeInUp 0.4s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.detail-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.detail-icon-wrapper.success {
  background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
  color: #28a745;
}

.detail-icon-wrapper.warning {
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  color: #ff9800;
}

.detail-icon-wrapper.danger {
  background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
  color: #dc3545;
}

.detail-icon-wrapper.info {
  background: linear-gradient(135deg, #d1ecf1 0%, #bee5eb 100%);
  color: #17a2b8;
}

.detail-title-section {
  flex: 1;
}

.detail-title {
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 6px 0;
  line-height: 1.4;
}

.detail-time {
  font-size: 13px;
  color: #94a3b8;
}

.detail-body {
  padding: 8px 0;
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #334155;
  margin: 0;
}
</style>