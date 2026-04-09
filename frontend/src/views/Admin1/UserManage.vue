<template>
  <div class="user-manage">
    <el-card>
      <div slot="header" class="card-header">
        <span>用户管理</span>
        <el-button type="primary" @click="handleAdd">添加用户</el-button>
      </div>
      <div class="search-section">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable></el-input>
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
              <el-option label="一级管理员" value="admin1"></el-option>
              <el-option label="二级管理员" value="admin2"></el-option>
              <el-option label="普通用户" value="user"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="role" label="角色" width="120">
          <template slot-scope="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="phone" label="手机号" width="130"></el-table-column>
        <el-table-column prop="organization" label="单位" width="200">
          <template slot-scope="scope">
            {{ scope.row.organization || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" style="color: #F56C6C;" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <el-dialog title="编辑用户" :visible.sync="editDialogVisible" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width: 100%;">
            <el-option label="一级管理员" value="admin1"></el-option>
            <el-option label="二级管理员" value="admin2"></el-option>
            <el-option label="普通用户" value="user"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="editForm.organization" placeholder="请输入单位名称"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, updateUser } from '@/api/user';

export default {
  name: 'UserManage',
  data() {
    return {
      loading: false,
      searchForm: {
        username: '',
        role: ''
      },
      userList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      editDialogVisible: false,
      editForm: {
        id: null,
        username: '',
        role: '',
        email: '',
        phone: '',
        organization: '',
        status: 1
      }
    }
  },
  created() {
    this.loadUsers();
  },
  methods: {
    async loadUsers() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          username: this.searchForm.username,
          role: this.searchForm.role
        };
        const response = await getUserList(params);
        if (response && response.data) {
          this.userList = response.data.list || [];
          this.total = response.data.total || 0;
        } else {
          this.userList = [];
          this.total = 0;
        }
      } catch (error) {
        this.$message.error('获取用户列表失败');
        this.userList = [];
        this.total = 0;
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.loadUsers()
    },
    resetSearch() {
      this.searchForm = { username: '', role: '' }
      this.currentPage = 1
      this.loadUsers()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.loadUsers()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.loadUsers()
    },
    getRoleType(role) {
      switch (role) {
        case 1: 
        case 'admin1': return 'primary'
        case 2: 
        case 'admin2': return 'success'
        case 3: 
        case 'user': return 'info'
        default: return 'info'
      }
    },
    getRoleText(role) {
      const roleMap = {
        1: '一级管理员',
        2: '二级管理员',
        3: '普通用户',
        'admin1': '一级管理员',
        'admin2': '二级管理员',
        'user': '普通用户'
      }
      return roleMap[role] || '未知角色'
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    },
    handleAdd() {
      this.$message.info('请使用注册页面添加新用户')
    },
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        username: row.username,
        role: row.role,
        email: row.email,
        phone: row.phone || '',
        organization: row.organization || '',
        status: row.status
      }
      this.editDialogVisible = true
    },
    async saveEdit() {
      try {
        await updateUser(this.editForm.id, this.editForm)
        this.$message.success('修改成功')
        this.editDialogVisible = false
        this.loadUsers()
      } catch (error) {
        this.$message.error(error.message || '修改失败')
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除用户 "${row.username}" 吗？`, '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const { deleteUser } = require('@/api/user')
        await deleteUser(row.id)
        this.$message.success('删除成功')
        this.loadUsers()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '删除失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.user-manage { padding: 10px 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.search-section { margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
