// 认证状态调试工具
console.log('======= 认证状态调试 =======');

// 检查localStorage中的token和用户信息
function checkAuthStatus() {
  console.log('1. localStorage内容:');
  const token = localStorage.getItem('token');
  const userInfo = localStorage.getItem('userInfo');
  const rememberUser = localStorage.getItem('rememberUser');
  const rememberPwd = localStorage.getItem('rememberPwd');
  
  console.log('- token存在:', !!token);
  console.log('- token长度:', token ? token.length : 0);
  console.log('- 用户信息:', userInfo ? JSON.parse(userInfo) : '无');
  console.log('- 记住用户名:', rememberUser);
  console.log('- 记住密码:', rememberPwd);
  
  // 检查Vuex状态
  if (window.__VUE_APP__) {
    const store = window.__VUE_APP__.$store;
    console.log('\n2. Vuex认证状态:');
    console.log('- isAuthenticated:', store.getters.isAuthenticated);
    console.log('- currentUser:', store.getters.currentUser);
    console.log('- token:', store.state.token);
    console.log('- user:', store.state.user);
    
    // 检查角色权限
    console.log('\n3. 用户角色状态:');
    console.log('- isAdmin1:', store.getters.isAdmin1);
    console.log('- isAdmin2:', store.getters.isAdmin2);
    console.log('- isUser:', store.getters.isUser);
  }
  
  // 检查路由守卫是否正常工作
  console.log('\n4. 路由信息:');
  if (window.__VUE_APP__ && window.__VUE_APP__.$route) {
    const route = window.__VUE_APP__.$route;
    console.log('- 当前路径:', route.path);
    console.log('- 路由名称:', route.name);
    console.log('- 路由元信息:', route.meta);
  }
  
  // 检查认证相关的网络请求
  console.log('\n5. 认证请求检查:');
  console.log('- 检查axios拦截器是否正常工作');
}

// 尝试模拟登录以测试认证流程
function testLogin(username = 'admin', password = 'admin') {
  console.log(`\n6. 尝试模拟登录 (${username}/${password}):`);
  
  if (window.__VUE_APP__ && window.__VUE_APP__.$store) {
    console.log('- 调用登录action...');
    try {
      // 这里不实际调用，只显示信息
      console.log('- 登录请求将发送到后端');
      console.log('- 成功后应设置token并更新Vuex状态');
    } catch (error) {
      console.error('- 登录测试失败:', error);
    }
  }
}

// 导出调试函数
export { checkAuthStatus, testLogin };