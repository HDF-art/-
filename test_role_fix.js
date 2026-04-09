#!/usr/bin/env node

/**
 * 测试角色修复功能的脚本
 * 验证admin/admin123登录后是否正确显示为二级管理员
 */

const fs = require('fs');
const path = require('path');

console.log('='.repeat(50));
console.log('农业大数据联合建模平台角色修复测试');
console.log('='.repeat(50));

// 测试1: 检查数据库初始化脚本
console.log('\n1. 检查数据库初始化脚本...');
const initSqlPath = path.join(__dirname, 'backend', 'src', 'main', 'resources', 'init.sql');
if (fs.existsSync(initSqlPath)) {
    const initSql = fs.readFileSync(initSqlPath, 'utf8');
    const adminInsert = initSql.match(/INSERT INTO `user`.*?'admin'.*?(\d+).*?1\)/);
    if (adminInsert) {
        const role = adminInsert[1];
        console.log(`✓ 数据库中admin用户的role值: ${role}`);
        if (role === '2') {
            console.log('✓ 数据库配置正确，admin用户为二级管理员');
        } else {
            console.log('✗ 数据库配置不正确，admin用户role值应为2');
        }
    } else {
        console.log('✗ 未找到admin用户的插入语句');
    }
} else {
    console.log('✗ 未找到数据库初始化脚本');
}

// 测试2: 检查后端UserController修复
console.log('\n2. 检查后端UserController修复...');
const userControllerPath = path.join(__dirname, 'backend', 'src', 'main', 'java', 'com', 'agri', 'controller', 'UserController.java');
if (fs.existsSync(userControllerPath)) {
    const userController = fs.readFileSync(userControllerPath, 'utf8');
    
    // 检查是否添加了HttpServletRequest参数
    if (userController.includes('getCurrentUserInfo(HttpServletRequest request)')) {
        console.log('✓ UserController已添加HttpServletRequest参数');
    } else {
        console.log('✗ UserController未正确添加HttpServletRequest参数');
    }
    
    // 检查是否添加了默认管理员创建方法
    if (userController.includes('createDefaultAdminUserInfo()')) {
        console.log('✓ UserController已添加createDefaultAdminUserInfo方法');
    } else {
        console.log('✗ UserController未添加createDefaultAdminUserInfo方法');
    }
    
    // 检查默认用户信息是否正确设置
    if (userController.includes('role: 2') && userController.includes('二级管理员')) {
        console.log('✓ 默认用户信息正确设置为二级管理员');
    } else {
        console.log('✗ 默认用户信息未正确设置');
    }
} else {
    console.log('✗ 未找到UserController文件');
}

// 测试3: 检查JWT工具类修复
console.log('\n3. 检查JWT工具类修复...');
const jwtUtilsPath = path.join(__dirname, 'backend', 'src', 'main', 'java', 'com', 'agri', 'utils', 'JwtUtils.java');
if (fs.existsSync(jwtUtilsPath)) {
    const jwtUtils = fs.readFileSync(jwtUtilsPath, 'utf8');
    
    // 检查默认角色是否设置为"2"
    const defaultRoleMatches = (jwtUtils.match(/role.*?["']2["']/g) || []).length;
    if (defaultRoleMatches >= 4) { // 应该在4个地方设置默认角色为"2"
        console.log('✓ JWT工具类默认角色已正确设置为"2"');
    } else {
        console.log(`✗ JWT工具类默认角色设置不完整，找到${defaultRoleMatches}处设置`);
    }
    
    // 检查getRoleFromToken方法
    if (jwtUtils.includes('getRoleFromToken') && jwtUtils.includes('return (String) claims.getOrDefault("role", "2")')) {
        console.log('✓ getRoleFromToken方法已正确设置默认值为"2"');
    } else {
        console.log('✗ getRoleFromToken方法未正确设置默认值');
    }
} else {
    console.log('✗ 未找到JwtUtils文件');
}

// 测试4: 检查前端Vuex Store修复
console.log('\n4. 检查前端Vuex Store修复...');
const storePath = path.join(__dirname, 'frontend', 'src', 'store', 'index.js');
if (fs.existsSync(storePath)) {
    const store = fs.readFileSync(storePath, 'utf8');
    
    // 检查登录响应处理
    if (store.includes('response.data.userInfo') && store.includes('role: 2')) {
        console.log('✓ Vuex Store已正确处理登录响应中的用户信息');
    } else {
        console.log('✗ Vuex Store未正确处理登录响应');
    }
    
    // 检查角色判断逻辑
    if (store.includes('state.user.role === 2') && store.includes('state.user.role === 1')) {
        console.log('✓ Vuex Store角色判断逻辑已支持数字格式');
    } else {
        console.log('✗ Vuex Store角色判断逻辑未正确更新');
    }
} else {
    console.log('✗ 未找到Vuex Store文件');
}

// 测试5: 检查前端角色映射修复
console.log('\n5. 检查前端角色映射修复...');

// 检查Profile.vue
const profilePath = path.join(__dirname, 'frontend', 'src', 'views', 'User', 'Profile.vue');
if (fs.existsSync(profilePath)) {
    const profile = fs.readFileSync(profilePath, 'utf8');
    if (profile.includes('1: \'一级管理员\'') && profile.includes('2: \'二级管理员\'')) {
        console.log('✓ Profile.vue角色映射已更新支持数字格式');
    } else {
        console.log('✗ Profile.vue角色映射未正确更新');
    }
}

// 检查Home.vue
const homePath = path.join(__dirname, 'frontend', 'src', 'views', 'Home.vue');
if (fs.existsSync(homePath)) {
    const home = fs.readFileSync(homePath, 'utf8');
    if (home.includes('effectiveUser()') && home.includes('role: 2')) {
        console.log('✓ Home.vue默认用户角色已设置为二级管理员');
    }
    if (home.includes('1: \'一级管理员\'') && home.includes('2: \'二级管理员\'')) {
        console.log('✓ Home.vue角色映射已更新支持数字格式');
    }
}

// 检查UserManage.vue
const userManagePath = path.join(__dirname, 'frontend', 'src', 'views', 'Admin1', 'UserManage.vue');
if (fs.existsSync(userManagePath)) {
    const userManage = fs.readFileSync(userManagePath, 'utf8');
    if (userManage.includes('role: 2') && userManage.includes('role: 1') && userManage.includes('role: 3')) {
        console.log('✓ UserManage.vue用户列表已更新为数字格式角色');
    }
    if (userManage.includes('1: \'一级管理员\'') && userManage.includes('2: \'二级管理员\'')) {
        console.log('✓ UserManage.vue角色映射已更新支持数字格式');
    }
}

console.log('\n' + '='.repeat(50));
console.log('测试完成！');
console.log('请使用admin/admin123登录系统验证：');
console.log('1. 登录成功后，用户角色应显示为"二级管理员"');
console.log('2. 侧边栏和用户信息页面应正确显示角色信息');
console.log('3. 应能正常访问管理员功能页面');
console.log('='.repeat(50));