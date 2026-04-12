import axios from 'axios'
import router from '../router'

// 创建一个全新的、干净的axios实例
// 完全不添加任何可能导致请求头过大的内容
const service = axios.create({
  baseURL: '/api',
  timeout: 60000, // 增加超时时间到60秒，避免登录请求超时
  // 明确设置不自动携带凭证，避免额外的头部信息
  withCredentials: false
})

// 请求拦截器 - 添加认证信息
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 根据后端定义的响应格式进行处理
    if (res.code !== 200) {
      console.error('请求失败:', res.message || '未知错误')
      
      // 处理特定错误码
      if (res.code === 401) {
        // 未授权，清除token并跳转到登录页
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        if (router.currentRoute.path !== '/login') {
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.fullPath }
          })
        }
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error) // 这是错误信息中提到的第48行左右
    
    // 处理网络错误等
    let errorMessage = '网络错误，请稍后重试'
    
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 400:
          errorMessage = data.message || '请求参数错误'
          break
        case 401:
          errorMessage = '未授权，请重新登录'
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          if (router.currentRoute.path !== '/login') {
            router.push({
              path: '/login',
              query: { redirect: router.currentRoute.fullPath }
            })
          }
          break
        case 431:
          errorMessage = '请求头过大，请清除缓存后重试'
          console.warn('检测到431错误，彻底清除所有可能的数据')
          // 更彻底地清除所有可能的数据
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          sessionStorage.removeItem('token')
          // 强制刷新页面，确保所有缓存都被清除
          window.location.reload()
          break
        case 403:
          errorMessage = '拒绝访问'
          break
        case 404:
          errorMessage = '请求的资源不存在'
          break
        case 500:
          errorMessage = '服务器内部错误'
          break
        default:
          errorMessage = data.message || '请求失败'
      }
    } else if (error.request) {
      errorMessage = '网络连接失败，请检查网络'
    }
    
    return Promise.reject(new Error(errorMessage))
  }
)

export default service