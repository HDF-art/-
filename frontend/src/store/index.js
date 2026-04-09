import Vue from 'vue'
import Vuex from 'vuex'
import { login, phoneLogin, getUserInfo } from '../api/user'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    user: null,
    token: null,
    isAuthenticated: false,
    loading: false,
    error: null
  },
  
  mutations: {
    SET_USER(state, user) {
      state.user = user
    },
    SET_TOKEN(state, token) {
      state.token = token
      state.isAuthenticated = !!token
    },
    SET_LOADING(state, status) {
      state.loading = status
    },
    SET_ERROR(state, error) {
      state.error = error
    },
    LOGOUT(state) {
      state.user = null
      state.token = null
      state.isAuthenticated = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  },
  
  actions: {
    // 登录操作
    async login({ commit }, credentials) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      try {
        const response = await login(credentials)
        
        if (response.code === 200) {
          // 根据后端实际返回的数据格式进行适配
          const token = response.data.token || response.data
          // 从登录响应中获取完整的用户信息
          const user = response.data.userInfo || response.data.user || { 
            role: 2, // 默认为二级管理员
            roleName: '二级管理员',
            username: 'admin'
          }
          
          // 保存token和用户信息到localStorage
          localStorage.setItem('token', token)
          localStorage.setItem('user', JSON.stringify(user))
          
          commit('SET_TOKEN', token)
          commit('SET_USER', user)
          
          return response
        } else {
          commit('SET_ERROR', response.message || '登录失败')
          throw new Error(response.message || '登录失败')
        }
      } catch (error) {
        commit('SET_ERROR', error.message || '登录失败')
        throw error
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 手机验证码登录
    async phoneLogin({ commit }, credentials) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      try {
        const response = await phoneLogin(credentials)
        
        if (response.code === 200) {
          // 根据后端实际返回的数据格式进行适配
          const token = response.data.token || response.data
          // 从登录响应中获取完整的用户信息
          const user = response.data.userInfo || response.data.user || { 
            role: 2, // 默认为二级管理员
            roleName: '二级管理员',
            username: 'admin'
          }
          
          // 保存token和用户信息到localStorage
          localStorage.setItem('token', token)
          localStorage.setItem('user', JSON.stringify(user))
          
          commit('SET_TOKEN', token)
          commit('SET_USER', user)
          
          return response
        } else {
          commit('SET_ERROR', response.message || '登录失败')
          throw new Error(response.message || '登录失败')
        }
      } catch (error) {
        commit('SET_ERROR', error.message || '登录失败')
        throw error
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 获取用户信息
    async fetchUserInfo({ commit, state }) {
      if (!state.token) return
      
      commit('SET_LOADING', true)
      
      try {
        const response = await getUserInfo()
        
        if (response.code === 200) {
          commit('SET_USER', response.data)
          // 更新localStorage中的用户信息
          localStorage.setItem('user', JSON.stringify(response.data))
        }
      } catch (error) {
        console.error('获取用户信息失败', error)
        // 如果获取用户信息失败，可能是token过期，执行登出操作
        commit('LOGOUT')
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 登出操作
    logout({ commit }) {
      commit('LOGOUT')
    }
  },
  
  getters: {
    isAuthenticated: state => state.isAuthenticated,
    currentUser: state => state.user,
    isLoading: state => state.loading,
    error: state => state.error,
    // 判断用户角色
    isAdmin1: state => state.user && (state.user.role === 'admin1' || state.user.role === 1),
    isAdmin2: state => state.user && (state.user.role === 'admin2' || state.user.role === 2),
    isUser: state => state.user && (state.user.role === 'user' || state.user.role === 3 || (typeof state.user.role === 'number' && state.user.role > 2))
  }
})