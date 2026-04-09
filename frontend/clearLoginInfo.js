// 清除所有登录相关信息的脚本
console.log('执行登录信息清理...');
localStorage.removeItem('token');
localStorage.removeItem('userInfo');
localStorage.removeItem('user');
console.log('登录信息已清理完成');
console.log('现在需要通过登录页面输入账号密码进行登录');