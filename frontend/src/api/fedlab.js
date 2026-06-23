import request from '../utils/request'

// 创建 FedLab 任务
export const createTask = (data) => {
  return request({
    url: '/fedlab/task/create',
    method: 'post',
    data
  })
}

// 获取所有 FedLab 任务
export const getTasks = () => {
  return request({
    url: '/fedlab/tasks',
    method: 'get'
  })
}

// 获取 FedLab 任务详情
export const getTaskDetail = (taskId) => {
  return request({
    url: `/fedlab/task/${taskId}`,
    method: 'get'
  })
}

// 获取 FedLab 任务状态
export const getTaskStatus = (taskId) => {
  return request({
    url: `/fedlab/task/${taskId}/status`,
    method: 'get'
  })
}

// 注册参与客户端
export const registerParticipant = (data) => {
  return request({
    url: '/fedlab/participant/register',
    method: 'post',
    data
  })
}

// 启动 FedLab 任务
export const startTask = (taskId) => {
  return request({
    url: `/fedlab/task/${taskId}/start`,
    method: 'post'
  })
}

// 停止 FedLab 任务
export const stopTask = (taskId) => {
  return request({
    url: `/fedlab/task/${taskId}/stop`,
    method: 'post'
  })
}

// 获取任务参与者列表
export const getParticipants = (taskId) => {
  return request({
    url: `/fedlab/task/${taskId}/participants`,
    method: 'get'
  })
}

// 下载客户端配置脚本
export const downloadScript = (taskId, clientIp) => {
  let url = `/fedlab/task/${taskId}/download-script`
  if (clientIp) {
    url += `?clientIp=${encodeURIComponent(clientIp)}`
  }
  return request({
    url: url,
    method: 'get'
  })
}

// 删除 FedLab 任务
export const deleteTask = (taskId) => {
  return request({
    url: `/fedlab/task/${taskId}`,
    method: 'delete'
  })
}
