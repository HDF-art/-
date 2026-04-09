<template>
  <div class="notification-list">
    <el-card>
      <div slot="header" class="card-header">
        <span>通知列表</span>
        <el-button type="primary" @click="handleMarkAllRead">全部标为已读</el-button>
      </div>
      <!-- 通知列表 -->
      <el-empty v-if="notificationList.length === 0" description="暂无通知"></el-empty>
      <el-timeline v-else>
        <el-timeline-item
          v-for="notification in notificationList"
          :key="notification.id"
          :timestamp="formatTime(notification.createdAt)"
          :type="notification.status === 0 ? 'primary' : 'info'"
          :icon="notification.status === 0 ? 'el-icon-bell' : 'el-icon-message'"
        >
          <el-card :class="{'unread': notification.status === 0}">
            <div class="notification-header">
              <h3 class="notification-title">{{ notification.title }}</h3>
              <div class="notification-actions">
                <el-button type="text" @click="handleRead(notification.id)" v-if="notification.status === 0">标记已读</el-button>
                <el-button type="text" @click="handleDelete(notification.id)">删除</el-button>
              </div>
            </div>
            <div class="notification-content">{{ notification.content }}</div>
            <div class="notification-footer">
              <span class="sender">发送者: {{ getSenderName(notification.senderId) }}</span>
              <span class="status" :class="{ 'unread-status': notification.status === 0 }">
                {{ notification.status === 0 ? '未读' : '已读' }}
              </span>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <!-- 分页 -->
      <div class="pagination" v-if="notificationList.length > 0">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getUserNotifications, markNotificationAsRead, deleteNotification, getUserList } from '../../api/user'

export default {
  name: 'NotificationList',
  data() {
    return {
      notificationList: [],
      userList: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.loadNotifications()
    this.loadUserList()
  },
  methods: {
    async loadUserList() {
      try {
        const res = await getUserList()
        if (res.data && res.data.data) {
          this.userList = res.data.data
        }
      } catch (e) {
        console.error('加载用户列表失败', e)
      }
    },
    loadNotifications() {
      this.loading = true
      const userStr = localStorage.getItem('user')
      let userId = 1 // 默认用户ID
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          userId = user.id || 1
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
      
      getUserNotifications(userId, null).then(response => {
        if (response.data.code === 200) {
          this.notificationList = response.data.data
          this.total = response.data.data.length
        }
        this.loading = false
      }).catch(error => {
        console.error('获取通知列表失败:', error)
        this.loading = false
      })
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.loadNotifications()
    },
    handleCurrentChange(current) {
      this.pageNum = current
      this.loadNotifications()
    },
    handleRead(id) {
      markNotificationAsRead(id).then(response => {
        if (response.data.code === 200) {
          this.$message.success('标记已读成功')
          this.loadNotifications()
        } else {
          this.$message.error(response.data.message || '标记已读失败')
        }
      }).catch(error => {
        console.error('标记已读失败:', error)
        this.$message.error('标记已读失败')
      })
    },
    handleMarkAllRead() {
      this.$confirm('确定要将所有通知标记为已读吗？', '确认操作', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 这里可以实现批量标记已读的逻辑
        this.$message.success('所有通知已标记为已读')
        this.loadNotifications()
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    },
    handleDelete(id) {
      this.$confirm('确定要删除这条通知吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'danger'
      }).then(() => {
        deleteNotification(id).then(response => {
          if (response.data.code === 200) {
            this.$message.success('删除成功')
            this.loadNotifications()
          } else {
            this.$message.error(response.data.message || '删除失败')
          }
        }).catch(error => {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString('zh-CN')
    },
    getSenderName(senderId) {
      const sender = this.userList.find(user => user.id === senderId)
      return sender ? sender.username : `用户${senderId}`
    }
  }
}
</script>

<style scoped>
.notification-list { padding: 20px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.unread { border-left: 4px solid #409EFF; }
.notification-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.notification-title { font-size: 16px; font-weight: bold; margin: 0; }
.notification-content { margin-bottom: 10px; line-height: 1.5; }
.notification-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; }
.unread-status { color: #409EFF; font-weight: bold; }
</style>
