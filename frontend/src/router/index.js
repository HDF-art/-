import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('../views/ResetPassword.vue'),
    meta: { title: '找回密码' }
  },
  {
    path: '/',
    redirect: '/home/data-panel'
  },
  {
    path: '/home',
    component: Home,
    redirect: '/home/data-panel',
    children: [
      {
        path: 'data-panel',
        name: 'DataPanel',
        component: () => import('../views/DataPanel.vue'),
        meta: { title: '数据面板' }
      },
      {
        path: 'federated-learning',
        name: 'FederatedLearning',
        component: () => import('../views/FederatedLearningCenter.vue'),
        meta: { title: '模型训练' }
      },

      {
        path: 'audit-log',
        name: 'AuditLog',
        component: () => import('../views/AuditLog.vue'),
        meta: { title: '审计日志' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'admin1/user-manage',
        name: 'UserManage',
        component: () => import('../views/Admin1/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'admin1/model-manage',
        name: 'ModelManage',
        component: () => import('../views/Admin1/ModelManage.vue'),
        meta: { title: '模型管理' }
      },
      {
        path: 'admin1/send-notification',
        name: 'SendNotification',
        component: () => import('../views/Admin1/SendNotification.vue'),
        meta: { title: '发送通知' }
      },
      {
        path: 'admin1/task-audit',
        name: 'TaskAudit',
        component: () => import('../views/Admin1/TaskAudit.vue'),
        meta: { title: '任务审核' }
      },
      {
        path: 'admin1/audit-manage',
        name: 'AdminAudit',
        component: () => import('../views/Admin1/AdminAudit.vue'),
        meta: { title: '待审核' }
      },
      {
        path: 'admin1/audit-history',
        name: 'AuditHistory',
        component: () => import('../views/Admin1/AuditHistory.vue'),
        meta: { title: '已处理' }
      },
      {
        path: 'admin2/task-manage',
        name: 'TaskManage',
        component: () => import('../views/Admin2/TaskManage.vue'),
        meta: { title: '任务管理' }
      },
      {
        path: 'admin2/send-notification',
        name: 'Admin2SendNotification',
        component: () => import('../views/Admin2/SendNotification.vue'),
        meta: { title: '发送通知' }
      },
      {
        path: 'admin2/model-training',
        name: 'ModelTraining',
        component: () => import('../views/Admin2/ModelTraining.vue'),
        meta: { title: '模型训练' }
      },
      {
        path: 'admin2/task-participation',
        name: 'TaskParticipation',
        component: () => import('../views/Admin2/TaskParticipation.vue'),
        meta: { title: '任务参与' }
      },
      {
        path: 'user/image-identify',
        name: 'ImageIdentify',
        component: () => import('../views/User/ImageIdentify.vue'),
        meta: { title: '图像识别' }
      },
      {
        path: 'admin1/data/data-collection',
        name: 'DataCollection',
        component: () => import('../views/Admin1/DataCollection.vue'),
        meta: { title: '数据采集' }
      },
      {
        path: 'admin1/data/import-export',
        name: 'ImportExport',
        component: () => import('../views/Admin1/ImportExport.vue'),
        meta: { title: '数据导入导出' }
      },
      {
        path: 'admin2/dataset-manage',
        name: 'DatasetManage',
        component: () => import('../views/Admin2/DatasetManage.vue'),
        meta: { title: '数据集管理' }
      },
      {
        path: 'admin2/data-circulation',
        name: 'DataCirculation',
        component: () => import('../views/Admin2/DataCirculation.vue'),
        meta: { title: '数据流通' }
      },
      {
        path: 'model/list',
        name: 'ModelList',
        component: () => import('../views/ModelList.vue'),
        meta: { title: '模型列表' }
      },
      {
        path: 'user/history-record',
        name: 'HistoryRecord',
        component: () => import('../views/User/HistoryRecord.vue'),
        meta: { title: '识别记录' }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/User/Profile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'user/change-password',
        name: 'ChangePassword',
        component: () => import('../views/User/ChangePassword.vue'),
        meta: { title: '修改密码' }
      },
      {
        path: 'user/join-farm',
        name: 'JoinFarm',
        component: () => import('../views/User/JoinFarm.vue'),
        meta: { title: '加入农场' }
      },
      {
        path: 'user/notification-list',
        name: 'NotificationList',
        component: () => import('../views/User/NotificationList.vue'),
        meta: { title: '通知列表' }
      },
      {
        path: 'help',
        name: 'Help',
        component: () => import('../views/Help.vue'),
        meta: { title: '帮助中心' }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: '/',
  routes
})

router.beforeEach((to, from, next) => {
  next()
})

export default router
