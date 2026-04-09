// 认证状态调试辅助工具

// 检查localStorage中的认证信息
export function checkAuthStorage() {
  console.log('======= 认证状态调试 =======');
  console.log('localStorage中的token:', localStorage.getItem('token'));
  console.log('localStorage中的user:', localStorage.getItem('user'));
  console.log('token存在:', !!localStorage.getItem('token'));
  console.log('user存在:', !!localStorage.getItem('user'));
  
  if (localStorage.getItem('user')) {
    try {
      const user = JSON.parse(localStorage.getItem('user'));
      console.log('解析后的user对象:', user);
      console.log('用户角色:', user.role);
    } catch (error) {
      console.error('user信息解析失败:', error);
    }
  }
}

// 设置测试登录信息
export function setTestAuth() {
  console.log('设置测试登录信息...');
  const testToken = 'test_token_123456';
  const testUser = { role: 'admin1', username: 'test_admin' };
  
  localStorage.setItem('token', testToken);
  localStorage.setItem('user', JSON.stringify(testUser));
  
  console.log('测试登录信息已设置');
  checkAuthStorage();
}

// 在页面上添加调试按钮
export function addDebugButtons() {
  const debugDiv = document.createElement('div');
  debugDiv.style.position = 'fixed';
  debugDiv.style.top = '10px';
  debugDiv.style.right = '10px';
  debugDiv.style.zIndex = '9999';
  debugDiv.style.background = 'white';
  debugDiv.style.padding = '10px';
  debugDiv.style.border = '1px solid #ccc';
  debugDiv.style.borderRadius = '5px';
  
  const checkButton = document.createElement('button');
  checkButton.innerText = '检查登录状态';
  checkButton.onclick = checkAuthStorage;
  checkButton.style.marginRight = '5px';
  
  const setButton = document.createElement('button');
  setButton.innerText = '设置测试登录';
  setButton.onclick = setTestAuth;
  setButton.style.marginRight = '5px';
  
  const clearButton = document.createElement('button');
  clearButton.innerText = '清除登录信息';
  clearButton.onclick = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    console.log('登录信息已清除');
    checkAuthStorage();
  };
  
  debugDiv.appendChild(checkButton);
  debugDiv.appendChild(setButton);
  debugDiv.appendChild(clearButton);
  
  document.body.appendChild(debugDiv);
  console.log('调试按钮已添加到页面');
}

// 监听路由变化，打印认证状态
export function watchRouteChanges(router) {
  router.afterEach((to, from) => {
    console.log(`\n路由变化: ${from.path} -> ${to.path}`);
    checkAuthStorage();
  });
}