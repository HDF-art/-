import axios from '@/utils/axios'

// 创建任务
export const createTask = (data) => {
  return axios.post('/api/flgo/task/create', data)
}

// 获取所有任务
export const getTasks = () => {
  return axios.get('/api/flgo/tasks')
}

// 获取任务详情
export const getTaskDetail = (taskId) => {
  return axios.get(`/api/flgo/task/${taskId}`)
}

// 获取任务状态
export const getTaskStatus = (taskId) => {
  return axios.get(`/api/flgo/task/${taskId}/status`)
}

// 注册参与客户端
export const registerParticipant = (data) => {
  return axios.post('/api/flgo/participant/register', data)
}

// 选择服务器
export const selectServer = (data) => {
  return axios.post('/api/flgo/task/select-server', data)
}

// 启动任务
export const startTask = (taskId) => {
  return axios.post(`/api/flgo/task/${taskId}/start`)
}

// 停止任务
export const stopTask = (taskId) => {
  return axios.post(`/api/flgo/task/${taskId}/stop`)
}

// 获取服务器配置
export const getServerConfig = () => {
  return axios.get('/api/flgo/config/server')
}

// 配置服务器IP
export const configServer = (data) => {
  return axios.post('/api/flgo/config/server', data)
}

// 获取所有客户端状态（一级管理员）
export const getAllClientStatus = (taskId) => {
  return axios.get(`/api/flgo/status/task/${taskId}/all`)
}

// 获取指定客户端状态
export const getClientStatus = (taskId, clientIp) => {
  return axios.get(`/api/flgo/status/task/${taskId}/client/${clientIp}`)
}

// 客户端上报状态
export const reportStatus = (data) => {
  return axios.post('/api/flgo/status/report', data)
}

// 获取用户任务
export const getUserTasks = (userId) => {
  return axios.get(`/api/flgo/status/user/${userId}`)
}

// 获取训练历史
export const getTrainingHistory = () => {
  return axios.get('/api/flgo/status/history')
}
