// 调试帮助函数，用于检查用户认证状态和事件处理

// 检查localStorage中的用户信息
export function checkLocalStorage() {
  console.log('===== LocalStorage 状态检查 =====');
  console.log('Token存在:', !!localStorage.getItem('token'));
  console.log('User存在:', !!localStorage.getItem('user'));
  
  try {
    const user = localStorage.getItem('user');
    if (user) {
      console.log('User内容:', JSON.parse(user));
    }
  } catch (error) {
    console.error('解析User失败:', error);
  }
}

// 注入全局点击事件监听器
export function setupGlobalClickDebugger() {
  document.addEventListener('click', (e) => {
    console.log('点击元素:', e.target);
    console.log('元素类名:', e.target.className);
    console.log('元素ID:', e.target.id);
    
    // 特别关注菜单和按钮点击
    if (e.target.closest('.el-menu-item') || e.target.closest('.el-dropdown-item')) {
      console.log('菜单/下拉项被点击:', e.target.closest('.el-menu-item, .el-dropdown-item'));
    }
  }, true); // 使用捕获阶段
}

// 检查Vuex状态
export function checkVuexState(store) {
  console.log('===== Vuex 状态检查 =====');
  console.log('Store状态:', {
    isAuthenticated: store.state.isAuthenticated,
    user: store.state.user,
    token: store.state.token
  });
}

// 修复Home组件中的事件处理
export function patchHomeEvents() {
  // 为退出登录按钮添加直接的点击事件
  setTimeout(() => {
    const logoutBtn = document.querySelector('.el-dropdown-item[divided]');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', (e) => {
        console.log('退出登录按钮直接点击事件触发');
        if (confirm('确定要退出登录吗？')) {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          window.location.href = '/login';
        }
      }, true);
    }
    
    // 为菜单添加直接的点击事件
    const menuItems = document.querySelectorAll('.el-menu-item');
    menuItems.forEach(item => {
      item.addEventListener('click', (e) => {
        const path = item.getAttribute('index');
        if (path && path !== window.location.pathname) {
          console.log('菜单直接点击事件触发，路径:', path);
          window.location.href = path;
        }
      }, true);
    });
  }, 1000);
}