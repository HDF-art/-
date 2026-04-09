import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import VueLazyload from 'vue-lazyload'
import request from './utils/request'
import './assets/css/global.css'

Vue.prototype.$http = request
Vue.use(ElementUI)
Vue.use(VueLazyload, {
  preLoad: 1.3,
  error: '',
  loading: '',
  attempt: 1
})
Vue.config.productionTip = false

// 全局错误处理
Vue.config.errorHandler = function (err, vm, info) {
  console.error('全局错误:', err)
  console.error('错误信息:', info)
}

window.addEventListener('error', function (e) {
  console.error('资源加载错误:', e.target.src || e.target.href)
})

// 清除可能过大的token
const existingToken = localStorage.getItem('token')
if (existingToken && existingToken.length >= 10000) {
  console.warn('清除过大的token')
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')

console.log('Vue应用已启动')
