import request from '../utils/request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/users/info',
    method: 'get'
  })
}

// 用户登出
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 修改用户密码
export const updatePassword = (data) => {
  return request({
    url: '/users/password',
    method: 'put',
    data
  })
}

// 更新用户信息
export const updateUserInfo = (data) => {
  return request({
    url: '/users/profile',
    method: 'put',
    data
  })
}

// 上传头像
export const uploadAvatar = (formData) => {
  return request({
    url: '/users/upload-avatar',
    method: 'post',
    data: formData
  })
}

// 发送验证码 (邮箱)
export const sendCode = (data) => {
  return request({
    url: '/email/send-code',
    method: 'post',
    data
  })
}

// 手机验证码登录
export const emailLogin = (data) => {
  return request({
    url: '/email/email-login',
    method: 'post',
    data
  })
}

// 找回密码
export const resetPassword = (data) => {
  return request({
    url: '/users/reset-password',
    method: 'post',
    data
  })
}

// 获取用户列表（管理员1权限）
export const getUserList = (params) => {
  return request({
    url: '/users/list',
    method: 'get',
    params
  })
}

// 创建用户（管理员1权限）
export const createUser = (data) => {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 更新用户信息（管理员1权限）
export const updateUser = (userId, data) => {
  return request({
    url: `/users/${userId}`,
    method: 'put',
    data
  })
}

// 删除用户（管理员1权限）
export const deleteUser = (userId) => {
  return request({
    url: `/users/${userId}`,
    method: 'delete'
  })
}

// 创建审核请求
export const createAuditRequest = (data) => {
  return request({
    url: '/audit-requests',
    method: 'post',
    data
  })
}

// 处理审核请求
export const processAuditRequest = (id, data) => {
  return request({
    url: `/audit-requests/${id}/process`,
    method: 'put',
    data
  })
}

// 获取审核请求列表
export const getAuditRequests = (params) => {
  return request({
    url: '/audit-requests',
    method: 'get',
    params
  })
}

// 根据申请人ID获取审核请求
export const getAuditRequestsByApplicant = (applicantId) => {
  return request({
    url: `/audit-requests/applicant/${applicantId}`,
    method: 'get'
  })
}

// 根据状态获取审核请求
export const getAuditRequestsByStatus = (status) => {
  return request({
    url: `/audit-requests/status/${status}`,
    method: 'get'
  })
}

// 根据类型获取审核请求
export const getAuditRequestsByType = (type) => {
  return request({
    url: `/audit-requests/type/${type}`,
    method: 'get'
  })
}

// 发送通知
export const sendNotification = (data) => {
  return request({
    url: '/notifications',
    method: 'post',
    data
  })
}

// 发送通知给指定类型的用户
export const sendNotificationToUsers = (data) => {
  return request({
    url: '/notifications/send-to-users',
    method: 'post',
    data
  })
}

// 标记通知为已读
export const markNotificationAsRead = (id) => {
  return request({
    url: `/notifications/${id}/read`,
    method: 'put'
  })
}

// 获取用户的通知列表
export const getUserNotifications = (userId, status) => {
  return request({
    url: `/notifications/user/${userId}`,
    method: 'get',
    params: { status }
  })
}

// 获取用户的未读通知数量
export const getUnreadNotificationCount = (userId) => {
  return request({
    url: `/notifications/user/${userId}/unread-count`,
    method: 'get'
  })
}

// 获取农场用户的通知列表
export const getFarmNotifications = (farmId, status) => {
  return request({
    url: `/notifications/farm/${farmId}`,
    method: 'get',
    params: { status }
  })
}

// 删除通知
export const deleteNotification = (id) => {
  return request({
    url: `/notifications/${id}`,
    method: 'delete'
  })
}

// 上传数据集
export const uploadDataset = (formData) => {
  return request({
    url: '/datasets/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取数据集列表
export const getDatasetList = () => {
  return request({
    url: '/datasets',
    method: 'get'
  })
}

// 根据上传者ID获取数据集列表
export const getDatasetsByUploaderId = (uploaderId) => {
  return request({
    url: `/datasets/uploader/${uploaderId}`,
    method: 'get'
  })
}

// 根据农场ID获取数据集列表
export const getDatasetsByFarmId = (farmId) => {
  return request({
    url: `/datasets/farm/${farmId}`,
    method: 'get'
  })
}

// 删除数据集
export const deleteDataset = (id) => {
  return request({
    url: `/datasets/${id}`,
    method: 'delete'
  })
}

// 发布数据集交易信息
export const publishDataCirculation = (data) => {
  return request({
    url: '/data-circulation/publish',
    method: 'post',
    data
  })
}

// 获取所有待交易的数据集
export const getAvailableData = () => {
  return request({
    url: '/data-circulation/available',
    method: 'get'
  })
}

// 根据发布者ID获取数据集列表
export const getDataByPublisherId = (publisherId) => {
  return request({
    url: `/data-circulation/publisher/${publisherId}`,
    method: 'get'
  })
}

// 根据ID获取数据流通信息
export const getDataCirculationById = (id) => {
  return request({
    url: `/data-circulation/${id}`,
    method: 'get'
  })
}

// 更新数据流通状态
export const updateDataCirculationStatus = (id, status) => {
  return request({
    url: `/data-circulation/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 创建交易
export const createTransaction = (data) => {
  return request({
    url: '/data-transaction',
    method: 'post',
    data
  })
}

// 根据交易ID获取交易信息
export const getTransactionById = (id) => {
  return request({
    url: `/data-transaction/${id}`,
    method: 'get'
  })
}

// 根据买家ID获取交易列表
export const getTransactionsByBuyerId = (buyerId) => {
  return request({
    url: `/data-transaction/buyer/${buyerId}`,
    method: 'get'
  })
}

// 根据卖家ID获取交易列表
export const getTransactionsBySellerId = (sellerId) => {
  return request({
    url: `/data-transaction/seller/${sellerId}`,
    method: 'get'
  })
}

// 更新交易状态
export const updateTransactionStatus = (id, status) => {
  return request({
    url: `/data-transaction/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 获取所有交易记录（一级管理员使用）
export const getAllTransactions = () => {
  return request({
    url: '/data-transaction/all',
    method: 'get'
  })
}

// 发送加密消息
export const sendSecureMessage = (data) => {
  return request({
    url: '/secure-message',
    method: 'post',
    data
  })
}

// 根据交易ID获取消息列表
export const getMessagesByTransactionId = (transactionId) => {
  return request({
    url: `/secure-message/transaction/${transactionId}`,
    method: 'get'
  })
}

// 根据接收者ID获取未读消息列表
export const getUnreadMessages = (receiverId) => {
  return request({
    url: `/secure-message/unread/${receiverId}`,
    method: 'get'
  })
}

// 标记消息为已读
export const markMessageAsRead = (id) => {
  return request({
    url: `/secure-message/${id}/read`,
    method: 'put'
  })
}

// 获取训练日志列表
export const getTrainingLogs = (taskId) => {
  return request({
    url: `/training-logs/task/${taskId}`,
    method: 'get'
  })
}

// 获取指定类型的训练日志
export const getTrainingLogsByType = (taskId, logType) => {
  return request({
    url: `/training-logs/task/${taskId}/type/${logType}`,
    method: 'get'
  })
}

// 获取最新的训练日志
export const getLatestTrainingLogs = (taskId, limit = 50) => {
  return request({
    url: `/training-logs/task/${taskId}/latest`,
    method: 'get',
    params: { limit }
  })
}

// 清空训练日志
export const clearTrainingLogs = (taskId) => {
  return request({
    url: `/training-logs/task/${taskId}`,
    method: 'delete'
  })
}
// 发送注册验证码
export const sendRegisterCode = (email) => {
  return request({
    url: '/register/send-code',
    method: 'post',
    data: { email }
  })
}

// 注册用户
export const registerUser = (data) => {
  return request({
    url: '/register/user',
    method: 'post',
    data
  })
}

// 注册二级管理员
export const registerAdmin2 = (data) => {
  return request({
    url: '/register/admin2',
    method: 'post',
    data
  })
}

// 获取可加入的农场
export const getAvailableFarms = () => {
  return request({
    url: '/register/farms',
    method: 'get'
  })
}

// 申请加入农场
export const applyFarm = (data) => {
  return request({
    url: '/user/apply-farm',
    method: 'post',
    data
  })
}

// 获取待审核的二级管理员列表
export const getPendingAuditUsers = () => {
  return request({
    url: '/users/pending-audit',
    method: 'get'
  })
}

// 获取已处理的审核记录
export const getProcessedAuditUsers = () => {
  return request({
    url: '/users/audit/processed',
    method: 'get'
  })
}

// 审核二级管理员
export const auditAdmin2 = (userId, auditStatus, rejectReason = '') => {
  return request({
    url: `/users/audit/${userId}`,
    method: 'post',
    data: { auditStatus, rejectReason }
  })
}

// 获取操作日志
export const getAuditLogs = (params) => {
  return request({
    url: '/audit/logs',
    method: 'get',
    params
  })
}

// 更新当前用户信息
export const updateCurrentUser = (data) => {
  return request({
    url: '/users/profile',
    method: 'put',
    data
  })
}

export const getCurrentUser = () => {
  return request({
    url: '/users/profile',
    method: 'get'
  })
}

export const getModelList = (params) => {
  return request({
    url: '/models',
    method: 'get',
    params
  })
}

export const getTaskList = () => {
  return request({
    url: '/flgo/tasks',
    method: 'get'
  })
}

export const getTaskSuccessCount = () => {
  return request({
    url: '/stats/task-success-count',
    method: 'get'
  })
}

export const getAverageAccuracy = () => {
  return request({
    url: '/stats/average-accuracy',
    method: 'get'
  })
}

export const getTotalRecognitionCount = () => {
  return request({
    url: '/recognition-records/count',
    method: 'get'
  })
}

export const getTodayRecognitionCount = () => {
  return request({
    url: '/recognition-records/count-today',
    method: 'get'
  })
}

export const getDashboardStats = () => {
  return request({
    url: '/stats/dashboard',
    method: 'get'
  })
}

export const getRecognitionStats = () => {
  return request({
    url: '/stats/recognition',
    method: 'get'
  })
}

export const getTaskStatusStats = () => {
  return request({
    url: '/stats/task-status',
    method: 'get'
  })
}

export const getUserRoleStats = () => {
  return request({
    url: '/stats/user-roles',
    method: 'get'
  })
}

export const getTaskParticipations = (userId) => {
  return request({
    url: `/task-participations/user/${userId}`,
    method: 'get'
  })
}
